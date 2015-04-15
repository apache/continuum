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

import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.component.annotations.Component;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
@Repository( "repositoryPurgeConfigurationDao" )
@Component( role = org.apache.continuum.dao.RepositoryPurgeConfigurationDao.class )
public class RepositoryPurgeConfigurationDaoImpl
    extends AbstractDao
    implements RepositoryPurgeConfigurationDao
{
    public List<RepositoryPurgeConfiguration> getAllRepositoryPurgeConfigurations()
    {
        return getAllObjectsDetached( RepositoryPurgeConfiguration.class );
    }

    public List<RepositoryPurgeConfiguration> getRepositoryPurgeConfigurationsBySchedule( int scheduleId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( RepositoryPurgeConfiguration.class, true );

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

    public List<RepositoryPurgeConfiguration> getEnableRepositoryPurgeConfigurationsBySchedule( int scheduleId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( RepositoryPurgeConfiguration.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int scheduleId" );

            query.setFilter( "this.schedule.id == scheduleId  && this.enabled == true" );

            List result = (List) query.execute( scheduleId );

            return result == null ? Collections.EMPTY_LIST : (List) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }

    public List<RepositoryPurgeConfiguration> getRepositoryPurgeConfigurationsByLocalRepository( int repositoryId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( RepositoryPurgeConfiguration.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int repositoryId" );

            query.setFilter( "this.repository.id == repositoryId" );

            List result = (List) query.execute( repositoryId );

            return result == null ? Collections.EMPTY_LIST : (List) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }

    public RepositoryPurgeConfiguration getRepositoryPurgeConfiguration( int configurationId )
        throws ContinuumStoreException
    {
        return getObjectById( RepositoryPurgeConfiguration.class, configurationId );
    }

    public RepositoryPurgeConfiguration addRepositoryPurgeConfiguration(
        RepositoryPurgeConfiguration purgeConfiguration )
        throws ContinuumStoreException
    {
        return addObject( purgeConfiguration );
    }

    public void updateRepositoryPurgeConfiguration( RepositoryPurgeConfiguration purgeConfiguration )
        throws ContinuumStoreException
    {
        updateObject( purgeConfiguration );
    }

    public void removeRepositoryPurgeConfiguration( RepositoryPurgeConfiguration purgeConfiguration )
        throws ContinuumStoreException
    {
        removeObject( purgeConfiguration );
    }

}
