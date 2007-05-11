package org.apache.maven.continuum.xmlrpc.continuum;

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
        ContinuumXmlRpcClient client =
            new ContinuumXmlRpcClient( new URL( "http://localhost:9090/xmlrpc" ), "admin", "admin1" );

        System.out.println( "Adding project..." );
        AddingResult result = client.addMavenTwoProject( "http://svn.codehaus.org/plexus/plexus-utils/trunk/pom.xml" );
        if ( result.hasErrors() )
        {
            System.out.println( result.getErrorsAsString() );
            return;
        }
        System.out.println( "Project Groups added." );
        System.out.println( "=====================" );
        for ( Iterator i = result.getProjectGroups().iterator(); i.hasNext(); )
        {
            ProjectGroupSummary pg = (ProjectGroupSummary) i.next();
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

        ProjectSummary ps = (ProjectSummary) result.getProjects().get( 0 );

        for ( int i = 0; i < 30; i++ )
        {
            ProjectSummary p = client.refreshProjectSummary( ps );
            System.out.println(
                "State of " + p.getName() + "(" + p.getId() + "): " + client.getProjectStatusAsString( p.getState() ) );
            Thread.sleep( 1000 );
        }

        System.out.println();

        System.out.println( "Projects list." );
        System.out.println( "=====================" );
        List projects = client.getProjects();
        for ( Iterator i = projects.iterator(); i.hasNext(); )
        {
            ProjectSummary p = (ProjectSummary) i.next();
            printProjectSummary( p );
            System.out.println();
        }

        Thread.sleep( 60000 );

        System.out.println();

        System.out.println( "Remove all projects." );
        System.out.println( "=====================" );
        for ( Iterator i = projects.iterator(); i.hasNext(); )
        {
            ProjectSummary p = (ProjectSummary) i.next();
            System.out.println( "Remove '" + p.getName() + "' - " + p.getVersion() + " (" + p.getId() + ")" );
            client.removeProject( p.getId() );
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
