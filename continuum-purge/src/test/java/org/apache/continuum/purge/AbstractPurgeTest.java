package org.apache.continuum.purge;

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
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.maven.continuum.jdo.MemoryJdoFactory;
import org.apache.maven.continuum.store.ContinuumStore;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.jpox.SchemaTool;

/**
 * @author Maria Catherine Tan
 */
public abstract class AbstractPurgeTest
    extends PlexusInSpringTestCase
{   
    private static final String TEST_DEFAULT_REPO_DIR = "target/default-repository";
    
    private static final String TEST_DEFAULT_REPO_NAME = "defaultRepo";
    
    private static final String TEST_DEFAULT_RELEASES_DIR = "target/working-directory";
    
    private static final String TEST_DEFAULT_BUILDOUTPUT_DIR = "target/build-output-directory";
    
    protected static final int TEST_DAYS_OLDER = 30;
    
    protected static final int TEST_RETENTION_COUNT = 2;
    
    protected static final String TEST_RELEASES_DIRECTORY_TYPE = "releases";
    
    protected static final String TEST_BUILDOUTPUT_DIRECTORY_TYPE = "buildOutput";
    
    private ContinuumStore store;
    
    protected RepositoryPurgeConfiguration defaultRepoPurge;
    
    protected DirectoryPurgeConfiguration defaultReleasesDirPurge;
    
    protected DirectoryPurgeConfiguration defaultBuildOutputDirPurge;
    
    protected LocalRepository defaultRepository;
    
    protected void setUp()
        throws Exception
    {
        super.setUp();
    
        getStore();        
        
        if ( store.getAllLocalRepositories().size() == 0 )
        {
            createDefaultRepository();
            assertEquals( "check # repository", 1, store.getAllLocalRepositories().size() );
            createDefaultRepoPurgeConfiguration();
        }
        else
        {
            assertEquals( "check # repository", 1, store.getAllLocalRepositories().size() );
            defaultRepository = store.getLocalRepositoryByName( TEST_DEFAULT_REPO_NAME );
            defaultRepoPurge = store.getRepositoryPurgeConfigurationsByLocalRepository( defaultRepository.getId() ).get( 0 );
        }
        
        if ( store.getDirectoryPurgeConfigurationsByType( TEST_RELEASES_DIRECTORY_TYPE ).size() == 0 )
        {
            createDefaultReleasesDirPurgeConfiguration();
        }
        else
        {
            defaultReleasesDirPurge = store.getDirectoryPurgeConfigurationsByType( TEST_RELEASES_DIRECTORY_TYPE ).get( 0 );
        }
        
        if ( store.getDirectoryPurgeConfigurationsByType( TEST_BUILDOUTPUT_DIRECTORY_TYPE ).size() == 0 )
        {
            createDefaultBuildOutputDirPurgeConfiguration();
        }
        else
        {
            defaultBuildOutputDirPurge = store.getDirectoryPurgeConfigurationsByType( TEST_BUILDOUTPUT_DIRECTORY_TYPE ).get( 0 );
        }
    }
    
    protected ContinuumStore getStore()
        throws Exception
    {
        if ( store != null )
        {
            return store;
        }
    
        // ----------------------------------------------------------------------
        // Set up the JDO factory
        // ----------------------------------------------------------------------
    
        Object o = lookup( JdoFactory.ROLE, "continuum" );
    
        assertEquals( MemoryJdoFactory.class.getName(), o.getClass().getName() );
    
        MemoryJdoFactory jdoFactory = (MemoryJdoFactory) o;
        
        String url = "jdbc:hsqldb:mem:" + getClass().getName() + "." + getName();
    
        jdoFactory.setUrl( url );
    
        jdoFactory.reconfigure();
        
        // ----------------------------------------------------------------------
        // Check the configuration
        // ----------------------------------------------------------------------
        
        PersistenceManagerFactory pmf = jdoFactory.getPersistenceManagerFactory();
    
        assertNotNull( pmf );
    
        assertEquals( url, pmf.getConnectionURL() );
    
        PersistenceManager pm = pmf.getPersistenceManager();
    
        pm.close();
        
        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------
    
        Properties properties = jdoFactory.getProperties();
    
        for ( Map.Entry entry : properties.entrySet() )
        {
            System.setProperty( (String) entry.getKey(), (String) entry.getValue() );
        }
    
        SchemaTool.createSchemaTables( new URL[]{getClass().getResource( "/META-INF/package.jdo" )}, new URL[]{}, null,
                                       false, null );
    
        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------
    
        store = (ContinuumStore) lookup( ContinuumStore.ROLE, "jdo" );
    
        return store;
    }
    
    protected File getDefaultRepositoryLocation()
        throws Exception
    {
        File repositoryLocation = getTestFile( TEST_DEFAULT_REPO_DIR );
        
        if ( !repositoryLocation.exists() )
        {
            repositoryLocation.mkdirs();
        }
        
        return repositoryLocation;
    }
    
    protected File getReleasesDirectoryLocation()
    {
        File releasesDirectory = getTestFile( TEST_DEFAULT_RELEASES_DIR );
        
        if ( !releasesDirectory.exists() )
        {
            releasesDirectory.mkdir();
        }
        
        return releasesDirectory;
    }
    
    protected File getBuildOutputDirectoryLocation()
    {
        File buildOutputDir = getTestFile( TEST_DEFAULT_BUILDOUTPUT_DIR );
        
        if ( !buildOutputDir.exists() )
        {
            buildOutputDir.mkdir();
        }
        
        return buildOutputDir;
    }

    private void createDefaultRepository()
        throws Exception
    {
        defaultRepository = store.getLocalRepositoryByName( TEST_DEFAULT_REPO_NAME );
        
        if ( defaultRepository == null )
        {
            LocalRepository repository = new LocalRepository();
            
            repository.setName( TEST_DEFAULT_REPO_NAME );
            repository.setLocation( getDefaultRepositoryLocation().getAbsolutePath() );
            defaultRepository = store.addLocalRepository( repository );
        }
    }
    
    private void createDefaultRepoPurgeConfiguration()
        throws Exception
    {
        RepositoryPurgeConfiguration repoPurge = new RepositoryPurgeConfiguration();
        
        repoPurge.setRepository( defaultRepository );
        repoPurge.setDeleteAll( true );
        
        defaultRepoPurge = store.addRepositoryPurgeConfiguration(  repoPurge );
    }
    
    private void createDefaultReleasesDirPurgeConfiguration()
        throws Exception
    {
        DirectoryPurgeConfiguration dirPurge = new DirectoryPurgeConfiguration();
        
        dirPurge.setLocation( getReleasesDirectoryLocation().getAbsolutePath() );
        dirPurge.setDirectoryType( "releases" );
        dirPurge.setDeleteAll( true );
        
        defaultReleasesDirPurge = store.addDirectoryPurgeConfiguration( dirPurge );
    }
    
    private void createDefaultBuildOutputDirPurgeConfiguration()
        throws Exception
    {
        DirectoryPurgeConfiguration dirPurge = new DirectoryPurgeConfiguration();
        
        dirPurge.setLocation( getBuildOutputDirectoryLocation().getAbsolutePath() );
        dirPurge.setDirectoryType( "buildOutput" );
        dirPurge.setDeleteAll( true );
        
        defaultBuildOutputDirPurge = store.addDirectoryPurgeConfiguration( dirPurge );
    }
}
