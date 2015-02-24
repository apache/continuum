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

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DebugResolutionListener;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.artifact.resolver.filter.TypeArtifactFilter;
import org.apache.maven.continuum.management.util.PlexusFileSystemXmlApplicationContext;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.repository.RepositoryPermissions;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.spring.PlexusClassPathXmlApplicationContext;
import org.codehaus.plexus.spring.PlexusContainerAdapter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * An application for performing database upgrades from old Continuum and Redback versions. A suitable tool until it
 * is natively incorporated into Continuum itself.
 */
public class DataManagementCli
{
    private static final Logger LOGGER = Logger.getLogger( DataManagementCli.class );

    private static final String JAR_FILE_PREFIX = "jar:file:";

    private static final String FILE_PREFIX = "file:";

    private static final String SPRING_CONTEXT_LOC = "!/**/META-INF/spring-context.xml";

    private static final String PLEXUS_XML_LOC = "!/**/META-INF/plexus/components.xml";

    public static void main( String[] args )
        throws Exception
    {
        Commands command = new Commands();

        DatabaseFormat databaseFormat;
        OperationMode mode;
        SupportedDatabase databaseType;

        try
        {
            Args.parse( command, args );
            if ( command.help )
            {
                Args.usage( command );
                return;
            }
            if ( command.version )
            {
                System.out.print( "continuum-data-management version " + getVersion() );
                return;
            }
            databaseFormat = DatabaseFormat.valueOf( command.databaseFormat );
            mode = OperationMode.valueOf( command.mode );
            databaseType = SupportedDatabase.valueOf( command.databaseType );
        }
        catch ( IllegalArgumentException e )
        {
            System.err.println( e.getMessage() );
            Args.usage( command );
            return;
        }

        if ( command.directory.exists() && !command.directory.isDirectory() )
        {
            System.err.println( command.directory + " already exists and is not a directory." );
            Args.usage( command );
            return;
        }

        if ( !command.overwrite && mode == OperationMode.EXPORT && command.directory.exists() )
        {
            System.err.println(
                command.directory + " already exists and will not be overwritten unless the -overwrite flag is used." );
            Args.usage( command );
            return;
        }

        if ( command.buildsJdbcUrl == null && command.usersJdbcUrl == null )
        {
            System.err.println( "You must specify one of -buildsJdbcUrl and -usersJdbcUrl" );
            Args.usage( command );
            return;
        }

        if ( command.usersJdbcUrl != null && databaseFormat == DatabaseFormat.CONTINUUM_103 )
        {
            System.err.println( "The -usersJdbcUrl option can not be used with Continuum 1.0.3 databases" );
            Args.usage( command );
            return;
        }

        if ( SupportedDatabase.OTHER.equals( databaseType ) )
        {
            if ( command.driverClass == null || command.artifactId == null || command.groupId == null ||
                command.artifactVersion == null || command.password == null || command.username == null )
            {
                System.err.println(
                    "If OTHER databaseType is selected, -driverClass, -artifactId, -groupId, -artifactVersion, -username and -password must be provided together" );
                Args.usage( command );
                return;
            }
            databaseType.defaultParams = new DatabaseParams( command.driverClass, command.groupId, command.artifactId,
                                                             command.artifactVersion, command.username,
                                                             command.password );
        }

        BasicConfigurator.configure();
        if ( command.debug )
        {
            Logger.getRootLogger().setLevel( Level.DEBUG );
            Logger.getLogger( "JPOX" ).setLevel( Level.DEBUG );
        }
        else
        {
            Logger.getRootLogger().setLevel( Level.INFO );
            Logger.getLogger( "JPOX" ).setLevel( Level.WARN );
        }

        if ( command.settings != null && !command.settings.isFile() )
        {
            System.err.println( command.settings + " not exists or is not a file." );
            Args.usage( command );
            return;
        }

        if ( command.buildsJdbcUrl != null )
        {
            LOGGER.info( "Processing Continuum database..." );
            processDatabase( databaseType, databaseFormat, mode, command.buildsJdbcUrl, command.directory,
                             command.settings, databaseFormat.getContinuumToolRoleHint(), "data-management-jdo",
                             "continuum", command.strict );
        }

        if ( command.usersJdbcUrl != null )
        {
            LOGGER.info( "Processing Redback database..." );
            processDatabase( databaseType, databaseFormat, mode, command.usersJdbcUrl, command.directory,
                             command.settings, databaseFormat.getRedbackToolRoleHint(), "data-management-redback-jdo",
                             "redback", command.strict );
        }

        LOGGER.info( "Export complete. Shutting down..." );
    }

    private static void processDatabase( SupportedDatabase databaseType, DatabaseFormat databaseFormat,
                                         OperationMode mode, String jdbcUrl, File directory, File setting,
                                         String toolRoleHint, String managementArtifactId, String configRoleHint,
                                         boolean strict )
        throws PlexusContainerException, ComponentLookupException, ComponentLifecycleException,
        ArtifactNotFoundException, ArtifactResolutionException, IOException
    {
        String applicationVersion = getVersion();

        DatabaseParams params = new DatabaseParams( databaseType.defaultParams );
        params.setUrl( jdbcUrl );

        PlexusClassPathXmlApplicationContext classPathApplicationContext = null;
        PlexusFileSystemXmlApplicationContext fileSystemApplicationContext = null;
        try
        {
            classPathApplicationContext = new PlexusClassPathXmlApplicationContext(
                new String[] { "classpath*:/META-INF/spring-context.xml", "classpath*:/META-INF/plexus/components.xml",
                    "classpath*:/META-INF/plexus/plexus.xml" } );

            PlexusContainerAdapter container = new PlexusContainerAdapter();
            container.setApplicationContext( classPathApplicationContext );

            initializeWagon( container, setting );

            List<Artifact> artifacts = new ArrayList<Artifact>();
            artifacts.addAll(
                downloadArtifact( container, params.getGroupId(), params.getArtifactId(), params.getVersion(),
                                  setting ) );
            artifacts.addAll(
                downloadArtifact( container, "org.apache.continuum", managementArtifactId, applicationVersion,
                                  setting ) );
            artifacts.addAll( downloadArtifact( container, "jpox", "jpox", databaseFormat.getJpoxVersion(), setting ) );

            // Filter the list so we only use jars
            TypeArtifactFilter jarFilter = new TypeArtifactFilter( "jar" );
            for ( Iterator<Artifact> iter = artifacts.iterator(); iter.hasNext(); iter.next() )
            {
                if ( !jarFilter.include( iter.next() ) )
                {
                    iter.remove();
                }
            }

            List<String> jars = new ArrayList<String>();

            // Little hack to make it work more nicely in the IDE
            List<String> exclusions = new ArrayList<String>();
            URLClassLoader cp = (URLClassLoader) DataManagementCli.class.getClassLoader();
            List<URL> jarUrls = new ArrayList<URL>();

            for ( URL url : cp.getURLs() )
            {
                String urlEF = url.toExternalForm();
                if ( urlEF.endsWith( "target/classes/" ) )
                {
                    int idEndIdx = urlEF.length() - 16;
                    String id = urlEF.substring( urlEF.lastIndexOf( '/', idEndIdx - 1 ) + 1, idEndIdx );
                    // continuum-legacy included because the IDE doesn't do the proper assembly of enhanced classes and JDO metadata
                    if ( !"data-management-api".equals( id ) && !"data-management-cli".equals( id ) &&
                        !"continuum-legacy".equals( id ) && !"continuum-model".equals( id ) &&
                        !"redback-legacy".equals( id ) )
                    {
                        LOGGER.debug( "[IDE Help] Adding '" + id + "' as an exclusion and using one from classpath" );
                        exclusions.add( "org.apache.continuum:" + id );
                        jars.add( url.getPath() );
                        jarUrls.add( url );
                    }
                }

                // Sometimes finds its way into the IDE. Make sure it is loaded in the extra classloader too
                if ( urlEF.contains( "jpox-enhancer" ) )
                {
                    LOGGER.debug( "[IDE Help] Adding 'jpox-enhancer' as an exclusion and using one from classpath" );
                    jars.add( url.getPath() );
                    jarUrls.add( url );
                }
            }

            ArtifactFilter filter = new ExcludesArtifactFilter( exclusions );

            for ( Artifact a : artifacts )
            {
                if ( "jpox".equals( a.getGroupId() ) && "jpox".equals( a.getArtifactId() ) )
                {
                    if ( a.getVersion().equals( databaseFormat.getJpoxVersion() ) )
                    {
                        LOGGER.debug( "Adding artifact: " + a.getFile() );
                        jars.add( JAR_FILE_PREFIX + a.getFile().getAbsolutePath() + SPRING_CONTEXT_LOC );
                        jars.add( JAR_FILE_PREFIX + a.getFile().getAbsolutePath() + PLEXUS_XML_LOC );
                        jarUrls.add( new URL( FILE_PREFIX + a.getFile().getAbsolutePath() ) );
                    }
                }
                else if ( filter.include( a ) )
                {
                    LOGGER.debug( "Adding artifact: " + a.getFile() );
                    jars.add( JAR_FILE_PREFIX + a.getFile().getAbsolutePath() + SPRING_CONTEXT_LOC );
                    jars.add( JAR_FILE_PREFIX + a.getFile().getAbsolutePath() + PLEXUS_XML_LOC );
                    jarUrls.add( new URL( FILE_PREFIX + a.getFile().getAbsolutePath() ) );
                }
            }

            URLClassLoader newClassLoader =
                new URLClassLoader( (URL[]) jarUrls.toArray( new URL[jarUrls.size()] ), cp );
            Thread.currentThread().setContextClassLoader( newClassLoader );
            classPathApplicationContext.setClassLoader( newClassLoader );

            fileSystemApplicationContext = new PlexusFileSystemXmlApplicationContext(
                (String[]) jars.toArray( new String[jars.size()] ), classPathApplicationContext );
            fileSystemApplicationContext.setClassLoader( newClassLoader );
            container.setApplicationContext( fileSystemApplicationContext );

            DatabaseFactoryConfigurator configurator = (DatabaseFactoryConfigurator) container.lookup(
                DatabaseFactoryConfigurator.class.getName(), configRoleHint );
            configurator.configure( params );

            DataManagementTool manager = (DataManagementTool) container.lookup( DataManagementTool.class.getName(),
                                                                                toolRoleHint );

            if ( mode == OperationMode.EXPORT )
            {
                manager.backupDatabase( directory );
            }
            else if ( mode == OperationMode.IMPORT )
            {
                manager.eraseDatabase();
                manager.restoreDatabase( directory, strict );
            }
        }
        finally
        {
            if ( fileSystemApplicationContext != null )
                fileSystemApplicationContext.close();
            if ( classPathApplicationContext != null )
                classPathApplicationContext.close();
        }
    }

    private static void initializeWagon( PlexusContainerAdapter container, File setting )
        throws ComponentLookupException, ComponentLifecycleException, IOException
    {
        WagonManager wagonManager = (WagonManager) container.lookup( WagonManager.ROLE );

        Settings settings = getSettings( container, setting );

        try
        {
            Proxy proxy = settings.getActiveProxy();

            if ( proxy != null )
            {
                if ( proxy.getHost() == null )
                {
                    throw new IOException( "Proxy in settings.xml has no host" );
                }

                wagonManager.addProxy( proxy.getProtocol(), proxy.getHost(), proxy.getPort(), proxy.getUsername(),
                                       proxy.getPassword(), proxy.getNonProxyHosts() );
            }

            for ( Iterator i = settings.getServers().iterator(); i.hasNext(); )
            {
                Server server = (Server) i.next();

                wagonManager.addAuthenticationInfo( server.getId(), server.getUsername(), server.getPassword(),
                                                    server.getPrivateKey(), server.getPassphrase() );

                wagonManager.addPermissionInfo( server.getId(), server.getFilePermissions(),
                                                server.getDirectoryPermissions() );

                if ( server.getConfiguration() != null )
                {
                    wagonManager.addConfiguration( server.getId(), (Xpp3Dom) server.getConfiguration() );
                }
            }

            RepositoryPermissions defaultPermissions = new RepositoryPermissions();

            defaultPermissions.setDirectoryMode( "775" );

            defaultPermissions.setFileMode( "664" );

            wagonManager.setDefaultRepositoryPermissions( defaultPermissions );

            for ( Iterator i = settings.getMirrors().iterator(); i.hasNext(); )
            {
                Mirror mirror = (Mirror) i.next();

                wagonManager.addMirror( mirror.getId(), mirror.getMirrorOf(), mirror.getUrl() );
            }
        }
        finally
        {
            container.release( wagonManager );
        }

    }

    private static Collection<Artifact> downloadArtifact( PlexusContainer container, String groupId, String artifactId,
                                                          String version, File setting )
        throws ComponentLookupException, ArtifactNotFoundException, ArtifactResolutionException, IOException
    {
        ArtifactRepositoryFactory factory = (ArtifactRepositoryFactory) container.lookup(
            ArtifactRepositoryFactory.ROLE );

        DefaultRepositoryLayout layout = (DefaultRepositoryLayout) container.lookup( ArtifactRepositoryLayout.ROLE,
                                                                                     "default" );

        ArtifactRepository localRepository = factory.createArtifactRepository( "local", getLocalRepositoryURL(
            container, setting ), layout, null, null );

        List<ArtifactRepository> remoteRepositories = new ArrayList<ArtifactRepository>();
        remoteRepositories.add( factory.createArtifactRepository( "central", "http://repo1.maven.org/maven2", layout,
                                                                  null, null ) );
        //Load extra repositories from active profile

        Settings settings = getSettings( container, setting );
        List<String> profileIds = settings.getActiveProfiles();
        Map<String, Profile> profilesAsMap = settings.getProfilesAsMap();
        if ( profileIds != null && !profileIds.isEmpty() )
        {
            for ( String profileId : profileIds )
            {
                Profile profile = profilesAsMap.get( profileId );
                if ( profile != null )
                {
                    List<Repository> repos = profile.getRepositories();
                    if ( repos != null && !repos.isEmpty() )
                    {
                        for ( Repository repo : repos )
                        {
                            remoteRepositories.add( factory.createArtifactRepository( repo.getId(), repo.getUrl(),
                                                                                      layout, null, null ) );
                        }
                    }
                }
            }
        }

        ArtifactFactory artifactFactory = (ArtifactFactory) container.lookup( ArtifactFactory.ROLE );
        Artifact artifact = artifactFactory.createArtifact( groupId, artifactId, version, Artifact.SCOPE_RUNTIME,
                                                            "jar" );
        Artifact dummyArtifact = artifactFactory.createProjectArtifact( "dummy", "dummy", "1.0" );

        if ( artifact.isSnapshot() )
        {
            remoteRepositories.add( factory.createArtifactRepository( "apache.snapshots",
                                                                      "http://people.apache.org/repo/m2-snapshot-repository",
                                                                      layout, null, null ) );
        }

        ArtifactResolver resolver = (ArtifactResolver) container.lookup( ArtifactResolver.ROLE );

        List<String> exclusions = new ArrayList<String>();
        exclusions.add( "org.apache.continuum:data-management-api" );
        exclusions.add( "org.codehaus.plexus:plexus-component-api" );
        exclusions.add( "org.codehaus.plexus:plexus-container-default" );
        exclusions.add( "org.slf4j:slf4j-api" );
        exclusions.add( "log4j:log4j" );

        ArtifactFilter filter = new ExcludesArtifactFilter( exclusions );

        List<? extends ResolutionListener> listeners;
        if ( LOGGER.isDebugEnabled() )
        {
            listeners = Collections.singletonList( new DebugResolutionListener( container.getLogger() ) );
        }
        else
        {
            listeners = Collections.emptyList();
        }

        ArtifactMetadataSource source = (ArtifactMetadataSource) container.lookup( ArtifactMetadataSource.ROLE,
                                                                                   "maven" );
        ArtifactResolutionResult result = resolver.resolveTransitively( Collections.singleton( artifact ),
                                                                        dummyArtifact, Collections.emptyMap(),
                                                                        localRepository, remoteRepositories, source,
                                                                        filter, listeners );

        return result.getArtifacts();
    }

    private static String getLocalRepositoryURL( PlexusContainer container, File setting )
        throws ComponentLookupException, IOException
    {
        String repositoryPath;
        File settingsFile = new File( System.getProperty( "user.home" ), ".m2/settings.xml" );
        if ( setting != null )
        {
            Settings settings = getSettings( container, setting );
            repositoryPath = new File( settings.getLocalRepository() ).toURL().toString();
        }
        else if ( !settingsFile.exists() )
        {
            repositoryPath = new File( System.getProperty( "user.home" ), ".m2/repository" ).toURL().toString();
        }
        else
        {
            Settings settings = getSettings( container, null );
            repositoryPath = new File( settings.getLocalRepository() ).toURL().toString();
        }
        return repositoryPath;
    }

    private static Settings getSettings( PlexusContainer container, File setting )
        throws ComponentLookupException, IOException
    {
        MavenSettingsBuilder mavenSettingsBuilder = (MavenSettingsBuilder) container.lookup(
            MavenSettingsBuilder.class.getName() );
        try
        {
            if ( setting != null )
            {
                return mavenSettingsBuilder.buildSettings( setting, false );
            }
            else
            {
                return mavenSettingsBuilder.buildSettings( false );
            }
        }
        catch ( XmlPullParserException e )
        {
            e.printStackTrace();
            throw new IOException( "Can't read settings.xml. " + e.getMessage() );
        }
    }

    private static String getVersion()
        throws IOException
    {
        Properties properties = new Properties();
        properties.load( DataManagementCli.class.getResourceAsStream(
            "/META-INF/maven/org.apache.continuum/data-management-api/pom.properties" ) );
        return properties.getProperty( "version" );
    }

    private static class Commands
    {

        @Argument( description = "Display help information", value = "help", alias = "h" )
        private boolean help;

        @Argument( description = "Display version information", value = "version", alias = "v" )
        private boolean version;

        @Argument(
            description = "The JDBC URL for the Continuum database that contains the data to convert, or to import the data into",
            value = "buildsJdbcUrl" )
        private String buildsJdbcUrl;

        @Argument(
            description = "The JDBC URL for the Redback database that contains the data to convert, or to import the data into",
            value = "usersJdbcUrl" )
        private String usersJdbcUrl;

        // TODO: ability to use the enum directly would be nice
        @Argument(
            description = "Format of the database. Valid values are CONTINUUM_103, CONTINUUM_109, CONTINUUM_11. Default is CONTINUUM_11." )
        private String databaseFormat = DatabaseFormat.CONTINUUM_11.toString();

/* TODO: not yet supported
        @Argument(
            description = "Format of the backup directory. Valid values are CONTINUUM_103, CONTINUUM_109, CONTINUUM_11. Default is CONTINUUM_11.")
        private String dataFileFormat = DatabaseFormat.CONTINUUM_11.toString();
*/

        @Argument(
            description = "The directory to export the data to, or import the data from. Default is 'backups' in the current working directory.",
            value = "directory" )
        private File directory = new File( "backups" );

        @Argument(
            description = "Mode of operation. Valid values are IMPORT and EXPORT. Default is EXPORT.",
            value = "mode" )
        private String mode = OperationMode.EXPORT.toString();

        @Argument(
            description = "Whether to overwrite the designated directory if it already exists in export mode. Default is false.",
            value = "overwrite" )
        private boolean overwrite;

        @Argument(
            description = "The type of database to use. Currently supported values are DERBY_10_1. The default value is DERBY_10_1.",
            value = "databaseType" )
        private String databaseType = SupportedDatabase.DERBY_10_1.toString();

        @Argument( description = "JDBC driver class", value = "driverClass", required = false )
        private String driverClass;

        @Argument( description = "JDBC driver groupId", value = "groupId", required = false )
        private String groupId;

        @Argument( description = "JDBC driver artifactId", value = "artifactId", required = false )
        private String artifactId;

        @Argument( description = "Artifact version of the JDBC driver class",
                   value = "artifactVersion",
                   required = false )
        private String artifactVersion;

        @Argument( description = "Username", value = "username", required = false )
        private String username;

        @Argument( description = "Password", value = "password", required = false )
        private String password;

        @Argument(
            description = "Turn on debugging information. Default is off.",
            value = "debug" )
        private boolean debug;

        @Argument( description = "Alternate path for the user settings file", value = "settings", required = false,
                   alias = "s" )
        private File settings;

        @Argument( description = "Run on strict mode. Default is false.", value = "strict" )
        private boolean strict;
    }

    private enum OperationMode
    {
        IMPORT, EXPORT
    }

    private enum SupportedDatabase
    {
        DERBY_10_1( new DatabaseParams( "org.apache.derby.jdbc.EmbeddedDriver", "org.apache.derby", "derby", "10.1.3.1",
                                        "sa", "" ) ),

        OTHER( new DatabaseParams( null, null, null, null, null, null ) );

        private DatabaseParams defaultParams;

        SupportedDatabase( DatabaseParams defaultParams )
        {
            this.defaultParams = defaultParams;
        }
    }
}
