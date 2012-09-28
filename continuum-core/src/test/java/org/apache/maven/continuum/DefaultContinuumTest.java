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
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.shared.release.ReleaseResult;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultContinuumTest
    extends AbstractContinuumTest
{
    private static final Logger log = LoggerFactory.getLogger( DefaultContinuumTest.class );

    private Mockery context;

    private TaskQueueManager taskQueueManager;

    private ProjectDao projectDao;

    private BuildResultDao buildResultDao;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        context = new JUnit3Mockery();

        taskQueueManager = context.mock( TaskQueueManager.class );

        projectDao = context.mock( ProjectDao.class );
    }

    public void testContinuumConfiguration()
        throws Exception
    {
        lookup( Continuum.ROLE );
    }

    public void testAddMavenTwoProjectSet()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

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

    public void testUpdateMavenTwoProject()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

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

    public void testRemoveMavenTwoProject()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

        Project project = makeStubProject( "test-project" );

        ProjectGroup defaultGroup = getDefaultProjectGroup();

        defaultGroup.addProject( project );

        getProjectGroupDao().updateProjectGroup( defaultGroup );

        project = getProjectDao().getProjectByName( "test-project" );

        assertNotNull( project );

        BuildResult buildResult = new BuildResult();

        getBuildResultDao().addBuildResult( project, buildResult );

        Collection<BuildResult> brs = continuum.getBuildResultsForProject( project.getId() );

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
            brs = continuum.getBuildResultsForProject( project.getId() );

            assertEquals( "Build result of project was not removed", 0, brs.size() );
        }
    }

    public void testBuildDefinitions()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

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
    public void testProjectGroups()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

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
    public void testProjectAndGroupNotifiers()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

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

    public void testExecuteAction()
        throws Exception
    {
        DefaultContinuum continuum = (DefaultContinuum) lookup( Continuum.ROLE );

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

        BuildDefinitionService service = (BuildDefinitionService) lookup( BuildDefinitionService.class );
        assertEquals( 4, service.getAllBuildDefinitionTemplate().size() );
        assertEquals( 5, service.getAllBuildDefinitions().size() );

        BuildDefinition buildDef =
            (BuildDefinition) service.getDefaultAntBuildDefinitionTemplate().getBuildDefinitions().get( 0 );
        buildDef = service.cloneBuildDefinition( buildDef );
        buildDef.setTemplate( false );
        continuum.addBuildDefinitionToProject( project.getId(), buildDef );
        project = continuum.getProjectWithAllDetails( project.getId() );
        assertEquals( 2, project.getBuildDefinitions().size() );
        assertEquals( 4, service.getAllBuildDefinitionTemplate().size() );
        assertEquals( 6, service.getAllBuildDefinitions().size() );
    }

    public void testRemoveProjectGroupWithRepository()
        throws Exception
    {
        Continuum continuum = getContinuum();
        RepositoryService service = (RepositoryService) lookup( RepositoryService.ROLE );

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

    public void testBuildProjectWhileProjectIsInReleaseStage()
        throws Exception
    {
        DefaultContinuum continuum = (DefaultContinuum) getContinuum();

        continuum.setTaskQueueManager( taskQueueManager );

        continuum.setProjectDao( projectDao );

        final Project project = new Project();
        project.setId( 1 );
        project.setName( "Continuum Core" );
        project.setGroupId( "org.apache.continuum" );
        project.setArtifactId( "continuum-core" );

        context.checking( new Expectations()
        {
            {
                one( projectDao ).getProject( 1 );
                will( returnValue( project ) );

                one( taskQueueManager ).isProjectInReleaseStage( "org.apache.continuum:continuum-core" );
                will( returnValue( true ) );
            }
        } );

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

    public void testBuildProjectGroupWhileAtLeastOneProjectIsInReleaseStage()
        throws Exception
    {
        DefaultContinuum continuum = (DefaultContinuum) getContinuum();

        continuum.setTaskQueueManager( taskQueueManager );

        continuum.setProjectDao( projectDao );

        final List<Project> projects = new ArrayList<Project>();

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

        context.checking( new Expectations()
        {
            {
                one( projectDao ).getProjectsInGroup( 1 );
                will( returnValue( projects ) );

                one( taskQueueManager ).isProjectInReleaseStage( "org.apache.continuum:continuum-core" );
                will( returnValue( true ) );
            }
        } );

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

    private Continuum getContinuum()
        throws Exception
    {
        return (Continuum) lookup( Continuum.ROLE );
    }

    private BuildResultDao getBuildResultDao()
    {
        return (BuildResultDao) lookup( BuildResultDao.class.getName() );
    }
}
