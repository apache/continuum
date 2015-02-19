package org.apache.maven.continuum.reports.surefire;

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

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 13 nov. 07
 */
public class ReportTestResult
{

    private int testCount = 0;

    private int failureCount = 0;

    private int errorCount = 0;

    private float totalTime = 0;

    private List<ReportTestSuite> suiteResults;

    public void addReportTestSuite( ReportTestSuite reportTestSuite )
    {
        if ( this.suiteResults == null )
        {
            this.suiteResults = new LinkedList<ReportTestSuite>();
        }
        this.suiteResults.add( reportTestSuite );
        this.testCount += reportTestSuite.getNumberOfTests();
        this.failureCount += reportTestSuite.getNumberOfFailures();
        this.errorCount += reportTestSuite.getNumberOfErrors();
        this.totalTime += reportTestSuite.getTimeElapsed();
    }


    public int getTestCount()
    {
        return testCount;
    }

    public void setTestCount( int testCount )
    {
        this.testCount = testCount;
    }

    public int getFailureCount()
    {
        return failureCount;
    }

    public void setFailureCount( int failureCount )
    {
        this.failureCount = failureCount;
    }

    public int getErrorCount()
    {
        return errorCount;
    }

    public void setErrorCount( int errorCount )
    {
        this.errorCount = errorCount;
    }

    public List<ReportTestSuite> getSuiteResults()
    {
        return suiteResults;
    }

    public void setSuiteResults( List<ReportTestSuite> suiteResults )
    {
        this.suiteResults = suiteResults;
    }

    public float getTotalTime()
    {
        return totalTime;
    }

    public void setTotalTime( float totalTime )
    {
        this.totalTime = totalTime;
    }

}
