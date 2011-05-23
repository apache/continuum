package org.apache.continuum.buildagent.action;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.scm.ContinuumScm;
import org.apache.continuum.scm.ContinuumScmConfiguration;
import org.apache.continuum.scm.ContinuumScmUtils;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
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

        UpdateScmResult scmResult;

        ScmResult result;
        
        try
        {
            File workingDirectory = buildAgentConfigurationService.getWorkingDirectory( project.getId() );
            ContinuumScmConfiguration config = createScmConfiguration( project, workingDirectory );
            config.setLatestUpdateDate( ContinuumBuildAgentUtil.getLatestUpdateDate( context ) );
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

        Date latestUpdateDate = getLatestUpdateDate( result );

        if ( latestUpdateDate == null )
        {
            latestUpdateDate = ContinuumBuildAgentUtil.getLatestUpdateDate( context );
        }

        context.put( ContinuumBuildAgentUtil.KEY_LATEST_UPDATE_DATE, latestUpdateDate );
    }

    private ContinuumScmConfiguration createScmConfiguration( Project project, File workingDirectory )
    {
        ContinuumScmConfiguration config = new ContinuumScmConfiguration();
        config.setUrl( project.getScmUrl() );

        // CONTINUUM-2628
        config = ContinuumScmUtils.setSCMCredentialsforSSH( config, project.getScmUrl(), project.getScmUsername(), project.getScmPassword() );
        
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

        if ( scmResult.getChanges() != null && !scmResult.getChanges().isEmpty() )
        {
            for ( org.apache.maven.scm.ChangeSet scmChangeSet : (List<org.apache.maven.scm.ChangeSet>) scmResult.getChanges() )
            {
                ChangeSet change = new ChangeSet();

                change.setAuthor( scmChangeSet.getAuthor() );

                change.setComment( scmChangeSet.getComment() );

                if ( scmChangeSet.getDate() != null )
                {
                    change.setDate( scmChangeSet.getDate().getTime() );
                }

                if ( scmChangeSet.getFiles() != null )
                {
                    for ( org.apache.maven.scm.ChangeFile f : (List<org.apache.maven.scm.ChangeFile>) scmChangeSet.getFiles() )
                    {
                        ChangeFile file = new ChangeFile();

                        file.setName( f.getName() );

                        file.setRevision( f.getRevision() );

                        change.addFile( file );
                    }
                }

                result.addChange( change );
            }
        }
        else
        {
            // We don't have a changes information probably because provider doesn't have a changelog command
            // so we use the updated list that contains only the updated files list
            ChangeSet changeSet = convertScmFileSetToChangeSet( scmResult.getUpdatedFiles() );

            if ( changeSet != null )
            {
                result.addChange( changeSet );
            }
        }
        return result;
    }

    private static ChangeSet convertScmFileSetToChangeSet( List<ScmFile> files )
    {
        ChangeSet changeSet = null;

        if ( files != null && !files.isEmpty() )
        {
            changeSet = new ChangeSet();

            // TODO: author, etc.
            for ( ScmFile scmFile : files )
            {
                ChangeFile file = new ChangeFile();

                file.setName( scmFile.getPath() );

                // TODO: revision?

                file.setStatus( scmFile.getStatus().toString() );

                changeSet.addFile( file );
            }
        }
        return changeSet;
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

    private Date getLatestUpdateDate( ScmResult result )
    {
        List<ChangeSet> changes = result.getChanges();

        if ( changes != null && !changes.isEmpty() )
        {
            long date = 0;

            for ( ChangeSet change : changes )
            {
                if ( date < change.getDate() )
                {
                    date = change.getDate();
                }
            }

            if ( date != 0 )
            {
                return new Date( date );
            }
        }

        return null;
    }
}
