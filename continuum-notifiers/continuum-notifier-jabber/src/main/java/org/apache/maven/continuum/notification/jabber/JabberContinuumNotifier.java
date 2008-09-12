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

import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.notification.AbstractContinuumNotifier;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.notification.MessageContext;
import org.apache.maven.continuum.notification.NotificationException;
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
        ProjectScmRoot projectScmRoot = context.getProjectScmRoot();

        boolean isPrepareBuildComplete = 
            messageId.equals( ContinuumNotificationDispatcher.MESSAGE_ID_PREPARE_BUILD_COMPLETE );

        if ( projectScmRoot == null && isPrepareBuildComplete )
        {
            return;
        }
        
        // ----------------------------------------------------------------------
        // If there wasn't any building done, don't notify
        // ----------------------------------------------------------------------

        if ( build == null && !isPrepareBuildComplete )
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
                buildComplete( project, notifier, build, buildDefinition );
            }
        }
        else if ( isPrepareBuildComplete )
        {
            for ( ProjectNotifier notifier : notifiers )
            {
                prepareBuildComplete( projectScmRoot, notifier );
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void buildComplete( Project project, ProjectNotifier notifier, BuildResult build, BuildDefinition buildDef )
        throws NotificationException
    {
        // ----------------------------------------------------------------------
        // Check if the mail should be sent at all
        // ----------------------------------------------------------------------

        BuildResult previousBuild = getPreviousBuild( project, buildDef, build );

        if ( !shouldNotify( build, previousBuild, notifier ) )
        {
            return;
        }

        sendMessage( notifier.getConfiguration(), generateMessage( project, build, configurationService ) );
    }
    
    private void prepareBuildComplete( ProjectScmRoot projectScmRoot, ProjectNotifier notifier )
        throws NotificationException
    {
        if ( !shouldNotify( projectScmRoot, notifier ) )
        {
            return;
        }
        
        sendMessage( notifier.getConfiguration(), generateMessage( projectScmRoot, configurationService ) );
    }
    
    private void sendMessage( Map<String, String> configuration, String message )
        throws NotificationException
    {
        jabberClient.setHost( getHost( configuration ) );

        jabberClient.setPort( getPort( configuration ) );

        jabberClient.setUser( getUsername( configuration ) );

        jabberClient.setPassword( getPassword( configuration ) );

        jabberClient.setImDomainName( getImDomainName( configuration ) );

        jabberClient.setSslConnection( isSslConnection( configuration ) );

        try
        {
            jabberClient.connect();

            jabberClient.logon();

            if ( configuration != null && StringUtils.isNotEmpty( (String) configuration.get( ADDRESS_FIELD ) ) )
            {
                String address = (String) configuration.get( ADDRESS_FIELD );
                String[] recipients = StringUtils.split( address, "," );
                for ( String recipient : recipients )
                {
                    if ( isGroup( configuration ) )
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
