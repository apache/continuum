package org.apache.maven.continuum.notification.msn;

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
import org.codehaus.plexus.msn.MsnClient;
import org.codehaus.plexus.msn.MsnException;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
@Service( "notifier#msn" )
public class MsnContinuumNotifier
    extends AbstractContinuumNotifier
{
    private static final Logger log = LoggerFactory.getLogger( MsnContinuumNotifier.class );

    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    @Resource
    private MsnClient msnClient;

    @Resource
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

    public String getType()
    {
        return "msn";
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
            log.info( "No MSN recipients for '" + project.getName() + "'." );

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
        // Check if the message should be sent at all
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
        msnClient.setLogin( getUsername( configuration ) );

        msnClient.setPassword( getPassword( configuration ) );

        try
        {
            msnClient.login();

            if ( configuration != null && StringUtils.isNotEmpty( configuration.get( ADDRESS_FIELD ) ) )
            {
                String address = configuration.get( ADDRESS_FIELD );
                String[] recipients = StringUtils.split( address, "," );
                for ( String recipient : recipients )
                {
                    msnClient.sendMessage( recipient, message );
                }
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

    private String getUsername( Map<String, String> configuration )
    {
        if ( configuration.containsKey( "login" ) )
        {
            return configuration.get( "login" );
        }

        return fromAddress;
    }

    private String getPassword( Map<String, String> configuration )
    {
        if ( configuration.containsKey( "password" ) )
        {
            return configuration.get( "password" );
        }

        return fromPassword;
    }
}
