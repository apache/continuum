package org.apache.continuum.buildagent.action;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager;
import org.apache.continuum.buildagent.configuration.ConfigurationService;
import org.apache.continuum.buildagent.utils.BuildContextToBuildDefinition;
import org.apache.continuum.buildagent.utils.BuildContextToProject;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.scm.ContinuumScm;
import org.apache.continuum.scm.ContinuumScmConfiguration;
import org.apache.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.action.AbstractAction;

/**
 * @plexus.requirement role="org.codehaus.plexus.action.Action" role-hint="checkout-agent-project"
 */
public class CheckoutProjectAction
    extends AbstractAction
{
    /**
     * @plexus.requirement
     */
    private BuildContextManager buildContextManager;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private ContinuumScm scm;

    public void execute( Map context )
        throws Exception
    {
        BuildContext buildContext = buildContextManager.getBuildContext( ContinuumBuildAgentUtil.getProjectId( context ) );
        
        Project project = BuildContextToProject.getProject( buildContext );
        BuildDefinition buildDefinition = BuildContextToBuildDefinition.getBuildDefinition( buildContext );

        File workingDirectory = configurationService.getWorkingDirectory( project.getId() );

        // ----------------------------------------------------------------------
        // Check out the project
        // ----------------------------------------------------------------------

        ScmResult result;

        try
        {
            String scmUserName = ContinuumBuildAgentUtil.getString( context, ContinuumBuildAgentUtil.KEY_SCM_USERNAME, "" );
            String scmPassword = ContinuumBuildAgentUtil.getString( context, ContinuumBuildAgentUtil.KEY_SCM_PASSWORD, "" );

            ContinuumScmConfiguration config =
                createScmConfiguration( project, workingDirectory, scmUserName, scmPassword );

            String tag = config.getTag();
            getLogger().info( "Checking out project: '" + project.getName() + "', id: '" + project.getId() + "' " +
                "to '" + workingDirectory + "'" + ( tag != null ? " with branch/tag " + tag + "." : "." ) );

            CheckOutScmResult checkoutResult = scm.checkout( config );
            //if ( StringUtils.isNotEmpty( checkoutResult.getRelativePathProjectDirectory() ) )
            //{
            //    context.put( AbstractContinuumAction.KEY_PROJECT_RELATIVE_PATH,
            //                 checkoutResult.getRelativePathProjectDirectory() );
            //}

            if ( !checkoutResult.isSuccess() )
            {
                // TODO: is it more appropriate to return this in the converted result so that it can be presented to
                // the user?
                String msg = "Error while checking out the code for project: '" + project.getName() + "', id: '" +
                    project.getId() + "' to '" + workingDirectory.getAbsolutePath() + "'" +
                    ( tag != null ? " with branch/tag " + tag + "." : "." );
                getLogger().warn( msg );

                getLogger().warn( "Command output: " + checkoutResult.getCommandOutput() );

                getLogger().warn( "Provider message: " + checkoutResult.getProviderMessage() );
            }
            else
            {
                getLogger().info( "Checked out " + checkoutResult.getCheckedOutFiles().size() + " files." );
            }

            result = convertScmResult( checkoutResult );
        }
        catch ( ScmRepositoryException e )
        {
            result = new ScmResult();

            result.setSuccess( false );

            result.setProviderMessage( e.getMessage() + ": " + getValidationMessages( e ) );

            getLogger().error( e.getMessage(), e );
        }
        catch ( NoSuchScmProviderException e )
        {
            // TODO: this is not making it back into a result of any kind - log it at least. Same is probably the case for ScmException
            result = new ScmResult();

            result.setSuccess( false );

            result.setProviderMessage( e.getMessage() );

            getLogger().error( e.getMessage(), e );
        }
        catch ( ScmException e )
        {
            result = new ScmResult();

            result.setSuccess( false );

            result.setException( ContinuumUtils.throwableMessagesToString( e ) );

            getLogger().error( e.getMessage(), e );
        }
        catch ( Throwable t )
        {
            // TODO: do we want this here, or should it be to the logs?
            // TODO: what throwables do we really get here that we can cope with?
            result = new ScmResult();

            result.setSuccess( false );

            result.setException( ContinuumUtils.throwableMessagesToString( t ) );

            getLogger().error( t.getMessage(), t );
        }

        context.put( ContinuumBuildAgentUtil.KEY_CHECKOUT_SCM_RESULT, result );
    }

    private ContinuumScmConfiguration createScmConfiguration( Project project, File workingDirectory,
                                                              String scmUserName, String scmPassword )
    {
        ContinuumScmConfiguration config = new ContinuumScmConfiguration();
        config.setUrl( project.getScmUrl() );
        config.setUsername( scmUserName );
        config.setPassword( scmPassword );
        config.setUseCredentialsCache( project.isScmUseCache() );
        config.setWorkingDirectory( workingDirectory );
        config.setTag( project.getScmTag() );
        return config;
    }

    private ScmResult convertScmResult( CheckOutScmResult scmResult )
    {
        ScmResult result = new ScmResult();

        result.setSuccess( scmResult.isSuccess() );

        result.setCommandLine( maskPassword( scmResult.getCommandLine() ) );

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
                message.append( (String) i.next() );

                if ( i.hasNext() )
                {
                    message.append( System.getProperty( "line.separator" ) );
                }
            }
        }
        return message.toString();
    }
}