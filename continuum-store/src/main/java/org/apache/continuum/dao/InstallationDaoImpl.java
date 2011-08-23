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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.stereotype.Repository;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.apache.continuum.dao.InstallationDao"
 */
@Repository("installationDao")
public class InstallationDaoImpl
    extends AbstractDao
    implements InstallationDao
{
    public Installation addInstallation( Installation installation )
    {
        return (Installation) addObject( installation );
    }

    public List<Installation> getAllInstallations()
    {
        return getAllObjectsDetached( Installation.class, "name ascending", null );
    }

    public void removeInstallation( Installation installation )
        throws ContinuumStoreException
    {
        // first delete link beetwen profile and this installation
        // then removing this
        //attachAndDelete( installation );
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            // this must be done in the same transaction
            tx.begin();

            // first removing linked jdk

            Extent extent = pm.getExtent( Profile.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String name" );

            query.setFilter( "this.jdk.name == name" );

            Collection<Profile> result = (Collection) query.execute( installation.getName() );

            if ( result.size() != 0 )
            {
                for ( Profile profile : result )
                {
                    profile.setJdk( null );
                    pm.makePersistent( profile );
                }
            }

            // removing linked builder
            query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String name" );

            query.setFilter( "this.builder.name == name" );

            result = (Collection) query.execute( installation.getName() );

            if ( result.size() != 0 )
            {
                for ( Profile profile : result )
                {
                    profile.setBuilder( null );
                    pm.makePersistent( profile );
                }
            }

            // removing linked env Var
            query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );
            query.declareImports( "import " + Installation.class.getName() );

            query.declareParameters( "Installation installation" );

            query.setFilter( "environmentVariables.contains(installation)" );

            //query = pm
            //    .newQuery( "SELECT FROM profile WHERE environmentVariables.contains(installation) && installation.name == name" );

            result = (Collection) query.execute( installation );

            if ( result.size() != 0 )
            {
                for ( Profile profile : result )
                {
                    List<Installation> newEnvironmentVariables = new ArrayList<Installation>();
                    for ( Installation current : (Iterable<Installation>) profile.getEnvironmentVariables() )
                    {
                        if ( !StringUtils.equals( current.getName(), installation.getName() ) )
                        {
                            newEnvironmentVariables.add( current );
                        }
                    }
                    profile.setEnvironmentVariables( newEnvironmentVariables );
                    pm.makePersistent( profile );
                }
            }

            pm.deletePersistent( installation );

            tx.commit();

        }
        finally
        {
            rollback( tx );
        }
    }

    public void updateInstallation( Installation installation )
        throws ContinuumStoreException
    {
        updateObject( installation );
    }

    public Installation getInstallation( int installationId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( Installation.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int installationId" );

            query.setFilter( "this.installationId == installationId" );

            Collection result = (Collection) query.execute( installationId );

            if ( result.size() == 0 )
            {
                tx.commit();

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return (Installation) object;
        }
        finally
        {
            rollback( tx );
        }
    }

    public Installation getInstallation( String installationName )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( Installation.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String name" );

            query.setFilter( "this.name == name" );

            Collection result = (Collection) query.execute( installationName );

            if ( result.size() == 0 )
            {
                tx.commit();

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return (Installation) object;
        }
        finally
        {
            rollback( tx );
        }
    }
}
