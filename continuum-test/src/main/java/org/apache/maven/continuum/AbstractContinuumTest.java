package org.apache.maven.continuum;

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

import org.apache.continuum.dao.DaoUtils;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectGroupDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.dao.ScheduleDao;
import org.apache.continuum.utils.file.FileSystemManager;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.initialization.ContinuumInitializer;
import org.apache.maven.continuum.jdo.MemoryJdoFactory;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.jdo.JdoFactory;
import org.jpox.SchemaTool;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public abstract class AbstractContinuumTest
    extends PlexusSpringTestCase
{
    private DaoUtils daoUtils;

    private ProjectDao projectDao;

    private ProjectGroupDao projectGroupDao;

    private ScheduleDao scheduleDao;

    private ProjectScmRootDao projectScmRootDao;

    private FileSystemManager fsManager;

    @Rule
    public TestName testName = new TestName();

    protected String getName()
    {
        return testName.getMethodName();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    @Before
    public void setupContinuum()
        throws Exception
    {
        init();
        getProjectDao();
        getProjectGroupDao();
        getScheduleDao();
        getProjectScmRootDao();
        getFileSystemManager();

        setUpConfigurationService( (ConfigurationService) lookup( "configurationService" ) );

        Collection<ProjectGroup> projectGroups = projectGroupDao.getAllProjectGroupsWithProjects();
        if ( projectGroups.size() == 0 ) //if ContinuumInitializer is loaded by Spring at startup, size == 1
        {
            createDefaultProjectGroup();
            projectGroups = projectGroupDao.getAllProjectGroupsWithProjects();
        }

        assertEquals( 1, projectGroups.size() );
    }

    @After
    public void wipeData()
        throws Exception
    {
        daoUtils.eraseDatabase();
    }

    protected void createDefaultProjectGroup()
        throws Exception
    {
        try
        {
            getDefaultProjectGroup();
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            ProjectGroup group;

            group = new ProjectGroup();

            group.setName( "Default Project Group" );

            group.setGroupId( ContinuumInitializer.DEFAULT_PROJECT_GROUP_GROUP_ID );

            group.setDescription( "Contains all projects that do not have a group of their own" );

            projectGroupDao.addProjectGroup( group );
        }
    }

    public static void setUpConfigurationService( ConfigurationService configurationService )
        throws Exception
    {
        configurationService.setBuildOutputDirectory( getTestFile( "target/build-output" ) );

        configurationService.setWorkingDirectory( getTestFile( "target/working-directory" ) );

        configurationService.setReleaseOutputDirectory( getTestFile( "target/release-output" ) );

        configurationService.store();
    }

    protected ProjectGroup getDefaultProjectGroup()
        throws ContinuumStoreException
    {
        return projectGroupDao.getProjectGroupByGroupIdWithProjects(
            ContinuumInitializer.DEFAULT_PROJECT_GROUP_GROUP_ID );
    }

    // ----------------------------------------------------------------------
    // Store
    // ----------------------------------------------------------------------

    private void init()
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Set up the JDO factory
        // ----------------------------------------------------------------------

        MemoryJdoFactory jdoFactory = (MemoryJdoFactory) lookup( JdoFactory.class, "continuum" );

        assertEquals( MemoryJdoFactory.class.getName(), jdoFactory.getClass().getName() );

        String url = "jdbc:hsqldb:mem:" + getClass().getName() + "." + getName();

        jdoFactory.setUrl( url );

        jdoFactory.reconfigure();

        // ----------------------------------------------------------------------
        // Check the configuration
        // ----------------------------------------------------------------------

        PersistenceManagerFactory pmf = jdoFactory.getPersistenceManagerFactory();

        assertNotNull( pmf );

        assertEquals( url, pmf.getConnectionURL() );

        PersistenceManager pm = pmf.getPersistenceManager();

        pm.close();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        Properties properties = jdoFactory.getProperties();

        for ( Map.Entry entry : properties.entrySet() )
        {
            System.setProperty( (String) entry.getKey(), (String) entry.getValue() );
        }

        SchemaTool.createSchemaTables( new URL[] { getClass().getResource( "/package.jdo" ) }, new URL[] {}, null,
                                       false,
                                       null );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        daoUtils = lookup( DaoUtils.class );
    }

    protected ProjectDao getProjectDao()
    {
        if ( projectDao == null )
        {
            projectDao = (ProjectDao) lookup( ProjectDao.class.getName() );
        }
        return projectDao;
    }

    protected ProjectGroupDao getProjectGroupDao()
    {
        if ( projectGroupDao == null )
        {
            projectGroupDao = (ProjectGroupDao) lookup( ProjectGroupDao.class.getName() );
        }
        return projectGroupDao;
    }

    protected ScheduleDao getScheduleDao()
    {
        if ( scheduleDao == null )
        {
            scheduleDao = (ScheduleDao) lookup( ScheduleDao.class.getName() );
        }
        return scheduleDao;
    }

    protected ProjectScmRootDao getProjectScmRootDao()
    {
        if ( projectScmRootDao == null )
        {
            projectScmRootDao = (ProjectScmRootDao) lookup( ProjectScmRootDao.class.getName() );
        }
        return projectScmRootDao;
    }

    public FileSystemManager getFileSystemManager()
    {
        if ( fsManager == null )
        {
            fsManager = (FileSystemManager) lookup( FileSystemManager.class );
        }
        return fsManager;
    }

    // ----------------------------------------------------------------------
    // Build Executor
    // ----------------------------------------------------------------------

    protected ContinuumBuildExecutor getBuildExecutor( String id )
        throws Exception
    {
        ContinuumBuildExecutor buildExecutor = (ContinuumBuildExecutor) lookup( ContinuumBuildExecutor.ROLE, id );

        assertNotNull( "Could not look up build executor '" + id + "'", buildExecutor );

        return buildExecutor;
    }

    // ----------------------------------------------------------------------
    // Maven 2 Project Generators
    // ----------------------------------------------------------------------

    public static Project makeStubProject( String name )
    {
        return makeProject( name, "foo@bar.com", "1.0" );
    }

    public static Project makeProject( String name, String emailAddress, String version )
    {
        Project project = new Project();

        makeProject( project, name, version );

        List<ProjectNotifier> notifiers = createMailNotifierList( emailAddress );

        project.setNotifiers( notifiers );

        return project;
    }

    // ----------------------------------------------------------------------
    // Shell Project Generators
    // ----------------------------------------------------------------------

    public static Project makeStubShellProject( String name, String script )
    {
        Project project = new Project();

        makeProject( project, name, "1.0" );
        project.setExecutorId( ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR );

        BuildDefinition def = new BuildDefinition();
        def.setBuildFile( script );
        project.addBuildDefinition( def );

        return project;
    }

    public static Project makeProject( Project project, String name, String version )
    {
        project.setExecutorId( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );
        project.setName( name );
        project.setVersion( version );

        return project;
    }

    protected static List<ProjectNotifier> createMailNotifierList( String emailAddress )
    {
        if ( emailAddress == null )
        {
            return null;
        }

        ProjectNotifier notifier = new ProjectNotifier();

        notifier.setType( "mail" );

        Properties props = new Properties();

        props.put( "address", emailAddress );

        notifier.setConfiguration( props );

        List<ProjectNotifier> notifiers = new ArrayList<ProjectNotifier>();

        notifiers.add( notifier );

        return notifiers;
    }

    // ----------------------------------------------------------------------
    // Public utility methods
    // ----------------------------------------------------------------------

    public Project addProject( Project project )
        throws Exception
    {
        ProjectGroup defaultProjectGroup = getDefaultProjectGroup();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ScmResult scmResult = new ScmResult();

        scmResult.setSuccess( true );

        scmResult.setCommandOutput( "commandOutput" );

        scmResult.setProviderMessage( "providerMessage" );

        project.setCheckoutResult( scmResult );

        defaultProjectGroup.addProject( project );

        projectGroupDao.updateProjectGroup( defaultProjectGroup );

        project = projectDao.getProject( project.getId() );

        assertNotNull( "project group == null", project.getProjectGroup() );

        return project;
    }

    public Project addProject( String name )
        throws Exception
    {
        return addProject( makeStubProject( name ) );
    }

    // ----------------------------------------------------------------------
    // Assertions
    // ----------------------------------------------------------------------

    public void assertProjectEquals( Project expected, Project actual )
    {
        assertProjectEquals( expected.getName(), expected.getNotifiers(), expected.getVersion(), actual );
    }

    public void assertProjectEquals( String name, String emailAddress, String version, Project actual )
    {
        assertProjectEquals( name, createMailNotifierList( emailAddress ), version, actual );
    }

    public void assertProjectEquals( String name, List<ProjectNotifier> notifiers, String version, Project actual )
    {
        assertEquals( "project.name", name, actual.getName() );

        if ( notifiers != null )
        {
            assertNotNull( "project.notifiers", actual.getNotifiers() );

            assertEquals( "project.notifiers.size", notifiers.size(), actual.getNotifiers().size() );

            for ( int i = 0; i < notifiers.size(); i++ )
            {
                ProjectNotifier notifier = notifiers.get( i );

                ProjectNotifier actualNotifier = (ProjectNotifier) actual.getNotifiers().get( i );

                assertEquals( "project.notifiers.notifier.type", notifier.getType(), actualNotifier.getType() );

                assertEquals( "project.notifiers.notifier.configuration.address", notifier.getConfiguration().get(
                    "address" ), actualNotifier.getConfiguration().get( "address" ) );
            }
        }

        assertEquals( "project.version", version, actual.getVersion() );
    }

    // ----------------------------------------------------------------------
    // Simple utils
    // ----------------------------------------------------------------------

    public ProjectGroup createStubProjectGroup( String name, String description )
    {
        ProjectGroup projectGroup = new ProjectGroup();

        projectGroup.setName( name );

        projectGroup.setGroupId( name );

        projectGroup.setDescription( description );

        return projectGroup;
    }

    public Project addProject( String name, ProjectGroup group )
        throws Exception
    {
        Project project = makeStubProject( name );

        project.setGroupId( group.getGroupId() );

        group.addProject( project );

        try
        {
            projectGroupDao.getProjectGroup( group.getId() );

            projectGroupDao.updateProjectGroup( group );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            projectGroupDao.addProjectGroup( group );
        }

        return projectDao.getProject( project.getId() );
    }
}
