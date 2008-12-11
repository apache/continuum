package org.apache.continuum.plugin.manager;

import org.apache.continuum.plugin.api.builder.Builder;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class BuilderTracker
    extends ServiceTracker
{
    public BuilderTracker( BundleContext bundleContext )
    {
        super( bundleContext, Builder.class.getName(), null );
    }
}
