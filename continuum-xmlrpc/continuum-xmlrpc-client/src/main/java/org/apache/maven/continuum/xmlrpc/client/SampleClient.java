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

import org.apache.continuum.xmlrpc.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.xmlrpc.repository.LocalRepository;
import org.apache.continuum.xmlrpc.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.xmlrpc.utils.BuildTrigger;
import org.apache.maven.continuum.xmlrpc.project.AddingResult;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.project.BuildResult;
import org.apache.maven.continuum.xmlrpc.project.BuildResultSummary;
import org.apache.maven.continuum.xmlrpc.project.ContinuumProjectState;
import org.apache.maven.continuum.xmlrpc.project.ProjectDependency;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;
import org.apache.maven.continuum.xmlrpc.project.Schedule;
import org.apache.maven.continuum.xmlrpc.scm.ChangeSet;
import org.apache.maven.continuum.xmlrpc.scm.ScmResult;
import org.apache.maven.continuum.xmlrpc.system.Installation;

import java.net.URL;
import java.util.ArrayList;
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
        
        // Test for [CONTINUUM-2641]: (test with distributed builds with multiple build agents or parallel builds with > 1 build queue)
        // make sure to set the projectIds to the actual projectIds of your projects added in Continuum
        int projectIds[] = new int[] { 2, 3, 4, 5, 6 };

        List<Thread> threads = new ArrayList<Thread>();

        for ( int i = 0; i < projectIds.length; i++ )
        {
            final int order = i;
            final int projectId = projectIds[i];
            Runnable task = new Runnable (){
                public void run()
                {
                    BuildTrigger buildTrigger = new BuildTrigger();
                    buildTrigger.setTrigger( ContinuumProjectState.TRIGGER_FORCED );
                    buildTrigger.setTriggeredBy( "admin" );
                    System.out.println( "Building project #" + order + " '" + projectId + "'.." );
                    try
                    {
                        client.buildProject( projectId, buildTrigger );
                    }
                    catch ( Exception e )
                    {
                        throw new RuntimeException( e );
                    }
                }
            };
            threads.add(new Thread(task));
        }
        
        for ( Thread thread : threads )
        {
            thread.start();
        }

        System.out.println( "Adding project..." );
        AddingResult result = client.addMavenTwoProject( "http://svn.apache.org/repos/asf/continuum/sandbox/simple-example/pom.xml" );
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
            printProjectGroupSummary( client.getProjectGroupSummary( projectGroupId ) );
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

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setArguments( "A-Za-z0-9_./=,\": \\-" );
        buildDef.setSchedule( client.getSchedule( 1 ) );
        client.addBuildDefinitionToProjectGroup( 1, buildDef );

        ps = client.getProjectSummary( 1 );
        System.out.println( "Add the project to the build queue." );
        BuildTrigger trigger = new BuildTrigger();
        trigger.setTrigger( 1 );
        trigger.setTriggeredBy( "<script>alert('hahaha' )</script>" );
        client.buildProject( ps.getId(), trigger );
        while ( !"Building".equals( client.getProjectStatusAsString( ps.getState() ) ) )
        {
            ps = client.refreshProjectSummary( ps );
            Thread.sleep( 1000 );
        }

        System.out.println( "Building..." );
        String state = "unknown";
        while ( "Updating".equals( client.getProjectStatusAsString( ps.getState() ) ) ||
                "Building".equals( client.getProjectStatusAsString( ps.getState() ) ) )
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

        System.out.println( "Removing build results." );
        System.out.println( "============================" );
        BuildResultSummary brs;
        List results = client.getBuildResultsForProject( ps.getId() );
        for ( Iterator i = results.iterator(); i.hasNext(); )
        {
            brs = (BuildResultSummary) i.next();
            System.out.print( "Removing build result (" + brs.getId() + ") - " );
            BuildResult br = client.getBuildResult( ps.getId(), brs.getId() );
            System.out.println( (client.removeBuildResult( br ) == 0 ? "OK" : "Error" ) );
        }
        System.out.println( "Done.");

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
        System.out.println();

        LocalRepository repository = new LocalRepository();
        repository.setLocation( "/home/marica/repository" );
        repository.setName( "Repository" );
        repository.setLayout( "default" );
        System.out.println( "Adding local repository..." );
        repository = client.addLocalRepository( repository );
        System.out.println();

        System.out.println( "Repository list" );
        System.out.println( "=====================" );
        List<LocalRepository> repositories = client.getAllLocalRepositories();
        for ( LocalRepository repo : repositories )
        {
            printLocalRepository( repo );
            System.out.println();
        }

        DirectoryPurgeConfiguration dirPurgeConfig = new DirectoryPurgeConfiguration();
        dirPurgeConfig.setDirectoryType( "buildOutput" );
        System.out.println( "Adding Directory Purge Configuration..." );
        dirPurgeConfig = client.addDirectoryPurgeConfiguration( dirPurgeConfig );
        System.out.println();
        
        RepositoryPurgeConfiguration purgeConfig = new RepositoryPurgeConfiguration();
        purgeConfig.setDeleteAll( true );
        purgeConfig.setRepository( repository );
        purgeConfig.setDescription( "Delete all artifacts from repository" );
        System.out.println( "Adding Repository Purge Configuration..." );
        purgeConfig = client.addRepositoryPurgeConfiguration( purgeConfig );
        System.out.println();

        System.out.println( "Repository Purge list" );
        System.out.println( "=====================" );
        List<RepositoryPurgeConfiguration> repoPurges = client.getAllRepositoryPurgeConfigurations();
        for ( RepositoryPurgeConfiguration repoPurge : repoPurges )
        {
            printRepositoryPurgeConfiguration( repoPurge );
        }
        System.out.println();

        System.out.println( "Remove local repository" );
        System.out.println( "=====================" );
        System.out.println( "Removing Local Repository '" + repository.getName() + "' (" + 
                            repository.getId() + ")..." );
        client.removeLocalRepository( repository.getId() );
        System.out.println( "Done." );
    }

    public static void printProjectGroupSummary( ProjectGroupSummary pg )
    {
        System.out.println( "Id: " + pg.getId() );
        System.out.println( "Group Id" + pg.getGroupId() );
        System.out.println( "Name: " + pg.getName() );
        System.out.println( "Description:" + pg.getDescription() );
        if ( pg.getLocalRepository() != null )
        {
            System.out.println( "Local Repository:" + pg.getLocalRepository().getName() );
        }
        else
        {
            System.out.println( "Local Repository:" );
        }
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

    public static void printLocalRepository( LocalRepository repo )
    {
        System.out.println( "Id: " +repo.getId() );
        System.out.println( "Layout: " + repo.getLayout() );
        System.out.println( "Location: " + repo.getLocation() );
        System.out.println( "Name: " + repo.getName() );
    }

    public static void printRepositoryPurgeConfiguration( RepositoryPurgeConfiguration repoPurge )
    {
        System.out.println( "Id: " + repoPurge.getId() );
        System.out.println( "Description: " + repoPurge.getDescription() );
        System.out.println( "Local Repository: " + repoPurge.getRepository().getName() );
        System.out.println( "Days Older: " + repoPurge.getDaysOlder() );
        System.out.println( "Retention Count: " + repoPurge.getRetentionCount() );
        System.out.println( "Delete All: " + repoPurge.isDeleteAll() );
        System.out.println( "Delete Released Snapshots: " + repoPurge.isDeleteReleasedSnapshots() );
        System.out.println( "Default Purge: " + repoPurge.isDefaultPurge() );
    }
}
