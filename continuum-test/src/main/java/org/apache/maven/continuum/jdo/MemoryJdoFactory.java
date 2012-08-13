package org.apache.maven.continuum.jdo;

import org.codehaus.plexus.jdo.DefaultConfigurableJdoFactory;

import java.util.Properties;

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
