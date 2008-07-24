package org.apache.maven.continuum.notification.jabber;

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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.notification.AbstractContinuumNotifier;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.notification.MessageContext;
import org.apache.maven.continuum.notification.NotificationException;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.jabber.JabberClient;
import org.codehaus.plexus.jabber.JabberClientException;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class JabberContinuumNotifier
    extends AbstractContinuumNotifier
{
    private Logger log = LoggerFactory.getLogger( getClass() );

    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private JabberClient jabberClient;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    /**
     * @plexus.configuration
     */
    private String fromAddress;

    /**
     * @plexus.configuration
     */
    private String fromPassword;

    /**
     * @plexus.configuration
     */
    private String host;

    /**
     * @plexus.configuration
     */
    private int port;

    /**
     * @plexus.configuration
     */
    private String imDomainName;

    /**
     * @plexus.configuration
     */
    private boolean sslConnection;

    // ----------------------------------------------------------------------
    // Notifier Implementation
    // ----------------------------------------------------------------------

    public String getType()
    {
        return "jabber";
    }

    public void sendMessage( String messageId, MessageContext context )
        throws NotificationException
    {
        Project project = context.getProject();

        List<ProjectNotifier> notifiers = context.getNotifiers();
        BuildDefinition buildDefinition = context.getBuildDefinition();
        BuildResult build = context.getBuildResult();
        // ----------------------------------------------------------------------
        // If there wasn't any building done, don't notify
        // ----------------------------------------------------------------------

        if ( build == null )
        {
            return;
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        List<String> recipients = new ArrayList<String>();
        for ( ProjectNotifier notifier : notifiers )
        {
            Map<String, String> configuration = notifier.getConfiguration();
            if ( configuration != null && StringUtils.isNotEmpty( configuration.get( ADDRESS_FIELD ) ) )
            {
                recipients.add( configuration.get( ADDRESS_FIELD ) );
            }
        }

        if ( recipients.size() == 0 )
        {
            log.info( "No Jabber recipients for '" + project.getName() + "'." );

            return;
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        if ( messageId.equals( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE ) )
        {
            for ( ProjectNotifier notifier : notifiers )
            {
                sendMessage( project, notifier, build, buildDefinition );
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String generateMessage( Project project, BuildResult build )
        throws ContinuumException
    {
        int state = project.getState();

        if ( build != null )
        {
            state = build.getState();
        }

        String message;

        if ( state == ContinuumProjectState.OK )
        {
            message = "BUILD SUCCESSFUL: " + project.getName();
        }
        else if ( state == ContinuumProjectState.FAILED )
        {
            message = "BUILD FAILURE: " + project.getName();
        }
        else if ( state == ContinuumProjectState.ERROR )
        {
            message = "BUILD ERROR: " + project.getName();
        }
        else
        {
            log.warn( "Unknown build state " + state + " for project " + project.getId() );

            message = "ERROR: Unknown build state " + state + " for " + project.getName() + " project";
        }

        return message + " " + getReportUrl( project, build, configurationService );
    }

    private void sendMessage( Project project, ProjectNotifier notifier, BuildResult build, BuildDefinition buildDef )
        throws NotificationException
    {
        String message;

        // ----------------------------------------------------------------------
        // Check if the mail should be sent at all
        // ----------------------------------------------------------------------

        BuildResult previousBuild = getPreviousBuild( project, buildDef, build );

        if ( !shouldNotify( build, previousBuild, notifier ) )
        {
            return;
        }

        try
        {
            message = generateMessage( project, build );
        }
        catch ( ContinuumException e )
        {
            throw new NotificationException( "Can't generate the message.", e );
        }

        jabberClient.setHost( getHost( notifier.getConfiguration() ) );

        jabberClient.setPort( getPort( notifier.getConfiguration() ) );

        jabberClient.setUser( getUsername( notifier.getConfiguration() ) );

        jabberClient.setPassword( getPassword( notifier.getConfiguration() ) );

        jabberClient.setImDomainName( getImDomainName( notifier.getConfiguration() ) );

        jabberClient.setSslConnection( isSslConnection( notifier.getConfiguration() ) );

        try
        {
            jabberClient.connect();

            jabberClient.logon();

            if ( notifier.getConfiguration() != null &&
                StringUtils.isNotEmpty( (String) notifier.getConfiguration().get( ADDRESS_FIELD ) ) )
            {
                String address = (String) notifier.getConfiguration().get( ADDRESS_FIELD );
                String[] recipients = StringUtils.split( address, "," );
                for ( String recipient : recipients )
                {
                    if ( isGroup( notifier.getConfiguration() ) )
                    {
                        jabberClient.sendMessageToGroup( recipient, message );
                    }
                    else
                    {
                        jabberClient.sendMessageToUser( recipient, message );
                    }
                }
            }
        }
        catch ( JabberClientException e )
        {
            throw new NotificationException( "Exception while sending message.", e );
        }
        finally
        {
            try
            {
                jabberClient.logoff();
            }
            catch ( JabberClientException e )
            {

            }
        }
    }

    private String getHost( Map configuration )
    {
        if ( configuration.containsKey( "host" ) )
        {
            return (String) configuration.get( "host" );
        }
        else
        {
            if ( configuration.containsKey( "address" ) )
            {
                String username = (String) configuration.get( "address" );

                if ( username.indexOf( "@" ) > 0 )
                {
                    return username.substring( username.indexOf( "@" ) + 1 );
                }
            }
        }

        return host;
    }

    private int getPort( Map configuration )
    {
        if ( configuration.containsKey( "port" ) )
        {
            try
            {
                return Integer.parseInt( (String) configuration.get( "port" ) );
            }
            catch ( NumberFormatException e )
            {
                log.error( "jabber port isn't a number.", e );
            }
        }

        if ( port > 0 )
        {
            return port;
        }
        else if ( isSslConnection( configuration ) )
        {
            return 5223;
        }
        else
        {
            return 5222;
        }
    }

    private String getUsername( Map configuration )
    {
        if ( configuration.containsKey( "login" ) )
        {
            String username = (String) configuration.get( "login" );

            if ( username.indexOf( "@" ) > 0 )
            {
                username = username.substring( 0, username.indexOf( "@" ) );
            }

            return username;
        }

        return fromAddress;
    }

    private String getPassword( Map configuration )
    {
        if ( configuration.containsKey( "password" ) )
        {
            return (String) configuration.get( "password" );
        }

        return fromPassword;
    }

    private boolean isSslConnection( Map configuration )
    {
        if ( configuration.containsKey( "sslConnection" ) )
        {
            return convertBoolean( (String) configuration.get( "sslConnection" ) );
        }

        return sslConnection;
    }

    private String getImDomainName( Map configuration )
    {
        if ( configuration.containsKey( "domainName" ) )
        {
            return (String) configuration.get( "domainName" );
        }

        return imDomainName;
    }

    private boolean isGroup( Map configuration )
    {
        return configuration.containsKey( "isGroup" ) && convertBoolean( (String) configuration.get( "isGroup" ) );
    }

    private boolean convertBoolean( String value )
    {
        return "true".equalsIgnoreCase( value ) || "on".equalsIgnoreCase( value ) || "yes".equalsIgnoreCase( value );
    }
}
