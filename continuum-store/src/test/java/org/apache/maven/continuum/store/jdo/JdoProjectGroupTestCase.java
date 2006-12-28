package org.apache.maven.continuum.store.jdo;

import org.apache.maven.continuum.store.ProjectGroupStore;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 */
public class JdoProjectGroupTestCase extends AbstractJdoStoreTestCase
{

    public void testComponentLookup() throws Exception
    {
        ProjectGroupStore store = (ProjectGroupStore) lookup( ProjectGroupStore.ROLE, "jdo" );
        assertNotNull( store );
    }

}
