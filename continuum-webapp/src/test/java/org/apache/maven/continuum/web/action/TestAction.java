package org.apache.maven.continuum.web.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * TestAction:
 *
 * @author jesse
 */
public class TestAction
    extends PlexusActionSupport
{
    private static final Logger logger = LoggerFactory.getLogger( TestAction.class );

    private String testString;

    public String execute()
        throws Exception
    {
        logger.info( testString );

        return INPUT;
    }

    public String getTestString()
    {
        return testString;
    }

    public void setTestString( String testString )
    {
        this.testString = testString;
    }
}
