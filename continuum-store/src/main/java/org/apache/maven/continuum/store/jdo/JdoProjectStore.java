package org.apache.maven.continuum.store.jdo;

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
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.store.ProjectStore;
import org.codehaus.plexus.jdo.PlexusJdoUtils;
import org.codehaus.plexus.jdo.PlexusStoreException;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import java.util.List;

/**
 * Concrete implementation for {@link ProjectStore}.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 * @plexus.component role="org.apache.maven.continuum.store.ProjectStore"
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

            Extent extent = pm.getExtent( Project.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "String groupKey, String projectKey" );

            // XXX: Why do we have a 'groupKey' column set up in Project table?
            query.setFilter( "this.projectGroup.key == groupKey && this.key == projectKey" );

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
        try
        {
            if ( project.getId() > 0 )
                PlexusJdoUtils.saveObject( getPersistenceManager(), project, new String[0] );
            else
                PlexusJdoUtils.addObject( getPersistenceManager(), project );
        }
        catch ( PlexusStoreException e )
        {
            throw new ContinuumStoreException( "Error saving Project.", e );
        }
        return project;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#getAllProjects()
     */
    public List getAllProjects() throws ContinuumStoreException
    {
        return getAllObjectsDetached( Project.class, "name ascending", null );
    }

}
