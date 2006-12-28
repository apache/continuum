package org.apache.maven.continuum.store;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

import java.util.List;

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

    /**
     * Obtains and returns a {@link List} of <b>all</b> {@link ProjectGroup}
     * instances for the system, stored in the underlying store.
     * 
     * @return list of all {@link ProjectGroup} instances stored.
     * @throws ContinuumStoreException
     */
    public List getAllProjectGroups() throws ContinuumStoreException;

}
