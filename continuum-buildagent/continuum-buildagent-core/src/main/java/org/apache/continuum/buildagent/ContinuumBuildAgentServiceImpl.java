package org.apache.continuum.buildagent;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.manager.BuildAgentReleaseManager;
import org.apache.continuum.buildagent.model.Installation;
import org.apache.continuum.buildagent.taskqueue.PrepareBuildProjectsTask;
import org.apache.continuum.buildagent.taskqueue.manager.BuildAgentTaskQueueManager;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.buildagent.utils.WorkingCopyContentGenerator;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionInfo;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @plexus.component role="org.apache.continuum.buildagent.ContinuumBuildAgentService"
 */
public class ContinuumBuildAgentServiceImpl
    implements ContinuumBuildAgentService
{
    private static final Logger log = LoggerFactory.getLogger( ContinuumBuildAgentServiceImpl.class );

    /**
     * @plexus.requirement
     */
    private BuildAgentConfigurationService buildAgentConfigurationService;

    /**
     * @plexus.requirement
     */
    private BuildAgentTaskQueueManager buildAgentTaskQueueManager;

    /**
     * @plexus.requirement
     */
    private BuildContextManager buildContextManager;

    /**
     * @plexus.requirement
     */
    private WorkingCopyContentGenerator generator;

    /**
     * @plexus.requirement
     */
    private BuildAgentReleaseManager buildAgentReleaseManager;

    public void buildProjects( List<Map<String, Object>> projectsBuildContext )
        throws ContinuumBuildAgentException
    {
        List<BuildContext> buildContextList = initializeBuildContext( projectsBuildContext );

        PrepareBuildProjectsTask task = createPrepareBuildProjectsTask( buildContextList );

        if ( task == null )
        {
            return;
        }

        try
        {
            buildAgentTaskQueueManager.getPrepareBuildQueue().put( task );
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumBuildAgentException( "Error while enqueuing projects", e );
        }

    }

    public List<Map<String, String>> getAvailableInstallations()
        throws ContinuumBuildAgentException
    {
        List<Map<String, String>> installationsList = new ArrayList<Map<String, String>>();

        List<Installation> installations = buildAgentConfigurationService.getAvailableInstallations();

        for ( Installation installation : installations )
        {
            Map<String, String> map = new HashMap<String, String>();

            if ( StringUtils.isBlank( installation.getName() ) )
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_NAME, "" );
            }
            else
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_NAME, installation.getName() );
            }

            if ( StringUtils.isBlank( installation.getType() ) )
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_TYPE, "" );
            }
            else
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_TYPE, installation.getType() );
            }

            if ( StringUtils.isBlank( installation.getVarName() ) )
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_VAR_NAME, "" );
            }
            else
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_VAR_VALUE, installation.getVarValue() );
            }

            if ( StringUtils.isBlank( installation.getVarValue() ) )
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_VAR_VALUE, "" );
            }
            else
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_VAR_VALUE, installation.getVarValue() );
            }

            installationsList.add( map );
        }

        return installationsList;
    }

    public Map<String, Object> getBuildResult( int projectId )
        throws ContinuumBuildAgentException
    {
        Map<String, Object> result = new HashMap<String, Object>();

        int currentBuildId = 0;

        try
        {
            currentBuildId = buildAgentTaskQueueManager.getIdOfProjectCurrentlyBuilding();
        }
        catch ( TaskQueueManagerException e )
        {
            throw new ContinuumBuildAgentException( e.getMessage(), e );
        }

        if ( projectId == currentBuildId )
        {
            BuildContext buildContext = buildContextManager.getBuildContext( projectId );

            result.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, buildContext.getProjectId() );
            result.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION_ID, buildContext.getBuildDefinitionId() );
            result.put( ContinuumBuildAgentUtil.KEY_TRIGGER, buildContext.getTrigger() );
            result.put( ContinuumBuildAgentUtil.KEY_USERNAME, buildContext.getUsername() );

            BuildResult buildResult = buildContext.getBuildResult();

            if ( buildResult != null )
            {
                if ( buildResult.getStartTime() <= 0 )
                {
                    result.put( ContinuumBuildAgentUtil.KEY_START_TIME,
                                Long.toString( buildContext.getBuildStartTime() ) );
                }
                else
                {
                    result.put( ContinuumBuildAgentUtil.KEY_START_TIME, Long.toString( buildResult.getStartTime() ) );
                }

                if ( buildResult.getError() == null )
                {
                    result.put( ContinuumBuildAgentUtil.KEY_BUILD_ERROR, "" );
                }
                else
                {
                    result.put( ContinuumBuildAgentUtil.KEY_BUILD_ERROR, buildResult.getError() );
                }

                result.put( ContinuumBuildAgentUtil.KEY_BUILD_STATE, buildResult.getState() );
                result.put( ContinuumBuildAgentUtil.KEY_END_TIME, Long.toString( buildResult.getEndTime() ) );
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_EXIT_CODE, buildResult.getExitCode() );
            }
            else
            {
                result.put( ContinuumBuildAgentUtil.KEY_START_TIME, Long.toString( buildContext.getBuildStartTime() ) );
                result.put( ContinuumBuildAgentUtil.KEY_END_TIME, Long.toString( 0 ) );
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_STATE, ContinuumProjectState.BUILDING );
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_ERROR, "" );
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_EXIT_CODE, 0 );
            }

            String buildOutput = getBuildOutputText( projectId );
            if ( buildOutput == null )
            {
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_OUTPUT, "" );
            }
            else
            {
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_OUTPUT, buildOutput );
            }

            result.put( ContinuumBuildAgentUtil.KEY_SCM_RESULT,
                        ContinuumBuildAgentUtil.createScmResult( buildContext ) );
        }
        return result;
    }

    public void cancelBuild()
        throws ContinuumBuildAgentException
    {
        try
        {
            buildAgentTaskQueueManager.cancelBuild();
        }
        catch ( TaskQueueManagerException e )
        {
            throw new ContinuumBuildAgentException( e.getMessage(), e );
        }
    }

    public String generateWorkingCopyContent( int projectId, String userDirectory, String baseUrl,
                                              String imagesBaseUrl )
        throws ContinuumBuildAgentException
    {
        File workingDirectory = buildAgentConfigurationService.getWorkingDirectory( projectId );

        try
        {
            List<File> files = ContinuumBuildAgentUtil.getFiles( userDirectory, workingDirectory );
            return generator.generate( files, baseUrl, imagesBaseUrl, workingDirectory );
        }
        catch ( ContinuumException e )
        {
            log.error( "Failed to generate working copy content", e );
        }

        return "";
    }

    public String getProjectFileContent( int projectId, String directory, String filename )
        throws ContinuumBuildAgentException
    {
        String relativePath = "\\.\\./"; // prevent users from using relative paths.
        Pattern pattern = Pattern.compile( relativePath );
        Matcher matcher = pattern.matcher( directory );
        String filteredDirectory = matcher.replaceAll( "" );

        matcher = pattern.matcher( filename );
        String filteredFilename = matcher.replaceAll( "" );

        File workingDirectory = buildAgentConfigurationService.getWorkingDirectory( projectId );

        File fileDirectory = new File( workingDirectory, filteredDirectory );

        File userFile = new File( fileDirectory, filteredFilename );

        try
        {
            return FileUtils.fileRead( userFile );
        }
        catch ( IOException e )
        {
            throw new ContinuumBuildAgentException( "Can't read file " + filename, e );
        }
    }

    public Map<String, Object> getReleasePluginParameters( int projectId, String pomFilename )
        throws ContinuumBuildAgentException
    {
        Map<String, Object> releaseParameters = new HashMap<String, Object>();

        String workingDirectory = buildAgentConfigurationService.getWorkingDirectory( projectId ).getPath();

        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        try
        {
            Model model = pomReader.read( ReaderFactory.newXmlReader( new File( workingDirectory, pomFilename ) ) );

            if ( model.getBuild() != null && model.getBuild().getPlugins() != null )
            {
                for ( Plugin plugin : (List<Plugin>) model.getBuild().getPlugins() )
                {
                    if ( plugin.getGroupId() != null && plugin.getGroupId().equals( "org.apache.maven.plugins" ) &&
                        plugin.getArtifactId() != null && plugin.getArtifactId().equals( "maven-release-plugin" ) )
                    {
                        Xpp3Dom dom = (Xpp3Dom) plugin.getConfiguration();

                        if ( dom != null )
                        {
                            Xpp3Dom configuration = dom.getChild( "releaseLabel" );
                            if ( configuration != null )
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_SCM_TAG, configuration.getValue() );
                            }
                            else
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_SCM_TAG, "" );
                            }

                            configuration = dom.getChild( "tag" );
                            if ( configuration != null )
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_SCM_TAG, configuration.getValue() );
                            }
                            else
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_SCM_TAG, "" );
                            }

                            configuration = dom.getChild( "tagBase" );
                            if ( configuration != null )
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_SCM_TAGBASE,
                                                       configuration.getValue() );
                            }
                            else
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_SCM_TAGBASE, "" );
                            }

                            configuration = dom.getChild( "preparationGoals" );
                            if ( configuration != null )
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_PREPARE_GOALS,
                                                       configuration.getValue() );
                            }
                            else
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_PREPARE_GOALS, "" );
                            }

                            configuration = dom.getChild( "arguments" );
                            if ( configuration != null )
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_ARGUMENTS,
                                                       configuration.getValue() );
                            }
                            else
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_ARGUMENTS, "" );
                            }

                            configuration = dom.getChild( "scmCommentPrefix" );
                            if ( configuration != null )
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_SCM_COMMENT_PREFIX,
                                                       configuration.getValue() );
                            }
                            else
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_SCM_COMMENT_PREFIX, "" );
                            }

                            configuration = dom.getChild( "autoVersionSubmodules" );
                            if ( configuration != null )
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_AUTO_VERSION_SUBMODULES,
                                                       Boolean.valueOf( configuration.getValue() ) );
                            }
                            else
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_AUTO_VERSION_SUBMODULES, false );
                            }

                            configuration = dom.getChild( "addSchema" );
                            if ( configuration != null )
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_ADD_SCHEMA,
                                                       Boolean.valueOf( configuration.getValue() ) );
                            }
                            else
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_ADD_SCHEMA, false );
                            }

                            configuration = dom.getChild( "useReleaseProfile" );
                            if ( configuration != null )
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_USE_RELEASE_PROFILE,
                                                       Boolean.valueOf( configuration.getValue() ) );
                            }
                            else
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_USE_RELEASE_PROFILE, false );
                            }

                            configuration = dom.getChild( "goals" );
                            if ( configuration != null )
                            {
                                String goals = configuration.getValue();
                                if ( model.getDistributionManagement() != null &&
                                    model.getDistributionManagement().getSite() != null )
                                {
                                    goals += "site-deploy";
                                }

                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_PERFORM_GOALS, goals );
                            }
                            else
                            {
                                releaseParameters.put( ContinuumBuildAgentUtil.KEY_PERFORM_GOALS, "" );
                            }
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new ContinuumBuildAgentException( "Error getting release plugin parameters from pom file", e );
        }

        return releaseParameters;
    }

    public List<Map<String, String>> processProject( int projectId, String pomFilename, boolean autoVersionSubmodules )
        throws ContinuumBuildAgentException
    {
        List<Map<String, String>> projects = new ArrayList<Map<String, String>>();

        String workingDirectory = buildAgentConfigurationService.getWorkingDirectory( projectId ).getPath();

        try
        {
            processProject( workingDirectory, pomFilename, autoVersionSubmodules, projects );
        }
        catch ( Exception e )
        {
            throw new ContinuumBuildAgentException( "Unable to process project " + projectId, e );
        }

        return projects;
    }

    public String releasePrepare( Map project, Properties properties, Map releaseVersion, Map developmentVersion,
                                  Map<String, String> environments, String username )
        throws ContinuumBuildAgentException
    {
        try
        {
            return buildAgentReleaseManager.releasePrepare( project, properties, releaseVersion, developmentVersion,
                                                            environments, username );
        }
        catch ( ContinuumReleaseException e )
        {
            throw new ContinuumBuildAgentException( "Unable to prepare release", e );
        }
    }

    public Map<String, Object> getReleaseResult( String releaseId )
        throws ContinuumBuildAgentException
    {
        ReleaseResult result = buildAgentReleaseManager.getReleaseResult( releaseId );

        Map<String, Object> map = new HashMap<String, Object>();
        map.put( ContinuumBuildAgentUtil.KEY_START_TIME, Long.toString( result.getStartTime() ) );
        map.put( ContinuumBuildAgentUtil.KEY_END_TIME, Long.toString( result.getEndTime() ) );
        map.put( ContinuumBuildAgentUtil.KEY_RELEASE_RESULT_CODE, result.getResultCode() );
        map.put( ContinuumBuildAgentUtil.KEY_RELEASE_OUTPUT, result.getOutput() );

        return map;
    }

    public Map<String, Object> getListener( String releaseId )
        throws ContinuumBuildAgentException
    {
        return buildAgentReleaseManager.getListener( releaseId );
    }

    public void removeListener( String releaseId )
    {
        buildAgentReleaseManager.removeListener( releaseId );
    }

    public String getPreparedReleaseName( String releaseId )
    {
        return buildAgentReleaseManager.getPreparedReleaseName( releaseId );
    }

    public void releasePerform( String releaseId, String goals, String arguments, boolean useReleaseProfile,
                                Map repository, String username )
        throws ContinuumBuildAgentException
    {
        try
        {
            buildAgentReleaseManager.releasePerform( releaseId, goals, arguments, useReleaseProfile, repository, username );
        }
        catch ( ContinuumReleaseException e )
        {
            throw new ContinuumBuildAgentException( "Unable to perform release " + releaseId, e );
        }
    }

    public String releasePerformFromScm( String goals, String arguments, boolean useReleaseProfile, Map repository,
                                         String scmUrl, String scmUsername, String scmPassword, String scmTag,
                                         String scmTagBase, Map<String, String> environments, String username )
        throws ContinuumBuildAgentException
    {
        try
        {
            return buildAgentReleaseManager.releasePerformFromScm( goals, arguments, useReleaseProfile, repository,
                                                                   scmUrl, scmUsername, scmPassword, scmTag, scmTagBase,
                                                                   environments, username );
        }
        catch ( ContinuumReleaseException e )
        {
            throw new ContinuumBuildAgentException( "Unable to perform release from scm", e );
        }
    }

    public String releaseCleanup( String releaseId )
        throws ContinuumBuildAgentException
    {
        return buildAgentReleaseManager.releaseCleanup( releaseId );
    }

    public void releaseRollback( String releaseId, int projectId )
        throws ContinuumBuildAgentException
    {
        try
        {
            buildAgentReleaseManager.releaseRollback( releaseId, projectId );
        }
        catch ( ContinuumReleaseException e )
        {
            throw new ContinuumBuildAgentException( "Unable to rollback release " + releaseId, e );
        }
    }

    public int getBuildSizeOfAgent()
    {
        int size = 0;
        
        try
        {
            if ( buildAgentTaskQueueManager.getCurrentProjectInBuilding() != null )
            {
                size++;
            }

            if ( buildAgentTaskQueueManager.getCurrentProjectInPrepareBuild() != null )
            {
                size++;
            }

            size = size + buildAgentTaskQueueManager.getProjectsInBuildQueue().size();

            size = size + buildAgentTaskQueueManager.getProjectsInPrepareBuildQueue().size();
        }
        catch ( TaskQueueManagerException e )
        {
            log.error( "Error occurred while getting build size of agent" );
        }

        return size;
    }

    public List<Map<String, Object>> getProjectsInPrepareBuildQueue()
        throws ContinuumBuildAgentException
    {
        try
        {
            List<Map<String, Object>> projects = new ArrayList<Map<String, Object>>();

            for ( PrepareBuildProjectsTask task : buildAgentTaskQueueManager.getProjectsInPrepareBuildQueue() )
            {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put( ContinuumBuildAgentUtil.KEY_PROJECT_GROUP_ID, new Integer( task.getProjectGroupId() ) );
                map.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_ID, new Integer( task.getScmRootId() ) );
                map.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_ADDRESS, task.getScmRootAddress() );
                map.put( ContinuumBuildAgentUtil.KEY_TRIGGER, task.getBuildTrigger().getTrigger() );
                map.put( ContinuumBuildAgentUtil.KEY_USERNAME, task.getBuildTrigger().getTriggeredBy() );

                projects.add( map );
            }

            return projects;
        }
        catch( TaskQueueManagerException e )
        {
            log.error( "Error occurred while retrieving projects in prepare build queue", e );
            throw new ContinuumBuildAgentException( "Error occurred while retrieving projects in prepare build queue", e );
        }
    }

    public List<Map<String, Object>> getProjectsAndBuildDefinitionsInPrepareBuildQueue()
        throws ContinuumBuildAgentException
    {
        try
        {
            List<Map<String, Object>> projects = new ArrayList<Map<String, Object>>();

            for ( PrepareBuildProjectsTask task : buildAgentTaskQueueManager.getProjectsInPrepareBuildQueue() )
            {
                for ( BuildContext context : task.getBuildContexts() )
                {
                    Map<String, Object> map = new HashMap<String, Object>();

                    map.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, context.getProjectId() );
                    map.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION_ID, context.getBuildDefinitionId() );

                    projects.add( map );
                }
            }

            return projects;
        }
        catch( TaskQueueManagerException e )
        {
            log.error( "Error occurred while retrieving projects in prepare build queue", e );
            throw new ContinuumBuildAgentException( "Error occurred while retrieving projects in prepare build queue", e );
        }
    }

    public List<Map<String, Object>> getProjectsInBuildQueue()
        throws ContinuumBuildAgentException
    {
        try
        {
            List<Map<String, Object>> projects = new ArrayList<Map<String, Object>>();

            for ( BuildProjectTask task : buildAgentTaskQueueManager.getProjectsInBuildQueue() )
            {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, new Integer( task.getProjectId() ) );
                map.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION_ID, new Integer( task.getBuildDefinitionId() ) );
                map.put( ContinuumBuildAgentUtil.KEY_TRIGGER, task.getBuildTrigger().getTrigger() );
                map.put( ContinuumBuildAgentUtil.KEY_USERNAME, task.getBuildTrigger().getTriggeredBy() );
                map.put( ContinuumBuildAgentUtil.KEY_PROJECT_GROUP_ID, new Integer( task.getProjectGroupId() ) );
                map.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION_LABEL, task.getBuildDefinitionLabel() );

                projects.add( map );
            }

            return projects;
        }
        catch( TaskQueueManagerException e )
        {
            log.error( "Error occurred while retrieving projects in build queue", e );
            throw new ContinuumBuildAgentException( "Error occurred while retrieving projects in build queue", e );
        }
    }

    public Map<String, Object> getProjectCurrentlyPreparingBuild()
        throws ContinuumBuildAgentException
    {
        try
        {
            Map<String, Object> project = new HashMap<String, Object>();

            PrepareBuildProjectsTask task = buildAgentTaskQueueManager.getCurrentProjectInPrepareBuild();

            if ( task != null )
            {
                project.put( ContinuumBuildAgentUtil.KEY_PROJECT_GROUP_ID, new Integer( task.getProjectGroupId() ) );
                project.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_ID, new Integer( task.getScmRootId() ) );
                project.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_ADDRESS, task.getScmRootAddress() );
                project.put( ContinuumBuildAgentUtil.KEY_TRIGGER, task.getBuildTrigger().getTrigger() );
                project.put( ContinuumBuildAgentUtil.KEY_USERNAME, task.getBuildTrigger().getTriggeredBy() );
            }

            return project;
        }
        catch ( TaskQueueManagerException e )
        {
            log.error( "Error occurred while retrieving current project in prepare build", e );
            throw new ContinuumBuildAgentException( "Error occurred while retrieving current project in prepare build", e );
        }
    }

    public List<Map<String, Object>> getProjectsAndBuildDefinitionsCurrentlyPreparingBuild()
        throws ContinuumBuildAgentException
    {
        try
        {
            List<Map<String, Object>> projects = new ArrayList<Map<String, Object>>();

            PrepareBuildProjectsTask task = buildAgentTaskQueueManager.getCurrentProjectInPrepareBuild();

            if ( task != null )
            {
                for ( BuildContext context : task.getBuildContexts() )
                {
                    Map<String, Object> map = new HashMap<String, Object>();

                    map.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, context.getProjectId() );
                    map.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION_ID, context.getBuildDefinitionId() );

                    projects.add( map );
                }
            }

            return projects;
        }
        catch ( TaskQueueManagerException e )
        {
            log.error( "Error occurred while retrieving current projects in prepare build", e );
            throw new ContinuumBuildAgentException( "Error occurred while retrieving current projects in prepare build", e );
        }
    }
    public Map<String, Object> getProjectCurrentlyBuilding()
        throws ContinuumBuildAgentException
    {
        try
        {
            Map<String, Object> project = new HashMap<String, Object>();

            BuildProjectTask task = buildAgentTaskQueueManager.getCurrentProjectInBuilding();

            if ( task != null )
            {
                project.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, new Integer( task.getProjectId() ) );
                project.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION_ID, new Integer( task.getBuildDefinitionId() ) );
                project.put( ContinuumBuildAgentUtil.KEY_TRIGGER, task.getBuildTrigger().getTrigger() );
                project.put( ContinuumBuildAgentUtil.KEY_USERNAME, task.getBuildTrigger().getTriggeredBy() );
                project.put( ContinuumBuildAgentUtil.KEY_PROJECT_GROUP_ID, new Integer( task.getProjectGroupId() ) );
                project.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION_LABEL, task.getBuildDefinitionLabel() );
            }

            return project;
        }
        catch ( TaskQueueManagerException e )
        {
            log.error( "Error occurred while retrieving current project in building", e );
            throw new ContinuumBuildAgentException( "Error occurred while retrieving current project in building", e );
        }
    }

    public boolean isProjectGroupInQueue( int projectGroupId )
    {
        try
        {
            for ( PrepareBuildProjectsTask task : buildAgentTaskQueueManager.getProjectsInPrepareBuildQueue() )
            {
                if ( task.getProjectGroupId() == projectGroupId )
                {
                    log.debug( "projectGroup {} is in prepare build queue", projectGroupId );
                    return true;
                }
            }

            PrepareBuildProjectsTask currentPrepareBuildTask = buildAgentTaskQueueManager.getCurrentProjectInPrepareBuild();

            if ( currentPrepareBuildTask != null && currentPrepareBuildTask.getProjectGroupId() == projectGroupId )
            {
                log.debug( "projectGroup {} is currently preparing build", projectGroupId );
                return true;
            }

            for ( BuildProjectTask task : buildAgentTaskQueueManager.getProjectsInBuildQueue() )
            {
                if ( task.getProjectGroupId() == projectGroupId )
                {
                    log.debug( "projectGroup {} is in build queue", projectGroupId );
                    return true;
                }
            }

            BuildProjectTask currentBuildTask = buildAgentTaskQueueManager.getCurrentProjectInBuilding();

            if ( currentBuildTask != null && currentBuildTask.getProjectGroupId() == projectGroupId )
            {
                log.debug( "projectGroup {} is currently building", projectGroupId );
                return true;
            }            
        }
        catch ( TaskQueueManagerException e )
        {
            log.error( "Error while checking if project group " + projectGroupId + " is queued in agent", e);
        }

        return false;
    }

    public boolean isProjectScmRootInQueue( int projectScmRootId, List<Integer> projectIds )
    {
        try
        {
            PrepareBuildProjectsTask currentPrepareBuildTask = buildAgentTaskQueueManager.getCurrentProjectInPrepareBuild();

            if ( currentPrepareBuildTask != null && currentPrepareBuildTask.getScmRootId() == projectScmRootId )
            {
                return true;
            }

            BuildProjectTask currentBuildTask = buildAgentTaskQueueManager.getCurrentProjectInBuilding();
            
            if ( currentBuildTask != null )
            {
                int projectId = currentBuildTask.getProjectId();

                for ( Integer pid : projectIds )
                {
                    if ( pid == projectId )
                    {
                        return true;
                    }
                }
            }

            for ( PrepareBuildProjectsTask task : buildAgentTaskQueueManager.getProjectsInPrepareBuildQueue() )
            {
                if ( task.getScmRootId() == projectScmRootId )
                {
                    return true;
                }
            }

            for ( BuildProjectTask task : buildAgentTaskQueueManager.getProjectsInBuildQueue() )
            {
                int projectId = task.getProjectId();

                for ( Integer pid : projectIds )
                {
                    if ( pid == projectId )
                    {
                        return true;
                    }
                }
            }
        }
        catch ( TaskQueueManagerException e )
        {
            log.error( "Error while checking if project scm root " + projectScmRootId + " is queued in agent", e);
        }

        return false;
    }

    public boolean isProjectGroupInPrepareBuildQueue( int projectGroupId )
    {
        try
        {
            for ( PrepareBuildProjectsTask task : buildAgentTaskQueueManager.getProjectsInPrepareBuildQueue() )
            {
                if ( task.getProjectGroupId() == projectGroupId )
                {
                    return true;
                }
            }
        }
        catch ( TaskQueueManagerException e )
        {
            log.error( "Error while checking if project group " + projectGroupId + " is in prepare build queue in agent", e);
        }

        return false;
    }

    public boolean isProjectGroupCurrentlyPreparingBuild( int projectGroupId )
    {
        try
        {
            PrepareBuildProjectsTask currentPrepareBuildTask = buildAgentTaskQueueManager.getCurrentProjectInPrepareBuild();

            if ( currentPrepareBuildTask != null && currentPrepareBuildTask.getProjectGroupId() == projectGroupId )
            {
                return true;
            }
        }
        catch ( TaskQueueManagerException e )
        {
            log.error( "Error while checking if project group " + projectGroupId + " is currently preparing build in agent", e);
        }

        return false;
    }

    public boolean isProjectCurrentlyBuilding( int projectId )
    {
        try
        {
            BuildProjectTask currentBuildTask = buildAgentTaskQueueManager.getCurrentProjectInBuilding();

            if ( currentBuildTask != null && currentBuildTask.getProjectId() == projectId )
            {
                return true;
            }
        }
        catch ( TaskQueueManagerException e )
        {
            log.error( "Error occurred while checking if project " + projectId + " is currently building in agent", e );
        }

        return false;
    }

    public boolean isProjectInBuildQueue( int projectId )
    {
        try
        {
            List<BuildProjectTask> buildTasks = buildAgentTaskQueueManager.getProjectsInBuildQueue();

            if ( buildTasks != null )
            {
                for ( BuildProjectTask task : buildTasks )
                {
                    if ( task.getProjectId() == projectId )
                    {
                        return true;
                    }
                }
            }
        }
        catch ( TaskQueueManagerException e )
        {
            log.error( "Error occurred while checking if project " + projectId + " is in build queue of agent", e );
        }

        return false;
    }

    public boolean removeFromPrepareBuildQueue( int projectGroupId, int scmRootId )
        throws ContinuumBuildAgentException
    {
        try
        {
            return buildAgentTaskQueueManager.removeFromPrepareBuildQueue( projectGroupId, scmRootId );
        }
        catch ( TaskQueueManagerException e )
        {
            log.error( "Error occurred while removing projects from prepare build queue", e );
            throw new ContinuumBuildAgentException( "Error occurred while removing projects from prepare build queue", e );
        }
    }

    public void removeFromPrepareBuildQueue( List<String> hashCodes )
        throws ContinuumBuildAgentException
    {
        try
        {
            buildAgentTaskQueueManager.removeFromPrepareBuildQueue( listToIntArray( hashCodes ) );
        }
        catch ( TaskQueueManagerException e )
        {
            log.error( "Error occurred while removing projects from prepare build queue", e );
            throw new ContinuumBuildAgentException( "Error occurred while removing projects from prepare build queue", e );
        }
    }

    public boolean removeFromBuildQueue( int projectId, int buildDefinitionId )
        throws ContinuumBuildAgentException
    {
        try
        {
            return buildAgentTaskQueueManager.removeFromBuildQueue( projectId, buildDefinitionId );
        }
        catch ( TaskQueueManagerException e )
        {
            log.error( "Error occurred while removing project from build queue", e );
            throw new ContinuumBuildAgentException( "Error occurred while removing project from build queue ", e );
        }
    }

    public void removeFromBuildQueue( List<String> hashCodes )
        throws ContinuumBuildAgentException
    {
        try
        {
            buildAgentTaskQueueManager.removeFromBuildQueue( listToIntArray( hashCodes ) );
        }
        catch ( TaskQueueManagerException e )
        {
            log.error( "Error occurred while removing projects from build queue", e );
            throw new ContinuumBuildAgentException( "Error occurred while removing project from build queue ", e );
        }
    }

    public boolean ping()
    {
        log.info( "Ping Ok" );

        return Boolean.TRUE;
    }

    public String getBuildAgentPlatform() 
        throws ContinuumBuildAgentException
    {
        try
        {
            return System.getProperty( "os.name" );
        }
        catch ( Exception e )
        {
            log.error( "Error in when trying to get build agent's platform", e );
            throw new ContinuumBuildAgentException( "Error in when trying to get build agent's platform", e );
        }
    }

    private void processProject( String workingDirectory, String pomFilename, boolean autoVersionSubmodules,
                                 List<Map<String, String>> projects )
        throws Exception
    {
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model = pomReader.read( ReaderFactory.newXmlReader( new File( workingDirectory, pomFilename ) ) );

        if ( model.getGroupId() == null )
        {
            model.setGroupId( model.getParent().getGroupId() );
        }

        if ( model.getVersion() == null )
        {
            model.setVersion( model.getParent().getVersion() );
        }

        setProperties( model, projects );

        if ( !autoVersionSubmodules )
        {
            for ( Iterator modules = model.getModules().iterator(); modules.hasNext(); )
            {
                String module = StringUtils.replace( modules.next().toString(), '\\', '/' );
            	
                processProject( workingDirectory + "/" + module, "pom.xml", autoVersionSubmodules,
                                projects );
            }
        }
    }

    private void setProperties( Model model, List<Map<String, String>> projects )
        throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();

        params.put( "key", model.getGroupId() + ":" + model.getArtifactId() );

        if ( model.getName() == null )
        {
            model.setName( model.getArtifactId() );
        }
        params.put( "name", model.getName() );

        VersionInfo version = new DefaultVersionInfo( model.getVersion() );

        params.put( "release", version.getReleaseVersionString() );
        params.put( "dev", version.getNextVersion().getSnapshotVersionString() );

        projects.add( params );
    }

    private List<BuildContext> initializeBuildContext( List<Map<String, Object>> projectsBuildContext )
    {
        List<BuildContext> buildContext = new ArrayList<BuildContext>();

        for ( Map<String, Object> map : projectsBuildContext )
        {
            BuildContext context = new BuildContext();
            context.setProjectId( ContinuumBuildAgentUtil.getProjectId( map ) );
            context.setProjectVersion( ContinuumBuildAgentUtil.getProjectVersion( map ) );
            context.setBuildDefinitionId( ContinuumBuildAgentUtil.getBuildDefinitionId( map ) );
            context.setBuildFile( ContinuumBuildAgentUtil.getBuildFile( map ) );
            context.setExecutorId( ContinuumBuildAgentUtil.getExecutorId( map ) );
            context.setGoals( ContinuumBuildAgentUtil.getGoals( map ) );
            context.setArguments( ContinuumBuildAgentUtil.getArguments( map ) );
            context.setScmUrl( ContinuumBuildAgentUtil.getScmUrl( map ) );
            context.setScmUsername( ContinuumBuildAgentUtil.getScmUsername( map ) );
            context.setScmPassword( ContinuumBuildAgentUtil.getScmPassword( map ) );
            context.setBuildFresh( ContinuumBuildAgentUtil.isBuildFresh( map ) );
            context.setProjectGroupId( ContinuumBuildAgentUtil.getProjectGroupId( map ) );
            context.setProjectGroupName( ContinuumBuildAgentUtil.getProjectGroupName( map ) );
            context.setScmRootAddress( ContinuumBuildAgentUtil.getScmRootAddress( map ) );
            context.setScmRootId( ContinuumBuildAgentUtil.getScmRootId( map ) );
            context.setProjectName( ContinuumBuildAgentUtil.getProjectName( map ) );
            context.setProjectState( ContinuumBuildAgentUtil.getProjectState( map ) );
            context.setTrigger( ContinuumBuildAgentUtil.getTrigger( map ) );
            context.setUsername( ContinuumBuildAgentUtil.getUsername( map ) );
            context.setLocalRepository( ContinuumBuildAgentUtil.getLocalRepository( map ) );
            context.setBuildNumber( ContinuumBuildAgentUtil.getBuildNumber( map ) );
            context.setOldScmResult( getScmResult( ContinuumBuildAgentUtil.getOldScmChanges( map ) ) );
            context.setLatestUpdateDate( ContinuumBuildAgentUtil.getLatestUpdateDate( map ) );
            context.setBuildAgentUrl( ContinuumBuildAgentUtil.getBuildAgentUrl( map ) );
            context.setMaxExecutionTime( ContinuumBuildAgentUtil.getMaxExecutionTime( map ) );
            context.setBuildDefinitionLabel( ContinuumBuildAgentUtil.getBuildDefinitionLabel( map ) );
            context.setScmTag( ContinuumBuildAgentUtil.getScmTag( map ) );

            buildContext.add( context );
        }

        buildContextManager.addBuildContexts( buildContext );

        return buildContext;
    }

    private String getBuildOutputText( int projectId )
    {
        try
        {
            File buildOutputFile = buildAgentConfigurationService.getBuildOutputFile( projectId );

            if ( buildOutputFile.exists() )
            {
                return StringEscapeUtils.escapeHtml( FileUtils.fileRead( buildOutputFile ) );
            }
        }
        catch ( Exception e )
        {
            // do not throw exception, just log it
            log.error( "Error retrieving build output file", e );
        }

        return null;
    }

    private ScmResult getScmResult( List<Map<String, Object>> scmChanges )
    {
        ScmResult scmResult = null;

        if ( scmChanges != null && scmChanges.size() > 0 )
        {
            scmResult = new ScmResult();

            for ( Map<String, Object> map : scmChanges )
            {
                ChangeSet changeSet = new ChangeSet();
                changeSet.setAuthor( ContinuumBuildAgentUtil.getChangeSetAuthor( map ) );
                changeSet.setComment( ContinuumBuildAgentUtil.getChangeSetComment( map ) );
                changeSet.setDate( ContinuumBuildAgentUtil.getChangeSetDate( map ) );
                setChangeFiles( changeSet, map );
                scmResult.addChange( changeSet );
            }
        }

        return scmResult;
    }

    private void setChangeFiles( ChangeSet changeSet, Map<String, Object> context )
    {
        List<Map<String, Object>> files = ContinuumBuildAgentUtil.getChangeSetFiles( context );

        if ( files != null )
        {
            for ( Map<String, Object> map : files )
            {
                ChangeFile changeFile = new ChangeFile();
                changeFile.setName( ContinuumBuildAgentUtil.getChangeFileName( map ) );
                changeFile.setRevision( ContinuumBuildAgentUtil.getChangeFileRevision( map ) );
                changeFile.setStatus( ContinuumBuildAgentUtil.getChangeFileStatus( map ) );

                changeSet.addFile( changeFile );
            }
        }
    }

    private PrepareBuildProjectsTask createPrepareBuildProjectsTask( List<BuildContext> buildContexts )
        throws ContinuumBuildAgentException
    {
        if ( buildContexts != null && buildContexts.size() > 0 )
        {
            BuildContext context = buildContexts.get( 0 );
            return new PrepareBuildProjectsTask( buildContexts, new BuildTrigger( context.getTrigger(), context.getUsername() ),
            		                             context.getProjectGroupId(), context.getScmRootAddress(), context.getScmRootId() );
        }
        else
        {
            log.info( "Nothing to build" );
            return null;
        }
    }

    private int[] listToIntArray( List<String> strings )
    {
        if ( strings == null || strings.isEmpty() )
        {
            return new int[0];
        }
        int[] array = new int[0];
        for ( String intString : strings )
        {
            array = ArrayUtils.add( array, Integer.parseInt( intString ) );
        }
        return array;
    }
}
