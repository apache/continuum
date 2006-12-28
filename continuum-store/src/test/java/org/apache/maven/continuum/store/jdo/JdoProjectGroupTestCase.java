package org.apache.maven.continuum.store.jdo;

import org.apache.maven.continuum.store.ProjectGroupStore;

import java.util.List;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 */
public class JdoProjectGroupTestCase extends AbstractJdoStoreTestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();
        createBuildDatabase();
    }

    /**
     * @see junit.framework.TestCase#getName()
     */
    public String getName()
    {
        return getClass().getName();
    }

    public void testComponentLookup() throws Exception
    {
        ProjectGroupStore store = (ProjectGroupStore) lookup( ProjectGroupStore.ROLE, "jdo" );
        assertNotNull( store );
    }

    public void testVerifyDatabase() throws Exception
    {
        ProjectGroupStore store = (ProjectGroupStore) lookup( ProjectGroupStore.ROLE, "jdo" );
        List list = store.getAllProjectGroups();
        assertNotNull( list );
        assertEquals( 2, list.size() );
    }

}
