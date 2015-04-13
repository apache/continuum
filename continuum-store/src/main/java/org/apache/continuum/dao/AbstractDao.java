package org.apache.continuum.dao;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.jdo.PlexusJdoUtils;
import org.codehaus.plexus.jdo.PlexusObjectNotFoundException;
import org.codehaus.plexus.jdo.PlexusStoreException;

import javax.annotation.Resource;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
public class AbstractDao
{
    // ----------------------------------------------------------------------
    // Fetch Groups
    // ----------------------------------------------------------------------

    protected static final String PROJECT_WITH_BUILDS_FETCH_GROUP = "project-with-builds";

    protected static final String PROJECT_WITH_CHECKOUT_RESULT_FETCH_GROUP = "project-with-checkout-result";

    protected static final String BUILD_RESULT_WITH_DETAILS_FETCH_GROUP = "build-result-with-details";

    protected static final String PROJECT_BUILD_DETAILS_FETCH_GROUP = "project-build-details";

    protected static final String PROJECT_ALL_DETAILS_FETCH_GROUP = "project-all-details";

    protected static final String PROJECT_DEPENDENCIES_FETCH_GROUP = "project-dependencies";

    protected static final String PROJECTGROUP_PROJECTS_FETCH_GROUP = "projectgroup-projects";

    protected static final String BUILD_TEMPLATE_BUILD_DEFINITIONS = "build-template-build-definitions";

    @Resource
    @Requirement
    protected StoreUtilities storeUtilities;

    protected Object addObject( Object object )
    {
        return addObject( getPersistenceManager(), object );
    }

    private Object addObject( PersistenceManager pmf, Object object )
    {
        return PlexusJdoUtils.addObject( pmf, object );
    }

    protected void removeObject( Object o )
    {
        PlexusJdoUtils.removeObject( getPersistenceManager(), o );
    }

    protected void updateObject( Object object )
        throws ContinuumStoreException
    {
        updateObject( getPersistenceManager(), object );
    }

    private void updateObject( PersistenceManager pmf, Object object )
        throws ContinuumStoreException
    {
        try
        {
            PlexusJdoUtils.updateObject( pmf, object );
        }
        catch ( PlexusStoreException e )
        {
            throw new ContinuumStoreException( e.getMessage(), e );
        }
    }

    protected Object getObjectById( Class clazz, int id )
        throws ContinuumStoreException
    {
        return getObjectById( clazz, id, null );
    }

    protected Object getObjectById( Class clazz, int id, String fetchGroup )
        throws ContinuumStoreException
    {
        try
        {
            return PlexusJdoUtils.getObjectById( getPersistenceManager(), clazz, id, fetchGroup );
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

    protected Object getObjectFromQuery( Class clazz, String idField, String id, String fetchGroup )
        throws ContinuumStoreException
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

    protected List getAllObjectsDetached( Class clazz )
    {
        return getAllObjectsDetached( clazz, null );
    }

    protected List getAllObjectsDetached( Class clazz, String fetchGroup )
    {
        return getAllObjectsDetached( clazz, null, fetchGroup );
    }

    protected List getAllObjectsDetached( Class clazz, String ordering, String fetchGroup )
    {
        return getAllObjectsDetached( getPersistenceManager(), clazz, ordering, fetchGroup );
    }

    protected List getAllObjectsDetached( PersistenceManager pmf, Class clazz, String ordering, String fetchGroup )
    {
        return PlexusJdoUtils.getAllObjectsDetached( pmf, clazz, ordering, fetchGroup );
    }

    protected void rollback( Transaction tx )
    {
        PlexusJdoUtils.rollbackIfActive( tx );
    }

    protected void attachAndDelete( Object object )
    {
        PlexusJdoUtils.attachAndDelete( getPersistenceManager(), object );
    }

    protected PersistenceManager getPersistenceManager()
    {
        return getPersistenceManager( getContinuumPersistenceManagerFactory() );
    }

    private PersistenceManager getPersistenceManager( PersistenceManagerFactory pmf )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        pm.getFetchPlan().setMaxFetchDepth( -1 );
        pm.getFetchPlan().setDetachmentOptions( FetchPlan.DETACH_LOAD_FIELDS );

        return pm;
    }

    protected PersistenceManagerFactory getContinuumPersistenceManagerFactory()
    {
        return storeUtilities.getContinuumPersistenceManagerFactory();
    }

    protected Object makePersistent( PersistenceManager pm, Object object, boolean detach )
    {
        return PlexusJdoUtils.makePersistent( pm, object, detach );
    }

}
