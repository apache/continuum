/**
 * 
 */
package org.apache.maven.continuum.store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.persistence.test.SingleEMTestCase;

/**
 * Test Case support class that allows extensions to load test data from specified SQL files.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.2
 * @version $Id$
 */
public abstract class StoreTestCase extends SingleEMTestCase
{

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
