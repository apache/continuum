package org.apache.continuum.web.action;

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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.web.model.ProjectBuildsSummary;
import org.codehaus.plexus.component.annotations.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component( role = com.opensymphony.xwork2.Action.class, hint = "projectBuildsReport", instantiationStrategy = "per-lookup" )
public class ViewBuildsReportAction
    extends ContinuumActionSupport
{
    private int buildStatus;

    private String triggeredBy = "";

    private String startDate = "";

    private String endDate = "";

    private int projectGroupId;

    private int rowCount = 30;

    private int page = 1;

    private int numPages;

    private Map<Integer, String> buildStatuses;

    private Map<Integer, String> projectGroups;

    private List<ProjectBuildsSummary> projectBuilds;

    private InputStream inputStream;

    public static final String SEND_FILE = "send-file";

    private static final String[] datePatterns =
        new String[] { "MM/dd/yy", "MM/dd/yyyy", "MMMMM/dd/yyyy", "MMMMM/dd/yy", "dd MMMMM yyyy", "dd/MM/yy",
            "dd/MM/yyyy", "yyyy/MM/dd", "yyyy-MM-dd", "yyyy-dd-MM", "MM-dd-yyyy", "MM-dd-yy" };

    public void prepare()
        throws Exception
    {
        super.prepare();

        buildStatuses = new LinkedHashMap<Integer, String>();
        buildStatuses.put( 0, "ALL" );
        buildStatuses.put( ContinuumProjectState.OK, "Ok" );
        buildStatuses.put( ContinuumProjectState.FAILED, "Failed" );
        buildStatuses.put( ContinuumProjectState.ERROR, "Error" );

        projectGroups = new LinkedHashMap<Integer, String>();
        projectGroups.put( 0, "ALL" );

        List<ProjectGroup> groups = getContinuum().getAllProjectGroups();
        if ( groups != null )
        {
            for ( ProjectGroup group : groups )
            {
                projectGroups.put( group.getId(), group.getName() );
            }
        }
    }

    public String init()
    {
        try
        {
            checkViewReportsAuthorization();
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        // action class was called from the Menu; do not generate report first
        return SUCCESS;
    }

    public String execute()
    {
        try
        {
            checkViewReportsAuthorization();
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        Date fromDate = null;
        Date toDate = null;

        try
        {
            fromDate = getStartDateInDateFormat();
            toDate = getEndDateInDateFormat();
        }
        catch ( ParseException e )
        {
            addActionError( "Error parsing date(s): " + e.getMessage() );
            return ERROR;
        }

        if ( fromDate != null && toDate != null && fromDate.after( toDate ) )
        {
            addFieldError( "startDate", "Start Date must be earlier than the End Date" );
            return INPUT;
        }

        if ( rowCount < 10 )
        {
            // TODO: move to validation framework
            addFieldError( "rowCount", "Row count should be at least 10." );
            return INPUT;
        }

        List<BuildResult> buildResults = getContinuum().getBuildResultsInRange( projectGroupId, fromDate, toDate,
                                                                                buildStatus, triggeredBy );
        projectBuilds = Collections.emptyList();

        if ( buildResults != null && !buildResults.isEmpty() )
        {
            projectBuilds = mapBuildResultsToProjectBuildsSummaries( buildResults );

            int extraPage = ( projectBuilds.size() % rowCount ) != 0 ? 1 : 0;
            numPages = ( projectBuilds.size() / rowCount ) + extraPage;

            if ( page > numPages )
            {
                addActionError(
                    "Error encountered while generating project builds report :: The requested page exceeds the total number of pages." );
                return ERROR;
            }

            int start = rowCount * ( page - 1 );
            int end = ( start + rowCount );

            if ( end > projectBuilds.size() )
            {
                end = projectBuilds.size();
            }

            projectBuilds = projectBuilds.subList( start, end );
        }

        return SUCCESS;
    }

    /*
    * Export Builds Report to .csv
    */
    public String downloadBuildsReport()
    {
        try
        {
            checkViewReportsAuthorization();
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        Date fromDate = null;
        Date toDate = null;

        try
        {
            fromDate = getStartDateInDateFormat();
            toDate = getEndDateInDateFormat();
        }
        catch ( ParseException e )
        {
            addActionError( "Error parsing date(s): " + e.getMessage() );
            return ERROR;
        }

        if ( fromDate != null && toDate != null && fromDate.after( toDate ) )
        {
            addFieldError( "startDate", "Start Date must be earlier than the End Date" );
            return INPUT;
        }

        List<BuildResult> buildResults = getContinuum().getBuildResultsInRange( projectGroupId, fromDate, toDate,
                                                                                buildStatus, triggeredBy );
        List<ProjectBuildsSummary> builds = Collections.emptyList();

        StringBuffer input = new StringBuffer( "Project Group,Project Name,Build Date,Triggered By,Build Status\n" );

        if ( buildResults != null && !buildResults.isEmpty() )
        {
            builds = mapBuildResultsToProjectBuildsSummaries( buildResults );

            for ( ProjectBuildsSummary build : builds )
            {
                input.append( build.getProjectGroupName() ).append( "," );
                input.append( build.getProjectName() ).append( "," );

                input.append( new Date( build.getBuildDate() ) ).append( "," );

                input.append( build.getBuildTriggeredBy() ).append( "," );

                String status;
                switch ( build.getBuildState() )
                {
                    case 2:
                        status = "Ok";
                        break;
                    case 3:
                        status = "Failed";
                        break;
                    case 4:
                        status = "Error";
                        break;
                    case 6:
                        status = "Building";
                        break;
                    case 7:
                        status = "Checking Out";
                        break;
                    case 8:
                        status = "Updating";
                        break;
                    default:
                        status = "";
                }
                input.append( status );
                input.append( "\n" );
            }
        }

        StringReader reader = new StringReader( input.toString() );

        try
        {
            inputStream = new ByteArrayInputStream( IOUtils.toByteArray( reader ) );
        }
        catch ( IOException e )
        {
            addActionError( "Error occurred while generating CSV file." );
            return ERROR;
        }

        return SEND_FILE;
    }

    private List<ProjectBuildsSummary> mapBuildResultsToProjectBuildsSummaries( List<BuildResult> buildResults )
    {
        List<ProjectBuildsSummary> buildsSummary = new ArrayList<ProjectBuildsSummary>();

        for ( BuildResult buildResult : buildResults )
        {
            Project project = buildResult.getProject();

            // check if user is authorised to view build result
            if ( !isAuthorized( project.getProjectGroup().getName() ) )
            {
                continue;
            }

            ProjectBuildsSummary summary = new ProjectBuildsSummary();
            summary.setProjectGroupName( project.getProjectGroup().getName() );
            summary.setProjectName( project.getName() );
            summary.setBuildDate( buildResult.getStartTime() );
            summary.setBuildState( buildResult.getState() );
            summary.setBuildTriggeredBy( buildResult.getUsername() );

            buildsSummary.add( summary );
        }

        return buildsSummary;
    }

    private Date getStartDateInDateFormat()
        throws ParseException
    {
        Date date = null;

        if ( !StringUtils.isEmpty( startDate ) )
        {
            date = DateUtils.parseDate( startDate, datePatterns );
        }

        return date;
    }

    private Date getEndDateInDateFormat()
        throws ParseException
    {
        Date date = null;

        if ( !StringUtils.isEmpty( endDate ) )
        {
            date = DateUtils.parseDate( endDate, datePatterns );
        }

        return date;
    }

    public int getBuildStatus()
    {
        return this.buildStatus;
    }

    public void setBuildStatus( int buildStatus )
    {
        this.buildStatus = buildStatus;
    }

    public int getProjectGroupId()
    {
        return this.projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public String getTriggeredBy()
    {
        return this.triggeredBy;
    }

    public void setTriggeredBy( String triggeredBy )
    {
        this.triggeredBy = triggeredBy;
    }

    public String getStartDate()
    {
        return this.startDate;
    }

    public void setStartDate( String startDate )
    {
        this.startDate = startDate;
    }

    public String getEndDate()
    {
        return this.endDate;
    }

    public void setEndDate( String endDate )
    {
        this.endDate = endDate;
    }

    public int getRowCount()
    {
        return rowCount;
    }

    public void setRowCount( int rowCount )
    {
        this.rowCount = rowCount;
    }

    public Map<Integer, String> getBuildStatuses()
    {
        return buildStatuses;
    }

    public Map<Integer, String> getProjectGroups()
    {
        return projectGroups;
    }

    public List<ProjectBuildsSummary> getProjectBuilds()
    {
        return projectBuilds;
    }

    public int getPage()
    {
        return page;
    }

    public void setPage( int page )
    {
        this.page = page;
    }

    public int getNumPages()
    {
        return numPages;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    private boolean isAuthorized( String projectGroupName )
    {
        try
        {
            checkViewProjectGroupAuthorization( projectGroupName );
            return true;
        }
        catch ( AuthorizationRequiredException authzE )
        {
            return false;
        }
    }
}
