package org.apache.continuum.buildagent.build.execution;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.installation.BuildAgentInstallationService;
import org.apache.continuum.buildagent.manager.BuildAgentManager;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.utils.shell.ExecutionResult;
import org.apache.continuum.utils.shell.ShellCommandHelper;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.commandline.ExecutableResolver;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBuildExecutor
    implements ContinuumAgentBuildExecutor, Initializable
{
    protected static final Logger log = LoggerFactory.getLogger( AbstractBuildExecutor.class );

    /**
     * @plexus.requirement
     */
    private ShellCommandHelper shellCommandHelper;

    /**
     * @plexus.requirement
     */
    private ExecutableResolver executableResolver;

    /**
     * @plexus.requirement
     */
    private BuildAgentConfigurationService buildAgentConfigurationService;

    /**
     * @plexus.requirement
     */
    private BuildAgentInstallationService buildAgentInstallationService;

    /**
     * @plexus.configuration
     */
    private String defaultExecutable;

    /**
     * @plexus.requirement
     */
    private BuildAgentManager buildAgentManager;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private final String id;

    private boolean resolveExecutable;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected AbstractBuildExecutor( String id, boolean resolveExecutable )
    {
        this.id = id;

        this.resolveExecutable = resolveExecutable;
    }

    public void setShellCommandHelper( ShellCommandHelper shellCommandHelper )
    {
        this.shellCommandHelper = shellCommandHelper;
    }

    public ShellCommandHelper getShellCommandHelper()
    {
        return shellCommandHelper;
    }

    public void setDefaultExecutable( String defaultExecutable )
    {
        this.defaultExecutable = defaultExecutable;
    }

    public BuildAgentConfigurationService getBuildAgentConfigurationService()
    {
        return buildAgentConfigurationService;
    }

    public void setBuildAgentConfigurationService( BuildAgentConfigurationService buildAgentConfigurationService )
    {
        this.buildAgentConfigurationService = buildAgentConfigurationService;
    }

    public BuildAgentInstallationService getBuildAgentInstallationService()
    {
        return buildAgentInstallationService;
    }

    public void setBuildAgentInstallationService( BuildAgentInstallationService buildAgentInstallationService )
    {
        this.buildAgentInstallationService = buildAgentInstallationService;
    }

    public BuildAgentManager getBuildAgentManager()
    {
        return buildAgentManager;
    }

    public void setBuildAgentManager( BuildAgentManager buildAgentManager )
    {
        this.buildAgentManager = buildAgentManager;
    }

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public String getDefaultExecutable()
    {
        return defaultExecutable;
    }

    public void initialize()
        throws InitializationException
    {
        List path = executableResolver.getDefaultPath();

        if ( resolveExecutable )
        {
            if ( StringUtils.isEmpty( defaultExecutable ) )
            {
                log.warn( "The default executable for build executor '" + id + "' is not set. " +
                    "This will cause a problem unless the project has a executable configured." );
            }
            else
            {
                File resolvedExecutable = executableResolver.findExecutable( defaultExecutable, path );

                if ( resolvedExecutable == null )
                {
                    log.warn(
                        "Could not find the executable '" + defaultExecutable + "' in the " + "path '" + path + "'." );
                }
                else
                {
                    log.info( "Resolved the executable '" + defaultExecutable + "' to " + "'" +
                        resolvedExecutable.getAbsolutePath() + "'." );
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * Find the actual executable path to be used
     *
     * @param defaultExecutable
     * @return The executable path
     */
    protected String findExecutable( String executable, String defaultExecutable, boolean resolveExecutable,
                                     File workingDirectory )
    {
        // ----------------------------------------------------------------------
        // If we're not searching the path for the executable, prefix the
        // executable with the working directory to make sure the path is
        // absolute and thus won't be tried resolved by using the PATH
        // ----------------------------------------------------------------------

        String actualExecutable;

        if ( !resolveExecutable )
        {
            actualExecutable = new File( workingDirectory, executable ).getAbsolutePath();
        }
        else
        {
            List<String> path = executableResolver.getDefaultPath();

            if ( StringUtils.isEmpty( executable ) )
            {
                executable = defaultExecutable;
            }

            File e = executableResolver.findExecutable( executable, path );

            if ( e == null )
            {
                log.debug( "Could not find the executable '" + executable + "' in this path: " );

                for ( String element : path )
                {
                    log.debug( element );
                }

                actualExecutable = defaultExecutable;
            }
            else
            {
                actualExecutable = e.getAbsolutePath();
            }
        }

        //sometimes executable isn't found in path but it exit (CONTINUUM-365)
        File actualExecutableFile = new File( actualExecutable );

        if ( !actualExecutableFile.exists() )
        {
            actualExecutable = executable;
        }

        return actualExecutable;
    }

    protected ContinuumAgentBuildExecutionResult executeShellCommand( Project project, String executable,
                                                                      String arguments, File output,
                                                                      Map<String, String> environments )
        throws ContinuumAgentBuildExecutorException, ContinuumAgentBuildCancelledException
    {

        File workingDirectory = getWorkingDirectory( project.getId() );

        String actualExecutable = findExecutable( executable, defaultExecutable, resolveExecutable, workingDirectory );

        // ----------------------------------------------------------------------
        // Execute the build
        // ----------------------------------------------------------------------

        try
        {
            ExecutionResult result =
                getShellCommandHelper().executeShellCommand( workingDirectory, actualExecutable, arguments, output,
                                                             project.getId(), environments );

            log.info( "Exit code: " + result.getExitCode() );

            return new ContinuumAgentBuildExecutionResult( output, result.getExitCode() );
        }
        catch ( CommandLineException e )
        {
            if ( e.getCause() instanceof InterruptedException )
            {
                throw new ContinuumAgentBuildCancelledException( "The build was cancelled", e );
            }
            else
            {
                throw new ContinuumAgentBuildExecutorException(
                    "Error while executing shell command. The most common error is that '" + executable + "' " +
                        "is not in your path.", e );
            }
        }
        catch ( Exception e )
        {
            throw new ContinuumAgentBuildExecutorException(
                "Error while executing shell command. " + "The most common error is that '" + executable + "' " +
                    "is not in your path.", e );
        }
    }

    protected Properties getContinuumSystemProperties( Project project )
    {
        Properties properties = new Properties();
        properties.setProperty( "continuum.project.group.name", project.getProjectGroup().getName() );
        properties.setProperty( "continuum.project.lastBuild.state", String.valueOf( project.getOldState() ) );
        properties.setProperty( "continuum.project.lastBuild.number", String.valueOf( project.getBuildNumber() ) );
        properties.setProperty( "continuum.project.nextBuild.number", String.valueOf( project.getBuildNumber() + 1 ) );
        properties.setProperty( "continuum.project.id", String.valueOf( project.getId() ) );
        properties.setProperty( "continuum.project.name", project.getName() );
        properties.setProperty( "continuum.project.version", project.getVersion() );
        return properties;
    }

    protected String getBuildFileForProject( BuildDefinition buildDefinition )
    {
        return StringUtils.clean( buildDefinition.getBuildFile() );
    }

    protected void updateProject( Project project )
        throws ContinuumAgentBuildExecutorException
    {
        Map<String, Object> projectMap = new HashMap<String, Object>();

        projectMap.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, project.getId() );
        if ( StringUtils.isNotEmpty( project.getVersion() ) )
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_PROJECT_VERSION, project.getVersion() );
        }
        else
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_PROJECT_VERSION, "" );
        }
        if ( StringUtils.isNotEmpty( project.getArtifactId() ) )
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_ARTIFACT_ID, project.getArtifactId() );
        }
        else
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_ARTIFACT_ID, "" );
        }
        if ( StringUtils.isNotEmpty( project.getGroupId() ) )
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_GROUP_ID, project.getGroupId() );
        }
        else
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_GROUP_ID, "" );
        }
        if ( StringUtils.isNotEmpty( project.getName() ) )
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_PROJECT_NAME, project.getName() );
        }
        else
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_PROJECT_NAME, "" );
        }
        if ( StringUtils.isNotEmpty( project.getDescription() ) )
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_PROJECT_DESCRIPTION, project.getDescription() );
        }
        else
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_PROJECT_DESCRIPTION, "" );
        }
        if ( StringUtils.isNotEmpty( project.getScmUrl() ) )
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_SCM_URL, project.getScmUrl() );
        }
        else
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_SCM_URL, "" );
        }
        if ( StringUtils.isNotEmpty( project.getScmTag() ) )
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_SCM_TAG, project.getScmTag() );
        }
        else
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_SCM_TAG, "" );
        }
        if ( StringUtils.isNotEmpty( project.getUrl() ) )
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_PROJECT_URL, project.getUrl() );
        }
        else
        {
            projectMap.put( ContinuumBuildAgentUtil.KEY_PROJECT_URL, "" );
        }
        projectMap.put( ContinuumBuildAgentUtil.KEY_PROJECT_PARENT, getProjectParent( project.getParent() ) );
        projectMap.put( ContinuumBuildAgentUtil.KEY_PROJECT_DEVELOPERS,
                        getProjectDevelopers( project.getDevelopers() ) );
        projectMap.put( ContinuumBuildAgentUtil.KEY_PROJECT_DEPENDENCIES,
                        getProjectDependencies( project.getDependencies() ) );
        projectMap.put( ContinuumBuildAgentUtil.KEY_PROJECT_NOTIFIERS, getProjectNotifiers( project.getNotifiers() ) );

        try
        {
            buildAgentManager.updateProject( projectMap );
        }
        catch ( Exception e )
        {
            throw new ContinuumAgentBuildExecutorException( "Failed to update project", e );
        }
    }

    protected List<Map<String, String>> getProjectDevelopers( List<ProjectDeveloper> developers )
    {
        List<Map<String, String>> pDevelopers = new ArrayList<Map<String, String>>();

        if ( developers != null )
        {
            for ( ProjectDeveloper developer : developers )
            {
                Map<String, String> map = new HashMap<String, String>();
                map.put( ContinuumBuildAgentUtil.KEY_PROJECT_DEVELOPER_NAME, developer.getName() );
                if ( StringUtils.isNotEmpty( developer.getEmail() ) )
                {
                	map.put( ContinuumBuildAgentUtil.KEY_PROJECT_DEVELOPER_EMAIL, developer.getEmail() );
                }
                else
                {
                	map.put( ContinuumBuildAgentUtil.KEY_PROJECT_DEVELOPER_EMAIL, "" );
                }
                if ( StringUtils.isNotEmpty( developer.getScmId() ) )
                {
                	map.put( ContinuumBuildAgentUtil.KEY_PROJECT_DEVELOPER_SCMID, developer.getScmId() );
                }
                else
                {
                    map.put( ContinuumBuildAgentUtil.KEY_PROJECT_DEVELOPER_SCMID, "" );
                }

                pDevelopers.add( map );
            }
        }
        return pDevelopers;
    }

    protected Map<String, Object> getProjectParent( ProjectDependency parent )
    {
        Map<String, Object> map = new HashMap<String, Object>();

        if ( parent != null )
        {
            if ( StringUtils.isNotEmpty( parent.getGroupId() ) )
            {
                map.put( ContinuumBuildAgentUtil.KEY_GROUP_ID, parent.getGroupId() );
            }
            else
            {
                map.put( ContinuumBuildAgentUtil.KEY_GROUP_ID, "" );
            }
            if ( StringUtils.isNotEmpty( parent.getArtifactId() ) )
            {
                map.put( ContinuumBuildAgentUtil.KEY_ARTIFACT_ID, parent.getArtifactId() );
            }
            else
            {
                map.put( ContinuumBuildAgentUtil.KEY_ARTIFACT_ID, "" );
            }
            if ( StringUtils.isNotEmpty( parent.getVersion() ) )
            {
                map.put( ContinuumBuildAgentUtil.KEY_PROJECT_VERSION, parent.getVersion() );
            }
            else
            {
                map.put( ContinuumBuildAgentUtil.KEY_PROJECT_VERSION, "" );
            }
        }
        return map;
    }

    protected List<Map<String, Object>> getProjectDependencies( List<ProjectDependency> dependencies )
    {
        List<Map<String, Object>> pDependencies = new ArrayList<Map<String, Object>>();

        if ( dependencies != null )
        {
            for ( ProjectDependency dependency : dependencies )
            {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put( ContinuumBuildAgentUtil.KEY_GROUP_ID, dependency.getGroupId() );
                map.put( ContinuumBuildAgentUtil.KEY_ARTIFACT_ID, dependency.getArtifactId() );
                if ( StringUtils.isNotBlank( dependency.getVersion() ) )
                {
                    map.put( ContinuumBuildAgentUtil.KEY_PROJECT_VERSION, dependency.getVersion() );
                }
                else
                {
                    map.put( ContinuumBuildAgentUtil.KEY_PROJECT_VERSION, "" );
                }

                pDependencies.add( map );
            }
        }
        return pDependencies;
    }

    protected List<Map<String, Object>> getProjectNotifiers( List<ProjectNotifier> notifiers )
    {
        List<Map<String, Object>> pNotifiers = new ArrayList<Map<String, Object>>();

        if ( notifiers != null )
        {
            for ( ProjectNotifier notifier : notifiers )
            {
                Map<String, Object> map = new HashMap<String, Object>();

                if ( notifier.getConfiguration() != null )
                {
                    map.put( ContinuumBuildAgentUtil.KEY_NOTIFIER_CONFIGURATION, notifier.getConfiguration() );
                }
                else
                {
                    map.put( ContinuumBuildAgentUtil.KEY_NOTIFIER_CONFIGURATION, "" );
                }
                if ( StringUtils.isNotBlank( notifier.getType() ) )
                {
                    map.put( ContinuumBuildAgentUtil.KEY_NOTIFIER_TYPE, notifier.getType() );
                }
                else
                {
                    map.put( ContinuumBuildAgentUtil.KEY_NOTIFIER_TYPE, "" );
                }
                map.put( ContinuumBuildAgentUtil.KEY_NOTIFIER_FROM, notifier.getFrom() );
                map.put( ContinuumBuildAgentUtil.KEY_NOTIFIER_RECIPIENT_TYPE, notifier.getRecipientType() );
                map.put( ContinuumBuildAgentUtil.KEY_NOTIFIER_ENABLED, notifier.isEnabled() );
                map.put( ContinuumBuildAgentUtil.KEY_NOTIFIER_SEND_ON_ERROR, notifier.isSendOnError() );
                map.put( ContinuumBuildAgentUtil.KEY_NOTIFIER_SEND_ON_SUCCESS, notifier.isSendOnSuccess() );
                map.put( ContinuumBuildAgentUtil.KEY_NOTIFIER_SEND_ON_FAILURE, notifier.isSendOnFailure() );
                map.put( ContinuumBuildAgentUtil.KEY_NOTIFIER_SEND_ON_SCMFAILURE, notifier.isSendOnScmFailure() );
                map.put( ContinuumBuildAgentUtil.KEY_NOTIFIER_SEND_ON_WARNING, notifier.isSendOnWarning() );
                pNotifiers.add(map);
            }
        }
        return pNotifiers;
    }

    public boolean isBuilding( Project project )
    {
        return project.getState() == ContinuumProjectState.BUILDING ||
            getShellCommandHelper().isRunning( project.getId() );
    }

    public void killProcess( Project project )
    {
        getShellCommandHelper().killProcess( project.getId() );
    }

    public List<Artifact> getDeployableArtifacts( Project project, File workingDirectory,
                                                  BuildDefinition buildDefinition )
        throws ContinuumAgentBuildExecutorException
    {
        // Not supported by this builder
        return Collections.EMPTY_LIST;
    }

    public MavenProject getMavenProject( File workingDirectory, BuildDefinition buildDefinition )
        throws ContinuumAgentBuildExecutorException
    {
        return null;
    }

    public File getWorkingDirectory( int projectId )
    {
        return getBuildAgentConfigurationService().getWorkingDirectory( projectId );
    }

    public boolean isResolveExecutable()
    {
        return resolveExecutable;
    }

    public void setResolveExecutable( boolean resolveExecutable )
    {
        this.resolveExecutable = resolveExecutable;
    }

    public void setExecutableResolver( ExecutableResolver executableResolver )
    {
        this.executableResolver = executableResolver;
    }

    public ExecutableResolver getExecutableResolver()
    {
        return executableResolver;
    }
}
