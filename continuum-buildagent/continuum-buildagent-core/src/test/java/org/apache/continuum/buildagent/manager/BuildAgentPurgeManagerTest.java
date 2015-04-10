package org.apache.continuum.buildagent.manager;

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

import org.apache.commons.io.FileUtils;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.purge.executor.agent.DirectoryPurgeExecutorFactoryImpl;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * For CONTINUUM-2658 tests, Support purging of working and release directories of build agents on a schedule
 */
public class BuildAgentPurgeManagerTest
    extends PlexusInSpringTestCase
{
    private static final int AGE = 2;

    private static final int NONE = 0;

    private static final int RELEASE_DIRS = 5;

    private static final int OLD_RELEASE_DIRS = 3;

    private static final int WORKING_DIRS = 10;

    private static final int OLD_WORKING_DIRS = 9;

    private static final int REGULAR_FILES = 2;

    public static final int TOTAL_FILES_AND_DIRS = RELEASE_DIRS + WORKING_DIRS + REGULAR_FILES;

    private static final int MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

    private BuildAgentConfigurationService buildAgentConfigurationService;

    private DefaultBuildAgentPurgeManager purgeManager;

    private File tempDir;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        purgeManager = (DefaultBuildAgentPurgeManager) lookup( BuildAgentPurgeManager.class );

        buildAgentConfigurationService = mock( BuildAgentConfigurationService.class );

        purgeManager.setBuildAgentConfigurationService( buildAgentConfigurationService );

        createTestFiles();

        assertEquals( TOTAL_FILES_AND_DIRS, tempDir.list().length );

        when( buildAgentConfigurationService.getWorkingDirectory() ).thenReturn( tempDir );
    }

    protected void tearDown()
        throws Exception
    {
        purgeManager = null;
        cleanUpTestFiles();
        super.tearDown();
    }

    // CONTINUUM-2658
    public void testAllWorking()
        throws Exception
    {
        int ignored = 1;
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.WORKING_TYPE, ignored, ignored, true );
        assertEquals( RELEASE_DIRS + REGULAR_FILES, fileCount() );
    }

    // CONTINUUM-2658
    public void testAllReleases()
        throws Exception
    {
        int ignored = 1;
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.RELEASE_TYPE, ignored, ignored, true );
        assertEquals( WORKING_DIRS + REGULAR_FILES, fileCount() );
    }

    // CONTINUUM-2658
    public void testAllWorkingAndReleases()
        throws Exception
    {
        int ignored = 1;
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.WORKING_TYPE, ignored, ignored, true );
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.RELEASE_TYPE, ignored, ignored, true );
        assertEquals( REGULAR_FILES, fileCount() );
    }

    public void testRetentionOnlyWorking()
        throws Exception
    {
        int retainedWorking = 2;
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.WORKING_TYPE, NONE, retainedWorking, false );
        assertEquals( RELEASE_DIRS + REGULAR_FILES + retainedWorking, fileCount() );
    }

    public void testRetentionOnlyReleases()
        throws Exception
    {
        int retainedReleases = 4;
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.RELEASE_TYPE, NONE, retainedReleases, false );
        assertEquals( WORKING_DIRS + retainedReleases + REGULAR_FILES, fileCount() );
    }

    public void testRetentionOnlyWorkingAndReleases()
        throws Exception
    {
        int retainedReleases = 4, retainedWorking = 2;
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.WORKING_TYPE, NONE, retainedWorking, false );
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.RELEASE_TYPE, NONE, retainedReleases, false );
        assertEquals( retainedWorking + retainedReleases + REGULAR_FILES, fileCount() );
    }

    public void testDaysOldOnlyWorking()
        throws Exception
    {
        int maxAge = 1, ineligibleWorkDirs = WORKING_DIRS - OLD_WORKING_DIRS;
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.WORKING_TYPE, maxAge, NONE, false );
        assertEquals( RELEASE_DIRS + ineligibleWorkDirs + REGULAR_FILES, fileCount() );
    }

    public void testDaysOldOnlyReleases()
        throws Exception
    {
        int maxAge = 1, ineligibleReleaseDirs = RELEASE_DIRS - OLD_RELEASE_DIRS;
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.RELEASE_TYPE, maxAge, NONE, false );
        assertEquals( WORKING_DIRS + ineligibleReleaseDirs + REGULAR_FILES, fileCount() );
    }

    public void testDaysOldOnlyWorkingAndReleases()
        throws Exception
    {
        int maxAge = 1;
        int ineligibleWorkDirs = WORKING_DIRS - OLD_WORKING_DIRS;
        int ineligibleReleaseDirs = RELEASE_DIRS - OLD_RELEASE_DIRS;
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.WORKING_TYPE, maxAge, NONE, false );
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.RELEASE_TYPE, maxAge, NONE, false );
        assertEquals( ineligibleWorkDirs + ineligibleReleaseDirs + REGULAR_FILES, fileCount() );
    }

    public void testRetentionAndDaysOldWorking()
        throws Exception
    {
        int maxAge = 1;
        int retainWorking = 5;
        int ineligibleWorkDirs = WORKING_DIRS - OLD_WORKING_DIRS;
        int expectedWorkDirs = retainWorking + ineligibleWorkDirs;
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.WORKING_TYPE, maxAge, retainWorking, false );
        assertEquals( RELEASE_DIRS + expectedWorkDirs + REGULAR_FILES, fileCount() );
    }

    public void testRetentionAndDaysOldReleases()
        throws Exception
    {
        int maxAge = 1;
        int retainRelease = 1;
        int ineligibleReleaseDirs = RELEASE_DIRS - OLD_RELEASE_DIRS;
        int expectedReleaseDirs = retainRelease + ineligibleReleaseDirs;
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.RELEASE_TYPE, maxAge, retainRelease, false );
        assertEquals( WORKING_DIRS + expectedReleaseDirs + REGULAR_FILES, fileCount() );
    }

    public void testRetentionAndDaysOldWorkingAndReleases()
        throws Exception
    {
        int maxAge = 1;
        int retainWorking = 5, retainRelease = 1;
        int ineligibleWorkDirs = WORKING_DIRS - OLD_WORKING_DIRS;
        int ineligibleReleaseDirs = RELEASE_DIRS - OLD_RELEASE_DIRS;
        int expectedWorkDirs = retainWorking + ineligibleWorkDirs;
        int expectedReleaseDirs = retainRelease + ineligibleReleaseDirs;
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.WORKING_TYPE, maxAge, retainWorking, false );
        purgeManager.executeDirectoryPurge( DirectoryPurgeExecutorFactoryImpl.RELEASE_TYPE, maxAge, retainRelease, false );
        assertEquals( expectedReleaseDirs + expectedWorkDirs + REGULAR_FILES, fileCount() );
    }

    private int fileCount()
    {
        return tempDir.list().length;
    }

    private void createTestFiles()
        throws Exception
    {
        SimpleDateFormat format = new SimpleDateFormat( "yyyyMMddHHmmss" );
        tempDir = new File( System.getProperty( "java.io.tmpdir" ) + System.getProperty( "file.separator" ) +
                                format.format( new Date() ) );
        if ( !tempDir.mkdirs() )
        {
            throw new IOException( "Unable to create test directory: " + tempDir.getName() );
        }
        createReleasesDirectories( RELEASE_DIRS, AGE, OLD_RELEASE_DIRS );
        createWorkingDirectories( WORKING_DIRS, AGE, OLD_WORKING_DIRS );
        createRegularFile( "random.txt" );
        createRegularFile( "releases-random.txt" );
    }

    private void cleanUpTestFiles()
        throws IOException
    {
        FileUtils.deleteDirectory( tempDir );
    }

    private void createReleasesDirectories( int count, int daysOld, int daysOldCount )
        throws Exception
    {
        createDirectories( "releases-", count, daysOld, daysOldCount );
    }

    private void createWorkingDirectories( int count, int daysOld, int daysOldCount )
        throws IOException
    {
        createDirectories( "", count, daysOld, daysOldCount );
    }

    private void createDirectories( String namePrefix, int count, int age, int toAge )
        throws IOException
    {
        long generationStart = System.currentTimeMillis();
        for ( int x = 1; x <= count; x++ )
        {
            File file = new File( tempDir.getAbsolutePath() + System.getProperty( "file.separator" ) + namePrefix + x );
            if ( !file.mkdirs() )
            {
                throw new IOException( "Unable to create test directory: " + file.getName() );
            }
            if ( x <= toAge )
            {
                file.setLastModified( generationStart - ( age * MILLIS_IN_DAY ) );
            }
        }
    }

    private File createRegularFile( String fileName )
        throws IOException
    {
        File randomFile = new File( tempDir.getAbsolutePath() + System.getProperty( "file.separator" ) + fileName );
        if ( !randomFile.exists() )
        {
            randomFile.createNewFile();
        }
        return randomFile;
    }
}
