package org.apache.continuum.purge.executor;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.maven.archiva.consumers.core.repository.ArtifactFilenameFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

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

/**
 * @author Maria Catherine Tan
 */
public class CleanAllPurgeExecutorTest
    extends AbstractPurgeExecutorTest
{
    protected void setUp()
        throws Exception
    {
        super.setUp();

        purgeDefaultRepoTask = getCleanAllDefaultRepoPurgeTask();

        purgeReleasesDirTask = getCleanAllReleasesDirPurgeTask();

        purgeBuildOutputDirTask = getCleanAllBuildOutputDirPurgeTask();
    }

    public void testCleanAllRepositoryPurging()
        throws Exception
    {
        populateDefaultRepository();

        purgeExecutor.executeTask( purgeDefaultRepoTask );

        assertIsEmpty( getDefaultRepositoryLocation() );
    }

    public void testCleanAllReleasesPurging()
        throws Exception
    {
        populateReleasesDirectory();

        File workingDir = getReleasesDirectoryLocation();

        FilenameFilter filter = new ArtifactFilenameFilter( "releases-" );

        File[] releasesDir = workingDir.listFiles( filter );

        assertExists( workingDir.getAbsolutePath() + "/1" );

        assertEquals( "check # of releases directory", 3, releasesDir.length );

        purgeExecutor.executeTask( purgeReleasesDirTask );

        // check if no releases dir

        releasesDir = workingDir.listFiles( filter );

        assertEquals( "releases directory must be empty", 0, releasesDir.length );

        assertExists( workingDir.getAbsolutePath() + "/1" );
    }

    public void testCleanAllBuildOutputPurging()
        throws Exception
    {
        populateBuildOutputDirectory();

        File buildOutputDir = getBuildOutputDirectoryLocation();

        purgeExecutor.executeTask( purgeBuildOutputDirTask );

        FileFilter filter = DirectoryFileFilter.DIRECTORY;

        File[] projectsDir = buildOutputDir.listFiles( filter );

        for ( File projectDir : projectsDir )
        {
            assertIsEmpty( projectDir );
        }
    }
}
