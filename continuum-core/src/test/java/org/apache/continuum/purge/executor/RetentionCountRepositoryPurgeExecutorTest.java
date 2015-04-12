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

import org.junit.Before;
import org.junit.Test;

/**
 * Tests were taken from Archiva and just added a check if the metadata was deleted.
 */
public class RetentionCountRepositoryPurgeExecutorTest
    extends AbstractPurgeExecutorTest
{
    @Before
    public void setUp()
        throws Exception
    {
        purgeDefaultRepoTask = getRetentionCountRepoPurgeTask();
    }

    @Test
    public void testRetentionCountPurging()
        throws Exception
    {
        populateDefaultRepositoryForRetentionCount();

        String repoRoot = getDefaultRepositoryLocation().getAbsolutePath();

        String projectRoot1 = repoRoot + "/org/jruby/plugins/jruby-rake-plugin";
        String projectRoot2 = repoRoot + "/org/codehaus/castor/castor-anttasks";

        purgeExecutor.executeTask( purgeDefaultRepoTask );

        // assert if metadata was removed
        assertMetadataDeleted( projectRoot1 );
        assertMetadataDeleted( projectRoot2 );

        // assert if removed from repo
        assertDeleted( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070504.153317-1.jar" );
        assertDeleted( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070504.153317-1.jar.md5" );
        assertDeleted( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070504.153317-1.jar.sha1" );
        assertDeleted( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070504.153317-1.pom" );
        assertDeleted( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070504.153317-1.pom.md5" );
        assertDeleted( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070504.153317-1.pom.sha1" );

        assertDeleted( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070504.160758-2.jar" );
        assertDeleted( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070504.160758-2.jar.md5" );
        assertDeleted( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070504.160758-2.jar.sha1" );
        assertDeleted( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070504.160758-2.pom" );
        assertDeleted( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070504.160758-2.pom.md5" );
        assertDeleted( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070504.160758-2.pom.sha1" );

        // assert if not removed from repo
        assertExists( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070505.090015-3.jar" );
        assertExists( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070505.090015-3.jar.md5" );
        assertExists( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070505.090015-3.jar.sha1" );
        assertExists( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070505.090015-3.pom" );
        assertExists( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070505.090015-3.pom.md5" );
        assertExists( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070505.090015-3.pom.sha1" );

        assertExists( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070506.090132-4.jar" );
        assertExists( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070506.090132-4.jar.md5" );
        assertExists( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070506.090132-4.jar.sha1" );
        assertExists( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070506.090132-4.pom" );
        assertExists( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070506.090132-4.pom.md5" );
        assertExists( projectRoot1 + "/1.0RC1-SNAPSHOT/jruby-rake-plugin-1.0RC1-20070506.090132-4.pom.sha1" );

        // assert if removed from repo
        assertDeleted( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070427.065136-1.jar" );
        assertDeleted( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070427.065136-1.jar.md5" );
        assertDeleted( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070427.065136-1.jar.sha1" );
        assertDeleted( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070427.065136-1.pom" );
        assertDeleted( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070427.065136-1.pom.md5" );
        assertDeleted( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070427.065136-1.pom.sha1" );

        // assert if not removed from repo
        assertExists( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070615.105019-3.pom" );
        assertExists( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070615.105019-3.pom.md5" );
        assertExists( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070615.105019-3.pom.sha1" );
        assertExists( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070615.105019-3.jar" );
        assertExists( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070615.105019-3.jar.md5" );
        assertExists( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070615.105019-3.jar.sha1" );

        assertExists( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070506.163513-2.pom" );
        assertExists( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070506.163513-2.pom.md5" );
        assertExists( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070506.163513-2.pom.sha1" );
        assertExists( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070506.163513-2.jar" );
        assertExists( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070506.163513-2.jar.md5" );
        assertExists( projectRoot2 + "/1.1.2-SNAPSHOT/castor-anttasks-1.1.2-20070506.163513-2.jar.sha1" );
    }

    @Test
    public void testOrderOfDeletion()
        throws Exception
    {
        populateDefaultRepository();

        String repoRoot = getDefaultRepositoryLocation().getAbsolutePath();

        String projectRoot1 = repoRoot + "/org/apache/maven/plugins/maven-assembly-plugin";
        String projectRoot2 = repoRoot + "/org/apache/maven/plugins/maven-install-plugin";

        purgeExecutor.executeTask( purgeDefaultRepoTask );

        assertMetadataDeleted( projectRoot1 );
        assertMetadataDeleted( projectRoot2 );

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
    }
}
