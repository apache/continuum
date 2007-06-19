package org.apache.maven.continuum.project.builder;

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

import junit.framework.TestCase;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import java.net.URL;

/**
 * Test for {@link AbstractContinuumProjectBuilder}
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class AbstractContinuumProjectBuilderTest
    extends TestCase
{

    private ContinuumProjectBuilder builder;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        builder = new ContinuumProjectBuilder();
        builder.enableLogging( new ConsoleLogger( Logger.LEVEL_DEBUG, "" ) );
    }

    /**
     * Test for CONTINUUM-747. Disable as it requires a password protected resource under https.
     *
     * @throws Exception
     */
    public void disabledTestCreateMetadataFileURLStringString()
        throws Exception
    {
        URL url = new URL( "https://someurl/pom.xml" );
        String username = "myusername";
        String password = "mypassword";
        builder.createMetadataFile( url, username, password );
    }

    private class ContinuumProjectBuilder
        extends AbstractContinuumProjectBuilder
    {

        public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password )
            throws ContinuumProjectBuilderException
        {
            return null;
        }

    }

}