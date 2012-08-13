package org.apache.maven.continuum.buildcontroller;

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
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.ContinuumProjectState;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class DefaultBuildControllerTest
    extends AbstractContinuumTest
{
    private DefaultBuildController controller;

    private static String FORCED_BUILD_USER = "TestUsername";

    private static String SCHEDULE_NAME = "TEST_SCHEDULE";

    int projectId1;

    int projectId2;

    int buildDefinitionId1;

    int buildDefinitionId2;

    public void setUp()
        throws Exception
    {
        super.setUp();

        BuildDefinitionDao buildDefinitionDao = (BuildDefinitionDao) lookup( BuildDefinitionDao.class.getName() );

        BuildResultDao buildResultDao = (BuildResultDao) lookup( BuildResultDao.class.getName() );

        Project project1 = createProject( "project1" );
        BuildDefinition bd1 = createBuildDefinition();
        project1.addBuildDefinition( bd1 );
        project1.setState( ContinuumProjectState.OK );
        projectId1 = addProject( project1 ).getId();
        buildDefinitionId1 = buildDefinitionDao.getDefaultBuildDefinition( projectId1 ).getId();
        project1 = getProjectDao().getProject( projectId1 );
        BuildResult buildResult1 = new BuildResult();
        buildResult1.setStartTime( Calendar.getInstance().getTimeInMillis() );
        buildResult1.setEndTime( Calendar.getInstance().getTimeInMillis() );
        buildResult1.setState( ContinuumProjectState.OK );
        buildResult1.setSuccess( true );
        buildResult1.setBuildDefinition( bd1 );
        buildResultDao.addBuildResult( project1, buildResult1 );
        BuildResult buildResult2 = new BuildResult();
        buildResult2.setStartTime( Calendar.getInstance().getTimeInMillis() - 7200000 );
        buildResult2.setEndTime( Calendar.getInstance().getTimeInMillis() - 7200000 );
        buildResult2.setSuccess( true );
        buildResult2.setState( ContinuumProjectState.OK );
        buildResult2.setBuildDefinition( bd1 );
        buildResultDao.addBuildResult( project1, buildResult2 );
        createPomFile( getProjectDao().getProjectWithAllDetails( projectId1 ) );

        Project project2 = createProject( "project2" );
        ProjectDependency dep1 = new ProjectDependency();
        dep1.setGroupId( "org.apache.maven.testproject" );
        dep1.setArtifactId( "project1" );
        dep1.setVersion( "1.0-SNAPSHOT" );
        project2.addDependency( dep1 );
        ProjectDependency dep2 = new ProjectDependency();
        dep2.setGroupId( "junit" );
        dep2.setArtifactId( "junit" );
        dep2.setVersion( "3.8.1" );
        project2.addDependency( dep2 );
        BuildDefinition bd2 = createBuildDefinition();
        project2.addBuildDefinition( bd2 );
        project2.setState( ContinuumProjectState.OK );
        projectId2 = addProject( project2 ).getId();
        buildDefinitionId2 = buildDefinitionDao.getDefaultBuildDefinition( projectId2 ).getId();
        createPomFile( getProjectDao().getProjectWithAllDetails( projectId2 ) );

        controller = (DefaultBuildController) lookup( BuildController.ROLE );
    }

    private Project createProject( String artifactId )
    {
        Project project = new Project();
        project.setExecutorId( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );
        project.setName( artifactId );
        project.setGroupId( "org.apache.maven.testproject" );
        project.setArtifactId( artifactId );
        project.setVersion( "1.0-SNAPSHOT" );
        return project;
    }

    private BuildDefinition createBuildDefinition()
    {
        BuildDefinition builddef = new BuildDefinition();
        Schedule schedule = new Schedule();
        schedule.setName( SCHEDULE_NAME );
        builddef.setSchedule( schedule );
        builddef.setBuildFile( "pom.xml" );
        builddef.setGoals( "clean" );
        builddef.setDefaultForProject( true );
        return builddef;
    }

    private BuildContext getScheduledBuildContext()
        throws Exception
    {
        return controller.initializeBuildContext( projectId2, buildDefinitionId2, new BuildTrigger(
            ContinuumProjectState.TRIGGER_SCHEDULED ), new ScmResult() );
    }

    private BuildContext getForcedBuildContext()
        throws Exception
    {
        return controller.initializeBuildContext( projectId2, buildDefinitionId2, new BuildTrigger(
            ContinuumProjectState.TRIGGER_FORCED, FORCED_BUILD_USER ), new ScmResult() );
    }

    private BuildContext getContext( int hourOfLastExecution )
        throws Exception
    {
        BuildContext context = getScheduledBuildContext();
        BuildResult oldBuildResult = new BuildResult();
        oldBuildResult.setEndTime( Calendar.getInstance().getTimeInMillis() + ( hourOfLastExecution * 3600000 ) );
        context.setOldBuildResult( oldBuildResult );
        context.setScmResult( new ScmResult() );

        Map<String, Object> actionContext = context.getActionContext();
        ProjectScmRoot projectScmRoot = new ProjectScmRoot();
        projectScmRoot.setId( 1 );
        projectScmRoot.setScmRootAddress( "scm:local:src/test-projects:flat-multi-module" );
        AbstractContinuumAction.setProjectScmRoot( actionContext, projectScmRoot );

        return context;
    }

    public void testWithoutDependencyChanges()
        throws Exception
    {
        BuildContext context = getContext( +1 );
        controller.checkProjectDependencies( context );
        assertEquals( 0, context.getModifiedDependencies().size() );
        assertFalse( controller.shouldBuild( context ) );
    }

    public void testWithNewProjects()
        throws Exception
    {
        Project p1 = getProjectDao().getProject( projectId1 );
        p1.setState( ContinuumProjectState.NEW );
        getProjectDao().updateProject( p1 );

        Project p2 = getProjectDao().getProject( projectId2 );
        p2.setState( ContinuumProjectState.NEW );
        getProjectDao().updateProject( p2 );

        BuildContext context = getScheduledBuildContext();
        controller.checkProjectDependencies( context );
        assertEquals( 0, context.getModifiedDependencies().size() );
        assertTrue( controller.shouldBuild( context ) );
    }

    public void testWithNewBuildDefinition()
        throws Exception
    {
        BuildContext context = getScheduledBuildContext();
        assertNull( context.getOldBuildResult() );
        assertTrue( controller.shouldBuild( context ) );
    }

    public void testWithDependencyChanges()
        throws Exception
    {
        BuildContext context = getContext( -1 );
        controller.checkProjectDependencies( context );
        assertEquals( 1, context.getModifiedDependencies().size() );
        assertTrue( controller.shouldBuild( context ) );
    }

    public void testWithNullScmResult()
        throws Exception
    {
        BuildContext context = getContext( +1 );
        context.setScmResult( null );
        controller.checkProjectDependencies( context );
        assertEquals( 0, context.getModifiedDependencies().size() );
        assertFalse( controller.shouldBuild( context ) );
    }

    public void testForcedBuildTriggeredByField()
        throws Exception
    {
        BuildContext context = getForcedBuildContext();
        assertEquals( FORCED_BUILD_USER, context.getBuildTrigger().getTriggeredBy() );
    }

    public void testScheduledBuildTriggeredByField()
        throws Exception
    {
        BuildContext context = getScheduledBuildContext();
        assertEquals( SCHEDULE_NAME, context.getBuildTrigger().getTriggeredBy() );
    }

    public void testScheduledBuildTriggeredByField_UsernameProvided()
        throws Exception
    {
        BuildTrigger buildTrigger = new BuildTrigger( ContinuumProjectState.TRIGGER_SCHEDULED, "test-user" );

        BuildContext context = controller.initializeBuildContext( projectId2, buildDefinitionId2, buildTrigger,
                                                                  new ScmResult() );

        String contextTriggeredBy = context.getBuildTrigger().getTriggeredBy();
        assertFalse( "test-user".equals( contextTriggeredBy ) );
        assertEquals( SCHEDULE_NAME, contextTriggeredBy );
    }

    private File getWorkingDirectory()
        throws Exception
    {
        File workingDirectory = getTestFile( "target/working-directory" );

        if ( !workingDirectory.exists() )
        {
            workingDirectory.mkdir();
        }

        return workingDirectory;
    }

    private File getWorkingDirectory( Project project )
        throws Exception
    {
        File projectDir = new File( getWorkingDirectory(), Integer.toString( project.getId() ) );

        if ( !projectDir.exists() )
        {
            projectDir.mkdirs();
            System.out.println( "projectdirectory created" + projectDir.getAbsolutePath() );
        }

        return projectDir;
    }

    private void createPomFile( Project project )
        throws Exception
    {
        File pomFile = new File( getWorkingDirectory( project ), "pom.xml" );

        BufferedWriter out = new BufferedWriter( new FileWriter( pomFile ) );
        out.write( "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                       "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                       "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" );
        out.write( "<modelVersion>4.0.0</modelVersion>\n" );
        out.write( "<groupId>" + project.getGroupId() + "</groupId>\n" );
        out.write( "<artifactId>" + project.getArtifactId() + "</artifactId>\n" );
        out.write( "<version>" + project.getVersion() + "</version>\n" );
        out.write( "<scm>\n" );
        out.write( "<connection>" + "scm:local|" + getWorkingDirectory().getAbsolutePath() +
                       "|" + project.getId() + "</connection>\n" );
        out.write( "</scm>" );

        if ( project.getDependencies().size() > 0 )
        {
            out.write( "<dependencies>\n" );

            List<ProjectDependency> dependencies = project.getDependencies();

            for ( ProjectDependency dependency : dependencies )
            {
                out.write( "<dependency>\n" );
                out.write( "<groupId>" + dependency.getGroupId() + "</groupId>\n" );
                out.write( "<artifactId>" + dependency.getArtifactId() + "</artifactId>\n" );
                out.write( "<version>" + dependency.getVersion() + "</version>\n" );
                out.write( "</dependency>\n" );
            }
            out.write( "</dependencies>\n" );
        }

        out.write( "</project>" );
        out.close();

        System.out.println( "pom file created" );
    }
}
