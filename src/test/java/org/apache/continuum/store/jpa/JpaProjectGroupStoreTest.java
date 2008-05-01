/**
 *
 */
package org.apache.continuum.store.jpa;

import org.apache.continuum.model.project.ProjectGroup;
import org.apache.continuum.service.api.ProjectGroupService;
import org.apache.continuum.store.ApplicationContextAwareStoreTestCase;
import org.apache.continuum.store.api.StoreException;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @see <a
 *      href="http://mail-archives.apache.org/mod_mbox/openjpa-users/200706.mbox/%3CBF2B99E3-7EF3-4E99-91E1-8AEB940524C7@apache.org%3E">
 *      http://mail-archives.apache.org/mod_mbox/openjpa-users/200706.mbox/%3CBF2B99E3-7EF3-4E99-91E1-8AEB940524C7@apache.org%3E
 *      </a>
 * @since 1.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/META-INF/spring-config.xml")
public class JpaProjectGroupStoreTest
    extends ApplicationContextAwareStoreTestCase
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

    @Override
    @After
    public void tearDown()
        throws Exception
    {
        super.tearDown();
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
    public void testCreateProjectGroup()
        throws StoreException
    {
        ProjectGroup group = new ProjectGroup();
        group.setGroupId( "org.sample.group" );
        group.setName( "Sample Project Group" );
        group.setDescription( "A sample project group" );

        Assert.assertTrue( null == group.getId() );
        group = getService().saveOrUpdate( group );
        Assert.assertTrue( null != group.getId() );
        Assert.assertTrue( group.getId() > 0L );
        Assert.assertEquals( 0, group.getProjects().size() );
    }

    private ProjectGroupService getService()
    {
        return (ProjectGroupService) getObject( "projectGroupService" );
    }
}
