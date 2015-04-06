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

import org.apache.commons.io.FileUtils;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.AbstractPurgeTest;
import org.apache.continuum.purge.task.PurgeTask;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maria Catherine Tan
 */
public abstract class AbstractPurgeExecutorTest
    extends AbstractPurgeTest
{
    private static final String[] jar_extensions = new String[]{".jar", ".jar.md5", ".jar.sha1"};

    private static final String[] pom_extensions = new String[]{".pom", ".pom.md5", ".pom.sha1"};

    private static final String[] metadata_extensions = new String[]{".xml", ".xml.sha1", ".xml.md5"};

    private static final String TEST_MAVEN_METADATA = "maven-metadata-central";

    private RepositoryPurgeConfiguration repoConfig;

    private DirectoryPurgeConfiguration dirConfig;

    protected TaskExecutor purgeExecutor;

    protected PurgeTask purgeDefaultRepoTask;

    protected PurgeTask purgeReleasesDirTask;

    protected PurgeTask purgeBuildOutputDirTask;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        if ( purgeExecutor == null )
        {
            purgeExecutor = (TaskExecutor) lookup( TaskExecutor.class.getName(), "purge" );
        }
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        FileUtils.deleteDirectory( getDefaultRepositoryLocation() );
        FileUtils.deleteDirectory( getReleasesDirectoryLocation() );
        FileUtils.deleteDirectory( getBuildOutputDirectoryLocation() );
    }

    protected PurgeTask getDaysOldRepoPurgeTask()
        throws Exception
    {
        repoConfig = new RepositoryPurgeConfiguration();
        repoConfig.setRepository( defaultRepository );
        repoConfig.setDaysOlder( AbstractPurgeTest.TEST_DAYS_OLDER );
        repoConfig = repositoryPurgeConfigurationDao.addRepositoryPurgeConfiguration( repoConfig );

        return new PurgeTask( repoConfig.getId() );
    }

    protected PurgeTask getRetentionCountRepoPurgeTask()
        throws Exception
    {
        repoConfig = new RepositoryPurgeConfiguration();
        repoConfig.setRepository( defaultRepository );
        repoConfig.setDaysOlder( -1 );
        repoConfig.setRetentionCount( AbstractPurgeTest.TEST_RETENTION_COUNT );
        repoConfig = repositoryPurgeConfigurationDao.addRepositoryPurgeConfiguration( repoConfig );

        return new PurgeTask( repoConfig.getId() );
    }

    protected PurgeTask getReleasedSnapshotsRepoPurgeTask()
        throws Exception
    {
        repoConfig = new RepositoryPurgeConfiguration();
        repoConfig.setRepository( defaultRepository );
        repoConfig.setDaysOlder( -1 );
        repoConfig.setRetentionCount( AbstractPurgeTest.TEST_RETENTION_COUNT );
        repoConfig.setDeleteReleasedSnapshots( true );
        repoConfig = repositoryPurgeConfigurationDao.addRepositoryPurgeConfiguration( repoConfig );

        return new PurgeTask( repoConfig.getId() );
    }

    protected PurgeTask getDaysOldReleasesDirPurgeTask()
        throws Exception
    {
        dirConfig = new DirectoryPurgeConfiguration();
        dirConfig.setDirectoryType( AbstractPurgeTest.TEST_RELEASES_DIRECTORY_TYPE );
        dirConfig.setLocation( getReleasesDirectoryLocation().getAbsolutePath() );
        dirConfig.setDaysOlder( AbstractPurgeTest.TEST_DAYS_OLDER );
        dirConfig = directoryPurgeConfigurationDao.addDirectoryPurgeConfiguration( dirConfig );

        return new PurgeTask( dirConfig.getId() );
    }

    protected PurgeTask getDaysOldBuildOutputDirPurgeTask()
        throws Exception
    {
        dirConfig = new DirectoryPurgeConfiguration();
        dirConfig.setDirectoryType( AbstractPurgeTest.TEST_BUILDOUTPUT_DIRECTORY_TYPE );
        dirConfig.setLocation( getBuildOutputDirectoryLocation().getAbsolutePath() );
        dirConfig.setDaysOlder( AbstractPurgeTest.TEST_DAYS_OLDER );
        dirConfig = directoryPurgeConfigurationDao.addDirectoryPurgeConfiguration( dirConfig );

        return new PurgeTask( dirConfig.getId() );
    }

    protected PurgeTask getRetentionCountReleasesDirPurgeTask()
        throws Exception
    {
        dirConfig = new DirectoryPurgeConfiguration();
        dirConfig.setDirectoryType( AbstractPurgeTest.TEST_RELEASES_DIRECTORY_TYPE );
        dirConfig.setLocation( getReleasesDirectoryLocation().getAbsolutePath() );
        dirConfig.setDaysOlder( -1 );
        dirConfig.setRetentionCount( AbstractPurgeTest.TEST_RETENTION_COUNT );
        dirConfig = directoryPurgeConfigurationDao.addDirectoryPurgeConfiguration( dirConfig );

        return new PurgeTask( dirConfig.getId() );
    }

    protected PurgeTask getRetentionCountBuildOutputDirPurgeTask()
        throws Exception
    {
        dirConfig = new DirectoryPurgeConfiguration();
        dirConfig.setDirectoryType( AbstractPurgeTest.TEST_BUILDOUTPUT_DIRECTORY_TYPE );
        dirConfig.setLocation( getBuildOutputDirectoryLocation().getAbsolutePath() );
        dirConfig.setDaysOlder( -1 );
        dirConfig.setRetentionCount( AbstractPurgeTest.TEST_RETENTION_COUNT );
        dirConfig = directoryPurgeConfigurationDao.addDirectoryPurgeConfiguration( dirConfig );

        return new PurgeTask( dirConfig.getId() );
    }

    protected PurgeTask getCleanAllDefaultRepoPurgeTask()
        throws Exception
    {
        return new PurgeTask( defaultRepoPurge.getId() );
    }

    protected PurgeTask getCleanAllReleasesDirPurgeTask()
        throws Exception
    {
        return new PurgeTask( defaultReleasesDirPurge.getId() );
    }

    protected PurgeTask getCleanAllBuildOutputDirPurgeTask()
        throws Exception
    {
        return new PurgeTask( defaultBuildOutputDirPurge.getId() );
    }

    protected void setLastModified( String dirPath, long lastModified, boolean recurse )
    {
        File dir = new File( dirPath );

        if ( recurse )
        {
            for ( File content : dir.listFiles() )
            {
                content.setLastModified( lastModified );

                if ( content.list() != null && content.list().length > 0 )
                {
                    setLastModified( content.getAbsolutePath(), lastModified, true );
                }
            }
        }
        else
        {
            dir.setLastModified( lastModified );
        }
    }

    protected void assertIsEmpty( File dir )
    {
        File[] files = dir.listFiles();

        assertEquals( "Directory should be clean: " + dir.getName(), 0, files.length );
    }

    protected void assertDeleted( String path )
    {
        assertFalse( "File should have been deleted: " + path, new File( path ).exists() );
    }

    protected void assertExists( String path )
    {
        assertTrue( "File should exist: " + path, new File( path ).exists() );
    }

    protected void assertMetadataDeleted( String projectRoot )
    {
        assertDeleted( projectRoot + "/" + TEST_MAVEN_METADATA + ".xml" );
        assertDeleted( projectRoot + "/" + TEST_MAVEN_METADATA + ".xml.sha1" );
        assertDeleted( projectRoot + "/" + TEST_MAVEN_METADATA + ".xml.md5" );
    }

    protected void populateDefaultRepositoryForRetentionCount()
        throws Exception
    {
        prepareTestFolders();

        List<String> versions = new ArrayList<String>();
        versions.add( "1.0RC1-20070504.153317-1" );
        versions.add( "1.0RC1-20070504.160758-2" );
        versions.add( "1.0RC1-20070505.090015-3" );
        versions.add( "1.0RC1-20070506.090132-4" );

        createDefaultRepoFiles( "/org/jruby/plugins/jruby-rake-plugin/1.0RC1-SNAPSHOT", "jruby-rake-plugin", versions );

        versions = new ArrayList<String>();
        versions.add( "1.1.2-20070427.065136-1" );
        versions.add( "1.1.2-20070615.105019-3" );
        versions.add( "1.1.2-20070506.163513-2" );

        createDefaultRepoFiles( "/org/codehaus/castor/castor-anttasks/1.1.2-SNAPSHOT", "castor-anttasks", versions );
    }

    protected void populateDefaultRepository()
        throws Exception
    {
        prepareTestFolders();

        List<String> versions = new ArrayList<String>();
        versions.add( "1.1.2-20070427.065136-1" );
        versions.add( "1.1.2-20070506.163513-2" );
        versions.add( "1.1.2-20070615.105019-3" );

        createDefaultRepoFiles( "/org/apache/maven/plugins/maven-assembly-plugin/1.1.2-SNAPSHOT",
                                "maven-assembly-plugin", versions );

        versions = new ArrayList<String>();
        versions.add( "2.2-20061118.060401-2" );
        versions.add( "2.2-20070513.034619-5" );
        versions.add( "2.2-SNAPSHOT" );

        createDefaultRepoFiles( "/org/apache/maven/plugins/maven-install-plugin/2.2-SNAPSHOT", "maven-install-plugin",
                                versions );
    }

    protected void populateDefaultRepositoryForReleasedSnapshots()
        throws Exception
    {
        populateDefaultRepository();

        List<String> versions = new ArrayList<String>();
        versions.add( "2.2" );

        createDefaultRepoFiles( "/org/apache/maven/plugins/maven-plugin-plugin/2.2", "maven-plugin-plugin", versions );

        versions = new ArrayList<String>();
        versions.add( "2.3" );
        createDefaultRepoFiles( "/org/apache/maven/plugins/maven-plugin-plugin/2.3", "maven-plugin-plugin", versions );

        versions = new ArrayList<String>();
        versions.add( "2.3-SNAPSHOT" );
        createDefaultRepoFiles( "/org/apache/maven/plugins/maven-plugin-plugin/2.3-SNAPSHOT", "maven-plugin-plugin",
                                versions );
    }

    protected void populateReleasesDirectory()
        throws Exception
    {
        prepareTestFolders();

        String repoPath = getReleasesDirectoryLocation().getAbsolutePath();

        String[] folders = new String[]{"1", "releases-4234729018", "", "releases-1234567809", "releases-1234567890"};

        for ( String folder : folders )
        {
            File dir = new File( repoPath, folder );
            dir.mkdir();
        }
    }

    protected void populateBuildOutputDirectory()
        throws Exception
    {
        prepareTestFolders();

        String repoPath = getBuildOutputDirectoryLocation().getAbsolutePath();

        File projectDir1 = new File( repoPath, "1" );
        projectDir1.mkdir();

        File projectDir2 = new File( repoPath, "2" );
        projectDir2.mkdir();

        String[] buildOutputs1 = new String[]{"1", "3", "6"};
        String[] buildOutputs2 = new String[]{"4", "7", "9"};

        for ( int i = 0; i < 3; i++ )
        {
            File outputDir1 = new File( projectDir1.getAbsolutePath(), buildOutputs1[i] );
            outputDir1.mkdir();

            File outputFile1 = new File( projectDir1.getAbsolutePath(), buildOutputs1[i] + ".log.txt" );
            outputFile1.createNewFile();

            File outputDir2 = new File( projectDir2.getAbsolutePath(), buildOutputs2[i] );
            outputDir2.mkdir();

            File outputFile2 = new File( projectDir2.getAbsolutePath(), buildOutputs2[i] + ".log.txt" );
            outputFile2.createNewFile();
        }
    }

    private void createDefaultRepoFiles( String versionPath, String artifactId, List<String> versions )
        throws Exception
    {
        String repoPath = getDefaultRepositoryLocation().getAbsolutePath();

        File versionDir = new File( repoPath + versionPath );
        if ( !versionDir.exists() )
        {
            versionDir.mkdirs();

            // create maven-metadata* files
            for ( String metadata_extension : metadata_extensions )
            {
                File metadata = new File( versionDir.getParentFile().getAbsolutePath(),
                                          TEST_MAVEN_METADATA + metadata_extension );
                metadata.createNewFile();
            }
        }

        for ( String version : versions )
        {
            for ( String jar_extension : jar_extensions )
            {
                File file = new File( versionDir.getAbsolutePath(), artifactId + "-" + version + jar_extension );
                file.createNewFile();
            }

            for ( String pom_extension : pom_extensions )
            {
                File file = new File( versionDir.getAbsolutePath(), artifactId + "-" + version + pom_extension );
                file.createNewFile();
            }
        }
    }

    private void prepareTestFolders()
        throws Exception
    {
        FileUtils.cleanDirectory( getDefaultRepositoryLocation() );
        FileUtils.cleanDirectory( getReleasesDirectoryLocation() );
        FileUtils.cleanDirectory( getBuildOutputDirectoryLocation() );
    }
}
