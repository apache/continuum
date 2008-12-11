package org.apache.continuum.plugin.manager;

import org.apache.continuum.plugin.api.builder.Builder;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.cache.BundleCache;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.felix.framework.util.StringMap;
import org.apache.felix.main.AutoActivator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class PluginManager
{
    private Felix felix = null;

    private HostActivator activator = null;

    public PluginManager()
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            public void run()
            {
                try
                {
                    System.out.println("Stopping the application...");
                    if ( felix != null )
                    {
                        felix.stop();
                    }
                }
                catch ( BundleException e )
                {
                    e.printStackTrace();
                }
            }
        } );

        Properties props = new Properties();
        InputStream is = getClass().getClassLoader().getResourceAsStream( "osgi-manager.properties" );
        try
        {
            props.load( is );
        }
        catch ( IOException ioe )
        {
            ioe.printStackTrace();
        }

        //noinspection unchecked
        Map<String, String> configMap = new StringMap( false );
        configMap.put( FelixConstants.EMBEDDED_EXECUTION_PROP, "true" );
        configMap.put( Constants.FRAMEWORK_SYSTEMPACKAGES, props.getProperty( "osgi.system-packages" ) );

        StringBuilder sb = new StringBuilder();
        File bundleSystemDir = new File( "system" );
        if ( !bundleSystemDir.exists() )
        {
            bundleSystemDir.mkdirs();
        }
        for ( File pluginFile : bundleSystemDir.listFiles() )
        {
            if ( pluginFile.isFile() )
            {
                if ( sb.length() > 0 )
                {
                    sb.append( " " );
                }
                sb.append( pluginFile.toURI() );
            }
        }
        File bundlePluginsDir = new File( "plugins" );
        if ( !bundlePluginsDir.exists() )
        {
            bundlePluginsDir.mkdirs();
        }
        for ( File pluginFile : bundlePluginsDir.listFiles() )
        {
            if ( pluginFile.isFile() )
            {
                if ( sb.length() > 0 )
                {
                    sb.append( " " );
                }
                sb.append( pluginFile.toURI() );
            }
        }

        System.out.println( "Autostart path : " + sb.toString() );
        String autostart = props.getProperty( "osgi.autostart.1" ) + " " + sb.toString();
        configMap.put( AutoActivator.AUTO_START_PROP + ".1", autostart );
        configMap.put( BundleCache.CACHE_PROFILE_DIR_PROP, props.getProperty( "osgi.cache-directory" ) );

        List<BundleActivator> activators = new ArrayList<BundleActivator>();
        activators.add( new AutoActivator( configMap ) );
        activator = new HostActivator();
        activators.add( activator );
        felix = new Felix( configMap, activators );
        try
        {
            felix.start();
        }
        catch ( BundleException e )
        {
            System.err.println( "Could not create framework: " + e );
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    public Builder getBuilder()
    {
        return activator.getBuilder();
    }

    public static void main( String[] args )
        throws Exception
    {
        PluginManager manager = new PluginManager();

        manager.getBuilder().execute( 1, 1 );
        //Thread.sleep( 10000 );
        //felix.stop();
    }
}
