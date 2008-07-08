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

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.notification.manager.NotifierManager;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.notification.ContinuumNotificationDispatcher"
 * role-hint="default"
 */
public class DefaultContinuumNotificationDispatcher
    implements ContinuumNotificationDispatcher
{
    private Logger log = LoggerFactory.getLogger( getClass() );

    /**
     * @plexus.requirement
     */
    private NotifierManager notifierManager;

    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;

    // ----------------------------------------------------------------------
    // ContinuumNotificationDispatcher Implementation
    // ----------------------------------------------------------------------

    public void buildStarted( Project project, BuildDefinition buildDefinition )
    {
        sendNotification( MESSAGE_ID_BUILD_STARTED, project, buildDefinition, null );
    }

    public void checkoutStarted( Project project, BuildDefinition buildDefinition )
    {
        sendNotification( MESSAGE_ID_CHECKOUT_STARTED, project, buildDefinition, null );
    }

    public void checkoutComplete( Project project, BuildDefinition buildDefinition )
    {
        sendNotification( MESSAGE_ID_CHECKOUT_COMPLETE, project, buildDefinition, null );
    }

    public void runningGoals( Project project, BuildDefinition buildDefinition, BuildResult buildResult )
    {
        sendNotification( MESSAGE_ID_RUNNING_GOALS, project, buildDefinition, buildResult );
    }

    public void goalsCompleted( Project project, BuildDefinition buildDefinition, BuildResult buildResult )
    {
        sendNotification( MESSAGE_ID_GOALS_COMPLETED, project, buildDefinition, buildResult );
    }

    public void buildComplete( Project project, BuildDefinition buildDefinition, BuildResult buildResult )
    {
        sendNotification( MESSAGE_ID_BUILD_COMPLETE, project, buildDefinition, buildResult );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void sendNotification( String messageId, Project project, BuildDefinition buildDefinition,
                                   BuildResult buildResult )
    {
        //Map context = new HashMap();

        // ----------------------------------------------------------------------
        // The objects are reread from the store to make sure they're getting the "final"
        // state of the objects. Ideally this should be done on a per notifier basis or the
        // objects should be made read only.
        // ----------------------------------------------------------------------

        try
        {
            // TODO: remove re-reading?
            // Here we need to get all the project details
            //  - builds are used to detect if the state has changed (TODO: maybe previousState field is better)
            //  - notifiers are used to send the notification
            project = store.getProjectWithAllDetails( project.getId() );

            ProjectGroup projectGroup =
                store.getProjectGroupWithBuildDetailsByProjectGroupId( project.getProjectGroup().getId() );

            Map<String, List<ProjectNotifier>> notifiersMap = new HashMap<String, List<ProjectNotifier>>();
            // perform the project level notifications
            for ( ProjectNotifier notifier : (List<ProjectNotifier>) project.getNotifiers() )
            {
                List<ProjectNotifier> notifiers = notifiersMap.get( notifier.getType() );
                if ( notifiers == null )
                {
                    notifiers = new ArrayList<ProjectNotifier>();
                }

                if ( !notifier.isEnabled() )
                {
                    log.info( notifier.getType() + " notifier (id=" + notifier.getId() + ") is disabled." );

                    continue;
                }

                notifiers.add( notifier );
                notifiersMap.put( notifier.getType(), notifiers );
            }

            // perform the project group level notifications
            if ( projectGroup.getNotifiers() != null )
            {
                for ( ProjectNotifier projectNotifier : (List<ProjectNotifier>) projectGroup.getNotifiers() )
                {
                    List<ProjectNotifier> projectNotifiers = notifiersMap.get( projectNotifier.getType() );
                    if ( projectNotifiers == null )
                    {
                        projectNotifiers = new ArrayList<ProjectNotifier>();
                    }

                    if ( !projectNotifier.isEnabled() )
                    {
                        log.info( projectNotifier.getType() + " projectNotifier (id=" + projectNotifier.getId() +
                            ") is disabled." );

                        continue;
                    }

                    projectNotifiers.add( projectNotifier );
                    notifiersMap.put( projectNotifier.getType(), projectNotifiers );
                }
            }

            for ( String notifierType : notifiersMap.keySet() )
            {
                MessageContext context = new MessageContext();
                context.setProject( project );
                context.setBuildDefinition( buildDefinition );

                if ( buildResult != null )
                {
                    context.setBuildResult( buildResult );
                }

                List<ProjectNotifier> projectNotiiers = notifiersMap.get( notifierType );
                context.setNotifier( projectNotiiers );

                sendNotification( messageId, context );
            }
        }
        catch ( ContinuumStoreException e )
        {
            log.error( "Error while population the notification context.", e );
        }
    }

    private void sendNotification( String messageId, MessageContext context )
    {
        String notifierType = context.getNotifiers().get( 0 ).getType();

        try
        {
            Notifier notifier = notifierManager.getNotifier( notifierType );

            notifier.sendMessage( messageId, context );
        }
        catch ( NotificationException e )
        {
            log.error( "Error while trying to use the " + notifierType + " notifier.", e );
        }
    }
}
