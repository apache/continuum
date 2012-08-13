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

import org.apache.continuum.dao.BuildDefinitionTemplateDao;
import org.apache.continuum.dao.BuildQueueDao;
import org.apache.continuum.dao.ContinuumReleaseResultDao;
import org.apache.continuum.dao.DaoUtils;
import org.apache.continuum.dao.DirectoryPurgeConfigurationDao;
import org.apache.continuum.dao.InstallationDao;
import org.apache.continuum.dao.LocalRepositoryDao;
import org.apache.continuum.dao.ProfileDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectGroupDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.dao.RepositoryPurgeConfigurationDao;
import org.apache.continuum.dao.ScheduleDao;
import org.apache.continuum.dao.SystemConfigurationDao;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.model.release.ContinuumReleaseResult;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.maven.continuum.installation.InstallationService;
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
import org.codehaus.plexus.jdo.DefaultConfigurableJdoFactory;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Base class for tests using the continuum store.
 */
public abstract class AbstractContinuumStoreTestCase
    extends PlexusInSpringTestCase
{
    protected DaoUtils daoUtilsImpl;

    protected DirectoryPurgeConfigurationDao directoryPurgeConfigurationDao;

    protected LocalRepositoryDao localRepositoryDao;

    protected RepositoryPurgeConfigurationDao repositoryPurgeConfigurationDao;

    protected InstallationDao installationDao;

    protected ProfileDao profileDao;

    protected ProjectGroupDao projectGroupDao;

    protected ProjectDao projectDao;

    protected ScheduleDao scheduleDao;

    protected SystemConfigurationDao systemConfigurationDao;

    protected ProjectScmRootDao projectScmRootDao;

    protected ContinuumReleaseResultDao releaseResultDao;

    protected BuildQueueDao buildQueueDao;

    protected BuildDefinitionTemplateDao buildDefinitionTemplateDao;

    protected ProjectGroup defaultProjectGroup;

    protected ProjectGroup testProjectGroup2;

    protected Project testProject1;

    protected Project testProject2;

    protected Schedule testSchedule1;

    protected Schedule testSchedule2;

    protected Schedule testSchedule3;

    protected Profile testProfile1;

    protected Profile testProfile2;

    protected Profile testProfile3;

    protected Profile testProfile4;

    protected Installation testInstallationJava13;

    protected Installation testInstallationJava14;

    protected Installation testInstallationMaven20a3;

    protected Installation testInstallationEnvVar;

    protected BuildResult testBuildResult1;

    protected BuildResult testBuildResult2;

    protected BuildResult testBuildResult3;

    protected ScmResult testCheckoutResult1;

    protected LocalRepository testLocalRepository1;

    protected LocalRepository testLocalRepository2;

    protected LocalRepository testLocalRepository3;

    protected RepositoryPurgeConfiguration testRepoPurgeConfiguration1;

    protected RepositoryPurgeConfiguration testRepoPurgeConfiguration2;

    protected RepositoryPurgeConfiguration testRepoPurgeConfiguration3;

    protected DirectoryPurgeConfiguration testDirectoryPurgeConfig;

    protected ProjectScmRoot testProjectScmRoot;

    protected ContinuumReleaseResult testContinuumReleaseResult;

    protected BuildQueue testBuildQueue1;

    protected BuildQueue testBuildQueue2;

    protected BuildQueue testBuildQueue3;

    protected long baseTime;

    private SystemConfiguration systemConfiguration;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        createStore();

        directoryPurgeConfigurationDao = (DirectoryPurgeConfigurationDao) lookup(
            DirectoryPurgeConfigurationDao.class.getName() );

        localRepositoryDao = (LocalRepositoryDao) lookup( LocalRepositoryDao.class.getName() );

        repositoryPurgeConfigurationDao = (RepositoryPurgeConfigurationDao) lookup(
            RepositoryPurgeConfigurationDao.class.getName() );

        installationDao = (InstallationDao) lookup( InstallationDao.class.getName() );

        profileDao = (ProfileDao) lookup( ProfileDao.class.getName() );

        projectGroupDao = (ProjectGroupDao) lookup( ProjectGroupDao.class.getName() );

        projectDao = (ProjectDao) lookup( ProjectDao.class.getName() );

        scheduleDao = (ScheduleDao) lookup( ScheduleDao.class.getName() );

        systemConfigurationDao = (SystemConfigurationDao) lookup( SystemConfigurationDao.class.getName() );

        projectScmRootDao = (ProjectScmRootDao) lookup( ProjectScmRootDao.class.getName() );

        releaseResultDao = (ContinuumReleaseResultDao) lookup( ContinuumReleaseResultDao.class.getName() );

        buildQueueDao = (BuildQueueDao) lookup( BuildQueueDao.class.getName() );

        buildDefinitionTemplateDao = (BuildDefinitionTemplateDao) lookup( BuildDefinitionTemplateDao.class.getName() );
    }

    protected void createBuildDatabase( boolean isTestFromDataManagementTool )
        throws Exception
    {
        createBuildDatabase( true, isTestFromDataManagementTool );
    }

    protected void createBuildDatabase( boolean addToStore, boolean isTestFromDataManagementTool )
        throws Exception
    {
        // Setting up test data
        testLocalRepository1 = createTestLocalRepository( "name1", "location1", "layout1" );

        LocalRepository localRepository1 = createTestLocalRepository( testLocalRepository1 );
        if ( addToStore )
        {
            localRepository1 = localRepositoryDao.addLocalRepository( localRepository1 );
            testLocalRepository1.setId( localRepository1.getId() );
        }
        else
        {
            localRepository1.setId( 1 );
            testLocalRepository1.setId( 1 );
        }

        testLocalRepository2 = createTestLocalRepository( "name2", "location2", "layout2" );

        LocalRepository localRepository2 = createTestLocalRepository( testLocalRepository2 );
        if ( addToStore )
        {
            localRepository2 = localRepositoryDao.addLocalRepository( localRepository2 );
            testLocalRepository2.setId( localRepository2.getId() );
        }
        else
        {
            localRepository2.setId( 2 );
            testLocalRepository2.setId( 2 );
        }

        testLocalRepository3 = createTestLocalRepository( "name3", "location3", "layout3" );

        LocalRepository localRepository3 = createTestLocalRepository( testLocalRepository3 );
        if ( addToStore )
        {
            localRepository3 = localRepositoryDao.addLocalRepository( localRepository3 );
            testLocalRepository3.setId( localRepository3.getId() );
        }
        else
        {
            localRepository3.setId( 3 );
            testLocalRepository3.setId( 3 );
        }

        testBuildQueue1 = createTestBuildQueue( "build queue 1" );

        BuildQueue buildQueue1 = createTestBuildQueue( testBuildQueue1 );
        if ( addToStore )
        {
            buildQueue1 = buildQueueDao.addBuildQueue( buildQueue1 );
            testBuildQueue1.setId( buildQueue1.getId() );
        }
        else
        {
            buildQueue1.setId( 1 );
            testBuildQueue1.setId( 1 );
        }

        testBuildQueue2 = createTestBuildQueue( "build queue 2" );

        BuildQueue buildQueue2 = createTestBuildQueue( testBuildQueue2 );
        if ( addToStore )
        {
            buildQueue2 = buildQueueDao.addBuildQueue( buildQueue2 );
            testBuildQueue2.setId( buildQueue2.getId() );
        }
        else
        {
            buildQueue2.setId( 2 );
            testBuildQueue2.setId( 2 );
        }

        testBuildQueue3 = createTestBuildQueue( "build queue 3" );

        BuildQueue buildQueue3 = createTestBuildQueue( testBuildQueue3 );
        if ( addToStore )
        {
            buildQueue3 = buildQueueDao.addBuildQueue( buildQueue3 );
            testBuildQueue3.setId( buildQueue3.getId() );
        }
        else
        {
            buildQueue3.setId( 3 );
            testBuildQueue3.setId( 3 );
        }

        defaultProjectGroup = createTestProjectGroup( "Default Group", "The Default Group",
                                                      "org.apache.maven.test.default", localRepository1 );

        testProjectGroup2 = createTestProjectGroup( "test group 2", "test group 2 desc", "test group 2 groupId",
                                                    localRepository2 );

        testProject1 = createTestProject( "artifactId1", 1, "description1", defaultProjectGroup.getGroupId(), "name1",
                                          "scmUrl1", 1, "url1", "version1", "workingDirectory1" );

        // state must be 1 unless we setup a build in the correct state
        testProject2 = createTestProject( "artifactId2", 2, "description2", defaultProjectGroup.getGroupId(), "name2",
                                          "scmUrl2", 1, "url2", "version2", "workingDirectory2" );

        testSchedule1 = createTestSchedule( "name1", "description1", 1, "cronExpression1", true );
        testSchedule1.addBuildQueue( buildQueue1 );
        testSchedule1.addBuildQueue( buildQueue2 );

        testSchedule2 = createTestSchedule( "name2", "description2", 2, "cronExpression2", true );
        testSchedule2.addBuildQueue( buildQueue2 );
        testSchedule2.addBuildQueue( buildQueue3 );

        testSchedule3 = createTestSchedule( "name3", "description3", 3, "cronExpression3", true );

        testInstallationJava13 = createTestInstallation( "JDK 1.3", InstallationService.JDK_TYPE, "JAVA_HOME",
                                                         "/usr/local/java-1.3" );
        testInstallationJava14 = createTestInstallation( "JDK 1.4", InstallationService.JDK_TYPE, "JAVA_HOME",
                                                         "/usr/local/java-1.4" );
        testInstallationMaven20a3 = createTestInstallation( "Maven 2.0 alpha 3", InstallationService.MAVEN2_TYPE,
                                                            "M2_HOME", "/usr/local/maven-2.0-alpha-3" );
        testInstallationEnvVar = createTestInstallation( "Maven Heap Size", InstallationService.ENVVAR_TYPE,
                                                         "MAVEN_OPTS", "-Xms256m -Xmx256m" );

        ProjectNotifier testGroupNotifier1 = createTestNotifier( 1, true, false, true, "type1" );
        ProjectNotifier testGroupNotifier2 = createTestNotifier( 2, false, true, false, "type2" );
        ProjectNotifier testGroupNotifier3 = createTestNotifier( 3, true, false, false, "type3" );

        ProjectNotifier testNotifier1 = createTestNotifier( 11, true, true, false, "type11" );
        ProjectNotifier testNotifier2 = createTestNotifier( 12, false, false, true, "type12" );
        ProjectNotifier testNotifier3 = createTestNotifier( 13, false, true, false, "type13" );

        ProjectDeveloper testDeveloper1 = createTestDeveloper( 1, "email1", "name1", "scmId1" );
        ProjectDeveloper testDeveloper2 = createTestDeveloper( 2, "email2", "name2", "scmId2" );
        ProjectDeveloper testDeveloper3 = createTestDeveloper( 3, "email3", "name3", "scmId3" );

        ProjectDependency testDependency1 = createTestDependency( "groupId1", "artifactId1", "version1" );
        ProjectDependency testDependency2 = createTestDependency( "groupId2", "artifactId2", "version2" );
        ProjectDependency testDependency3 = createTestDependency( "groupId3", "artifactId3", "version3" );

        // TODO: simplify by deep copying the relationships in createTest... ?
        baseTime = System.currentTimeMillis();

        Calendar cal = Calendar.getInstance();
        cal.setTime( new Date( baseTime ) );
        cal.add( Calendar.DAY_OF_MONTH, 1 );

        long newTime = cal.getTimeInMillis();

        // successful forced build
        testBuildResult1 = createTestBuildResult( 1, true, 2, 1, "error1", 1, baseTime, baseTime + 1000, "user" );
        BuildResult buildResult1 = createTestBuildResult( testBuildResult1 );
        ScmResult scmResult = createTestScmResult( "commandOutput1", "providerMessage1", true, "1" );
        buildResult1.setScmResult( scmResult );
        ScmResult testBuildResult1ScmResult = createTestScmResult( scmResult, "1" );
        testBuildResult1.setScmResult( testBuildResult1ScmResult );
        testCheckoutResult1 = createTestScmResult( "commandOutputCO1", "providerMessageCO1", false, "CO1" );
        ScmResult checkoutResult1 = createTestScmResult( testCheckoutResult1, "CO1" );
        testProject1.setCheckoutResult( checkoutResult1 );
        testProject1.addBuildResult( buildResult1 );

        // failed scheduled build
        testBuildResult2 = createTestBuildResult( 2, false, 3, 2, "error2", 2, newTime, newTime + 3000, "schedule" );
        BuildResult buildResult2 = createTestBuildResult( testBuildResult2 );
        testProject1.addBuildResult( buildResult2 );

        cal.add( Calendar.DAY_OF_MONTH, 2 );
        newTime = cal.getTimeInMillis();

        // successful scheduled build
        testBuildResult3 = createTestBuildResult( 2, true, 2, 3, "error3", 3, newTime, newTime + 5000, "schedule" );
        BuildResult buildResult3 = createTestBuildResult( testBuildResult3 );
        scmResult = createTestScmResult( "commandOutput3", "providerMessage3", true, "3" );
        buildResult3.setScmResult( scmResult );
        testBuildResult3.setScmResult( createTestScmResult( scmResult, "3" ) );
        testProject2.addBuildResult( buildResult3 );

        // TODO: better way? this assumes that some untested methods already work!
        Schedule schedule2 = createTestSchedule( testSchedule2 );
        if ( addToStore )
        {
            schedule2 = scheduleDao.addSchedule( schedule2 );
            testSchedule2.setId( schedule2.getId() );
        }
        else
        {
            // from expected.xml, continuum-data-management
            testSchedule2.setId( 1 );
        }

        Schedule schedule1 = createTestSchedule( testSchedule1 );
        if ( addToStore )
        {
            schedule1 = scheduleDao.addSchedule( schedule1 );
            testSchedule1.setId( schedule1.getId() );
        }
        else
        {
            // from expected.xml, continuum-data-management
            testSchedule1.setId( 2 );
        }

        Schedule schedule3 = createTestSchedule( testSchedule3 );
        if ( addToStore )
        {
            schedule3 = scheduleDao.addSchedule( schedule3 );
            testSchedule3.setId( schedule3.getId() );
        }
        else
        {
            // from expected.xml, continuum-data-management
            testSchedule3.setId( 3 );
        }

        Installation installationJava14 = createTestInstallation( testInstallationJava14 );
        if ( addToStore )
        {
            installationJava14 = installationDao.addInstallation( installationJava14 );
        }
        else
        {
            installationJava14.setInstallationId( 1 );
        }

        Installation installationMaven20a3 = createTestInstallation( testInstallationMaven20a3 );
        if ( addToStore )
        {
            installationMaven20a3 = installationDao.addInstallation( installationMaven20a3 );
        }
        else
        {
            installationMaven20a3.setInstallationId( 2 );
        }

        Installation installationJava13 = createTestInstallation( testInstallationJava13 );
        if ( addToStore )
        {
            installationJava13 = installationDao.addInstallation( installationJava13 );
        }
        else
        {
            installationJava13.setInstallationId( 3 );
        }

        Installation installationEnvVar = createTestInstallation( testInstallationEnvVar );
        if ( addToStore )
        {
            installationEnvVar = installationDao.addInstallation( installationEnvVar );
        }
        else
        {
            installationEnvVar.setInstallationId( 4 );
        }

        testProfile1 = createTestProfile( "name1", "description1", 1, true, true, installationJava13,
                                          installationMaven20a3 );
        testProfile2 = createTestProfile( "name2", "description2", 2, false, true, installationJava14,
                                          installationMaven20a3 );
        testProfile3 = createTestProfile( "name3", "description3", 3, true, false, installationJava14,
                                          installationMaven20a3 );
        testProfile4 = createTestProfile( "name4", "description4", 4, false, false, installationJava14,
                                          installationMaven20a3 );
        testProfile4.addEnvironmentVariable( installationEnvVar );

        Profile profile1 = createTestProfile( testProfile1 );
        if ( addToStore )
        {
            profile1 = profileDao.addProfile( profile1 );
            testProfile1.setId( profile1.getId() );
        }
        else
        {
            testProfile1.setId( 1 );
        }

        Profile profile2 = createTestProfile( testProfile2 );
        if ( addToStore )
        {
            profile2 = profileDao.addProfile( profile2 );
            testProfile2.setId( profile2.getId() );
        }
        else
        {
            testProfile2.setId( 2 );
        }

        Profile profile3 = createTestProfile( testProfile3 );
        if ( addToStore )
        {
            profile3 = profileDao.addProfile( profile3 );
            testProfile3.setId( profile3.getId() );
        }
        else
        {
            profile3.setId( 3 );
        }

        Profile profile4 = createTestProfile( testProfile4 );
        if ( addToStore )
        {
            profile4 = profileDao.addProfile( profile4 );
            testProfile4.setId( profile4.getId() );
        }
        else
        {
            profile4.setId( 4 );
        }

        testRepoPurgeConfiguration1 = createTestRepositoryPurgeConfiguration( true, 5, 50, false, schedule2, true,
                                                                              localRepository1 );
        if ( addToStore )
        {
            testRepoPurgeConfiguration1 = repositoryPurgeConfigurationDao.addRepositoryPurgeConfiguration(
                testRepoPurgeConfiguration1 );
        }
        else
        {
            testRepoPurgeConfiguration1.setId( 1 );
        }

        testRepoPurgeConfiguration2 = createTestRepositoryPurgeConfiguration( false, 10, 200, true, schedule1, true,
                                                                              localRepository2 );
        if ( addToStore )
        {
            testRepoPurgeConfiguration2 = repositoryPurgeConfigurationDao.addRepositoryPurgeConfiguration(
                testRepoPurgeConfiguration2 );
        }
        else
        {
            testRepoPurgeConfiguration2.setId( 2 );
        }

        testRepoPurgeConfiguration3 = createTestRepositoryPurgeConfiguration( false, 10, 200, true, schedule2, true,
                                                                              localRepository1 );
        if ( addToStore )
        {
            testRepoPurgeConfiguration3 = repositoryPurgeConfigurationDao.addRepositoryPurgeConfiguration(
                testRepoPurgeConfiguration3 );
        }
        else
        {
            testRepoPurgeConfiguration3.setId( 3 );
        }

        testDirectoryPurgeConfig = createTestDirectoryPurgeConfiguration( "location1", "directoryType1", true, 10, 50,
                                                                          schedule2, true );
        if ( addToStore )
        {
            testDirectoryPurgeConfig = directoryPurgeConfigurationDao.addDirectoryPurgeConfiguration(
                testDirectoryPurgeConfig );
        }
        else
        {
            testDirectoryPurgeConfig.setId( 1 );
        }

        BuildDefinition testGroupBuildDefinition1 = createTestBuildDefinition( "arguments1", "buildFile1", "goals1",
                                                                               profile1, schedule2, false, false );
        BuildDefinition testGroupBuildDefinition2 = createTestBuildDefinition( "arguments2", "buildFile2", "goals2",
                                                                               profile1, schedule1, false, false );
        BuildDefinition testGroupBuildDefinition3 = createTestBuildDefinition( "arguments3", "buildFile3", "goals3",
                                                                               profile2, schedule1, false, false );
        BuildDefinition testGroupBuildDefinition4 = createTestBuildDefinition( null, null, "deploy", null, null, false,
                                                                               false );

        BuildDefinition testBuildDefinition1 = createTestBuildDefinition( "arguments11", "buildFile11", "goals11",
                                                                          profile2, schedule1, false, false );
        BuildDefinition testBuildDefinition2 = createTestBuildDefinition( "arguments12", "buildFile12", "goals12",
                                                                          profile2, schedule2, false, false );
        BuildDefinition testBuildDefinition3 = createTestBuildDefinition( "arguments13", "buildFile13", "goals13",
                                                                          profile1, schedule2, false, false );
        BuildDefinition testBuildDefinition4 = createTestBuildDefinition( null, null, "deploy", null, null, false,
                                                                          false );
        BuildDefinition testBuildDefinition5 = createTestBuildDefinition( "arguments14", "buildFile14", "goals14",
                                                                          profile1, schedule1, false, false );
        testBuildDefinition5.setTemplate( true );

        BuildDefinitionTemplate testBuildDefinitionTemplate1 = createTestBuildDefinitionTemplate( "template2", "type2",
                                                                                                  false );
        testBuildDefinitionTemplate1.addBuildDefinition( testBuildDefinition5 );

        if ( addToStore )
        {
            buildDefinitionTemplateDao.addBuildDefinitionTemplate( testBuildDefinitionTemplate1 );
        }

        ProjectGroup group = createTestProjectGroup( defaultProjectGroup );

        Project project1 = createTestProject( testProject1 );
        project1.addBuildResult( buildResult1 );
        project1.addBuildResult( buildResult2 );
        project1.setCheckoutResult( checkoutResult1 );
        ProjectNotifier notifier1 = createTestNotifier( testNotifier1 );
        project1.addNotifier( notifier1 );
        testProject1.addNotifier( testNotifier1 );

        BuildDefinition buildDefinition1 = createTestBuildDefinition( testBuildDefinition1 );
        project1.addBuildDefinition( buildDefinition1 );
        testProject1.addBuildDefinition( testBuildDefinition1 );
        BuildDefinition buildDefinition2 = createTestBuildDefinition( testBuildDefinition2 );
        project1.addBuildDefinition( buildDefinition2 );
        testProject1.addBuildDefinition( testBuildDefinition2 );

        ProjectDeveloper projectDeveloper1 = createTestDeveloper( testDeveloper1 );
        project1.addDeveloper( projectDeveloper1 );
        testProject1.addDeveloper( testDeveloper1 );

        ProjectDependency projectDependency1 = createTestDependency( testDependency1 );
        project1.addDependency( projectDependency1 );
        testProject1.addDependency( testDependency1 );

        ProjectDependency projectDependency2 = createTestDependency( testDependency2 );
        project1.addDependency( projectDependency2 );
        testProject1.addDependency( testDependency2 );

        group.addProject( project1 );
        defaultProjectGroup.addProject( project1 );
        Project project2 = createTestProject( testProject2 );
        project2.addBuildResult( buildResult3 );
        ProjectNotifier notifier2 = createTestNotifier( testNotifier2 );
        project2.addNotifier( notifier2 );
        testProject2.addNotifier( testNotifier2 );
        ProjectNotifier notifier3 = createTestNotifier( testNotifier3 );
        project2.addNotifier( notifier3 );
        testProject2.addNotifier( testNotifier3 );

        BuildDefinition buildDefinition3 = createTestBuildDefinition( testBuildDefinition3 );
        project2.addBuildDefinition( buildDefinition3 );
        testProject2.addBuildDefinition( testBuildDefinition3 );

        BuildDefinition buildDefinition4 = createTestBuildDefinition( testBuildDefinition4 );
        project2.addBuildDefinition( buildDefinition4 );
        testProject2.addBuildDefinition( testBuildDefinition4 );

        ProjectDeveloper projectDeveloper2 = createTestDeveloper( testDeveloper2 );
        project2.addDeveloper( projectDeveloper2 );
        testProject2.addDeveloper( testDeveloper2 );

        ProjectDeveloper projectDeveloper3 = createTestDeveloper( testDeveloper3 );
        project2.addDeveloper( projectDeveloper3 );
        testProject2.addDeveloper( testDeveloper3 );

        ProjectDependency projectDependency3 = createTestDependency( testDependency3 );
        project2.addDependency( projectDependency3 );
        testProject2.addDependency( testDependency3 );

        group.addProject( project2 );
        defaultProjectGroup.addProject( project2 );

        ProjectNotifier groupNotifier1 = createTestNotifier( testGroupNotifier1 );
        group.addNotifier( groupNotifier1 );
        defaultProjectGroup.addNotifier( testGroupNotifier1 );
        ProjectNotifier groupNotifier2 = createTestNotifier( testGroupNotifier2 );
        group.addNotifier( groupNotifier2 );
        defaultProjectGroup.addNotifier( testGroupNotifier2 );

        BuildDefinition groupBuildDefinition1 = createTestBuildDefinition( testGroupBuildDefinition1 );
        group.addBuildDefinition( groupBuildDefinition1 );
        defaultProjectGroup.addBuildDefinition( testGroupBuildDefinition1 );

        if ( addToStore )
        {
            group = projectGroupDao.addProjectGroup( group );
            defaultProjectGroup.setId( group.getId() );
            testProject1.setId( project1.getId() );
            testProject2.setId( project2.getId() );
            testBuildResult1.setId( buildResult1.getId() );
            testBuildResult2.setId( buildResult2.getId() );
            testBuildResult3.setId( buildResult3.getId() );
        }
        else
        {
            // from expected.xml, continuum-data-management
            defaultProjectGroup.setId( 1 );
            testProject1.setId( 1 );
            testProject2.setId( 2 );
        }

        group = createTestProjectGroup( testProjectGroup2 );

        ProjectNotifier groupNotifier3 = createTestNotifier( testGroupNotifier3 );
        group.addNotifier( groupNotifier3 );
        testProjectGroup2.addNotifier( testGroupNotifier3 );

        BuildDefinition groupBuildDefinition2 = createTestBuildDefinition( testGroupBuildDefinition2 );
        group.addBuildDefinition( groupBuildDefinition2 );
        testProjectGroup2.addBuildDefinition( testGroupBuildDefinition2 );

        BuildDefinition groupBuildDefinition3 = createTestBuildDefinition( testGroupBuildDefinition3 );
        group.addBuildDefinition( groupBuildDefinition3 );
        testProjectGroup2.addBuildDefinition( testGroupBuildDefinition3 );

        BuildDefinition groupBuildDefinition4 = createTestBuildDefinition( testGroupBuildDefinition4 );
        group.addBuildDefinition( groupBuildDefinition4 );
        testProjectGroup2.addBuildDefinition( testGroupBuildDefinition4 );

        if ( addToStore )
        {
            group = projectGroupDao.addProjectGroup( group );
            testProjectGroup2.setId( group.getId() );
        }
        else
        {
            group.setId( 2 );
            testProjectGroup2.setId( 2 ); // from expected.xml, continuum-data-management
        }

        systemConfiguration = new SystemConfiguration();
        systemConfiguration.setBaseUrl( "baseUrl" );
        systemConfiguration.setBuildOutputDirectory( "buildOutputDirectory" );
        systemConfiguration.setDefaultScheduleCronExpression( "* * * * *" );
        systemConfiguration.setDefaultScheduleDescription( "Description" );
        systemConfiguration.setDeploymentRepositoryDirectory( "deployment" );
        systemConfiguration.setGuestAccountEnabled( false );
        systemConfiguration.setInitialized( true );
        systemConfiguration.setWorkingDirectory( "workingDirectory" );

        if ( addToStore && !isTestFromDataManagementTool )
        {
            systemConfiguration = systemConfigurationDao.addSystemConfiguration( systemConfiguration );
        }
        else
        {
            // hack for DataManagementTool test
            // data-management-jdo has a dependency to continuum-commons where DefaultConfigurationService
            //      is located. DefaultConfiguration loads the data and already adds a system configuration, causing
            //      this to throw an exception
            boolean isExisting = false;
            try
            {
                systemConfigurationDao.getSystemConfiguration();
            }
            catch ( ContinuumStoreException e )
            {
                isExisting = true;
            }

            if ( !isExisting )
            {
                systemConfiguration = systemConfigurationDao.getSystemConfiguration();
                systemConfiguration.setBaseUrl( "baseUrl" );
                systemConfiguration.setBuildOutputDirectory( "buildOutputDirectory" );
                systemConfiguration.setDefaultScheduleCronExpression( "* * * * *" );
                systemConfiguration.setDefaultScheduleDescription( "Description" );
                systemConfiguration.setDeploymentRepositoryDirectory( "deployment" );
                systemConfiguration.setGuestAccountEnabled( false );
                systemConfiguration.setInitialized( true );
                systemConfiguration.setWorkingDirectory( "workingDirectory" );

                systemConfigurationDao.updateSystemConfiguration( systemConfiguration );
            }
        }

        testProjectScmRoot = createTestProjectScmRoot( "scmRootAddress1", 1, 0, "error1", group );
        ProjectScmRoot scmRoot = createTestProjectScmRoot( testProjectScmRoot );

        if ( addToStore )
        {
            scmRoot = projectScmRootDao.addProjectScmRoot( scmRoot );
            testProjectScmRoot.setId( scmRoot.getId() );
        }
        else
        {
            testProjectScmRoot.setId( 1 );
        }

        testContinuumReleaseResult = createTestContinuumReleaseResult( group, null, "releaseGoal", 0, 0, 0 );
        ContinuumReleaseResult releaseResult = createTestContinuumReleaseResult( testContinuumReleaseResult );

        if ( addToStore )
        {
            releaseResult = releaseResultDao.addContinuumReleaseResult( releaseResult );
            testContinuumReleaseResult.setId( releaseResult.getId() );
        }
        else
        {
            testContinuumReleaseResult.setId( 1 );
        }
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        daoUtilsImpl.eraseDatabase();

        daoUtilsImpl.closeStore();
    }

    protected void assertBuildDatabase()
        throws ContinuumStoreException
    {
        assertProjectGroupEquals( defaultProjectGroup, projectGroupDao.getProjectGroup( defaultProjectGroup.getId() ) );
        assertProjectGroupEquals( testProjectGroup2, projectGroupDao.getProjectGroup( testProjectGroup2.getId() ) );

        assertProjectEquals( testProject1, projectDao.getProject( testProject1.getId() ) );
        assertProjectEquals( testProject2, projectDao.getProject( testProject2.getId() ) );

        assertScheduleEquals( testSchedule1, scheduleDao.getSchedule( testSchedule1.getId() ) );
        assertScheduleEquals( testSchedule2, scheduleDao.getSchedule( testSchedule2.getId() ) );
        assertScheduleEquals( testSchedule3, scheduleDao.getSchedule( testSchedule3.getId() ) );

        Iterator<Installation> iterator = installationDao.getAllInstallations().iterator();
        assertInstallationEquals( testInstallationJava13, iterator.next() );
        assertInstallationEquals( testInstallationJava14, iterator.next() );
        assertInstallationEquals( testInstallationMaven20a3, iterator.next() );

/*
        // TODO!!! -- definitely need to test the changeset stuff since it uses modello.refid
        ProjectNotifier testGroupNotifier1 = createTestNotifier( 1, true, false, true, "type1" );
        ProjectNotifier testGroupNotifier2 = createTestNotifier( 2, false, true, false, "type2" );
        ProjectNotifier testGroupNotifier3 = createTestNotifier( 3, true, false, false, "type3" );

        ProjectNotifier testNotifier1 = createTestNotifier( 11, true, true, false, "type11" );
        ProjectNotifier testNotifier2 = createTestNotifier( 12, false, false, true, "type12" );
        ProjectNotifier testNotifier3 = createTestNotifier( 13, false, true, false, "type13" );

        ProjectDeveloper testDeveloper1 = createTestDeveloper( 1, "email1", "name1", "scmId1" );
        ProjectDeveloper testDeveloper2 = createTestDeveloper( 2, "email2", "name2", "scmId2" );
        ProjectDeveloper testDeveloper3 = createTestDeveloper( 3, "email3", "name3", "scmId3" );

        ProjectDependency testDependency1 = createTestDependency( "groupId1", "artifactId1", "version1" );
        ProjectDependency testDependency2 = createTestDependency( "groupId2", "artifactId2", "version2" );
        ProjectDependency testDependency3 = createTestDependency( "groupId3", "artifactId3", "version3" );

        // TODO: simplify by deep copying the relationships in createTest... ?
        long baseTime = System.currentTimeMillis();
        testBuildResult1 = createTestBuildResult( 1, true, 1, 1, "error1", 1, baseTime, baseTime + 1000 );
        BuildResult buildResult1 = createTestBuildResult( testBuildResult1 );
        ScmResult scmResult = createTestScmResult( "commandOutput1", "providerMessage1", true, "1" );
        buildResult1.setScmResult( scmResult );
        ScmResult testBuildResult1ScmResult = createTestScmResult( scmResult, "1" );
        testBuildResult1.setScmResult( testBuildResult1ScmResult );
        testCheckoutResult1 = createTestScmResult( "commandOutputCO1", "providerMessageCO1", false, "CO1" );
        ScmResult checkoutResult1 = createTestScmResult( testCheckoutResult1, "CO1" );
        testProject1.setCheckoutResult( checkoutResult1 );
        testProject1.addBuildResult( buildResult1 );

        testBuildResult2 = createTestBuildResult( 2, false, 2, 2, "error2", 2, baseTime + 2000, baseTime + 3000 );
        BuildResult buildResult2 = createTestBuildResult( testBuildResult2 );
        testProject1.addBuildResult( buildResult2 );

        testBuildResult3 = createTestBuildResult( 3, true, 3, 3, "error3", 3, baseTime + 4000, baseTime + 5000 );
        BuildResult buildResult3 = createTestBuildResult( testBuildResult3 );
        scmResult = createTestScmResult( "commandOutput3", "providerMessage3", true, "3" );
        buildResult3.setScmResult( scmResult );
        testBuildResult3.setScmResult( createTestScmResult( scmResult, "3" ) );
        testProject2.addBuildResult( buildResult3 );

        BuildDefinition testGroupBuildDefinition1 =
            createTestBuildDefinition( "arguments1", "buildFile1", "goals1", profile1, schedule2 );
        BuildDefinition testGroupBuildDefinition2 =
            createTestBuildDefinition( "arguments2", "buildFile2", "goals2", profile1, schedule1 );
        BuildDefinition testGroupBuildDefinition3 =
            createTestBuildDefinition( "arguments3", "buildFile3", "goals3", profile2, schedule1 );

        BuildDefinition testBuildDefinition1 =
            createTestBuildDefinition( "arguments11", "buildFile11", "goals11", profile2, schedule1 );
        BuildDefinition testBuildDefinition2 =
            createTestBuildDefinition( "arguments12", "buildFile12", "goals12", profile2, schedule2 );
        BuildDefinition testBuildDefinition3 =
            createTestBuildDefinition( "arguments13", "buildFile13", "goals13", profile1, schedule2 );

        ProjectGroup group = createTestProjectGroup( defaultProjectGroup );

        Project project1 = createTestProject( testProject1 );
        project1.addBuildResult( buildResult1 );
        project1.addBuildResult( buildResult2 );
        project1.setCheckoutResult( checkoutResult1 );
        ProjectNotifier notifier1 = createTestNotifier( testNotifier1 );
        project1.addNotifier( notifier1 );
        testProject1.addNotifier( testNotifier1 );

        BuildDefinition buildDefinition1 = createTestBuildDefinition( testBuildDefinition1 );
        project1.addBuildDefinition( buildDefinition1 );
        testProject1.addBuildDefinition( testBuildDefinition1 );
        BuildDefinition buildDefinition2 = createTestBuildDefinition( testBuildDefinition2 );
        project1.addBuildDefinition( buildDefinition2 );
        testProject1.addBuildDefinition( testBuildDefinition2 );

        ProjectDeveloper projectDeveloper1 = createTestDeveloper( testDeveloper1 );
        project1.addDeveloper( projectDeveloper1 );
        testProject1.addDeveloper( testDeveloper1 );

        ProjectDependency projectDependency1 = createTestDependency( testDependency1 );
        project1.addDependency( projectDependency1 );
        testProject1.addDependency( testDependency1 );

        ProjectDependency projectDependency2 = createTestDependency( testDependency2 );
        project1.addDependency( projectDependency2 );
        testProject1.addDependency( testDependency2 );

        group.addProject( project1 );
        defaultProjectGroup.addProject( project1 );
        Project project2 = createTestProject( testProject2 );
        project2.addBuildResult( buildResult3 );
        ProjectNotifier notifier2 = createTestNotifier( testNotifier2 );
        project2.addNotifier( notifier2 );
        testProject2.addNotifier( testNotifier2 );
        ProjectNotifier notifier3 = createTestNotifier( testNotifier3 );
        project2.addNotifier( notifier3 );
        testProject2.addNotifier( testNotifier3 );

        BuildDefinition buildDefinition3 = createTestBuildDefinition( testBuildDefinition3 );
        project2.addBuildDefinition( buildDefinition3 );
        testProject2.addBuildDefinition( testBuildDefinition3 );

        ProjectDeveloper projectDeveloper2 = createTestDeveloper( testDeveloper2 );
        project2.addDeveloper( projectDeveloper2 );
        testProject2.addDeveloper( testDeveloper2 );

        ProjectDeveloper projectDeveloper3 = createTestDeveloper( testDeveloper3 );
        project2.addDeveloper( projectDeveloper3 );
        testProject2.addDeveloper( testDeveloper3 );

        ProjectDependency projectDependency3 = createTestDependency( testDependency3 );
        project2.addDependency( projectDependency3 );
        testProject2.addDependency( testDependency3 );

        group.addProject( project2 );
        defaultProjectGroup.addProject( project2 );

        ProjectNotifier groupNotifier1 = createTestNotifier( testGroupNotifier1 );
        group.addNotifier( groupNotifier1 );
        defaultProjectGroup.addNotifier( testGroupNotifier1 );
        ProjectNotifier groupNotifier2 = createTestNotifier( testGroupNotifier2 );
        group.addNotifier( groupNotifier2 );
        defaultProjectGroup.addNotifier( testGroupNotifier2 );

        BuildDefinition groupBuildDefinition1 = createTestBuildDefinition( testGroupBuildDefinition1 );
        group.addBuildDefinition( groupBuildDefinition1 );
        defaultProjectGroup.addBuildDefinition( testGroupBuildDefinition1 );

        store.addProjectGroup( group );
        defaultProjectGroup.setId( group.getId() );
        testProject1.setId( project1.getId() );
        testBuildResult1.setId( buildResult1.getId() );
        testBuildResult2.setId( buildResult2.getId() );
        testProject2.setId( project2.getId() );
        testBuildResult3.setId( buildResult3.getId() );

        group = createTestProjectGroup( testProjectGroup2 );

        ProjectNotifier groupNotifier3 = createTestNotifier( testGroupNotifier3 );
        group.addNotifier( groupNotifier3 );
        testProjectGroup2.addNotifier( testGroupNotifier3 );

        BuildDefinition groupBuildDefinition2 = createTestBuildDefinition( testGroupBuildDefinition2 );
        group.addBuildDefinition( groupBuildDefinition2 );
        testProjectGroup2.addBuildDefinition( testGroupBuildDefinition2 );

        BuildDefinition groupBuildDefinition3 = createTestBuildDefinition( testGroupBuildDefinition3 );
        group.addBuildDefinition( groupBuildDefinition3 );
        testProjectGroup2.addBuildDefinition( testGroupBuildDefinition3 );

        store.addProjectGroup( group );
        testProjectGroup2.setId( group.getId() );
*/
        assertSystemConfiguration( systemConfiguration, systemConfigurationDao.getSystemConfiguration() );

        assertLocalRepositoryEquals( testLocalRepository1, localRepositoryDao.getLocalRepository(
            testLocalRepository1.getId() ) );
        assertLocalRepositoryEquals( testLocalRepository2, localRepositoryDao.getLocalRepository(
            testLocalRepository2.getId() ) );
        assertLocalRepositoryEquals( testLocalRepository3, localRepositoryDao.getLocalRepository(
            testLocalRepository3.getId() ) );

/*
        assertRepositoryPurgeConfigurationEquals( testRepoPurgeConfiguration1,
                                                  repositoryPurgeConfigurationDao.getRepositoryPurgeConfiguration( testRepoPurgeConfiguration1.getId() ) );
        assertRepositoryPurgeConfigurationEquals( testRepoPurgeConfiguration2,
                                                  repositoryPurgeConfigurationDao.getRepositoryPurgeConfiguration( testRepoPurgeConfiguration2.getId() ) );
        assertRepositoryPurgeConfigurationEquals( testRepoPurgeConfiguration3,
                                                  repositoryPurgeConfigurationDao.getRepositoryPurgeConfiguration( testRepoPurgeConfiguration3.getId() ) );

        assertDirectoryPurgeConfigurationEquals( testDirectoryPurgeConfig, 
                                                 directoryPurgeConfigurationDao.getDirectoryPurgeConfiguration( testDirectoryPurgeConfig.getId() ) );
*/
        assertProjectScmRootEquals( testProjectScmRoot, projectScmRootDao.getProjectScmRoot(
            testProjectScmRoot.getId() ) );

        assertReleaseResultEquals( testContinuumReleaseResult, releaseResultDao.getContinuumReleaseResult(
            testContinuumReleaseResult.getId() ) );
    }

    private void assertSystemConfiguration( SystemConfiguration expected, SystemConfiguration actual )
    {
        assertNotSame( expected, actual );
        assertEquals( expected.getBaseUrl(), actual.getBaseUrl() );
        assertEquals( expected.getBuildOutputDirectory(), actual.getBuildOutputDirectory() );
        assertEquals( expected.getDefaultScheduleCronExpression(), actual.getDefaultScheduleCronExpression() );
        assertEquals( expected.getDefaultScheduleDescription(), actual.getDefaultScheduleDescription() );
        assertEquals( expected.getDeploymentRepositoryDirectory(), actual.getDeploymentRepositoryDirectory() );
        assertEquals( expected.isGuestAccountEnabled(), actual.isGuestAccountEnabled() );
        assertEquals( expected.isInitialized(), actual.isInitialized() );
        assertEquals( expected.getWorkingDirectory(), actual.getWorkingDirectory() );
    }

    protected void assertEmpty( boolean isTestFromDataManagementTool )
        throws ContinuumStoreException
    {
        assertEquals( 0, installationDao.getAllInstallations().size() );
        assertEquals( 0, profileDao.getAllProfilesByName().size() );
        assertEquals( 0, projectGroupDao.getAllProjectGroups().size() );
        assertEquals( 0, projectDao.getAllProjectsByName().size() );
        if ( !isTestFromDataManagementTool )
        {
            assertNull( systemConfigurationDao.getSystemConfiguration() );
        }
    }

    protected static BuildDefinition createTestBuildDefinition( BuildDefinition buildDefinition )
    {
        return createTestBuildDefinition( buildDefinition.getArguments(), buildDefinition.getBuildFile(),
                                          buildDefinition.getGoals(), buildDefinition.getProfile(),
                                          buildDefinition.getSchedule(), buildDefinition.isDefaultForProject(),
                                          buildDefinition.isBuildFresh() );
    }

    protected static BuildDefinition createTestBuildDefinition( String arguments, String buildFile, String goals,
                                                                Profile profile, Schedule schedule,
                                                                boolean defaultForProject, boolean buildFresh )
    {
        BuildDefinition definition = new BuildDefinition();
        definition.setArguments( arguments );
        definition.setBuildFile( buildFile );
        definition.setGoals( goals );
        definition.setProfile( profile );
        definition.setSchedule( schedule );
        definition.setDefaultForProject( defaultForProject );
        definition.setBuildFresh( buildFresh );
        return definition;
    }

    protected static ProjectNotifier createTestNotifier( ProjectNotifier notifier )
    {
        return createTestNotifier( notifier.getRecipientType(), notifier.isSendOnError(), notifier.isSendOnFailure(),
                                   notifier.isSendOnSuccess(), notifier.getType() );
    }

    protected static ProjectNotifier createTestNotifier( int recipientType, boolean sendOnError, boolean sendOnFailure,
                                                         boolean sendOnSuccess, String type )
    {
        Map<String, String> configuration = new HashMap<String, String>();
        configuration.put( "key1", "value1" );
        configuration.put( "key2", "value2" );

        ProjectNotifier notifier = new ProjectNotifier();
        notifier.setConfiguration( configuration );
        notifier.setRecipientType( recipientType );
        notifier.setSendOnError( sendOnError );
        notifier.setSendOnFailure( sendOnFailure );
        notifier.setSendOnSuccess( sendOnSuccess );
        notifier.setType( type );

        return notifier;
    }

    private static ScmResult createTestScmResult( ScmResult scmResult, String base )
    {
        return createTestScmResult( scmResult.getCommandOutput(), scmResult.getProviderMessage(), scmResult.isSuccess(),
                                    base );
    }

    private static ScmResult createTestScmResult( String commandOutput, String providerMessage, boolean success,
                                                  String base )
    {
        ScmResult scmResult = new ScmResult();
        scmResult.setCommandOutput( commandOutput );
        scmResult.setProviderMessage( providerMessage );
        scmResult.setSuccess( success );

        List<ChangeSet> changes = new ArrayList<ChangeSet>();
        changes.add( createTestChangeSet( "author" + base + ".1", "comment" + base + ".1", base + ".1" ) );
        changes.add( createTestChangeSet( "author" + base + ".2", "comment" + base + ".2", base + ".2" ) );
        scmResult.setChanges( changes );
        return scmResult;
    }

    private static ChangeSet createTestChangeSet( String author, String comment, String base )
    {
        ChangeSet changeSet = new ChangeSet();
        changeSet.setAuthor( author );
        changeSet.setComment( comment );
        changeSet.setDate( System.currentTimeMillis() );
        List<ChangeFile> files = new ArrayList<ChangeFile>();
        files.add( createTestChangeFile( "name" + base + ".1", "rev" + base + ".1" ) );
        files.add( createTestChangeFile( "name" + base + ".2", "rev" + base + ".2" ) );
        files.add( createTestChangeFile( "name" + base + ".3", "rev" + base + ".3" ) );
        changeSet.setFiles( files );
        return changeSet;
    }

    private static ChangeFile createTestChangeFile( String name, String revision )
    {
        ChangeFile changeFile = new ChangeFile();
        changeFile.setName( name );
        changeFile.setRevision( revision );
        return changeFile;
    }

    private static BuildResult createTestBuildResult( BuildResult buildResult )
    {
        return createTestBuildResult( buildResult.getTrigger(), buildResult.isSuccess(), buildResult.getState(),
                                      buildResult.getExitCode(), buildResult.getError(), buildResult.getBuildNumber(),
                                      buildResult.getStartTime(), buildResult.getEndTime(), buildResult.getUsername() );
    }

    private static BuildResult createTestBuildResult( int trigger, boolean success, int state, int exitCode,
                                                      String error, int buildNumber, long startTime, long endTime,
                                                      String triggeredBy )
    {
        BuildResult result = new BuildResult();
        result.setBuildNumber( buildNumber );
        result.setStartTime( startTime );
        result.setEndTime( endTime );
        result.setError( error );
        result.setExitCode( exitCode );
        result.setState( state );
        result.setSuccess( success );
        result.setTrigger( trigger );
        result.setUsername( triggeredBy );
        return result;
    }

    protected static Installation createTestInstallation( String name, String type, String varName, String varValue )
    {
        Installation installation = new Installation();
        installation.setName( name );
        installation.setType( type );
        installation.setVarName( varName );
        installation.setVarValue( varValue );
        return installation;
    }

    protected static Installation createTestInstallation( Installation installation )
    {
        return createTestInstallation( installation.getName(), installation.getType(), installation.getVarName(),
                                       installation.getVarValue() );
    }

    protected static Schedule createTestSchedule( Schedule schedule )
    {
        return createTestSchedule( schedule.getName(), schedule.getDescription(), schedule.getDelay(),
                                   schedule.getCronExpression(), schedule.isActive(), schedule.getBuildQueues() );
    }

    protected static Schedule createTestSchedule( String name, String description, int delay, String cronExpression,
                                                  boolean active )
    {
        return createTestSchedule( name, description, delay, cronExpression, active, null );
    }

    protected static Schedule createTestSchedule( String name, String description, int delay, String cronExpression,
                                                  boolean active, List<BuildQueue> buildQueues )
    {
        Schedule schedule = new Schedule();
        schedule.setActive( active );
        schedule.setCronExpression( cronExpression );
        schedule.setDelay( delay );
        schedule.setDescription( description );
        schedule.setName( name );
        schedule.setBuildQueues( buildQueues );

        return schedule;
    }

    protected static Profile createTestProfile( Profile profile )
    {
        return createTestProfile( profile.getName(), profile.getDescription(), profile.getScmMode(),
                                  profile.isBuildWithoutChanges(), profile.isActive(), profile.getJdk(),
                                  profile.getBuilder(), profile.getEnvironmentVariables() );
    }

    protected static Profile createTestProfile( String name, String description, int scmMode,
                                                boolean buildWithoutChanges, boolean active, Installation jdk,
                                                Installation builder )
    {
        return createTestProfile( name, description, scmMode, buildWithoutChanges, active, jdk, builder, null );
    }

    protected static Profile createTestProfile( String name, String description, int scmMode,
                                                boolean buildWithoutChanges, boolean active, Installation jdk,
                                                Installation builder, List<Installation> envVars )
    {
        Profile profile = new Profile();
        profile.setActive( active );
        profile.setBuildWithoutChanges( buildWithoutChanges );
        profile.setScmMode( scmMode );
        profile.setDescription( description );
        profile.setName( name );
        profile.setBuilder( builder );
        profile.setJdk( jdk );
        profile.setEnvironmentVariables( envVars );
        return profile;
    }

    protected static ProjectGroup createTestProjectGroup( ProjectGroup group )
    {
        return createTestProjectGroup( group.getName(), group.getDescription(), group.getGroupId(),
                                       group.getLocalRepository() );
    }

    protected static ProjectGroup createTestProjectGroup( String name, String description, String groupId,
                                                          LocalRepository repository )
    {
        ProjectGroup group = new ProjectGroup();
        group.setName( name );
        group.setDescription( description );
        group.setGroupId( groupId );
        group.setLocalRepository( repository );
        return group;
    }

    protected static Project createTestProject( Project project )
    {
        return createTestProject( project.getArtifactId(), project.getBuildNumber(), project.getDescription(),
                                  project.getGroupId(), project.getName(), project.getScmUrl(), project.getState(),
                                  project.getUrl(), project.getVersion(), project.getWorkingDirectory() );
    }

    protected static Project createTestProject( String artifactId, int buildNumber, String description, String groupId,
                                                String name, String scmUrl, int state, String url, String version,
                                                String workingDirectory )
    {
        Project project = new Project();
        project.setArtifactId( artifactId );
        project.setBuildNumber( buildNumber );
        project.setDescription( description );
        project.setGroupId( groupId );
        project.setName( name );
        project.setScmUrl( scmUrl );
        project.setState( state );
        project.setUrl( url );
        project.setVersion( version );
        project.setWorkingDirectory( workingDirectory );
        return project;
    }

    protected static void assertProjectEquals( Project expectedProject, Project project )
    {
        assertEquals( "compare projects", expectedProject, project );
        assertNotSame( expectedProject, project );
        // aggressive compare, as equals is using the identity
        assertEquals( "compare expectedProject - name", expectedProject.getName(), project.getName() );
        assertEquals( "compare expectedProject - desc", expectedProject.getDescription(), project.getDescription() );
        assertEquals( "compare expectedProject - groupId", expectedProject.getGroupId(), project.getGroupId() );
        assertEquals( "compare expectedProject - artifactId", expectedProject.getArtifactId(),
                      project.getArtifactId() );
        assertEquals( "compare expectedProject - buildNumber", expectedProject.getBuildNumber(),
                      project.getBuildNumber() );
        assertEquals( "compare expectedProject - scmUrl", expectedProject.getScmUrl(), project.getScmUrl() );
        assertEquals( "compare expectedProject - state", expectedProject.getState(), project.getState() );
        assertEquals( "compare expectedProject - url", expectedProject.getUrl(), project.getUrl() );
        assertEquals( "compare expectedProject - version", expectedProject.getVersion(), project.getVersion() );
        assertEquals( "compare expectedProject - workingDirectory", expectedProject.getWorkingDirectory(),
                      project.getWorkingDirectory() );
    }

    protected static void assertProjectGroupEquals( ProjectGroup expectedGroup, ProjectGroup actualGroup )
    {
        assertEquals( "compare project groups", expectedGroup, actualGroup );
        assertNotSame( expectedGroup, actualGroup );
        // aggressive compare, as equals is using the identity
        assertEquals( "compare project groups - name", expectedGroup.getName(), actualGroup.getName() );
        assertEquals( "compare project groups - desc", expectedGroup.getDescription(), actualGroup.getDescription() );
        assertEquals( "compare project groups - groupId", expectedGroup.getGroupId(), actualGroup.getGroupId() );
    }

    protected static void assertScheduleEquals( Schedule expectedSchedule, Schedule actualSchedule )
    {
        assertEquals( expectedSchedule, actualSchedule );
        if ( expectedSchedule != null )
        {
            assertNotSame( expectedSchedule, actualSchedule );
            assertEquals( "compare schedule - id", expectedSchedule.getId(), actualSchedule.getId() );
            assertEquals( "compare schedule - name", expectedSchedule.getName(), actualSchedule.getName() );
            assertEquals( "compare schedule - desc", expectedSchedule.getDescription(),
                          actualSchedule.getDescription() );
            assertEquals( "compare schedule - delay", expectedSchedule.getDelay(), actualSchedule.getDelay() );
            assertEquals( "compare schedule - cron", expectedSchedule.getCronExpression(),
                          actualSchedule.getCronExpression() );
            assertEquals( "compare schedule - active", expectedSchedule.isActive(), actualSchedule.isActive() );
        }
    }

    protected static void assertProfileEquals( Profile expectedProfile, Profile actualProfile )
    {
        assertEquals( expectedProfile, actualProfile );
        if ( expectedProfile != null )
        {
            assertNotSame( expectedProfile, actualProfile );
            assertEquals( "compare profile - name", expectedProfile.getName(), actualProfile.getName() );
            assertEquals( "compare profile - desc", expectedProfile.getDescription(), actualProfile.getDescription() );
            assertEquals( "compare profile - scmMode", expectedProfile.getScmMode(), actualProfile.getScmMode() );
            assertEquals( "compare profile - build w/o changes", expectedProfile.isBuildWithoutChanges(),
                          actualProfile.isBuildWithoutChanges() );
            assertEquals( "compare profile - active", expectedProfile.isActive(), actualProfile.isActive() );
        }
    }

    protected static void assertInstallationEquals( Installation expected, Installation actual )
    {
        assertNotNull( actual );
        assertEquals( "compare installation - name", expected.getName(), actual.getName() );
        assertEquals( "compare installation - varName", expected.getVarName(), actual.getVarName() );
        assertEquals( "compare installation - varValue", expected.getVarValue(), actual.getVarValue() );
    }

    protected static void assertBuildResultEquals( BuildResult expected, BuildResult actual )
    {
        assertEquals( "compare build result - build #", expected.getBuildNumber(), actual.getBuildNumber() );
        assertEquals( "compare build result - end time", expected.getEndTime(), actual.getEndTime() );
        assertEquals( "compare build result - error", expected.getError(), actual.getError() );
        assertEquals( "compare build result - exit code", expected.getExitCode(), actual.getExitCode() );
        assertEquals( "compare build result - start time", expected.getStartTime(), actual.getStartTime() );
        assertEquals( "compare build result - state", expected.getState(), actual.getState() );
        assertEquals( "compare build result - trigger", expected.getTrigger(), actual.getTrigger() );
    }

    protected static void assertScmResultEquals( ScmResult expected, ScmResult actual )
    {
        assertEquals( "compare SCM result - output", expected.getCommandOutput(), actual.getCommandOutput() );
        assertEquals( "compare SCM result - message", expected.getProviderMessage(), actual.getProviderMessage() );
        assertEquals( "compare SCM result - success", expected.isSuccess(), actual.isSuccess() );
        assertEquals( "compare SCM result - changes size", actual.getChanges().size(), expected.getChanges().size() );
        for ( int i = 0; i < actual.getChanges().size(); i++ )
        {
            assertChangeSetEquals( (ChangeSet) expected.getChanges().get( i ), (ChangeSet) actual.getChanges().get(
                i ) );
        }
    }

    private static void assertChangeSetEquals( ChangeSet expected, ChangeSet actual )
    {
        assertEquals( "compare change set result - author", expected.getAuthor(), actual.getAuthor() );
        assertEquals( "compare change set result - comment", expected.getComment(), actual.getComment() );
        //Remove this test, in some case we have a 1ms difference between two dates
        //assertEquals( "compare change set result - date", changeSet.getDate(), retrievedChangeSet.getDate() );
        assertEquals( "compare change set result - files size", expected.getFiles().size(), actual.getFiles().size() );
        for ( int i = 0; i < actual.getFiles().size(); i++ )
        {
            assertChangeFileEquals( (ChangeFile) expected.getFiles().get( i ), (ChangeFile) actual.getFiles().get(
                i ) );
        }
    }

    private static void assertChangeFileEquals( ChangeFile expected, ChangeFile actual )
    {
        assertEquals( "compare change file result - name", expected.getName(), actual.getName() );
        assertEquals( "compare change file result - revision", expected.getRevision(), actual.getRevision() );
    }

    protected static void assertNotifiersEqual( List<ProjectNotifier> expected, List<ProjectNotifier> actual )
    {
        for ( int i = 0; i < actual.size(); i++ )
        {
            assertNotifierEquals( expected.get( i ), actual.get( i ) );
        }
    }

    protected static void assertNotifierEquals( ProjectNotifier expected, ProjectNotifier actual )
    {
        assertEquals( "compare notifier - recipient type", expected.getRecipientType(), actual.getRecipientType() );
        assertEquals( "compare notifier - type", expected.getType(), actual.getType() );
        assertEquals( "compare notifier - configuration", expected.getConfiguration(), actual.getConfiguration() );
        assertEquals( "compare notifier - send on success", expected.isSendOnSuccess(), actual.isSendOnSuccess() );
        assertEquals( "compare notifier - send on failure", expected.isSendOnFailure(), actual.isSendOnFailure() );
        assertEquals( "compare notifier - send on error", expected.isSendOnError(), actual.isSendOnError() );
    }

    protected static void assertBuildDefinitionsEqual( List<BuildDefinition> expectedBuildDefinitions,
                                                       List<BuildDefinition> actualBuildDefinitions )
    {
        for ( int i = 0; i < expectedBuildDefinitions.size(); i++ )
        {
            BuildDefinition expectedBuildDefinition = expectedBuildDefinitions.get( i );
            BuildDefinition actualBuildDefinition = actualBuildDefinitions.get( i );
            assertBuildDefinitionEquals( expectedBuildDefinition, actualBuildDefinition );
            assertScheduleEquals( expectedBuildDefinition.getSchedule(), actualBuildDefinition.getSchedule() );
            assertProfileEquals( expectedBuildDefinition.getProfile(), actualBuildDefinition.getProfile() );
        }
    }

    protected static void assertBuildDefinitionEquals( BuildDefinition expectedBuildDefinition,
                                                       BuildDefinition actualBuildDefinition )
    {
        assertEquals( "compare build definition - arguments", expectedBuildDefinition.getArguments(),
                      actualBuildDefinition.getArguments() );
        assertEquals( "compare build definition - build file", expectedBuildDefinition.getBuildFile(),
                      actualBuildDefinition.getBuildFile() );
        assertEquals( "compare build definition - goals", expectedBuildDefinition.getGoals(),
                      actualBuildDefinition.getGoals() );
        assertEquals( "compare build definition - build fresh", expectedBuildDefinition.isBuildFresh(),
                      actualBuildDefinition.isBuildFresh() );
        assertEquals( "compare build definition - defaultForProject", expectedBuildDefinition.isDefaultForProject(),
                      actualBuildDefinition.isDefaultForProject() );
    }

    protected static void assertDevelopersEqual( List<ProjectDeveloper> expectedDevelopers,
                                                 List<ProjectDeveloper> actualDevelopers )
    {
        for ( int i = 0; i < actualDevelopers.size(); i++ )
        {
            assertDeveloperEquals( expectedDevelopers.get( i ), actualDevelopers.get( i ) );
        }
    }

    protected static void assertDeveloperEquals( ProjectDeveloper expectedDeveloper, ProjectDeveloper actualDeveloper )
    {
        assertEquals( "compare developer - name", expectedDeveloper.getName(), actualDeveloper.getName() );
        assertEquals( "compare developer - email", expectedDeveloper.getEmail(), actualDeveloper.getEmail() );
        assertEquals( "compare developer - scmId", expectedDeveloper.getScmId(), actualDeveloper.getScmId() );
        assertEquals( "compare developer - continuumId", expectedDeveloper.getContinuumId(),
                      actualDeveloper.getContinuumId() );
    }

    protected static void assertDependenciesEqual( List<ProjectDependency> expectedDependencies,
                                                   List<ProjectDependency> actualDependencies )
    {
        for ( int i = 0; i < actualDependencies.size(); i++ )
        {
            assertDependencyEquals( expectedDependencies.get( i ), actualDependencies.get( i ) );
        }
    }

    protected static void assertDependencyEquals( ProjectDependency expectedDependency,
                                                  ProjectDependency actualDependency )
    {
        assertEquals( "compare dependency - groupId", expectedDependency.getGroupId(), actualDependency.getGroupId() );
        assertEquals( "compare dependency - artifactId", expectedDependency.getArtifactId(),
                      actualDependency.getArtifactId() );
        assertEquals( "compare dependency - version", expectedDependency.getVersion(), actualDependency.getVersion() );
    }

    protected static ProjectDependency createTestDependency( ProjectDependency dependency )
    {
        return createTestDependency( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion() );
    }

    protected static ProjectDeveloper createTestDeveloper( ProjectDeveloper developer )
    {
        return createTestDeveloper( developer.getContinuumId(), developer.getEmail(), developer.getName(),
                                    developer.getScmId() );
    }

    protected static ProjectDependency createTestDependency( String groupId, String artifactId, String version )
    {
        ProjectDependency dependency = new ProjectDependency();
        dependency.setArtifactId( artifactId );
        dependency.setGroupId( groupId );
        dependency.setVersion( version );
        return dependency;
    }

    protected static ProjectDeveloper createTestDeveloper( int continuumId, String email, String name, String scmId )
    {
        ProjectDeveloper developer = new ProjectDeveloper();
        developer.setContinuumId( continuumId );
        developer.setEmail( email );
        developer.setName( name );
        developer.setScmId( scmId );
        return developer;
    }

    protected static LocalRepository createTestLocalRepository( LocalRepository repository )
    {
        return createTestLocalRepository( repository.getName(), repository.getLocation(), repository.getLayout() );
    }

    protected static LocalRepository createTestLocalRepository( String name, String location, String layout )
    {
        LocalRepository repository = new LocalRepository();
        repository.setName( name );
        repository.setLocation( location );
        repository.setLayout( layout );
        return repository;
    }

    protected static RepositoryPurgeConfiguration createTestRepositoryPurgeConfiguration(
        RepositoryPurgeConfiguration purgeConfig )
    {
        return createTestRepositoryPurgeConfiguration( purgeConfig.isDeleteAll(), purgeConfig.getRetentionCount(),
                                                       purgeConfig.getDaysOlder(),
                                                       purgeConfig.isDeleteReleasedSnapshots(),
                                                       purgeConfig.getSchedule(), purgeConfig.isEnabled(),
                                                       purgeConfig.getRepository() );
    }

    protected static RepositoryPurgeConfiguration createTestRepositoryPurgeConfiguration( boolean deleteAllArtifacts,
                                                                                          int retentionCount,
                                                                                          int daysOlder,
                                                                                          boolean deleteReleasedSnapshots,
                                                                                          Schedule schedule,
                                                                                          boolean enabled,
                                                                                          LocalRepository repository )
    {
        RepositoryPurgeConfiguration purgeConfig = new RepositoryPurgeConfiguration();
        purgeConfig.setDeleteAll( deleteAllArtifacts );
        purgeConfig.setEnabled( enabled );
        purgeConfig.setRetentionCount( retentionCount );
        purgeConfig.setDaysOlder( daysOlder );
        purgeConfig.setDeleteReleasedSnapshots( deleteReleasedSnapshots );
        purgeConfig.setSchedule( schedule );
        purgeConfig.setRepository( repository );
        return purgeConfig;
    }

    protected static DirectoryPurgeConfiguration createTestDirectoryPurgeConfiguration(
        DirectoryPurgeConfiguration purgeConfig )
    {
        return createTestDirectoryPurgeConfiguration( purgeConfig.getLocation(), purgeConfig.getDirectoryType(),
                                                      purgeConfig.isDeleteAll(), purgeConfig.getRetentionCount(),
                                                      purgeConfig.getDaysOlder(), purgeConfig.getSchedule(),
                                                      purgeConfig.isEnabled() );
    }

    protected static DirectoryPurgeConfiguration createTestDirectoryPurgeConfiguration( String location,
                                                                                        String directoryType,
                                                                                        boolean deleteAllDirectories,
                                                                                        int retentionCount,
                                                                                        int daysOlder,
                                                                                        Schedule schedule,
                                                                                        boolean enabled )
    {
        DirectoryPurgeConfiguration purgeConfig = new DirectoryPurgeConfiguration();
        purgeConfig.setDaysOlder( daysOlder );
        purgeConfig.setDeleteAll( deleteAllDirectories );
        purgeConfig.setDirectoryType( directoryType );
        purgeConfig.setEnabled( enabled );
        purgeConfig.setLocation( location );
        purgeConfig.setRetentionCount( retentionCount );
        purgeConfig.setSchedule( schedule );
        return purgeConfig;
    }

    protected static void assertLocalRepositoryEquals( LocalRepository expectedRepository,
                                                       LocalRepository actualRepository )
    {
        assertEquals( expectedRepository, actualRepository );
        if ( expectedRepository != null )
        {
            assertNotSame( expectedRepository, actualRepository );
            assertEquals( "compare local repository - id", expectedRepository.getId(), actualRepository.getId() );
            assertEquals( "compare local repository - name", expectedRepository.getName(), actualRepository.getName() );
            assertEquals( "compare local repository - location", expectedRepository.getLocation(),
                          actualRepository.getLocation() );
            assertEquals( "compare local repository - layout", expectedRepository.getLayout(),
                          actualRepository.getLayout() );
        }
    }

    protected static void assertRepositoryPurgeConfigurationEquals( RepositoryPurgeConfiguration expectedConfig,
                                                                    RepositoryPurgeConfiguration actualConfig )
    {
        assertEquals( "compare repository purge configuration - id", expectedConfig.getId(), actualConfig.getId() );
        assertEquals( "compare repository purge configuration - deleteAll", expectedConfig.isDeleteAll(),
                      actualConfig.isDeleteAll() );
        assertEquals( "compare repository purge configuration - retentionCount", expectedConfig.getRetentionCount(),
                      actualConfig.getRetentionCount() );
        assertEquals( "compare repository purge configuration - daysOlder", expectedConfig.getDaysOlder(),
                      actualConfig.getDaysOlder() );
        assertEquals( "compare repository purge configuration - deleteReleasedSnapshots",
                      expectedConfig.isDeleteReleasedSnapshots(), actualConfig.isDeleteReleasedSnapshots() );
        assertEquals( "compare repository purge configuration - enabled", expectedConfig.isEnabled(),
                      actualConfig.isEnabled() );
    }

    protected static void assertDirectoryPurgeConfigurationEquals( DirectoryPurgeConfiguration expectedConfig,
                                                                   DirectoryPurgeConfiguration actualConfig )
    {
        assertEquals( "compare directory purge configuration - id", expectedConfig.getId(), actualConfig.getId() );
        assertEquals( "compare directory purge configuration - location", expectedConfig.getLocation(),
                      actualConfig.getLocation() );
        assertEquals( "compare directory purge configuration - directoryType", expectedConfig.getDirectoryType(),
                      actualConfig.getDirectoryType() );
        assertEquals( "compare directory purge configuration - deleteAll", expectedConfig.isDeleteAll(),
                      actualConfig.isDeleteAll() );
        assertEquals( "compare directory purge configuration - retentionCount", expectedConfig.getRetentionCount(),
                      actualConfig.getRetentionCount() );
        assertEquals( "compare directory purge configuration - daysOlder", expectedConfig.getDaysOlder(),
                      actualConfig.getDaysOlder() );
        assertEquals( "compare directory purge configuration - enabled", expectedConfig.isEnabled(),
                      actualConfig.isEnabled() );
    }

    protected static ProjectScmRoot createTestProjectScmRoot( String scmRootAddress, int state, int oldState,
                                                              String error, ProjectGroup group )
    {
        ProjectScmRoot projectScmRoot = new ProjectScmRoot();

        projectScmRoot.setScmRootAddress( scmRootAddress );
        projectScmRoot.setState( state );
        projectScmRoot.setOldState( oldState );
        projectScmRoot.setError( error );
        projectScmRoot.setProjectGroup( group );

        return projectScmRoot;
    }

    protected static ProjectScmRoot createTestProjectScmRoot( ProjectScmRoot scmRoot )
    {
        return createTestProjectScmRoot( scmRoot.getScmRootAddress(), scmRoot.getState(), scmRoot.getOldState(),
                                         scmRoot.getError(), scmRoot.getProjectGroup() );
    }

    protected static void assertProjectScmRootEquals( ProjectScmRoot expectedConfig, ProjectScmRoot actualConfig )
    {
        assertEquals( "compare project scm root - id", expectedConfig.getId(), actualConfig.getId() );
        assertEquals( "compare project scm root - scmUrl", expectedConfig.getScmRootAddress(),
                      actualConfig.getScmRootAddress() );
        assertEquals( "compare project scm root - state", expectedConfig.getState(), actualConfig.getState() );
        assertEquals( "compare project scm root - oldState", expectedConfig.getOldState(), actualConfig.getOldState() );
        assertEquals( "compare project scm root - error", expectedConfig.getError(), actualConfig.getError() );
    }

    protected static ContinuumReleaseResult createTestContinuumReleaseResult( ProjectGroup group, Project project,
                                                                              String releaseGoal, int resultCode,
                                                                              long startTime, long endTime )
    {
        ContinuumReleaseResult releaseResult = new ContinuumReleaseResult();
        releaseResult.setProjectGroup( group );
        releaseResult.setProject( project );
        releaseResult.setReleaseGoal( releaseGoal );
        releaseResult.setResultCode( resultCode );
        releaseResult.setStartTime( startTime );
        releaseResult.setEndTime( endTime );

        return releaseResult;
    }

    protected static ContinuumReleaseResult createTestContinuumReleaseResult( ContinuumReleaseResult releaseResult )
    {
        return createTestContinuumReleaseResult( releaseResult.getProjectGroup(), releaseResult.getProject(),
                                                 releaseResult.getReleaseGoal(), releaseResult.getResultCode(),
                                                 releaseResult.getStartTime(), releaseResult.getEndTime() );
    }

    protected static void assertReleaseResultEquals( ContinuumReleaseResult expectedConfig,
                                                     ContinuumReleaseResult actualConfig )
    {
        assertEquals( "compare continuum release result - id", expectedConfig.getId(), actualConfig.getId() );
        assertEquals( "compare continuum release result - releaseGoal", expectedConfig.getReleaseGoal(),
                      actualConfig.getReleaseGoal() );
        assertEquals( "compare continuum release result - resultCode", expectedConfig.getResultCode(),
                      actualConfig.getResultCode() );
        assertEquals( "compare continuum release result - startTime", expectedConfig.getStartTime(),
                      actualConfig.getStartTime() );
        assertEquals( "compare continuum release result - endTime", expectedConfig.getEndTime(),
                      actualConfig.getEndTime() );
    }

    protected static BuildQueue createTestBuildQueue( String name )
    {
        BuildQueue buildQueue = new BuildQueue();
        buildQueue.setName( name );

        return buildQueue;
    }

    protected static BuildQueue createTestBuildQueue( BuildQueue buildQueue )
    {
        return createTestBuildQueue( buildQueue.getName() );
    }

    protected static void assertBuildQueueEquals( BuildQueue expectedConfig, BuildQueue actualConfig )
    {
        assertEquals( "compare build queue - id", expectedConfig.getId(), actualConfig.getId() );
        assertEquals( "compare build queue - name", expectedConfig.getName(), actualConfig.getName() );
    }

    protected static BuildDefinitionTemplate createTestBuildDefinitionTemplate( String name, String type,
                                                                                boolean continuumDefault )
    {
        BuildDefinitionTemplate template = new BuildDefinitionTemplate();
        template.setName( name );
        template.setType( type );
        template.setContinuumDefault( continuumDefault );

        return template;
    }

    /**
     * Setup JDO Factory
     *
     * @todo push down to a Jdo specific test
     */
    protected void createStore()
        throws Exception
    {
        DefaultConfigurableJdoFactory jdoFactory = (DefaultConfigurableJdoFactory) lookup( JdoFactory.ROLE,
                                                                                           "continuum" );

        jdoFactory.setUrl( "jdbc:hsqldb:mem:" + getName() );

        daoUtilsImpl = (DaoUtils) lookup( DaoUtils.class.getName() );
    }
}
