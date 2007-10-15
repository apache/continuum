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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationLoadingException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.notification.notifier.AbstractNotifier;

import java.util.List;

public abstract class AbstractContinuumNotifier
    extends AbstractNotifier
{
    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;

    /**
     * @plexus.configuration
     */
    private boolean alwaysSend = false;

    /**
     * Returns url of the last build
     *
     * @param project              The project
     * @param build                The build
     * @param configurationService The configuration Service
     * @return The report URL
     * @throws ContinuumException whne the configuration can't be loaded
     */
    public String getReportUrl( Project project, BuildResult build, ConfigurationService configurationService )
        throws ContinuumException
    {
        try
        {
            if ( !configurationService.isLoaded() )
            {
                configurationService.load();
            }

            StringBuffer buf = new StringBuffer( configurationService.getUrl() );

            if ( project != null && build != null )
            {
                if ( !buf.toString().endsWith( "/" ) )
                {
                    buf.append( "/" );
                }

                buf.append( "buildResult.action?buildId=" ).append( build.getId() ).append( "&projectId=" )
                    .append( project.getId() );
            }

            return buf.toString();
        }
        catch ( ConfigurationLoadingException e )
        {
            throw new ContinuumException( "Can't obtain the base url from configuration.", e );
        }
    }

    /**
     * Determine if message must be sent
     *
     * @param build           The current build result
     * @param previousBuild   The previous build result
     * @param projectNotifier The project notifier
     * @return True if a message must be sent
     */
    public boolean shouldNotify( BuildResult build, BuildResult previousBuild, ProjectNotifier projectNotifier )
    {
        if ( projectNotifier == null )
        {
            projectNotifier = new ProjectNotifier();
        }

        if ( build == null )
        {
            return false;
        }

        if ( alwaysSend )
        {
            return true;
        }

        if ( build.getState() == ContinuumProjectState.FAILED && projectNotifier.isSendOnFailure() )
        {
            return true;
        }

        if ( build.getState() == ContinuumProjectState.ERROR && projectNotifier.isSendOnError() )
        {
            return true;
        }

        // Send if this is the first build
        if ( previousBuild == null )
        {
            if ( build.getState() == ContinuumProjectState.ERROR )
            {
                return projectNotifier.isSendOnError();
            }

            if ( build.getState() == ContinuumProjectState.FAILED )
            {
                return projectNotifier.isSendOnFailure();
            }

            if ( build.getState() == ContinuumProjectState.OK )
            {
                return projectNotifier.isSendOnSuccess();
            }

            if ( build.getState() == ContinuumProjectState.WARNING )
            {
                return projectNotifier.isSendOnWarning();
            }

            return true;
        }

        // Send if the state has changed
        getLogger().debug(
            "Current build state: " + build.getState() + ", previous build state: " + previousBuild.getState() );

        if ( build.getState() != previousBuild.getState() )
        {
            if ( build.getState() == ContinuumProjectState.ERROR )
            {
                return projectNotifier.isSendOnError();
            }

            if ( build.getState() == ContinuumProjectState.FAILED )
            {
                return projectNotifier.isSendOnFailure();
            }

            if ( build.getState() == ContinuumProjectState.OK )
            {
                return projectNotifier.isSendOnSuccess();
            }

            if ( build.getState() == ContinuumProjectState.WARNING )
            {
                return projectNotifier.isSendOnWarning();
            }

            return true;
        }

        getLogger().info( "Same state, not sending message." );

        return false;
    }

    protected BuildResult getPreviousBuild( Project project, BuildDefinition buildDef, BuildResult currentBuild )
        throws NotificationException
    {
        List<BuildResult> builds;
        try
        {
            if ( buildDef != null )
            {
                builds = store.getBuildResultsByBuildDefinition( project.getId(), buildDef.getId() );

                if ( builds.size() < 2 )
                {
                    return null;
                }
                //builds are sorted in descending way
                BuildResult build = builds.get( 0 );
                if ( currentBuild != null && build.getId() != currentBuild.getId() )
                {
                    throw new NotificationException(
                        "INTERNAL ERROR: The current build wasn't the first in the build list. " + "Current build: '" +
                            currentBuild.getId() + "', " + "first build: '" + build.getId() + "'." );
                }
                else
                {
                    return builds.get( 1 );
                }
            }
            else
            {
                //Normally, it isn't possible, buildDef should be != null
                if ( project.getId() > 0 )
                {
                    project = store.getProjectWithBuilds( project.getId() );
                }
                builds = project.getBuildResults();

                if ( builds.size() < 2 )
                {
                    return null;
                }

                BuildResult build = builds.get( builds.size() - 1 );

                if ( currentBuild != null && build.getId() != currentBuild.getId() )
                {
                    throw new NotificationException(
                        "INTERNAL ERROR: The current build wasn't the first in the build list. " + "Current build: '" +
                            currentBuild.getId() + "', " + "first build: '" + build.getId() + "'." );
                }

                return builds.get( builds.size() - 2 );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new NotificationException( "Unable to obtain project builds", e );
        }
    }

}
