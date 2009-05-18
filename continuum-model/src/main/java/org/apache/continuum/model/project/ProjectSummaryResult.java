package org.apache.continuum.model.project;

public class ProjectSummaryResult
{
    private int projectGroupId;

    private int projectState;

    private long size;

    public ProjectSummaryResult( int projectGroupId, int projectState, long size )
    {
        this.projectGroupId = projectGroupId;

        this.projectState = projectState;

        this.size = size;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public int getProjectState()
    {
        return projectState;
    }

    public void setProjectState( int projectState )
    {
        this.projectState = projectState;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize( long size )
    {
        this.size = size;
    }
}
