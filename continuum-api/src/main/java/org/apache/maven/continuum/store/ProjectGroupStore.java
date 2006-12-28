/**
 * 
 */
package org.apache.maven.continuum.store;

import org.apache.maven.continuum.key.GroupProjectKey;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;

/**
 * Defines the contract consisting of operations that can be performed on
 * {@link ProjectGroup} entity.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 */
public interface ProjectGroupStore
{
    
    public static final String ROLE = ProjectGroupStore.class.getName();

    /**
     * Removes the passed {@link ProjectGroup} instance from the underlying
     * store.
     * 
     * @param project {@link ProjectGroup} instance to remove.
     * @throws ContinuumStoreException if there was an error removing the
     *             entity.
     */
    public void deleteProjectGroup( ProjectGroup project ) throws ContinuumStoreException;

    /**
     * Looks up the underlying store and returns a {@link Project} instance that
     * matches the key specified by the passed in {@link GroupProjectKey}.
     * <p>
     * The key is the {@link ProjectGroup}'s key that is obtained from
     * {@link GroupProjectKey#getGroupKey()}.
     * 
     * @param key Composite key that identifies the target project group.
     * @return {@link ProjectGroup} instance that matches the specified key.
     * @throws ContinuumObjectNotFoundException if the instance could not be
     *             looked up.
     * @throws ContinuumStoreException
     */
    public ProjectGroup lookupProjectGroup( GroupProjectKey key )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    /**
     * Persists the passed in {@link ProjectGroup} instance to the underlying
     * store.
     * <p>
     * If the entity instance already exists in the database it is updated, else
     * a new instance is created and an store-generated identifier assigned to
     * it.
     * 
     * @param project {@link ProjectGroup} instance to be created/saved.
     * @return updated {@link ProjectGroup} instance.
     * @throws ContinuumStoreException if there was an error saving the entity.
     */
    public ProjectGroup saveProjectGroup( ProjectGroup projectGroup ) throws ContinuumStoreException;
    

}
