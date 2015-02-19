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

import org.apache.continuum.model.repository.AbstractPurgeConfiguration;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.DistributedDirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.repository.content.RepositoryManagedContent;

import java.util.List;

/**
 * @author Maria Catherine Tan
 * @since 25 jul 07
 */
public interface PurgeConfigurationService
{
    String ROLE = PurgeConfigurationService.class.getName();

    AbstractPurgeConfiguration addPurgeConfiguration( AbstractPurgeConfiguration purgeConfig )
        throws PurgeConfigurationServiceException;

    void updatePurgeConfiguration( AbstractPurgeConfiguration purgeConfig )
        throws PurgeConfigurationServiceException;

    void removePurgeConfiguration( int purgeConfigId )
        throws PurgeConfigurationServiceException;

    RepositoryPurgeConfiguration addRepositoryPurgeConfiguration( RepositoryPurgeConfiguration repoPurge )
        throws PurgeConfigurationServiceException;

    void updateRepositoryPurgeConfiguration( RepositoryPurgeConfiguration repoPurge )
        throws PurgeConfigurationServiceException;

    void removeRepositoryPurgeConfiguration( RepositoryPurgeConfiguration repoPurge )
        throws PurgeConfigurationServiceException;

    RepositoryPurgeConfiguration getRepositoryPurgeConfiguration( int repoPurgeId )
        throws PurgeConfigurationServiceException;

    RepositoryPurgeConfiguration getDefaultPurgeConfigurationForRepository( int repositoryId );

    List<RepositoryPurgeConfiguration> getRepositoryPurgeConfigurationsBySchedule( int scheduleId );

    List<RepositoryPurgeConfiguration> getEnableRepositoryPurgeConfigurationsBySchedule( int scheduleId );

    List<RepositoryPurgeConfiguration> getRepositoryPurgeConfigurationsByRepository( int repositoryId );

    List<RepositoryPurgeConfiguration> getAllRepositoryPurgeConfigurations();

    DirectoryPurgeConfiguration addDirectoryPurgeConfiguration( DirectoryPurgeConfiguration dirPurge )
        throws PurgeConfigurationServiceException;

    void updateDirectoryPurgeConfiguration( DirectoryPurgeConfiguration dirPurge )
        throws PurgeConfigurationServiceException;

    void removeDirectoryPurgeConfiguration( DirectoryPurgeConfiguration dirPurge )
        throws PurgeConfigurationServiceException;

    DirectoryPurgeConfiguration getDirectoryPurgeConfiguration( int dirPurgeId )
        throws PurgeConfigurationServiceException;

    DirectoryPurgeConfiguration getDefaultPurgeConfigurationForDirectoryType( String directoryType );

    List<DirectoryPurgeConfiguration> getDirectoryPurgeConfigurationsBySchedule( int scheduleId );

    List<DirectoryPurgeConfiguration> getEnableDirectoryPurgeConfigurationsBySchedule( int scheduleId );

    List<DirectoryPurgeConfiguration> getDirectoryPurgeConfigurationsByLocation( String location );

    List<DirectoryPurgeConfiguration> getAllDirectoryPurgeConfigurations();

    List<AbstractPurgeConfiguration> getAllPurgeConfigurations();

    List<DistributedDirectoryPurgeConfiguration> getAllDistributedDirectoryPurgeConfigurations();

    DistributedDirectoryPurgeConfiguration getDistributedDirectoryPurgeConfiguration( int dirPurgeId )
        throws PurgeConfigurationServiceException;

    DistributedDirectoryPurgeConfiguration addDistributedDirectoryPurgeConfiguration(
        DistributedDirectoryPurgeConfiguration dirPurge )
        throws PurgeConfigurationServiceException;

    void updateDistributedDirectoryPurgeConfiguration( DistributedDirectoryPurgeConfiguration dirPurge )
        throws PurgeConfigurationServiceException;

    void removeDistributedDirectoryPurgeConfiguration( DistributedDirectoryPurgeConfiguration dirPurge )
        throws PurgeConfigurationServiceException;

    AbstractPurgeConfiguration getPurgeConfiguration( int purgeConfigId );

    List<DistributedDirectoryPurgeConfiguration> getEnableDistributedDirectoryPurgeConfigurationsBySchedule(
        int scheduleId );

    /**
     * @param repositoryId
     * @return
     * @throws PurgeConfigurationServiceException
     *
     */
    RepositoryManagedContent getManagedRepositoryContent( int repositoryId )
        throws PurgeConfigurationServiceException;
}
