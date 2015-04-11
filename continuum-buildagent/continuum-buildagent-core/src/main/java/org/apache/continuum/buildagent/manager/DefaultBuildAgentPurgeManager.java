package org.apache.continuum.buildagent.manager;

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

import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationException;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.model.LocalRepository;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutor;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutorException;
import org.apache.continuum.purge.executor.dir.DirectoryPurgeExecutorFactory;
import org.apache.continuum.purge.executor.repo.RepositoryPurgeExecutorFactory;
import org.apache.continuum.purge.repository.content.RepositoryManagedContent;
import org.apache.continuum.purge.repository.content.RepositoryManagedContentFactory;
import org.apache.continuum.utils.m2.LocalRepositoryHelper;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Component( role = org.apache.continuum.buildagent.manager.BuildAgentPurgeManager.class, hint = "default" )
public class DefaultBuildAgentPurgeManager
    implements BuildAgentPurgeManager
{
    private static final Logger log = LoggerFactory.getLogger( DefaultBuildAgentPurgeManager.class );

    @Requirement
    private BuildAgentConfigurationService buildAgentConfigurationService;

    @Requirement
    private DirectoryPurgeExecutorFactory dirExecutorFactory;

    @Requirement
    private RepositoryPurgeExecutorFactory repoExecutorFactory;

    @Requirement
    private RepositoryManagedContentFactory contentFactory;

    @Requirement
    private LocalRepositoryHelper localRepositoryHelper;

    public void executeDirectoryPurge( String directoryType, int daysOlder, int retentionCount, boolean deleteAll )
        throws ContinuumPurgeExecutorException
    {
        File directory = buildAgentConfigurationService.getWorkingDirectory();
        String path = directory.getAbsolutePath();
        ContinuumPurgeExecutor executor =
            dirExecutorFactory.create( deleteAll, daysOlder, retentionCount, directoryType );
        log.info( "purging directory '{}' [type={},full={},age={},retain={}]",
                  new Object[] { path, directoryType, deleteAll, daysOlder, retentionCount } );
        executor.purge( path );
        log.info( "purge completed '{}'", path );
    }

    public void executeRepositoryPurge( String repoName, int daysOlder, int retentionCount, boolean deleteAll,
                                        boolean deleteReleasedSnapshots )
        throws ContinuumPurgeExecutorException
    {
        try
        {
            LocalRepository localRepo = buildAgentConfigurationService.getLocalRepositoryByName( repoName );
            String path = localRepo.getLocation(), layout = localRepo.getLayout();
            RepositoryManagedContent managedContent = getManagedContent( localRepo );
            ContinuumPurgeExecutor executor = repoExecutorFactory.create( deleteAll, daysOlder, retentionCount,
                                                                          deleteReleasedSnapshots, managedContent );
            log.info( "purging repo '{}' [full={},age={},retain={},snapshots={},layout={}]",
                      new Object[] { path, deleteAll, daysOlder, retentionCount, deleteReleasedSnapshots, layout } );
            executor.purge( path );
            log.info( "purge completed '{}'", path );
        }
        catch ( BuildAgentConfigurationException e )
        {
            log.warn( "ignoring repository purge, check agent repo configuration: {}", e.getMessage() );
        }
    }

    private RepositoryManagedContent getManagedContent( LocalRepository localRepo )
        throws BuildAgentConfigurationException
    {
        String layout = localRepo.getLayout();
        try
        {
            RepositoryManagedContent managedContent = contentFactory.create( layout );
            managedContent.setRepository( localRepositoryHelper.convertAgentRepo( localRepo ) );
            return managedContent;
        }
        catch ( ComponentLookupException e )
        {
            throw new BuildAgentConfigurationException(
                String.format( "managed repo layout %s not found", layout ) );
        }
    }

    public void setBuildAgentConfigurationService( BuildAgentConfigurationService buildAgentConfigurationService )
    {
        this.buildAgentConfigurationService = buildAgentConfigurationService;
    }
}
