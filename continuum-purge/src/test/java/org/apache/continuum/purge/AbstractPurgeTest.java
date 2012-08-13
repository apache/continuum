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

import org.apache.continuum.dao.DirectoryPurgeConfigurationDao;
import org.apache.continuum.dao.DistributedDirectoryPurgeConfigurationDao;
import org.apache.continuum.dao.LocalRepositoryDao;
import org.apache.continuum.dao.RepositoryPurgeConfigurationDao;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.maven.continuum.jdo.MemoryJdoFactory;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.jpox.SchemaTool;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

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

    protected static final String TEST_BUILD_AGENT_URL = "http://localhost:8181/continuum-buildagent/xmlrpc";

    protected static final int TEST_DAYS_OLDER = 30;

    protected static final int TEST_RETENTION_COUNT = 2;

    protected static final String TEST_RELEASES_DIRECTORY_TYPE = "releases";

    protected static final String TEST_BUILDOUTPUT_DIRECTORY_TYPE = "buildOutput";

    protected static final String TEST_WORKING_DIRECTORY_TYPE = "working";

    protected LocalRepositoryDao localRepositoryDao;

    protected DirectoryPurgeConfigurationDao directoryPurgeConfigurationDao;

    protected RepositoryPurgeConfigurationDao repositoryPurgeConfigurationDao;

    protected DistributedDirectoryPurgeConfigurationDao distributedDirectoryPurgeConfigurationDao;

    protected RepositoryPurgeConfiguration defaultRepoPurge;

    protected DirectoryPurgeConfiguration defaultReleasesDirPurge;

    protected DirectoryPurgeConfiguration defaultBuildOutputDirPurge;

    protected LocalRepository defaultRepository;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        init();

        localRepositoryDao = (LocalRepositoryDao) lookup( LocalRepositoryDao.class.getName() );

        repositoryPurgeConfigurationDao = (RepositoryPurgeConfigurationDao) lookup(
            RepositoryPurgeConfigurationDao.class );

        directoryPurgeConfigurationDao = (DirectoryPurgeConfigurationDao) lookup(
            DirectoryPurgeConfigurationDao.class );

        distributedDirectoryPurgeConfigurationDao = (DistributedDirectoryPurgeConfigurationDao) lookup(
            DistributedDirectoryPurgeConfigurationDao.class );

        if ( localRepositoryDao.getAllLocalRepositories().size() == 0 )
        {
            createDefaultRepository();
            assertEquals( "check # repository", 1, localRepositoryDao.getAllLocalRepositories().size() );
            createDefaultRepoPurgeConfiguration();
        }
        else
        {
            assertEquals( "check # repository", 1, localRepositoryDao.getAllLocalRepositories().size() );
            defaultRepository = localRepositoryDao.getLocalRepositoryByName( TEST_DEFAULT_REPO_NAME );
            defaultRepoPurge = repositoryPurgeConfigurationDao.getRepositoryPurgeConfigurationsByLocalRepository(
                defaultRepository.getId() ).get( 0 );
        }

        if ( directoryPurgeConfigurationDao.getDirectoryPurgeConfigurationsByType(
            TEST_RELEASES_DIRECTORY_TYPE ).size() == 0 )
        {
            createDefaultReleasesDirPurgeConfiguration();
        }
        else
        {
            defaultReleasesDirPurge = directoryPurgeConfigurationDao.getDirectoryPurgeConfigurationsByType(
                TEST_RELEASES_DIRECTORY_TYPE ).get( 0 );
        }

        if ( directoryPurgeConfigurationDao.getDirectoryPurgeConfigurationsByType(
            TEST_BUILDOUTPUT_DIRECTORY_TYPE ).size() == 0 )
        {
            createDefaultBuildOutputDirPurgeConfiguration();
        }
        else
        {
            defaultBuildOutputDirPurge = directoryPurgeConfigurationDao.getDirectoryPurgeConfigurationsByType(
                TEST_BUILDOUTPUT_DIRECTORY_TYPE ).get( 0 );
        }
    }

    protected void init()
        throws Exception
    {
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

        SchemaTool.createSchemaTables( new URL[]{getClass().getResource( "/package.jdo" )}, new URL[]{}, null, false,
                                       null );
    }

    protected File getDefaultRepositoryLocation()
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
            releasesDirectory.mkdirs();
        }

        return releasesDirectory;
    }

    protected File getBuildOutputDirectoryLocation()
    {
        File buildOutputDir = getTestFile( TEST_DEFAULT_BUILDOUTPUT_DIR );

        if ( !buildOutputDir.exists() )
        {
            buildOutputDir.mkdirs();
        }

        return buildOutputDir;
    }

    private void createDefaultRepository()
        throws Exception
    {
        defaultRepository = localRepositoryDao.getLocalRepositoryByName( TEST_DEFAULT_REPO_NAME );

        if ( defaultRepository == null )
        {
            LocalRepository repository = new LocalRepository();

            repository.setName( TEST_DEFAULT_REPO_NAME );
            repository.setLocation( getDefaultRepositoryLocation().getAbsolutePath() );
            defaultRepository = localRepositoryDao.addLocalRepository( repository );
        }
    }

    private void createDefaultRepoPurgeConfiguration()
        throws Exception
    {
        RepositoryPurgeConfiguration repoPurge = new RepositoryPurgeConfiguration();

        repoPurge.setRepository( defaultRepository );
        repoPurge.setDeleteAll( true );

        defaultRepoPurge = repositoryPurgeConfigurationDao.addRepositoryPurgeConfiguration( repoPurge );
    }

    private void createDefaultReleasesDirPurgeConfiguration()
        throws Exception
    {
        DirectoryPurgeConfiguration dirPurge = new DirectoryPurgeConfiguration();

        dirPurge.setLocation( getReleasesDirectoryLocation().getAbsolutePath() );
        dirPurge.setDirectoryType( "releases" );
        dirPurge.setDeleteAll( true );

        defaultReleasesDirPurge = directoryPurgeConfigurationDao.addDirectoryPurgeConfiguration( dirPurge );
    }

    private void createDefaultBuildOutputDirPurgeConfiguration()
        throws Exception
    {
        DirectoryPurgeConfiguration dirPurge = new DirectoryPurgeConfiguration();

        dirPurge.setLocation( getBuildOutputDirectoryLocation().getAbsolutePath() );
        dirPurge.setDirectoryType( "buildOutput" );
        dirPurge.setDeleteAll( true );

        defaultBuildOutputDirPurge = directoryPurgeConfigurationDao.addDirectoryPurgeConfiguration( dirPurge );
    }
}
