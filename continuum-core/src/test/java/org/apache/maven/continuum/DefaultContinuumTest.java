package org.apache.maven.continuum;

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

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.model.release.ContinuumReleaseResult;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.release.config.ContinuumReleaseDescriptor;
import org.apache.continuum.repository.RepositoryService;
import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.initialization.ContinuumInitializer;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.shared.release.ReleaseResult;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class DefaultContinuumTest
    extends AbstractContinuumTest
{
    private static final Logger log = LoggerFactory.getLogger( DefaultContinuumTest.class );

    private TaskQueueManager taskQueueManager;

    private ProjectDao projectDao;

    private BuildResultDao buildResultDao;

    @Before
    public void setUp()
        throws Exception
    {
        taskQueueManager = mock( TaskQueueManager.class );
        projectDao = mock( ProjectDao.class );
        buildResultDao = mock( BuildResultDao.class );
    }

    @Test
    public void testContinuumConfiguration()
        throws Exception
    {
        lookup( Continuum.ROLE );
    }

    @Test
    public void testAddMavenTwoProjectSet()
        throws Exception
    {
        Continuum continuum = lookup( Continuum.class );

        int projectCount = getProjectDao().getAllProjectsByName().size();

        int projectGroupCount = getProjectGroupDao().getAllProjectGroupsWithProjects().size();

        File rootPom = getTestFile( "src/test/resources/projects/continuum/continuum-notifiers/pom.xml" );

        assertTrue( rootPom.exists() );

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( rootPom.toURI().toURL().toExternalForm(),
                                                                              -1, true, false, true, -1, false );

        assertNotNull( result );

        assertEquals( "result.warnings.size" + result.getErrors(), 0, result.getErrors().size() );

        assertEquals( "result.projects.size", 3, result.getProjects().size() );

        assertEquals( "result.projectGroups.size", 1, result.getProjectGroups().size() );

        log.info( "number of projects: " + getProjectDao().getAllProjectsByName().size() );

        log.info( "number of project groups: " + getProjectGroupDao().getAllProjectGroupsWithProjects().size() );

        assertEquals( "Total project count", projectCount + 3, getProjectDao().getAllProjectsByName().size() );

        assertEquals( "Total project group count.", projectGroupCount + 1,
                      getProjectGroupDao().getAllProjectGroupsWithProjects().size() );

        Map<String, Project> projects = new HashMap<String, Project>();

        for ( Project project : getProjectDao().getAllProjectsByName() )
        {
            projects.put( project.getName(), project );

            // validate project in project group
            assertTrue( "project not in project group", getProjectGroupDao().getProjectGroupByProjectId(
                project.getId() ) != null );
        }

        assertTrue( "no irc notifier", projects.containsKey( "Continuum IRC Notifier" ) );

        assertTrue( "no jabber notifier", projects.containsKey( "Continuum Jabber Notifier" ) );

    }

/* test failing intermittently, possibly due to the dodgy for loop
    // handle flat multi-module projects
    public void testAddMavenTwoProjectSetInSingleDirectory()
        throws Exception
    {   
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );
        
        String url = getTestFile( "src/test-projects/flat-multi-module/parent-project/pom.xml" ).toURL().toExternalForm();

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url, -1, true, false, true, -1, true );
 
        assertNotNull( result );

        List<Project> projects = result.getProjects();

        assertEquals( 4, projects.size() );     
        
        Project rootProject = result.getRootProject();
        
        assertNotNull( rootProject );
        
        Map<String, Project> projectsMap = new HashMap<String, Project>();

        int projectGroupId = 0;

        for ( Project project : getProjectDao().getAllProjectsByName() )
        {
            projectsMap.put( project.getName(), project );

            ProjectGroup projectGroup = getProjectGroupDao().getProjectGroupByProjectId( project.getId() );
            projectGroupId = projectGroup.getId();

            // validate project in project group
            assertTrue( "project not in project group", projectGroup != null );
        }

        // sometimes projects don't get added to checkout queue
        continuum.buildProjectGroup( projectGroupId, new org.apache.continuum.utils.build.BuildTrigger( 1, "user" ) );

        assertTrue( "no module-a", projectsMap.containsKey( "module-a" ) );
        
        assertTrue( "no module-b", projectsMap.containsKey( "module-b" ) );

        assertTrue( "no module-d", projectsMap.containsKey( "module-d" ) );

        // check if the modules were checked out in the same directory as the parent
        ConfigurationService configurationService = ( ConfigurationService ) lookup( "configurationService" );
        
        File workingDir = configurationService.getWorkingDirectory();
        
        Project parentProject = getProjectDao().getProjectByName( "parent-project" );
        
        File checkoutDir = new File( workingDir, String.valueOf( parentProject.getId() ) );

        for( long delay = 0; delay <= 999999999; delay++ )
        {
            // wait while the project has been checked out/build
        }
        
        assertTrue( "checkout directory of project 'parent-project' does not exist." , new File( checkoutDir, "parent-project" ).exists() );
        
        assertFalse( "module-a should not have been checked out as a separate project.",
                    new File( workingDir, String.valueOf( getProjectDao().getProjectByName( "module-a" ).getId() ) ).exists() );
        
        assertFalse( "module-b should not have been checked out as a separate project.",
                    new File( workingDir, String.valueOf( getProjectDao().getProjectByName( "module-b" ).getId() ) ).exists() );

        assertFalse( "module-d should not have been checked out as a separate project.",
                     new File( workingDir, String.valueOf( getProjectDao().getProjectByName( "module-d" ).getId() ) ).exists() );

        assertTrue( "module-a was not checked out in the same directory as it's parent.", new File( checkoutDir, "module-a" ).exists() );
        
        assertTrue( "module-b was not checked out in the same directory as it's parent.", new File( checkoutDir, "module-b" ).exists() );

        assertTrue( "module-d was not checked out in the same directory as it's parent.", new File( checkoutDir, "module-c/module-d" ).exists() );

        // assert project state
        // commented out this test case as it sometimes fails because the actual checkout hasn't finished yet so
        //    the state hasn't been updated yet
        //assertEquals( "state of 'parent-project' should have been updated.", ContinuumProjectState.CHECKEDOUT, parentProject.getState() );
        //
        //assertEquals( "state of 'module-a' should have been updated.", ContinuumProjectState.CHECKEDOUT,
        //            getProjectDao().getProjectByName( "module-a" ).getState() );
        //
        //assertEquals( "state of 'module-b' should have been updated.", ContinuumProjectState.CHECKEDOUT,
        //            getProjectDao().getProjectByName( "module-b" ).getState() );
    }
*/

    @Test
    public void testUpdateMavenTwoProject()
        throws Exception
    {
        Continuum continuum = lookup( Continuum.class );

        // ----------------------------------------------------------------------
        // Test projects with duplicate names
        // ----------------------------------------------------------------------

        String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );

        assertNotNull( result );

        List<Project> projects = result.getProjects();

        assertEquals( 1, projects.size() );

        assertEquals( Project.class, projects.get( 0 ).getClass() );

        Project project = projects.get( 0 );

        // reattach
        project = continuum.getProject( project.getId() );

        project.setName( project.getName() + " 2" );

        continuum.updateProject( project );

        project = continuum.getProject( project.getId() );
    }

    @Test
    public void testRemoveMavenTwoProject()
        throws Exception
    {
        Continuum continuum = lookup( Continuum.class );

        Project project = makeStubProject( "test-project" );

        ProjectGroup defaultGroup = getDefaultProjectGroup();

        defaultGroup.addProject( project );

        getProjectGroupDao().updateProjectGroup( defaultGroup );

        project = getProjectDao().getProjectByName( "test-project" );

        assertNotNull( project );

        BuildResult buildResult = new BuildResult();

        getBuildResultDao().addBuildResult( project, buildResult );

        Collection<BuildResult> brs = continuum.getBuildResultsForProject( project.getId(), 0, 5 );

        assertEquals( "Build result of project was not added", 1, brs.size() );

        // delete project
        continuum.removeProject( project.getId() );

        try
        {
            continuum.getProject( project.getId() );

            fail( "Project was not removed" );
        }
        catch ( ContinuumException expected )
        {
            brs = continuum.getBuildResultsForProject( project.getId(), 0, 5 );

            assertEquals( "Build result of project was not removed", 0, brs.size() );
        }
    }

    @Test
    public void testBuildDefinitions()
        throws Exception
    {
        Continuum continuum = lookup( Continuum.class );

        String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );

        assertNotNull( result );

        List<Project> projects = result.getProjects();

        assertEquals( 1, projects.size() );

        assertEquals( Project.class, projects.get( 0 ).getClass() );

        Project project = projects.get( 0 );

        // reattach
        project = continuum.getProject( project.getId() );

        ProjectGroup projectGroup = getProjectGroupDao().getProjectGroupByProjectId( project.getId() );

        projectGroup = getProjectGroupDao().getProjectGroupWithBuildDetailsByProjectGroupId( projectGroup.getId() );

        List<BuildDefinition> buildDefs = projectGroup.getBuildDefinitions();

        assertTrue( "missing project group build definition", !buildDefs.isEmpty() );

        assertTrue( "more then one project group build definition on add project", buildDefs.size() == 1 );

        BuildDefinition pgbd = buildDefs.get( 0 );

        pgbd.setGoals( "foo" );

        continuum.updateBuildDefinitionForProjectGroup( projectGroup.getId(), pgbd );

        pgbd = continuum.getBuildDefinition( pgbd.getId() );

        assertTrue( "update failed for project group build definition", "foo".equals( pgbd.getGoals() ) );

        assertTrue( "project group build definition is not default", pgbd.isDefaultForProject() );

        BuildDefinition nbd = new BuildDefinition();
        nbd.setGoals( "clean" );
        nbd.setArguments( "" );
        nbd.setDefaultForProject( true );
        nbd.setSchedule( getScheduleDao().getScheduleByName( ConfigurationService.DEFAULT_SCHEDULE_NAME ) );

        continuum.addBuildDefinitionToProject( project.getId(), nbd );

        assertTrue( "project lvl build definition not default for project", continuum.getDefaultBuildDefinition(
            project.getId() ).getId() == nbd.getId() );

        continuum.removeBuildDefinitionFromProject( project.getId(), nbd.getId() );

        assertTrue( "default build definition didn't toggle back to project group level",
                    continuum.getDefaultBuildDefinition( project.getId() ).getId() == pgbd.getId() );

        try
        {
            continuum.removeBuildDefinitionFromProjectGroup( projectGroup.getId(), pgbd.getId() );
            fail( "we were able to remove the default build definition, and that is bad" );
        }
        catch ( ContinuumException expected )
        {

        }
    }

    /**
     * todo add another project group to test
     */
    @Test
    public void testProjectGroups()
        throws Exception
    {
        Continuum continuum = lookup( Continuum.class );

        Collection projectGroupList = continuum.getAllProjectGroups();

        int projectGroupsBefore = projectGroupList.size();

        assertEquals( 1, projectGroupsBefore );

        String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );

        assertNotNull( result );

        assertEquals( 1, result.getProjectGroups().size() );

        ProjectGroup projectGroup = result.getProjectGroups().get( 0 );

        assertEquals( "plexus", projectGroup.getGroupId() );

        projectGroupList = continuum.getAllProjectGroups();

        assertEquals( "Project group missing, should have " + ( projectGroupsBefore + 1 ) + " project groups",
                      projectGroupsBefore + 1, projectGroupList.size() );

        projectGroup = (ProjectGroup) projectGroupList.iterator().next();

        assertNotNull( projectGroup );

        BuildsManager buildsManager = continuum.getBuildsManager();

        List<Project> projects = continuum.getProjectGroupWithProjects( projectGroup.getId() ).getProjects();
        int[] projectIds = new int[projects.size()];

        int idx = 0;
        for ( Project project : projects )
        {
            projectIds[idx] = project.getId();
            idx++;
        }

        while ( buildsManager.isAnyProjectCurrentlyBeingCheckedOut( projectIds ) )
        {
        }

        continuum.removeProjectGroup( projectGroup.getId() );

        projectGroupList = continuum.getAllProjectGroups();

        assertEquals( "Remove project group failed", projectGroupsBefore, projectGroupList.size() );
    }

    /**
     * test the logic for notifiers
     */
    @Test
    public void testProjectAndGroupNotifiers()
        throws Exception
    {
        Continuum continuum = lookup( Continuum.class );

        Collection projectGroupList = continuum.getAllProjectGroups();

        int projectGroupsBefore = projectGroupList.size();

        assertEquals( 1, projectGroupsBefore );

        String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );

        assertNotNull( result );

        assertEquals( 1, result.getProjectGroups().size() );

        ProjectGroup projectGroup = result.getProjectGroups().get( 0 );

        continuum.addGroupNotifier( projectGroup.getId(), new ProjectNotifier() );

        for ( Project p : (List<Project>) projectGroup.getProjects() )
        {
            continuum.addNotifier( p.getId(), new ProjectNotifier() );
        }

        projectGroup = continuum.getProjectGroupWithBuildDetails( projectGroup.getId() );

        assertEquals( 1, projectGroup.getNotifiers().size() );

        for ( Project p : (List<Project>) projectGroup.getProjects() )
        {
            assertEquals( 2, p.getNotifiers().size() );
        }
    }

    @Test
    public void testExecuteAction()
        throws Exception
    {
        DefaultContinuum continuum = (DefaultContinuum) lookup( Continuum.class );

        String exceptionName = ContinuumException.class.getName();
        try
        {
            continuum.executeAction( "testAction", new HashMap() );
        }
        catch ( ContinuumException e )
        {
            //expected, check for twice wrapped exception
            if ( e.getCause() != null )
            {
                assertFalse( exceptionName + " is wrapped in " + exceptionName, e.getCause().getClass().equals(
                    ContinuumException.class ) );
            }
        }
    }

    @Test
    public void testRemoveProjectFromCheckoutQueue()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

        BuildsManager parallelBuildsManager = continuum.getBuildsManager();

        String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );

        assertNotNull( result );

        List<Project> projects = result.getProjects();

        assertEquals( 1, projects.size() );

        assertEquals( Project.class, projects.get( 0 ).getClass() );

        Project project = projects.get( 0 );

        parallelBuildsManager.removeProjectFromCheckoutQueue( project.getId() );

        assertFalse( "project still exist on the checkout queue", parallelBuildsManager.isInAnyCheckoutQueue(
            project.getId() ) );
    }

    /*public void testCreationOfProjectScmRootDuringInitialization()
    throws Exception
{
    DefaultContinuum continuum = (DefaultContinuum) getContinuum();

    ProjectGroup defaultProjectGroup =
        continuum.getProjectGroupByGroupId( ContinuumInitializer.DEFAULT_PROJECT_GROUP_GROUP_ID );

    ProjectScmRoot scmRoot = new ProjectScmRoot();
    scmRoot.setProjectGroup( defaultProjectGroup );
    scmRoot.setScmRootAddress( "http://temp.company.com/svn/trunk" );
    getProjectScmRootDao().addProjectScmRoot( scmRoot );

    defaultProjectGroup = continuum.getProjectGroupWithProjects( defaultProjectGroup.getId() );
    assertEquals( 0, defaultProjectGroup.getProjects().size() );

    Project project = new Project();
    project.setGroupId( "project1" );
    project.setArtifactId( "project1" );
    project.setVersion( "1.0-SNAPSHOT" );
    project.setScmUrl( "http://temp.company.com/svn/trunk/project1" );
    defaultProjectGroup.addProject( project );

    project = new Project();
    project.setGroupId( "project2" );
    project.setArtifactId( "project2" );
    project.setVersion( "1.0-SNAPSHOT" );
    project.setScmUrl( "http://temp.company.com/svn/trunk/project2" );
    defaultProjectGroup.addProject( project );

    project = new Project();
    project.setGroupId( "project3" );
    project.setArtifactId( "project3" );
    project.setVersion( "1.0-SNAPSHOT" );
    project.setScmUrl( "http://temp.company.com/svn/trunk/project3" );
    defaultProjectGroup.addProject( project );

    getProjectGroupDao().updateProjectGroup( defaultProjectGroup );

    continuum.initialize();

    List<ProjectScmRoot> scmRoots = continuum.getProjectScmRootByProjectGroup( defaultProjectGroup.getId() );
    assertEquals( "#scmRoots in the group", 1, scmRoots.size() );
}    */

    @Test
    public void testAddAntProjectWithdefaultBuildDef()
        throws Exception
    {
        Continuum continuum = getContinuum();

        Project project = new Project();
        project.setScmUrl( "scmUrl" );
        ProjectGroup defaultProjectGroup = continuum.getProjectGroupByGroupId(
            ContinuumInitializer.DEFAULT_PROJECT_GROUP_GROUP_ID );
        int projectId = continuum.addProject( project, ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR,
                                              defaultProjectGroup.getId() );
        assertEquals( 1, continuum.getProjectGroupWithProjects( defaultProjectGroup.getId() ).getProjects().size() );
        project = continuum.getProjectWithAllDetails( projectId );
        assertNotNull( project );

        BuildDefinitionService service = lookup( BuildDefinitionService.class );
        assertEquals( 4, service.getAllBuildDefinitionTemplate().size() );
        assertEquals( 5, service.getAllBuildDefinitions().size() );

        BuildDefinition buildDef =
            service.getDefaultAntBuildDefinitionTemplate().getBuildDefinitions().get( 0 );
        buildDef = service.cloneBuildDefinition( buildDef );
        buildDef.setTemplate( false );
        continuum.addBuildDefinitionToProject( project.getId(), buildDef );
        project = continuum.getProjectWithAllDetails( project.getId() );
        assertEquals( 2, project.getBuildDefinitions().size() );
        assertEquals( 4, service.getAllBuildDefinitionTemplate().size() );
        assertEquals( 6, service.getAllBuildDefinitions().size() );
    }

    @Test
    public void testRemoveProjectGroupWithRepository()
        throws Exception
    {
        Continuum continuum = getContinuum();
        RepositoryService service = lookup( RepositoryService.class );

        LocalRepository repository = new LocalRepository();
        repository.setName( "defaultRepo" );
        repository.setLocation( getTestFile( "target/default-repository" ).getAbsolutePath() );
        repository = service.addLocalRepository( repository );

        ProjectGroup group = new ProjectGroup();
        group.setGroupId( "testGroup" );
        group.setName( "testGroup" );
        group.setLocalRepository( repository );
        continuum.addProjectGroup( group );

        ProjectGroup retrievedDefaultProjectGroup = continuum.getProjectGroupByGroupId( "testGroup" );
        assertNotNull( retrievedDefaultProjectGroup.getLocalRepository() );

        continuum.removeProjectGroup( retrievedDefaultProjectGroup.getId() );

        try
        {
            continuum.getProjectGroupByGroupId( "testGroup" );
            fail( "project group was not deleted" );
        }
        catch ( Exception e )
        {
            // should fail. do nothing.
        }

        LocalRepository retrievedRepository = service.getLocalRepository( repository.getId() );
        assertNotNull( retrievedRepository );
        assertEquals( repository, retrievedRepository );
    }

    @Test
    public void testContinuumReleaseResult()
        throws Exception
    {
        Continuum continuum = getContinuum();

        Project project = makeStubProject( "test-project" );
        ProjectGroup defaultGroup = getDefaultProjectGroup();
        defaultGroup.addProject( project );
        getProjectGroupDao().updateProjectGroup( defaultGroup );
        project = getProjectDao().getProjectByName( "test-project" );
        assertNotNull( project );
        assertEquals( 0, continuum.getAllContinuumReleaseResults().size() );

        ReleaseResult result = new ReleaseResult();
        result.setStartTime( System.currentTimeMillis() );
        result.setEndTime( System.currentTimeMillis() );
        result.setResultCode( 200 );
        result.appendOutput( "Error in release" );

        ContinuumReleaseDescriptor descriptor = new ContinuumReleaseDescriptor();
        descriptor.setPreparationGoals( "clean" );
        descriptor.setReleaseBy( "admin" );

        continuum.getReleaseManager().getReleaseResults().put( "test-release-id", result );
        continuum.getReleaseManager().getPreparedReleases().put( "test-release-id", descriptor );

        ContinuumReleaseResult releaseResult = continuum.addContinuumReleaseResult( project.getId(), "test-release-id",
                                                                                    "prepare" );

        releaseResult = continuum.addContinuumReleaseResult( releaseResult );

        List<ContinuumReleaseResult> releaseResults = continuum.getContinuumReleaseResultsByProjectGroup(
            defaultGroup.getId() );
        assertEquals( 1, releaseResults.size() );
        assertEquals( releaseResult, releaseResults.get( 0 ) );

        continuum.removeContinuumReleaseResult( releaseResult.getId() );
        assertEquals( 0, continuum.getAllContinuumReleaseResults().size() );
        assertEquals( defaultGroup, continuum.getProjectGroupByGroupId(
            ContinuumInitializer.DEFAULT_PROJECT_GROUP_GROUP_ID ) );
    }

    @Test
    public void testBuildProjectWhileProjectIsInReleaseStage()
        throws Exception
    {
        DefaultContinuum continuum = (DefaultContinuum) getContinuum();
        continuum.setTaskQueueManager( taskQueueManager );
        continuum.setProjectDao( projectDao );

        Project project = new Project();
        project.setId( 1 );
        project.setName( "Continuum Core" );
        project.setGroupId( "org.apache.continuum" );
        project.setArtifactId( "continuum-core" );

        when( projectDao.getProject( 1 ) ).thenReturn( project );
        when( taskQueueManager.isProjectInReleaseStage( "org.apache.continuum:continuum-core" ) ).thenReturn( true );

        try
        {
            continuum.buildProject( 1, "test-user" );
            fail( "An exception should have been thrown." );
        }
        catch ( ContinuumException e )
        {
            assertEquals( "Project (id=1) is currently in release stage.", e.getMessage() );
        }
    }

    @Test
    public void testBuildProjectGroupWhileAtLeastOneProjectIsInReleaseStage()
        throws Exception
    {
        DefaultContinuum continuum = (DefaultContinuum) getContinuum();
        continuum.setTaskQueueManager( taskQueueManager );
        continuum.setProjectDao( projectDao );

        List<Project> projects = new ArrayList<Project>();
        Project project = new Project();
        project.setId( 1 );
        project.setName( "Continuum Core" );
        project.setGroupId( "org.apache.continuum" );
        project.setArtifactId( "continuum-core" );
        projects.add( project );
        project = new Project();
        project.setId( 2 );
        project.setName( "Continuum API" );
        project.setGroupId( "org.apache.continuum" );
        project.setArtifactId( "continuum-api" );
        projects.add( project );

        when( projectDao.getProjectsInGroup( 1 ) ).thenReturn( projects );
        when( taskQueueManager.isProjectInReleaseStage( "org.apache.continuum:continuum-core" ) ).thenReturn( true );

        try
        {
            continuum.buildProjectGroup( 1, new BuildTrigger( 1, "test-user" ) );
            fail( "An exception should have been thrown." );
        }
        catch ( ContinuumException e )
        {
            assertEquals( "Cannot build project group. Project (id=1) in group is currently in release stage.",
                          e.getMessage() );
        }
    }

    @Test
    public void testGetChangesSinceLastSuccessNoSuccess()
        throws Exception
    {
        DefaultContinuum continuum = getContinuum();
        continuum.setBuildResultDao( buildResultDao );

        when( buildResultDao.getPreviousBuildResultInSuccess( anyInt(), anyInt() ) ).thenReturn( null );

        List<ChangeSet> changes = continuum.getChangesSinceLastSuccess( 5, 5 );

        assertEquals( "no prior success should return no changes", 0, changes.size() );
    }

    @Test
    public void testGetChangesSinceLastSuccessNoInterveningFailures()
        throws Exception
    {
        DefaultContinuum continuum = getContinuum();
        continuum.setBuildResultDao( buildResultDao );

        int projectId = 123, fromId = 789, toId = 1011;
        BuildResult priorResult = new BuildResult();
        priorResult.setId( fromId );

        when( buildResultDao.getPreviousBuildResultInSuccess( projectId, toId ) ).thenReturn( priorResult );
        when( buildResultDao.getBuildResultsForProjectWithDetails( projectId, fromId, toId ) ).thenReturn(
            Collections.EMPTY_LIST );

        List<ChangeSet> changes = continuum.getChangesSinceLastSuccess( projectId, toId );

        assertEquals( "no intervening failures, should return no changes", 0, changes.size() );
    }

    @Test
    public void testGetChangesSinceLastSuccessInterveningFailures()
        throws Exception
    {
        DefaultContinuum continuum = getContinuum();
        continuum.setBuildResultDao( buildResultDao );

        int projectId = 123, fromId = 789, toId = 1011;
        BuildResult priorResult = new BuildResult();
        priorResult.setId( fromId );

        BuildResult[] failures = { resultWithChanges( 1 ), resultWithChanges( 0 ), resultWithChanges( 0, 1 ),
            resultWithChanges( 1, 0 ), resultWithChanges( 0, 1, 0 ), resultWithChanges( 1, 0, 1 ) };

        when( buildResultDao.getPreviousBuildResultInSuccess( projectId, toId ) ).thenReturn( priorResult );
        when( buildResultDao.getBuildResultsForProjectWithDetails( projectId, fromId, toId ) ).thenReturn(
            Arrays.asList( failures ) );

        List<ChangeSet> changes = continuum.getChangesSinceLastSuccess( projectId, toId );

        assertEquals( "should return same number of changes as in failed results", 6, changes.size() );
        assertOldestToNewest( changes );
    }

    private static int changeCounter = 1;

    private BuildResult resultWithChanges( int... changeSetCounts )
    {
        BuildResult result = new BuildResult();
        ScmResult scmResult = new ScmResult();
        result.setScmResult( scmResult );
        for ( Integer changeCount : changeSetCounts )
        {
            for ( int i = 0; i < changeCount; i++ )
            {
                ChangeSet change = new ChangeSet();
                change.setId( String.format( "%011d", changeCounter++ ) );  // zero-padded for string comparison
                scmResult.addChange( change );
            }
        }
        return result;
    }

    private void assertOldestToNewest( List<ChangeSet> changes )
    {
        if ( changes == null || changes.isEmpty() || changes.size() == 1 )
            return;
        for ( int prior = 0, next = 1; next < changes.size(); prior++, next = prior + 1 )
        {
            String priorId = changes.get( prior ).getId(), nextId = changes.get( next ).getId();
            assertTrue( "changes were not in ascending order", priorId.compareTo( nextId ) < 0 );
        }
    }

    private DefaultContinuum getContinuum()
        throws Exception
    {
        return (DefaultContinuum) lookup( Continuum.class );
    }

    private BuildResultDao getBuildResultDao()
    {
        return lookup( BuildResultDao.class );
    }
}
