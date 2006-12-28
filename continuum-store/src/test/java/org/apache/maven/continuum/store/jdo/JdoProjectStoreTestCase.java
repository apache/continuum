/**
 * 
 */
package org.apache.maven.continuum.store.jdo;


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

    public void testBasic()
    {
        // TODO: do something
    }

}
