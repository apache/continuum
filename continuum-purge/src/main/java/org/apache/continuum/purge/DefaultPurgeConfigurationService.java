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

import java.util.ArrayList;
import java.util.List;

import org.apache.continuum.model.repository.AbstractPurgeConfiguration;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.repository.content.RepositoryManagedContent;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

/**
 * DefaultPurgeConfigurationService
 * 
 * @author Maria Catherine Tan
 * @version $Id$
 * @since 25 jul 07
 * @plexus.component role="org.apache.continuum.purge.PurgeConfigurationService" role-hint="default"
 */
public class DefaultPurgeConfigurationService
    implements PurgeConfigurationService, Contextualizable
{
    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;
 
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
    }
    
    public DirectoryPurgeConfiguration addDirectoryPurgeConfiguration( DirectoryPurgeConfiguration dirPurge )
        throws PurgeConfigurationServiceException
    {
        DirectoryPurgeConfiguration dirPurgeConfig = null;
        
        try
        {
            dirPurgeConfig = store.addDirectoryPurgeConfiguration( dirPurge );
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
        RepositoryPurgeConfiguration repoPurgeConfig = null;
        
        try
        {
            repoPurgeConfig = store.addRepositoryPurgeConfiguration( repoPurge );
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
        return store.getAllDirectoryPurgeConfigurations();
    }
    
    public List<RepositoryPurgeConfiguration> getAllRepositoryPurgeConfigurations()
    {
        return store.getAllRepositoryPurgeConfigurations();
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
        List<DirectoryPurgeConfiguration> purgeConfigs = store.getDirectoryPurgeConfigurationsByType( directoryType );
        
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
        return store.getDirectoryPurgeConfigurationsByLocation( location );
    }
    
    public List<DirectoryPurgeConfiguration> getDirectoryPurgeConfigurationsBySchedule( int scheduleId )
    {
        return store.getDirectoryPurgeConfigurationsBySchedule( scheduleId );
    }
    
    public List<RepositoryPurgeConfiguration> getRepositoryPurgeConfigurationsByRepository( int repositoryId )
    {
        return store.getRepositoryPurgeConfigurationsByLocalRepository( repositoryId );
    }
    
    public List<RepositoryPurgeConfiguration> getRepositoryPurgeConfigurationsBySchedule( int scheduleId )
    {
        return store.getRepositoryPurgeConfigurationsBySchedule( scheduleId );
    }
    
    public void removeDirectoryPurgeConfiguration( DirectoryPurgeConfiguration purgeConfig )
        throws PurgeConfigurationServiceException
    {
        try
        {
            store.removeDirectoryPurgeConfiguration( purgeConfig );
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
            store.removeRepositoryPurgeConfiguration( purgeConfig );
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
            store.updateDirectoryPurgeConfiguration( purgeConfig );
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
            store.updateRepositoryPurgeConfiguration( purgeConfig );
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
            return store.getDirectoryPurgeConfiguration( purgeConfigId );
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
            return store.getRepositoryPurgeConfiguration( purgeConfigId );
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
        
        return purgeConfig;
    }
    
    public RepositoryManagedContent getManagedRepositoryContent( int repositoryId )
        throws PurgeConfigurationServiceException
    {
        try
        {
            LocalRepository repository = store.getLocalRepository( repositoryId );
            
            RepositoryManagedContent repoContent;
            
            repoContent = (RepositoryManagedContent) container.lookup( RepositoryManagedContent.class, repository.getLayout() );
            repoContent.setRepository( repository );
            
            return repoContent;
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new PurgeConfigurationServiceException( "Error retrieving managed repository content for: " + repositoryId, e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new PurgeConfigurationServiceException( "Error retrieving managed repository content for: " + repositoryId, e );
        }
        catch ( ComponentLookupException e )
        {
            throw new PurgeConfigurationServiceException( "Error retrieving managed repository content for: " + repositoryId, e );
        }
    }
    
    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
