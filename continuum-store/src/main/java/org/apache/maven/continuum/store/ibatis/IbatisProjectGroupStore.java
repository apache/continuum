package org.apache.maven.continuum.store.ibatis;

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
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.store.ProjectGroupStore;

import java.sql.SQLException;
import java.util.List;

/**
 * Concrete implementation of {@link ProjectGroupStore} that uses Ibatis
 * framework to access the underlying datastore.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 * @plexus.component role="org.apache.maven.continuum.store.ProjectGroupStore"
 *                   role-hint="ibatis"
 */
public class IbatisProjectGroupStore extends AbstractIbatisStore implements ProjectGroupStore
{

    // ------------------------------------------------------------------------
    // Available SQL Maps
    // ------------------------------------------------------------------------

    /**
     * Obtains all {@link ProjectGroup} instances in the system.
     */
    private static final String SQLMAP_GET_ALL_PROJECT_GROUPS = "GetAllProjectGroups";

    /**
     * Saves the specified {@link ProjectGroup} instance to the underlying
     * store.
     */
    private static final String SQLMAP_SAVE_PROJECT_GROUP = "SaveProjectGroup";

    /**
     * Deletes the specified {@link ProjectGroup} instance from the underlying
     * store.
     */
    private static final String SQLMAP_DELETE_PROJECT_GROUP = "DeleteProjectGroup";

    /**
     * Obtains a {@link ProjectGroup} instances from the underlying that matches
     * the specified criteria.
     */
    private static final String SQLMAP_LOOKUP_PROJECT_GROUP = "LookupProjectGroup";

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectGroupStore#deleteProjectGroup(org.apache.maven.continuum.model.project.ProjectGroup)
     */
    public void deleteProjectGroup( ProjectGroup project ) throws ContinuumStoreException
    {
        try
        {
            getSqlMapClient().startTransaction();

            getSqlMapClient().delete( SQLMAP_DELETE_PROJECT_GROUP, project );

            getSqlMapClient().commitTransaction();
        }
        catch ( SQLException e )
        {
            throw new ContinuumStoreException( "Unable to delete ProjectGroup '" + project.getName() + "'", e );
        }
        finally
        {
            try
            {
                getSqlMapClient().endTransaction();
            }
            catch ( SQLException e )
            {
                // log and forget
                getLogger().warn( "Unable to end transaction.", e );
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectGroupStore#getAllProjectGroups()
     */
    public List getAllProjectGroups() throws ContinuumStoreException
    {
        try
        {
            getSqlMapClient().startTransaction();

            return getSqlMapClient().queryForList( SQLMAP_GET_ALL_PROJECT_GROUPS, null );

        }
        catch ( SQLException e )
        {
            throw new ContinuumStoreException( "Unable to retrieve ProjectGroups.", e );
        }
        finally
        {
            try
            {
                getSqlMapClient().endTransaction();
            }
            catch ( SQLException e )
            {
                // log and forget
                getLogger().warn( "Unable to end transaction.", e );
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectGroupStore#lookupProjectGroup(org.apache.maven.continuum.key.GroupProjectKey)
     */
    public ProjectGroup lookupProjectGroup( GroupProjectKey key )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectGroupStore#saveProjectGroup(org.apache.maven.continuum.model.project.ProjectGroup)
     */
    public ProjectGroup saveProjectGroup( ProjectGroup group ) throws ContinuumStoreException
    {
        // TODO Auto-generated method stub

        return null;
    }

}
