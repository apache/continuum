/**
 * 
 */
package org.apache.maven.continuum.store;

import org.apache.maven.continuum.model.project.ProjectGroup;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.jdo.DefaultConfigurableJdoFactory;
import org.codehaus.plexus.jdo.JdoFactory;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * 
 */
public class AbstractRefactoredContinuumStoreTestCase extends PlexusTestCase
{
    protected RefactoredContinuumStore store;

    /**
     * Setup JDO Factory
     * 
     * @todo push down to a Jdo specific test
     */
    protected RefactoredContinuumStore createStore() throws Exception
    {
        DefaultConfigurableJdoFactory jdoFactory = (DefaultConfigurableJdoFactory) lookup( JdoFactory.ROLE );

        jdoFactory.setUrl( "jdbc:hsqldb:mem:" + getName() );

        return (RefactoredContinuumStore) lookup( RefactoredContinuumStore.ROLE );
    }

    protected void createBuildDatabase()
    {
     
    }
}
