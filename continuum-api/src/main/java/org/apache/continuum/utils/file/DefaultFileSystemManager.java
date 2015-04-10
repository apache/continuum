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

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component( role = FileSystemManager.class )
public class DefaultFileSystemManager
    implements FileSystemManager
{
    public void wipeDir( File dir )
        throws IOException
    {
        FileUtils.cleanDirectory( dir );
    }

    public void removeDir( File dir )
        throws IOException
    {
        FileUtils.deleteDirectory( dir );
    }

    public void copyDir( File source, File dest )
        throws IOException
    {
        FileUtils.copyDirectoryStructure( source, dest );
    }

    public void delete( File target )
        throws IOException
    {
        FileUtils.forceDelete( target );
    }

    public String fileContents( File file )
        throws IOException
    {
        return FileUtils.fileRead( file );
    }

    public byte[] fileBytes( File file )
        throws IOException
    {
        return org.apache.commons.io.FileUtils.readFileToByteArray( file );
    }

    public void copyFile( File source, File dest )
        throws IOException
    {
        FileUtils.copyFile( source, dest );
    }

    public void writeFile( File file, String contents )
        throws IOException
    {
        FileUtils.fileWrite( file, contents );
    }

    public void writeFile( File file, InputStream input )
        throws IOException
    {
        FileOutputStream fout = null;
        try
        {
            fout = new FileOutputStream( file );
            IOUtils.copy( input, fout );
        }
        finally
        {
            if ( fout != null )
            {
                fout.close();
            }
        }
    }

    public File createTempFile( String prefix, String suffix, File parent )
    {
        return FileUtils.createTempFile( prefix, suffix, parent );
    }

    public void copyFileToDir( File file, File dest )
        throws IOException
    {
        FileUtils.copyFileToDirectory( file, dest );
    }

    public void deleteOnExit( File fileOrDir )
        throws IOException
    {
        FileUtils.forceDeleteOnExit( fileOrDir );
    }
}
