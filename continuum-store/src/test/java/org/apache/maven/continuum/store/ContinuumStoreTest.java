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

import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildDefinitionTemplateDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;

import javax.jdo.JDODetachedFieldAccessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 * @todo I think this should have all the JDO stuff from the abstract test, and the abstract test
 * should use a mock continuum store with the exception of the integration tests which should be
 * running against a fully deployed plexus application instead
 * @todo review for ambiguities and ensure it is all encapsulated in the store, otherwise the code may make the same mistake about not deleting things, etc
 */
public class ContinuumStoreTest
    extends AbstractContinuumStoreTestCase
{
    private static final int INVALID_ID = 15000;

    private BuildDefinitionTemplateDao buildDefinitionTemplateDao;

    protected BuildDefinitionDao buildDefinitionDao;

    protected BuildResultDao buildResultDao;

    // ----------------------------------------------------------------------
    //  TEST METHODS
    // ----------------------------------------------------------------------

    public void testAddProjectGroup()
        throws ContinuumStoreException
    {
        String name = "testAddProjectGroup";
        String description = "testAddProjectGroup description";
        String groupId = "org.apache.maven.continuum.test";
        LocalRepository repository = localRepositoryDao.getLocalRepository( testLocalRepository3.getId() );
        ProjectGroup group = createTestProjectGroup( name, description, groupId, repository );

        ProjectGroup copy = createTestProjectGroup( group );
        projectGroupDao.addProjectGroup( group );
        copy.setId( group.getId() );

        ProjectGroup retrievedGroup = projectGroupDao.getProjectGroup( group.getId() );
        assertProjectGroupEquals( copy, retrievedGroup );
        assertLocalRepositoryEquals( testLocalRepository3, retrievedGroup.getLocalRepository() );
    }

    public void testGetProjectGroup()
        throws ContinuumStoreException
    {
        ProjectGroup retrievedGroup = projectGroupDao.getProjectGroupWithProjects( defaultProjectGroup.getId() );
        assertProjectGroupEquals( defaultProjectGroup, retrievedGroup );
        assertLocalRepositoryEquals( testLocalRepository1, retrievedGroup.getLocalRepository() );

        List projects = retrievedGroup.getProjects();
        assertEquals( "Check number of projects", 2, projects.size() );
        assertTrue( "Check existence of project 1", projects.contains( testProject1 ) );
        assertTrue( "Check existence of project 2", projects.contains( testProject2 ) );

        checkProjectGroupDefaultFetchGroup( retrievedGroup );

        Project project = (Project) projects.get( 0 );
        checkProjectDefaultFetchGroup( project );
        //assertSame( "Check project group reference matches", project.getProjectGroup(), retrievedGroup );
        assertEquals( project.getProjectGroup().getId(), retrievedGroup.getId() );
        assertProjectEquals( testProject1, project );

        project = (Project) projects.get( 1 );
        checkProjectDefaultFetchGroup( project );
        //assertSame( "Check project group reference matches", project.getProjectGroup(), retrievedGroup );
        assertEquals( project.getProjectGroup().getId(), retrievedGroup.getId() );
        assertProjectEquals( testProject2, project );
    }

    public void testGetInvalidProjectGroup()
        throws ContinuumStoreException
    {
        try
        {
            projectGroupDao.getProjectGroup( INVALID_ID );
            fail( "Should not find group with invalid ID" );
        }
        catch ( ContinuumObjectNotFoundException expected )
        {
            assertTrue( true );
        }
    }

    public void testEditProjectGroup()
        throws ContinuumStoreException
    {
        ProjectGroup newGroup = projectGroupDao.getProjectGroup( testProjectGroup2.getId() );

        newGroup.setName( "testEditProjectGroup2" );
        newGroup.setDescription( "testEditProjectGroup updated description" );
        newGroup.setGroupId( "org.apache.maven.continuum.test.new" );

        ProjectGroup copy = createTestProjectGroup( newGroup );
        copy.setId( newGroup.getId() );
        projectGroupDao.updateProjectGroup( newGroup );

        ProjectGroup retrievedGroup = projectGroupDao.getProjectGroup( testProjectGroup2.getId() );
        assertProjectGroupEquals( copy, retrievedGroup );
        assertLocalRepositoryEquals( testLocalRepository2, retrievedGroup.getLocalRepository() );
    }

    public void testUpdateUndetachedGroup()
    {
        ProjectGroup newGroup = new ProjectGroup();
        newGroup.setId( testProjectGroup2.getId() );
        newGroup.setName( "testUpdateUndetachedGroup2" );
        newGroup.setDescription( "testUpdateUndetachedGroup updated description" );
        newGroup.setGroupId( "org.apache.maven.continuum.test.new" );

        try
        {
            projectGroupDao.updateProjectGroup( newGroup );
            fail( "Should not have succeeded" );
        }
        catch ( ContinuumStoreException expected )
        {
            // good!
            assertTrue( true );
        }
    }

    public void testGetAllProjectGroups()
    {
        Collection groups = projectGroupDao.getAllProjectGroupsWithProjects();

        assertEquals( "check size", 2, groups.size() );
        assertTrue( groups.contains( defaultProjectGroup ) );
        assertTrue( groups.contains( testProjectGroup2 ) );

        for ( Iterator i = groups.iterator(); i.hasNext(); )
        {
            ProjectGroup group = (ProjectGroup) i.next();
            List projects = group.getProjects();
            if ( group.getId() == testProjectGroup2.getId() )
            {
                assertProjectGroupEquals( testProjectGroup2, group );
                assertLocalRepositoryEquals( testLocalRepository2, group.getLocalRepository() );
                assertTrue( "check no projects", projects.isEmpty() );
            }
            else if ( group.getId() == defaultProjectGroup.getId() )
            {
                assertProjectGroupEquals( defaultProjectGroup, group );
                assertLocalRepositoryEquals( testLocalRepository1, group.getLocalRepository() );
                assertEquals( "Check number of projects", 2, projects.size() );
                assertTrue( "Check existence of project 1", projects.contains( testProject1 ) );
                assertTrue( "Check existence of project 2", projects.contains( testProject2 ) );

                checkProjectGroupDefaultFetchGroup( group );

                Project p = (Project) projects.get( 0 );
                checkProjectDefaultFetchGroup( p );
                assertSame( "Check project group reference matches", p.getProjectGroup(), group );
            }
        }
    }

    public void testGetProject()
        throws ContinuumStoreException
    {
        Project retrievedProject = projectDao.getProject( testProject1.getId() );
        assertProjectEquals( testProject1, retrievedProject );
        checkProjectDefaultFetchGroup( retrievedProject );
    }

    public void testGetProjectWithDetails()
        throws ContinuumStoreException
    {
        Project retrievedProject = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertProjectEquals( testProject1, retrievedProject );
        checkProjectFetchGroup( retrievedProject, false, false, true, true );

        assertBuildDefinitionsEqual( retrievedProject.getBuildDefinitions(), testProject1.getBuildDefinitions() );
        assertNotifiersEqual( testProject1.getNotifiers(), retrievedProject.getNotifiers() );
        assertDevelopersEqual( testProject1.getDevelopers(), retrievedProject.getDevelopers() );
        assertDependenciesEqual( testProject1.getDependencies(), retrievedProject.getDependencies() );
    }

    public void testGetProjectWithCheckoutResult()
        throws ContinuumStoreException
    {
        Project retrievedProject = projectDao.getProjectWithCheckoutResult( testProject1.getId() );
        assertProjectEquals( testProject1, retrievedProject );
        assertScmResultEquals( testCheckoutResult1, retrievedProject.getCheckoutResult() );
        checkProjectFetchGroup( retrievedProject, true, false, false, false );
    }

    public void testGetInvalidProject()
        throws ContinuumStoreException
    {
        try
        {
            projectDao.getProject( INVALID_ID );
            fail( "Should not find project with invalid ID" );
        }
        catch ( ContinuumObjectNotFoundException expected )
        {
            assertTrue( true );
        }
    }

    public void testEditProject()
        throws ContinuumStoreException
    {
        Project newProject = projectDao.getProject( testProject2.getId() );

        newProject.setName( "testEditProject2" );
        newProject.setDescription( "testEditProject updated description" );
        newProject.setGroupId( "org.apache.maven.continuum.test.new" );

        Project copy = createTestProject( newProject );
        copy.setId( newProject.getId() );
        projectDao.updateProject( newProject );

        Project retrievedProject = projectDao.getProject( testProject2.getId() );
        assertProjectEquals( copy, retrievedProject );

    }

    public void testUpdateUndetachedProject()
    {
        Project newProject = new Project();
        newProject.setId( testProject2.getId() );
        newProject.setName( "testUpdateUndetached2" );
        newProject.setDescription( "testUpdateUndetached updated description" );
        newProject.setGroupId( "org.apache.maven.continuum.test.new" );

        try
        {
            projectDao.updateProject( newProject );
            fail( "Should not have succeeded" );
        }
        catch ( ContinuumStoreException expected )
        {
            // good!
            assertTrue( true );
        }
    }

    public void testGetAllProjects()
    {
        List projects = projectDao.getAllProjectsByName();
        assertEquals( "check items", Arrays.asList( new Project[]{testProject1, testProject2} ), projects );

        Project project = (Project) projects.get( 1 );
        assertProjectEquals( testProject2, project );
        checkProjectDefaultFetchGroup( project );
        assertNotNull( "Check project group reference matches", project.getProjectGroup() );
    }

    public void testAddSchedule()
    {
        Schedule newSchedule = createTestSchedule( "testAddSchedule", "testAddSchedule desc", 10, "cron test", false );
        Schedule copy = createTestSchedule( newSchedule );
        scheduleDao.addSchedule( newSchedule );
        copy.setId( newSchedule.getId() );

        List schedules = scheduleDao.getAllSchedulesByName();
        Schedule retrievedSchedule = (Schedule) schedules.get( schedules.size() - 1 );
        assertScheduleEquals( copy, retrievedSchedule );
    }

    public void testEditSchedule()
        throws ContinuumStoreException
    {
        Schedule newSchedule = (Schedule) scheduleDao.getAllSchedulesByName().get( 0 );
        newSchedule.setName( "name1.1" );
        newSchedule.setDescription( "testEditSchedule updated description" );

        Schedule copy = createTestSchedule( newSchedule );
        copy.setId( newSchedule.getId() );
        scheduleDao.updateSchedule( newSchedule );

        Schedule retrievedSchedule = (Schedule) scheduleDao.getAllSchedulesByName().get( 0 );
        assertScheduleEquals( copy, retrievedSchedule );
    }

    public void testRemoveSchedule()
    {
        Schedule schedule = (Schedule) scheduleDao.getAllSchedulesByName().get( 2 );

        // TODO: test if it has any attachments

        scheduleDao.removeSchedule( schedule );

        List schedules = scheduleDao.getAllSchedulesByName();
        assertEquals( "check size", 2, schedules.size() );
        assertFalse( "check not there", schedules.contains( schedule ) );
    }

    public void testGetAllSchedules()
    {
        List schedules = scheduleDao.getAllSchedulesByName();

        assertEquals( "check item count", 3, schedules.size() );

        // check equality and order
        Schedule schedule = (Schedule) schedules.get( 0 );
        assertScheduleEquals( testSchedule1, schedule );
        schedule = (Schedule) schedules.get( 1 );
        assertScheduleEquals( testSchedule2, schedule );
        schedule = (Schedule) schedules.get( 2 );
        assertScheduleEquals( testSchedule3, schedule );
    }

    public void testAddProfile()
        throws Exception
    {
        List installations = installationDao.getAllInstallations();
        Profile newProfile = createTestProfile( "testAddProfile", "testAddProfile desc", 5, false, false,
                                                (Installation) installations.get( 1 ), (Installation) installations
            .get( 2 ) );
        Profile copy = createTestProfile( newProfile );
        profileDao.addProfile( newProfile );
        copy.setId( newProfile.getId() );

        List profiles = profileDao.getAllProfilesByName();
        Profile retrievedProfile = (Profile) profiles.get( profiles.size() - 1 );
        assertProfileEquals( copy, retrievedProfile );
        assertInstallationEquals( testInstallationMaven20a3, retrievedProfile.getBuilder() );
        assertInstallationEquals( testInstallationJava14, retrievedProfile.getJdk() );
    }

    public void testEditProfile()
        throws ContinuumStoreException
    {
        Profile newProfile = (Profile) profileDao.getAllProfilesByName().get( 0 );
        newProfile.setName( "name1.1" );
        newProfile.setDescription( "testEditProfile updated description" );

        Profile copy = createTestProfile( newProfile );
        copy.setId( newProfile.getId() );
        profileDao.updateProfile( newProfile );

        Profile retrievedProfile = (Profile) profileDao.getAllProfilesByName().get( 0 );
        assertProfileEquals( copy, retrievedProfile );
        assertInstallationEquals( copy.getBuilder(), retrievedProfile.getBuilder() );
        assertInstallationEquals( copy.getJdk(), retrievedProfile.getJdk() );

    }

    public void testRemoveProfile()
    {
        Profile profile = (Profile) profileDao.getAllProfilesByName().get( 2 );

        // TODO: test if it has any attachments

        profileDao.removeProfile( profile );

        List profiles = profileDao.getAllProfilesByName();
        assertEquals( "check size", 2, profiles.size() );
        assertFalse( "check not there", profiles.contains( profile ) );
    }

    public void testGetAllProfiles()
    {
        List profiles = profileDao.getAllProfilesByName();

        assertEquals( "check item count", 3, profiles.size() );

        // check equality and order
        Profile profile = (Profile) profiles.get( 0 );
        assertProfileEquals( testProfile1, profile );
        assertInstallationEquals( testProfile1.getBuilder(), profile.getBuilder() );
        assertInstallationEquals( testProfile1.getJdk(), profile.getJdk() );
        profile = (Profile) profiles.get( 1 );
        assertProfileEquals( testProfile2, profile );
        assertInstallationEquals( testProfile2.getBuilder(), profile.getBuilder() );
        assertInstallationEquals( testProfile2.getJdk(), profile.getJdk() );
        profile = (Profile) profiles.get( 2 );
        assertProfileEquals( testProfile3, profile );
        assertInstallationEquals( testProfile3.getBuilder(), profile.getBuilder() );
        assertInstallationEquals( testProfile3.getJdk(), profile.getJdk() );
    }

    /*
        public void testGetgetProfileByName()
            throws ContinuumStoreException
        {
            Profile profile = store.getProfileByName( "name1" );
            assertNotNull( profile );
        }
    */
    public void testGetAllInstallations()
        throws Exception
    {
        List installations = installationDao.getAllInstallations();

        assertEquals( "check item count", 3, installations.size() );

        // check equality and order
        Installation installation = (Installation) installations.get( 0 );
        assertInstallationEquals( testInstallationJava13, installation );
        installation = (Installation) installations.get( 1 );
        assertInstallationEquals( testInstallationJava14, installation );
        installation = (Installation) installations.get( 2 );
        assertInstallationEquals( testInstallationMaven20a3, installation );
    }

    public void testUpdateInstallation()
        throws Exception
    {
        String name = "installationTest";
        Installation testOne = createTestInstallation( name, InstallationService.JDK_TYPE, "varName", "varValue" );
        testOne = installationDao.addInstallation( testOne );

        Installation fromStore = installationDao.getInstallation( testOne.getInstallationId() );
        assertInstallationEquals( testOne, fromStore );

        fromStore.setVarName( "JAVA_HOME" );
        fromStore.setVarValue( "/usr/local/jdk1.5.0_08" );
        installationDao.updateInstallation( fromStore );

        Installation updatedFromStore = installationDao.getInstallation( testOne.getInstallationId() );

        assertInstallationEquals( fromStore, updatedFromStore );
    }

    public void testRemoveInstallation()
        throws Exception
    {
        String name = "installationTestRemove";
        Installation testOne = createTestInstallation( name, InstallationService.JDK_TYPE, "varName", "varValue" );
        testOne = installationDao.addInstallation( testOne );

        installationDao.removeInstallation( testOne );
        Installation fromStore = installationDao.getInstallation( testOne.getInstallationId() );
        assertNull( fromStore );
    }

    public void testRemoveLinkedInstallations()
        throws Exception
    {
        String nameFirstInst = "linkedFirstInstallationTestRemove";
        String nameSecondInst = "linkedSecondInstallationTestRemove";
        String nameFirstEnvVar = "firstEnvVar";
        String nameSecondEnvVar = "secondEnvVar";

        Installation testOne =
            createTestInstallation( nameFirstInst, InstallationService.JDK_TYPE, "varName", "varValue" );

        Installation testTwo =
            createTestInstallation( nameSecondInst, InstallationService.MAVEN2_TYPE, "varName", "varValue" );

        Installation firstEnvVar =
            createTestInstallation( nameFirstEnvVar, InstallationService.MAVEN2_TYPE, "varName", "varValue" );

        Installation secondEnvVar =
            createTestInstallation( nameSecondEnvVar, InstallationService.MAVEN2_TYPE, "varName", "varValue" );

        testOne = installationDao.addInstallation( testOne );
        testTwo = installationDao.addInstallation( testTwo );

        firstEnvVar = installationDao.addInstallation( firstEnvVar );
        secondEnvVar = installationDao.addInstallation( secondEnvVar );

        List<Installation> envVars = new ArrayList<Installation>( 2 );
        envVars.add( firstEnvVar );
        envVars.add( secondEnvVar );

        Profile firstProfile = createTestProfile( "first", "", 1, true, true, testOne, testTwo, envVars );

        Profile secondProfile = createTestProfile( "first", "", 1, true, true, testOne, testTwo, envVars );

        firstProfile = profileDao.addProfile( firstProfile );
        secondProfile = profileDao.addProfile( secondProfile );

        Profile firstGetted = profileDao.getProfile( firstProfile.getId() );
        Profile secondGetted = profileDao.getProfile( secondProfile.getId() );

        assertNotNull( firstGetted );
        assertNotNull( firstGetted.getJdk() );
        assertEquals( nameFirstInst, firstGetted.getJdk().getName() );

        assertNotNull( secondGetted );
        assertNotNull( secondGetted.getJdk() );
        assertEquals( nameFirstInst, secondGetted.getJdk().getName() );

        assertNotNull( firstGetted.getBuilder() );
        assertEquals( nameSecondInst, firstGetted.getBuilder().getName() );
        assertEquals( 2, firstGetted.getEnvironmentVariables().size() );

        assertNotNull( secondGetted.getBuilder() );
        assertEquals( nameSecondInst, secondGetted.getBuilder().getName() );
        assertEquals( 2, secondGetted.getEnvironmentVariables().size() );

        installationDao.removeInstallation( testOne );

        Installation fromStore = installationDao.getInstallation( testOne.getInstallationId() );
        assertNull( fromStore );

        firstGetted = profileDao.getProfile( firstProfile.getId() );
        secondGetted = profileDao.getProfile( secondProfile.getId() );
        assertNotNull( firstGetted );
        assertNull( firstGetted.getJdk() );
        assertNotNull( firstGetted.getBuilder() );
        assertEquals( 2, firstGetted.getEnvironmentVariables().size() );
        assertNotNull( secondGetted );
        assertNull( secondGetted.getJdk() );
        assertNotNull( secondGetted.getBuilder() );
        assertEquals( 2, secondGetted.getEnvironmentVariables().size() );
        // removing builder
        installationDao.removeInstallation( testTwo );

        firstGetted = profileDao.getProfile( firstProfile.getId() );
        secondGetted = profileDao.getProfile( secondProfile.getId() );

        assertNotNull( firstGetted );
        assertNull( firstGetted.getJdk() );
        assertNull( firstGetted.getBuilder() );
        assertEquals( 2, firstGetted.getEnvironmentVariables().size() );

        assertNotNull( secondGetted );
        assertNull( secondGetted.getJdk() );
        assertNull( secondGetted.getBuilder() );
        assertEquals( 2, secondGetted.getEnvironmentVariables().size() );

        // removing firstEnvVar
        installationDao.removeInstallation( firstEnvVar );
        firstGetted = profileDao.getProfile( firstProfile.getId() );
        secondGetted = profileDao.getProfile( secondProfile.getId() );
        assertNotNull( firstGetted );
        assertNull( firstGetted.getJdk() );
        assertNull( firstGetted.getBuilder() );
        assertEquals( 1, firstGetted.getEnvironmentVariables().size() );
        Installation env = (Installation) firstGetted.getEnvironmentVariables().get( 0 );
        assertEquals( nameSecondEnvVar, env.getName() );

        assertNotNull( secondGetted );
        assertNull( secondGetted.getJdk() );
        assertNull( secondGetted.getBuilder() );
        assertEquals( 1, secondGetted.getEnvironmentVariables().size() );
        env = (Installation) secondGetted.getEnvironmentVariables().get( 0 );
        assertEquals( nameSecondEnvVar, env.getName() );

        // removing secondEnvVar
        installationDao.removeInstallation( secondEnvVar );
        firstGetted = profileDao.getProfile( firstProfile.getId() );
        secondGetted = profileDao.getProfile( secondProfile.getId() );
        assertNotNull( firstGetted );
        assertNull( firstGetted.getJdk() );
        assertNull( firstGetted.getBuilder() );
        assertEquals( 0, firstGetted.getEnvironmentVariables().size() );

        assertNotNull( secondGetted );
        assertNull( secondGetted.getJdk() );
        assertNull( secondGetted.getBuilder() );
        assertEquals( 0, secondGetted.getEnvironmentVariables().size() );
    }

    public void testDeleteProject()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithBuilds( testProject1.getId() );

        projectDao.removeProject( project );

        ProjectGroup projectGroup = projectGroupDao.getProjectGroupWithProjects( defaultProjectGroup.getId() );
        assertEquals( "check size is now 1", 1, projectGroup.getProjects().size() );
        assertProjectEquals( testProject2, (Project) projectGroup.getProjects().get( 0 ) );

        confirmProjectDeletion( testProject1 );
    }

    public void testDeleteProjectGroup()
        throws ContinuumStoreException
    {
        projectGroupDao.removeProjectGroup( projectGroupDao.getProjectGroup( defaultProjectGroup.getId() ) );

        try
        {
            projectGroupDao.getProjectGroup( defaultProjectGroup.getId() );
            fail( "Project group was not deleted" );
        }
        catch ( ContinuumObjectNotFoundException expected )
        {
            assertTrue( true );
        }

        confirmProjectDeletion( testProject1 );
        confirmProjectDeletion( testProject2 );
        // TODO: test the project group's notifiers are physically deleted
        // TODO: test the project group's build definitions are physically deleted
    }

    public void testDeleteBuildResult()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithBuilds( testProject1.getId() );

        for ( Iterator i = project.getBuildResults().iterator(); i.hasNext(); )
        {
            BuildResult result = (BuildResult) i.next();
            if ( result.getId() == testBuildResult1.getId() )
            {
                i.remove();
            }
        }
        projectDao.updateProject( project );

        project = projectDao.getProjectWithBuilds( testProject1.getId() );
        assertEquals( "check size is now 1", 1, project.getBuildResults().size() );
        assertBuildResultEquals( testBuildResult2, (BuildResult) project.getBuildResults().get( 0 ) );

        List results = buildResultDao.getAllBuildsForAProjectByDate( testProject1.getId() );
        assertEquals( "check item count", 1, results.size() );
        assertBuildResultEquals( testBuildResult2, (BuildResult) results.get( 0 ) );

        // !! These actually aren't happening !!
        // TODO: test the build result was physically deleted
        // TODO: test the build result's SCM result was physically deleted
        // TODO: test the build result's SCM result's change sets and change files were physically deleted
    }

    public void testGetInvalidBuildResult()
        throws ContinuumStoreException
    {
        try
        {
            buildResultDao.getBuildResult( INVALID_ID );
            fail( "Should not find build result with invalid ID" );
        }
        catch ( ContinuumObjectNotFoundException expected )
        {
            assertTrue( true );
        }
    }

    public void testGetAllBuildsForAProject()
    {
        List results = buildResultDao.getAllBuildsForAProjectByDate( testProject1.getId() );

        assertEquals( "check item count", 2, results.size() );

        // check equality and order
        BuildResult buildResult = (BuildResult) results.get( 0 );
        assertBuildResultEquals( testBuildResult2, buildResult );
        assertProjectEquals( testProject1, buildResult.getProject() );
        checkBuildResultDefaultFetchGroup( buildResult );
        buildResult = (BuildResult) results.get( 1 );
        assertBuildResultEquals( testBuildResult1, buildResult );
        assertProjectEquals( testProject1, buildResult.getProject() );
        checkBuildResultDefaultFetchGroup( buildResult );
    }

    public void testGetBuildResult()
        throws ContinuumStoreException
    {
        BuildResult buildResult = buildResultDao.getBuildResult( testBuildResult3.getId() );
        assertBuildResultEquals( testBuildResult3, buildResult );
        assertScmResultEquals( testBuildResult3.getScmResult(), buildResult.getScmResult() );
        assertProjectEquals( testProject2, buildResult.getProject() );
        // TODO: reports, artifacts, data
    }

    public void testGetProjectGroupWithDetails()
        throws ContinuumStoreException
    {
        ProjectGroup retrievedGroup =
            projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup
                .getId() );
        assertProjectGroupEquals( defaultProjectGroup, retrievedGroup );
        assertNotifiersEqual( defaultProjectGroup.getNotifiers(), retrievedGroup.getNotifiers() );
        assertBuildDefinitionsEqual( retrievedGroup.getBuildDefinitions(), defaultProjectGroup.getBuildDefinitions() );

        List projects = retrievedGroup.getProjects();
        assertEquals( "Check number of projects", 2, projects.size() );

        Project project = (Project) projects.get( 0 );
        checkProjectFetchGroup( project, false, false, true, false );
        //assertSame( "Check project group reference matches", project.getProjectGroup(), retrievedGroup );
        assertEquals( project.getProjectGroup().getId(), retrievedGroup.getId() );
        assertProjectEquals( testProject1, project );
        assertNotifiersEqual( testProject1.getNotifiers(), project.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject1.getBuildDefinitions() );

        project = (Project) projects.get( 1 );
        checkProjectFetchGroup( project, false, false, true, false );
        //assertSame( "Check project group reference matches", project.getProjectGroup(), retrievedGroup );
        assertEquals( project.getProjectGroup().getId(), retrievedGroup.getId() );
        assertProjectEquals( testProject2, project );
        assertNotifiersEqual( testProject2.getNotifiers(), project.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject2.getBuildDefinitions() );
    }

    public void testGetAllProjectsGroupWithDetails()
    {
        List projectGroups = projectGroupDao.getAllProjectGroupsWithBuildDetails();
        ProjectGroup group1 = (ProjectGroup) projectGroups.get( 0 );
        assertProjectGroupEquals( defaultProjectGroup, group1 );
        assertNotifiersEqual( defaultProjectGroup.getNotifiers(), group1.getNotifiers() );
        assertBuildDefinitionsEqual( group1.getBuildDefinitions(), defaultProjectGroup.getBuildDefinitions() );
        ProjectGroup group2 = (ProjectGroup) projectGroups.get( 1 );
        assertProjectGroupEquals( testProjectGroup2, group2 );
        assertNotifiersEqual( testProjectGroup2.getNotifiers(), group2.getNotifiers() );
        assertBuildDefinitionsEqual( group2.getBuildDefinitions(), testProjectGroup2.getBuildDefinitions() );

        List projects = group1.getProjects();
        assertEquals( "Check number of projects", 2, projects.size() );

        Project project = (Project) projects.get( 0 );
        checkProjectFetchGroup( project, false, false, true, false );
        assertSame( "Check project group reference matches", project.getProjectGroup(), group1 );
        assertProjectEquals( testProject1, project );
        assertNotifiersEqual( testProject1.getNotifiers(), project.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject1.getBuildDefinitions() );

        project = (Project) projects.get( 1 );
        checkProjectFetchGroup( project, false, false, true, false );
        assertSame( "Check project group reference matches", project.getProjectGroup(), group1 );
        assertProjectEquals( testProject2, project );
        assertNotifiersEqual( testProject2.getNotifiers(), project.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject2.getBuildDefinitions() );

        projects = group2.getProjects();
        assertEquals( "Check number of projects", 0, projects.size() );
    }

    public void testAddDeveloperToProject()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );

        ProjectDeveloper developer = createTestDeveloper( 11, "email TADTP", "name TADTP", "scmId TADTP" );
        ProjectDeveloper copy = createTestDeveloper( developer );
        project.addDeveloper( developer );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # devs", 2, project.getDevelopers().size() );
        assertDeveloperEquals( copy, (ProjectDeveloper) project.getDevelopers().get( 1 ) );
    }

    public void testEditDeveloper()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );

        ProjectDeveloper newDeveloper = (ProjectDeveloper) project.getDevelopers().get( 0 );
        newDeveloper.setName( "name1.1" );
        newDeveloper.setEmail( "email1.1" );

        ProjectDeveloper copy = createTestDeveloper( newDeveloper );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # devs", 1, project.getDevelopers().size() );
        assertDeveloperEquals( copy, (ProjectDeveloper) project.getDevelopers().get( 0 ) );
    }

    public void testDeleteDeveloper()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        project.getDevelopers().remove( 0 );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check size is now 0", 0, project.getDevelopers().size() );

        // !! These actually aren't happening !!
        // TODO: test the developer was physically deleted
    }

    public void testAddDependencyToProject()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );

        ProjectDependency dependency = createTestDependency( "TADTP groupId", "TADTP artifactId", "TADTP version" );
        ProjectDependency copy = createTestDependency( dependency );
        project.addDependency( dependency );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # deps", 3, project.getDependencies().size() );
        assertDependencyEquals( copy, (ProjectDependency) project.getDependencies().get( 2 ) );
    }

    public void testEditDependency()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );

        ProjectDependency newDependency = (ProjectDependency) project.getDependencies().get( 0 );
        newDependency.setGroupId( "groupId1.1" );
        newDependency.setArtifactId( "artifactId1.1" );

        ProjectDependency copy = createTestDependency( newDependency );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # deps", 2, project.getDependencies().size() );
        assertDependencyEquals( copy, (ProjectDependency) project.getDependencies().get( 0 ) );
    }

    public void testDeleteDependency()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        ProjectDependency dependency = (ProjectDependency) project.getDependencies().get( 1 );
        project.getDependencies().remove( 0 );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check size is now 1", 1, project.getDependencies().size() );
        assertDependencyEquals( dependency, (ProjectDependency) project.getDependencies().get( 0 ) );

        // !! These actually aren't happening !!
        // TODO: test the dependency was physically deleted
    }

    public void testAddNotifierToProject()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );

        ProjectNotifier notifier = createTestNotifier( 13, true, false, true, "TADNTP type" );
        ProjectNotifier copy = createTestNotifier( notifier );
        project.addNotifier( notifier );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # notifiers", 2, project.getNotifiers().size() );
        assertNotifierEquals( copy, (ProjectNotifier) project.getNotifiers().get( 1 ) );
    }

    public void testEditNotifier()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );

        ProjectNotifier newNotifier = (ProjectNotifier) project.getNotifiers().get( 0 );
        // If we use "type1.1", jpox-rc2 store "type11", weird
        String type = "type11";
        newNotifier.setType( type );

        ProjectNotifier copy = createTestNotifier( newNotifier );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # notifiers", 1, project.getNotifiers().size() );
        assertNotifierEquals( copy, (ProjectNotifier) project.getNotifiers().get( 0 ) );
    }

    public void testDeleteNotifier()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        project.getNotifiers().remove( 0 );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check size is now 0", 0, project.getNotifiers().size() );

        // !! These actually aren't happening !!
        // TODO: test the notifier was physically deleted
    }

    public void testAddBuildDefinitionToProject()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );

        Profile profile = profileDao.getProfile( testProfile1.getId() );
        Schedule schedule = scheduleDao.getSchedule( testSchedule1.getId() );
        BuildDefinition buildDefinition = createTestBuildDefinition( "TABDTP arguments", "TABDTP buildFile",
                                                                     "TABDTP goals", profile, schedule, false, false );
        BuildDefinition copy = createTestBuildDefinition( buildDefinition );
        project.addBuildDefinition( buildDefinition );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # build defs", 3, project.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 2 );
        assertBuildDefinitionEquals( copy, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule1, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile1, retrievedBuildDefinition.getProfile() );
    }

    public void testEditBuildDefinition()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );

        BuildDefinition newBuildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 0 );
        newBuildDefinition.setBuildFresh( true );
        new BuildDefinition().setDefaultForProject( true );
        String arguments = "arguments1.1";
        newBuildDefinition.setArguments( arguments );

        BuildDefinition copy = createTestBuildDefinition( newBuildDefinition );
        buildDefinitionDao.storeBuildDefinition( newBuildDefinition );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # build defs", 2, project.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 0 );
        assertBuildDefinitionEquals( copy, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule1, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile2, retrievedBuildDefinition.getProfile() );
    }

    public void testDeleteBuildDefinition()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        BuildDefinition buildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 1 );
        project.getBuildDefinitions().remove( 0 );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check size is now 1", 1, project.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 0 );
        assertBuildDefinitionEquals( buildDefinition, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule2, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile2, retrievedBuildDefinition.getProfile() );

        // !! These actually aren't happening !!
        // TODO: test the def was physically deleted
        // TODO: test the schedule/profile was NOT physically deleted
    }

    public void testAddNotifierToProjectGroup()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup =
            projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );

        ProjectNotifier notifier = createTestNotifier( 14, true, false, true, "TADNTPG type" );
        ProjectNotifier copy = createTestNotifier( notifier );
        projectGroup.addNotifier( notifier );
        projectGroupDao.updateProjectGroup( projectGroup );

        projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );
        assertEquals( "check # notifiers", 3, projectGroup.getNotifiers().size() );
        assertNotifierEquals( copy, (ProjectNotifier) projectGroup.getNotifiers().get( 2 ) );
    }

    public void testEditGroupNotifier()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup =
            projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );

        ProjectNotifier newNotifier = (ProjectNotifier) projectGroup.getNotifiers().get( 0 );
        // If we use "type1.1", jpox-rc2 store "type1", weird
        String type = "type1";
        newNotifier.setType( type );

        ProjectNotifier copy = createTestNotifier( newNotifier );
        projectGroupDao.updateProjectGroup( projectGroup );

        projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );
        assertEquals( "check # notifiers", 2, projectGroup.getNotifiers().size() );
        assertNotifierEquals( copy, (ProjectNotifier) projectGroup.getNotifiers().get( 0 ) );
    }

    public void testDeleteGroupNotifier()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup =
            projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );
        ProjectNotifier notifier = (ProjectNotifier) projectGroup.getNotifiers().get( 1 );
        projectGroup.getNotifiers().remove( 0 );
        projectGroupDao.updateProjectGroup( projectGroup );

        projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );
        assertEquals( "check size is now 1", 1, projectGroup.getNotifiers().size() );
        assertNotifierEquals( notifier, (ProjectNotifier) projectGroup.getNotifiers().get( 0 ) );

        // !! These actually aren't happening !!
        // TODO: test the notifier was physically deleted
    }

    public void testAddBuildDefinitionToProjectGroup()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup =
            projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );

        Profile profile = profileDao.getProfile( testProfile1.getId() );
        Schedule schedule = scheduleDao.getSchedule( testSchedule1.getId() );
        BuildDefinition buildDefinition = createTestBuildDefinition( "TABDTPG arguments", "TABDTPG buildFile",
                                                                     "TABDTPG goals", profile, schedule, false, false );
        BuildDefinition copy = createTestBuildDefinition( buildDefinition );
        projectGroup.addBuildDefinition( buildDefinition );
        projectGroupDao.updateProjectGroup( projectGroup );

        projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );
        assertEquals( "check # build defs", 2, projectGroup.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) projectGroup.getBuildDefinitions().get( 1 );
        assertBuildDefinitionEquals( copy, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule1, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile1, retrievedBuildDefinition.getProfile() );
    }

    public void testEditGroupBuildDefinition()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup =
            projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );

        BuildDefinition newBuildDefinition = (BuildDefinition) projectGroup.getBuildDefinitions().get( 0 );
        // If we use "arguments1.1", jpox-rc2 store "arguments11", weird
        String arguments = "arguments1";
        newBuildDefinition.setArguments( arguments );

        BuildDefinition copy = createTestBuildDefinition( newBuildDefinition );
        projectGroupDao.updateProjectGroup( projectGroup );

        projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );
        assertEquals( "check # build defs", 1, projectGroup.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) projectGroup.getBuildDefinitions().get( 0 );
        assertBuildDefinitionEquals( copy, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule2, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile1, retrievedBuildDefinition.getProfile() );
    }

    public void testDeleteGroupBuildDefinition()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup =
            projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );
        projectGroup.getBuildDefinitions().remove( 0 );
        projectGroupDao.updateProjectGroup( projectGroup );

        projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );
        assertEquals( "check size is now 0", 0, projectGroup.getBuildDefinitions().size() );

        // !! These actually aren't happening !!
        // TODO: test the def was physically deleted
        // TODO: test the schedule/profile was NOT physically deleted
    }

    public void testgetTemplatesBuildDefinitions()
        throws Exception
    {

        int all = buildDefinitionDao.getAllBuildDefinitions().size();
        BuildDefinition buildDefinition = new BuildDefinition();
        buildDefinition.setBuildFile( "pom.xml" );
        buildDefinition.setGoals( "clean" );
        buildDefinition.setTemplate( true );
        BuildDefinitionTemplate template = new BuildDefinitionTemplate();
        template.setName( "test" );
        template.setContinuumDefault( true );
        template.setType( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );
        template = buildDefinitionTemplateDao.addBuildDefinitionTemplate( template );
        buildDefinition = buildDefinitionDao.addBuildDefinition( buildDefinition );

        template.addBuildDefinition( buildDefinition );

        template = buildDefinitionTemplateDao.updateBuildDefinitionTemplate( template );

        assertEquals( "test", template.getName() );
        assertTrue( template.isContinuumDefault() );
        assertEquals( 1, template.getBuildDefinitions().size() );
        assertEquals( all + 1, buildDefinitionDao.getAllBuildDefinitions().size() );
        assertEquals( 1, buildDefinitionTemplateDao.getAllBuildDefinitionTemplate().size() );

        template = buildDefinitionTemplateDao
            .getContinuumBuildDefinitionTemplateWithType( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );

        assertNotNull( template );
        assertEquals( 1, template.getBuildDefinitions().size() );

        assertEquals( 1, buildDefinitionTemplateDao.getAllBuildDefinitionTemplate().size() );
    }

    public void testAddLocalRepository()
        throws Exception
    {
        String name = "testAddLocalRepository";
        String directory = "testAddLocalRepositoryDirectory";
        String layout = "default";

        LocalRepository repository = createTestLocalRepository( name, directory, layout );

        LocalRepository copy = createTestLocalRepository( repository );
        localRepositoryDao.addLocalRepository( repository );
        copy.setId( repository.getId() );

        LocalRepository retrievedRepository = localRepositoryDao.getLocalRepository( repository.getId() );
        assertLocalRepositoryEquals( copy, retrievedRepository );
    }

    public void testRemoveLocalRepository()
        throws Exception
    {
        LocalRepository repository = localRepositoryDao.getLocalRepositoryByName( testLocalRepository2.getName() );

        ProjectGroup projectGroup = projectGroupDao.getProjectGroupByGroupId( testProjectGroup2.getGroupId() );
        assertLocalRepositoryEquals( testLocalRepository2, projectGroup.getLocalRepository() );
        projectGroup.setLocalRepository( null );

        ProjectGroup copy = createTestProjectGroup( projectGroup );
        projectGroupDao.updateProjectGroup( projectGroup );

        projectGroup = projectGroupDao.getProjectGroup( testProjectGroup2.getId() );
        assertNull( "check local repository", projectGroup.getLocalRepository() );

        List<RepositoryPurgeConfiguration> repoPurgeList =
            repositoryPurgeConfigurationDao.getRepositoryPurgeConfigurationsByLocalRepository( repository.getId() );

        assertEquals( "check # repo purge config", 1, repoPurgeList.size() );
        repositoryPurgeConfigurationDao.removeRepositoryPurgeConfiguration( repoPurgeList.get( 0 ) );
        localRepositoryDao.removeLocalRepository( repository );

        List<LocalRepository> localRepositories = localRepositoryDao.getAllLocalRepositories();
        assertEquals( "check # local repositories", 2, localRepositories.size() );
        assertFalse( "check not there", localRepositories.contains( repository ) );
    }

    public void testGetAllLocalRepositories()
        throws Exception
    {
        List<LocalRepository> localRepositories = localRepositoryDao.getAllLocalRepositories();

        assertEquals( "check # local repositories", 3, localRepositories.size() );
        assertLocalRepositoryEquals( testLocalRepository1, localRepositories.get( 0 ) );
        assertLocalRepositoryEquals( testLocalRepository2, localRepositories.get( 1 ) );
        assertLocalRepositoryEquals( testLocalRepository3, localRepositories.get( 2 ) );
    }

    public void testAddRepositoryPurgeConfiguration()
        throws Exception
    {
        LocalRepository repository = localRepositoryDao.getLocalRepository( testLocalRepository3.getId() );
        Schedule schedule = scheduleDao.getSchedule( testSchedule1.getId() );

        RepositoryPurgeConfiguration repoPurge =
            createTestRepositoryPurgeConfiguration( true, 2, 100, false, schedule, true, repository );

        RepositoryPurgeConfiguration copy = createTestRepositoryPurgeConfiguration( repoPurge );
        repositoryPurgeConfigurationDao.addRepositoryPurgeConfiguration( repoPurge );
        copy.setId( repoPurge.getId() );

        RepositoryPurgeConfiguration retrieved =
            repositoryPurgeConfigurationDao.getRepositoryPurgeConfiguration( repoPurge.getId() );
        assertRepositoryPurgeConfigurationEquals( copy, retrieved );
        assertLocalRepositoryEquals( testLocalRepository3, retrieved.getRepository() );
        assertScheduleEquals( testSchedule1, retrieved.getSchedule() );
    }

    public void testRemoveRepositoryPurgeConfiguration()
        throws Exception
    {
        RepositoryPurgeConfiguration repoPurge =
            repositoryPurgeConfigurationDao.getRepositoryPurgeConfiguration( testRepoPurgeConfiguration2.getId() );
        repositoryPurgeConfigurationDao.removeRepositoryPurgeConfiguration( repoPurge );

        List<RepositoryPurgeConfiguration> repoPurgeList =
            repositoryPurgeConfigurationDao.getAllRepositoryPurgeConfigurations();
        assertEquals( "check # repo purge configurations", 2, repoPurgeList.size() );
        assertFalse( "check not there", repoPurgeList.contains( repoPurge ) );
    }

    public void testAddDirectoryPurgeConfiguration()
        throws Exception
    {
        String location = "release-directory";
        String directoryType = "release";

        Schedule schedule = scheduleDao.getSchedule( testSchedule1.getId() );
        DirectoryPurgeConfiguration dirPurge =
            createTestDirectoryPurgeConfiguration( location, directoryType, false, 2, 100, schedule, true );

        DirectoryPurgeConfiguration copy = createTestDirectoryPurgeConfiguration( dirPurge );
        directoryPurgeConfigurationDao.addDirectoryPurgeConfiguration( dirPurge );
        copy.setId( dirPurge.getId() );

        DirectoryPurgeConfiguration retrieved =
            directoryPurgeConfigurationDao.getDirectoryPurgeConfiguration( dirPurge.getId() );
        assertDirectoryPurgeConfigurationEquals( copy, retrieved );
        assertScheduleEquals( testSchedule1, retrieved.getSchedule() );
    }

    public void testRemoveDirectoryPurgeConfiguration()
        throws Exception
    {
        DirectoryPurgeConfiguration dirPurge =
            directoryPurgeConfigurationDao.getDirectoryPurgeConfiguration( testDirectoryPurgeConfig.getId() );
        directoryPurgeConfigurationDao.removeDirectoryPurgeConfiguration( dirPurge );

        List<DirectoryPurgeConfiguration> dirPurgeList =
            directoryPurgeConfigurationDao.getAllDirectoryPurgeConfigurations();
        assertEquals( "check #  dir purge configurations", 0, dirPurgeList.size() );
    }

    public void testGetPurgeConfigurationsBySchedule()
        throws Exception
    {
        List<RepositoryPurgeConfiguration> repoPurgeList =
            repositoryPurgeConfigurationDao.getRepositoryPurgeConfigurationsBySchedule( testSchedule2.getId() );
        List<DirectoryPurgeConfiguration> dirPurgeList =
            directoryPurgeConfigurationDao.getDirectoryPurgeConfigurationsBySchedule( testSchedule2.getId() );

        assertEquals( "check # repo purge configurations", 2, repoPurgeList.size() );
        assertEquals( "check # dir purge configurations", 1, dirPurgeList.size() );

        assertRepositoryPurgeConfigurationEquals( testRepoPurgeConfiguration1, repoPurgeList.get( 0 ) );
        assertRepositoryPurgeConfigurationEquals( testRepoPurgeConfiguration3, repoPurgeList.get( 1 ) );
        assertDirectoryPurgeConfigurationEquals( testDirectoryPurgeConfig, dirPurgeList.get( 0 ) );
    }

    // ----------------------------------------------------------------------
    //  HELPER METHODS
    // ----------------------------------------------------------------------

    private void confirmProjectDeletion( Project project )
        throws ContinuumStoreException
    {
        try
        {
            projectDao.getProject( project.getId() );
            fail( "Project should no longer exist" );
        }
        catch ( ContinuumObjectNotFoundException expected )
        {
            assertTrue( true );
        }

        // !! These actually aren't happening !!
        // TODO: test the project's checkout SCM result was physically deleted
        // TODO: test the project's checkout SCM result's change sets and change files were physically deleted
        // TODO: test the project's dependencies are physically deleted
        // TODO: test the project's developers are physically deleted
        // TODO: test the project's builds are physically deleted
        // TODO: test the project's notifiers are physically deleted
        // TODO: test the project's build definitions are physically deleted
    }

    private static void checkProjectGroupDefaultFetchGroup( ProjectGroup retrievedGroup )
    {
        try
        {
            retrievedGroup.getBuildDefinitions();
            fail( "buildDefinitions should not be in the default fetch group" );
        }
        catch ( JDODetachedFieldAccessException expected )
        {
            assertTrue( true );
        }

        try
        {
            retrievedGroup.getNotifiers();
            fail( "notifiers should not be in the default fetch group" );
        }
        catch ( JDODetachedFieldAccessException expected )
        {
            assertTrue( true );
        }
    }

    private static void checkProjectDefaultFetchGroup( Project project )
    {
        checkProjectFetchGroup( project, false, false, false, false );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        buildDefinitionDao = (BuildDefinitionDao) lookup( BuildDefinitionDao.class.getName() );

        buildDefinitionTemplateDao = (BuildDefinitionTemplateDao) lookup( BuildDefinitionTemplateDao.class.getName() );

        buildResultDao = (BuildResultDao) lookup( BuildResultDao.class.getName() );

        createBuildDatabase();
    }

    private static void checkProjectFetchGroup( Project project, boolean checkoutFetchGroup,
                                                boolean buildResultsFetchGroup, boolean detailsFetchGroup,
                                                boolean fineDetailsFetchGroup )
    {
        if ( !fineDetailsFetchGroup )
        {
            try
            {
                project.getDevelopers();

                fail( "developers should not be in the default fetch group" );
            }
            catch ( JDODetachedFieldAccessException expected )
            {
                assertTrue( true );
            }

            try
            {
                project.getDependencies();

                fail( "dependencies should not be in the default fetch group" );
            }
            catch ( JDODetachedFieldAccessException expected )
            {
                assertTrue( true );
            }
        }

        if ( !detailsFetchGroup )
        {
            try
            {
                project.getNotifiers();

                fail( "notifiers should not be in the default fetch group" );
            }
            catch ( JDODetachedFieldAccessException expected )
            {
                assertTrue( true );
            }

            try
            {
                project.getBuildDefinitions();

                fail( "buildDefinitions should not be in the default fetch group" );
            }
            catch ( JDODetachedFieldAccessException expected )
            {
                assertTrue( true );
            }
        }

        if ( !checkoutFetchGroup )
        {
            try
            {
                project.getCheckoutResult();

                fail( "checkoutResult should not be in the fetch group" );
            }
            catch ( JDODetachedFieldAccessException expected )
            {
                assertTrue( true );
            }
        }

        if ( !buildResultsFetchGroup )
        {
            try
            {
                project.getBuildResults();

                fail( "buildResults should not be in the default fetch group" );
            }
            catch ( JDODetachedFieldAccessException expected )
            {
                assertTrue( true );
            }
        }
    }

    private static void checkBuildResultDefaultFetchGroup( BuildResult buildResult )
    {
        try
        {
            buildResult.getScmResult();

            fail( "scmResult should not be in the default fetch group" );
        }
        catch ( JDODetachedFieldAccessException expected )
        {
            assertTrue( true );
        }
        // TODO: artifacts
        // TODO: report
        // TODO: long error data
    }

}
