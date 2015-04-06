package org.apache.continuum.purge.executor;

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

import org.apache.continuum.purge.ContinuumPurgeConstants;
import org.apache.continuum.purge.repository.content.RepositoryManagedContent;
import org.apache.maven.archiva.common.utils.VersionComparator;
import org.apache.maven.archiva.common.utils.VersionUtil;
import org.apache.maven.archiva.model.ArtifactReference;
import org.apache.maven.archiva.model.ProjectReference;
import org.apache.maven.archiva.model.VersionedReference;
import org.apache.maven.archiva.repository.ContentNotFoundException;
import org.apache.maven.archiva.repository.layout.LayoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Codes were taken from Archiva's CleanupReleasedSnapshotsRepositoryPurge and just made some few changes
 *
 * @author Maria Catherine Tan
 */
public class ReleasedSnapshotsRepositoryPurgeExecutor
    extends AbstractContinuumPurgeExecutor
{
    private Logger log = LoggerFactory.getLogger( ReleasedSnapshotsRepositoryPurgeExecutor.class );

    private final RepositoryManagedContent repository;

    public ReleasedSnapshotsRepositoryPurgeExecutor( RepositoryManagedContent repository )
    {
        this.repository = repository;
    }

    public void purge( String path )
        throws ContinuumPurgeExecutorException
    {
        try
        {
            File artifactFile = new File( repository.getRepoRoot(), path );

            if ( !artifactFile.exists() )
            {
                // Nothing to do here, file doesn't exist, skip it.
                return;
            }

            ArtifactReference artifact = repository.toArtifactReference( path );

            if ( !VersionUtil.isSnapshot( artifact.getVersion() ) )
            {
                // Nothing to do here, not a snapshot, skip it.
                return;
            }

            ProjectReference reference = new ProjectReference();
            reference.setGroupId( artifact.getGroupId() );
            reference.setArtifactId( artifact.getArtifactId() );

            // Gather up all of the versions.
            List<String> allVersions = new ArrayList<String>( repository.getVersions( reference ) );

            // Split the versions into released and snapshots.
            List<String> releasedVersions = new ArrayList<String>();
            List<String> snapshotVersions = new ArrayList<String>();

            for ( String version : allVersions )
            {
                if ( VersionUtil.isSnapshot( version ) )
                {
                    snapshotVersions.add( version );
                }
                else
                {
                    releasedVersions.add( version );
                }
            }

            Collections.sort( allVersions, VersionComparator.getInstance() );
            Collections.sort( releasedVersions, VersionComparator.getInstance() );
            Collections.sort( snapshotVersions, VersionComparator.getInstance() );

            VersionedReference versionRef = new VersionedReference();
            versionRef.setGroupId( artifact.getGroupId() );
            versionRef.setArtifactId( artifact.getArtifactId() );

            for ( String version : snapshotVersions )
            {
                if ( releasedVersions.contains( VersionUtil.getReleaseVersion( version ) ) )
                {
                    versionRef.setVersion( version );
                    repository.deleteVersion( versionRef );

                    log.info( ContinuumPurgeConstants.PURGE_PROJECT + " - " + VersionedReference.toKey( versionRef ) );

                    removeMetadata( versionRef );
                }
            }
        }
        catch ( LayoutException e )
        {
            throw new ContinuumPurgeExecutorException( e.getMessage(), e );
        }
        catch ( ContentNotFoundException e )
        {
            throw new ContinuumPurgeExecutorException( e.getMessage(), e );
        }
    }

    private void removeMetadata( VersionedReference versionRef )
        throws ContinuumPurgeExecutorException
    {
        String path = repository.toMetadataPath( versionRef );
        File projectPath = new File( repository.getRepoRoot(), path );

        File projectDir = projectPath.getParentFile();

        purgeSupportFiles( projectDir, "maven-metadata" );
    }
}
