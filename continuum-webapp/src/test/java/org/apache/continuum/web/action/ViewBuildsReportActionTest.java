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

import com.opensymphony.xwork2.Action;
import org.apache.continuum.web.action.stub.ViewBuildsReportActionStub;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.codehaus.plexus.redback.system.SecuritySession;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ViewBuildsReportActionTest
    extends AbstractActionTest
{
    private ViewBuildsReportActionStub action;

    private Continuum continuum;

    private List<BuildResult> buildResults = new ArrayList<BuildResult>();

    private ProjectGroup allowedGroup;

    @Before
    public void setUp()
        throws Exception
    {
        continuum = mock( Continuum.class );

        action = new ViewBuildsReportActionStub();
        action.setSecuritySession( mock( SecuritySession.class ) );
        action.setContinuum( continuum );

        allowedGroup = new ProjectGroup();
        allowedGroup.setId( 1 );
        allowedGroup.setName( "Allowed Group" );
        action.setAuthorizedGroups( Arrays.asList( allowedGroup ) );

        action.prepare();
    }

    @Test
    public void testEndDateBeforeStartDate()
    {
        action.setStartDate( "04/25/2010" );
        action.setEndDate( "04/24/2010" );
        String result = action.execute();

        assertEquals( Action.INPUT, result );
        assertTrue( action.hasFieldErrors() );
        assertFalse( action.hasActionErrors() );
    }

    @Test
    public void testMalformedStartDate()
    {
        action.setStartDate( "not a date" );
        String result = action.execute();

        assertEquals( Action.INPUT, result );
        assertTrue( action.hasActionErrors() );
        assertFalse( action.hasFieldErrors() );
    }

    @Test
    public void testMalformedEndDate()
    {
        action.setEndDate( "not a date" );
        String result = action.execute();

        assertEquals( Action.INPUT, result );
        assertTrue( action.hasActionErrors() );
        assertFalse( action.hasFieldErrors() );
    }

    @Test
    public void testStartDateSameWithEndDate()
    {
        when( continuum.getBuildResultsInRange( anyCollection(), any( Date.class ), any( Date.class ), anyInt(),
                                                anyString(), anyInt(), anyInt() ) ).thenReturn( buildResults );

        action.setStartDate( "04/25/2010" );
        action.setEndDate( "04/25/2010" );
        String result = action.execute();

        assertSuccessResult( result );
    }

    @Test
    public void testEndDateWithNoStartDate()
    {
        when( continuum.getBuildResultsInRange( anyCollection(), any( Date.class ), any( Date.class ), anyInt(),
                                                anyString(), anyInt(), anyInt() ) ).thenReturn( buildResults );
        action.setEndDate( "04/25/2010" );
        String result = action.execute();

        assertSuccessResult( result );
    }

    @Test
    public void testExportToCsv()
        throws Exception
    {
        Calendar cal = Calendar.getInstance();
        cal.set( 2010, 1, 1, 1, 1, 1 );

        List<BuildResult> results = createBuildResult( cal.getTimeInMillis() );

        when( continuum.getBuildResultsInRange( anyCollection(), any( Date.class ), any( Date.class ), anyInt(),
                                                anyString(), anyInt(), anyInt() ) ).thenReturn( results );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpServletResponse response = mock( HttpServletResponse.class );
        when( response.getWriter() ).thenReturn( new PrintWriter( out ) );

        action.setServletResponse( response );
        action.setProjectGroupId( 0 );
        action.setBuildStatus( 0 );
        action.setStartDate( "" );
        action.setEndDate( "" );
        action.setTriggeredBy( "" );

        String result = action.downloadBuildsReport();

        assertNull( "result should be null", result );
        assertExportContents( results, out.toString() );
    }

    private void assertSuccessResult( String result )
    {
        assertEquals( Action.SUCCESS, result );
        assertFalse( action.hasFieldErrors() );
        assertFalse( action.hasActionErrors() );
    }

    private List<BuildResult> createBuildResult( long timeInMillis )
    {
        List<BuildResult> results = new ArrayList<BuildResult>();

        BuildResult result = new BuildResult();

        Project project = new Project();
        project.setName( "Test Project" );
        project.setProjectGroup( allowedGroup );

        result.setProject( project );
        result.setState( 2 );
        result.setStartTime( timeInMillis );
        result.setUsername( "test-admin" );

        results.add( result );

        return results;
    }

    private void assertExportContents( List<BuildResult> results, String actualContents )
    {
        DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ" );
        String expectedContents = "Group,Project,ID,Build#,Started,Duration,Triggered By,Status\n";
        for ( BuildResult result : results )
        {
            Project p = result.getProject();
            ProjectGroup pg = p.getProjectGroup();
            expectedContents += String.format( "%s,%s,%s,%s,%s,%s,%s,%s\n",
                                               pg.getName(), p.getName(), result.getId(), result.getBuildNumber(),
                                               dateFormat.format( new Date( result.getStartTime() ) ),
                                               ( result.getEndTime() - result.getStartTime() ) / 1000,
                                               result.getUsername(),
                                               ViewBuildsReportAction.ResultState.fromId(
                                                   result.getState() ).getTextKey() );
        }
        assertEquals( expectedContents, actualContents );
    }
}
