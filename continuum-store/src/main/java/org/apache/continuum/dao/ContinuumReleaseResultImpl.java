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

import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.continuum.model.release.ContinuumReleaseResult;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 * @plexus.component role="org.apache.continuum.dao.ContinuumReleaseResultDao"
 */
public class ContinuumReleaseResultImpl
    extends AbstractDao
    implements ContinuumReleaseResultDao
{
    public ContinuumReleaseResult addContinuumReleaseResult( ContinuumReleaseResult releaseResult )
        throws ContinuumStoreException
    {
        return (ContinuumReleaseResult) addObject( releaseResult );
    }

    public List<ContinuumReleaseResult> getAllContinuumReleaseResults()
    {
        return getAllObjectsDetached( ContinuumReleaseResult.class );
    }

    public ContinuumReleaseResult getContinuumReleaseResult( int releaseResultId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (ContinuumReleaseResult) getObjectById( ContinuumReleaseResult.class, releaseResultId );
    }

    public List<ContinuumReleaseResult> getContinuumReleaseResultsByProjectGroup( int projectGroupId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ContinuumReleaseResult.class, true );

            Query query = pm.newQuery( extent, "projectGroup.id == " + projectGroupId );

            List result = (List) query.execute();

            result = (List) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public void removeContinuumReleaseResult( ContinuumReleaseResult releaseResult )
        throws ContinuumStoreException
    {
        removeObject( releaseResult );
    }

}
