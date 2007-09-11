package org.apache.maven.continuum.notification;

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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.codehaus.plexus.notification.AbstractRecipientSource;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ContinuumRecipientSource
    extends AbstractRecipientSource
    implements Initializable
{
    public static String ADDRESS_FIELD = "address";

    public static String COMMITTER_FIELD = "committers";

    /**
     * @plexus.configuration
     */
    private String toOverride;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
    {
        // ----------------------------------------------------------------------
        // To address
        // ----------------------------------------------------------------------

        if ( StringUtils.isEmpty( toOverride ) )
        {
            getLogger().info(
                "To override address is not configured, will use the nag email address from the project." );
        }
        else
        {
            getLogger().warn( "Using '" + toOverride + "' as the to address for all emails." );
        }
    }

    // ----------------------------------------------------------------------
    // RecipientSource Implementation
    // ----------------------------------------------------------------------

    public Set getRecipients( String notifierId, String messageId, Map configuration, Map context )
        throws NotificationException
    {
        Project project = (Project) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT );

        ProjectNotifier projectNotifier =
            (ProjectNotifier) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT_NOTIFIER );

        if ( project == null )
        {
            throw new NotificationException( "Missing project from the notification context." );
        }

        Set<String> recipients = new HashSet<String>();

        if ( !StringUtils.isEmpty( toOverride ) )
        {
            recipients.add( toOverride );
        }
        else if ( projectNotifier != null )
        {
            addNotifierAdresses( projectNotifier, recipients, project, context );
        }
        else if ( project.getNotifiers() != null && !project.getNotifiers().isEmpty() )
        {
            for ( ProjectNotifier notifier : (List<ProjectNotifier>) project.getNotifiers() )
            {
                if ( notifier.getId() == Integer.parseInt( notifierId ) &&
                    notifier.getConfiguration().containsKey( ADDRESS_FIELD ) )
                {
                    addNotifierAdresses( notifier, recipients, project, context );
                }
            }
        }

        if ( recipients.isEmpty() )
        {
            return Collections.EMPTY_SET;
        }
        else
        {
            return recipients;
        }
    }

    private void addNotifierAdresses( ProjectNotifier notifier, Set<String> recipients, Project project, Map context )
        throws NotificationException
    {
        if ( notifier.getConfiguration() != null )
        {
            String addressField = (String) notifier.getConfiguration().get( ADDRESS_FIELD );

            if ( StringUtils.isNotEmpty( addressField ) )
            {
                String[] addresses = StringUtils.split( addressField, "," );

                for ( String address : addresses )
                {
                    recipients.add( address.trim() );
                }
            }

            if ( "mail".equals( notifier.getType() ) )
            {
                String committerField = (String) notifier.getConfiguration().get( COMMITTER_FIELD );
                if ( StringUtils.isNotEmpty( committerField ) )
                {
                    if ( Boolean.parseBoolean( committerField ) )
                    {
                        ScmResult scmResult =
                            (ScmResult) context.get( ContinuumNotificationDispatcher.CONTEXT_UPDATE_SCM_RESULT );
                        if ( scmResult != null && scmResult.getChanges() != null && !scmResult.getChanges().isEmpty() )
                        {
                            if ( project == null )
                            {
                                throw new NotificationException( "Missing project from the notification context." );
                            }

                            List<ProjectDeveloper> developers = project.getDevelopers();
                            if ( developers == null || developers.isEmpty() )
                            {
                                getLogger().warn(
                                    "No developers have been configured...notifcation email " + "will not be sent" );
                                return;
                            }

                            Map<String, String> developerToEmailMap = mapDevelopersToRecipients( developers );

                            List<ChangeSet> changes = scmResult.getChanges();

                            for ( ChangeSet changeSet : changes )
                            {
                                String scmId = changeSet.getAuthor();
                                if ( StringUtils.isNotEmpty( scmId ) )
                                {
                                    String email = developerToEmailMap.get( scmId );
                                    if ( StringUtils.isEmpty( email ) )
                                    {
                                        getLogger().warn( "no email address is defined in developers list for '" +
                                            scmId + "' scm id." );
                                    }
                                    else
                                    {
                                        recipients.add( email );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Map<String, String> mapDevelopersToRecipients( List<ProjectDeveloper> developers )
    {
        Map<String, String> developersMap = new HashMap<String, String>();

        for ( ProjectDeveloper developer : developers )
        {
            if ( StringUtils.isNotEmpty( developer.getScmId() ) && StringUtils.isNotEmpty( developer.getEmail() ) )
            {
                developersMap.put( developer.getScmId(), developer.getEmail() );
            }
        }

        return developersMap;
    }
}
