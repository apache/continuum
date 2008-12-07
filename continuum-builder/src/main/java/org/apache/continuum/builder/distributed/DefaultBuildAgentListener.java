package org.apache.continuum.builder.distributed;

import java.util.List;

import org.apache.maven.continuum.model.project.Project;

public class DefaultBuildAgentListener
    implements BuildAgentListener
{
    private String url;

    private boolean busy;

    private boolean enabled;

    private List<Project> projects;

    public DefaultBuildAgentListener()
    {
    }

    public DefaultBuildAgentListener( String url, boolean busy, boolean enabled )
    {
        this.url = url;
        this.busy = busy;
        this.enabled = enabled;
    }

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

    public boolean hasProjects()
    {
        if ( projects != null || projects.size() > 0 )
        {
            return true;
        }

        return false;
    }

    public void setProjects( List<Project> projects )
    {
        this.projects = projects;
    }
}
