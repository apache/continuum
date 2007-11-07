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

import org.apache.maven.continuum.xmlrpc.project.AddingResult;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.project.BuildResult;
import org.apache.maven.continuum.xmlrpc.project.ProjectDependency;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;
import org.apache.maven.continuum.xmlrpc.scm.ChangeSet;
import org.apache.maven.continuum.xmlrpc.scm.ScmResult;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SampleClient
{
    private static ContinuumXmlRpcClient client;

    public static void main( String[] args )
        throws Exception
    {
        client = new ContinuumXmlRpcClient( new URL( args[0] ), args[1], args[2] );

        System.out.println( "Adding project..." );
        AddingResult result = client.addMavenTwoProject( "http://svn.codehaus.org/plexus/plexus-utils/trunk/pom.xml" );
        if ( result.hasErrors() )
        {
            System.out.println( result.getErrorsAsString() );
            return;
        }
        System.out.println( "Project Groups added." );
        System.out.println( "=====================" );
        int projectGroupId = 0;
        for ( Iterator i = result.getProjectGroups().iterator(); i.hasNext(); )
        {
            ProjectGroupSummary pg = (ProjectGroupSummary) i.next();
            projectGroupId = pg.getId();
            System.out.println( "Id: " + pg.getId() );
            System.out.println( "Group Id" + pg.getGroupId() );
            System.out.println( "Name: " + pg.getName() );
            System.out.println( "Description:" + pg.getDescription() );
        }

        System.out.println();

        System.out.println( "Projects added." );
        System.out.println( "=====================" );
        for ( Iterator i = result.getProjects().iterator(); i.hasNext(); )
        {
            ProjectSummary p = (ProjectSummary) i.next();
            printProjectSummary( client.getProjectSummary( p.getId() ) );
        }

        System.out.println();

        System.out.println( "Waiting the end of the check out..." );

        ProjectSummary ps = (ProjectSummary) result.getProjects().get( 0 );

        while ( !"New".equals( client.getProjectStatusAsString( ps.getState() ) ) )
        {
            ps = client.refreshProjectSummary( ps );
            System.out.println( "State of " + ps.getName() + "(" + ps.getId() + "): " +
                client.getProjectStatusAsString( ps.getState() ) );
            Thread.sleep( 1000 );
        }

        System.out.println();

        System.out.println( "Add the project to the build queue." );
        client.buildProject( ps.getId() );
        while ( !"Building".equals( client.getProjectStatusAsString( ps.getState() ) ) )
        {
            ps = client.refreshProjectSummary( ps );
            Thread.sleep( 1000 );
        }

        System.out.println( "Building..." );
        String state = "unknown";
        while ( "Building".equals( client.getProjectStatusAsString( ps.getState() ) ) )
        {
            ps = client.refreshProjectSummary( ps );
            state = client.getProjectStatusAsString( ps.getState() );
            System.out.println( "State of " + ps.getName() + "(" + ps.getId() + "): " + state );
            Thread.sleep( 1000 );
        }
        System.out.println( "Build done with state=" + state + "." );

        System.out.println( "Build result." );
        System.out.println( "=====================" );
        printBuildResult( client.getLatestBuildResult( ps.getId() ) );

        System.out.println();

        System.out.println( "Build output." );
        System.out.println( "=====================" );
        System.out.println( client.getBuildOutput( ps.getId(), ps.getLatestBuildId() ) );

        System.out.println();

        System.out.println( "Projects list." );
        System.out.println( "=====================" );
        List projects = client.getProjects( projectGroupId );
        for ( Iterator i = projects.iterator(); i.hasNext(); )
        {
            ps = (ProjectSummary) i.next();
            printProjectSummary( ps );
            System.out.println();
        }

        System.out.println();

        System.out.println( "Remove all projects." );
        System.out.println( "=====================" );
        for ( Iterator i = projects.iterator(); i.hasNext(); )
        {
            ps = (ProjectSummary) i.next();
            System.out.println( "Removing '" + ps.getName() + "' - " + ps.getVersion() + " (" + ps.getId() + ")'..." );
            client.removeProject( ps.getId() );
            System.out.println( "Done." );
        }

        System.out.println();

        System.out.println( "Remove project group." );
        System.out.println( "=====================" );
        ProjectGroupSummary pg = client.getProjectGroupSummary( projectGroupId );
        System.out.println(
            "Removing Project Group '" + pg.getName() + "' - " + pg.getGroupId() + " (" + pg.getId() + ")'..." );
        client.removeProjectGroup( pg.getId() );
        System.out.println( "Done." );
    }

    public static void printProjectSummary( ProjectSummary project )
    {
        System.out.println( "Id: " + project.getId() );
        System.out.println( "Group Id:" + project.getGroupId() );
        System.out.println( "Artifact Id: " + project.getArtifactId() );
        System.out.println( "Version: " + project.getVersion() );
        System.out.println( "Name: " + project.getName() );
        System.out.println( "Description: " + project.getDescription() );
        System.out.println( "SCM Url: " + project.getScmUrl() );
    }

    public static void printBuildResult( BuildResult result )
    {
        System.out.println( "Id: " + result.getId() );
        System.out.println( "Project Id: " + result.getProject().getId() );
        System.out.println( "Build Number: " + result.getBuildNumber() );
        System.out.println( "Start Time: " + result.getStartTime() );
        System.out.println( "End Time: " + result.getEndTime() );
        System.out.println( "State: " + client.getProjectStatusAsString( result.getState() ) );
        System.out.println( "Trigger: " + result.getTrigger() );
        System.out.println( "Is success: " + result.isSuccess() );
        System.out.println( "Exit code: " + result.getExitCode() );
        System.out.println( "Error: " + result.getError() );

        if ( result.getModifiedDependencies() != null )
        {
            System.out.println( "Modified dependencies:" );
            for ( Iterator i = result.getModifiedDependencies().iterator(); i.hasNext(); )
            {
                printDependency( (ProjectDependency) i.next() );
            }
        }

        if ( result.getScmResult() != null )
        {
            System.out.println( "Scm Result:" );
            printScmResult( result.getScmResult() );
        }
    }

    public static void printDependency( ProjectDependency dep )
    {
        System.out.println( "Group Id: " + dep.getGroupId() );
        System.out.println( "Artifact Id: " + dep.getArtifactId() );
        System.out.println( "Version: " + dep.getVersion() );
    }

    public static void printScmResult( ScmResult scmResult )
    {
        System.out.println( "Command Line: " + scmResult.getCommandLine() );
        System.out.println( "Command Output: " + scmResult.getCommandOutput() );
        System.out.println( "SCM Providr Messqge: " + scmResult.getProviderMessage() );
        System.out.println( "Is Success: " + scmResult.isSuccess() );
        System.out.println( "Exception: " + scmResult.getException() );

        if ( scmResult.getChanges() != null )
        {
            System.out.println( "Changes:" );
            for ( Iterator i = scmResult.getChanges().iterator(); i.hasNext(); )
            {
                printChangeSet( (ChangeSet) i.next() );
            }
        }
        System.out.println( scmResult.getCommandLine() );
    }

    public static void printChangeSet( ChangeSet changeSet )
    {
        System.out.println( "Author: " + changeSet.getAuthor() );
        System.out.println( "Date: " + changeSet.getDateAsDate() );
        System.out.println( "Comment: " + changeSet.getComment() );

        if ( changeSet.getFiles() != null )
        {
            System.out.println( "Author: " + changeSet.getFiles() );
        }
    }

    public static void printBuildDefinition( BuildDefinition buildDef )
    {
        System.out.println( buildDef.getId() );
        System.out.println( buildDef.getBuildFile() );
        System.out.println( buildDef.getArguments() );
        System.out.println( buildDef.getGoals() );
        //printProfile( buildDef.getProfile() );
        //printSchedule( buildDef.getSchedule() );
        System.out.println( buildDef.isBuildFresh() );
        System.out.println( buildDef.isDefaultForProject() );
    }

}
