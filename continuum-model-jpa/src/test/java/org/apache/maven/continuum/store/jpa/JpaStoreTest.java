/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.store.ApplicationContextAwareStoreTestCase;
import org.apache.maven.continuum.store.api.ProjectNotifierQuery;
import org.apache.maven.continuum.store.api.Store;
import org.apache.maven.continuum.store.api.StoreException;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = "/META-INF/spring-config.xml" )
public class JpaStoreTest extends ApplicationContextAwareStoreTestCase
{
    private static final String BEAN_REF__PROJECT_STORE = "projectStore";

    @Override
    @Before
    public void setUp()
    {
        File testData = new File( "src/test/resources/sql/project-table-data.sql" );
        Assert.assertTrue( "Unable to find test data resource: " + testData.getAbsolutePath(), testData.exists() );
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

    // @Test
    public void testCreateProjectNotifier() throws StoreException
    {
        ProjectNotifier notifier = new ProjectNotifier();
        notifier.setModelEncoding( "UTF-8" );
        Assert.assertTrue( null == notifier.getId() );
        notifier = getProjectNotifierStore().save( notifier );
        Assert.assertTrue( null != notifier.getId() );
        Assert.assertTrue( notifier.getId() > 0L );
    }

    private Store<ProjectNotifier, ProjectNotifierQuery<ProjectNotifier>> getProjectNotifierStore()
    {
        Store<ProjectNotifier, ProjectNotifierQuery<ProjectNotifier>> store = getStore( BEAN_REF__PROJECT_STORE );
        return store;
    }
}
