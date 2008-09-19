package org.apache.maven.continuum.release;

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

import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.release.config.ContinuumReleaseDescriptor;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.release.tasks.PerformReleaseProjectTask;
import org.apache.maven.continuum.release.tasks.PrepareReleaseProjectTask;
import org.apache.maven.continuum.release.tasks.RollbackReleaseProjectTask;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.apache.maven.shared.release.ReleaseManagerListener;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.config.ReleaseDescriptorStore;
import org.apache.maven.shared.release.config.ReleaseDescriptorStoreException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Jason van Zyl
 * @author Edwin Punzalan
 */
public class DefaultContinuumReleaseManager
    implements ContinuumReleaseManager
{
    /**
     * @plexus.requirement
     */
    private ReleaseDescriptorStore releaseStore;

    /**
     * @plexus.requirement
     */
    private TaskQueue prepareReleaseQueue;

    /**
     * @plexus.requirement
     */
    private TaskQueue performReleaseQueue;

    /**
     * @plexus.requirement
     */
    private TaskQueue rollbackReleaseQueue;

    /**
     * @plexus.requirement
     */
    private WorkingDirectoryService workingDirectoryService;

    /**
     * @plexus.requirement
     */
    private InstallationService installationService;
    
    private Map listeners;

    /**
     * contains previous release:prepare descriptors; one per project
     *
     * @todo remove static when singleton strategy is working
     */
    private static Map preparedReleases;

    /**
     * contains results
     *
     * @todo remove static when singleton strategy is working
     */
    private static Map releaseResults;

    public String prepare( Project project, Properties releaseProperties, Map relVersions, Map devVersions,
                           ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException
    {
        return prepare( project, releaseProperties, relVersions, devVersions, listener, null );
    }

    public String prepare( Project project, Properties releaseProperties, Map relVersions, Map devVersions,
                           ContinuumReleaseManagerListener listener, Profile profile )
        throws ContinuumReleaseException
    {
        String releaseId = project.getGroupId() + ":" + project.getArtifactId();

        ReleaseDescriptor descriptor = getReleaseDescriptor( project, releaseProperties, relVersions, devVersions, profile );

        getListeners().put( releaseId, listener );

        try
        {
            prepareReleaseQueue.put(
                new PrepareReleaseProjectTask( releaseId, descriptor, (ReleaseManagerListener) listener, profile ) );

        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumReleaseException( "Failed to add prepare release task in queue.", e );
        }

        return releaseId;
    }

    public void perform( String releaseId, File buildDirectory, String goals, boolean useReleaseProfile,
                         ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException
    {
        perform( releaseId, buildDirectory, goals, useReleaseProfile, listener, null );
    }
    
    public void perform( String releaseId, File buildDirectory, String goals, boolean useReleaseProfile,
                         ContinuumReleaseManagerListener listener, LocalRepository repository )
        throws ContinuumReleaseException
    {
        ReleaseDescriptor descriptor = (ReleaseDescriptor) getPreparedReleases().get( releaseId );
        if ( descriptor != null )
        {
            perform( releaseId, descriptor, buildDirectory, goals, useReleaseProfile, listener, repository );
        }
    }

    public void perform( String releaseId, String workingDirectory, File buildDirectory, String goals,
                         boolean useReleaseProfile, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException
    {
        ReleaseDescriptor descriptor = readReleaseDescriptor( workingDirectory );

        perform( releaseId, descriptor, buildDirectory, goals, useReleaseProfile, listener, null );
    }

    private void perform( String releaseId, ReleaseDescriptor descriptor, File buildDirectory, String goals,
                          boolean useReleaseProfile, ContinuumReleaseManagerListener listener, LocalRepository repository )
        throws ContinuumReleaseException
    {
        try
        {
            getListeners().put( releaseId, listener );

            performReleaseQueue.put( new PerformReleaseProjectTask( releaseId, descriptor, buildDirectory, goals,
                                                                    useReleaseProfile,
                                                                    (ReleaseManagerListener) listener,
                                                                    repository ) );
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumReleaseException( "Failed to add perform release task in queue.", e );
        }
    }

    public void rollback( String releaseId, String workingDirectory, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException
    {
        ReleaseDescriptor descriptor = readReleaseDescriptor( workingDirectory );

        rollback( releaseId, descriptor, listener );
    }

    private void rollback( String releaseId, ReleaseDescriptor descriptor, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException
    {
        Task releaseTask = new RollbackReleaseProjectTask( releaseId, descriptor, (ReleaseManagerListener) listener );

        try
        {
            rollbackReleaseQueue.put( releaseTask );
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumReleaseException( "Failed to rollback release.", e );
        }
    }

    public Map getPreparedReleases()
    {
        if ( preparedReleases == null )
        {
            preparedReleases = new Hashtable();
        }

        return preparedReleases;
    }

    public Map getReleaseResults()
    {
        if ( releaseResults == null )
        {
            releaseResults = new Hashtable();
        }

        return releaseResults;
    }

    public Map<String, String> getEnvironments( Profile profile )
    {
        if ( profile == null )
        {
            return Collections.EMPTY_MAP;
        }

        Map<String, String> envVars = new HashMap<String, String>();
        if ( profile == null )
        {
            return envVars;
        }

        String javaHome = getJavaHomeValue( profile );
        if ( !StringUtils.isEmpty( javaHome ) )
        {
            envVars.put( installationService.getEnvVar( InstallationService.JDK_TYPE ), javaHome );
        }

        Installation builder = profile.getBuilder();
        if ( builder != null )
        {
            envVars.put( installationService.getEnvVar( InstallationService.MAVEN2_TYPE ), builder.getVarValue() );
        }

        List<Installation> installations = profile.getEnvironmentVariables();
        for ( Installation installation : installations )
        {
            envVars.put( installation.getVarName(), installation.getVarValue() );
        }
        return envVars;
    }

    private ReleaseDescriptor getReleaseDescriptor( Project project, Properties releaseProperties, Map relVersions,
                                                    Map devVersions, Profile profile )
    {
        ContinuumReleaseDescriptor descriptor = new ContinuumReleaseDescriptor();
        String workingDirectory = workingDirectoryService.getWorkingDirectory( project ).getPath(); 

        //release properties from the project
        descriptor.setWorkingDirectory( workingDirectory );
        descriptor.setScmSourceUrl( project.getScmUrl() );

        //required properties
        descriptor.setScmReleaseLabel( releaseProperties.getProperty( "tag" ) );
        descriptor.setScmTagBase( releaseProperties.getProperty( "tagBase" ) );
        descriptor.setReleaseVersions( relVersions );
        descriptor.setDevelopmentVersions( devVersions );
        descriptor.setPreparationGoals( releaseProperties.getProperty( "prepareGoals" ) );
        
        LocalRepository repository = project.getProjectGroup().getLocalRepository();
        
        if ( repository != null )
        {
            descriptor.setAdditionalArguments( "\"-Dmaven.repo.local=" + repository.getLocation() + "\"" );
        }
        
        //other properties
        if ( releaseProperties.containsKey( "username" ) )
        {
            descriptor.setScmUsername( releaseProperties.getProperty( "username" ) );
        }
        if ( releaseProperties.containsKey( "password" ) )
        {
            descriptor.setScmPassword( releaseProperties.getProperty( "password" ) );
        }

        //forced properties
        descriptor.setInteractive( false );
        
        //set environments
        descriptor.setEnvironments( getEnvironments( profile ) );
        
        return descriptor;
    }

    private ReleaseDescriptor readReleaseDescriptor( String workingDirectory )
        throws ContinuumReleaseException
    {
        ReleaseDescriptor descriptor = new ReleaseDescriptor();
        descriptor.setWorkingDirectory( workingDirectory );

        try
        {
            descriptor = releaseStore.read( descriptor );
        }
        catch ( ReleaseDescriptorStoreException e )
        {
            throw new ContinuumReleaseException( "Failed to parse descriptor file.", e );
        }

        return descriptor;
    }

    public Map getListeners()
    {
        if ( listeners == null )
        {
            listeners = new Hashtable();
        }

        return listeners;
    }

    private String getJavaHomeValue( Profile profile )
    {
        Installation jdk = profile.getJdk();
        if ( jdk == null )
        {
            return null;
        }
        return jdk.getVarValue();
    }
}
