package org.apache.continuum.buildagent.build.execution.ant;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.continuum.buildagent.build.execution.AbstractBuildExecutor;
import org.apache.continuum.buildagent.build.execution.ContinuumBuildCancelledException;
import org.apache.continuum.buildagent.build.execution.ContinuumBuildExecutionResult;
import org.apache.continuum.buildagent.build.execution.ContinuumBuildExecutor;
import org.apache.continuum.buildagent.build.execution.ContinuumBuildExecutorException;
import org.apache.continuum.buildagent.installation.InstallationService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.util.StringUtils;

public class AntBuildExecutor
    extends AbstractBuildExecutor   
    implements ContinuumBuildExecutor
{
    public static final String CONFIGURATION_EXECUTABLE = "executable";

    public static final String CONFIGURATION_TARGETS = "targets";

    public static final String ID = ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR;

    protected AntBuildExecutor()
    {
        super( ID, true );
    }

    public ContinuumBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput )
        throws ContinuumBuildExecutorException, ContinuumBuildCancelledException
    {
        String executable = getInstallationService().getExecutorConfigurator( InstallationService.ANT_TYPE )
            .getExecutable();

        StringBuffer arguments = new StringBuffer();
    
        String buildFile = getBuildFileForProject( project, buildDefinition );
    
        if ( !StringUtils.isEmpty( buildFile ) )
        {
            arguments.append( "-f " ).append( buildFile ).append( " " );
        }
    
        arguments.append( StringUtils.clean( buildDefinition.getArguments() ) ).append( " " );
    
        Properties props = getContinuumSystemProperties( project );
        for ( Enumeration itr = props.propertyNames(); itr.hasMoreElements(); )
        {
            String name = (String) itr.nextElement();
            String value = props.getProperty( name );
            arguments.append( "\"-D" ).append( name ).append( "=" ).append( value ).append( "\" " );
        }
    
        arguments.append( StringUtils.clean( buildDefinition.getGoals() ) );

        return executeShellCommand( project, executable, arguments.toString(), buildOutput, null );
    }
}
