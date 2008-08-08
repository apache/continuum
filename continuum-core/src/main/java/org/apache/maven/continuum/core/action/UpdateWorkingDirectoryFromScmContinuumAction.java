package org.apache.maven.continuum.core.action;

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

import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.scm.ContinuumScm;
import org.apache.continuum.scm.ContinuumScmConfiguration;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.action.Action" role-hint="update-working-directory-from-scm"
 */
public class UpdateWorkingDirectoryFromScmContinuumAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private ContinuumNotificationDispatcher notifier;

    /**
     * @plexus.requirement
     */
    private ContinuumScm scm;

    /**
     * @plexus.requirement
     */
    private WorkingDirectoryService workingDirectoryService;

    /**
     * @plexus.requirement
     */
    private BuildResultDao buildResultDao;

    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;

    public void execute( Map context )
        throws ScmRepositoryException, NoSuchScmProviderException, ScmException, ContinuumObjectNotFoundException,
        ContinuumStoreException
    {
        Project project = projectDao.getProject( getProject( context ).getId() );

        BuildDefinition buildDefinition = getBuildDefinition( context );

        int state = project.getState();

        project.setState( ContinuumProjectState.UPDATING );

        projectDao.updateProject( project );

        UpdateScmResult scmResult;

        Date latestUpdateDate = null;
        try
        {
            BuildResult result = buildResultDao.getLatestBuildResultForProject( project.getId() );

            latestUpdateDate = new Date( result.getStartTime() );
        }
        catch ( Exception e )
        {
        }

        try
        {
            notifier.checkoutStarted( project, buildDefinition );

            // TODO: not sure why this is different to the context, but it all needs to change
            File workingDirectory = workingDirectoryService.getWorkingDirectory( project );
            ContinuumScmConfiguration config = createScmConfiguration( project, workingDirectory );
            config.setLatestUpdateDate( latestUpdateDate );
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

            context.put( KEY_UPDATE_SCM_RESULT, convertScmResult( scmResult ) );
        }
        finally
        {
            // set back to the original state
            // TODO: transient states!
            try
            {
                project = projectDao.getProject( project.getId() );

                project.setState( state );

                projectDao.updateProject( project );
            }
            catch ( Exception e )
            {
                // nasty nasty, but we're in finally, so just sacrifice the state to keep the original exception
                getLogger().error( e.getMessage(), e );
            }

            notifier.checkoutComplete( project, buildDefinition );
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
}
