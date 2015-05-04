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
import org.apache.continuum.model.project.ProjectGroupSummary;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.model.release.ContinuumReleaseResult;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.*;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.junit.Before;
import org.junit.Test;

import javax.jdo.JDODetachedFieldAccessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
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

    @Test
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

    @Test
    public void testGetProjectGroup()
        throws ContinuumStoreException
    {
        ProjectGroup retrievedGroup = projectGroupDao.getProjectGroupWithProjects( defaultProjectGroup.getId() );
        assertProjectGroupEquals( defaultProjectGroup, retrievedGroup );
        assertLocalRepositoryEquals( testLocalRepository1, retrievedGroup.getLocalRepository() );

        List<Project> projects = retrievedGroup.getProjects();
        assertEquals( "Check number of projects", 2, projects.size() );
        assertTrue( "Check existence of project 1", projects.contains( testProject1 ) );
        assertTrue( "Check existence of project 2", projects.contains( testProject2 ) );

        checkProjectGroupDefaultFetchGroup( retrievedGroup );

        Project project = projects.get( 0 );
        checkProjectDefaultFetchGroup( project );

        assertEquals( project.getProjectGroup().getId(), retrievedGroup.getId() );
        assertProjectEquals( testProject1, project );

        project = projects.get( 1 );
        checkProjectDefaultFetchGroup( project );

        assertEquals( project.getProjectGroup().getId(), retrievedGroup.getId() );
        assertProjectEquals( testProject2, project );
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testGetAllProjectGroups()
    {
        Collection<ProjectGroup> groups = projectGroupDao.getAllProjectGroupsWithProjects();

        assertEquals( "check size", 2, groups.size() );
        assertTrue( groups.contains( defaultProjectGroup ) );
        assertTrue( groups.contains( testProjectGroup2 ) );

        for ( ProjectGroup group : groups )
        {
            List<Project> projects = group.getProjects();
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

                Project p = projects.get( 0 );
                checkProjectDefaultFetchGroup( p );
                assertSame( "Check project group reference matches", p.getProjectGroup(), group );
            }
        }
    }

    @Test
    public void testGetProject()
        throws ContinuumStoreException
    {
        Project retrievedProject = projectDao.getProject( testProject1.getId() );
        assertProjectEquals( testProject1, retrievedProject );
        checkProjectDefaultFetchGroup( retrievedProject );
    }

    @Test
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

    @Test
    public void testGetProjectWithCheckoutResult()
        throws ContinuumStoreException
    {
        Project retrievedProject = projectDao.getProjectWithCheckoutResult( testProject1.getId() );
        assertProjectEquals( testProject1, retrievedProject );
        assertScmResultEquals( testCheckoutResult1, retrievedProject.getCheckoutResult() );
        checkProjectFetchGroup( retrievedProject, true, false, false, false );
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testGetAllProjects()
    {
        List<Project> projects = projectDao.getAllProjectsByName();
        assertEquals( "check items", Arrays.asList( testProject1, testProject2 ), projects );

        Project project = projects.get( 1 );
        assertProjectEquals( testProject2, project );
        checkProjectDefaultFetchGroup( project );
        assertNotNull( "Check project group reference matches", project.getProjectGroup() );
    }

    @Test
    public void testAddSchedule()
        throws ContinuumStoreException
    {
        BuildQueue buildQueue = buildQueueDao.getAllBuildQueues().get( 0 );

        Schedule newSchedule = createTestSchedule( "testAddSchedule", "testAddSchedule desc", 10, "cron test", false );
        newSchedule.addBuildQueue( buildQueue );

        Schedule copy = createTestSchedule( newSchedule );
        scheduleDao.addSchedule( newSchedule );
        copy.setId( newSchedule.getId() );

        List<Schedule> schedules = scheduleDao.getAllSchedulesByName();
        Schedule retrievedSchedule = schedules.get( schedules.size() - 1 );
        assertScheduleEquals( copy, retrievedSchedule );
        assertEquals( "check size of build queues", 1, retrievedSchedule.getBuildQueues().size() );
        assertBuildQueueEquals( buildQueue, retrievedSchedule.getBuildQueues().get( 0 ) );
    }

    @Test
    public void testEditSchedule()
        throws ContinuumStoreException
    {
        Schedule newSchedule = scheduleDao.getAllSchedulesByName().get( 0 );
        newSchedule.setName( "name1.1" );
        newSchedule.setDescription( "testEditSchedule updated description" );

        assertEquals( "check size of build queues", 2, newSchedule.getBuildQueues().size() );
        BuildQueue buildQueue1 = newSchedule.getBuildQueues().get( 0 );
        BuildQueue buildQueue2 = newSchedule.getBuildQueues().get( 1 );

        Schedule copy = createTestSchedule( newSchedule );
        copy.setId( newSchedule.getId() );
        scheduleDao.updateSchedule( newSchedule );

        Schedule retrievedSchedule = scheduleDao.getAllSchedulesByName().get( 0 );
        assertScheduleEquals( copy, retrievedSchedule );
        assertBuildQueueEquals( buildQueue1, retrievedSchedule.getBuildQueues().get( 0 ) );
        assertBuildQueueEquals( buildQueue2, retrievedSchedule.getBuildQueues().get( 1 ) );
    }

    @Test
    public void testRemoveSchedule()
    {
        Schedule schedule = scheduleDao.getAllSchedulesByName().get( 2 );

        // TODO: test if it has any attachments
        assertEquals( "check size of build queues", 0, schedule.getBuildQueues().size() );
        scheduleDao.removeSchedule( schedule );

        List<Schedule> schedules = scheduleDao.getAllSchedulesByName();
        assertEquals( "check size", 2, schedules.size() );
        assertFalse( "check not there", schedules.contains( schedule ) );
    }

    @Test
    public void testGetAllSchedules()
        throws ContinuumStoreException
    {
        List<Schedule> schedules = scheduleDao.getAllSchedulesByName();
        List<BuildQueue> buildQueues = buildQueueDao.getAllBuildQueues();

        assertEquals( "check item count", 3, schedules.size() );
        assertEquals( "check build queues count", 3, buildQueues.size() );

        BuildQueue buildQueue1 = buildQueues.get( 0 );
        BuildQueue buildQueue2 = buildQueues.get( 1 );
        BuildQueue buildQueue3 = buildQueues.get( 2 );

        // check equality and order
        Schedule schedule = schedules.get( 0 );
        assertScheduleEquals( testSchedule1, schedule );
        assertEquals( "check size of buildQueues", 2, schedule.getBuildQueues().size() );
        assertBuildQueueEquals( buildQueue1, schedule.getBuildQueues().get( 0 ) );
        assertBuildQueueEquals( buildQueue2, schedule.getBuildQueues().get( 1 ) );

        schedule = schedules.get( 1 );
        assertScheduleEquals( testSchedule2, schedule );
        assertEquals( "check size of buildQueues", 2, schedule.getBuildQueues().size() );
        assertBuildQueueEquals( buildQueue2, schedule.getBuildQueues().get( 0 ) );
        assertBuildQueueEquals( buildQueue3, schedule.getBuildQueues().get( 1 ) );

        schedule = schedules.get( 2 );
        assertScheduleEquals( testSchedule3, schedule );
        assertEquals( "check size of buildQueues", 0, schedule.getBuildQueues().size() );
    }

    @Test
    public void testAddProfile()
        throws Exception
    {
        List<Installation> installations = installationDao.getAllInstallations();
        Profile newProfile = createTestProfile( "testAddProfile", "testAddProfile desc", 5, false, false,
                                                installations.get( 1 ), installations.get( 2 ) );
        Profile copy = createTestProfile( newProfile );
        profileDao.addProfile( newProfile );
        copy.setId( newProfile.getId() );

        List<Profile> profiles = profileDao.getAllProfilesByName();
        Profile retrievedProfile = profiles.get( profiles.size() - 1 );
        assertProfileEquals( copy, retrievedProfile );
        assertInstallationEquals( testInstallationMaven20a3, retrievedProfile.getBuilder() );
        assertInstallationEquals( testInstallationJava14, retrievedProfile.getJdk() );
    }

    @Test
    public void testEditProfile()
        throws ContinuumStoreException
    {
        Profile newProfile = profileDao.getAllProfilesByName().get( 0 );
        newProfile.setName( "name1.1" );
        newProfile.setDescription( "testEditProfile updated description" );

        Profile copy = createTestProfile( newProfile );
        copy.setId( newProfile.getId() );
        profileDao.updateProfile( newProfile );

        Profile retrievedProfile = profileDao.getAllProfilesByName().get( 0 );
        assertProfileEquals( copy, retrievedProfile );
        assertInstallationEquals( copy.getBuilder(), retrievedProfile.getBuilder() );
        assertInstallationEquals( copy.getJdk(), retrievedProfile.getJdk() );

    }

    @Test
    public void testRemoveProfile()
    {
        Profile profile = profileDao.getAllProfilesByName().get( 2 );

        // TODO: test if it has any attachments

        profileDao.removeProfile( profile );

        List<Profile> profiles = profileDao.getAllProfilesByName();
        assertEquals( "check size", 3, profiles.size() );
        assertFalse( "check not there", profiles.contains( profile ) );
    }

    @Test
    public void testGetAllProfiles()
    {
        List<Profile> profiles = profileDao.getAllProfilesByName();

        assertEquals( "check item count", 4, profiles.size() );

        // check equality and order
        Profile profile = profiles.get( 0 );
        assertProfileEquals( testProfile1, profile );
        assertInstallationEquals( testProfile1.getBuilder(), profile.getBuilder() );
        assertInstallationEquals( testProfile1.getJdk(), profile.getJdk() );
        profile = profiles.get( 1 );
        assertProfileEquals( testProfile2, profile );
        assertInstallationEquals( testProfile2.getBuilder(), profile.getBuilder() );
        assertInstallationEquals( testProfile2.getJdk(), profile.getJdk() );
        profile = profiles.get( 2 );
        assertProfileEquals( testProfile3, profile );
        assertInstallationEquals( testProfile3.getBuilder(), profile.getBuilder() );
        assertInstallationEquals( testProfile3.getJdk(), profile.getJdk() );
        profile = profiles.get( 3 );
        assertProfileEquals( testProfile4, profile );
        assertInstallationEquals( testProfile4.getBuilder(), profile.getBuilder() );
        assertInstallationEquals( testProfile4.getJdk(), profile.getJdk() );
        assertEquals( "check env var count", 1, profile.getEnvironmentVariables().size() );
        assertInstallationEquals( testProfile4.getEnvironmentVariables().get( 0 ),
                                  profile.getEnvironmentVariables().get( 0 ) );
    }

    @Test
    public void testGetAllInstallations()
        throws Exception
    {
        List<Installation> installations = installationDao.getAllInstallations();

        assertEquals( "check item count", 4, installations.size() );

        // check equality and order
        Installation installation = installations.get( 0 );
        assertInstallationEquals( testInstallationJava13, installation );
        installation = installations.get( 1 );
        assertInstallationEquals( testInstallationJava14, installation );
        installation = installations.get( 2 );
        assertInstallationEquals( testInstallationMaven20a3, installation );
        installation = installations.get( 3 );
        assertInstallationEquals( testInstallationEnvVar, installation );
    }

    @Test
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

    @Test
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

    @Test
    public void testRemoveLinkedInstallations()
        throws Exception
    {
        String nameFirstInst = "linkedFirstInstallationTestRemove";
        String nameSecondInst = "linkedSecondInstallationTestRemove";
        String nameFirstEnvVar = "firstEnvVar";
        String nameSecondEnvVar = "secondEnvVar";

        Installation testOne = createTestInstallation( nameFirstInst, InstallationService.JDK_TYPE, "varName",
                                                       "varValue" );

        Installation testTwo = createTestInstallation( nameSecondInst, InstallationService.MAVEN2_TYPE, "varName",
                                                       "varValue" );

        Installation firstEnvVar = createTestInstallation( nameFirstEnvVar, InstallationService.MAVEN2_TYPE, "varName",
                                                           "varValue" );

        Installation secondEnvVar = createTestInstallation( nameSecondEnvVar, InstallationService.MAVEN2_TYPE,
                                                            "varName", "varValue" );

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
        Installation env = firstGetted.getEnvironmentVariables().get( 0 );
        assertEquals( nameSecondEnvVar, env.getName() );

        assertNotNull( secondGetted );
        assertNull( secondGetted.getJdk() );
        assertNull( secondGetted.getBuilder() );
        assertEquals( 1, secondGetted.getEnvironmentVariables().size() );
        env = secondGetted.getEnvironmentVariables().get( 0 );
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

    @Test
    public void testDeleteProject()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithBuilds( testProject1.getId() );

        projectDao.removeProject( project );

        ProjectGroup projectGroup = projectGroupDao.getProjectGroupWithProjects( defaultProjectGroup.getId() );
        assertEquals( "check size is now 1", 1, projectGroup.getProjects().size() );
        assertProjectEquals( testProject2, projectGroup.getProjects().get( 0 ) );

        confirmProjectDeletion( testProject1 );
    }

    @Test
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

    @Test
    public void testDeleteBuildResult()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithBuilds( testProject1.getId() );

        for ( Iterator<BuildResult> i = project.getBuildResults().iterator(); i.hasNext(); )
        {
            BuildResult result = i.next();
            if ( result.getId() == testBuildResult1.getId() )
            {
                i.remove();
            }
        }
        projectDao.updateProject( project );

        project = projectDao.getProjectWithBuilds( testProject1.getId() );
        assertEquals( "check size is now 1", 1, project.getBuildResults().size() );
        assertBuildResultEquals( testBuildResult2, project.getBuildResults().get( 0 ) );

        List<BuildResult> results = buildResultDao.getAllBuildsForAProjectByDate( testProject1.getId() );
        assertEquals( "check item count", 1, results.size() );
        assertBuildResultEquals( testBuildResult2, results.get( 0 ) );

        // !! These actually aren't happening !!
        // TODO: test the build result was physically deleted
        // TODO: test the build result's SCM result was physically deleted
        // TODO: test the build result's SCM result's change sets and change files were physically deleted
    }

    @Test
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

    @Test
    public void testGetAllBuildsForAProject()
    {
        List<BuildResult> results = buildResultDao.getAllBuildsForAProjectByDate( testProject1.getId() );

        assertEquals( "check item count", 2, results.size() );

        // check equality and order
        BuildResult buildResult = results.get( 0 );
        assertBuildResultEquals( testBuildResult2, buildResult );
        assertProjectEquals( testProject1, buildResult.getProject() );
        //checkBuildResultDefaultFetchGroup( buildResult );
        buildResult = results.get( 1 );
        assertBuildResultEquals( testBuildResult1, buildResult );
        assertProjectEquals( testProject1, buildResult.getProject() );
        //checkBuildResultDefaultFetchGroup( buildResult );
    }

    @Test
    public void testGetBuildResult()
        throws ContinuumStoreException
    {
        BuildResult buildResult = buildResultDao.getBuildResult( testBuildResult3.getId() );
        assertBuildResultEquals( testBuildResult3, buildResult );
        //assertScmResultEquals( testBuildResult3.getScmResult(), buildResult.getScmResult() );
        assertProjectEquals( testProject2, buildResult.getProject() );
        // TODO: reports, artifacts, data
    }

    @Test
    public void testGetProjectGroupWithDetails()
        throws ContinuumStoreException
    {
        ProjectGroup retrievedGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId(
            defaultProjectGroup.getId() );
        assertProjectGroupEquals( defaultProjectGroup, retrievedGroup );
        assertNotifiersEqual( defaultProjectGroup.getNotifiers(), retrievedGroup.getNotifiers() );
        assertBuildDefinitionsEqual( retrievedGroup.getBuildDefinitions(), defaultProjectGroup.getBuildDefinitions() );

        List<Project> projects = retrievedGroup.getProjects();
        assertEquals( "Check number of projects", 2, projects.size() );

        Project project = projects.get( 0 );
        checkProjectFetchGroup( project, false, false, true, false );
        //assertSame( "Check project group reference matches", project.getProjectGroup(), retrievedGroup );
        assertEquals( project.getProjectGroup().getId(), retrievedGroup.getId() );
        assertProjectEquals( testProject1, project );
        assertNotifiersEqual( testProject1.getNotifiers(), project.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject1.getBuildDefinitions() );

        project = projects.get( 1 );
        checkProjectFetchGroup( project, false, false, true, false );
        //assertSame( "Check project group reference matches", project.getProjectGroup(), retrievedGroup );
        assertEquals( project.getProjectGroup().getId(), retrievedGroup.getId() );
        assertProjectEquals( testProject2, project );
        assertNotifiersEqual( testProject2.getNotifiers(), project.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject2.getBuildDefinitions() );
    }

    @Test
    public void testGetAllProjectsGroupWithDetails()
    {
        List<ProjectGroup> projectGroups = projectGroupDao.getAllProjectGroupsWithBuildDetails();
        ProjectGroup group1 = projectGroups.get( 0 );
        assertProjectGroupEquals( defaultProjectGroup, group1 );
        assertNotifiersEqual( defaultProjectGroup.getNotifiers(), group1.getNotifiers() );
        assertBuildDefinitionsEqual( group1.getBuildDefinitions(), defaultProjectGroup.getBuildDefinitions() );
        ProjectGroup group2 = projectGroups.get( 1 );
        assertProjectGroupEquals( testProjectGroup2, group2 );
        assertNotifiersEqual( testProjectGroup2.getNotifiers(), group2.getNotifiers() );
        assertBuildDefinitionsEqual( group2.getBuildDefinitions(), testProjectGroup2.getBuildDefinitions() );

        List<Project> projects = group1.getProjects();
        assertEquals( "Check number of projects", 2, projects.size() );

        Project project = projects.get( 0 );
        checkProjectFetchGroup( project, false, false, true, false );
        assertSame( "Check project group reference matches", project.getProjectGroup(), group1 );
        assertProjectEquals( testProject1, project );
        assertNotifiersEqual( testProject1.getNotifiers(), project.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject1.getBuildDefinitions() );

        project = projects.get( 1 );
        checkProjectFetchGroup( project, false, false, true, false );
        assertSame( "Check project group reference matches", project.getProjectGroup(), group1 );
        assertProjectEquals( testProject2, project );
        assertNotifiersEqual( testProject2.getNotifiers(), project.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject2.getBuildDefinitions() );

        projects = group2.getProjects();
        assertEquals( "Check number of projects", 0, projects.size() );
    }

    @Test
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
        assertDeveloperEquals( copy, project.getDevelopers().get( 1 ) );
    }

    @Test
    public void testEditDeveloper()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );

        ProjectDeveloper newDeveloper = project.getDevelopers().get( 0 );
        newDeveloper.setName( "name1.1" );
        newDeveloper.setEmail( "email1.1" );

        ProjectDeveloper copy = createTestDeveloper( newDeveloper );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # devs", 1, project.getDevelopers().size() );
        assertDeveloperEquals( copy, project.getDevelopers().get( 0 ) );
    }

    @Test
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

    @Test
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
        assertDependencyEquals( copy, project.getDependencies().get( 2 ) );
    }

    @Test
    public void testEditDependency()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );

        ProjectDependency newDependency = project.getDependencies().get( 0 );
        newDependency.setGroupId( "groupId1.1" );
        newDependency.setArtifactId( "artifactId1.1" );

        ProjectDependency copy = createTestDependency( newDependency );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # deps", 2, project.getDependencies().size() );
        assertDependencyEquals( copy, project.getDependencies().get( 0 ) );
    }

    @Test
    public void testDeleteDependency()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        ProjectDependency dependency = project.getDependencies().get( 1 );
        project.getDependencies().remove( 0 );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check size is now 1", 1, project.getDependencies().size() );
        assertDependencyEquals( dependency, project.getDependencies().get( 0 ) );

        // !! These actually aren't happening !!
        // TODO: test the dependency was physically deleted
    }

    @Test
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
        assertNotifierEquals( copy, project.getNotifiers().get( 1 ) );
    }

    @Test
    public void testEditNotifier()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );

        ProjectNotifier newNotifier = project.getNotifiers().get( 0 );
        // If we use "type1.1", jpox-rc2 store "type11", weird
        String type = "type11";
        newNotifier.setType( type );

        ProjectNotifier copy = createTestNotifier( newNotifier );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # notifiers", 1, project.getNotifiers().size() );
        assertNotifierEquals( copy, project.getNotifiers().get( 0 ) );
    }

    @Test
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

    @Test
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
        BuildDefinition retrievedBuildDefinition = project.getBuildDefinitions().get( 2 );
        assertBuildDefinitionEquals( copy, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule1, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile1, retrievedBuildDefinition.getProfile() );
    }

    @Test
    public void testEditBuildDefinition()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );

        BuildDefinition newBuildDefinition = project.getBuildDefinitions().get( 0 );
        newBuildDefinition.setBuildFresh( true );
        new BuildDefinition().setDefaultForProject( true );
        String arguments = "arguments1.1";
        newBuildDefinition.setArguments( arguments );
        BuildDefinition copy = createTestBuildDefinition( newBuildDefinition );
        buildDefinitionDao.storeBuildDefinition( newBuildDefinition );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # build defs", 2, project.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = project.getBuildDefinitions().get( 0 );

        assertBuildDefinitionEquals( copy, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule1, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile2, retrievedBuildDefinition.getProfile() );
    }

    @Test
    public void testDeleteBuildDefinition()
        throws ContinuumStoreException
    {
        Project project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        BuildDefinition buildDefinition = project.getBuildDefinitions().get( 1 );
        project.getBuildDefinitions().remove( 0 );
        projectDao.updateProject( project );

        project = projectDao.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check size is now 1", 1, project.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = project.getBuildDefinitions().get( 0 );
        assertBuildDefinitionEquals( buildDefinition, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule2, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile2, retrievedBuildDefinition.getProfile() );

        // !! These actually aren't happening !!
        // TODO: test the def was physically deleted
        // TODO: test the schedule/profile was NOT physically deleted
    }

    @Test
    public void testAddNotifierToProjectGroup()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId(
            defaultProjectGroup.getId() );

        ProjectNotifier notifier = createTestNotifier( 14, true, false, true, "TADNTPG type" );
        ProjectNotifier copy = createTestNotifier( notifier );
        projectGroup.addNotifier( notifier );
        projectGroupDao.updateProjectGroup( projectGroup );

        projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );
        assertEquals( "check # notifiers", 3, projectGroup.getNotifiers().size() );
        assertNotifierEquals( copy, projectGroup.getNotifiers().get( 2 ) );
    }

    @Test
    public void testEditGroupNotifier()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId(
            defaultProjectGroup.getId() );

        ProjectNotifier newNotifier = projectGroup.getNotifiers().get( 0 );
        // If we use "type1.1", jpox-rc2 store "type1", weird
        String type = "type1";
        newNotifier.setType( type );

        ProjectNotifier copy = createTestNotifier( newNotifier );
        projectGroupDao.updateProjectGroup( projectGroup );

        projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );
        assertEquals( "check # notifiers", 2, projectGroup.getNotifiers().size() );
        assertNotifierEquals( copy, projectGroup.getNotifiers().get( 0 ) );
    }

    @Test
    public void testDeleteGroupNotifier()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId(
            defaultProjectGroup.getId() );
        ProjectNotifier notifier = projectGroup.getNotifiers().get( 1 );
        projectGroup.getNotifiers().remove( 0 );
        projectGroupDao.updateProjectGroup( projectGroup );

        projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );
        assertEquals( "check size is now 1", 1, projectGroup.getNotifiers().size() );
        assertNotifierEquals( notifier, projectGroup.getNotifiers().get( 0 ) );

        // !! These actually aren't happening !!
        // TODO: test the notifier was physically deleted
    }

    @Test
    public void testAddBuildDefinitionToProjectGroup()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId(
            defaultProjectGroup.getId() );

        Profile profile = profileDao.getProfile( testProfile1.getId() );
        Schedule schedule = scheduleDao.getSchedule( testSchedule1.getId() );
        BuildDefinition buildDefinition = createTestBuildDefinition( "TABDTPG arguments", "TABDTPG buildFile",
                                                                     "TABDTPG goals", profile, schedule, false, false );
        BuildDefinition copy = createTestBuildDefinition( buildDefinition );
        projectGroup.addBuildDefinition( buildDefinition );
        projectGroupDao.updateProjectGroup( projectGroup );

        projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );
        assertEquals( "check # build defs", 2, projectGroup.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = projectGroup.getBuildDefinitions().get( 1 );
        assertBuildDefinitionEquals( copy, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule1, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile1, retrievedBuildDefinition.getProfile() );
    }

    @Test
    public void testEditGroupBuildDefinition()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId(
            defaultProjectGroup.getId() );

        BuildDefinition newBuildDefinition = projectGroup.getBuildDefinitions().get( 0 );

        // If we use "arguments1.1", jpox-rc2 store "arguments11", weird
        String arguments = "arguments1";
        newBuildDefinition.setArguments( arguments );

        BuildDefinition copy = createTestBuildDefinition( newBuildDefinition );
        projectGroupDao.updateProjectGroup( projectGroup );

        projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );
        assertEquals( "check # build defs", 1, projectGroup.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = projectGroup.getBuildDefinitions().get( 0 );
        assertBuildDefinitionEquals( copy, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule2, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile1, retrievedBuildDefinition.getProfile() );
    }

    @Test
    public void testDeleteGroupBuildDefinition()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId(
            defaultProjectGroup.getId() );
        projectGroup.getBuildDefinitions().remove( 0 );
        projectGroupDao.updateProjectGroup( projectGroup );

        projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( defaultProjectGroup.getId() );
        assertEquals( "check size is now 0", 0, projectGroup.getBuildDefinitions().size() );

        // !! These actually aren't happening !!
        // TODO: test the def was physically deleted
        // TODO: test the schedule/profile was NOT physically deleted
    }

    @Test
    public void testGetTemplatesBuildDefinitions()
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
        assertEquals( 2, buildDefinitionTemplateDao.getAllBuildDefinitionTemplate().size() );

        template = buildDefinitionTemplateDao.getContinuumBuildDefinitionTemplateWithType(
            ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );

        assertNotNull( template );
        assertEquals( 1, template.getBuildDefinitions().size() );

        assertEquals( 2, buildDefinitionTemplateDao.getAllBuildDefinitionTemplate().size() );
    }

    @Test
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

    @Test
    public void testRemoveLocalRepository()
        throws Exception
    {
        LocalRepository repository = localRepositoryDao.getLocalRepositoryByName( testLocalRepository2.getName() );

        ProjectGroup projectGroup = projectGroupDao.getProjectGroupByGroupId( testProjectGroup2.getGroupId() );
        assertLocalRepositoryEquals( testLocalRepository2, projectGroup.getLocalRepository() );
        projectGroup.setLocalRepository( null );

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

    @Test
    public void testGetAllLocalRepositories()
        throws Exception
    {
        List<LocalRepository> localRepositories = localRepositoryDao.getAllLocalRepositories();

        assertEquals( "check # local repositories", 3, localRepositories.size() );
        assertLocalRepositoryEquals( testLocalRepository1, localRepositories.get( 0 ) );
        assertLocalRepositoryEquals( testLocalRepository2, localRepositories.get( 1 ) );
        assertLocalRepositoryEquals( testLocalRepository3, localRepositories.get( 2 ) );
    }

    @Test
    public void testAddRepositoryPurgeConfiguration()
        throws Exception
    {
        LocalRepository repository = localRepositoryDao.getLocalRepository( testLocalRepository3.getId() );
        Schedule schedule = scheduleDao.getSchedule( testSchedule1.getId() );

        RepositoryPurgeConfiguration repoPurge = createTestRepositoryPurgeConfiguration( true, 2, 100, false, schedule,
                                                                                         true, repository );

        RepositoryPurgeConfiguration copy = createTestRepositoryPurgeConfiguration( repoPurge );
        repositoryPurgeConfigurationDao.addRepositoryPurgeConfiguration( repoPurge );
        copy.setId( repoPurge.getId() );

        RepositoryPurgeConfiguration retrieved = repositoryPurgeConfigurationDao.getRepositoryPurgeConfiguration(
            repoPurge.getId() );
        assertRepositoryPurgeConfigurationEquals( copy, retrieved );
        assertLocalRepositoryEquals( testLocalRepository3, retrieved.getRepository() );
        assertScheduleEquals( testSchedule1, retrieved.getSchedule() );
    }

    @Test
    public void testRemoveRepositoryPurgeConfiguration()
        throws Exception
    {
        RepositoryPurgeConfiguration repoPurge = repositoryPurgeConfigurationDao.getRepositoryPurgeConfiguration(
            testRepoPurgeConfiguration2.getId() );
        repositoryPurgeConfigurationDao.removeRepositoryPurgeConfiguration( repoPurge );

        List<RepositoryPurgeConfiguration> repoPurgeList =
            repositoryPurgeConfigurationDao.getAllRepositoryPurgeConfigurations();
        assertEquals( "check # repo purge configurations", 2, repoPurgeList.size() );
        assertFalse( "check not there", repoPurgeList.contains( repoPurge ) );
    }

    @Test
    public void testAddDirectoryPurgeConfiguration()
        throws Exception
    {
        String location = "release-directory";
        String directoryType = "release";

        Schedule schedule = scheduleDao.getSchedule( testSchedule1.getId() );
        DirectoryPurgeConfiguration dirPurge = createTestDirectoryPurgeConfiguration( location, directoryType, false, 2,
                                                                                      100, schedule, true );

        DirectoryPurgeConfiguration copy = createTestDirectoryPurgeConfiguration( dirPurge );
        directoryPurgeConfigurationDao.addDirectoryPurgeConfiguration( dirPurge );
        copy.setId( dirPurge.getId() );

        DirectoryPurgeConfiguration retrieved = directoryPurgeConfigurationDao.getDirectoryPurgeConfiguration(
            dirPurge.getId() );
        assertDirectoryPurgeConfigurationEquals( copy, retrieved );
        assertScheduleEquals( testSchedule1, retrieved.getSchedule() );
    }

    @Test
    public void testRemoveDirectoryPurgeConfiguration()
        throws Exception
    {
        DirectoryPurgeConfiguration dirPurge = directoryPurgeConfigurationDao.getDirectoryPurgeConfiguration(
            testDirectoryPurgeConfig.getId() );
        directoryPurgeConfigurationDao.removeDirectoryPurgeConfiguration( dirPurge );

        List<DirectoryPurgeConfiguration> dirPurgeList =
            directoryPurgeConfigurationDao.getAllDirectoryPurgeConfigurations();
        assertEquals( "check #  dir purge configurations", 0, dirPurgeList.size() );
    }

    @Test
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

    @Test
    public void testAddProjectScmRoot()
        throws Exception
    {
        ProjectGroup projectGroup = projectGroupDao.getProjectGroup( testProjectGroup2.getId() );
        ProjectScmRoot projectScmRoot = createTestProjectScmRoot( "scmRootAddress", 1, 0, "", projectGroup );

        projectScmRoot = projectScmRootDao.addProjectScmRoot( projectScmRoot );

        List<ProjectScmRoot> projectScmRoots = projectScmRootDao.getProjectScmRootByProjectGroup(
            projectGroup.getId() );

        assertEquals( "check # of project scm root", 2, projectScmRoots.size() );

        ProjectScmRoot retrievedProjectScmRoot = projectScmRootDao.getProjectScmRootByProjectGroupAndScmRootAddress(
            projectGroup.getId(), "scmRootAddress" );

        assertProjectScmRootEquals( projectScmRoot, retrievedProjectScmRoot );
        assertProjectGroupEquals( projectScmRoot.getProjectGroup(), retrievedProjectScmRoot.getProjectGroup() );
    }

    @Test
    public void testRemoveProjectScmRoot()
        throws Exception
    {
        ProjectGroup projectGroup = projectGroupDao.getProjectGroup( testProjectGroup2.getId() );

        List<ProjectScmRoot> projectScmRoots = projectScmRootDao.getProjectScmRootByProjectGroup(
            projectGroup.getId() );

        assertEquals( "check # of project scm root", 1, projectScmRoots.size() );

        ProjectScmRoot projectScmRoot = projectScmRoots.get( 0 );
        projectScmRootDao.removeProjectScmRoot( projectScmRoot );

        projectScmRoots = projectScmRootDao.getProjectScmRootByProjectGroup( projectGroup.getId() );

        assertEquals( "check # of project scm root", 0, projectScmRoots.size() );
    }

    @Test
    public void testRemoveProjectWithReleaseResult()
        throws Exception
    {
        Project project = projectDao.getProject( testProject1.getId() );
        ProjectGroup group = project.getProjectGroup();

        ContinuumReleaseResult releaseResult = createTestContinuumReleaseResult( group, project, "releaseGoal", 0, 0,
                                                                                 0 );
        releaseResult = releaseResultDao.addContinuumReleaseResult( releaseResult );

        List<ContinuumReleaseResult> releaseResults = releaseResultDao.getAllContinuumReleaseResults();
        assertEquals( "check size of continuum release results", 2, releaseResults.size() );

        ContinuumReleaseResult retrievedResult = releaseResults.get( 1 );
        assertReleaseResultEquals( releaseResult, retrievedResult );
        assertProjectGroupEquals( group, retrievedResult.getProjectGroup() );
        assertProjectEquals( project, retrievedResult.getProject() );

        releaseResultDao.removeContinuumReleaseResult( releaseResult );
        projectDao.removeProject( project );
        assertFalse( projectDao.getProjectsInGroup( group.getId() ).contains( project ) );

        releaseResults = releaseResultDao.getAllContinuumReleaseResults();
        assertEquals( "check size of continuum release results", 1, releaseResults.size() );
    }

    @Test
    public void testGetProjectSummaryByProjectGroup()
        throws Exception
    {
        List<Project> projects = projectDao.getProjectsInGroup( defaultProjectGroup.getId() );
        assertEquals( 2, projects.size() );

        Project project = projects.get( 0 );
        project.setState( 2 );
        projectDao.updateProject( project );

        project = projects.get( 1 );
        project.setState( 2 );
        projectDao.updateProject( project );

        ProjectGroup newGroup = projectGroupDao.getProjectGroupWithProjects( testProjectGroup2.getId() );
        Project project1 = createTestProject( testProject1 );
        project1.setState( 4 );
        newGroup.addProject( project1 );

        Project project2 = createTestProject( testProject2 );
        project2.setState( 1 );
        newGroup.addProject( project2 );
        projectGroupDao.updateProjectGroup( newGroup );

        Map<Integer, ProjectGroupSummary> summaries = projectDao.getProjectsSummary();

        assertNotNull( summaries );
        assertEquals( "check size of project summaries", 2, summaries.size() );

        ProjectGroupSummary summary = summaries.get( testProjectGroup2.getId() );
        assertEquals( "check id of project group", testProjectGroup2.getId(), summary.getProjectGroupId() );
        assertEquals( "check number of errors", 1, summary.getNumberOfErrors() );
        assertEquals( "check number of successes", 0, summary.getNumberOfSuccesses() );
        assertEquals( "check number of failures", 0, summary.getNumberOfFailures() );
        assertEquals( "check number of projects", 2, summary.getNumberOfProjects() );

        summary = summaries.get( defaultProjectGroup.getId() );
        assertEquals( "check id of project group", defaultProjectGroup.getId(), summary.getProjectGroupId() );
        assertEquals( "check number of errors", 0, summary.getNumberOfErrors() );
        assertEquals( "check number of successes", 2, summary.getNumberOfSuccesses() );
        assertEquals( "check number of failures", 0, summary.getNumberOfFailures() );
        assertEquals( "check number of projects", 2, summary.getNumberOfProjects() );

    }

    @Test
    public void testGetBuildResultsInRange()
        throws Exception
    {
        int maxFetch = 5;
        List<BuildResult> results = buildResultDao.getBuildResultsInRange( null, null, 0, null, null, 0, maxFetch );
        assertEquals( "check number of build results returned", 3, results.size() );

        results = buildResultDao.getBuildResultsInRange( null, null, 2, null, null, 0, maxFetch );
        assertEquals( "check number of build results returned with state == OK", 2, results.size() );

        results = buildResultDao.getBuildResultsInRange( null, null, 0, "user", null, 0, maxFetch );
        assertEquals( "check number of build results returned with triggeredBy == user", 1, results.size() );

        results = buildResultDao.getBuildResultsInRange( null, null, 0, "schedule", null, 0, maxFetch );
        assertEquals( "check number of build results returned with triggeredBy == schedule", 2, results.size() );

        results = buildResultDao.getBuildResultsInRange( null, null, 2, "schedule", null, 0, maxFetch );
        assertEquals( "check number of build results returned with state == Ok and triggeredBy == schedule", 1,
                      results.size() );

        results = buildResultDao.getBuildResultsInRange( null, null, 3, "user", null, 0, maxFetch );
        assertEquals( "check number of build results returned with state == Failed and triggeredBy == user", 0,
                      results.size() );

        Calendar cal = Calendar.getInstance();
        cal.setTime( new Date( baseTime ) );
        cal.add( Calendar.DAY_OF_MONTH, 1 );

        results = buildResultDao.getBuildResultsInRange( new Date( baseTime ), cal.getTime(), 0, null, null, 0, maxFetch );
        assertEquals( "check number of build results returned with startDate and endDate", 2, results.size() );

        results = buildResultDao.getBuildResultsInRange( new Date( baseTime ), new Date( baseTime ), 0, null, null, 0,
                                                         maxFetch );
        assertEquals( "check number of build results returned with the same startDate and endDate", 1, results.size() );

        results = buildResultDao.getBuildResultsInRange( null, null, 0, null, null, 0, maxFetch );
        assertEquals( "check number of build results returned with an existing group id", 3, results.size() );

        results = buildResultDao.getBuildResultsInRange( null, null, 0, null, null, 0, maxFetch );
        assertEquals( "check number of build results returned with non-existing group id", 0, results.size() );
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

    @Before
    public void setUp()
        throws Exception
    {
        buildDefinitionDao = lookup( BuildDefinitionDao.class );
        buildDefinitionTemplateDao = lookup( BuildDefinitionTemplateDao.class );
        buildResultDao = lookup( BuildResultDao.class );
        createBuildDatabase( false );
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
}
