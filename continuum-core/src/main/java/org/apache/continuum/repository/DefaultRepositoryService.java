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

import org.apache.continuum.dao.LocalRepositoryDao;
import org.apache.continuum.dao.ProjectGroupDao;
import org.apache.continuum.dao.RepositoryPurgeConfigurationDao;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.ContinuumPurgeManager;
import org.apache.continuum.purge.ContinuumPurgeManagerException;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.List;

/**
 * DefaultRepositoryService
 *
 * @author Maria Catherine Tan
 * @version $Id$
 * @plexus.component role="org.apache.continuum.repository.RepositoryService" role-hint="default"
 * @since 25 jul 07
 */
public class DefaultRepositoryService
    extends AbstractLogEnabled
    implements RepositoryService
{
    /**
     * @plexus.requirement
     */
    private LocalRepositoryDao localRepositoryDao;

    /**
     * @plexus.requirement
     */
    private RepositoryPurgeConfigurationDao repositoryPurgeConfigurationDao;

    /**
     * @plexus.requirement
     */
    private ProjectGroupDao projectGroupDao;

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
            List<LocalRepository> repos = getAllLocalRepositories();
            for ( LocalRepository repo : repos )
            {
                if ( repo.getName().equals( localRepository.getName() ) )
                {
                    throw new RepositoryServiceException( "Local repository name must be unique" );
                }
                
                if ( repo.getLocation().equals( localRepository.getLocation() ) )
                {
                    throw new RepositoryServiceException( "Local repository location must be unique" );
                }
            }

            localRepository.setName( localRepository.getName().trim() );
            localRepository.setLocation( localRepository.getLocation().trim() );

            repository = localRepositoryDao.addLocalRepository( localRepository );

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

            List<ProjectGroup> groups = projectGroupDao.getProjectGroupByRepository( repositoryId );

            for ( ProjectGroup group : groups )
            {
                group.setLocalRepository( null );
                projectGroupDao.updateProjectGroup( group );
            }

            localRepositoryDao.removeLocalRepository( repository );

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

            localRepositoryDao.updateLocalRepository( localRepository );

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
        return localRepositoryDao.getAllLocalRepositories();
    }

    public List<LocalRepository> getLocalRepositoriesByLayout( String layout )
    {
        return localRepositoryDao.getLocalRepositoriesByLayout( layout );
    }

    public LocalRepository getLocalRepositoryByLocation( String location )
        throws RepositoryServiceException
    {
        try
        {
            return localRepositoryDao.getLocalRepositoryByLocation( location );
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
            return localRepositoryDao.getLocalRepository( repositoryId );
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
                repositoryPurgeConfigurationDao.getRepositoryPurgeConfigurationsByLocalRepository( repositoryId );

            for ( RepositoryPurgeConfiguration purgeConfig : purgeConfigs )
            {
                repositoryPurgeConfigurationDao.removeRepositoryPurgeConfiguration( purgeConfig );
            }
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new RepositoryServiceException( "Error while removing local repository: " + repositoryId, e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new RepositoryServiceException(
                "Error while removing purge configurations of local repository: " + repositoryId, e );
        }
    }
}