package org.apache.maven.continuum.release.phase;

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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.continuum.scm.ContinuumScmUtils;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.plexus.PlexusLogger;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.ScmUrlUtils;
import org.apache.maven.scm.provider.git.gitexe.command.branch.GitBranchCommand;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.apache.maven.shared.release.phase.AbstractReleasePhase;
import org.apache.maven.shared.release.scm.ReleaseScmCommandException;
import org.apache.maven.shared.release.scm.ReleaseScmRepositoryException;
import org.apache.maven.shared.release.scm.ScmRepositoryConfigurator;

/**
 * Update working copy
 *
 * @author Edwin Punzalan
 * @version $Id$
 */
public class UpdateWorkingCopyPhase
    extends AbstractReleasePhase
{
    /**
     * Tool that gets a configured SCM repository from release configuration.
     */
    private ScmRepositoryConfigurator scmRepositoryConfigurator;

    private boolean copyUpdated = false;

    public ReleaseResult execute( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException
    {
        ReleaseResult relResult = new ReleaseResult();

        logInfo( relResult, "Updating local copy against the scm..." );

        ScmRepository repository;
        ScmProvider provider;

        // CONTINUUM-2628
        // if git ssh, use credentials specified in scm url if present. otherwise, use the scm credentials of project
        String providerType = ScmUrlUtils.getProvider( releaseDescriptor.getScmSourceUrl() );
        String scmSpecificUrl = releaseDescriptor.getScmSourceUrl().substring( providerType.length() + 5 );

        if( providerType.contains( ContinuumScmUtils.GIT_SCM_PROVIDERTYPE ) && scmSpecificUrl.startsWith( GitScmProviderRepository.PROTOCOL_SSH ) )
        {
            scmSpecificUrl = scmSpecificUrl.substring( GitScmProviderRepository.PROTOCOL_SSH.length() + 3 );

            // extract user information
            int indexAt = scmSpecificUrl.indexOf( "@" );
            String sshScmUsername = "";
            String sshScmPassword = "";

            if ( indexAt >= 0 )
            {
                String userInfo = scmSpecificUrl.substring( 0, indexAt );
                sshScmUsername = userInfo;

                int indexPwdSep = userInfo.indexOf( ":" );
                // password is specified in the url
                if ( indexPwdSep < 0 )
                {
                    sshScmUsername = userInfo.substring( indexPwdSep + 1);
                }
                else
                {
                    sshScmUsername = userInfo.substring( 0, indexPwdSep );
                    sshScmPassword = userInfo.substring( indexPwdSep + 1 );
                }
            }

            if( !StringUtils.isBlank( sshScmUsername ) )
            {
                releaseDescriptor.setScmUsername( sshScmUsername );
                if( !StringUtils.isBlank( sshScmPassword ) )
                {
                    releaseDescriptor.setScmPassword( sshScmPassword );
                }
                else
                {
                    releaseDescriptor.setScmPassword( null );
                }
            }
        }

        try
        {
            repository = scmRepositoryConfigurator.getConfiguredRepository( releaseDescriptor, settings );

            provider = scmRepositoryConfigurator.getRepositoryProvider( repository );
        }
        catch ( ScmRepositoryException e )
        {
            throw new ReleaseScmRepositoryException(
                e.getMessage() + " for URL: " + releaseDescriptor.getScmSourceUrl(), e.getValidationMessages() );
        }
        catch ( NoSuchScmProviderException e )
        {
            throw new ReleaseExecutionException( "Unable to configure SCM repository: " + e.getMessage(), e );
        }

        UpdateScmResult updateScmResult = null;
        CheckOutScmResult checkOutScmResult = null;
        
        File workingDirectory = new File( releaseDescriptor.getWorkingDirectory() );
        ScmFileSet workingDirSet = new ScmFileSet( workingDirectory );
        
        try
        {
            if ( !workingDirectory.exists() )
            {
                workingDirectory.mkdirs();
            }
            
            ScmVersion scmTag = null;

            ScmProviderRepository providerRepo = repository.getProviderRepository();

            // FIXME: This should be handled by the maven-scm git provider
            if ( providerRepo instanceof GitScmProviderRepository )
            {
                String branchName =
                    GitBranchCommand.getCurrentBranch( new PlexusLogger(getLogger()), (GitScmProviderRepository) providerRepo,
                                                       workingDirSet );
                scmTag = new ScmBranch( branchName );
            }

            if( workingDirectory.listFiles().length > 1 )
            {
                updateScmResult = provider.update( repository, workingDirSet, scmTag );
            }
            else
            {
                checkOutScmResult = provider.checkOut( repository, new ScmFileSet( workingDirectory ) );
                checkOutScmResult = provider.checkOut( repository, workingDirSet, scmTag );
            }
        }
        catch ( ScmException e )
        {
            throw new ReleaseExecutionException( "An error occurred while updating your local copy: " + e.getMessage(),
                                                 e );
        }

        if ( updateScmResult != null )
        {
            if( !updateScmResult.isSuccess() )
            {
                throw new ReleaseScmCommandException( "Unable to update current working copy", updateScmResult );
            }
            
            copyUpdated = updateScmResult.getUpdatedFiles().size() > 0;
        }
        else
        {
            if( !checkOutScmResult.isSuccess() )
            {
                throw new ReleaseScmCommandException( "Unable to checkout project", checkOutScmResult );
            }
            
            copyUpdated = checkOutScmResult.getCheckedOutFiles().size() > 0;
        }

        relResult.setResultCode( ReleaseResult.SUCCESS );

        return relResult;
    }

    public ReleaseResult simulate( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException
    {
        return execute( releaseDescriptor, settings, reactorProjects );
    }

    public boolean isCopyUpdated()
    {
        return copyUpdated;
    }

    public void setCopyUpdated( boolean copyUpdated )
    {
        this.copyUpdated = copyUpdated;
    }

	public ReleaseResult execute(ReleaseDescriptor releaseDescriptor,
			ReleaseEnvironment releaseEnvironment, List reactorProjects)
			throws ReleaseExecutionException, ReleaseFailureException {
		return execute(releaseDescriptor, releaseEnvironment.getSettings(),
				reactorProjects);
	}

	public ReleaseResult simulate(ReleaseDescriptor releaseDescriptor,
			ReleaseEnvironment releaseEnvironment, List reactorProjects)
			throws ReleaseExecutionException, ReleaseFailureException {
		return execute(releaseDescriptor, releaseEnvironment.getSettings(),
				reactorProjects);
	}
}
