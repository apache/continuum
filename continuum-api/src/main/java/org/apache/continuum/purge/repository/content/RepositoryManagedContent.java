package org.apache.continuum.purge.repository.content;

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

import java.io.File;
import java.util.Set;

import org.apache.continuum.model.repository.LocalRepository;
import org.apache.maven.archiva.model.ArtifactReference;
import org.apache.maven.archiva.model.ProjectReference;
import org.apache.maven.archiva.model.VersionedReference;
import org.apache.maven.archiva.repository.ContentNotFoundException;
import org.apache.maven.archiva.repository.layout.LayoutException;

/**
 * Taken from Archiva's ManagedRepositoryContent interface and made some few changes.
 * @author Maria Catherine Tan
 * @version $Id$
 * @since 25 jul 07
 */
public interface RepositoryManagedContent
{
    /**
     * Delete from the local repository all files / directories associated with the
     * provided version reference.
     * 
     * @param reference the version reference to delete.
     * @throws ContentNotFoundException 
     */
    public void deleteVersion( VersionedReference reference )
        throws ContentNotFoundException;
    
    /**
     * <p>
     * Convenience method to get the repository id.
     * </p>
     * 
     * <p>
     * Equivalent to calling <code>.getRepository().getId()</code>
     * </p>
     * 
     * @return the repository id.
     */
    public int getId();
    
    /**
     * <p>
     * Gather up the list of related artifacts to the ArtifactReference provided.
     * This typically inclues the pom files, and those things with 
     * classifiers (such as doc, source code, test libs, etc...)
     * </p>
     * 
     * <p>
     * <strong>NOTE:</strong> Some layouts (such as maven 1 "legacy") are not compatible with this query.
     * </p> 
     * 
     * @param reference the reference to work off of.
     * @return the set of ArtifactReferences for related artifacts.
     * @throws ContentNotFoundException if the initial artifact reference does not exist within the repository.
     * @throws LayoutException 
     */
    public Set<ArtifactReference> getRelatedArtifacts( ArtifactReference reference )
        throws ContentNotFoundException, LayoutException;

    /**
     * <p>
     * Convenience method to get the repository (on disk) root directory.
     * </p>
     * 
     * <p>
     * Equivalent to calling <code>.getLocalRepository().getDirectory()</code>
     * </p>
     * 
     * @return the repository (on disk) root directory.
     */
    public String getRepoRoot();

    /**
     * Get the local repository associated with this
     * repository content.
     * 
     * @return the local repository that is associated with this repository content.
     */
    public LocalRepository getRepository();

    /**
     * Given a specific {@link ProjectReference}, return the list of available versions for
     * that project reference.
     * 
     * @param reference the project reference to work off of.
     * @return the list of versions found for that project reference.
     * @throws ContentNotFoundException if the project reference does not exist within the repository.
     * @throws LayoutException 
     */
    public Set<String> getVersions( ProjectReference reference )
        throws ContentNotFoundException, LayoutException;

    /**
     * <p>
     * Given a specific {@link VersionedReference}, return the list of available versions for that
     * versioned reference.
     * </p>
     * 
     * <p>
     * <strong>NOTE:</strong> This is really only useful when working with SNAPSHOTs.
     * </p>
     * 
     * @param reference the versioned reference to work off of.
     * @return the set of versions found.
     * @throws ContentNotFoundException if the versioned reference does not exist within the repository.
     * @throws LayoutException 
     */
    public Set<String> getVersions( VersionedReference reference )
        throws ContentNotFoundException, LayoutException;
    
    /**
     * Set the local repository to associate with this
     * repository content.
     * 
     * @param repo the repository to associate with this repository content.
     */
    public void setRepository( LocalRepository repo );

    /**
     * Given a repository relative path to a filename, return the {@link VersionedReference} object suitable for the path.
     *
     * @param path the path relative to the repository base dir for the artifact.
     * @return the {@link ArtifactReference} representing the path.  (or null if path cannot be converted to
     *         a {@link ArtifactReference})
     * @throws LayoutException if there was a problem converting the path to an artifact.
     */
    public ArtifactReference toArtifactReference( String path )
        throws LayoutException;

    /**
     * Given an {@link ArtifactReference}, return the file reference to the artifact.
     *
     * @param reference the artifact reference to use.
     * @return the relative path to the artifact.
     */
    public File toFile( ArtifactReference reference );

    /**
     * Given a {@link ProjectReference}, return the path to the metadata for
     * the project. 
     * 
     * @param reference the reference to use.
     * @return the path to the metadata file, or null if no metadata is appropriate.
     */
    public String toMetadataPath( ProjectReference reference );

    /**
     * Given a {@link VersionedReference}, return the path to the metadata for
     * the specific version of the project. 
     * 
     * @param reference the reference to use.
     * @return the path to the metadata file, or null if no metadata is appropriate.
     */
    public String toMetadataPath( VersionedReference reference );

    /**
     * Given an {@link ArtifactReference}, return the relative path to the artifact.
     *
     * @param reference the artifact reference to use.
     * @return the relative path to the artifact.
     */
    public String toPath( ArtifactReference reference );
}