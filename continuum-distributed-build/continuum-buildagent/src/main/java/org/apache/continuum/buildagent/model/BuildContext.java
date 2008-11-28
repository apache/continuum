package org.apache.continuum.buildagent.model;

public class BuildContext
{
    private int projectId;

    private int buildDefinitionId;

    private String buildFile;

    private String goals;

    private String arguments;

    private String executorId;

    private String scmUrl;

    private String scmUsername;

    private String scmPassword;

    private int trigger;

    private boolean buildFresh;

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

    public String getBuildFile()
    {
        return buildFile;
    }

    public void setBuildFile( String buildFile )
    {
        this.buildFile = buildFile;
    }

    public String getGoals()
    {
        return goals;
    }

    public void setGoals( String goals )
    {
        this.goals = goals;
    }

    public String getArguments()
    {
        return arguments;
    }

    public void setArguments( String arguments )
    {
        this.arguments = arguments;
    }

    public String getExecutorId()
    {
        return executorId;
    }

    public void setExecutorId( String executorId )
    {
        this.executorId = executorId;
    }

    public String getScmUrl()
    {
        return scmUrl;
    }

    public void setScmUrl( String scmUrl )
    {
        this.scmUrl = scmUrl;
    }

    public String getScmUsername()
    {
        return scmUsername;
    }

    public void setScmUsername( String scmUsername )
    {
        this.scmUsername = scmUsername;
    }

    public String getScmPassword()
    {
        return scmPassword;
    }

    public void setScmPassword( String scmPassword )
    {
        this.scmPassword = scmPassword;
    }

    public int getTrigger()
    {
        return trigger;
    }

    public void setTrigger( int trigger )
    {
        this.trigger = trigger;
    }

    public boolean isBuildFresh()
    {
        return buildFresh;
    }

    public void setBuildFresh( boolean buildFresh )
    {
        this.buildFresh = buildFresh;
    }
}
