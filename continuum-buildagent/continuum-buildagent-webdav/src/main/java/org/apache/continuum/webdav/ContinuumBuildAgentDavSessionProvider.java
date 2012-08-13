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
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.codehaus.plexus.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

public class ContinuumBuildAgentDavSessionProvider
    implements DavSessionProvider
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    private BuildAgentConfigurationService buildAgentConfigurationService;

    public ContinuumBuildAgentDavSessionProvider( BuildAgentConfigurationService buildAgentConfigurationService )
    {
        this.buildAgentConfigurationService = buildAgentConfigurationService;
    }

    public boolean attachSession( WebdavRequest request )
        throws DavException
    {
        if ( !isAuthorized( request ) )
        {
            throw new DavException( HttpServletResponse.SC_UNAUTHORIZED );
        }

        request.setDavSession( new ContinuumBuildAgentDavSession() );

        return true;
    }

    public void releaseSession( WebdavRequest request )
    {
        request.setDavSession( null );
    }

    private boolean isAuthorized( WebdavRequest request )
    {
        String header = request.getHeader( "Authorization" );

        // in tomcat this is : authorization=Basic YWRtaW46TWFuYWdlMDc=
        if ( header == null )
        {
            header = request.getHeader( "authorization" );
        }

        if ( ( header != null ) && header.startsWith( "Basic " ) )
        {
            String base64Token = header.substring( 6 );
            String token = new String( Base64.decodeBase64( base64Token.getBytes() ) );

            String password = "";
            int delim = token.indexOf( ':' );

            if ( delim != ( -1 ) )
            {
                password = token.substring( delim + 1 );
            }

            if ( buildAgentConfigurationService.getSharedSecretPassword() != null &&
                buildAgentConfigurationService.getSharedSecretPassword().equals( password ) )
            {
                log.debug( "Password matches configured shared key in continuum build agent." );
                return true;
            }
        }

        log.warn( "Not authorized to access the working copy." );

        return false;
    }
}
