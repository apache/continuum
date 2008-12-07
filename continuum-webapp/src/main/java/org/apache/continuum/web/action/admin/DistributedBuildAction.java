package org.apache.continuum.web.action.admin;

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

import org.apache.continuum.builder.distributed.BuildAgentListener;
import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.model.DistributedBuildSummary;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

/**
 * @author Maria Catherine Tan
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="distributedBuild"
 */
public class DistributedBuildAction
    extends ContinuumActionSupport
    implements SecureAction
{
    /**
     * @plexus.requirement
     */
    DistributedBuildManager distributedBuildManager;

    private List<DistributedBuildSummary> distributedBuildSummary;

    private int projectId;

    private String buildAgentUrl;

    public String view()
    {
        distributedBuildSummary = new ArrayList<DistributedBuildSummary>();
        
        for ( BuildAgentListener listener : distributedBuildManager.getBuildAgentListeners() )
        {
            if ( listener.hasProjects() )
            {
                for ( Project project : listener.getProjects() )
                {
                    DistributedBuildSummary summary = new DistributedBuildSummary();
                    summary.setProjectId( project.getId() );
                    summary.setProjectName( project.getName() );
                    summary.setUrl( listener.getUrl() );
                    
                    distributedBuildSummary.add( summary );
                }
            }
        }

        return SUCCESS;
    }

    public String cancel()
        throws Exception
    {
        return SUCCESS;
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_QUEUES, Resource.GLOBAL );

        return bundle;
    }

    public List<DistributedBuildSummary> getDistributedBuildSummary()
    {
        return distributedBuildSummary;
    }

    public void setDistributedBuildSummary( List<DistributedBuildSummary> distributedBuildSummary )
    {
        this.distributedBuildSummary = distributedBuildSummary;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getBuildAgentUrl()
    {
        return buildAgentUrl;
    }

    public void setBuildAgentUrl( String buildAgentUrl )
    {
        this.buildAgentUrl = buildAgentUrl;
    }
}
