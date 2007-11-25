/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.2
 * @version $Id$
 * @see <a
 *      href="http://mail-archives.apache.org/mod_mbox/openjpa-users/200706.mbox/%3CBF2B99E3-7EF3-4E99-91E1-8AEB940524C7@apache.org%3E">
 *      http://mail-archives.apache.org/mod_mbox/openjpa-users/200706.mbox/%3CBF2B99E3-7EF3-4E99-91E1-8AEB940524C7@apache.org%3E
 *      </a>
 */
public class JpaProjectStoreTest extends SingleEMTestCase
{
    private static final String PERSISTENT_UNIT_CONTINUUM_STORE = "continuum-store";

    @Override
    public void setUp()
    {
        File testData = new File( "src/test/resources/sql/table-project-data.sql" );
        assertTrue( "Unable to find test data resource: " + testData.getAbsolutePath(), testData.exists() );
        Properties propMap = new Properties();
        setUp( propMap );

        // load test data from SQL file.
        setSqlSource( testData );
    }

    /**
     * Returns the name of the persistent-unit setup in <code>persistence.xml</code>.
     */
    @Override
    protected String getPersistenceUnitName()
    {
        return PERSISTENT_UNIT_CONTINUUM_STORE;
    }

    public void testContinuumJPAStoreActions()
    {
        OpenJPAQuery q = em.createQuery( "select p from Project p" );
        String[] sql = q.getDataStoreActions( null );
        assertEquals( 1, sql.length );
        assertTrue( sql[0].startsWith( "SELECT" ) );
        List results = q.getResultList();
        assertNotNull( results );
        assertEquals( 1, results.size() );
    }

    /**
     * TODO: Investigate {@link org.apache.openjpa.persistence.PersistenceException} attempting to clear tables from
     * schema.
     */
    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
        // do nothing
    }

    /**
     * Imports sql from the specified file.
     * 
     * @param sqlResource
     *            Resource containing sql
     */
    public void setSqlSource( File sqlResource )
    {
        try
        {
            // TODO: Use Logger!
            // System.out.println( "Loading sql: " + sqlResource.getAbsolutePath() );
            List<String> statements = new ArrayList<String>( 20 );
            BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream( sqlResource ) ) );
            String line = null;
            StringBuffer currentStatement = new StringBuffer();
            while ( ( line = br.readLine() ) != null )
            {
                if ( line.trim().length() == 0 )
                    continue;
                if ( line.trim().startsWith( "#" ) )
                    continue;

                currentStatement.append( line );
                if ( line.endsWith( ";" ) )
                {
                    statements.add( currentStatement.toString() );
                    currentStatement = new StringBuffer();
                }
            }
            // append a line if missing a ';'
            if ( currentStatement.length() > 0 )
            {
                statements.add( currentStatement.toString() );
            }
            runSQLStatements( statements );
        }
        catch ( Throwable e )
        {
            // TODO: User logger!
            System.err.println( "Problem executing SQL!" );
            e.printStackTrace();
        }
    }

    /**
     * Run a bunch of SQL statements.
     * 
     * @param statements
     *            Statements to run.
     * @throws SQLException
     */

    public void runSQLStatements( final List<String> statements ) throws SQLException
    {
        for ( String qry : statements )
        {
            Connection con = (Connection) this.em.getConnection();
            try
            {
                Statement stmt = con.createStatement();
                System.out.println( qry );
                stmt.execute( qry );
                con.commit();
            }
            catch ( SQLException e )
            {
                try
                {
                    con.rollback();
                }
                catch ( SQLException e1 )
                {
                    // TODO: Use logger!
                    System.err.println( "Unable to rollback transaction." );
                    throw e1;
                }
                throw e;
            }
        }
    }

}
