package org.apache.maven.continuum.store.jdo;

import org.apache.maven.continuum.key.GroupProjectKey;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ProjectGroupStore;
import org.apache.maven.continuum.store.utils.StoreTestUtils;

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

    public void testGetAllProjectGroups() throws Exception
    {
        ProjectGroupStore store = (ProjectGroupStore) lookup( ProjectGroupStore.ROLE, "jdo" );
        List list = store.getAllProjectGroups();
        assertNotNull( list );
        assertEquals( 3, list.size() );
    }

    public void testLookupProjectGroup() throws Exception
    {
        ProjectGroupStore store = (ProjectGroupStore) lookup( ProjectGroupStore.ROLE, "jdo" );
        GroupProjectKey key = new GroupProjectKey( "Default", null );
        ProjectGroup group = store.lookupProjectGroup( key );
        assertNotNull( group );
        assertEquals( 100L, group.getId() );
        assertEquals( "Default Group Desc.", group.getDescription() );
        assertEquals( "default", group.getGroupId() );
        assertEquals( "Default", group.getKey() );
        assertEquals( "Default Group", group.getName() );
    }

    public void testDeleteProjectGroup() throws Exception
    {
        ProjectGroupStore store = (ProjectGroupStore) lookup( ProjectGroupStore.ROLE, "jdo" );
        GroupProjectKey key = new GroupProjectKey( "DeleteableGroup", null );
        ProjectGroup group = store.lookupProjectGroup( key );
        assertNotNull( group );

        store.deleteProjectGroup( group );

        try
        {
            group = store.lookupProjectGroup( key );
            fail( "Expected ContinuumObjectNotFoundException." );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            // expected
        }

    }

    public void testSaveNewProjectGroup() throws Exception
    {
        ProjectGroupStore store = (ProjectGroupStore) lookup( ProjectGroupStore.ROLE, "jdo" );
        String name = "testAddProjectGroup";
        String description = "testAddProjectGroup description";
        String groupId = "org.apache.maven.continuum.test";
        String groupKey = "AddProjectGroupKey";
        ProjectGroup group = StoreTestUtils.createTestProjectGroup( name, description, groupId, groupKey );

        group = store.saveProjectGroup( group );
        assertNotNull( group );
        assertTrue( group.getId() > 0 );
    }

}
