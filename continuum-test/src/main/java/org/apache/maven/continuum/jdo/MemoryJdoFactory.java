package org.apache.maven.continuum.jdo;

import java.util.Properties;

import org.codehaus.plexus.jdo.DefaultConfigurableJdoFactory;

public class MemoryJdoFactory
    extends DefaultConfigurableJdoFactory
{
    public Properties getOtherProperties()
    {
        return otherProperties;
    }

    public void setOtherProperties( Properties otherProperties )
    {
        this.otherProperties = otherProperties;
    }

    public void reconfigure()
    {
        configured = Boolean.FALSE;
        getPersistenceManagerFactory();
    }
}
