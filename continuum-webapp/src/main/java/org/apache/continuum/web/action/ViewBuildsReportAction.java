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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component( role = com.opensymphony.xwork2.Action.class, hint = "projectBuildsReport", instantiationStrategy = "per-lookup" )
public class ViewBuildsReportAction
    extends ContinuumActionSupport
    implements ServletResponseAware
{
    private static final Logger log = LoggerFactory.getLogger( ViewBuildsReportAction.class );

    private static final int MAX_BROWSE_SIZE = 500;

    private static final int EXPORT_BATCH_SIZE = 4000;

    private static final int MAX_EXPORT_SIZE = 100000;

    private static final String[] datePatterns =
        new String[] { "MM/dd/yy", "MM/dd/yyyy", "MMMMM/dd/yyyy", "MMMMM/dd/yy", "dd MMMMM yyyy", "dd/MM/yy",
            "dd/MM/yyyy", "yyyy/MM/dd", "yyyy-MM-dd", "yyyy-dd-MM", "MM-dd-yyyy", "MM-dd-yy" };

    /**
     * Encapsulates constants relevant for build results and makes them localizable.
     */
    public enum ResultState
    {
        OK( ContinuumProjectState.OK, "projectBuilds.report.resultOk" ),
        FAILED( ContinuumProjectState.FAILED, "projectBuilds.report.resultFailed" ),
        ERROR( ContinuumProjectState.ERROR, "projectBuilds.report.resultError" ),
        BUILDING( ContinuumProjectState.BUILDING, "projectBuilds.report.resultBuilding" ),
        CANCELLED( ContinuumProjectState.CANCELLED, "projectBuilds.report.resultCanceled" );

        private static final Map<Integer, ResultState> dataMap;

        static
        {
            dataMap = new HashMap<Integer, ResultState>();
            for ( ResultState val : ResultState.values() )
            {
                dataMap.put( val.dataId, val );
            }
        }

        private int dataId;

        private String textKey;

        ResultState( int dataId, String textKey )
        {
            this.dataId = dataId;
            this.textKey = textKey;
        }

        public int getDataId()
        {
            return dataId;
        }

        public String getTextKey()
        {
            return textKey;
        }

        public static ResultState fromId( int state )
        {
            return dataMap.get( state );
        }

        public static boolean knownState( int state )
        {
            return dataMap.containsKey( state );
        }
    }

    private int buildStatus;

    private String triggeredBy = "";

    private String startDate = "";

    private String endDate = "";

    private int projectGroupId;

    private int rowCount = 25;

    private int page = 1;

    private int pageTotal;

    private Map<Integer, String> buildStatuses;

    private Map<Integer, String> projectGroups;

    private Map<String, Integer> permittedGroups;

    private List<BuildResult> filteredResults = new ArrayList<BuildResult>();

    private HttpServletResponse rawResponse;

    public void setServletResponse( HttpServletResponse response )
    {
        this.rawResponse = response;
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

    public void prepare()
        throws Exception
    {
        super.prepare();

        // Populate the state drop downs
        buildStatuses = new LinkedHashMap<Integer, String>();
        buildStatuses.put( 0, "ALL" );
        for ( ResultState state : ResultState.values() )
        {
            buildStatuses.put( state.getDataId(), getText( state.getTextKey() ) );
        }

        permittedGroups = new HashMap<String, Integer>();
        projectGroups = new LinkedHashMap<Integer, String>();
        projectGroups.put( 0, "ALL" );

        // TODO: Use these to limit results at the data layer
        List<ProjectGroup> groups = getContinuum().getAllProjectGroups();
        if ( groups != null )
        {
            for ( ProjectGroup group : groups )
            {
                String groupName = group.getName();
                if ( isAuthorized( groupName ) )
                {
                    projectGroups.put( group.getId(), groupName );
                    permittedGroups.put( groupName, group.getId() );
                }
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

        if ( permittedGroups.isEmpty() )
        {
            addActionError( getText( "projectBuilds.report.noGroupsAuthorized" ) );
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

        if ( permittedGroups.isEmpty() )
        {
            addActionError( getText( "projectBuilds.report.noGroupsAuthorized" ) );
            return REQUIRES_AUTHORIZATION;
        }

        Date fromDate;
        Date toDate;

        try
        {
            fromDate = getStartDateInDateFormat();
            toDate = getEndDateInDateFormat();
        }
        catch ( ParseException e )
        {
            addActionError( getText( "projectBuilds.report.badDates", new String[] { e.getMessage() } ) );
            return INPUT;
        }

        if ( fromDate != null && toDate != null && fromDate.after( toDate ) )
        {
            addFieldError( "startDate", getText( "projectBuilds.report.endBeforeStartDate" ) );
            return INPUT;
        }

        // Limit query to scan only what the user is permitted to see
        Collection<Integer> groupIds = new HashSet<Integer>();
        if ( projectGroupId > 0 )
        {
            groupIds.add( projectGroupId );
        }
        else
        {
            groupIds.addAll( permittedGroups.values() );
        }

        // Users can preview a limited number of records (use export for more)
        int offset = 0;
        List<BuildResult> results;
        populating:
        do
        {

            // Fetch a batch of records (may be filtered based on permissions)
            results = getContinuum().getBuildResultsInRange( groupIds, fromDate, toDate, buildStatus, triggeredBy,
                                                             offset, MAX_BROWSE_SIZE );
            offset += MAX_BROWSE_SIZE;

            for ( BuildResult result : results )
            {
                if ( permittedGroups.containsKey( result.getProject().getProjectGroup().getName() ) )
                {
                    filteredResults.add( result );
                }

                if ( filteredResults.size() >= MAX_BROWSE_SIZE )
                {
                    break populating;  // Halt when we have filled a batch equivalent with results
                }
            }
        }
        while ( results.size() == MAX_BROWSE_SIZE );  // Keep fetching until batch is empty or incomplete

        if ( filteredResults.size() == MAX_BROWSE_SIZE )
        {
            addActionMessage( getText( "projectBuilds.report.limitedResults" ) );
        }

        int resultSize = filteredResults.size();
        pageTotal = resultSize / rowCount + ( resultSize % rowCount == 0 ? 0 : 1 );

        if ( resultSize > 0 && ( page < 1 || page > pageTotal ) )
        {
            addActionError( getText( "projectBuilds.report.invalidPage" ) );
            return INPUT;
        }

        int pageStart = rowCount * ( page - 1 ), pageEnd = rowCount * page;

        // Restrict results to just the page we will show
        filteredResults = filteredResults.subList( pageStart, pageEnd > resultSize ? resultSize : pageEnd );

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

        if ( permittedGroups.isEmpty() )
        {
            addActionError( getText( "projectBuilds.report.noGroupsAuthorized" ) );
            return REQUIRES_AUTHORIZATION;
        }

        Date fromDate;
        Date toDate;

        try
        {
            fromDate = getStartDateInDateFormat();
            toDate = getEndDateInDateFormat();
        }
        catch ( ParseException e )
        {
            addActionError( getText( "projectBuilds.report.badDates", new String[] { e.getMessage() } ) );
            return INPUT;
        }

        if ( fromDate != null && toDate != null && fromDate.after( toDate ) )
        {
            addFieldError( "startDate", getText( "projectBuilds.report.endBeforeStartDate" ) );
            return INPUT;
        }

        try
        {
            // First, build the output file
            rawResponse.setContentType( "text/csv" );
            rawResponse.addHeader( "Content-disposition", "attachment;filename=continuum_project_builds_report.csv" );
            Writer output = rawResponse.getWriter();

            DateFormat dateTimeFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ" );

            try
            {
                // Write the header
                output.append( "Group,Project,ID,Build#,Started,Duration,Triggered By,Status\n" );

                // Limit query to scan only what the user is permitted to see
                Collection<Integer> groupIds = new HashSet<Integer>();
                if ( projectGroupId > 0 )
                {
                    groupIds.add( projectGroupId );
                }
                else
                {
                    groupIds.addAll( permittedGroups.values() );
                }

                // Build the output file by walking through the results in batches
                int offset = 0, exported = 0;
                List<BuildResult> results;
                export:
                do
                {
                    results = getContinuum().getBuildResultsInRange( groupIds, fromDate, toDate, buildStatus,
                                                                     triggeredBy, offset, EXPORT_BATCH_SIZE );

                    offset += EXPORT_BATCH_SIZE;  // Ensure we advance through results

                    // Convert each build result to a line in the CSV file
                    for ( BuildResult result : results )
                    {

                        if ( !permittedGroups.containsKey( result.getProject().getProjectGroup().getName() ) )
                        {
                            continue;
                        }

                        exported += 1;

                        Project project = result.getProject();
                        ProjectGroup projectGroup = project.getProjectGroup();

                        int resultState = result.getState();
                        String stateName = ResultState.knownState( resultState ) ?
                            getText( ResultState.fromId( resultState ).getTextKey() ) :
                            getText( "projectBuilds.report.resultUnknown" );

                        String buildTime = dateTimeFormat.format( new Date( result.getStartTime() ) );
                        long buildDuration = ( result.getEndTime() - result.getStartTime() ) / 1000;

                        String formattedLine = String.format( "%s,%s,%s,%s,%s,%s,%s,%s\n",
                                                              projectGroup.getName(),
                                                              project.getName(),
                                                              result.getId(),
                                                              result.getBuildNumber(),
                                                              buildTime,
                                                              buildDuration,
                                                              result.getUsername(),
                                                              stateName );
                        output.append( formattedLine );

                        if ( exported >= MAX_EXPORT_SIZE )
                        {
                            log.warn( "build report export hit limit of {} records", MAX_EXPORT_SIZE );
                            break export;
                        }
                    }
                }
                while ( results.size() == EXPORT_BATCH_SIZE );
            }
            finally
            {
                output.flush();
            }
        }
        catch ( IOException e )
        {
            addActionError( getText( "projectBuilds.report.exportIOError", new String[] { e.getMessage() } ) );
            return INPUT;
        }

        return null;
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

    public Map<Integer, String> getBuildStatuses()
    {
        return buildStatuses;
    }

    public int getPage()
    {
        return page;
    }

    public void setPage( int page )
    {
        this.page = page;
    }

    public int getPageTotal()
    {
        return pageTotal;
    }

    public List<BuildResult> getFilteredResults()
    {
        return filteredResults;
    }

    public Map<Integer, String> getProjectGroups()
    {
        return projectGroups;
    }
}