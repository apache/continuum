package org.apache.continuum.builder.distributed.manager;

import java.util.HashMap;
import java.util.Map;

import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.ContinuumProjectState;

public class DefaultDistributedBuildManagerTest
    extends AbstractContinuumTest
{
    DistributedBuildManager distributedBuildManager;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        distributedBuildManager = (DistributedBuildManager) lookup( DistributedBuildManager.class );
    }

    public void testUpdateScmResult()
        throws Exception
    {
        ProjectScmRootDao projectScmRootDao = (ProjectScmRootDao) lookup( ProjectScmRootDao.class.getName() );
        
        Project project = addProject( "project1" );
        project.setScmUrl( "http://sample/scmurl" );
        getProjectDao().updateProject( project );

        ProjectScmRoot scmRoot = new ProjectScmRoot();
        scmRoot.setScmRootAddress( "http://sample/scmurl" );
        scmRoot.setProjectGroup( project.getProjectGroup() );
        projectScmRootDao.addProjectScmRoot( scmRoot );

        ScmResult expectedScmResult = new ScmResult();
        expectedScmResult.setSuccess( true );

        Map context = new HashMap();
        context.put( "project-id", project.getId() );
        context.put( "scm-command-line", null );
        context.put( "scm-command-output", null );
        context.put( "scm-exception", null );
        context.put( "scm-provider-message", null );
        context.put( "scm-success", true );

        distributedBuildManager.updateScmResult( context );

        project = getProjectDao().getProjectWithScmDetails( project.getId() );

        assertNotNull( project.getScmResult() );
        assertScmResultEquals( expectedScmResult, project.getScmResult() );
    }

    public void testUpdateBuildResult()
        throws Exception
    {
        Project project = addProject( "project1" );
        project = getProjectDao().getProjectWithAllDetails( project.getId() );
        
        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setBuildFile( "pom.xml" );
        buildDef.setGoals( "mvn clean" );
        buildDef.setType( "maven2" );
        buildDef.setTemplate( false );
        
        BuildDefinitionDao buildDefinitionDao = (BuildDefinitionDao) lookup( BuildDefinitionDao.class.getName() );
        buildDef = buildDefinitionDao.addBuildDefinition( buildDef );
        
        BuildResult expectedBuildResult = new BuildResult();
        expectedBuildResult.setBuildNumber( project.getBuildNumber() + 1 );
        expectedBuildResult.setBuildDefinition( buildDef );
        expectedBuildResult.setError( null );
        expectedBuildResult.setExitCode( 0 );
        expectedBuildResult.setTrigger( ContinuumProjectState.TRIGGER_FORCED );
        expectedBuildResult.setEndTime( new Long( "3456789012345" ) );
        expectedBuildResult.setStartTime( new Long( "1234567890123" ) );
        expectedBuildResult.setState( ContinuumProjectState.OK );

        Map context = new HashMap();
        context.put( "project-id", project.getId() );
        context.put( "builddefinition-id", buildDef.getId() );
        context.put( "build-start", expectedBuildResult.getStartTime() );
        context.put( "build-end", expectedBuildResult.getEndTime() );
        context.put( "build-error", expectedBuildResult.getError() );
        context.put( "build-exit-code", expectedBuildResult.getExitCode() );
        context.put( "build-state", expectedBuildResult.getState() );
        context.put( "trigger", expectedBuildResult.getTrigger() );
        
        distributedBuildManager.updateBuildResult( context );

        project = getProjectDao().getProjectWithBuildDetails( project.getId() );

        BuildResultDao buildResultDao = (BuildResultDao) lookup( BuildResultDao.class.getName() );
        BuildResult buildResult = buildResultDao.getBuildResult( project.getLatestBuildId() );

        assertNotNull( buildResult );
        assertBuildResultEquals( expectedBuildResult, buildResult );
    }

    private void assertBuildResultEquals( BuildResult expected, BuildResult actual )
        throws Exception
    {
        assertEquals( expected.getEndTime(), actual.getEndTime() );
        assertEquals( expected.getStartTime(), actual.getStartTime() );
        assertEquals( expected.getState(), actual.getState() );
        assertEquals( expected.getBuildNumber(), actual.getBuildNumber() );
        assertEquals( expected.getError(), actual.getError() );
        assertEquals( expected.getExitCode(), actual.getExitCode() );
        assertEquals( expected.getTrigger(), actual.getTrigger() );
        assertEquals( expected.getBuildDefinition().getId(), actual.getBuildDefinition().getId() );
    }

    private void assertScmResultEquals( ScmResult expected, ScmResult actual )
        throws Exception
    {
        assertEquals( expected.getChanges(), actual.getChanges() );
        assertEquals( expected.getCommandLine(), actual.getCommandLine() );
        assertEquals( expected.getCommandOutput(), actual.getCommandOutput() );
        assertEquals( expected.getException(), actual.getException() );
        assertEquals( expected.getProviderMessage(), actual.getProviderMessage() );
    }
}
