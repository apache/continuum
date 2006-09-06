package org.apache.maven.continuum.release.executors;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.maven.continuum.release.tasks.PrepareReleaseProjectTask;
import org.apache.maven.plugins.release.config.ReleaseDescriptor;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.ScmFileSet;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;

import java.io.File;

/**
 * @author Edwin Punzalan
 */
public class PrepareReleaseTaskExecutorTest
    extends PlexusTestCase
{
    private ScmManager scmManager;

    private TaskExecutor taskExec;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        scmManager = (ScmManager) lookup( "org.apache.maven.scm.manager.ScmManager" );

        taskExec = (TaskExecutor) lookup( ReleaseTaskExecutor.ROLE, "prepare-release" );
    }

    public void testRelease()
        throws Exception
    {
        File testProjectDir = new File( getBasedir(), "target/test-classes/scm-src/trunk" );
        File workDir = new File( getBasedir(), "target/test-classes/work-dir" );
        workDir.mkdirs();

        ReleaseDescriptor descriptor = new ReleaseDescriptor();
        descriptor.setInteractive( false );
        descriptor.setScmSourceUrl( "scm:svn:file://localhost/" +
            testProjectDir.getAbsolutePath().replace( '\\', '/' ) );
        descriptor.setWorkingDirectory( workDir.getAbsolutePath() );

        ScmRepository repository = getScmRepositorty( descriptor.getScmSourceUrl() );
        ScmFileSet fileSet = new ScmFileSet( workDir );
        scmManager.getProviderByRepository( repository ).checkOut( repository, fileSet, null );

        taskExec.executeTask( getPrepareTask( "testRelease", descriptor ) );
    }

    protected Task getPrepareTask( String releaseId, ReleaseDescriptor descriptor )
    {
        Task task = new PrepareReleaseProjectTask( releaseId, descriptor );



        return task;
    }

    private ScmRepository getScmRepositorty( String scmUrl )
        throws ScmRepositoryException, NoSuchScmProviderException
    {
        ScmRepository repository = scmManager.makeScmRepository( scmUrl.trim() );

        repository.getProviderRepository().setPersistCheckout( true );

        return repository;
    }
}
