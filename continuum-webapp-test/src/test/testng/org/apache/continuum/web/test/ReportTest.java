package org.apache.continuum.web.test;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.continuum.web.test.parent.AbstractAdminTest;
import org.testng.annotations.Test;

@Test( groups = {"report"} )
public class ReportTest
    extends AbstractAdminTest
{
    @Test( dependsOnMethods = {"testProjectGroupAllBuildSuccess"} )
    public void testViewBuildsReportWithSuccessfulBuild()
        throws Exception
    {
        goToProjectBuildsReport();
        selectValue( "buildStatus", "Ok" );
        clickButtonWithValue( "View Report" );

        assertProjectBuildReportWithResult();
        assertTextPresent( getProperty( "M2_PROJ_GRP_NAME" ) );
        assertImgWithAlt( "Success" );
    }

    /*@Test( dependsOnMethods = { "testProjectGroupAllBuildSuccess" } )
    public void testBuildsReportPagination()
        throws Exception
    {
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_PROJ_GRP_ID" );
        String M2_PROJ_GRP_DESCRIPTION = getProperty( "M2_PROJ_GRP_DESCRIPTION" );
        
        for ( int ctr = 0; ctr < 10; ctr++ )
        {
            buildProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION, M2_PROJ_GRP_NAME, true );
            clickButtonWithValue( "Release" );
            assertReleaseSuccess();
        }

        goToProjectBuildsReport();
        setFieldValue( "rowCount", "10" );
        clickButtonWithValue( "View Report" );

        assertProjectBuildReportWithResult();
        assertLinkNotPresent( "Prev" );
        assertLinkNotPresent( "1" );
        assertLinkPresent( "2" );
        assertLinkPresent( "Next" );

        clickLinkWithText( "2" );
        assertProjectBuildReportWithResult();
        assertLinkNotPresent( "Next" );
        assertLinkNotPresent( "2" );
        assertLinkPresent( "1" );
        assertLinkPresent( "Prev" );

        clickLinkWithText( "Prev" );
        assertProjectBuildReportWithResult();
        assertLinkNotPresent( "Prev" );
        assertLinkNotPresent( "1" );
        assertLinkPresent( "2" );
        assertLinkPresent( "Next" );
    }*/

    public void testBuildsReportWithInvalidRowCount()
    {
        goToProjectBuildsReport();
        setFieldValue( "rowCount", "1" );
        clickButtonWithValue( "View Report" );

        assertProjectBuildReportWithFieldError();
        assertTextPresent( "Row count should be at least 10." );
    }

    public void testBuildsReportWithInvalidDates()
    {
        goToProjectBuildsReport();
        setFieldValue( "startDate", "05/25/2010" );
        setFieldValue( "endDate", "05/24/2010" );
        clickButtonWithValue( "View Report" );

        assertProjectBuildReportWithFieldError();
        assertTextPresent( "Start Date must be earlier than the End Date" );
    }

    public void testViewBuildsReportWithFailedBuild()
        throws Exception
    {
        String M2_POM_URL = getProperty( "M2_FAILING_PROJ_POM_URL" );
        String M2_POM_USERNAME = getProperty( "M2_POM_USERNAME" );
        String M2_POM_PASSWORD = getProperty( "M2_POM_PASSWORD" );

        String M2_PROJ_GRP_NAME = getProperty( "M2_FAILING_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_FAILING_PROJ_GRP_ID" );
        String M2_PROJ_GRP_DESCRIPTION = getProperty( "M2_FAILING_PROJ_DESCRIPTION" );

        addMavenTwoProject( M2_POM_URL, M2_POM_USERNAME, M2_POM_PASSWORD, null, true );
        assertProjectGroupSummaryPage( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION );

        buildProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION, M2_PROJ_GRP_NAME, false );

        goToProjectBuildsReport();
        selectValue( "buildStatus", "Failed" );
        clickButtonWithValue( "View Report" );

        assertProjectBuildReportWithResult();
        assertImgWithAlt( "Failed" );
        assertTextPresent( M2_PROJ_GRP_NAME );
    }

    public void testViewBuildsReportWithErrorBuild()
    {
        goToProjectBuildsReport();
        selectValue( "buildStatus", "Error" );
        clickButtonWithValue( "View Report" );

        assertProjectBuildReportWithNoResult();
    }
}
