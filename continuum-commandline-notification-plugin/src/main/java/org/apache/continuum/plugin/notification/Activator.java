package org.apache.continuum.plugin.notification;

import org.apache.continuum.plugin.api.SendNotificationPlugin;
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
        bundleContext.registerService( SendNotificationPlugin.class.getName(), new MyNotificationPlugin(),
                                       new Hashtable() );
    }

    public void stop( BundleContext bundleContext )
        throws Exception
    {
    }
}
