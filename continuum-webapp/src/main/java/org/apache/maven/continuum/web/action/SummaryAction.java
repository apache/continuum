package org.apache.maven.continuum.web.action;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.web.model.GroupSummary;
import org.apache.maven.continuum.web.model.ProjectSummary;

/**
 * Used to render the list of projects in the project group page.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="summary"
 */
public class SummaryAction
    extends ContinuumActionSupport
{
    private int projectGroupId;

    private String projectGroupName;

    private List summary;

    private GroupSummary groupSummary = new GroupSummary();

    public String execute()
        throws ContinuumException
    {
        try
        {
            checkViewProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        Collection projectsInGroup;

        //TODO: Create a summary jpox request so code will be more simple and performance will be better
        projectsInGroup = getContinuum().getProjectsInGroup( projectGroupId );
        
        Map buildResults = getContinuum().getLatestBuildResults( projectGroupId );

        Map buildResultsInSuccess = getContinuum().getBuildResultsInSuccess( projectGroupId );

        summary = new ArrayList<ProjectSummary>();

        groupSummary.setNumErrors( 0 );
        groupSummary.setNumFailures( 0 );
        groupSummary.setNumSuccesses( 0 );
        groupSummary.setNumProjects( 0 );

        for ( Iterator i = projectsInGroup.iterator(); i.hasNext(); )
        {
            Project project = (Project) i.next();

            groupSummary.setNumProjects( groupSummary.getNumProjects() + 1 );

            ProjectSummary model = new ProjectSummary();

            model.setId( project.getId() );

            model.setName( project.getName() );

            model.setVersion( project.getVersion() );

            model.setProjectGroupId( project.getProjectGroup().getId() );

            model.setProjectGroupName( project.getProjectGroup().getName() );

            if ( getContinuum().isInBuildingQueue( project.getId() ) )
            {
                model.setInBuildingQueue( true );
            }
            else if ( getContinuum().isInCheckoutQueue( project.getId() ) )
            {
                model.setInCheckoutQueue( true );
            }
            else
            {
                model.setInBuildingQueue( false );
                model.setInCheckoutQueue( false );
            }

            model.setState( project.getState() );

            model.setBuildNumber( project.getBuildNumber() );

            if ( buildResultsInSuccess != null )
            {
                BuildResult buildInSuccess = (BuildResult) buildResultsInSuccess.get( new Integer( project.getId() ) );

                if ( buildInSuccess != null )
                {
                    model.setBuildInSuccessId( buildInSuccess.getId() );
                }
            }

            if ( buildResults != null )
            {
                BuildResult latestBuild = (BuildResult) buildResults.get( new Integer( project.getId() ) );

                if ( latestBuild != null )
                {
                    model.setLatestBuildId( latestBuild.getId() );
                    populateGroupSummary( latestBuild );
                    model.setLastBuildDateTime( dateFormatter.format( new Date( latestBuild.getEndTime() ) ) );
                }
            }

            summary.add( model );
        }

        Comparator<ProjectSummary> projectComparator = new Comparator<ProjectSummary>()
        {
            public int compare( ProjectSummary ps1, ProjectSummary ps2 )
            {
                return ps1.getName().compareTo( ps2.getName() );
            }
        };

        Collections.sort( summary, projectComparator );

        return SUCCESS;
    }

    private void populateGroupSummary( BuildResult latestBuild )
    {
        switch ( latestBuild.getState() )
        {
            case ContinuumProjectState.ERROR:
                groupSummary.setNumErrors( groupSummary.getNumErrors() + 1 );
                break;
            case ContinuumProjectState.OK:
                groupSummary.setNumSuccesses( groupSummary.getNumSuccesses() + 1 );
                break;
            case ContinuumProjectState.FAILED:
                groupSummary.setNumFailures( groupSummary.getNumFailures() + 1 );
                break;
            default:
                if ( latestBuild.getState() == 5 || latestBuild.getState() > 10 )
                {
                    getLogger().warn(
                        "unknown buildState value " + latestBuild.getState() + " with build " + latestBuild.getId() );
                }
        }
    }

    public List getProjects()
    {
        return summary;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }


    public String getProjectGroupName()
    {
        return projectGroupName;
    }

    public void setProjectGroupName( String projectGroupName )
    {
        this.projectGroupName = projectGroupName;
    }

    public GroupSummary getGroupSummary()
    {
        return groupSummary;
    }

    public void setGroupSummary( GroupSummary groupSummary )
    {
        this.groupSummary = groupSummary;
    }
}
