/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import static org.apache.maven.continuum.store.matcher.JpaEntity.isDeleted;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.ApplicationContextAwareStoreTestCase;
import org.apache.maven.continuum.store.api.Store;
import org.apache.maven.continuum.store.api.StoreException;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.2
 * @version $Id$
 * @see <a
 *      href="http://mail-archives.apache.org/mod_mbox/openjpa-users/200706.mbox/%3CBF2B99E3-7EF3-4E99-91E1-8AEB940524C7@apache.org%3E">
 *      http://mail-archives.apache.org/mod_mbox/openjpa-users/200706.mbox/%3CBF2B99E3-7EF3-4E99-91E1-8AEB940524C7@apache.org%3E
 *      </a>
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = "/META-INF/spring-config.xml" )
public class JpaProjectStoreTest extends ApplicationContextAwareStoreTestCase
{
    private static final String BEAN_REF__PROJECT_STORE = "projectStore";

    @Override
    @Before
    public void setUp()
    {
        File testData = new File( "src/test/resources/sql/project-table-data.sql" );
        Properties propMap = new Properties();
        setUp( propMap );
        // load test data from SQL file.
        setSqlSource( testData );
    }

    @Test
    public void testOpenJPASetup()
    {
        OpenJPAQuery q = em.createQuery( "select p from Project p" );
        String[] sql = q.getDataStoreActions( null );
        Assert.assertEquals( 1, sql.length );
        Assert.assertTrue( sql[0].startsWith( "SELECT" ) );
        List results = q.getResultList();
        Assert.assertNotNull( results );
        Assert.assertEquals( 2, results.size() );
    }

    @Test
    public void testCreateProject() throws StoreException
    {
        Project project = new Project();
        project.setArtifactId( "sample-project" );
        project.setGroupId( "org.sample.group" );
        project.setName( "Sample Project" );
        project.setDescription( "A sample project" );
        project.setScmUseCache( false );
        project.setScmUrl( "https://localhost/svn/sample-project" );
        project.setModelEncoding( "UTF-8" );

        Assert.assertTrue( null == project.getId() );
        project = getProjectStore().save( project );
        Assert.assertTrue( "Identifier of the persisted new Entity should not be null.", null != project.getId() );
        Assert.assertTrue( "Identifier of the persisted new Entity should be a valid positive value.",
                           project.getId() > 0L );
    }

    @Test
    public void testLookupProject() throws StoreException
    {
        Project project = getProjectStore().lookup( Project.class, 100L );
        Assert.assertNotNull( project );
        Assert.assertTrue( project.getId() > 0L );
    }

    @Test
    @Transactional
    public void testDeleteProject() throws StoreException
    {
        Project project = getProjectStore().lookup( Project.class, 100L );
        Assert.assertNotNull( project );
        Assert.assertTrue( project.getId() > 0L );
        getProjectStore().delete( project );
        // assertion follows in a separate transaction
        isDeleted( getProjectStore(), Project.class, project );
    }

    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Convenience method to get {@link Store} implementation tied to a {@link Project}.
     * 
     * @return
     */
    private Store<Project, ProjectQuery<Project>> getProjectStore()
    {
        Store<Project, ProjectQuery<Project>> store = getStore( BEAN_REF__PROJECT_STORE );
        return store;
    }

}
