package org.apache.continuum.model.project;

public class ProjectRunSummary
{
    private int projectId;

    private int buildDefinitionId;

    private int projectGroupId;

    private int projectScmRootId;

    private String buildAgentUrl;

    private int trigger;

    private String triggeredBy;

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public int getBuildDefinitionId()
    {
        return buildDefinitionId;
    }

    public void setBuildDefinitionId( int buildDefinitionId )
    {
        this.buildDefinitionId = buildDefinitionId;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public int getProjectScmRootId()
    {
        return projectScmRootId;
    }

    public void setProjectScmRootId( int projectScmRootId )
    {
        this.projectScmRootId = projectScmRootId;
    }

    public String getBuildAgentUrl()
    {
        return buildAgentUrl;
    }

    public void setBuildAgentUrl( String buildAgentUrl )
    {
        this.buildAgentUrl = buildAgentUrl;
    }

    public int getTrigger()
    {
        return trigger;
    }

    public void setTrigger( int trigger )
    {
        this.trigger = trigger;
    }

    public String getTriggeredBy()
    {
        return triggeredBy;
    }

    public void setTriggeredBy( String triggeredBy )
    {
        this.triggeredBy = triggeredBy;
    }
}
