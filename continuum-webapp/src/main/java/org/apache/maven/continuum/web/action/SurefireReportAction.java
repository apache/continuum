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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.reports.surefire.ReportTest;
import org.apache.maven.continuum.reports.surefire.ReportTestSuite;
import org.apache.maven.continuum.reports.surefire.ReportTestSuiteGenerator;
import org.apache.maven.continuum.reports.surefire.ReportTestSuiteGeneratorException;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Edwin Punzalan
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="surefireReport"
 * @todo too many inner classes, maybe a continuum-reports project group ?
 */
public class SurefireReportAction
    extends ContinuumActionSupport
{
    /**
     * @plexus.requirement
     */    
    private ReportTestSuiteGenerator reportTestSuiteGenerator;
    
    private int buildId;

    private int projectId;

    private List testSuites;

    private List testSummaryList;

    private List testPackageList;

    private String projectName;

    private Project project;

    public String execute()
        throws ContinuumException, ConfigurationException, ReportTestSuiteGeneratorException
    {
        try
        {
            checkViewProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        project = getProjectById( projectId );

        testSuites = reportTestSuiteGenerator.generateReports( buildId, projectId );

        getSummary( testSuites );

        getDetails( testSuites );

        return SUCCESS;
    }

    private void getSummary( List suiteList )
    {
        int totalTests = 0;

        int totalErrors = 0;

        int totalFailures = 0;

        float totalTime = 0.0f;

        for ( Iterator suites = suiteList.iterator(); suites.hasNext(); )
        {

            ReportTestSuite suite = (ReportTestSuite) suites.next();

            totalTests += suite.getNumberOfTests();

            totalErrors += suite.getNumberOfErrors();

            totalFailures += suite.getNumberOfFailures();

            totalTime += suite.getTimeElapsed();
        }

        ReportTest report = new ReportTest();
        report.setTests( totalTests );
        report.setErrors( totalErrors );
        report.setFailures( totalFailures );
        report.setElapsedTime( totalTime );

        testSummaryList = Collections.singletonList( report );
    }

    private void getDetails( List<ReportTestSuite> suiteList )
    {
        Map testsByPackage = new LinkedHashMap();

        for ( ReportTestSuite suite : suiteList )
        {
            ReportTest report = (ReportTest) testsByPackage.get( suite.getPackageName() );

            if ( report == null )
            {
                report = new ReportTest();

                report.setId( suite.getPackageName() );

                report.setName( suite.getPackageName() );
            }

            report.setTests( report.getTests() + suite.getNumberOfTests() );
            report.setErrors( report.getErrors() + suite.getNumberOfErrors() );
            report.setFailures( report.getFailures() + suite.getNumberOfFailures() );
            report.setElapsedTime( report.getElapsedTime() + suite.getTimeElapsed() );

            ReportTest reportTest = new ReportTest();
            reportTest.setId( suite.getPackageName() + "." + suite.getName() );
            reportTest.setName( suite.getName() );
            reportTest.setTests( suite.getNumberOfTests() );
            reportTest.setErrors( suite.getNumberOfErrors() );
            reportTest.setFailures( suite.getNumberOfFailures() );
            reportTest.setElapsedTime( suite.getTimeElapsed() );
            reportTest.setChildren( suite.getTestCases() );

            report.getChildren().add( reportTest );

            testsByPackage.put( suite.getPackageName(), report );
        }

        testPackageList = new ArrayList( testsByPackage.values() );
    }

    public int getBuildId()
    {
        return buildId;
    }

    public void setBuildId( int buildId )
    {
        this.buildId = buildId;
    }

    public Project getProject()
    {
        return project;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    private String[] getIncludedFiles( File directory, String includes, String excludes )
    {
        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir( directory );

        scanner.setIncludes( StringUtils.split( includes, "," ) );

        scanner.setExcludes( StringUtils.split( excludes, "," ) );

        scanner.scan();

        return scanner.getIncludedFiles();
    }

    public List getTestSuites()
    {
        return testSuites;
    }

    public void setTestSuites( List testSuites )
    {
        this.testSuites = testSuites;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }

    public List getTestSummaryList()
    {
        return testSummaryList;
    }

    public void setTestSummaryList( List testSummaryList )
    {
        this.testSummaryList = testSummaryList;
    }

    public List getTestPackageList()
    {
        return testPackageList;
    }

    public void setTestPackageList( List testPackageList )
    {
        this.testPackageList = testPackageList;
    }

    public Project getProjectById( int projectId )
        throws ContinuumException
    {
        return getContinuum().getProject( projectId );
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        return getProjectById( projectId ).getProjectGroup().getName();
    }

}
