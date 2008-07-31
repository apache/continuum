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

import java.util.List;

import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.ContinuumPurgeManager;
import org.apache.continuum.purge.ContinuumPurgeManagerException;
import org.apache.continuum.repository.RepositoryService;
import org.apache.continuum.repository.RepositoryServiceException;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * DefaultRepositoryService
 * 
 * @author Maria Catherine Tan
 * @version $Id$
 * @since 25 jul 07
 * @plexus.component role="org.apache.continuum.repository.RepositoryService" role-hint="default"
 */
public class DefaultRepositoryService
    extends AbstractLogEnabled
    implements RepositoryService
{
    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;
    
    /**
     * @plexus.requirement
     */
    private ContinuumPurgeManager purgeManager;
    
    public LocalRepository addLocalRepository( LocalRepository localRepository )
        throws RepositoryServiceException
    {
        LocalRepository repository = null;
        
        try
        {
            localRepository.setName( localRepository.getName().trim() );
            localRepository.setLocation( localRepository.getLocation().trim() );
            
            repository = store.addLocalRepository( localRepository );
            
            getLogger().info( "Added new local repository: " + repository.getName() );
        }
        catch ( ContinuumStoreException e )
        {
            throw new RepositoryServiceException( "Unable to add the requested local repository", e );
        }
        
        return repository;
    }

    public void removeLocalRepository( int repositoryId )
        throws RepositoryServiceException
    {
        try
        {
            LocalRepository repository = getLocalRepository( repositoryId );
            
            if ( purgeManager.isRepositoryInUse( repositoryId ) )
            {
                return;
            }
            
            if ( purgeManager.isRepositoryInPurgeQueue( repositoryId ) )
            {
                purgeManager.removeRepositoryFromPurgeQueue( repositoryId );
            }
            
            getLogger().info( "Remove purge configurations of " + repository.getName() );
            removePurgeConfigurationsOfRepository( repositoryId );
            
            List<ProjectGroup> groups = store.getProjectGroupByRepository( repositoryId );
            
            for( ProjectGroup group : groups )
            {
                group.setLocalRepository( null );
                store.updateProjectGroup( group );
            }
            
            store.removeLocalRepository( repository );
            
            getLogger().info( "Removed local repository: " + repository.getName() );
        }
        catch ( ContinuumPurgeManagerException e )
        {
            // swallow?
        }
        catch ( ContinuumStoreException e )
        {
            throw new RepositoryServiceException( "Unable to delete the requested local repository", e ); 
        }
    }

    public void updateLocalRepository( LocalRepository localRepository )
        throws RepositoryServiceException
    {
        localRepository.setName( localRepository.getName().trim() );
        localRepository.setLocation( localRepository.getLocation().trim() );
        
        try
        {
            if ( purgeManager.isRepositoryInUse( localRepository.getId() ) )
            {
                return;
            }
            
            store.updateLocalRepository( localRepository );
            
            getLogger().info( "Updated local repository: " + localRepository.getName() );
        }
        catch ( ContinuumPurgeManagerException e )
        {
            // swallow?
        }
        catch ( ContinuumStoreException e )
        {
            throw new RepositoryServiceException( "Unable to update the requested local repository", e );
        }
    }
    
    public List<LocalRepository> getAllLocalRepositories()
    {
        return store.getAllLocalRepositories();
    }

    public List<LocalRepository> getLocalRepositoriesByLayout( String layout )
    {
        return store.getLocalRepositoriesByLayout( layout );
    }

    public LocalRepository getLocalRepositoryByLocation( String location )
        throws RepositoryServiceException
    {
        try
        {
            return store.getLocalRepositoryByLocation( location );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new RepositoryServiceException( "No repository found with location: " + location, e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new RepositoryServiceException( "Unable to retrieve local repository by location: " + location, e );
        }
    }
    
    public LocalRepository getLocalRepository( int repositoryId )
        throws RepositoryServiceException
    {
        try
        {
            return store.getLocalRepository( repositoryId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new RepositoryServiceException( "No repository found with id: " + repositoryId, e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new RepositoryServiceException( "Unable to retrieve local repository: " + repositoryId, e );
        }
    }
    
    private void removePurgeConfigurationsOfRepository( int repositoryId )
        throws RepositoryServiceException
    {
        try
        {
            List<RepositoryPurgeConfiguration> purgeConfigs = 
                store.getRepositoryPurgeConfigurationsByLocalRepository( repositoryId );
            
            for( RepositoryPurgeConfiguration purgeConfig : purgeConfigs )
            {
                store.removeRepositoryPurgeConfiguration( purgeConfig );
            }
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new RepositoryServiceException( "Error while removing local repository: " + repositoryId, e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new RepositoryServiceException( "Error while removing purge configurations of local repository: " 
                                                  + repositoryId, e );
        }
    }
}