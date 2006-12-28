package org.apache.maven.continuum.store.jdo;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Profile;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.model.scm.SuiteResult;
import org.apache.maven.continuum.model.scm.TestCaseFailure;
import org.apache.maven.continuum.model.scm.TestResult;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.jdo.DefaultConfigurableJdoFactory;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.logging.LogEnabled;
import org.jpox.SchemaTool;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Provides some service methods for Store test case extensions.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 */
public abstract class AbstractJdoStoreTestCase extends PlexusTestCase
{

    /**
     * Default location for SQL test data.
     */
    private static final String SQL_TEST_DATA = "src/test/resources/testData.sql";

    /**
     * Password to connect to the target test database instance.
     */
    private static final String PASSWORD_TEST_DATABASE = "";

    /**
     * Username to connect to the target test database instance.
     */
    private static final String USERNAME_TEST_DATABASE = "sa";

    /**
     * Driver class to connect to the target database instance.
     */
    private static final String DRIVER_TEST_DATABASE = "org.hsqldb.jdbcDriver";

    /**
     * JDBC URL to connect to the target test database instance.
     */
    private final String URL_TEST_DATABASE = "jdbc:hsqldb:mem:" + getName() + new Date().getTime();

    /**
     * DDL for Database creation.
     */
    private static final File SQL_DATABSE_SCHEMA = getTestFile( getBasedir(), "src/test/resources/schema.sql" );

    /**
     * Provides an interface to clients to execute queries on the underlying
     * database.
     */
    private PersistenceManager persistenceManager;

    private DefaultConfigurableJdoFactory jdoFactory;

    /**
     * @see org.codehaus.plexus.PlexusTestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        jdoFactory = createJdoFactory();

        // persistenceManager =
        // jdoFactory.getPersistenceManagerFactory().getPersistenceManager();
    }

    /**
     * @see org.codehaus.plexus.PlexusTestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();

        teardownBuildDatabase();
    }

    /**
     * Creates an instance of Continuum Database for test purposes and loads up
     * the test data from the specified schema and test data SQL scripts.
     * 
     * @throws Exception if there was an error with test database set up.
     */
    protected void createBuildDatabase() throws Exception
    {
        if ( null == persistenceManager || persistenceManager.isClosed() )
            persistenceManager = jdoFactory.getPersistenceManagerFactory().getPersistenceManager();

        Connection connection = (Connection) persistenceManager.getDataStoreConnection().getNativeConnection();

        loadSQL( SQL_DATABSE_SCHEMA, connection );

        // load test data.
        List scripts = getSQLScripts();

        for ( Iterator it = scripts.iterator(); it.hasNext(); )
        {
            File script = (File) it.next();

            // System.out.println( "Loading SQL data from script: " +
            // script.getAbsolutePath() );

            loadSQL( script, connection );
        }

    }

    /**
     * Clean up the Continuum test database.
     * 
     * @throws Exception
     */
    protected void teardownBuildDatabase() throws Exception
    {
        if ( null == persistenceManager || persistenceManager.isClosed() )
            persistenceManager = jdoFactory.getPersistenceManagerFactory().getPersistenceManager();

        // deleteAllEntities( Project.class );
        // deleteAllEntities( ProjectGroup.class );
        // deleteAllEntities( Schedule.class );
        // deleteAllEntities( Profile.class );
        // deleteAllEntities( Installation.class );
        // deleteAllEntities( ScmResult.class );
        // deleteAllEntities( BuildResult.class );
        // deleteAllEntities( TestResult.class );
        // deleteAllEntities( SuiteResult.class );
        // deleteAllEntities( TestCaseFailure.class );
        // deleteAllEntities( SystemConfiguration.class );
        // deleteAllEntities( ProjectNotifier.class );
        // deleteAllEntities( ProjectDeveloper.class );
        // deleteAllEntities( ProjectDependency.class );
        // deleteAllEntities( ChangeSet.class );
        // deleteAllEntities( ChangeFile.class );
        // deleteAllEntities( BuildDefinition.class );

        URL[] jdoFiles = new URL[] { this.getClass().getClassLoader().getResource( "META-INF/package.jdo" ) };

        URL[] classFiles =
            new URL[] { this.getClass().getClassLoader().getResource( Project.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( ProjectGroup.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( Schedule.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( Profile.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( Installation.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( ScmResult.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( BuildResult.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( TestResult.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( SuiteResult.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( TestCaseFailure.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( SystemConfiguration.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( ProjectNotifier.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( ProjectDeveloper.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( ProjectDependency.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( ChangeSet.class.getCanonicalName() ),
                this.getClass().getClassLoader().getResource( BuildDefinition.class.getCanonicalName() ) };

        // prepare System properties that the SchemaTool expects
        System.setProperty( SchemaTool.JDO_DATASTORE_DRIVERNAME_PROPERTY, DRIVER_TEST_DATABASE );
        System.setProperty( SchemaTool.JDO_DATASTORE_URL_PROPERTY, URL_TEST_DATABASE );
        System.setProperty( SchemaTool.JDO_DATASTORE_USERNAME_PROPERTY, USERNAME_TEST_DATABASE );
        System.setProperty( SchemaTool.JDO_DATASTORE_PASSWORD_PROPERTY, PASSWORD_TEST_DATABASE );
        SchemaTool.deleteSchemaTables( jdoFiles, null, true );
    }

    /**
     * Deletes records for a given entity.
     * 
     * @param klass Entity class for which the records are to be deleted.
     * 
     */
    private void deleteAllEntities( Class klass )
    {
        Transaction tx = persistenceManager.currentTransaction();

        try
        {
            tx.begin();

            Query query = persistenceManager.newQuery( klass );
            query.deletePersistentAll();

            tx.commit();
        }
        finally
        {
            if ( tx.isActive() )
            {
                tx.rollback();
            }
        }
    }

    /**
     * Returns {@link PersistenceManager} instance to interact with database
     * 
     * @return {@link PersistenceManager} instance to interact with database.
     */
    protected PersistenceManager getPersistenceManager()
    {
        return persistenceManager;
    }

    /**
     * Extensions are allowed to implement and return a list of SQL script
     * {@link File} instances that are to be read and loaded into the target
     * test database.
     * 
     * @return List of locations of SQL scripts
     */
    protected List getSQLScripts()
    {
        List list = new ArrayList();
        // add default test data source.
        list.add( getTestFile( getBasedir(), SQL_TEST_DATA ) );
        return list;
    }

    /**
     * Reads SQL statements from the specified file and uses the passed in
     * {@link Connection} to populate the target database instance.
     * 
     * @param sqlData SQL data to load.
     * @param connection {@link Connection} instance that wraps an underlying
     *            connection to target database.
     * @throws Exception if there was an error reading SQL scripts or executing
     *             SQL statements for the target Database.
     */
    private void loadSQL( File sqlData, Connection connection ) throws Exception
    {
        FileInputStream fis = new FileInputStream( sqlData );
        BufferedReader br = new BufferedReader( new InputStreamReader( fis ) );
        List sqlList = new ArrayList();

        // build a list of SQL statements to execute here
        String line = br.readLine();
        StringBuffer sb = new StringBuffer();
        while ( null != line )
        {
            // only add to sql list if its not empty or not commented
            if ( !line.trim().equals( PASSWORD_TEST_DATABASE ) && !line.startsWith( "#" ) && !line.startsWith( "--" ) )
            {
                sb.append( line );
                // check if the SQL statement was terminated
                if ( line.endsWith( ";" ) )
                {
                    sqlList.add( sb.toString() );
                    sb = new StringBuffer();
                }
            }
            line = br.readLine();
        }

        if ( sb.length() > 0 )
            sqlList.add( sb.toString() );

        // System.out.println( "Running SQL statements..." );

        // Execute list of SQL statements
        for ( Iterator it = sqlList.iterator(); it.hasNext(); )
        {
            String sql = (String) it.next();

            // System.out.println( sql );

            try
            {
                Statement stmt = connection.createStatement();
                stmt.execute( sql );
                connection.commit();
            }
            catch ( SQLException e )
            {
                connection.rollback();
                throw e;
            }
        }
        // System.out.println( "Done!" );
    }

    /**
     * @return
     * @throws Exception
     */
    private DefaultConfigurableJdoFactory createJdoFactory() throws Exception
    {
        DefaultConfigurableJdoFactory jdoFactory = (DefaultConfigurableJdoFactory) lookup( JdoFactory.ROLE );

        jdoFactory.setUrl( URL_TEST_DATABASE );

        jdoFactory.setDriverName( DRIVER_TEST_DATABASE );

        jdoFactory.setUserName( USERNAME_TEST_DATABASE );

        jdoFactory.setPassword( PASSWORD_TEST_DATABASE );

        jdoFactory.setProperty( "javax.jdo.option.NontransactionalRead", "true" );
        return jdoFactory;
    }
}
