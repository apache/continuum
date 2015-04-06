package org.apache.continuum.dao;

import org.apache.continuum.model.repository.DistributedRepositoryPurgeConfiguration;
import org.apache.maven.continuum.store.ContinuumStoreException;

import java.util.List;

public interface DistributedRepositoryPurgeConfigurationDao
{
    /**
     * Retrieve all DistributedDirectoryPurgeConfiguration instances.
     *
     * @return list of all DistributedDirectoryPurgeConfiguration instances
     */
    List<DistributedRepositoryPurgeConfiguration> getAllDistributedRepositoryPurgeConfigurations();

    /**
     * Adds a new DistributedRepositoryPurgeConfiguration instance.
     *
     * @param purgeConfiguration DistributedRepositoryPurgeConfiguration instance to be added
     * @return DistributedRepositoryPurgeConfiguration instance that was added
     * @throws ContinuumStoreException if unable to add the new instance
     */
    DistributedRepositoryPurgeConfiguration addDistributedRepositoryPurgeConfiguration(
        DistributedRepositoryPurgeConfiguration purgeConfiguration )
        throws ContinuumStoreException;

    /**
     * Removes an existing DistributedRepositoryPurgeConfiguration instance.
     *
     * @param purgeConfig
     */
    void removeDistributedRepositoryPurgeConfiguration( DistributedRepositoryPurgeConfiguration purgeConfig )
        throws ContinuumStoreException;

    /**
     * Retrieves an existing configuration object.
     *
     * @param dirPurgeId
     * @return
     * @throws ContinuumStoreException
     */
    DistributedRepositoryPurgeConfiguration getDistributedRepositoryPurgeConfiguration( int dirPurgeId )
        throws ContinuumStoreException;

    /**
     * Updates an existing configuration object.
     *
     * @param purgeConfig
     * @throws ContinuumStoreException
     */
    void updateDistributedRepositoryPurgeConfiguration( DistributedRepositoryPurgeConfiguration purgeConfig )
        throws ContinuumStoreException;

    List<DistributedRepositoryPurgeConfiguration> getEnableDistributedRepositoryPurgeConfigurationsBySchedule(
        int scheduleId );
}
