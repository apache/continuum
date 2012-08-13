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

import org.apache.continuum.model.repository.DistributedDirectoryPurgeConfiguration;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * @author
 * @version $Id$
 * @plexus.component role="org.apache.continuum.dao.DistributedDirectoryPurgeConfigurationDao"
 */
@Repository( "distributedDirectoryPurgeConfigurationDao" )
public class DistributedDirectoryPurgeConfigurationDaoImpl
    extends AbstractDao
    implements DistributedDirectoryPurgeConfigurationDao
{
    public List<DistributedDirectoryPurgeConfiguration> getAllDistributedDirectoryPurgeConfigurations()
    {
        return getAllObjectsDetached( DistributedDirectoryPurgeConfiguration.class );
    }

    public List<DistributedDirectoryPurgeConfiguration> getDistributedDirectoryPurgeConfigurationsBySchedule(
        int scheduleId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( DistributedDirectoryPurgeConfiguration.class, true );

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

    public List<DistributedDirectoryPurgeConfiguration> getEnableDistributedDirectoryPurgeConfigurationsBySchedule(
        int scheduleId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( DistributedDirectoryPurgeConfiguration.class, true );

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

    public List<DistributedDirectoryPurgeConfiguration> getDistributedDirectoryPurgeConfigurationsByType( String type )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( DistributedDirectoryPurgeConfiguration.class, true );

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

    public DistributedDirectoryPurgeConfiguration getDistributedDirectoryPurgeConfiguration( int configurationId )
        throws ContinuumStoreException
    {
        return (DistributedDirectoryPurgeConfiguration) getObjectById( DistributedDirectoryPurgeConfiguration.class,
                                                                       configurationId );
    }

    public DistributedDirectoryPurgeConfiguration addDistributedDirectoryPurgeConfiguration(
        DistributedDirectoryPurgeConfiguration purgeConfiguration )
        throws ContinuumStoreException
    {
        return (DistributedDirectoryPurgeConfiguration) addObject( purgeConfiguration );
    }

    public void updateDistributedDirectoryPurgeConfiguration(
        DistributedDirectoryPurgeConfiguration purgeConfiguration )
        throws ContinuumStoreException
    {
        updateObject( purgeConfiguration );
    }

    public void removeDistributedDirectoryPurgeConfiguration(
        DistributedDirectoryPurgeConfiguration purgeConfiguration )
        throws ContinuumStoreException
    {
        removeObject( purgeConfiguration );
    }
}
