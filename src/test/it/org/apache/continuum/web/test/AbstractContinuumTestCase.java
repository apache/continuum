package org.apache.continuum.web.test;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractContinuumTestCase
    extends AbstractSeleniumTestCase
{

    public final static String DEFAULT_PROJ_GRP_NAME = "Default Project Group";

    public final static String DEFAULT_PROJ_GRP_ID = "default";

    public final static String DEFAULT_PROJ_GRP_DESCRIPTION =
        "Contains all projects that do not have a group of their own";

    public final static String TEST_PROJ_GRP_NAME = "Test Project Group Name";

    public final static String TEST_PROJ_GRP_ID = "Test Project Group Id";

    public final static String TEST_PROJ_GRP_DESCRIPTION = "Test Project Group Description";

    public final static String TEST_POM_URL = "http://svn.apache.org/repos/asf/maven/pom/trunk/maven/pom.xml";

    public final static String TEST_POM_USERNAME = "dummy";

    public final static String TEST_POM_PASSWORD = "dummy";

    public static final String TEST_PROJECT_GROUP_NAME = "Apache Maven";

    public static final String TEST_PROJECT_NAME = "Apache Maven";

    
    //////////////////////////////////////
    // Overriden AbstractSeleniumTestCase methods
    //////////////////////////////////////
    protected String getApplicationName()
    {
        return "Continuum";
    }

    protected String getInceptionYear()
    {
        return "2005";
    }

    public void assertHeader()
    {
        assertElementPresent( "xpath=//img[@alt='Continuum']" );
        assertLinkPresent( "Continuum" );
        assertLinkPresent( "Maven" );
        assertLinkPresent( "Apache" );
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    //////////////////////////////////////
    // About
    //////////////////////////////////////
    public void goToAboutPage()
    {
        clickLinkWithText( "About" );

        assertAboutPage();
    }

    public void assertAboutPage()
    {
        assertPage( "Continuum - About" );
        assertTextPresent( "About Continuum" );
        assertTextPresent( "Version:" );
        assertTextPresent( "1.3.2-SNAPSHOT" );
        assertTextPresent( "Build Number:" );
    }

    //////////////////////////////////////
    // Configuration
    //////////////////////////////////////
    public void goToConfigurationPage() 
    {
    	clickLinkWithText( "Configuration" );
    	
    	assertEditConfigurationPage();
    }
    
    public void assertEditConfigurationPage()
    {
    	assertPage( "Continuum - Configuration" );
    	String configText = "General Configuration,Working Directory*:,Build Output Directory*:,Release Output Directory:,Deployment Repository Directory:,Base URL*:,Number of Allowed Builds in Parallel:";
    	String[] arrayConfigText = configText.split(",");
    	for( String configtext : arrayConfigText )
    		assertTextPresent( configtext );
    	String configElements = "configuration_workingDirectory,configuration_buildOutputDirectory,configuration_releaseOutputDirectory,configuration_deploymentRepositoryDirectory,configuration_baseUrl,configuration_numberOfAllowedBuildsinParallel,configuration_distributedBuildEnabled";
    	String[] arrayConfigElements = configElements.split( "," );
    	for ( String configelements : arrayConfigElements )
    		assertElementPresent( configelements );
    }

    
    //////////////////////////////////////
    // ANT/SHELL Projects
    //////////////////////////////////////
    public void assertAddProjectPage( String type )
    {
        String title = type.substring( 0, 1 ).toUpperCase() + type.substring( 1 ).toLowerCase();
        assertPage( "Continuum - Add" + title + " Project" );
        assertTextPresent( "Add " + title + " Project" );
        assertTextPresent( "Project Name*:" );
        assertElementPresent( "projectName" );
        assertTextPresent( "Description:" );
        assertElementPresent( "projectDescription" );
        assertTextPresent( "Version*:" );
        assertElementPresent( "projectVersion" );
        assertTextPresent( "Scm Url*:" );
        assertElementPresent( "projectScmUrl" );
        assertTextPresent( "Scm Username:" );
        assertElementPresent( "projectScmUsername" );
        assertTextPresent( "Scm Password:" );
        assertElementPresent( "projectScmPassword" );
        assertTextPresent( "Scm Branch/Tag:" );
        assertElementPresent( "projectScmTag" );
        assertElementPresent( "projectScmUseCache" );
        assertTextPresent( "Project Group Name:" );
        assertElementPresent( "selectedProjectGroup" );
        assertTextPresent( "Build Definition Template:" );
        assertElementPresent( "buildDefinitionTemplateId" );
    }

    public void assertAddAntProjectPage()
    {
        assertAddProjectPage( "ant" );
    }

    public void assertAddShellProjectPage()
    {
        assertAddProjectPage( "shell" );
    }
    
    //////////////////////////////////////
    // Parallel Build Queue
    //////////////////////////////////////
    public void goToParallelBuildQueuePage()
    {
    	clickLinkWithText( "Build Queue" );
    	assertParallelBuildQueuePage();
    }
    
    public void assertParallelBuildQueuePage() 
    {
    	assertPage( "Continuum - Parallel Build Queue" );
    	assertTextPresent( "Continuum - Parallel Build Queue" );
    	assertTextPresent( "Name" );
    	assertTextPresent( "DEFAULT_BUILD_QUEUE" );
    }
    
    public void assertAddParallelBuildQueuePage() 
    {
    	assertPage( "Continuum - Add/Edit Parallel Build Queue" );
    	assertTextPresent( "Continuum - Add/Edit Parallel Build Queue" );
    	assertTextPresent( "Name*:" );
    	assertElementPresent( "name" );
    }
    
    public void addParallelBuildQueue(String name) 
    {
    	goToParallelBuildQueuePage();
    	
    	clickButtonWithValue( "Add" );
    	assertAddParallelBuildQueuePage();
    	
    	setFieldValue( "name", name );
    	clickButtonWithValue( "Save" );
    	
    	if ( isTextPresent( "You are only allowed 1 number of builds in parallel." ))
    	{
    		goToConfigurationPage();
    		setFieldValue( "numberOfAllowedBuildsinParallel", "3" );
    	}
    	else
    	{
    		assertPage( "Continuum - Parallel Build Queue" );
    	}
    }
    
    public void removeParallelBuildQueue()
    {
    	clickLinkWithLocator( "//table[@id='ec_table']/tbody/tr[2]/td[2]/a/img" );
    }
    
    
    //////////////////////////////////////
    // Local Repositories
    //////////////////////////////////////
    public void goToLocalRepositoriesPage()
    {
    	clickLinkWithText( "Local Repositories" );
    	assertLocalRepositoryPage();
    }

    public void assertLocalRepositoryPage() 
    {
    	assertPage( "Continuum - Local Repositories" );
    	
    	assertTextPresent( "Local Repositories" );
    	String tableElement = "ec_table";
    	assertCellValueFromTable("Name", tableElement, 0, 0);
    	assertCellValueFromTable("Location", tableElement, 0, 1);
    	assertCellValueFromTable("Layout", tableElement, 0, 2);
    	assertCellValueFromTable("", tableElement, 0, 3);
    	assertCellValueFromTable("", tableElement, 0, 4);
    	assertCellValueFromTable("", tableElement, 0, 5);
    	assertCellValueFromTable("DEFAULT", tableElement, 1, 0);
    	assertCellValueFromTable("default", tableElement, 1, 2);
    	assertImgWithAlt( "Edit" );
    	assertImgWithAlt( "Purge" );
    	assertImgWithAlt( "Delete" );
    }
    
    public void assertAddEditLocalRepositoryPage() 
    {
    	assertPage( "Continuum - Add/Edit Local Repository" );
    	assertTextPresent( "Continuum - Add/Edit Local Repository" );
    	assertTextPresent( "Name*:" );
    	assertElementPresent( "repository.name" );
    	assertTextPresent( "Location*:" );
    	assertElementPresent( "repository.location" );
    	assertTextPresent( "Layout:" );
    	assertElementPresent( "repository.layout" );
    }
    
    public void addLocalRepository( String name, String location, String layout ) 
    {
    	goToLocalRepositoriesPage();
    	
    	clickLinkWithText( "Add" );
    	assertAddEditLocalRepositoryPage();
    	
    	setFieldValue( "repository.name" , name );
    	setFieldValue( "repository.location" , location );
    	setFieldValue( "repository.layout", layout );
    	clickButtonWithValue( "Save" );
    	assertPage( "Continuum - Local Repositories" )	;
    }

    //////////////////////////////////////
    // Purge Configuration
    //////////////////////////////////////
    public void goToPurgeConfigPage()
    {
    	clickLinkWithText( "Purge Configurations" );
    	assertPurgeConfigurationPage();
    }
    
    public void assertPurgeConfigurationPage() 
    {
    	assertPage( "Continuum - Purge Configurations" );
    	
    	assertTextPresent( "Repository Purge Configurations" );
    	String tableElement = "ec_table";
    	assertCellValueFromTable( "Repository", tableElement, 0, 0 );
    	assertCellValueFromTable( "Days Older", tableElement, 0, 1 );
    	assertCellValueFromTable( "Retention Count", tableElement, 0, 2 );
    	assertCellValueFromTable( "Delete All", tableElement, 0, 3 );
    	assertCellValueFromTable( "Delete Released Snapshots", tableElement, 0, 4 );
    	assertCellValueFromTable( "Schedule", tableElement, 0, 5 );
    	assertCellValueFromTable( "Default", tableElement, 0, 6 );
    	assertCellValueFromTable( "Enabled", tableElement, 0, 7 );
    	assertCellValueFromTable( "Description", tableElement, 0, 8 );
    	assertCellValueFromTable( "", tableElement, 0, 9 );
    	assertCellValueFromTable( "", tableElement, 0, 10 );
    	assertCellValueFromTable( "", tableElement, 0, 11 );
    	assertCellValueFromTable( "DEFAULT", tableElement, 1, 0 );
    	assertCellValueFromTable( "100", tableElement, 1, 1 );
    	assertCellValueFromTable( "2", tableElement, 1, 2 );
    	assertCellValueFromTable( "false", tableElement, 1, 3 );
    	assertCellValueFromTable( "false", tableElement, 1, 4 );
    	assertCellValueFromTable( "", tableElement, 1, 5 );
    	assertCellValueFromTable( "true", tableElement, 1, 6 );
    	assertCellValueFromTable( "true", tableElement, 1, 7 );
    	assertCellValueFromTable( "", tableElement, 1, 8 );
    	assertImgWithAlt("Edit");
    	assertImgWithAlt("Purge");
    	assertImgWithAlt("Deletes");
    	
    	assertTextPresent( "Directory Purge Configurations" );
    	assertTextPresent( "Directory Type" );
    	assertTextPresent( "Days Older" );
    	assertTextPresent( "Retention Count" );
    	assertTextPresent( "Delete All" );
    	assertTextPresent( "Schedule" );
    	assertTextPresent( "Default" );
    	assertTextPresent( "Enabled" );
    	assertTextPresent( "Description" );
    }
    
    public void addRepositoryPurgeConfig(String repository, String daysOlder, 
    		String retentionCount, String schedule, String description) 
    {
    	goToPurgeConfigPage();
    	
    	clickButtonWithValue( "Add" );
    	assertAddEditRepositoryPurgeConfigPage();
    	selectValue( "savePurgeConfig_repositoryId", repository );
    	setFieldValue( "daysOlder", daysOlder );
    	setFieldValue( "retentionCount", retentionCount );
    	selectValue( "savePurgeConfig_scheduleId", schedule );
    	setFieldValue( "description", description );
    	
    	clickButtonWithValue( "Save" );
    	assertPage( "Continuum - Purge Configurations" );
    }
    
    public void removeRepositoryPurgeConfig()
    {
    	goToPurgeConfigPage();
    	clickLinkWithLocator( "//table[@id='ec_table']/tbody/tr[2]/td[12]/a/img" );
    	assertPage( "Delete Purge Configuration" );
    	clickButtonWithValue( "Delete" );
    }
    
    public void editRepositoryPurgeConfig(String repository, String daysOlder, 
    		String retentionCount, String schedule, String description)
    {
    	//TODO
    }
    
    public void assertAddEditRepositoryPurgeConfigPage() 
    {
    	assertPage( "Continuum - Add/Edit Repository Purge Configuration" );
    	assertTextPresent( "Add/Edit Repository Purge Configuration" );
    	assertTextPresent( "Repository*:" );
    	assertElementPresent( "repositoryId" );
    	assertTextPresent( "Days Older:" );
    	assertElementPresent( "daysOlder" );
    	assertTextPresent( "Retention Count:" );
    	assertElementPresent( "retentionCount" );
    	assertElementPresent( "deleteAll" );
    	assertElementPresent( "deleteReleasedSnapshots" );
    	assertElementPresent( "defaultPurgeConfiguration" );
    	assertTextPresent( "Schedule:" );
    	assertElementPresent( "scheduleId" );
    	assertTextPresent( "Description:" );
    	assertElementPresent( "description" );
    }
    
    public void addDirectoryPurgeConfig(String repository, String daysOlder, 
    		String retentionCount, String schedule, String description)
    {
    	//TODO
    }
    
    public void editDirectoryPurgeConfig(String repository, String daysOlder, 
    		String retentionCount, String schedule, String description)
    {
    	//TODO
    }
    
    public void removeDirectoryPurgeConfig()
    {
    	//TODO
    }
    
    public void assertAddEditDirectoryPurgeConfigurationPage()
    {
    	assertPage( "Continuum - Add/Edit Directory Purge Configuration" );
    	assertTextPresent( "Add/Edit Directory Purge Configuration" );
    	assertTextPresent( "Directory Type:" );
    	assertElementPresent( "directoryType" );
    	assertTextPresent( "Days Older:" );
    	assertElementPresent( "daysOlder" );
    	assertTextPresent( "Retention Count:" );
    	assertElementPresent( "retentionCount" );
    	assertElementPresent( "deleteAll" );
    	assertElementPresent( "defaultPurgeConfiguration" );
    	assertTextPresent( "Schedule:" );
    	assertElementPresent( "scheduleId" );
    	assertTextPresent( "Description:" );
    	assertElementPresent( "description" );
    }
    
    //////////////////////////////////////
    // Schedules
    //////////////////////////////////////
    public void goToSchedulesPage()
    {
    	clickLinkWithText( "Schedules" );
    	assertSchedulesPage();
    }
    
    public void assertSchedulesPage() 
    {
    	assertPage( "Continuum - Schedules" );
    	
    	assertTextPresent( "Schedules" );
    	String tableElement = "ec_table";
    	assertCellValueFromTable("Name", tableElement, 0, 0);
    	assertCellValueFromTable("Description", tableElement, 0, 1);
    	assertCellValueFromTable("Quiet Period", tableElement, 0, 2);
    	assertCellValueFromTable("Cron Expression", tableElement, 0, 3);
    	assertCellValueFromTable("Max Job Time", tableElement, 0, 4);
    	assertCellValueFromTable("Active", tableElement, 0, 5);
    	assertCellValueFromTable("", tableElement, 1, 6);
    	assertCellValueFromTable("", tableElement, 1, 7);
    	
    	assertCellValueFromTable("DEFAULT_SCHEDULE", tableElement, 1, 0);
    	assertCellValueFromTable("Run hourly", tableElement, 1, 1);
    	assertCellValueFromTable("0", tableElement, 1, 2);
    	assertCellValueFromTable("exact:0 0 * * * ?", tableElement, 1, 3);
    	assertCellValueFromTable("3600", tableElement, 1, 4);
    	assertCellValueFromTable("true", tableElement, 1, 5);
    	assertImgWithAlt( "Edit" );
    	assertImgWithAlt( "Delete" );
    }
    
    //////////////////////////////////////
    // Installations
    //////////////////////////////////////
    public void assertInstallationTypeChoicePage() 
    {
    	assertPage( "Continuum - Installation Type Choice" );
    	assertTextPresent( "Installation Type Choice" );
    	assertTextPresent( "Installation Type:" );
    	assertElementPresent( "installationType" );
    }
    
    public void assertAddEditInstallationToolPage() 
    {
    	assertPage( "Continuum - Installation" );
    	assertTextPresent( "Continuum - Installation" );
    	assertTextPresent( "Name*:" );
    	assertElementPresent( "installation.name" );
    	assertTextPresent( "Type:" );
    	assertElementPresent( "installation.type" );
    	assertTextPresent( "Value/Path*:" );
    	assertElementPresent( "installation.varValue" );
    	assertElementPresent( "automaticProfile" );
    	assertTextPresent( "Create a Build Environment with the Installation name" );
    }
    
    public void assertAddEditInstallationEnvironmentVariablePage()
    {
    	assertPage( "Continuum - Installation" );
    	assertTextPresent( "Continuum - Installation" );
    	assertTextPresent( "Name*:" );
    	assertElementPresent( "installation.name" );
    	assertTextPresent( "Environment Variable Name*:" );
    	assertElementPresent( "installation.varName" );
    	assertTextPresent( "Value/Path*:" );
    	assertElementPresent( "installation.varValue" );
    	assertElementPresent( "automaticProfile" );
    }
    
    public void assertInstallationPage() 
    {
    	assertPage( "Continuum - Installations" );
    	assertTextPresent( "Installations" );
    	assertTextPresent( "Name" );
    	assertTextPresent( "Type" );
    	assertTextPresent( "Environment Variable Name" );
    	assertTextPresent( "Value/Path" );
    }
    
    //////////////////////////////////////
    // Build Environments
    //////////////////////////////////////
    public void assertBuildEnvironmentPage() 
    {
    	assertPage( "Continuum - Build Environments" );
    	assertTextPresent( "Build Environments" );
    }
    
    public void assertBuildEnvironmentNamePage() 
    {
    	assertPage( "Continuum - Build Environment" );
    	assertTextPresent( "Build Environment" );
    	assertTextPresent( "Build Environment Name*:" );
    	assertElementPresent( "profile.name" );
    }
    
    public void assertBuildEnvironmentBuildAgentInstallationPage()
    {
    	assertPage( "Continuum - Build Environment" );
    	assertTextPresent( "Build Environment" );
    	assertTextPresent( "Build Environment Name*:" );
    	assertElementPresent( "profile.name" );
    	assertTextPresent( "Build Agent Group:" );
    	assertElementPresent( "profile.buildAgentGroup" );
    	assertTextPresent( "Installation Name" );
    	assertTextPresent( "Type" );
    	assertElementPresent( "installationId" );
    }
    
    //////////////////////////////////////
    // Queues
    //////////////////////////////////////
    public void assertQueuePage() 
    {
    	assertPage( "Continuum - Queues" );
    	assertTextPresent( "Current Build" );
    	assertTextPresent( "Build Queue" );
    	assertTextPresent( "Project Name" );
    	assertTextPresent( "Build Definition" );
    	assertTextPresent( "Continuum - Build Queue" );
    	assertTextPresent( "Current Checkout" );
    	assertTextPresent( "Checkout Queue" );
    }
    
    //////////////////////////////////////
    // Build Definition Templates
    //////////////////////////////////////
    public void assertBuildDefTemplatesPage() 
    {
    	//Available Templates Table Assertion
    	assertPage( "Continuum - Build Definition Templates" );
    	assertTextPresent( "Available Templates" );
    	assertTextPresent( "Name" );
    	assertTextPresent( "Default Ant Template" );
    	assertImgWithAlt( "Edit" );
    	assertImgWithAlt( "Disabled" );
    	assertTextPresent( "Default Maven 1 Template" );
    	
    	assertTextPresent( "Default Maven 2 Template" );
    	assertTextPresent( "Default Shell Template" );
    	
    	//Available Build Definitions Table Assertion
    	assertTextPresent( "Goals" );
    	assertTextPresent( "Arguments" );
    	assertTextPresent( "Build File" );
    	assertTextPresent( "Schedule" );
    	assertTextPresent( "Build Environment" );
    	assertTextPresent( "Is Build Fresh?" );
    	assertTextPresent( "Default" );
    	assertTextPresent( "Description" );
    	assertTextPresent( "Type" );
    	
    }
    
    public void assertAddEditAvailableTemplatesPage()
    {
    	assertPage( "Continuum - Build Definition Template" );
    	assertTextPresent( "Build Definition Template" );
    	assertTextPresent( "Name*:" );
    	assertElementPresent( "buildDefinitionTemplate.name" );
    	assertTextPresent( "Configure the used Build Definitions:" );
    	assertElementPresent( "buildDefinitionIds" );
    	assertElementPresent( "selectedBuildDefinitionIds" );
    }
    
    public void assertAddEditAvailableBuildDefPage()
    {
    	assertPage( "Continuum - Add/Edit Build Definition" );
    	assertTextPresent( "Add/Edit Build Definition" );
    	assertTextPresent( "POM filename*:" );
    	assertElementPresent( "buildFile" );
    	assertTextPresent( "Goals:" );
    	assertElementPresent( "goals" );
    	assertTextPresent( "Arguments:" );
    	assertElementPresent( "arguments" );
    	assertElementPresent( "buildFresh" );
    	assertElementPresent( "alwaysBuild" );
    	assertElementPresent( "defaultBuildDefinition" );
    	assertTextPresent( "Schedule:" );
    	assertElementPresent( "scheduleId" );
    	assertTextPresent( "Build Environment:" );
    	assertElementPresent( "profileId" );
    	assertTextPresent( "Type:" );
    	assertElementPresent( "buildDefinitionType" );
    	assertTextPresent( "Description*:" );
    	assertElementPresent( "description" );
    }
    
    //TODO assertion for Appearance Page
    
    //////////////////////////////////////
    // Build Agents
    //////////////////////////////////////
    public void assertBuildAgentPage()
    {
    	assertPage( "Continuum - Build Agents" );
    	assertTextPresent( "Build Agent URL" );
    	assertTextPresent( "Enabled" );
    	assertTextPresent( "Description" );
    	assertTextPresent( "Build Agent Groups" );
    	assertTextPresent( "Name" );
    	assertTextPresent( "Build Agents" );
    }
    
    public void assertAddEditBuildAgentPage() 
    {
    	assertPage( "Continuum - Add/Edit Build Agent" );
    	assertTextPresent( "Continuum - Add/Edit Build Agent" );
    	assertTextPresent( "Build Agent URL*:" );
    	assertElementPresent( "buildAgent.url" );
    	assertTextPresent( "Description:" );
    	assertElementPresent( "buildAgent.description" );
    	assertElementPresent( "buildAgent.enabled" );
    }
    
    public void assertAddEditBuildAgentGroupPage() 
    {
    	assertPage( "Continuum - Add/Edit Build Agent Group" );
    	assertTextPresent( "Add/Edit Build Agent Group" );
    	assertTextPresent( "Name*:" );
    	assertElementPresent( "buildAgentGroup.name" );
    }
    
    //////////////////////////////////////
    // Project Groups
    //////////////////////////////////////
    public void goToProjectGroupsSummaryPage()
        throws Exception
    {
        clickLinkWithText( "Show Project Groups" );
    }

    public void assertProjectGroupsSummaryPage()
    	throws Exception
    {
    	goToProjectGroupsSummaryPage();
        assertPage( "Continuum - Group Summary" );

        if ( isTextPresent( "Project Groups list is empty." ) )
        {
            assertTextNotPresent( "Name" );
            assertTextNotPresent( "Group Id" );
            assertTextNotPresent( "Total" );
            assertTextNotPresent( "Summary" );
        }
        else
        {
        	String tableElement = "ec_table";
        	assertCellValueFromTable( "Name", tableElement, 0, 0 );
        	assertCellValueFromTable( "Group Id", tableElement, 0, 1 );
        	assertCellValueFromTable( "", tableElement, 0, 2 );
        	assertCellValueFromTable( "", tableElement, 0, 3 );
        	assertCellValueFromTable( "", tableElement, 0, 4 );
        	assertCellValueFromTable( "", tableElement, 0, 5 );
        	assertCellValueFromTable( "", tableElement, 0, 6 );
        	assertCellValueFromTable( "", tableElement, 0, 7 );
        	assertCellValueFromTable( "Total", tableElement, 0, 8 );
        	assertCellValueFromTable( "Default Project Group", tableElement, 1, 0 );
        	assertCellValueFromTable( "default", tableElement, 1, 1 );
        	assertImgWithAlt( "Build all projects" );
        	assertImgWithAlt( "Release Group" );
        	assertImgWithAlt( "Delete Group" );
        	assertCellValueFromTable( "0", tableElement, 1, 5 );
        	assertCellValueFromTable( "0", tableElement, 1, 6 );
        	assertCellValueFromTable( "0", tableElement, 1, 7 );
        	assertCellValueFromTable( "0", tableElement, 1, 8 );
            
        }
    }

    //////////////////////////////////////
    // Project Group
    //////////////////////////////////////
    public void showProjectGroup( String name, String groupId, String description )
        throws Exception
    {
        goToProjectGroupsSummaryPage();

        // Checks the link to the created Project Group
        assertLinkPresent( name );
        clickLinkWithText( name );

        assertProjectGroupSummaryPage( name, groupId, description);
    }

    public void assertProjectGroupSummaryPage( String name, String groupId, String description )
    {
        assertTextPresent( "Project Group Name:" );
        assertTextPresent( name );
        assertTextPresent( "Group Id:" );
        assertTextPresent( groupId );
        assertTextPresent( "Description:" );
        assertTextPresent( description );
        assertTextPresent( "Local Repository" );
        assertTextPresent( "DEFAULT" );

        //Assert Project Group SCM Root
        assertTextPresent( "Project Group Scm Root" );
        assertTextPresent( "Scm Root URL" );
        
        // Assert the available Project Group Actions
        assertTextPresent( "Group Actions" );
        assertElementPresent( "buildDefinitionId" );
        assertElementPresent( "build" );
        assertElementPresent( "edit" );
        assertElementPresent( "release" );
        assertElementPresent( "preferredExecutor" );
        assertButtonWithValuePresent("Add");
        assertElementPresent( "remove" );
        assertElementPresent( "cancel" );

        if ( isTextPresent( "Member Projects" ) )
        {
        	assertTextPresent( "Project Name" );
            assertTextPresent( "Version" );
            assertTextPresent( "Build" );
            assertTextPresent( "Last Build Date" );
            assertElementPresent( "buildDef" );
            assertElementPresent( "build-projects" );
            assertElementPresent( "cancel-builds" );
            assertElementPresent( "delete-projects" );
        }
        else
        {
            assertTextNotPresent( "Project Name" );
        }
    }

    public void assertDefaultProjectGroupBuildDefinitionPage() 
    {
        String tableElement = "ec_table";
        assertCellValueFromTable( "Goals", tableElement, 0, 0 );
        assertCellValueFromTable( "Arguments", tableElement, 0, 1 );
        assertCellValueFromTable( "Build File", tableElement, 0, 2 );
        assertCellValueFromTable( "Schedule", tableElement, 0, 3 );
        assertCellValueFromTable( "Build Environment", tableElement, 0, 4 );
        assertCellValueFromTable( "From", tableElement, 0, 5 );
        assertCellValueFromTable( "Build Fresh", tableElement, 0, 6 );
        assertCellValueFromTable( "Default", tableElement, 0, 7 );
        assertCellValueFromTable( "Description", tableElement, 0, 8 );
        assertCellValueFromTable( "Type", tableElement, 0, 9 );
        assertCellValueFromTable( "Always Build", tableElement, 0, 10);
        assertCellValueFromTable( "", tableElement, 0, 11 );
        assertCellValueFromTable( "", tableElement, 0, 12 );
        assertCellValueFromTable( "", tableElement, 0, 13 );

        assertCellValueFromTable( "clean install", tableElement, 1, 0 );
        assertCellValueFromTable( "--batch-mode --non-recursive", tableElement, 1, 1 );
        assertCellValueFromTable( "pom.xml", tableElement, 1, 2 );
        assertCellValueFromTable( "DEFAULT_SCHEDULE", tableElement, 1, 3 );
        assertCellValueFromTable( "", tableElement, 1, 4 );
        assertCellValueFromTable( "GROUP", tableElement, 1, 5 );
        assertCellValueFromTable( "false", tableElement, 1, 6 );
        assertCellValueFromTable( "true", tableElement, 1, 7 );
        assertCellValueFromTable( "Default Maven 2 Build Definition", tableElement, 1, 8 );
        assertCellValueFromTable( "maven2", tableElement, 1, 9 );
        assertCellValueFromTable( "false", tableElement, 1, 10 );
        assertImgWithAlt( "Build" );
        assertImgWithAlt( "Edit" );
        assertImgWithAlt( "Delete" );
    }
    
    
    public void addProjectGroup( String name, String groupId, String description )
        throws Exception
    {
        goToProjectGroupsSummaryPage();

        // Go to Add Project Group Page
        clickButtonWithValue( "Add Project Group" );
        assertAddProjectGroupPage();

        // Enter values into Add Project Group fields, and submit  
        setFieldValue( "name", name );
        setFieldValue( "groupId", groupId );
        setFieldValue( "description", description );

        submit();
        waitPage();

        //TODO: Check the result Page
    }

    public void assertAddProjectGroupPage()
    {
        assertPage( "Continuum - Add Project Group" );

        assertTextPresent( "Add Project Group" );
        assertTextPresent( "Project Group Name*:" );
        assertElementPresent( "name" );
        assertTextPresent( "Project Group Id*:" );
        assertElementPresent( "groupId" );
        assertTextPresent( "Description:" );
        assertElementPresent( "description" );
        assertTextPresent( "Local Repository:" );
        assertElementPresent( "repositoryId" );
    }

    public void removeProjectGroup( String name, String groupId, String description )
        throws Exception
    {
    	showProjectGroup( name, groupId, description );

        // Remove
        clickSubmitWithLocator( "remove" );
    	//clickButtonWithValue("Delete Group");
    	
        // Assert Confirmation
        assertElementPresent( "removeProjectGroup_" );
        assertElementPresent( "Cancel" );

        // Confirm Project Group deletion
        clickSubmitWithLocator( "removeProjectGroup_" );
    }

    public void editProjectGroup( String name, String groupId, String description, String newName, String newDescription )
        throws Exception
    {
        showProjectGroup( name, groupId, description );
        clickButtonWithValue( "Edit" );
        assertEditGroupPage( groupId );
        setFieldValue( "saveProjectGroup_name", newName );
        setFieldValue( "saveProjectGroup_description", newDescription );
        clickButtonWithValue( "Save" );
    }

    public void assertEditGroupPage( String groupId ) throws Exception
    {
        assertPage( "Continuum - Update Project Group" );
        assertTextPresent( "Update Project Group" );
        assertTextPresent( "Project Group Name*:" );
        assertElementPresent( "saveProjectGroup_name" );
        assertTextPresent( "Project Group Id:" );
        assertTextPresent( groupId );
        assertTextPresent( "Description:" );
        assertElementPresent( "saveProjectGroup_description" );
        assertTextPresent( "Local Repository:" );
        assertElementPresent( "saveProjectGroup_repositoryId" );
        assertTextPresent( "Homepage Url:" );
        assertElementPresent( "saveProjectGroup_url" );

        assertElementPresent( "saveProjectGroup_" );
        assertElementPresent( "Cancel" );
    }

    public void buildProjectGroup( String projectGroupName, String groupId, String description ) throws Exception
    {
        int tries = 0, maxTries = 100;

        showProjectGroup( projectGroupName, groupId, description );
        clickButtonWithValue( "Build" );

        // make sure build will be completed
        while ( isElementPresent( "//img[@alt='Updating sources']" ) )
        {
            Thread.sleep( 5000 );
            showProjectGroup( projectGroupName, groupId, description );

            if ( tries++ >= maxTries )
            {
                assertTrue( "Max tries waiting for the project to build reached.", false );
            }
        }
        // test if successfully built, then return to the original page
        clickLinkWithText( "Apache Maven" );
        clickLinkWithText( "Builds" );
        clickLinkWithText( "Result" );
        assertTextPresent( "BUILD SUCCESSFUL" );
        clickLinkWithText( "Project Group Summary" );
    }

    public void assertReleaseSuccess()
    {
        assertTextPresent( "Choose Release Goal for Apache Maven" );
    }

    /*public void assertReleaseEmpty()
    {
        assertTextPresent( "Cannot release an empty group" );
    }*/

    public void addValidM2ProjectFromProjectGroup( String projectGroupName, String groupId, String description,
                                                   String m2PomUrl ) throws Exception
    {
        showProjectGroup( projectGroupName, groupId, description );
        selectValue( "preferredExecutor", "Add M2 Project" );
        clickButtonWithValue( "Add" );
        assertAddMavenTwoProjectPage();

        setFieldValue( "m2PomUrl", m2PomUrl );
        clickButtonWithValue( "Add" );

        // if success redirect to summary page
        assertProjectGroupsSummaryPage();
    }

    public void goToBuildDefinitionPage( String projectGroupName, String groupId, String description ) throws Exception
    {
        showProjectGroup( projectGroupName, groupId, description );
        clickLinkWithText( "Build Definitions" );
        assertTextPresent( "Project Group Build Definitions of " + projectGroupName + " group" );

        assertBuildDefinitionPage( projectGroupName );

    }

    public void assertBuildDefinitionPage( String projectGroupName )
    {

        assertTextPresent( "Project Group Build Definitions of " + projectGroupName + " group" );
        assertElementPresent( "buildDefinition_0" );
    }

    public void assertNotifierPage( String projectGroupName )
    {
        assertTextPresent( "Project Group Notifiers of group " + projectGroupName );
    }

    public void assertAddNotifierPage()
    {
        assertPage( "Continuum - Add Notifier" );
        assertTextPresent( "Add Notifier" );
        assertTextPresent( "Type" );
        assertElementPresent( "notifierType" );
        assertElementPresent( "Cancel" );
    }

    public void assertAddEditMailNotifierPage()
    {
        assertPage( "Continuum - Add/Edit Mail Notifier" );
        assertTextPresent( "Add/Edit Mail Notifier" );
        assertTextPresent( "Mail Recipient Address:" );
        assertElementPresent( "address" );
        assertTextPresent( "Send a mail to latest committers" );
        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
        assertTextPresent( "Send On SCM Failure" ); 
        assertElementPresent( "Cancel" );
    }

    public void assertAddEditIrcNotifierPage()
    {
        assertPage( "Continuum - Add/Edit IRC Notifier" );

        assertTextPresent( "IRC Host*:" );
        assertElementPresent( "host" );

        assertTextPresent( "IRC port:" );
        assertElementPresent( "port" );

        assertTextPresent( "IRC channel*:" );
        assertElementPresent( "channel" );

        assertTextPresent( "Nick Name (default value is continuum):" );
        assertElementPresent( "nick" );
        
        assertTextPresent( "Alternate Nick Name (default value is continuum_):" );
        assertElementPresent( "alternateNick" );
        
        assertTextPresent( "User Name (default value is the nick name):" );
        assertElementPresent( "username" );

        assertTextPresent( "Full Name (default value is the nick name):" );
        assertElementPresent( "fullName" );

        assertTextPresent( "Password" );
        assertElementPresent( "password" );

        assertTextPresent( "SSL" );
        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
        assertTextPresent( "Send on SCM Failure") ;
    }

    public void assertAddEditJabberPage()
    {
        assertPage( "Continuum - Add/Edit Jabber Notifier" );

        assertTextPresent( "Jabber Host*:" );
        assertElementPresent( "host" );
        assertTextPresent( "Jabber port:" );
        assertElementPresent( "port" );
        assertTextPresent( "Jabber login*:" );
        assertElementPresent( "login" );
        assertTextPresent( "Jabber Password*:" );
        assertElementPresent( "password" );
        assertTextPresent( "Jabber Domain Name:" );
        assertElementPresent( "domainName" );
        assertTextPresent( "Jabber Recipient Address*:" );
        assertElementPresent( "address" );

        assertTextPresent( "Is it a SSL connection?" );
        assertTextPresent( "Is it a Jabber group?" );
        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
        assertTextPresent( "Send on SCM Failure" );
    }

    public void assertAddEditMsnPage()
    {
        assertPage( "Continuum - Add/Edit MSN Notifier" );

        assertTextPresent( "MSN login*:" );
        assertElementPresent( "login" );
        assertTextPresent( "MSN Password*:" );
        assertElementPresent( "password" );
        assertTextPresent( "MSN Recipient Address*:" );
        assertElementPresent( "address" );

        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
        assertTextPresent( "Send on SCM Failure" );
    }

    public void assertAddEditWagonPage()
    {
        assertPage( "Continuum - Add/Edit Wagon Notifier" );

        assertTextPresent( "Project Site URL*:" );
        assertElementPresent( "url" );
        assertTextPresent( "Server Id (defined in your settings.xml for authentication)*:" );
        assertElementPresent( "id" );

        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
    }

    public void addMailNotifier( String projectGroupName, String projectGroupId, String projectGroupDescription,
                                 String email, boolean isValid ) throws Exception
    {

        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertNotifierPage( projectGroupName );

        clickButtonWithValue( "Add" );
        assertAddNotifierPage();
        selectValue( "addProjectGroupNotifier_notifierType", "Mail" );
        clickButtonWithValue( "Submit" );
        assertAddEditMailNotifierPage();
        setFieldValue( "address", email );
        clickButtonWithValue( "Save" );

        if ( isValid )
        {
            assertNotifierPage( projectGroupName );
        }
        else
        {
            assertTextPresent( "Address is invalid" );
        }
    }

    public void addIrcNotifier( String projectGroupName, String projectGroupId, String projectGroupDescription,
                                String host, String channel, boolean isValid ) throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );

        clickLinkWithText( "Notifiers" );
        assertNotifierPage( projectGroupName );

        clickButtonWithValue( "Add" );
        assertAddNotifierPage();
        selectValue( "addProjectGroupNotifier_notifierType", "IRC" );

        clickButtonWithValue( "Submit" );
        assertAddEditIrcNotifierPage();
        setFieldValue( "host", host );
        setFieldValue( "channel", channel );

        clickButtonWithValue( "Save" );
        if ( isValid )
        {
            assertNotifierPage( projectGroupName );
        }
        else
        {
            assertTextPresent( "Host is required" );
            assertTextPresent( "Channel is required" );
        }
    }

    public void addJabberNotifier( String projectGroupName, String projectGroupId, String projectGroupDescription,
                                   String host, String login, String password, String address, boolean isValid )
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );

        clickLinkWithText( "Notifiers" );
        assertNotifierPage( projectGroupName );

        clickButtonWithValue( "Add" );
        assertAddNotifierPage();
        selectValue( "addProjectGroupNotifier_notifierType", "Jabber" );
        clickButtonWithValue( "Submit" );

        assertAddEditJabberPage();
        setFieldValue( "host", host );
        setFieldValue( "login", login );
        setFieldValue( "password", password );
        setFieldValue( "address", address );
        clickButtonWithValue( "Save" );

        if ( isValid )
        {
            assertNotifierPage( projectGroupName );
        }
        else
        {
            assertTextPresent( "Host is required" );
            assertTextPresent( "Login is required" );
            assertTextPresent( "Password is required" );
            assertTextPresent( "Address is required" );
        }
    }

    public void addMsnNotifierPage( String projectGroupName, String projectGroupId, String projectGroupDescription,
                                    String login, String password, String recipientAddress, boolean isValid )
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );

        clickLinkWithText( "Notifiers" );
        assertNotifierPage( projectGroupName );

        clickButtonWithValue( "Add" );
        assertAddNotifierPage();
        selectValue( "addProjectGroupNotifier_notifierType", "MSN" );
        clickButtonWithValue( "Submit" );
        assertAddEditMsnPage();
        setFieldValue( "login", login );
        setFieldValue( "password", password );
        setFieldValue( "address", recipientAddress );
        clickButtonWithValue( "Save" );

        if ( isValid )
        {
            assertNotifierPage( projectGroupName );
        }
        else
        {
            assertTextPresent( "Login is required" );
            assertTextPresent( "Password is required" );
            assertTextPresent( "Address is required" );
        }
    }

    public void addWagonNotifierPage( String projectGroupName, String projectGroupId, String projectGroupDescription,
                                      String siteUrl, boolean isValid ) throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );

        clickLinkWithText( "Notifiers" );
        assertNotifierPage( projectGroupName );

        clickButtonWithValue( "Add" );
        assertAddNotifierPage();
        selectValue( "addProjectGroupNotifier_notifierType", "Wagon" );
        clickButtonWithValue( "Submit" );
        assertAddEditWagonPage();
        setFieldValue( "url", siteUrl );
        clickButtonWithValue( "Save" );

        if ( isValid )
        {
            assertNotifierPage( projectGroupName );
        }
        else
        {
            assertTextPresent( "Destination URL is required" );
        }
    }

    //////////////////////////////////////
    // General Project Pages
    //////////////////////////////////////
    public void goToEditProjectPage( String projectGroupName, String projectName )
    {
        clickLinkWithText( "Show Project Groups" );
        clickLinkWithText( projectGroupName );
        clickLinkWithText( projectName );
        clickButtonWithValue( "Edit" );

        assertEditProjectPage();
    }

    public void assertEditProjectPage()
    {
        assertTextPresent( "Update Continuum Project" );
        assertTextPresent( "Project Name*:" );
        assertElementPresent( "name" );
        assertTextPresent( "Version*:" );
        assertElementPresent( "version" );
        assertTextPresent( "SCM Url*:" );
        assertElementPresent( "scmUrl" );
        assertTextPresent( "Use SCM Credentials Cache, if available" );
        assertElementPresent( "scmUseCache" );
        assertTextPresent( "SCM Username:" );
        assertElementPresent( "scmUsername" );
        assertTextPresent( "SCM Password:" );
        assertElementPresent( "scmPassword" );
        assertTextPresent( "SCM Branch/Tag:" );
        assertElementPresent( "scmTag" );
    }

    public void goToAddBuildDefinitionPage( String projectGroupName, String projectName )
    {
        clickLinkWithText( "Show Project Groups" );
        clickLinkWithText( projectGroupName );
        clickLinkWithText( projectName );
        clickButtonWithValue( "Add" );

        assertAddBuildDefinitionPage();
    }

    public void assertAddBuildDefinitionPage()
    {
        assertTextPresent( "Add/Edit Build Definition" );
        assertTextPresent( "POM filename*:" );
        assertElementPresent( "buildFile" );
        assertTextPresent( "Goals:" );
        assertElementPresent( "goals" );
        assertTextPresent( "Arguments:" );
        assertElementPresent( "arguments" );
        assertTextPresent( "Build Fresh" );
        assertElementPresent( "buildFresh" );
        assertTextPresent( "Is it default?" );
        assertElementPresent( "defaultBuildDefinition" );
        assertTextPresent( "Schedule:" );
        assertElementPresent( "scheduleId" );
    }

    public void addBuildDefinition( String projectGroupName, String projectName, String buildFile, String goals,
                                    String arguments, boolean buildFresh, boolean isDefault )
    {
        goToAddBuildDefinitionPage( projectGroupName, projectName );

        // Enter values into Add Build Definition fields, and submit
        setFieldValue( "buildFile", buildFile );
        setFieldValue( "goals", goals );
        setFieldValue( "arguments", arguments );
        if ( buildFresh )
        {
            checkField( "buildFresh" );
        }
        if ( isDefault )
        {
            checkField( "defaultBuildDefinition" );
        }

        submit();

        assertProjectInformationPage();
    }

    public void goToAddNotifierPage( String projectGroupName, String projectName )
    {
        clickLinkWithText( "Show Project Groups" );
        clickLinkWithText( projectGroupName );
        clickLinkWithText( projectName );
        getSelenium().click( "addProjectNotifier" );
        clickLinkWithXPath( "//input[@id='addProjectNotifier_0']" );

        assertNotifierPage();
    }

    public void assertNotifierPage()
    {
        assertPage( "Continuum - Add Notifier" );
        assertTextPresent( "Add Notifier" );
        assertTextPresent( "Type:" );
        assertElementPresent( "notifierType" );
    }

    public void addMailNotifier( String projectGroupName, String projectName, String email, boolean success,
                                 boolean failure, boolean error, boolean warning )
    {
        goToAddNotifierPage( projectGroupName, projectName );
        clickButtonWithValue( "Submit" );

        // Enter values into Add Notifier fields, and submit
        setFieldValue( "address", email );
        if ( success )
        {
            checkField( "sendOnSuccess" );
        }
        if ( failure )
        {
            checkField( "sendOnFailure" );
        }
        if ( error )
        {
            checkField( "sendOnError" );
        }
        if ( warning )
        {
            checkField( "sendOnWarning" );
        }

        submit();
        assertProjectInformationPage();
    }

    public void goToProjectInformationPage( String projectGroupName, String projectName )
    {
        clickLinkWithText( "Show Project Groups" );
        clickLinkWithText( projectGroupName );
        clickLinkWithText( projectName );

        assertProjectInformationPage();
    }

    public void assertProjectInformationPage()
    {
        assertTextPresent( "Project Group Summary" );
        assertTextPresent( "Project Information" );
        assertTextPresent( "Builds" );
        assertTextPresent( "Working Copy" );
        assertTextPresent( "Build Definitions" );
        assertTextPresent( "Notifiers" );
        assertTextPresent( "Dependencies" );
        assertTextPresent( "Developers" );
    }

    //////////////////////////////////////
    // Maven 2.0.x Project
    //////////////////////////////////////
    public void goToAddMavenTwoProjectPage()
    {
        clickLinkWithText( "Maven 2.0.x Project" );

        assertAddMavenTwoProjectPage();
    }

    public void assertAddMavenTwoProjectPage()
    {
        assertTextPresent( "POM Url*:" );
        assertElementPresent( "m2PomUrl" );
        assertTextPresent( "Username:" );
        assertElementPresent( "scmUsername" );
        assertTextPresent( "Password:" );
        assertElementPresent( "scmPassword" );
        assertElementPresent( "scmUseCache" );
        assertTextPresent( "Upload POM:" );
        assertElementPresent( "m2PomFile" );
        assertTextPresent( "Project Group:" );
        assertElementPresent( "selectedProjectGroup" );
        assertTextPresent( "Build Definition Template:" );
        assertElementPresent( "buildDefinitionTemplateId" );
    }

    public void addMavenTwoProject( String pomUrl, String username, String password, String projectGroup, boolean validProject )
    	throws Exception
    {
        goToAddMavenTwoProjectPage();

        // Enter values into Add Maven Two Project fields, and submit  
        setFieldValue( "m2PomUrl", pomUrl );
        setFieldValue( "scmUsername", username );
        setFieldValue( "scmPassword", password );

        if ( projectGroup != null )
        {
            selectValue( "addMavenTwoProject_selectedProjectGroup", projectGroup );
        }

        submit();

        if ( validProject )
        {
            assertProjectGroupsSummaryPage();
        }
        else
        {
            assertAddMavenTwoProjectPage();
        }
    }

    //TODO: problem with input type="file", selenium.type(..) does not work,
    // TODO: refer to http://forums.openqa.org/thread.jspa?messageID=1365&#1365 for workaround
    /*
    public void addMavenTwoProject( String pomFile, String projectGroup, boolean validProject )
        throws Exception
    {
        goToAddMavenTwoProjectPage();

        // Enter values into Add Maven Two Project fields, and submit  
        setFieldValue( "m2PomFile", pomFile );

        if ( projectGroup != null )
        {
            selectValue( "addMavenTwoProject_selectedProjectGroup", projectGroup );
        }

        submit();

        if ( validProject )
        {
            assertProjectGroupsSummaryPage();
        }
        else
        {
            assertAddMavenTwoProjectPage();
        }
    }
    */

    //////////////////////////////////////
    // Maven 1.x Project
    //////////////////////////////////////
    public void goToAddMavenOneProjectPage()
    {
        clickLinkWithText( "Maven 1.x Project" );

        assertAddMavenOneProjectPage();
    }

    public void assertAddMavenOneProjectPage()
    {
        assertTextPresent( "M1 POM Url:" );
        assertElementPresent( "m1PomUrl" );
        assertTextPresent( "Username:" );
        assertElementPresent( "scmUsername" );
        assertTextPresent( "Password:" );
        assertElementPresent( "scmPassword" );
        assertElementPresent( "scmUseCache" );
        assertTextPresent( "Upload POM:" );
        assertElementPresent( "m1PomFile" );
        assertTextPresent( "Project Group:" );
        assertElementPresent( "selectedProjectGroup" );
        assertTextPresent( "Build Definition Template:" );
        assertElementPresent( "buildDefinitionTemplateId" );
    }

    public void addMavenOneProject( String pomUrl, String username, String password, String projectGroup, boolean validProject )
    	throws Exception
    {
        goToAddMavenOneProjectPage();

        // Enter values into Add Maven One Project fields, and submit  
        setFieldValue( "m1PomUrl", pomUrl );
        setFieldValue( "scmUsername", username );
        setFieldValue( "scmPassword", password );

        if ( projectGroup != null )
        {
            selectValue( "addMavenOneProject_selectedProjectGroup", projectGroup );
        }

        //submit();
        clickButtonWithValue( "Add" );

        if ( validProject )
        {
            assertProjectGroupsSummaryPage();
        }
        else
        {
            assertAddMavenOneProjectPage();
        }
    }

    //TODO: problem with input type="file", selenium.type(..) does not work,
    // TODO: refer to http://forums.openqa.org/thread.jspa?messageID=1365&#1365 for workaround
    /*
    public void addMavenOneProject( String pomFile, String projectGroup, boolean validProject )
        throws Exception
    {
        goToAddMavenOneProjectPage();

        // Enter values into Add Maven One Project fields, and submit  
        setFieldValue( "m1PomFile", pomFile );

        if ( projectGroup != null )
        {
            selectValue( "addMavenOneProject_selectedProjectGroup", projectGroup );
        }

        submit();

        if ( validProject )
        {
            assertProjectGroupsSummaryPage();
        }
        else
        {
            assertAddMavenOneProjectPage();
        }
    }
    */

    public void moveProjectToProjectGroup( String name, String groupId, String description, String newProjectGroup )
        throws Exception
    {
        showProjectGroup( name, groupId, description );

        assertElementPresent( "edit" );
        clickButtonWithValue( "Edit" );

        assertTextPresent( "Move to Group" );
        selectValue( "//select", newProjectGroup );

        assertElementPresent( "saveProjectGroup_" );
        clickButtonWithValue( "Save" );
    }

    public void tearDown()
        throws Exception
    {
        /* TODO: This causes the browser not closing after each tests. Will repair this one too.
         * 
         * login( adminUsername, adminPassword );

        goToProjectGroupsSummaryPage();

        if ( isLinkPresent( TEST_PROJ_GRP_NAME ) )
        {
            removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        }
        // TODO: clean this up
        if ( isLinkPresent( "Apache Maven" ) )
        {
            removeProjectGroup( "Apache Maven", "org.apache.maven", "Maven is a software project management and comprehension tool. Based on the concept of a project object model (POM), Maven can manage a project's build, reporting and documentation from a central piece of information." );
        }
        if ( isLinkPresent( "Maven One Project" ) )
        {
            removeProjectGroup( "Maven One Project", "maven-one-project", "This is a sample Maven One Project." );
        }
        if ( isLinkPresent( DEFAULT_PROJ_GRP_NAME ) &&
            "0".equals( getCellValueFromTable( "ec_table", 1, 2 ) ) == false )
        {
            removeProjectGroup( DEFAULT_PROJ_GRP_NAME, DEFAULT_PROJ_GRP_ID, DEFAULT_PROJ_GRP_DESCRIPTION );
            addProjectGroup( DEFAULT_PROJ_GRP_NAME, DEFAULT_PROJ_GRP_ID, DEFAULT_PROJ_GRP_DESCRIPTION );
        }*/

        super.tearDown();
    }

    protected String getWebContext()
    {
        return "/continuum";
    }
}
