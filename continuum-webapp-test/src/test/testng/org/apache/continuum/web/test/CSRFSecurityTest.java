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

import org.apache.continuum.web.test.parent.AbstractContinuumTest;
import org.testng.annotations.Test;

/**
 * Test actions that are vulnerable to CSRF.
 */
@Test( groups = { "csrf" }, dependsOnMethods = { "testWithCorrectUsernamePassword" } )
public class CSRFSecurityTest
    extends AbstractContinuumTest
{
    public void testCSRFDeleteProject()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/deleteProject!default.action?projectGroupId=2&projectId=2" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );   
    }

    public void testCSRFRemoveProjectBuildDefinition()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/removeProjectBuildDefinition.action?projectId=1&buildDefinitionId=9&confirmed=true" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );
    }

    public void testCSRFRemoveGroupBuildDefinition()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/removeGroupBuildDefinition.action?projectGroupId=2&buildDefinitionId=8&confirmed=true" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );
    }

    public void testCSRFRemoveProjectGroup()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/removeProjectGroup.action?projectGroupId=2" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );
    } 

    public void testCSRFRemoveBuildResult()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/removeBuildResult.action?projectId=1&buildId=1&confirmed=true" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );
    }

    public void testCSRFRemoveSchedule()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/removeSchedule.action?id=1&name=DEFAULT_SCHEDULE" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );
    }

    public void testCSRFRemoveReleaseResults()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/removeReleaseResults.action?projectGroupId=2&selectedReleaseResults=1&confirmed=true" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );   
    }

    public void testCSRFSaveFooter()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/admin/saveFooter!saveFooter.action?footer=testValue" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );
    }

    public void testCSRFSaveCompanyPOM()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/admin/saveCompanyPom.action" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );
    }

    public void testCSRFDeleteBuildEnvironment()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/deleteBuildEnv.action?profile.id=1" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );
    }

    public void testCSRFDeleteBuildDefinitionTemplate()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/deleteDefinitionTemplate.action?buildDefinitionTemplate.id=5&buildDefinitionTemplate.name=Test+Template" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );
    }

    public void testCSRFDeleteBuildQueue()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/deleteBuildQueue.action?buildQueue.id=3&buildQueue.name=TEST_BUILD_QUEUE" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );
    }

    public void testCSRFRemoveLocalRepository()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/removeRepository.action?repository.id=2" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );
    }

    public void testCSRFRemovePurgeConfiguration()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/removePurgeConfig.action?purgeConfigId=2&confirmed=true" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );
    }

    public void testCSRFDeleteBuildAgent()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/security/deleteBuildAgent.action?buildAgent.url=http%3A%2F%2Flocalhost%3A8181%2Fcontinuum-buildagent%2Fxmlrpc&confirmed=true" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );
    }

    public void testCSRFDeleteBuildAgentGroup()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/security/deleteBuildAgentGroup.action?buildAgentGroup.name=Test+Agent+Group&confirmed=true" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );    
    }

    public void testCSRFDeleteProjectGroupNotifier()
    {
        getSelenium().open( baseUrl );
        getSelenium().open( baseUrl + "/deleteProjectGroupNotifier.action?projectGroupId=2&notifierId=1&notifierType=mail" );
        assertTextPresent( "Security Alert - Invalid Token Found" );
        assertTextPresent( "Possible CSRF attack detected! Invalid token found in the request." );
    }
}
