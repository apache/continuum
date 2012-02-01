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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.Arrays;

/**
 * For CONTINUUM-2658 tests, Support purging of working and release directories of build agents on a schedule
 */
public class BuildAgentPurgeManagerTest
    extends PlexusInSpringTestCase
{
    private static final int DAYS_OLD = 2;
    
    private static final int RELEASES_COUNT = 5;
    
    private static final int RELEASES_DAYS_OLD_COUNT = 3;
    
    private static final int WORKING_COUNT = 10;
    
    private static final int WORKING_DAYS_OLD_COUNT = 9;
    
    private static final String DIRECTORY_TYPE_RELEASES = "releases";
    
    private static final String DIRECTORY_TYPE_WORKING = "working";
    
    private Mockery context;

    private BuildAgentConfigurationService buildAgentConfigurationService;

    private DefaultBuildAgentPurgeManager purgeManager;
    
    private File tempDir;
    
    protected void setUp()
        throws Exception
    {
        super.setUp();

        context = new JUnit3Mockery();
        context.setImposteriser( ClassImposteriser.INSTANCE );

        purgeManager = (DefaultBuildAgentPurgeManager) lookup( BuildAgentPurgeManager.class );

        buildAgentConfigurationService = context.mock( BuildAgentConfigurationService.class );

        purgeManager.setBuildAgentConfigurationService( buildAgentConfigurationService );
        
        createTestDirectoriesAndFiles();
    }

    protected void tearDown()
        throws Exception
    {
        purgeManager = null;
        cleanUpTestDirectoriesAndFiles();
        super.tearDown();
    }

    // CONTINUUM-2658
    public void testCleanAllPurge()
        throws Exception
    {
        context.checking( new Expectations()
        {
            {
                one( buildAgentConfigurationService ).getWorkingDirectory( );
                will( returnValue( tempDir ) );
                
                one( buildAgentConfigurationService ).getWorkingDirectory( );
                will( returnValue( tempDir ) );
            }
        } );

        //confirm current content of directory
        //2 random files
        assertEquals( RELEASES_COUNT + WORKING_COUNT + 2, tempDir.list().length );
        
        purgeManager.executeDirectoryPurge( DIRECTORY_TYPE_WORKING, 1, 1, true );
        
        //confirm current content of directory
        //working directories deleted
        assertEquals( RELEASES_COUNT + 2, tempDir.list().length );
        
        purgeManager.executeDirectoryPurge( DIRECTORY_TYPE_RELEASES, 1, 1, true );
        
        //confirm current content of directory
        //releases directories deleted
        assertEquals( 2, tempDir.list().length );
    }    
    
    public void testRetentionOnlyPurge() throws Exception
    {
        context.checking( new Expectations()
        {
            {
                one( buildAgentConfigurationService ).getWorkingDirectory( );
                will( returnValue( tempDir ) );
                
                one( buildAgentConfigurationService ).getWorkingDirectory( );
                will( returnValue( tempDir ) );
            }
        } );

        //confirm current content of directory
        //2 random files
        assertEquals( RELEASES_COUNT + WORKING_COUNT + 2, tempDir.list().length );
        
        purgeManager.executeDirectoryPurge( DIRECTORY_TYPE_WORKING, 0, 2, false );
        
        List<String> fileNames = Arrays.asList( tempDir.list() );
        
        File[] files =  tempDir.listFiles() ;
        
        //confirm current content of directory
        //2 working directories left
        assertEquals( RELEASES_COUNT + 2 + 2, fileNames.size() );
        
        purgeManager.executeDirectoryPurge( DIRECTORY_TYPE_RELEASES, 0, 4, false );
        
        fileNames = Arrays.asList( tempDir.list() );
        
        //confirm current content of directory
        //4 releases directories left
        assertEquals( 4 + 2 + 2, fileNames.size() );
    }

    public void testDaysOldOnlyPurge() throws Exception
    {
        context.checking( new Expectations()
        {
            {
                one( buildAgentConfigurationService ).getWorkingDirectory( );
                will( returnValue( tempDir ) );
                
                one( buildAgentConfigurationService ).getWorkingDirectory( );
                will( returnValue( tempDir ) );
            }
        } );

        //confirm current content of directory
        //2 random files
        assertEquals( RELEASES_COUNT + WORKING_COUNT + 2, tempDir.list().length );
        
        purgeManager.executeDirectoryPurge( DIRECTORY_TYPE_WORKING, 1, 0, false );
        
        List<String> fileNames = Arrays.asList( tempDir.list() );
        
        //confirm current content of directory
        //days old directories are deleted
        assertEquals( RELEASES_COUNT + ( WORKING_COUNT - WORKING_DAYS_OLD_COUNT ) + 2, fileNames.size() );
        
        purgeManager.executeDirectoryPurge( DIRECTORY_TYPE_RELEASES, 1, 0, false );
        
        fileNames = Arrays.asList( tempDir.list() );
        
        //confirm current content of directory
        //days old directories are deleted
        assertEquals( ( RELEASES_COUNT - RELEASES_DAYS_OLD_COUNT ) + ( WORKING_COUNT - WORKING_DAYS_OLD_COUNT ) + 2, fileNames.size() );
    }

    public void testRetentionAndDaysOldOnlyPurge() throws Exception
    {
        context.checking( new Expectations()
        {
            {
                one( buildAgentConfigurationService ).getWorkingDirectory( );
                will( returnValue( tempDir ) );
                
                one( buildAgentConfigurationService ).getWorkingDirectory( );
                will( returnValue( tempDir ) );
            }
        } );

        //confirm current content of directory
        //2 random files
        assertEquals( RELEASES_COUNT + WORKING_COUNT + 2, tempDir.list().length );
        
        purgeManager.executeDirectoryPurge( DIRECTORY_TYPE_WORKING, 1, 5, false );
        
        List<String> fileNames = Arrays.asList( tempDir.list() );
        
        //confirm current content of directory
        //days old directories are deleted
        assertEquals( RELEASES_COUNT + Math.max( 5, WORKING_COUNT - WORKING_DAYS_OLD_COUNT ) + 2, fileNames.size() );
        
        purgeManager.executeDirectoryPurge( DIRECTORY_TYPE_RELEASES, 1, 1, false );
        
        fileNames = Arrays.asList( tempDir.list() );
        
        //confirm current content of directory
        //days old directories are deleted
        assertEquals( Math.max( 1, RELEASES_COUNT - RELEASES_DAYS_OLD_COUNT ) + Math.max( 5, WORKING_COUNT - WORKING_DAYS_OLD_COUNT ) + 2, fileNames.size() );
    }
    
    private void createTestDirectoriesAndFiles() throws Exception
    {
        SimpleDateFormat format = new SimpleDateFormat( "yyyyMMddHHmmss" );
        tempDir = new File( System.getProperty( "java.io.tmpdir" ) + System.getProperty( "file.separator" ) + format.format( new Date() ) );
        if ( !tempDir.mkdirs() )
        {
            throw new IOException( "Unable to create test directory: " + tempDir.getName() );
        }
        
        createReleasesDirectories( tempDir, RELEASES_COUNT, DAYS_OLD, RELEASES_DAYS_OLD_COUNT );
        createWorkingDirectories( tempDir, WORKING_COUNT, DAYS_OLD, WORKING_DAYS_OLD_COUNT );
        createRandomFile( tempDir, "random.txt" );
        createRandomFile( tempDir, "releases-random.txt" );
    }
    
    private void createReleasesDirectories( File parentDir, int count, int daysOld, int daysOldCount ) throws Exception
    {   
        int daysOldIndex = 0;
        for ( int x = 1; x <= count; x++ )
        {
            File file = new File( tempDir.getAbsolutePath() + System.getProperty( "file.separator" ) + "releases-" + x  );
            if ( !file.mkdirs() )
            {
                throw new IOException( "Unable to create test directory: " + file.getName() );
            }
            if ( daysOldIndex < daysOldCount )
            {
                long daysOldTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000 * daysOld;
                file.setLastModified( daysOldTime );
                daysOldIndex++;
            }
            
        }
    }
    
    private void createWorkingDirectories( File parentDir, int count, int daysOld, int daysOldCount ) throws Exception
    {
        int daysOldIndex = 0;
        for ( int x = 1; x <= count; x++ )
        {
            File file = new File( tempDir.getAbsolutePath() + System.getProperty( "file.separator" ) + x );
            if ( !file.mkdirs() )
            {
                throw new IOException( "Unable to create test directory: " + file.getName() );
            }
            if ( daysOldIndex < daysOldCount )
            {
                long daysOldTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000 * daysOld;
                file.setLastModified( daysOldTime );
                daysOldIndex++;
            }
            
        }
    }
    
    private File createRandomFile( File parentDir, String fileName ) throws IOException
    {
        File randomFile = new File( parentDir.getAbsolutePath() + System.getProperty( "file.separator" ) + fileName );
        if ( !randomFile.exists() )
        {
            randomFile.createNewFile();
        }
        return randomFile;
    }
    
    private void cleanUpTestDirectoriesAndFiles() throws IOException
    {
        FileUtils.deleteDirectory( tempDir );
    }
}
