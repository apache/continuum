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
import org.apache.continuum.dao.DistributedRepositoryPurgeConfigurationDao;
import org.apache.continuum.dao.LocalRepositoryDao;
import org.apache.continuum.dao.RepositoryPurgeConfigurationDao;
import org.apache.continuum.model.repository.AbstractPurgeConfiguration;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.DistributedDirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.DistributedRepositoryPurgeConfiguration;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.repository.content.RepositoryManagedContent;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

import java.util.ArrayList;
import java.util.List;

/**
 * DefaultPurgeConfigurationService
 *
 * @author Maria Catherine Tan
 * @since 25 jul 07
 */
@Component( role = org.apache.continuum.purge.PurgeConfigurationService.class, hint = "default" )
public class DefaultPurgeConfigurationService
    implements PurgeConfigurationService, Contextualizable
{

    @Requirement
    private DirectoryPurgeConfigurationDao directoryPurgeConfigurationDao;

    @Requirement
    private LocalRepositoryDao localRepositoryDao;

    @Requirement
    private RepositoryPurgeConfigurationDao repositoryPurgeConfigurationDao;

    @Requirement
    private DistributedDirectoryPurgeConfigurationDao distributedDirectoryPurgeConfigurationDao;

    @Requirement
    private DistributedRepositoryPurgeConfigurationDao distributedRepositoryPurgeConfigurationDao;

    private PlexusContainer container;

    public AbstractPurgeConfiguration addPurgeConfiguration( AbstractPurgeConfiguration purgeConfig )
        throws PurgeConfigurationServiceException
    {
        AbstractPurgeConfiguration purgeConfiguration = null;

        if ( purgeConfig instanceof RepositoryPurgeConfiguration )
        {
            purgeConfiguration = addRepositoryPurgeConfiguration( (RepositoryPurgeConfiguration) purgeConfig );
        }
        else if ( purgeConfig instanceof DirectoryPurgeConfiguration )
        {
            purgeConfiguration = addDirectoryPurgeConfiguration( (DirectoryPurgeConfiguration) purgeConfig );
        }
        else if ( purgeConfig instanceof DistributedDirectoryPurgeConfiguration )
        {
            purgeConfiguration = addDistributedDirectoryPurgeConfiguration(
                (DistributedDirectoryPurgeConfiguration) purgeConfig );
        }
        else if ( purgeConfig instanceof DistributedRepositoryPurgeConfiguration )
        {
            purgeConfiguration =
                addDistributedRepositoryPurgeConfiguration( (DistributedRepositoryPurgeConfiguration) purgeConfig );
        }

        return purgeConfiguration;
    }

    public void updatePurgeConfiguration( AbstractPurgeConfiguration purgeConfig )
        throws PurgeConfigurationServiceException
    {
        if ( purgeConfig instanceof RepositoryPurgeConfiguration )
        {
            updateRepositoryPurgeConfiguration( (RepositoryPurgeConfiguration) purgeConfig );
        }
        else if ( purgeConfig instanceof DirectoryPurgeConfiguration )
        {
            updateDirectoryPurgeConfiguration( (DirectoryPurgeConfiguration) purgeConfig );
        }
        else if ( purgeConfig instanceof DistributedDirectoryPurgeConfiguration )
        {
            updateDistributedDirectoryPurgeConfiguration( (DistributedDirectoryPurgeConfiguration) purgeConfig );
        }
        else if ( purgeConfig instanceof DistributedRepositoryPurgeConfiguration )
        {
            updateDistributedRepositoryPurgeConfiguration( (DistributedRepositoryPurgeConfiguration) purgeConfig );
        }
    }

    public void removePurgeConfiguration( int purgeConfigId )
        throws PurgeConfigurationServiceException
    {
        AbstractPurgeConfiguration purgeConfig = getPurgeConfiguration( purgeConfigId );

        if ( purgeConfig instanceof RepositoryPurgeConfiguration )
        {
            removeRepositoryPurgeConfiguration( (RepositoryPurgeConfiguration) purgeConfig );
        }
        else if ( purgeConfig instanceof DirectoryPurgeConfiguration )
        {
            removeDirectoryPurgeConfiguration( (DirectoryPurgeConfiguration) purgeConfig );
        }
        else if ( purgeConfig instanceof DistributedDirectoryPurgeConfiguration )
        {
            removeDistributedDirectoryPurgeConfiguration( (DistributedDirectoryPurgeConfiguration) purgeConfig );
        }
        else if ( purgeConfig instanceof DistributedRepositoryPurgeConfiguration )
        {
            removeDistributedRepositoryPurgeConfiguration( (DistributedRepositoryPurgeConfiguration) purgeConfig );
        }
    }

    public DirectoryPurgeConfiguration addDirectoryPurgeConfiguration( DirectoryPurgeConfiguration dirPurge )
        throws PurgeConfigurationServiceException
    {
        DirectoryPurgeConfiguration dirPurgeConfig;

        try
        {
            dirPurgeConfig = directoryPurgeConfigurationDao.addDirectoryPurgeConfiguration( dirPurge );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }

        return dirPurgeConfig;
    }

    public RepositoryPurgeConfiguration addRepositoryPurgeConfiguration( RepositoryPurgeConfiguration repoPurge )
        throws PurgeConfigurationServiceException
    {
        RepositoryPurgeConfiguration repoPurgeConfig;

        try
        {
            repoPurgeConfig = repositoryPurgeConfigurationDao.addRepositoryPurgeConfiguration( repoPurge );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }

        return repoPurgeConfig;
    }

    public RepositoryPurgeConfiguration getDefaultPurgeConfigurationForRepository( int repositoryId )
    {
        List<RepositoryPurgeConfiguration> purgeConfigs = getRepositoryPurgeConfigurationsByRepository( repositoryId );

        for ( RepositoryPurgeConfiguration purgeConfig : purgeConfigs )
        {
            if ( purgeConfig.isDefaultPurge() )
            {
                return purgeConfig;
            }
        }

        return null;
    }

    public List<DirectoryPurgeConfiguration> getAllDirectoryPurgeConfigurations()
    {
        return directoryPurgeConfigurationDao.getAllDirectoryPurgeConfigurations();
    }

    public List<RepositoryPurgeConfiguration> getAllRepositoryPurgeConfigurations()
    {
        return repositoryPurgeConfigurationDao.getAllRepositoryPurgeConfigurations();
    }

    public List<AbstractPurgeConfiguration> getAllPurgeConfigurations()
    {
        List<RepositoryPurgeConfiguration> repoPurge = getAllRepositoryPurgeConfigurations();
        List<DirectoryPurgeConfiguration> dirPurge = getAllDirectoryPurgeConfigurations();

        List<AbstractPurgeConfiguration> allPurgeConfigs = new ArrayList<AbstractPurgeConfiguration>();

        allPurgeConfigs.addAll( repoPurge );
        allPurgeConfigs.addAll( dirPurge );

        return allPurgeConfigs;
    }

    public DirectoryPurgeConfiguration getDefaultPurgeConfigurationForDirectoryType( String directoryType )
    {
        List<DirectoryPurgeConfiguration> purgeConfigs =
            directoryPurgeConfigurationDao.getDirectoryPurgeConfigurationsByType( directoryType );

        for ( DirectoryPurgeConfiguration purgeConfig : purgeConfigs )
        {
            if ( purgeConfig.isDefaultPurge() )
            {
                return purgeConfig;
            }
        }

        return null;
    }

    public List<DirectoryPurgeConfiguration> getDirectoryPurgeConfigurationsByLocation( String location )
    {
        return directoryPurgeConfigurationDao.getDirectoryPurgeConfigurationsByLocation( location );
    }

    public List<DirectoryPurgeConfiguration> getDirectoryPurgeConfigurationsBySchedule( int scheduleId )
    {
        return directoryPurgeConfigurationDao.getDirectoryPurgeConfigurationsBySchedule( scheduleId );
    }

    public List<DirectoryPurgeConfiguration> getEnableDirectoryPurgeConfigurationsBySchedule( int scheduleId )
    {
        return directoryPurgeConfigurationDao.getEnableDirectoryPurgeConfigurationsBySchedule( scheduleId );
    }

    public List<RepositoryPurgeConfiguration> getRepositoryPurgeConfigurationsByRepository( int repositoryId )
    {
        return repositoryPurgeConfigurationDao.getRepositoryPurgeConfigurationsByLocalRepository( repositoryId );
    }

    public List<RepositoryPurgeConfiguration> getRepositoryPurgeConfigurationsBySchedule( int scheduleId )
    {
        return repositoryPurgeConfigurationDao.getRepositoryPurgeConfigurationsBySchedule( scheduleId );
    }

    public List<RepositoryPurgeConfiguration> getEnableRepositoryPurgeConfigurationsBySchedule( int scheduleId )
    {
        return repositoryPurgeConfigurationDao.getEnableRepositoryPurgeConfigurationsBySchedule( scheduleId );
    }

    public void removeDirectoryPurgeConfiguration( DirectoryPurgeConfiguration purgeConfig )
        throws PurgeConfigurationServiceException
    {
        try
        {
            directoryPurgeConfigurationDao.removeDirectoryPurgeConfiguration( purgeConfig );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
    }

    public void removeRepositoryPurgeConfiguration( RepositoryPurgeConfiguration purgeConfig )
        throws PurgeConfigurationServiceException
    {
        try
        {
            repositoryPurgeConfigurationDao.removeRepositoryPurgeConfiguration( purgeConfig );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
    }

    public void updateDirectoryPurgeConfiguration( DirectoryPurgeConfiguration purgeConfig )
        throws PurgeConfigurationServiceException
    {
        try
        {
            directoryPurgeConfigurationDao.updateDirectoryPurgeConfiguration( purgeConfig );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
    }

    public void updateRepositoryPurgeConfiguration( RepositoryPurgeConfiguration purgeConfig )
        throws PurgeConfigurationServiceException
    {
        try
        {
            repositoryPurgeConfigurationDao.updateRepositoryPurgeConfiguration( purgeConfig );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
    }

    public DirectoryPurgeConfiguration getDirectoryPurgeConfiguration( int purgeConfigId )
        throws PurgeConfigurationServiceException
    {
        try
        {
            return directoryPurgeConfigurationDao.getDirectoryPurgeConfiguration( purgeConfigId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
    }

    public RepositoryPurgeConfiguration getRepositoryPurgeConfiguration( int purgeConfigId )
        throws PurgeConfigurationServiceException
    {
        try
        {
            return repositoryPurgeConfigurationDao.getRepositoryPurgeConfiguration( purgeConfigId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
    }

    public AbstractPurgeConfiguration getPurgeConfiguration( int purgeConfigId )
    {
        AbstractPurgeConfiguration purgeConfig = null;

        try
        {
            purgeConfig = getRepositoryPurgeConfiguration( purgeConfigId );
        }
        catch ( PurgeConfigurationServiceException e )
        {
            // purgeConfigId is not of type repository purge configuration
        }

        if ( purgeConfig == null )
        {
            try
            {
                purgeConfig = getDirectoryPurgeConfiguration( purgeConfigId );
            }
            catch ( PurgeConfigurationServiceException e )
            {
                // purgeConfigId is not of type directory purge configuration
            }
        }

        if ( purgeConfig == null )
        {
            try
            {
                purgeConfig = getDistributedDirectoryPurgeConfiguration( purgeConfigId );
            }
            catch ( PurgeConfigurationServiceException e )
            {
                // purgeConfigId is not of type directory purge configuration
            }
        }

        if ( purgeConfig == null )
        {
            try
            {
                purgeConfig = getDistributedRepositoryPurgeConfiguration( purgeConfigId );
            }
            catch ( PurgeConfigurationServiceException e )
            {
                // purgeConfigId is not of type directory purge configuration
            }
        }

        return purgeConfig;
    }

    public RepositoryManagedContent getManagedRepositoryContent( int repositoryId )
        throws PurgeConfigurationServiceException
    {
        try
        {
            LocalRepository repository = localRepositoryDao.getLocalRepository( repositoryId );

            RepositoryManagedContent repoContent;

            repoContent = (RepositoryManagedContent) container.lookup( RepositoryManagedContent.class,
                                                                       repository.getLayout() );
            repoContent.setRepository( repository );

            return repoContent;
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new PurgeConfigurationServiceException(
                "Error retrieving managed repository content for: " + repositoryId, e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException(
                "Error retrieving managed repository content for: " + repositoryId, e );
        }
        catch ( ComponentLookupException e )
        {
            throw new PurgeConfigurationServiceException(
                "Error retrieving managed repository content for: " + repositoryId, e );
        }
    }

    public List<DistributedDirectoryPurgeConfiguration> getAllDistributedDirectoryPurgeConfigurations()
    {
        return distributedDirectoryPurgeConfigurationDao.getAllDistributedDirectoryPurgeConfigurations();
    }

    public List<DistributedRepositoryPurgeConfiguration> getAllDistributedRepositoryPurgeConfigurations()
    {
        return distributedRepositoryPurgeConfigurationDao.getAllDistributedRepositoryPurgeConfigurations();
    }

    public DistributedDirectoryPurgeConfiguration addDistributedDirectoryPurgeConfiguration(
        DistributedDirectoryPurgeConfiguration dirPurge )
        throws PurgeConfigurationServiceException
    {
        try
        {
            return distributedDirectoryPurgeConfigurationDao.addDistributedDirectoryPurgeConfiguration( dirPurge );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
    }

    public DistributedRepositoryPurgeConfiguration addDistributedRepositoryPurgeConfiguration(
        DistributedRepositoryPurgeConfiguration repoPurge )
        throws PurgeConfigurationServiceException
    {
        try
        {
            return distributedRepositoryPurgeConfigurationDao.addDistributedRepositoryPurgeConfiguration(
                repoPurge );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
    }

    public DistributedDirectoryPurgeConfiguration getDistributedDirectoryPurgeConfiguration( int dirPurgeId )
        throws PurgeConfigurationServiceException
    {
        try
        {
            return distributedDirectoryPurgeConfigurationDao.getDistributedDirectoryPurgeConfiguration( dirPurgeId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
    }

    public void updateDistributedDirectoryPurgeConfiguration( DistributedDirectoryPurgeConfiguration dirPurge )
        throws PurgeConfigurationServiceException
    {
        try
        {
            distributedDirectoryPurgeConfigurationDao.updateDistributedDirectoryPurgeConfiguration( dirPurge );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
    }

    private void updateDistributedRepositoryPurgeConfiguration( DistributedRepositoryPurgeConfiguration purgeConfig )
        throws PurgeConfigurationServiceException
    {
        try
        {
            distributedRepositoryPurgeConfigurationDao.updateDistributedRepositoryPurgeConfiguration( purgeConfig );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
    }

    public void removeDistributedDirectoryPurgeConfiguration( DistributedDirectoryPurgeConfiguration purgeConfig )
        throws PurgeConfigurationServiceException
    {
        try
        {
            distributedDirectoryPurgeConfigurationDao.removeDistributedDirectoryPurgeConfiguration( purgeConfig );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
    }

    public void removeDistributedRepositoryPurgeConfiguration( DistributedRepositoryPurgeConfiguration purgeConfig )
        throws PurgeConfigurationServiceException
    {
        try
        {
            distributedRepositoryPurgeConfigurationDao.removeDistributedRepositoryPurgeConfiguration( purgeConfig );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
    }

    public DistributedRepositoryPurgeConfiguration getDistributedRepositoryPurgeConfiguration( int dirPurgeId )
        throws PurgeConfigurationServiceException
    {
        try
        {
            return distributedRepositoryPurgeConfigurationDao.getDistributedRepositoryPurgeConfiguration( dirPurgeId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( e.getMessage(), e );
        }
    }

    public List<DistributedDirectoryPurgeConfiguration> getEnableDistributedDirectoryPurgeConfigurationsBySchedule(
        int scheduleId )
    {
        return distributedDirectoryPurgeConfigurationDao.getEnableDistributedDirectoryPurgeConfigurationsBySchedule(
            scheduleId );
    }

    public List<DistributedRepositoryPurgeConfiguration> getEnableDistributedRepositoryPurgeConfigurationsBySchedule(
        int scheduleId )
    {
        return distributedRepositoryPurgeConfigurationDao.getEnableDistributedRepositoryPurgeConfigurationsBySchedule(
            scheduleId );
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
