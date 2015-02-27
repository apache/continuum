package org.apache.continuum.builder.distributed;

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

import org.apache.commons.io.IOUtils;
import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.builder.distributed.util.DistributedBuildUtil;
import org.apache.continuum.builder.utils.ContinuumBuildConstant;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component( role = org.apache.continuum.builder.distributed.DistributedBuildService.class )
public class DefaultDistributedBuildService
    implements DistributedBuildService
{
    private static final Logger log = LoggerFactory.getLogger( DefaultDistributedBuildService.class );

    @Requirement
    private ProjectDao projectDao;

    @Requirement
    private BuildDefinitionDao buildDefinitionDao;

    @Requirement
    private BuildResultDao buildResultDao;

    @Requirement
    private ProjectScmRootDao projectScmRootDao;

    @Requirement
    private ConfigurationService configurationService;

    @Requirement
    private InstallationService installationService;

    @Requirement
    private ContinuumNotificationDispatcher notifierDispatcher;

    @Requirement
    private DistributedBuildUtil distributedBuildUtil;

    @Requirement
    private DistributedBuildManager distributedBuildManager;

    public void updateBuildResult( Map<String, Object> context )
        throws ContinuumException
    {
        try
        {
            int projectId = ContinuumBuildConstant.getProjectId( context );
            int buildDefinitionId = ContinuumBuildConstant.getBuildDefinitionId( context );

            Project project = projectDao.getProjectWithAllDetails( projectId );

            int buildNumber;
            if ( ContinuumBuildConstant.getBuildState( context ) == ContinuumProjectState.OK )
            {
                buildNumber = project.getBuildNumber() + 1;
            }
            else
            {
                buildNumber = project.getBuildNumber();
            }

            BuildResult buildResult, oldBuildResult;

            log.info( "update build result of project '{}'", projectId );

            int existingResultId = 0;
            try
            {
                existingResultId =
                    distributedBuildManager.getCurrentRun( projectId, buildDefinitionId ).getBuildResultId();
            }
            catch ( ContinuumException e )
            {
                log.warn( "failed to find result for remote build {}", e.getMessage() );
            }

            boolean existingResult = existingResultId > 0;

            if ( existingResult )
            {
                buildResult = buildResultDao.getBuildResult( existingResultId );
                distributedBuildUtil.updateBuildResultFromMap( buildResult, context );
                oldBuildResult =
                    buildResultDao.getPreviousBuildResultForBuildDefinition( projectId, buildDefinitionId,
                                                                             existingResultId );
            }
            else
            {
                buildResult = distributedBuildUtil.convertMapToBuildResult( context );
                oldBuildResult = buildResultDao.getLatestBuildResultForBuildDefinition( projectId, buildDefinitionId );
            }

            // Set the complete contents of the build result...

            BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );
            buildResult.setBuildDefinition( buildDefinition );
            buildResult.setBuildNumber( buildNumber );
            buildResult.setModifiedDependencies( distributedBuildUtil.getModifiedDependencies( oldBuildResult,
                                                                                               context ) );
            buildResult.setScmResult( distributedBuildUtil.getScmResult( context ) );

            Date date = ContinuumBuildConstant.getLatestUpdateDate( context );
            if ( date != null )
            {
                buildResult.setLastChangedDate( date.getTime() );
            }
            else if ( oldBuildResult != null )
            {
                buildResult.setLastChangedDate( oldBuildResult.getLastChangedDate() );
            }

            if ( existingResult )
            {
                buildResultDao.updateBuildResult( buildResult );
            }
            else
            {
                buildResultDao.addBuildResult( project, buildResult );
                buildResult = buildResultDao.getBuildResult( buildResult.getId() );
            }

            project.setOldState( project.getState() );
            project.setState( ContinuumBuildConstant.getBuildState( context ) );
            project.setBuildNumber( buildNumber );
            project.setLatestBuildId( buildResult.getId() );

            projectDao.updateProject( project );

            File buildOutputFile = configurationService.getBuildOutputFile( buildResult.getId(), project.getId() );
            FileWriter fileWriter = null;
            try
            {
                fileWriter = new FileWriter( buildOutputFile );
                String output = ContinuumBuildConstant.getBuildOutput( context );
                fileWriter.write( output == null ? "" : output );
            }
            finally
            {
                IOUtils.closeQuietly( fileWriter );
            }

            notifierDispatcher.buildComplete( project, buildDefinition, buildResult );

            distributedBuildManager.removeCurrentRun( projectId, buildDefinitionId );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while updating build result for project", e );
        }
        catch ( ConfigurationException e )
        {
            throw new ContinuumException( "Error retrieving build output file", e );
        }
        catch ( IOException e )
        {
            throw new ContinuumException( "Error while writing build output to file", e );
        }
    }

    public void prepareBuildFinished( Map<String, Object> context )
        throws ContinuumException
    {
        int projectGroupId = ContinuumBuildConstant.getProjectGroupId( context );
        String scmRootAddress = ContinuumBuildConstant.getScmRootAddress( context );

        try
        {
            ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRootByProjectGroupAndScmRootAddress( projectGroupId,
                                                                                                         scmRootAddress );

            String error = ContinuumBuildConstant.getScmError( context );

            if ( StringUtils.isEmpty( error ) )
            {
                scmRoot.setState( ContinuumProjectState.UPDATED );
            }
            else
            {
                scmRoot.setState( ContinuumProjectState.ERROR );
                scmRoot.setError( error );
            }

            projectScmRootDao.updateProjectScmRoot( scmRoot );

            notifierDispatcher.prepareBuildComplete( scmRoot );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while updating project scm root '" + scmRootAddress + "'", e );
        }
    }

    public void startProjectBuild( int projectId )
        throws ContinuumException
    {
        try
        {
            Project project = projectDao.getProject( projectId );
            project.setOldState( project.getState() );
            project.setState( ContinuumProjectState.BUILDING );
            projectDao.updateProject( project );

            // Should actually use current run summary, only the tuple (project, buildDef) is unique
            BuildResult result = buildResultDao.getBuildResult( project.getLatestBuildId() );
            result.setState( ContinuumProjectState.BUILDING );
            buildResultDao.updateBuildResult( result );
        }
        catch ( ContinuumStoreException e )
        {
            log.error( "Error while updating project's state (projectId=" + projectId + ")", e );
            throw new ContinuumException( "Error while updating project's state (projectId=" + projectId + ")", e );
        }
    }

    public void startPrepareBuild( Map<String, Object> context )
        throws ContinuumException
    {
        int projectGroupId = ContinuumBuildConstant.getProjectGroupId( context );

        try
        {
            String scmRootAddress = ContinuumBuildConstant.getScmRootAddress( context );

            ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRootByProjectGroupAndScmRootAddress( projectGroupId,
                                                                                                         scmRootAddress );
            scmRoot.setOldState( scmRoot.getState() );
            scmRoot.setState( ContinuumProjectState.UPDATING );
            projectScmRootDao.updateProjectScmRoot( scmRoot );
        }
        catch ( ContinuumStoreException e )
        {
            log.error( "Error while updating project group'" + projectGroupId + "' scm root's state", e );
            throw new ContinuumException( "Error while updating project group'" + projectGroupId + "' scm root's state",
                                          e );
        }
    }

    public Map<String, String> getEnvironments( int buildDefinitionId, String installationType )
        throws ContinuumException
    {
        BuildDefinition buildDefinition;

        try
        {
            buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Failed to retrieve build definition: " + buildDefinitionId, e );
        }

        Profile profile = buildDefinition.getProfile();
        if ( profile == null )
        {
            return Collections.EMPTY_MAP;
        }
        Map<String, String> envVars = new HashMap<String, String>();
        String javaHome = getJavaHomeValue( buildDefinition );
        if ( !StringUtils.isEmpty( javaHome ) )
        {
            envVars.put( installationService.getEnvVar( InstallationService.JDK_TYPE ), javaHome );
        }
        Installation builder = profile.getBuilder();
        if ( builder != null )
        {
            envVars.put( installationService.getEnvVar( installationType ), builder.getVarValue() );
        }
        envVars.putAll( getEnvironmentVariables( buildDefinition ) );
        return envVars;
    }

    public void updateProject( Map<String, Object> context )
        throws ContinuumException
    {
        try
        {
            Project project = projectDao.getProjectWithAllDetails( ContinuumBuildConstant.getProjectId( context ) );

            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getGroupId( context ) ) )
            {
                project.setGroupId( ContinuumBuildConstant.getGroupId( context ) );
            }
            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getArtifactId( context ) ) )
            {
                project.setArtifactId( ContinuumBuildConstant.getArtifactId( context ) );
            }
            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getVersion( context ) ) )
            {
                project.setVersion( ContinuumBuildConstant.getVersion( context ) );
            }
            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getProjectName( context ) ) )
            {
                project.setName( ContinuumBuildConstant.getProjectName( context ) );
            }
            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getProjectDescription( context ) ) )
            {
                project.setDescription( ContinuumBuildConstant.getProjectDescription( context ) );
            }
            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getProjectUrl( context ) ) )
            {
                project.setUrl( ContinuumBuildConstant.getProjectUrl( context ) );
            }
            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getScmUrl( context ) ) )
            {
                project.setScmUrl( ContinuumBuildConstant.getScmUrl( context ) );
            }
            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getScmTag( context ) ) )
            {
                project.setScmTag( ContinuumBuildConstant.getScmTag( context ) );
            }
            project.setParent( getProjectParent( context ) );
            project.setDependencies( getProjectDependencies( context ) );
            project.setDevelopers( getProjectDevelopers( context ) );

            List<ProjectNotifier> userNotifiers = new ArrayList<ProjectNotifier>();

            if ( project.getNotifiers() != null )
            {
                for ( ProjectNotifier notifier : project.getNotifiers() )
                {
                    if ( notifier.isFromUser() )
                    {
                        ProjectNotifier userNotifier = new ProjectNotifier();

                        userNotifier.setType( notifier.getType() );

                        userNotifier.setEnabled( notifier.isEnabled() );

                        userNotifier.setConfiguration( notifier.getConfiguration() );

                        userNotifier.setFrom( notifier.getFrom() );

                        userNotifier.setRecipientType( notifier.getRecipientType() );

                        userNotifier.setSendOnError( notifier.isSendOnError() );

                        userNotifier.setSendOnFailure( notifier.isSendOnFailure() );

                        userNotifier.setSendOnSuccess( notifier.isSendOnSuccess() );

                        userNotifier.setSendOnWarning( notifier.isSendOnWarning() );

                        userNotifier.setSendOnScmFailure( notifier.isSendOnScmFailure() );

                        userNotifiers.add( userNotifier );
                    }
                }
            }

            project.setNotifiers( getProjectNotifiers( context ) );

            for ( ProjectNotifier userNotifier : userNotifiers )
            {
                project.addNotifier( userNotifier );
            }

            projectDao.updateProject( project );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Unable to update project '" + ContinuumBuildConstant.getProjectId(
                context ) +
                                              "' from working copy", e );
        }
    }

    public boolean shouldBuild( Map<String, Object> context )
    {
        int projectId = ContinuumBuildConstant.getProjectId( context );

        try
        {
            int buildDefinitionId = ContinuumBuildConstant.getBuildDefinitionId( context );

            int trigger = ContinuumBuildConstant.getTrigger( context );

            Project project = projectDao.getProjectWithAllDetails( projectId );

            BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );

            BuildResult oldBuildResult = buildResultDao.getLatestBuildResultForBuildDefinition( projectId,
                                                                                                buildDefinitionId );

            List<ProjectDependency> modifiedDependencies = distributedBuildUtil.getModifiedDependencies( oldBuildResult,
                                                                                                         context );

            List<ChangeSet> changes = distributedBuildUtil.getScmChanges( context );

            if ( buildDefinition.isAlwaysBuild() )
            {
                log.info( "AlwaysBuild configured, building (projectId=" + projectId + ")" );
                return true;
            }
            if ( oldBuildResult == null )
            {
                log.info(
                    "The project '" + projectId + "' was never built with the current build definition, building" );
                return true;
            }

            //CONTINUUM-1428
            if ( project.getOldState() == ContinuumProjectState.ERROR ||
                oldBuildResult.getState() == ContinuumProjectState.ERROR )
            {
                log.info( "Latest state was 'ERROR', building (projectId=" + projectId + ")" );
                return true;
            }

            if ( trigger == ContinuumProjectState.TRIGGER_FORCED )
            {
                log.info( "The project '" + projectId + "' build is forced, building" );
                return true;
            }

            Date date = ContinuumBuildConstant.getLatestUpdateDate( context );
            if ( date != null && oldBuildResult.getLastChangedDate() >= date.getTime() )
            {
                log.info( "No changes found, not building (projectId=" + projectId + ")" );
                return false;
            }
            else if ( date != null && changes.isEmpty() )
            {
                // fresh checkout from build agent that's why changes is empty
                log.info( "Changes found in the current project, building (projectId=" + projectId + ")" );
                return true;
            }

            boolean shouldBuild = false;

            boolean allChangesUnknown = true;

            if ( project.getOldState() != ContinuumProjectState.NEW &&
                project.getOldState() != ContinuumProjectState.CHECKEDOUT &&
                project.getState() != ContinuumProjectState.NEW &&
                project.getState() != ContinuumProjectState.CHECKEDOUT )
            {
                // Check SCM changes
                allChangesUnknown = checkAllChangesUnknown( changes );

                if ( allChangesUnknown )
                {
                    if ( !changes.isEmpty() )
                    {
                        log.info( "The project '" + projectId +
                                      "' was not built because all changes are unknown (maybe local modifications or ignored files not defined in your SCM tool." );
                    }
                    else
                    {
                        log.info( "The project '" + projectId +
                                      "' was not built because no changes were detected in sources since the last build." );
                    }
                }

                // Check dependencies changes
                if ( modifiedDependencies != null && !modifiedDependencies.isEmpty() )
                {
                    log.info( "Found dependencies changes, building (projectId=" + projectId + ")" );
                    shouldBuild = true;
                }
            }

            // Check changes
            if ( !shouldBuild && ( ( !allChangesUnknown && !changes.isEmpty() ) || project.getExecutorId().equals(
                ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR ) ) )
            {
                shouldBuild = shouldBuild( changes, buildDefinition, project, getMavenProjectVersion( context ),
                                           getMavenProjectModules( context ) );
            }

            if ( shouldBuild )
            {
                log.info( "Changes found in the current project, building (projectId=" + projectId + ")" );
            }
            else
            {
                log.info( "No changes in the current project, not building (projectId=" + projectId + ")" );
            }

            return shouldBuild;
        }
        catch ( ContinuumStoreException e )
        {
            log.error( "Failed to determine if project '" + projectId + "' should build", e );
        }
        catch ( ContinuumException e )
        {
            log.error( "Failed to determine if project '" + projectId + "' should build", e );
        }

        return false;
    }

    private boolean shouldBuild( List<ChangeSet> changes, BuildDefinition buildDefinition, Project project,
                                 String mavenProjectVersion, List<String> mavenProjectModules )
    {
        //Check if it's a recursive build
        boolean isRecursive = false;
        if ( StringUtils.isNotEmpty( buildDefinition.getArguments() ) )
        {
            isRecursive = buildDefinition.getArguments().indexOf( "-N" ) < 0 && buildDefinition.getArguments().indexOf(
                "--non-recursive" ) < 0;
        }

        if ( isRecursive && changes != null && !changes.isEmpty() )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "recursive build and changes found --> building (projectId=" + project.getId() + ")" );
            }
            return true;
        }

        if ( !project.getVersion().equals( mavenProjectVersion ) )
        {
            log.info( "Found changes in project's version ( maybe project '" + project.getId() +
                          "' was recently released ), building" );
            return true;
        }

        if ( changes == null || changes.isEmpty() )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Found no changes, not building (projectId=" + project.getId() + ")" );
            }
            return false;
        }

        //check if changes are only in sub-modules or not
        List<ChangeFile> files = new ArrayList<ChangeFile>();
        for ( ChangeSet changeSet : changes )
        {
            files.addAll( changeSet.getFiles() );
        }

        int i = 0;
        while ( i <= files.size() - 1 )
        {
            ChangeFile file = files.get( i );
            if ( log.isDebugEnabled() )
            {
                log.debug( "changeFile.name " + file.getName() );
                log.debug( "check in modules " + mavenProjectModules );
            }
            boolean found = false;
            if ( mavenProjectModules != null )
            {
                for ( String module : mavenProjectModules )
                {
                    if ( file.getName().indexOf( module ) >= 0 )
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "changeFile.name " + file.getName() + " removed because in a module" );
                        }
                        files.remove( file );
                        found = true;
                        break;
                    }
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "not removing file " + file.getName() + " not in module " + module );
                    }
                }
            }
            if ( !found )
            {
                i++;
            }
        }

        boolean shouldBuild = !files.isEmpty();

        if ( !shouldBuild )
        {
            log.info( "Changes are only in sub-modules (projectId=" + project.getId() + ")." );
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "shoulbuild = " + shouldBuild );
        }

        return shouldBuild;
    }

    private boolean checkAllChangesUnknown( List<ChangeSet> changes )
    {
        for ( ChangeSet changeSet : changes )
        {
            List<ChangeFile> changeFiles = changeSet.getFiles();

            for ( ChangeFile changeFile : changeFiles )
            {
                if ( !"unknown".equalsIgnoreCase( changeFile.getStatus() ) )
                {
                    return false;
                }
            }
        }

        return true;
    }

    private String getJavaHomeValue( BuildDefinition buildDefinition )
    {
        Profile profile = buildDefinition.getProfile();
        if ( profile == null )
        {
            return null;
        }
        Installation jdk = profile.getJdk();
        if ( jdk == null )
        {
            return null;
        }
        return jdk.getVarValue();
    }

    private Map<String, String> getEnvironmentVariables( BuildDefinition buildDefinition )
    {
        Profile profile = buildDefinition.getProfile();
        Map<String, String> envVars = new HashMap<String, String>();
        if ( profile == null )
        {
            return envVars;
        }
        List<Installation> environmentVariables = profile.getEnvironmentVariables();
        if ( environmentVariables.isEmpty() )
        {
            return envVars;
        }
        for ( Installation installation : environmentVariables )
        {
            envVars.put( installation.getVarName(), installation.getVarValue() );
        }
        return envVars;
    }

    private ProjectDependency getProjectParent( Map<String, Object> context )
    {
        Map<String, Object> map = ContinuumBuildConstant.getProjectParent( context );

        if ( map != null && map.size() > 0 )
        {
            ProjectDependency parent = new ProjectDependency();
            parent.setGroupId( ContinuumBuildConstant.getGroupId( map ) );
            parent.setArtifactId( ContinuumBuildConstant.getArtifactId( map ) );
            parent.setVersion( ContinuumBuildConstant.getVersion( map ) );

            return parent;
        }

        return null;
    }

    private List<ProjectDependency> getProjectDependencies( Map<String, Object> context )
    {
        List<ProjectDependency> projectDependencies = new ArrayList<ProjectDependency>();

        List<Map<String, Object>> dependencies = ContinuumBuildConstant.getProjectDependencies( context );

        if ( dependencies != null )
        {
            for ( Map<String, Object> map : dependencies )
            {
                ProjectDependency dependency = new ProjectDependency();
                dependency.setGroupId( ContinuumBuildConstant.getGroupId( map ) );
                dependency.setArtifactId( ContinuumBuildConstant.getArtifactId( map ) );
                dependency.setVersion( ContinuumBuildConstant.getVersion( map ) );

                projectDependencies.add( dependency );
            }
        }
        return projectDependencies;
    }

    private List<ProjectDeveloper> getProjectDevelopers( Map<String, Object> context )
    {
        List<ProjectDeveloper> projectDevelopers = new ArrayList<ProjectDeveloper>();

        List<Map<String, Object>> developers = ContinuumBuildConstant.getProjectDevelopers( context );

        if ( developers != null )
        {
            for ( Map<String, Object> map : developers )
            {
                ProjectDeveloper developer = new ProjectDeveloper();
                developer.setName( ContinuumBuildConstant.getDeveloperName( map ) );
                developer.setEmail( ContinuumBuildConstant.getDeveloperEmail( map ) );
                developer.setScmId( ContinuumBuildConstant.getDeveloperScmId( map ) );

                projectDevelopers.add( developer );
            }
        }
        return projectDevelopers;
    }

    private List<ProjectNotifier> getProjectNotifiers( Map<String, Object> context )
    {
        List<ProjectNotifier> projectNotifiers = new ArrayList<ProjectNotifier>();

        List<Map<String, Object>> notifiers = ContinuumBuildConstant.getProjectNotifiers( context );

        if ( notifiers != null )
        {
            for ( Map<String, Object> map : notifiers )
            {
                ProjectNotifier notifier = new ProjectNotifier();
                notifier.setConfiguration( ContinuumBuildConstant.getNotifierConfiguration( map ) );
                notifier.setEnabled( ContinuumBuildConstant.isNotifierEnabled( map ) );
                notifier.setFrom( ContinuumBuildConstant.getNotifierFrom( map ) );
                notifier.setRecipientType( ContinuumBuildConstant.getNotifierRecipientType( map ) );
                notifier.setSendOnError( ContinuumBuildConstant.isNotifierSendOnError( map ) );
                notifier.setSendOnFailure( ContinuumBuildConstant.isNotifierSendOnFailure( map ) );
                notifier.setSendOnScmFailure( ContinuumBuildConstant.isNotifierSendOnScmFailure( map ) );
                notifier.setSendOnSuccess( ContinuumBuildConstant.isNotifierSendOnSuccess( map ) );
                notifier.setSendOnWarning( ContinuumBuildConstant.isNotifierSendOnWarning( map ) );
                notifier.setType( ContinuumBuildConstant.getNotifierType( map ) );

                projectNotifiers.add( notifier );
            }
        }
        return projectNotifiers;
    }

    private String getMavenProjectVersion( Map<String, Object> context )
    {
        Map<String, Object> map = ContinuumBuildConstant.getMavenProject( context );

        if ( !map.isEmpty() )
        {
            return ContinuumBuildConstant.getVersion( map );
        }

        return null;
    }

    private List<String> getMavenProjectModules( Map<String, Object> context )
    {
        Map<String, Object> map = ContinuumBuildConstant.getMavenProject( context );

        if ( !map.isEmpty() )
        {
            return ContinuumBuildConstant.getProjectModules( map );
        }

        return null;
    }
}
