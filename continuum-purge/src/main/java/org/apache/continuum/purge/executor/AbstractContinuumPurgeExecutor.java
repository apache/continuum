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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

import org.apache.continuum.purge.repository.content.RepositoryManagedContent;
import org.apache.continuum.purge.ContinuumPurgeConstants;
import org.apache.maven.archiva.consumers.core.repository.ArtifactFilenameFilter;
import org.apache.maven.archiva.model.ArtifactReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some codes were taken from Archiva.
 *
 * @author Maria Catherine Tan
 */
public abstract class AbstractContinuumPurgeExecutor
    implements ContinuumPurgeExecutor
{
    private static final char DELIM = ' ';

    private Logger logger = LoggerFactory.getLogger( "AuditLog" );

    public void purge( Set<ArtifactReference> references, RepositoryManagedContent repository )
    {
        if ( references != null && !references.isEmpty() )
        {
            for ( ArtifactReference reference : references )
            {
                File artifactFile = repository.toFile( reference );
                artifactFile.delete();
                triggerAuditEvent( artifactFile.getName(), ContinuumPurgeConstants.PURGE_ARTIFACT );
                purgeSupportFiles( artifactFile, artifactFile.getName() );
                // purge maven metadata
                purgeSupportFiles( artifactFile.getParentFile(), "maven-metadata" );
            }
        }
    }

    /**
     * <p>
     * This find support files for the artifactFile and deletes them.
     * </p>
     * <p>
     * Support Files are things like ".sha1", ".md5", ".asc", etc.
     * </p>
     *
     * @param artifactFile the file to base off of.
     */
    protected void purgeSupportFiles( File artifactFile, String filename )
    {
        File parentDir = artifactFile.getParentFile();

        if ( !parentDir.exists() )
        {
            return;
        }

        FilenameFilter filter = new ArtifactFilenameFilter( filename );

        File[] files = parentDir.listFiles( filter );

        for ( File file : files )
        {
            if ( file.exists() && file.isFile() )
            {
                file.delete();
                triggerAuditEvent( file.getName(), ContinuumPurgeConstants.PURGE_FILE );
            }
        }
    }
    
    protected void triggerAuditEvent( String resource, String action )
    {
        String msg = ContinuumPurgeConstants.PURGE + DELIM + "<continuum>" + DELIM + '\"' + resource + '\"' + DELIM + '\"' + action + '\"';
        
        logger.info( msg );
    }
}
