package org.apache.continuum.purge.repository.utils;

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

import org.codehaus.plexus.util.SelectorUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Codes were taken from Archiva and made some changes.
 */
public class FileTypes
{
    private List<String> artifactFileTypePatterns;

    private List<String> ignoredFileTypePatterns;

    public static final List<String> DEFAULT_EXCLUSIONS = Arrays.asList( "**/maven-metadata.xml",
                                                                         "**/maven-metadata-*.xml", "**/*.sha1",
                                                                         "**/*.asc", "**/*.md5", "**/*.pgp",
                                                                         "**/*.lastUpdated", "**/*.repositories",
                                                                         "**/resolver-status.properties" );

    public List<String> getIgnoredFileTypePatterns()
    {
        if ( ignoredFileTypePatterns == null )
        {
            ignoredFileTypePatterns = DEFAULT_EXCLUSIONS;
        }

        return ignoredFileTypePatterns;
    }

    public List<String> getArtifactFileTypePatterns()
    {
        return artifactFileTypePatterns;
    }

    public synchronized boolean matchesArtifactPattern( String relativePath )
    {
        // Correct the slash pattern.
        relativePath = relativePath.replace( '\\', '/' );

        if ( artifactFileTypePatterns == null )
        {
            return false;
        }

        for ( String pattern : artifactFileTypePatterns )
        {
            if ( SelectorUtils.matchPath( pattern, relativePath, false ) )
            {
                // Found match
                return true;
            }
        }

        // No match.
        return false;
    }

    public boolean matchesDefaultExclusions( String relativePath )
    {
        // Correct the slash pattern.
        relativePath = relativePath.replace( '\\', '/' );

        for ( String pattern : DEFAULT_EXCLUSIONS )
        {
            if ( SelectorUtils.matchPath( pattern, relativePath, false ) )
            {
                // Found match
                return true;
            }
        }

        // No match.
        return false;
    }
}
