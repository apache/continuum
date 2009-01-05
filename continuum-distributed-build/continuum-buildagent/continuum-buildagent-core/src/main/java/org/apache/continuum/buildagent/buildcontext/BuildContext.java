package org.apache.continuum.buildagent.buildcontext;

import java.util.Map;

import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.scm.ScmResult;

public class BuildContext
{
    private int projectId;

    private String projectName;

    private int projectState;

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

    private int projectGroupId;

    private String scmRootAddress;

    private Map<String, Object> actionContext;

    private ScmResult scmResult;

    private BuildResult buildResult;

    private long buildStartTime;

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

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }

    public int getProjectState()
    {
        return projectState;
    }

    public void setProjectState( int projectState )
    {
        this.projectState = projectState;
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

    public Map<String, Object> getActionContext()
    {
        return actionContext;
    }

    public void setActionContext( Map<String, Object> actionContext ) 
    {
        this.actionContext = actionContext;
    }

    public ScmResult getScmResult()
    {
        return scmResult;
    }

    public void setScmResult( ScmResult scmResult )
    {
        this.scmResult = scmResult;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public void setBuildResult( BuildResult buildResult )
    {
        this.buildResult = buildResult;
    }

    public long getBuildStartTime()
    {
        return buildStartTime;
    }

    public void setBuildStartTime( long buildStartTime )
    {
        this.buildStartTime = buildStartTime;
    }
}
