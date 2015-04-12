package org.apache.continuum.purge.executor;

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

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.maven.archiva.consumers.core.repository.ArtifactFilenameFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import static org.junit.Assert.assertEquals;

/**
 * @author Maria Catherine Tan
 */
public class RetentionCountDirectoryPurgeExecutorTest
    extends AbstractPurgeExecutorTest
{
    @Before
    public void setUp()
        throws Exception
    {
        purgeReleasesDirTask = getRetentionCountReleasesDirPurgeTask();
        purgeBuildOutputDirTask = getRetentionCountBuildOutputDirPurgeTask();
    }

    @Test
    public void testReleasesDirPurging()
        throws Exception
    {
        populateReleasesDirectory();

        String dirPath = getReleasesDirectoryLocation().getAbsolutePath();
        FilenameFilter filter = new ArtifactFilenameFilter( "releases-" );

        File[] workingDir = new File( dirPath ).listFiles();
        File[] releasesDir = new File( dirPath ).listFiles( filter );

        assertEquals( "# of folders inside working directory", 4, workingDir.length );
        assertEquals( "# of releases folders inside working directory", 3, releasesDir.length );
        assertExists( dirPath + "/1" );

        purgeExecutor.executeTask( purgeReleasesDirTask );

        workingDir = new File( dirPath ).listFiles();
        releasesDir = new File( dirPath ).listFiles( filter );

        assertEquals( "# of folders inside working directory", 3, workingDir.length );
        assertEquals( "# of releases folders inside working directory", 2, releasesDir.length );
        assertExists( dirPath + "/1" );
    }

    @Test
    public void testBuildOutputDirPurging()
        throws Exception
    {
        populateBuildOutputDirectory();

        String dirPath = getBuildOutputDirectoryLocation().getAbsolutePath();

        File projectPath1 = new File( dirPath, "1" );
        File projectPath2 = new File( dirPath, "2" );

        FileFilter filter = DirectoryFileFilter.DIRECTORY;
        File[] files1 = projectPath1.listFiles( filter );
        File[] files2 = projectPath2.listFiles( filter );

        assertEquals( "check # of build output dir", 3, files1.length );
        assertEquals( "check # of build output dir", 3, files2.length );

        purgeExecutor.executeTask( purgeBuildOutputDirTask );

        files1 = projectPath1.listFiles( filter );
        files2 = projectPath2.listFiles( filter );

        assertEquals( "check # of build output dir", 2, files1.length );
        assertEquals( "check # of build output dir", 2, files2.length );

        for ( File file : files1 )
        {
            assertExists( file.getAbsolutePath() + ".log.txt" );
        }

        for ( File file : files2 )
        {
            assertExists( file.getAbsolutePath() + ".log.txt" );
        }
    }
}
