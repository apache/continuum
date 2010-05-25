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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.web.model.ProjectBuildsSummary;

/**
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="projectBuildsReport"
 */
public class ViewBuildsReportAction
    extends ContinuumActionSupport
{
    private int buildStatus;

    private String triggeredBy = "";

    private String startDate = "";

    private String endDate = "";

    private int rowCount = 30;

    private int page = 1;

    private int numPages;

    private Map<Integer, String> buildStatuses;

    private List<ProjectBuildsSummary> projectBuilds;

    private static final String[] datePatterns =
        new String[]{"MM/dd/yy", "MM/dd/yyyy", "MMMMM/dd/yyyy", "MMMMM/dd/yy", "dd MMMMM yyyy", "dd/MM/yy",
            "dd/MM/yyyy", "yyyy/MM/dd", "yyyy-MM-dd", "yyyy-dd-MM", "MM-dd-yyyy", "MM-dd-yy"};

    public void prepare()
        throws Exception
    {
        super.prepare();

        buildStatuses = new LinkedHashMap<Integer, String>();
        buildStatuses.put( 0, "ALL" );
        buildStatuses.put( ContinuumProjectState.OK, "Ok" );
        buildStatuses.put( ContinuumProjectState.FAILED, "Failed" );
        buildStatuses.put( ContinuumProjectState.ERROR, "Error" );    
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

        long fromDate = 0;
        long toDate = 0;

        try
        {
            if ( !StringUtils.isEmpty( startDate ) )
            {
                fromDate = DateUtils.parseDate( startDate, datePatterns ).getTime();
            }
    
            if ( !StringUtils.isEmpty( endDate ) )
            {
                toDate = DateUtils.parseDate( endDate, datePatterns ).getTime();
            }
        }
        catch ( ParseException e )
        {
            addActionError( "Error parsing date(s): " + e.getMessage() );
            return ERROR;
        }

        if ( fromDate != 0 && toDate != 0 && new Date( fromDate ).after( new Date( toDate ) ) )
        {
            addFieldError( "startDate", "Start Date must be earlier than the End Date" );
            return INPUT;
        }

        if ( rowCount < 10 )
        {
            // TODO: move to validation framework
            addFieldError( "rowCount", "Row count must be larger than 10." );
            return INPUT;
        }

        List<BuildResult> buildResults = getContinuum().getBuildResultsInRange( fromDate, toDate, buildStatus, triggeredBy );

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
            int end = ( start + rowCount ) - 1;
    
            if ( end > projectBuilds.size() )
            {
                end = projectBuilds.size() - 1;
            }

            projectBuilds = projectBuilds.subList( start, end + 1 );
        }

        return SUCCESS;
    }

    private List<ProjectBuildsSummary> mapBuildResultsToProjectBuildsSummaries( List<BuildResult> buildResults )
    {
        List<ProjectBuildsSummary> buildsSummary = new ArrayList<ProjectBuildsSummary>();

        for( BuildResult buildResult : buildResults )
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

    public int getBuildStatus()
    {
        return this.buildStatus;
    }

    public void setBuildStatus( int buildStatus )
    {
        this.buildStatus = buildStatus;
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
