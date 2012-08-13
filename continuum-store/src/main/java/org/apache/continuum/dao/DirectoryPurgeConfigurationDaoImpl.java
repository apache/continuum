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

import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.apache.continuum.dao.DirectoryPurgeConfigurationDao"
 */
@Repository( "directoryPurgeConfigurationDao" )
public class DirectoryPurgeConfigurationDaoImpl
    extends AbstractDao
    implements DirectoryPurgeConfigurationDao
{
    public List<DirectoryPurgeConfiguration> getAllDirectoryPurgeConfigurations()
    {
        return getAllObjectsDetached( DirectoryPurgeConfiguration.class );
    }

    public List<DirectoryPurgeConfiguration> getDirectoryPurgeConfigurationsBySchedule( int scheduleId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( DirectoryPurgeConfiguration.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int scheduleId" );

            query.setFilter( "this.schedule.id == scheduleId" );

            List result = (List) query.execute( scheduleId );

            return result == null ? Collections.EMPTY_LIST : (List) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }

    public List<DirectoryPurgeConfiguration> getEnableDirectoryPurgeConfigurationsBySchedule( int scheduleId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( DirectoryPurgeConfiguration.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int scheduleId" );

            query.setFilter( "this.schedule.id == scheduleId && this.enabled == true" );

            List result = (List) query.execute( scheduleId );

            return result == null ? Collections.EMPTY_LIST : (List) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }

    public List<DirectoryPurgeConfiguration> getDirectoryPurgeConfigurationsByLocation( String location )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( DirectoryPurgeConfiguration.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String location" );

            query.setFilter( "this.location == location" );

            List result = (List) query.execute( location );

            return result == null ? Collections.EMPTY_LIST : (List) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }

    public List<DirectoryPurgeConfiguration> getDirectoryPurgeConfigurationsByType( String type )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( DirectoryPurgeConfiguration.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String type" );

            query.setFilter( "this.directoryType == type" );

            List result = (List) query.execute( type );

            return result == null ? Collections.EMPTY_LIST : (List) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }

    public DirectoryPurgeConfiguration getDirectoryPurgeConfiguration( int configurationId )
        throws ContinuumStoreException
    {
        return (DirectoryPurgeConfiguration) getObjectById( DirectoryPurgeConfiguration.class, configurationId );
    }

    public DirectoryPurgeConfiguration addDirectoryPurgeConfiguration( DirectoryPurgeConfiguration purgeConfiguration )
        throws ContinuumStoreException
    {
        return (DirectoryPurgeConfiguration) addObject( purgeConfiguration );
    }

    public void updateDirectoryPurgeConfiguration( DirectoryPurgeConfiguration purgeConfiguration )
        throws ContinuumStoreException
    {
        updateObject( purgeConfiguration );
    }

    public void removeDirectoryPurgeConfiguration( DirectoryPurgeConfiguration purgeConfiguration )
        throws ContinuumStoreException
    {
        removeObject( purgeConfiguration );
    }
}
