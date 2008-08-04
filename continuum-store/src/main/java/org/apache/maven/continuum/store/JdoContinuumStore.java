package org.apache.maven.continuum.store;

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

import org.apache.continuum.dao.AbstractDao;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
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
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.jdo.PlexusJdoUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.store.ContinuumStore"
 * role-hint="jdo"
 */
public class JdoContinuumStore
    extends AbstractDao
    implements ContinuumStore
{
    private static Logger log = LoggerFactory.getLogger( JdoContinuumStore.class );

    // ----------------------------------------------------------------------
    // ContinuumStore Implementation
    // ----------------------------------------------------------------------

    private List<BuildDefinition> getDefaultBuildDefinitionsForProjectGroup( int projectGroupId )
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup =
            (ProjectGroup) getObjectById( ProjectGroup.class, projectGroupId, PROJECT_BUILD_DETAILS_FETCH_GROUP );

        List<BuildDefinition> bds = new ArrayList<BuildDefinition>();

        for ( Iterator i = projectGroup.getBuildDefinitions().iterator(); i.hasNext(); )
        {
            BuildDefinition bd = (BuildDefinition) i.next();

            // also applies to project group membership
            if ( bd.isDefaultForProject() )
            {
                bds.add( bd );
            }
        }

        return bds;
/*        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ProjectGroup.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import " + BuildDefinition.class.getName() );

            query.declareParameters( "int projectGroupId" );

            query.setFilter(
                "this.id == projectGroupId && this.buildDefinitions.contains(buildDef) && buildDef.defaultForProject == true" );

            query.declareVariables( "BuildDefinition buildDef" );

            query.setResult( "buildDef" );

            List<BuildDefinition> result = (List<BuildDefinition>) query.execute( projectGroupId );

            pm.detachCopyAll( result );

            tx.commit();

            if ( result != null )
            {
                return result;
            }
        }
        finally
        {
            rollback( tx );
        }

        return new ArrayList<BuildDefinition>();*/
    }

    private BuildDefinition getDefaultBuildDefinitionForProject( int projectId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildDefinition.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import " + Project.class.getName() );

            query.declareParameters( "int projectId" );

            query.setFilter(
                "project.id == projectId && project.buildDefinitions.contains(this) && this.defaultForProject == true" );

            query.declareVariables( "Project project" );

            query.setResult( "this" );

            query.setUnique( true );

            BuildDefinition result = (BuildDefinition) query.execute( projectId );

            pm.detachCopy( result );

            tx.commit();

            if ( result != null )
            {
                return result;
            }
        }
        finally
        {
            rollback( tx );
        }

        throw new ContinuumObjectNotFoundException( "no default build definition declared for project " + projectId );
    }

    /**
     * get the combined list of projectId and build definitions, including the
     * ones inherited by their project group
     *
     * @param scheduleId
     * @return
     * @throws ContinuumStoreException
     */
    public Map getAggregatedProjectIdsAndBuildDefinitionIdsBySchedule( int scheduleId )
        throws ContinuumStoreException
    {
        Map projectSource = getProjectIdsAndBuildDefinitionsIdsBySchedule( scheduleId );
        Map projectGroupSource = getProjectGroupIdsAndBuildDefinitionsIdsBySchedule( scheduleId );

        Map aggregate = new HashMap();

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
            for ( Iterator i = projectGroupSource.keySet().iterator(); i.hasNext(); )
            {
                Integer projectGroupId = (Integer) i.next();
                List projectsInGroup = getProjectsInGroup( projectGroupId.intValue() );

                for ( Iterator j = projectsInGroup.iterator(); j.hasNext(); )
                {
                    Integer projectId = new Integer( ( (Project) j.next() ).getId() );
                    if ( !aggregate.keySet().contains( projectId ) )
                    {
                        aggregate.put( projectId, projectGroupSource.get( projectGroupId ) );
                    }
                }
            }
        }
        return aggregate;
    }

    public Map getProjectIdsAndBuildDefinitionsIdsBySchedule( int scheduleId )
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

            List result = (List) query.execute( new Integer( scheduleId ) );

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

    public Map getProjectGroupIdsAndBuildDefinitionsIdsBySchedule( int scheduleId )
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

            pm.makePersistent( project );

            tx.commit();
        }
        finally
        {
            rollback( tx );
        }
    }

    public void addBuildResult( Project project, BuildResult build )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            pm.getFetchPlan().addGroup( PROJECT_WITH_BUILDS_FETCH_GROUP );

            Object objectId = pm.newObjectIdInstance( Project.class, project.getId() );

            project = (Project) pm.getObjectById( objectId );

            build = (BuildResult) makePersistent( pm, build, false );

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
                return (BuildResult) result.get( 0 );
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
                return (BuildResult) result.get( 0 );
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

    public Map<Integer, BuildResult> getLatestBuildResults()
    {
        return getLatestBuildResultsByProjectGroupId( -1 );
    }

    public void removeBuildResult( BuildResult buildResult )
    {
        removeObject( buildResult );
    }

    // ------------------------------------------------------
    //  BuildDefinition
    // ------------------------------------------------------    

    public BuildDefinition getDefaultBuildDefinition( int projectId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        BuildDefinition bd = null;

        try
        {
            bd = getDefaultBuildDefinitionForProject( projectId );
        }
        catch ( ContinuumObjectNotFoundException cne )
        {
            // ignore since we will try the project group
            log.debug( "no default build definition on project, trying project group" );
        }

        // project group should have default build definition defined
        if ( bd == null )
        {
            ProjectGroup projectGroup = getProjectGroupByProjectId( projectId );

            Project p = getProject( projectId );

            List<BuildDefinition> bds = getDefaultBuildDefinitionsForProjectGroup( projectGroup.getId() );

            for ( BuildDefinition bdef : bds )
            {
                if ( p.getExecutorId().equals( bdef.getType() ) || ( StringUtils.isEmpty( bdef.getType() ) &&
                    ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR.equals( p.getExecutorId() ) ) )
                {
                    return bdef;
                }
            }
        }

        return bd;
    }

    private ProjectGroup getProjectGroupByProjectId( int projectId )
        throws ContinuumObjectNotFoundException
    {
        try
        {
            return getProject( projectId ).getProjectGroup();
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumObjectNotFoundException(
                "unable to find project group containing project with id: " + projectId );

        }
    }

    public Map getDefaultBuildDefinitions()
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( Project.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import org.apache.maven.continuum.model.project.BuildDefinition" );

            query.setFilter( "this.buildDefinitions.contains(buildDef) && buildDef.defaultForProject == true" );

            query.declareVariables( "BuildDefinition buildDef" );

            query.setResult( "this.id, buildDef.id" );

            List result = (List) query.execute();

            // result = (List) pm.detachCopyAll( result );

            Map builds = new HashMap();

            if ( result != null && !result.isEmpty() )
            {
                for ( Object aResult : result )
                {
                    Object[] obj = (Object[]) aResult;

                    builds.put( (Integer) obj[0], (Integer) obj[1] );
                }

                return builds;
            }
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }

        return null;
    }


    public List<BuildDefinitionTemplate> getContinuumBuildDefinitionTemplates()
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildDefinitionTemplate.class, true );

            Query query = pm.newQuery( extent );
            query.setFilter( "continuumDefault == true" );
            pm.getFetchPlan().addGroup( BUILD_TEMPLATE_BUILD_DEFINITIONS );
            List result = (List) query.execute();
            return result == null ? Collections.EMPTY_LIST : (List) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }


    public BuildDefinitionTemplate getContinuumBuildDefinitionTemplateWithType( String type )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildDefinitionTemplate.class, true );

            Query query = pm.newQuery( extent );
            query.declareImports( "import java.lang.String" );
            query.declareParameters( "String type" );
            query.setFilter( "continuumDefault == true && this.type == type" );
            pm.getFetchPlan().addGroup( BUILD_TEMPLATE_BUILD_DEFINITIONS );
            List result = (List) query.execute( type );
            if ( result == null || result.isEmpty() )
            {
                return null;
            }
            return (BuildDefinitionTemplate) pm.detachCopy( result.get( 0 ) );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }

    // ------------------------------------------------------
    //  BuildDefinitionTemplate
    // ------------------------------------------------------      


    public BuildDefinitionTemplate addBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws ContinuumStoreException
    {
        return (BuildDefinitionTemplate) addObject( buildDefinitionTemplate );
    }

    public List<BuildDefinitionTemplate> getAllBuildDefinitionTemplate()
        throws ContinuumStoreException
    {
        return getAllObjectsDetached( BuildDefinitionTemplate.class, BUILD_TEMPLATE_BUILD_DEFINITIONS );
    }

    public BuildDefinitionTemplate getBuildDefinitionTemplate( int id )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        return (BuildDefinitionTemplate) getObjectById( BuildDefinitionTemplate.class, id,
                                                        BUILD_TEMPLATE_BUILD_DEFINITIONS );
    }

    public void removeBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws ContinuumStoreException
    {
        removeObject( buildDefinitionTemplate );
    }

    public BuildDefinitionTemplate updateBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws ContinuumStoreException
    {
        updateObject( buildDefinitionTemplate );

        return buildDefinitionTemplate;
    }

    public List<BuildDefinitionTemplate> getContinuumDefaultdDefinitions()
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildDefinitionTemplate.class, true );

            Query query = pm.newQuery( extent );
            query.setFilter( "continuumDefault == true" );

            List result = (List) query.execute();

            return result == null ? Collections.EMPTY_LIST : (List) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }

    public List<BuildDefinitionTemplate> getBuildDefinitionTemplatesWithType( String type )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildDefinitionTemplate.class, true );

            Query query = pm.newQuery( extent );
            query.declareImports( "import java.lang.String" );
            query.declareParameters( "String type" );
            query.setFilter( "this.type == type" );
            pm.getFetchPlan().addGroup( BUILD_TEMPLATE_BUILD_DEFINITIONS );
            List result = (List) query.execute( type );
            return result == null ? Collections.EMPTY_LIST : (List) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }


    private Object makePersistent( PersistenceManager pm, Object object, boolean detach )
    {
        return PlexusJdoUtils.makePersistent( pm, object, detach );
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

            List result = (List) query.execute( projectId );

            result = (List) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public Project getProject( int projectId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        return (Project) getObjectById( Project.class, projectId );
    }

    public BuildResult getBuildResult( int buildId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (BuildResult) getObjectById( BuildResult.class, buildId, BUILD_RESULT_WITH_DETAILS_FETCH_GROUP );
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

            List result = (List) query.execute( projectId, buildNumber );

            result = (List) pm.detachCopyAll( result );

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

    public List<BuildResult> getBuildResultsForProject( int projectId )
    {
        return getBuildResultsForProject( projectId, -1, -1 );
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

            List result = (List) query.execute( projectId );

            result = (List) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public List<BuildResult> getBuildResultsForProject( int projectId, long fromDate )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        pm.getFetchPlan().addGroup( BUILD_RESULT_WITH_DETAILS_FETCH_GROUP );

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "int projectId, long fromDate" );

            query.setFilter( "this.project.id == projectId && this.startTime > fromDate" );

            List result = (List) query.execute( projectId, fromDate );

            result = (List) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public List<BuildResult> getBuildResultsInSuccessForProject( int projectId, long fromDate )
    {
        List<BuildResult> buildResults = getBuildResultsForProject( projectId, fromDate );

        List<BuildResult> results = new ArrayList<BuildResult>();

        if ( buildResults != null )
        {
            for ( BuildResult res : buildResults )
            {
                if ( res.getState() == ContinuumProjectState.OK )
                {
                    results.add( res );
                }
            }
        }
        return results;
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
                result = (List) query.execute( projectGroupId );
            }
            else
            {
                result = (List) query.execute();
            }

            result = (List) pm.detachCopyAll( result );

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

    public Map<Integer, BuildResult> getBuildResultsInSuccess()
    {
        return getBuildResultsInSuccessByProjectGroupId( -1 );
    }

    public void removeProject( Project project )
    {
        removeObject( project );
    }

    public List<Project> getProjectsInGroup( int projectGroupId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( Project.class, true );

            Query query = pm.newQuery( extent, "projectGroup.id == " + projectGroupId );

            query.setOrdering( "name ascending" );

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

    private List getAllObjectsDetached( Class clazz, String fetchGroup )
    {
        return getAllObjectsDetached( clazz, null, fetchGroup );
    }

    public Project getProjectWithBuildDetails( int projectId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (Project) getObjectById( Project.class, projectId, PROJECT_BUILD_DETAILS_FETCH_GROUP );
    }

    public void closeStore()
    {
        closePersistenceManagerFactory( getContinuumPersistenceManagerFactory(), 1 );
    }

    public void eraseDatabase()
    {
        PlexusJdoUtils.removeAll( getPersistenceManager(), BuildDefinitionTemplate.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), BuildResult.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ProjectGroup.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), Project.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), BuildDefinition.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), Schedule.class );
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
                catch ( SecurityException e )
                {
                    throw e;
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
}
