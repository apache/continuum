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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import org.apache.continuum.web.test.parent.AbstractReleaseTest;
import org.testng.annotations.Test;

@Test( groups = { "release" } )
public class ReleaseTest
    extends AbstractReleaseTest
{
    @Test( dependsOnMethods = { "testProjectGroupAllBuildSuccessWithDistributedBuilds" } )
    public void testReleasePrepareProjectWithInvalidUsernamePasswordInDistributedBuilds()
        throws Exception
    {
        String M2_PROJ_GRP_NAME = getProperty( "M2_DELETE_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_DELETE_PROJ_GRP_ID" );

        String M2_PROJ_USERNAME = "invalid";
        String M2_PROJ_PASSWORD = "invalid";
        String M2_PROJ_TAGBASE = getProperty( "M2_DELETE_PROJ_TAGBASE" );
        String M2_PROJ_TAG = getProperty( "M2_DELETE_PROJ_TAG" );
        String M2_PROJ_RELEASE_VERSION = getProperty( "M2_DELETE_PROJ_RELEASE_VERSION" );
        String M2_PROJ_DEVELOPMENT_VERSION = getProperty( "M2_DELETE_PROJ_DEVELOPMENT_VERSION" );

        init();

        try
        {
            enableDistributedBuilds();
        
            String M2_PROJECT_BUILD_ENV = getProperty( "M2_RELEASE_BUILD_ENV" );
            createBuildEnvAndBuildagentGroup();
            
            showProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, "" );
            clickButtonWithValue( "Release" );
            assertReleaseSuccess();
            releasePrepareProject( M2_PROJ_USERNAME, M2_PROJ_PASSWORD, M2_PROJ_TAGBASE, M2_PROJ_TAG,
                                   M2_PROJ_RELEASE_VERSION, M2_PROJ_DEVELOPMENT_VERSION, M2_PROJECT_BUILD_ENV, false );
            assertPreparedReleasesFileCreated();
        }
        finally
        {
            disableDistributedBuilds();
        }
    }
    
    /*
     * Test release prepare with no build agent configured in the selected build environment.
     */
    @Test( dependsOnMethods = { "testReleasePrepareProjectWithInvalidUsernamePasswordInDistributedBuilds" } )
    public void testReleasePrepareProjectWithNoBuildagentInBuildEnvironment()
        throws Exception
    {
        String M2_PROJECT_POM_URL = getProperty( "M2_RELEASE_POM_URL" );
        String M2_PROJECT_NAME = getProperty( "M2_RELEASE_PROJECT_NAME" );
        String M2_PROJECT_GROUP_NAME = getProperty( "M2_RELEASE_GRP_NAME" );
        String M2_PROJECT_GROUP_ID = getProperty( "M2_RELEASE_GRP_ID" );
        String M2_PROJECT_DESCRIPTION = getProperty( "M2_RELEASE_GRP_DESCRIPTION" );
        String M2_PROJECT_TAGBASE = getProperty( "M2_RELEASE_TAGBASE_URL" );
        String M2_PROJECT_TAG = getProperty( "M2_RELEASE_TAG" );
        String M2_PROJECT_RELEASE_VERSION = getProperty( "M2_RELEASE_RELEASE_VERSION" );
        String M2_PROJECT_DEVELOPMENT_VERSION = getProperty( "M2_RELEASE_DEVELOPMENT_VERSION" );
        String ERROR_TEXT = getProperty( "M2_RELEASE_NO_AGENT_MESSAGE" );
        
        addProjectGroup( M2_PROJECT_GROUP_NAME, M2_PROJECT_GROUP_ID, M2_PROJECT_DESCRIPTION, true );
        addMavenTwoProject( M2_PROJECT_POM_URL, "", "", M2_PROJECT_GROUP_NAME, true );

        init();

        try
        {
            enableDistributedBuilds();
        
            String M2_PROJECT_BUILD_ENV = getProperty( "M2_RELEASE_BUILD_ENV" );
            createBuildEnvAndBuildagentGroup();
            detachBuildagentFromGroup();
            
            buildProjectGroup( M2_PROJECT_GROUP_NAME, M2_PROJECT_GROUP_ID, M2_PROJECT_DESCRIPTION, M2_PROJECT_NAME, true );
            
            clickButtonWithValue( "Release" );
            assertReleaseSuccess();
            releasePrepareProject( "", "", M2_PROJECT_TAGBASE, M2_PROJECT_TAG, M2_PROJECT_RELEASE_VERSION,
                                   M2_PROJECT_DEVELOPMENT_VERSION, M2_PROJECT_BUILD_ENV );
            
            assertTextPresent( "Release Error" );
            assertTextPresent( ERROR_TEXT );
        }
        finally
        {
            attachBuildagentInGroup();
            disableDistributedBuilds();
        }
    }
    
    /*
     * Test release prepare with no build agent group in the selected build environment.
     */
    @Test( dependsOnMethods = { "testReleasePrepareProjectWithNoBuildagentInBuildEnvironment" } )
    public void testReleasePrepareProjectWithNoBuildagentGroupInBuildEnvironment()
        throws Exception
    {
        String M2_PROJECT_NAME = getProperty( "M2_RELEASE_PROJECT_NAME" );
        String M2_PROJECT_GROUP_NAME = getProperty( "M2_RELEASE_GRP_NAME" );
        String M2_PROJECT_GROUP_ID = getProperty( "M2_RELEASE_GRP_ID" );
        String M2_PROJECT_DESCRIPTION = getProperty( "M2_RELEASE_GRP_DESCRIPTION" );
        String M2_PROJECT_TAGBASE = getProperty( "M2_RELEASE_TAGBASE_URL" );
        String M2_PROJECT_TAG = getProperty( "M2_RELEASE_TAG" );
        String M2_PROJECT_RELEASE_VERSION = getProperty( "M2_RELEASE_RELEASE_VERSION" );
        String M2_PROJECT_DEVELOPMENT_VERSION = getProperty( "M2_RELEASE_DEVELOPMENT_VERSION" );
        String ERROR_TEXT = getProperty( "M2_RELEASE_NO_AGENT_MESSAGE" );

        init();

        try
        {
            enableDistributedBuilds();
            
            String M2_PROJECT_BUILD_ENV = getProperty( "M2_RELEASE_BUILD_ENV" );
            createBuildEnvAndBuildagentGroup();
            removeBuildagentGroupFromBuildEnv();
            
            showProjectGroup( M2_PROJECT_GROUP_NAME, M2_PROJECT_GROUP_ID, M2_PROJECT_GROUP_ID );
            
            clickButtonWithValue( "Release" );
            assertReleaseSuccess();
            releasePrepareProject( "", "", M2_PROJECT_TAGBASE, M2_PROJECT_TAG, M2_PROJECT_RELEASE_VERSION,
                                   M2_PROJECT_DEVELOPMENT_VERSION, M2_PROJECT_BUILD_ENV );
            
            assertTextPresent( "Release Error" );
            assertTextPresent( ERROR_TEXT );
        }
        finally
        {
            attachBuildagentGroupToBuildEnv();
            disableDistributedBuilds();
        }
    }
    
    /*
     * Test release prepare with no build environment selected.
     */
    @Test( dependsOnMethods = { "testReleasePrepareProjectWithNoBuildagentGroupInBuildEnvironment" } )
    public void testReleasePrepareProjectWithNoBuildEnvironment()
        throws Exception
    {
        String M2_PROJECT_NAME = getProperty( "M2_RELEASE_PROJECT_NAME" );
        String M2_PROJECT_GROUP_NAME = getProperty( "M2_RELEASE_GRP_NAME" );
        String M2_PROJECT_GROUP_ID = getProperty( "M2_RELEASE_GRP_ID" );
        String M2_PROJECT_DESCRIPTION = getProperty( "M2_RELEASE_GRP_DESCRIPTION" );
        String M2_PROJECT_TAGBASE = getProperty( "M2_RELEASE_TAGBASE_URL" );
        String M2_PROJECT_TAG = getProperty( "M2_RELEASE_TAG" );
        String M2_PROJECT_RELEASE_VERSION = getProperty( "M2_RELEASE_RELEASE_VERSION" );
        String M2_PROJECT_DEVELOPMENT_VERSION = getProperty( "M2_RELEASE_DEVELOPMENT_VERSION" );
        String ERROR_TEXT = getProperty( "M2_RELEASE_NO_AGENT_MESSAGE" );

        init();

        try
        {
            enableDistributedBuilds();
            
            showProjectGroup( M2_PROJECT_GROUP_NAME, M2_PROJECT_GROUP_ID, M2_PROJECT_GROUP_ID );
            
            clickButtonWithValue( "Release" );
            assertReleaseSuccess();
            releasePrepareProject( "", "", M2_PROJECT_TAGBASE, M2_PROJECT_TAG, M2_PROJECT_RELEASE_VERSION,
                                   M2_PROJECT_DEVELOPMENT_VERSION, "" );
            
            assertTextPresent( "Release Error" );
            assertTextPresent( ERROR_TEXT );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }

    @Test( dependsOnMethods = { "testReleasePrepareProjectWithNoBuildEnvironment" } )
    public void testReleasePerformUsingProvideParamtersWithDistributedBuilds()
        throws Exception
    {
        String M2_PROJ_GRP_NAME = getProperty( "M2_DELETE_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_DELETE_PROJ_GRP_ID" );

        String M2_PROJ_USERNAME = "invalid";
        String M2_PROJ_PASSWORD = "invalid";
        String M2_PROJ_TAGBASE = getProperty( "M2_DELETE_PROJ_TAGBASE_PERFORM" );
        String M2_PROJ_TAG = getProperty( "M2_DELETE_PROJ_TAG" );
        String M2_PROJ_SCM_URL = getProperty( "M2_DELETE_PROJ_GRP_SCM_ROOT_URL" );

        init();
        
        try
        {
            enableDistributedBuilds();
        
            String M2_PROJECT_BUILD_ENV = getProperty( "M2_RELEASE_BUILD_ENV" );
            createBuildEnvAndBuildagentGroup();
            
            showProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, "" );
            clickButtonWithValue( "Release" );
            assertReleaseSuccess();
            releasePerformProjectWithProvideParameters( M2_PROJ_USERNAME, M2_PROJ_PASSWORD, M2_PROJ_TAGBASE, M2_PROJ_TAG, 
                                                        M2_PROJ_SCM_URL, M2_PROJECT_BUILD_ENV, false );
            assertPreparedReleasesFileCreated();

            removeProjectGroup( M2_PROJ_GRP_NAME );
            assertLinkNotPresent( M2_PROJ_GRP_NAME );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }

    private void init()
    {
        File file = new File( "target/conf/prepared-releases.xml" );

        if ( file.exists() )
        {
            file.delete();
        }
    }
    
    private void createBuildEnvAndBuildagentGroup()
        throws Exception
    {
        String M2_PROJECT_BUILD_ENV = getProperty( "M2_RELEASE_BUILD_ENV" );
        String M2_PROJECT_AGENT_GROUP = getProperty( "M2_RELEASE_AGENT_GROUP" );
        
        // add build agent group no agents
        goToBuildAgentPage();
        if ( !isTextPresent( M2_PROJECT_AGENT_GROUP ) )
        {
            clickAndWait( "//input[@id='editBuildAgentGroup_0']" );
            setFieldValue( "saveBuildAgentGroup_buildAgentGroup_name", M2_PROJECT_AGENT_GROUP );
            clickButtonWithValue( "Save" );
        }
            
        // add build environment with build agent group
        clickLinkWithText( "Build Environments" );
        if ( !isTextPresent( M2_PROJECT_BUILD_ENV ) )
        {
            clickAndWait( "//input[@id='addBuildEnv_0']" );
            setFieldValue( "saveBuildEnv_profile_name", M2_PROJECT_BUILD_ENV );
            clickButtonWithValue( "Save" );
            attachBuildagentGroupToBuildEnv();
        }
        
        // attach build agent in build agent group created
        attachBuildagentInGroup();
    }
    
    private void attachBuildagentGroupToBuildEnv()
    {
        String M2_PROJECT_BUILD_ENV = getProperty( "M2_RELEASE_BUILD_ENV" );
        String M2_PROJECT_AGENT_GROUP = getProperty( "M2_RELEASE_AGENT_GROUP" );
        
        clickLinkWithText( "Build Environments" );
        String xPath = "//preceding::td[text()='" + M2_PROJECT_BUILD_ENV + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        selectValue( "profile.buildAgentGroup", M2_PROJECT_AGENT_GROUP );
        clickButtonWithValue( "Save" );
    }
    
    private void removeBuildagentGroupFromBuildEnv()
    {
        String M2_PROJECT_BUILD_ENV = getProperty( "M2_RELEASE_BUILD_ENV" );
        
        clickLinkWithText( "Build Environments" );
        String xPath = "//preceding::td[text()='" + M2_PROJECT_BUILD_ENV + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        selectValue( "profile.buildAgentGroup", "" );
        clickButtonWithValue( "Save" );
    }
    
    private void attachBuildagentInGroup()
        throws Exception
    {
        String M2_PROJECT_AGENT_GROUP = getProperty( "M2_RELEASE_AGENT_GROUP" );
        String buildAgent = getBuildAgentUrl();
        
        clickLinkWithText( "Build Agents" );
        String xPath = "//preceding::td[text()='" + M2_PROJECT_AGENT_GROUP + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        
        if ( isElementPresent( "xpath=//select[@id='saveBuildAgentGroup_buildAgentIds']/option[@value='" + buildAgent + "']" ) )
        {
            selectValue( "buildAgentIds", buildAgent );
            clickLinkWithXPath( "//input[@value='->']", false );
            submit();
        }
    }
    
    private void detachBuildagentFromGroup()
        throws Exception
    {
        String M2_PROJECT_AGENT_GROUP = getProperty( "M2_RELEASE_AGENT_GROUP" );
        String buildAgent = getBuildAgentUrl();
        
        clickLinkWithText( "Build Agents" );
        String xPath = "//preceding::td[text()='" + M2_PROJECT_AGENT_GROUP + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        
        if ( isElementPresent( "xpath=//select[@id='saveBuildAgentGroup_selectedBuildAgentIds']/option[@value='" + buildAgent + "']" ) )
        {
            selectValue( "selectedBuildAgentIds", buildAgent );
            clickLinkWithXPath( "//input[@value='<-']", false );
            submit();
        }
    }
}
