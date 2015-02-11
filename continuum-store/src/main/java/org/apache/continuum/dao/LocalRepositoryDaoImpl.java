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

import org.apache.continuum.model.repository.LocalRepository;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.component.annotations.Component;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
@Repository( "localRepositoryDao" )
@Component( role = org.apache.continuum.dao.LocalRepositoryDao.class )
public class LocalRepositoryDaoImpl
    extends AbstractDao
    implements LocalRepositoryDao
{
    public LocalRepository addLocalRepository( LocalRepository repository )
        throws ContinuumStoreException
    {
        return (LocalRepository) addObject( repository );
    }

    public void updateLocalRepository( LocalRepository repository )
        throws ContinuumStoreException
    {
        updateObject( repository );
    }

    public void removeLocalRepository( LocalRepository repository )
        throws ContinuumStoreException
    {
        removeObject( repository );
    }

    public List<LocalRepository> getAllLocalRepositories()
    {
        return getAllObjectsDetached( LocalRepository.class );
    }

    public List<LocalRepository> getLocalRepositoriesByLayout( String layout )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( LocalRepository.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String layout" );

            query.setFilter( "this.layout == layout" );

            List result = (List) query.execute( layout );

            return result == null ? Collections.EMPTY_LIST : (List) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }

    public LocalRepository getLocalRepository( int repositoryId )
        throws ContinuumStoreException
    {
        return (LocalRepository) getObjectById( LocalRepository.class, repositoryId );
    }

    public LocalRepository getLocalRepositoryByName( String name )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( LocalRepository.class, true );

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

            return (LocalRepository) object;
        }
        finally
        {
            rollback( tx );
        }
    }

    public LocalRepository getLocalRepositoryByLocation( String location )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( LocalRepository.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String location" );

            query.setFilter( "this.location == location" );

            Collection result = (Collection) query.execute( location );

            if ( result.size() == 0 )
            {
                tx.commit();

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return (LocalRepository) object;
        }
        finally
        {
            rollback( tx );
        }
    }
}
