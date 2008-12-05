package org.apache.continuum.distributed;

import java.util.List;

import org.apache.maven.continuum.model.project.Project;

public class BuildAgent
{
    String url;

    boolean busy;

    boolean enabled;

    List<Project> projects;

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public boolean isBusy()
    {
        return busy;
    }

    public void setBusy( boolean busy )
    {
        this.busy = busy;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }

    public List<Project> getProjects()
    {
        return projects;
    }

    public void setProjects( List<Project> projects )
    {
        this.projects = projects;
    }
}
