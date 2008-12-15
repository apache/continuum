package org.apache.continuum.buildagent.build.execution.shell;

import java.io.File;

import org.apache.continuum.buildagent.build.execution.AbstractBuildExecutor;
import org.apache.continuum.buildagent.build.execution.ContinuumBuildCancelledException;
import org.apache.continuum.buildagent.build.execution.ContinuumBuildExecutionResult;
import org.apache.continuum.buildagent.build.execution.ContinuumBuildExecutor;
import org.apache.continuum.buildagent.build.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;

public class ShellBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumBuildExecutor
{
    public static final String CONFIGURATION_EXECUTABLE = "executable";

    public static final String ID = ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR;

    public ShellBuildExecutor()
    {
        super( ID, false );
    }

    public ContinuumBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput )
        throws ContinuumBuildExecutorException, ContinuumBuildCancelledException
    {
        String executable = getBuildFileForProject( project, buildDefinition );

        return executeShellCommand( project, executable, buildDefinition.getArguments(), buildOutput, null );
    }
}
