package org.apache.continuum.dao;

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

import org.apache.continuum.model.repository.DistributedDirectoryPurgeConfiguration;
import org.apache.maven.continuum.store.ContinuumStoreException;

/**
 * @author
 * @version $Id$
 */
public interface DistributedDirectoryPurgeConfigurationDao
{
    /**
     * Retrieve all DistributedDirectoryPurgeConfiguration instances.
     * 
     * @return list of all DistributedDirectoryPurgeConfiguration instances
     */
    List<DistributedDirectoryPurgeConfiguration> getAllDistributedDirectoryPurgeConfigurations();

    /**
     * Retrieve all DistributedDirectoryPurgeConfiguration instances associated with the input scheduleId.
     * 
     * @param scheduleId schedule id
     * 
     * @return list of all DistributedDirectoryPurgeConfiguration instances associated with the input scheduleId
     */
    List<DistributedDirectoryPurgeConfiguration> getDistributedDirectoryPurgeConfigurationsBySchedule( int scheduleId );

    /**
     * Retrieve all enabled DistributedDirectoryPurgeConfiguration instances associated with the input scheduleId.
     * 
     * @param scheduleId schedule id
     * 
     * @return list of all enabled DistributedDirectoryPurgeConfiguration instances associated with the input scheduleId
     */
    List<DistributedDirectoryPurgeConfiguration> getEnableDistributedDirectoryPurgeConfigurationsBySchedule( int scheduleId );

    /**
     * Retrieve all DistributedDirectoryPurgeConfiguration instances having the specified directory type.
     * 
     * @param type directory type
     * 
     * @return list of all DistributedDirectoryPurgeConfiguration instances having the specified directory type
     */
    List<DistributedDirectoryPurgeConfiguration> getDistributedDirectoryPurgeConfigurationsByType( String type );

    /**
     * Retrieve the DistributedDirectoryPurgeConfiguration instance associated with the input id.
     * 
     * @param configurationId DistributedDirectoryPurgeConfiguration instance id
     * 
     * @return DistributedDirectoryPurgeConfiguration instance
     * 
     * @throws ContinuumStoreException if unable to retrieve an instance associated with the input id
     */
    DistributedDirectoryPurgeConfiguration getDistributedDirectoryPurgeConfiguration( int configurationId )
        throws ContinuumStoreException;

    /**
     * Adds a new DistributedDirectoryPurgeConfiguration instance.
     * 
     * @param purgeConfiguration DistributedDirectoryPurgeConfiguration instance to be added
     * 
     * @return DistributedDirectoryPurgeConfiguration instance that was added
     * 
     * @throws ContinuumStoreException if unable to add the new instance
     */
    DistributedDirectoryPurgeConfiguration addDistributedDirectoryPurgeConfiguration( DistributedDirectoryPurgeConfiguration purgeConfiguration )
        throws ContinuumStoreException;

    /**
     * Updates the DistributedDirectoryPurgeConfiguration instance.
     * 
     * @param purgeConfiguration DistributedDirectoryPurgeConfiguration instance to be updated
     * 
     * @throws ContinuumStoreException if unable to update the instance
     */
    void updateDistributedDirectoryPurgeConfiguration( DistributedDirectoryPurgeConfiguration purgeConfiguration )
        throws ContinuumStoreException;

    /**
     * Removes the DistributedDirectoryPurgeConfiguration instance.
     * 
     * @param purgeConfiguration DistributedDirectoryPurgeConfiguration instance to be removed
     * 
     * @throws ContinuumStoreException if unable to remove the instance
     */
    void removeDistributedDirectoryPurgeConfiguration( DistributedDirectoryPurgeConfiguration purgeConfiguration )
        throws ContinuumStoreException;
}
