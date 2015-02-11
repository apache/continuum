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

import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectGroupDao;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.notification.manager.NotifierManager;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
@Component( role = org.apache.maven.continuum.notification.ContinuumNotificationDispatcher.class, hint = "default" )
public class DefaultContinuumNotificationDispatcher
    implements ContinuumNotificationDispatcher
{
    private static final Logger log = LoggerFactory.getLogger( DefaultContinuumNotificationDispatcher.class );

    @Requirement
    private NotifierManager notifierManager;

    @Requirement
    private ProjectDao projectDao;

    @Requirement
    private ProjectGroupDao projectGroupDao;

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

    public void prepareBuildComplete( ProjectScmRoot projectScmRoot )
    {
        sendNotification( MESSAGE_ID_PREPARE_BUILD_COMPLETE, projectScmRoot );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void sendNotification( String messageId, Project project, BuildDefinition buildDefinition,
                                   BuildResult buildResult )
    {
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
            //  - scm results are used to detect if scm failed
            project = projectDao.getProjectWithAllDetails( project.getId() );

            ProjectGroup projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId(
                project.getProjectGroup().getId() );

            Map<String, List<ProjectNotifier>> notifiersMap = new HashMap<String, List<ProjectNotifier>>();

            getProjectNotifiers( project, notifiersMap );
            getProjectGroupNotifiers( projectGroup, notifiersMap );

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

    private void sendNotification( String messageId, ProjectScmRoot projectScmRoot )
    {
        try
        {
            ProjectGroup group = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId(
                projectScmRoot.getProjectGroup().getId() );

            Map<String, List<ProjectNotifier>> notifiersMap = new HashMap<String, List<ProjectNotifier>>();
            getProjectGroupNotifiers( group, notifiersMap );

            for ( String notifierType : notifiersMap.keySet() )
            {
                MessageContext context = new MessageContext();
                context.setProjectScmRoot( projectScmRoot );

                List<ProjectNotifier> projectNotifiers = notifiersMap.get( notifierType );
                context.setNotifier( projectNotifiers );

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

    private void getProjectNotifiers( Project project, Map<String, List<ProjectNotifier>> notifiersMap )
    {
        if ( project.getNotifiers() != null )
        {
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
        }
    }

    private void getProjectGroupNotifiers( ProjectGroup projectGroup, Map<String, List<ProjectNotifier>> notifiersMap )
    {
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
    }
}
