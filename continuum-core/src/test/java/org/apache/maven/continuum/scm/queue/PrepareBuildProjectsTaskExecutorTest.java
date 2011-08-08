package org.apache.maven.continuum.scm.queue;

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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.buildmanager.ParallelBuildsManager;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilderException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.maven.MavenTwoContinuumProjectBuilder;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.util.StringUtils;

public class PrepareBuildProjectsTaskExecutorTest
    extends AbstractContinuumTest
{
    private ContinuumProjectBuilder projectBuilder;

    private ActionManager actionManager;

    private ProjectScmRootDao projectScmRootDao;

    private ConfigurationService configurationService;

    private BuildsManager buildsManager;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        projectBuilder =
            (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE, MavenTwoContinuumProjectBuilder.ID );

        projectScmRootDao = (ProjectScmRootDao) lookup( ProjectScmRootDao.class.getName() );

        actionManager = (ActionManager) lookup( ActionManager.ROLE );

        configurationService =  (ConfigurationService ) lookup( "configurationService" );

        buildsManager = (ParallelBuildsManager) lookup( BuildsManager.class, "parallel" );
    }

    public void testCheckoutPrepareBuildMultiModuleProject()
        throws Exception
    {
        PrepareBuildProjectsTask task = createTask( "src/test-projects/multi-module/pom.xml", false, false );

        List<Project> projects = getProjectDao().getProjectsInGroup( task.getProjectGroupId() );

        assertEquals( "failed to add all projects", 4, projects.size() );

        Project rootProject = getProjectDao().getProjectByName( "multi-module-parent" );
        Project moduleA = getProjectDao().getProjectByName( "module-A" );
        Project moduleB = getProjectDao().getProjectByName( "module-B" );
        Project moduleD = getProjectDao().getProjectByName( "module-D" );

        buildsManager.prepareBuildProjects( task.getProjectsBuildDefinitionsMap(), task.getBuildTrigger(), task.getProjectGroupId(),
                                            task.getProjectGroupName(), task.getScmRootAddress(), task.getProjectScmRootId() );

        // wait while task finishes prepare build
        waitForPrepareBuildToFinish( task.getProjectGroupId(), task.getProjectScmRootId() );

        ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        assertEquals( "Failed to update multi-module project", ContinuumProjectState.UPDATED, scmRoot.getState() );

        File workingDir = configurationService.getWorkingDirectory();

        assertTrue( "checkout directory of project 'multi-module-parent' does not exist.", new File( workingDir, Integer.toString( rootProject.getId() ) ).exists() );

        assertTrue( "checkout directory of project 'module-A' does not exist.", new File( workingDir, Integer.toString( moduleA.getId() ) ).exists() );

        assertTrue( "checkout directory of project 'module-B' does not exist.", new File( workingDir, Integer.toString( moduleB.getId() ) ).exists() );

        assertTrue( "checkout directory of project 'module-D' does not exist.", new File( workingDir, Integer.toString( moduleD.getId() ) ).exists() );
 
        assertTrue( "failed to checkout project 'multi-module-parent'", new File( workingDir, Integer.toString( rootProject.getId() ) ).list().length > 0 );

        assertTrue( "failed to checkout project 'module-A'", new File( workingDir, Integer.toString( moduleA.getId() ) ).list().length > 0 );

        assertTrue( "failed to checkout project 'module-B'", new File( workingDir, Integer.toString( moduleB.getId() ) ).list().length > 0 );

        assertTrue( "failed to checkout project 'module-D'", new File( workingDir, Integer.toString( moduleD.getId() ) ).list().length > 0 );

        // wait while task finished build
        waitForBuildToFinish();
    }

    public void testCheckoutPrepareBuildMultiModuleProjectFreshBuild()
        throws Exception
    {
        PrepareBuildProjectsTask task = createTask( "src/test-projects/multi-module/pom.xml", false, true );


        List<Project> projects = getProjectDao().getProjectsInGroup( task.getProjectGroupId() );

        assertEquals( "failed to add all projects", 4, projects.size() );

        Project rootProject = getProjectDao().getProjectByName( "multi-module-parent" );
        Project moduleA = getProjectDao().getProjectByName( "module-A" );
        Project moduleB = getProjectDao().getProjectByName( "module-B" );
        Project moduleD = getProjectDao().getProjectByName( "module-D" );

        buildsManager.prepareBuildProjects( task.getProjectsBuildDefinitionsMap(), task.getBuildTrigger(), task.getProjectGroupId(),
                                            task.getProjectGroupName(), task.getScmRootAddress(), task.getProjectScmRootId() );

        // wait while task finishes prepare build
        waitForPrepareBuildToFinish( task.getProjectGroupId(), task.getProjectScmRootId() );

        ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        assertEquals( "Failed to update multi-module project", ContinuumProjectState.UPDATED, scmRoot.getState() );

        File workingDir = configurationService.getWorkingDirectory();

        assertTrue( "checkout directory of project 'multi-module-parent' does not exist.", new File( workingDir, Integer.toString( rootProject.getId() ) ).exists() );

        assertTrue( "checkout directory of project 'module-A' does not exist.", new File( workingDir, Integer.toString( moduleA.getId() ) ).exists() );

        assertTrue( "checkout directory of project 'module-B' does not exist.", new File( workingDir, Integer.toString( moduleB.getId() ) ).exists() );
        
        assertTrue( "checkout directory of project 'module-D' does not exist.", new File( workingDir, Integer.toString( moduleD.getId() ) ).exists() );
 
        assertTrue( "failed to checkout project 'multi-module-parent'", new File( workingDir, Integer.toString( rootProject.getId() ) ).list().length > 0 );

        assertTrue( "failed to checkout project 'module-A'", new File( workingDir, Integer.toString( moduleA.getId() ) ).list().length > 0 );

        assertTrue( "failed to checkout project 'module-B'", new File( workingDir, Integer.toString( moduleB.getId() ) ).list().length > 0 );

        assertTrue( "failed to checkout project 'module-D'", new File( workingDir, Integer.toString( moduleD.getId() ) ).list().length > 0 );
 
        // wait while task finished build
        waitForBuildToFinish();
    }

    public void testCheckoutPrepareBuildSingleCheckedoutMultiModuleProject()
        throws Exception
    {
        PrepareBuildProjectsTask task = createTask( "src/test-projects/multi-module/pom.xml", true, false );

        List<Project> projects = getProjectDao().getProjectsInGroup( task.getProjectGroupId() );

        assertEquals( "failed to add all projects", 4, projects.size() );

        Project rootProject = getProjectDao().getProjectByName( "multi-module-parent" );

        buildsManager.prepareBuildProjects( task.getProjectsBuildDefinitionsMap(), task.getBuildTrigger(), task.getProjectGroupId(),
                                            task.getProjectGroupName(), task.getScmRootAddress(), task.getProjectScmRootId() );

        // wait while task finishes prepare build
        waitForPrepareBuildToFinish( task.getProjectGroupId(), task.getProjectScmRootId() );

        ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        assertEquals( "Failed to update multi-module project", ContinuumProjectState.UPDATED, scmRoot.getState() );

        File checkedOutDir = new File( configurationService.getWorkingDirectory(), Integer.toString( rootProject.getId() ) );

        assertTrue( "checkout directory of project 'multi-module-parent' does not exist.", checkedOutDir.exists() );

        assertTrue( "module-A was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-A" ).exists() );

        assertTrue( "module-B was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-B" ).exists() );
       
        assertTrue( "module-D was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-C/module-D" ).exists() );

        assertTrue( "failed to checkout project 'multi-module-parent'", checkedOutDir.list().length > 0 );
   
            assertTrue( "failed to checkout project 'module-A'", new File( checkedOutDir, "module-A" ).list().length > 0 );

        assertTrue( "failed to checkout project 'module-B'", new File( checkedOutDir, "module-B" ).list().length > 0 );

        assertTrue( "failed to checkout project 'module-D'", new File( checkedOutDir, "module-C/module-D" ).list().length > 0 );

        // wait while task finishes build
        waitForBuildToFinish();
    }

    public void testCheckoutPrepareBuildSingleCheckedoutMultiModuleProjectFreshBuild()
        throws Exception
    {
        PrepareBuildProjectsTask task = createTask( "src/test-projects/multi-module/pom.xml", true, true );

        List<Project> projects = getProjectDao().getProjectsInGroup( task.getProjectGroupId() );

        assertEquals( "failed to add all projects", 4, projects.size() );

        Project rootProject = getProjectDao().getProjectByName( "multi-module-parent" );

        buildsManager.prepareBuildProjects( task.getProjectsBuildDefinitionsMap(), task.getBuildTrigger(), task.getProjectGroupId(),
                                            task.getProjectGroupName(), task.getScmRootAddress(), task.getProjectScmRootId() );

        // wait while task finishes prepare build
        waitForPrepareBuildToFinish( task.getProjectGroupId(), task.getProjectScmRootId() );

        ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        assertEquals( "Failed to update multi-module project", ContinuumProjectState.UPDATED, scmRoot.getState() );

        File checkedOutDir = new File( configurationService.getWorkingDirectory(), Integer.toString( rootProject.getId() ) );

        assertTrue( "checkout directory of project 'multi-module-parent' does not exist.", checkedOutDir.exists() );

        assertTrue( "module-A was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-A" ).exists() );

        assertTrue( "module-B was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-B" ).exists() );
        
        assertTrue( "module-D was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-C/module-D" ).exists() );

        assertTrue( "failed to checkout project 'multi-module-parent'", checkedOutDir.list().length > 0 );

        assertTrue( "failed to checkout project 'module-A'", new File( checkedOutDir, "module-A" ).list().length > 0 );

        assertTrue( "failed to checkout project 'module-B'", new File( checkedOutDir, "module-B" ).list().length > 0 );

        assertTrue( "failed to checkout project 'module-D'", new File( checkedOutDir, "module-C/module-D" ).list().length > 0 );

        // wait while task finishes build
        waitForBuildToFinish();
    }

    public void testCheckoutPrepareBuildSingleCheckoutFlatMultiModuleProject()
        throws Exception
    {
        PrepareBuildProjectsTask task = createTask( "src/test-projects/flat-multi-module/parent-project/pom.xml", true, false );

        List<Project> projects = getProjectDao().getProjectsInGroup( task.getProjectGroupId() );

        assertEquals( "failed to add all projects", 4, projects.size() );
        
        Project rootProject = getProjectDao().getProjectByName( "parent-project" );

        buildsManager.prepareBuildProjects( task.getProjectsBuildDefinitionsMap(), task.getBuildTrigger(), task.getProjectGroupId(),
                                            task.getProjectGroupName(), task.getScmRootAddress(), task.getProjectScmRootId() );

        // wait while task finishes prepare build
        waitForPrepareBuildToFinish( task.getProjectGroupId(), task.getProjectScmRootId() );

        ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        assertEquals( "Failed to update multi-module project", ContinuumProjectState.UPDATED, scmRoot.getState() );

        File checkedOutDir = new File( configurationService.getWorkingDirectory(), Integer.toString( rootProject.getId() ) );

        assertTrue( "checkout directory of project 'parent-project' does not exist.", new File( checkedOutDir, "parent-project" ).exists() );

        assertTrue( "module-a was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-a" ).exists() );

        assertTrue( "module-b was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-b" ).exists() );

        assertTrue( "module-d was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-c/module-d" ).exists() );

        assertTrue( "failed to checkout parent-project", new File( checkedOutDir, "parent-project" ).list().length > 0 );

        assertTrue( "failed to checkout module-a", new File( checkedOutDir, "module-a" ).list().length > 0 );
        
        assertTrue( "failed to checkout module-b", new File( checkedOutDir, "module-b" ).list().length > 0 );
        
        assertTrue( "failed to checkout module-d", new File( checkedOutDir, "module-c/module-d" ).list().length > 0 );

        // wait while task finishes build
        waitForPrepareBuildToFinish( task.getProjectGroupId(), task.getProjectScmRootId() );
    }

    public void testCheckoutPrepareBuildSingleCheckoutFlatMultiModuleProjectBuildFresh()
        throws Exception
    {
        PrepareBuildProjectsTask task = createTask( "src/test-projects/flat-multi-module/parent-project/pom.xml", true, true );

        List<Project> projects = getProjectDao().getProjectsInGroup( task.getProjectGroupId() );

        assertEquals( "failed to add all projects", 4, projects.size() );

        Project rootProject = getProjectDao().getProjectByName( "parent-project" );

        buildsManager.prepareBuildProjects( task.getProjectsBuildDefinitionsMap(), task.getBuildTrigger(), task.getProjectGroupId(),
                                            task.getProjectGroupName(), task.getScmRootAddress(), task.getProjectScmRootId() );

        // wait while task finishes prepare build
        waitForPrepareBuildToFinish( task.getProjectGroupId(), task.getProjectScmRootId() );

        ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        assertEquals( "Failed to update multi-module project", ContinuumProjectState.UPDATED, scmRoot.getState() );

        File checkedOutDir = new File( configurationService.getWorkingDirectory(), Integer.toString( rootProject.getId() ) );

        assertTrue( "checkout directory of project 'parent-project' does not exist.", new File( checkedOutDir, "parent-project" ).exists() );

        assertTrue( "module-a was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-a" ).exists() );

        assertTrue( "module-b was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-b" ).exists() );

        assertTrue( "module-d was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-c/module-d" ).exists() );

        assertTrue( "failed to checkout parent-project", new File( checkedOutDir, "parent-project" ).list().length > 0 );

        assertTrue( "failed to checkout module-a", new File( checkedOutDir, "module-a" ).list().length > 0 );
        
        assertTrue( "failed to checkout module-b", new File( checkedOutDir, "module-b" ).list().length > 0 );
       
        assertTrue( "failed to checkout module-d", new File( checkedOutDir, "module-c/module-d" ).list().length > 0 );

        // wait while task finishes build
        waitForBuildToFinish();
    }

    public void testCheckoutPrepareBuildSingleCheckoutFlatMultiModuleProjectBuildFreshAfterRemovingWorkingCopy()
        throws Exception
    {
        PrepareBuildProjectsTask task = createTask( "src/test-projects/flat-multi-module/parent-project/pom.xml", true, true );

        List<Project> projects = getProjectDao().getProjectsInGroup( task.getProjectGroupId() );
     
        assertEquals( "failed to add all projects", 4, projects.size() );
    
        Project rootProject = getProjectDao().getProjectByName( "parent-project" );
    
        File rootProjectDir = new File( configurationService.getWorkingDirectory(), Integer.toString( rootProject.getId() ) );
        rootProjectDir = new File( rootProjectDir, "parent-project" );
    
        rootProject.setWorkingDirectory( rootProjectDir.getAbsolutePath() );
    
        getProjectDao().updateProject( rootProject );
   
        buildsManager.prepareBuildProjects( task.getProjectsBuildDefinitionsMap(), task.getBuildTrigger(), task.getProjectGroupId(),
                                            task.getProjectGroupName(), task.getScmRootAddress(), task.getProjectScmRootId() );

        // wait while task finishes prepare build
        waitForPrepareBuildToFinish( task.getProjectGroupId(), task.getProjectScmRootId() );

        ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        assertEquals( "Failed to update multi-module project", ContinuumProjectState.UPDATED, scmRoot.getState() );
    
        File checkedOutDir = new File( configurationService.getWorkingDirectory(), Integer.toString( rootProject.getId() ) );
    
        assertTrue( "checkout directory of project 'parent-project' does not exist.", new File( checkedOutDir, "parent-project" ).exists() );
    
        assertTrue( "module-a was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-a" ).exists() );
    
        assertTrue( "module-b was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-b" ).exists() );

        assertTrue( "module-d was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-c/module-d" ).exists() );

        assertTrue( "failed to checkout parent-project", new File( checkedOutDir, "parent-project" ).list().length > 0 );

        assertTrue( "failed to checkout module-a", new File( checkedOutDir, "module-a" ).list().length > 0 );
        
        assertTrue( "failed to checkout module-b", new File( checkedOutDir, "module-b" ).list().length > 0 );
        
        assertTrue( "failed to checkout module-d", new File( checkedOutDir, "module-c/module-d" ).list().length > 0 );

        // wait while task finishes build
        waitForBuildToFinish();
    }

    private PrepareBuildProjectsTask createTask( String pomResource, boolean singleCheckout, boolean buildFresh )
        throws Exception
    {
        ProjectGroup projectGroup = getProjectGroup( pomResource, singleCheckout );

        BuildDefinition buildDefinition = new BuildDefinition();
        buildDefinition.setId( 0 );
        buildDefinition.setGoals( "clean" );
        buildDefinition.setBuildFresh( buildFresh );
        
        projectGroup.addBuildDefinition( buildDefinition );

        Map<String, Object> pgContext = new HashMap<String, Object>();

        AbstractContinuumAction.setWorkingDirectory( pgContext, configurationService.getWorkingDirectory().getAbsolutePath() );

        AbstractContinuumAction.setUnvalidatedProjectGroup( pgContext, projectGroup );

        actionManager.lookup( "validate-project-group" ).execute( pgContext );

        actionManager.lookup( "store-project-group" ).execute( pgContext );

        int projectGroupId = AbstractContinuumAction.getProjectGroupId( pgContext );

        projectGroup = getProjectGroupDao().getProjectGroupWithBuildDetailsByProjectGroupId( projectGroupId );

        String scmRootUrl = getScmRootUrl( projectGroup );

        assertNotNull( scmRootUrl );

        ProjectScmRoot scmRoot = getProjectScmRoot( projectGroup, scmRootUrl );

        assertNotNull( scmRoot );

        buildDefinition = (BuildDefinition) projectGroup.getBuildDefinitions().get( 0 );

        Map<Integer, Integer> map = new HashMap<Integer, Integer>();

        Project rootProject = null;

        List<Project> projects = (List<Project>) projectGroup.getProjects();

        for ( Project project : projects )
        {
            assertFalse( project.getId() == 0 );
            
            map.put( project.getId(), buildDefinition.getId() );

            if ( rootProject == null || rootProject.getId() > project.getId() )
            {
                rootProject = project;
            }
        }

        assertEquals( 4, map.size() );
        PrepareBuildProjectsTask task = new PrepareBuildProjectsTask( map, new org.apache.continuum.utils.build.BuildTrigger( 1, "user" ),
                                                                               projectGroupId, projectGroup.getName(), 
                                                                               scmRoot.getScmRootAddress(), scmRoot.getId() );

        return task;
    }

    private ProjectGroup getProjectGroup( String pomResource, boolean singleCheckout )
        throws ContinuumProjectBuilderException, IOException
    {
        File pom = getTestFile( pomResource );
    
        assertNotNull( "Can't find project " + pomResource, pom );

        //ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( pom.toURL(), null, null, true );
        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( pom.toURL(), null, null, true, singleCheckout );

        // some assertions to make sure our expectations match. This is NOT
        // meant as a unit test for the projectbuilder!
    
        assertNotNull( "Project list not null", result.getProjects() );
    
        assertEquals( "#Projectgroups", 1, result.getProjectGroups().size() );
    
        ProjectGroup pg = result.getProjectGroups().get( 0 );
    
        if ( !result.getProjects().isEmpty() )
        {
            for ( Project p : result.getProjects() )
            {
                pg.addProject( p );
            }
        }

        return pg;
    }

    private ProjectScmRoot getProjectScmRoot ( ProjectGroup pg, String url )
        throws Exception
    {
        if ( StringUtils.isEmpty( url ) )
        {
            return null;
        }

        ProjectScmRoot scmRoot =
            projectScmRootDao.getProjectScmRootByProjectGroupAndScmRootAddress( pg.getId(), url );

        if ( scmRoot != null )
        {
            return scmRoot;
        }

        ProjectScmRoot projectScmRoot = new ProjectScmRoot();

        projectScmRoot.setProjectGroup( pg );

        projectScmRoot.setScmRootAddress( url );

        //projectScmRoot.setState( ContinuumProjectState.ERROR );

        return projectScmRootDao.addProjectScmRoot( projectScmRoot );
    }

    private String getScmRootUrl( ProjectGroup pg )
    {
        String scmRootUrl = null;

        for ( Project project : (List<Project>) pg.getProjects() )
        {
            String scmUrl = project.getScmUrl();

            scmRootUrl = getCommonPath( scmUrl, scmRootUrl );
        }

        return scmRootUrl;
    }

    private String getCommonPath( String path1, String path2 )
    {
        if ( path2 == null || path2.equals( "" ) )
        {
            return path1;
        }
        else
        {
            int indexDiff = StringUtils.differenceAt( path1, path2 );
            return path1.substring( 0, indexDiff );
        }
    }

    private void waitForPrepareBuildToFinish( int projectGroupId, int scmRootId )
        throws Exception
    {
        while( buildsManager.isInPrepareBuildQueue( projectGroupId, scmRootId ) || 
               buildsManager.isProjectGroupCurrentlyPreparingBuild( projectGroupId, scmRootId ) )
        {
            Thread.sleep( 10 );
        }
    }

    private void waitForBuildToFinish()
        throws Exception
    {
        while( buildsManager.isBuildInProgress() || isAnyProjectInBuildQueue() )
        {
            Thread.sleep( 10 );
        }
    }

    private boolean isAnyProjectInBuildQueue()
        throws Exception
    {
        Map<String, List<BuildProjectTask>> buildTasks = buildsManager.getProjectsInBuildQueues();

        for ( String queue : buildTasks.keySet() )
        {
            if ( !buildTasks.get( queue ).isEmpty() )
            {
                return true;
            }
        }

        return false;
    }
}
