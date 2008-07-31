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
package org.apache.maven.continuum.builddefinition;

import org.apache.log4j.Logger;
import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;

import java.util.List;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @version $Id$
 * @since 15 sept. 07
 */
public class DefaultBuildDefinitionServiceTest
    extends AbstractContinuumTest
{

    private Logger logger = Logger.getLogger( getClass() );

    private ProjectGroup projectGroup;

    private Project project;

    private BuildDefinition buildDefinition;

    private BuildDefinitionTemplate buildDefinitionTemplate;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        getStore().eraseDatabase();

        projectGroup = new ProjectGroup();
        projectGroup.setName( "test" );
        projectGroup = getProjectGroupDao().addProjectGroup( projectGroup );

        project = new Project();
        project.setGroupId( "foo" );
        project.setArtifactId( "bar" );
        project.setVersion( "0.1-alpha-1-SNAPSHOT" );
        projectGroup.addProject( project );
        getProjectGroupDao().updateProjectGroup( projectGroup );

        buildDefinition = new BuildDefinition();
        buildDefinition.setTemplate( true );
        buildDefinition.setArguments( "-N" );
        buildDefinition.setGoals( "clean test-compile" );
        buildDefinition.setBuildFile( "pom.xml" );
        buildDefinition.setDescription( "desc template" );
        buildDefinition = getBuildDefinitionService().addBuildDefinition( buildDefinition );

        buildDefinitionTemplate = new BuildDefinitionTemplate();
        buildDefinitionTemplate.setName( "test" );
        buildDefinitionTemplate = getBuildDefinitionService().addBuildDefinitionTemplate( buildDefinitionTemplate );
        buildDefinitionTemplate =
            getBuildDefinitionService().addBuildDefinitionInTemplate( buildDefinitionTemplate, buildDefinition, false );


    }

    protected BuildDefinitionService getBuildDefinitionService()
        throws Exception
    {
        return (BuildDefinitionService) lookup( BuildDefinitionService.class );
    }

    public void testaddTemplateInProject()
        throws Exception
    {
        try
        {
            List<BuildDefinitionTemplate> templates = getBuildDefinitionService().getAllBuildDefinitionTemplate();
            assertEquals( 5, templates.size() );
            assertEquals( 5, getBuildDefinitionService().getAllBuildDefinitions().size() );

            getBuildDefinitionService().addTemplateInProject( buildDefinitionTemplate.getId(), project );
            project = getStore().getProjectWithAllDetails( project.getId() );
            templates = getBuildDefinitionService().getAllBuildDefinitionTemplate();
            assertEquals( 1, project.getBuildDefinitions().size() );
            assertEquals( 5, templates.size() );
            List<BuildDefinition> all = getBuildDefinitionService().getAllBuildDefinitions();
            assertEquals( 6, all.size() );

            getBuildDefinitionService().addTemplateInProject( buildDefinitionTemplate.getId(), project );

            project = getStore().getProjectWithAllDetails( project.getId() );
            templates = getBuildDefinitionService().getAllBuildDefinitionTemplate();
            assertEquals( 2, project.getBuildDefinitions().size() );
            assertEquals( 5, templates.size() );
            all = getBuildDefinitionService().getAllBuildDefinitions();
            assertEquals( 7, all.size() );

        }
        catch ( Exception e )
        {
            logger.error( e.getMessage(), e );
            throw e;
        }
    }


    public void testGetDefaultBuildDef()
        throws Exception
    {
        BuildDefinition bd = (BuildDefinition) getBuildDefinitionService().getDefaultAntBuildDefinitionTemplate()
            .getBuildDefinitions().get( 0 );
        assertNotNull( bd );
        assertEquals( "build.xml", bd.getBuildFile() );

        bd = (BuildDefinition) getBuildDefinitionService().getDefaultMavenTwoBuildDefinitionTemplate()
            .getBuildDefinitions().get( 0 );
        BuildDefinitionService buildDefinitionService = (BuildDefinitionService) lookup( BuildDefinitionService.class );

        assertEquals( 5, buildDefinitionService.getAllBuildDefinitionTemplate().size() );
        assertNotNull( bd );
        assertEquals( "pom.xml", bd.getBuildFile() );
        assertEquals( "clean install", bd.getGoals() );
    }


    public void testAddBuildDefinitionTemplate()
        throws Exception
    {
        BuildDefinitionTemplate template = new BuildDefinitionTemplate();
        template.setName( "test" );

        template = getBuildDefinitionService().addBuildDefinitionTemplate( template );
        template = getBuildDefinitionService().getBuildDefinitionTemplate( template.getId() );
        assertNotNull( template );
        assertEquals( "test", template.getName() );
        List<BuildDefinition> all = getBuildDefinitionService().getAllBuildDefinitions();
        assertEquals( 5, all.size() );
        BuildDefinition bd = (BuildDefinition) getBuildDefinitionService().getDefaultMavenTwoBuildDefinitionTemplate()
            .getBuildDefinitions().get( 0 );
        template = getBuildDefinitionService().addBuildDefinitionInTemplate( template, bd, false );
        assertEquals( 1, template.getBuildDefinitions().size() );
        all = getBuildDefinitionService().getAllBuildDefinitions();
        assertEquals( 5, all.size() );

        template = getBuildDefinitionService().getBuildDefinitionTemplate( template.getId() );
        template = getBuildDefinitionService().updateBuildDefinitionTemplate( template );
        template = getBuildDefinitionService().removeBuildDefinitionFromTemplate( template, bd );
        assertEquals( 0, template.getBuildDefinitions().size() );
        all = getBuildDefinitionService().getAllBuildDefinitions();
        assertEquals( 5, all.size() );

    }
}
