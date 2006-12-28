/**
 * 
 */
package org.apache.maven.continuum.store;

import org.apache.maven.continuum.key.GroupProjectKey;
import org.apache.maven.continuum.model.project.ProjectGroup;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * 
 */
public class JdoProjectGroupStore extends AbstractJdoStore implements ProjectGroupStore
{

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectGroupStore#deleteProjectGroup(org.apache.maven.continuum.model.project.ProjectGroup)
     */
    public void deleteProjectGroup( ProjectGroup group ) throws ContinuumStoreException
    {
        // TODO: Any checks before Group should be deleted?
        removeObject( group );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectGroupStore#lookupProjectGroup(org.apache.maven.continuum.key.GroupProjectKey)
     */
    public ProjectGroup lookupProjectGroup( GroupProjectKey key )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (ProjectGroup) getObjectFromQuery( ProjectGroup.class, "key", key.getProjectKey(), null );

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectGroupStore#saveProjectGroup(org.apache.maven.continuum.model.project.ProjectGroup)
     */
    public ProjectGroup saveProjectGroup( ProjectGroup group ) throws ContinuumStoreException
    {
        updateObject( group );
        return group;
    }

}
