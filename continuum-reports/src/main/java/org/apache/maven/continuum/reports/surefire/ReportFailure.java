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

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @version $Id$
 * @since 13 nov. 07
 */
public class ReportFailure
{
    private String type;

    private String exception;

    private String testName;

    public ReportFailure( String type, String exception, String testName )
    {
        this.type = type;
        this.exception = exception;
        this.testName = testName;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getException()
    {
        return exception;
    }

    public void setException( String exception )
    {
        this.exception = exception;
    }

    public String getTestName()
    {
        return testName;
    }

    public void setTestName( String testName )
    {
        this.testName = testName;
    }
}
