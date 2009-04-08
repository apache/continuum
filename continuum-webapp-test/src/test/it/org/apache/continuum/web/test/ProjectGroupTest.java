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
 * Test case for project groups.
 */
public class ProjectGroupTest
    extends AbstractAuthenticatedAdminAccessTestCase
{
    public void testAddRemoveProjectGroup()
        throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testDefaultBuildDefinition()
        throws Exception
    {
        goToProjectGroupsSummaryPage();

        showProjectGroup( DEFAULT_PROJ_GRP_NAME, DEFAULT_PROJ_GRP_ID, DEFAULT_PROJ_GRP_DESCRIPTION );

        clickLinkWithText( "Build Definitions" );
        
        assertDefaultProjectGroupBuildDefinitionPage();
    }
/*
    public void testMoveProject()
        throws Exception
    {
        // Add a project group and a project to it
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        addMavenTwoProject( TEST_POM_URL, TEST_POM_USERNAME, TEST_POM_PASSWORD, TEST_PROJ_GRP_NAME, true );
        
        goToProjectGroupsSummaryPage();
        
        // assert that the default project group has 0 projects while the test project group has 1
        assertCellValueFromTable( "0", "ec_table", 1, 8 );
        //assertCellValueFromTable( "1", "ec_table", 2, 8 );

        // move the project of the test project group to the default project group
        moveProjectToProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION,
                                   DEFAULT_PROJ_GRP_NAME );

        // assert that the default project group now has 1 while the test project group has 0
        goToProjectGroupsSummaryPage();
        assertCellValueFromTable( "1", "ec_table", 1, 8 );
        assertCellValueFromTable( "0", "ec_table", 2, 8 );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }
*/
    public void testAddBuildDefinitionWithEmptyStrings() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        clickLinkWithText( "Build Definitions" );
        clickButtonWithValue( "Add" );
        assertAddEditAvailableBuildDefPage();
        setFieldValue( "buildFile" , "" );
        clickButtonWithValue( "Save" );

        assertTextPresent( "Build file is required and cannot contain spaces only" );
        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testAddBuildDefinitionWithSpaces() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        clickLinkWithText( "Build Definitions" );
        clickButtonWithValue( "Add" );
        assertAddEditAvailableBuildDefPage();
        setFieldValue( "buildFile", " " );
        clickButtonWithValue( "Save" );

        assertTextPresent( "Build file is required and cannot contain spaces only" );
        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testCancelAddProjectGroup() throws Exception
    {
        clickLinkWithText( "Show Project Groups" );
        clickButtonWithValue( "Add Project Group" );
        clickButtonWithValue( "Cancel" );

        assertPage( "Continuum - Group Summary" );
        assertTextPresent( "Project Groups" );
        assertButtonWithValuePresent( "Add Project Group" );
    }

    public void testCancelEditProjectGroup() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        clickButtonWithValue( "Edit" );
        clickButtonWithValue( "Cancel" );

        assertProjectGroupSummaryPage( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testCancelDeleteProjectGroup() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        clickButtonWithValue( "Delete Group" );
        clickButtonWithValue( "Cancel" );

        assertProjectGroupSummaryPage( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testCancelAddBuildDefinition() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        clickLinkWithText( "Build Definitions" );
        clickButtonWithValue( "Add" );
        clickButtonWithValue( "Cancel" );

        assertTextPresent( "Project Group Build Definitions of " + TEST_PROJ_GRP_NAME );
        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

   /* TODO: Create a test with build definition that can be deleted.
    * Default Build Definition can't be deleted with the latest version.
    * 
    * public void testCancelDeleteBuildDefinition() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        clickLinkWithText( "Build Definitions" );
        clickLinkWithXPath( "//img[@alt='Delete']" );
        clickButtonWithValue( "Cancel" );

        assertTextPresent( "Project Group Build Definitions of " + TEST_PROJ_GRP_NAME );
        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }*/

    public void testCancelAddNotifier() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        clickLinkWithText( "Notifiers" );
        clickButtonWithValue( "Add" );
        clickButtonWithValue( "Cancel" );

        assertTextPresent( "Project Group Notifiers of group " + TEST_PROJ_GRP_NAME );
        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testCancelDeleteNotifier() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        clickLinkWithText( "Notifiers" );
        clickButtonWithValue( "Add" );
        clickButtonWithValue( "Submit" );
        setFieldValue( "address", "email@domain.com" );
        submit();
        clickLinkWithXPath( "//img[@alt='Delete']" );
        clickButtonWithValue( "Cancel" );

        assertTextPresent( "Project Group Notifiers of group " + TEST_PROJ_GRP_NAME );
        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testAddProjectGroupWithEmptyString() throws Exception
    {
        addProjectGroup( "", "", "" );
        assertTextPresent( "Project Group Name is required." );
        assertTextPresent( "Project Group ID is required." );
    }

    public void testAddProjectGroupWithWhitespaceString() throws Exception
    {
        addProjectGroup( " ", " ", " " );
        assertTextPresent( "Project Group Name cannot contain spaces only." );
        assertTextPresent( "Project Group ID cannot contain spaces only." );
    }

    public void testEditProjectGroupWithInvalidValues() throws Exception
    {

        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        editProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, "",
                          TEST_PROJ_GRP_DESCRIPTION + "_2" );

        assertTextPresent( "Project Group Name is required." );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testEditProjectGroupWithValidValues() throws Exception
    {
        final String sNewProjectName = TEST_PROJ_GRP_NAME + "_2";
        final String sNewProjectDescription = TEST_PROJ_GRP_DESCRIPTION + "_2";

        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        editProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, sNewProjectName,
                          sNewProjectDescription );

        assertProjectGroupSummaryPage( sNewProjectName, TEST_PROJ_GRP_ID, sNewProjectDescription );

        removeProjectGroup( sNewProjectName, TEST_PROJ_GRP_ID, sNewProjectDescription );
    }

    /* TODO Modify scripts with re: to latest working version
     * 
     * public void testProjectGroupAllBuildSuccess() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        addValidM2ProjectFromProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION,
                                           TEST_POM_URL );

        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        buildProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        clickButtonWithValue( "Release" );

        assertReleaseSuccess();

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }*/

    public void testProjectGroupNoProject() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        clickButtonWithValue( "Release" );
        // assertReleaseEmpty(); TODO file an issue for this

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    /* The sample URL for projectScmUrl does not exist. Need to have an existing sample project for ant
     * 
     * public void testAddGroupProjectAnt() throws Exception
    {
        clickLinkWithText( "Ant Project" );
        assertAddAntProjectPage();
        setFieldValue( "projectName", "Foo" );
        setFieldValue( "projectVersion", "1.0-SNAPSHOT" );
        // TODO change to invalid url
        setFieldValue( "projectScmUrl",
                       "https://svn.apache.org/repos/asf/maven/continuum/trunk/continuum-test-projects/ant/" );

        // selectValue( "projectTypes", "Add M2 Project" );
        // setselectedProjectGroup
        clickButtonWithValue( "Add" );

        assertProjectGroupsSummaryPage();
        assertTextPresent( "Default Project Group" );
        clickLinkWithText( "Default Project Group" );
        assertTextPresent( "Foo" );

    }*/

    public void testfromGroupBuildDefinition() throws Exception
    {
        final String projectGroupName2 = TEST_PROJ_GRP_NAME + "_2";
        final String projectgroupId2 = TEST_PROJ_GRP_ID + "_2";
        final String projectGroupDescription2 = TEST_PROJ_GRP_DESCRIPTION + "_2";

        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        addProjectGroup( projectGroupName2, projectgroupId2, projectGroupDescription2 );

        addMavenTwoProject( TEST_POM_URL, "", "", TEST_PROJ_GRP_NAME, true );

        addMavenTwoProject( TEST_POM_URL, "", "", projectGroupName2, true );

        goToBuildDefinitionPage( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        clickImgWithAlt( "Build" );

        goToProjectGroupsSummaryPage();

        removeProjectGroup( projectGroupName2, projectgroupId2, projectGroupDescription2 );
        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    /* TODO
     * 
     * public void testBuildFromProjectGroupSummary() throws Exception
    {
        final String projectGroupName2 = TEST_PROJ_GRP_NAME + "_2";
        final String projectgroupId2 = TEST_PROJ_GRP_ID + "_2";
        final String projectGroupDescription2 = TEST_PROJ_GRP_DESCRIPTION + "_2";

        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        addProjectGroup( projectGroupName2, projectgroupId2, projectGroupDescription2 );

        addMavenTwoProject( TEST_POM_URL, "", "", TEST_PROJ_GRP_NAME, true );

        addMavenTwoProject( TEST_POM_URL, "", "", projectGroupName2, true );

        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        clickButtonWithValue( "Build" );

        assertProjectGroupSummaryPage( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        removeProjectGroup( projectGroupName2, projectgroupId2, projectGroupDescription2 );
        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

    }*/

    /* TODO
     * 
     * public void testDeleteProjectGroupBuild() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        addMavenTwoProject( TEST_POM_URL, "", "", TEST_PROJ_GRP_NAME, true );

        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        clickImgWithAlt( "Delete" );
        assertElementPresent( "deleteProject_0" );
        assertElementPresent( "Cancel" );

        clickSubmitWithLocator( "deleteProject_0" );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }*/

    
    public void testAddValidMailNotifier() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        addMailNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, "test@test.com", true );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testAddInvalidMailNotifier() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        addMailNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, "invalid_email_add", false );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    /* TODO
     * 
     * public void testAddValidIrcNotifier() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        addIrcNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, "test.com", "test_channel",
                        true );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testAddInvalidIrcNotifier() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        addIrcNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, "", "", false );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testAddValidJabberNotifier() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        addJabberNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, "test", "test_login",
                           "hello", "test@address.com", true );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testAddInvalidJabberNotifier() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        addJabberNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, "", "", "", "", false );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testAddValidMsnNotifier() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        addMsnNotifierPage( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, "test", "hello",
                            "test@address.com", true );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testAddInvalidMsnNotifier() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        addMsnNotifierPage( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, "", "", "", false );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testAddValidWagonNotifier() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        addWagonNotifierPage( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, TEST_POM_URL, true );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testAddInvalidWagonNotifier() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        addWagonNotifierPage( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, "", false );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }*/

    /* TODO
     * 
     * 
     * public void testDeleteNotifier() throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        clickLinkWithText( "Notifiers" );
        assertNotifierPage( TEST_PROJ_GRP_NAME );

        clickButtonWithValue( "Add" );
        assertAddNotifierPage();
        selectValue( "addProjectGroupNotifier_notifierType", "Wagon" );
        clickButtonWithValue( "Submit" );
        assertAddEditWagonPage();
        setFieldValue( "url", TEST_POM_URL );
        clickButtonWithValue( "Save" );

        assertNotifierPage( TEST_PROJ_GRP_NAME );

        clickImgWithAlt( "Delete" );
        clickSubmitWithLocator( "deleteProjectGroupNotifier_0" );
        assertNotifierPage( TEST_PROJ_GRP_NAME );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }*/
}
