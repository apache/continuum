package org.apache.continuum.buildagent.manager;

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

import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.installation.BuildAgentInstallationService;
import org.apache.continuum.buildagent.model.Installation;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.release.config.ContinuumReleaseDescriptor;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.release.ContinuumReleaseManagerListener;
import org.apache.maven.continuum.release.DefaultReleaseManagerListener;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @plexus.component role="org.apache.continuum.buildagent.manager.BuildAgentReleaseManager" role-hint="default"
 */
public class DefaultBuildAgentReleaseManager
    implements BuildAgentReleaseManager
{
    private static final Logger log = LoggerFactory.getLogger( DefaultBuildAgentReleaseManager.class );

    /**
     * @plexus.requirement
     */
    ContinuumReleaseManager releaseManager;

    /**
     * @plexus.requirement
     */
    BuildAgentConfigurationService buildAgentConfigurationService;

    /**
     * @plexus.requirement
     */
    BuildAgentInstallationService buildAgentInstallationService;

    public String releasePrepare( Map<String, Object> projectMap, Properties releaseProperties,
                                  Map<String, String> releaseVersion, Map<String, String> developmentVersion,
                                  Map<String, String> environments, String username )
        throws ContinuumReleaseException
    {
        Project project = getProject( projectMap );

        ContinuumReleaseManagerListener listener = new DefaultReleaseManagerListener();

        listener.setUsername( username );

        String workingDirectory = buildAgentConfigurationService.getWorkingDirectory( project.getId() ).getPath();

        String executable = buildAgentInstallationService.getExecutorConfigurator(
            BuildAgentInstallationService.MAVEN2_TYPE ).getExecutable();

        if ( environments == null )
        {
            environments = new HashMap<String, String>();
        }

        // get environments from Slave (Build Agent)
        List<Installation> installations = buildAgentConfigurationService.getAvailableInstallations();

        if ( installations != null )
        {
            for ( Installation installation : installations )
            {
                // combine environments (Master and Slave); Slave's environments overwrite Master's environments
                environments.put( installation.getVarName(), installation.getVarValue() );
            }
        }

        if ( environments != null )
        {
            String m2Home = environments.get( buildAgentInstallationService.getEnvVar(
                BuildAgentInstallationService.MAVEN2_TYPE ) );
            if ( StringUtils.isNotEmpty( m2Home ) )
            {
                executable = m2Home + File.separator + "bin" + File.separator + executable;
            }
        }

        try
        {
            return releaseManager.prepare( project, releaseProperties, releaseVersion, developmentVersion, listener,
                                           workingDirectory, environments, executable );
        }
        catch ( ContinuumReleaseException e )
        {
            log.error( "Error while preparing release", e );
            throw e;
        }
    }

    public ReleaseResult getReleaseResult( String releaseId )
    {
        return (ReleaseResult) releaseManager.getReleaseResults().get( releaseId );
    }

    public Map<String, Object> getListener( String releaseId )
    {
        ContinuumReleaseManagerListener listener = (ContinuumReleaseManagerListener) releaseManager.getListeners().get(
            releaseId );

        Map<String, Object> map = new HashMap<String, Object>();

        if ( listener != null )
        {
            map.put( ContinuumBuildAgentUtil.KEY_RELEASE_STATE, listener.getState() );

            map.put( ContinuumBuildAgentUtil.KEY_USERNAME, listener.getUsername() );

            if ( listener.getPhases() != null )
            {
                map.put( ContinuumBuildAgentUtil.KEY_RELEASE_PHASES, listener.getPhases() );
            }
            if ( listener.getCompletedPhases() != null )
            {
                map.put( ContinuumBuildAgentUtil.KEY_COMPLETED_RELEASE_PHASES, listener.getCompletedPhases() );
            }
            if ( listener.getInProgress() != null )
            {
                map.put( ContinuumBuildAgentUtil.KEY_RELEASE_IN_PROGRESS, listener.getInProgress() );
            }
            if ( listener.getError() != null )
            {
                map.put( ContinuumBuildAgentUtil.KEY_RELEASE_ERROR, listener.getError() );
            }
        }

        return map;
    }

    public void removeListener( String releaseId )
    {
        releaseManager.getListeners().remove( releaseId );
    }

    public String getPreparedReleaseName( String releaseId )
    {
        Map preparedReleases = releaseManager.getPreparedReleases();

        if ( preparedReleases.containsKey( releaseId ) )
        {
            ReleaseDescriptor descriptor = (ReleaseDescriptor) preparedReleases.get( releaseId );
            return descriptor.getReleaseVersions().get( releaseId ).toString();
        }

        return "";
    }

    @SuppressWarnings( "unchecked" )
    public void releasePerform( String releaseId, String goals, String arguments, boolean useReleaseProfile,
                                Map repository, String username )
        throws ContinuumReleaseException
    {
        ContinuumReleaseManagerListener listener = new DefaultReleaseManagerListener();

        listener.setUsername( username );

        LocalRepository repo = null;

        if ( !repository.isEmpty() )
        {
            List<org.apache.continuum.buildagent.model.LocalRepository> localRepos =
                buildAgentConfigurationService.getLocalRepositories();
            for ( org.apache.continuum.buildagent.model.LocalRepository localRepo : localRepos )
            {
                if ( localRepo.getName().equalsIgnoreCase( ContinuumBuildAgentUtil.getLocalRepositoryName(
                    repository ) ) )
                {
                    repo = new LocalRepository();
                    repo.setLayout( localRepo.getLayout() );
                    repo.setName( localRepo.getName() );
                    repo.setLocation( localRepo.getLocation() );

                    break;
                }
            }
        }

        File performDirectory = new File( buildAgentConfigurationService.getWorkingDirectory(),
                                          "releases-" + System.currentTimeMillis() );
        performDirectory.mkdirs();

        releaseManager.perform( releaseId, performDirectory, goals, arguments, useReleaseProfile, listener, repo );
    }

    public String releasePerformFromScm( String goals, String arguments, boolean useReleaseProfile, Map repository,
                                         String scmUrl, String scmUsername, String scmPassword, String scmTag,
                                         String scmTagBase, Map<String, String> environments, String username )
        throws ContinuumReleaseException
    {
        ContinuumReleaseDescriptor descriptor = new ContinuumReleaseDescriptor();
        descriptor.setScmSourceUrl( scmUrl );
        descriptor.setScmUsername( scmUsername );
        descriptor.setScmPassword( scmPassword );
        descriptor.setScmReleaseLabel( scmTag );
        descriptor.setScmTagBase( scmTagBase );
        descriptor.setEnvironments( environments );

        String releaseId = "";

        do
        {
            releaseId = String.valueOf( System.currentTimeMillis() );
        }
        while ( releaseManager.getPreparedReleases().containsKey( releaseId ) );

        releaseManager.getPreparedReleases().put( releaseId, descriptor );

        releasePerform( releaseId, goals, arguments, useReleaseProfile, repository, username );

        return releaseId;
    }

    public String releaseCleanup( String releaseId )
    {
        releaseManager.getReleaseResults().remove( releaseId );

        ContinuumReleaseManagerListener listener =
            (ContinuumReleaseManagerListener) releaseManager.getListeners().remove( releaseId );

        if ( listener != null )
        {
            return listener.getGoalName() + "Finished";
        }
        else
        {
            return "";
        }
    }

    public void releaseRollback( String releaseId, int projectId )
        throws ContinuumReleaseException
    {
        ContinuumReleaseManagerListener listener = new DefaultReleaseManagerListener();

        releaseManager.rollback( releaseId, buildAgentConfigurationService.getWorkingDirectory( projectId ).getPath(),
                                 listener );

        //recurse until rollback is finished
        while ( listener.getState() != ContinuumReleaseManagerListener.FINISHED )
        {
            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException e )
            {
                //do nothing
            }
        }

        releaseManager.getPreparedReleases().remove( releaseId );

        if ( StringUtils.isNotBlank( listener.getError() ) )
        {
            throw new ContinuumReleaseException( "Failed to rollback release: " + listener.getError() );
        }
    }

    private Project getProject( Map<String, Object> context )
    {
        Project project = new Project();

        project.setId( ContinuumBuildAgentUtil.getProjectId( context ) );
        project.setGroupId( ContinuumBuildAgentUtil.getGroupId( context ) );
        project.setArtifactId( ContinuumBuildAgentUtil.getArtifactId( context ) );
        project.setScmUrl( ContinuumBuildAgentUtil.getScmUrl( context ) );

        ProjectGroup group = new ProjectGroup();

        String localRepo = ContinuumBuildAgentUtil.getLocalRepositoryName( context );

        if ( StringUtils.isBlank( localRepo ) )
        {
            group.setLocalRepository( null );
        }
        else
        {
            LocalRepository localRepository = new LocalRepository();
            List<org.apache.continuum.buildagent.model.LocalRepository> localRepos =
                buildAgentConfigurationService.getLocalRepositories();
            for ( org.apache.continuum.buildagent.model.LocalRepository localRepoBA : localRepos )
            {
                if ( localRepoBA.getName().equalsIgnoreCase( localRepo ) )
                {
                    localRepository.setLocation( localRepoBA.getLocation() );
                    group.setLocalRepository( localRepository );
                    break;
                }
            }
        }

        project.setProjectGroup( group );

        return project;
    }

    public void setBuildAgentConfigurationService( BuildAgentConfigurationService buildAgentConfigurationService )
    {
        this.buildAgentConfigurationService = buildAgentConfigurationService;
    }

    public ContinuumReleaseManager getReleaseManager()
    {
        return releaseManager;
    }
}
