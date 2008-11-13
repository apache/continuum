package org.apache.continuum.configuration;

public class BuildAgentConfiguration
{
    private String url;

    private String operatingSystem;

    private boolean enabled;

    public BuildAgentConfiguration()
    {
        // do nothing
    }

    public BuildAgentConfiguration( String url, String operatingSystem, boolean enabled )
    {
        this.url = url;
        this.operatingSystem = operatingSystem;
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

    public String getOperatingSystem()
    {
        return operatingSystem;
    }

    public void setOperatingSystem( String operatingSystem )
    {
        this.operatingSystem = operatingSystem;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }
}
