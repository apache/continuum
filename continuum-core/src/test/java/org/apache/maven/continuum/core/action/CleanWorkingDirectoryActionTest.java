package org.apache.maven.continuum.core.action;

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

import org.apache.commons.io.IOUtils;
import org.apache.continuum.dao.ProjectDao;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.codehaus.plexus.util.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests that the cleanup action works properly. This is a non-portable test, and needs a special setup to run.
 * Specifically:
 * * OOME test requires a small heap (-Xmx2m -Xms2m)
 * * Link traversal tests require a system with "/bin/ln"
 *
 * @see org.apache.maven.continuum.core.action.CleanWorkingDirectoryAction
 */
public class CleanWorkingDirectoryActionTest
{

    public static final String FILE_PREFIX = "cwdat";

    private CleanWorkingDirectoryAction action;

    private WorkingDirectoryService mockWorkingDirectoryService;

    private ProjectDao mockProjectDao;

    private Mockery context;

    /**
     * Builds and returns a directory levels deep with the specified number of directories and files at each level.
     *
     * @param root   optional root of the tree, a temporary directory will be created otherwise.
     * @param levels the depth of directories to build
     * @param files  the number of files to create in each directory
     * @param dirs   the number of directories to include in each directory
     * @return the location of the root of the tree, should be root if it was specified
     * @throws IOException
     */
    private File createFileTree( File root, int levels, int files, int dirs )
        throws IOException
    {
        // Create a root path if one isn't specified
        root = root == null ? FileUtils.createTempFile( FILE_PREFIX, "", null ) : root;

        // Create the directory at that path
        if ( !root.mkdir() )
        {
            throw new IOException( "Failed to create directory " + root );
        }

        // Create the files for this directory
        for ( int i = 0; i < files; i++ )
        {
            File newFile = FileUtils.createTempFile( FILE_PREFIX, "", root );
            FileUtils.fileWrite( newFile.getAbsolutePath(), "" );
        }

        // Create the directories
        if ( levels > 1 )
        {
            for ( int i = 0; i < dirs; i++ )
            {

                File newDir = FileUtils.createTempFile( FILE_PREFIX, "", root );
                createFileTree( newDir, levels - 1, files, dirs );
            }
        }
        return root;
    }

    @Before
    public void setUp()
        throws NoSuchFieldException, IllegalAccessException
    {

        context = new JUnit3Mockery();
        context.setImposteriser( ClassImposteriser.INSTANCE );

        // Create mocks
        mockWorkingDirectoryService = context.mock( WorkingDirectoryService.class );
        mockProjectDao = context.mock( ProjectDao.class );

        action = new CleanWorkingDirectoryAction();

        // Get the private fields and make them accessible
        Field wdsField = action.getClass().getDeclaredField( "workingDirectoryService" );
        Field pdField = action.getClass().getDeclaredField( "projectDao" );
        for ( Field f : new Field[] { wdsField, pdField } )
        {
            f.setAccessible( true );
        }

        // Inject the mocks as dependencies
        wdsField.set( action, mockWorkingDirectoryService );
        pdField.set( action, mockProjectDao );
    }

    /**
     * Tests that deleting large directories doesn't result in an OutOfMemoryError.
     * Reported as CONTINUUM-2199.
     *
     * @throws Exception
     */
    @Test
    public void testOutOfMemory()
        throws Exception
    {
        final File deepTree = createFileTree( null, 10, 10, 2 );
        context.checking( new Expectations()
        {
            {
                Project p = new Project();
                one( mockProjectDao ).getProject( 0 );
                will( returnValue( p ) );

                one( mockWorkingDirectoryService ).getWorkingDirectory( p, null, new ArrayList<Project>() );
                will( returnValue( deepTree ) );
            }
        } );
        action.execute( new HashMap() );
        assertFalse( String.format( "%s should not exist after deletion", deepTree.getPath() ), deepTree.exists() );
    }

    private int numFiles( File f )
    {
        return f.listFiles().length;
    }

    /**
     * Tests that cleanup doesn't traverse across symlinks.
     */
    @Test
    public void testSymlinkTraversal()
        throws Exception
    {
        int size = 10;

        final File tree1 = createFileTree( null, 1, 10, 0 );
        assertEquals( String.format( "%s should contain %s files", tree1, size ), size, numFiles( tree1 ) );

        final File tree2 = createFileTree( null, 1, 10, 0 );
        assertEquals( String.format( "%s should contain %s files", tree2, size ), size, numFiles( tree2 ) );

        final File symlink = new File( tree1, "tree2soft" );

        // Create a symbolic link to second tree in first tree
        String[] symlinkCommand = { "/bin/ln", "-s", tree2.getPath(), symlink.getPath() };
        Process p1 = Runtime.getRuntime().exec( symlinkCommand );

        if ( p1.waitFor() != 0 )
        {
            System.err.println( "Failed to run command " + Arrays.toString( symlinkCommand ) );
            IOUtils.copy( p1.getInputStream(), System.err );
        }
        assertTrue( String.format( "Symbolic link %s should have been created", symlink ), symlink.exists() );
        assertEquals( size + 1, numFiles( tree1 ) );

        context.checking( new Expectations()
        {
            {
                Project p = new Project();
                one( mockProjectDao ).getProject( 0 );
                will( returnValue( p ) );

                one( mockWorkingDirectoryService ).getWorkingDirectory( p, null, new ArrayList<Project>() );
                will( returnValue( tree1 ) );
            }
        } );
        action.execute( new HashMap() );
        assertFalse( String.format( "%s should not exist after deletion", tree1.getPath() ), tree1.exists() );
        assertTrue( String.format( "%s should exist after deletion", tree2 ), tree2.exists() );
        assertEquals( String.format( "%s should have %s files", tree2, size ), size, numFiles( tree2 ) );
    }

    /**
     * Tests that cleanup doesn't traverse across hard links.
     */
    @Test
    public void testHardlinkTraversal()
        throws Exception
    {
        int size = 10;

        final File tree1 = createFileTree( null, 1, 10, 0 );
        assertEquals( String.format( "%s should contain %s files", tree1, size ), size, numFiles( tree1 ) );

        final File tree2 = createFileTree( null, 1, 10, 0 );
        assertEquals( String.format( "%s should contain %s files", tree2, size ), size, numFiles( tree2 ) );

        final File hardlink = new File( tree1, "tree2hard" );

        File hardLinkedFile = new File( tree2, tree2.list()[0] ); // Hardlinks can't be to directories
        String[] hardlinkCommand = { "/bin/ln", hardLinkedFile.getPath(), hardlink.getPath() };
        Process p2 = Runtime.getRuntime().exec( hardlinkCommand );

        if ( p2.waitFor() != 0 )
        {
            System.err.println( "Failed to run command " + Arrays.toString( hardlinkCommand ) );
            IOUtils.copy( p2.getInputStream(), System.err );
        }
        assertTrue( String.format( "Hard link %s should have been created", hardlink ), hardlink.exists() );
        assertEquals( size + 1, numFiles( tree1 ) );

        context.checking( new Expectations()
        {
            {
                Project p = new Project();
                one( mockProjectDao ).getProject( 0 );
                will( returnValue( p ) );

                one( mockWorkingDirectoryService ).getWorkingDirectory( p, null, new ArrayList<Project>() );
                will( returnValue( tree1 ) );
            }
        } );
        action.execute( new HashMap() );
        assertFalse( String.format( "%s should not exist after deletion", tree1.getPath() ), tree1.exists() );
        assertTrue( String.format( "%s should exist after deletion", tree2 ), tree2.exists() );
        assertEquals( String.format( "%s should have %s files", tree2, size ), size, numFiles( tree2 ) );
    }
}
