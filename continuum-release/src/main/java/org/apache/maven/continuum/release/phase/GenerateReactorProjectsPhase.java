package org.apache.maven.continuum.release.phase;

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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.DuplicateProjectException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectSorter;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.apache.maven.shared.release.phase.AbstractReleasePhase;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Generate the reactor projects
 *
 * @author Edwin Punzalan
 * @version $Id$
 */
public class GenerateReactorProjectsPhase
    extends AbstractReleasePhase
    implements Contextualizable
{
    private MavenProjectBuilder projectBuilder;

    private MavenSettingsBuilder settingsBuilder;

    private PlexusContainer container;

    private static final char SET_SYSTEM_PROPERTY = 'D';

    private static final char ACTIVATE_PROFILES = 'P';

    public ReleaseResult execute( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException
    {
        ReleaseResult result = new ReleaseResult();

        try
        {
            reactorProjects.addAll( getReactorProjects( releaseDescriptor ) );
        }
        catch ( ContinuumReleaseException e )
        {
            throw new ReleaseExecutionException( "Unable to get reactor projects: " + e.getMessage(), e );
        }

        result.setResultCode( ReleaseResult.SUCCESS );

        return result;
    }

    public ReleaseResult simulate( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException
    {
        return execute( releaseDescriptor, settings, reactorProjects );
    }


    public ReleaseResult execute( ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment,
                                  List reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException
    {
        return execute( releaseDescriptor, releaseEnvironment.getSettings(), reactorProjects );
    }

    public ReleaseResult simulate( ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment,
                                   List reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException
    {
        return execute( releaseDescriptor, releaseEnvironment.getSettings(), reactorProjects );
    }

    private List getReactorProjects( ReleaseDescriptor descriptor )
        throws ContinuumReleaseException
    {
        List<MavenProject> reactorProjects = new ArrayList<MavenProject>();

        MavenProject project;
        try
        {
            String arguments = descriptor.getAdditionalArguments();
            ArtifactRepository repository = getLocalRepository( arguments );
            ProfileManager profileManager = getProfileManager( getSettings() );

            if ( arguments != null )
            {
                activateProfiles( arguments, profileManager );
            }

            project = projectBuilder.build( getProjectDescriptorFile( descriptor ), repository, profileManager );

            reactorProjects.add( project );

            addModules( reactorProjects, project, repository );
        }
        catch ( ParseException e )
        {
            throw new ContinuumReleaseException( "Unable to parse arguments.", e );
        }
        catch ( ProjectBuildingException e )
        {
            throw new ContinuumReleaseException( "Failed to build project.", e );
        }

        try
        {
            reactorProjects = new ProjectSorter( reactorProjects ).getSortedProjects();
        }
        catch ( CycleDetectedException e )
        {
            throw new ContinuumReleaseException( "Failed to sort projects.", e );
        }
        catch ( DuplicateProjectException e )
        {
            throw new ContinuumReleaseException( "Failed to sort projects.", e );
        }

        return reactorProjects;
    }

    private void addModules( List<MavenProject> reactorProjects, MavenProject project, ArtifactRepository repository )
        throws ContinuumReleaseException
    {
        for ( Object o : project.getModules() )
        {
            String moduleDir = StringUtils.replace( o.toString(), '\\', '/' );

            File pomFile = new File( project.getBasedir(), moduleDir + "/pom.xml" );

            try
            {
                MavenProject reactorProject = projectBuilder.build( pomFile, repository, getProfileManager(
                    getSettings() ) );

                reactorProjects.add( reactorProject );

                addModules( reactorProjects, reactorProject, repository );
            }
            catch ( ProjectBuildingException e )
            {
                throw new ContinuumReleaseException( "Failed to build project.", e );
            }
        }
    }

    private File getProjectDescriptorFile( ReleaseDescriptor descriptor )
    {
        String parentPath = descriptor.getWorkingDirectory();

        String pomFilename = descriptor.getPomFileName();
        if ( pomFilename == null )
        {
            pomFilename = "pom.xml";
        }

        return new File( parentPath, pomFilename );
    }

    private ArtifactRepository getLocalRepository( String arguments )
        throws ContinuumReleaseException
    {
        String localRepository = null;
        boolean found = false;

        if ( arguments != null )
        {
            String[] args = arguments.split( " " );

            for ( String arg : args )
            {
                if ( arg.contains( "-Dmaven.repo.local=" ) )
                {
                    localRepository = arg.substring( arg.indexOf( "=" ) + 1 );

                    if ( localRepository.endsWith( "\"" ) )
                    {
                        localRepository = localRepository.substring( 0, localRepository.indexOf( "\"" ) );
                        break;
                    }

                    found = true;
                    continue;
                }

                if ( found )
                {
                    localRepository += " " + arg;

                    if ( localRepository.endsWith( "\"" ) )
                    {
                        localRepository = localRepository.substring( 0, localRepository.indexOf( "\"" ) );
                        break;
                    }
                }
            }
        }

        if ( localRepository == null )
        {
            localRepository = getSettings().getLocalRepository();
        }

        return new DefaultArtifactRepository( "local-repository", "file://" + localRepository,
                                              new DefaultRepositoryLayout() );
    }

    private ProfileManager getProfileManager( Settings settings )
    {
        Properties props = new Properties();
        return new DefaultProfileManager( container, settings, props );
    }

    private Settings getSettings()
        throws ContinuumReleaseException
    {
        try
        {
            return settingsBuilder.buildSettings();
        }
        catch ( IOException e )
        {
            throw new ContinuumReleaseException( "Failed to get Maven Settings.", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ContinuumReleaseException( "Failed to get Maven Settings.", e );
        }
    }

    @SuppressWarnings( "static-access" )
    private void activateProfiles( String arguments, ProfileManager profileManager )
        throws ParseException
    {
        CommandLineParser parser = new GnuParser();

        Options options = new Options();

        options.addOption( OptionBuilder.withLongOpt( "activate-profiles" ).withDescription(
            "Comma-delimited list of profiles to activate" ).hasArg().create( ACTIVATE_PROFILES ) );

        options.addOption( OptionBuilder.withLongOpt( "define" ).hasArg().withDescription(
            "Define a system property" ).create( SET_SYSTEM_PROPERTY ) );

        String[] args = StringUtils.split( arguments );

        CommandLine commandLine = parser.parse( options, args );

        if ( commandLine.hasOption( ACTIVATE_PROFILES ) )
        {
            String[] profileOptionValues = commandLine.getOptionValues( ACTIVATE_PROFILES );

            if ( profileOptionValues != null )
            {
                for ( int i = 0; i < profileOptionValues.length; ++i )
                {
                    StringTokenizer profileTokens = new StringTokenizer( profileOptionValues[i], "," );

                    while ( profileTokens.hasMoreTokens() )
                    {
                        String profileAction = profileTokens.nextToken().trim();

                        if ( profileAction.startsWith( "-" ) || profileAction.startsWith( "!" ) )
                        {
                            profileManager.explicitlyDeactivate( profileAction.substring( 1 ) );
                        }
                        else if ( profileAction.startsWith( "+" ) )
                        {
                            profileManager.explicitlyActivate( profileAction.substring( 1 ) );
                        }
                        else
                        {
                            profileManager.explicitlyActivate( profileAction );
                        }
                    }
                }
            }
        }
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }


}
