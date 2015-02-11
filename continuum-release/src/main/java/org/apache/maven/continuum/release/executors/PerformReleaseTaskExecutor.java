package org.apache.maven.continuum.release.executors;

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

import org.apache.continuum.model.repository.LocalRepository;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.apache.maven.continuum.release.tasks.PerformReleaseProjectTask;
import org.apache.maven.continuum.release.tasks.ReleaseProjectTask;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.DuplicateProjectException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MissingProjectException;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectSorter;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.ReleaseManagerListener;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.DefaultReleaseEnvironment;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.dag.CycleDetectedException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Edwin Punzalan
 */
public class PerformReleaseTaskExecutor
    extends AbstractReleaseTaskExecutor
    implements Contextualizable
{

    @Requirement
    private MavenProjectBuilder projectBuilder;

    private ProfileManager profileManager;

    private PlexusContainer container;

    private LocalRepository repository;

    public void execute( ReleaseProjectTask task )
        throws TaskExecutionException
    {
        PerformReleaseProjectTask performTask = (PerformReleaseProjectTask) task;

        ReleaseManagerListener listener = performTask.getListener();

        ReleaseDescriptor descriptor = performTask.getDescriptor();
        descriptor.setUseReleaseProfile( performTask.isUseReleaseProfile() );
        descriptor.setPerformGoals( performTask.getGoals() );
        descriptor.setCheckoutDirectory( performTask.getBuildDirectory().getAbsolutePath() );

        repository = performTask.getLocalRepository();

        List reactorProjects;

        try
        {
            if ( !StringUtils.isEmpty( descriptor.getWorkingDirectory() ) )
            {
                MavenProject mavenProject = getMavenProject( performTask );
                reactorProjects = getReactorProjects( descriptor, mavenProject );
            }
            else
            {
                //Perform with provided release parameters (CONTINUUM-1541)
                descriptor.setCheckoutDirectory( performTask.getBuildDirectory().getAbsolutePath() );

                // Workaround bugs in maven-release-manager 2.1 that require a project even though it will ultimately
                // not be used. TODO: check if upgrading will fix, and return to being an empty list
                // The project is fake and won't exist in this location
                MavenProject project = new MavenProject();
                project.setFile( new File( descriptor.getCheckoutDirectory(), "pom.xml" ) );
                reactorProjects = Collections.singletonList( project );
                //                reactorProjects = Collections.emptyList();
            }
        }
        catch ( ContinuumReleaseException e )
        {
            ReleaseResult result = createReleaseResult();

            result.appendError( e );

            continuumReleaseManager.getReleaseResults().put( performTask.getReleaseId(), result );

            performTask.getListener().error( e.getMessage() );

            throw new TaskExecutionException( "Failed to build reactor projects.", e );
        }

        ReleaseResult result = releaseManager.performWithResult( descriptor,
                                                                 new DefaultReleaseEnvironment().setSettings(
                                                                     settings ), reactorProjects, listener );

        //override to show the actual start time
        result.setStartTime( getStartTime() );

        if ( result.getResultCode() == ReleaseResult.SUCCESS )
        {
            continuumReleaseManager.getPreparedReleases().remove( performTask.getReleaseId() );
        }

        continuumReleaseManager.getReleaseResults().put( performTask.getReleaseId(), result );
    }

    /**
     * @todo remove and use generate-reactor-projects phase
     */
    protected List<MavenProject> getReactorProjects( ReleaseDescriptor descriptor, MavenProject project )
        throws ContinuumReleaseException
    {
        List<MavenProject> reactorProjects = new ArrayList<MavenProject>();

        reactorProjects.add( project );

        addModules( reactorProjects, project );

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
        catch ( MissingProjectException e )
        {
            throw new ContinuumReleaseException( "Failed to sort projects.", e );
        }

        return reactorProjects;
    }

    private void addModules( List<MavenProject> reactorProjects, MavenProject project )
        throws ContinuumReleaseException
    {
        for ( Object o : project.getModules() )
        {
            String moduleDir = o.toString();

            File pomFile = new File( project.getBasedir(), moduleDir + "/pom.xml" );

            try
            {
                MavenProject reactorProject = projectBuilder.build( pomFile, getLocalRepository(), getProfileManager(
                    settings ) );

                reactorProjects.add( reactorProject );

                addModules( reactorProjects, reactorProject );
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

    private ArtifactRepository getLocalRepository()
    {
        if ( repository == null )
        {
            return new DefaultArtifactRepository( "local-repository", "file://" + settings.getLocalRepository(),
                                                  new DefaultRepositoryLayout() );
        }
        else
        {
            return new DefaultArtifactRepository( repository.getName(), "file://" + repository.getLocation(),
                                                  new DefaultRepositoryLayout() );
        }
    }

    private ProfileManager getProfileManager( Settings settings )
    {
        if ( profileManager == null )
        {
            profileManager = new DefaultProfileManager( container, settings );
        }

        return profileManager;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    protected MavenProject getMavenProject( PerformReleaseProjectTask releaseTask )
        throws ContinuumReleaseException
    {
        ReleaseDescriptor descriptor = releaseTask.getDescriptor();

        MavenProject project;
        try
        {
            project = projectBuilder.build( getProjectDescriptorFile( descriptor ), getLocalRepository(),
                                            getProfileManager( settings ) );
        }
        catch ( ProjectBuildingException e )
        {
            throw new ContinuumReleaseException( "Failed to build project.", e );
        }

        return project;
    }
}
