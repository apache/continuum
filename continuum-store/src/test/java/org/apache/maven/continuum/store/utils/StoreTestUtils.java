package org.apache.maven.continuum.store.utils;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
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
import org.apache.maven.continuum.model.system.Installation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Utility methods shared across Store's unit tests.
 * <p>
 * TODO: Review and document as appropriate!
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 */
public class StoreTestUtils
{

    public static void assertBuildDefinitionEquals( BuildDefinition expectedBuildDefinition,
                                                    BuildDefinition actualBuildDefinition )
    {
        TestCase.assertEquals( "compare build definition - arguments", expectedBuildDefinition.getArguments(),
                               actualBuildDefinition.getArguments() );
        TestCase.assertEquals( "compare build definition - build file", expectedBuildDefinition.getBuildFile(),
                               actualBuildDefinition.getBuildFile() );
        TestCase.assertEquals( "compare build definition - goals", expectedBuildDefinition.getGoals(),
                               actualBuildDefinition.getGoals() );
    }

    public static void assertBuildDefinitionsEqual( List expectedBuildDefinitions, List actualBuildDefinitions )
    {
        for ( int i = 0; i < expectedBuildDefinitions.size(); i++ )
        {
            BuildDefinition expectedBuildDefinition = (BuildDefinition) expectedBuildDefinitions.get( i );
            BuildDefinition actualBuildDefinition = (BuildDefinition) actualBuildDefinitions.get( i );
            assertBuildDefinitionEquals( expectedBuildDefinition, actualBuildDefinition );
            assertScheduleEquals( expectedBuildDefinition.getSchedule(), actualBuildDefinition.getSchedule() );
            assertProfileEquals( expectedBuildDefinition.getProfile(), actualBuildDefinition.getProfile() );
        }
    }

    public static void assertBuildResultEquals( BuildResult expected, BuildResult actual )
    {
        TestCase.assertEquals( "compare build result - build #", expected.getBuildNumber(), actual.getBuildNumber() );
        TestCase.assertEquals( "compare build result - end time", expected.getEndTime(), actual.getEndTime() );
        TestCase.assertEquals( "compare build result - error", expected.getError(), actual.getError() );
        TestCase.assertEquals( "compare build result - exit code", expected.getExitCode(), actual.getExitCode() );
        TestCase.assertEquals( "compare build result - start time", expected.getStartTime(), actual.getStartTime() );
        TestCase.assertEquals( "compare build result - state", expected.getState(), actual.getState() );
        TestCase.assertEquals( "compare build result - trigger", expected.getTrigger(), actual.getTrigger() );
    }

    private static void assertChangeFileEquals( ChangeFile expected, ChangeFile actual )
    {
        TestCase.assertEquals( "compare change file result - name", expected.getName(), actual.getName() );
        TestCase.assertEquals( "compare change file result - revision", expected.getRevision(), actual.getRevision() );
    }

    private static void assertChangeSetEquals( ChangeSet expected, ChangeSet actual )
    {
        TestCase.assertEquals( "compare change set result - author", expected.getAuthor(), actual.getAuthor() );
        TestCase.assertEquals( "compare change set result - comment", expected.getComment(), actual.getComment() );
        // Remove this test, in some case we have a 1ms difference between two
        // dates
        // TestCase.assertEquals( "compare change set result - date",
        // changeSet.getDate(), retrievedChangeSet.getDate() );
        TestCase.assertEquals( "compare change set result - files size", expected.getFiles().size(),
                               actual.getFiles().size() );
        for ( int i = 0; i < actual.getFiles().size(); i++ )
        {
            assertChangeFileEquals( (ChangeFile) expected.getFiles().get( i ), (ChangeFile) actual.getFiles().get( i ) );
        }
    }

    public static void assertDependenciesEqual( List expectedDependencies, List actualDependencies )
    {
        for ( int i = 0; i < actualDependencies.size(); i++ )
        {
            assertDependencyEquals( (ProjectDependency) expectedDependencies.get( i ),
                                    (ProjectDependency) actualDependencies.get( i ) );
        }
    }

    public static void assertDependencyEquals( ProjectDependency expectedDependency, ProjectDependency actualDependency )
    {
        TestCase.assertEquals( "compare dependency - groupId", expectedDependency.getGroupId(),
                               actualDependency.getGroupId() );
        TestCase.assertEquals( "compare dependency - artifactId", expectedDependency.getArtifactId(),
                               actualDependency.getArtifactId() );
        TestCase.assertEquals( "compare dependency - version", expectedDependency.getVersion(),
                               actualDependency.getVersion() );
    }

    public static void assertDeveloperEquals( ProjectDeveloper expectedDeveloper, ProjectDeveloper actualDeveloper )
    {
        TestCase.assertEquals( "compare developer - name", expectedDeveloper.getName(), actualDeveloper.getName() );
        TestCase.assertEquals( "compare developer - email", expectedDeveloper.getEmail(), actualDeveloper.getEmail() );
        TestCase.assertEquals( "compare developer - scmId", expectedDeveloper.getScmId(), actualDeveloper.getScmId() );
        TestCase.assertEquals( "compare developer - continuumId", expectedDeveloper.getContinuumId(),
                               actualDeveloper.getContinuumId() );
    }

    public static void assertDevelopersEqual( List expectedDevelopers, List actualDevelopers )
    {
        for ( int i = 0; i < actualDevelopers.size(); i++ )
        {
            assertDeveloperEquals( (ProjectDeveloper) expectedDevelopers.get( i ),
                                   (ProjectDeveloper) actualDevelopers.get( i ) );
        }
    }

    public static void assertInstallationEquals( Installation expected, Installation actual )
    {
        TestCase.assertEquals( "compare installation - name", expected.getName(), actual.getName() );
        TestCase.assertEquals( "compare installation - path", expected.getPath(), actual.getPath() );
        TestCase.assertEquals( "compare installation - version", expected.getVersion(), actual.getVersion() );
    }

    public static void assertNotifierEquals( ProjectNotifier expected, ProjectNotifier actual )
    {
        TestCase.assertEquals( "compare notifier - recipient type", expected.getRecipientType(),
                               actual.getRecipientType() );
        TestCase.assertEquals( "compare notifier - type", expected.getType(), actual.getType() );
        TestCase.assertEquals( "compare notifier - configuration", expected.getConfiguration(),
                               actual.getConfiguration() );
        TestCase.assertEquals( "compare notifier - send on success", expected.isSendOnSuccess(),
                               actual.isSendOnSuccess() );
        TestCase.assertEquals( "compare notifier - send on failure", expected.isSendOnFailure(),
                               actual.isSendOnFailure() );
        TestCase.assertEquals( "compare notifier - send on error", expected.isSendOnError(), actual.isSendOnError() );
    }

    public static void assertNotifiersEqual( List expected, List actual )
    {
        for ( int i = 0; i < actual.size(); i++ )
        {
            assertNotifierEquals( (ProjectNotifier) expected.get( i ), (ProjectNotifier) actual.get( i ) );
        }
    }

    public static void assertProfileEquals( Profile expectedProfile, Profile actualProfile )
    {
        TestCase.assertEquals( expectedProfile, actualProfile );
        if ( expectedProfile != null )
        {
            TestCase.assertNotSame( expectedProfile, actualProfile );
            TestCase.assertEquals( "compare profile - name", expectedProfile.getName(), actualProfile.getName() );
            TestCase.assertEquals( "compare profile - desc", expectedProfile.getDescription(),
                                   actualProfile.getDescription() );
            TestCase.assertEquals( "compare profile - scmMode", expectedProfile.getScmMode(),
                                   actualProfile.getScmMode() );
            TestCase.assertEquals( "compare profile - build w/o changes", expectedProfile.isBuildWithoutChanges(),
                                   actualProfile.isBuildWithoutChanges() );
            TestCase.assertEquals( "compare profile - active", expectedProfile.isActive(), actualProfile.isActive() );
        }
    }

    public static void assertProjectEquals( Project expectedProject, Project project )
    {
        TestCase.assertEquals( "compare projects", expectedProject, project );
        TestCase.assertNotSame( expectedProject, project );
        // aggressive compare, as equals is using the identity
        TestCase.assertEquals( "compare expectedProject - name", expectedProject.getName(), project.getName() );
        TestCase.assertEquals( "compare expectedProject - desc", expectedProject.getDescription(),
                               project.getDescription() );
        TestCase.assertEquals( "compare expectedProject - groupId", expectedProject.getGroupId(), project.getGroupId() );
        TestCase.assertEquals( "compare expectedProject - artifactId", expectedProject.getArtifactId(),
                               project.getArtifactId() );
        TestCase.assertEquals( "compare expectedProject - buildNumber", expectedProject.getBuildNumber(),
                               project.getBuildNumber() );
        TestCase.assertEquals( "compare expectedProject - scmUrl", expectedProject.getScmUrl(), project.getScmUrl() );
        TestCase.assertEquals( "compare expectedProject - state", expectedProject.getState(), project.getState() );
        TestCase.assertEquals( "compare expectedProject - url", expectedProject.getUrl(), project.getUrl() );
        TestCase.assertEquals( "compare expectedProject - version", expectedProject.getVersion(), project.getVersion() );
        TestCase.assertEquals( "compare expectedProject - workingDirectory", expectedProject.getWorkingDirectory(),
                               project.getWorkingDirectory() );
    }

    public static void assertProjectGroupEquals( ProjectGroup expectedGroup, ProjectGroup actualGroup )
    {
        TestCase.assertEquals( "compare project groups", expectedGroup, actualGroup );
        TestCase.assertNotSame( expectedGroup, actualGroup );
        // aggressive compare, as equals is using the identity
        TestCase.assertEquals( "compare project groups - name", expectedGroup.getName(), actualGroup.getName() );
        TestCase.assertEquals( "compare project groups - desc", expectedGroup.getDescription(),
                               actualGroup.getDescription() );
        TestCase.assertEquals( "compare project groups - groupId", expectedGroup.getGroupId(), actualGroup.getGroupId() );
    }

    public static void assertScheduleEquals( Schedule expectedSchedule, Schedule actualSchedule )
    {
        TestCase.assertEquals( expectedSchedule, actualSchedule );
        if ( expectedSchedule != null )
        {
            TestCase.assertNotSame( expectedSchedule, actualSchedule );
            TestCase.assertEquals( "compare schedule - id", expectedSchedule.getId(), actualSchedule.getId() );
            TestCase.assertEquals( "compare schedule - name", expectedSchedule.getName(), actualSchedule.getName() );
            TestCase.assertEquals( "compare schedule - desc", expectedSchedule.getDescription(),
                                   actualSchedule.getDescription() );
            TestCase.assertEquals( "compare schedule - delay", expectedSchedule.getDelay(), actualSchedule.getDelay() );
            TestCase.assertEquals( "compare schedule - cron", expectedSchedule.getCronExpression(),
                                   actualSchedule.getCronExpression() );
            TestCase.assertEquals( "compare schedule - active", expectedSchedule.isActive(), actualSchedule.isActive() );
        }
    }

    public static void assertScmResultEquals( ScmResult expected, ScmResult actual )
    {
        TestCase.assertEquals( "compare SCM result - output", expected.getCommandOutput(), actual.getCommandOutput() );
        TestCase.assertEquals( "compare SCM result - message", expected.getProviderMessage(),
                               actual.getProviderMessage() );
        TestCase.assertEquals( "compare SCM result - success", expected.isSuccess(), actual.isSuccess() );
        TestCase.assertEquals( "compare SCM result - changes size", actual.getChanges().size(),
                               expected.getChanges().size() );
        for ( int i = 0; i < actual.getChanges().size(); i++ )
        {
            assertChangeSetEquals( (ChangeSet) expected.getChanges().get( i ), (ChangeSet) actual.getChanges().get( i ) );
        }
    }

    public static BuildDefinition createTestBuildDefinition( BuildDefinition buildDefinition )
    {
        return createTestBuildDefinition( buildDefinition.getArguments(), buildDefinition.getBuildFile(),
                                          buildDefinition.getGoals(), buildDefinition.getProfile(),
                                          buildDefinition.getSchedule() );
    }

    public static BuildDefinition createTestBuildDefinition( String arguments, String buildFile, String goals,
                                                             Profile profile, Schedule schedule )
    {
        BuildDefinition definition = new BuildDefinition();
        definition.setArguments( arguments );
        definition.setBuildFile( buildFile );
        definition.setGoals( goals );
        definition.setProfile( profile );
        definition.setSchedule( schedule );
        return definition;
    }

    private static BuildResult createTestBuildResult( BuildResult buildResult )
    {
        return createTestBuildResult( buildResult.getTrigger(), buildResult.isSuccess(), buildResult.getState(),
                                      buildResult.getExitCode(), buildResult.getError(), buildResult.getBuildNumber(),
                                      buildResult.getStartTime(), buildResult.getEndTime() );
    }

    private static BuildResult createTestBuildResult( int trigger, boolean success, int state, int exitCode,
                                                      String error, long buildNumber, long startTime, long endTime )
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
        return result;
    }

    private static ChangeFile createTestChangeFile( String name, String revision )
    {
        ChangeFile changeFile = new ChangeFile();
        changeFile.setName( name );
        changeFile.setRevision( revision );
        return changeFile;
    }

    private static ChangeSet createTestChangeSet( String author, String comment, String base )
    {
        ChangeSet changeSet = new ChangeSet();
        changeSet.setAuthor( author );
        changeSet.setComment( comment );
        changeSet.setDate( System.currentTimeMillis() );
        List files = new ArrayList();
        files.add( createTestChangeFile( "name" + base + ".1", "rev" + base + ".1" ) );
        files.add( createTestChangeFile( "name" + base + ".2", "rev" + base + ".2" ) );
        files.add( createTestChangeFile( "name" + base + ".3", "rev" + base + ".3" ) );
        changeSet.setFiles( files );
        return changeSet;
    }

    public static ProjectDependency createTestDependency( ProjectDependency dependency )
    {
        return createTestDependency( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion() );
    }

    public static ProjectDependency createTestDependency( String groupId, String artifactId, String version )
    {
        ProjectDependency dependency = new ProjectDependency();
        dependency.setArtifactId( artifactId );
        dependency.setGroupId( groupId );
        dependency.setVersion( version );
        return dependency;
    }

    public static ProjectDeveloper createTestDeveloper( int continuumId, String email, String name, String scmId )
    {
        ProjectDeveloper developer = new ProjectDeveloper();
        developer.setContinuumId( continuumId );
        developer.setEmail( email );
        developer.setName( name );
        developer.setScmId( scmId );
        return developer;
    }

    public static ProjectDeveloper createTestDeveloper( ProjectDeveloper developer )
    {
        return createTestDeveloper( developer.getContinuumId(), developer.getEmail(), developer.getName(),
                                    developer.getScmId() );
    }

    public static Installation createTestInstallation( Installation installation )
    {
        return createTestInstallation( installation.getName(), installation.getPath(), installation.getVersion() );
    }

    private static Installation createTestInstallation( String name, String path, String version )
    {
        Installation installation = new Installation();
        installation.setName( name );
        installation.setPath( path );
        installation.setVersion( version );
        return installation;
    }

    public static ProjectNotifier createTestNotifier( int recipientType, boolean sendOnError, boolean sendOnFailure,
                                                      boolean sendOnSuccess, String type )
    {
        Map configuration = new HashMap();
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

    public static ProjectNotifier createTestNotifier( ProjectNotifier notifier )
    {
        return createTestNotifier( notifier.getRecipientType(), notifier.isSendOnError(), notifier.isSendOnFailure(),
                                   notifier.isSendOnSuccess(), notifier.getType() );
    }

    public static Profile createTestProfile( Profile profile )
    {
        return createTestProfile( profile.getName(), profile.getDescription(), profile.getScmMode(),
                                  profile.isBuildWithoutChanges(), profile.isActive(), profile.getJdk(),
                                  profile.getBuilder() );
        // createTestInstallation( profile.getJdk() ),
        // createTestInstallation( profile.getBuilder() ) );
    }

    public static Profile createTestProfile( String name, String description, int scmMode, boolean buildWithoutChanges,
                                             boolean active, Installation jdk, Installation builder )
    {
        Profile profile = new Profile();
        profile.setActive( active );
        profile.setBuildWithoutChanges( buildWithoutChanges );
        profile.setScmMode( scmMode );
        profile.setDescription( description );
        profile.setName( name );
        profile.setBuilder( builder );
        profile.setJdk( jdk );
        return profile;
    }

    public static Project createTestProject( Project project )
    {
        return createTestProject( project.getArtifactId(), project.getBuildNumber(), project.getDescription(),
                                  project.getGroupId(), project.getName(), project.getScmUrl(), project.getState(),
                                  project.getUrl(), project.getVersion(), project.getWorkingDirectory(),
                                  project.getGroupKey(), project.getKey() );
    }

    private static Project createTestProject( String artifactId, long buildNumber, String description, String groupId,
                                              String name, String scmUrl, int state, String url, String version,
                                              String workingDirectory, String groupKey, String projectKey )
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
        // assumes that a project will *always* have a group.
        project.setGroupKey( groupKey );
        project.setKey( projectKey );
        return project;
    }

    public static ProjectGroup createTestProjectGroup( ProjectGroup group )
    {
        return createTestProjectGroup( group.getName(), group.getDescription(), group.getGroupId(), group.getKey() );
    }

    public static ProjectGroup createTestProjectGroup( String name, String description, String groupId, String groupKey )
    {
        ProjectGroup group = new ProjectGroup();
        group.setName( name );
        group.setDescription( description );
        group.setGroupId( groupId );
        group.setKey( groupKey );
        return group;
    }

    public static Schedule createTestSchedule( Schedule schedule )
    {
        return createTestSchedule( schedule.getName(), schedule.getDescription(), schedule.getDelay(),
                                   schedule.getCronExpression(), schedule.isActive() );
    }

    public static Schedule createTestSchedule( String name, String description, long delay, String cronExpression,
                                               boolean active )
    {
        Schedule schedule = new Schedule();
        schedule.setActive( active );
        schedule.setCronExpression( cronExpression );
        schedule.setDelay( delay );
        schedule.setDescription( description );
        schedule.setName( name );
        return schedule;
    }

    private static ScmResult createTestScmResult( ScmResult scmResult, String base )
    {
        return createTestScmResult( scmResult.getCommandOutput(), scmResult.getProviderMessage(),
                                    scmResult.isSuccess(), base );
    }

    private static ScmResult createTestScmResult( String commandOutput, String providerMessage, boolean success,
                                                  String base )
    {
        ScmResult scmResult = new ScmResult();
        scmResult.setCommandOutput( commandOutput );
        scmResult.setProviderMessage( providerMessage );
        scmResult.setSuccess( success );

        List changes = new ArrayList();
        changes.add( createTestChangeSet( "author" + base + ".1", "comment" + base + ".1", base + ".1" ) );
        changes.add( createTestChangeSet( "author" + base + ".2", "comment" + base + ".2", base + ".2" ) );
        scmResult.setChanges( changes );
        return scmResult;
    }

}
