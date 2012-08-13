package org.apache.continuum.webdav;

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
import org.apache.continuum.webdav.util.IndexWriter;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.io.OutputContext;

import java.io.FileInputStream;
import java.io.IOException;
import javax.activation.MimetypesFileTypeMap;

public class MockContinuumBuildAgentDavResource
    extends ContinuumBuildAgentDavResource
{
    public MockContinuumBuildAgentDavResource( String localResource, String logicalResource, DavSession session,
                                               ContinuumBuildAgentDavResourceLocator locator,
                                               DavResourceFactory factory, MimetypesFileTypeMap mimeTypes )
    {
        super( localResource, logicalResource, session, locator, factory, mimeTypes );
    }

    @Override
    public void spool( OutputContext outputContext )
        throws IOException
    {
        if ( !isCollection() )
        {
            outputContext.setContentLength( getLocalResource().length() );
            outputContext.setContentType( getMimeTypes().getContentType( getLocalResource() ) );
        }

        if ( !isCollection() && outputContext.hasStream() )
        {
            FileInputStream is = null;
            try
            {
                // Write content to stream
                is = new FileInputStream( getLocalResource() );
                IOUtils.copy( is, outputContext.getOutputStream() );
            }
            finally
            {
                IOUtils.closeQuietly( is );
            }
        }
        else if ( outputContext.hasStream() )
        {
            IndexWriter writer = new IndexWriter( this, getLocalResource(), getLogicalResource() );
            writer.write( outputContext );
        }
    }
}
