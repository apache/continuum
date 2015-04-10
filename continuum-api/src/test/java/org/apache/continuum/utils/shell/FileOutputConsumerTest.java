package org.apache.continuum.utils.shell;

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

import org.apache.continuum.utils.file.DefaultFileSystemManager;
import org.apache.continuum.utils.file.FileSystemManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @see org.apache.continuum.utils.shell.FileOutputConsumer
 */
public class FileOutputConsumerTest
{

    File outputFile;

    FileOutputConsumer consumer;

    FileSystemManager fsManager;

    @Before
    public void setUp()
        throws IOException
    {
        outputFile = File.createTempFile( FileOutputConsumerTest.class.getName(), "output" );
        outputFile.delete();  // we want to test whether it is created
        assertFalse( "file should not exist", outputFile.exists() );
        consumer = new FileOutputConsumer( outputFile );
        fsManager = new DefaultFileSystemManager();
    }

    @After
    public void tearDown()
    {
        outputFile.delete();
        consumer = null;
    }

    @Test
    public void noOutputCreatesEmptyFile()
        throws IOException
    {
        consumer.close();
        assertFileEmpty();
    }

    @Test
    public void fileExistsAfterClose()
        throws IOException
    {
        String[] lines = { "first", "second", "third" };
        writeContent( lines );
        consumer.close();
        assertFileExists();
        assertFileContents( lines );
    }

    @Test
    public void newConsumerTruncatesFile()
        throws IOException
    {
        // Write content to the file
        String[] content1 = { "should be overwritten" };
        writeContent( content1 );
        consumer.close();
        assertFileExists();
        assertFileContents( content1 );

        // New consumer should truncate (overwrite) the previous contents
        consumer = new FileOutputConsumer( outputFile );
        assertFileExists();
        assertFileEmpty();
    }

    @Test
    public void contentsAvailableBeforeClose()
        throws IOException
    {
        String[] content = { "contents", "are not", "critical" };
        writeContent( content );
        assertFileContents( content );
    }

    private void writeContent( String... lines )
    {
        for ( String line : lines )
            consumer.consume( line );
    }

    private void assertFileExists()
    {
        assertTrue( "file should exist", outputFile.exists() );
    }

    private void assertFileEmpty()
        throws IOException
    {
        assertEquals( "file should have been empty", "", fsManager.fileContents( outputFile ) );
    }

    private void assertFileContents( String... contents )
        throws IOException
    {
        StringBuilder finalContents = new StringBuilder();
        if ( contents.length >= 1 )
        {
            for ( String line : contents )
                finalContents.append( String.format( "%s%n", line ) );
        }
        assertEquals( "file did not contain expected contents", finalContents.toString(),
                      fsManager.fileContents( outputFile ) );
    }
}
