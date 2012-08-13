package org.apache.maven.continuum.xmlrpc.backup;

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
import org.apache.continuum.xmlrpc.release.ContinuumReleaseResult;
import org.apache.continuum.xmlrpc.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.xmlrpc.repository.LocalRepository;
import org.apache.continuum.xmlrpc.repository.RepositoryPurgeConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.maven.continuum.xmlrpc.client.ContinuumXmlRpcClient;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.xmlrpc.project.BuildResult;
import org.apache.maven.continuum.xmlrpc.project.BuildResultSummary;
import org.apache.maven.continuum.xmlrpc.project.Project;
import org.apache.maven.continuum.xmlrpc.project.ProjectDependency;
import org.apache.maven.continuum.xmlrpc.project.ProjectDeveloper;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroup;
import org.apache.maven.continuum.xmlrpc.project.ProjectNotifier;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;
import org.apache.maven.continuum.xmlrpc.project.Schedule;
import org.apache.maven.continuum.xmlrpc.scm.ChangeFile;
import org.apache.maven.continuum.xmlrpc.scm.ChangeSet;
import org.apache.maven.continuum.xmlrpc.scm.ScmResult;
import org.apache.maven.continuum.xmlrpc.system.Installation;
import org.apache.maven.continuum.xmlrpc.system.Profile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Backup
{
    private static final Logger LOGGER = Logger.getLogger( Backup.class );

    private static ContinuumXmlRpcClient client;

    private static int indent = 0;

    private static PrintWriter writer;

    public static void main( String[] args )
        throws Exception
    {
        Commands command = new Commands();

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
                System.out.println( "continuum-xmlrpc-backup version: " + getVersion() );
                return;
            }
            if ( command.url == null )
            {
                System.out.println( "You must specified the Continuum XMLRPC URL" );
                Args.usage( command );
                return;
            }
            if ( command.username == null )
            {
                System.out.println( "You must specified the Continuum username" );
                Args.usage( command );
                return;
            }
            if ( command.password == null )
            {
                System.out.println( "You must specified the Continuum password" );
                Args.usage( command );
                return;
            }
        }
        catch ( IllegalArgumentException e )
        {
            System.err.println( e.getMessage() );
            Args.usage( command );
            return;
        }

        BasicConfigurator.configure();
        if ( command.debug )
        {
            Logger.getRootLogger().setLevel( Level.DEBUG );
        }
        else
        {
            Logger.getRootLogger().setLevel( Level.INFO );
        }

        LOGGER.info( "Connection to " + command.url + "with username '" + command.username + "'..." );
        client = new ContinuumXmlRpcClient( command.url, command.username, command.password );
        LOGGER.info( "connected" );

        File out = command.outputFile;
        if ( out == null )
        {
            out = new File( "backup/builds.xml" );
        }
        out.getParentFile().mkdirs();

        if ( !command.overwrite && out.exists() )
        {
            System.err.println( out.getAbsolutePath() +
                                    " already exists and will not be overwritten unless the -overwrite flag is used." );
            Args.usage( command );
            return;
        }

        writer = new PrintWriter( new FileWriter( out ) );

        writer.println( "<?xml version='1.0' encoding='UTF-8'?>" );
        startTag( "continuumDatabase", true );
        backupSystemConfiguration();
        backupAllSchedules();
        backupAllInstallations();
        backupAllProfiles();
        backupAllBuildDefinitionTemplates();
        backupAllProjectGroup();
        backupAllLocalRepositories();
        backupAllRepositoryPurgeConfigurations();
        backupAllDirectoryPurgeConfigurations();
        endTag( "continuumDatabase", true );
        writer.close();
        LOGGER.info( "Done." );
    }

    private static String getVersion()
        throws IOException
    {
        Properties properties = new Properties();
        properties.load( Backup.class.getResourceAsStream(
            "/META-INF/maven/org.apache.maven.continuum/continuum-xmlrpc-backup/pom.properties" ) );
        return properties.getProperty( "version" );
    }

    private static class Commands
    {

        @Argument( description = "Display help information", value = "help", alias = "h" )
        private boolean help;

        @Argument( description = "Display version information", value = "version", alias = "v" )
        private boolean version;

        @Argument( description = "Continuum XMLRPC URL", value = "url" )
        private URL url;

        @Argument( description = "Username", value = "username", alias = "u" )
        private String username;

        @Argument( description = "Password", value = "password", alias = "p" )
        private String password;

        @Argument( description = "Backup file", value = "outputFile", alias = "o" )
        private File outputFile;

        @Argument(
            description = "Whether to overwrite the designated backup file if it already exists in export mode. Default is false.",
            value = "overwrite" )
        private boolean overwrite;

        @Argument(
            description = "Turn on debugging information. Default is off.",
            value = "debug" )
        private boolean debug;
    }

    private static void backupSystemConfiguration()
        throws Exception
    {
        LOGGER.info( "Backup system configuration" );
        writeObject( client.getSystemConfiguration(), "systemConfiguration", true );
    }

    private static void backupAllSchedules()
        throws Exception
    {
        LOGGER.info( "Backup schedules" );
        List<Schedule> schedules = client.getSchedules();
        if ( schedules != null && !schedules.isEmpty() )
        {
            startTag( "schedules", true );
            for ( Schedule schedule : schedules )
            {
                LOGGER.debug( "Backup schedule " + schedule.getName() );
                writeObject( schedule, "schedule", true );
            }
            endTag( "schedules", true );
        }
    }

    private static void backupAllInstallations()
        throws Exception
    {
        LOGGER.info( "Backup installations" );
        List<Installation> installs = client.getInstallations();
        if ( installs != null && !installs.isEmpty() )
        {
            startTag( "installations", true );
            for ( Installation install : installs )
            {
                LOGGER.debug( "Backup installation " + install.getName() );
                writeObject( install, "installation", true );
            }
            endTag( "installations", true );
        }
    }

    private static void backupAllBuildDefinitionTemplates()
        throws Exception
    {
        LOGGER.info( "Backup Build Definitions Templates" );
        List<BuildDefinitionTemplate> bdts = client.getBuildDefinitionTemplates();
        if ( bdts != null && !bdts.isEmpty() )
        {
            startTag( "buildDefinitionTemplates", true );
            for ( BuildDefinitionTemplate bdt : bdts )
            {
                LOGGER.debug( "Backup build definition template " + bdt.getName() );
                startTag( "buildDefinitionTemplate", true );
                writeSimpleFields( bdt );

                List<BuildDefinition> bds = bdt.getBuildDefinitions();
                if ( bds != null && !bds.isEmpty() )
                {
                    for ( BuildDefinition bd : bds )
                    {
                        backupBuildDefinition( bd );
                    }
                }
                endTag( "buildDefinitionTemplate", true );
            }
            endTag( "buildDefinitionTemplates", true );
        }
    }

    private static void backupAllProfiles()
        throws Exception
    {
        LOGGER.info( "Backup profiles" );
        List<Profile> profiles = client.getProfiles();
        if ( profiles != null && !profiles.isEmpty() )
        {
            startTag( "profiles", true );
            for ( Profile p : profiles )
            {
                LOGGER.debug( "Backup profile " + p.getName() );
                writeProfile( p );
            }
            endTag( "profiles", true );
        }
    }

    private static void backupAllProjectGroup()
        throws Exception
    {
        LOGGER.info( "Backup project groups" );
        List<ProjectGroup> pgs = client.getAllProjectGroupsWithAllDetails();
        if ( pgs != null && !pgs.isEmpty() )
        {
            startTag( "projectGroups", true );
            for ( ProjectGroup pg : pgs )
            {
                backupProjectGroup( pg );
            }
            endTag( "projectGroups", true );
        }
    }

    private static void backupProjectGroup( ProjectGroup pg )
        throws Exception
    {
        if ( pg == null )
        {
            return;
        }

        LOGGER.debug( "Backup project group " + pg.getName() );
        startTag( "projectGroup", true );
        writeSimpleFields( pg );

        if ( pg.getProjects() != null && !pg.getProjects().isEmpty() )
        {
            startTag( "projects", true );
            for ( ProjectSummary ps : (List<ProjectSummary>) pg.getProjects() )
            {
                backupProject( ps );
            }
            endTag( "projects", true );
        }

        if ( pg.getBuildDefinitions() != null && !pg.getBuildDefinitions().isEmpty() )
        {
            startTag( "buildDefinitions", true );
            for ( BuildDefinition bd : (List<BuildDefinition>) pg.getBuildDefinitions() )
            {
                backupBuildDefinition( bd );
            }
            endTag( "buildDefinitions", true );
        }

        if ( pg.getNotifiers() != null && !pg.getNotifiers().isEmpty() )
        {
            startTag( "notifiers", true );
            for ( ProjectNotifier notif : (List<ProjectNotifier>) pg.getNotifiers() )
            {
                backupNotifier( notif );
            }
            endTag( "notifiers", true );
        }

        backupContinuumReleaseResultsForProjectGroup( pg.getId() );
        endTag( "projectGroup", true );
    }

    private static void backupProject( ProjectSummary ps )
        throws Exception
    {
        if ( ps == null )
        {
            return;
        }

        LOGGER.debug( "Backup project " + ps.getName() );

        Project p = client.getProjectWithAllDetails( ps.getId() );
        startTag( "project", true );
        writeSimpleFields( p );

        if ( p.getProjectGroup() != null )
        {
            writeTagWithParameter( "projectGroup", "id", String.valueOf( p.getProjectGroup().getId() ) );
        }

        if ( p.getDevelopers() != null && !p.getDevelopers().isEmpty() )
        {
            startTag( "developers", true );
            for ( ProjectDeveloper pd : (List<ProjectDeveloper>) p.getDevelopers() )
            {
                writeObject( pd, "developer", true );
            }
            endTag( "developers", true );
        }

        if ( p.getDependencies() != null && !p.getDependencies().isEmpty() )
        {
            startTag( "dependencies", true );
            for ( ProjectDependency pd : (List<ProjectDependency>) p.getDependencies() )
            {
                writeObject( pd, "dependency", true );
            }
            endTag( "dependencies", true );
        }

        if ( p.getBuildDefinitions() != null && !p.getBuildDefinitions().isEmpty() )
        {
            startTag( "buildDefinitions", true );
            for ( BuildDefinition bd : (List<BuildDefinition>) p.getBuildDefinitions() )
            {
                backupBuildDefinition( bd );
            }
            endTag( "buildDefinitions", true );
        }

        if ( p.getNotifiers() != null && !p.getNotifiers().isEmpty() )
        {
            startTag( "notifiers", true );
            for ( ProjectNotifier notif : (List<ProjectNotifier>) p.getNotifiers() )
            {
                backupNotifier( notif );
            }
            endTag( "notifiers", true );
        }

        List<BuildResultSummary> brs = client.getBuildResultsForProject( p.getId() );
        if ( brs != null && !brs.isEmpty() )
        {
            startTag( "buildResults", true );
            for ( BuildResultSummary brSummary : brs )
            {
                BuildResult br = client.getBuildResult( p.getId(), brSummary.getId() );
                backupBuildResult( br );
            }
            endTag( "buildResults", true );
        }
        endTag( "project", true );
    }

    private static void backupBuildResult( BuildResult br )
        throws Exception
    {
        if ( br == null )
        {
            return;
        }

        startTag( "buildResult", true );
        writeSimpleFields( br );

        if ( br.getProject() != null )
        {
            writeTagWithParameter( "project", "id", String.valueOf( br.getProject().getId() ) );
        }

        if ( br.getBuildDefinition() != null )
        {
            writeTagWithParameter( "buildDefinition", "id", String.valueOf( br.getBuildDefinition().getId() ) );
        }

        if ( br.getModifiedDependencies() != null && !br.getModifiedDependencies().isEmpty() )
        {
            startTag( "dependencies", true );
            for ( ProjectDependency pd : (List<ProjectDependency>) br.getModifiedDependencies() )
            {
                writeObject( pd, "dependency", true );
            }
            endTag( "dependencies", true );
        }
        endTag( "buildResult", true );
    }

    private static void writeSimpleFields( Object obj )
        throws Exception
    {
        if ( obj == null )
        {
            return;
        }

        for ( Field f : getFieldsIncludingSuperclasses( obj.getClass() ) )
        {
            if ( "modelEncoding".equals( f.getName() ) )
            {
                continue;
            }

            if ( !f.isAccessible() )
            {
                f.setAccessible( true );
            }

            if ( f.getType().getName().equals( "int" ) || f.getType().getName().equals( "long" ) ||
                f.getType().getName().equals( "boolean" ) || f.getType().getName().equals( "java.lang.String" ) )
            {
                Object value = f.get( obj );
                if ( value != null )
                {
                    startTag( f.getName(), false );
                    writer.print( value );
                    endTag( f.getName(), false );
                }
            }
            else if ( ScmResult.class.getName().equals( f.getType().getName() ) )
            {
                writeScmResult( (ScmResult) f.get( obj ) );
            }
            else if ( ChangeFile.class.getName().equals( f.getType().getName() ) )
            {
                writeObject( f.get( obj ), "changeFile", true );
            }
            else if ( Profile.class.getName().equals( f.getType().getName() ) )
            {
                writeProfile( (Profile) f.get( obj ) );
            }
            else
            {
                //LOGGER.debug(
                //    "Rejected: (" + f.getName() + ") " + f.getType() + " in object " + obj.getClass().getName() );
            }
        }
    }

    private static void writeObject( Object obj, String tagName, boolean addNewLine )
        throws Exception
    {
        if ( obj == null )
        {
            return;
        }
        startTag( tagName, addNewLine );
        writeSimpleFields( obj );
        endTag( tagName, addNewLine );
    }

    private static void backupBuildDefinition( BuildDefinition buildDef )
        throws Exception
    {
        if ( buildDef == null )
        {
            return;
        }
        startTag( "buildDefinition", true );
        writeSimpleFields( buildDef );
        if ( buildDef.getSchedule() != null )
        {
            writeTagWithParameter( "schedule", "id", String.valueOf( buildDef.getSchedule().getId() ) );
        }
        endTag( "buildDefinition", true );
    }

    private static void backupNotifier( ProjectNotifier notifier )
        throws Exception
    {
        startTag( "notifier", true );
        writeSimpleFields( notifier );

        Map conf = notifier.getConfiguration();
        startTag( "configuration", true );
        for ( String key : (Set<String>) conf.keySet() )
        {
            startTag( key, false );
            writer.print( conf.get( key ) );
            endTag( key, false );
        }
        endTag( "configuration", true );

        endTag( "notifier", true );
    }

    private static void writeProfile( Profile profile )
        throws Exception
    {
        if ( profile == null )
        {
            return;
        }

        startTag( "profile", true );
        writeSimpleFields( profile );

        if ( profile.getEnvironmentVariables() != null && !profile.getEnvironmentVariables().isEmpty() )
        {
            startTag( "environmentVariables", true );
            for ( Installation env : (List<Installation>) profile.getEnvironmentVariables() )
            {
                writeTagWithParameter( "environmentVariable", "installationId", String.valueOf(
                    env.getInstallationId() ) );
            }
            endTag( "environmentVariables", true );
        }

        if ( profile.getJdk() != null )
        {
            writeTagWithParameter( "jdk", "installationId", String.valueOf( profile.getJdk().getInstallationId() ) );
        }

        if ( profile.getBuilder() != null )
        {
            writeTagWithParameter( "builder", "installationId", String.valueOf(
                profile.getBuilder().getInstallationId() ) );
        }

        endTag( "profile", true );
    }

    private static void writeScmResult( ScmResult scmResult )
        throws Exception
    {
        if ( scmResult == null )
        {
            return;
        }

        startTag( "scmResult", true );
        writeSimpleFields( scmResult );

        if ( scmResult.getChanges() != null && !scmResult.getChanges().isEmpty() )
        {
            startTag( "changeSets", true );
            for ( ChangeSet cs : (List<ChangeSet>) scmResult.getChanges() )
            {
                writeObject( cs, "changeSet", true );
            }
            endTag( "changeSets", true );
        }
        endTag( "scmResult", true );
    }

    private static void startTag( String tagName, boolean addNewLineAfter )
    {
        writer.print( getIndent() );
        writer.print( "<" );
        writer.print( tagName );
        writer.print( ">" );
        if ( addNewLineAfter )
        {
            writer.println();
            indent++;
        }
    }

    private static void endTag( String tagName, boolean isOnNewLine )
    {
        if ( isOnNewLine )
        {
            indent--;
            writer.print( getIndent() );
        }
        writer.print( "</" );
        writer.print( tagName );
        writer.println( ">" );
    }

    private static void writeTagWithParameter( String tagName, String parameterName, String parameterValue )
    {
        writer.print( getIndent() );
        writer.print( "<" );
        writer.print( tagName );
        writer.print( " " );
        writer.print( parameterName );
        writer.print( "=\"" );
        writer.print( parameterValue );
        writer.print( "\"></" );
        writer.print( tagName );
        writer.println( ">" );
    }

    private static String getIndent()
    {
        String result = "";
        for ( int i = 0; i < indent; i++ )
        {
            result += "  ";
        }
        return result;
    }

    private static List<Field> getFieldsIncludingSuperclasses( Class clazz )
    {
        List<Field> fields = new ArrayList<Field>( Arrays.asList( clazz.getDeclaredFields() ) );

        Class superclass = clazz.getSuperclass();

        if ( superclass != null )
        {
            fields.addAll( getFieldsIncludingSuperclasses( superclass ) );
        }

        return fields;
    }

    private static void backupAllLocalRepositories()
        throws Exception
    {
        LOGGER.info( "Backup local repositories" );
        List<LocalRepository> repos = client.getAllLocalRepositories();
        if ( repos != null && !repos.isEmpty() )
        {
            startTag( "localRepositories", true );
            for ( LocalRepository repo : repos )
            {
                LOGGER.debug( "Backup local repository " + repo.getName() );
                writeObject( repo, "localRepository", true );
            }
            endTag( "localRepositories", true );
        }
    }

    private static void backupAllRepositoryPurgeConfigurations()
        throws Exception
    {
        LOGGER.info( "Backup repository purge configurations" );
        List<RepositoryPurgeConfiguration> purgeConfigs = client.getAllRepositoryPurgeConfigurations();
        if ( purgeConfigs != null && !purgeConfigs.isEmpty() )
        {
            startTag( "repositoryPurgeConfigurations", true );
            for ( RepositoryPurgeConfiguration purgeConfig : purgeConfigs )
            {
                LOGGER.debug( "Backup repository purge configuration" );
                backupRepositoryPurgeConfiguration( purgeConfig );
            }
            endTag( "repositoryPurgeConfigurations", true );
        }
    }

    private static void backupRepositoryPurgeConfiguration( RepositoryPurgeConfiguration repoPurge )
        throws Exception
    {
        if ( repoPurge == null )
        {
            return;
        }
        startTag( "repositoryPurgeConfiguration", true );
        writeSimpleFields( repoPurge );

        if ( repoPurge.getRepository() != null )
        {
            writeTagWithParameter( "repository", "id", String.valueOf( repoPurge.getRepository().getId() ) );
        }

        if ( repoPurge.getSchedule() != null )
        {
            writeTagWithParameter( "schedule", "id", String.valueOf( repoPurge.getSchedule().getId() ) );
        }
        endTag( "repositoryPurgeConfiguration", true );
    }

    private static void backupAllDirectoryPurgeConfigurations()
        throws Exception
    {
        LOGGER.info( "Backup repository purge configurations" );
        List<DirectoryPurgeConfiguration> purgeConfigs = client.getAllDirectoryPurgeConfigurations();
        if ( purgeConfigs != null && !purgeConfigs.isEmpty() )
        {
            startTag( "directoryPurgeConfigurations", true );
            for ( DirectoryPurgeConfiguration purgeConfig : purgeConfigs )
            {
                LOGGER.debug( "Backup directory purge configuration" );
                backupDirectoryPurgeConfiguration( purgeConfig );
            }
            endTag( "directoryPurgeConfigurations", true );
        }
    }

    private static void backupDirectoryPurgeConfiguration( DirectoryPurgeConfiguration dirPurge )
        throws Exception
    {
        if ( dirPurge == null )
        {
            return;
        }
        startTag( "directoryPurgeConfiguration", true );
        writeSimpleFields( dirPurge );

        if ( dirPurge.getSchedule() != null )
        {
            writeTagWithParameter( "schedule", "id", String.valueOf( dirPurge.getSchedule().getId() ) );
        }
        endTag( "directoryPurgeConfiguration", true );
    }

    private static void backupContinuumReleaseResultsForProjectGroup( int projectGroupId )
        throws Exception
    {
        LOGGER.info( "Backup release results" );
        List<ContinuumReleaseResult> results = client.getReleaseResultsForProjectGroup( projectGroupId );
        if ( results != null && !results.isEmpty() )
        {
            startTag( "continuumReleaseResults", true );
            for ( ContinuumReleaseResult result : results )
            {
                LOGGER.debug( "Backup release result" );
                backupContinuumReleaseResult( result );
            }
            endTag( "continuumReleaseResults", true );
        }
    }

    private static void backupContinuumReleaseResult( ContinuumReleaseResult result )
        throws Exception
    {
        if ( result == null )
        {
            return;
        }
        startTag( "continuumReleaseResult", true );
        writeSimpleFields( result );

        if ( result.getProjectGroup() != null )
        {
            writeTagWithParameter( "projectGroup", "id", String.valueOf( result.getProjectGroup().getId() ) );
        }
        if ( result.getProject() != null )
        {
            writeTagWithParameter( "project", "id", String.valueOf( result.getProject().getId() ) );
        }
        endTag( "continuumReleaseResult", true );
    }
}
