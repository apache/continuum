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

import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
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
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 */
@Repository( "projectScmRootDao" )
@Component( role = org.apache.continuum.dao.ProjectScmRootDao.class )
public class ProjectScmRootDaoImpl
    extends AbstractDao
    implements ProjectScmRootDao
{
    public ProjectScmRoot addProjectScmRoot( ProjectScmRoot projectScmRoot )
        throws ContinuumStoreException
    {
        return addObject( projectScmRoot );
    }

    public List<ProjectScmRoot> getAllProjectScmRoots()
    {
        return getAllObjectsDetached( ProjectScmRoot.class );
    }

    public List<ProjectScmRoot> getProjectScmRootByProjectGroup( int projectGroupId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ProjectScmRoot.class, true );

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

    public void removeProjectScmRoot( ProjectScmRoot projectScmRoot )
        throws ContinuumStoreException
    {
        removeObject( projectScmRoot );
    }

    public void updateProjectScmRoot( ProjectScmRoot projectScmRoot )
        throws ContinuumStoreException
    {
        updateObject( projectScmRoot );
    }

    public ProjectScmRoot getProjectScmRootByProjectGroupAndScmRootAddress( int projectGroupId, String scmRootAddress )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ProjectScmRoot.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "int projectGroupId, String scmRootAddress" );

            query.setFilter( "this.projectGroup.id == projectGroupId && this.scmRootAddress == scmRootAddress" );

            Object[] params = new Object[2];
            params[0] = projectGroupId;
            params[1] = scmRootAddress;

            Collection result = (Collection) query.executeWithArray( params );

            if ( result.size() == 0 )
            {
                tx.commit();

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return (ProjectScmRoot) object;
        }
        finally
        {
            rollback( tx );
        }
    }

    public ProjectScmRoot getProjectScmRoot( int projectScmRootId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return getObjectById( ProjectScmRoot.class, projectScmRootId );
    }
}
