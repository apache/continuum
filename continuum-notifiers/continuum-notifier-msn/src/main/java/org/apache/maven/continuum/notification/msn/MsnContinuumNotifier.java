package org.apache.maven.continuum.notification.msn;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.notification.AbstractContinuumNotifier;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;

import org.codehaus.plexus.msn.MsnClient;
import org.codehaus.plexus.msn.MsnException;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class MsnContinuumNotifier
    extends AbstractContinuumNotifier
{
    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private MsnClient msnClient;

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

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Notifier Implementation
    // ----------------------------------------------------------------------

    public void sendNotification( String source,
                                  Set recipients,
                                  Map configuration,
                                  Map context )
        throws NotificationException
    {
        ContinuumProject project = (ContinuumProject) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT );

        ContinuumBuild build = (ContinuumBuild) context.get( ContinuumNotificationDispatcher.CONTEXT_BUILD );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        if ( recipients.size() == 0 )
        {
            getLogger().info( "No MSN recipients for '" + project.getName() + "'." );

            return;
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        if ( source.equals( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE ) )
        {
            sendMessage( project, build, recipients, configuration );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String generateMessage( ContinuumProject project, ContinuumBuild build )
        throws ContinuumException
    {
        int state = build.getState();

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
            getLogger().warn( "Unknown build state " + build.getState() + " for project " + project.getId() );

            message = "ERROR: Unknown build state " + build.getState();
        }

        return message + " " + getReportUrl( project, build, configurationService );
    }

    private void sendMessage( ContinuumProject project,
                              ContinuumBuild build,
                              Set recipients,
                              Map configuration )
        throws NotificationException
    {
        String message = "";

        try
        {
            message = generateMessage( project, build );
        }
        catch ( ContinuumException e )
        {
            throw new NotificationException( "Can't generate the message.", e );
        }

        msnClient.setLogin( getUsername( configuration ) );

        msnClient.setPassword( getPassword( configuration ) );

        try
        {
            msnClient.login();

            for ( Iterator i = recipients.iterator(); i.hasNext(); )
            {
                String recipient = (String) i.next();

                msnClient.sendMessage( recipient, message );
            }
        }
        catch ( MsnException e )
        {
            throw new NotificationException( "Exception while sending message.", e );
        }
        finally
        {
            try
            {
                msnClient.logout();
            }
            catch ( MsnException e )
            {

            }
        }
    }

    /**
     * @see org.codehaus.plexus.notification.notifier.Notifier#sendNotification(java.lang.String, java.util.Set, java.util.Properties)
     */
    public void sendNotification( String arg0, Set arg1, Properties arg2 )
        throws NotificationException
    {
        throw new NotificationException( "Not implemented." );
    }

    private String getUsername( Map configuration )
    {
        if ( configuration.containsKey( "login" ) )
        {
            String username = (String) configuration.get( "login" );

            return username;
        }

        return fromAddress;
    }

    private String getPassword( Map configuration )
    {
        if ( configuration.containsKey( "password" ) )
        {
            String password = (String) configuration.get( "password" );

            return password;
        }

        return fromPassword;
    }
}
