package org.apache.continuum.buildagent.build.execution.maven.m2;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
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

/**
 * @plexus.component role="org.apache.continuum.buildagent.build.execution.maven.m2.MavenBuilderHelper" 
 * role-hint="default"
 */
public class DefaultMavenBuilderHelper
    implements MavenBuilderHelper, Contextualizable, Initializable
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

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
    private ArtifactRepositoryLayout repositoryLayout;

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
                    for ( Iterator<String> i = validationResult.getMessages().iterator(); i.hasNext(); )
                    {
                        String valmsg = i.next();
                        result.addError( ContinuumProjectBuildingResult.ERROR_VALIDATION, valmsg );
                        messages.append( valmsg );
                        messages.append( "\n" );
                    }
                }
            }

            if ( cause instanceof ArtifactNotFoundException )
            {
                result.addError( ContinuumProjectBuildingResult.ERROR_ARTIFACT_NOT_FOUND,
                                 ( (ArtifactNotFoundException) cause ).toString() );
                return null;
            }

            result.addError( ContinuumProjectBuildingResult.ERROR_PROJECT_BUILDING, e.getMessage() );

            String msg = "Cannot build maven project from " + file + " (" + e.getMessage() + ").\n" + messages;

            file.delete();

            log.error( msg );

            return null;
        }
        // TODO catch all exceptions is bad
        catch ( Exception e )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_PROJECT_BUILDING, e.getMessage() );

            String msg = "Cannot build maven project from " + file + " (" + e.getMessage() + ").";

            file.delete();

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

            log.error(
                "Missing 'connection' element in the 'scm' element in the " + getProjectName( project ) + " POM." );

            return null;
        }

        return project;
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
                                                                   repositoryLayout, null, null );
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
                message.append( "\n - " ).append( profile.getId() ).append( " (source: " )
                    .append( profile.getSource() ).append( ")" );
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

            for ( Iterator<Server> i = settings.getServers().iterator(); i.hasNext(); )
            {
                Server server = i.next();

                wagonManager.addAuthenticationInfo( server.getId(), server.getUsername(), server.getPassword(),
                                                    server.getPrivateKey(), server.getPassphrase() );

                wagonManager.addPermissionInfo( server.getId(), server.getFilePermissions(),
                                                server.getDirectoryPermissions() );

                if ( server.getConfiguration() != null )
                {
                    wagonManager.addConfiguration( server.getId(), (Xpp3Dom) server.getConfiguration() );
                }
            }

            for ( Iterator<Mirror> i = settings.getMirrors().iterator(); i.hasNext(); )
            {
                Mirror mirror = i.next();

                wagonManager.addMirror( mirror.getId(), mirror.getMirrorOf(), mirror.getUrl() );
            }
        }
        finally
        {
            container.release( wagonManager );
        }
    }
}
