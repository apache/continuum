/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import static org.apache.maven.continuum.store.matcher.JpaEntity.isDeleted;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ApplicationContextAwareStoreTestCase;
import org.apache.maven.continuum.store.api.EntityNotFoundException;
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
public class JpaProjectGroupStoreTest extends ApplicationContextAwareStoreTestCase
{
    private static final String BEAN_REF__PROJECT_GROUP_STORE = "projectGroupStore";

    @Override
    @Before
    public void setUp()
    {
        File testData = new File( "src/test/resources/sql/project-group-table-data.sql" );
        Assert.assertTrue( "Unable to find test data resource: " + testData.getAbsolutePath(), testData.exists() );
        Properties propMap = new Properties();
        setUp( propMap );

        // load test data from SQL file.
        setSqlSource( testData );
    }

    @Test
    public void testOpenJPASetup()
    {
        OpenJPAQuery q = em.createQuery( "select pg from ProjectGroup pg" );
        String[] sql = q.getDataStoreActions( null );
        Assert.assertEquals( 1, sql.length );
        Assert.assertTrue( sql[0].startsWith( "SELECT" ) );
        List results = q.getResultList();
        Assert.assertNotNull( results );
        Assert.assertEquals( 1, results.size() );
    }

    @Test
    public void testCreateProjectGroup() throws StoreException
    {
        ProjectGroup group = new ProjectGroup();
        group.setGroupId( "org.sample.group" );
        group.setName( "Sample Project Group" );
        group.setDescription( "A sample project group" );
        group.setModelEncoding( "UTF-8" );

        Assert.assertTrue( null == group.getId() );
        group = getProjectGroupStore().save( group );
        Assert.assertTrue( null != group.getId() );
        Assert.assertTrue( group.getId() > 0L );
        Assert.assertEquals( 0, group.getProjects().size() );
    }

    @Test
    public void testLookupProjectGroup() throws EntityNotFoundException, StoreException
    {
        ProjectGroup projectGroup = getProjectGroupStore().lookup( ProjectGroup.class, 100L );
        Assert.assertNotNull( projectGroup );
        Assert.assertEquals( 100L, projectGroup.getId().longValue() );
        Assert.assertEquals( "Continuum Projects", projectGroup.getName() );
        Assert.assertEquals( "org.apache.continuum", projectGroup.getGroupId() );
    }

    @Test( expected = EntityNotFoundException.class )
    public void testLookupInvalidProjectGroup() throws StoreException
    {
        ProjectGroup projectGroup = getProjectGroupStore().lookup( ProjectGroup.class, 99999L );
        Assert.fail( "Expected " + EntityNotFoundException.class.getSimpleName()
                        + " on account of an invalid ProjectGroup Id." );
    }

    @Test
    @Transactional
    public void testDeleteProjectGroup() throws StoreException
    {
        ProjectGroup group = getProjectGroupStore().lookup( ProjectGroup.class, 100L );
        Assert.assertNotNull( group );
        Assert.assertTrue( group.getId() > 0L );
        getProjectGroupStore().delete( group );
        // assertion follows in a separate transaction
        isDeleted( getProjectGroupStore(), ProjectGroup.class, group );
    }

    @Test
    public void testQueryProjectGroup() throws Exception
    {
        ProjectGroupQuery qry = new ProjectGroupQuery();
        qry.setName( "Continuum Projects" );
        List<ProjectGroup> results = getProjectGroupStore().query( qry );
        Assert.assertNotNull( results );
        Assert.assertEquals( "Expected 1 matching Project Group.", 1, results.size() );

        // with an invalid ProjectGroup Id
        qry = new ProjectGroupQuery();
        qry.setName( "invalid Project" );
        results = getProjectGroupStore().query( qry );
        Assert.assertNotNull( results );
        Assert.assertEquals( "Expected 0 matching Project Group.", 0, results.size() );
    }

    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Convenience method to get {@link Store} implementation tied to a {@link ProjectGroup}.
     * 
     * @return
     */
    private Store<ProjectGroup, ProjectGroupQuery<ProjectGroup>> getProjectGroupStore()
    {
        Store<ProjectGroup, ProjectGroupQuery<ProjectGroup>> store = getStore( BEAN_REF__PROJECT_GROUP_STORE );
        return store;
    }

}
