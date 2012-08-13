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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class WorkingCopyPathUtil
{
    public static String getLogicalResource( final String href )
    {
        String logicalResource = null;
        String requestPathInfo = StringUtils.defaultString( href );

        //remove prefix ie /workingcopy/blah becomes /blah
        requestPathInfo = removePrefix( requestPathInfo );

        // Remove prefixing slash as the project id doesn't contain it;
        if ( requestPathInfo.startsWith( "/" ) )
        {
            requestPathInfo = requestPathInfo.substring( 1 );
        }

        int slash = requestPathInfo.indexOf( '/' );
        if ( slash > 0 )
        {
            logicalResource = requestPathInfo.substring( slash );

            if ( logicalResource.endsWith( "/.." ) )
            {
                logicalResource += "/";
            }

            if ( logicalResource != null && logicalResource.startsWith( "//" ) )
            {
                logicalResource = logicalResource.substring( 1 );
            }

            if ( logicalResource == null )
            {
                logicalResource = "/";
            }
        }
        else
        {
            logicalResource = "/";
        }
        return logicalResource;
    }

    public static int getProjectId( final String href )
    {
        String requestPathInfo = StringUtils.defaultString( href );

        // Remove prefix ie /workingcopy/blah becomes /blah
        requestPathInfo = removePrefix( requestPathInfo );

        // Remove prefixing slash as the project id doesn't contain it;
        if ( requestPathInfo.startsWith( "/" ) )
        {
            requestPathInfo = requestPathInfo.substring( 1 );
        }

        int projectId = 0;

        try
        {
            // Find first element, if slash exists.
            int slash = requestPathInfo.indexOf( '/' );
            if ( slash > 0 )
            {
                // Filtered: "1/src/main/java/" -> "1"
                projectId = Integer.parseInt( requestPathInfo.substring( 0, slash ) );
            }
            else
            {
                projectId = Integer.parseInt( requestPathInfo );
            }
        }
        catch ( NumberFormatException e )
        {
        }

        return projectId;
    }

    private static String removePrefix( final String href )
    {
        String[] parts = StringUtils.split( href, '/' );
        parts = (String[]) ArrayUtils.subarray( parts, 1, parts.length );
        if ( parts == null || parts.length == 0 )
        {
            return "/";
        }

        String joinedString = StringUtils.join( parts, '/' );
        if ( href.endsWith( "/" ) )
        {
            joinedString = joinedString + "/";
        }

        return joinedString;
    }
}
