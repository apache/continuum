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

import java.util.List;

import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.repository.content.ManagedDefaultRepositoryContent;
import org.apache.continuum.purge.repository.content.RepositoryManagedContent;

/**
 * @author Maria Catherine Tan
 */
public class DefaultPurgeConfigurationServiceTest
    extends AbstractPurgeTest
{
    private PurgeConfigurationService purgeConfigurationService;
    
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        purgeConfigurationService = (PurgeConfigurationService) lookup( PurgeConfigurationService.ROLE );
    }
    
    public void testRepositoryPurgeConfiguration()
        throws Exception
    {
        RepositoryPurgeConfiguration repoConfig = new RepositoryPurgeConfiguration();
        
        repoConfig.setRepository( defaultRepository );
        repoConfig.setDaysOlder( TEST_DAYS_OLDER );
        repoConfig.setRetentionCount( TEST_RETENTION_COUNT );
        
        repoConfig = purgeConfigurationService.addRepositoryPurgeConfiguration( repoConfig );
        
        assertNotNull( repoConfig );
        
        RepositoryPurgeConfiguration retrieved = getStore().getRepositoryPurgeConfiguration( repoConfig.getId() );
        assertEquals( repoConfig, retrieved );
        
        purgeConfigurationService.removeRepositoryPurgeConfiguration( repoConfig );
        
        List<RepositoryPurgeConfiguration> repoConfigs = purgeConfigurationService.getAllRepositoryPurgeConfigurations();
        
        assertFalse( "check if repo purge configuration was removed", repoConfigs.contains( repoConfig ) );
        assertNotNull( "check if repository still exists", getStore().getLocalRepository( defaultRepository.getId() ) );
    }
    
    public void testDirectoryPurgeConfiguration()
        throws Exception
    {
        DirectoryPurgeConfiguration dirConfig = new DirectoryPurgeConfiguration();
        
        dirConfig.setLocation( getReleasesDirectoryLocation().getAbsolutePath() );
        dirConfig.setDirectoryType( TEST_RELEASES_DIRECTORY_TYPE );
        dirConfig.setDaysOlder( TEST_DAYS_OLDER );
        dirConfig.setRetentionCount( TEST_RETENTION_COUNT );
        
        dirConfig = purgeConfigurationService.addDirectoryPurgeConfiguration( dirConfig );
        
        assertNotNull( dirConfig );
        
        DirectoryPurgeConfiguration retrieved = getStore().getDirectoryPurgeConfiguration( dirConfig.getId() );
        assertEquals( dirConfig, retrieved );
        
        dirConfig.setDirectoryType( TEST_BUILDOUTPUT_DIRECTORY_TYPE );
        purgeConfigurationService.updateDirectoryPurgeConfiguration( dirConfig );
        retrieved = getStore().getDirectoryPurgeConfiguration( dirConfig.getId() );
        assertEquals( dirConfig, retrieved );
        
        purgeConfigurationService.removeDirectoryPurgeConfiguration( dirConfig );
        
        List<DirectoryPurgeConfiguration> dirConfigs = purgeConfigurationService.getAllDirectoryPurgeConfigurations();
        assertFalse( "check if dir purge configuration was removed", dirConfigs.contains( dirConfig ) );
    }
    
    public void testRepositoryManagedContent()
        throws Exception
    {
        RepositoryManagedContent repo = purgeConfigurationService.getManagedRepositoryContent( defaultRepository.getId() );
        
        assertTrue( "check repository managed content", ( repo instanceof ManagedDefaultRepositoryContent ) );
        assertEquals( "check repository of the managed content", defaultRepository, repo.getRepository() );
    }
}
