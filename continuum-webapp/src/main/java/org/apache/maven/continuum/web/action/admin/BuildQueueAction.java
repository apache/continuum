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
package org.apache.maven.continuum.web.action.admin;

import java.util.List;

import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.redback.xwork.interceptor.SecureAction;
import org.codehaus.plexus.redback.xwork.interceptor.SecureActionBundle;
import org.codehaus.plexus.redback.xwork.interceptor.SecureActionException;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 24 sept. 07
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="buildQueue"
 */
public class BuildQueueAction
    extends ContinuumActionSupport
    implements SecureAction
{

    private List<BuildProjectTask> buildProjectTasks;
    
    private List<String> selectedProjectIds;
    
    private int buildDefinitionId;
    
    private int projectId;
    
    private int trigger;
    
    private String projectName;
    
    // -----------------------------------------------------
    //  webwork
    // -----------------------------------------------------     
    
    public String global()
        throws Exception
    {
        return SUCCESS;
    }

    public String display()
        throws Exception
    {
        this.setBuildProjectTasks( getContinuum().getBuildProjectTasksInQueue() );
        return SUCCESS;
    }

    public String remove()
        throws Exception
    {
        BuildProjectTask buildProjectTask = new BuildProjectTask( projectId, buildDefinitionId, trigger, projectName );
        getContinuum().removeFromBuildingQueue( projectId, buildDefinitionId, trigger, projectName );
        Project project = getContinuum().getProject( projectId );
        project.setState( project.getOldState() );
        getContinuum().updateProject( project );
        
        return SUCCESS;
    }
    

    // -----------------------------------------------------
    //  security
    // -----------------------------------------------------    

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_BUILD_QUEUE, Resource.GLOBAL );

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

    public List<String> getSelectedProjectIds()
    {
        return selectedProjectIds;
    }

    public void setSelectedProjectIds( List<String> selectedProjectIds )
    {
        this.selectedProjectIds = selectedProjectIds;
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

    
}
