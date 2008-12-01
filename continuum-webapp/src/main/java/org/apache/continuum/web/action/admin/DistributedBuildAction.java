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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.distributed.BuildAgent;
import org.apache.continuum.distributed.manager.DistributedBuildManager;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
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

    private Map<String, String> distributedBuilds;

    public String view()
    {
        List<BuildAgent> buildAgents = distributedBuildManager.getBuildAgents();
        
        distributedBuilds = new LinkedHashMap<String, String>();
        
        for ( BuildAgent buildAgent : buildAgents )
        {
            if ( buildAgent.getProjects() != null )
            {
                for ( Project project : buildAgent.getProjects() )
                {
                    distributedBuilds.put( project.getName(), buildAgent.getUrl() );
                }
            }
        }
        return SUCCESS;
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.SYSTEM_ADMINISTRATOR_ROLE, Resource.GLOBAL );

        return bundle;
    }

    public Map<String, String> getDistributedBuilds()
    {
        return distributedBuilds;
    }

    public void setDistributedBuilds( Map<String, String> distributedBuilds )
    {
        this.distributedBuilds = distributedBuilds;
    }
}
