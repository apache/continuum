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

import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.continuum.builder.distributed.BuildAgentListener;
import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumConfirmAction;
import org.apache.struts2.ServletActionContext;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

import java.util.List;

/**
 * @author Maria Catherine Tan
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="buildAgent"
 */
public class BuildAgentAction
    extends ContinuumConfirmAction
    implements SecureAction
{
    /**
     * @plexus.requirement
     */
    private DistributedBuildManager distributedBuildManager;
    
    private List<BuildAgentConfiguration> buildAgents;

    private BuildAgentConfiguration buildAgent;

    private List<Installation> installations;

    private boolean confirmed;

    private String message;

    public String input()
        throws Exception
    {
        if ( buildAgent != null && StringUtils.isBlank( buildAgent.getUrl() ) )
        {
            List<BuildAgentConfiguration> agents = getContinuum().getConfiguration().getBuildAgents();
            
            for ( BuildAgentConfiguration agent : agents )
            {
                if ( agent.getUrl().equals( buildAgent.getUrl() ) )
                {
                    buildAgent = agent;
                }
            }
        }

        return INPUT;
    }

    public String list()
        throws Exception
    {
        String errorMessage = ServletActionContext.getRequest().getParameter( "errorMessage" );

        if ( errorMessage != null )
        {
            addActionError( errorMessage );
        }

        this.buildAgents = getContinuum().getConfiguration().getBuildAgents();

        return SUCCESS;
    }

    public String view()
        throws Exception
    {
        ConfigurationService configuration = getContinuum().getConfiguration();

        for ( BuildAgentConfiguration agent : configuration.getBuildAgents() )
        {
            if ( agent.getUrl().equals( buildAgent.getUrl() ) )
            {
                buildAgent = agent;

                // connect to BuildAgentXMLRPCCLIENT
                // installations = client.getAvailableInstallations();
                break;
            }
        }

        return SUCCESS;
    }

    public String save()
        throws Exception
    {
        boolean found = false;

        ConfigurationService configuration = getContinuum().getConfiguration();

        if ( configuration.getBuildAgents() != null )
        {
            for ( BuildAgentConfiguration agent : configuration.getBuildAgents() )
            {
                if ( buildAgent.getUrl().equals( agent.getUrl() ) )
                {
                    agent.setDescription( buildAgent.getDescription() );
                    agent.setEnabled( buildAgent.isEnabled() );

                    configuration.updateBuildAgent( agent );
                    found = true;
                }
            }
        }

        if ( !found )
        {
            configuration.addBuildAgent( buildAgent );
        }

        distributedBuildManager.reload();

        return SUCCESS;
    }

    public String delete()
        throws Exception
    {
        if ( !confirmed )
        {
            return CONFIRM;
        }

        List<BuildAgentListener> listeners = distributedBuildManager.getBuildAgentListeners();

        for ( BuildAgentListener listener : listeners )
        {
            if ( listener.getUrl().equals( buildAgent.getUrl() ) )
            {
                if ( listener.isBusy() )
                {
                    message = getText( "buildAgent.error.delete.busy" );
                    return ERROR;
                }
                else
                {
                    listeners.remove( listener );
                    break;
                }
            }
        }

        ConfigurationService configuration = getContinuum().getConfiguration();

        for ( BuildAgentConfiguration agent : configuration.getBuildAgents() )
        {
            if ( buildAgent.getUrl().equals( agent.getUrl() ) )
            {
                configuration.removeBuildAgent( agent );
                return SUCCESS;
            }
        }

        message = getText( "buildAgent.error.notfound" );
        return ERROR;
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_DISTRIBUTED_BUILDS, Resource.GLOBAL );

        return bundle;
    }

    public List<BuildAgentConfiguration> getBuildAgents()
    {
        return buildAgents;
    }

    public void setBuildAgents( List<BuildAgentConfiguration> buildAgents )
    {
        this.buildAgents = buildAgents;
    }

    public BuildAgentConfiguration getBuildAgent()
    {
        return buildAgent;
    }

    public void setBuildAgent( BuildAgentConfiguration buildAgent )
    {
        this.buildAgent = buildAgent;
    }

    public List<Installation> getInstallations()
    {
        return installations;
    }

    public void setInstallations( List<Installation> installations )
    {
        this.installations = installations;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed( boolean confirmed )
    {
        this.confirmed = confirmed;
    }

    public String getMessage()
    {
        return this.message;
    }
    
    public void setMessage( String message )
    {
        this.message = message;
    }
}
