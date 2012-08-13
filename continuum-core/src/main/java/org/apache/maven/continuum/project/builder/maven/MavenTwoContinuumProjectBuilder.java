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
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.project.builder.ContinuumProjectBuilder" role-hint="maven-two-builder"
 */
public class MavenTwoContinuumProjectBuilder
    extends AbstractContinuumProjectBuilder
    implements ContinuumProjectBuilder
{
    public static final String ID = "maven-two-builder";

    private static final String POM_PART = "/pom.xml";

    /**
     * @plexus.requirement
     */
    private LocalRepositoryDao localRepositoryDao;

    /**
     * @plexus.requirement
     */
    private MavenBuilderHelper builderHelper;

    /**
     * @plexus.requirement
     */
    private ScheduleDao scheduleDao;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionService buildDefinitionService;

    /**
     * @plexus.configuration
     */
    private List<String> excludedPackagingTypes = new ArrayList<String>();

    private Project rootProject;

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
        // ----------------------------------------------------------------------
        // We need to roll the project data into a file so that we can use it
        // ----------------------------------------------------------------------

        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        try
        {
            readModules( url, result, true, username, password, null, loadRecursiveProjects, buildDefinitionTemplate,
                         checkoutInSingleDirectory );
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumProjectBuilderException( e.getMessage(), e );
        }
        return result;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void readModules( URL url, ContinuumProjectBuildingResult result, boolean groupPom, String username,
                              String password, String scmUrl, boolean loadRecursiveProjects,
                              BuildDefinitionTemplate buildDefinitionTemplate, boolean checkoutInSingleDirectory )
        throws ContinuumProjectBuilderException, BuildDefinitionServiceException
    {

        MavenProject mavenProject;

        File pomFile = null;

        try
        {
            pomFile = createMetadataFile( result, url, username, password );

            if ( result.hasErrors() )
            {
                return;
            }

            mavenProject = builderHelper.getMavenProject( result, pomFile );

            if ( result.hasErrors() )
            {
                return;
            }
        }
        finally
        {
            if ( pomFile != null && pomFile.exists() )
            {
                pomFile.delete();
            }
        }
        log.debug( "groupPom " + groupPom );

        ProjectGroup projectGroup = null;
        if ( groupPom )
        {
            projectGroup = buildProjectGroup( mavenProject, result );

            // project groups have the top lvl build definition which is the default build defintion for the sub
            // projects
            log.debug( "projectGroup != null" + ( projectGroup != null ) );
            if ( projectGroup != null )
            {
                List<BuildDefinition> buildDefinitions = getBuildDefinitions( buildDefinitionTemplate,
                                                                              loadRecursiveProjects );
                boolean defaultSetted = false;
                for ( BuildDefinition buildDefinition : buildDefinitions )
                {
                    if ( !defaultSetted && buildDefinition.isDefaultForProject() )
                    {
                        buildDefinition.setDefaultForProject( true );
                        defaultSetted = true;
                    }
                    buildDefinition = buildDefinitionService.addBuildDefinition(
                        buildDefinitionService.cloneBuildDefinition( buildDefinition ) );
                    //CONTINUUM-1296
                    String defaultGoal = mavenProject.getBuild().getDefaultGoal();
                    if ( StringUtils.isNotEmpty( defaultGoal ) )
                    {
                        buildDefinition.setGoals( defaultGoal );
                    }
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
                    // jdo complains that Collections.singletonList(bd) is a second class object and fails.
                    //ArrayList arrayList = new ArrayList();

                    //arrayList.add( buildDefinition );

                    projectGroup.addBuildDefinition( buildDefinition );
                    // .setBuildDefinitions( arrayList );
                }
                result.addProjectGroup( projectGroup );
            }
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
                    "Error adding project: Unknown error mapping project " + url + ": " + result.getErrorsAsString() );
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
            if ( !loadRecursiveProjects )
            {
                // should only contain 1 project group
                ProjectGroup pg = result.getProjectGroups().iterator().next();
                if ( pg.equals( projectGroup ) )
                {
                    List<BuildDefinition> pgBuildDefs = pg.getBuildDefinitions();
                    for ( BuildDefinition bD : pgBuildDefs )
                    {
                        if ( bD.isDefaultForProject() )
                        {
                            // create a default build definition at the project
                            // level
                            BuildDefinition projectBuildDef = buildDefinitionService.cloneBuildDefinition( bD );
                            projectBuildDef.setDefaultForProject( true );

                            String arguments = projectBuildDef.getArguments().replace( "--non-recursive", "" );
                            arguments = arguments.replace( "-N", "" );
                            projectBuildDef.setArguments( arguments );

                            log.info( "Adding default build definition for project '" + continuumProject.getName() +
                                          "' without '--non-recursive' flag." );

                            continuumProject.addBuildDefinition( projectBuildDef );

                            break;
                        }
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
                    readModules( moduleUrl, result, false, username, password, moduleScmUrl, true,
                                 buildDefinitionTemplate, checkoutInSingleDirectory );
                }
            }
        }
    }

    private List<BuildDefinition> getBuildDefinitions( BuildDefinitionTemplate template, boolean loadRecursiveProjects )
        throws ContinuumProjectBuilderException, BuildDefinitionServiceException
    {
        List<BuildDefinition> buildDefinitions = new ArrayList<BuildDefinition>();
        for ( BuildDefinition buildDefinition : (List<BuildDefinition>) template.getBuildDefinitions() )
        {
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
