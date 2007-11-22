/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.util.List;
import java.util.Properties;

import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.2
 * @version $Id$
 * @see {@linkplain http://mail-archives.apache.org/mod_mbox/openjpa-users/200706.mbox/%3CBF2B99E3-7EF3-4E99-91E1-8AEB940524C7@apache.org%3E}
 */
public class JpaProjectStoreTest extends SingleEMTestCase
{
    private static final String PERSITENT_UNIT_CONTINUUM_STORE = "continuum-store";

    @Override
    public void setUp()
    {
        Properties propMap = new Properties();
        setUp( propMap );
    }

    /**
     * Returns the name of the persistent-unit setup in <code>persistence.xml</code>.
     */
    @Override
    protected String getPersistenceUnitName()
    {
        return PERSITENT_UNIT_CONTINUUM_STORE;
    }

    public void testContinuumJPAStoreActions()
    {
        OpenJPAQuery q = em.createQuery( "select p from Project p" );
        String[] sql = q.getDataStoreActions( null );
        assertEquals( 1, sql.length );
        assertTrue( sql[0].startsWith( "SELECT" ) );
        // TODO: Uncomment following!
        //List results = q.getResultList();
        //assertNotNull( results );
        //assertEquals( 0, results.size() );
    }

    /**
     * TODO: Investigate {@link org.apache.openjpa.persistence.PersistenceException} attempting to clear tables from
     * schema.
     */
    @Override
    public void tearDown() throws Exception
    {
        // super.tearDown();
        // do nothing
    }

}
