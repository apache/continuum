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

import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.component.annotations.Component;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
@Repository( "scheduleDao" )
@Component( role = org.apache.continuum.dao.ScheduleDao.class )
public class ScheduleDaoImpl
    extends AbstractDao
    implements ScheduleDao
{
    public List<Schedule> getAllSchedulesByName()
    {
        return getAllObjectsDetached( Schedule.class, "name ascending", null );
    }

    public Schedule addSchedule( Schedule schedule )
    {
        return addObject( schedule );
    }

    public Schedule getScheduleByName( String name )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( Schedule.class, true );

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

            return (Schedule) object;
        }
        finally
        {
            rollback( tx );
        }
    }

    public Schedule storeSchedule( Schedule schedule )
        throws ContinuumStoreException
    {
        updateObject( schedule );

        return schedule;
    }

    public void updateSchedule( Schedule schedule )
        throws ContinuumStoreException
    {
        updateObject( schedule );
    }

    public void removeSchedule( Schedule schedule )
    {
        removeObject( schedule );
    }

    public Schedule getSchedule( int scheduleId )
        throws ContinuumStoreException
    {
        return getObjectById( Schedule.class, scheduleId );
    }
}
