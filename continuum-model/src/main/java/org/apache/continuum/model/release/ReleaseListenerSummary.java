package org.apache.continuum.model.release;

import java.util.List;

public class ReleaseListenerSummary
{
    private String goalName;

    private String error;

    private String username;

    private String inProgress;

    private int state;

    private List<String> phases;

    public String getGoalName()
    {
        return goalName;
    }

    public void setGoalName( String goalName )
    {
        this.goalName = goalName;
    }

    public String getError()
    {
        return error;
    }

    public void setError( String error )
    {
        this.error = error;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getInProgress()
    {
        return inProgress;
    }

    public void setInProgress( String inProgress )
    {
        this.inProgress = inProgress;
    }

    public int getState()
    {
        return state;
    }

    public void setState( int state )
    {
        this.state = state;
    }

    public List<String> getPhases()
    {
        return phases;
    }

    public void setPhases( List<String> phases )
    {
        this.phases = phases;
    }

    public List<String> getCompletedPhases()
    {
        return completedPhases;
    }

    public void setCompletedPhases( List<String> completedPhases )
    {
        this.completedPhases = completedPhases;
    }

    private List<String> completedPhases;
}
