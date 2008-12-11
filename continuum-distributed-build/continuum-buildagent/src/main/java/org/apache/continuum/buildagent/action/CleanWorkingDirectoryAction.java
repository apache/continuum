package org.apache.continuum.buildagent.action;

import java.io.File;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager;
import org.apache.continuum.buildagent.configuration.ConfigurationService;
import org.apache.continuum.buildagent.util.BuildContextToProject;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * @plexus.component role="org.codehaus.plexus.action.Action" role-hint="clean-working-directory-dist" 
 */
public class CleanWorkingDirectoryAction
extends AbstractContinuumAction
{

    /**
     * @plexus.requirement
     */
    BuildContextManager buildContextManager;
    
    /**
     * @plexus.requirement
     */    
    ConfigurationService configurationService;
    
    public void execute( Map context )
        throws Exception
    {
        Project project = BuildContextToProject.getProject( buildContextManager.getBuildContext( getProjectId( context ) ) );

        File workingDirectory = configurationService.getWorkingDirectory( project.getId() );

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
