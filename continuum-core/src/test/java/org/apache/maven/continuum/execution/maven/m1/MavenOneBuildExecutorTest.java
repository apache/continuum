package org.apache.maven.continuum.execution.maven.m1;

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
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class MavenOneBuildExecutorTest
    extends AbstractContinuumTest
{
    private File checkOut;

    private MavenOneBuildExecutor executor;

    @Before
    public void setUp()
        throws Exception
    {
        BuildExecutorManager builderManager = lookup( BuildExecutorManager.class );

        executor = (MavenOneBuildExecutor) builderManager.getBuildExecutor( MavenOneBuildExecutor.ID );

        // ----------------------------------------------------------------------
        // Make a checkout
        // ----------------------------------------------------------------------

        checkOut = getTestFile( "target/test-checkout" );

        if ( !checkOut.exists() )
        {
            assertTrue( checkOut.mkdirs() );
        }

        getFileSystemManager().wipeDir( checkOut );

    }

    @Test
    public void testUpdatingAProjectFromScmWithAExistingProjectAndAEmptyMaven1Pom()
        throws Exception
    {
        getFileSystemManager().writeFile( new File( checkOut, "project.xml" ), "<project/>" );

        // ----------------------------------------------------------------------
        // Make the "existing" project
        // ----------------------------------------------------------------------

        Project project = new Project();

        project.setName( "Maven" );

        project.setGroupId( "org.apache.maven" );

        project.setArtifactId( "maven" );

        project.setScmUrl( "scm:svn:http://svn.apache.org/repos/asf:maven/maven-1/core/trunk/" );

        ProjectNotifier notifier = new ProjectNotifier();

        Properties props = new Properties();

        props.put( "address", "dev@maven.apache.org" );

        notifier.setConfiguration( props );

        notifier.setFrom( ProjectNotifier.FROM_USER );

        List<ProjectNotifier> notifiers = new ArrayList<ProjectNotifier>();

        notifiers.add( notifier );

        project.setNotifiers( notifiers );

        project.setVersion( "1.1-SNAPSHOT" );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        executor.updateProjectFromCheckOut( checkOut, project, null, null );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertNotNull( project );

        assertEquals( "Maven", project.getName() );

        assertEquals( "scm:svn:http://svn.apache.org/repos/asf:maven/maven-1/core/trunk/", project.getScmUrl() );

        ProjectNotifier actualNotifier = (ProjectNotifier) project.getNotifiers().get( 0 );

        assertEquals( "dev@maven.apache.org", actualNotifier.getConfiguration().get( "address" ) );

        assertEquals( "1.1-SNAPSHOT", project.getVersion() );
    }

    @Test
    public void testUpdatingAProjectWithNagEMailAddress()
        throws Exception
    {
        getFileSystemManager().writeFile( new File( checkOut, "project.xml" ),
                                          "<project><build><nagEmailAddress>myuser@myhost.org</nagEmailAddress></build></project>" );

        // ----------------------------------------------------------------------
        // Make the "existing" project
        // ----------------------------------------------------------------------

        Project project = new Project();

        project.setName( "Maven" );

        project.setGroupId( "org.apache.maven" );

        project.setArtifactId( "maven" );

        project.setScmUrl( "scm:svn:http://svn.apache.org/repos/asf:maven/maven-1/core/trunk/" );

        project.setVersion( "1.1-SNAPSHOT" );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        executor.updateProjectFromCheckOut( checkOut, project, null, null );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertNotNull( project );

        assertEquals( "Maven", project.getName() );

        assertEquals( 1, project.getNotifiers().size() );

        ProjectNotifier actualNotifier = (ProjectNotifier) project.getNotifiers().get( 0 );

        assertEquals( "myuser@myhost.org", actualNotifier.getConfiguration().get( "address" ) );

        // ----------------------------------------------------------------------
        // Updating a new time to prevent duplicated notifiers
        // ----------------------------------------------------------------------

        executor.updateProjectFromCheckOut( checkOut, project, null, null );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertEquals( 1, project.getNotifiers().size() );

        actualNotifier = (ProjectNotifier) project.getNotifiers().get( 0 );

        assertEquals( "myuser@myhost.org", actualNotifier.getConfiguration().get( "address" ) );
    }

    @Test
    public void testUpdatingAProjectWithNagEMailAddressAndOneNotifier()
        throws Exception
    {
        getFileSystemManager().writeFile( new File( checkOut, "project.xml" ),
                                          "<project><build><nagEmailAddress>myuser@myhost.org</nagEmailAddress></build></project>" );

        // ----------------------------------------------------------------------
        // Make the "existing" project
        // ----------------------------------------------------------------------

        Project project = new Project();

        project.setName( "Maven" );

        project.setGroupId( "org.apache.maven" );

        project.setArtifactId( "maven" );

        project.setScmUrl( "scm:svn:http://svn.apache.org/repos/asf:maven/maven-1/core/trunk/" );

        ProjectNotifier notifier = new ProjectNotifier();

        Properties props = new Properties();

        props.put( "address", "dev@maven.apache.org" );

        notifier.setConfiguration( props );

        notifier.setFrom( ProjectNotifier.FROM_USER );

        List<ProjectNotifier> notifiers = new ArrayList<ProjectNotifier>();

        notifiers.add( notifier );

        project.setNotifiers( notifiers );

        project.setVersion( "1.1-SNAPSHOT" );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        executor.updateProjectFromCheckOut( checkOut, project, null, null );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertNotNull( project );

        assertEquals( "Maven", project.getName() );

        assertEquals( 2, project.getNotifiers().size() );

        ProjectNotifier actualNotifier = (ProjectNotifier) project.getNotifiers().get( 0 );

        assertEquals( "myuser@myhost.org", actualNotifier.getConfiguration().get( "address" ) );

        actualNotifier = (ProjectNotifier) project.getNotifiers().get( 1 );

        assertEquals( "dev@maven.apache.org", actualNotifier.getConfiguration().get( "address" ) );

        // ----------------------------------------------------------------------
        // Updating a new time to prevent duplicated notifiers
        // ----------------------------------------------------------------------

        executor.updateProjectFromCheckOut( checkOut, project, null, null );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertEquals( 2, project.getNotifiers().size() );

        actualNotifier = (ProjectNotifier) project.getNotifiers().get( 0 );

        assertEquals( "myuser@myhost.org", actualNotifier.getConfiguration().get( "address" ) );

        actualNotifier = (ProjectNotifier) project.getNotifiers().get( 1 );

        assertEquals( "dev@maven.apache.org", actualNotifier.getConfiguration().get( "address" ) );
    }

    @Test
    public void testUpdatingAProjectWithOneNotifier()
        throws Exception
    {
        getFileSystemManager().writeFile( new File( checkOut, "project.xml" ), "<project/>" );

        // ----------------------------------------------------------------------
        // Make the "existing" project
        // ----------------------------------------------------------------------

        Project project = new Project();

        project.setName( "Maven" );

        project.setGroupId( "org.apache.maven" );

        project.setArtifactId( "maven" );

        project.setScmUrl( "scm:svn:http://svn.apache.org/repos/asf:maven/maven-1/core/trunk/" );

        ProjectNotifier notifier = new ProjectNotifier();

        Properties props = new Properties();

        props.put( "address", "dev@maven.apache.org" );

        notifier.setConfiguration( props );

        notifier.setFrom( ProjectNotifier.FROM_USER );

        List<ProjectNotifier> notifiers = new ArrayList<ProjectNotifier>();

        notifiers.add( notifier );

        project.setNotifiers( notifiers );

        project.setVersion( "1.1-SNAPSHOT" );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        executor.updateProjectFromCheckOut( checkOut, project, null, null );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertNotNull( project );

        assertEquals( "Maven", project.getName() );

        assertEquals( 1, project.getNotifiers().size() );

        ProjectNotifier actualNotifier = (ProjectNotifier) project.getNotifiers().get( 0 );

        assertEquals( "dev@maven.apache.org", actualNotifier.getConfiguration().get( "address" ) );

        // ----------------------------------------------------------------------
        // Updating a new time to prevent duplicated notifiers
        // ----------------------------------------------------------------------

        executor.updateProjectFromCheckOut( checkOut, project, null, null );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertEquals( 1, project.getNotifiers().size() );

        actualNotifier = (ProjectNotifier) project.getNotifiers().get( 0 );

        assertEquals( "dev@maven.apache.org", actualNotifier.getConfiguration().get( "address" ) );
    }
}
