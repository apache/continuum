package org.apache.maven.continuum.core.action;

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

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManager;
import org.apache.maven.continuum.utils.ContinuumUrlValidator;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class CreateProjectsFromMetadataTest
    extends MockObjectTestCase
{

    private CreateProjectsFromMetadataAction action;

    private ContinuumProjectBuildingResult result;

    protected void setUp()
        throws Exception
    {
        result = new ContinuumProjectBuildingResult();
        action = new CreateProjectsFromMetadataAction();
        action.enableLogging( new ConsoleLogger( Logger.LEVEL_DEBUG, "" ) );
        Mock projectBuilderManagerMock = mock( ContinuumProjectBuilderManager.class );
        Mock mavenSettingsBuilderMock = mock( MavenSettingsBuilder.class );
        action.setProjectBuilderManager( (ContinuumProjectBuilderManager) projectBuilderManagerMock.proxy() );
        action.setMavenSettingsBuilder( (MavenSettingsBuilder) mavenSettingsBuilderMock.proxy() );
        action.setUrlValidator( new ContinuumUrlValidator() );
        Mock projectBuilder = mock( ContinuumProjectBuilder.class );

        projectBuilderManagerMock.expects( once() ).method( "getProjectBuilder" ).will(
            returnValue( projectBuilder.proxy() ) );
        projectBuilder.expects( once() ).method( "buildProjectsFromMetadata" ).will(
            returnValue( result ) );

        projectBuilder.expects( once() ).method( "getDefaultBuildDefinitionTemplate" ).will(
            returnValue( getDefaultBuildDefinitionTemplate() ) );

        mavenSettingsBuilderMock.expects( once() ).method( "buildSettings" ).will( returnValue( new Settings() ) );

    }

    private BuildDefinitionTemplate getDefaultBuildDefinitionTemplate()
        throws Exception
    {
        BuildDefinition bd = new BuildDefinition();

        bd.setDefaultForProject( true );

        bd.setGoals( "clean install" );

        bd.setArguments( "-B" );

        bd.setBuildFile( "pom.xml" );

        bd.setType( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );

        BuildDefinitionTemplate bdt = new BuildDefinitionTemplate();
        bdt.addBuildDefinition( bd );
        return bdt;
    }

    @SuppressWarnings("unchecked")
    public void testExecuteWithNonRecursiveMode()
        throws Exception
    {
        Map<String, Object> context = new HashMap<String, Object>();
        CreateProjectsFromMetadataAction.setUrl( context,
                                                 "http://svn.apache.org/repos/asf/maven/continuum/trunk/pom.xml" );
        CreateProjectsFromMetadataAction.setProjectBuilderId( context, "id" );
        CreateProjectsFromMetadataAction.setLoadRecursiveProject( context, true );

        action.execute( context );

        ContinuumProjectBuildingResult result = CreateProjectsFromMetadataAction.getProjectBuildingResult( context );

        assertFalse(
            "Should not have errors but had " + result.getErrorsAsString() + " (this test requires internet access)",
            result.hasErrors() );
    }

    public void testExecuteWithRecursiveMode()
        throws Exception
    {
        Map<String, Object> context = new HashMap<String, Object>();
        CreateProjectsFromMetadataAction.setUrl( context,
                                                 "http://svn.apache.org/repos/asf/maven/archiva/trunk/pom.xml" );
        CreateProjectsFromMetadataAction.setProjectBuilderId( context, "id" );
        CreateProjectsFromMetadataAction.setLoadRecursiveProject( context, false );

        action.execute( context );

        ContinuumProjectBuildingResult result = CreateProjectsFromMetadataAction.getProjectBuildingResult( context );

        assertFalse(
            "Should not have errors but had " + result.getErrorsAsString() + " (this test requires internet access)",
            result.hasErrors() );
    }

    public void testExecuteFlatMultiModuleProjectThatStartsWithTheSameLetter()
        throws Exception
    {
        Project project = new Project();
        project.setGroupId( "com.example.flat" );
        project.setArtifactId( "flat-parent" );
        project.setVersion( "1.0-SNAPSHOT" );
        project.setId( 6 );
        project.setName( "Flat Example" );
        project.setScmUrl( "scm:svn:http://svn.apache.org/repos/asf/continuum/sandbox/flat-example/flat-parent" );

        this.result.addProject( project );

        project = new Project();
        project.setGroupId( "com.example.flat" );
        project.setArtifactId( "flat-core" );
        project.setVersion( "1.0-SNAPSHOT" );
        project.setId( 7 );
        project.setName( "flat-core" );
        project.setScmUrl( "scm:svn:http://svn.apache.org/repos/asf/continuum/sandbox/flat-example/flat-core" );

        this.result.addProject( project );

        project = new Project();
        project.setGroupId( "com.example.flat" );
        project.setArtifactId( "flat-webapp" );
        project.setVersion( "1.0-SNAPSHOT" );
        project.setId( 8 );
        project.setName( "flat-webapp Maven Webapp" );
        project.setScmUrl( "scm:svn:http://svn.apache.org/repos/asf/continuum/sandbox/flat-example/flat-webapp" );

        this.result.addProject( project );

        Map<String, Object> context = new HashMap<String, Object>();
        CreateProjectsFromMetadataAction.setUrl( context,
                                                 "http://svn.apache.org/repos/asf/continuum/sandbox/flat-example/flat-parent/pom.xml" );
        CreateProjectsFromMetadataAction.setProjectBuilderId( context, "id" );
        CreateProjectsFromMetadataAction.setLoadRecursiveProject( context, true );

        action.execute( context );

        ContinuumProjectBuildingResult result = CreateProjectsFromMetadataAction.getProjectBuildingResult( context );

        assertFalse(
            "Should not have errors but had " + result.getErrorsAsString() + " (this test requires internet access)",
            result.hasErrors() );

        assertEquals(
            "Wrong scm root url created", "scm:svn:http://svn.apache.org/repos/asf/continuum/sandbox/flat-example/",
            CreateProjectsFromMetadataAction.getUrl( context ) );
    }
}
