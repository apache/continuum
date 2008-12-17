package org.apache.continuum.buildagent.action;

import java.io.File;
import java.util.Map;

import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.codehaus.plexus.action.AbstractAction;

/**
 * @plexus.component role="org.codehaus.plexus.action.Action" role-hint="clean-agent-working-directory"
 */
public class CleanWorkingDirectoryAction
    extends AbstractAction
{
    /**
     * @plexus.requirement
     */
    private BuildAgentConfigurationService buildAgentConfigurationService;

    public void execute( Map context )
        throws Exception
    {
        Project project = ContinuumBuildAgentUtil.getProject( context );
    
        File workingDirectory = buildAgentConfigurationService.getWorkingDirectory( project.getId() );
    
        if ( workingDirectory.exists() )
        {
            FileSetManager fileSetManager = new FileSetManager();
            FileSet fileSet = new FileSet();
            fileSet.setDirectory( workingDirectory.getPath() );
            fileSet.addInclude( "**/**" );
            // TODO : this with a configuration option somewhere ?
            fileSet.setFollowSymlinks( false );
            fileSetManager.delete( fileSet );
        }
    }
}
