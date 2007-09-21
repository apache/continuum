package org.apache.maven.continuum.project.builder.maven;

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

import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;

import java.util.Iterator;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenOneContinuumProjectBuilderTest
    extends AbstractContinuumTest
{
    public void testBuildingAProjectFromMetadataWithACompleteMaven1Pom()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder =
            (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE, MavenOneContinuumProjectBuilder.ID );

        
        BuildDefinition bd = new BuildDefinition();

        bd.setDefaultForProject( true );

        bd.setGoals( "clean:clean jar:install" );

        bd.setBuildFile( "project.xml" );

        bd.setType( ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR );
        
        bd.setTemplate( true );
        
        BuildDefinitionService service = (BuildDefinitionService) lookup( BuildDefinitionService.class );
        
        BuildDefinitionTemplate bdt = new BuildDefinitionTemplate();
        bdt.setName( "maven1" );
        bd = service.addBuildDefinition( bd );
        bdt = service.addBuildDefinitionTemplate( bdt );
        bdt = service.addBuildDefinitionInTemplate( bdt, bd, false );
        
        ContinuumProjectBuildingResult result = projectBuilder
            .buildProjectsFromMetadata( getTestFile( "src/test/resources/projects/maven-1.pom.xml" ).toURL(), null,
                                        null, false, bdt );


        assertOnResult( result );
        
    }
    
    public void testBuildingAProjectFromMetadataWithACompleteMaven1PomWithDefaultBuildDef()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder = (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE,
                                                                                   MavenOneContinuumProjectBuilder.ID );

        BuildDefinitionService service = (BuildDefinitionService) lookup( BuildDefinitionService.class );

        ContinuumProjectBuildingResult result = projectBuilder
            .buildProjectsFromMetadata( getTestFile( "src/test/resources/projects/maven-1.pom.xml" ).toURL(), null,
                                        null, false, service.getDefaultMavenOneBuildDefinitionTemplate() );

        assertOnResult( result );

    }    
    
    protected void assertOnResult( ContinuumProjectBuildingResult result )
    {
        assertNotNull( result.getWarnings() );

        assertNotNull( result.getProjects() );

        for ( Iterator it = result.getWarnings().iterator(); it.hasNext(); )
        {
            String s = (String) it.next();

            System.err.println( s );
        }

        assertEquals( "result.warning.length", 0, result.getWarnings().size() );

        assertEquals( "result.projects.length", 1, result.getProjects().size() );

        Project project = (Project) result.getProjects().get( 0 );

        assertNotNull( project );

        assertEquals( "Maven", project.getName() );

        assertEquals( "scm:svn:http://svn.apache.org/repos/asf:maven/maven-1/core/trunk/", project.getScmUrl() );

        ProjectNotifier notifier = (ProjectNotifier) project.getNotifiers().get( 0 );

        assertEquals( "mail", notifier.getType() );

        assertEquals( "dev@maven.apache.org", notifier.getConfiguration().get( "address" ) );

        assertEquals( "1.1-SNAPSHOT", project.getVersion() );
    }
}
