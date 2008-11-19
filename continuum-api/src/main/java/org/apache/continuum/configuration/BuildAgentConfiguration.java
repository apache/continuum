package org.apache.continuum.configuration;

public class BuildAgentConfiguration
{
    private String url;

    private String type;

    private String description;

    private boolean enabled;

    public BuildAgentConfiguration()
    {
        // do nothing
    }

    public BuildAgentConfiguration( String url, String type, boolean enabled )
    {
        this( url, type, null, enabled );
    }

    public BuildAgentConfiguration( String url, String type, String description, boolean enabled )
    {
        this.url = url;
        this.type = type;
        this.enabled = enabled;
        this.description = description;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
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
