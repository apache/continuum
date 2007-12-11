/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import static org.apache.maven.continuum.store.matcher.JpaEntity.isDeleted;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.maven.continuum.model.project.ProjectNotifier;
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
 * @version $Id$
 * @since 1.2
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = "/META-INF/spring-config.xml" )
public class JpaProjectNotifierStoreTest extends ApplicationContextAwareStoreTestCase
{
    private static final String BEAN_REF__PROJECT_NOTIFIER_STORE = "projectNotifierStore";

    @Override
    @Before
    public void setUp()
    {
        File testData = new File( "src/test/resources/sql/project-notifier-table-data.sql" );
        Assert.assertTrue( "Unable to find test data resource: " + testData.getAbsolutePath(), testData.exists() );
        Properties propMap = new Properties();
        setUp( propMap );

        // load test data from SQL file.
        setSqlSource( testData );
    }

    @Test
    public void testOpenJPASetup()
    {
        OpenJPAQuery q = em.createQuery( "select n from ProjectNotifier n" );
        String[] sql = q.getDataStoreActions( null );
        Assert.assertEquals( 1, sql.length );
        Assert.assertTrue( sql[0].startsWith( "SELECT" ) );
        List results = q.getResultList();
        Assert.assertNotNull( results );
        Assert.assertEquals( 1, results.size() );
    }

    @Test
    public void testCreateProjectNotifier() throws StoreException
    {
        ProjectNotifier notifier = new ProjectNotifier();
        notifier.setModelEncoding( "UTF-8" );
        Assert.assertTrue( null == notifier.getId() );
        notifier = getProjectNotifierStore().save( notifier );
        Assert.assertTrue( null != notifier.getId() );
        Assert.assertTrue( notifier.getId() > 0L );
    }

    @Test
    public void testLookupProjectNotifier() throws StoreException
    {
        ProjectNotifier notifier = getProjectNotifierStore().lookup( ProjectNotifier.class, 100L );
        Assert.assertNotNull( notifier );
        Assert.assertTrue( notifier.getId() > 0L );
    }

    @Test
    @Transactional
    public void testDeleteProjectNotifier() throws StoreException
    {
        ProjectNotifier notifier = getProjectNotifierStore().lookup( ProjectNotifier.class, 100L );
        Assert.assertNotNull( notifier );
        Assert.assertTrue( notifier.getId() > 0L );
        getProjectNotifierStore().delete( notifier );
        // assertion follows in a separate transaction
        isDeleted( getProjectNotifierStore(), ProjectNotifier.class, notifier );
    }

    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    private Store<ProjectNotifier, ProjectNotifierQuery<ProjectNotifier>> getProjectNotifierStore()
    {
        Store<ProjectNotifier, ProjectNotifierQuery<ProjectNotifier>> store =
            getStore( BEAN_REF__PROJECT_NOTIFIER_STORE );
        return store;
    }
}
