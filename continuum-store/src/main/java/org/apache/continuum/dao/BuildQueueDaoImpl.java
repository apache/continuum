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

import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
@Repository( "buildQueueDao" )
@Component( role = org.apache.continuum.dao.BuildQueueDao.class )
public class BuildQueueDaoImpl
    extends AbstractDao
    implements BuildQueueDao
{
    private static Logger log = LoggerFactory.getLogger( BuildQueueDaoImpl.class );

    public BuildQueue addBuildQueue( BuildQueue buildQueue )
        throws ContinuumStoreException
    {
        return (BuildQueue) addObject( buildQueue );
    }

    public List<BuildQueue> getAllBuildQueues()
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildQueue.class, true );

            Query query = pm.newQuery( extent );

            List result = (List) query.execute();

            return result == null ? Collections.EMPTY_LIST : (List<BuildQueue>) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }

    public BuildQueue getBuildQueue( int buildQueueId )
        throws ContinuumStoreException
    {
        return (BuildQueue) getObjectById( BuildQueue.class, buildQueueId );
    }

    public BuildQueue getBuildQueueByName( String name )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildQueue.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String name" );

            query.setFilter( "this.name == name" );

            Collection result = (Collection) query.execute( name );

            if ( result.size() == 0 )
            {
                tx.commit();

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return (BuildQueue) object;
        }
        finally
        {
            rollback( tx );
        }
    }

    public void removeBuildQueue( BuildQueue buildQueue )
        throws ContinuumStoreException
    {
        removeObject( buildQueue );
    }

    public BuildQueue storeBuildQueue( BuildQueue buildQueue )
        throws ContinuumStoreException
    {
        updateObject( buildQueue );

        return buildQueue;
    }
}
