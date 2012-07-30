package org.apache.continuum.release.config;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.model.Scm;
import org.apache.maven.shared.release.config.PropertiesReleaseDescriptorStore;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.config.ReleaseDescriptorStoreException;
import org.apache.maven.shared.release.config.ReleaseUtils;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.jetty.util.security.Password;

public class ContinuumPropertiesReleaseDescriptorStore
    extends PropertiesReleaseDescriptorStore
{
    public ReleaseDescriptor read( ReleaseDescriptor mergeDescriptor, File file )
        throws ReleaseDescriptorStoreException
    {
        Properties properties = new Properties();

        InputStream inStream = null;
        try
        {
            inStream = new FileInputStream( file );

            properties.load( inStream );
        }
        catch ( FileNotFoundException e )
        {
            getLogger().debug( file.getName() + " not found - using empty properties" );
        }
        catch ( IOException e )
        {
            throw new ReleaseDescriptorStoreException(
                "Error reading properties file '" + file.getName() + "': " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( inStream );
        }

        ContinuumReleaseDescriptor releaseDescriptor = new ContinuumReleaseDescriptor();
        releaseDescriptor.setCompletedPhase( properties.getProperty( "completedPhase" ) );
        releaseDescriptor.setScmSourceUrl( properties.getProperty( "scm.url" ) );
        releaseDescriptor.setScmUsername( properties.getProperty( "scm.username" ) );
        
        String password = properties.getProperty( "scm.password" );
        if ( password != null && password.startsWith( "OBF:" ) )
        {
            releaseDescriptor.setScmPassword( Password.deobfuscate( password ) );
        }
        else
        {
            releaseDescriptor.setScmPassword( password );
        }
        releaseDescriptor.setScmPrivateKey( properties.getProperty( "scm.privateKey" ) );
        releaseDescriptor.setScmPrivateKeyPassPhrase( properties.getProperty( "scm.passphrase" ) );
        releaseDescriptor.setScmTagBase( properties.getProperty( "scm.tagBase" ) );
        releaseDescriptor.setScmReleaseLabel( properties.getProperty( "scm.tag" ) );
        releaseDescriptor.setScmCommentPrefix( properties.getProperty( "scm.commentPrefix" ) );
        releaseDescriptor.setAdditionalArguments( properties.getProperty( "exec.additionalArguments" ) );
        releaseDescriptor.setPomFileName( properties.getProperty( "exec.pomFileName" ) );
        releaseDescriptor.setPreparationGoals( properties.getProperty( "preparationGoals" ) );
        releaseDescriptor.setExecutable( properties.getProperty( "build.executable" ) );
        releaseDescriptor.setReleaseBy( properties.getProperty( "release.by" ) );

        loadResolvedDependencies( properties, releaseDescriptor );

        // boolean properties are not written to the properties file because the value from the caller is always used

        for ( Object o : properties.keySet() )
        {
            String property = (String) o;
            if ( property.startsWith( "project.rel." ) )
            {
                releaseDescriptor.mapReleaseVersion( property.substring( "project.rel.".length() ),
                                                     properties.getProperty( property ) );
            }
            else if ( property.startsWith( "project.dev." ) )
            {
                releaseDescriptor.mapDevelopmentVersion( property.substring( "project.dev.".length() ),
                                                         properties.getProperty( property ) );
            }
            else if ( property.startsWith( "project.scm." ) )
            {
                int index = property.lastIndexOf( '.' );
                if ( index > "project.scm.".length() )
                {
                    String key = property.substring( "project.scm.".length(), index );

                    if ( !releaseDescriptor.getOriginalScmInfo().containsKey( key ) )
                    {
                        if ( properties.getProperty( "project.scm." + key + ".empty" ) != null )
                        {
                            releaseDescriptor.mapOriginalScmInfo( key, null );
                        }
                        else
                        {
                            Scm scm = new Scm();
                            scm.setConnection( properties.getProperty( "project.scm." + key + ".connection" ) );
                            scm.setDeveloperConnection(
                                properties.getProperty( "project.scm." + key + ".developerConnection" ) );
                            scm.setUrl( properties.getProperty( "project.scm." + key + ".url" ) );
                            scm.setTag( properties.getProperty( "project.scm." + key + ".tag" ) );

                            releaseDescriptor.mapOriginalScmInfo( key, scm );
                        }
                    }
                }
            }
            else if ( property.startsWith( "build.env." ) )
            {
                releaseDescriptor.mapEnvironments( property.substring( "build.env.".length() ),
                                                   properties.getProperty( property ) );
            }
        }

        if ( mergeDescriptor != null )
        {
            releaseDescriptor = (ContinuumReleaseDescriptor) ReleaseUtils.merge( releaseDescriptor, mergeDescriptor );
            releaseDescriptor.setEnvironments( ( (ContinuumReleaseDescriptor) mergeDescriptor ).getEnvironments() );
        }

        return releaseDescriptor;
    }

    public void write( ReleaseDescriptor configFile, File file )
        throws ReleaseDescriptorStoreException
    {
        ContinuumReleaseDescriptor config = (ContinuumReleaseDescriptor) configFile;
        Properties properties = new Properties();
        properties.setProperty( "completedPhase", config.getCompletedPhase() );
        properties.setProperty( "scm.url", config.getScmSourceUrl() );
        if ( config.getScmUsername() != null )
        {
            properties.setProperty( "scm.username", config.getScmUsername() );
        }
        if ( config.getScmPassword() != null )
        {
            // obfuscate password
            properties.setProperty( "scm.password", Password.obfuscate( config.getScmPassword() ) );
        }
        if ( config.getScmPrivateKey() != null )
        {
            properties.setProperty( "scm.privateKey", config.getScmPrivateKey() );
        }
        if ( config.getScmPrivateKeyPassPhrase() != null )
        {
            properties.setProperty( "scm.passphrase", config.getScmPrivateKeyPassPhrase() );
        }
        if ( config.getScmTagBase() != null )
        {
            properties.setProperty( "scm.tagBase", config.getScmTagBase() );
        }
        if ( config.getScmReleaseLabel() != null )
        {
            properties.setProperty( "scm.tag", config.getScmReleaseLabel() );
        }
        if ( config.getScmCommentPrefix() != null )
        {
            properties.setProperty( "scm.commentPrefix", config.getScmCommentPrefix() );
        }
        if ( config.getAdditionalArguments() != null )
        {
            properties.setProperty( "exec.additionalArguments", config.getAdditionalArguments() );
        }
        if ( config.getPomFileName() != null )
        {
            properties.setProperty( "exec.pomFileName", config.getPomFileName() );
        }
        if ( config.getPreparationGoals() != null )
        {
            properties.setProperty( "preparationGoals", config.getPreparationGoals() );
        }

        // boolean properties are not written to the properties file because the value from the caller is always used

        for ( Object o : config.getReleaseVersions().entrySet() )
        {
            Entry entry = (Entry) o;
            properties.setProperty( "project.rel." + entry.getKey(), (String) entry.getValue() );
        }

        for ( Object o : config.getDevelopmentVersions().entrySet() )
        {
            Entry entry = (Entry) o;
            properties.setProperty( "project.dev." + entry.getKey(), (String) entry.getValue() );
        }

        for ( Object o : config.getOriginalScmInfo().entrySet() )
        {
            Entry entry = (Entry) o;
            Scm scm = (Scm) entry.getValue();
            String prefix = "project.scm." + entry.getKey();
            if ( scm != null )
            {
                if ( scm.getConnection() != null )
                {
                    properties.setProperty( prefix + ".connection", scm.getConnection() );
                }
                if ( scm.getDeveloperConnection() != null )
                {
                    properties.setProperty( prefix + ".developerConnection", scm.getDeveloperConnection() );
                }
                if ( scm.getUrl() != null )
                {
                    properties.setProperty( prefix + ".url", scm.getUrl() );
                }
                if ( scm.getTag() != null )
                {
                    properties.setProperty( prefix + ".tag", scm.getTag() );
                }
            }
            else
            {
                properties.setProperty( prefix + ".empty", "true" );
            }
        }

        for ( Object o : config.getEnvironments().entrySet() )
        {
            Entry entry = (Entry) o;
            properties.setProperty( "build.env." + entry.getKey(), (String) entry.getValue() );
        }

        if ( ( config.getResolvedSnapshotDependencies() != null ) &&
            ( config.getResolvedSnapshotDependencies().size() > 0 ) )
        {
            processResolvedDependencies( properties, config.getResolvedSnapshotDependencies() );
        }

        // executables
        if ( config.getExecutable() != null )
        {
            properties.setProperty( "build.executable", config.getExecutable() );
        }

        // release by
        if ( config.getReleaseBy() != null )
        {
            properties.setProperty( "release.by", config.getReleaseBy() );
        }

        OutputStream outStream = null;
        //noinspection OverlyBroadCatchBlock
        try
        {
            outStream = new FileOutputStream( file );

            properties.store( outStream, "release configuration" );
        }
        catch ( IOException e )
        {
            throw new ReleaseDescriptorStoreException(
                "Error writing properties file '" + file.getName() + "': " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( outStream );
        }

    }

    private void processResolvedDependencies( Properties prop, Map resolvedDependencies )
    {
        Set entries = resolvedDependencies.entrySet();
        Iterator iterator = entries.iterator();
        Entry currentEntry;

        while ( iterator.hasNext() )
        {
            currentEntry = (Entry) iterator.next();

            Map versionMap = (Map) currentEntry.getValue();

            prop.setProperty( "dependency." + currentEntry.getKey() + ".release",
                              (String) versionMap.get( ReleaseDescriptor.RELEASE_KEY ) );
            prop.setProperty( "dependency." + currentEntry.getKey() + ".development",
                              (String) versionMap.get( ReleaseDescriptor.DEVELOPMENT_KEY ) );
        }
    }

    private void loadResolvedDependencies( Properties prop, ReleaseDescriptor descriptor )
    {
        Map<String, Map<String, Object>> resolvedDependencies = new HashMap<String, Map<String, Object>>();

        Set entries = prop.entrySet();
        Iterator iterator = entries.iterator();
        String propertyName;
        Entry currentEntry;

        while ( iterator.hasNext() )
        {
            currentEntry = (Entry) iterator.next();
            propertyName = (String) currentEntry.getKey();

            if ( propertyName.startsWith( "dependency." ) )
            {
                Map<String, Object> versionMap;
                String artifactVersionlessKey;
                int startIndex;
                int endIndex;
                String versionType;

                startIndex = propertyName.lastIndexOf( "dependency." );

                if ( propertyName.indexOf( ".development" ) != -1 )
                {
                    endIndex = propertyName.indexOf( ".development" );
                    versionType = ReleaseDescriptor.DEVELOPMENT_KEY;
                }
                else
                {
                    endIndex = propertyName.indexOf( ".release" );
                    versionType = ReleaseDescriptor.RELEASE_KEY;
                }

                artifactVersionlessKey = propertyName.substring( startIndex, endIndex );

                if ( resolvedDependencies.containsKey( artifactVersionlessKey ) )
                {
                    versionMap = resolvedDependencies.get( artifactVersionlessKey );
                }
                else
                {
                    versionMap = new HashMap<String, Object>();
                    resolvedDependencies.put( artifactVersionlessKey, versionMap );
                }

                versionMap.put( versionType, currentEntry.getValue() );
            }
        }

        descriptor.setResolvedSnapshotDependencies( resolvedDependencies );
    }
}
