package org.apache.continuum.purge.executor.dir;

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

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.EMPTY_LIST;
import static org.apache.commons.io.comparator.LastModifiedFileComparator.LASTMODIFIED_COMPARATOR;
import static org.apache.commons.io.filefilter.DirectoryFileFilter.DIRECTORY;
import static org.apache.commons.io.filefilter.FileFileFilter.FILE;
import static org.apache.commons.io.filefilter.TrueFileFilter.TRUE;

public class PurgeBuilder
{
    public static Purge purge( File dir )
    {
        return new PurgeDelegate( dir );
    }
}

interface Purge
{
    Purge dirs();

    Purge files();

    Purge namedLike( String pattern );

    Purge notNamedLike( String pattern );

    Purge olderThan( int ageInDays );

    Purge inAgeOrder();

    Purge retainLast( int min );

    void executeWith( Handler handler )
        throws PurgeBuilderException;

    List<File> list()
        throws PurgeBuilderException;
}

interface Handler
{
    void handle( File f );
}

class PurgeBuilderException
    extends Exception
{
    public PurgeBuilderException( String message )
    {
        super( message );
    }
}

class PurgeDelegate
    implements Purge
{
    private static long MILLIS_IN_DAY = 24 * 60 * 26 * 1000;

    private File root;

    boolean recursive;

    private int maxScanDepth = -1;

    private AndFileFilter filter;

    private Comparator<File> ordering;

    private int retainMin;

    public PurgeDelegate( File root )
    {
        this.root = root;
        filter = new AndFileFilter();
        filter.addFileFilter( TRUE );
        retainMin = 0;
    }

    public Purge dirs()
    {
        filter.addFileFilter( DIRECTORY );
        return this;
    }

    public Purge files()
    {
        filter.addFileFilter( FILE );
        return this;
    }

    public Purge namedLike( String pattern )
    {
        filter.addFileFilter( new WildcardFileFilter( pattern ) );
        return this;
    }

    public Purge notNamedLike( String pattern )
    {
        filter.addFileFilter( new NotFileFilter( new WildcardFileFilter( pattern ) ) );
        return this;
    }

    public Purge olderThan( int age )
    {
        if ( age > 0 )
        {
            filter.addFileFilter( new AgeFileFilter( System.currentTimeMillis() - age * MILLIS_IN_DAY ) );
        }
        return this;
    }

    @SuppressWarnings( "unchecked" )
    public Purge inAgeOrder()
    {
        ordering = LASTMODIFIED_COMPARATOR;
        return this;
    }

    public Purge retainLast( int min )
    {
        if ( min > 0 )
        {
            retainMin = min;
        }
        return this;
    }

    public void executeWith( Handler handler )
        throws PurgeBuilderException
    {
        for ( File file : list() )
        {
            handler.handle( file );
        }
    }

    public List<File> list()
        throws PurgeBuilderException
    {
        List<File> files = listRoot();
        if ( retainMin > 0 )
        {
            sort( files );
            int limit = files.size() - retainMin;
            return limit < 0 ? EMPTY_LIST : files.subList( 0, limit );
        }
        return files;
    }

    private void sort( List<File> files )
    {
        if ( ordering != null )
        {
            Collections.sort( files, ordering );
        }
    }

    private List<File> listRoot()
        throws PurgeBuilderException
    {
        if ( !root.exists() )
        {
            throw new PurgeBuilderException( String.format( "purge root %s does not exist", root ) );
        }
        if ( !root.isDirectory() )
        {
            throw new PurgeBuilderException( String.format( "purge root %s is not a directory", root ) );
        }
        if ( !root.canRead() )
        {
            throw new PurgeBuilderException( String.format( "purge root %s is not readable", root ) );
        }

        if ( !recursive )
        {
            maxScanDepth = 1;
        }

        try
        {
            return new PurgeScanner( root, filter, maxScanDepth ).scan();
        }
        catch ( IOException e )
        {
            throw new PurgeBuilderException( "failure during scan: " + e.getMessage() );
        }
    }
}

class PurgeScanner
    extends DirectoryWalker
{
    private File root;

    private FileFilter filter;

    PurgeScanner( File root, FileFilter filter, int depth )
    {
        super( null, depth );
        this.root = root;
        this.filter = filter;
    }

    public List<File> scan()
        throws IOException
    {
        List<File> scanned = new ArrayList<File>();
        walk( root, scanned );
        return scanned;
    }

    @Override
    protected void handleFile( File file, int depth, Collection results )
        throws IOException
    {
        if ( filter.accept( file ) )
        {
            results.add( file );
        }
    }

    @Override
    protected boolean handleDirectory( File directory, int depth, Collection results )
        throws IOException
    {
        if ( !root.equals( directory ) && filter.accept( directory ) )
        {
            results.add( directory );
        }
        return true;
    }
}