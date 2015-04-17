package org.apache.maven.continuum.project.builder.maven;

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

import org.apache.continuum.dao.LocalRepositoryDao;
import org.apache.continuum.dao.ProjectGroupDao;
import org.apache.continuum.dao.ScheduleDao;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.builddefinition.BuildDefinitionServiceException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.maven.m2.MavenBuilderHelper;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.project.builder.AbstractContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilderException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
@Component( role = org.apache.maven.continuum.project.builder.ContinuumProjectBuilder.class, hint = "maven-two-builder" )
public class MavenTwoContinuumProjectBuilder
    extends AbstractContinuumProjectBuilder
    implements ContinuumProjectBuilder
{
    public static final String ID = "maven-two-builder";

    private static final String POM_PART = "/pom.xml";

    @Requirement
    private LocalRepositoryDao localRepositoryDao;

    @Requirement
    private MavenBuilderHelper builderHelper;

    @Requirement
    private ScheduleDao scheduleDao;

    @Requirement
    private BuildDefinitionService buildDefinitionService;

    @Configuration( "" )
    private List<String> excludedPackagingTypes = new ArrayList<String>();

    private Project rootProject;

    @Requirement
    private ProjectGroupDao projectGroupDao;

    // ----------------------------------------------------------------------
    // AbstractContinuumProjectBuilder Implementation
    // ----------------------------------------------------------------------
    public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password )
        throws ContinuumProjectBuilderException
    {
        return buildProjectsFromMetadata( url, username, password, true, false );
    }

    public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password,
                                                                     boolean loadRecursiveProjects,
                                                                     boolean checkoutInSingleDirectory )
        throws ContinuumProjectBuilderException
    {
        try
        {
            return buildProjectsFromMetadata( url, username, password, loadRecursiveProjects,
                                              buildDefinitionService.getDefaultMavenTwoBuildDefinitionTemplate(),
                                              checkoutInSingleDirectory );
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumProjectBuilderException( e.getMessage(), e );
        }
    }

    public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password,
                                                                     boolean loadRecursiveProjects,
                                                                     BuildDefinitionTemplate buildDefinitionTemplate,
                                                                     boolean checkoutInSingleDirectory )
        throws ContinuumProjectBuilderException
    {
        return buildProjectsFromMetadata( url, username, password, loadRecursiveProjects, buildDefinitionTemplate,
                                          checkoutInSingleDirectory, -1 );
    }

    public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password,
                                                                     boolean loadRecursiveProjects,
                                                                     BuildDefinitionTemplate buildDefinitionTemplate,
                                                                     boolean checkoutInSingleDirectory,
                                                                     int projectGroupId )
        throws ContinuumProjectBuilderException
    {
        // ----------------------------------------------------------------------
        // We need to roll the project data into a file so that we can use it
        // ----------------------------------------------------------------------

        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        try
        {
            ProjectGroup projectGroup = null;
            if ( projectGroupId > 0 )
            {
                projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( projectGroupId );
            }

            importProject( url, result, projectGroup, username, password, null, loadRecursiveProjects,
                           buildDefinitionTemplate,
                           checkoutInSingleDirectory );
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumProjectBuilderException( e.getMessage(), e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumProjectBuilderException( e.getMessage(), e );
        }
        return result;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void importProject( URL url, ContinuumProjectBuildingResult result, ProjectGroup projectGroup,
                                String username, String password, String scmUrl, boolean loadRecursiveProjects,
                                BuildDefinitionTemplate buildDefinitionTemplate, boolean checkoutInSingleDirectory )
        throws ContinuumProjectBuilderException, BuildDefinitionServiceException
    {
        File importRoot = fsManager.createTempFile( "continuum-m2import-", "", fsManager.getTempDir() );
        if ( !importRoot.mkdirs() )
        {
            throw new ContinuumProjectBuilderException( "failed to create directory for import: " + importRoot );
        }

        try
        {
            importProjects( importRoot, url, result, projectGroup, username, password, scmUrl, loadRecursiveProjects,
                            buildDefinitionTemplate, checkoutInSingleDirectory );
        }
        finally
        {
            if ( importRoot.exists() )
            {
                try
                {
                    fsManager.removeDir( importRoot );
                }
                catch ( IOException e )
                {
                    log.warn( "failed to remove {} after project import: {}", importRoot, e.getMessage() );
                }
            }
        }
    }

    private void importProjects( File importRoot, URL url, ContinuumProjectBuildingResult result,
                                 ProjectGroup projectGroup, String username, String password, String scmUrl,
                                 boolean loadRecursiveProjects, BuildDefinitionTemplate buildDefinitionTemplate,
                                 boolean checkoutInSingleDirectory )
        throws ContinuumProjectBuilderException, BuildDefinitionServiceException
    {
        File pomFile = createMetadataFile( importRoot, result, url, username, password );

        if ( result.hasErrors() )
        {
            return;
        }

        MavenProject mavenProject = builderHelper.getMavenProject( result, pomFile );

        if ( result.hasErrors() )
        {
            return;
        }

        log.debug( "projectGroup " + projectGroup );

        if ( projectGroup == null )
        {
            projectGroup = buildProjectGroup( mavenProject, result );

            // project groups have the top lvl build definition which is the default build defintion for the sub
            // projects
            log.debug( "projectGroup != null" + ( projectGroup != null ) );
            if ( projectGroup != null )
            {
                List<BuildDefinition> buildDefinitions = getBuildDefinitions( buildDefinitionTemplate,
                                                                              loadRecursiveProjects,
                                                                              mavenProject.getBuild().getDefaultGoal() );
                for ( BuildDefinition buildDefinition : buildDefinitions )
                {
                    buildDefinition = persistBuildDefinition( buildDefinition );

                    projectGroup.addBuildDefinition( buildDefinition );
                }
            }
        }
        if ( result.getProjectGroups().isEmpty() )
        {
            result.addProjectGroup( projectGroup );
        }

        if ( !excludedPackagingTypes.contains( mavenProject.getPackaging() ) )
        {
            Project continuumProject = new Project();

            /*
            We are interested in having the scm username and password being passed into this method be taken into
            account during project mapping so make sure we set it to the continuum project instance.
             */
            if ( username != null && StringUtils.isNotEmpty( username ) )
            {
                continuumProject.setScmUsername( username );

                if ( password != null && StringUtils.isNotEmpty( password ) )
                {
                    continuumProject.setScmPassword( password );
                }
            }

            continuumProject.setCheckedOutInSingleDirectory( checkoutInSingleDirectory );

            // New project
            builderHelper.mapMavenProjectToContinuumProject( result, mavenProject, continuumProject, true );

            if ( result.hasErrors() )
            {
                log.info(
                    "Error adding project: Unknown error mapping project " + url + ": "
                        + result.getErrorsAsString() );
                return;
            }

            // Rewrite scmurl from the one found in added project due to a bug in scm url resolution
            // for projects that doesn't have module name != artifactId
            if ( StringUtils.isNotEmpty( scmUrl ) )
            {
                continuumProject.setScmUrl( scmUrl );
            }
            else
            {
                scmUrl = continuumProject.getScmUrl();
            }

            if ( !"HEAD".equals( mavenProject.getScm().getTag() ) )
            {
                continuumProject.setScmTag( mavenProject.getScm().getTag() );
            }

            // CONTINUUM-2563
            // Don't create if the project has a build definition template assigned to it already
            if ( !loadRecursiveProjects && buildDefinitionTemplate.equals( getDefaultBuildDefinitionTemplate() ) )
            {
                List<BuildDefinition> buildDefinitions = projectGroup.getBuildDefinitions();
                for ( BuildDefinition buildDefinition : buildDefinitions )
                {
                    if ( buildDefinition.isDefaultForProject() )
                    {
                        // create a default build definition at the project level
                        BuildDefinition projectBuildDef =
                            buildDefinitionService.cloneBuildDefinition( buildDefinition );
                        projectBuildDef.setDefaultForProject( true );

                        String arguments = projectBuildDef.getArguments().replace( "--non-recursive", "" );
                        arguments = arguments.replace( "-N", "" );
                        arguments = arguments.trim();

                        // add build definition only if it differs
                        if ( !projectBuildDef.getArguments().equals( arguments ) )
                        {
                            log.info( "Adding default build definition for project '" + continuumProject.getName() +
                                          "' without '--non-recursive' flag." );

                            projectBuildDef.setArguments( arguments );
                            continuumProject.addBuildDefinition( projectBuildDef );
                        }

                        break;
                    }
                }
            }

            result.addProject( continuumProject, MavenTwoBuildExecutor.ID );

            if ( checkoutInSingleDirectory && rootProject == null )
            {
                rootProject = continuumProject;
                result.setRootProject( rootProject );
            }
        }

        List<String> modules = mavenProject.getModules();

        String prefix = url.toExternalForm();

        String suffix = "";

        int i = prefix.indexOf( '?' );

        int lastSlash;

        if ( i != -1 )
        {
            suffix = prefix.substring( i );

            lastSlash = prefix.lastIndexOf( "/", i );
        }
        else
        {
            lastSlash = prefix.lastIndexOf( "/" );
        }

        prefix = prefix.substring( 0, lastSlash );
        if ( loadRecursiveProjects )
        {
            for ( String module : modules )
            {
                if ( StringUtils.isNotEmpty( module ) )
                {
                    String urlString = prefix + "/" + module + POM_PART + suffix;

                    URL moduleUrl;

                    try
                    {
                        urlString = StringUtils.replace( urlString, '\\', '/' );
                        moduleUrl = new URL( urlString );
                    }
                    catch ( MalformedURLException e )
                    {
                        log.debug( "Error adding project module: Malformed URL " + urlString, e );
                        result.addError( ContinuumProjectBuildingResult.ERROR_MALFORMED_URL, urlString );
                        continue;
                    }

                    String moduleScmUrl = "";

                    String modulePath = StringUtils.replace( new String( module ), '\\', '/' );

                    // check if module is relative
                    if ( modulePath.indexOf( "../" ) != -1 )
                    {
                        int depth = StringUtils.countMatches( StringUtils.substring( modulePath, 0,
                                                                                     modulePath.lastIndexOf( '/' ) +
                                                                                         1 ), "/" );
                        String baseUrl = "";
                        for ( int j = 1; j <= depth; j++ )
                        {
                            scmUrl = StringUtils.chompLast( new String( scmUrl ), "/" );
                            baseUrl = StringUtils.substring( scmUrl, 0, scmUrl.lastIndexOf( '/' ) );
                        }
                        moduleScmUrl = baseUrl + "/" + StringUtils.substring( modulePath, modulePath.lastIndexOf(
                            "../" ) + 3 );
                    }
                    else
                    {
                        scmUrl = StringUtils.chompLast( scmUrl, "/" );
                        moduleScmUrl = scmUrl + "/" + modulePath;
                    }
                    // we are in recursive loading mode
                    importProjects( importRoot, moduleUrl, result, projectGroup, username, password, moduleScmUrl, true,
                                    buildDefinitionTemplate, checkoutInSingleDirectory );
                }
            }
        }
    }

    private BuildDefinition persistBuildDefinition( BuildDefinition buildDefinition )
        throws BuildDefinitionServiceException
    {
        buildDefinition = buildDefinitionService.addBuildDefinition( buildDefinition );
        if ( buildDefinition.getSchedule() == null )
        {
            try
            {
                Schedule schedule = scheduleDao.getScheduleByName(
                    ConfigurationService.DEFAULT_SCHEDULE_NAME );

                buildDefinition.setSchedule( schedule );
            }
            catch ( ContinuumStoreException e )
            {
                log.warn( "Can't get default schedule.", e );
            }
        }
        return buildDefinition;
    }

    private List<BuildDefinition> getBuildDefinitions( BuildDefinitionTemplate template, boolean loadRecursiveProjects,
                                                       String defaultGoal )
        throws ContinuumProjectBuilderException, BuildDefinitionServiceException
    {
        List<BuildDefinition> buildDefinitions = new ArrayList<BuildDefinition>();
        boolean defaultSet = false;
        for ( BuildDefinition buildDefinition : template.getBuildDefinitions() )
        {
            buildDefinition = buildDefinitionService.cloneBuildDefinition( buildDefinition );

            if ( !defaultSet && buildDefinition.isDefaultForProject() )
            {
                defaultSet = true;

                //CONTINUUM-1296
                if ( StringUtils.isNotEmpty( defaultGoal ) )
                {
                    buildDefinition.setGoals( defaultGoal );
                }
            }
            else
            {
                buildDefinition.setDefaultForProject( false );
            }

            // due to CONTINUUM-1207 CONTINUUM-1436 user can do what they want with arguments
            // we must remove if exists --non-recursive or -N
            if ( !loadRecursiveProjects )
            {
                if ( StringUtils.isEmpty( buildDefinition.getArguments() ) )
                {
                    // strange for a mvn build 
                    log.info( "build definition '" + buildDefinition.getId() + "' has empty args" );
                }
                else
                {
                    String arguments = buildDefinition.getArguments().replace( "--non-recursive", "" );
                    arguments = arguments.replace( "-N", "" );
                    arguments = arguments.trim();
                    buildDefinition.setArguments( arguments );
                }
            }
            buildDefinition.setTemplate( false );
            buildDefinitions.add( buildDefinition );
        }
        return buildDefinitions;

    }

    private ProjectGroup buildProjectGroup( MavenProject mavenProject, ContinuumProjectBuildingResult result )
    {
        ProjectGroup projectGroup = new ProjectGroup();

        // ----------------------------------------------------------------------
        // Group id
        // ----------------------------------------------------------------------

        if ( StringUtils.isEmpty( mavenProject.getGroupId() ) )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_GROUPID );

            return null;
        }

        projectGroup.setGroupId( mavenProject.getGroupId() );

        // ----------------------------------------------------------------------
        // Name
        // ----------------------------------------------------------------------

        String name = mavenProject.getName();

        if ( StringUtils.isEmpty( name ) )
        {
            name = mavenProject.getGroupId();
        }

        projectGroup.setName( name );

        // ----------------------------------------------------------------------
        // Description
        // ----------------------------------------------------------------------

        projectGroup.setDescription( mavenProject.getDescription() );

        // ----------------------------------------------------------------------
        // Local Repository
        // ----------------------------------------------------------------------

        try
        {
            LocalRepository repository = localRepositoryDao.getLocalRepositoryByName( "DEFAULT" );

            projectGroup.setLocalRepository( repository );
        }
        catch ( ContinuumStoreException e )
        {
            log.warn( "Can't get default repository.", e );
        }

        return projectGroup;
    }

    public BuildDefinitionTemplate getDefaultBuildDefinitionTemplate()
        throws ContinuumProjectBuilderException
    {
        try
        {
            return buildDefinitionService.getDefaultMavenTwoBuildDefinitionTemplate();
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumProjectBuilderException( e.getMessage(), e );
        }
    }

}
