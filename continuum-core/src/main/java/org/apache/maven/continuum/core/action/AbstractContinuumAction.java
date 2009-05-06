package org.apache.maven.continuum.core.action;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.codehaus.plexus.action.AbstractAction;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractContinuumAction
    extends AbstractAction
{
    // ----------------------------------------------------------------------
    // Keys for the values that can be in the context
    // ----------------------------------------------------------------------

    public static final String KEY_PROJECT_ID = "project-id";

    public static final String KEY_PROJECT = "project";

    public static final String KEY_PROJECTS = "projects";

    public static final String KEY_PROJECTS_BUILD_DEFINITIONS_MAP = "projects-build-definitions";

    public static final String KEY_BUILD_DEFINITION_TEMPLATE = "build-definition-template";

    public static final String KEY_BUILD_DEFINITION = "build-definition";

    public static final String KEY_BUILD_DEFINITION_ID = "build-definition-id";

    public static final String KEY_UNVALIDATED_PROJECT = "unvalidated-project";

    public static final String KEY_PROJECT_GROUP_ID = "project-group-id";

    public static final String KEY_UNVALIDATED_PROJECT_GROUP = "unvalidated-project-group";

    public static final String KEY_BUILD_ID = "build-id";

    public static final String KEY_WORKING_DIRECTORY = "working-directory";

    public static final String KEY_WORKING_DIRECTORY_EXISTS = "working-directory-exists";

    public static final String KEY_CHECKOUT_SCM_RESULT = "checkout-result";

    public static final String KEY_UPDATE_SCM_RESULT = "update-result";

    public static final String KEY_UPDATE_DEPENDENCIES = "update-dependencies";

    public static final String KEY_TRIGGER = "trigger";

    public static final String KEY_FIRST_RUN = "first-run";

    public static final String KEY_PROJECT_RELATIVE_PATH = "project-relative-path";

    public static final String KEY_SCM_USE_CREDENTIALS_CACHE = "useCredentialsCache";

    public static final String KEY_SCM_USERNAME = "scmUserName";

    public static final String KEY_SCM_PASSWORD = "scmUserPassword";

    public static final String KEY_SCM_RESULT = "scmResult";

    public static final String KEY_OLD_SCM_RESULT = "old-scmResult";

    public static final String KEY_PROJECT_SCM_ROOT = "projectScmRoot";
    
    /**
     * SCM root url. Used in these actions add-project-to-checkout-queue, checkout-project, clean-working-directory,
     *      create-projects-from-metadata, update-project-from-working-directory, 
     *      update-working-directory-from-scm
     */
    public static final String KEY_PROJECT_SCM_ROOT_URL = "projectScmRootUrl";

    public static final String KEY_OLD_BUILD_ID = "old-buildResult-id";

    public static final String KEY_CANCELLED = "cancelled";

    public static final String KEY_SCM_RESULT_MAP = "scm-result-map";
    
    /**
     * Metadata url for adding projects.
     */
    public static final String KEY_URL = "url";
    
    /**
     * List of projects in a project group with a common scm root url.
     */
    public static final String KEY_PROJECTS_IN_GROUP_WITH_COMMON_SCM_ROOT = "projects-in-group-with-common-scm-root";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static int getProjectId( Map<String, Object> context )
    {
        return getInteger( context, KEY_PROJECT_ID );
    }

    public static Project getProject( Map<String, Object> context )
    {
        return (Project) getObject( context, KEY_PROJECT );
    }

    public static int getProjectGroupId( Map<String, Object> context )
    {
        return getInteger( context, KEY_PROJECT_GROUP_ID );
    }

    public static BuildDefinitionTemplate getBuildDefinitionTemplate( Map<String, Object> context )
    {
        return (BuildDefinitionTemplate) getObject( context, KEY_BUILD_DEFINITION_TEMPLATE, null );
    }

    public static BuildDefinition getBuildDefinition( Map<String, Object> context )
    {
        return (BuildDefinition) getObject( context, KEY_BUILD_DEFINITION, null );
    }

    public static int getBuildDefinitionId( Map<String, Object> context )
    {
        return getInteger( context, KEY_BUILD_DEFINITION_ID );
    }

    public static String getBuildId( Map<String, Object> context )
    {
        return getString( context, KEY_BUILD_ID );
    }

    public static int getTrigger( Map<String, Object> context )
    {
        return getInteger( context, KEY_TRIGGER );
    }

    public static Project getUnvalidatedProject( Map<String, Object> context )
    {
        return (Project) getObject( context, KEY_UNVALIDATED_PROJECT );
    }

    public static ProjectGroup getUnvalidatedProjectGroup( Map<String, Object> context )
    {
        return (ProjectGroup) getObject( context, KEY_UNVALIDATED_PROJECT_GROUP );
    }

    public static File getWorkingDirectory( Map<String, Object> context )
    {
        return new File( getString( context, KEY_WORKING_DIRECTORY ) );
    }

    public static ScmResult getCheckoutResult( Map<String, Object> context, Object defaultValue )
    {
        return (ScmResult) getObject( context, KEY_CHECKOUT_SCM_RESULT, defaultValue );
    }

    public static ScmResult getUpdateScmResult( Map<String, Object> context )
    {
        return getUpdateScmResult( context, null );
    }

    public static ScmResult getUpdateScmResult( Map<String, Object> context, ScmResult defaultValue )
    {
        return (ScmResult) getObject( context, KEY_UPDATE_SCM_RESULT, defaultValue );
    }

    public static List<ProjectDependency> getUpdatedDependencies( Map<String, Object> context )
    {
        return getUpdatedDependencies( context, null );
    }

    public static List<ProjectDependency> getUpdatedDependencies( Map<String, Object> context,
                                                                  List<ProjectDependency> defaultValue )
    {
        return (List<ProjectDependency>) getObject( context, KEY_UPDATE_DEPENDENCIES, defaultValue );
    }

    public static ScmResult getScmResult( Map<String, Object> context )
    {
        return getScmResult( context, null );
    }

    public static ScmResult getScmResult( Map<String, Object> context, ScmResult defaultValue )
    {
        return (ScmResult) getObject( context, KEY_SCM_RESULT, defaultValue );
    }

    public static ScmResult getOldScmResult( Map<String, Object> context )
    {
        return getOldScmResult( context, null );
    }

    public static ScmResult getOldScmResult( Map<String, Object> context, ScmResult defaultValue )
    {
        return (ScmResult) getObject( context, KEY_OLD_SCM_RESULT, defaultValue );
    }

    public static ProjectScmRoot getProjectScmRoot( Map<String, Object> context )
    {
        return (ProjectScmRoot) getObject( context, KEY_PROJECT_SCM_ROOT );
    }

    public static int getOldBuildId( Map<String, Object> context )
    {
        return getInteger( context, KEY_OLD_BUILD_ID );
    }

    public static List<Project> getListOfProjects( Map<String, Object> context )
    {
        return (List<Project>) getObject( context, KEY_PROJECTS );
    }

    public static Map<Integer, BuildDefinition> getProjectsBuildDefinitionsMap( Map<String, Object> context )
    {
        return (Map<Integer, BuildDefinition>) getObject( context, KEY_PROJECTS_BUILD_DEFINITIONS_MAP );
    }

    public static Map<Integer, ScmResult> getScmResultMap( Map<String, Object> context )
    {
        return (Map<Integer, ScmResult>) getObject( context, KEY_SCM_RESULT_MAP );
    }
    
    public static List<Project> getListOfProjectsInGroupWithCommonScmRoot( Map<String, Object> context )
    {
        return (List<Project>) getObject( context, KEY_PROJECTS_IN_GROUP_WITH_COMMON_SCM_ROOT, new ArrayList<Integer>() );
    }
    
    
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static String getString( Map<String, Object> context, String key )
    {
        return (String) getObject( context, key );
    }

    public static String getString( Map<String, Object> context, String key, String defaultValue )
    {
        return (String) getObject( context, key, defaultValue );
    }

    public static boolean getBoolean( Map<String, Object> context, String key )
    {
        return (Boolean) getObject( context, key );
    }

    public static boolean getBoolean( Map<String, Object> context, String key, boolean defaultValue )
    {
        return (Boolean) getObject( context, key, defaultValue );
    }

    protected static int getInteger( Map<String, Object> context, String key )
    {
        Object obj = getObject( context, key, null );

        if ( obj == null )
        {
            return 0;
        }
        else
        {
            return (Integer) obj;
        }
    }

    protected static Object getObject( Map<String, Object> context, String key )
    {
        if ( !context.containsKey( key ) )
        {
            throw new RuntimeException( "Missing key '" + key + "'." );
        }

        Object value = context.get( key );

        if ( value == null )
        {
            throw new RuntimeException( "Missing value for key '" + key + "'." );
        }

        return value;
    }

    protected static Object getObject( Map<String, Object> context, String key, Object defaultValue )
    {
        Object value = context.get( key );

        if ( value == null )
        {
            return defaultValue;
        }

        return value;
    }
}
