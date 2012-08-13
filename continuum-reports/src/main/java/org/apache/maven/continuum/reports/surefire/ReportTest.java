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

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @version $Id$
 * @since 12 nov. 07
 */
public class ReportTest
{
    private String id;

    private String name;

    private int tests;

    private int errors;

    private int failures;

    private float elapsedTime;

    private List children;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public int getTests()
    {
        return tests;
    }

    public void setTests( int tests )
    {
        this.tests = tests;
    }

    public int getErrors()
    {
        return errors;
    }

    public void setErrors( int errors )
    {
        this.errors = errors;
    }

    public int getFailures()
    {
        return failures;
    }

    public void setFailures( int failures )
    {
        this.failures = failures;
    }

    public float getSuccessRate()
    {
        float percentage;
        if ( tests == 0 )
        {
            percentage = 0;
        }
        else
        {
            percentage = ( (float) ( tests - errors - failures ) / (float) tests ) * 100;
        }

        return percentage;
    }

    public float getElapsedTime()
    {
        return elapsedTime;
    }

    public void setElapsedTime( float elapsedTime )
    {
        this.elapsedTime = elapsedTime;
    }

    public List getChildren()
    {
        if ( children == null )
        {
            children = new ArrayList();
        }

        return children;
    }

    public void setChildren( List children )
    {
        this.children = children;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }
}
