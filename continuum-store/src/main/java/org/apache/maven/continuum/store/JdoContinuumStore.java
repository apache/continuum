package org.apache.maven.continuum.store;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.continuum.key.GroupProjectKey;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Profile;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.model.scm.SuiteResult;
import org.apache.maven.continuum.model.scm.TestCaseFailure;
import org.apache.maven.continuum.model.scm.TestResult;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.jdo.PlexusJdoUtils;
import org.codehaus.plexus.jdo.PlexusObjectNotFoundException;
import org.codehaus.plexus.jdo.PlexusStoreException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.store.ContinuumStore" role-hint="jdo"
 */
public class JdoContinuumStore
    extends AbstractContinuumStore
    implements ContinuumStore, Initializable
{

    /**
     * Column identifier under which the {@link ProjectGroup} key values are stored.
     * <p>
     * @see Modello configuration for continuum-model. 
     */
    private static final String LOOKUP_KEY_GROUP_KEY = "groupKey";

    /**
     * Column identifier under which the {@link Project} key values are stored.
     * <p>
     * @see Modello configuration for continuum-model. 
     */
    private static final String LOOKUP_KEY_PROJECT_KEY = "projectKey";

    /**
     * @plexus.requirement role-hint="continuum"
     */
    private JdoFactory continuumJdoFactory;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private PersistenceManagerFactory continuumPmf;

    // ----------------------------------------------------------------------
    // Fetch Groups
    // ----------------------------------------------------------------------

    private static final String PROJECT_WITH_BUILDS_FETCH_GROUP = "project-with-builds";

    private static final String PROJECT_WITH_CHECKOUT_RESULT_FETCH_GROUP = "project-with-checkout-result";

    private static final String BUILD_RESULT_WITH_DETAILS_FETCH_GROUP = "build-result-with-details";

    private static final String PROJECT_BUILD_DETAILS_FETCH_GROUP = "project-build-details";

    private static final String PROJECT_ALL_DETAILS_FETCH_GROUP = "project-all-details";

    private static final String PROJECT_DEPENDENCIES_FETCH_GROUP = "project-dependencies";

    private static final String PROJECTGROUP_PROJECTS_FETCH_GROUP = "projectgroup-projects";

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
        throws InitializationException
    {
        continuumPmf = continuumJdoFactory.getPersistenceManagerFactory();
    }

    // ----------------------------------------------------------------------
    // ContinuumStore Implementation
    // ----------------------------------------------------------------------

    public Project getProjectByName( String name )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( Project.class, true );

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

            return (Project) object;
        }
        finally
        {
            rollback( tx );
        }
    }

    public Project getProject( String groupId, String artifactId, String version )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( Project.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String groupId, String artifactId, String version" );

            query.setFilter( "this.groupId == groupId && this.artifactId == artifactId && this.version == version" );

            Object[] params = new Object[3];
            params[0] = groupId;
            params[1] = artifactId;
            params[2] = version;

            Collection result = (Collection) query.executeWithArray( params );

            if ( result.size() == 0 )
            {
                tx.commit();

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return (Project) object;
        }
        finally
        {
            rollback( tx );
        }
    }

    /**
     * get the combined list of projectId and build definitions, including the ones
     * inherited by their project group
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

        // iterate through the project groups and make sure we are not walking over projects that
        // might define their own build definitions
        if ( projectGroupSource != null )
        {
            for ( Iterator i = projectGroupSource.keySet().iterator(); i.hasNext(); )
            {
                Integer projectGroupId = (Integer) i.next();
                // FIXME: Need to check with Jesse and refactor this to use GroupProjectKey                
                //List projectsInGroup = getProjectsInGroup( projectGroupId.intValue() );
                List projectsInGroup = getProjectsInGroup( null );

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

            List result = (List) query.execute( new Integer( scheduleId ) );

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

            Object objectId = pm.newObjectIdInstance( Project.class, new Integer( project.getId() ) );

            project = (Project) pm.getObjectById( objectId );

            build = (BuildResult) makePersistent( pm, build, false );

            // TODO: these are in the wrong spot - set them on success (though currently some depend on latest build being the one in progress)
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

    public BuildResult getLatestBuildResultForProject( GroupProjectKey groupProjectKey )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            //query.declareParameters( "int projectId" );
            query.declareParameters( "String projectKey" );

            //query.setFilter( "this.project.id == projectId && this.project.latestBuildId == this.id" );
            query.setFilter( "this.project.key == projectKey && this.project.latestBuildId == this.id" );

            List result = (List) query.execute( groupProjectKey.getProjectKey() );

            result = (List) pm.detachCopyAll( result );

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

    public Map getLatestBuildResults()
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.setFilter( "this.project.latestBuildId == this.id" );

            List result = (List) query.execute();

            result = (List) pm.detachCopyAll( result );

            tx.commit();

            if ( result != null && !result.isEmpty() )
            {
                Map builds = new HashMap();

                for ( Iterator i = result.iterator(); i.hasNext(); )
                {
                    BuildResult br = (BuildResult) i.next();

                    builds.put( new Integer( br.getProject().getId() ), br );
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

    public void removeNotifier( ProjectNotifier notifier )
        throws ContinuumStoreException
    {
        attachAndDelete( notifier );
    }

    public ProjectNotifier storeNotifier( ProjectNotifier notifier )
        throws ContinuumStoreException
    {
        updateObject( notifier );

        return notifier;
    }

    public BuildDefinition getDefaultBuildDefinition( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {

        BuildDefinition bd = null;

        try
        {
            bd = getDefaultBuildDefinitionForProject( groupProjectKey );
        }
        catch ( ContinuumObjectNotFoundException cne )
        {
            // ignore since we will try the project group
            getLogger().debug( "no default build definition on project, trying project group" );
        }

        //project group should have default build definition defined
        if ( bd == null )
        {
            //ProjectGroup projectGroup = getProjectGroupByProjectId( groupProjectKey );

            //bd = getDefaultBuildDefinitionForProjectGroup( projectGroup.getId() );
            bd = getDefaultBuildDefinitionForProjectGroup( groupProjectKey );
        }

        return bd;
    }

    public BuildDefinition getDefaultBuildDefinitionForProject( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        Project project;

        try
        {
            project = getProjectWithBuildDetails( groupProjectKey );
        }
        catch ( Exception e )
        {
            project = null;
        }

        // check if the project has a default build definition defined
        if ( project != null && project.getBuildDefinitions() != null )
        {
            for ( Iterator i = project.getBuildDefinitions().iterator(); i.hasNext(); )
            {
                BuildDefinition bd = (BuildDefinition) i.next();

                if ( bd.isDefaultForProject() )
                {
                    return bd;
                }
            }
        }

        throw new ContinuumObjectNotFoundException( "no default build definition declared for project "
            + groupProjectKey );
    }

    public BuildDefinition getDefaultBuildDefinitionForProjectGroup( String groupKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        GroupProjectKey key = new GroupProjectKey();
        key.setGroupKey( groupKey );
        ProjectGroup projectGroup = getProjectGroupWithBuildDetails( key );

        for ( Iterator i = projectGroup.getBuildDefinitions().iterator(); i.hasNext(); )
        {
            BuildDefinition bd = (BuildDefinition) i.next();

            // also applies to project group membership
            if ( bd.isDefaultForProject() )
            {
                return bd;
            }
        }
        return null;
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

            //result = (List) pm.detachCopyAll( result );

            Map builds = new HashMap();

            if ( result != null && !result.isEmpty() )
            {
                for ( Iterator i = result.iterator(); i.hasNext(); )
                {
                    Object[] obj = (Object[]) i.next();

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

    public BuildDefinition getBuildDefinition( int buildDefinitionId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        return (BuildDefinition) getObjectById( BuildDefinition.class, buildDefinitionId );
    }

    public void removeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException
    {
        attachAndDelete( buildDefinition );
    }

    public BuildDefinition storeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException
    {
        updateObject( buildDefinition );

        return buildDefinition;
    }

    private Object makePersistent( PersistenceManager pm, Object object, boolean detach )
    {
        return PlexusJdoUtils.makePersistent( pm, object, detach );
    }

    /**
     * Looks up and returns an Entity instance from the underlying store given 
     * the String key and the field name to match it against.
     * 
     * @param clazz Expected {@link Class} of the entity being looked up. 
     * @param idField Column identifier/name for the field in the underlying 
     *          store to match the String identifier against.
     * @param id Identifier value to match in the Id field.
     * @param fetchGroup TODO: Document! What is a fetchGroup?
     * @return Entity instance that matches the lookup criteria as specified by
     *          the passed in Id.
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException if there was no instance that
     *          matched the criteria in the underlying store. 
     */
    private Object getObjectFromQuery( Class clazz, String idField, String id, String fetchGroup )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        try
        {
            return PlexusJdoUtils.getObjectFromQuery( getPersistenceManager(), clazz, idField, id, fetchGroup );
        }
        catch ( PlexusObjectNotFoundException e )
        {
            throw new ContinuumObjectNotFoundException( e.getMessage() );
        }
        catch ( PlexusStoreException e )
        {
            throw new ContinuumStoreException( e.getMessage(), e );
        }
    }

    private void attachAndDelete( Object object )
    {
        PlexusJdoUtils.attachAndDelete( getPersistenceManager(), object );
    }

    // ----------------------------------------------------------------------
    // Transaction Management
    // ----------------------------------------------------------------------

    private void rollback( Transaction tx )
    {
        PlexusJdoUtils.rollbackIfActive( tx );
    }

    public ProjectGroup getProjectGroup( String groupKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        //return (ProjectGroup) getObjectById( ProjectGroup.class, groupKey );        
        return (ProjectGroup) getObjectFromQuery( ProjectGroup.class, LOOKUP_KEY_GROUP_KEY, groupKey, null );
    }

    private Object getObjectById( Class clazz, int id )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        return getObjectById( clazz, id, null );
    }

    private Object getObjectById( Class clazz, int id, String fetchGroup )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        try
        {
            return PlexusJdoUtils.getObjectById( getPersistenceManager(), clazz, id, fetchGroup );
        }
        catch ( PlexusObjectNotFoundException e )
        {
            throw new ContinuumObjectNotFoundException( e.getMessage() );
        }
        catch ( PlexusStoreException e )
        {
            throw new ContinuumStoreException( e.getMessage(), e );
        }
    }

    public void updateProjectGroup( ProjectGroup group )
        throws ContinuumStoreException
    {
        updateObject( group );
    }

    private void updateObject( Object object )
        throws ContinuumStoreException
    {
        updateObject( getPersistenceManager(), object );
    }

    private void updateObject( PersistenceManager pmf, Object object )
        throws ContinuumStoreException
    {
        try
        {
            PlexusJdoUtils.updateObject( pmf, object );
        }
        catch ( PlexusStoreException e )
        {
            throw new ContinuumStoreException( e.getMessage(), e );
        }
    }

    public Collection getAllProjectGroupsWithProjects()
    {
        return getAllObjectsDetached( ProjectGroup.class, "name ascending", PROJECTGROUP_PROJECTS_FETCH_GROUP );
    }

    public Collection getAllProjectGroups()
    {
        return getAllObjectsDetached( ProjectGroup.class, "name ascending", null );
    }

    public List getAllProjectsByName()
    {
        return getAllObjectsDetached( Project.class, "name ascending", null );
    }

    // todo get this natively supported in the store
    public List getProjectsWithDependenciesByGroupId( String groupKey )
    {
        List allProjects = getAllObjectsDetached( Project.class, "name ascending", PROJECT_DEPENDENCIES_FETCH_GROUP );

        List groupProjects = new ArrayList();

        for ( Iterator i = allProjects.iterator(); i.hasNext(); )
        {
            Project project = (Project) i.next();

            if ( project.getProjectGroup().getKey().equals( groupKey ) )
            {
                groupProjects.add( project );
            }
        }

        return groupProjects;
    }

    public List getAllProjectsByNameWithDependencies()
    {
        return getAllObjectsDetached( Project.class, "name ascending", PROJECT_DEPENDENCIES_FETCH_GROUP );
    }

    public List getAllProjectsByNameWithBuildDetails()
    {
        return getAllObjectsDetached( Project.class, "name ascending", PROJECT_BUILD_DETAILS_FETCH_GROUP );
    }

    public List getAllSchedulesByName()
    {
        return getAllObjectsDetached( Schedule.class, "name ascending", null );
    }

    public Schedule addSchedule( Schedule schedule )
    {
        return (Schedule) addObject( schedule );
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

    public List getAllProfilesByName()
    {
        return getAllObjectsDetached( Profile.class, "name ascending", null );
    }

    public Profile addProfile( Profile profile )
    {
        return (Profile) addObject( profile );
    }

    public Installation addInstallation( Installation installation )
    {
        return (Installation) addObject( installation );
    }

    public List getAllInstallations()
    {
        return getAllObjectsDetached( Installation.class, "name ascending, version ascending", null );
    }

    public List getAllBuildsForAProjectByDate( GroupProjectKey groupProjectKey )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Query query = pm.newQuery( "SELECT FROM " + BuildResult.class.getName()
                + " WHERE project.key == projectKey PARAMETERS String projectKey ORDER BY endTime DESC" );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String projectKey" );

            List result = (List) query.execute( groupProjectKey.getProjectKey() );

            result = (List) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public Project getProject( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        //return (Project) getObjectById( Project.class, groupProjectKey );  
        return (Project) getObjectFromQuery( Project.class, LOOKUP_KEY_PROJECT_KEY, groupProjectKey.getProjectKey(),
                                             null );
    }

    public void updateProject( Project project )
        throws ContinuumStoreException
    {
        updateObject( project );
    }

    public void updateProfile( Profile profile )
        throws ContinuumStoreException
    {
        updateObject( profile );
    }

    public void updateSchedule( Schedule schedule )
        throws ContinuumStoreException
    {
        updateObject( schedule );
    }

    public Project getProjectWithBuilds( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        //return (Project) getObjectById( Project.class, groupProjectKey, PROJECT_WITH_BUILDS_FETCH_GROUP );
        return (Project) getObjectFromQuery( Project.class, LOOKUP_KEY_PROJECT_KEY, groupProjectKey.getProjectKey(),
                                             PROJECT_WITH_BUILDS_FETCH_GROUP );
    }

    public void removeProfile( Profile profile )
    {
        removeObject( profile );
    }

    public void removeSchedule( Schedule schedule )
    {
        removeObject( schedule );
    }

    public Project getProjectWithCheckoutResult( GroupProjectKey groupProjectKey )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        // return (Project) getObjectById( Project.class, groupProjectkey, PROJECT_WITH_CHECKOUT_RESULT_FETCH_GROUP );
        return (Project) getObjectFromQuery( Project.class, LOOKUP_KEY_PROJECT_KEY, groupProjectKey.getProjectKey(),
                                             PROJECT_BUILD_DETAILS_FETCH_GROUP );
    }

    public BuildResult getBuildResult( int buildId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (BuildResult) getObjectById( BuildResult.class, buildId, BUILD_RESULT_WITH_DETAILS_FETCH_GROUP );
    }

    public List getBuildResultByBuildNumber( GroupProjectKey groupProjectKey, int buildNumber )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "String  projectKey, int buildNumber" );

            query.setFilter( "this.project.key == projectKey && this.buildNumber == buildNumber" );

            List result = (List) query.execute( groupProjectKey.getProjectKey(), new Integer( buildNumber ) );

            result = (List) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public List getBuildResultsForProject( GroupProjectKey groupProjectKey, long fromDate )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        pm.getFetchPlan().addGroup( BUILD_RESULT_WITH_DETAILS_FETCH_GROUP );

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.declareParameters( "String projectKey, long fromDate" );

            query.setFilter( "this.project.key == projectKey && this.startTime > fromDate" );

            List result = (List) query.execute( groupProjectKey, new Long( fromDate ) );

            result = (List) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public List getBuildResultsInSuccessForProject( GroupProjectKey groupProjectkey, long fromDate )
    {
        List buildResults = getBuildResultsForProject( groupProjectkey, fromDate );

        List results = new ArrayList();

        if ( buildResults != null )
        {
            for ( Iterator i = buildResults.iterator(); i.hasNext(); )
            {
                BuildResult res = (BuildResult) i.next();
                if ( res.getState() == ContinuumProjectState.OK )
                {
                    results.add( res );
                }
            }
        }

        return results;
    }

    public Map getBuildResultsInSuccess()
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildResult.class, true );

            Query query = pm.newQuery( extent );

            query.setFilter( "this.project.buildNumber == this.buildNumber" );

            List result = (List) query.execute();

            result = (List) pm.detachCopyAll( result );

            tx.commit();

            if ( result != null && !result.isEmpty() )
            {
                Map builds = new HashMap();

                for ( Iterator i = result.iterator(); i.hasNext(); )
                {
                    BuildResult br = (BuildResult) i.next();

                    builds.put( new Integer( br.getProject().getId() ), br );
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

    public void removeProject( Project project )
    {
        removeObject( project );
    }

    public void removeProjectGroup( ProjectGroup projectGroup )
    {
        ProjectGroup pg = null;
        try
        {
            GroupProjectKey key = new GroupProjectKey();
            key.setGroupKey( projectGroup.getKey() );
            pg = getProjectGroupWithProjects( key );
        }
        catch ( Exception e )
        {
            //Do nothing
        }

        if ( pg != null )
        {
            // TODO: why do we need to do this? if not - build results are not removed and a integrity constraint is violated. I assume its because of the fetch groups
            for ( Iterator i = pg.getProjects().iterator(); i.hasNext(); )
            {
                removeProject( (Project) i.next() );
            }
            removeObject( pg );
        }
    }

    public List getProjectsInGroup( GroupProjectKey groupProjectkey )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( Project.class, true );

            Query query = pm.newQuery( extent, "projectGroup.id == " + groupProjectkey );

            query.setOrdering( "name ascending" );

            pm.getFetchPlan().addGroup( PROJECTGROUP_PROJECTS_FETCH_GROUP );

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

    public ProjectGroup getProjectGroupWithProjects( GroupProjectKey groupProjectKey )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (ProjectGroup) getObjectFromQuery( ProjectGroup.class, LOOKUP_KEY_GROUP_KEY, groupProjectKey
            .getGroupKey(), PROJECTGROUP_PROJECTS_FETCH_GROUP );
    }

    public ProjectGroup getProjectGroupWithBuildDetails( GroupProjectKey groupProjectKey )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (ProjectGroup) getObjectFromQuery( ProjectGroup.class, LOOKUP_KEY_GROUP_KEY, groupProjectKey
            .getGroupKey(), PROJECT_BUILD_DETAILS_FETCH_GROUP );
    }

    public List getAllProjectGroupsWithBuildDetails()
    {
        return getAllObjectsDetached( ProjectGroup.class, "name ascending", PROJECT_BUILD_DETAILS_FETCH_GROUP );
    }

    public List getAllProjectsWithAllDetails()
    {
        return getAllObjectsDetached( Project.class, "name ascending", PROJECT_ALL_DETAILS_FETCH_GROUP );
    }

    public Project getProjectWithAllDetails( GroupProjectKey groupProjectKey )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (Project) getObjectFromQuery( Project.class, LOOKUP_KEY_PROJECT_KEY, groupProjectKey.getProjectKey(),
                                             PROJECT_ALL_DETAILS_FETCH_GROUP );
    }

    public Schedule getSchedule( int scheduleId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (Schedule) getObjectById( Schedule.class, scheduleId );
    }

    public Profile getProfile( int profileId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (Profile) getObjectById( Profile.class, profileId );
    }

    private void removeObject( Object o )
    {
        PlexusJdoUtils.removeObject( getPersistenceManager(), o );
    }

    private List getAllObjectsDetached( Class clazz )
    {
        return getAllObjectsDetached( clazz, null );
    }

    private List getAllObjectsDetached( Class clazz, String fetchGroup )
    {
        return getAllObjectsDetached( clazz, null, fetchGroup );
    }

    private List getAllObjectsDetached( Class clazz, String ordering, String fetchGroup )
    {
        return getAllObjectsDetached( getPersistenceManager(), clazz, ordering, fetchGroup );
    }

    private List getAllObjectsDetached( PersistenceManager pmf, Class clazz )
    {
        return getAllObjectsDetached( pmf, clazz, null );
    }

    private List getAllObjectsDetached( PersistenceManager pmf, Class clazz, String fetchGroup )
    {
        return getAllObjectsDetached( pmf, clazz, null, fetchGroup );
    }

    private List getAllObjectsDetached( PersistenceManager pmf, Class clazz, String ordering, String fetchGroup )
    {
        return PlexusJdoUtils.getAllObjectsDetached( pmf, clazz, ordering, fetchGroup );
    }

    public ProjectGroup addProjectGroup( ProjectGroup group )
    {
        return (ProjectGroup) addObject( group );
    }

    private Object addObject( Object object )
    {
        return addObject( getPersistenceManager(), object );
    }

    private Object addObject( PersistenceManager pmf, Object object )
    {
        return PlexusJdoUtils.addObject( pmf, object );
    }

    public ProjectGroup getProjectGroupByGroupId( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        //return (ProjectGroup) getObjectFromQuery( ProjectGroup.class, "groupId", groupProjectKey, null );
        return (ProjectGroup) getObjectFromQuery( ProjectGroup.class, LOOKUP_KEY_GROUP_KEY, groupProjectKey
            .getGroupKey(), PROJECT_BUILD_DETAILS_FETCH_GROUP );
    }

    public ProjectGroup getProjectGroupByGroupIdWithBuildDetails( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        //return (ProjectGroup) getObjectFromQuery( ProjectGroup.class, "groupId", groupProjectKey,
        //                                          PROJECT_BUILD_DETAILS_FETCH_GROUP );
        return (ProjectGroup) getObjectFromQuery( ProjectGroup.class, LOOKUP_KEY_GROUP_KEY, groupProjectKey
            .getGroupKey(), PROJECT_BUILD_DETAILS_FETCH_GROUP );
    }

    public ProjectGroup getProjectGroupByGroupIdWithProjects( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        //return (ProjectGroup) getObjectFromQuery( ProjectGroup.class, "groupId", groupProjectKey,
        //                                          PROJECTGROUP_PROJECTS_FETCH_GROUP );
        return (ProjectGroup) getObjectFromQuery( ProjectGroup.class, LOOKUP_KEY_GROUP_KEY, groupProjectKey
            .getGroupKey(), PROJECTGROUP_PROJECTS_FETCH_GROUP );
    }

    public Project getProjectWithBuildDetails( GroupProjectKey groupProjectKey )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        //return (Project) getObjectById( Project.class, groupProjectKey, PROJECT_BUILD_DETAILS_FETCH_GROUP );
        return (Project) getObjectFromQuery( Project.class, LOOKUP_KEY_PROJECT_KEY, groupProjectKey.getProjectKey(),
                                             PROJECT_BUILD_DETAILS_FETCH_GROUP );
    }

    public ProjectGroup getProjectGroupByProjectId( GroupProjectKey groupProjectKey )
        throws ContinuumObjectNotFoundException
    {
        // todo this chunk should be optimized in the store by a good query
        Collection projectGroups = getAllProjectGroupsWithProjects();

        for ( Iterator i = projectGroups.iterator(); i.hasNext(); )
        {
            ProjectGroup projectGroup = (ProjectGroup) i.next();

            for ( Iterator j = projectGroup.getProjects().iterator(); j.hasNext(); )
            {
                Project project = (Project) j.next();

                if ( groupProjectKey.getProjectKey().equals( project.getKey() ) )
                {
                    return projectGroup;
                }
            }
        }

        throw new ContinuumObjectNotFoundException( "unable to find project group containing project with id: "
            + groupProjectKey );
    }

    public SystemConfiguration addSystemConfiguration( SystemConfiguration systemConf )
    {
        return (SystemConfiguration) addObject( systemConf );
    }

    public void updateSystemConfiguration( SystemConfiguration systemConf )
        throws ContinuumStoreException
    {
        updateObject( systemConf );
    }

    public SystemConfiguration getSystemConfiguration()
        throws ContinuumStoreException
    {
        List systemConfs = getAllObjectsDetached( SystemConfiguration.class );

        if ( systemConfs == null || systemConfs.isEmpty() )
        {
            return null;
        }
        else if ( systemConfs.size() > 1 )
        {
            throw new ContinuumStoreException(
                                               "Database is corrupted. There are more than one systemConfiguration object." );
        }
        else
        {
            return (SystemConfiguration) systemConfs.get( 0 );
        }
    }

    private PersistenceManager getPersistenceManager()
    {
        return getPersistenceManager( continuumPmf );
    }

    private PersistenceManager getPersistenceManager( PersistenceManagerFactory pmf )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        pm.getFetchPlan().setMaxFetchDepth( -1 );
        pm.getFetchPlan().setDetachmentOptions( FetchPlan.DETACH_LOAD_FIELDS );

        return pm;
    }

    public void closeStore()
    {
        closePersistenceManagerFactory( continuumPmf, 1 );
    }

    public Collection getAllProjectGroupsWithTheLot()
    {
        List fetchGroups = Arrays.asList( new String[] {
            PROJECT_WITH_BUILDS_FETCH_GROUP,
            PROJECTGROUP_PROJECTS_FETCH_GROUP,
            BUILD_RESULT_WITH_DETAILS_FETCH_GROUP,
            PROJECT_WITH_CHECKOUT_RESULT_FETCH_GROUP,
            PROJECT_ALL_DETAILS_FETCH_GROUP,
            PROJECT_BUILD_DETAILS_FETCH_GROUP } );
        return PlexusJdoUtils.getAllObjectsDetached( getPersistenceManager(), ProjectGroup.class, "name ascending",
                                                     fetchGroups );
    }

    public void eraseDatabase()
    {
        PlexusJdoUtils.removeAll( getPersistenceManager(), ProjectGroup.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), Project.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), Schedule.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), Profile.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), Installation.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ScmResult.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), BuildResult.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), TestResult.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), SuiteResult.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), TestCaseFailure.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), SystemConfiguration.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ProjectNotifier.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ProjectDeveloper.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ProjectDependency.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ChangeSet.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), ChangeFile.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), BuildDefinition.class );
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
                            //nothing to do
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

    // added after latest refactoring for keys

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ContinuumStore#getDefaultBuildDefinitionForProjectGroup(org.apache.maven.continuum.key.GroupProjectKey)
     */
    public BuildDefinition getDefaultBuildDefinitionForProjectGroup( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        // TODO Implement!
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ContinuumStore#getProjectGroup(org.apache.maven.continuum.key.GroupProjectKey)
     */
    public ProjectGroup getProjectGroup( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        // TODO Implement!
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ContinuumStore#getProjectsWithDependenciesByGroupId(org.apache.maven.continuum.key.GroupProjectKey)
     */
    public List getProjectsWithDependenciesByGroupId( GroupProjectKey groupProjectKey )
    {
        // TODO Implement!
        return null;
    }

}
