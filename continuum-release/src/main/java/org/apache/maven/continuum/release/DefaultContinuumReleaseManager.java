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

import java.io.File;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.BooleanUtils;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.release.config.ContinuumReleaseDescriptor;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.release.tasks.PerformReleaseProjectTask;
import org.apache.maven.continuum.release.tasks.PrepareReleaseProjectTask;
import org.apache.maven.continuum.release.tasks.RollbackReleaseProjectTask;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.shared.release.ReleaseManagerListener;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.config.ReleaseDescriptorStore;
import org.apache.maven.shared.release.config.ReleaseDescriptorStoreException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Jason van Zyl
 * @author Edwin Punzalan
 * @version $Id$
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
    private ScmManager scmManager;

    private Map<String, ContinuumReleaseManagerListener> listeners;

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

    public String prepare( Project project, Properties releaseProperties, Map<String, String> relVersions,
                           Map<String, String> devVersions, ContinuumReleaseManagerListener listener,
                           String workingDirectory )
        throws ContinuumReleaseException
    {
        return prepare( project, releaseProperties, relVersions, devVersions, listener, workingDirectory, null, null );
    }

    public String prepare( Project project, Properties releaseProperties, Map<String, String> relVersions,
                           Map<String, String> devVersions, ContinuumReleaseManagerListener listener,
                           String workingDirectory, Map<String, String> environments, String executable )
        throws ContinuumReleaseException
    {
        String releaseId = project.getGroupId() + ":" + project.getArtifactId();

        ReleaseDescriptor descriptor =
            getReleaseDescriptor( project, releaseProperties, relVersions, devVersions, environments, workingDirectory,
                                  executable );

        getListeners().put( releaseId, listener );

        try
        {
            prepareReleaseQueue.put(
                new PrepareReleaseProjectTask( releaseId, descriptor, (ReleaseManagerListener) listener ) );
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumReleaseException( "Failed to add prepare release task in queue.", e );
        }

        return releaseId;
    }

    public void perform( String releaseId, File buildDirectory, String goals, String arguments,
                         boolean useReleaseProfile, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException
    {
        perform( releaseId, buildDirectory, goals, arguments, useReleaseProfile, listener, null );
    }

    public void perform( String releaseId, File buildDirectory, String goals, String arguments,
                         boolean useReleaseProfile, ContinuumReleaseManagerListener listener,
                         LocalRepository repository )
        throws ContinuumReleaseException
    {
        ReleaseDescriptor descriptor = (ReleaseDescriptor) getPreparedReleases().get( releaseId );
        if ( descriptor != null )
        {
            perform( releaseId, descriptor, buildDirectory, goals, arguments, useReleaseProfile, listener, repository );
        }
    }

    public void perform( String releaseId, String workingDirectory, File buildDirectory, String goals, String arguments,
                         boolean useReleaseProfile, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException
    {
        ReleaseDescriptor descriptor = readReleaseDescriptor( workingDirectory );

        perform( releaseId, descriptor, buildDirectory, goals, arguments, useReleaseProfile, listener, null );
    }

    private void perform( String releaseId, ReleaseDescriptor descriptor, File buildDirectory, String goals,
                          String arguments, boolean useReleaseProfile, ContinuumReleaseManagerListener listener,
                          LocalRepository repository )
        throws ContinuumReleaseException
    {
        if ( descriptor != null )
        {
            descriptor.setAdditionalArguments( arguments );
        }

        try
        {
            getListeners().put( releaseId, listener );

            performReleaseQueue.put(
                new PerformReleaseProjectTask( releaseId, descriptor, buildDirectory, goals, useReleaseProfile,
                                               (ReleaseManagerListener) listener, repository ) );
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

    private ReleaseDescriptor getReleaseDescriptor( Project project, Properties releaseProperties,
                                                    Map<String, String> relVersions, Map<String, String> devVersions,
                                                    Map<String, String> environments, String workingDirectory,
                                                    String executable )
    {
        ContinuumReleaseDescriptor descriptor = new ContinuumReleaseDescriptor();

        //release properties from the project
        descriptor.setWorkingDirectory( workingDirectory );
        descriptor.setScmSourceUrl( project.getScmUrl() );

        //required properties
        descriptor.setScmReleaseLabel( releaseProperties.getProperty( "tag" ) );
        descriptor.setScmTagBase( releaseProperties.getProperty( "tagBase" ) );
        descriptor.setReleaseVersions( relVersions );
        descriptor.setDevelopmentVersions( devVersions );
        descriptor.setPreparationGoals( releaseProperties.getProperty( "prepareGoals" ) );
        descriptor.setAdditionalArguments( releaseProperties.getProperty( "arguments" ) );
        descriptor.setAddSchema( Boolean.valueOf( releaseProperties.getProperty( "addSchema" ) ) );
        descriptor.setAutoVersionSubmodules(
            Boolean.valueOf( releaseProperties.getProperty( "autoVersionSubmodules" ) ) );

        String useEditMode = releaseProperties.getProperty( "useEditMode" );
        if ( BooleanUtils.toBoolean( useEditMode ) )
        {
            descriptor.setScmUseEditMode( Boolean.valueOf( useEditMode ) );
        }

        LocalRepository repository = project.getProjectGroup().getLocalRepository();

        if ( repository != null )
        {
            String args = descriptor.getAdditionalArguments();

            if ( StringUtils.isNotEmpty( args ) )
            {
                descriptor.setAdditionalArguments( args +
                                                   " \"-Dmaven.repo.local=" + repository.getLocation() + "\"" );
            }
            else
            {
                descriptor.setAdditionalArguments( "\"-Dmaven.repo.local=" + repository.getLocation() + "\"" );
            }
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
        if ( releaseProperties.containsKey( "commentPrefix" ) )
        {
            descriptor.setScmCommentPrefix( releaseProperties.getProperty( "commentPrefix" ) );
        }
        if ( releaseProperties.containsKey( "useReleaseProfile" ) )
        {
            descriptor.setUseReleaseProfile( Boolean.valueOf( releaseProperties.getProperty( "useReleaseProfile" ) ) );
        }

        //forced properties
        descriptor.setInteractive( false );

        //set environments
        descriptor.setEnvironments( environments );
        descriptor.setExecutable( executable );

        return descriptor;
    }

    private ReleaseDescriptor readReleaseDescriptor( String workingDirectory )
        throws ContinuumReleaseException
    {
        ReleaseDescriptor descriptor = new ContinuumReleaseDescriptor();
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

    public Map<String, ContinuumReleaseManagerListener> getListeners()
    {
        if ( listeners == null )
        {
            listeners = new Hashtable<String, ContinuumReleaseManagerListener>();
        }

        return listeners;
    }

    public String sanitizeTagName( String scmUrl, String tagName )
        throws Exception
    {
        ScmRepository scmRepo = scmManager.makeScmRepository( scmUrl );
        ScmProvider scmProvider = scmManager.getProviderByRepository( scmRepo );
        return scmProvider.sanitizeTagName( tagName );
    }
}
