package org.apache.continuum.web.test;

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

import org.apache.continuum.web.test.parent.AbstractPurgeTest;
import org.codehaus.plexus.util.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.testng.Assert.assertTrue;

/**
 * Confirms that distributed repositories populate when used and appropriate purge configurations function.
 */
@Test( groups = { "distributed", "purge" } )
public class DistributedPurgeTest
    extends AbstractPurgeTest
{
    private String projectGroupName;

    private String projectGroupId;

    private String projectGroupDescription;

    private String pomUrl;

    private String pomUsername;

    private String pomPassword;

    private String projectName;

    private String agentRepoName;

    private File agentRepo;

    private String purgeDescription;

    @BeforeClass
    @Parameters( { "agentRepoName", "agentRepo" } )
    public void setupClass(
        @Optional( "purgeable" ) String repoName,
        @Optional( "target/data/build-agent/.m2/repository" ) String repoPath )
    {
        loginAsAdmin();

        enableDistributedBuilds();

        addBuildAgent( buildAgentUrl );

        this.agentRepoName = repoName;
        agentRepo = new File( repoPath );
        assertTrue( !agentRepo.exists() );

        purgeDescription = "Wipe entire repo";

        projectGroupName = getProperty( "DISTRIBUTED_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "DISTRIBUTED_PROJECT_GROUP_ID" );
        projectGroupDescription = getProperty( "DISTRIBUTED_PROJECT_GROUP_DESCRIPTION" );
        pomUrl = getProperty( "MAVEN2_POM_URL" );
        pomUsername = getProperty( "MAVEN2_POM_USERNAME" );
        pomPassword = getProperty( "MAVEN2_POM_PASSWORD" );
        projectName = getProperty( "MAVEN2_POM_PROJECT_NAME" );

        addLocalRepository( repoName, agentRepo.getAbsolutePath(), true );
        addProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, repoName, true, true );
        addMavenTwoProject( pomUrl, pomUsername, pomPassword, projectGroupName, true );
    }

    @AfterClass
    public void tearDownClass()
        throws Throwable
    {
        removeProjectGroup( projectGroupName, false );
        removeBuildAgent( buildAgentUrl );
        disableDistributedBuilds();
        removeProjectGroup( projectGroupName, false );
        removeLocalRepository( agentRepoName );
    }

    @BeforeMethod
    public void setup()
    {
        buildProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, projectName, true );
        assertTrue( agentRepo.exists() );
    }

    @AfterMethod
    public void tearDown()
        throws IOException
    {
        FileUtils.deleteDirectory( agentRepo );
        removeRepositoryPurge( purgeDescription, true );
    }

    public void testDistributedRepositoryPurgeAll()
        throws Exception
    {
        addAgentPurgeConfigDeleteAll( agentRepoName, purgeDescription, buildAgentUrl );
        triggerRepoPurge( purgeDescription );
        assertTrue( agentRepo.exists() && agentRepo.list().length == 0 );
    }

    private void triggerRepoPurge( String configDesc )
        throws UnsupportedEncodingException
    {
        goToGeneralPurgePage();
        String purgeLocator =
            String.format( "//preceding::td[text()='%s']//following::img[@alt='Purge']", configDesc );
        clickLinkWithXPath( purgeLocator );
        assertGeneralPurgePage();
        assertTextPresent( "Purge successfully requested" );
    }

    private void addAgentPurgeConfigDeleteAll( String agentRepoName, String configDesc, String agentUrl )
    {
        goToAddRepositoryPurge( true );
        selectValue( "repositoryName", agentRepoName );
        getSelenium().click( "id=saveDistributedPurgeConfig_deleteAll" );
        setFieldValue( "description", configDesc );
        selectValue( "buildAgentUrl", agentUrl );
        submit();
        assertGeneralPurgePage();
        assertTextPresent( configDesc );
    }

    /*
       Defining these locally since we can't use AbstractLocalRepositoryTest code.
       A very real effect of using inheritance rather than composition to share code.
       Having injectable components for shared web test code would make more sense.
     */

    private void addLocalRepository( String repoName, String repoPath, boolean b )
    {
        goToLocalRepositoryPage();
        submit();
        assertPage( "Continuum - Add/Edit Local Repository" );
        setFieldValue( "repository.name", repoName );
        setFieldValue( "repository.location", repoPath );
        submit();
        assertPage( "Continuum - Local Repositories" );
        assertTextPresent( repoName );
    }

    private void goToLocalRepositoryPage()
    {
        clickLinkWithText( "Local Repositories" );
        assertLocalRepositoryPage();
    }

    private void assertLocalRepositoryPage()
    {
        assertPage( "Continuum - Local Repositories" );
    }

    private void removeLocalRepository( String repoName )
    {
        goToLocalRepositoryPage();
        String deleteLocator = String.format( "//preceding::td[text()='%s']//following::img[@alt='Delete']", repoName );
        clickLinkWithXPath( deleteLocator );
        assertTextPresent( "Delete Local Repository" );
        assertTextPresent( "Are you sure you want to delete Local Repository \"" + repoName + "\" ?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertLocalRepositoryPage();
        assertTextNotPresent( repoName );
    }

}
