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
package org.apache.maven.continuum.reports.surefire;

import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @version $Id$
 * @since 12 nov. 07
 */
@Service( "reportTestSuiteGenerator" )
public class DefaultReportTestSuiteGenerator
    implements ReportTestSuiteGenerator, Initializable
{

    @Resource
    private ConfigurationService configurationService;

    private List<String> defaultIncludes;

    private List<String> defaultexcludes;

    // -----------------------------
    //  Plexus Lifecycle
    // -----------------------------

    public void initialize()
        throws InitializationException
    {
        defaultIncludes = new ArrayList<String>( 1 );
        defaultIncludes.add( "*.xml" );
        defaultexcludes = new ArrayList<String>( 1 );
        defaultexcludes.add( "*.txt" );
    }

    /**
     * @see org.apache.maven.continuum.reports.surefire.ReportTestSuiteGenerator#generateReports(java.io.File, java.util.List, java.util.List)
     */
    public List<ReportTestSuite> generateReports( File directory, List<String> includes, List<String> excludes )
        throws ReportTestSuiteGeneratorException
    {
        if ( directory == null )
        {
            return Collections.EMPTY_LIST;
        }
        if ( !directory.exists() )
        {
            return Collections.EMPTY_LIST;
        }
        List<ReportTestSuite> reportTestSuites = new LinkedList<ReportTestSuite>();
        String[] includesArray;
        if ( includes == null )
        {
            includesArray = new String[0];
        }
        else
        {
            includesArray = includes.toArray( new String[includes.size()] );
        }
        String[] excludesArray;
        if ( excludes == null )
        {
            excludesArray = new String[0];
        }
        else
        {
            excludesArray = excludes.toArray( new String[excludes.size()] );
        }
        String[] xmlReportFiles = getIncludedFiles( directory, includesArray, excludesArray );

        if ( xmlReportFiles == null )
        {
            return Collections.EMPTY_LIST;
        }
        if ( xmlReportFiles.length == 0 )
        {
            return Collections.EMPTY_LIST;
        }
        for ( String currentReport : xmlReportFiles )
        {
            ReportTestSuite testSuite = new ReportTestSuite();

            try
            {
                testSuite.parse( directory + File.separator + currentReport );
            }
            catch ( ParserConfigurationException e )
            {
                throw new ReportTestSuiteGeneratorException( "Error setting up parser for Surefire XML report", e );
            }
            catch ( SAXException e )
            {
                throw new ReportTestSuiteGeneratorException( "Error parsing Surefire XML report " + currentReport, e );
            }
            catch ( IOException e )
            {
                throw new ReportTestSuiteGeneratorException( "Error reading Surefire XML report " + currentReport, e );
            }

            reportTestSuites.add( testSuite );
        }
        return reportTestSuites;
    }

    /**
     * @see org.apache.maven.continuum.reports.surefire.ReportTestSuiteGenerator#generateReports(java.io.File)
     */
    public List<ReportTestSuite> generateReports( File directory )
        throws ReportTestSuiteGeneratorException
    {
        return generateReports( directory, defaultIncludes, defaultexcludes );
    }

    /**
     * @see org.apache.maven.continuum.reports.surefire.ReportTestSuiteGenerator#generateReports(int, int)
     */
    public List<ReportTestSuite> generateReports( int buildId, int projectId )
        throws ReportTestSuiteGeneratorException
    {
        try
        {
            File directory = configurationService.getTestReportsDirectory( buildId, projectId );
            return generateReports( directory );
        }
        catch ( ConfigurationException e )
        {
            throw new ReportTestSuiteGeneratorException( e.getMessage(), e );
        }
    }

    /**
     * @see org.apache.maven.continuum.reports.surefire.ReportTestSuiteGenerator#generateReportTestResult(int, int)
     */
    public ReportTestResult generateReportTestResult( int buildId, int projectId )
        throws ReportTestSuiteGeneratorException
    {
        List<ReportTestSuite> reportTestSuites = generateReports( buildId, projectId );
        ReportTestResult reportTestResult = new ReportTestResult();
        for ( ReportTestSuite reportTestSuite : reportTestSuites )
        {
            reportTestResult.addReportTestSuite( reportTestSuite );
        }
        return reportTestResult;
    }

    private String[] getIncludedFiles( File directory, String[] includes, String[] excludes )
    {
        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir( directory );

        scanner.setIncludes( includes );

        scanner.setExcludes( excludes );

        scanner.scan();

        return scanner.getIncludedFiles();
    }

}
