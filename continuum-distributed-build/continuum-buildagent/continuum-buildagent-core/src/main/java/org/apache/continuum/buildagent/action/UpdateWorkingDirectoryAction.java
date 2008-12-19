package org.apache.continuum.buildagent.action;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.scm.ContinuumScm;
import org.apache.continuum.scm.ContinuumScmConfiguration;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.action.AbstractAction;

/**
 * @plexus.component role="org.codehaus.plexus.action.Action" role-hint="update-agent-working-directory"
 */
public class UpdateWorkingDirectoryAction
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

        BuildDefinition buildDefinition = ContinuumBuildAgentUtil.getBuildDefinition( context );

        UpdateScmResult scmResult;

        ScmResult result;
        
        try
        {
            // TODO: not sure why this is different to the context, but it all needs to change
            File workingDirectory = buildAgentConfigurationService.getWorkingDirectory( project.getId() );
            ContinuumScmConfiguration config = createScmConfiguration( project, workingDirectory );
            //config.setLatestUpdateDate( latestUpdateDate );
            String tag = config.getTag();
            String msg = project.getName() + "', id: '" + project.getId() + "' to '" +
                workingDirectory.getAbsolutePath() + "'" + ( tag != null ? " with branch/tag " + tag + "." : "." );
            getLogger().info( "Updating project: " + msg );
            scmResult = scm.update( config );

            if ( !scmResult.isSuccess() )
            {
                getLogger().warn( "Error while updating the code for project: '" + msg );

                getLogger().warn( "Command output: " + scmResult.getCommandOutput() );

                getLogger().warn( "Provider message: " + scmResult.getProviderMessage() );
            }

            if ( scmResult.getUpdatedFiles() != null && scmResult.getUpdatedFiles().size() > 0 )
            {
                getLogger().info( "Updated " + scmResult.getUpdatedFiles().size() + " files." );
            }

            result = convertScmResult( scmResult );
        }
        catch ( ScmRepositoryException e )
        {
            result = new ScmResult();

            result.setSuccess( false );

            result.setProviderMessage( e.getMessage() + ": " + getValidationMessages( e ) );
            
            getLogger().error( e.getMessage(), e);
        }
        catch ( NoSuchScmProviderException e )
        {
            // TODO: this is not making it back into a result of any kind - log it at least. Same is probably the case for ScmException
            result = new ScmResult();

            result.setSuccess( false );

            result.setProviderMessage( e.getMessage() );
            
            getLogger().error( e.getMessage(), e);
        }
        catch ( ScmException e )
        {
            result = new ScmResult();

            result.setSuccess( false );

            result.setException( ContinuumBuildAgentUtil.throwableMessagesToString( e ) );
            
            getLogger().error( e.getMessage(), e);
        }

        context.put( ContinuumBuildAgentUtil.KEY_UPDATE_SCM_RESULT, result );
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

    private ScmResult convertScmResult( UpdateScmResult scmResult )
    {
        ScmResult result = new ScmResult();

        result.setCommandLine( maskPassword( scmResult.getCommandLine() ) );

        result.setSuccess( scmResult.isSuccess() );

        result.setCommandOutput( scmResult.getCommandOutput() );

        result.setProviderMessage( scmResult.getProviderMessage() );

        return result;
    }
    
 // TODO: migrate to the SvnCommandLineUtils version (preferably properly encapsulated in the provider)
    private String maskPassword( String commandLine )
    {
        String cmd = commandLine;

        if ( cmd != null && cmd.startsWith( "svn" ) )
        {
            String pwdString = "--password";

            if ( cmd.indexOf( pwdString ) > 0 )
            {
                int index = cmd.indexOf( pwdString ) + pwdString.length() + 1;

                int nextSpace = cmd.indexOf( " ", index );

                cmd = cmd.substring( 0, index ) + "********" + cmd.substring( nextSpace );
            }
        }

        return cmd;
    }
    
    private String getValidationMessages( ScmRepositoryException ex )
    {
        List<String> messages = ex.getValidationMessages();

        StringBuffer message = new StringBuffer();

        if ( messages != null && !messages.isEmpty() )
        {
            for ( Iterator<String> i = messages.iterator(); i.hasNext(); )
            {
                message.append( i.next() );

                if ( i.hasNext() )
                {
                    message.append( System.getProperty( "line.separator" ) );
                }
            }
        }
        return message.toString();
    }
}
