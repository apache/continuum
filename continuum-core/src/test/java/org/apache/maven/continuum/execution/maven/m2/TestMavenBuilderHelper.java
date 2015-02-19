package org.apache.maven.continuum.execution.maven.m2;

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
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 6 juin 2008
 */
public class TestMavenBuilderHelper
    extends AbstractContinuumTest
{
    private static final Logger log = LoggerFactory.getLogger( TestMavenBuilderHelper.class );

    public void testgetMavenProject()
        throws Exception
    {
        MavenBuilderHelper mavenBuilderHelper = (MavenBuilderHelper) lookup( MavenBuilderHelper.ROLE, "default" );
        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();
        File file = new File( getBasedir(), "src/test-poms/pom.xml" );
        MavenProject project = mavenBuilderHelper.getMavenProject( result, file );
        assertNotNull( project );

        assertEquals( "plexus", project.getGroupId() );
        assertEquals( "continuum-project2", project.getArtifactId() );
        assertEquals( "This is a sample pom for test purposes", project.getDescription() );
        assertNotNull( project.getScm() );
        assertTrue( project.getDependencies().isEmpty() );
        assertTrue( result.getErrors().isEmpty() );
    }

    public void testgetMavenProjectMissingDeps()
        throws Exception
    {
        MavenBuilderHelper mavenBuilderHelper = (MavenBuilderHelper) lookup( MavenBuilderHelper.ROLE, "default" );
        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();
        File file = new File( getBasedir(), "src/test-poms/pom-unknown-dependency.xml" );
        mavenBuilderHelper.getMavenProject( result, file );
        assertFalse( result.getErrors().isEmpty() );
        String errorsAsString = result.getErrorsAsString();
        assertFalse( StringUtils.isEmpty( errorsAsString ) );
        log.info( "errorAsString " + errorsAsString );
        assertTrue( errorsAsString.contains( "ghd:non-exists:pom:2.6.267676-beta-754-alpha-95" ) );
        log.info( "errors " + result.getErrors() );
    }
}
