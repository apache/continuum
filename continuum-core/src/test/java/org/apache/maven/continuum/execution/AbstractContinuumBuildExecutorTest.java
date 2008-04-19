package org.apache.maven.continuum.execution;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.configuration.DefaultConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.utils.ChrootJailWorkingDirectoryService;
import org.apache.maven.continuum.utils.shell.ExecutionResult;
import org.apache.maven.continuum.utils.shell.ShellCommandHelper;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.jmock.Expectations;
import org.jmock.Mock;
import org.jmock.Mockery;
import org.jmock.core.Constraint;

public class AbstractContinuumBuildExecutorTest
    extends TestCase
{
    protected AbstractBuildExecutor executor = new BuildExecutorStub();

    private Mockery context = new Mockery();

    private String toSystemPath( String path )
    {
        if ( File.separator.equals( "\\" ) )
        {
            return path.replaceAll( "/", "\\" + File.separator );
        }
        return path;
    }

    public void testExecuteShellCommand()
        throws Exception
    {
        final File chrootJailFile = new File( toSystemPath( "/home" ) );
        final File workingDirectory = new File( toSystemPath( "/dir1/dir2/workingdir" ) );

        final ShellCommandHelper helper = context.mock( ShellCommandHelper.class );

        ConfigurationService configurationService = new DefaultConfigurationService()
        {
            @Override
            public File getWorkingDirectory()
            {
                return workingDirectory;
            }
        };

        ChrootJailWorkingDirectoryService directoryService = new ChrootJailWorkingDirectoryService();
        directoryService.setConfigurationService( configurationService );
        directoryService.setChrootJailDirectory( chrootJailFile );

        executor.setChrootJailDirectory( chrootJailFile );
        executor.setShellCommandHelper( helper );
        executor.setWorkingDirectoryService( directoryService );
        executor.enableLogging( new ConsoleLogger( Logger.LEVEL_DEBUG, "" ) );

        final Project project = new Project();
        project.setId( 7 );
        project.setGroupId( "xx" );
        ProjectGroup projectGroup = new ProjectGroup();
        projectGroup.setGroupId( project.getGroupId() );
        project.setProjectGroup( projectGroup );

        assertEquals( toSystemPath( chrootJailFile.getPath() + "/" + project.getGroupId() + workingDirectory.getPath() +
            "/" + project.getId() ), directoryService.getWorkingDirectory( project ).getPath() );

        String executable = "mvn";
        final String arguments = "-o clean install";
        final File output = new File( "target/tmp" );
        final Map<String, String> environments = new HashMap<String, String>();

        final String cmd =
            "chroot /home/xx " + " cd /dir1/dir2/workingdir/" + project.getId() + " && " + executable + " " + arguments;
        // Constraint[] args =
        // new Constraint[] { eq( chrootJailFile ), eq( "sudo" ), eq( toSystemPath( cmd ) ), eq( output ),
        // eq( project.getId() ), eq( environments ) };
        final ExecutionResult result = new ExecutionResult( 0 );

        context.checking( new Expectations()
        {
            {
                one( helper ).executeShellCommand( chrootJailFile, "sudo", toSystemPath( cmd ), output,
                                                   project.getId(), environments );
                will( returnValue( result ) );
            }
        } );

        // helperMock.expects( once() ).method( "executeShellCommand" ).with( args ).will( returnValue( result ) );

        executor.executeShellCommand( project, executable, arguments, output, environments );

        context.assertIsSatisfied();
        // super.verify();
    }

    class BuildExecutorStub
        extends AbstractBuildExecutor
    {

        protected BuildExecutorStub()
        {
            super( "stub", true );
        }

        protected String findExecutable( Project project, String executable, String defaultExecutable,
                                         boolean resolveExecutable, File workingDirectory )
        {
            return executable;
        }

        @Override
        protected Map<String, String> getEnvironments( BuildDefinition buildDefinition )
        {
            // TODO Auto-generated method stub
            return null;
        }

        public ContinuumBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput )
            throws ContinuumBuildExecutorException
        {
            // TODO Auto-generated method stub
            return null;
        }

        public void updateProjectFromCheckOut( File workingDirectory, Project project, BuildDefinition buildDefinition )
            throws ContinuumBuildExecutorException
        {
            // TODO Auto-generated method stub

        }
    }
}
