package org.apache.continuum.model.project;

public class ProjectGroupSummary
{
    private int projectGroupId;

    private int numberOfSuccesses;

    private int numberOfFailures;

    private int numberOfErrors;

    private int numberOfProjects;

    public ProjectGroupSummary()
    {
    }

    public ProjectGroupSummary( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }
    
    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public void setNumberOfSuccesses( int numberOfSuccesses )
    {
        this.numberOfSuccesses = numberOfSuccesses;
    }

    public int getNumberOfSuccesses()
    {
        return numberOfSuccesses;
    }

    public void setNumberOfFailures( int numberOfFailures )
    {
        this.numberOfFailures = numberOfFailures;
    }

    public int getNumberOfFailures()
    {
        return numberOfFailures;
    }

    public void setNumberOfErrors( int numberOfErrors )
    {
        this.numberOfErrors = numberOfErrors;
    }

    public int getNumberOfErrors()
    {
        return numberOfErrors;
    }

    public void setNumberOfProjects( int numberOfProjects )
    {
        this.numberOfProjects = numberOfProjects;
    }

    public int getNumberOfProjects()
    {
        return numberOfProjects;
    }

    public void addProjects( int projects )
    {
        this.numberOfProjects += projects;
    }

    public void addNumberOfSuccesses( int numberOfSuccesses )
    {
        this.numberOfSuccesses += numberOfSuccesses;
    }

    public void addNumberOfErrors( int numberOfErrors )
    {
        this.numberOfErrors += numberOfErrors;
    }

    public void addNumberOfFailures( int numberOfFailures )
    {
        this.numberOfFailures += numberOfFailures;
    }
}
