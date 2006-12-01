package org.apache.maven.continuum.configuration;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.continuum.configuration.io.xpp3.ConfigurationXpp3Reader;
import org.apache.maven.continuum.configuration.io.xpp3.ConfigurationXpp3Writer;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Load and store the configuration. No synchronization is used, but it is unnecessary as the old configuration object
 * can continue to be used.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @plexus.component
 */
public class DefaultConfigurationStore
    extends AbstractLogEnabled
    implements ConfigurationStore, Initializable
{
    /**
     * @plexus.configuration default-value="${configuration.store.file}"
     */
    private File file;

    /**
     * The cached configuration.
     */
    private Configuration configuration;

    /**
     * List of listeners to configuration changes.
     */
    private List/*<ConfigurationChangeListener>*/ listeners = new LinkedList();

    public Configuration getConfigurationFromStore()
        throws ConfigurationStoreException
    {
        if ( configuration == null )
        {
            ConfigurationXpp3Reader reader = new ConfigurationXpp3Reader();

            FileReader fileReader;
            try
            {
                fileReader = new FileReader( file );
            }
            catch ( FileNotFoundException e )
            {
                getLogger().warn( "Configuration file: " + file + " not found. Using defaults." );
                configuration = new Configuration();
                return configuration;
            }

            getLogger().info( "Reading configuration from " + file );
            try
            {
                configuration = reader.read( fileReader, false );
            }
            catch ( IOException e )
            {
                throw new ConfigurationStoreException( e.getMessage(), e );
            }
            catch ( XmlPullParserException e )
            {
                throw new ConfigurationStoreException( e.getMessage(), e );
            }
            finally
            {
                IOUtil.close( fileReader );
            }
        }
        return configuration;
    }

    public void storeConfiguration( Configuration configuration )
        throws ConfigurationStoreException, InvalidConfigurationException, ConfigurationChangeException
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ConfigurationChangeListener listener = (ConfigurationChangeListener) i.next();

            listener.notifyOfConfigurationChange( configuration );
        }

        ConfigurationXpp3Writer writer = new ConfigurationXpp3Writer();

        getLogger().info( "Writing configuration to " + file );
        FileWriter fileWriter = null;
        try
        {
            file.getParentFile().mkdirs();

            fileWriter = new FileWriter( file );
            writer.write( fileWriter, configuration );
        }
        catch ( IOException e )
        {
            throw new ConfigurationStoreException( e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( fileWriter );
        }
    }

    public void addChangeListener( ConfigurationChangeListener listener )
    {
        listeners.add( listener );
    }

    public void initialize()
        throws InitializationException
    {
        if ( file == null )
        {
            file = new File( System.getProperty( "user.home" ), "/.m2/continuum.xml" );
        }
    }
}
