package org.apache.maven.continuum.store;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.maven.continuum.key.GroupProjectKey;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import java.util.List;

/**
 * Store implementation that interacts with the underlying JDO-based store.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 * @plexus.component role="org.apache.maven.continuum.store.RefactoredContinuumStore"
 *                   role-hint="jdo"
 */
public class JdoProjectStore extends AbstractJdoStore implements ProjectStore
{

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#deleteProject(org.apache.maven.continuum.model.project.Project)
     */
    public void deleteProject( Project project ) throws ContinuumStoreException
    {
        // TODO: Any checks before Project should be deleted?
        removeObject( project );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#lookupProject(org.apache.maven.continuum.key.GroupProjectKey)
     */
    public Project lookupProject( GroupProjectKey key )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        // TODO: Review! Would be nice if we can some how bind param to values
        // and then execute the query.
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "String groupKey, String projectKey" );

            query.setFilter( "this.project.groupKey = groupKey && this.project.key == projectKey" );

            List result = (List) query.execute( key.getGroupKey(), key.getProjectKey() );

            result = (List) pm.detachCopyAll( result );

            tx.commit();

            if ( result.size() == 0 )
                throw new ContinuumObjectNotFoundException( "Unable to lookup Project with groupKey '"
                                + key.getGroupKey() + "' and projectKey '" + key.getProjectKey() + "'" );

            return (Project) result.get( 0 );
        }
        finally
        {
            rollback( tx );
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#saveProject(org.apache.maven.continuum.model.project.Project)
     */
    public Project saveProject( Project project ) throws ContinuumStoreException
    {
        updateObject( project );
        return project;
    }
}
