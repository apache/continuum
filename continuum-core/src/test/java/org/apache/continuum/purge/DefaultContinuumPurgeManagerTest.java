package org.apache.continuum.purge;

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

import org.apache.continuum.dao.DirectoryPurgeConfigurationDao;
import org.apache.continuum.dao.DistributedDirectoryPurgeConfigurationDao;
import org.apache.continuum.dao.LocalRepositoryDao;
import org.apache.continuum.dao.RepositoryPurgeConfigurationDao;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.DistributedDirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.task.PurgeTask;
import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.maven.continuum.AbstractContinuumTest;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;

/**
 * @author Maria Catherine Tan
 * @version $Id$
 * @since 25 jul 07
 */
public class DefaultContinuumPurgeManagerTest
    extends AbstractContinuumTest
{
    private LocalRepositoryDao localRepositoryDao;

    private DirectoryPurgeConfigurationDao directoryPurgeConfigurationDao;

    private DistributedDirectoryPurgeConfigurationDao distributedDirectoryPurgeConfigurationDao;

    private RepositoryPurgeConfigurationDao repositoryPurgeConfigurationDao;

    private ContinuumPurgeManager purgeManager;

    private TaskQueue purgeQueue;

    private RepositoryPurgeConfiguration repoPurge;

    private DirectoryPurgeConfiguration dirPurge;

    private DistributedDirectoryPurgeConfiguration distDirPurge;

    private TaskQueueManager taskQueueManager;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        localRepositoryDao = (LocalRepositoryDao) lookup( LocalRepositoryDao.class.getName() );

        directoryPurgeConfigurationDao = (DirectoryPurgeConfigurationDao) lookup(
            DirectoryPurgeConfigurationDao.class.getName() );

        repositoryPurgeConfigurationDao = (RepositoryPurgeConfigurationDao) lookup(
            RepositoryPurgeConfigurationDao.class.getName() );

        distributedDirectoryPurgeConfigurationDao = (DistributedDirectoryPurgeConfigurationDao) lookup(
            DistributedDirectoryPurgeConfigurationDao.class.getName() );

        purgeManager = (ContinuumPurgeManager) lookup( ContinuumPurgeManager.ROLE );

        purgeQueue = (TaskQueue) lookup( TaskQueue.ROLE, "purge" );

        taskQueueManager = (TaskQueueManager) lookup( TaskQueueManager.ROLE );

        setupDefaultPurgeConfigurations();
    }

    public void testPurgingWithSinglePurgeConfiguration()
        throws Exception
    {
        purgeManager.purgeRepository( repoPurge );

        assertNextBuildIs( repoPurge.getId() );
        assertNextBuildIsNull();

        purgeManager.purgeRepository( repoPurge );
        purgeManager.purgeRepository( repoPurge );
        purgeManager.purgeRepository( repoPurge );
        purgeManager.purgeRepository( repoPurge );
        purgeManager.purgeRepository( repoPurge );

        assertNextBuildIs( repoPurge.getId() );
        assertNextBuildIsNull();
    }

    public void testPurgingWithMultiplePurgeConfiguration()
        throws Exception
    {
        purgeManager.purgeRepository( repoPurge );
        purgeManager.purgeDirectory( dirPurge );

        assertNextBuildIs( repoPurge.getId() );
        assertNextBuildIs( dirPurge.getId() );
        assertNextBuildIsNull();

        for ( int i = 0; i < 5; i++ )
        {
            purgeManager.purgeRepository( repoPurge );
            purgeManager.purgeDirectory( dirPurge );
        }

        assertNextBuildIs( repoPurge.getId() );
        assertNextBuildIs( dirPurge.getId() );
        assertNextBuildIsNull();
    }

    public void testRemoveFromPurgeQueue()
        throws Exception
    {
        purgeManager.purgeRepository( repoPurge );
        purgeManager.purgeDirectory( dirPurge );
        purgeManager.purgeDistributedDirectory( distDirPurge );

        assertNextBuildIs( repoPurge.getId() );
        assertNextBuildIs( dirPurge.getId() );
        assertNextBuildIs( distDirPurge.getId() );
        assertNextBuildIsNull();

        purgeManager.purgeRepository( repoPurge );
        purgeManager.purgeDirectory( dirPurge );
        taskQueueManager.removeFromPurgeQueue( repoPurge.getId() );

        assertNextBuildIs( dirPurge.getId() );
        assertNextBuildIsNull();

        purgeManager.purgeRepository( repoPurge );
        purgeManager.purgeDirectory( dirPurge );
        taskQueueManager.removeFromPurgeQueue( dirPurge.getId() );

        assertNextBuildIs( repoPurge.getId() );
        assertNextBuildIsNull();
    }

    private void setupDefaultPurgeConfigurations()
        throws Exception
    {
        LocalRepository repository = new LocalRepository();
        repository.setName( "defaultRepo" );
        repository.setLocation( getTestFile( "target/default-repository" ).getAbsolutePath() );
        repository = localRepositoryDao.addLocalRepository( repository );

        repoPurge = new RepositoryPurgeConfiguration();
        repoPurge.setRepository( repository );
        repoPurge = repositoryPurgeConfigurationDao.addRepositoryPurgeConfiguration( repoPurge );

        dirPurge = new DirectoryPurgeConfiguration();
        dirPurge.setDirectoryType( "releases" );
        dirPurge.setLocation( getTestFile( "target/working-directory" ).getAbsolutePath() );
        dirPurge = directoryPurgeConfigurationDao.addDirectoryPurgeConfiguration( dirPurge );

        distDirPurge = new DistributedDirectoryPurgeConfiguration();
        distDirPurge.setDirectoryType( "releases" );
        distDirPurge.setBuildAgentUrl( "http://localhost:8186/continuum-buildagent/xmlrpc" );
        distDirPurge = distributedDirectoryPurgeConfigurationDao.addDistributedDirectoryPurgeConfiguration(
            distDirPurge );
    }

    private void assertNextBuildIs( int expectedPurgeConfigId )
        throws Exception
    {
        Task task = purgeQueue.take();

        assertEquals( PurgeTask.class.getName(), task.getClass().getName() );

        PurgeTask purgeTask = (PurgeTask) task;

        assertEquals( "Didn't get the expected purge config id.", expectedPurgeConfigId,
                      purgeTask.getPurgeConfigurationId() );
    }

    private void assertNextBuildIsNull()
        throws Exception
    {
        Task task = purgeQueue.take();

        if ( task != null )
        {
            fail( "Got a non-null purge task returned. Purge Config id: " +
                      ( (PurgeTask) task ).getPurgeConfigurationId() );
        }
    }
}