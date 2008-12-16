package org.apache.continuum.buildagent.build.execution.shell;

import java.io.File;

import org.apache.continuum.buildagent.build.execution.AbstractBuildExecutor;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildCancelledException;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutionResult;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutor;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutorException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;

public class ShellBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumAgentBuildExecutor
{
    public static final String CONFIGURATION_EXECUTABLE = "executable";

    public static final String ID = ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR;

    public ShellBuildExecutor()
    {
        super( ID, false );
    }

    public ContinuumAgentBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput )
        throws ContinuumAgentBuildExecutorException, ContinuumAgentBuildCancelledException
    {
        String executable = getBuildFileForProject( project, buildDefinition );

        return executeShellCommand( project, executable, buildDefinition.getArguments(), buildOutput, null );
    }
}
