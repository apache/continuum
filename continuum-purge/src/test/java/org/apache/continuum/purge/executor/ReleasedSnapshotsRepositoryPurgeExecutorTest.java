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
 * Tests were taken from Archiva and added a check if metadata was deleted.
 */
public class ReleasedSnapshotsRepositoryPurgeExecutorTest
    extends AbstractPurgeExecutorTest
{
    protected void setUp()
        throws Exception
    {
        super.setUp();

        populateDefaultRepositoryForReleasedSnapshots();

        purgeDefaultRepoTask = getReleasedSnapshotsRepoPurgeTask();
    }

    public void testDefaultRepoReleasedSnapshotsPurging()
        throws Exception
    {
        String repoRoot = getDefaultRepositoryLocation().getAbsolutePath();

        String projectRoot1 = repoRoot + "/org/apache/maven/plugins/maven-assembly-plugin";
        String projectRoot2 = repoRoot + "/org/apache/maven/plugins/maven-install-plugin";
        String projectRoot3 = repoRoot + "/org/apache/maven/plugins/maven-plugin-plugin";

        purgeExecutor.executeTask( purgeDefaultRepoTask );

        assertMetadataDeleted( projectRoot1 );
        assertMetadataDeleted( projectRoot2 );
        assertMetadataDeleted( projectRoot3 );

        assertDeleted( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070427.065136-1.jar" );
        assertDeleted( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070427.065136-1.jar.sha1" );
        assertDeleted( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070427.065136-1.jar.md5" );
        assertDeleted( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070427.065136-1.pom" );
        assertDeleted( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070427.065136-1.pom.sha1" );
        assertDeleted( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070427.065136-1.pom.md5" );

        assertExists( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070506.163513-2.jar" );
        assertExists( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070506.163513-2.jar.sha1" );
        assertExists( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070506.163513-2.jar.md5" );
        assertExists( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070506.163513-2.pom" );
        assertExists( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070506.163513-2.pom.sha1" );
        assertExists( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070506.163513-2.pom.md5" );

        assertExists( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070615.105019-3.jar" );
        assertExists( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070615.105019-3.jar.sha1" );
        assertExists( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070615.105019-3.jar.md5" );
        assertExists( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070615.105019-3.pom" );
        assertExists( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070615.105019-3.pom.sha1" );
        assertExists( projectRoot1 + "/1.1.2-SNAPSHOT/maven-assembly-plugin-1.1.2-20070615.105019-3.pom.md5" );

        assertDeleted( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-SNAPSHOT.jar" );
        assertDeleted( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-SNAPSHOT.jar.md5" );
        assertDeleted( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-SNAPSHOT.jar.sha1" );
        assertDeleted( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-SNAPSHOT.pom" );
        assertDeleted( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-SNAPSHOT.pom.md5" );
        assertDeleted( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-SNAPSHOT.pom.sha1" );

        assertExists( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-20070513.034619-5.jar" );
        assertExists( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-20070513.034619-5.jar.md5" );
        assertExists( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-20070513.034619-5.jar.sha1" );
        assertExists( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-20070513.034619-5.pom" );
        assertExists( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-20070513.034619-5.pom.md5" );
        assertExists( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-20070513.034619-5.pom.sha1" );

        assertExists( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-20061118.060401-2.jar" );
        assertExists( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-20061118.060401-2.jar.md5" );
        assertExists( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-20061118.060401-2.jar.sha1" );
        assertExists( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-20061118.060401-2.pom" );
        assertExists( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-20061118.060401-2.pom.md5" );
        assertExists( projectRoot2 + "/2.2-SNAPSHOT/maven-install-plugin-2.2-20061118.060401-2.pom.sha1" );

        // check if the snapshot version was removed
        assertDeleted( projectRoot3 + "/2.3-SNAPSHOT" );
        assertDeleted( projectRoot3 + "/2.3-SNAPSHOT/maven-plugin-plugin-2.3-SNAPSHOT.jar" );
        assertDeleted( projectRoot3 + "/2.3-SNAPSHOT/maven-plugin-plugin-2.3-SNAPSHOT.jar.md5" );
        assertDeleted( projectRoot3 + "/2.3-SNAPSHOT/maven-plugin-plugin-2.3-SNAPSHOT.jar.sha1" );
        assertDeleted( projectRoot3 + "/2.3-SNAPSHOT/maven-plugin-plugin-2.3-SNAPSHOT.pom" );
        assertDeleted( projectRoot3 + "/2.3-SNAPSHOT/maven-plugin-plugin-2.3-SNAPSHOT.pom.md5" );
        assertDeleted( projectRoot3 + "/2.3-SNAPSHOT/maven-plugin-plugin-2.3-SNAPSHOT.pom.sha1" );

        // check if the released version was not removed
        assertExists( projectRoot3 + "/2.2" );
        assertExists( projectRoot3 + "/2.2/maven-plugin-plugin-2.2.jar" );
        assertExists( projectRoot3 + "/2.2/maven-plugin-plugin-2.2.jar.md5" );
        assertExists( projectRoot3 + "/2.2/maven-plugin-plugin-2.2.jar.sha1" );
        assertExists( projectRoot3 + "/2.2/maven-plugin-plugin-2.2.pom" );
        assertExists( projectRoot3 + "/2.2/maven-plugin-plugin-2.2.pom.md5" );
        assertExists( projectRoot3 + "/2.2/maven-plugin-plugin-2.2.pom.sha1" );

        assertExists( projectRoot3 + "/2.3" );
        assertExists( projectRoot3 + "/2.3/maven-plugin-plugin-2.3.jar" );
        assertExists( projectRoot3 + "/2.3/maven-plugin-plugin-2.3.jar.md5" );
        assertExists( projectRoot3 + "/2.3/maven-plugin-plugin-2.3.jar.sha1" );
        assertExists( projectRoot3 + "/2.3/maven-plugin-plugin-2.3.pom" );
        assertExists( projectRoot3 + "/2.3/maven-plugin-plugin-2.3.pom.md5" );
        assertExists( projectRoot3 + "/2.3/maven-plugin-plugin-2.3.pom.sha1" );
    }
}
