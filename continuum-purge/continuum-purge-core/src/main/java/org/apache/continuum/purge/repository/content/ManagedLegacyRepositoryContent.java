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

import org.apache.commons.lang.StringUtils;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.purge.repository.utils.FileTypes;
import org.apache.maven.archiva.common.utils.PathUtil;
import org.apache.maven.archiva.model.ArtifactReference;
import org.apache.maven.archiva.model.ProjectReference;
import org.apache.maven.archiva.model.VersionedReference;
import org.apache.maven.archiva.repository.ContentNotFoundException;
import org.apache.maven.archiva.repository.content.ArtifactExtensionMapping;
import org.apache.maven.archiva.repository.content.PathParser;
import org.apache.maven.archiva.repository.layout.LayoutException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Taken from Archiva's ManagedLegacyRepositoryContent and made some few changes
 */
@Component( role = org.apache.continuum.purge.repository.content.RepositoryManagedContent.class, hint = "legacy",
    instantiationStrategy = "per-lookup" )
public class ManagedLegacyRepositoryContent
    implements RepositoryManagedContent
{
    private static final String PATH_SEPARATOR = "/";

    private static final Map<String, String> typeToDirectoryMap;

    static
    {
        typeToDirectoryMap = new HashMap<String, String>();
        typeToDirectoryMap.put( "ejb-client", "ejb" );
        typeToDirectoryMap.put( ArtifactExtensionMapping.MAVEN_PLUGIN, "maven-plugin" );
        typeToDirectoryMap.put( ArtifactExtensionMapping.MAVEN_ONE_PLUGIN, "plugin" );
        typeToDirectoryMap.put( "distribution-tgz", "distribution" );
        typeToDirectoryMap.put( "distribution-zip", "distribution" );
        typeToDirectoryMap.put( "javadoc", "javadoc.jar" );
    }

    @Requirement( hint = "legacy-parser" )
    private PathParser legacyPathParser;
    
    @Requirement( hint = "file-types" )
    private FileTypes filetypes;

    private LocalRepository repository;

    public void deleteVersion( VersionedReference reference )
        throws ContentNotFoundException
    {
        File groupDir = new File( repository.getLocation(), reference.getGroupId() );

        if ( !groupDir.exists() )
        {
            throw new ContentNotFoundException(
                "Unable to get versions using a non-existant groupId directory: " + groupDir.getAbsolutePath() );
        }

        if ( !groupDir.isDirectory() )
        {
            throw new ContentNotFoundException(
                "Unable to get versions using a non-directory: " + groupDir.getAbsolutePath() );
        }

        // First gather up the versions found as artifacts in the managed repository.
        File typeDirs[] = groupDir.listFiles();
        for ( File typeDir : typeDirs )
        {
            if ( !typeDir.isDirectory() )
            {
                // Skip it, we only care about directories.
                continue;
            }

            if ( !typeDir.getName().endsWith( "s" ) )
            {
                // Skip it, we only care about directories that end in "s".
            }

            deleteVersions( typeDir, reference );
        }
    }

    private void deleteVersions( File typeDir, VersionedReference reference )
    {
        File repoFiles[] = typeDir.listFiles();
        for ( File repoFile : repoFiles )
        {
            if ( repoFile.isDirectory() )
            {
                // Skip it. it's a directory.
                continue;
            }

            String relativePath = PathUtil.getRelative( repository.getLocation(), repoFile );

            if ( filetypes.matchesArtifactPattern( relativePath ) )
            {
                try
                {
                    ArtifactReference artifact = toArtifactReference( relativePath );
                    if ( StringUtils.equals( artifact.getArtifactId(), reference.getArtifactId() ) &&
                        StringUtils.equals( artifact.getVersion(), reference.getVersion() ) )
                    {
                        repoFile.delete();
                        deleteSupportFiles( repoFile );
                    }
                }
                catch ( LayoutException e )
                {
                    /* don't fail the process if there is a bad artifact within the directory. */
                }
            }
        }
    }

    private void deleteSupportFiles( File repoFile )
    {
        deleteSupportFile( repoFile, ".sha1" );
        deleteSupportFile( repoFile, ".md5" );
        deleteSupportFile( repoFile, ".asc" );
        deleteSupportFile( repoFile, ".gpg" );
    }

    private void deleteSupportFile( File repoFile, String supportExtension )
    {
        File supportFile = new File( repoFile.getAbsolutePath() + supportExtension );
        if ( supportFile.exists() && supportFile.isFile() )
        {
            supportFile.delete();
        }
    }

    public int getId()
    {
        return repository.getId();
    }

    public Set<ArtifactReference> getRelatedArtifacts( ArtifactReference reference )
        throws ContentNotFoundException, LayoutException
    {
        File artifactFile = toFile( reference );
        File repoDir = artifactFile.getParentFile();

        if ( !repoDir.exists() )
        {
            throw new ContentNotFoundException(
                "Unable to get related artifacts using a non-existant directory: " + repoDir.getAbsolutePath() );
        }

        if ( !repoDir.isDirectory() )
        {
            throw new ContentNotFoundException(
                "Unable to get related artifacts using a non-directory: " + repoDir.getAbsolutePath() );
        }

        Set<ArtifactReference> foundArtifacts = new HashSet<ArtifactReference>();

        // First gather up the versions found as artifacts in the managed repository.
        File projectParentDir = repoDir.getParentFile();
        File typeDirs[] = projectParentDir.listFiles();
        for ( File typeDir : typeDirs )
        {
            if ( !typeDir.isDirectory() )
            {
                // Skip it, we only care about directories.
                continue;
            }

            if ( !typeDir.getName().endsWith( "s" ) )
            {
                // Skip it, we only care about directories that end in "s".
            }

            getRelatedArtifacts( typeDir, reference, foundArtifacts );
        }

        return foundArtifacts;
    }

    public String getRepoRoot()
    {
        return repository.getLocation();
    }

    public LocalRepository getRepository()
    {
        return repository;
    }

    public Set<String> getVersions( ProjectReference reference )
        throws ContentNotFoundException
    {
        File groupDir = new File( repository.getLocation(), reference.getGroupId() );

        if ( !groupDir.exists() )
        {
            throw new ContentNotFoundException(
                "Unable to get versions using a non-existant groupId directory: " + groupDir.getAbsolutePath() );
        }

        if ( !groupDir.isDirectory() )
        {
            throw new ContentNotFoundException(
                "Unable to get versions using a non-directory: " + groupDir.getAbsolutePath() );
        }

        Set<String> foundVersions = new HashSet<String>();

        // First gather up the versions found as artifacts in the managed repository.
        File typeDirs[] = groupDir.listFiles();
        for ( File typeDir : typeDirs )
        {
            if ( !typeDir.isDirectory() )
            {
                // Skip it, we only care about directories.
                continue;
            }

            if ( !typeDir.getName().endsWith( "s" ) )
            {
                // Skip it, we only care about directories that end in "s".
            }

            getProjectVersions( typeDir, reference, foundVersions );
        }

        return foundVersions;
    }

    public Set<String> getVersions( VersionedReference reference )
        throws ContentNotFoundException
    {
        File groupDir = new File( repository.getLocation(), reference.getGroupId() );

        if ( !groupDir.exists() )
        {
            throw new ContentNotFoundException(
                "Unable to get versions using a non-existant groupId directory: " + groupDir.getAbsolutePath() );
        }

        if ( !groupDir.isDirectory() )
        {
            throw new ContentNotFoundException(
                "Unable to get versions using a non-directory: " + groupDir.getAbsolutePath() );
        }

        Set<String> foundVersions = new HashSet<String>();

        // First gather up the versions found as artifacts in the managed repository.
        File typeDirs[] = groupDir.listFiles();
        for ( File typeDir : typeDirs )
        {
            if ( !typeDir.isDirectory() )
            {
                // Skip it, we only care about directories.
                continue;
            }

            if ( !typeDir.getName().endsWith( "s" ) )
            {
                // Skip it, we only care about directories that end in "s".
            }

            getVersionedVersions( typeDir, reference, foundVersions );
        }

        return foundVersions;
    }

    /**
     * Convert a path to an artifact reference.
     *
     * @param path the path to convert. (relative or full location path)
     * @throws LayoutException if the path cannot be converted to an artifact reference.
     */
    public ArtifactReference toArtifactReference( String path )
        throws LayoutException
    {
        if ( ( path != null ) && path.startsWith( repository.getLocation() ) )
        {
            return legacyPathParser.toArtifactReference( path.substring( repository.getLocation().length() ) );
        }

        return legacyPathParser.toArtifactReference( path );
    }

    public File toFile( ArtifactReference reference )
    {
        return new File( repository.getLocation(), toPath( reference ) );
    }

    public String toMetadataPath( ProjectReference reference )
    {
        // No metadata present in legacy repository.
        return null;
    }

    public String toMetadataPath( VersionedReference reference )
    {
        // No metadata present in legacy repository.
        return null;
    }

    public String toPath( ArtifactReference reference )
    {
        if ( reference == null )
        {
            throw new IllegalArgumentException( "Artifact reference cannot be null" );
        }

        return toPath( reference.getGroupId(), reference.getArtifactId(), reference.getVersion(),
                       reference.getClassifier(), reference.getType() );
    }

    public void setRepository( LocalRepository repo )
    {
        this.repository = repo;
    }

    private void getProjectVersions( File typeDir, ProjectReference reference, Set<String> foundVersions )
    {
        File repoFiles[] = typeDir.listFiles();
        for ( File repoFile : repoFiles )
        {
            if ( repoFile.isDirectory() )
            {
                // Skip it. it's a directory.
                continue;
            }

            String relativePath = PathUtil.getRelative( repository.getLocation(), repoFile );

            if ( filetypes.matchesArtifactPattern( relativePath ) )
            {
                try
                {
                    ArtifactReference artifact = toArtifactReference( relativePath );
                    if ( StringUtils.equals( artifact.getArtifactId(), reference.getArtifactId() ) )
                    {
                        foundVersions.add( artifact.getVersion() );
                    }
                }
                catch ( LayoutException e )
                {
                    /* don't fail the process if there is a bad artifact within the directory. */
                }
            }
        }
    }

    private void getRelatedArtifacts( File typeDir, ArtifactReference reference, Set<ArtifactReference> foundArtifacts )
    {
        for ( File repoFile : typeDir.listFiles() )
        {
            if ( repoFile.isDirectory() )
            {
                // Skip it. it's a directory.
                continue;
            }

            String relativePath = PathUtil.getRelative( repository.getLocation(), repoFile );

            if ( filetypes.matchesArtifactPattern( relativePath ) )
            {
                try
                {
                    ArtifactReference artifact = toArtifactReference( relativePath );
                    if ( StringUtils.equals( artifact.getArtifactId(), reference.getArtifactId() ) &&
                        artifact.getVersion().startsWith( reference.getVersion() ) )
                    {
                        foundArtifacts.add( artifact );
                    }
                }
                catch ( LayoutException e )
                {
                    /* don't fail the process if there is a bad artifact within the directory. */
                }
            }
        }
    }

    private void getVersionedVersions( File typeDir, VersionedReference reference, Set<String> foundVersions )
    {
        for ( File repoFile : typeDir.listFiles() )
        {
            if ( repoFile.isDirectory() )
            {
                // Skip it. it's a directory.
                continue;
            }

            String relativePath = PathUtil.getRelative( repository.getLocation(), repoFile );

            if ( filetypes.matchesArtifactPattern( relativePath ) )
            {
                try
                {
                    ArtifactReference artifact = toArtifactReference( relativePath );
                    if ( StringUtils.equals( artifact.getArtifactId(), reference.getArtifactId() ) &&
                        artifact.getVersion().startsWith( reference.getVersion() ) )
                    {
                        foundVersions.add( artifact.getVersion() );
                    }
                }
                catch ( LayoutException e )
                {
                    /* don't fail the process if there is a bad artifact within the directory. */
                }
            }
        }
    }

    private String toPath( String groupId, String artifactId, String version, String classifier, String type )
    {
        StringBuffer path = new StringBuffer();

        path.append( groupId ).append( PATH_SEPARATOR );
        path.append( getDirectory( type ) ).append( PATH_SEPARATOR );

        if ( version != null )
        {
            path.append( artifactId ).append( '-' ).append( version );

            if ( StringUtils.isNotBlank( classifier ) )
            {
                path.append( '-' ).append( classifier );
            }

            path.append( '.' ).append( ArtifactExtensionMapping.getExtension( type ) );
        }

        return path.toString();
    }

    private String getDirectory( String type )
    {
        String dirname = typeToDirectoryMap.get( type );

        if ( dirname != null )
        {
            return dirname + "s";
        }

        // Default process.
        return type + "s";
    }
}