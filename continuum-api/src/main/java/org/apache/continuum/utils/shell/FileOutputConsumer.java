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

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Collects output to a file using a buffered writer. Unlike the list-based consumer, this should be safe to use when
 * the output size is expected to be large.
 */
public class FileOutputConsumer
    implements OutputConsumer
{
    private PrintWriter writer;

    /**
     * Creates a output consumer for the given file.
     *
     * @param outputFile the file to write the results to
     * @throws IOException if there is a problem creating a file
     */
    public FileOutputConsumer( File outputFile )
        throws IOException
    {
        this.writer = new PrintWriter( new FileWriter( outputFile ) );
        this.writer.close();  // make the file exist immediately
        this.writer = new PrintWriter( new FileWriter( outputFile, true ), true );  // append to created file
    }

    public void consume( String line )
    {
        if ( writer != null )
            writer.println( line );
    }

    public void close()
    {
        if ( writer != null )
            writer.close();
    }
}