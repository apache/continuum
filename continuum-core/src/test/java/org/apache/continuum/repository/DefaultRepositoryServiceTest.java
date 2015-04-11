package org.apache.continuum.repository;

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

import org.apache.continuum.dao.RepositoryPurgeConfigurationDao;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.model.project.ProjectGroup;

import java.util.List;

/**
 * @author Maria Catherine Tan
 * @since 25 jul 07
 */
public class DefaultRepositoryServiceTest
    extends AbstractContinuumTest
{
    private RepositoryPurgeConfigurationDao repositoryPurgeConfigurationDao;

    private RepositoryService repositoryService;

    private LocalRepository repository;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        repositoryPurgeConfigurationDao =
            (RepositoryPurgeConfigurationDao) lookup( RepositoryPurgeConfigurationDao.class );

        repositoryService = (RepositoryService) lookup( RepositoryService.class );
    }

    public void testRemoveRepository()
        throws Exception
    {
        setupDefaultRepository();

        repositoryService.removeLocalRepository( repository.getId() );

        List<LocalRepository> repositories = repositoryService.getAllLocalRepositories();
        assertEquals( "check # repositories", 0, repositories.size() );

        ProjectGroup group = getDefaultProjectGroup();
        assertNull( group.getLocalRepository() );

        List<RepositoryPurgeConfiguration> purgeConfigs =
            repositoryPurgeConfigurationDao.getRepositoryPurgeConfigurationsByLocalRepository( repository.getId() );
        assertEquals( "check # purge configs of repository", 0, purgeConfigs.size() );
    }

    private void setupDefaultRepository()
        throws Exception
    {
        repository = new LocalRepository();
        repository.setName( "DefaultRepo" );
        repository.setLocation( getTestFile( "target/default-repo" ).getAbsolutePath() );
        repository = repositoryService.addLocalRepository( repository );

        ProjectGroup group = getDefaultProjectGroup();
        group.setLocalRepository( repository );
        getProjectGroupDao().updateProjectGroup( group );

        RepositoryPurgeConfiguration repoConfig = new RepositoryPurgeConfiguration();
        repoConfig.setRepository( repository );
        repoConfig = repositoryPurgeConfigurationDao.addRepositoryPurgeConfiguration( repoConfig );

        List<LocalRepository> repositories = repositoryService.getAllLocalRepositories();
        assertEquals( "check # repositories", 1, repositories.size() );
        assertTrue( "check if repository was added", repositories.contains( repository ) );

        LocalRepository repo = repositoryService.getLocalRepositoryByName( "DefaultRepo" );
        assertNotNull( repo );
        assertEquals( "check if repository name is the same", repository.getName(), repo.getName() );

        repo = repositoryService.getLocalRepositoryByLocation( repository.getLocation() );
        assertNotNull( repo );
        assertEquals( "check if repository location is the same", repository.getLocation(), repo.getLocation() );

        ProjectGroup retrievedGroup = getDefaultProjectGroup();
        assertNotNull( retrievedGroup.getLocalRepository() );
        assertEquals( "check if repository is the same", repository, retrievedGroup.getLocalRepository() );

        List<RepositoryPurgeConfiguration> purgeConfigs =
            repositoryPurgeConfigurationDao.getRepositoryPurgeConfigurationsByLocalRepository( repository.getId() );
        assertEquals( "check # purge configs found", 1, purgeConfigs.size() );
        assertEquals( "check if purge configuration is the same", repoConfig, purgeConfigs.get( 0 ) );
    }
}