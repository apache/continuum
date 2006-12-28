/**
 * 
 */
package org.apache.maven.continuum.store.jdo;

import org.apache.maven.continuum.store.ProjectStore;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.jdo.DefaultConfigurableJdoFactory;
import org.codehaus.plexus.jdo.JdoFactory;

import javax.jdo.PersistenceManager;

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
 * 
 */
public abstract class AbstractJdoStoreTestCase extends PlexusTestCase
{
    protected ProjectStore store;

    /**
     * DDL for Database creation.
     */
    protected File sqlSchema = getTestFile( getBasedir(), "src/test/resources/schema.sql" );

    /**
     * Test Data.
     */
    protected File sqlTestData = getTestFile( getBasedir(), "src/test/resources/testData.sql" );

    /**
     * Setup JDO Factory
     * 
     * @todo push down to a Jdo specific test
     */
    protected ProjectStore createStore() throws Exception
    {
        DefaultConfigurableJdoFactory jdoFactory = (DefaultConfigurableJdoFactory) lookup( JdoFactory.ROLE );

        jdoFactory.setUrl( "jdbc:hsqldb:mem:continuum" );

        jdoFactory.setDriverName( "org.hsqldb.jdbcDriver" );

        jdoFactory.setUserName( "sa" );

        jdoFactory.setPassword( "" );

        return (ProjectStore) lookup( ProjectStore.ROLE );
    }

    protected void createBuildDatabase() throws Exception
    {
        DefaultConfigurableJdoFactory jdoFactory = (DefaultConfigurableJdoFactory) lookup( JdoFactory.ROLE );

        jdoFactory.setUrl( "jdbc:hsqldb:mem:continuum" );

        jdoFactory.setDriverName( "org.hsqldb.jdbcDriver" );

        jdoFactory.setUserName( "sa" );

        jdoFactory.setPassword( "" );

        PersistenceManager pm = jdoFactory.getPersistenceManagerFactory().getPersistenceManager();

        Connection connection = (Connection) pm.getDataStoreConnection().getNativeConnection();

        createStoreFromSQL( sqlSchema, connection );

        createStoreFromSQL( sqlTestData, connection );
    }

    public void createStoreFromSQL( File sqlData, Connection connection ) throws Exception
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
            if ( !line.trim().equals( "" ) && !line.startsWith( "#" ) && !line.startsWith( "--" ) )
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

            System.out.println( sql );

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
