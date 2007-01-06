package org.apache.maven.continuum.store.ibatis;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.maven.continuum.store.ProjectGroupStore;

import java.util.List;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id: IbatisProjectGroupTestCase.java 491371 2006-12-31 03:00:05Z
 *          rinku $
 */
public class IbatisProjectGroupTestCase extends AbstractIbatisStoreTestCase
{

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.plexus.PlexusTestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        createBuildDatabase();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.plexus.PlexusTestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        teardownBuildDatabase();
    }

    public void testLookup() throws Exception
    {
        IbatisProjectGroupStore store = (IbatisProjectGroupStore) lookup( ProjectGroupStore.ROLE, "ibatis" );
        assertNotNull( store );
    }

    public void testLookupProjectGroup() throws Exception
    {
        IbatisProjectGroupStore store = (IbatisProjectGroupStore) lookup( ProjectGroupStore.ROLE, "ibatis" );
        List list = store.getAllProjectGroups();
        assertNotNull( list );

    }

}
