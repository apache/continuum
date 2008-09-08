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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.continuum.model.release.ContinuumReleaseResult;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.repository.RepositoryService;
import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.utils.ContinuumUrlValidator;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultContinuumTest
    extends AbstractContinuumTest
{
    protected Logger log = LoggerFactory.getLogger( getClass() );
    
    public void testContinuumConfiguration()
        throws Exception
    {
        lookup( Continuum.ROLE );
    }

    public void testLookups()
        throws Exception
    {
        lookup( TaskQueue.ROLE, "build-project" );

        lookup( TaskQueue.ROLE, "check-out-project" );

        lookup( TaskQueueExecutor.ROLE, "build-project" );

        lookup( TaskQueueExecutor.ROLE, "check-out-project" );
    }

    public void testAddMavenTwoProjectSet()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

        int projectCount = getProjectDao().getAllProjectsByName().size();

        int projectGroupCount = getProjectGroupDao().getAllProjectGroupsWithProjects().size();

        File rootPom = getTestFile( "src/test/resources/projects/continuum/continuum-notifiers/pom.xml" );

        assertTrue( rootPom.exists() );
        
        ContinuumUrlValidator validator = (ContinuumUrlValidator) lookup( ContinuumUrlValidator.class, "continuumUrl" );
        
        String fileUrl = rootPom.toURL().toExternalForm();
        
        //assertTrue( validator.validate( fileUrl ) );
        
        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( fileUrl );

        assertNotNull( result );

        assertEquals( "result.warnings.size", 0, result.getWarnings().size() );

        assertEquals( "result.projects.size", 3, result.getProjects().size() );

        assertEquals( "result.projectGroups.size", 1, result.getProjectGroups().size() );

        log.info( "number of projects: " + getProjectDao().getAllProjectsByName().size() );

        log.info(
            "number of project groups: " + getProjectGroupDao().getAllProjectGroupsWithProjects().size() );

        assertEquals( "Total project count", projectCount + 3, getProjectDao().getAllProjectsByName().size() );

        assertEquals( "Total project group count.", projectGroupCount + 1,
                      getProjectGroupDao().getAllProjectGroupsWithProjects().size() );

        Map projects = new HashMap();

        for ( Iterator i = getProjectDao().getAllProjectsByName().iterator(); i.hasNext(); )
        {
            Project project = (Project) i.next();

            projects.put( project.getName(), project );

            // validate project in project group
            assertTrue( "project not in project group",
                        getProjectGroupDao().getProjectGroupByProjectId( project.getId() ) != null );
        }

        assertTrue( "no irc notifier", projects.containsKey( "Continuum IRC Notifier" ) );

        assertTrue( "no jabber notifier", projects.containsKey( "Continuum Jabber Notifier" ) );


    }

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

        List projects = result.getProjects();

        assertEquals( 1, projects.size() );

        assertEquals( Project.class, projects.get( 0 ).getClass() );

        Project project = (Project) projects.get( 0 );

        // reattach
        project = continuum.getProject( project.getId() );

        project.setName( project.getName() + " 2" );

        continuum.updateProject( project );

        project = continuum.getProject( project.getId() );
    }

    public void testBuildDefinitions()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

        String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );

        assertNotNull( result );

        List projects = result.getProjects();

        assertEquals( 1, projects.size() );

        assertEquals( Project.class, projects.get( 0 ).getClass() );

        Project project = (Project) projects.get( 0 );

        // reattach
        project = continuum.getProject( project.getId() );

        ProjectGroup projectGroup = getProjectGroupDao().getProjectGroupByProjectId( project.getId() );

        projectGroup = getProjectGroupDao().getProjectGroupWithBuildDetailsByProjectGroupId( projectGroup.getId() );

        List buildDefs = projectGroup.getBuildDefinitions();

        assertTrue( "missing project group build definition", !buildDefs.isEmpty() );

        assertTrue( "more then one project group build definition on add project", buildDefs.size() == 1 );

        BuildDefinition pgbd = (BuildDefinition) buildDefs.get( 0 );

        pgbd.setGoals( "foo" );

        continuum.updateBuildDefinitionForProjectGroup( projectGroup.getId(), pgbd );

        pgbd = continuum.getBuildDefinition( pgbd.getId() );

        assertTrue( "update failed for project group build definition", "foo".equals( pgbd.getGoals() ) );

        assertTrue( "project group build definition is not default", pgbd.isDefaultForProject() );

        assertTrue( "project group build definition not default for project",
                    continuum.getDefaultBuildDefinition( project.getId() ).getId() == pgbd.getId() );

        BuildDefinition nbd = new BuildDefinition();
        nbd.setGoals( "clean" );
        nbd.setArguments( "" );
        nbd.setDefaultForProject( true );
        nbd.setSchedule( getScheduleDao().getScheduleByName( ConfigurationService.DEFAULT_SCHEDULE_NAME ) );

        continuum.addBuildDefinitionToProject( project.getId(), nbd );

        assertTrue( "project lvl build definition not default for project",
                    continuum.getDefaultBuildDefinition( project.getId() ).getId() == nbd.getId() );

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
     *
     * @throws Exception
     */
    public void testProjectGroups()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

        Collection projectGroupList = continuum.getAllProjectGroupsWithProjects();

        int projectGroupsBefore = projectGroupList.size();

        assertEquals( 1, projectGroupsBefore );

        String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );

        assertNotNull( result );

        assertEquals( 1, result.getProjectGroups().size() );

        ProjectGroup projectGroup = (ProjectGroup) result.getProjectGroups().get( 0 );

        assertEquals( "plexus", projectGroup.getGroupId() );

        projectGroupList = continuum.getAllProjectGroupsWithProjects();

        assertEquals( "Project group missing, should have " + ( projectGroupsBefore + 1 ) + " project groups",
                      projectGroupsBefore + 1, projectGroupList.size() );

        projectGroup = (ProjectGroup) projectGroupList.iterator().next();

        assertNotNull( projectGroup );

        continuum.removeProjectGroup( projectGroup.getId() );

        projectGroupList = continuum.getAllProjectGroupsWithProjects();

        assertEquals( "Remove project group failed", projectGroupsBefore, projectGroupList.size() );
    }

    /**
     * test the logic for notifiers
     *
     * @throws Exception
     */
    public void testProjectAndGroupNotifiers()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

        Collection projectGroupList = continuum.getAllProjectGroupsWithProjects();

        int projectGroupsBefore = projectGroupList.size();

        assertEquals( 1, projectGroupsBefore );

        String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );

        assertNotNull( result );

        assertEquals( 1, result.getProjectGroups().size() );

        ProjectGroup projectGroup = (ProjectGroup) result.getProjectGroups().get( 0 );

        continuum.addGroupNotifier( projectGroup.getId(), new ProjectNotifier() );

        for ( Iterator i = projectGroup.getProjects().iterator(); i.hasNext(); )
        {
            Project p = (Project) i.next();
            continuum.addNotifier( p.getId(), new ProjectNotifier() );
        }

        projectGroup = continuum.getProjectGroupWithBuildDetails( projectGroup.getId() );

        assertEquals( 1, projectGroup.getNotifiers().size() );

        for ( Iterator i = projectGroup.getProjects().iterator(); i.hasNext(); )
        {
            Project p = (Project) i.next();
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
                assertFalse( exceptionName + " is wrapped in " + exceptionName, e.getCause().getClass()
                    .equals( ContinuumException.class ) );
            }
        }
    }

    public void testRemoveProjectFromCheckoutQueue()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

        String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );

        assertNotNull( result );

        List projects = result.getProjects();

        assertEquals( 1, projects.size() );

        assertEquals( Project.class, projects.get( 0 ).getClass() );

        Project project = (Project) projects.get( 0 );

        assertTrue( "project missing from the checkout queue",
                    continuum.removeProjectFromCheckoutQueue( project.getId() ) );

        assertFalse( "project still exist on the checkout queue",
                     continuum.removeProjectFromCheckoutQueue( project.getId() ) );
    }

    public void testAddAntProjectWithdefaultBuildDef()
        throws Exception
    {
        Continuum continuum = getContinuum();

        Project project = new Project();
        int projectId = continuum.addProject( project, ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR );
        ProjectGroup defaultProjectGroup = continuum
            .getProjectGroupByGroupId( Continuum.DEFAULT_PROJECT_GROUP_GROUP_ID );
        assertEquals( 1, continuum.getProjectGroupWithProjects( defaultProjectGroup.getId() ).getProjects().size() );
        project = continuum.getProjectWithAllDetails( projectId );
        assertNotNull( project );

        BuildDefinitionService service = (BuildDefinitionService) lookup( BuildDefinitionService.class );
        assertEquals( 4, service.getAllBuildDefinitionTemplate().size() );
        assertEquals( 5, service.getAllBuildDefinitions().size() );

        BuildDefinition buildDef = (BuildDefinition) service.getDefaultAntBuildDefinitionTemplate()
            .getBuildDefinitions().get( 0 );
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
        
        ProjectGroup retrievedDefaultProjectGroup = continuum
        .getProjectGroupByGroupId( "testGroup" );
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

        ProjectGroup defaultProjectGroup = continuum.getProjectGroupByGroupId( Continuum.DEFAULT_PROJECT_GROUP_GROUP_ID );

        assertEquals( 0, continuum.getAllContinuumReleaseResults().size() );

        ContinuumReleaseResult releaseResult = new ContinuumReleaseResult();
        releaseResult.setStartTime( System.currentTimeMillis() );

        File logFile = continuum.getConfiguration().getReleaseOutputFile( defaultProjectGroup.getId(), 
                                                                          "releases-" + releaseResult.getStartTime() );
        logFile.mkdirs();

        assertTrue( logFile.exists() );

        releaseResult.setResultCode( 0 );
        releaseResult.setEndTime( System.currentTimeMillis() );
        releaseResult.setProjectGroup( defaultProjectGroup );

        releaseResult = continuum.addContinuumReleaseResult( releaseResult );

        List<ContinuumReleaseResult> releaseResults = continuum.getContinuumReleaseResultsByProjectGroup( defaultProjectGroup.getId() );
        assertEquals( 1, releaseResults.size() );
        assertEquals( releaseResult, releaseResults.get( 0 ) );

        continuum.removeContinuumReleaseResult( releaseResult.getId() );
        assertEquals( 0 , continuum.getAllContinuumReleaseResults().size() );
        assertFalse( logFile.exists() );
        assertEquals( defaultProjectGroup, continuum.getProjectGroupByGroupId( Continuum.DEFAULT_PROJECT_GROUP_GROUP_ID ) );
    }

    private Continuum getContinuum()
        throws Exception
    {
        return (Continuum) lookup( Continuum.ROLE );
    }


}
