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

import org.codehaus.plexus.spring.PlexusInSpringTestCase;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @version $Id$
 * @since 12 nov. 07
 */
public class DefaultReportTestSuiteGeneratorTest
    extends PlexusInSpringTestCase
{

    private File getReportsDirectory( String pathDir )
    {
        return new File( getBasedir() + File.separatorChar + "src" + File.separatorChar + "test" + File.separatorChar +
                             "resources" + File.separatorChar + pathDir );
    }

    public void testSimpleFile()
        throws Exception
    {
        File testDirectory = getReportsDirectory( "simplereport" );

        ReportTestSuiteGenerator generator = (ReportTestSuiteGenerator) lookup( ReportTestSuiteGenerator.class,
                                                                                "default" );

        List<ReportTestSuite> reports = generator.generateReports( testDirectory );
        assertEquals( 1, reports.size() );

        ReportTestSuite report = reports.get( 0 );

        assertEquals( "AppTest", report.getName() );

        assertEquals( 1, report.getNumberOfTests() );
    }

    public void testContinuumCore()
        throws Exception
    {
        ReportTestSuiteGenerator generator = (ReportTestSuiteGenerator) lookup( ReportTestSuiteGenerator.class,
                                                                                "default" );
        List<ReportTestSuite> reports = generator.generateReports( 1, 1 );

        assertEquals( 18, reports.size() );

        for ( ReportTestSuite report : reports )
        {
            if ( report.getName().equals( "MailContinuumNotifierTest" ) && report.getPackageName().equals(
                "org.apache.maven.continuum.notification.mail" ) )
            {
                assertEquals( 1, report.getNumberOfFailures() );
                // don't test this because can plate forme dependant
                //assertEquals( 11.578, report.getTimeElapsed() );
                assertEquals( 3, report.getNumberOfTests() );

                for ( ReportTestCase testCase : report.getTestCases() )
                {
                    if ( testCase.getName().equals( "testSuccessfulBuild" ) )
                    {
                        assertEquals( "junit.framework.ComparisonFailure", testCase.getFailureType() );
                        assertEquals( "expected:&lt;...s&gt; but was:&lt;...&gt;", testCase.getFailureMessage() );
                        assertTrue( testCase.getFailureDetails().startsWith( "junit.framework.ComparisonFailure" ) );
                    }
                }
            }

        }
    }

    public void testgenerateReportTestResult()
        throws Exception
    {
        ReportTestSuiteGenerator generator = (ReportTestSuiteGenerator) lookup( ReportTestSuiteGenerator.class,
                                                                                "default" );
        ReportTestResult reportTestResult = generator.generateReportTestResult( 1, 1 );
        assertEquals( 18, reportTestResult.getSuiteResults().size() );
        assertEquals( 1, reportTestResult.getFailureCount() );
        assertEquals( 62, reportTestResult.getTestCount() );
        assertEquals( 1, reportTestResult.getErrorCount() );
    }
}
