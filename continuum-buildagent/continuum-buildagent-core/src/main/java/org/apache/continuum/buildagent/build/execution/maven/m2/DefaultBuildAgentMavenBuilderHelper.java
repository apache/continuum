package org.apache.continuum.buildagent.build.execution.maven.m2;

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

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Model;
import org.apache.maven.model.Notifier;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Profile;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.Scm;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.InvalidProjectModelException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.validation.ModelValidationResult;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Writer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @plexus.component role="org.apache.continuum.buildagent.build.execution.maven.m2.BuildAgentMavenBuilderHelper"
 * role-hint="default"
 */
public class DefaultBuildAgentMavenBuilderHelper
    implements BuildAgentMavenBuilderHelper, Contextualizable, Initializable
{
    private static final Logger log = LoggerFactory.getLogger( DefaultBuildAgentMavenBuilderHelper.class );

    /**
     * @plexus.requirement
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * @plexus.requirement
     */
    private MavenSettingsBuilder mavenSettingsBuilder;

    /**
     * @plexus.requirement
     */
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    /**
     * @plexus.requirement
     */
    private ArtifactRepositoryLayout defaultRepositoryLayout;

    private PlexusContainer container;

    public MavenProject getMavenProject( ContinuumProjectBuildingResult result, File file )
    {
        MavenProject project;

        try
        {
            //   TODO: This seems like code that is shared with DefaultMaven, so it should be moved to the project
            //   builder perhaps

            Settings settings = getSettings();

            if ( log.isDebugEnabled() )
            {
                writeSettings( settings );
            }

            ProfileManager profileManager = new DefaultProfileManager( container, settings );

            project = projectBuilder.build( file, getRepository( settings ), profileManager, false );

            if ( log.isDebugEnabled() )
            {
                writePom( project );
                writeActiveProfileStatement( project );
            }

        }
        catch ( ProjectBuildingException e )
        {
            StringBuffer messages = new StringBuffer();

            Throwable cause = e.getCause();

            if ( cause != null )
            {
                while ( ( cause.getCause() != null ) && ( cause instanceof ProjectBuildingException ) )
                {
                    cause = cause.getCause();
                }
            }

            if ( e instanceof InvalidProjectModelException )
            {
                InvalidProjectModelException ex = (InvalidProjectModelException) e;

                ModelValidationResult validationResult = ex.getValidationResult();

                if ( validationResult != null && validationResult.getMessageCount() > 0 )
                {
                    for ( String valmsg : (List<String>) validationResult.getMessages() )
                    {
                        result.addError( ContinuumProjectBuildingResult.ERROR_VALIDATION, valmsg );
                        messages.append( valmsg );
                        messages.append( "\n" );
                    }
                }
            }

            if ( cause instanceof ArtifactNotFoundException )
            {
                result.addError( ContinuumProjectBuildingResult.ERROR_ARTIFACT_NOT_FOUND, ( cause ).toString() );
                return null;
            }

            result.addError( ContinuumProjectBuildingResult.ERROR_PROJECT_BUILDING, e.getMessage() );

            String msg = "Cannot build maven project from " + file + " (" + e.getMessage() + ").\n" + messages;

            log.error( msg );

            return null;
        }
        // TODO catch all exceptions is bad
        catch ( Exception e )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_PROJECT_BUILDING, e.getMessage() );

            String msg = "Cannot build maven project from " + file + " (" + e.getMessage() + ").";

            log.error( msg );

            return null;
        }

        // ----------------------------------------------------------------------
        // Validate the MavenProject using some Continuum rules
        // ----------------------------------------------------------------------

        // SCM connection
        Scm scm = project.getScm();

        if ( scm == null )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_SCM, getProjectName( project ) );

            log.error( "Missing 'scm' element in the " + getProjectName( project ) + " POM." );

            return null;
        }

        String url = scm.getConnection();

        if ( StringUtils.isEmpty( url ) )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_SCM_CONNECTION, getProjectName( project ) );

            log.error( "Missing 'connection' element in the 'scm' element in the " + getProjectName( project ) +
                           " POM." );

            return null;
        }

        return project;
    }

    public void mapMetadataToProject( ContinuumProjectBuildingResult result, File metadata, Project continuumProject )
    {
        MavenProject mavenProject = getMavenProject( result, metadata );

        if ( mavenProject == null )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_UNKNOWN,
                             "Can't load the maven project. Verify that your scm url is correct and remove/readd the project." );
            return;
        }

        mapMavenProjectToContinuumProject( result, mavenProject, continuumProject, false );
    }

    public void mapMavenProjectToContinuumProject( ContinuumProjectBuildingResult result, MavenProject mavenProject,
                                                   Project continuumProject, boolean groupPom )
    {
        if ( mavenProject == null )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_UNKNOWN, "The maven project is null." );
            return;
        }

        // ----------------------------------------------------------------------
        // Name
        // ----------------------------------------------------------------------

        continuumProject.setName( getProjectName( mavenProject ) );

        // ----------------------------------------------------------------------
        // SCM Url
        // ----------------------------------------------------------------------

        // TODO: Remove this: scm url shouldn't be null there
        if ( StringUtils.isEmpty( continuumProject.getScmUrl() ) )
        {
            String scmUrl = getScmUrl( mavenProject );

            continuumProject.setScmUrl( scmUrl );

            if ( !"HEAD".equals( mavenProject.getScm().getTag() ) )
            {
                continuumProject.setScmTag( mavenProject.getScm().getTag() );
            }
        }

        // ----------------------------------------------------------------------
        // Version
        // ----------------------------------------------------------------------

        continuumProject.setVersion( getVersion( mavenProject ) );

        // ----------------------------------------------------------------------
        // GroupId
        // ----------------------------------------------------------------------

        if ( !StringUtils.isEmpty( mavenProject.getGroupId() ) )
        {
            continuumProject.setGroupId( mavenProject.getGroupId() );
        }
        else
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_GROUPID );
            return;
        }

        // ----------------------------------------------------------------------
        // artifactId
        // ----------------------------------------------------------------------

        if ( !StringUtils.isEmpty( mavenProject.getArtifactId() ) )
        {
            continuumProject.setArtifactId( mavenProject.getArtifactId() );
        }
        else
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_ARTIFACTID );
            return;
        }

        // ----------------------------------------------------------------------
        // Project Url
        // ----------------------------------------------------------------------

        if ( !StringUtils.isEmpty( mavenProject.getUrl() ) )
        {
            continuumProject.setUrl( mavenProject.getUrl() );
        }

        // ----------------------------------------------------------------------
        // Developers
        // ----------------------------------------------------------------------

        if ( mavenProject.getDevelopers() != null )
        {
            List<ProjectDeveloper> developers = new ArrayList<ProjectDeveloper>();

            for ( Developer d : (List<Developer>) mavenProject.getDevelopers() )
            {
                ProjectDeveloper cd = new ProjectDeveloper();

                cd.setScmId( d.getId() );

                cd.setName( d.getName() );

                cd.setEmail( d.getEmail() );

                developers.add( cd );
            }

            continuumProject.setDevelopers( developers );
        }

        // ----------------------------------------------------------------------
        // Parent
        // ----------------------------------------------------------------------

        if ( mavenProject.getParent() != null )
        {
            MavenProject parentProject = mavenProject.getParent();

            ProjectDependency parent = new ProjectDependency();

            parent.setGroupId( parentProject.getGroupId() );

            parent.setArtifactId( parentProject.getArtifactId() );

            parent.setVersion( parentProject.getVersion() );

            continuumProject.setParent( parent );
        }

        // ----------------------------------------------------------------------
        // Dependencies
        // ----------------------------------------------------------------------

        List<ProjectDependency> dependencies = new ArrayList<ProjectDependency>();

        for ( Dependency dependency : (List<Dependency>) mavenProject.getDependencies() )
        {
            ProjectDependency cd = new ProjectDependency();

            cd.setGroupId( dependency.getGroupId() );

            cd.setArtifactId( dependency.getArtifactId() );

            cd.setVersion( dependency.getVersion() );

            dependencies.add( cd );
        }

        for ( Plugin dependency : (List<Plugin>) mavenProject.getBuildPlugins() )
        {
            ProjectDependency cd = new ProjectDependency();

            cd.setGroupId( dependency.getGroupId() );

            cd.setArtifactId( dependency.getArtifactId() );

            cd.setVersion( dependency.getVersion() );

            dependencies.add( cd );
        }

        for ( ReportPlugin dependency : (List<ReportPlugin>) mavenProject.getReportPlugins() )
        {
            ProjectDependency cd = new ProjectDependency();

            cd.setGroupId( dependency.getGroupId() );

            cd.setArtifactId( dependency.getArtifactId() );

            cd.setVersion( dependency.getVersion() );

            dependencies.add( cd );
        }

        for ( Extension dependency : (List<Extension>) mavenProject.getBuildExtensions() )
        {
            ProjectDependency cd = new ProjectDependency();

            cd.setGroupId( dependency.getGroupId() );

            cd.setArtifactId( dependency.getArtifactId() );

            cd.setVersion( dependency.getVersion() );

            dependencies.add( cd );
        }

        continuumProject.setDependencies( dependencies );

        // ----------------------------------------------------------------------
        // Notifiers
        // ----------------------------------------------------------------------

        List<ProjectNotifier> userNotifiers = new ArrayList<ProjectNotifier>();

        if ( continuumProject.getNotifiers() != null )
        {
            for ( int i = 0; i < continuumProject.getNotifiers().size(); i++ )
            {
                ProjectNotifier notifier = (ProjectNotifier) continuumProject.getNotifiers().get( i );

                if ( notifier.isFromUser() )
                {
                    ProjectNotifier userNotifier = new ProjectNotifier();

                    userNotifier.setType( notifier.getType() );

                    userNotifier.setEnabled( notifier.isEnabled() );

                    userNotifier.setConfiguration( notifier.getConfiguration() );

                    userNotifier.setFrom( notifier.getFrom() );

                    userNotifier.setRecipientType( notifier.getRecipientType() );

                    userNotifier.setSendOnError( notifier.isSendOnError() );

                    userNotifier.setSendOnFailure( notifier.isSendOnFailure() );

                    userNotifier.setSendOnSuccess( notifier.isSendOnSuccess() );

                    userNotifier.setSendOnWarning( notifier.isSendOnWarning() );

                    userNotifier.setSendOnScmFailure( notifier.isSendOnScmFailure() );

                    userNotifiers.add( userNotifier );
                }
            }
        }

        List<ProjectNotifier> notifiers = getNotifiers( result, mavenProject, continuumProject );
        if ( notifiers != null )
        {
            continuumProject.setNotifiers( notifiers );
        }

        for ( ProjectNotifier notifier : userNotifiers )
        {
            continuumProject.addNotifier( notifier );
        }
    }

    private String getScmUrl( MavenProject project )
    {
        return project.getScm().getConnection();
    }

    private List<ProjectNotifier> getNotifiers( ContinuumProjectBuildingResult result, MavenProject mavenProject,
                                                Project continuumProject )
    {
        List<ProjectNotifier> notifiers = new ArrayList<ProjectNotifier>();

        if ( mavenProject.getCiManagement() != null && mavenProject.getCiManagement().getNotifiers() != null )
        {
            for ( Notifier projectNotifier : (List<Notifier>) mavenProject.getCiManagement().getNotifiers() )
            {
                ProjectNotifier notifier = new ProjectNotifier();

                if ( StringUtils.isEmpty( projectNotifier.getType() ) )
                {
                    result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_NOTIFIER_TYPE );
                    return null;
                }

                notifier.setType( projectNotifier.getType() );

                if ( projectNotifier.getConfiguration() == null )
                {
                    result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_NOTIFIER_CONFIGURATION );
                    return null;
                }

                notifier.setConfiguration( projectNotifier.getConfiguration() );

                notifier.setFrom( ProjectNotifier.FROM_PROJECT );

                notifier.setSendOnSuccess( projectNotifier.isSendOnSuccess() );

                notifier.setSendOnFailure( projectNotifier.isSendOnFailure() );

                notifier.setSendOnError( projectNotifier.isSendOnError() );

                notifier.setSendOnWarning( projectNotifier.isSendOnWarning() );

                notifier.setSendOnScmFailure( false );

                notifiers.add( notifier );
            }
        }

        return notifiers;
    }

    private String getVersion( MavenProject project )
    {
        return project.getVersion();
    }

    private Settings getSettings()
        throws SettingsConfigurationException
    {
        try
        {
            return mavenSettingsBuilder.buildSettings( false );
        }
        catch ( IOException e )
        {
            throw new SettingsConfigurationException( "Error reading settings file", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new SettingsConfigurationException( e.getMessage(), e.getDetail(), e.getLineNumber(),
                                                      e.getColumnNumber() );
        }
    }

    private ArtifactRepository getRepository( Settings settings )
    {
        return artifactRepositoryFactory.createArtifactRepository( "local", "file://" + settings.getLocalRepository(),
                                                                   defaultRepositoryLayout, null, null );
    }

    public String getProjectName( MavenProject project )
    {
        String name = project.getName();

        if ( StringUtils.isEmpty( name ) )
        {
            return project.getId();
        }

        return name;
    }

    private void writeSettings( Settings settings )
    {
        StringWriter sWriter = new StringWriter();

        SettingsXpp3Writer settingsWriter = new SettingsXpp3Writer();

        try
        {
            settingsWriter.write( sWriter, settings );

            StringBuffer message = new StringBuffer();

            message.append( "\n************************************************************************************" );
            message.append( "\nEffective Settings" );
            message.append( "\n************************************************************************************" );
            message.append( "\n" );
            message.append( sWriter.toString() );
            message.append( "\n************************************************************************************" );
            message.append( "\n\n" );

            log.debug( message.toString() );
        }
        catch ( IOException e )
        {
            log.warn( "Cannot serialize Settings to XML.", e );
        }
    }

    private void writePom( MavenProject project )
    {
        StringBuffer message = new StringBuffer();

        Model pom = project.getModel();

        StringWriter sWriter = new StringWriter();

        MavenXpp3Writer pomWriter = new MavenXpp3Writer();

        try
        {
            pomWriter.write( sWriter, pom );

            message.append( "\n************************************************************************************" );
            message.append( "\nEffective POM for project \'" ).append( project.getId() ).append( "\'" );
            message.append( "\n************************************************************************************" );
            message.append( "\n" );
            message.append( sWriter.toString() );
            message.append( "\n************************************************************************************" );
            message.append( "\n\n" );

            log.debug( message.toString() );
        }
        catch ( IOException e )
        {
            log.warn( "Cannot serialize POM to XML.", e );
        }
    }

    private void writeActiveProfileStatement( MavenProject project )
    {
        List<Profile> profiles = project.getActiveProfiles();

        StringBuffer message = new StringBuffer();

        message.append( "\n" );

        message.append( "\n************************************************************************************" );
        message.append( "\nActive Profiles for Project \'" ).append( project.getId() ).append( "\'" );
        message.append( "\n************************************************************************************" );
        message.append( "\n" );

        if ( profiles == null || profiles.isEmpty() )
        {
            message.append( "There are no active profiles." );
        }
        else
        {
            message.append( "The following profiles are active:\n" );

            for ( Profile profile : profiles )
            {
                message.append( "\n - " ).append( profile.getId() ).append( " (source: " ).append(
                    profile.getSource() ).append( ")" );
            }

        }

        message.append( "\n************************************************************************************" );
        message.append( "\n\n" );

        log.debug( message.toString() );
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    public void initialize()
        throws InitializationException
    {
        try
        {
            Settings settings = getSettings();

            resolveParameters( settings );
        }
        catch ( Exception e )
        {
            throw new InitializationException( "Can't initialize '" + getClass().getName() + "'", e );
        }
    }

    /**
     * @todo [BP] this might not be required if there is a better way to pass
     * them in. It doesn't feel quite right.
     * @todo [JC] we should at least provide a mapping of protocol-to-proxy for
     * the wagons, shouldn't we?
     */
    private void resolveParameters( Settings settings )
        throws ComponentLookupException, ComponentLifecycleException, SettingsConfigurationException
    {
        WagonManager wagonManager = (WagonManager) container.lookup( WagonManager.ROLE );

        try
        {
            Proxy proxy = settings.getActiveProxy();

            if ( proxy != null )
            {
                if ( proxy.getHost() == null )
                {
                    throw new SettingsConfigurationException( "Proxy in settings.xml has no host" );
                }

                wagonManager.addProxy( proxy.getProtocol(), proxy.getHost(), proxy.getPort(), proxy.getUsername(),
                                       proxy.getPassword(), proxy.getNonProxyHosts() );
            }

            for ( Server server : (List<Server>) settings.getServers() )
            {
                wagonManager.addAuthenticationInfo( server.getId(), server.getUsername(), server.getPassword(),
                                                    server.getPrivateKey(), server.getPassphrase() );

                wagonManager.addPermissionInfo( server.getId(), server.getFilePermissions(),
                                                server.getDirectoryPermissions() );

                if ( server.getConfiguration() != null )
                {
                    wagonManager.addConfiguration( server.getId(), (Xpp3Dom) server.getConfiguration() );
                }
            }

            for ( Mirror mirror : (List<Mirror>) settings.getMirrors() )
            {
                wagonManager.addMirror( mirror.getId(), mirror.getMirrorOf(), mirror.getUrl() );
            }
        }
        finally
        {
            container.release( wagonManager );
        }
    }

    void setMavenSettingsBuilder( MavenSettingsBuilder mavenSettingsBuilder )
    {
        this.mavenSettingsBuilder = mavenSettingsBuilder;
    }
}
