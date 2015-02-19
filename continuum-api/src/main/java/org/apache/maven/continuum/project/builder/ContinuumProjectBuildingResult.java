package org.apache.maven.continuum.project.builder;

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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holder for results of adding projects to Continuum. Contains added projects, project groups
 * and errors that happened during the add.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 */
public class ContinuumProjectBuildingResult
{
    public static final String ERROR_MALFORMED_URL = "add.project.malformed.url.error";

    public static final String ERROR_UNKNOWN_HOST = "add.project.unknown.host.error";

    public static final String ERROR_CONNECT = "add.project.connect.error";

    public static final String ERROR_XML_PARSE = "add.project.xml.parse.error";

    public static final String ERROR_EXTEND = "add.project.extend.error";

    public static final String ERROR_MISSING_GROUPID = "add.project.missing.groupid.error";

    public static final String ERROR_MISSING_ARTIFACTID = "add.project.missing.artifactid.error";

    public static final String ERROR_POM_NOT_FOUND = "add.project.missing.pom.error";

    public static final String ERROR_MISSING_VERSION = "add.project.missing.version.error";

    public static final String ERROR_MISSING_NAME = "add.project.missing.name.error";

    public static final String ERROR_MISSING_REPOSITORY = "add.project.missing.repository.error";

    public static final String ERROR_MISSING_SCM = "add.project.missing.scm.error";

    public static final String ERROR_MISSING_SCM_CONNECTION = "add.project.missing.scm.connection.error";

    public static final String ERROR_MISSING_NOTIFIER_TYPE = "add.project.missing.notifier.type.error";

    public static final String ERROR_MISSING_NOTIFIER_CONFIGURATION =
        "add.project.missing.notifier.configuration.error";

    public static final String ERROR_METADATA_TRANSFER = "add.project.metadata.transfer.error";

    public static final String ERROR_VALIDATION = "add.project.validation.error";

    public static final String ERROR_UNAUTHORIZED = "add.project.unauthorized.error";

    public static final String ERROR_PROTOCOL_NOT_ALLOWED = "add.project.validation.protocol.not_allowed";

    public static final String ERROR_ARTIFACT_NOT_FOUND = "add.project.artifact.not.found.error";

    public static final String ERROR_PROJECT_BUILDING = "add.project.project.building.error";

    public static final String ERROR_UNKNOWN = "add.project.unknown.error";

    public static final String ERROR_DUPLICATE_PROJECTS = "add.project.duplicate.error";

    private final List<Project> projects = new ArrayList<Project>();

    private final List<ProjectGroup> projectGroups = new ArrayList<ProjectGroup>();

    private final Map<String, String> errors = new HashMap<String, String>();

    private static final String LS = System.getProperty( "line.separator" );

    private Project rootProject;

    public void addProject( Project project )
    {
        projects.add( project );
    }

    public void addProjectGroup( ProjectGroup projectGroup )
    {
        projectGroups.add( projectGroup );
    }

    public void addProject( Project project, String executorId )
    {
        project.setExecutorId( executorId );

        projects.add( project );
    }

    public List<Project> getProjects()
    {
        return projects;
    }

    public List<ProjectGroup> getProjectGroups()
    {
        return projectGroups;
    }

    /**
     * Add a warning that happened during adding the project to Continuum.
     *
     * @param warningKey warning id (so it can be internationalized later)
     * @deprecated Use {@link #addError(String)} instead
     */
    public void addWarning( String warningKey )
    {
        addError( warningKey );
    }

    /**
     * Add an error that happened during adding the project to Continuum.
     *
     * @param errorKey error id (so it can be internationalized later)
     */
    public void addError( String errorKey )
    {
        errors.put( errorKey, "" );
    }

    /**
     * Add an error that happened during adding the project to Continuum.
     *
     * @param errorKey error id (so it can be internationalized later)
     */
    public void addError( String errorKey, Object param )
    {
        errors.put( errorKey, param == null ? "" : param.toString() );
    }

    /**
     * Add an error that happened during adding the project to Continuum.
     *
     * @param errorKey error id (so it can be internationalized later)
     */
    public void addError( String errorKey, Object params[] )
    {
        if ( params != null )
        {
            errors.put( errorKey, Arrays.asList( params ).toString() );
        }
    }

    /**
     * Get the warnings that happened during adding the project to Continuum.
     * There is an entry with the warning key (so it can be internationalized later) for each warning.
     *
     * @return {@link List} &lt; {@link String} >
     * @deprecated Use {@link #getErrors()} instead
     */
    public List<String> getWarnings()
    {
        return getErrors();
    }

    /**
     * Get the errors that happened during adding the project to Continuum.
     * There is an entry with the error key (so it can be internationalized later) for each error.
     *
     * @return {@link List} &lt; {@link String} >
     */
    public List<String> getErrors()
    {
        return new ArrayList<String>( errors.keySet() );
    }

    public Map<String, String> getErrorsWithCause()
    {
        return errors;
    }

    /**
     * Quick check to see if there are any errors.
     *
     * @return boolean indicating if there are any errors.
     */
    public boolean hasErrors()
    {
        return ( errors != null ) && ( !errors.isEmpty() );
    }

    /**
     * Returns a string representation of the errors.
     *
     * @return a string representation of the errors.
     */
    public String getErrorsAsString()
    {
        if ( !hasErrors() )
        {
            return null;
        }

        StringBuilder message = new StringBuilder();
        for ( String key : errors.keySet() )
        {
            message.append( errors.get( key ) );
            message.append( LS );
        }
        return message.toString();
    }

    public Project getRootProject()
    {
        return rootProject;
    }

    public void setRootProject( Project rootProject )
    {
        this.rootProject = rootProject;
    }
}
