/**
 * 
 */
package org.apache.maven.continuum.store.jdo;

import org.apache.maven.continuum.store.SystemStore;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 */
public class JdoSystemStoreTestCase extends AbstractJdoStoreTestCase
{
    public void testComponentLookup() throws Exception
    {
        SystemStore store = (SystemStore) lookup( SystemStore.ROLE, "jdo" );
        assertNotNull( store );
    }
}
