package org.apache.continuum.buildagent.action;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.scm.ContinuumScm;
import org.apache.continuum.scm.ContinuumScmConfiguration;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.codehaus.plexus.action.AbstractAction;

/**
 * @plexus.component role="org.codehaus.plexus.action.Action" role-hint="changelog-agent-project"
 */
public class ChangeLogProjectAction
    extends AbstractAction
{
    /**
     * @plexus.requirement
     */
    private BuildAgentConfigurationService buildAgentConfigurationService;

    /**
     * @plexus.requirement
     */
    private ContinuumScm scm;

    public void execute( Map context )
        throws Exception
    {
        Project project = ContinuumBuildAgentUtil.getProject( context );

        try
        {
            File workingDirectory = buildAgentConfigurationService.getWorkingDirectory( project.getId() );
            ContinuumScmConfiguration config = createScmConfiguration( project, workingDirectory );
            getLogger().info( "Getting changeLog of project: " + project.getName() );
            ChangeLogScmResult changeLogResult = scm.changeLog( config );

            if ( !changeLogResult.isSuccess() )
            {
                getLogger().warn( "Error getting change log of project " + project.getName() );

                getLogger().warn( "Command Output: " + changeLogResult.getCommandOutput() );

                getLogger().warn( "Provider Message: " + changeLogResult.getProviderMessage() );
            }

            context.put( ContinuumBuildAgentUtil.KEY_LATEST_UPDATE_DATE, getLatestUpdateDate( changeLogResult ) );
        }
        catch ( ScmException e )
        {
            context.put( ContinuumBuildAgentUtil.KEY_LATEST_UPDATE_DATE, null );

            getLogger().error( e.getMessage(), e );
        }
    }

    private ContinuumScmConfiguration createScmConfiguration( Project project, File workingDirectory )
    {
        ContinuumScmConfiguration config = new ContinuumScmConfiguration();
        config.setUrl( project.getScmUrl() );
        config.setUsername( project.getScmUsername() );
        config.setPassword( project.getScmPassword() );
        config.setUseCredentialsCache( project.isScmUseCache() );
        config.setWorkingDirectory( workingDirectory );
        config.setTag( project.getScmTag() );
        return config;
    }

    private Date getLatestUpdateDate( ChangeLogScmResult changeLogScmResult )
    {
        ChangeLogSet changeLogSet = changeLogScmResult.getChangeLog();

        if ( changeLogSet != null )
        {
            List<ChangeSet> changes = changeLogSet.getChangeSets();

            if ( changes != null && !changes.isEmpty() )
            {
                long date = 0;

                for ( ChangeSet change : changes )
                {
                    if ( date < change.getDate().getTime() )
                    {
                        date = change.getDate().getTime();
                    }
                }

                if ( date != 0 )
                {
                    return new Date( date );
                }
            }
        }

        return null;
    }
}
