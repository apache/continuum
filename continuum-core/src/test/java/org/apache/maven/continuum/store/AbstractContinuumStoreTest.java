package org.apache.maven.continuum.store;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Collections;

import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.scm.ScmFile;
import org.apache.maven.continuum.scm.ScmResult;

import org.codehaus.plexus.jdo.JdoFactory;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class AbstractContinuumStoreTest
    extends AbstractContinuumTest
{
    private ContinuumStore store;

    private String roleHint;

    private Class implementationClass;

    public AbstractContinuumStoreTest( String roleHint, Class implementationClass )
    {
        this.roleHint = roleHint;

        this.implementationClass = implementationClass;
    }

    public void setUp()
        throws Exception
    {
        super.setUp();

        store = (ContinuumStore) lookup( ContinuumStore.ROLE, roleHint );

        assertEquals( implementationClass, store.getClass() );

        for ( Iterator it = store.getAllProjects().iterator(); it.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) it.next();

            store.removeProject( project.getId() );
        }
    }

    // ----------------------------------------------------------------------
    // Project
    // ----------------------------------------------------------------------

    public void testAddProject()
        throws Exception
    {
        Properties configuration = new Properties();

        configuration.setProperty( "foo", "bar" );

        String projectId = store.addProject( makeMavenTwoProject( "Test Project",
                                                                  "scm:local:src/test/repo",
                                                                  "foo@bar.com",
                                                                  "1.0",
                                                                  "a b",
                                                                  "/tmp" ) );

        assertNotNull( "The project id is null.", projectId );

        ContinuumProject actual = store.getProject( projectId );

        assertProjectEquals( projectId, makeMavenTwoProject( "Test Project",
                                                             "scm:local:src/test/repo",
                                                             "foo@bar.com",
                                                             "1.0",
                                                             "a b",
                                                             "/tmp" ), actual );
    }

    public void testGetNonExistingProject()
        throws Exception
    {
        try
        {
            store.getProject( "foo" );

            fail( "Expected ContinuumObjectNotFoundException.") ;
        }
        catch( ContinuumObjectNotFoundException ex )
        {
            // expected
        }
        // TODO: Remove me when the generated stuff throws a better exception when the object is missing
        catch ( ContinuumStoreException ex )
        {
            // expected
        }
    }

    public void testProjectCRUD()
        throws Exception
    {
        String name = "Test Project 2";
        String scmUrl = "scm:local:jalla";
        String nagEmailAddress = "foo@bar.com";
        String version = "1.0";
        String commandLineArguments = "";
        String workingDirectory = "/tmp";

        ContinuumProject project = makeMavenTwoProject( name,
                                                        scmUrl,
                                                        nagEmailAddress,
                                                        version,
                                                        commandLineArguments,
                                                        workingDirectory );

        String projectId = store.addProject( project );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        project = store.getProject( projectId );

        assertNotNull( project );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ScmResult scmResult = new ScmResult();

        scmResult.setSuccess( true );

        setCheckoutDone( store, projectId, scmResult, null, null );

        project = store.getProject( projectId );

        assertNotNull( project );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        project = store.getProject( projectId );

        String name2 = "name 2";
        String scmUrl2 = "scm url 2";
        String emailAddress2 = "2@bar";
        String version2 = "v2";
        String commandLineArguments2 = "";

        project.setName( name2 );
        project.setScmUrl( scmUrl2 );

        assertNotNull( project.getNotifiers() );
        assertEquals( 1, project.getNotifiers().size() );
        ContinuumNotifier notifier = ((ContinuumNotifier) project.getNotifiers().get( 0 ));
        notifier.setType( "kewk" );
        notifier.getConfiguration().put( "address", emailAddress2 );
        notifier.getConfiguration().put( "name", "tryg" );
        project.setVersion( version2 );
        project.setCommandLineArguments( commandLineArguments2 );

        store.updateProject( project );

        project = store.getProject( projectId );

        notifier = new ContinuumNotifier();
        notifier.setType( "kewk" );
        notifier.getConfiguration().put( "address", emailAddress2 );
        notifier.getConfiguration().put( "name", "tryg" );
        List notifiers = new ArrayList();
        notifiers.add( notifier );

        assertProjectEquals( projectId,
                             name2,
                             scmUrl2,
                             notifiers,
                             version2,
                             commandLineArguments2,
                             MavenTwoBuildExecutor.ID,
                             workingDirectory,
                             project );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        store.removeProject( projectId );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        try
        {
            store.getProject( "foo" );

            fail( "Expected ContinuumStoreException." );
        }
        catch ( ContinuumObjectNotFoundException ex )
        {
            // expected
        }
        // TODO: Remove me when the generated stuff throws a better exception when the object is missing
        catch ( ContinuumStoreException ex )
        {
            // expected
        }
    }

    public void testGetAllProjects()
        throws Exception
    {
        String name1 = "Test All Projects 1";
        String scmUrl1 = "scm:local:src/test/repo/foo";
        String nagEmailAddress1 = "foo@bar.com";
        String version1 = "1.0";
        String commandLineArguments1 = "";
        String workingDirectory1 = "/tmp";

        String id1 = addMavenTwoProject( store,
                                         name1,
                                         scmUrl1,
                                         nagEmailAddress1,
                                         version1,
                                         commandLineArguments1,
                                         workingDirectory1 );

        String name2 = "Test All Projects 2";
        String scmUrl2 = "scm:local:src/test/repo/bar";
        String nagEmailAddress2 = "foo@bar.com";
        String version2 = "1.0";
        String commandLineArguments2 = "";
        String workingDirectory2 = "/tmp";

        String id2 = addMavenTwoProject( store,
                                         name2,
                                         scmUrl2,
                                         nagEmailAddress2,
                                         version2,
                                         commandLineArguments2,
                                         workingDirectory2 );

        Map projects = new HashMap();

        Collection projectsCollection = store.getAllProjects();

        assertEquals( 2, projectsCollection.size() );

        for ( Iterator it = projectsCollection.iterator(); it.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) it.next();

            assertNotNull( "While getting all projects: project.id", project.getId() );

            assertNotNull( "While getting all projects: project.name", project.getName() );

            projects.put( project.getName(), project );
        }

        ContinuumProject project1 = (ContinuumProject) projects.get( name1 );

        assertProjectEquals( id1,
                             name1,
                             scmUrl1,
                             nagEmailAddress1,
                             version1,
                             commandLineArguments1,
                             MavenTwoBuildExecutor.ID,
                             workingDirectory1,
                             project1 );

        ContinuumProject project2 = (ContinuumProject) projects.get( name2 );

        assertProjectEquals( id2,
                             name2,
                             scmUrl2,
                             nagEmailAddress2,
                             version2,
                             commandLineArguments2,
                             MavenTwoBuildExecutor.ID,
                             workingDirectory2,
                             project2 );
    }

    public void testRemoveProject()
        throws Exception
    {
        String projectId = addMavenTwoProject( "Remove Test Project", "scm:remove-project" );

        String buildId = createBuild( store, projectId, false );

        ScmResult scmResult = new ScmResult();

        ScmFile file = new ScmFile();

        file.setPath( "foo" );

        scmResult.addFile( file );

        setBuildResult( store,
                        buildId,
                        ContinuumProjectState.OK,
                        makeContinuumBuildExecutionResult( true, "", "", 0 ),
                        scmResult,
                        null );

        store.removeProject( projectId );
    }

    private ContinuumBuildExecutionResult makeContinuumBuildExecutionResult( boolean success,
                                                                             String standardOutput,
                                                                             String standardError,
                                                                             int exitCode )
    {
        return new ContinuumBuildExecutionResult( success,
                                                  standardOutput,
                                                  standardError,
                                                  exitCode );
    }

    public void testGetLatestBuildForProject()
        throws Exception
    {
        String projectId = addMavenTwoProject( store,
                                               makeStubMavenTwoProject( "Last project", "scm:foo" ) );

        assertNull( store.getLatestBuildForProject( projectId ) );

        List expectedBuilds = new ArrayList();

        for ( int i = 0; i < 10; i++ )
        {
            expectedBuilds.add( createBuild( store, projectId, false ) );
        }

        assertEquals( expectedBuilds.get( expectedBuilds.size() - 1 ),
                      store.getLatestBuildForProject( projectId ).getId() );
    }

    // ----------------------------------------------------------------------
    // Maven Two project tests
    // ----------------------------------------------------------------------

    public void testUpdateMavenTwoProject()
        throws Exception
    {
        String projectId = addMavenTwoProject( "Maven Two Project", "scm:foo" );

        MavenTwoProject project = (MavenTwoProject) store.getProject( projectId );

        project.setName( "New name" );
        project.setGoals( "clean test" );

        store.updateProject( project );

        project = (MavenTwoProject) store.getProject( projectId );

        assertEquals( "New name", project.getName() );
        assertEquals( "clean test", project.getGoals() );
    }
/*
    public void testUpdateMavenTwoProjectWithANonJdoObject()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        // ----------------------------------------------------------------------
        // Make a project in the store
        // ----------------------------------------------------------------------

        String projectId = addMavenTwoProject( "Maven Two Project", "scm:foo" );

        // ----------------------------------------------------------------------
        // This is a object constructed from outside Continuum, typically
        // something that comes in over the wire.
        // ----------------------------------------------------------------------

        MavenTwoProject external = makeStubMavenTwoProject( "Maven Two Project", "scm:foo" );

        external.setId( projectId );

        external.setName( "New name" );

        MavenTwoProject p = (MavenTwoProject) store.getProject( projectId );

        assertEquals( "Maven Two Project", p.getName() );

        store.updateProject( external );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        MavenTwoProject actual = (MavenTwoProject) store.getProject( projectId );

        assertEquals( "New name", actual.getName() );
    }
*/
    // ----------------------------------------------------------------------
    // Build
    // ----------------------------------------------------------------------

    public void testBuild()
        throws Exception
    {
        lookup( JdoFactory.ROLE );

        String projectId = addMavenTwoProject( "Build Test Project", "scm:build" );

        // ----------------------------------------------------------------------
        // Construct a build object
        // ----------------------------------------------------------------------

        String buildId = createBuild( store, projectId, false );

        ScmResult scmResult = new ScmResult();

        scmResult.setCommandOutput( "commandOutput" );

        scmResult.setProviderMessage( "providerMessage" );

        scmResult.setSuccess( true );

        ScmFile scmFile = new ScmFile();

        scmFile.setPath( "/foo" );

        scmResult.getFiles().add( scmFile );

        setBuildComplete( store,
                          buildId,
                          scmResult,
                          makeContinuumBuildExecutionResult( true, "stdout", "stderr", 10 ) );

        // ----------------------------------------------------------------------
        // Store and check the build object
        // ----------------------------------------------------------------------

        Collection builds = store.getBuildsForProject( projectId, 0, 0 );

        assertNotNull( "The collection with all builds was null.", builds );

        assertEquals( "Expected the build set to contain a single build.", 1, builds.size() );

        ContinuumBuild build = (ContinuumBuild) builds.iterator().next();

        assertNotNull( build );

        assertEquals( "build.id", buildId, build.getId() );
    }

    private void setBuildComplete( ContinuumStore store,
                                   String buildId,
                                   ScmResult scmResult,
                                   ContinuumBuildExecutionResult result )
        throws ContinuumStoreException
    {
        ContinuumBuild build = store.getBuild( buildId );

        build.setScmResult( scmResult );

        build.setSuccess( result.isSuccess() );

        build.setStandardOutput( result.getStandardOutput() );

        build.setStandardError( result.getStandardError() );

        build.setExitCode( result.getExitCode() );

        store.updateBuild( build );
    }

    public void testTheAssociationBetweenTheProjectAndItsBuilds()
        throws Exception
    {
        lookup( JdoFactory.ROLE );

        // ----------------------------------------------------------------------
        // Set up projects
        // ----------------------------------------------------------------------

        String projectId = addMavenTwoProject( "Association Test Project", "scm:association" );

        String projectIdFoo = addMavenTwoProject( "Foo Project", "scm:association-foo" );

        String projectIdBar = addMavenTwoProject( "Bar Project", "scm:association-bar" );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        List expectedBuilds = new ArrayList();

        for ( int i = 0; i < 10; i++ )
        {
            expectedBuilds.add( createBuild( store, projectId, false ) );

            createBuild( store, projectIdFoo, false );

            createBuild( store, projectIdBar, false );

            createBuild( store, projectIdFoo, false );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ContinuumBuild build = store.getLatestBuildForProject( projectId );

        assertNotNull( build );

        assertEquals( build.getId(), (String) expectedBuilds.get( expectedBuilds.size() - 1) );

        List actualBuilds = new ArrayList( store.getBuildsForProject( projectId, 0, 0 ) );

        Collections.reverse( actualBuilds );

        assertEquals( "builds.size", expectedBuilds.size(), actualBuilds.size() );

        Iterator expectedIt = expectedBuilds.iterator();

        Iterator actualIt = actualBuilds.iterator();

        for ( int i = 0; expectedIt.hasNext(); i++ )
        {
            String expectedBuildId = (String) expectedIt.next();

            String actualBuildId = ((ContinuumBuild) actualIt.next()).getId();

            assertEquals( "builds[" + i + "]", expectedBuildId, actualBuildId );
        }
    }

    public void testGetLatestBuild()
        throws Exception
    {
        String projectId = addMavenTwoProject( "Association Test Project", "scm:association" );

        int size = 10;

        List expectedBuilds = new ArrayList();

        for ( int i = 0; i < size; i++ )
        {
            expectedBuilds.add( createBuild( store, projectId, false ) );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ContinuumBuild build = store.getLatestBuildForProject( projectId );

        assertNotNull( build );

        assertEquals( build.getId(), (String) expectedBuilds.get( size - 1 ) );

        Collection actualBuilds = store.getBuildsForProject( projectId, 0, 0 );

        assertEquals( build.getId(), ( (ContinuumBuild) actualBuilds.iterator().next() ).getId() );

        assertEquals( size, actualBuilds.size() );
    }

    public void testBuildResult()
        throws Exception
    {
        lookup( JdoFactory.ROLE );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String projectId = addMavenTwoProject( "Build Result Project", "scm:build/result" );

        long now = System.currentTimeMillis();

        String buildId = createBuild( store, projectId, false );

        assertNotNull( buildId );

        ContinuumBuild build = store.getBuild( buildId );

        assertNotNull( build );

        assertEquals( now / 10000, build.getStartTime() / 10000 );

        assertEquals( 0, build.getEndTime() );

        assertNull( build.getError() );

        assertEquals( ContinuumProjectState.BUILDING, build.getState() );

        // ----------------------------------------------------------------------
        // Check the build result
        // ----------------------------------------------------------------------

        ScmResult scmResult = new ScmResult();

        setBuildResult( store,
                        buildId,
                        ContinuumProjectState.OK,
                        makeContinuumBuildExecutionResult( true, "output", "error", 1 ),
                        scmResult,
                        null );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        build = store.getBuild( buildId );

        assertEquals( 1, build.getExitCode() );

        assertEquals( "output", build.getStandardOutput() );

        assertEquals( "error", build.getStandardError() );
    }

    // ----------------------------------------------------------------------
    // Private utility methods
    // ----------------------------------------------------------------------

    private String addMavenTwoProject( String name, String scmUrl )
        throws Exception
    {
        return addMavenTwoProject( store,
                                   makeStubMavenTwoProject( name, scmUrl ) );
    }
}
