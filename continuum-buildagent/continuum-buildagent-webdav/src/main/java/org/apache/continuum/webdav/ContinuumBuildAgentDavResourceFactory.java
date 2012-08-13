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

import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.webdav.util.WebdavMethodUtil;
import org.apache.continuum.webdav.util.WorkingCopyPathUtil;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;

/**
 * @plexus.component role="org.apache.continuum.webdav.ContinuumBuildAgentDavResourceFactory"
 */
public class ContinuumBuildAgentDavResourceFactory
    implements DavResourceFactory
{
    private static final Logger log = LoggerFactory.getLogger( ContinuumBuildAgentDavResourceFactory.class );

    private static final MimetypesFileTypeMap mimeTypes;

    /**
     * @plexus.requirement
     */
    private BuildAgentConfigurationService buildAgentConfigurationService;

    static
    {
        mimeTypes = new MimetypesFileTypeMap();
        mimeTypes.addMimeTypes( "application/java-archive jar war ear" );
        mimeTypes.addMimeTypes( "application/java-class class" );
        mimeTypes.addMimeTypes( "image/png png" );
    }

    public DavResource createResource( final DavResourceLocator locator, final DavSession davSession )
        throws DavException
    {
        ContinuumBuildAgentDavResourceLocator continuumLocator = checkLocatorIsInstanceOfContinuumBuildAgentLocator(
            locator );

        String logicalResource = WorkingCopyPathUtil.getLogicalResource( locator.getResourcePath() );
        if ( logicalResource.startsWith( "/" ) )
        {
            logicalResource = logicalResource.substring( 1 );
        }

        File resourceFile = getResourceFile( continuumLocator.getProjectId(), logicalResource );

        if ( !resourceFile.exists() || ( continuumLocator.getHref( false ).endsWith( "/" ) &&
            !resourceFile.isDirectory() ) )
        {
            // force a resource not found
            log.error( "Resource file '" + resourceFile.getAbsolutePath() + "' does not exist" );
            throw new DavException( HttpServletResponse.SC_NOT_FOUND, "Resource does not exist" );
        }
        else
        {
            return createResource( resourceFile, logicalResource, davSession, continuumLocator );
        }
    }

    public DavResource createResource( DavResourceLocator locator, DavServletRequest request,
                                       DavServletResponse response )
        throws DavException
    {
        ContinuumBuildAgentDavResourceLocator continuumLocator = checkLocatorIsInstanceOfContinuumBuildAgentLocator(
            locator );

        if ( !WebdavMethodUtil.isReadMethod( request.getMethod() ) )
        {
            throw new DavException( HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                                    "Write method not allowed in working copy" );
        }

        String logicalResource = WorkingCopyPathUtil.getLogicalResource( continuumLocator.getResourcePath() );
        if ( logicalResource.startsWith( "/" ) )
        {
            logicalResource = logicalResource.substring( 1 );
        }

        File resourceFile = getResourceFile( continuumLocator.getProjectId(), logicalResource );

        if ( !resourceFile.exists() || ( continuumLocator.getHref( false ).endsWith( "/" ) &&
            !resourceFile.isDirectory() ) )
        {
            // force a resource not found
            log.error( "Resource file '" + resourceFile.getAbsolutePath() + "' does not exist" );
            throw new DavException( HttpServletResponse.SC_NOT_FOUND, "Resource does not exist" );
        }
        else
        {
            return createResource( resourceFile, logicalResource, request.getDavSession(), continuumLocator );
        }
    }

    public BuildAgentConfigurationService getBuildAgentConfigurationService()
    {
        return buildAgentConfigurationService;
    }

    public MimetypesFileTypeMap getMimeTypes()
    {
        return mimeTypes;
    }

    public void setBuildAgentConfigurationService( BuildAgentConfigurationService buildAgentConfigurationService )
    {
        this.buildAgentConfigurationService = buildAgentConfigurationService;
    }

    private ContinuumBuildAgentDavResourceLocator checkLocatorIsInstanceOfContinuumBuildAgentLocator(
        DavResourceLocator locator )
        throws DavException
    {
        if ( !( locator instanceof ContinuumBuildAgentDavResourceLocator ) )
        {
            throw new DavException( HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                    "Locator does not implement ContinuumBuildAgentLocator" );
        }

        // Hidden paths
        if ( locator.getResourcePath().startsWith( ContinuumBuildAgentDavResource.HIDDEN_PATH_PREFIX ) )
        {
            throw new DavException( HttpServletResponse.SC_NOT_FOUND );
        }

        ContinuumBuildAgentDavResourceLocator continuumLocator = (ContinuumBuildAgentDavResourceLocator) locator;
        if ( continuumLocator.getProjectId() <= 0 )
        {
            log.error( "Invalid project id: " + continuumLocator.getProjectId() );
            throw new DavException( HttpServletResponse.SC_NO_CONTENT );
        }

        return continuumLocator;
    }

    protected File getResourceFile( int projectId, String logicalResource )
    {
        File workingDir = buildAgentConfigurationService.getWorkingDirectory( projectId );

        return new File( workingDir, logicalResource );
    }

    protected DavResource createResource( File resourceFile, String logicalResource, DavSession session,
                                          ContinuumBuildAgentDavResourceLocator locator )
    {
        return new ContinuumBuildAgentDavResource( resourceFile.getAbsolutePath(), logicalResource, session, locator,
                                                   this, mimeTypes );
    }
}
