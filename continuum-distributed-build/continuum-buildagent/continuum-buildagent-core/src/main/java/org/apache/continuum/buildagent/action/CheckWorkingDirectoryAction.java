package org.apache.continuum.buildagent.action;

import java.io.File;
import java.util.Map;

import org.apache.continuum.buildagent.configuration.ConfigurationService;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.action.AbstractAction;

/**
 * @plexus.component role="org.codehaus.plexus.action.Action" role-hint="check-agent-working-directory"
 */
public class CheckWorkingDirectoryAction
    extends AbstractAction
{
    /**
     * @plexus.requirement
     */    
    ConfigurationService configurationService;
    
    public void execute( Map context )
        throws Exception
    {
        Project project = ContinuumBuildAgentUtil.getProject( context );

        File workingDirectory = configurationService.getWorkingDirectory( project.getId() );

        if ( !workingDirectory.exists() )
        {
            context.put( ContinuumBuildAgentUtil.KEY_WORKING_DIRECTORY_EXISTS, Boolean.FALSE );

            return;
        }

        File[] files = workingDirectory.listFiles();

        context.put( ContinuumBuildAgentUtil.KEY_WORKING_DIRECTORY_EXISTS, Boolean.valueOf( files.length > 0 ) );
    }

}
