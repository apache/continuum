package org.apache.maven.continuum.store.jdo;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.continuum.key.GroupProjectKey;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ProjectGroupStore;
import org.apache.maven.continuum.store.ProjectStore;
import org.apache.maven.continuum.store.utils.StoreTestUtils;

import java.util.List;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 */
public class JdoProjectStoreTestCase extends AbstractJdoStoreTestCase
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
        ProjectStore store = (ProjectStore) lookup( ProjectStore.ROLE, "jdo" );
        assertNotNull( store );
    }

    public void testGetAllProjects() throws Exception
    {
        ProjectStore store = (ProjectStore) lookup( ProjectStore.ROLE, "jdo" );
        List list = store.getAllProjects();
        assertNotNull( list );
        assertEquals( 4, list.size() );
    }

    public void testLookupProject() throws Exception
    {
        ProjectStore store = (ProjectStore) lookup( ProjectStore.ROLE, "jdo" );
        GroupProjectKey key = new GroupProjectKey( "Default", "project1" );

        Project project = store.lookupProject( key );
        assertNotNull( project );
        ProjectGroup group = project.getProjectGroup();
        assertNotNull( group );

        // verify group properties
        assertEquals( 1L, group.getId() );
        assertEquals( "Default Group", group.getDescription() );
        assertEquals( "default", group.getGroupId() );
        assertEquals( "Default", group.getKey() );
        assertEquals( "Default Group", group.getName() );

        // verify project properties
        assertEquals( 1L, project.getId() );
        assertEquals( "Test Project 1", group.getDescription() );
        assertEquals( "org.test.projects", group.getGroupId() );
        assertEquals( "project1", group.getKey() );
        assertEquals( "Project 1", group.getName() );
    }

    public void testDeleteProject() throws Exception
    {
        ProjectStore store = (ProjectStore) lookup( ProjectStore.ROLE, "jdo" );
        GroupProjectKey key = new GroupProjectKey( "DeleteableGroup", "deleteableProject" );
        Project project = store.lookupProject( key );
        assertNotNull( project );

        store.deleteProject( project );

        try
        {
            project = store.lookupProject( key );
            fail( "Expected ContinuumObjectNotFoundException." );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            // expected
        }

    }

    public void testSaveNewProjectGroup() throws Exception
    {
        // TODO: Implement!
    }

}
