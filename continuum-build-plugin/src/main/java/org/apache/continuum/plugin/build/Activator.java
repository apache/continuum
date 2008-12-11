package org.apache.continuum.plugin.build;

import org.apache.continuum.plugin.api.BuildProjectPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class Activator
    implements BundleActivator
{
    public void start( BundleContext bundleContext )
        throws Exception
    {
        bundleContext.registerService( BuildProjectPlugin.class.getName(), new BuildPlugin(), new Hashtable() );
    }

    public void stop( BundleContext bundleContext )
        throws Exception
    {
    }
}
