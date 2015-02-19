package org.apache.maven.continuum.builddefinition;

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

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;

import java.util.List;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 15 sept. 07
 */
public interface BuildDefinitionService
{

    /**
     * @param buildDefinitionId
     * @return null if not in store
     * @throws BuildDefinitionServiceException
     *
     */
    BuildDefinition getBuildDefinition( int buildDefinitionId )
        throws BuildDefinitionServiceException;

    /**
     * @return List<BuildDefinition> all build defintions
     * @throws BuildDefinitionServiceException
     *
     */
    List<BuildDefinition> getAllBuildDefinitions()
        throws BuildDefinitionServiceException;

    BuildDefinition addBuildDefinition( BuildDefinition buildDefinition )
        throws BuildDefinitionServiceException;

    void removeBuildDefinition( BuildDefinition buildDefinition )
        throws BuildDefinitionServiceException;

    void updateBuildDefinition( BuildDefinition buildDefinition )
        throws BuildDefinitionServiceException;

    List<BuildDefinition> getAllTemplates()
        throws BuildDefinitionServiceException;

    /**
     * @param buildDefinition
     * @return clone of {@link BuildDefinition} template/continuumDefault set to false
     */
    BuildDefinition cloneBuildDefinition( BuildDefinition buildDefinition );

    boolean isBuildDefinitionInUse( BuildDefinition buildDefinition )
        throws BuildDefinitionServiceException;

    // ------------------------------------------------------
    //  BuildDefinitionTemplate
    // ------------------------------------------------------

    void addTemplateInProject( int buildDefinitionTemplateId, Project project )
        throws BuildDefinitionServiceException;


    List<BuildDefinitionTemplate> getAllBuildDefinitionTemplate()
        throws BuildDefinitionServiceException;

    BuildDefinitionTemplate getBuildDefinitionTemplate( int id )
        throws BuildDefinitionServiceException;

    BuildDefinitionTemplate addBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws BuildDefinitionServiceException;

    BuildDefinitionTemplate updateBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws BuildDefinitionServiceException;

    void removeBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws BuildDefinitionServiceException;

    public BuildDefinitionTemplate addBuildDefinitionInTemplate( BuildDefinitionTemplate buildDefinitionTemplate,
                                                                 BuildDefinition buildDefinition, boolean template )
        throws BuildDefinitionServiceException;

    BuildDefinitionTemplate removeBuildDefinitionFromTemplate( BuildDefinitionTemplate buildDefinitionTemplate,
                                                               BuildDefinition buildDefinition )
        throws BuildDefinitionServiceException;

    public BuildDefinitionTemplate getDefaultAntBuildDefinitionTemplate()
        throws BuildDefinitionServiceException;

    public BuildDefinitionTemplate getDefaultMavenOneBuildDefinitionTemplate()
        throws BuildDefinitionServiceException;

    public BuildDefinitionTemplate getDefaultMavenTwoBuildDefinitionTemplate()
        throws BuildDefinitionServiceException;

    public BuildDefinitionTemplate getDefaultShellBuildDefinitionTemplate()
        throws BuildDefinitionServiceException;

    public BuildDefinitionTemplate getContinuumDefaultWithType( String type )
        throws BuildDefinitionServiceException;

    public List<BuildDefinitionTemplate> getBuildDefinitionTemplatesWithType( String type )
        throws BuildDefinitionServiceException;

    public ProjectGroup addBuildDefinitionTemplateToProjectGroup( int projectGroupId,
                                                                  BuildDefinitionTemplate buildDefinitionTemplate )
        throws BuildDefinitionServiceException, ContinuumObjectNotFoundException;

    public List<BuildDefinitionTemplate> getContinuumBuildDefinitionTemplates()
        throws BuildDefinitionServiceException;
}
