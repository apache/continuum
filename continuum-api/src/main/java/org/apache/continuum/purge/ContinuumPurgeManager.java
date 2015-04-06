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

import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.DistributedDirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.DistributedRepositoryPurgeConfiguration;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.maven.continuum.model.project.Schedule;

/**
 * @author Maria Catherine Tan
 * @since 25 jul 07
 */
public interface ContinuumPurgeManager
{
    String ROLE = ContinuumPurgeManager.class.getName();

    /**
     * Purge repositories and directories
     *
     * @param schedule
     * @throws ContinuumPurgeManagerException
     */
    void purge( Schedule schedule )
        throws ContinuumPurgeManagerException;

    /**
     * Purge repository
     *
     * @param repoPurgeConfig
     * @throws ContinuumPurgeManagerException
     */
    void purgeRepository( RepositoryPurgeConfiguration repoPurgeConfig )
        throws ContinuumPurgeManagerException;

    /**
     * Purge directory
     *
     * @param dirPurgeConfig
     * @throws ContinuumPurgeManagerException
     */
    void purgeDirectory( DirectoryPurgeConfiguration dirPurgeConfig )
        throws ContinuumPurgeManagerException;

    /**
     * Purge directory in distributed build mode
     *
     * @param dirPurgeConfig distributed purge configuration
     * @throws ContinuumPurgeManagerException
     */
    void purgeDistributedDirectory( DistributedDirectoryPurgeConfiguration dirPurgeConfig )
        throws ContinuumPurgeManagerException;

    /**
     * Purge repository in distributed build mode
     *
     * @param repoPurgeConfig distributed purge configuration
     * @throws ContinuumPurgeManagerException
     */
    void purgeDistributedRepository( DistributedRepositoryPurgeConfiguration repoPurgeConfig )
        throws ContinuumPurgeManagerException;
}
