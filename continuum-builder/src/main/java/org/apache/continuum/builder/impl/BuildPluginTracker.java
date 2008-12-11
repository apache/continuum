package org.apache.continuum.builder.impl;

import org.apache.continuum.plugin.api.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class BuildPluginTracker
    extends ServiceTracker
{
    private static final Logger log = LoggerFactory.getLogger( BuildPluginTracker.class );

    public BuildPluginTracker( BundleContext context, Class<? extends Plugin> pluginClass )
    {
        super( context, pluginClass.getName(), null );
    }

    @Override
    public Object addingService( ServiceReference serviceReference )
    {
        log.info( "adding " + serviceReference );
        return super.addingService( serviceReference );
    }

    @Override
    public void modifiedService( ServiceReference serviceReference, Object o )
    {
        log.info( "modified " + serviceReference );
        super.modifiedService( serviceReference, o );
    }

    @Override
    public void removedService( ServiceReference serviceReference, Object o )
    {
        log.info( "removing " + serviceReference );
        super.removedService( serviceReference, o );
    }
}
