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

import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.component.annotations.Component;
import org.springframework.stereotype.Repository;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.maven.continuum.project.ContinuumProjectState.BUILDING;
import static org.apache.maven.continuum.project.ContinuumProjectState.CANCELLED;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
@Repository( "buildResultDao" )
@Component( role = org.apache.continuum.dao.BuildResultDao.class )
public class BuildResultDaoImpl
    extends AbstractDao
    implements BuildResultDao
{
    public void updateBuildResult( BuildResult build )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        Project project = build.getProject();
        try
        {
            tx.begin();

            if ( !JDOHelper.isDetached( build ) )
            {
                throw new ContinuumStoreException( "Not detached: " + build );
            }

            pm.makePersistent( build );

            if ( !JDOHelper.isDetached( project ) )
            {
                throw new ContinuumStoreException( "Not detached: " + project );
            }

            project.setState( build.getState() );

            //TODO: Use projectDao
            pm.makePersistent( project );

            tx.commit();
        }
        finally
        {
            rollback( tx );
        }
    }

    public void addBuildResult( Project project, BuildResult build )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            pm.getFetchPlan().addGroup( PROJECT_WITH_BUILDS_FETCH_GROUP );

            Object objectId = pm.newObjectIdInstance( Project.class, project.getId() );

            project = (Project) pm.getObjectById( objectId );

            build = makePersistent( pm, build, false );

            // TODO: these are in the wrong spot - set them on success (though
            // currently some depend on latest build being the one in progress)
            project.setLatestBuildId( build.getId() );

            project.setState( build.getState() );

            project.addBuildResult( build );

            tx.commit();
        }
        finally
        {
            rollback( tx );
        }
    }

    public BuildResult getLatestBuildResultForProject( int projectId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int projectId" );

            query.setFilter( "this.project.id == projectId && this.project.latestBuildId == this.id" );

            List<BuildResult> result = (List<BuildResult>) query.execute( projectId );

            result = (List<BuildResult>) pm.detachCopyAll( result );

            tx.commit();

            if ( result != null && !result.isEmpty() )
            {
                return result.get( 0 );
            }
        }
        finally
        {
            rollback( tx );
        }
        return null;
    }

    public BuildResult getLatestBuildResultForProjectWithDetails( int projectId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            pm.getFetchPlan().addGroup( BUILD_RESULT_WITH_DETAILS_FETCH_GROUP );

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int projectId" );

            query.setFilter( "this.project.id == projectId && this.project.latestBuildId == this.id" );

            List<BuildResult> result = (List<BuildResult>) query.execute( projectId );

            result = (List<BuildResult>) pm.detachCopyAll( result );

            tx.commit();

            if ( result != null && !result.isEmpty() )
            {
                return result.get( 0 );
            }
        }
        finally
        {
            rollback( tx );
        }
        return null;
    }

    public BuildResult getLatestBuildResultForBuildDefinition( int projectId, int buildDefinitionId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int projectId, int buildDefinitionId" );

            query.setFilter( "this.project.id == projectId && this.buildDefinition.id == buildDefinitionId" );
            query.setOrdering( "id descending" );

            Object[] params = new Object[2];
            params[0] = projectId;
            params[1] = buildDefinitionId;

            List<BuildResult> result = (List<BuildResult>) query.executeWithArray( params );

            result = (List<BuildResult>) pm.detachCopyAll( result );

            tx.commit();

            if ( result != null && !result.isEmpty() )
            {
                return result.get( 0 );
            }
        }
        finally
        {
            rollback( tx );
        }
        return null;
    }

    public BuildResult getPreviousBuildResultForBuildDefinition( int projectId, int buildDefinitionId,
                                                                 int buildResultId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int projectId, int buildDefinitionId, int buildResultId" );

            query.setFilter( "this.project.id == projectId && this.buildDefinition.id == buildDefinitionId "
                                 + "&& this.id < buildResultId" );
            query.setOrdering( "id descending" );

            Object[] params = new Object[3];
            params[0] = projectId;
            params[1] = buildDefinitionId;
            params[2] = buildResultId;

            List<BuildResult> result = (List<BuildResult>) query.executeWithArray( params );

            result = (List<BuildResult>) pm.detachCopyAll( result );

            tx.commit();

            if ( result != null && !result.isEmpty() )
            {
                return result.get( 0 );
            }
        }
        finally
        {
            rollback( tx );
        }
        return null;
    }

    public Map<Integer, BuildResult> getLatestBuildResultsByProjectGroupId( int projectGroupId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            String filter = "this.project.latestBuildId == this.id";

            if ( projectGroupId > 0 )
            {
                query.declareParameters( "int projectGroupId" );
                filter += " && this.project.projectGroup.id == projectGroupId";
            }

            query.setFilter( filter );

            List<BuildResult> result;
            if ( projectGroupId > 0 )
            {
                result = (List<BuildResult>) query.execute( projectGroupId );
            }
            else
            {
                result = (List<BuildResult>) query.execute();
            }

            result = (List<BuildResult>) pm.detachCopyAll( result );

            tx.commit();

            if ( result != null && !result.isEmpty() )
            {
                Map<Integer, BuildResult> builds = new HashMap<Integer, BuildResult>();

                for ( BuildResult br : result )
                {
                    builds.put( br.getProject().getId(), br );
                }

                return builds;
            }
        }
        finally
        {
            rollback( tx );
        }

        return null;
    }

    public void removeBuildResult( BuildResult buildResult )
    {
        removeObject( buildResult );
    }

    public List<BuildResult> getAllBuildsForAProjectByDate( int projectId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Query query = pm.newQuery( "SELECT FROM " + BuildResult.class.getName() +
                                           " WHERE project.id == projectId PARAMETERS int projectId ORDER BY endTime DESC" );

            query.declareImports( "import java.lang.Integer" );

            query.declareParameters( "Integer projectId" );

            List<BuildResult> result = (List<BuildResult>) query.execute( projectId );

            result = (List<BuildResult>) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public BuildResult getBuildResult( int buildId )
        throws ContinuumStoreException
    {
        return getObjectById( BuildResult.class, buildId, BUILD_RESULT_WITH_DETAILS_FETCH_GROUP );
    }

    public List<BuildResult> getBuildResultByBuildNumber( int projectId, int buildNumber )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int projectId, int buildNumber" );

            query.setFilter( "this.project.id == projectId && this.buildNumber == buildNumber" );

            List<BuildResult> result = (List<BuildResult>) query.execute( projectId, buildNumber );

            result = (List<BuildResult>) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public List<BuildResult> getBuildResultsByBuildDefinition( int projectId, int buildDefinitionId )
    {
        return getBuildResultsByBuildDefinition( projectId, buildDefinitionId, -1, -1 );
    }

    public List<BuildResult> getBuildResultsByBuildDefinition( int projectId, int buildDefinitionId, long startIndex,
                                                               long endIndex )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            if ( startIndex >= 0 && endIndex >= 0 )
            {
                query.setRange( startIndex, endIndex );
            }

            query.declareParameters( "int projectId, int buildDefinitionId" );

            query.setFilter( "this.project.id == projectId && this.buildDefinition.id == buildDefinitionId" );

            query.setOrdering( "this.id descending" );

            List<BuildResult> result = (List<BuildResult>) query.execute( projectId, buildDefinitionId );

            result = (List<BuildResult>) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public long getNbBuildResultsForProject( int projectId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Query query = pm.newQuery( BuildResult.class, "project.id == projectId" );

            query.declareParameters( "int projectId" );

            query.setResult( "count(this)" );

            long result = (Long) query.execute( projectId );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public long getNbBuildResultsInSuccessForProject( int projectId, long fromDate )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int projectId, long fromDate, int state" );

            query.setFilter( "this.project.id == projectId && this.startTime > fromDate && this.state == state" );

            query.setResult( "count(this)" );

            long result = (Long) query.execute( projectId, fromDate, ContinuumProjectState.OK );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public List<BuildResult> getBuildResultsForProjectWithDetails( int projectId, long fromDate, int tobuildResultId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );
            pm.getFetchPlan().addGroup( BUILD_RESULT_WITH_DETAILS_FETCH_GROUP );

            Query query = pm.newQuery( extent );

            String parameters = "int projectId, long fromDate";
            String filter = "this.project.id == projectId && this.startTime > fromDate";

            if ( tobuildResultId > 0 )
            {
                parameters += ", int buildResultId";
                filter += " && this.id < buildResultId";
            }
            query.declareParameters( parameters );

            query.setFilter( filter );

            query.setOrdering( "this.id descending" );

            List<BuildResult> result;

            if ( tobuildResultId > 0 )
            {
                result = (List<BuildResult>) query.execute( projectId, fromDate, tobuildResultId );
            }
            else
            {
                result = (List<BuildResult>) query.execute( projectId, fromDate );
            }

            result = (List<BuildResult>) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public List<BuildResult> getBuildResultsForProject( int projectId )
    {
        return getBuildResultsForProjectWithDetails( projectId, -1, -1 );
    }

    public List<BuildResult> getBuildResultsForProject( int projectId, long startIndex, long endIndex )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int projectId" );

            query.setFilter( "this.project.id == projectId" );

            query.setOrdering( "this.startTime descending" );

            if ( startIndex >= 0 )
            {
                query.setRange( startIndex, endIndex );
            }

            List<BuildResult> result = (List<BuildResult>) query.execute( projectId );

            result = (List<BuildResult>) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public List<BuildResult> getBuildResultsForProjectFromId( int projectId, long startId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        pm.getFetchPlan().addGroup( BUILD_RESULT_WITH_DETAILS_FETCH_GROUP );

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int projectId, int buildNumber" );

            query.setFilter( "this.project.id == projectId && this.buildNumber >= buildNumber" );

            query.setOrdering( "this.startTime descending" );

            List<BuildResult> result = (List<BuildResult>) query.execute( projectId, startId );

            result = (List<BuildResult>) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( e.getMessage(), e );
        }
        finally
        {
            rollback( tx );
        }
    }

    public BuildResult getLatestBuildResultInSuccess( int projectId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int projectId" );

            String filter = "this.project.buildNumber == this.buildNumber && this.project.id == projectId";

            query.setFilter( filter );

            query.setUnique( true );

            BuildResult result = (BuildResult) query.execute( projectId );

            result = (BuildResult) pm.detachCopy( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    private int getPreviousBuildResultIdInSuccess( int projectId, int buildResultId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int projectId, int buildResultId" );

            String filter = "this.id < buildResultId && this.state == " + ContinuumProjectState.OK +
                "  && this.project.id == projectId";

            query.setFilter( filter );

            query.setResult( "max(this.id)" );

            int result = (Integer) query.execute( projectId, buildResultId );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public Set<Integer> resolveOrphanedInProgressResults( long ageCutoff )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try
        {
            tx.begin();
            Query query = pm.newQuery( BuildResult.class );
            query.declareParameters( "long orphanCutoff" );
            String filter = String.format( " this.state == %s && this.startTime < orphanCutoff", BUILDING );
            query.setFilter( filter );
            List<BuildResult> orphans = (List<BuildResult>) pm.detachCopyAll( (List) query.execute( ageCutoff ) );
            Set<Integer> updatedIds = new HashSet<Integer>();
            for ( BuildResult orphan : orphans )
            {
                if ( updatedIds.contains( orphan.getId() ) )
                    continue;
                orphan.setState( CANCELLED );
                orphan.setError( "Build appears to have been orphaned, final status is unknown." );
                orphan.setEndTime( System.currentTimeMillis() );
                updateObject( orphan );
                updatedIds.add( orphan.getId() );
            }
            tx.commit();
            return updatedIds;
        }
        finally
        {
            rollback( tx );
        }
    }

    public BuildResult getPreviousBuildResultInSuccess( int projectId, int buildResultId )
        throws ContinuumStoreException
    {
        try
        {
            return getBuildResult( getPreviousBuildResultIdInSuccess( projectId, buildResultId ) );
        }
        catch ( NullPointerException e )
        {
            return null;
        }
    }

    public Map<Integer, BuildResult> getBuildResultsInSuccessByProjectGroupId( int projectGroupId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            String filter = "this.project.buildNumber == this.buildNumber";

            if ( projectGroupId > 0 )
            {
                query.declareParameters( "int projectGroupId" );
                filter += " && this.project.projectGroup.id == projectGroupId";
            }

            query.setFilter( filter );

            List<BuildResult> result;

            if ( projectGroupId > 0 )
            {
                result = (List<BuildResult>) query.execute( projectGroupId );
            }
            else
            {
                result = (List<BuildResult>) query.execute();
            }

            result = (List<BuildResult>) pm.detachCopyAll( result );

            tx.commit();

            if ( result != null && !result.isEmpty() )
            {
                Map<Integer, BuildResult> builds = new HashMap<Integer, BuildResult>();

                for ( BuildResult br : result )
                {
                    builds.put( br.getProject().getId(), br );
                }

                return builds;
            }
        }
        finally
        {
            rollback( tx );
        }

        return null;
    }

    @SuppressWarnings( "unchecked" )
    public List<BuildResult> getBuildResultsInRange( Date fromDate, Date toDate, int state, String triggeredBy,
                                                     int projectGroupId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            pm.getFetchPlan().addGroup( BUILD_RESULT_WITH_DETAILS_FETCH_GROUP );

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            String parameters = "";
            String filter = "";

            Map params = new HashMap();

            int ctr = 0;

            if ( state > 0 )
            {
                params.put( "state", state );
                ctr++;
                parameters += "int state, ";
                filter += "this.state == state && ";
            }

            if ( projectGroupId > 0 )
            {
                params.put( "projectGroupId", projectGroupId );
                ctr++;
                parameters += "int projectGroupId, ";
                filter += "this.project.projectGroup.id == projectGroupId && ";
            }

            if ( triggeredBy != null && !triggeredBy.equals( "" ) )
            {
                params.put( "triggeredBy", triggeredBy );
                ctr++;
                query.declareImports( "import java.lang.String" );
                parameters += "String triggeredBy, ";
                filter += "this.username == triggeredBy && ";
            }

            if ( fromDate != null )
            {
                params.put( "fromDate", fromDate.getTime() );
                ctr++;
                parameters += "long fromDate, ";
                filter += "this.startTime >= fromDate && ";
            }

            if ( toDate != null )
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime( toDate );
                cal.add( Calendar.DAY_OF_MONTH, 1 );

                params.put( "toDate", cal.getTimeInMillis() );
                ctr++;
                parameters += "long toDate";
                filter += "this.startTime < toDate";
            }

            if ( filter.endsWith( "&& " ) )
            {
                filter = filter.substring( 0, filter.length() - 3 );
                parameters = parameters.substring( 0, parameters.length() - 2 );
            }

            query.declareParameters( parameters );
            query.setFilter( filter );

            List<BuildResult> result = (List<BuildResult>) query.executeWithMap( params );

            result = (List<BuildResult>) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }
}
