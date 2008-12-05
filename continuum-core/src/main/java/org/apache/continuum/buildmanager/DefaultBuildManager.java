package org.apache.continuum.buildmanager;

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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.continuum.taskqueue.OverallBuildQueue;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;

/**
 * DefaultBuildManager
 * 
 * Manages the overall build queues and designates to which "overall" build queue a task will be 
 * queued. When parallel builds are enabled, a number of "overall" build queues (depending on what is
 * set in the configuration) will be utilized for the builds. Otherwise, only one "overall" build 
 * queue will be utilized.
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @plexus.component role="org.apache.continuum.buildmanager.BuildManager"
 */
public class DefaultBuildManager
    extends AbstractLogEnabled
    implements BuildManager, Contextualizable
{   
    // TODO: the number of "overall" build queues should be dynamic & sensitive to the
    //          changes in configuration!
    
    private List<OverallBuildQueue> overallBuildQueuesInUse;
    
    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;
    
    private PlexusContainer container;
    
    public DefaultBuildManager()
    {
        overallBuildQueuesInUse = new ArrayList<OverallBuildQueue>();        
    }

    // should be invoked before adding anything in any of the queues!
    private synchronized OverallBuildQueue getOverallBuildQueue()
        throws ComponentLookupException, TaskQueueException
    {
        OverallBuildQueue overallBuildQueue = null;
                
        int parallelBuildsNum = configurationService.getNumberOfBuildsInParallel();
        if( parallelBuildsNum <= 0 )
        {
            parallelBuildsNum = 1;
        }
        
        if( overallBuildQueuesInUse.size() < parallelBuildsNum )
        {   
            overallBuildQueue = ( OverallBuildQueue ) container.lookup( OverallBuildQueue.class );            
            overallBuildQueue.setId( overallBuildQueuesInUse.size() + 1 );
            
            overallBuildQueuesInUse.add( overallBuildQueue );            
        }       
        else
        {            
            int size = 0;            
            for( OverallBuildQueue overallBuildQueueInUse : overallBuildQueuesInUse )
            {   
                // TODO: must differentiate between checkout, prepare-build & build queues!
                List<TaskQueue> tasks = overallBuildQueueInUse.getBuildQueue().getQueueSnapshot();
                if( tasks != null )
                {   
                    if( size == 0 || tasks.size() < size )
                    {
                        overallBuildQueue = overallBuildQueueInUse;
                        size = tasks.size();
                    }                    
                }
            }
        }
        
        return overallBuildQueue;
    }
    
    public OverallBuildQueue getOverallBuildQueueWhereProjectIsQueued( int projectId )
        throws BuildManagerException
    {
        try
        {
            for( OverallBuildQueue overallBuildQueueInUse : overallBuildQueuesInUse )
            {                   
                if( overallBuildQueueInUse.isInBuildQueue( projectId ) )
                {   
                    return overallBuildQueueInUse;                    
                }
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while retrieving project in build queue: " +
                e.getMessage() );
        }
        
        return null;
    }
    
    public List<OverallBuildQueue> getOverallBuildQueuesInUse()
    {
        return overallBuildQueuesInUse;
    }
    
    public void addProjectToBuildQueue( int projectId, BuildDefinition buildDefinition, int trigger, String projectName,
                                        String buildDefLabel )
        throws BuildManagerException
    {   
        if( getOverallBuildQueueWhereProjectIsQueued( projectId ) == null )
        {
            BuildProjectTask task =
                new BuildProjectTask( projectId, buildDefinition.getId(), trigger, projectName, buildDefLabel );
            
            task.setMaxExecutionTime( buildDefinition.getSchedule().getMaxJobExecutionTime() * 1000 );
         
            try
            {
                OverallBuildQueue overallBuildQueue = getOverallBuildQueue();
                overallBuildQueue.addToBuildQueue( task );
            }
            catch ( ComponentLookupException e )
            {
                throw new BuildManagerException( e.getMessage() );
            }        
            catch ( TaskQueueException e )
            {
                throw new BuildManagerException( e.getMessage() );
            }
        }
        else
        {
            getLogger().warn( "Project '" + projectName + "' is already queued." );
        }
    }

    public void addProjectToCheckoutQueue( int id, File workingDirectory, String projectName,
                                           String projectScmUsername, String projectScmPassword )
        throws BuildManagerException
    {
        
    }

    public void addProjectToPrepareBuildQueue( Map<Integer, Integer> projectsBuildDefinitionsMap, int trigger )
        throws BuildManagerException
    {
        
    }

    public void addProjectsToPrepareBuildQueue( Collection<Map<Integer, Integer>> projectsBuildDefinitions, int trigger )
        throws BuildManagerException
    {
        // TODO Auto-generated method stub

    }

    public void cancelAllBuilds()
        throws BuildManagerException
    {
        // TODO Auto-generated method stub

    }

    public void cancelAllCheckouts()
        throws BuildManagerException
    {
        // TODO Auto-generated method stub

    }

    public void cancelProjectBuild( int projectId )
        throws BuildManagerException
    {
        // TODO Auto-generated method stub

    }

    public void cancelProjectCheckout( int projectId )
        throws BuildManagerException
    {
        // TODO Auto-generated method stub

    }

    public void removeProjectFromBuildQueue( int projectId, int buildDefinitionId, int trigger, String projectName )
        throws BuildManagerException
    {
        // TODO Auto-generated method stub

    }

    public void removeProjectFromCheckoutQueue( int projectId )
        throws BuildManagerException
    {
        // TODO Auto-generated method stub

    }

    public void removeProjectsFromBuildQueue( int[] projectIds )
        throws BuildManagerException
    {
        // TODO Auto-generated method stub

    }

    public void removeProjectsFromCheckoutQueue( int[] projectIds )
        throws BuildManagerException
    {
        // TODO Auto-generated method stub

    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
    
    public void setContainer( PlexusContainer container )
    {
        this.container = container;
    }
    
    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }
}
