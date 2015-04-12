package org.apache.maven.continuum;

import org.apache.continuum.AbstractAddProjectTest;
import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.*;

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

/**
 * @author olamy
 */
public class AddMaven2ProjectTest
    extends AbstractAddProjectTest
{
    protected final Logger log = LoggerFactory.getLogger( getClass() );

    protected BuildDefinitionTemplate bdt;

    protected BuildDefinition bd;

    protected BuildDefinitionService bds;

    @Before
    public void setUp()
        throws Exception
    {
        bd = new BuildDefinition();
        bd.setGoals( "clean deploy" );
        bd.setBuildFile( "pom.xml" );
        bd.setDescription( "my foo" );
        bd.setTemplate( true );
        bds = lookup( BuildDefinitionService.class, "default" );
        bd = bds.addBuildDefinition( bd );

        assertEquals( 5, bds.getAllBuildDefinitions().size() );

        bdt = new BuildDefinitionTemplate();
        bdt.setName( "bdt foo" );

        bdt = bds.addBuildDefinitionTemplate( bdt );

        bdt = bds.addBuildDefinitionInTemplate( bdt, bd, false );

        createLocalRepository();
    }

    @Test
    public void testAddProjectToExistingGroupWithBuildDefTemplate()
        throws Exception
    {
        ProjectGroup pg = new ProjectGroup();
        pg.setName( "foo" );
        pg.setDescription( "foo pg" );
        getContinuum().addProjectGroup( pg );
        pg = getContinuum().getAllProjectGroups().get( 1 );
        assertEquals( 2, getContinuum().getAllProjectGroups().size() );
        pg = getContinuum().getProjectGroupWithBuildDetails( pg.getId() );
        // group created with the m2 default build def 
        assertEquals( 1, pg.getBuildDefinitions().size() );
        assertEquals( "clean install", pg.getBuildDefinitions().get( 0 ).getGoals() );

        File rootPom = getTestFile( "src/test/resources/projects/continuum/continuum-core/pom.xml" );

        assertTrue( rootPom.exists() );
        //String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();
        ContinuumProjectBuildingResult result = getContinuum().addMavenTwoProject(
            rootPom.toURI().toURL().toExternalForm(), pg.getId(), true, false, false, bdt.getId(), false );
        assertNotNull( result );

        assertEquals( Collections.emptyList(), result.getErrors() );

        assertEquals( 1, result.getProjects().size() );

        Project project = result.getProjects().get( 0 );
        project = getContinuum().getProjectWithBuildDetails( project.getId() );
        assertNotNull( project );
        log.info( "project buildDef list size : " + project.getBuildDefinitions().size() );
        assertEquals( 1, project.getBuildDefinitions().size() );
        // project with the build def coming from template
        assertEquals( "clean deploy", project.getBuildDefinitions().get( 0 ).getGoals() );
    }

    @Test
    public void testAddProjectWithGroupCreationWithBuildDefTemplate()
        throws Exception
    {

        //bdt = bds.addBuildDefinitionInTemplate( bdt, bd, true );
        File rootPom = getTestFile( "src/test/resources/projects/continuum/continuum-core/pom.xml" );

        assertTrue( rootPom.exists() );

        ContinuumProjectBuildingResult result = getContinuum().addMavenTwoProject(
            rootPom.toURI().toURL().toExternalForm(), -1, true, false, true, bdt.getId(), false );
        assertNotNull( result );

        assertEquals( Collections.emptyList(), result.getErrors() );

        assertEquals( 1, result.getProjects().size() );
        Project project = result.getProjects().get( 0 );
        assertNotNull( project );
        project = getContinuum().getProjectWithBuildDetails( project.getId() );
        log.info( "project buildDef list size : " + project.getBuildDefinitions().size() );
        // build def only at the group level du to group creation 
        assertEquals( 0, project.getBuildDefinitions().size() );

        log.info( "all pg size " + getContinuum().getAllProjectGroups().size() );
        ProjectGroup pg = result.getProjectGroups().get( 0 );

        pg = getContinuum().getProjectGroupWithBuildDetails( pg.getId() );

        log.info( " pg groupId " + pg.getGroupId() );
        //@ group level the db from template must be used
        log.info( " pg builddefs size " + pg.getBuildDefinitions().size() );
        log.info( "pg bd goals " + ( pg.getBuildDefinitions().get( 0 ) ).getGoals() );
        assertEquals( "clean deploy", ( pg.getBuildDefinitions().get( 0 ) ).getGoals() );

    }

    @Test
    public void testAddProjectWithGroupCreationDefaultBuildDef()
        throws Exception
    {

        //bdt = bds.addBuildDefinitionInTemplate( bdt, bd, true );
        File rootPom = getTestFile( "src/test/resources/projects/continuum/continuum-core/pom.xml" );

        assertTrue( rootPom.exists() );

        ContinuumProjectBuildingResult result = getContinuum().addMavenTwoProject(
            rootPom.toURI().toURL().toExternalForm(), -1, true, false, true, -1, false );
        assertNotNull( result );

        assertEquals( Collections.emptyList(), result.getErrors() );

        assertEquals( 1, result.getProjects().size() );

        Project project = result.getProjects().get( 0 );
        assertNotNull( project );

        assertNotNull( project );
        project = getContinuum().getProjectWithBuildDetails( project.getId() );
        log.info( "project buildDef list size : " + project.getBuildDefinitions().size() );
        // only build def at group level
        assertEquals( 0, project.getBuildDefinitions().size() );

        log.info( "all pg size " + getContinuum().getAllProjectGroups().size() );
        ProjectGroup pg = result.getProjectGroups().get( 0 );

        log.info( getContinuum().getAllProjectGroups().toString() );
        log.info( " pg id " + Integer.toString( pg.getId() ) );

        pg = getContinuum().getProjectGroupWithBuildDetails( pg.getId() );

        //@ group level the db from template must be used
        assertEquals( "clean install", pg.getBuildDefinitions().get( 0 ).getGoals() );
    }

    @Test
    public void testAddProjectToExistingGroupDefaultBuildDef()
        throws Exception
    {
        ProjectGroup pg = new ProjectGroup();
        String groupId = "foo";
        pg.setName( groupId );
        pg.setGroupId( groupId );
        pg.setDescription( "foo pg" );
        getContinuum().addProjectGroup( pg );
        pg = getContinuum().getProjectGroupByGroupIdWithBuildDetails( groupId );

        assertEquals( 1, pg.getBuildDefinitions().size() );
        BuildDefinition buildDefinition = pg.getBuildDefinitions().get( 0 );
        assertEquals( "clean install", buildDefinition.getGoals() );
        assertEquals( "--batch-mode --non-recursive", buildDefinition.getArguments() );

        File rootPom = getTestFile( "src/test/resources/projects/continuum/continuum-core/pom.xml" );

        assertTrue( rootPom.exists() );
        //String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();
        ContinuumProjectBuildingResult result = getContinuum().addMavenTwoProject(
            rootPom.toURI().toURL().toExternalForm(), pg.getId(), true, false, false, -1, false );
        assertNotNull( result );

        assertEquals( Collections.emptyList(), result.getErrors() );

        assertEquals( 1, result.getProjects().size() );

        Project project = result.getProjects().get( 0 );
        project = getContinuum().getProjectWithBuildDetails( project.getId() );
        assertNotNull( project );
        assertEquals( 1, project.getBuildDefinitions().size() );

        buildDefinition = project.getBuildDefinitions().get( 0 );
        assertEquals( "clean install", buildDefinition.getGoals() );
        assertEquals( "--batch-mode", buildDefinition.getArguments() );
    }

    @Test
    public void testAddProjectToExistingGroupMatchingBuildDef()
        throws Exception
    {
        ProjectGroup pg = new ProjectGroup();
        String groupId = "testAddProjectToExistingGroupMatchingBuildDef";
        pg.setName( groupId );
        pg.setGroupId( groupId );
        pg.setDescription( "foo pg" );
        getContinuum().addProjectGroup( pg );
        pg = getContinuum().getProjectGroupByGroupIdWithBuildDetails( groupId );

        assertEquals( 1, pg.getBuildDefinitions().size() );
        BuildDefinition buildDefinition = pg.getBuildDefinitions().get( 0 );
        buildDefinition.setArguments( "--batch-mode" );
        bds.updateBuildDefinition( buildDefinition );

        pg = getContinuum().getProjectGroupByGroupIdWithBuildDetails( groupId );
        buildDefinition = pg.getBuildDefinitions().get( 0 );
        assertEquals( "clean install", buildDefinition.getGoals() );
        assertEquals( "--batch-mode", buildDefinition.getArguments() );

        File rootPom = getTestFile( "src/test/resources/projects/continuum/continuum-core/pom.xml" );

        assertTrue( rootPom.exists() );
        //String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();
        ContinuumProjectBuildingResult result = getContinuum().addMavenTwoProject(
            rootPom.toURI().toURL().toExternalForm(), pg.getId(), true, false, false, -1, false );
        assertNotNull( result );

        assertEquals( Collections.emptyList(), result.getErrors() );

        assertEquals( 1, result.getProjects().size() );

        Project project = result.getProjects().get( 0 );
        project = getContinuum().getProjectWithBuildDetails( project.getId() );
        assertNotNull( project );
        assertEquals( 0, project.getBuildDefinitions().size() );
    }

    private Continuum getContinuum()
        throws Exception
    {
        return (Continuum) lookup( Continuum.ROLE );
    }

    @Override
    protected String getPlexusConfigLocation()
    {
        return "org/apache/maven/continuum/DefaultContinuumTest.xml";
    }

    @Override
    protected String getSpringConfigLocation()
    {
        return "applicationContextSlf4jPlexusLogger.xml";
    }

}
