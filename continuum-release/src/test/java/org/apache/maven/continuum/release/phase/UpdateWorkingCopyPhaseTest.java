package org.apache.maven.continuum.release.phase;

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
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.phase.ReleasePhase;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;

public class UpdateWorkingCopyPhaseTest
    extends PlexusInSpringTestCase
{
    private UpdateWorkingCopyPhase phase;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        phase = (UpdateWorkingCopyPhase) lookup( ReleasePhase.ROLE, "update-working-copy" );

        // set up project scm
        File scmPathFile = new File( getBasedir(), "target/scm-src" ).getAbsoluteFile();
        File scmTargetPathFile = new File( getBasedir(), "/target/scm-test" ).getAbsoluteFile();
        FileUtils.copyDirectoryStructure( scmPathFile, scmTargetPathFile );
    }

    public void testWorkingDirDoesNotExist()
        throws Exception
    {
        assertNotNull( phase );

        ContinuumReleaseDescriptor releaseDescriptor = createReleaseDescriptor();

        File workingDirectory = new File( releaseDescriptor.getWorkingDirectory() );

        FileUtils.deleteDirectory( workingDirectory );
        assertFalse( workingDirectory.exists() );

        phase.execute( releaseDescriptor, new Settings(), null );

        assertTrue( workingDirectory.exists() );
    }

    public void testWorkingDirAlreadyExistsWithProjectCheckout()
        throws Exception
    {
        assertNotNull( phase );

        ContinuumReleaseDescriptor releaseDescriptor = createReleaseDescriptor();

        File workingDirectory = new File( releaseDescriptor.getWorkingDirectory() );

        // assert working directory already exists with project checkout
        assertTrue( workingDirectory.exists() );
        assertTrue( workingDirectory.listFiles().length > 0 );

        phase.execute( releaseDescriptor, new Settings(), null );

        assertTrue( workingDirectory.exists() );
    }

    public void testWorkingDirAlreadyExistsNoProjectCheckout()
        throws Exception
    {
        assertNotNull( phase );

        ContinuumReleaseDescriptor releaseDescriptor = createReleaseDescriptor();

        File workingDirectory = new File( releaseDescriptor.getWorkingDirectory() );
        FileUtils.deleteDirectory( workingDirectory );
        workingDirectory.mkdirs();

        // assert empty working directory
        assertTrue( workingDirectory.exists() );
        assertTrue( workingDirectory.listFiles().length == 0 );

        phase.execute( releaseDescriptor, new Settings(), null );

        assertTrue( workingDirectory.exists() );
    }

    private ContinuumReleaseDescriptor createReleaseDescriptor()
    {
        // project source and working directory paths
        String projectUrl = getBasedir() + "/target/scm-test/trunk";
        String workingDirPath = getBasedir() + "/target/test-classes/updateWorkingCopy_working-directory";

        // create release descriptor
        ContinuumReleaseDescriptor releaseDescriptor = new ContinuumReleaseDescriptor();
        releaseDescriptor.setScmSourceUrl( "scm:svn:file://localhost/" + projectUrl );
        releaseDescriptor.setWorkingDirectory( workingDirPath );

        return releaseDescriptor;
    }
}
