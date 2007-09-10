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
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * An application for performing database upgrades from old Continuum and Redback versions. A suitable tool until it
 * is natively incorporated into Continuum itself.
 */
public class DataManagementCli
{
    private static final Logger LOGGER = Logger.getLogger( DataManagementCli.class );

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
                System.exit( 0 );
            }
            if (command.version)
            {
                System.out.print("continuum-data-management version " + getVersion() );
                System.exit( 0 );
            }
            databaseFormat = DatabaseFormat.valueOf( command.databaseFormat );
            mode = OperationMode.valueOf( command.mode );
            databaseType = SupportedDatabase.valueOf( command.databaseType );
        }
        catch ( IllegalArgumentException e )
        {
            Args.usage( command );

            System.err.println( e.getMessage() );
            return;
        }

        if ( command.directory.exists() && !command.directory.isDirectory() )
        {
            System.err.println( command.directory + " already exists and is not a directory." );
            return;
        }

        if ( !command.overwrite && mode == OperationMode.EXPORT && command.directory.exists() )
        {
            System.err.println(
                command.directory + " already exists and will not be overwritten unless the -overwrite flag is used." );
            return;
        }

        if ( command.buildsJdbcUrl == null && command.usersJdbcUrl == null )
        {
            System.err.println( "You must specify one of -buildsJdbcUrl and -usersJdbcUrl" );
            return;
        }

        if ( command.usersJdbcUrl != null && databaseFormat == DatabaseFormat.CONTINUUM_103 )
        {
            System.err.println( "The -usersJdbcUrl option can not be used with Continuum 1.0.3 databases" );
            return;
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
        }

        if ( command.buildsJdbcUrl != null )
        {
            LOGGER.info( "Processing Continuum database..." );
            processDatabase( databaseType, databaseFormat, mode, command.buildsJdbcUrl, command.directory,
                             databaseFormat.getContinuumToolRoleHint(), "data-management-jdo", "continuum" );
        }

        if ( command.usersJdbcUrl != null )
        {
            LOGGER.info( "Processing Redback database..." );
            processDatabase( databaseType, databaseFormat, mode, command.usersJdbcUrl, command.directory,
                             databaseFormat.getRedbackToolRoleHint(), "data-management-redback-jdo", "redback" );
        }
    }

    private static void processDatabase( SupportedDatabase databaseType, DatabaseFormat databaseFormat,
                                         OperationMode mode, String jdbcUrl, File directory, String toolRoleHint,
                                         String managementArtifactId, String configRoleHint )
        throws PlexusContainerException, ComponentLookupException, ArtifactNotFoundException,
        ArtifactResolutionException, IOException
    {
        String applicationVersion = getVersion();

        DatabaseParams params = new DatabaseParams( databaseType.defaultParams );
        params.setUrl( jdbcUrl );

        DefaultPlexusContainer container = new DefaultPlexusContainer();
        List<Artifact> artifacts = new ArrayList<Artifact>();
        artifacts.addAll(
            downloadArtifact( container, params.getGroupId(), params.getArtifactId(), params.getVersion() ) );
        artifacts.addAll(
            downloadArtifact( container, "org.apache.maven.continuum", managementArtifactId, applicationVersion ) );
        artifacts.addAll( downloadArtifact( container, "jpox", "jpox", databaseFormat.getJpoxVersion() ) );

        List<File> jars = new ArrayList<File>();

        // Little hack to make it work more nicely in the IDE
        List<String> exclusions = new ArrayList<String>();
        URLClassLoader cp = (URLClassLoader) DataManagementCli.class.getClassLoader();
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
                    exclusions.add( "org.apache.maven.continuum:" + id );
                    jars.add( new File( url.getPath() ) );
                }
            }

            // Sometimes finds its way into the IDE. Make sure it is loaded in the extra classloader too
            if ( urlEF.contains( "jpox-enhancer" ) )
            {
                jars.add( new File( url.getPath() ) );
            }
        }
        ArtifactFilter filter = new ExcludesArtifactFilter( exclusions );

        for ( Artifact a : artifacts )
        {
            if ( "jpox".equals( a.getGroupId() ) && "jpox".equals( a.getArtifactId() ) )
            {
                if ( a.getVersion().equals( databaseFormat.getJpoxVersion() ) )
                {
                    jars.add( a.getFile() );
                }
            }
            else if ( filter.include( a ) )
            {
                jars.add( a.getFile() );
            }
        }

        ClassRealm realm = container.createComponentRealm( "app", jars );

        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( realm );

        ClassRealm oldRealm = container.setLookupRealm( realm );

        DatabaseFactoryConfigurator configurator = (DatabaseFactoryConfigurator) container.lookup(
            DatabaseFactoryConfigurator.class.getName(), configRoleHint, realm );
        configurator.configure( params );

        DataManagementTool manager =
            (DataManagementTool) container.lookup( DataManagementTool.class.getName(), toolRoleHint, realm );

        if ( mode == OperationMode.EXPORT )
        {
            manager.backupDatabase( directory );
        }
        else if ( mode == OperationMode.IMPORT )
        {
            manager.eraseDatabase();
            manager.restoreDatabase( directory );
        }

        container.setLookupRealm( oldRealm );
        Thread.currentThread().setContextClassLoader( oldLoader );
    }

    private static Collection<Artifact> downloadArtifact( PlexusContainer container, String groupId, String artifactId,
                                                          String version )
        throws ComponentLookupException, ArtifactNotFoundException, ArtifactResolutionException, IOException
    {
        ArtifactRepositoryFactory factory =
            (ArtifactRepositoryFactory) container.lookup( ArtifactRepositoryFactory.ROLE );

        DefaultRepositoryLayout layout =
            (DefaultRepositoryLayout) container.lookup( ArtifactRepositoryLayout.ROLE, "default" );

        ArtifactRepository localRepository =
            factory.createArtifactRepository( "local", getLocalRepositoryURL( container ), layout, null, null );

        List<ArtifactRepository> remoteRepositories = new ArrayList<ArtifactRepository>();
        remoteRepositories.add(
            factory.createArtifactRepository( "central", "http://repo1.maven.org/maven2", layout, null, null ) );

        ArtifactFactory artifactFactory = (ArtifactFactory) container.lookup( ArtifactFactory.ROLE );
        Artifact artifact =
            artifactFactory.createArtifact( groupId, artifactId, version, Artifact.SCOPE_RUNTIME, "jar" );
        Artifact dummyArtifact = artifactFactory.createProjectArtifact( "dummy", "dummy", "1.0" );

        if ( artifact.isSnapshot() )
        {
            remoteRepositories.add( factory.createArtifactRepository( "apache.snapshots",
                                                                      "http://people.apache.org/repo/m2-snapshot-repository",
                                                                      layout, null, null ) );
        }

        ArtifactResolver resolver = (ArtifactResolver) container.lookup( ArtifactResolver.ROLE );

        List<String> exclusions = new ArrayList<String>();
        exclusions.add( "org.apache.maven.continuum:data-management-api" );
        exclusions.add( "org.codehaus.plexus:plexus-component-api" );
        exclusions.add( "org.codehaus.plexus:plexus-container-default" );
        exclusions.add( "stax:stax-api" );
        exclusions.add( "log4j:log4j" );

        ArtifactFilter filter = new ExcludesArtifactFilter( exclusions );

        ArtifactMetadataSource source =
            (ArtifactMetadataSource) container.lookup( ArtifactMetadataSource.ROLE, "maven" );
        ArtifactResolutionResult result = resolver.resolveTransitively( Collections.singleton( artifact ),
                                                                        dummyArtifact, localRepository,
                                                                        remoteRepositories, source, filter );

        return result.getArtifacts();
    }

    private static String getLocalRepositoryURL( PlexusContainer container )
        throws ComponentLookupException, IOException
    {
        File settingsFile = new File( System.getProperty( "user.home" ), "/.m2/settings.xml" );
        if ( !settingsFile.exists() )
        {
            return new File( System.getProperty( "user.home" ), "/.m2/repository" ).toURL().toString();
        }
        else
        {
            Settings settings = getSettings( container );
            return new File( settings.getLocalRepository() ).toURL().toString();
        }
    }

    private static Settings getSettings( PlexusContainer container )
        throws ComponentLookupException, IOException
    {
        MavenSettingsBuilder mavenSettingsBuilder =
            (MavenSettingsBuilder) container.lookup( MavenSettingsBuilder.class.getName() );
        try
        {
            return mavenSettingsBuilder.buildSettings( false );
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
            "/META-INF/maven/org.apache.maven.continuum/data-management-api/pom.properties" ) );
        return properties.getProperty( "version" );
    }

    private static class Commands
    {
        
        @Argument(description = "Display help information", value = "help", alias = "h")
        private boolean help;        
        
        @Argument(description = "Display version information", value = "version", alias = "v")
        private boolean version;        
        
        @Argument(
            description = "The JDBC URL for the Continuum database that contains the data to convert, or to import the data into",
            value = "buildsJdbcUrl")
        private String buildsJdbcUrl;

        @Argument(
            description = "The JDBC URL for the Redback database that contains the data to convert, or to import the data into",
            value = "usersJdbcUrl")
        private String usersJdbcUrl;

        // TODO: ability to use the enum directly would be nice
        @Argument(
            description = "Format of the database. Valid values are CONTINUUM_103, CONTINUUM_109, CONTINUUM_11. Default is CONTINUUM_11.")
        private String databaseFormat = DatabaseFormat.CONTINUUM_11.toString();

/* TODO: not yet supported
        @Argument(
            description = "Format of the backup directory. Valid values are CONTINUUM_103, CONTINUUM_109, CONTINUUM_11. Default is CONTINUUM_11.")
        private String dataFileFormat = DatabaseFormat.CONTINUUM_11.toString();
*/

        @Argument(
            description = "The directory to export the data to, or import the data from. Default is 'backups' in the current working directory.",
            value = "directory")
        private File directory = new File( "backups" );

        @Argument(
            description = "Mode of operation. Valid values are IMPORT and EXPORT. Default is EXPORT.",
            value = "mode")
        private String mode = OperationMode.EXPORT.toString();

        @Argument(
            description = "Whether to overwrite the designated directory if it already exists in export mode. Default is false.",
            value = "overwrite")
        private boolean overwrite;

        @Argument(
            description = "The type of database to use. Currently supported values are DERBY_10_1. The default value is DERBY_10_1.",
            value = "databaseType")
        private String databaseType = SupportedDatabase.DERBY_10_1.toString();

        @Argument(
            description = "Turn on debugging information. Default is off.",
            value = "debug")
        private boolean debug;
    }

    private enum OperationMode
    {
        IMPORT, EXPORT
    }

    private enum SupportedDatabase
    {
        DERBY_10_1( new DatabaseParams( "org.apache.derby.jdbc.EmbeddedDriver", "org.apache.derby", "derby", "10.1.3.1",
                                        "sa", "" ) );

        private DatabaseParams defaultParams;

        SupportedDatabase( DatabaseParams defaultParams )
        {
            this.defaultParams = defaultParams;
        }
    }
}
