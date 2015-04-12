package org.apache.maven.continuum;

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

import org.apache.continuum.AbstractAddProjectTest;
import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.maven.MavenTwoContinuumProjectBuilder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 12 juin 2008
 */
public class AddProjectTest
    extends AbstractAddProjectTest
{
    static final String SCM_USERNAME = "test";

    static final String SCM_PASSWORD = ";password";

    private Server server;

    private String scmUrl;

    @Before
    public void setUp()
        throws Exception
    {
        createLocalRepository();
        server = startJettyServer();
        int port = server.getConnectors()[0].getLocalPort();
        scmUrl = "http://test:;password@localhost:" + port + "/projects/continuum/continuum-core/pom.xml";
    }

    @After
    public void tearDown()
        throws Exception
    {
        server.stop();
    }

    private Server startJettyServer()
        throws Exception
    {
        Server server = new Server( 0 );
        ResourceHandler handler = new ResourceHandler();
        handler.setResourceBase( getTestFile( "src/test/resources" ).getAbsolutePath() );
        HandlerList handlers = new HandlerList();
        handlers.setHandlers( new Handler[] { handler, new DefaultHandler() } );
        server.setHandler( handlers );
        server.start();
        return server;
    }

    @Test
    public void testScmUserNamePasswordNotStoring()
        throws Exception
    {
        DefaultContinuum continuum = (DefaultContinuum) lookup( Continuum.class );

        ContinuumProjectBuildingResult result = continuum.executeAddProjectsFromMetadataActivity( scmUrl,
                                                                                                  MavenTwoContinuumProjectBuilder.ID,
                                                                                                  getDefaultProjectGroup().getId(),
                                                                                                  false, true, false,
                                                                                                  -1, false, false );
        assertEquals( Collections.emptyList(), result.getErrors() );

        assertEquals( 1, result.getProjects().size() );

        // read the project from store
        Project project = continuum.getProject( result.getProjects().get( 0 ).getId() );
        assertNull( project.getScmUsername() );
        assertNull( project.getScmPassword() );
        assertTrue( project.isScmUseCache() );
    }

    @Test
    public void testScmUserNamePasswordStoring()
        throws Exception
    {
        DefaultContinuum continuum = (DefaultContinuum) lookup( Continuum.class );

        ContinuumProjectBuildingResult result = continuum.executeAddProjectsFromMetadataActivity( scmUrl,
                                                                                                  MavenTwoContinuumProjectBuilder.ID,
                                                                                                  getDefaultProjectGroup().getId(),
                                                                                                  false, false, false,
                                                                                                  -1, false, false );
        assertEquals( Collections.emptyList(), result.getErrors() );

        assertEquals( 1, result.getProjects().size() );

        // read the project from store
        Project project = continuum.getProject( result.getProjects().get( 0 ).getId() );
        assertEquals( SCM_USERNAME, project.getScmUsername() );
        assertEquals( SCM_PASSWORD, project.getScmPassword() );
        assertFalse( project.isScmUseCache() );
    }

    @Test
    public void testAntProjectScmUserNamePasswordNotStoring()
        throws Exception
    {
        Continuum continuum = lookup( Continuum.class );

        Project project = new Project();
        project.setName( "Sample Ant Project" );
        project.setVersion( "1.0" );
        project.setScmUsername( SCM_USERNAME );
        project.setScmPassword( SCM_PASSWORD );
        project.setScmUrl( this.scmUrl );
        project.setScmUseCache( true );

        BuildDefinitionService bdService = lookup( BuildDefinitionService.class );

        int projectId = continuum.addProject( project, ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR,
                                              getDefaultProjectGroup().getId(),
                                              bdService.getDefaultAntBuildDefinitionTemplate().getId() );

        // read the project from store
        Project retrievedProject = continuum.getProject( projectId );
        assertNull( retrievedProject.getScmUsername() );
        assertNull( retrievedProject.getScmPassword() );
        assertTrue( retrievedProject.isScmUseCache() );
    }

    @Test
    public void testAntProjectScmUserNamePasswordStoring()
        throws Exception
    {
        Continuum continuum = lookup( Continuum.class );

        Project project = new Project();
        project.setName( "Sample Ant Project" );
        project.setVersion( "1.0" );
        project.setScmUsername( SCM_USERNAME );
        project.setScmPassword( SCM_PASSWORD );
        project.setScmUrl( scmUrl );
        project.setScmUseCache( false );

        BuildDefinitionService bdService = lookup( BuildDefinitionService.class );

        int projectId = continuum.addProject( project, ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR,
                                              getDefaultProjectGroup().getId(),
                                              bdService.getDefaultAntBuildDefinitionTemplate().getId() );

        // read the project from store
        Project retrievedProject = continuum.getProject( projectId );
        assertEquals( SCM_USERNAME, retrievedProject.getScmUsername() );
        assertEquals( SCM_PASSWORD, retrievedProject.getScmPassword() );
        assertFalse( retrievedProject.isScmUseCache() );
    }

}
