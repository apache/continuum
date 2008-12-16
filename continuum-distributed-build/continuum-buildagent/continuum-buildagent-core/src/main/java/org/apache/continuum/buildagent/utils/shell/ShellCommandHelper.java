package org.apache.continuum.buildagent.utils.shell;

import java.io.File;
import java.util.Map;

import org.apache.maven.shared.release.ReleaseResult;

public interface ShellCommandHelper
{
    String ROLE = ShellCommandHelper.class.getName();

    ExecutionResult executeShellCommand( File workingDirectory, String executable, String arguments, File output,
                                         long idCommand, Map<String, String> environments )
        throws Exception;

    ExecutionResult executeShellCommand( File workingDirectory, String executable, String[] arguments, File output,
                                         long idCommand, Map<String, String> environments )
        throws Exception;

    boolean isRunning( long idCommand );

    void killProcess( long idCommand );

    void executeGoals( File workingDirectory, String goals, boolean interactive, String arguments,
                       ReleaseResult relResult, Map<String, String> environments )
        throws Exception;

    void executeGoals( File workingDirectory, String goals, boolean interactive, String[] arguments,
                       ReleaseResult relResult, Map<String, String> environments )
        throws Exception;
}
