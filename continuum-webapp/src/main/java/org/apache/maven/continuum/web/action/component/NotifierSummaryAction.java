/**
 *
 */
package org.apache.maven.continuum.web.action.component;

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

import org.apache.continuum.web.util.GenerateRecipentNotifier;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.web.model.NotifierSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Component Action that prepares and provides Project Group Notifier and
 * Project Notifier summaries.
 *
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="notifierSummary"
 */
public class NotifierSummaryAction
    extends ContinuumActionSupport
{
    private static final Logger logger = LoggerFactory.getLogger( NotifierSummaryAction.class );

    /**
     * Identifier for the {@link ProjectGroup} for which the Notifier summary
     * needs to be prepared for.
     */
    private int projectGroupId;

    /**
     * Identifier for the {@link Project} for which the Notifier summary needs
     * to be prepared for.
     */
    private int projectId;

    /**
     * {@link ProjectGroup} instance to obtain the Notifier summary for.
     */
    private ProjectGroup projectGroup;

    private List<NotifierSummary> projectGroupNotifierSummaries = new ArrayList<NotifierSummary>();

    private List<NotifierSummary> projectNotifierSummaries = new ArrayList<NotifierSummary>();

    private String projectGroupName = "";

    /**
     * Prepare Notifier summary for a {@link Project}.
     *
     * @return
     */
    public String summarizeForProject()
    {
        logger.debug( "Obtaining summary for Project Id: " + projectId );

        try
        {
            checkViewProjectGroupAuthorization( getProjectGroupName() );

            projectNotifierSummaries = summarizeForProject( projectId );
        }
        catch ( ContinuumException e )
        {
            logger.error( "Unable to prepare Notifier summaries for Project Id: " + projectId, e );
            return ERROR;
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        return SUCCESS;
    }

    /**
     * Prepare Notifier summary for a {@link Project}.
     *
     * @param projectId The project id.
     * @return
     */
    private List<NotifierSummary> summarizeForProject( int projectId )
        throws ContinuumException
    {
        return gatherProjectNotifierSummaries( projectId );
    }

    /**
     * Prepare Notifier summary for a {@link ProjectGroup}.
     *
     * @return
     */
    public String summarizeForProjectGroup()
    {
        logger.debug( "Obtaining summary for ProjectGroup Id:" + projectGroupId );

        try
        {
            checkViewProjectGroupAuthorization( getProjectGroupName() );

            projectGroupNotifierSummaries = gatherGroupNotifierSummaries();

            Collection<Project> projects = getContinuum().getProjectsInGroup( projectGroupId );
            if ( projects != null )
            {
                for ( Project project : projects )
                {
                    projectNotifierSummaries.addAll( summarizeForProject( project.getId() ) );
                }
            }
        }
        catch ( ContinuumException e )
        {
            logger.error( "Unable to prepare Notifier summaries for ProjectGroup Id: " + projectGroupId, e );
            return ERROR;
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        return SUCCESS;
    }

    /**
     * Prepares and returns a list of Notifier summaries for the specified Project Id.
     *
     * @param projectId The project id.
     * @return List of {@link NotifierSummary} instance for the specified project.
     * @throws ContinuumException if there was an error obtaining
     *                            and preparing Notifier Summary list for the project
     */
    private List<NotifierSummary> gatherProjectNotifierSummaries( int projectId )
        throws ContinuumException
    {
        List<NotifierSummary> summaryList = new ArrayList<NotifierSummary>();
        Project project = getContinuum().getProjectWithAllDetails( projectId );

        for ( ProjectNotifier pn : (List<ProjectNotifier>) project.getNotifiers() )
        {
            NotifierSummary ns = generateProjectNotifierSummary( pn, project );
            summaryList.add( ns );
        }

        return summaryList;
    }

    /**
     * Prepares and returns {@link ProjectGroup} summaries for the specified project group Id.
     *
     * @return
     * @throws ContinuumException if there was an error fetching the {@link ProjectGroup} for specified Id.
     */
    private List<NotifierSummary> gatherGroupNotifierSummaries()
        throws ContinuumException
    {
        List<NotifierSummary> summaryList = new ArrayList<NotifierSummary>();
        projectGroup = getContinuum().getProjectGroupWithBuildDetails( projectGroupId );

        for ( ProjectNotifier pn : (List<ProjectNotifier>) projectGroup.getNotifiers() )
        {
            NotifierSummary ns = generateGroupNotifierSummary( pn );
            summaryList.add( ns );
        }

        return summaryList;
    }

    /**
     * Prepares a {@link NotifierSummary} from a {@link ProjectNotifier} instance.
     *
     * @param notifier
     * @return
     */
    private NotifierSummary generateProjectNotifierSummary( ProjectNotifier notifier, Project project )
    {
        return generateNotifierSummary( notifier, projectGroupId, project );
    }

    /**
     * Prepares a {@link NotifierSummary} from a {@link ProjectNotifier} instance.
     *
     * @param notifier
     * @return
     */
    private NotifierSummary generateGroupNotifierSummary( ProjectNotifier notifier )
    {
        return generateNotifierSummary( notifier, projectGroupId, null );
    }

    /**
     * Prepares a {@link NotifierSummary} from a {@link ProjectNotifier} instance.
     *
     * @param notifier
     * @return
     */
    private NotifierSummary generateNotifierSummary( ProjectNotifier notifier, int projectGroupId, Project project )
    {
        NotifierSummary ns = new NotifierSummary();
        ns.setId( notifier.getId() );
        ns.setType( notifier.getType() );
        ns.setProjectGroupId( projectGroupId );
        if ( project != null )
        {
            ns.setProjectId( project.getId() );
            ns.setProjectName( project.getName() );
        }

        if ( notifier.isFromProject() )
        {
            ns.setFromProject( true );
        }
        else
        {
            ns.setFromProject( false );
        }

        String recipient = GenerateRecipentNotifier.generate( notifier );

        ns.setRecipient( recipient );

        // XXX: Hack - just for testing :)
        StringBuffer sb = new StringBuffer();
        if ( notifier.isSendOnError() )
        {
            sb.append( "Error" );
        }
        if ( notifier.isSendOnFailure() )
        {
            if ( sb.length() > 0 )
            {
                sb.append( '/' );
            }
            sb.append( "Failure" );
        }
        if ( notifier.isSendOnSuccess() )
        {
            if ( sb.length() > 0 )
            {
                sb.append( '/' );
            }
            sb.append( "Success" );
        }
        if ( notifier.isSendOnWarning() )
        {
            if ( sb.length() > 0 )
            {
                sb.append( '/' );
            }
            sb.append( "Warning" );
        }
        if ( notifier.isSendOnScmFailure() )
        {
            if ( sb.length() > 0 )
            {
                sb.append( '/' );
            }
            sb.append( "SCM Failure" );
        }
        ns.setEvents( sb.toString() );

        ns.setEnabled( notifier.isEnabled() );
        return ns;
    }

    // property accessors

    /**
     * @return the projectGroupId
     */
    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    /**
     * @param projectGroupId the projectGroupId to set
     */
    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    /**
     * @return the projectId
     */
    public int getProjectId()
    {
        return projectId;
    }

    /**
     * @param projectId the projectId to set
     */
    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    /**
     * @return the projectGroup
     */
    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

    /**
     * @param projectGroup the projectGroup to set
     */
    public void setProjectGroup( ProjectGroup projectGroup )
    {
        this.projectGroup = projectGroup;
    }

    /**
     * @return the projectGroupNotifierSummaries
     */
    public List<NotifierSummary> getProjectGroupNotifierSummaries()
    {
        return projectGroupNotifierSummaries;
    }

    /**
     * @param projectGroupNotifierSummaries the projectGroupNotifierSummaries to set
     */
    public void setProjectGroupNotifierSummaries( List<NotifierSummary> projectGroupNotifierSummaries )
    {
        this.projectGroupNotifierSummaries = projectGroupNotifierSummaries;
    }

    /**
     * @return the projectNotifierSummaries
     */
    public List<NotifierSummary> getProjectNotifierSummaries()
    {
        return projectNotifierSummaries;
    }

    /**
     * @param projectNotifierSummaries the projectNotifierSummaries to set
     */
    public void setProjectNotifierSummaries( List<NotifierSummary> projectNotifierSummaries )
    {
        this.projectNotifierSummaries = projectNotifierSummaries;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( projectGroupName == null || "".equals( projectGroupName ) )
        {
            if ( projectGroupId != 0 )
            {
                projectGroupName = getContinuum().getProjectGroup( projectGroupId ).getName();
            }
            else
            {
                projectGroupName = getContinuum().getProjectGroupByProjectId( projectId ).getName();
            }
        }

        return projectGroupName;
    }
}
