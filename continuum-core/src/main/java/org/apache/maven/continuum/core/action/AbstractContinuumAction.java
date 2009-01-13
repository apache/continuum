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
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.codehaus.plexus.action.AbstractAction;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

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

    public static final String KEY_OLD_BUILD_ID = "old-buildResult-id";

    public static final String KEY_CANCELLED = "cancelled";

    // ----------------------------------------------------------------------
    // Utils
    // ----------------------------------------------------------------------

    protected String nullIfEmpty( String string )
    {
        if ( StringUtils.isEmpty( string ) )
        {
            return null;
        }

        return string;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static int getProjectId( Map context )
    {
        return getInteger( context, KEY_PROJECT_ID );
    }

    public static Project getProject( Map context )
    {
        return (Project) getObject( context, KEY_PROJECT );
    }

    public static int getProjectGroupId( Map context )
    {
        return getInteger( context, KEY_PROJECT_GROUP_ID );
    }

    public static BuildDefinitionTemplate getBuildDefinitionTemplate( Map context )
    {
        return (BuildDefinitionTemplate) getObject( context, KEY_BUILD_DEFINITION_TEMPLATE, null );
    }

    public static BuildDefinition getBuildDefinition( Map context )
    {
        return (BuildDefinition) getObject( context, KEY_BUILD_DEFINITION, null );
    }

    public static int getBuildDefinitionId( Map context )
    {
        return getInteger( context, KEY_BUILD_DEFINITION_ID );
    }

    public static String getBuildId( Map context )
    {
        return getString( context, KEY_BUILD_ID );
    }

    public static int getTrigger( Map context )
    {
        return getInteger( context, KEY_TRIGGER );
    }

    public static Project getUnvalidatedProject( Map context )
    {
        return (Project) getObject( context, KEY_UNVALIDATED_PROJECT );
    }

    public static ProjectGroup getUnvalidatedProjectGroup( Map context )
    {
        return (ProjectGroup) getObject( context, KEY_UNVALIDATED_PROJECT_GROUP );
    }

    public static File getWorkingDirectory( Map context )
    {
        return new File( getString( context, KEY_WORKING_DIRECTORY ) );
    }

    public static ScmResult getCheckoutResult( Map context, Object defaultValue )
    {
        return (ScmResult) getObject( context, KEY_CHECKOUT_SCM_RESULT, defaultValue );
    }

    public static ScmResult getUpdateScmResult( Map context )
    {
        return (ScmResult) getObject( context, KEY_UPDATE_SCM_RESULT );
    }

    public static ScmResult getUpdateScmResult( Map context, ScmResult defaultValue )
    {
        return (ScmResult) getObject( context, KEY_UPDATE_SCM_RESULT, defaultValue );
    }

    public static List getUpdatedDependencies( Map context )
    {
        return getUpdatedDependencies( context, null );
    }

    public static List getUpdatedDependencies( Map context, List defaultValue )
    {
        return (List) getObject( context, KEY_UPDATE_DEPENDENCIES, defaultValue );
    }

    public static ScmResult getScmResult( Map context )
    {
        return (ScmResult) getObject( context, KEY_SCM_RESULT );
    }

    public static ScmResult getScmResult( Map context, ScmResult defaultValue )
    {
        return (ScmResult) getObject( context, KEY_SCM_RESULT, defaultValue );
    }

    public static ScmResult getOldScmResult( Map context )
    {
        return (ScmResult) getObject( context, KEY_OLD_SCM_RESULT );
    }

    public static ScmResult getOldScmResult( Map context, ScmResult defaultValue )
    {
        return (ScmResult) getObject( context, KEY_OLD_SCM_RESULT, defaultValue );
    }

    public static ProjectScmRoot getProjectScmRoot( Map context )
    {
        return (ProjectScmRoot) getObject( context, KEY_PROJECT_SCM_ROOT );
    }

    public static int getOldBuildId( Map context )
    {
        return getInteger( context, KEY_OLD_BUILD_ID ); 
    }
    
    public static List<Project> getListOfProjects( Map context )
    {
        return (List<Project>) getObject( context, KEY_PROJECTS );
    }
    
    public static Map<Integer, BuildDefinition> getProjectsBuildDefinitionsMap( Map context )
    {
        return (Map<Integer, BuildDefinition>) getObject( context, KEY_PROJECTS_BUILD_DEFINITIONS_MAP );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected static String getString( Map context, String key )
    {
        return (String) getObject( context, key );
    }

    protected static String getString( Map context, String key, String defaultValue )
    {
        return (String) getObject( context, key, defaultValue );
    }

    public static boolean getBoolean( Map context, String key )
    {
        return ( (Boolean) getObject( context, key ) ).booleanValue();
    }
    
    public static boolean getBoolean( Map context, String key, boolean defaultValue )
    {
        return ( (Boolean) getObject( context, key, Boolean.valueOf( defaultValue ) ) ).booleanValue();
    }    

    protected static int getInteger( Map context, String key )
    {
        Object obj = getObject( context, key, null );
        
        if ( obj == null )
        {
            return 0;
        }
        else
        {
            return ( (Integer) obj ).intValue();
        }
    }

    protected static Object getObject( Map context, String key )
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

    protected static Object getObject( Map context, String key, Object defaultValue )
    {
        Object value = context.get( key );

        if ( value == null )
        {
            return defaultValue;
        }

        return value;
    }
}
