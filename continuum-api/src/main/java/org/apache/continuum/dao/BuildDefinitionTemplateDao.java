package org.apache.continuum.dao;

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

import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.store.ContinuumStoreException;

import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
public interface BuildDefinitionTemplateDao
{
    List<BuildDefinitionTemplate> getAllBuildDefinitionTemplate()
        throws ContinuumStoreException;

    BuildDefinitionTemplate getBuildDefinitionTemplate( int id )
        throws ContinuumStoreException;

    BuildDefinitionTemplate addBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws ContinuumStoreException;

    BuildDefinitionTemplate updateBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws ContinuumStoreException;

    void removeBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws ContinuumStoreException;

    public List<BuildDefinitionTemplate> getBuildDefinitionTemplatesWithType( String type )
        throws ContinuumStoreException;

    public List<BuildDefinitionTemplate> getContinuumBuildDefinitionTemplates()
        throws ContinuumStoreException;

    /**
     * @param type
     * @return BuildDefinitionTemplate null if not found
     * @throws ContinuumStoreException
     */
    BuildDefinitionTemplate getContinuumBuildDefinitionTemplateWithType( String type )
        throws ContinuumStoreException;

    /**
     * the list returned will contains only continuumDefaults {@link org.apache.maven.continuum.model.project.BuildDefinition}
     *
     * @return List<BuildDefinitionTemplate>
     * @throws ContinuumStoreException
     */
    List<BuildDefinitionTemplate> getContinuumDefaultdDefinitions()
        throws ContinuumStoreException;
}
