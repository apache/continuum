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

import org.apache.continuum.configuration.ContinuumConfigurationException;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationLoadingException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import javax.annotation.Resource;

public abstract class AbstractContinuumNotifier
    implements Notifier
{
    public static final String ADDRESS_FIELD = "address";

    public static final String COMMITTER_FIELD = "committers";

    public static final String DEVELOPER_FIELD = "developers";

    private static final Logger log = LoggerFactory.getLogger( AbstractContinuumNotifier.class );

    @Resource
    private ConfigurationService configurationService;

    @Resource
    private BuildResultDao buildResultDao;

    @Resource
    private ProjectDao projectDao;

    @Resource
    private ProjectScmRootDao projectScmRootDao;

    private boolean alwaysSend = false;

    protected String getBuildOutput( Project project, BuildResult buildResult )
    {
        if ( buildResult == null )
        {
            return "";
        }
        try
        {
            if ( buildResult.getEndTime() != 0 )
            {
                return configurationService.getBuildOutput( buildResult.getId(), project.getId() );
            }
            else
            {
                return "";
            }
        }
        catch ( ConfigurationException e )
        {
            String msg = "Error while population the notification context.";
            log.error( msg, e );
            return msg;
        }
    }

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
                configurationService.reload();
            }

            StringBuffer buf = new StringBuffer( configurationService.getUrl() );

            if ( project != null && build != null )
            {
                if ( !buf.toString().endsWith( "/" ) )
                {
                    buf.append( "/" );
                }

                buf.append( "buildResult.action?buildId=" ).append( build.getId() ).append( "&projectId=" ).append(
                    project.getId() );
            }

            return buf.toString();
        }
        catch ( ConfigurationLoadingException e )
        {
            throw new ContinuumException( "Can't obtain the base url from configuration.", e );
        }
        catch ( ContinuumConfigurationException e )
        {
            throw new ContinuumException( "Can't obtain the base url from configuration.", e );
        }
    }

    public String getReportUrl( ProjectGroup projectGroup, ProjectScmRoot projectScmRoot,
                                ConfigurationService configurationService )
        throws ContinuumException
    {
        try
        {
            if ( !configurationService.isLoaded() )
            {
                configurationService.reload();
            }

            StringBuffer buf = new StringBuffer( configurationService.getUrl() );

            if ( projectGroup != null && projectScmRoot != null )
            {
                if ( !buf.toString().endsWith( "/" ) )
                {
                    buf.append( "/" );
                }

                buf.append( "scmResult.action?projectScmRootId=" ).append( projectScmRoot.getId() ).append(
                    "&projectGroupId=" ).append( projectGroup.getId() );
            }

            return buf.toString();
        }
        catch ( ConfigurationLoadingException e )
        {
            throw new ContinuumException( "Can't obtain the base url from configuration.", e );
        }
        catch ( ContinuumConfigurationException e )
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

            return build.getState() != ContinuumProjectState.WARNING || projectNotifier.isSendOnWarning();

        }

        // Send if the state has changed
        if ( log.isDebugEnabled() )
        {
            log.debug(
                "Current build state: " + build.getState() + ", previous build state: " + previousBuild.getState() );
        }

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

            return build.getState() != ContinuumProjectState.WARNING || projectNotifier.isSendOnWarning();

        }

        log.info( "Same state, not sending message." );

        return false;
    }

    public boolean shouldNotify( ProjectScmRoot projectScmRoot, ProjectNotifier projectNotifier )
    {
        if ( projectNotifier == null )
        {
            projectNotifier = new ProjectNotifier();
        }

        return projectScmRoot != null && ( alwaysSend ||
            projectScmRoot.getState() == ContinuumProjectState.ERROR && projectNotifier.isSendOnScmFailure() &&
                projectScmRoot.getOldState() != projectScmRoot.getState() );

    }

    protected BuildResult getPreviousBuild( Project project, BuildDefinition buildDef, BuildResult currentBuild )
        throws NotificationException
    {
        List<BuildResult> builds;
        try
        {
            if ( buildDef != null )
            {
                builds = buildResultDao.getBuildResultsByBuildDefinition( project.getId(), buildDef.getId(), 0, 2 );

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
                    project = projectDao.getProjectWithBuilds( project.getId() );
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

    protected String generateMessage( Project project, BuildResult build, ConfigurationService configurationService )
        throws NotificationException
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
            log.warn( "Unknown build state " + state + " for project " + project.getId() );

            message = "ERROR: Unknown build state " + state + " for " + project.getName() + " project";
        }

        try
        {
            return message + " " + getReportUrl( project, build, configurationService );
        }
        catch ( ContinuumException e )
        {
            throw new NotificationException( "Cannot generate message", e );
        }
    }

    protected String generateMessage( ProjectScmRoot projectScmRoot, ConfigurationService configurationService )
        throws NotificationException
    {
        int state = projectScmRoot.getState();
        String scmRootAddress = projectScmRoot.getScmRootAddress();

        String message;

        if ( state == ContinuumProjectState.UPDATED )
        {
            message = "PREPARE BUILD SUCCESSFUL: " + scmRootAddress;
        }
        else if ( state == ContinuumProjectState.ERROR )
        {
            message = "PREPARE BUILD ERROR: " + scmRootAddress;
        }
        else
        {
            log.warn( "Unknown prepare build state " + state + " for SCM root URL " + scmRootAddress );

            message = "ERROR: Unknown prepare build state " + state + " for SCM root URL" + scmRootAddress;
        }

        try
        {
            return message + " " +
                getReportUrl( projectScmRoot.getProjectGroup(), projectScmRoot, configurationService );
        }
        catch ( ContinuumException e )
        {
            throw new NotificationException( "Cannot generate message", e );
        }
    }
}
