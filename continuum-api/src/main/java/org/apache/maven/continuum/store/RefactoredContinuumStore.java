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
import org.apache.maven.continuum.model.project.Profile;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.SystemConfiguration;

/**
 * Defines the contract consisting of operations that can be performed on
 * system's entities.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 */
public interface RefactoredContinuumStore
{
    String ROLE = RefactoredContinuumStore.class.getName();

    /**
     * Looks up the underlying store and returns a {@link Project} instance that
     * matches the key specified by the passed in {@link GroupProjectKey}.
     * 
     * @param key Composite key that identifies the target project under a
     *            group.
     * @return {@link Project} instance that matches the specified key.
     * @throws ContinuumObjectNotFoundException if the instance could not be
     *             looked up.
     * @throws ContinuumStoreException
     */
    public Project lookupProject( GroupProjectKey key )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

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
     * Looks up the underlying store and returns a {@link Schedule} instance
     * that matches the specified id.
     * 
     * @param id {@link Schedule} id to match.
     * @return matching {@link Schedule} instance.
     * @throws ContinuumObjectNotFoundException if the instance could not be
     *             looked up.
     * @throws ContinuumStoreException
     */
    public Schedule lookupSchedule( int id ) throws ContinuumObjectNotFoundException, ContinuumStoreException;

    /**
     * Looks up the underlying store and returns a {@link Profile} instance that
     * matches the specified id.
     * 
     * @param id {@link Profile} id to match.
     * @return matching {@link Profile} instance.
     * @throws ContinuumObjectNotFoundException if the instance could not be
     *             looked up.
     * @throws ContinuumStoreException
     */
    public Profile lookupProfile( int id ) throws ContinuumObjectNotFoundException, ContinuumStoreException;

    /**
     * Looks up the underlying store and returns a {@link Installation} instance
     * that matches the specified id.
     * 
     * @param id {@link Installation} id to match.
     * @return matching {@link Installation} instance.
     * @throws ContinuumObjectNotFoundException if the instance could not be
     *             looked up.
     * @throws ContinuumStoreException
     */
    public Installation lookupInstallation( int id ) throws ContinuumObjectNotFoundException, ContinuumStoreException;

    /**
     * Looks up the underlying store and returns a {@link SystemConfiguration}
     * instance that matches the specified id.
     * 
     * @param id {@link SystemConfiguration} id to match.
     * @return matching {@link SystemConfiguration} instance.
     * @throws ContinuumObjectNotFoundException if the instance could not be
     *             looked up.
     * @throws ContinuumStoreException
     */
    public SystemConfiguration lookupSystemConfiguration( int id )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    /**
     * Persists the passed in {@link Project} instance to the underlying store.
     * <p>
     * If the entity instance already exists in the database it is updated, else
     * a new instance is created and an store-generated identifier assigned to
     * it.
     * 
     * @param project {@link Project} instance to be created/saved.
     * @return updated {@link Project} instance.
     * @throws ContinuumStoreException if there was an error saving the entity.
     */
    public Project saveProject( Project project ) throws ContinuumStoreException;

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
     * Persists the passed in {@link Schedule} instance to the underlying store.
     * <p>
     * If the entity instance already exists in the database it is updated, else
     * a new instance is created and an store-generated identifier assigned to
     * it.
     * 
     * @param project {@link Schedule} instance to be created/saved.
     * @return updated {@link Schedule} instance.
     * @throws ContinuumStoreException if there was an error saving the entity.
     */
    public Schedule saveSchedule( Schedule schedule ) throws ContinuumStoreException;

    /**
     * Persists the passed in {@link Profile} instance to the underlying store.
     * <p>
     * If the entity instance already exists in the database it is updated, else
     * a new instance is created and an store-generated identifier assigned to
     * it.
     * 
     * @param project {@link Profile} instance to be created/saved.
     * @return updated {@link Profile} instance.
     * @throws ContinuumStoreException if there was an error saving the entity.
     */
    public Profile saveProfile( Profile profile ) throws ContinuumStoreException;

    /**
     * Persists the passed in {@link Installation} instance to the underlying
     * store.
     * <p>
     * If the entity instance already exists in the database it is updated, else
     * a new instance is created and an store-generated identifier assigned to
     * it.
     * 
     * @param project {@link Installation} instance to be created/saved.
     * @return updated {@link Installation} instance.
     * @throws ContinuumStoreException if there was an error saving the entity.
     */
    public Installation saveInstallation( Installation installation ) throws ContinuumStoreException;

    /**
     * Persists the passed in {@link SystemConfiguration} instance to the
     * underlying store.
     * <p>
     * If the entity instance already exists in the database it is updated, else
     * a new instance is created and an store-generated identifier assigned to
     * it.
     * 
     * @param project {@link SystemConfiguration} instance to be created/saved.
     * @return updated {@link SystemConfiguration} instance.
     * @throws ContinuumStoreException if there was an error saving the entity.
     */
    public SystemConfiguration saveSystemConfiguration( SystemConfiguration systemConfiguration )
        throws ContinuumStoreException;

    /**
     * Removes the passed {@link Project} instance from the underlying store.
     * 
     * @param project {@link Project} instance to remove.
     * @throws ContinuumStoreException if there was an error removing the
     *             entity.
     */
    public void deleteProject( Project project ) throws ContinuumStoreException;

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
     * Removes the passed {@link Schedule} instance from the underlying store.
     * 
     * @param project {@link Schedule} instance to remove.
     * @throws ContinuumStoreException if there was an error removing the
     *             entity.
     */
    public void deleteSchedule( Schedule schedule ) throws ContinuumStoreException;

    /**
     * Removes the passed {@link Profile} instance from the underlying store.
     * 
     * @param project {@link Profile} instance to remove.
     * @throws ContinuumStoreException if there was an error removing the
     *             entity.
     */
    public void deleteProfile( Profile profile ) throws ContinuumStoreException;

    /**
     * Removes the passed {@link Installation} instance from the underlying
     * store.
     * 
     * @param project {@link Installation} instance to remove.
     * @throws ContinuumStoreException if there was an error removing the
     *             entity.
     */
    public void deleteInstallation( Installation installation ) throws ContinuumStoreException;

    /**
     * Removes the passed {@link SystemConfiguration} instance from the
     * underlying store.
     * 
     * @param project {@link SystemConfiguration} instance to remove.
     * @throws ContinuumStoreException if there was an error removing the
     *             entity.
     */
    public void deleteSystemConfiguration( SystemConfiguration systemConfiguration ) throws ContinuumStoreException;

}
