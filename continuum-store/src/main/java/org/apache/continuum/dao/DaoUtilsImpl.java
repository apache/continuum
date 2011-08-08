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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jdo.Extent;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.model.release.ContinuumReleaseResult;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.jdo.PlexusJdoUtils;
import org.springframework.stereotype.Repository;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.apache.continuum.dao.DaoUtils"
 */
@Repository("daoUtils")
public class DaoUtilsImpl
    extends AbstractDao
    implements DaoUtils
{
    /**
     * @plexus.requirement role="org.apache.continuum.dao.ProjectDao"
     */
    @Resource
    private ProjectDao projectDao;

    public void closeStore()
    {
        closePersistenceManagerFactory( getContinuumPersistenceManagerFactory(), 1 );
    }

    public void eraseDatabase()
    {
        PlexusJdoUtils.removeAll( getPersistenceManager(), BuildResult.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), BuildDefinitionTemplate.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ContinuumReleaseResult.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ProjectScmRoot.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ProjectGroup.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), Project.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), BuildDefinition.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), RepositoryPurgeConfiguration.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), LocalRepository.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), DirectoryPurgeConfiguration.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), Schedule.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), BuildQueue.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), Profile.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), Installation.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ScmResult.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), SystemConfiguration.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ProjectNotifier.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ProjectDeveloper.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ProjectDependency.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ChangeSet.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ChangeFile.class );
    }

    /**
     * Close the PersistenceManagerFactory.
     *
     * @param numTry The number of try. The maximum try is 5.
     */
    private void closePersistenceManagerFactory( PersistenceManagerFactory pmf, int numTry )
    {
        if ( pmf != null )
        {
            if ( !pmf.isClosed() )
            {
                try
                {
                    pmf.close();
                }
                catch ( JDOUserException e )
                {
                    if ( numTry < 5 )
                    {
                        try
                        {
                            Thread.currentThread().wait( 1000 );
                        }
                        catch ( InterruptedException ie )
                        {
                            // nothing to do
                        }

                        closePersistenceManagerFactory( pmf, numTry + 1 );
                    }
                    else
                    {
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * get the combined list of projectId and build definitions, including the
     * ones inherited by their project group
     *
     * @param scheduleId
     * @return
     * @throws org.apache.maven.continuum.store.ContinuumStoreException
     *
     * @todo Move to a better place
     */
    public Map<Integer, Object> getAggregatedProjectIdsAndBuildDefinitionIdsBySchedule( int scheduleId )
        throws ContinuumStoreException
    {
        Map<Integer, Object> projectSource = getProjectIdsAndBuildDefinitionsIdsBySchedule( scheduleId );
        Map<Integer, Object> projectGroupSource = getProjectGroupIdsAndBuildDefinitionsIdsBySchedule( scheduleId );

        Map<Integer, Object> aggregate = new HashMap<Integer, Object>();

        // start out by checking if we have projects with this scheduleId
        if ( projectSource != null )
        {
            aggregate.putAll( projectSource );
        }

        // iterate through the project groups and make sure we are not walking
        // over projects that
        // might define their own build definitions
        if ( projectGroupSource != null )
        {
            for ( Integer projectGroupId : projectGroupSource.keySet() )
            {
                List<Project> projectsInGroup = projectDao.getProjectsInGroup( projectGroupId );

                for ( Project p : projectsInGroup )
                {
                    Integer projectId = p.getId();
                    if ( !aggregate.keySet().contains( projectId ) )
                    {
                        aggregate.put( projectId, projectGroupSource.get( projectGroupId ) );
                    }
                }
            }
        }
        return aggregate;
    }

    /**
     * @param scheduleId
     * @return
     * @throws ContinuumStoreException
     * @todo Move to a better place
     */
    public Map<Integer, Object> getProjectIdsAndBuildDefinitionsIdsBySchedule( int scheduleId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( Project.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int scheduleId" );

            query.declareImports( "import org.apache.maven.continuum.model.project.BuildDefinition" );

            query.declareVariables( "BuildDefinition buildDef" );

            query.setFilter( "buildDefinitions.contains(buildDef) && buildDef.schedule.id == scheduleId" );

            query.setResult( "this.id, buildDef.id" );

            List result = (List) query.execute( scheduleId );

            Map projects = new HashMap();

            if ( result != null && !result.isEmpty() )
            {
                for ( Iterator i = result.iterator(); i.hasNext(); )
                {
                    Object[] obj = (Object[]) i.next();

                    List buildDefinitions;

                    if ( projects.get( obj[0] ) != null )
                    {
                        buildDefinitions = (List) projects.get( obj[0] );
                    }
                    else
                    {
                        buildDefinitions = new ArrayList();

                        projects.put( obj[0], buildDefinitions );
                    }

                    buildDefinitions.add( obj[1] );
                }

                return projects;
            }
            if ( !projects.isEmpty() )
            {
                return projects;
            }
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }

        return null;
    }

    /**
     * @param scheduleId
     * @return
     * @throws ContinuumStoreException
     * @todo Move to a better place
     */
    public Map<Integer, Object> getProjectGroupIdsAndBuildDefinitionsIdsBySchedule( int scheduleId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ProjectGroup.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int scheduleId" );

            query.declareImports( "import org.apache.maven.continuum.model.project.BuildDefinition" );

            query.declareVariables( "BuildDefinition buildDef" );

            query.setFilter( "buildDefinitions.contains(buildDef) && buildDef.schedule.id == scheduleId" );

            query.setResult( "this.id, buildDef.id" );

            List result = (List) query.execute( scheduleId );

            Map projectGroups = new HashMap();

            if ( result != null && !result.isEmpty() )
            {
                for ( Iterator i = result.iterator(); i.hasNext(); )
                {
                    Object[] obj = (Object[]) i.next();

                    List buildDefinitions;

                    if ( projectGroups.get( obj[0] ) != null )
                    {
                        buildDefinitions = (List) projectGroups.get( obj[0] );
                    }
                    else
                    {
                        buildDefinitions = new ArrayList();

                        projectGroups.put( obj[0], buildDefinitions );
                    }

                    buildDefinitions.add( obj[1] );
                }

                return projectGroups;
            }
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
        return null;
    }
}
