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

import org.apache.continuum.model.repository.LocalRepository;

import java.util.List;

/**
 * @author Maria Catherine Tan
 * @since 25 jul 07
 */
public interface RepositoryService
{
    String ROLE = RepositoryService.class.getName();

    // ------------------------------------------------------
    //  LocalRepository
    // ------------------------------------------------------

    /**
     * Add the local repository
     *
     * @param repository the local repository to add
     * @return LocalRepository the local repository
     * @throws RepositoryServiceException
     */
    LocalRepository addLocalRepository( LocalRepository repository )
        throws RepositoryServiceException;

    /**
     * Update the local repository
     *
     * @param repository the local repository to update
     * @throws RepositoryServiceException
     */
    void updateLocalRepository( LocalRepository repository )
        throws RepositoryServiceException;

    /**
     * Remove the local repository
     *
     * @param repositoryId the id of the local repository to remove
     * @throws RepositoryServiceException
     */
    void removeLocalRepository( int repositoryId )
        throws RepositoryServiceException;

    /**
     * Retrieve all local repositories
     *
     * @return list of all local repositories
     */
    List<LocalRepository> getAllLocalRepositories();

    /**
     * Retrieve local repository
     *
     * @param location the system file path of the repository
     * @return LocalRepository the local repository
     * @throws RepositoryServiceException
     */
    LocalRepository getLocalRepositoryByLocation( String location )
        throws RepositoryServiceException;

    /**
     * Retrieve list of local repositories with the specified layout
     *
     * @param layout the layout of the repository. "default" or "legacy"
     * @return List of local repositories
     * @throws RepositoryServiceException
     */
    List<LocalRepository> getLocalRepositoriesByLayout( String layout );

    /**
     * Retrieve local repository
     *
     * @param repositoryId the id of the local repository
     * @return LocalRepository the local repository
     * @throws RepositoryServiceException
     */
    LocalRepository getLocalRepository( int repositoryId )
        throws RepositoryServiceException;

    /**
     * Retrieve local repository
     *
     * @param repositoryName
     * @return
     * @throws RepositoryServiceException
     */
    LocalRepository getLocalRepositoryByName( String repositoryName )
        throws RepositoryServiceException;
}