package org.apache.maven.continuum.xmlrpc.client;

import org.apache.maven.continuum.xmlrpc.project.AddingResult;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SampleClient
{
    public static void main( String[] args )
        throws Exception
    {
        ContinuumXmlRpcClient client = new ContinuumXmlRpcClient( new URL( args[0] ), args[1], args[2] );

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
            System.out.println( "Removing '" + ps.getName() + "' - " + ps.getVersion() + " (" + ps.getId() + ")..." );
            client.removeProject( ps.getId() );
            System.out.println( "Done." );
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
}
