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

import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.model.project.ProjectScmRoot;
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
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.codehaus.plexus.util.StringUtils;

public class PrepareBuildProjectsTaskExecutorTest
    extends AbstractContinuumTest
{
    private ContinuumProjectBuilder projectBuilder;

    private TaskQueue prepareBuildQueue;

    private TaskQueueExecutor prepareBuildTaskQueueExecutor;

    private ActionManager actionManager;

    private ProjectScmRootDao projectScmRootDao;

    private ConfigurationService configurationService;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        projectBuilder =
            (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE, MavenTwoContinuumProjectBuilder.ID );

        prepareBuildQueue = (TaskQueue) lookup( TaskQueue.ROLE, "prepare-build-project" );

        prepareBuildTaskQueueExecutor = (TaskQueueExecutor) lookup( TaskQueueExecutor.ROLE, "prepare-build-project" );

        projectScmRootDao = (ProjectScmRootDao) lookup( ProjectScmRootDao.class.getName() );

        actionManager = (ActionManager) lookup( ActionManager.ROLE );

        configurationService =  (ConfigurationService ) lookup( "configurationService" );
    }

    public void testCheckoutPrepareBuildMultiModuleProject()
        throws Exception
    {
        PrepareBuildProjectsTask task = createTask( "src/test-projects/multi-module/pom.xml", false, false );

        this.prepareBuildQueue.put( task );

        List<Project> projects = getProjectDao().getProjectsInGroup( task.getProjectGroupId() );

        assertEquals( "failed to add all projects", 3, projects.size() );

        Project rootProject = getProjectDao().getProjectByName( "multi-module-parent" );
        Project moduleA = getProjectDao().getProjectByName( "module-A" );
        Project moduleB = getProjectDao().getProjectByName( "module-B" );

        // wait while task finishes prepare build
        while( !prepareBuildQueue.getQueueSnapshot().isEmpty() || 
                        prepareBuildTaskQueueExecutor.getCurrentTask() != null )
        {
            Thread.sleep( 10 );
        }

        ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        assertEquals( "Failed to update multi-module project", ContinuumProjectState.UPDATED, scmRoot.getState() );

        File workingDir = configurationService.getWorkingDirectory();

        assertTrue( "checkout directory of project 'multi-module-parent' does not exist.", new File( workingDir, Integer.toString( rootProject.getId() ) ).exists() );

        assertTrue( "checkout directory of project 'module-A' does not exist.", new File( workingDir, Integer.toString( moduleA.getId() ) ).exists() );

        assertTrue( "checkout directory of project 'module-B' does not exist.", new File( workingDir, Integer.toString( moduleB.getId() ) ).exists() );

        Thread.sleep( 5000 );
    }

    public void testCheckoutPrepareBuildMultiModuleProjectFreshBuild()
        throws Exception
    {
        PrepareBuildProjectsTask task = createTask( "src/test-projects/multi-module/pom.xml", false, true );

        this.prepareBuildQueue.put( task );

        List<Project> projects = getProjectDao().getProjectsInGroup( task.getProjectGroupId() );

        assertEquals( "failed to add all projects", 3, projects.size() );

        Project rootProject = getProjectDao().getProjectByName( "multi-module-parent" );
        Project moduleA = getProjectDao().getProjectByName( "module-A" );
        Project moduleB = getProjectDao().getProjectByName( "module-B" );

        // wait while task finishes prepare build
        while( !prepareBuildQueue.getQueueSnapshot().isEmpty() || 
                        prepareBuildTaskQueueExecutor.getCurrentTask() != null )
        {
            Thread.sleep( 10 );
        }

        ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        assertEquals( "Failed to update multi-module project", ContinuumProjectState.UPDATED, scmRoot.getState() );

        File workingDir = configurationService.getWorkingDirectory();

        assertTrue( "checkout directory of project 'multi-module-parent' does not exist.", new File( workingDir, Integer.toString( rootProject.getId() ) ).exists() );

        assertTrue( "checkout directory of project 'module-A' does not exist.", new File( workingDir, Integer.toString( moduleA.getId() ) ).exists() );

        assertTrue( "checkout directory of project 'module-B' does not exist.", new File( workingDir, Integer.toString( moduleB.getId() ) ).exists() );

        Thread.sleep( 5000 );
    }
/*
    public void testCheckoutPrepareBuildSingleCheckedoutMultiModuleProject()
        throws Exception
    {
        PrepareBuildProjectsTask task = createTask( "src/test-projects/multi-module/pom.xml", true, false );

        this.prepareBuildQueue.put( task );

        List<Project> projects = getProjectDao().getProjectsInGroup( task.getProjectGroupId() );

        assertEquals( "failed to add all projects", 3, projects.size() );

        Project rootProject = getProjectDao().getProjectByName( "multi-module-parent" );

        // wait while task finishes prepare build
        while( !prepareBuildQueue.getQueueSnapshot().isEmpty() || 
                        prepareBuildTaskQueueExecutor.getCurrentTask() != null )
        {
            Thread.sleep( 10 );
        }

        ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        assertEquals( "Failed to update multi-module project", ContinuumProjectState.UPDATED, scmRoot.getState() );

        File checkedOutDir = new File( configurationService.getWorkingDirectory(), Integer.toString( rootProject.getId() ) );

        assertTrue( "checkout directory of project 'multi-module-parent' does not exist.", checkedOutDir.exists() );

        assertTrue( "module-A was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-A" ).exists() );

        assertTrue( "module-B was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-B" ).exists() );
    }

    public void testCheckoutPrepareBuildSingleCheckedoutMultiModuleProjectFreshBuild()
        throws Exception
    {
        PrepareBuildProjectsTask task = createTask( "src/test-projects/multi-module/pom.xml", true, true );

        this.prepareBuildQueue.put( task );

        List<Project> projects = getProjectDao().getProjectsInGroup( task.getProjectGroupId() );

        assertEquals( "failed to add all projects", 3, projects.size() );

        Project rootProject = getProjectDao().getProjectByName( "multi-module-parent" );

        // wait while task finishes prepare build
        while( !prepareBuildQueue.getQueueSnapshot().isEmpty() || 
                        prepareBuildTaskQueueExecutor.getCurrentTask() != null )
        {
            Thread.sleep( 10 );
        }

        ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        assertEquals( "Failed to update multi-module project", ContinuumProjectState.UPDATED, scmRoot.getState() );

        File checkedOutDir = new File( configurationService.getWorkingDirectory(), Integer.toString( rootProject.getId() ) );

        assertTrue( "checkout directory of project 'multi-module-parent' does not exist.", checkedOutDir.exists() );

        assertTrue( "module-A was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-A" ).exists() );

        assertTrue( "module-B was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-B" ).exists() );
    }

    public void testCheckoutPrepareBuildSingleCheckoutFlatMultiModuleProject()
        throws Exception
    {
        PrepareBuildProjectsTask task = createTask( "src/test-projects/flat-multi-module/parent-project/pom.xml", true, false );

        this.prepareBuildQueue.put( task );

        List<Project> projects = getProjectDao().getProjectsInGroup( task.getProjectGroupId() );

        assertEquals( "failed to add all projects", 3, projects.size() );

        Project rootProject = getProjectDao().getProjectByName( "parent-project" );

        // wait while task finishes prepare build
        while( !prepareBuildQueue.getQueueSnapshot().isEmpty() || 
                        prepareBuildTaskQueueExecutor.getCurrentTask() != null )
        {
            Thread.sleep( 10 );
        }

        ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        assertEquals( "Failed to update multi-module project", ContinuumProjectState.UPDATED, scmRoot.getState() );

        File checkedOutDir = new File( configurationService.getWorkingDirectory(), Integer.toString( rootProject.getId() ) );

        assertTrue( "checkout directory of project 'parent-project' does not exist.", new File( checkedOutDir, "parent-project" ).exists() );

        assertTrue( "module-a was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-a" ).exists() );

        assertTrue( "module-b was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-b" ).exists() );
    }

    public void testCheckoutPrepareBuildSingleCheckoutFlatMultiModuleProjectBuildFresh()
        throws Exception
    {
        PrepareBuildProjectsTask task = createTask( "src/test-projects/flat-multi-module/parent-project/pom.xml", true, true );

        this.prepareBuildQueue.put( task );

        List<Project> projects = getProjectDao().getProjectsInGroup( task.getProjectGroupId() );

        assertEquals( "failed to add all projects", 3, projects.size() );

        Project rootProject = getProjectDao().getProjectByName( "parent-project" );

        ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        // wait while task finishes prepare build
        while( !prepareBuildQueue.getQueueSnapshot().isEmpty() || 
                        prepareBuildTaskQueueExecutor.getCurrentTask() != null || scmRoot.getState() == ContinuumProjectState.UPDATING )
        {
            Thread.sleep( 10 );

            scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        }

        scmRoot = projectScmRootDao.getProjectScmRoot( task.getProjectScmRootId() );
        assertEquals( "Failed to update multi-module project", ContinuumProjectState.UPDATED, scmRoot.getState() );

        File checkedOutDir = new File( configurationService.getWorkingDirectory(), Integer.toString( rootProject.getId() ) );

        assertTrue( "checkout directory of project 'parent-project' does not exist.", new File( checkedOutDir, "parent-project" ).exists() );

        assertTrue( "module-a was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-a" ).exists() );

        assertTrue( "module-b was not checked out in the same directory as it's parent.", new File( checkedOutDir, "module-b" ).exists() );
    }
*/
    
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

        assertEquals( 3, map.size() );
        PrepareBuildProjectsTask task = new PrepareBuildProjectsTask( map, 1, 
                                                                      projectGroupId, projectGroup.getName(), 
                                                                      scmRoot.getScmRootAddress(), scmRoot.getId() );

        return task;
    }

    private ProjectGroup getProjectGroup( String pomResource, boolean singleCheckout )
        throws ContinuumProjectBuilderException, IOException
    {
        File pom = getTestFile( pomResource );
    
        assertNotNull( "Can't find project " + pomResource, pom );

        //ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( pom.toURL(), null, null, true, singleCheckout );
        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( pom.toURL(), null, null, true );

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

        projectScmRoot.setState( ContinuumProjectState.ERROR );

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
}
