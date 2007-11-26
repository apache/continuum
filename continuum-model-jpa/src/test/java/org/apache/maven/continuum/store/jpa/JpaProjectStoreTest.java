/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.StoreTestCase;
import org.apache.openjpa.persistence.OpenJPAQuery;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.2
 * @version $Id$
 * @see <a
 *      href="http://mail-archives.apache.org/mod_mbox/openjpa-users/200706.mbox/%3CBF2B99E3-7EF3-4E99-91E1-8AEB940524C7@apache.org%3E">
 *      http://mail-archives.apache.org/mod_mbox/openjpa-users/200706.mbox/%3CBF2B99E3-7EF3-4E99-91E1-8AEB940524C7@apache.org%3E
 *      </a>
 */
public class JpaProjectStoreTest extends StoreTestCase
{
    private static final String PERSISTENT_UNIT_CONTINUUM_STORE = "continuum-store";

    @Override
    public void setUp()
    {
        File testData = new File( "src/test/resources/sql/project-table-data.sql" );
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

    public void testOpenJPASetup()
    {
        OpenJPAQuery q = em.createQuery( "select p from Project p" );
        String[] sql = q.getDataStoreActions( null );
        assertEquals( 1, sql.length );
        assertTrue( sql[0].startsWith( "SELECT" ) );
        List results = q.getResultList();
        assertNotNull( results );
        assertEquals( 2, results.size() );
    }
    
    public void testCreateProject()
    {
        Project project = new Project();
        project.setArtifactId( "sample-project" );
        project.setGroupId( "org.sample.group" );
        project.setName( "Sample Project" );
        project.setDescription( "A sample project" );
        project.setScmUseCache( false );
        project.setScmUrl( "https://localhost/svn/sample-project" );
        project.setModelEncoding( "UTF-8" );
        
        
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

}
