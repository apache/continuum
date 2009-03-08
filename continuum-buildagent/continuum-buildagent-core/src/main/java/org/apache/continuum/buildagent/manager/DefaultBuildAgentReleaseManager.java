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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.installation.BuildAgentInstallationService;
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

/**
 * @plexus.component role="org.apache.continuum.buildagent.manager.BuildAgentReleaseManager" role-hint="default"
 */
public class DefaultBuildAgentReleaseManager
    implements BuildAgentReleaseManager
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

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

    public String releasePrepare( Map projectMap, Map properties, Map releaseVersion, Map developmentVersion, Map<String, String> environments )
        throws ContinuumReleaseException
    {
        Project project = getProject( projectMap );

        Properties releaseProperties = getReleaseProperties( properties );

        ContinuumReleaseManagerListener listener = new DefaultReleaseManagerListener();

        String workingDirectory = buildAgentConfigurationService.getWorkingDirectory( project.getId() ).getPath();

        String executable = buildAgentInstallationService.getExecutorConfigurator( BuildAgentInstallationService.MAVEN2_TYPE ).getExecutable();

        if ( environments != null )
        {
            String m2Home = environments.get( buildAgentInstallationService.getEnvVar( BuildAgentInstallationService.MAVEN2_TYPE ) );
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
            log.error( "Error while preparing release" );
            throw e;
        }
    }

    public ReleaseResult getReleaseResult( String releaseId )
    {
        return (ReleaseResult) releaseManager.getReleaseResults().get( releaseId );
    }

    public Map getListener( String releaseId )
    {
        ContinuumReleaseManagerListener listener = (ContinuumReleaseManagerListener) releaseManager.getListeners().get( releaseId );

        Map map = new HashMap();

        if ( listener != null )
        { 
            map.put( ContinuumBuildAgentUtil.KEY_RELEASE_STATE, new Integer( listener.getState() ) );
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

    public void releasePerform( String releaseId, String goals, String arguments, boolean useReleaseProfile, Map repository )
        throws ContinuumReleaseException
    {
        ContinuumReleaseManagerListener listener = new DefaultReleaseManagerListener();

        LocalRepository repo = null;

        if ( !repository.isEmpty() )
        {
            repo = new LocalRepository();
            repo.setLayout( ContinuumBuildAgentUtil.getLocalRepositoryLayout( repository ) );
            repo.setName( ContinuumBuildAgentUtil.getLocalRepositoryName( repository ) );
            repo.setLocation( ContinuumBuildAgentUtil.getLocalRepository( repository ) );
        }

        File performDirectory = new File( buildAgentConfigurationService.getWorkingDirectory(),
                                          "releases-" + System.currentTimeMillis() );
        performDirectory.mkdirs();

        releaseManager.perform( releaseId, performDirectory, goals, arguments, useReleaseProfile, listener, repo );
    }

    public void releasePerformFromScm( String goals, String arguments, boolean useReleaseProfile, Map repository, String scmUrl, String scmUsername, 
                                String scmPassword, String scmTag, String scmTagBase, Map<String, String> environments )
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

        releasePerform( releaseId, goals, arguments, useReleaseProfile, repository );
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

    private Project getProject( Map context )
    {
        Project project = new Project();

        project.setId( ContinuumBuildAgentUtil.getProjectId( context ) );
        project.setGroupId( ContinuumBuildAgentUtil.getGroupId( context ) );
        project.setArtifactId( ContinuumBuildAgentUtil.getArtifactId( context ) );
        project.setScmUrl( ContinuumBuildAgentUtil.getScmUrl( context ) );

        ProjectGroup group = new ProjectGroup();

        String localRepo = ContinuumBuildAgentUtil.getLocalRepository( context );
        if ( StringUtils.isBlank( localRepo ) )
        {
            group.setLocalRepository( null );
        }
        else
        {
            LocalRepository localRepository = new LocalRepository();
            localRepository.setLocation( localRepo );
            group.setLocalRepository( localRepository );
        }

        project.setProjectGroup( group );

        return project;
    }

    private Properties getReleaseProperties( Map context )
    {
        Properties props = new Properties();

        String prop = ContinuumBuildAgentUtil.getScmUsername( context );
        if ( StringUtils.isNotBlank( prop ) )
        {
            props.put( "username", prop );
        }

        prop = ContinuumBuildAgentUtil.getScmPassword( context );
        if ( StringUtils.isNotBlank( prop ) )
        {
            props.put( "password", prop );
        }

        prop = ContinuumBuildAgentUtil.getScmTagBase( context );
        if ( StringUtils.isNotBlank( prop ) )
        {
            props.put( "tagBase", prop );
        }

        prop = ContinuumBuildAgentUtil.getScmCommentPrefix( context );
        if ( StringUtils.isNotBlank( prop ) );
        {
            props.put( "commentPrefix", prop );
        }

        prop = ContinuumBuildAgentUtil.getScmTag( context );
        if ( StringUtils.isNotBlank( prop ) )
        {
            props.put( "tag", prop );
        }
        
        prop = ContinuumBuildAgentUtil.getPrepareGoals( context );
        if ( StringUtils.isNotBlank( prop ) )
        {
            props.put( "prepareGoals", prop );
        }

        prop = ContinuumBuildAgentUtil.getArguments( context );
        if ( StringUtils.isNotBlank( prop ) )
        {
            props.put( "arguments", prop );
        }

        prop = ContinuumBuildAgentUtil.getUseEditMode( context );
        if ( StringUtils.isNotBlank( prop ) )
        {
            props.put( "useEditMode", prop );
        }

        prop = ContinuumBuildAgentUtil.getAddSchema( context );
        if ( StringUtils.isNotBlank( prop ) )
        {
            props.put( "addSchema", prop );
        }

        prop = ContinuumBuildAgentUtil.getAutoVersionSubmodules( context );
        if ( StringUtils.isNotBlank( prop ) )
        {
            props.put( "autoVersionSubmodules", prop );
        }
        return props;
    }

    
}
