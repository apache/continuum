package org.apache.continuum.scm;

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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @todo consider folding some of this into Maven SCM itself
 */
public class DefaultContinuumScm
    implements ContinuumScm
{
    /** The Maven SCM manager to use. */
    private ScmManager scmManager;

    public CheckOutScmResult checkout( ContinuumScmConfiguration configuration )
        throws IOException, ScmException
    {
        ScmVersion scmVersion = getScmVersion( configuration );

        // TODO: probably need to base this from a working directory in the main configuration
        File workingDirectory = configuration.getWorkingDirectory();

        ScmRepository repository = getScmRepository( configuration );

        CheckOutScmResult result;

        // TODO: synchronizing *all* checkouts is unnecessary
        synchronized ( this )
        {
            if ( !workingDirectory.exists() )
            {
                if ( !workingDirectory.mkdirs() )
                {
                    throw new IOException( "Could not make directory: " + workingDirectory.getAbsolutePath() );
                }
            }
            else
            {
                FileUtils.cleanDirectory( workingDirectory );
            }

            ScmFileSet fileSet = new ScmFileSet( workingDirectory );

            result = scmManager.checkOut( repository, fileSet, scmVersion );
        }
        return result;
    }

    private ScmVersion getScmVersion( ContinuumScmConfiguration configuration )
    {
        String tag = configuration.getTag();

        ScmVersion scmVersion = null;
        if ( tag != null )
        {
            // TODO: differentiate between tag and branch? Allow for revision?
            scmVersion = new ScmTag( tag );
        }
        return scmVersion;
    }

    public UpdateScmResult update( ContinuumScmConfiguration configuration )
        throws ScmException
    {
        ScmVersion scmVersion = getScmVersion( configuration );

        File workingDirectory = configuration.getWorkingDirectory();
        if ( !workingDirectory.exists() )
        {
            // TODO: maybe we could check it out - it seems we currently rely on Continuum figuring this out
            throw new IllegalStateException( "The working directory for the project doesn't exist " + "("
                + workingDirectory.getAbsolutePath() + ")." );
        }

        ScmRepository repository = getScmRepository( configuration );

        // Some SCM provider requires additional system properties during update
        if ( "starteam".equals( repository.getProvider() ) )
        {
            // TODO: remove the use of system property - need a better way to pass provider specific configuration

            // Remove the clientspec name, so it will be recalculated between each command for each project
            // instead of use the same for all projects
            System.setProperty( "maven.scm.starteam.deleteLocal", "true" );
        }

        UpdateScmResult result;

        ScmFileSet fileSet = new ScmFileSet( workingDirectory );

        // TODO: shouldn't need to synchronize this
        synchronized ( this )
        {
            result = scmManager.update( repository, fileSet, scmVersion, configuration.getLatestUpdateDate() );
        }

        return result;
    }

    /**
     * Create a Maven SCM repository for obtaining the checkout from.
     * 
     * @param scmUrl the SCM URL to obtain the checkout from
     * @param useCredentialsCache whether to allow the use of cached credentials for SVN
     * @return the repository created
     */
    private ScmRepository getScmRepository( ContinuumScmConfiguration configuration )
        throws ScmRepositoryException, NoSuchScmProviderException
    {
        ScmRepository repository = scmManager.makeScmRepository( configuration.getUrl() );

        // TODO: tie together with the clientspec change below
        // This checkout will be retained between uses, so it remains connected to the repository
        repository.getProviderRepository().setPersistCheckout( true );

        // TODO: should this be svnexe?
        if ( !configuration.isUseCredentialsCache() || !"svn".equals( repository.getProvider() ) )
        {
            if ( !StringUtils.isEmpty( configuration.getUsername() ) )
            {
                repository.getProviderRepository().setUser( configuration.getUsername() );

                if ( !StringUtils.isEmpty( configuration.getPassword() ) )
                {
                    repository.getProviderRepository().setPassword( configuration.getPassword() );
                }
                else
                {
                    repository.getProviderRepository().setPassword( "" );
                }
            }
        }

        if ( "perforce".equals( repository.getProvider() ) )
        {
            // TODO: remove the use of system property - need a better way to pass provider specific configuration

            // Remove the clientspec name, so it will be recalculated between each command for each project
            // instead of use the same for all projects
            System.setProperty( "maven.scm.perforce.clientspec.name", "" );
        }

        return repository;
    }

    public ScmManager getScmManager()
    {
        return scmManager;
    }

    public void setScmManager( ScmManager scmManager )
    {
        this.scmManager = scmManager;
    }

    // TODO: add a nuke() method
}
