package org.apache.continuum.purge.task;

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

import org.apache.continuum.model.repository.AbstractPurgeConfiguration;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.DistributedDirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.DistributedRepositoryPurgeConfiguration;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.PurgeConfigurationService;
import org.apache.continuum.purge.controller.PurgeController;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutorException;
import org.apache.continuum.purge.repository.scanner.RepositoryScanner;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;

import java.io.File;

/**
 * @author Maria Catherine Tan
 */
@Component( role = org.codehaus.plexus.taskqueue.execution.TaskExecutor.class, hint = "purge" )
public class PurgeTaskExecutor
    implements TaskExecutor, Contextualizable
{

    @Requirement
    private PurgeConfigurationService purgeConfigurationService;

    @Requirement( hint = "repository-scanner" )
    private RepositoryScanner scanner;

    private PlexusContainer container;

    public void executeTask( Task task )
        throws TaskExecutionException
    {
        PurgeTask purgeTask = (PurgeTask) task;

        AbstractPurgeConfiguration purgeConfig = purgeConfigurationService.getPurgeConfiguration(
            purgeTask.getPurgeConfigurationId() );

        try
        {
            if ( purgeConfig != null && purgeConfig instanceof RepositoryPurgeConfiguration )
            {
                RepositoryPurgeConfiguration repoPurge = (RepositoryPurgeConfiguration) purgeConfig;

                LocalRepository repository = repoPurge.getRepository();

                if ( repository == null )
                {
                    throw new TaskExecutionException(
                        "Error while executing purge repository task: no repository set" );
                }

                PurgeController purgeController = (PurgeController) container.lookup( PurgeController.ROLE,
                                                                                      "purge-repository" );

                purgeController.initializeExecutors( repoPurge );

                if ( repoPurge.isDeleteAll() )
                {
                    purgeController.doPurge( repoPurge );
                }
                else
                {
                    File repoLocation = new File( repository.getLocation() ).getAbsoluteFile();
                    scanner.scan( repoLocation, purgeController );
                }
            }
            else if ( purgeConfig != null && purgeConfig instanceof DirectoryPurgeConfiguration )
            {
                DirectoryPurgeConfiguration dirPurge = (DirectoryPurgeConfiguration) purgeConfig;

                PurgeController purgeController = (PurgeController) container.lookup( PurgeController.ROLE,
                                                                                      "purge-directory" );

                purgeController.initializeExecutors( dirPurge );

                purgeController.doPurge( dirPurge.getLocation() );
            }
            else if ( purgeConfig instanceof DistributedDirectoryPurgeConfiguration )
            {
                DistributedDirectoryPurgeConfiguration dirPurge = (DistributedDirectoryPurgeConfiguration) purgeConfig;

                PurgeController purgeController = (PurgeController) container.lookup( PurgeController.ROLE,
                                                                                      "purge-distributed-directory" );

                purgeController.initializeExecutors( dirPurge );

                purgeController.doPurge( dirPurge );
            }
            else if ( purgeConfig instanceof DistributedRepositoryPurgeConfiguration )
            {
                DistributedRepositoryPurgeConfiguration dirPurge =
                    (DistributedRepositoryPurgeConfiguration) purgeConfig;

                PurgeController purgeController = (PurgeController) container.lookup( PurgeController.ROLE,
                                                                                      "purge-distributed-repository" );

                purgeController.initializeExecutors( dirPurge );

                purgeController.doPurge( dirPurge );
            }

        }
        catch ( ComponentLookupException e )
        {
            throw new TaskExecutionException( "Error while executing purge task", e );
        }
        catch ( ContinuumPurgeExecutorException e )
        {
            throw new TaskExecutionException( "Error while executing purge task", e );
        }
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}