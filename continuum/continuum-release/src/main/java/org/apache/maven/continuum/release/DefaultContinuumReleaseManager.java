package org.apache.maven.continuum.release;

/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.release.tasks.PerformReleaseProjectTask;
import org.apache.maven.continuum.release.tasks.PrepareReleaseProjectTask;
import org.apache.maven.plugins.release.config.ReleaseDescriptor;
import org.apache.maven.plugins.release.config.io.xpp3.ReleaseDescriptorXpp3Reader;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.HashMap;

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
    private TaskQueue prepareReleaseQueue;

    /**
     * @plexus.requirement
     */
    private TaskQueue performReleaseQueue;

    /**
     * contains previous release:prepare descriptors; one per project
     */
    private Map preparedReleases;

    public void prepare( Project project, Properties releaseProperties, Map relVersions, Map devVersions )
        throws ContinuumReleaseException
    {
        String releaseId = project.getGroupId() + ":" + project.getArtifactId();

        ReleaseDescriptor descriptor = getReleaseDescriptor( project, releaseProperties, relVersions, devVersions );

        try
        {
            prepareReleaseQueue.put( new PrepareReleaseProjectTask( releaseId, descriptor ) );
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumReleaseException( "Failed to add prepare release task in queue.", e );
        }
    }

    public void perform( String releaseId, File buildDirectory, String goals, boolean useReleaseProfile )
        throws ContinuumReleaseException
    {
        ReleaseDescriptor descriptor = (ReleaseDescriptor) preparedReleases.get( releaseId );
        if ( descriptor != null )
        {
            perform( releaseId, descriptor, buildDirectory, goals, useReleaseProfile );
        }
    }

    public void perform( String releaseId, File descriptorFile, File buildDirectory,
                         String goals, boolean useReleaseProfile )
        throws ContinuumReleaseException
    {
        ReleaseDescriptor descriptor;
        try
        {
            descriptor = new ReleaseDescriptorXpp3Reader().read( new FileReader( descriptorFile ) );
        }
        catch ( IOException e )
        {
            throw new ContinuumReleaseException( "Failed to parse descriptor file.", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ContinuumReleaseException( "Failed to parse descriptor file.", e );
        }

        perform( releaseId, descriptor, buildDirectory, goals, useReleaseProfile );
    }

    private void perform( String releaseId, ReleaseDescriptor descriptor, File buildDirectory,
                          String goals, boolean useReleaseProfile )
        throws ContinuumReleaseException
    {
        try
        {
            performReleaseQueue.put( new PerformReleaseProjectTask( releaseId, descriptor, buildDirectory,
                                                                    goals, useReleaseProfile ) );
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumReleaseException( "Failed to add perform release task in queue.", e );
        }
    }

    public Map getPreparedReleases()
    {
        if ( preparedReleases == null )
        {
            preparedReleases = new HashMap();
        }

        return preparedReleases;
    }

    public void setPreparedReleases( Map preparedReleases )
    {
        this.preparedReleases = preparedReleases;
    }

    private ReleaseDescriptor getReleaseDescriptor( Project project, Properties releaseProperties,
                                                    Map relVersions, Map devVersions )
    {
        ReleaseDescriptor descriptor = new ReleaseDescriptor();

        //release properties from the project
        descriptor.setScmUsername( project.getScmUsername() );
        descriptor.setScmPassword( project.getScmPassword() );
        descriptor.setWorkingDirectory( project.getWorkingDirectory() );
        descriptor.setScmSourceUrl( project.getScmUrl() );

        //required properties
        descriptor.setScmReleaseLabel( releaseProperties.getProperty( "tag" ) );
        descriptor.setScmTagBase( releaseProperties.getProperty( "tagBase" ) );
        descriptor.setReleaseVersions( relVersions );
        descriptor.setDevelopmentVersions( devVersions );

        //forced properties
        descriptor.setInteractive( false );

        return descriptor;
    }
}
