package org.apache.maven.continuum.notification.console;

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
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.notification.AbstractContinuumNotifier;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.notification.MessageContext;
import org.apache.maven.continuum.notification.NotificationException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
@Component( role = org.apache.maven.continuum.notification.Notifier.class, hint = "console" )
public class ConsoleNotifier
    extends AbstractContinuumNotifier
{
    private static final Logger log = LoggerFactory.getLogger( ConsoleNotifier.class );

    // ----------------------------------------------------------------------
    // Notifier Implementation
    // ----------------------------------------------------------------------

    public String getType()
    {
        return "console";
    }

    public void sendMessage( String messageId, MessageContext context )
        throws NotificationException
    {
        Project project = context.getProject();

        BuildResult build = context.getBuildResult();

        ProjectScmRoot projectScmRoot = context.getProjectScmRoot();

        if ( messageId.equals( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_STARTED ) )
        {
            buildStarted( project );
        }
        else if ( messageId.equals( ContinuumNotificationDispatcher.MESSAGE_ID_CHECKOUT_STARTED ) )
        {
            checkoutStarted( project );
        }
        else if ( messageId.equals( ContinuumNotificationDispatcher.MESSAGE_ID_CHECKOUT_COMPLETE ) )
        {
            checkoutComplete( project );
        }
        else if ( messageId.equals( ContinuumNotificationDispatcher.MESSAGE_ID_RUNNING_GOALS ) )
        {
            runningGoals( project, build );
        }
        else if ( messageId.equals( ContinuumNotificationDispatcher.MESSAGE_ID_GOALS_COMPLETED ) )
        {
            goalsCompleted( project, build );
        }
        else if ( messageId.equals( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE ) )
        {
            buildComplete( project, build );
        }
        else if ( messageId.equals( ContinuumNotificationDispatcher.MESSAGE_ID_PREPARE_BUILD_COMPLETE ) )
        {
            prepareBuildComplete( projectScmRoot );
        }
        else
        {
            log.warn( "Unknown messageId: '" + messageId + "'." );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void buildStarted( Project project )
    {
        out( project, null, "Build started." );
    }

    private void checkoutStarted( Project project )
    {
        out( project, null, "Checkout started." );
    }

    private void checkoutComplete( Project project )
    {
        out( project, null, "Checkout complete." );
    }

    private void runningGoals( Project project, BuildResult build )
    {
        out( project, build, "Running goals." );
    }

    private void goalsCompleted( Project project, BuildResult build )
    {
        if ( build.getError() == null )
        {
            out( project, build, "Goals completed. state: " + build.getState() );
        }
        else
        {
            out( project, build, "Goals completed." );
        }
    }

    private void buildComplete( Project project, BuildResult build )
    {
        if ( build.getError() == null )
        {
            out( project, build, "Build complete. state: " + build.getState() );
        }
        else
        {
            out( project, build, "Build complete." );
        }
    }

    private void prepareBuildComplete( ProjectScmRoot projectScmRoot )
    {
        if ( StringUtils.isEmpty( projectScmRoot.getError() ) )
        {
            out( projectScmRoot, "Prepare build complete. state: " + projectScmRoot.getState() );
        }
        else
        {
            out( projectScmRoot, "Prepare build complete." );
        }
    }

    private void out( Project project, BuildResult build, String msg )
    {
        System.out.println( "Build event for project '" + project.getName() + "':" + msg );

        if ( build != null && !StringUtils.isEmpty( build.getError() ) )
        {
            System.out.println( build.getError() );
        }
    }

    private void out( ProjectScmRoot projectScmRoot, String msg )
    {
        if ( projectScmRoot != null )
        {
            System.out.println( "Prepare build event for '" + projectScmRoot.getScmRootAddress() + "':" + msg );

            if ( !StringUtils.isEmpty( projectScmRoot.getError() ) )
            {
                System.out.println( projectScmRoot.getError() );
            }
        }
    }
}
