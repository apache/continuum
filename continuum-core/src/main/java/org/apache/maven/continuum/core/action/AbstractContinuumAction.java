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

import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.codehaus.plexus.action.AbstractAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public abstract class AbstractContinuumAction
    extends AbstractAction
{
    // ----------------------------------------------------------------------
    // Keys for the values that can be in the context
    // ----------------------------------------------------------------------

    private static final String KEY_PROJECT_ID = "project-id";

    private static final String KEY_PROJECT = "project";

    private static final String KEY_PROJECTS = "projects";

    private static final String KEY_PROJECTS_BUILD_DEFINITIONS_MAP = "projects-build-definitions";

    private static final String KEY_BUILD_DEFINITION_TEMPLATE = "build-definition-template";

    private static final String KEY_BUILD_DEFINITION = "build-definition";

    private static final String KEY_BUILD_DEFINITION_ID = "build-definition-id";

    private static final String KEY_UNVALIDATED_PROJECT = "unvalidated-project";

    private static final String KEY_PROJECT_GROUP_ID = "project-group-id";

    private static final String KEY_UNVALIDATED_PROJECT_GROUP = "unvalidated-project-group";

    private static final String KEY_BUILD_ID = "build-id";

    private static final String KEY_WORKING_DIRECTORY = "working-directory";

    private static final String KEY_UPDATE_DEPENDENCIES = "update-dependencies";

    private static final String KEY_BUILD_TRIGGER = "buildTrigger";

    private static final String KEY_SCM_RESULT = "scmResult";

    private static final String KEY_OLD_SCM_RESULT = "old-scmResult";

    private static final String KEY_PROJECT_SCM_ROOT = "projectScmRoot";

    /**
     * SCM root url. Used in these actions add-project-to-checkout-queue, checkout-project, clean-working-directory,
     * create-projects-from-metadata, update-project-from-working-directory,
     * update-working-directory-from-scm
     */
    private static final String KEY_PROJECT_SCM_ROOT_URL = "projectScmRootUrl";

    /**
     * List of projects in a project group with a common scm root url.
     */
    private static final String KEY_PROJECTS_IN_GROUP_WITH_COMMON_SCM_ROOT = "projects-in-group-with-common-scm-root";

    private static final String KEY_OLD_BUILD_ID = "old-buildResult-id";

    private static final String KEY_SCM_RESULT_MAP = "scm-result-map";

    private static final String KEY_ROOT_DIRECTORY = "root-directory";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static int getProjectId( Map<String, Object> context )
    {
        return getInteger( context, KEY_PROJECT_ID );
    }

    public static void setProjectId( Map<String, Object> context, int projectId )
    {
        context.put( KEY_PROJECT_ID, projectId );
    }

    public static Project getProject( Map<String, Object> context )
    {
        return (Project) getObject( context, KEY_PROJECT );
    }

    public static Project getProject( Map<String, Object> context, Project defaultValue )
    {
        return (Project) getObject( context, KEY_PROJECT, defaultValue );
    }

    public static void setProject( Map<String, Object> context, Project p )
    {
        context.put( KEY_PROJECT, p );
    }

    public static int getProjectGroupId( Map<String, Object> context )
    {
        return getInteger( context, KEY_PROJECT_GROUP_ID );
    }

    public static void setProjectGroupId( Map<String, Object> context, int projectGroupId )
    {
        context.put( KEY_PROJECT_GROUP_ID, projectGroupId );
    }

    public static BuildDefinitionTemplate getBuildDefinitionTemplate( Map<String, Object> context )
    {
        return (BuildDefinitionTemplate) getObject( context, KEY_BUILD_DEFINITION_TEMPLATE, null );
    }

    public static void setBuildDefinitionTemplate( Map<String, Object> context, BuildDefinitionTemplate bdt )
    {
        context.put( KEY_BUILD_DEFINITION_TEMPLATE, bdt );
    }

    public static BuildDefinition getBuildDefinition( Map<String, Object> context )
    {
        return (BuildDefinition) getObject( context, KEY_BUILD_DEFINITION, null );
    }

    public static void setBuildDefinition( Map<String, Object> context, BuildDefinition bd )
    {
        context.put( KEY_BUILD_DEFINITION, bd );
    }

    public static int getBuildDefinitionId( Map<String, Object> context )
    {
        return getInteger( context, KEY_BUILD_DEFINITION_ID );
    }

    public static void setBuildDefinitionId( Map<String, Object> context, int buildDefintionId )
    {
        context.put( KEY_BUILD_DEFINITION_ID, buildDefintionId );
    }

    public static String getBuildId( Map<String, Object> context )
    {
        return getString( context, KEY_BUILD_ID );
    }

    public static String getBuildId( Map<String, Object> context, String defaultValue )
    {
        return getString( context, KEY_BUILD_ID, defaultValue );
    }

    public static void setBuildId( Map<String, Object> context, String buildId )
    {
        context.put( KEY_BUILD_ID, buildId );
    }

    public static BuildTrigger getBuildTrigger( Map<String, Object> context )
    {
        BuildTrigger defaultValue = new BuildTrigger( 0, "" );
        return (BuildTrigger) getObject( context, KEY_BUILD_TRIGGER, defaultValue );
    }

    public static void setBuildTrigger( Map<String, Object> context, BuildTrigger buildTrigger )
    {
        context.put( KEY_BUILD_TRIGGER, buildTrigger );
    }

    public static Project getUnvalidatedProject( Map<String, Object> context )
    {
        return (Project) getObject( context, KEY_UNVALIDATED_PROJECT );
    }

    public static void setUnvalidatedProject( Map<String, Object> context, Project p )
    {
        context.put( KEY_UNVALIDATED_PROJECT, p );
    }

    public static ProjectGroup getUnvalidatedProjectGroup( Map<String, Object> context )
    {
        return (ProjectGroup) getObject( context, KEY_UNVALIDATED_PROJECT_GROUP );
    }

    public static void setUnvalidatedProjectGroup( Map<String, Object> context, ProjectGroup pg )
    {
        context.put( KEY_UNVALIDATED_PROJECT_GROUP, pg );
    }

    public static File getWorkingDirectory( Map<String, Object> context )
    {
        return new File( getString( context, KEY_WORKING_DIRECTORY ) );
    }

    public static void setWorkingDirectory( Map<String, Object> context, String workingDirectory )
    {
        context.put( KEY_WORKING_DIRECTORY, workingDirectory );
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

    public static void setUpdatedDependencies( Map<String, Object> context, List<ProjectDependency> dependencies )
    {
        context.put( KEY_UPDATE_DEPENDENCIES, dependencies );
    }

    public static ScmResult getScmResult( Map<String, Object> context )
    {
        return getScmResult( context, null );
    }

    public static ScmResult getScmResult( Map<String, Object> context, ScmResult defaultValue )
    {
        return (ScmResult) getObject( context, KEY_SCM_RESULT, defaultValue );
    }

    public static void setScmResult( Map<String, Object> context, ScmResult scmResult )
    {
        context.put( KEY_SCM_RESULT, scmResult );
    }

    public static ScmResult getOldScmResult( Map<String, Object> context )
    {
        return getOldScmResult( context, null );
    }

    public static ScmResult getOldScmResult( Map<String, Object> context, ScmResult defaultValue )
    {
        return (ScmResult) getObject( context, KEY_OLD_SCM_RESULT, defaultValue );
    }

    public static void setOldScmResult( Map<String, Object> context, ScmResult oldScmResult )
    {
        context.put( KEY_OLD_SCM_RESULT, oldScmResult );
    }

    public static ProjectScmRoot getProjectScmRoot( Map<String, Object> context )
    {
        return (ProjectScmRoot) getObject( context, KEY_PROJECT_SCM_ROOT );
    }

    public static void setProjectScmRoot( Map<String, Object> context, ProjectScmRoot projectScmRoot )
    {
        context.put( KEY_PROJECT_SCM_ROOT, projectScmRoot );
    }

    public static int getOldBuildId( Map<String, Object> context )
    {
        return getInteger( context, KEY_OLD_BUILD_ID );
    }

    public static void setOldBuildId( Map<String, Object> context, int oldBuildId )
    {
        context.put( KEY_OLD_BUILD_ID, oldBuildId );
    }

    public static List<Project> getListOfProjects( Map<String, Object> context )
    {
        return (List<Project>) getObject( context, KEY_PROJECTS );
    }

    public static void setListOfProjects( Map<String, Object> context, List<Project> projects )
    {
        context.put( KEY_PROJECTS, projects );
    }

    public static List<Project> getListOfProjectsInGroupWithCommonScmRoot( Map<String, Object> context )
    {
        return (List<Project>) getObject( context, KEY_PROJECTS_IN_GROUP_WITH_COMMON_SCM_ROOT,
                                          new ArrayList<Integer>() );
    }

    public static void setListOfProjectsInGroupWithCommonScmRoot( Map<String, Object> context, List<Project> projects )
    {
        context.put( KEY_PROJECTS_IN_GROUP_WITH_COMMON_SCM_ROOT, projects );
    }

    public static Map<Integer, BuildDefinition> getProjectsBuildDefinitionsMap( Map<String, Object> context )
    {
        return (Map<Integer, BuildDefinition>) getObject( context, KEY_PROJECTS_BUILD_DEFINITIONS_MAP );
    }

    public static void setProjectsBuildDefinitionsMap( Map<String, Object> context,
                                                       Map<Integer, BuildDefinition> projectsBuildDefinitionsMap )
    {
        context.put( KEY_PROJECTS_BUILD_DEFINITIONS_MAP, projectsBuildDefinitionsMap );
    }

    public static Map<Integer, ScmResult> getScmResultMap( Map<String, Object> context )
    {
        return (Map<Integer, ScmResult>) getObject( context, KEY_SCM_RESULT_MAP );
    }

    public static void setScmResultMap( Map<String, Object> context, Map<Integer, ScmResult> scmResultMap )
    {
        context.put( KEY_SCM_RESULT_MAP, scmResultMap );
    }

    public static boolean isRootDirectory( Map<String, Object> context )
    {
        return getBoolean( context, KEY_ROOT_DIRECTORY, true );
    }

    public static void setRootDirectory( Map<String, Object> context, boolean isRootDirectory )
    {
        context.put( KEY_ROOT_DIRECTORY, isRootDirectory );
    }

    public static String getProjectScmRootUrl( Map<String, Object> context, String projectScmRootUrl )
    {
        return getString( context, KEY_PROJECT_SCM_ROOT_URL, projectScmRootUrl );
    }

    public static void setProjectScmRootUrl( Map<String, Object> context, String projectScmRootUrl )
    {
        context.put( KEY_PROJECT_SCM_ROOT_URL, projectScmRootUrl );
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
