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
import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    implements RepositoryService
{
    private static final Logger log = LoggerFactory.getLogger( DefaultRepositoryService.class );

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
    private TaskQueueManager taskQueueManager;

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

            log.info( "Added new local repository: " + repository.getName() );
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

            if ( taskQueueManager.isRepositoryInUse( repositoryId ) )
            {
                return;
            }

            if ( taskQueueManager.isRepositoryInPurgeQueue( repositoryId ) )
            {
                taskQueueManager.removeRepositoryFromPurgeQueue( repositoryId );
            }

            log.info( "Remove purge configurations of " + repository.getName() );
            removePurgeConfigurationsOfRepository( repositoryId );

            List<ProjectGroup> groups = projectGroupDao.getProjectGroupByRepository( repositoryId );

            for ( ProjectGroup group : groups )
            {
                group.setLocalRepository( null );
                projectGroupDao.updateProjectGroup( group );
            }

            localRepositoryDao.removeLocalRepository( repository );

            log.info( "Removed local repository: " + repository.getName() );
        }
        catch ( TaskQueueManagerException e )
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
            if ( taskQueueManager.isRepositoryInUse( localRepository.getId() ) )
            {
                return;
            }

            localRepositoryDao.updateLocalRepository( localRepository );

            log.info( "Updated local repository: " + localRepository.getName() );
        }
        catch ( TaskQueueManagerException e )
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

    public LocalRepository getLocalRepositoryByName( String repositoryName )
        throws RepositoryServiceException
    {
        try
        {
            return localRepositoryDao.getLocalRepositoryByName( repositoryName );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new RepositoryServiceException( "No repository found with name: " + repositoryName, e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new RepositoryServiceException( "Unable to retrieve local repository: " + repositoryName, e );
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