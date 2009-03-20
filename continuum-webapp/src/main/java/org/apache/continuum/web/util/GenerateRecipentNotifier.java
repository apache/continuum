package org.apache.continuum.web.util;

import java.util.Map;

import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.notification.AbstractContinuumNotifier;
import org.codehaus.plexus.util.StringUtils;

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
 * @version $Id$
 */
public final class GenerateRecipentNotifier
{
    private GenerateRecipentNotifier()
    {

    }

    @SuppressWarnings( "unchecked" )
    public static String generate( ProjectNotifier notifier )
    {
        Map configuration = notifier.getConfiguration();
        String recipent = "unknown";
        if ( ( "mail".equals( notifier.getType() ) ) || ( "msn".equals( notifier.getType() ) )
            || ( "jabber".equals( notifier.getType() ) ) )
        {
            if ( StringUtils.isNotEmpty( (String) configuration.get( AbstractContinuumNotifier.ADDRESS_FIELD ) ) )
            {
                recipent = (String) configuration.get( AbstractContinuumNotifier.ADDRESS_FIELD );
            }
            if ( StringUtils.isNotEmpty( (String) configuration.get( AbstractContinuumNotifier.COMMITTER_FIELD ) ) )
            {
                if ( Boolean.parseBoolean( (String) configuration.get( AbstractContinuumNotifier.COMMITTER_FIELD ) ) )
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
        }
        if ( "irc".equals( notifier.getType() ) )
        {
            recipent = (String) configuration.get( "host" );
            if ( configuration.get( "port" ) != null )
            {
                recipent = recipent + ":" + (String) configuration.get( "port" );
            }
            recipent = recipent + ":" + (String) configuration.get( "channel" );
        }
        if ( "wagon".equals( notifier.getType() ) )
        {
            recipent = (String) configuration.get( "url" );
        }
        return recipent;
    }
}
