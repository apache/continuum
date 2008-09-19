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

/**
 * @author Maria Catherine Tan
 */
public class DaysOldDirectoryPurgeExecutorTest
    extends AbstractPurgeExecutorTest
{
    protected void setUp()
        throws Exception
    {
        super.setUp();

        purgeReleasesDirTask = getDaysOldReleasesDirPurgeTask();

        purgeBuildOutputDirTask = getDaysOldBuildOutputDirPurgeTask();
    }

    public void testReleasesDirPurgingByLastModified()
        throws Exception
    {
        populateReleasesDirectory();

        String dirPath = getReleasesDirectoryLocation().getAbsolutePath();

        setLastModified( dirPath, 1179382029, true );
        setLastModified( dirPath + "/releases-1234567809", 1023453892, false );

        purgeExecutor.executeTask( purgeReleasesDirTask );

        assertDeleted( dirPath + "/releases-1234567809" );

        assertExists( dirPath + "/1" );
        assertExists( dirPath + "/releases-1234567890" );
        assertExists( dirPath + "/releases-4234729018" );
    }

    public void testReleasesDirPurgingByOrderOfDeletion()
        throws Exception
    {
        populateReleasesDirectory();

        String dirPath = getReleasesDirectoryLocation().getAbsolutePath();

        setLastModified( dirPath + "/releases-4234729018", new Long( "1234567809" ), false );
        setLastModified( dirPath + "/releases-1234567809", new Long( "4234729018" ), false );
        setLastModified( dirPath + "/releases-1234567890", new Long( "2234567890" ), false );

        purgeExecutor.executeTask( purgeReleasesDirTask );

        assertDeleted( dirPath + "/releases-4234729018" );

        assertExists( dirPath + "/1" );
        assertExists( dirPath + "/releases-1234567890" );
        assertExists( dirPath + "/releases-1234567809" );
    }

    public void testBuildOutputPurgingByLastModified()
        throws Exception
    {
        populateBuildOutputDirectory();

        String dirPath = getBuildOutputDirectoryLocation().getAbsolutePath();

        setLastModified( dirPath, 1179382029, true );
        setLastModified( dirPath + "/1/1", 1023453892, false );
        setLastModified( dirPath + "/1/1.log.txt", 1023453892, false );
        setLastModified( dirPath + "/2/4", 1023453892, false );
        setLastModified( dirPath + "/2/4.log.txt", 1023453892, false );

        purgeExecutor.executeTask( purgeBuildOutputDirTask );

        assertDeleted( dirPath + "/1/1" );
        assertDeleted( dirPath + "/1/1.log.txt" );

        assertExists( dirPath + "/1/3" );
        assertExists( dirPath + "/1/3.log.txt" );
        assertExists( dirPath + "/1/6" );
        assertExists( dirPath + "/1/6.log.txt" );

        assertDeleted( dirPath + "/2/4" );
        assertDeleted( dirPath + "/2/4.log.txt" );

        assertExists( dirPath + "/2/7" );
        assertExists( dirPath + "/2/7.log.txt" );
        assertExists( dirPath + "/2/9" );
        assertExists( dirPath + "/2/9.log.txt" );
    }

    public void testBuildOutputPurgingByOrderOfDeletion()
        throws Exception
    {
        populateBuildOutputDirectory();

        String dirPath = getBuildOutputDirectoryLocation().getAbsolutePath();

        setLastModified( dirPath + "/1/6", new Long( "1234567809" ), false );
        setLastModified( dirPath + "/1/6.log.txt", new Long( "1234567809" ), false );
        setLastModified( dirPath + "/1/1", new Long( "4234729018" ), false );
        setLastModified( dirPath + "/1/1.log.txt", new Long( "4234729018" ), false );
        setLastModified( dirPath + "/1/3", new Long( "2234567890" ), false );
        setLastModified( dirPath + "/1/3.log.txt", new Long( "2234567890" ), false );

        setLastModified( dirPath + "/2/7", new Long( "1234567809" ), false );
        setLastModified( dirPath + "/2/7.log.txt", new Long( "1234567809" ), false );
        setLastModified( dirPath + "/2/4", new Long( "4234729018" ), false );
        setLastModified( dirPath + "/2/4.log.txt", new Long( "4234729018" ), false );
        setLastModified( dirPath + "/2/9", new Long( "2234567890" ), false );
        setLastModified( dirPath + "/2/9.log.txt", new Long( "2234567890" ), false );

        purgeExecutor.executeTask( purgeBuildOutputDirTask );

        assertDeleted( dirPath + "/1/6" );
        assertDeleted( dirPath + "/1/6.log.txt" );

        assertExists( dirPath + "/1/3" );
        assertExists( dirPath + "/1/3.log.txt" );
        assertExists( dirPath + "/1/1" );
        assertExists( dirPath + "/1/1.log.txt" );

        assertDeleted( dirPath + "/2/7" );
        assertDeleted( dirPath + "/2/7.log.txt" );

        assertExists( dirPath + "/2/4" );
        assertExists( dirPath + "/2/4.log.txt" );
        assertExists( dirPath + "/2/9" );
        assertExists( dirPath + "/2/9.log.txt" );
    }
}
