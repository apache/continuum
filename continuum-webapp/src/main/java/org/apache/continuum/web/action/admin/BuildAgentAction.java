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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.continuum.configuration.BuildAgentGroupConfiguration;
import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumConfirmAction;
import org.apache.struts2.ServletActionContext;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Maria Catherine Tan
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="buildAgent"
 */
public class BuildAgentAction
    extends ContinuumConfirmAction
    implements SecureAction
{
    private static final Logger logger = LoggerFactory.getLogger( BuildAgentAction.class );

    private List<BuildAgentConfiguration> buildAgents;

    private BuildAgentConfiguration buildAgent;

    private BuildAgentGroupConfiguration buildAgentGroup;

    private List<BuildAgentGroupConfiguration> buildAgentGroups;

    private List<BuildAgentConfiguration> selectedbuildAgents;

    private List<String> selectedBuildAgentIds;

    private List<Installation> installations;

    private boolean confirmed;

    private String message;

    private String type;

    private String typeGroup;

    public void prepare()
        throws Exception
    {
        super.prepare();
        this.setBuildAgents( getContinuum().getConfiguration().getBuildAgents() );
    }

    public String input()
        throws Exception
    {
        if ( buildAgent != null && !StringUtils.isBlank( buildAgent.getUrl() ) )
        {
            String escapedBuildAgentUrl = StringEscapeUtils.escapeXml( buildAgent.getUrl() );
            buildAgent.setUrl( escapedBuildAgentUrl );

            List<BuildAgentConfiguration> agents = getContinuum().getConfiguration().getBuildAgents();

            for ( BuildAgentConfiguration agent : agents )
            {
                if ( agent.getUrl().equals( escapedBuildAgentUrl ) )
                {
                    buildAgent = agent;
                    type = "edit";
                }
            }
        }
        else
        {
            type = "new";
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
        this.buildAgentGroups = getContinuum().getConfiguration().getBuildAgentGroups();

        return SUCCESS;
    }

    public String view()
        throws Exception
    {
        ConfigurationService configuration = getContinuum().getConfiguration();

        if ( buildAgent != null )
        {
            String escapedBuildAgentUrl = StringEscapeUtils.escapeXml( buildAgent.getUrl() );
            buildAgent.setUrl( escapedBuildAgentUrl );

            for ( BuildAgentConfiguration agent : configuration.getBuildAgents() )
            {
                if ( agent.getUrl().equals( escapedBuildAgentUrl ) )
                {
                    buildAgent = agent;
    
                    try
                    {
                        installations = getContinuum().getDistributedBuildManager().getAvailableInstallations( escapedBuildAgentUrl );
                    }
                    catch ( ContinuumException e )
                    {
                        logger.error( "Unable to retrieve installations of build agent '" + agent.getUrl() + "'", e );
                    }
    
                    break;
                }
            }
        }

        return SUCCESS;
    }

    public String save()
        throws Exception
    {
        boolean found = false;

        ConfigurationService configuration = getContinuum().getConfiguration();

        // escape xml to prevent xss attacks
        buildAgent.setDescription( StringEscapeUtils.escapeXml( StringEscapeUtils.unescapeXml( buildAgent.getDescription() ) ) );

        if ( configuration.getBuildAgents() != null )
        {
            for ( BuildAgentConfiguration agent : configuration.getBuildAgents() )
            {
                if ( agent.getUrl().equals( buildAgent.getUrl() ) )
                {
                    agent.setDescription( buildAgent.getDescription() );
                    agent.setEnabled( buildAgent.isEnabled() );

                    configuration.updateBuildAgent( agent );
                    configuration.store();
                    found = true;
                }
            }
        }

        AuditLog event = new AuditLog( "Build Agent URL=" + buildAgent.getUrl(), AuditLogConstants.MODIFY_BUILD_AGENT );
        event.setCategory( AuditLogConstants.BUILD_AGENT );
        event.setCurrentUser( getPrincipal() );

        if ( !found )
        {
            configuration.addBuildAgent( buildAgent );
            configuration.store();
            event.setAction( AuditLogConstants.ADD_BUILD_AGENT );
        }
        else
        {
            if ( type.equals( "new" ) )
            {
                addActionError( getResourceBundle().getString( "buildAgent.error.duplicate" ) );
                return INPUT;
            }
        }

        getContinuum().getDistributedBuildManager().reload();
        event.log();

        return SUCCESS;
    }

    public String delete()
        throws Exception
    {
        buildAgent.setUrl( StringEscapeUtils.escapeXml( StringEscapeUtils.unescapeXml( buildAgent.getUrl() ) ) );

        if ( !confirmed )
        {
            return CONFIRM;
        }

        if ( getContinuum().getDistributedBuildManager().isBuildAgentBusy( buildAgent.getUrl() ) )
        {
            message = getText( "buildAgent.error.delete.busy" );
            return ERROR;
        }

        ConfigurationService configuration = getContinuum().getConfiguration();

        if ( configuration.getBuildAgentGroups() != null )
        {   
            for ( BuildAgentGroupConfiguration buildAgentGroup : configuration.getBuildAgentGroups() )
            {
                if ( configuration.containsBuildAgentUrl( buildAgent.getUrl(), buildAgentGroup ) )
                {
                    message = getText( "buildAgent.error.remove.in.use" );
                    return ERROR;
                }
            }
        }

        if ( configuration.getBuildAgents() != null )
        {
            for ( BuildAgentConfiguration agent : configuration.getBuildAgents() )
            {
                if ( buildAgent.getUrl().equals( agent.getUrl() ) )
                {
                    getContinuum().getDistributedBuildManager().removeDistributedBuildQueueOfAgent( buildAgent.getUrl() );
                    configuration.removeBuildAgent( agent );
                    configuration.store();

                    AuditLog event = new AuditLog( "Build Agent URL=" + agent.getUrl(), AuditLogConstants.REMOVE_BUILD_AGENT );
                    event.setCategory( AuditLogConstants.BUILD_AGENT );
                    event.setCurrentUser( getPrincipal() );
                    event.log();

                    getContinuum().getDistributedBuildManager().reload();

                    return SUCCESS;
                }
            }
        }

        message = getText( "buildAgent.error.notfound" );
        return ERROR;
    }

    public String deleteGroup()
        throws Exception
    {
        buildAgentGroup.setName( StringEscapeUtils.escapeXml( buildAgentGroup.getName() ) );

        if ( !confirmed )
        {
            return CONFIRM;
        }

        List<Profile> profiles = getContinuum().getProfileService().getAllProfiles();
        for ( Profile profile : profiles )
        {
            if ( buildAgentGroup.getName().equals( profile.getBuildAgentGroup() ) )
            {
                message = getText( "buildAgentGroup.error.remove.in.use" );
                return ERROR;
            }
        }

        ConfigurationService configuration = getContinuum().getConfiguration();

        for ( BuildAgentGroupConfiguration group : configuration.getBuildAgentGroups() )
        {
            if ( buildAgentGroup.getName().equals( group.getName() ) )
            {
                configuration.removeBuildAgentGroup( group );

                AuditLog event = new AuditLog( "Build Agent Group=" + group.getName(), AuditLogConstants.REMOVE_BUILD_AGENT_GROUP );
                event.setCategory( AuditLogConstants.BUILD_AGENT );
                event.setCurrentUser( getPrincipal() );
                event.log();

                return SUCCESS;
            }
        }

        message = getText( "buildAgentGroup.error.doesnotexist" );
        return ERROR;
    }

    public String saveGroup()
        throws Exception
    {
        boolean found = false;

        ConfigurationService configuration = getContinuum().getConfiguration();
        selectedbuildAgents = getBuildAgentsFromSelectedBuildAgents();

        if ( buildAgentGroup.getName() != null )
        {
            if ( buildAgentGroup.getName().equals( "" ) )
            {
                addActionError( getResourceBundle().getString( "buildAgentGroup.error.name.required" ) );
                return INPUT;
            }
            else if ( buildAgentGroup.getName().trim().equals( "" ) )
            {
                addActionError( getText( "buildAgentGroup.error.name.cannot.be.spaces" ) );
                return INPUT;
            }
        }

        if ( configuration.getBuildAgentGroups() != null )
        {
            for ( BuildAgentGroupConfiguration group : configuration.getBuildAgentGroups() )
            {
                if ( buildAgentGroup.getName().equals( group.getName() ) )
                {
                    group.setName( buildAgentGroup.getName() );
                    configuration.updateBuildAgentGroup( group );
                    found = true;
                    break;
                }
            }
        }

        AuditLog event = new AuditLog( "Build Agent Group=" + buildAgentGroup.getName(), AuditLogConstants.MODIFY_BUILD_AGENT_GROUP );
        event.setCategory( AuditLogConstants.BUILD_AGENT );
        event.setCurrentUser( getPrincipal() );

        if ( !found )
        {
            buildAgentGroup.setBuildAgents( selectedbuildAgents );
            configuration.addBuildAgentGroup( buildAgentGroup );
            event.setAction( AuditLogConstants.ADD_BUILD_AGENT_GROUP );
        }
        else
        // found
        {
            if ( typeGroup.equals( "new" ) )
            {
                addActionError( getResourceBundle().getString( "buildAgentGroup.error.duplicate" ) );
                return INPUT;
            }
            else if ( typeGroup.equals( "edit" ) )
            {
                buildAgentGroup.setBuildAgents( selectedbuildAgents );
                configuration.updateBuildAgentGroup( buildAgentGroup );
            }
        }

        getContinuum().getDistributedBuildManager().reload();
        event.log();

        return SUCCESS;
    }

    public String inputGroup()
        throws Exception
    {
        ConfigurationService configuration = getContinuum().getConfiguration();

        if ( buildAgentGroup != null && !StringUtils.isBlank( buildAgentGroup.getName() ) )
        {
            String escapedBuildAgentGroupName = StringEscapeUtils.escapeXml( buildAgentGroup.getName() );
            buildAgentGroup.setName( escapedBuildAgentGroupName );

            List<BuildAgentGroupConfiguration> agentGroups = configuration.getBuildAgentGroups();

            for ( BuildAgentGroupConfiguration group : agentGroups )
            {
                if ( group.getName().equals( escapedBuildAgentGroupName ) )
                {
                    buildAgentGroup = group;
                    typeGroup = "edit";

                    this.buildAgentGroup = configuration.getBuildAgentGroup( escapedBuildAgentGroupName );
                    this.buildAgents = configuration.getBuildAgents();

                    this.selectedBuildAgentIds = new ArrayList<String>();
                    if ( this.buildAgentGroup.getBuildAgents() != null )
                    {
                        for ( BuildAgentConfiguration buildAgentConfiguration : buildAgentGroup.getBuildAgents() )
                        {
                            this.selectedBuildAgentIds.add( buildAgentConfiguration.getUrl() );
                        }

                    }

                    List<BuildAgentConfiguration> unusedBuildAgents = new ArrayList<BuildAgentConfiguration>();

                    for ( BuildAgentConfiguration agent : getBuildAgents() )
                    {
                        if ( !this.selectedBuildAgentIds.contains( agent.getUrl() ) )
                        {
                            unusedBuildAgents.add( agent );
                        }
                    }
                    this.setBuildAgents( unusedBuildAgents );

                    break;
                }
            }
        }
        else
        {
            typeGroup = "new";
        }
        return INPUT;
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_DISTRIBUTED_BUILDS, Resource.GLOBAL );

        return bundle;
    }

    private List<BuildAgentConfiguration> getBuildAgentsFromSelectedBuildAgents()
    {
        if ( this.selectedBuildAgentIds == null )
        {
            return Collections.EMPTY_LIST;
        }

        List<BuildAgentConfiguration> selectedbuildAgents = new ArrayList<BuildAgentConfiguration>();
        for ( String ids : selectedBuildAgentIds )
        {
            BuildAgentConfiguration buildAgent = getContinuum().getConfiguration().getBuildAgent( ids );
            if ( buildAgent != null )
            {
                selectedbuildAgents.add( buildAgent );
            }
        }
        return selectedbuildAgents;
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

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public List<BuildAgentGroupConfiguration> getBuildAgentGroups()
    {
        return buildAgentGroups;
    }

    public void setBuildAgentGroups( List<BuildAgentGroupConfiguration> buildAgentGroups )
    {
        this.buildAgentGroups = buildAgentGroups;
    }

    public BuildAgentGroupConfiguration getBuildAgentGroup()
    {
        return buildAgentGroup;
    }

    public void setBuildAgentGroup( BuildAgentGroupConfiguration buildAgentGroup )
    {
        this.buildAgentGroup = buildAgentGroup;
    }

    public String getTypeGroup()
    {
        return typeGroup;
    }

    public void setTypeGroup( String typeGroup )
    {
        this.typeGroup = typeGroup;
    }

    public List<BuildAgentConfiguration> getSelectedbuildAgents()
    {
        return selectedbuildAgents;
    }

    public void setSelectedbuildAgents( List<BuildAgentConfiguration> selectedbuildAgents )
    {
        this.selectedbuildAgents = selectedbuildAgents;
    }

    public List<String> getSelectedBuildAgentIds()
    {
        return selectedBuildAgentIds == null ? Collections.EMPTY_LIST : selectedBuildAgentIds;
    }

    public void setSelectedBuildAgentIds( List<String> selectedBuildAgentIds )
    {
        this.selectedBuildAgentIds = selectedBuildAgentIds;
    }
}
