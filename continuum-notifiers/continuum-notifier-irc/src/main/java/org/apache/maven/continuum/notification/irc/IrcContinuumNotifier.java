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
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.util.StringUtils;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCConstants;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import org.schwering.irc.lib.ssl.SSLDefaultTrustManager;
import org.schwering.irc.lib.ssl.SSLIRCConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

/**
 * <b>This implementation assumes there aren't concurrent acces to the IRCConnection</b>
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
@Service( "notifier#irc" )
public class IrcContinuumNotifier
    extends AbstractContinuumNotifier
    implements Disposable
{
    private static final Logger log = LoggerFactory.getLogger( IrcContinuumNotifier.class );

    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    @Resource
    private ConfigurationService configurationService;

    private int defaultPort = 6667;

    /**
     * key is upper(hostname) + port + upper(nick) + upper(alternateNick)
     */
    private Map<String, IRCConnection> hostConnections = new HashMap<String, IRCConnection>();

    private Map<String, List<String>> channelConnections = new HashMap<String, List<String>>();


    // ----------------------------------------------------------------------
    // Plexus Lifecycle
    // ----------------------------------------------------------------------    
    public void dispose()
    {
        // cleanup connections
        for ( String key : hostConnections.keySet() )
        {
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
            List<String> channels = channelConnections.get( key );
            if ( channels != null )
            {
                for ( String channel : channels )
                {
                    connectToChannel( conn, channel );
                }
            }
        }
    }

    private void checkChannel( IRCConnection conn, String key, String channel )
    {
        List<String> channels = channelConnections.get( key );
        if ( channels == null )
        {
            connectToChannel( conn, channel );
            channels = new ArrayList<String>();
            channels.add( channel );
            channelConnections.put( key, channels );
        }
        else
        {
            boolean found = false;
            for ( String c : channels )
            {
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

    public String getType()
    {
        return "irc";
    }

    public void sendMessage( String messageId, MessageContext context )
        throws NotificationException
    {
        Project project = context.getProject();

        List<ProjectNotifier> notifiers = context.getNotifiers();

        BuildDefinition buildDefinition = context.getBuildDefinition();

        BuildResult build = context.getBuildResult();

        ProjectScmRoot projectScmRoot = context.getProjectScmRoot();

        boolean isPrepareBuildComplete = messageId.equals(
            ContinuumNotificationDispatcher.MESSAGE_ID_PREPARE_BUILD_COMPLETE );

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
        // Generate and send message
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

    private void buildComplete( Project project, ProjectNotifier projectNotifier, BuildResult build,
                                BuildDefinition buildDef )
        throws NotificationException
    {
        // ----------------------------------------------------------------------
        // Check if the message should be sent at all
        // ----------------------------------------------------------------------

        BuildResult previousBuild = getPreviousBuild( project, buildDef, build );

        if ( !shouldNotify( build, previousBuild, projectNotifier ) )
        {
            return;
        }

        sendMessage( projectNotifier.getConfiguration(), generateMessage( project, build, configurationService ) );
    }

    private void prepareBuildComplete( ProjectScmRoot projectScmRoot, ProjectNotifier projectNotifier )
        throws NotificationException
    {
        // ----------------------------------------------------------------------
        // Check if the message should be sent at all
        // ----------------------------------------------------------------------

        if ( !shouldNotify( projectScmRoot, projectNotifier ) )
        {
            return;
        }

        sendMessage( projectNotifier.getConfiguration(), generateMessage( projectScmRoot, configurationService ) );
    }

    private void sendMessage( Map<String, String> configuration, String message )
        throws NotificationException
    {
        // ----------------------------------------------------------------------
        // Gather configuration values
        // ----------------------------------------------------------------------

        String host = configuration.get( "host" );

        String portAsString = configuration.get( "port" );
        int port = defaultPort;
        if ( portAsString != null )
        {
            port = Integer.parseInt( portAsString );
        }
        String channel = configuration.get( "channel" );

        String nickName = configuration.get( "nick" );

        if ( StringUtils.isEmpty( nickName ) )
        {
            nickName = "continuum";
        }

        String alternateNickName = configuration.get( "alternateNick" );

        if ( StringUtils.isEmpty( alternateNickName ) )
        {
            alternateNickName = "continuum_";
        }

        String userName = configuration.get( "username" );

        if ( StringUtils.isEmpty( userName ) )
        {
            userName = nickName;
        }

        String fullName = configuration.get( "fullName" );

        if ( StringUtils.isEmpty( fullName ) )
        {
            fullName = nickName;
        }

        String password = configuration.get( "password" );

        boolean isSsl = Boolean.parseBoolean( configuration.get( "ssl" ) );

        try
        {
            IRCConnection ircConnection = getIRConnection( host, port, password, nickName, alternateNickName, userName,
                                                           fullName, channel, isSsl );
            ircConnection.doPrivmsg( channel, message );
        }
        catch ( IOException e )
        {
            throw new NotificationException( "Exception while checkConnection to irc ." + host, e );
        }
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
            log.info( "Connected" );
        }

        public void onDisconnected()
        {
            log.info( "Disconnected" );
        }

        public void onError( String msg )
        {
            log.error( "Error: " + msg );
        }

        public void onError( int num, String msg )
        {
            log.error( "Error #" + num + ": " + msg );
            if ( num == IRCConstants.ERR_NICKNAMEINUSE )
            {
                if ( alternateNick != null )
                {
                    log.info( "reconnection with alternate nick: '" + alternateNick + "'" );
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
            if ( log.isDebugEnabled() )
            {
                log.debug( chan + "> " + u.getNick() + " invites " + nickPass );
            }
        }

        public void onJoin( String chan, IRCUser u )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( chan + "> " + u.getNick() + " joins" );
            }
        }

        public void onKick( String chan, IRCUser u, String nickPass, String msg )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( chan + "> " + u.getNick() + " kicks " + nickPass );
            }
        }

        public void onMode( IRCUser u, String nickPass, String mode )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Mode: " + u.getNick() + " sets modes " + mode + " " + nickPass );
            }
        }

        public void onMode( String chan, IRCUser u, IRCModeParser mp )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( chan + "> " + u.getNick() + " sets mode: " + mp.getLine() );
            }
        }

        public void onNick( IRCUser u, String nickNew )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Nick: " + u.getNick() + " is now known as " + nickNew );
            }
        }

        public void onNotice( String target, IRCUser u, String msg )
        {
            log.info( target + "> " + u.getNick() + " (notice): " + msg );
        }

        public void onPart( String chan, IRCUser u, String msg )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( chan + "> " + u.getNick() + " parts" );
            }
        }

        public void onPrivmsg( String chan, IRCUser u, String msg )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( chan + "> " + u.getNick() + ": " + msg );
            }
        }

        public void onQuit( IRCUser u, String msg )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Quit: " + u.getNick() );
            }
        }

        public void onReply( int num, String value, String msg )
        {
            log.info( "Reply #" + num + ": " + value + " " + msg );
        }

        public void onTopic( String chan, IRCUser u, String topic )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( chan + "> " + u.getNick() + " changes topic into: " + topic );
            }
        }

        public void onPing( String p )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Ping:" + p );
            }
        }

        public void unknown( String a, String b, String c, String d )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "UNKNOWN: " + a + " b " + c + " " + d );
            }
        }
    }
}
