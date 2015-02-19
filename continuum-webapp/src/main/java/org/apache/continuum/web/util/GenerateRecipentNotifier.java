package org.apache.continuum.web.util;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.notification.AbstractContinuumNotifier;
import org.codehaus.plexus.util.StringUtils;

import java.util.Map;

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

/**
 * @author José Morales Martínez
 */
public final class GenerateRecipentNotifier
{
    private GenerateRecipentNotifier()
    {

    }

    @SuppressWarnings( "unchecked" )
    public static String generate( ProjectNotifier notifier )
    {
        Map<String, String> configuration = notifier.getConfiguration();
        String recipent = "unknown";
        if ( ( "mail".equals( notifier.getType() ) ) || ( "msn".equals( notifier.getType() ) ) ||
            ( "jabber".equals( notifier.getType() ) ) )
        {
            if ( StringUtils.isNotEmpty( configuration.get( AbstractContinuumNotifier.ADDRESS_FIELD ) ) )
            {
                recipent = configuration.get( AbstractContinuumNotifier.ADDRESS_FIELD );
            }
            if ( StringUtils.isNotEmpty( configuration.get( AbstractContinuumNotifier.COMMITTER_FIELD ) ) )
            {
                if ( Boolean.parseBoolean( configuration.get( AbstractContinuumNotifier.COMMITTER_FIELD ) ) )
                {
                    if ( "unknown".equals( recipent ) )
                    {
                        recipent = "latest committers";
                    }
                    else
                    {
                        recipent += ", " + "latest committers";
                    }
                }
            }
            if ( StringUtils.isNotEmpty( configuration.get( AbstractContinuumNotifier.DEVELOPER_FIELD ) ) )
            {
                if ( Boolean.parseBoolean( configuration.get( AbstractContinuumNotifier.DEVELOPER_FIELD ) ) )
                {
                    if ( "unknown".equals( recipent ) )
                    {
                        recipent = "project developers";
                    }
                    else
                    {
                        recipent += ", " + "project developers";
                    }
                }
            }
        }
        if ( "irc".equals( notifier.getType() ) )
        {
            recipent = configuration.get( "host" );
            if ( configuration.get( "port" ) != null )
            {
                recipent = recipent + ":" + configuration.get( "port" );
            }
            recipent = recipent + ":" + configuration.get( "channel" );
        }
        if ( "wagon".equals( notifier.getType() ) )
        {
            recipent = configuration.get( "url" );
        }
        // escape the characters, it may contain characters possible for an XSS attack
        return StringEscapeUtils.escapeXml( recipent );
    }
}
