package org.apache.maven.continuum.release.executors;

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

import org.apache.continuum.release.config.ContinuumReleaseDescriptor;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.release.tasks.PerformReleaseProjectTask;
import org.apache.maven.continuum.release.tasks.PrepareReleaseProjectTask;
import org.apache.maven.continuum.release.tasks.RollbackReleaseProjectTask;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;

/**
 * @author Edwin Punzalan
 */
public class ReleaseTaskExecutorTest
    extends PlexusInSpringTestCase
{
    private ScmManager scmManager;

    private TaskExecutor prepareExec;

    private TaskExecutor performExec;

    private TaskExecutor rollbackExec;

    private ContinuumReleaseManager releaseManager;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        if ( scmManager == null )
        {
            scmManager = (ScmManager) lookup( "org.apache.maven.scm.manager.ScmManager" );
        }

        if ( prepareExec == null )
        {
            prepareExec = (TaskExecutor) lookup( TaskExecutor.class.getName(), "prepare-release" );
        }

        if ( performExec == null )
        {
            performExec = (TaskExecutor) lookup( TaskExecutor.class.getName(), "perform-release" );
        }

        if ( rollbackExec == null )
        {
            rollbackExec = (TaskExecutor) lookup( TaskExecutor.class.getName(), "rollback-release" );
        }

        if ( releaseManager == null )
        {
            releaseManager = (ContinuumReleaseManager) lookup( ContinuumReleaseManager.ROLE );
        }
        File scmPath = new File( getBasedir(), "target/scm-src" ).getAbsoluteFile();
        File scmTargetPath = new File( getBasedir(), "target/scm-test" ).getAbsoluteFile();
        FileUtils.copyDirectoryStructure( scmPath, scmTargetPath );
    }

    public void releaseSimpleProject()
        throws Exception
    {
        String scmPath = new File( getBasedir(), "target/scm-test" ).getAbsolutePath().replace( '\\', '/' );
        File workDir = new File( getBasedir(), "target/test-classes/work-dir" );
        FileUtils.deleteDirectory( workDir );
        File testDir = new File( getBasedir(), "target/test-classes/test-dir" );
        FileUtils.deleteDirectory( testDir );

        ContinuumReleaseDescriptor descriptor = new ContinuumReleaseDescriptor();
        descriptor.setInteractive( false );
        descriptor.setScmSourceUrl( "scm:svn:file://localhost/" + scmPath + "/trunk" );
        descriptor.setWorkingDirectory( workDir.getAbsolutePath() );

        ScmRepository repository = getScmRepositorty( descriptor.getScmSourceUrl() );
        ScmFileSet fileSet = new ScmFileSet( workDir );
        scmManager.getProviderByRepository( repository ).checkOut( repository, fileSet, (ScmVersion) null );

        String pom = FileUtils.fileRead( new File( workDir, "pom.xml" ) );
        assertTrue( "Test dev version", pom.indexOf( "<version>1.0-SNAPSHOT</version>" ) > 0 );

        doPrepareWithNoError( descriptor );

        pom = FileUtils.fileRead( new File( workDir, "pom.xml" ) );
        assertTrue( "Test version increment", pom.indexOf( "<version>1.1-SNAPSHOT</version>" ) > 0 );

        repository = getScmRepositorty( "scm:svn:file://localhost/" + scmPath + "/tags/test-artifact-1.0" );
        fileSet = new ScmFileSet( testDir );
        scmManager.getProviderByRepository( repository ).checkOut( repository, fileSet, (ScmVersion) null );

        pom = FileUtils.fileRead( new File( testDir, "pom.xml" ) );
        assertTrue( "Test released version", pom.indexOf( "<version>1.0</version>" ) > 0 );
    }

    public void testReleases()
        throws Exception
    {
        releaseSimpleProject();
        releaseAndRollbackProject();
        releaseSimpleProjectWithNextVersion();
    }

    public void releaseSimpleProjectWithNextVersion()
        throws Exception
    {
        String scmPath = new File( getBasedir(), "target/scm-test" ).getAbsolutePath().replace( '\\', '/' );
        File workDir = new File( getBasedir(), "target/test-classes/work-dir" );
        FileUtils.deleteDirectory( workDir );
        File testDir = new File( getBasedir(), "target/test-classes/test-dir" );
        FileUtils.deleteDirectory( testDir );

        ContinuumReleaseDescriptor descriptor = new ContinuumReleaseDescriptor();
        descriptor.setInteractive( false );
        descriptor.setScmSourceUrl( "scm:svn:file://localhost/" + scmPath + "/trunk" );
        descriptor.setWorkingDirectory( workDir.getAbsolutePath() );
        descriptor.mapReleaseVersion( "test-group:test-artifact", "2.0" );
        descriptor.mapDevelopmentVersion( "test-group:test-artifact", "2.1-SNAPSHOT" );

        ScmRepository repository = getScmRepositorty( descriptor.getScmSourceUrl() );
        ScmFileSet fileSet = new ScmFileSet( workDir );
        scmManager.getProviderByRepository( repository ).checkOut( repository, fileSet, (ScmVersion) null );

        String pom = FileUtils.fileRead( new File( workDir, "pom.xml" ) );
        assertTrue( "Test dev version", pom.indexOf( "<version>1.1-SNAPSHOT</version>" ) > 0 );

        doPrepareWithNoError( descriptor );

        pom = FileUtils.fileRead( new File( workDir, "pom.xml" ) );
        assertTrue( "Test version increment", pom.indexOf( "<version>2.1-SNAPSHOT</version>" ) > 0 );

        repository = getScmRepositorty( "scm:svn:file://localhost/" + scmPath + "/tags/test-artifact-2.0" );
        fileSet = new ScmFileSet( testDir );
        scmManager.getProviderByRepository( repository ).checkOut( repository, fileSet, (ScmVersion) null );

        pom = FileUtils.fileRead( new File( testDir, "pom.xml" ) );
        assertTrue( "Test released version", pom.indexOf( "<version>2.0</version>" ) > 0 );

        performExec.executeTask(
            getPerformTask( "testRelease", descriptor, new File( getBasedir(), "target/test-classes/build-dir" ) ) );

        ReleaseResult result = (ReleaseResult) releaseManager.getReleaseResults().get( "testRelease" );
        if ( result.getResultCode() != ReleaseResult.SUCCESS )
        {
            fail( "Error in release:perform. Release output follows:\n" + result.getOutput() );
        }
    }

    public void releaseAndRollbackProject()
        throws Exception
    {
        String scmPath = new File( getBasedir(), "target/scm-test" ).getAbsolutePath().replace( '\\', '/' );
        File workDir = new File( getBasedir(), "target/test-classes/work-dir" );
        FileUtils.deleteDirectory( workDir );
        File testDir = new File( getBasedir(), "target/test-classes/test-dir" );
        FileUtils.deleteDirectory( testDir );

        ContinuumReleaseDescriptor descriptor = new ContinuumReleaseDescriptor();
        descriptor.setInteractive( false );
        descriptor.setScmSourceUrl( "scm:svn:file://localhost/" + scmPath + "/trunk" );
        descriptor.setWorkingDirectory( workDir.getAbsolutePath() );

        ScmRepository repository = getScmRepositorty( descriptor.getScmSourceUrl() );
        ScmFileSet fileSet = new ScmFileSet( workDir );
        scmManager.getProviderByRepository( repository ).checkOut( repository, fileSet, (ScmVersion) null );

        String pom = FileUtils.fileRead( new File( workDir, "pom.xml" ) );
        assertTrue( "Test dev version", pom.indexOf( "<version>1.1-SNAPSHOT</version>" ) > 0 );

        doPrepareWithNoError( descriptor );

        pom = FileUtils.fileRead( new File( workDir, "pom.xml" ) );
        assertTrue( "Test version increment", pom.indexOf( "<version>1.2-SNAPSHOT</version>" ) > 0 );

        repository = getScmRepositorty( "scm:svn:file://localhost/" + scmPath + "/tags/test-artifact-1.1" );
        fileSet = new ScmFileSet( testDir );
        scmManager.getProviderByRepository( repository ).checkOut( repository, fileSet, (ScmVersion) null );

        pom = FileUtils.fileRead( new File( testDir, "pom.xml" ) );
        assertTrue( "Test released version", pom.indexOf( "<version>1.1</version>" ) > 0 );

        rollbackExec.executeTask( new RollbackReleaseProjectTask( "testRelease", descriptor, null ) );

        pom = FileUtils.fileRead( new File( workDir, "pom.xml" ) );
        assertTrue( "Test rollback version", pom.indexOf( "<version>1.1-SNAPSHOT</version>" ) > 0 );

        assertFalse( "Test that release.properties has been cleaned",
                     new File( workDir, "release.properties" ).exists() );
        assertFalse( "Test that backup file has been cleaned", new File( workDir, "pom.xml.releaseBackup" ).exists() );

        //@todo when implemented already, check if tag was also removed
    }

    private void doPrepareWithNoError( ReleaseDescriptor descriptor )
        throws TaskExecutionException
    {
        prepareExec.executeTask( getPrepareTask( "testRelease", descriptor ) );

        ReleaseResult result = (ReleaseResult) releaseManager.getReleaseResults().get( "testRelease" );
        if ( result.getResultCode() != ReleaseResult.SUCCESS )
        {
            fail( "Error in release:prepare. Release output follows:\n" + result.getOutput() );
        }
    }

    private Task getPrepareTask( String releaseId, ReleaseDescriptor descriptor )
    {
        return new PrepareReleaseProjectTask( releaseId, descriptor, null );
    }

    private Task getPerformTask( String releaseId, ReleaseDescriptor descriptor, File buildDir )
    {
        return new PerformReleaseProjectTask( releaseId, descriptor, buildDir, "package", true, null );
    }

    private ScmRepository getScmRepositorty( String scmUrl )
        throws ScmRepositoryException, NoSuchScmProviderException
    {
        ScmRepository repository = scmManager.makeScmRepository( scmUrl.trim() );

        repository.getProviderRepository().setPersistCheckout( true );

        return repository;
    }
}
