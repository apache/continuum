package org.apache.maven.continuum.notification.irc;

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
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStore;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.util.StringUtils;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCConstants;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import org.schwering.irc.lib.ssl.SSLDefaultTrustManager;
import org.schwering.irc.lib.ssl.SSLIRCConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * <b>This implementation assumes there aren't concurrent acces to the IRCConnection</b>
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.notification.notifier.Notifier" role-hint="irc"
 */
public class IrcContinuumNotifier
    extends AbstractContinuumNotifier
    implements Disposable
{
    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;    
    
    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.configuration default-value="6667"
     */
    private int defaultPort;

    /**
     * key is upper(hostname) + port + upper(nick) + upper(alternateNick)
     */
    private Map<String, IRCConnection> hostConnections = new HashMap<String, IRCConnection>();

    private Map<String, List> channelConnections = new HashMap<String, List>();


    // ----------------------------------------------------------------------
    // Plexus Lifecycle
    // ----------------------------------------------------------------------    
    public void dispose()
    {
        // cleanup connections
        for ( Iterator<String> keys = hostConnections.keySet().iterator(); keys.hasNext(); )
        {
            String key = keys.next();
            IRCConnection connection = hostConnections.get( key );
            if ( connection.isConnected() )
            {
                connection.doQuit( "Continuum shutting down" );
                connection.close();
            }
        }

    }

    // ----------------------------------------------------------------------
    // Internal connections 
    // ----------------------------------------------------------------------    
    private IRCConnection getIRConnection( String host, int port, String password, String nick, String alternateNick,
                                           String userName, String realName, String channel, boolean ssl )
        throws IOException
    {
        String key = getConnectionKey( host, port, nick, alternateNick );
        IRCConnection conn = hostConnections.get( key );
        if ( conn != null )
        {
            checkConnection( conn, key );
            return conn;
        }

        if ( !ssl )
        {
            conn = new IRCConnection( host, new int[]{port}, password, nick, userName, realName );
        }
        else
        {
            conn = new SSLIRCConnection( host, new int[]{port}, password, nick, userName, realName );
            ( (SSLIRCConnection) conn ).addTrustManager( new SSLDefaultTrustManager() );
        }

        conn.addIRCEventListener( new Listener( conn, nick, alternateNick ) );
        checkConnection( conn, key );
        checkChannel( conn, key, channel );
        hostConnections.put( key, conn );
        return conn;
    }

    private String getConnectionKey( String host, int port, String nick, String alternateNick )
    {
        String nickname = nick;
        String alternateNickName = alternateNick;
        if ( nick == null )
        {
            nickname = "null";
        }
        if ( alternateNick == null )
        {
            alternateNickName = "null";
        }
        return host.toUpperCase() + Integer.toString( port ) + nickname.toUpperCase() + alternateNickName.toUpperCase();
    }

    private void checkConnection( IRCConnection conn, String key )
        throws IOException
    {
        if ( !conn.isConnected() )
        {
            conn.connect();
            //required for some servers that are slow to initialise the connection, in most of case, servers with auth 
            try
            {
                Thread.sleep( 5000 );
            }
            catch ( InterruptedException e )
            {
                //nothing to do
            }

            //join to all channels
            List channels = channelConnections.get( key );
            if ( channels != null )
            {
                for ( Iterator i = channels.iterator(); i.hasNext(); )
                {
                    String channel = (String) i.next();
                    connectToChannel( conn, channel );
                }
            }
        }
    }

    private void checkChannel( IRCConnection conn, String key, String channel )
    {
        List channels = channelConnections.get( key );
        if ( channels == null )
        {
            connectToChannel( conn, channel );
            channels = new ArrayList();
            channels.add( channel );
            channelConnections.put( key, channels );
        }
        else
        {
            boolean found = false;
            for ( Iterator i = channels.iterator(); i.hasNext(); )
            {
                String c = (String) i.next();
                if ( c.equalsIgnoreCase( channel ) )
                {
                    found = true;
                }
            }
            if ( !found )
            {
                channels.add( channel );
                channelConnections.put( key, channels );
            }

            //reconnect unconditionally
            connectToChannel( conn, channel );
        }
    }

    private void connectToChannel( IRCConnection conn, String channel )
    {
        conn.doJoin( channel );
    }

    // ----------------------------------------------------------------------
    // Notifier Implementation
    // ----------------------------------------------------------------------


    public void sendNotification( String source, Set recipients, Map configuration, Map context )
        throws NotificationException
    {
        Project project = (Project) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT );

        ProjectNotifier projectNotifier =
            (ProjectNotifier) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT_NOTIFIER );

        BuildDefinition buildDefinition = (BuildDefinition) context
            .get( ContinuumNotificationDispatcher.CONTEXT_BUILD_DEFINITION );

        BuildResult build = (BuildResult) context.get( ContinuumNotificationDispatcher.CONTEXT_BUILD );

        // ----------------------------------------------------------------------
        // If there wasn't any building done, don't notify
        // ----------------------------------------------------------------------

        if ( build == null )
        {
            return;
        }

        // ----------------------------------------------------------------------
        // Generate and send message
        // ----------------------------------------------------------------------

        try
        {
            if ( source.equals( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE ) )
            {
                buildComplete( project, projectNotifier, build, buildDefinition, configuration );
            }
        }
        catch ( ContinuumException e )
        {
            throw new NotificationException( "Error while notifiying.", e );
        }
    }

    private void buildComplete( Project project, ProjectNotifier projectNotifier, BuildResult build,
                                BuildDefinition buildDef, Map configuration )
        throws ContinuumException, NotificationException
    {
        // ----------------------------------------------------------------------
        // Check if the message should be sent at all
        // ----------------------------------------------------------------------

        BuildResult previousBuild = getPreviousBuild( project, buildDef, build );

        if ( !shouldNotify( build, previousBuild, projectNotifier ) )
        {
            return;
        }

        // ----------------------------------------------------------------------
        // Gather configuration values
        // ----------------------------------------------------------------------

        String host = (String) configuration.get( "host" );

        String portAsString = (String) configuration.get( "port" );
        int port = defaultPort;
        if ( portAsString != null )
        {
            port = Integer.parseInt( portAsString );
        }
        String channel = (String) configuration.get( "channel" );

        String nickName = (String) configuration.get( "nick" );

        if ( StringUtils.isEmpty( nickName ) )
        {
            nickName = "continuum";
        }

        String alternateNickName = (String) configuration.get( "alternateNick" );

        if ( StringUtils.isEmpty( alternateNickName ) )
        {
            alternateNickName = "continuum_";
        }

        String userName = (String) configuration.get( "username" );

        if ( StringUtils.isEmpty( userName ) )
        {
            userName = nickName;
        }

        String fullName = (String) configuration.get( "fullName" );

        if ( StringUtils.isEmpty( fullName ) )
        {
            fullName = nickName;
        }

        String password = (String) configuration.get( "password" );

        boolean isSsl = Boolean.parseBoolean( (String) configuration.get( "ssl" ) );

        try
        {
            IRCConnection ircConnection = getIRConnection( host, port, password, nickName, alternateNickName, userName,
                                                           fullName, channel, isSsl );
            ircConnection.doPrivmsg( channel, generateMessage( project, build ) );
        }
        catch ( IOException e )
        {
            throw new NotificationException( "Exception while checkConnection to irc ." + host, e );
        }
    }

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
            getLogger().warn( "Unknown build state " + state + " for project " + project.getId() );

            message = "ERROR: Unknown build state " + state + " for " + project.getName() + " project";
        }

        return message + " " + getReportUrl( project, build, configurationService );
    }

    /**
     * @see org.codehaus.plexus.notification.notifier.Notifier#sendNotification(java.lang.String,java.util.Set,java.util.Properties)
     */
    public void sendNotification( String arg0, Set arg1, Properties arg2 )
        throws NotificationException
    {
        throw new NotificationException( "Not implemented." );
    }

    /**
     * Treats IRC events. The most of them are just printed.
     */
    class Listener
        implements IRCEventListener
    {
        private String nick;

        private String alternateNick;

        private IRCConnection conn;

        public Listener( IRCConnection conn, String nick, String alternateNick )
        {
            this.conn = conn;
            this.nick = nick;
            this.alternateNick = alternateNick;
        }

        public void onRegistered()
        {
            getLogger().info( "Connected" );
        }

        public void onDisconnected()
        {
            getLogger().info( "Disconnected" );
        }

        public void onError( String msg )
        {
            getLogger().error( "Error: " + msg );
        }

        public void onError( int num, String msg )
        {
            getLogger().error( "Error #" + num + ": " + msg );
            if ( num == IRCConstants.ERR_NICKNAMEINUSE )
            {
                if ( alternateNick != null )
                {
                    getLogger().info( "reconnection with alternate nick: '" + alternateNick + "'" );
                    try
                    {
                        boolean ssl = false;
                        if ( conn instanceof SSLIRCConnection )
                        {
                            ssl = true;
                        }
                        String key = getConnectionKey( conn.getHost(), conn.getPort(), nick, alternateNick );
                        conn = getIRConnection( conn.getHost(), conn.getPort(), conn.getPassword(), alternateNick, null,
                                                conn.getUsername(), conn.getRealname(), "#foo", ssl );
                        hostConnections.put( key, conn );
                    }
                    catch ( IOException e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void onInvite( String chan, IRCUser u, String nickPass )
        {
            getLogger().debug( chan + "> " + u.getNick() + " invites " + nickPass );
        }

        public void onJoin( String chan, IRCUser u )
        {
            getLogger().debug( chan + "> " + u.getNick() + " joins" );
        }

        public void onKick( String chan, IRCUser u, String nickPass, String msg )
        {
            getLogger().debug( chan + "> " + u.getNick() + " kicks " + nickPass );
        }

        public void onMode( IRCUser u, String nickPass, String mode )
        {
            getLogger().debug( "Mode: " + u.getNick() + " sets modes " + mode + " " + nickPass );
        }

        public void onMode( String chan, IRCUser u, IRCModeParser mp )
        {
            getLogger().debug( chan + "> " + u.getNick() + " sets mode: " + mp.getLine() );
        }

        public void onNick( IRCUser u, String nickNew )
        {
            getLogger().debug( "Nick: " + u.getNick() + " is now known as " + nickNew );
        }

        public void onNotice( String target, IRCUser u, String msg )
        {
            getLogger().info( target + "> " + u.getNick() + " (notice): " + msg );
        }

        public void onPart( String chan, IRCUser u, String msg )
        {
            getLogger().debug( chan + "> " + u.getNick() + " parts" );
        }

        public void onPrivmsg( String chan, IRCUser u, String msg )
        {
            getLogger().debug( chan + "> " + u.getNick() + ": " + msg );
        }

        public void onQuit( IRCUser u, String msg )
        {
            getLogger().debug( "Quit: " + u.getNick() );
        }

        public void onReply( int num, String value, String msg )
        {
            getLogger().info( "Reply #" + num + ": " + value + " " + msg );
        }

        public void onTopic( String chan, IRCUser u, String topic )
        {
            getLogger().debug( chan + "> " + u.getNick() + " changes topic into: " + topic );
        }

        public void onPing( String p )
        {
            getLogger().debug( "Ping:" + p );
        }

        public void unknown( String a, String b, String c, String d )
        {
            getLogger().debug( "UNKNOWN: " + a + " b " + c + " " + d );
        }
    }

    protected ContinuumStore getContinuumStore()
    {
        return this.store;
    }    
}
