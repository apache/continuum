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

import org.apache.commons.lang.time.DateUtils;
import org.apache.continuum.purge.repository.content.RepositoryManagedContent;
import org.apache.maven.archiva.common.utils.VersionComparator;
import org.apache.maven.archiva.common.utils.VersionUtil;
import org.apache.maven.archiva.model.ArtifactReference;
import org.apache.maven.archiva.model.VersionedReference;
import org.apache.maven.archiva.repository.ContentNotFoundException;
import org.apache.maven.archiva.repository.layout.LayoutException;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Codes were taken from  Archiva's DaysOldRepositoryPurge and made some few changes.
 *
 * @author Maria Catherine Tan
 */
public class DaysOldRepositoryPurgeExecutor
    extends AbstractContinuumPurgeExecutor
    implements ContinuumPurgeExecutor
{
    private int daysOlder;

    private int retentionCount;

    private RepositoryManagedContent repository;

    private SimpleDateFormat timestampParser;

    public DaysOldRepositoryPurgeExecutor( RepositoryManagedContent repository, int daysOlder, int retentionCount )
    {
        this.repository = repository;
        this.daysOlder = daysOlder;
        this.retentionCount = retentionCount;
        timestampParser = new SimpleDateFormat( "yyyyMMdd.HHmmss" );
        timestampParser.setTimeZone( DateUtils.UTC_TIME_ZONE );
    }

    public void purge( String path )
        throws ContinuumPurgeExecutorException
    {
        try
        {
            File artifactFile = new File( repository.getRepoRoot(), path );

            if ( !artifactFile.exists() )
            {
                return;
            }

            ArtifactReference artifact = repository.toArtifactReference( path );

            Calendar olderThanThisDate = Calendar.getInstance( DateUtils.UTC_TIME_ZONE );
            olderThanThisDate.add( Calendar.DATE, -daysOlder );

            // respect retention count
            VersionedReference reference = new VersionedReference();
            reference.setGroupId( artifact.getGroupId() );
            reference.setArtifactId( artifact.getArtifactId() );
            reference.setVersion( artifact.getVersion() );

            List<String> versions = new ArrayList<String>( repository.getVersions( reference ) );

            Collections.sort( versions, VersionComparator.getInstance() );

            if ( retentionCount > versions.size() )
            {
                // Done. nothing to do here. skip it.
                return;
            }

            int countToPurge = versions.size() - retentionCount;

            for ( String version : versions )
            {
                if ( countToPurge <= 0 )
                {
                    break;
                }

                ArtifactReference newArtifactReference =
                    repository.toArtifactReference( artifactFile.getAbsolutePath() );
                newArtifactReference.setVersion( version );

                File newArtifactFile = repository.toFile( newArtifactReference );

                // Is this a generic snapshot "1.0-SNAPSHOT" ?
                if ( VersionUtil.isGenericSnapshot( newArtifactReference.getVersion() ) )
                {
                    if ( newArtifactFile.lastModified() < olderThanThisDate.getTimeInMillis() )
                    {
                        doPurgeAllRelated( newArtifactReference );
                        countToPurge--;
                    }
                }
                // Is this a timestamp snapshot "1.0-20070822.123456-42" ?
                else if ( VersionUtil.isUniqueSnapshot( newArtifactReference.getVersion() ) )
                {
                    Calendar timestampCal = uniqueSnapshotToCalendar( newArtifactReference.getVersion() );

                    if ( timestampCal.getTimeInMillis() < olderThanThisDate.getTimeInMillis() )
                    {
                        doPurgeAllRelated( newArtifactReference );
                        countToPurge--;
                    }
                    else if ( newArtifactFile.lastModified() < olderThanThisDate.getTimeInMillis() )
                    {
                        doPurgeAllRelated( newArtifactReference );
                        countToPurge--;
                    }
                }
            }
        }
        catch ( LayoutException e )
        {
            throw new ContinuumPurgeExecutorException( e.getMessage() + ": " + path, e );
        }
        catch ( ContentNotFoundException e )
        {
            throw new ContinuumPurgeExecutorException( e.getMessage(), e );
        }
    }

    private Calendar uniqueSnapshotToCalendar( String version )
    {
        // The latestVersion will contain the full version string "1.0-alpha-5-20070821.213044-8"
        // This needs to be broken down into ${base}-${timestamp}-${build_number}

        Matcher m = VersionUtil.UNIQUE_SNAPSHOT_PATTERN.matcher( version );
        if ( m.matches() )
        {
            Matcher mtimestamp = VersionUtil.TIMESTAMP_PATTERN.matcher( m.group( 2 ) );
            if ( mtimestamp.matches() )
            {
                String tsDate = mtimestamp.group( 1 );
                String tsTime = mtimestamp.group( 2 );

                Date versionDate;
                try
                {
                    versionDate = timestampParser.parse( tsDate + "." + tsTime );
                    Calendar cal = Calendar.getInstance( DateUtils.UTC_TIME_ZONE );
                    cal.setTime( versionDate );

                    return cal;
                }
                catch ( ParseException e )
                {
                    // Invalid Date/Time
                    return null;
                }
            }
        }
        return null;
    }

    private void doPurgeAllRelated( ArtifactReference reference )
        throws LayoutException
    {
        try
        {
            Set<ArtifactReference> related = repository.getRelatedArtifacts( reference );
            purge( related, repository );
        }
        catch ( ContentNotFoundException e )
        {
            // Nothing to do here.
            // TODO: Log this?
        }
    }
}