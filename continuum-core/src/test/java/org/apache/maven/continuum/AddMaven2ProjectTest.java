package org.apache.maven.continuum;

import java.io.File;
import java.util.Collections;

import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @since 
 * @version $Id$
 */
public class AddMaven2ProjectTest
    extends AbstractContinuumTest
{
    protected final Logger log = LoggerFactory.getLogger( getClass() );

    protected BuildDefinitionTemplate bdt;

    protected BuildDefinition bd;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        bd = new BuildDefinition();
        bd.setGoals( "clean deploy" );
        bd.setBuildFile( "pom.xml" );
        bd.setDescription( "my foo" );
        bd.setTemplate( true );
        BuildDefinitionService bds = (BuildDefinitionService) lookup( BuildDefinitionService.class.getName(), "default" );
        bd = bds.addBuildDefinition( bd );
        
        
        assertEquals( 5, bds.getAllBuildDefinitions().size() );

        bdt = new BuildDefinitionTemplate();
        bdt.setName( "bdt foo" );
        
        bdt = bds.addBuildDefinitionTemplate( bdt );
        
        bdt = bds.addBuildDefinitionInTemplate( bdt, bd, false );        
    }    
    
    
    
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
        

        File rootPom = getTestFile( "src/test/resources/projects/continuum/continuum-core/pom.xml" );

        assertTrue( rootPom.exists() );
        //String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();
        ContinuumProjectBuildingResult result = getContinuum().addMavenTwoProject(
                                                                                   rootPom.toURI().toURL()
                                                                                       .toExternalForm(), pg.getId(),
                                                                                       true, false, false, bdt.getId(), false );
        assertNotNull( result );

        assertEquals( Collections.emptyList(), result.getErrors() );

        assertEquals( 1, result.getProjects().size() );

        Project project = result.getProjects().get( 0 );
        project = getContinuum().getProjectWithBuildDetails( project.getId() );
        assertNotNull( project );
        pg = getContinuum().getProjectGroupWithBuildDetails( pg.getId() );
        log.info( "project buildDef list size : " + project.getBuildDefinitions().size() );
        // project with the build def coming from template
        assertEquals( 1, project.getBuildDefinitions().size() );
        assertEquals( "clean deploy", ( (BuildDefinition) project.getBuildDefinitions().get( 0 ) ).getGoals() );
    }
    
    public void testAddProjectWithGroupCreationWithBuildDefTemplate()
        throws Exception
    {

        //bdt = bds.addBuildDefinitionInTemplate( bdt, bd, true );
        File rootPom = getTestFile( "src/test/resources/projects/continuum/continuum-core/pom.xml" );

        assertTrue( rootPom.exists() );

        ContinuumProjectBuildingResult result = getContinuum().addMavenTwoProject(
                                                                                   rootPom.toURI().toURL()
                                                                                       .toExternalForm(), -1, true,
                                                                                       false, true, bdt.getId(), false );
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
        log.info( "pg bd goals " + ( (BuildDefinition) pg.getBuildDefinitions().get( 0 ) ).getGoals() );
        assertEquals( "clean deploy", ( (BuildDefinition) pg.getBuildDefinitions().get( 0 ) ).getGoals() );
        
    }    
    
    public void testAddProjectWithGroupCreationDefaultBuildDef()
        throws Exception
    {

        //bdt = bds.addBuildDefinitionInTemplate( bdt, bd, true );
        File rootPom = getTestFile( "src/test/resources/projects/continuum/continuum-core/pom.xml" );

        assertTrue( rootPom.exists() );

        ContinuumProjectBuildingResult result = getContinuum().addMavenTwoProject(
                                                                                   rootPom.toURI().toURL()
                                                                                       .toExternalForm(), -1, true,
                                                                                       false, true, -1, false );
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

        log.info( " pg groupId " + pg.getGroupId() );
        //@ group level the db from template must be used
        log.info( " mg builddefs size " + pg.getBuildDefinitions().size() );
        log.info( "pg bd goals " + ( (BuildDefinition) pg.getBuildDefinitions().get( 0 ) ).getGoals() );
        assertEquals( "clean install", ( (BuildDefinition) pg.getBuildDefinitions().get( 0 ) ).getGoals() );

    }       

    public void testAddProjectToExistingGroupDefaultBuildDef()
        throws Exception
    {

        ProjectGroup pg = new ProjectGroup();
        pg.setName( "foo" );
        pg.setDescription( "foo pg" );
        getContinuum().addProjectGroup( pg );
        pg = getContinuum().getAllProjectGroups().get( 1 );
        assertEquals( 2, getContinuum().getAllProjectGroups().size() );

        File rootPom = getTestFile( "src/test/resources/projects/continuum/continuum-core/pom.xml" );

        assertTrue( rootPom.exists() );
        //String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();
        ContinuumProjectBuildingResult result = getContinuum().addMavenTwoProject(
                                                                                   rootPom.toURI().toURL()
                                                                                       .toExternalForm(), pg.getId(),
                                                                                       true, false, false, -1, false );
        assertNotNull( result );

        assertEquals( Collections.emptyList(), result.getErrors() );

        assertEquals( 1, result.getProjects().size() );

        Project project = result.getProjects().get( 0 );
        project = getContinuum().getProjectWithBuildDetails( project.getId() );
        assertNotNull( project );
        pg = getContinuum().getProjectGroupWithBuildDetails( pg.getId() );
        log.info( "project buildDef list size : " + project.getBuildDefinitions().size() );
        assertEquals( 1, project.getBuildDefinitions().size() );
        pg = result.getProjectGroups().get( 0 );

        pg = getContinuum().getProjectGroupWithBuildDetails( pg.getId() );
        
        assertEquals( "clean install", ( (BuildDefinition) pg.getBuildDefinitions().get( 0 ) ).getGoals() );
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
