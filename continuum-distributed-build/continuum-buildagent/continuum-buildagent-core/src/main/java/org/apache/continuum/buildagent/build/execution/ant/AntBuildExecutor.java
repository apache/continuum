package org.apache.continuum.buildagent.build.execution.ant;

import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.apache.continuum.buildagent.build.execution.AbstractBuildExecutor;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildCancelledException;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutionResult;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutor;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutorException;
import org.apache.continuum.buildagent.installation.BuildAgentInstallationService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.util.StringUtils;

public class AntBuildExecutor
    extends AbstractBuildExecutor   
    implements ContinuumAgentBuildExecutor
{
    public static final String CONFIGURATION_EXECUTABLE = "executable";

    public static final String CONFIGURATION_TARGETS = "targets";

    public static final String ID = ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR;

    protected AntBuildExecutor()
    {
        super( ID, true );
    }

    public ContinuumAgentBuildExecutionResult build( Project project, BuildDefinition buildDefinition, 
                                                     File buildOutput, Map<String, String> environments,
                                                     String localRepository )
        throws ContinuumAgentBuildExecutorException, ContinuumAgentBuildCancelledException
    {
        String executable = getBuildAgentInstallationService().getExecutorConfigurator( BuildAgentInstallationService.ANT_TYPE )
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

        String antHome = null;

        if ( environments != null )
        {
            antHome = environments.get( getBuildAgentInstallationService().getEnvVar( BuildAgentInstallationService.ANT_TYPE ) );
        }

        if ( StringUtils.isNotEmpty( antHome ) )
        {
            executable = antHome + File.separator + "bin" + File.separator + executable;
            setResolveExecutable( false );
        }

        return executeShellCommand( project, executable, arguments.toString(), buildOutput, environments );
    }
}
