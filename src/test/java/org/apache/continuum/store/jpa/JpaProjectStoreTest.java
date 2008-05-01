package org.apache.continuum.store.jpa;

import org.apache.continuum.dao.api.GenericDao;
import org.apache.continuum.model.project.Project;
import org.apache.continuum.service.api.ProjectService;
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

import javax.persistence.Query;
import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @author <a href='mailto:evenisse@apache.org'>Emmanuel Venisse</a>
 * @version $Id$
 * @see <a
 *      href="http://mail-archives.apache.org/mod_mbox/openjpa-users/200706.mbox/%3CBF2B99E3-7EF3-4E99-91E1-8AEB940524C7@apache.org%3E">
 *      http://mail-archives.apache.org/mod_mbox/openjpa-users/200706.mbox/%3CBF2B99E3-7EF3-4E99-91E1-8AEB940524C7@apache.org%3E
 *      </a>
 * @since 1.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/META-INF/spring-config.xml")
public class JpaProjectStoreTest
    extends ApplicationContextAwareStoreTestCase
{
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
        OpenJPAQuery q = em.createQuery( "select p from Project p" );
        String[] sql = q.getDataStoreActions( null );
        Assert.assertEquals( 1, sql.length );
        Assert.assertTrue( sql[0].startsWith( "SELECT" ) );
        List results = q.getResultList();
        Assert.assertNotNull( results );
        Assert.assertEquals( 2, results.size() );
    }

    @Test
    public void testNamedQuery()
    {
        Query q = em.createNamedQuery( "Project.findAll" );
        List<Project> projects = q.getResultList();
        Assert.assertEquals( 2, projects.size() );
        for ( Project p : projects )
        {
            System.out.println( p.getClass().getName() );
            System.out.println( p );
        }
    }

    @Test
    public void testDao()
    {
        GenericDao<Project> dao = getDao( "projectDao" );
        List<Project> projects = dao.findAll();
        Assert.assertTrue( 2==projects.size() );
    }

    @Test
    public void testCreateProject()
        throws StoreException
    {
        Project project = new Project();
        project.setArtifactId( "sample-project" );
        project.setGroupId( "org.sample.group" );
        project.setName( "Sample Project" );
        project.setDescription( "A sample project" );
        project.setScmUseCache( false );
        project.setScmUrl( "https://localhost/svn/sample-project" );

        Assert.assertTrue( null == project.getId() );

        //store project
        project = getService().saveOrUpdate( project );
        Assert.assertTrue( "Identifier of the persisted new Entity should not be null.", null != project.getId() );
        Assert.assertTrue( "Identifier of the persisted new Entity should be a valid positive value.",
                           project.getId() > 0L );
        long id = project.getId();

        //search the project
        Project p = getService().getProject( project.getGroupId(), project.getArtifactId(), project.getVersion() );
        Assert.assertNotNull( p );
        Assert.assertTrue( id == p.getId() );
    }

    private ProjectService getService()
    {
        return (ProjectService) getObject( "projectService" );
    }
}
