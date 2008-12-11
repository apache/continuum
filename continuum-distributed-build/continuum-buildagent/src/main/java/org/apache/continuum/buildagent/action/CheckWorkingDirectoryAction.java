package org.apache.continuum.buildagent.action;

import java.io.File;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager;
import org.apache.continuum.buildagent.configuration.ConfigurationService;
import org.apache.continuum.buildagent.util.BuildContextToProject;
import org.apache.maven.continuum.model.project.Project;

/**
 * 
 * @plexus.component role="org.codehaus.plexus.action.Action"
 * role-hint="check-working-directory-dist"
 */
public class CheckWorkingDirectoryAction
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
        Project project =  BuildContextToProject.getProject( buildContextManager.getBuildContext( getProjectId( context ) ) );

        File workingDirectory = configurationService.getWorkingDirectory( project.getId() );

        if ( !workingDirectory.exists() )
        {
            context.put( KEY_WORKING_DIRECTORY_EXISTS, Boolean.FALSE );

            return;
        }

        File[] files = workingDirectory.listFiles();

        context.put( KEY_WORKING_DIRECTORY_EXISTS, Boolean.valueOf( files.length > 0 ) );
    }
}
