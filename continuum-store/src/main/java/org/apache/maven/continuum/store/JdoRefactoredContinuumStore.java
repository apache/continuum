package org.apache.maven.continuum.store;

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

import org.apache.maven.continuum.key.GroupProjectKey;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.codehaus.plexus.jdo.PlexusJdoUtils;
import org.codehaus.plexus.jdo.PlexusObjectNotFoundException;
import org.codehaus.plexus.jdo.PlexusStoreException;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import java.util.List;

/**
 * Store implementation that interacts with the underlying JDO-based store.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 */
public class JdoRefactoredContinuumStore implements RefactoredContinuumStore
{

    /**
     * Provides hook to obtainig a {@link PersistenceManager} instance for
     * invoking operations on the underlying store.
     */
    private PersistenceManagerFactory continuumPmf;

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.RefactoredContinuumStore#deleteProject(org.apache.maven.continuum.model.project.Project)
     */
    public void deleteProject( Project project ) throws ContinuumStoreException
    {
        // TODO: Any checks before Project should be deleted?
        removeObject( project );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.RefactoredContinuumStore#deleteProjectGroup(org.apache.maven.continuum.model.project.ProjectGroup)
     */
    public void deleteProjectGroup( ProjectGroup group ) throws ContinuumStoreException
    {
        // TODO: Any checks before Group should be deleted?
        removeObject( group );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.RefactoredContinuumStore#lookupProject(org.apache.maven.continuum.key.GroupProjectKey)
     */
    public Project lookupProject( GroupProjectKey key )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        // TODO: Review! Would be nice if we can some how bind param to values
        // and then execute the query.
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "String groupKey, String projectKey" );

            query.setFilter( "this.project.groupKey = groupKey && this.project.key == projectKey" );

            List result = (List) query.execute( key.getGroupKey(), key.getProjectKey() );

            result = (List) pm.detachCopyAll( result );

            tx.commit();

            if ( result.size() == 0 )
                throw new ContinuumObjectNotFoundException( "Unable to lookup Project with groupKey '"
                                + key.getGroupKey() + "' and projectKey '" + key.getProjectKey() + "'" );

            return (Project) result.get( 0 );
        }
        finally
        {
            rollback( tx );
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.RefactoredContinuumStore#lookupProjectGroup(org.apache.maven.continuum.key.GroupProjectKey)
     */
    public ProjectGroup lookupProjectGroup( GroupProjectKey key )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (ProjectGroup) getObjectFromQuery( ProjectGroup.class, "key", key.getProjectKey(), null );

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.RefactoredContinuumStore#saveProject(org.apache.maven.continuum.model.project.Project)
     */
    public Project saveProject( Project project ) throws ContinuumStoreException
    {
        updateObject( project );
        return project;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.RefactoredContinuumStore#saveProjectGroup(org.apache.maven.continuum.model.project.ProjectGroup)
     */
    public ProjectGroup saveProjectGroup( ProjectGroup group ) throws ContinuumStoreException
    {
        updateObject( group );
        return group;
    }

    // ------------------------------------------------------------------------
    // Service Methods
    // ------------------------------------------------------------------------

    /**
     * Looks up and returns an Entity instance from the underlying store given
     * the String key and the field name to match it against.
     * 
     * @param clazz Expected {@link Class} of the entity being looked up.
     * @param idField Column identifier/name for the field in the underlying
     *            store to match the String identifier against.
     * @param id Identifier value to match in the Id field.
     * @param fetchGroup TODO: Document! What is a fetchGroup?
     * @return Entity instance that matches the lookup criteria as specified by
     *         the passed in Id.
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException if there was no instance that
     *             matched the criteria in the underlying store.
     */
    private Object getObjectFromQuery( Class clazz, String idField, String id, String fetchGroup )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        try
        {
            return PlexusJdoUtils.getObjectFromQuery( getPersistenceManager(), clazz, idField, id, fetchGroup );
        }
        catch ( PlexusObjectNotFoundException e )
        {
            throw new ContinuumObjectNotFoundException( e.getMessage() );
        }
        catch ( PlexusStoreException e )
        {
            throw new ContinuumStoreException( e.getMessage(), e );
        }
    }

    private PersistenceManager getPersistenceManager()
    {
        PersistenceManager pm = continuumPmf.getPersistenceManager();

        pm.getFetchPlan().setMaxFetchDepth( -1 );
        pm.getFetchPlan().setDetachmentOptions( FetchPlan.DETACH_LOAD_FIELDS );

        return pm;
    }

    private void removeObject( Object o )
    {
        PlexusJdoUtils.removeObject( getPersistenceManager(), o );
    }

    private void rollback( Transaction tx )
    {
        PlexusJdoUtils.rollbackIfActive( tx );
    }

    private void updateObject( Object object ) throws ContinuumStoreException
    {
        try
        {
            PlexusJdoUtils.updateObject( getPersistenceManager(), object );
        }
        catch ( PlexusStoreException e )
        {
            throw new ContinuumStoreException( e.getMessage(), e );
        }
    }
}
