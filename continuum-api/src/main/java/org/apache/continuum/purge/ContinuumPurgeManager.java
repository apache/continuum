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
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.maven.continuum.model.project.Schedule;

/**
 * @author Maria Catherine Tan
 * @version $Id$
 * @since 25 jul 07
 */
public interface ContinuumPurgeManager
{   
    String ROLE = ContinuumPurgeManager.class.getName();
    
    /**
     * Purge repositories and directories 
     * @param schedule
     * @throws ContinuumPurgeManagerException
     */
    void purge( Schedule schedule )
        throws ContinuumPurgeManagerException;
    
    /**
     * Purge repository
     * @param repoPurgeConfig
     * @throws ContinuumPurgeManagerException
     */
    void purgeRepository( RepositoryPurgeConfiguration repoPurgeConfig )
        throws ContinuumPurgeManagerException;
    
    /**
     * Purge directory 
     * @param dirPurgeConfig
     * @throws ContinuumPurgeManagerException
     */
    void purgeDirectory( DirectoryPurgeConfiguration dirPurgeConfig )
        throws ContinuumPurgeManagerException;
    /*
    /**
     * Check if the repository is already in the purging queue
     * 
     * @param repositoryId the id of the repository purge configuration
     * @return true if the repository is in the purging queue, otherwise false
     * @throws ContinuumPurgeManagerException
     */
/*    boolean isRepositoryInPurgeQueue( int repositoryId )
        throws ContinuumPurgeManagerException;
    
    /**
     * Check if the repository is being used by a project that is currently building
     * 
     * @param repositoryId the id of the local repository
     * @return true if the repository is in use, otherwise false
     * @throws ContinuumPurgeManagerException
     */
/*    boolean isRepositoryInUse( int repositoryId )
        throws ContinuumPurgeManagerException;

    /**
     * Remove local repository from the purge queue
     * 
     * @param repositoryId the id of the local repository
     * @throws ContinuumPurgeManagerException
     */
/*    void removeRepositoryFromPurgeQueue( int repositoryId )
        throws ContinuumPurgeManagerException;
    
    /**
     * Remove local repository from the purge queue
     * 
     * @param purgeConfigId the id of the purge configuration
     * @return true if the purge configuration was successfully removed from the purge queue, otherwise false
     * @throws ContinuumPurgeManagerException
     */
/*    boolean removeFromPurgeQueue( int purgeConfigId )
        throws ContinuumPurgeManagerException;
    
    /**
     * Remove local repositories from the purge queue
     * 
     * @param purgeConfigIds the ids of the purge configuration
     * @return true if the purge configurations were successfully removed from the purge queue, otherwise false
     * @throws ContinuumPurgeManagerException
     */
/*    boolean removeFromPurgeQueue( int[] purgeConfigIds )
        throws ContinuumPurgeManagerException;*/
}
