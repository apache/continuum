package org.apache.continuum.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BuildAgentGroupConfiguration
{
    private String name;

    private List<BuildAgentConfiguration> buildAgents = new ArrayList<BuildAgentConfiguration>();

    public BuildAgentGroupConfiguration()
    {
        //nil
    }

    public BuildAgentGroupConfiguration( String name, List<BuildAgentConfiguration> buildAgents )
    {
        this.name = name;
        this.buildAgents = buildAgents;
    }

    public void addBuildAgent( BuildAgentConfiguration buildAgent )
    {
        buildAgents.add( buildAgent );
    }

    public void removeBuildAgent( BuildAgentConfiguration buildAgent )
    {
        Iterator<BuildAgentConfiguration> iterator = buildAgents.iterator();
        while ( iterator.hasNext() )
        {
            BuildAgentConfiguration agent = iterator.next();
            if ( agent.getUrl().equals( buildAgent.getUrl() ) )
            {
                iterator.remove();
            }
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public List<BuildAgentConfiguration> getBuildAgents()
    {
        return buildAgents;
    }

    public void setBuildAgents( List<BuildAgentConfiguration> buildAgents )
    {
        this.buildAgents = buildAgents;
    }

}
