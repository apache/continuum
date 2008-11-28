package org.apache.maven.continuum.web.action.admin;

import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

import java.util.List;

public class BuildAgentAction
    extends ContinuumActionSupport
    implements SecureAction
{
    private List<BuildAgentConfiguration> buildAgents;

    private BuildAgentConfiguration buildAgent;

    private String buildAgentUrl;

    private List<Installation> installations;

    public String list()
        throws Exception
    {
        this.buildAgents = getContinuum().getConfiguration().getBuildAgents();
        return SUCCESS;
    }

    public String view()
        throws Exception
    {
        ConfigurationService configuration = getContinuum().getConfiguration();

        for ( BuildAgentConfiguration agent : configuration.getBuildAgents() )
        {
            if ( agent.getUrl().equals( buildAgentUrl ) )
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
        ConfigurationService configuration = getContinuum().getConfiguration();

        for ( BuildAgentConfiguration agent : configuration.getBuildAgents() )
        {
            if ( buildAgent.getUrl().equals( agent.getUrl() ) )
            {
                addActionError( "buildAgent.error.exist" );
                return ERROR;
            }
        }

        configuration.addBuildAgent( buildAgent );

        return SUCCESS;
    }

    public String delete()
        throws Exception
    {
        ConfigurationService configuration = getContinuum().getConfiguration();

        for ( BuildAgentConfiguration agent : configuration.getBuildAgents() )
        {
            if ( buildAgent.getUrl().equals( agent.getUrl() ) )
            {
                configuration.removeBuildAgent( buildAgent );
                return SUCCESS;
            }
        }

        addActionError( "buildAgent.error.notfound" );
        return ERROR;
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.SYSTEM_ADMINISTRATOR_ROLE, Resource.GLOBAL );

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

    public String getBuildAgentUrl()
    {
        return buildAgentUrl;
    }

    public void setBuildAgentUrl( String buildAgentUrl )
    {
        this.buildAgentUrl = buildAgentUrl;
    }

    public List<Installation> getInstallations()
    {
        return installations;
    }

    public void setInstallations( List<Installation> installations )
    {
        this.installations = installations;
    }
}
