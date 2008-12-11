package org.apache.continuum.builder.impl;

import org.apache.continuum.plugin.api.BuildProjectPlugin;
import org.apache.continuum.plugin.api.BuildReportsPlugin;
import org.apache.continuum.plugin.api.DeployArtifactsPlugin;
import org.apache.continuum.plugin.api.EndBuildPlugin;
import org.apache.continuum.plugin.api.PostBuildProjectPlugin;
import org.apache.continuum.plugin.api.PostBuildReportsPlugin;
import org.apache.continuum.plugin.api.PostDeployArtifactsPlugin;
import org.apache.continuum.plugin.api.PostSendNotificationPlugin;
import org.apache.continuum.plugin.api.PostUpdateSourcesPlugin;
import org.apache.continuum.plugin.api.PreBuildProjectPlugin;
import org.apache.continuum.plugin.api.PreBuildReportsPlugin;
import org.apache.continuum.plugin.api.PreDeployArtifactsPlugin;
import org.apache.continuum.plugin.api.PreSendNotificationPlugin;
import org.apache.continuum.plugin.api.PreUpdateSourcesPlugin;
import org.apache.continuum.plugin.api.SendNotificationPlugin;
import org.apache.continuum.plugin.api.UpdateSourcesPlugin;
import org.apache.continuum.plugin.api.builder.Builder;
import org.apache.continuum.plugin.api.builder.Phase;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class BuilderActivator
    implements BundleActivator
{
    private BundleContext context;

    //private BuildPluginTracker tracker;

    private Map<String, BuildPluginTracker> trackers = new HashMap<String, BuildPluginTracker>();

    public void start( BundleContext bundleContext )
        throws Exception
    {
        context = bundleContext;

        //tracker = new BuildPluginTracker( context );
        //tracker.open();

        trackers.put( Phase.PRE_UPDATE_SOURCES.getName(), createTracker( context, Phase.PRE_UPDATE_SOURCES ) );
        trackers.put( Phase.UPDATE_SOURCES.getName(), createTracker( context, Phase.UPDATE_SOURCES ) );
        trackers.put( Phase.POST_UPDATE_SOURCES.getName(), createTracker( context, Phase.POST_UPDATE_SOURCES ) );

        trackers.put( Phase.PRE_BUILD_PROJECT.getName(), createTracker( context, Phase.PRE_BUILD_PROJECT ) );
        trackers.put( Phase.BUILD_PROJECT.getName(), createTracker( context, Phase.BUILD_PROJECT ) );
        trackers.put( Phase.POST_BUILD_PROJECT.getName(), createTracker( context, Phase.POST_BUILD_PROJECT ) );

        trackers.put( Phase.PRE_DEPLOY_ARTIFACTS.getName(), createTracker( context, Phase.PRE_DEPLOY_ARTIFACTS ) );
        trackers.put( Phase.DEPLOY_ARTIFACTS.getName(), createTracker( context, Phase.DEPLOY_ARTIFACTS ) );
        trackers.put( Phase.POST_DEPLOY_ARTIFACTS.getName(), createTracker( context, Phase.POST_DEPLOY_ARTIFACTS ) );

        trackers.put( Phase.PRE_BUILD_REPORTS.getName(), createTracker( context, Phase.PRE_BUILD_REPORTS ) );
        trackers.put( Phase.BUILD_REPORTS.getName(), createTracker( context, Phase.BUILD_REPORTS ) );
        trackers.put( Phase.POST_BUILD_REPORTS.getName(), createTracker( context, Phase.POST_BUILD_REPORTS ) );

        trackers.put( Phase.PRE_SEND_NOTIFICATIONS.getName(), createTracker( context, Phase.PRE_SEND_NOTIFICATIONS ) );
        trackers.put( Phase.SEND_NOTIFICATIONS.getName(), createTracker( context, Phase.SEND_NOTIFICATIONS ) );
        trackers.put( Phase.POST_SEND_NOTIFICATIONS.getName(),
                      createTracker( context, Phase.POST_SEND_NOTIFICATIONS ) );

        trackers.put( Phase.END_BUILD.getName(), createTracker( context, Phase.END_BUILD ) );

        Builder builder = new BuilderImpl( trackers );
        context.registerService( Builder.class.getName(), builder, null );
    }

    public void stop( BundleContext bundleContext )
        throws Exception
    {
        //tracker.close();

        for ( String key : trackers.keySet() )
        {
            BuildPluginTracker tracker = trackers.get( key );
            tracker.close();
            trackers.put( key, null );
        }
        context = null;
    }

    private BuildPluginTracker createTracker( BundleContext context, Phase phase )
    {
        BuildPluginTracker tracker = null;

        if ( Phase.PRE_UPDATE_SOURCES.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, PreUpdateSourcesPlugin.class );
        }
        else if ( Phase.UPDATE_SOURCES.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, UpdateSourcesPlugin.class );
        }
        else if ( Phase.POST_UPDATE_SOURCES.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, PostUpdateSourcesPlugin.class );
        }
        else if ( Phase.PRE_BUILD_PROJECT.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, PreBuildProjectPlugin.class );
        }
        else if ( Phase.BUILD_PROJECT.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, BuildProjectPlugin.class );
        }
        else if ( Phase.POST_BUILD_PROJECT.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, PostBuildProjectPlugin.class );
        }
        else if ( Phase.PRE_DEPLOY_ARTIFACTS.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, PreDeployArtifactsPlugin.class );
        }
        else if ( Phase.DEPLOY_ARTIFACTS.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, DeployArtifactsPlugin.class );
        }
        else if ( Phase.POST_DEPLOY_ARTIFACTS.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, PostDeployArtifactsPlugin.class );
        }
        else if ( Phase.PRE_BUILD_REPORTS.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, PreBuildReportsPlugin.class );
        }
        else if ( Phase.BUILD_REPORTS.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, BuildReportsPlugin.class );
        }
        else if ( Phase.POST_BUILD_REPORTS.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, PostBuildReportsPlugin.class );
        }
        else if ( Phase.PRE_SEND_NOTIFICATIONS.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, PreSendNotificationPlugin.class );
        }
        else if ( Phase.SEND_NOTIFICATIONS.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, SendNotificationPlugin.class );
        }
        else if ( Phase.POST_SEND_NOTIFICATIONS.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, PostSendNotificationPlugin.class );
        }
        else if ( Phase.END_BUILD.equals( phase ) )
        {
            tracker = new BuildPluginTracker( context, EndBuildPlugin.class );
        }

        tracker.open();
        return tracker;
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
}
