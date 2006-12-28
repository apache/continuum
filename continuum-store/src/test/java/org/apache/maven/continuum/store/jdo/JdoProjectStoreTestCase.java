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

import org.apache.maven.continuum.store.ProjectStore;

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

}
