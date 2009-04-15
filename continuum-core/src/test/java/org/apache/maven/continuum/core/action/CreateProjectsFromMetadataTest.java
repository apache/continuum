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

    protected void setUp()
        throws Exception
    {
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
            returnValue( new ContinuumProjectBuildingResult() ) );

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
        Map context = new HashMap();
        context.put( CreateProjectsFromMetadataAction.KEY_URL,
                     "http://svn.apache.org/repos/asf/maven/continuum/trunk/pom.xml" );
        context.put( CreateProjectsFromMetadataAction.KEY_PROJECT_BUILDER_ID, "id" );
        context.put( CreateProjectsFromMetadataAction.KEY_LOAD_RECURSIVE_PROJECTS, true );

        action.execute( context );

        ContinuumProjectBuildingResult result =
            (ContinuumProjectBuildingResult) context.get( CreateProjectsFromMetadataAction.KEY_PROJECT_BUILDING_RESULT )
            ;

        assertFalse(
            "Should not have errors but had " + result.getErrorsAsString() + " (this test requires internet access)",
            result.hasErrors() );
    }

    public void testExecuteWithRecursiveMode()
        throws Exception
    {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put( CreateProjectsFromMetadataAction.KEY_URL,
                     "http://svn.apache.org/repos/asf/maven/archiva/trunk/pom.xml" );
        context.put( CreateProjectsFromMetadataAction.KEY_PROJECT_BUILDER_ID, "id" );
        context.put( CreateProjectsFromMetadataAction.KEY_LOAD_RECURSIVE_PROJECTS, false );

        action.execute( context );

        ContinuumProjectBuildingResult result =
            (ContinuumProjectBuildingResult) context.get( CreateProjectsFromMetadataAction.KEY_PROJECT_BUILDING_RESULT )
            ;

        assertFalse(
            "Should not have errors but had " + result.getErrorsAsString() + " (this test requires internet access)",
            result.hasErrors() );
    }

}
