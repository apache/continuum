package org.apache.continuum.utils.file;

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
import java.io.InputStream;

/**
 * A service interface for handling common file system related tasks. By depending on this interface rather than static
 * utility methods, components can have consistent, bug-fixed and easily-stubbed file system operations. By limiting
 * coupling to utility libraries, it also allows for easier upgrades for utility libraries.
 */
public interface FileSystemManager
{
    /**
     * Removes contents contained in the specified directory, leaving the directory intact.
     *
     * @param dir the directory to clean out
     * @throws IOException
     */
    void wipeDir( File dir )
        throws IOException;

    /**
     * Removes the specified directory and all contents, if any.
     *
     * @param dir the directory to remove
     * @throws IOException
     */
    void removeDir( File dir )
        throws IOException;

    /**
     * Copies the source directory and its contents to the specified destination.
     *
     * @param source directory to copy
     * @param dest   location to copy the directory to
     */
    void copyDir( File source, File dest )
        throws IOException;

    /**
     * Forcibly removes the target directory or file, including contents. Also handles symlinks.
     *
     * @param fileOrDir file or directory to delete
     * @throws IOException
     */
    void delete( File fileOrDir )
        throws IOException;

    /**
     * Reads the entire contents of the specified file.
     *
     * @param file the file to read
     * @return contents of the file, as String
     * @throws IOException
     */
    String fileContents( File file )
        throws IOException;

    /**
     * Reads the entire contents of the specified file.
     *
     * @param file the file to read
     * @return contents of the file, as a byte array
     * @throws IOException
     */
    byte[] fileBytes( File file )
        throws IOException;

    /**
     * Copies the specified file to a file at the specified destination.
     *
     * @param source the file to copy
     * @param dest   the destination of the copy
     * @throws IOException
     */
    void copyFile( File source, File dest )
        throws IOException;

    /**
     * Writes a String as content for the specified file.
     *
     * @param file     file to write to
     * @param contents contents to write
     * @throws IOException
     */
    void writeFile( File file, String contents )
        throws IOException;

    /**
     * Writes contents of InputStream as content for the specified file.
     *
     * @param file  file to write to
     * @param input opened stream to read contents from, will not be closed automatically
     * @throws IOException
     */
    void writeFile( File file, InputStream input )
        throws IOException;

    /**
     * Returns the temporary directory for this environment.
     *
     * @return a {@link File} pointing at the file system path specified by the system property, java.io.tmpdir
     */
    File getTempDir();

    /**
     * Create a new temporary file under the specified parent directory.
     *
     * @param prefix prefix to start file with
     * @param suffix suffix to end file name with
     * @param parent the directory to create the temp file in
     * @return
     */
    File createTempFile( String prefix, String suffix, File parent );

    /**
     * Copies the specified file to the destination directory.
     *
     * @param file the file to copy
     * @param dest the destination directory to put the copy
     * @throws IOException
     */
    void copyFileToDir( File file, File dest )
        throws IOException;

    /**
     * Registers the given file or directory for removal upon JVM shutdown.
     *
     * @param fileOrDir the file or dir to remove when JVM shuts down
     * @throws IOException
     */
    void deleteOnExit( File fileOrDir )
        throws IOException;
}
