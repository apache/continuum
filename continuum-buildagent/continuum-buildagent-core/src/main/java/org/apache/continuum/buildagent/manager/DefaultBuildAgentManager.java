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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.distributed.transport.master.MasterBuildAgentTransportClient;
import org.apache.maven.continuum.ContinuumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @plexus.component role="org.apache.continuum.buildagent.manager.BuildAgentManager" role-hint="default"
 */
public class DefaultBuildAgentManager
    implements BuildAgentManager
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    /**
     * @plexus.requirement
     */
    private BuildAgentConfigurationService buildAgentConfigurationService;

    public void startProjectBuild( int projectId )
        throws ContinuumException
    {
        try
        {
            MasterBuildAgentTransportClient client = new MasterBuildAgentTransportClient(
                new URL( buildAgentConfigurationService.getContinuumServerUrl() ) );
            client.startProjectBuild( projectId );
        }
        catch ( MalformedURLException e )
        {
            log.error( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
            throw new ContinuumException( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
        }
        catch ( Exception e )
        {
            log.error( "Error starting project build", e );
            throw new ContinuumException( "Error starting project build", e );
        }
    }

    public void returnBuildResult( Map buildResult )
        throws ContinuumException
    {
        try
        {
            MasterBuildAgentTransportClient client = new MasterBuildAgentTransportClient(
                new URL( buildAgentConfigurationService.getContinuumServerUrl() ) );
            client.returnBuildResult( buildResult );
        }
        catch ( MalformedURLException e )
        {
            log.error( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
            throw new ContinuumException( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
        }
        catch ( Exception e )
        {
            log.error( "Error while returning build result to the continuum server", e );
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    public Map<String, String> getEnvironments( int buildDefinitionId, String installationType )
        throws ContinuumException
    {
        try
        {
            MasterBuildAgentTransportClient client = new MasterBuildAgentTransportClient(
                new URL( buildAgentConfigurationService.getContinuumServerUrl() ) );
            return client.getEnvironments( buildDefinitionId, installationType );
        }
        catch ( MalformedURLException e )
        {
            log.error( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
            throw new ContinuumException( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
        }
        catch ( Exception e )
        {
            log.error( "Error while retrieving environments for build definition " + buildDefinitionId, e );
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    public void updateProject( Map project )
        throws ContinuumException
    {
        try
        {
            MasterBuildAgentTransportClient client = new MasterBuildAgentTransportClient(
                new URL( buildAgentConfigurationService.getContinuumServerUrl() ) );
            client.updateProject( project );
        }
        catch ( MalformedURLException e )
        {
            log.error( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
            throw new ContinuumException( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
        }
        catch ( Exception e )
        {
            log.error( "Error while updating project", e );
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    public boolean shouldBuild( Map context )
        throws ContinuumException
    {
        try
        {
            MasterBuildAgentTransportClient client = new MasterBuildAgentTransportClient(
                new URL( buildAgentConfigurationService.getContinuumServerUrl() ) );
            return client.shouldBuild( context );
        }
        catch ( MalformedURLException e )
        {
            log.error( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
            throw new ContinuumException( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
        }
        catch ( Exception e )
        {
            log.error( "Failed to determine if project should build", e );
            throw new ContinuumException( "Failed to determine if project should build", e );
        }
    }

    public void startPrepareBuild( Map context )
        throws ContinuumException
    {
        try
        {
            MasterBuildAgentTransportClient client = new MasterBuildAgentTransportClient(
                new URL( buildAgentConfigurationService.getContinuumServerUrl() ) );
            client.startPrepareBuild( context );
        }
        catch ( MalformedURLException e )
        {
            log.error( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
            throw new ContinuumException( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'", e );
        }
        catch ( Exception e )
        {
            log.error( "Error starting prepare build", e );
            throw new ContinuumException( "Error starting prepare build", e );
        }
    }

    public void endPrepareBuild( Map context )
        throws ContinuumException
    {
        try
        {
            MasterBuildAgentTransportClient client = new MasterBuildAgentTransportClient(
                 new URL( buildAgentConfigurationService.getContinuumServerUrl() ) );
            client.prepareBuildFinished( context );
        }
        catch ( MalformedURLException e )
        {
            throw new ContinuumException( "Invalid Continuum Server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Error while finishing prepare build", e );
        }
    }
}
