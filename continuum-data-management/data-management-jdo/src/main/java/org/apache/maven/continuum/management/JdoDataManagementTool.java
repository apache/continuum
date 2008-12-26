package org.apache.maven.continuum.management;

import org.apache.continuum.dao.DaoUtils;
import org.apache.continuum.dao.DirectoryPurgeConfigurationDao;
import org.apache.continuum.dao.InstallationDao;
import org.apache.continuum.dao.LocalRepositoryDao;
import org.apache.continuum.dao.ProfileDao;
import org.apache.continuum.dao.ProjectGroupDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.dao.RepositoryPurgeConfigurationDao;
import org.apache.continuum.dao.ScheduleDao;
import org.apache.continuum.dao.SystemConfigurationDao;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.maven.continuum.model.project.BuildDefinition;
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

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import javax.xml.stream.XMLStreamException;
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

/**
 * JDO implementation the database management tool API.
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.management.DataManagementTool" role-hint="continuum-jdo"
 */
public class JdoDataManagementTool
    implements DataManagementTool
{
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

        database.setProjectScmRoots( projectScmRootDao.getAllProjectScmRoots() );

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

    public void restoreDatabase( File backupDirectory )
        throws IOException
    {
        ContinuumStaxReader reader = new ContinuumStaxReader();

        FileReader fileReader = new FileReader( new File( backupDirectory, BUILDS_XML ) );

        ContinuumDatabase database;
        try
        {
            database = reader.read( fileReader );
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

        Map<Integer, Schedule> schedules = new HashMap<Integer, Schedule>();
        for ( Iterator i = database.getSchedules().iterator(); i.hasNext(); )
        {
            Schedule schedule = (Schedule) i.next();

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

            profile = (Profile) PlexusJdoUtils.addObject( pmf.getPersistenceManager(), profile );
            profiles.put( Integer.valueOf( profile.getId() ), profile );
        }

        Map<Integer, LocalRepository> localRepositories = new HashMap<Integer, LocalRepository>();
        for ( LocalRepository localRepository : (List<LocalRepository>) database.getLocalRepositories() )
        {
            localRepository =
                (LocalRepository) PlexusJdoUtils.addObject( pmf.getPersistenceManager(), localRepository );
            localRepositories.put( Integer.valueOf( localRepository.getId() ), localRepository );
        }

        Map<Integer, ProjectGroup> projectGroups = new HashMap<Integer, ProjectGroup>();
        for ( Iterator i = database.getProjectGroups().iterator(); i.hasNext(); )
        {
            ProjectGroup projectGroup = (ProjectGroup) i.next();

            // first, we must map up any schedules, etc.
            processBuildDefinitions( projectGroup.getBuildDefinitions(), schedules, profiles, localRepositories );

            for ( Iterator j = projectGroup.getProjects().iterator(); j.hasNext(); )
            {
                Project project = (Project) j.next();

                processBuildDefinitions( project.getBuildDefinitions(), schedules, profiles, localRepositories );
            }
            
            if ( projectGroup.getLocalRepository() != null )
            {
                projectGroup.setLocalRepository( localRepositories.get( 
                                                 Integer.valueOf( projectGroup.getLocalRepository().getId() ) ) );
            }

            projectGroup = (ProjectGroup) PlexusJdoUtils.addObject( pmf.getPersistenceManager(), projectGroup );
            projectGroups.put( Integer.valueOf( projectGroup.getId() ), projectGroup );
        }
    }

    private static void processBuildDefinitions( List buildDefinitions, Map<Integer, Schedule> schedules,
                                                 Map<Integer, Profile> profiles,
                                                 Map<Integer, LocalRepository> localRepositories )
    {
        for ( Iterator i = buildDefinitions.iterator(); i.hasNext(); )
        {
            BuildDefinition def = (BuildDefinition) i.next();

            if ( def.getSchedule() != null )
            {
                def.setSchedule( schedules.get( Integer.valueOf( def.getSchedule().getId() ) ) );
            }

            if ( def.getProfile() != null )
            {
                def.setProfile( profiles.get( Integer.valueOf( def.getProfile().getId() ) ) );
            }
        }
    }
}
