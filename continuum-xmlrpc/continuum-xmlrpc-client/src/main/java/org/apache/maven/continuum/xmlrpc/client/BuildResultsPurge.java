package org.apache.maven.continuum.xmlrpc.client;

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

import org.apache.maven.continuum.xmlrpc.project.BuildResult;
import org.apache.maven.continuum.xmlrpc.project.BuildResultSummary;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;

import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * Utility class to purge old build results.
 * The easiest way to use it is to change the exec plugin config in the pom to execute this class instead of
 * SampleClient, change RETENTION_DAYS if desired, and type 'mvn clean install exec:exec'
 */
public class BuildResultsPurge
{

    private static ContinuumXmlRpcClient client;

    private static long RETENTION_DAYS = 60;

    private static long DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;

    public static void main( String[] args )
        throws Exception
    {

        client = new ContinuumXmlRpcClient( new URL( args[0] ), args[1], args[2] );

        long today = new Date().getTime();

        System.out.println( "Today is " + new Date( today ) );

        long purgeDate = today - ( RETENTION_DAYS * DAY_IN_MILLISECONDS );
        //long purgeDate = today - 1000;  // 1 second ago (for testing)

        System.out.println( "Purging build results older than " + new Date( purgeDate ) );

        List<ProjectGroupSummary> groups = client.getAllProjectGroups();

        for ( ProjectGroupSummary group : groups )
        {

            System.out.println( "Project Group [" + group.getId() + "] " + group.getName() );

            List<ProjectSummary> projects = client.getProjects( group.getId() );

            for ( ProjectSummary project : projects )
            {

                System.out.println( " Project [" + project.getId() + "] " + project.getName() );

                int batchSize = 100, offset = 0;
                List<BuildResultSummary> results;

                do
                {
                    int retained = 0;
                    results = client.getBuildResultsForProject( project.getId(), offset, batchSize );

                    for ( BuildResultSummary brs : results )
                    {

                        BuildResult br = client.getBuildResult( project.getId(), brs.getId() );

                        System.out.print( "  Build Result [" + br.getId() + "] ended " + new Date( br.getEndTime() ) );

                        if ( br.getEndTime() > 0 && br.getEndTime() < purgeDate )
                        {

                            client.removeBuildResult( br );
                            System.out.println( " ...removed." );
                        }
                        else
                        {
                            System.out.println( " ...retained." );
                            retained++;
                        }
                    }

                    offset += retained;  // Only need to advance past items we keep
                }
                while ( results != null && results.size() == batchSize );
            }

        }
    }
}
