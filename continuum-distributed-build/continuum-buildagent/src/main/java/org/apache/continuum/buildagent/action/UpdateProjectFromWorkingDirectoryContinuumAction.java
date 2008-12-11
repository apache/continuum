package org.apache.continuum.buildagent.action;

import org.apache.continuum.buildagent.configuration.ConfigurationService;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.WorkingDirectoryService;

import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.action.Action"
 * role-hint="update-project-from-working-directory-dist"
 */
public class UpdateProjectFromWorkingDirectoryContinuumAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private BuildExecutorManager buildExecutorManager;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;

    public void execute( Map context )
        throws ContinuumStoreException, ContinuumException, ContinuumBuildExecutorException
    {
        Project project = getProject( context );      

        getLogger().info( "Updating project '" + project.getName() + "' from checkout." );

        BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( getBuildDefinitionId( context ) );

        // ----------------------------------------------------------------------
        // Make a new descriptor
        // ----------------------------------------------------------------------

        ContinuumBuildExecutor builder = buildExecutorManager.getBuildExecutor( project.getExecutorId() );

        builder.updateProjectFromCheckOut( configurationService.getWorkingDirectory( project.getId() ), project,
                                           buildDefinition );
    }
}
