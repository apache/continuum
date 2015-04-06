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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.purge.repository.utils.FileTypes;
import org.apache.maven.archiva.common.utils.PathUtil;
import org.apache.maven.archiva.common.utils.VersionUtil;
import org.apache.maven.archiva.model.ArtifactReference;
import org.apache.maven.archiva.model.ProjectReference;
import org.apache.maven.archiva.model.VersionedReference;
import org.apache.maven.archiva.repository.ContentNotFoundException;
import org.apache.maven.archiva.repository.content.ArtifactExtensionMapping;
import org.apache.maven.archiva.repository.content.DefaultPathParser;
import org.apache.maven.archiva.repository.content.PathParser;
import org.apache.maven.archiva.repository.layout.LayoutException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Taken from Archiva's ManagedDefaultRepositoryContent and made some few changes.
 */
@Component( role = RepositoryManagedContent.class, hint = "default", instantiationStrategy = "per-lookup" )
public class ManagedDefaultRepositoryContent
    implements RepositoryManagedContent
{
    private static final String MAVEN_METADATA = "maven-metadata.xml";

    private static final char PATH_SEPARATOR = '/';

    private static final char GROUP_SEPARATOR = '.';

    private static final char ARTIFACT_SEPARATOR = '-';

    private final PathParser defaultPathParser = new DefaultPathParser();

    @Requirement( hint = "file-types" )
    private FileTypes filetypes;

    private LocalRepository repository;

    public void deleteVersion( VersionedReference reference )
        throws ContentNotFoundException

    {
        String path = toMetadataPath( reference );
        File projectPath = new File( getRepoRoot(), path );

        File projectDir = projectPath.getParentFile();
        if ( projectDir.exists() && projectDir.isDirectory() )
        {
            try
            {
                FileUtils.deleteDirectory( projectDir );
            }
            catch ( IOException e )
            {
                // TODO: log this somewhere?
            }
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
        for ( File repoFile : repoDir.listFiles() )
        {
            if ( repoFile.isDirectory() )
            {
                // Skip it. it's a directory.
                continue;
            }

            String relativePath = PathUtil.getRelative( repository.getLocation(), repoFile );

            if ( filetypes.matchesArtifactPattern( relativePath ) )
            {
                ArtifactReference artifact = toArtifactReference( relativePath );

                // Test for related, groupId / artifactId / version must match.
                if ( artifact.getGroupId().equals( reference.getGroupId() ) &&
                    artifact.getArtifactId().equals( reference.getArtifactId() ) &&
                    artifact.getVersion().equals( reference.getVersion() ) )
                {
                    foundArtifacts.add( artifact );
                }
            }
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

    /**
     * Gather the Available Versions (on disk) for a specific Project Reference, based on filesystem
     * information.
     *
     * @return the Set of available versions, based on the project reference.
     * @throws ContentNotFoundException
     * @throws LayoutException
     */
    public Set<String> getVersions( ProjectReference reference )
        throws ContentNotFoundException, LayoutException
    {
        String path = toMetadataPath( reference );

        int idx = path.lastIndexOf( '/' );
        if ( idx > 0 )
        {
            path = path.substring( 0, idx );
        }

        File repoDir = new File( repository.getLocation(), path );

        if ( !repoDir.exists() )
        {
            throw new ContentNotFoundException(
                "Unable to get Versions on a non-existant directory: " + repoDir.getAbsolutePath() );
        }

        if ( !repoDir.isDirectory() )
        {
            throw new ContentNotFoundException(
                "Unable to get Versions on a non-directory: " + repoDir.getAbsolutePath() );
        }

        Set<String> foundVersions = new HashSet<String>();
        VersionedReference versionRef = new VersionedReference();
        versionRef.setGroupId( reference.getGroupId() );
        versionRef.setArtifactId( reference.getArtifactId() );

        for ( File repoFile : repoDir.listFiles() )
        {
            if ( !repoFile.isDirectory() )
            {
                // Skip it. not a directory.
                continue;
            }

            // Test if dir has an artifact, which proves to us that it is a valid version directory.
            String version = repoFile.getName();
            versionRef.setVersion( version );

            if ( hasArtifact( versionRef ) )
            {
                // Found an artifact, must be a valid version.
                foundVersions.add( version );
            }
        }

        return foundVersions;
    }

    public Set<String> getVersions( VersionedReference reference )
        throws ContentNotFoundException, LayoutException
    {
        String path = toMetadataPath( reference );

        int idx = path.lastIndexOf( '/' );
        if ( idx > 0 )
        {
            path = path.substring( 0, idx );
        }

        File repoDir = new File( repository.getLocation(), path );

        if ( !repoDir.exists() )
        {
            throw new ContentNotFoundException(
                "Unable to get versions on a non-existant directory: " + repoDir.getAbsolutePath() );
        }

        if ( !repoDir.isDirectory() )
        {
            throw new ContentNotFoundException(
                "Unable to get versions on a non-directory: " + repoDir.getAbsolutePath() );
        }

        Set<String> foundVersions = new HashSet<String>();

        // First gather up the versions found as artifacts in the managed repository.
        for ( File repoFile : repoDir.listFiles() )
        {
            if ( repoFile.isDirectory() )
            {
                // Skip it. it's a directory.
                continue;
            }

            String relativePath = PathUtil.getRelative( repository.getLocation(), repoFile );

            if ( filetypes.matchesDefaultExclusions( relativePath ) )
            {
                // Skip it, it's metadata or similar
                continue;
            }

            if ( filetypes.matchesArtifactPattern( relativePath ) )
            {
                ArtifactReference artifact = toArtifactReference( relativePath );

                foundVersions.add( artifact.getVersion() );
            }
        }

        return foundVersions;
    }

    public String toMetadataPath( ProjectReference reference )
    {
        StringBuffer path = new StringBuffer();

        path.append( formatAsDirectory( reference.getGroupId() ) ).append( PATH_SEPARATOR );
        path.append( reference.getArtifactId() ).append( PATH_SEPARATOR );
        path.append( MAVEN_METADATA );

        return path.toString();
    }

    public String toMetadataPath( VersionedReference reference )
    {
        StringBuffer path = new StringBuffer();

        path.append( formatAsDirectory( reference.getGroupId() ) ).append( PATH_SEPARATOR );
        path.append( reference.getArtifactId() ).append( PATH_SEPARATOR );
        if ( reference.getVersion() != null )
        {
            // add the version only if it is present
            path.append( VersionUtil.getBaseVersion( reference.getVersion() ) ).append( PATH_SEPARATOR );
        }
        path.append( MAVEN_METADATA );

        return path.toString();
    }

    public String toPath( ArtifactReference reference )
    {
        if ( reference == null )
        {
            throw new IllegalArgumentException( "Artifact reference cannot be null" );
        }

        String baseVersion = VersionUtil.getBaseVersion( reference.getVersion() );
        return toPath( reference.getGroupId(), reference.getArtifactId(), baseVersion, reference.getVersion(),
                       reference.getClassifier(), reference.getType() );
    }

    public void setRepository( LocalRepository repository )
    {
        this.repository = repository;
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
            return defaultPathParser.toArtifactReference( path.substring( repository.getLocation().length() ) );
        }

        return defaultPathParser.toArtifactReference( path );
    }

    public File toFile( ArtifactReference reference )
    {
        return new File( repository.getLocation(), toPath( reference ) );
    }

    /**
     * Get the first Artifact found in the provided VersionedReference location.
     *
     * @param reference the reference to the versioned reference to search within
     * @return the ArtifactReference to the first artifact located within the versioned reference. or null if
     * no artifact was found within the versioned reference.
     * @throws IOException     if the versioned reference is invalid (example: doesn't exist, or isn't a directory)
     * @throws LayoutException if the path cannot be converted to an artifact reference.
     */
    private ArtifactReference getFirstArtifact( VersionedReference reference )
        throws LayoutException, IOException
    {
        String path = toMetadataPath( reference );

        int idx = path.lastIndexOf( '/' );
        if ( idx > 0 )
        {
            path = path.substring( 0, idx );
        }

        File repoDir = new File( repository.getLocation(), path );

        if ( !repoDir.exists() )
        {
            throw new IOException( "Unable to gather the list of snapshot versions on a non-existant directory: " +
                                       repoDir.getAbsolutePath() );
        }

        if ( !repoDir.isDirectory() )
        {
            throw new IOException(
                "Unable to gather the list of snapshot versions on a non-directory: " + repoDir.getAbsolutePath() );
        }

        for ( File repoFile : repoDir.listFiles() )
        {
            if ( repoFile.isDirectory() )
            {
                // Skip it. it's a directory.
                continue;
            }

            String relativePath = PathUtil.getRelative( repository.getLocation(), repoFile );

            if ( filetypes.matchesArtifactPattern( relativePath ) )
            {
                return toArtifactReference( relativePath );
            }
        }

        // No artifact was found.
        return null;
    }

    private boolean hasArtifact( VersionedReference reference )
        throws LayoutException
    {
        try
        {
            return ( getFirstArtifact( reference ) != null );
        }
        catch ( IOException e )
        {
            return false;
        }
    }

    private String formatAsDirectory( String directory )
    {
        return directory.replace( GROUP_SEPARATOR, PATH_SEPARATOR );
    }

    private String toPath( String groupId, String artifactId, String baseVersion, String version, String classifier,
                           String type )
    {
        StringBuffer path = new StringBuffer();

        path.append( formatAsDirectory( groupId ) ).append( PATH_SEPARATOR );
        path.append( artifactId ).append( PATH_SEPARATOR );

        if ( baseVersion != null )
        {
            path.append( baseVersion ).append( PATH_SEPARATOR );
            if ( ( version != null ) && ( type != null ) )
            {
                path.append( artifactId ).append( ARTIFACT_SEPARATOR ).append( version );

                if ( StringUtils.isNotBlank( classifier ) )
                {
                    path.append( ARTIFACT_SEPARATOR ).append( classifier );
                }

                path.append( GROUP_SEPARATOR ).append( ArtifactExtensionMapping.getExtension( type ) );
            }
        }

        return path.toString();
    }
}