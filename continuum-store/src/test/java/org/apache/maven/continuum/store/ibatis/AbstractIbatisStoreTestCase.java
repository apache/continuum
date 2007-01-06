package org.apache.maven.continuum.store.ibatis;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import org.codehaus.plexus.PlexusTestCase;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id: AbstractIbatisStoreTestCase.java 491397 2006-12-31 06:32:52Z
 *          rinku $
 * @since 1.1
 */
public abstract class AbstractIbatisStoreTestCase extends PlexusTestCase
{

    private static final String TEST_DATA_XML = "src/test/resources/db/test-data.xml";

    /**
     * Default location for SQL test data.
     */
    private static final String SQL_TEST_DATA = "src/test/resources/db/testData.sql";

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
    // private static final String DRIVER_TEST_DATABASE =
    // "org.hsqldb.jdbcDriver";
    private static final String DRIVER_TEST_DATABASE = "org.apache.derby.jdbc.EmbeddedDriver";

    /**
     * JDBC URL to connect to the target test database instance.
     */
    // private final String URL_TEST_DATABASE = "jdbc:hsqldb:mem:" + getName();
    private final String URL_TEST_DATABASE = "jdbc:derby:" + getName() + ";create=true";

    /**
     * DDL for Database creation.
     */
    private static final String SQL_DATABSE_SCHEMA = "src/test/resources/db/schema.sql";

    private IDatabaseTester dbTester;

    /**
     * Creates an instance of Continuum Database for test purposes and loads up
     * the test data from the specified schema and test data SQL scripts.
     * 
     * @throws Exception if there was an error with test database set up.
     */
    protected void createBuildDatabase() throws Exception
    {
        dbTester =
            new JdbcDatabaseTester( DRIVER_TEST_DATABASE, URL_TEST_DATABASE, USERNAME_TEST_DATABASE,
                                    PASSWORD_TEST_DATABASE );
        File testFile = getTestFile( getBasedir(), TEST_DATA_XML );
        assertTrue( testFile.exists() );

        // dbTester.setSchema( getName() );

        dbTester.setDataSet( new XmlDataSet( new FileInputStream( testFile ) ) );

        dbTester.setSetUpOperation( DatabaseOperation.INSERT );

        dbTester.onSetup();
    }

    /**
     * Clean up the Continuum test database.
     * 
     * @throws Exception
     */
    protected void teardownBuildDatabase() throws Exception
    {
        dbTester.setTearDownOperation( DatabaseOperation.DELETE );
        dbTester.onTearDown();
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
        list.add( getTestFile( getBasedir(), SQL_DATABSE_SCHEMA ) );
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

}
