package org.apache.continuum.webdav.util;

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

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.io.OutputContext;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class IndexWriter
{
    private final String logicalResource;

    private final List<File> localResources;

    public IndexWriter( DavResource resource, File localResource, String logicalResource )
    {
        this.localResources = new ArrayList<File>();
        this.localResources.add( localResource );
        this.logicalResource = logicalResource;
    }

    public IndexWriter( DavResource resource, List<File> localResources, String logicalResource )
    {
        this.localResources = localResources;
        this.logicalResource = logicalResource;
    }

    public void write( OutputContext outputContext )
    {
        outputContext.setModificationTime( new Date().getTime() );
        outputContext.setContentType( "text/html" );
        outputContext.setETag( "" );
        if ( outputContext.hasStream() )
        {
            PrintWriter writer = new PrintWriter( outputContext.getOutputStream() );
            writeDocumentStart( writer );
            writeHyperlinks( writer );
            writeDocumentEnd( writer );
            writer.flush();
            writer.close();
        }
    }

    private void writeDocumentStart( PrintWriter writer )
    {
        writer.println( "<html>" );
        writer.println( "<head>" );
        writer.println( "<title>Working Copy: /" + logicalResource + "</title>" );
        writer.println( "</head>" );
        writer.println( "<body>" );
        writer.println( "<h3>Working Copy: /" + logicalResource + "</h3>" );

        // Check if not root
        if ( logicalResource.length() > 0 )
        {
            File file = new File( logicalResource );
            String parentName = file.getParent() == null ? "/" : file.getParent();

            //convert to unix path in case archiva is hosted on windows
            parentName = StringUtils.replace( parentName, "\\", "/" );

            writer.println( "<ul>" );
            writer.println( "<li><a href=\"../\">" + parentName + "</a> <i><small>(Parent)</small></i></li>" );
            writer.println( "</ul>" );
        }

        writer.println( "</ul>" );
    }

    private void writeDocumentEnd( PrintWriter writer )
    {
        writer.println( "</ul>" );
        writer.println( "</body>" );
        writer.println( "</html>" );
    }

    private void writeHyperlinks( PrintWriter writer )
    {
        for ( File localResource : localResources )
        {
            List<File> files = new ArrayList<File>( Arrays.asList( localResource.listFiles() ) );
            Collections.sort( files );

            for ( File file : files )
            {
                writeHyperlink( writer, file.getName(), file.isDirectory() );
            }
        }
    }

    private void writeHyperlink( PrintWriter writer, String resourceName, boolean directory )
    {
        if ( directory && !"CVS".equals( resourceName ) && !".svn".equals( resourceName ) && !"SCCS".equals(
            resourceName ) )
        {
            writer.println( "<li><a href=\"" + resourceName + "/\">" + resourceName + "</a></li>" );
        }
        else if ( !directory && !".cvsignore".equals( resourceName ) && !"vssver.scc".equals( resourceName ) &&
            !".DS_Store".equals( resourceName ) && !"release.properties".equals( resourceName ) )
        {
            writer.println( "<li><a href=\"" + resourceName + "\">" + resourceName + "</a></li>" );
        }
    }
}
