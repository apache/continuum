package org.apache.maven.continuum.web.action.admin;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.CheckOutTask;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.exception.AuthenticationRequiredException;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.web.model.DistributedBuildSummary;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="queues"
 * @since 24 sept. 07
 */
public class QueuesAction
    extends ContinuumActionSupport
    implements SecureAction, LogEnabled
{
    private static final String DISTRIBUTED_BUILD_SUCCESS = "distributed-build-success";

    /**
     * @plexus.requirement role-hint='build-project'
     */
    private TaskQueueExecutor taskQueueExecutor;    
    
    private BuildProjectTask currentBuildProjectTask;
    
    private List<BuildProjectTask> buildProjectTasks;
    
    private List<String> selectedBuildTaskHashCodes;
    
    /**
     * @plexus.requirement role-hint='check-out-project'
     */    
    private TaskQueueExecutor checkoutTaskQueueExecutor; 
    
    private CheckOutTask currentCheckOutTask;
    
    private List<CheckOutTask> currentCheckOutTasks;
    
    private List<String> selectedCheckOutTaskHashCodes;
    
    private int buildDefinitionId;

    private int projectId;

    private int trigger;

    private String projectName;

    /**
     * @plexus.requirement
     */
    private TaskQueueManager taskQueueManager;

    /**
     * @plexus.requirement
     */
    DistributedBuildManager distributedBuildManager;

    private List<DistributedBuildSummary> distributedBuildSummary;

    private List<PrepareBuildProjectsTask> distributedBuildQueues;

    private String buildAgentUrl;

    private int projectGroupId;

    private String scmRootAddress;

    private List<String> selectedDistributedBuildTaskHashCodes;

    // -----------------------------------------------------
    //  webwork
    // -----------------------------------------------------     

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public String getScmRootAddress()
    {
        return scmRootAddress;
    }

    public void setScmRootAddress( String scmRootAddress )
    {
        this.scmRootAddress = scmRootAddress;
    }

    public String cancelCurrent()
        throws Exception
    {
        try 
        {
            checkManageQueuesAuthorization();
        }
        catch( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;
        }

        taskQueueManager.cancelBuildTask( projectId );

        return SUCCESS;
    }
    
    public String removeCheckout()
        throws Exception
    {
        try 
        {
            checkManageQueuesAuthorization();
        }
        catch( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;
        }
            
        taskQueueManager.removeProjectFromCheckoutQueue( projectId );
        return SUCCESS;
    }

    public String cancelCurrentCheckout()
        throws Exception
    {
        try 
        {
            checkManageQueuesAuthorization();
        }
        catch( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;
        }
        
        taskQueueManager.cancelCheckout( projectId );
        return SUCCESS;
    }
    
    public String display()
        throws Exception
    {
        Continuum continuum = getContinuum();

        if ( continuum.getConfiguration().isDistributedBuildEnabled() )
        {
            distributedBuildSummary = new ArrayList<DistributedBuildSummary>();

            Map<String, PrepareBuildProjectsTask> map = distributedBuildManager.getDistributedBuildProjects();

            for ( String url : map.keySet() )
            {
               PrepareBuildProjectsTask task = map.get( url );
               
               ProjectGroup projectGroup = continuum.getProjectGroup( task.getProjectGroupId() );
               
               DistributedBuildSummary summary = new DistributedBuildSummary();
               summary.setUrl( url );
               summary.setProjectGroupId( task.getProjectGroupId() );
               summary.setProjectGroupName( projectGroup.getName() );
               summary.setScmRootAddress( task.getScmRootAddress() );
               
               ProjectScmRoot scmRoot = continuum.getProjectScmRootByProjectGroupAndScmRootAddress( task.getProjectGroupId(), 
                                                                                                    task.getScmRootAddress() );
               if ( scmRoot.getState() == ContinuumProjectState.UPDATING )
               {
                   summary.setCancelEnabled( false );
               }
               else
               {
                   summary.setCancelEnabled( true );
               }

               distributedBuildSummary.add( summary );
            }

            distributedBuildQueues = taskQueueManager.getDistributedBuildProjectsInQueue();

            return DISTRIBUTED_BUILD_SUCCESS;
        }
        else
        {
            this.setCurrentBuildProjectTask( (BuildProjectTask) taskQueueExecutor.getCurrentTask() );        
            this.setBuildProjectTasks( taskQueueManager.getProjectsInBuildQueue() );
            this.setCurrentCheckOutTask( (CheckOutTask) checkoutTaskQueueExecutor.getCurrentTask() );
            this.setCurrentCheckOutTasks( taskQueueManager.getCheckOutTasksInQueue() );
            return SUCCESS;
        }
    }

    public String remove()
        throws Exception
    {
        try 
        {
            checkManageQueuesAuthorization();
        }
        catch( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;
        }
        
        taskQueueManager.removeFromBuildingQueue( projectId, buildDefinitionId, trigger, projectName );
        Project project = getContinuum().getProject( projectId );
        project.setState( project.getOldState() );
        getContinuum().updateProject( project );

        return SUCCESS;
    }
    
    public String removeBuildEntries()
        throws Exception
    {
        try 
        {
            checkManageQueuesAuthorization();
        }
        catch( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;
        }
        
        taskQueueManager.removeProjectsFromBuildingQueueWithHashCodes( listToIntArray(this.getSelectedBuildTaskHashCodes()) );
        return SUCCESS;
    }

    public String removeCheckoutEntries()
        throws Exception
    {
        try 
        {
            checkManageQueuesAuthorization();
        }
        catch( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;
        }
        
        taskQueueManager
            .removeTasksFromCheckoutQueueWithHashCodes( listToIntArray( this.getSelectedCheckOutTaskHashCodes() ) );
        return SUCCESS;
    }

    public String cancelDistributedBuild()
        throws Exception
    {
        try 
        {
            checkManageQueuesAuthorization();
        }
        catch( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;
        }

        distributedBuildManager.cancelDistributedBuild( buildAgentUrl, projectGroupId, scmRootAddress );

        return SUCCESS;
    }

    public String removeDistributedBuildEntry()
        throws Exception
    {
        try 
        {
            checkManageQueuesAuthorization();
        }
        catch( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;
        }

        taskQueueManager.removeFromDistributedBuildQueue( projectGroupId, scmRootAddress );

        return SUCCESS;
    }

    public String removeDistributedBuildEntries()
        throws Exception
    {
        try 
        {
            checkManageQueuesAuthorization();
        }
        catch( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;
        }

        taskQueueManager
            .removeTasksFromDistributedBuildQueueWithHashCodes( listToIntArray( this.getSelectedDistributedBuildTaskHashCodes() ) );

        return SUCCESS;
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
    
    
    // -----------------------------------------------------
    //  security
    // -----------------------------------------------------    

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_VIEW_QUEUES, Resource.GLOBAL );

        return bundle;
    }

    public List<BuildProjectTask> getBuildProjectTasks()
    {
        return buildProjectTasks;
    }

    public void setBuildProjectTasks( List<BuildProjectTask> buildProjectTasks )
    {
        this.buildProjectTasks = buildProjectTasks;
    }

    public int getBuildDefinitionId()
    {
        return buildDefinitionId;
    }

    public void setBuildDefinitionId( int buildDefinitionId )
    {
        this.buildDefinitionId = buildDefinitionId;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public int getTrigger()
    {
        return trigger;
    }

    public void setTrigger( int trigger )
    {
        this.trigger = trigger;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }

    public BuildProjectTask getCurrentBuildProjectTask()
    {
        return currentBuildProjectTask;
    }

    public void setCurrentBuildProjectTask( BuildProjectTask currentBuildProjectTask )
    {
        this.currentBuildProjectTask = currentBuildProjectTask;
    }

    public TaskQueueExecutor getTaskQueueExecutor()
    {
        return taskQueueExecutor;
    }


    public TaskQueueExecutor getCheckoutTaskQueueExecutor()
    {
        return checkoutTaskQueueExecutor;
    }


    public void setCheckoutTaskQueueExecutor( TaskQueueExecutor checkoutTaskQueueExecutor )
    {
        this.checkoutTaskQueueExecutor = checkoutTaskQueueExecutor;
    }


    public CheckOutTask getCurrentCheckOutTask()
    {
        return currentCheckOutTask;
    }


    public void setCurrentCheckOutTask( CheckOutTask currentCheckOutTask )
    {
        this.currentCheckOutTask = currentCheckOutTask;
    }


    public List<CheckOutTask> getCurrentCheckOutTasks()
    {
        return currentCheckOutTasks;
    }


    public void setCurrentCheckOutTasks( List<CheckOutTask> currentCheckOutTasks )
    {
        this.currentCheckOutTasks = currentCheckOutTasks;
    }

    public List<String> getSelectedBuildTaskHashCodes()
    {
        return selectedBuildTaskHashCodes;
    }

    public void setSelectedBuildTaskHashCodes( List<String> selectedBuildTaskHashCodes )
    {
        this.selectedBuildTaskHashCodes = selectedBuildTaskHashCodes;
    }

    public List<String> getSelectedCheckOutTaskHashCodes()
    {
        return selectedCheckOutTaskHashCodes;
    }

    public void setSelectedCheckOutTaskHashCodes( List<String> selectedCheckOutTaskHashCodes )
    {
        this.selectedCheckOutTaskHashCodes = selectedCheckOutTaskHashCodes;
    }

    public List<DistributedBuildSummary> getDistributedBuildSummary()
    {
        return distributedBuildSummary;
    }

    public void setDistributedBuildSummary( List<DistributedBuildSummary> distributedBuildSummary )
    {
        this.distributedBuildSummary = distributedBuildSummary;
    }

    public String getBuildAgentUrl()
    {
        return buildAgentUrl;
    }

    public void setBuildAgentUrl( String buildAgentUrl )
    {
        this.buildAgentUrl = buildAgentUrl;
    }

    public List<PrepareBuildProjectsTask> getDistributedBuildQueues()
    {
        return distributedBuildQueues;
    }

    public void setDistributedBuildQueues( List<PrepareBuildProjectsTask> distributedBuildQueues )
    {
        this.distributedBuildQueues = distributedBuildQueues;
    }

    public List<String> getSelectedDistributedBuildTaskHashCodes()
    {
        return selectedDistributedBuildTaskHashCodes;
    }

    public void setSelectedDistributedBuildTaskHashCodes( List<String> selectedDistributedBuildTaskHashCodes )
    {
        this.selectedDistributedBuildTaskHashCodes = selectedDistributedBuildTaskHashCodes;
    }
}
