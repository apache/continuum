package org.apache.continuum.plugin.manager;

import org.apache.continuum.plugin.api.builder.Builder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class HostActivator
    implements BundleActivator
{
    private BundleContext context = null;

    private BuilderTracker builderTracker;

    public void start( BundleContext bundleContext )
        throws Exception
    {
        context = bundleContext;
        builderTracker = new BuilderTracker( context );
        builderTracker.open();
    }

    public void stop( BundleContext bundleContext )
        throws Exception
    {
        builderTracker.close();
        context = null;
    }

    public Bundle[] getBundles()
    {
        Bundle[] bundles = null;
        if ( context != null )
        {
            bundles = context.getBundles();
        }
        return bundles;
    }

    public Builder getBuilder()
    {
        return (Builder) builderTracker.getService();
    }
}
