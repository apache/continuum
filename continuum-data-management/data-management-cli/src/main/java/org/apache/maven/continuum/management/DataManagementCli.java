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
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An application for performing database upgrades from old Continuum and Redback versions. A suitable tool until it
 * is natively incorporated into Continuum itself.
 */
public class DataManagementCli
{
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

        if ( command.debug )
        {
            BasicConfigurator.configure();
            Logger.getRootLogger().setLevel( Level.DEBUG );
            Logger.getLogger( "JPOX" ).setLevel( Level.DEBUG );
        }

        DatabaseParams params = new DatabaseParams( databaseType.defaultParams );
        params.setUrl( command.jdbcUrl );

        DefaultPlexusContainer container = new DefaultPlexusContainer();
        List<Artifact> artifacts = new ArrayList<Artifact>();
        artifacts.addAll( downloadArtifact( container, "jpox", "jpox", databaseFormat.getJpoxVersion() ) );
        artifacts.addAll(
            downloadArtifact( container, params.getGroupId(), params.getArtifactId(), params.getVersion() ) );
        artifacts.addAll(
            downloadArtifact( container, "org.apache.maven.continuum", "data-management-jdo", "1.1-SNAPSHOT" ) );

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
                    !"continuum-legacy".equals( id ) )
                {
                    exclusions.add( "org.apache.maven.continuum:" + id );
                    jars.add( new File( url.getPath() ) );
                }
            }
        }
        ArtifactFilter filter = new ExcludesArtifactFilter( exclusions );

        for ( Artifact a : artifacts )
        {
            if ( filter.include( a ) )
            {
                jars.add( a.getFile() );
            }
        }

        ClassRealm realm = container.createComponentRealm( "app", jars );

        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( realm );

        ClassRealm oldRealm = container.setLookupRealm( realm );

        DatabaseManager manager = (DatabaseManager) container.lookup( DatabaseManager.class.getName(), "jdo", realm );
        manager.configure( params );

        DataManagementTool tool =
            (DataManagementTool) container.lookup( DataManagementTool.ROLE, databaseFormat.getToolRoleHint(), realm );

        if ( mode == OperationMode.EXPORT )
        {
            tool.backupBuildDatabase( command.directory );
        }
        else if ( mode == OperationMode.IMPORT )
        {
            tool.eraseBuildDatabase();
            tool.restoreBuildDatabase( command.directory );
        }

        container.setLookupRealm( oldRealm );
        Thread.currentThread().setContextClassLoader( oldLoader );
    }

    private static Collection<Artifact> downloadArtifact( PlexusContainer container, String groupId, String artifactId,
                                                          String version )
        throws ComponentLookupException, MalformedURLException, ArtifactNotFoundException, ArtifactResolutionException
    {
        ArtifactRepositoryFactory factory =
            (ArtifactRepositoryFactory) container.lookup( ArtifactRepositoryFactory.ROLE );

        DefaultRepositoryLayout layout =
            (DefaultRepositoryLayout) container.lookup( ArtifactRepositoryLayout.ROLE, "default" );

        File file = new File( System.getProperty( "user.home" ), "/.m2/repository" );
        ArtifactRepository localRepository =
            factory.createArtifactRepository( "local", file.toURL().toString(), layout, null, null );

        List<ArtifactRepository> remoteRepositories = Collections.singletonList(
            factory.createArtifactRepository( "central", "http://repo1.maven.org/maven2", layout, null, null ) );

        ArtifactFactory artifactFactory = (ArtifactFactory) container.lookup( ArtifactFactory.ROLE );
        Artifact artifact =
            artifactFactory.createArtifact( groupId, artifactId, version, Artifact.SCOPE_RUNTIME, "jar" );
        Artifact dummyArtifact = artifactFactory.createProjectArtifact( "dummy", "dummy", "1.0" );

        ArtifactResolver resolver = (ArtifactResolver) container.lookup( ArtifactResolver.ROLE );

        List<String> exclusions = new ArrayList<String>();
        exclusions.add( "org.apache.maven.continuum:data-management-api" );
        exclusions.add( "org.codehaus.plexus:plexus-component-api" );
        exclusions.add( "org.codehaus.plexus:plexus-container-default" );
        exclusions.add( "stax:stax-api" );
        exclusions.add( "log4j:log4j" );

        Collection<File> jars = new ArrayList<File>();

        ArtifactFilter filter = new ExcludesArtifactFilter( exclusions );

        ArtifactMetadataSource source =
            (ArtifactMetadataSource) container.lookup( ArtifactMetadataSource.ROLE, "maven" );
        ArtifactResolutionResult result = resolver.resolveTransitively( Collections.singleton( artifact ),
                                                                        dummyArtifact, localRepository,
                                                                        remoteRepositories, source, filter );

        return result.getArtifacts();
    }

    private static class Commands
    {
        @Argument(required = true,
                  description = "The JDBC URL for the database that contains the data to convert, or to import the data into",
                  value = "jdbcUrl")
        private String jdbcUrl;

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

    // TODO: add user database formats

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
