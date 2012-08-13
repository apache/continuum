package org.apache.maven.continuum.utils;

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
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Configurable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.utils.ContinuumUrlValidator"
 * role-hint="continuumUrl"
 * @since 27 mars 2008
 */
@Service( "continuumUrlValidator#continuumUrl" )
public class ContinuumUrlValidator
    implements Configurable
{
    /**
     * The set of schemes that are allowed to be in a URL.
     */
    private Set<String> allowedSchemes = new HashSet<String>();

    /**
     * If no schemes are provided, default to this set.
     */
    protected String[] defaultSchemes = {"http", "https", "ftp"};

    /**
     * Create a UrlValidator with default properties.
     */
    public ContinuumUrlValidator()
    {
        this( null );
    }

    /**
     * Behavior of validation is modified by passing in several strings options:
     *
     * @param schemes Pass in one or more url schemes to consider valid, passing in
     *                a null will default to "http,https,ftp" being valid.
     *                If a non-null schemes is specified then all valid schemes must
     *                be specified.
     */
    public ContinuumUrlValidator( String[] schemes )
    {
        if ( schemes == null && this.allowedSchemes.isEmpty() )
        {
            schemes = this.defaultSchemes;
        }

        this.allowedSchemes.addAll( Arrays.asList( schemes ) );
    }

    /**
     * <p>Checks if a field has a valid url address.</p>
     *
     * @param value The value validation is being performed on.  A <code>null</code>
     *              value is considered invalid.
     * @return true if the url is valid.
     */
    public boolean validate( String value )
    {
        return isValid( value );
    }

    /**
     * <p>Checks if a field has a valid url address.</p>
     *
     * @param value The value validation is being performed on.  A <code>null</code>
     *              value is considered valid.
     * @return true if the url is valid.
     */
    public boolean isValid( String value )
    {
        if ( StringUtils.isEmpty( value ) )
        {
            return true;
        }

        try
        {
            URI uri = new URI( value );
            return this.allowedSchemes.contains( uri.getScheme() );
        }
        catch ( URISyntaxException e )
        {
            return false;
        }
    }

    /**
     * @param url
     * @return URLUserInfo cannot be null
     * @throws URISyntaxException
     */
    public URLUserInfo extractURLUserInfo( String url )
        throws URISyntaxException
    {
        URI uri = new URI( url );
        // can contains user:password
        String userInfoRaw = uri.getUserInfo();
        URLUserInfo urlUserInfo = new URLUserInfo();

        if ( !StringUtils.isEmpty( userInfoRaw ) )
        {
            int index = userInfoRaw.indexOf( ':' );
            if ( index >= 0 )
            {
                urlUserInfo.setUsername( userInfoRaw.substring( 0, index ) );
                urlUserInfo.setPassword( userInfoRaw.substring( index + 1, userInfoRaw.length() ) );
            }
            else
            {
                urlUserInfo.setUsername( userInfoRaw );
            }
        }
        return urlUserInfo;
    }

    public void configure( PlexusConfiguration plexusConfiguration )
        throws PlexusConfigurationException
    {
        PlexusConfiguration allowedSchemesElement = plexusConfiguration.getChild( "allowedSchemes" );
        if ( allowedSchemesElement != null )
        {
            PlexusConfiguration[] allowedSchemeElements = allowedSchemesElement.getChildren( "allowedScheme" );
            for ( int i = 0, size = allowedSchemeElements.length; i < size; i++ )
            {
                this.allowedSchemes.add( allowedSchemeElements[i].getValue() );
            }
        }
    }

}
