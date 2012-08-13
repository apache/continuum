package org.apache.maven.continuum.management;

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

import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildDefinitionTemplateDao;
import org.apache.continuum.dao.BuildQueueDao;
import org.apache.continuum.dao.ContinuumReleaseResultDao;
import org.apache.continuum.dao.DaoUtils;
import org.apache.continuum.dao.DirectoryPurgeConfigurationDao;
import org.apache.continuum.dao.DistributedDirectoryPurgeConfigurationDao;
import org.apache.continuum.dao.InstallationDao;
import org.apache.continuum.dao.LocalRepositoryDao;
import org.apache.continuum.dao.ProfileDao;
import org.apache.continuum.dao.ProjectGroupDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.dao.RepositoryPurgeConfigurationDao;
import org.apache.continuum.dao.ScheduleDao;
import org.apache.continuum.dao.SystemConfigurationDao;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.model.release.ContinuumReleaseResult;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.utils.ProjectSorter;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.ContinuumDatabase;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.project.io.stax.ContinuumStaxReader;
import org.apache.maven.continuum.model.project.io.stax.ContinuumStaxWriter;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.jdo.ConfigurableJdoFactory;
import org.codehaus.plexus.jdo.PlexusJdoUtils;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import javax.xml.stream.XMLStreamException;

/**
 * JDO implementation the database management tool API.
 *
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.management.DataManagementTool" role-hint="continuum-jdo"
 */
public class JdoDataManagementTool
    implements DataManagementTool
{
    private Logger log = LoggerFactory.getLogger( JdoDataManagementTool.class );

    /**
     * @plexus.requirement
     */
    private DaoUtils daoUtils;

    /**
     * @plexus.requirement
     */
    private LocalRepositoryDao localRepositoryDao;

    /**
     * @plexus.requirement
     */
    private DirectoryPurgeConfigurationDao directoryPurgeConfigurationDao;

    /**
     * @plexus.requirement
     */
    private RepositoryPurgeConfigurationDao repositoryPurgeConfigurationDao;

    /**
     * @plexus.requirement
     */
    private DistributedDirectoryPurgeConfigurationDao distributedDirectoryPurgeConfigurationDao;

    /**
     * @plexus.requirement
     */
    private InstallationDao installationDao;

    /**
     * @plexus.requirement
     */
    private ProfileDao profileDao;

    /**
     * @plexus.requirement
     */
    private ProjectGroupDao projectGroupDao;

    /**
     * @plexus.requirement
     */
    private ScheduleDao scheduleDao;

    /**
     * @plexus.requirement
     */
    private SystemConfigurationDao systemConfigurationDao;

    /**
     * @plexus.requirement
     */
    private ProjectScmRootDao projectScmRootDao;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionTemplateDao buildDefinitionTemplateDao;

    /**
     * @plexus.requirement
     */
    private ContinuumReleaseResultDao releaseResultDao;

    /**
     * @plexus.requirement
     */
    private BuildQueueDao buildQueueDao;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;

    protected static final String BUILDS_XML = "builds.xml";

    /**
     * @plexus.requirement role="org.codehaus.plexus.jdo.JdoFactory" role-hint="continuum"
     */
    protected ConfigurableJdoFactory factory;

    public void backupDatabase( File backupDirectory )
        throws IOException
    {
        ContinuumDatabase database = new ContinuumDatabase();
        try
        {
            database.setSystemConfiguration( systemConfigurationDao.getSystemConfiguration() );
        }
        catch ( ContinuumStoreException e )
        {
            throw new DataManagementException( e );
        }

        // TODO: need these to lazy load to conserve memory while we stream out the model
        Collection projectGroups = projectGroupDao.getAllProjectGroupsWithTheLot();
        database.setProjectGroups( new ArrayList( projectGroups ) );
        try
        {
            database.setInstallations( installationDao.getAllInstallations() );

            database.setBuildDefinitionTemplates( buildDefinitionTemplateDao.getAllBuildDefinitionTemplate() );

            database.setBuildQueues( buildQueueDao.getAllBuildQueues() );

            database.setBuildDefinitions( buildDefinitionDao.getAllTemplates() );
        }
        catch ( ContinuumStoreException e )
        {
            throw new DataManagementException( e );
        }
        database.setSchedules( scheduleDao.getAllSchedulesByName() );
        database.setProfiles( profileDao.getAllProfilesByName() );
        database.setLocalRepositories( localRepositoryDao.getAllLocalRepositories() );
        database.setRepositoryPurgeConfigurations(
            repositoryPurgeConfigurationDao.getAllRepositoryPurgeConfigurations() );
        database.setDirectoryPurgeConfigurations( directoryPurgeConfigurationDao.getAllDirectoryPurgeConfigurations() );
        database.setDistributedDirectoryPurgeConfigurations(
            distributedDirectoryPurgeConfigurationDao.getAllDistributedDirectoryPurgeConfigurations() );
        database.setProjectScmRoots( projectScmRootDao.getAllProjectScmRoots() );
        database.setContinuumReleaseResults( releaseResultDao.getAllContinuumReleaseResults() );

        ContinuumStaxWriter writer = new ContinuumStaxWriter();

        File backupFile = new File( backupDirectory, BUILDS_XML );
        File parentFile = backupFile.getParentFile();
        parentFile.mkdirs();

        OutputStream out = new FileOutputStream( backupFile );
        Writer fileWriter = new OutputStreamWriter( out, Charset.forName( database.getModelEncoding() ) );

        try
        {
            writer.write( fileWriter, database );
        }
        catch ( XMLStreamException e )
        {
            throw new DataManagementException( "Modello failure: unable to write data to StAX writer", e );
        }
        finally
        {
            IOUtil.close( fileWriter );
        }
    }

    public void eraseDatabase()
    {
        daoUtils.eraseDatabase();
    }

    public void restoreDatabase( File backupDirectory, boolean strict )
        throws IOException
    {
        ContinuumStaxReader reader = new ContinuumStaxReader();

        FileReader fileReader = new FileReader( new File( backupDirectory, BUILDS_XML ) );

        ContinuumDatabase database;
        try
        {
            database = reader.read( fileReader, strict );
        }
        catch ( XMLStreamException e )
        {
            throw new DataManagementException( e );
        }
        finally
        {
            IOUtil.close( fileReader );
        }

        // Take control of the JDO instead of using the store, and configure a new persistence factory
        // that won't generate new object IDs.
        Properties properties = new Properties();
        properties.putAll( factory.getProperties() );
        properties.setProperty( "org.jpox.metadata.jdoFileExtension", "jdorepl" );
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory( properties );

        PlexusJdoUtils.addObject( pmf.getPersistenceManager(), database.getSystemConfiguration() );

        Map<Integer, BuildQueue> buildQueues = new HashMap<Integer, BuildQueue>();
        for ( BuildQueue buildQueue : (List<BuildQueue>) database.getBuildQueues() )
        {
            buildQueue = (BuildQueue) PlexusJdoUtils.addObject( pmf.getPersistenceManager(), buildQueue );
            buildQueues.put( buildQueue.getId(), buildQueue );
        }

        Map<Integer, Schedule> schedules = new HashMap<Integer, Schedule>();
        for ( Iterator i = database.getSchedules().iterator(); i.hasNext(); )
        {
            Schedule schedule = (Schedule) i.next();
            schedule.setBuildQueues( getBuildQueuesBySchedule( buildQueues, schedule ) );

            schedule = (Schedule) PlexusJdoUtils.addObject( pmf.getPersistenceManager(), schedule );
            schedules.put( Integer.valueOf( schedule.getId() ), schedule );
        }

        Map<Integer, Installation> installations = new HashMap<Integer, Installation>();
        for ( Iterator i = database.getInstallations().iterator(); i.hasNext(); )
        {
            Installation installation = (Installation) i.next();

            installation = (Installation) PlexusJdoUtils.addObject( pmf.getPersistenceManager(), installation );
            installations.put( Integer.valueOf( installation.getInstallationId() ), installation );
        }

        Map<Integer, Profile> profiles = new HashMap<Integer, Profile>();
        for ( Iterator i = database.getProfiles().iterator(); i.hasNext(); )
        {
            Profile profile = (Profile) i.next();

            // process installations
            if ( profile.getJdk() != null )
            {
                profile.setJdk( installations.get( profile.getJdk().getInstallationId() ) );
            }
            if ( profile.getBuilder() != null )
            {
                profile.setBuilder( installations.get( profile.getBuilder().getInstallationId() ) );
            }
            List environmentVariables = new ArrayList();
            for ( Iterator envIt = profile.getEnvironmentVariables().listIterator(); envIt.hasNext(); )
            {
                Installation installation = (Installation) envIt.next();
                environmentVariables.add( installations.get( installation.getInstallationId() ) );
                envIt.remove();
            }
            profile.setEnvironmentVariables( environmentVariables );
            profile = (Profile) PlexusJdoUtils.addObject( pmf.getPersistenceManager(), profile );
            profiles.put( Integer.valueOf( profile.getId() ), profile );
        }

        Map<Integer, BuildDefinition> buildDefinitions = new HashMap<Integer, BuildDefinition>();
        for ( BuildDefinition buildDefinition : (List<BuildDefinition>) database.getBuildDefinitions() )
        {
            if ( buildDefinition.getSchedule() != null )
            {
                buildDefinition.setSchedule( schedules.get( Integer.valueOf(
                    buildDefinition.getSchedule().getId() ) ) );
            }

            if ( buildDefinition.getProfile() != null )
            {
                buildDefinition.setProfile( profiles.get( Integer.valueOf( buildDefinition.getProfile().getId() ) ) );
            }

            buildDefinition = (BuildDefinition) PlexusJdoUtils.addObject( pmf.getPersistenceManager(),
                                                                          buildDefinition );
            buildDefinitions.put( Integer.valueOf( buildDefinition.getId() ), buildDefinition );
        }

        Map<Integer, LocalRepository> localRepositories = new HashMap<Integer, LocalRepository>();
        for ( LocalRepository localRepository : (List<LocalRepository>) database.getLocalRepositories() )
        {
            localRepository = (LocalRepository) PlexusJdoUtils.addObject( pmf.getPersistenceManager(),
                                                                          localRepository );
            localRepositories.put( Integer.valueOf( localRepository.getId() ), localRepository );
        }

        Map<Integer, ProjectGroup> projectGroups = new HashMap<Integer, ProjectGroup>();
        for ( Iterator i = database.getProjectGroups().iterator(); i.hasNext(); )
        {
            ProjectGroup projectGroup = (ProjectGroup) i.next();

            // first, we must map up any schedules, etc.
            projectGroup.setBuildDefinitions( processBuildDefinitions( projectGroup.getBuildDefinitions(), schedules,
                                                                       profiles, buildDefinitions ) );

            for ( Iterator j = projectGroup.getProjects().iterator(); j.hasNext(); )
            {
                Project project = (Project) j.next();

                project.setBuildDefinitions( processBuildDefinitions( project.getBuildDefinitions(), schedules,
                                                                      profiles, buildDefinitions ) );
            }

            if ( projectGroup.getLocalRepository() != null )
            {
                projectGroup.setLocalRepository( localRepositories.get( Integer.valueOf(
                    projectGroup.getLocalRepository().getId() ) ) );
            }

            projectGroup = (ProjectGroup) PlexusJdoUtils.addObject( pmf.getPersistenceManager(), projectGroup );
            projectGroups.put( Integer.valueOf( projectGroup.getId() ), projectGroup );
        }

        // create project scm root data (CONTINUUM-2040)
        Map<Integer, ProjectScmRoot> projectScmRoots = new HashMap<Integer, ProjectScmRoot>();
        Set<Integer> keys = projectGroups.keySet();
        int id = 1;
        for ( Integer key : keys )
        {
            ProjectGroup projectGroup = projectGroups.get( key );
            String url = " ";
            List<Project> projects = ProjectSorter.getSortedProjects( getProjectsByGroupIdWithDependencies( pmf,
                                                                                                            projectGroup.getId() ),
                                                                      log );
            for ( Iterator j = projects.iterator(); j.hasNext(); )
            {
                Project project = (Project) j.next();
                if ( !project.getScmUrl().trim().startsWith( url ) )
                {
                    url = project.getScmUrl();
                    ProjectScmRoot projectScmRoot = new ProjectScmRoot();
                    projectScmRoot.setId( id );
                    projectScmRoot.setProjectGroup( projectGroup );
                    projectScmRoot.setScmRootAddress( url );
                    projectScmRoot.setState( project.getState() );

                    projectScmRoot = (ProjectScmRoot) PlexusJdoUtils.addObject( pmf.getPersistenceManager(),
                                                                                projectScmRoot );
                    projectScmRoots.put( Integer.valueOf( projectScmRoot.getId() ), projectScmRoot );
                    id++;
                }
            }
        }

        /*
        for ( RepositoryPurgeConfiguration repoPurge : (List<RepositoryPurgeConfiguration>) database.getRepositoryPurgeConfigurations() )
        {
            repoPurge.setRepository( localRepositories.get(
                                     Integer.valueOf( repoPurge.getRepository().getId() ) ) );

            if ( repoPurge.getSchedule() != null )
            {
                repoPurge.setSchedule( schedules.get(
                                       Integer.valueOf( repoPurge.getSchedule().getId() ) ) );
            }

            repoPurge = (RepositoryPurgeConfiguration) PlexusJdoUtils.addObject( pmf.getPersistenceManager(), repoPurge );
        }*/

        for ( DirectoryPurgeConfiguration dirPurge : (List<DirectoryPurgeConfiguration>) database.getDirectoryPurgeConfigurations() )
        {
            if ( dirPurge.getSchedule() != null )
            {
                dirPurge.setSchedule( schedules.get( Integer.valueOf( dirPurge.getSchedule().getId() ) ) );
            }

            dirPurge = (DirectoryPurgeConfiguration) PlexusJdoUtils.addObject( pmf.getPersistenceManager(), dirPurge );
        }

        for ( ContinuumReleaseResult releaseResult : (List<ContinuumReleaseResult>) database.getContinuumReleaseResults() )
        {
            releaseResult.setProjectGroup( projectGroups.get( Integer.valueOf(
                releaseResult.getProjectGroup().getId() ) ) );

            ProjectGroup group = releaseResult.getProjectGroup();

            for ( Project project : (List<Project>) group.getProjects() )
            {
                if ( project.getId() == releaseResult.getProject().getId() )
                {
                    try
                    {
                        Project proj = (Project) PlexusJdoUtils.getObjectById( pmf.getPersistenceManager(),
                                                                               Project.class, project.getId(), null );
                        releaseResult.setProject( proj );
                    }
                    catch ( Exception e )
                    {
                        throw new DataManagementException( e );
                    }
                }
            }

            releaseResult = (ContinuumReleaseResult) PlexusJdoUtils.addObject( pmf.getPersistenceManager(),
                                                                               releaseResult );
        }

        for ( BuildDefinitionTemplate template : (List<BuildDefinitionTemplate>) database.getBuildDefinitionTemplates() )
        {
            template.setBuildDefinitions( processBuildDefinitions( template.getBuildDefinitions(), buildDefinitions ) );

            template = (BuildDefinitionTemplate) PlexusJdoUtils.addObject( pmf.getPersistenceManager(), template );
        }
    }

    private List<Project> getProjectsByGroupIdWithDependencies( PersistenceManagerFactory pmf, int projectGroupId )
    {
        List<Project> allProjects = PlexusJdoUtils.getAllObjectsDetached( pmf.getPersistenceManager(), Project.class,
                                                                          "name ascending", "project-dependencies" );
        List<Project> groupProjects = new ArrayList<Project>();

        for ( Project project : allProjects )
        {
            if ( project.getProjectGroup().getId() == projectGroupId )
            {
                groupProjects.add( project );
            }
        }

        return groupProjects;
    }

    private List<BuildDefinition> processBuildDefinitions( List<BuildDefinition> buildDefinitions,
                                                           Map<Integer, Schedule> schedules,
                                                           Map<Integer, Profile> profiles,
                                                           Map<Integer, BuildDefinition> buildDefs )
    {
        List<BuildDefinition> buildDefsList = new ArrayList<BuildDefinition>();

        for ( BuildDefinition def : buildDefinitions )
        {
            if ( buildDefs.get( Integer.valueOf( def.getId() ) ) != null )
            {
                buildDefsList.add( buildDefs.get( Integer.valueOf( def.getId() ) ) );
            }
            else
            {
                if ( def.getSchedule() != null )
                {
                    def.setSchedule( schedules.get( Integer.valueOf( def.getSchedule().getId() ) ) );
                }

                if ( def.getProfile() != null )
                {
                    def.setProfile( profiles.get( Integer.valueOf( def.getProfile().getId() ) ) );
                }

                buildDefsList.add( def );
            }
        }

        return buildDefsList;
    }

    private List<BuildDefinition> processBuildDefinitions( List<BuildDefinition> buildDefinitions,
                                                           Map<Integer, BuildDefinition> buildDefs )
    {
        List<BuildDefinition> buildDefsList = new ArrayList<BuildDefinition>();

        for ( BuildDefinition buildDefinition : buildDefinitions )
        {
            buildDefsList.add( buildDefs.get( Integer.valueOf( buildDefinition.getId() ) ) );
        }

        return buildDefsList;
    }

    private List<BuildQueue> getBuildQueuesBySchedule( Map<Integer, BuildQueue> allBuildQueues, Schedule schedule )
    {
        List<BuildQueue> buildQueues = new ArrayList<BuildQueue>();

        for ( BuildQueue buildQueue : (List<BuildQueue>) schedule.getBuildQueues() )
        {
            buildQueues.add( allBuildQueues.get( Integer.valueOf( buildQueue.getId() ) ) );
        }

        return buildQueues;
    }
}
