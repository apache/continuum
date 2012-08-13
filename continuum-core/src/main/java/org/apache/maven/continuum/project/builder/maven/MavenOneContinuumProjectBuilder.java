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

import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.builddefinition.BuildDefinitionServiceException;
import org.apache.maven.continuum.execution.maven.m1.MavenOneBuildExecutor;
import org.apache.maven.continuum.execution.maven.m1.MavenOneMetadataHelper;
import org.apache.maven.continuum.execution.maven.m1.MavenOneMetadataHelperException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.project.builder.AbstractContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilderException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.project.builder.ContinuumProjectBuilder"
 * role-hint="maven-one-builder"
 */
public class MavenOneContinuumProjectBuilder
    extends AbstractContinuumProjectBuilder
    implements ContinuumProjectBuilder
{
    public static final String ID = "maven-one-builder";

    /**
     * @plexus.requirement
     */
    private BuildDefinitionService buildDefinitionService;

    /**
     * @plexus.requirement
     */
    private MavenOneMetadataHelper metadataHelper;

    // ----------------------------------------------------------------------
    // ProjectCreator Implementation
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password )
        throws ContinuumProjectBuilderException
    {
        return buildProjectsFromMetadata( url, username, password, true, false );
    }

    public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password,
                                                                     boolean recursiveProjects,
                                                                     boolean checkoutInSingleDirectory )
        throws ContinuumProjectBuilderException
    {
        try
        {
            return buildProjectsFromMetadata( url, username, password, recursiveProjects,
                                              buildDefinitionService.getDefaultMavenOneBuildDefinitionTemplate(),
                                              false );
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumProjectBuilderException( e.getMessage(), e );
        }
    }

    public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password,
                                                                     boolean recursiveProjects,
                                                                     BuildDefinitionTemplate buildDefinitionTemplate,
                                                                     boolean checkoutInSingleDirectory )
        throws ContinuumProjectBuilderException
    {
        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        File pomFile;

        pomFile = createMetadataFile( result, url, username, password );

        if ( pomFile == null )
        {
            return result;
        }

        Project project = new Project();

        try
        {
            metadataHelper.mapMetadata( result, pomFile, project, true );

            if ( result.hasErrors() )
            {
                return result;
            }
            for ( BuildDefinition bd : (List<BuildDefinition>) buildDefinitionTemplate.getBuildDefinitions() )
            {
                BuildDefinition cloneBuildDefinition = buildDefinitionService.cloneBuildDefinition( bd );
                cloneBuildDefinition.setTemplate( false );
                project.addBuildDefinition( cloneBuildDefinition );
            }
            result.addProject( project, MavenOneBuildExecutor.ID );
        }
        catch ( MavenOneMetadataHelperException e )
        {
            log.error( "Unknown error while processing metadata", e );

            result.addError( ContinuumProjectBuildingResult.ERROR_UNKNOWN );
        }
        finally
        {
            if ( pomFile.exists() )
            {
                pomFile.delete();
            }
        }

        ProjectGroup projectGroup = new ProjectGroup();

        // ----------------------------------------------------------------------
        // Group id
        // ----------------------------------------------------------------------

        if ( StringUtils.isEmpty( project.getGroupId() ) )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_GROUPID );
        }

        projectGroup.setGroupId( project.getGroupId() );

        // ----------------------------------------------------------------------
        // Name
        // ----------------------------------------------------------------------

        String name = project.getName();

        if ( StringUtils.isEmpty( name ) )
        {
            name = project.getGroupId();
        }

        projectGroup.setName( name );

        // ----------------------------------------------------------------------
        // Description
        // ----------------------------------------------------------------------

        projectGroup.setDescription( project.getDescription() );

        result.addProjectGroup( projectGroup );

        return result;
    }

    public BuildDefinitionTemplate getDefaultBuildDefinitionTemplate()
        throws ContinuumProjectBuilderException
    {
        try
        {
            return buildDefinitionService.getDefaultMavenOneBuildDefinitionTemplate();
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumProjectBuilderException( e.getMessage(), e );
        }
    }
}
