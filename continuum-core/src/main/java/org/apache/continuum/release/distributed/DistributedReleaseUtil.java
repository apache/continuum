package org.apache.continuum.release.distributed;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DistributedReleaseUtil
{
    public static final String KEY_SCM_TAG = "scm-tag";

    public static final String KEY_SCM_TAGBASE = "scm-tagbase";

    public static final String KEY_SCM_USERNAME = "scm-username";

    public static final String KEY_SCM_PASSWORD = "scm-password";

    public static final String KEY_ARGUMENTS = "arguments";

    public static final String KEY_PREPARE_GOALS = "preparation-goals";

    public static final String KEY_PERFORM_GOALS = "perform-goals";

    public static final String KEY_SCM_COMMENT_PREFIX = "scm-comment-prefix";

    public static final String KEY_AUTO_VERSION_SUBMODULES = "auto-version-submodules";

    public static final String KEY_ADD_SCHEMA = "add-schema";

    public static final String KEY_PROJECT = "project";

    public static final String KEY_PROFILE = "profile";

    public static final String KEY_PROPERTIES = "properties";

    public static final String KEY_RELEASE_VERSION = "releaseVersion";

    public static final String KEY_DEVELOPMENT_VERSION = "developmentVersion";

    public static final String KEY_PROJECT_ID = "project-id";

    public static final String KEY_GROUP_ID = "group-id";

    public static final String KEY_ARTIFACT_ID = "artifact-id";

    public static final String KEY_SCM_URL = "scm-url";

    public static final String KEY_LOCAL_REPOSITORY = "local-repository";

    public static final String KEY_USE_EDIT_MODE = "use-edit-mode";

    public static final String KEY_ENVIRONMENTS = "environments";

    public static final String KEY_START_TIME = "start-time";

    public static final String KEY_END_TIME = "end-time";

    public static final String KEY_RELEASE_RESULT_CODE = "release-result-code";

    public static final String KEY_RELEASE_OUTPUT = "release-output";

    public static final String KEY_RELEASE_STATE = "state";

    public static final String KEY_RELEASE_PHASES = "release-phases";

    public static final String KEY_COMPLETED_RELEASE_PHASES = "completed-release-phases";

    public static final String KEY_RELEASE_IN_PROGRESS = "release-in-progress";

    public static final String KEY_RELEASE_ERROR = "release-error";

    public static final String KEY_USE_RELEASE_PROFILE = "use-release-profile";

    public static final String KEY_GOALS = "goals";

    public static final String KEY_RELEASE_ID = "release-id";

    public static final String KEY_LOCAL_REPOSITORY_NAME = "repo-name";

    public static final String KEY_LOCAL_REPOSITORY_LAYOUT = "repo-layout";

    public static final String KEY_RELEASE_GOAL = "release-goal";

    public static final String KEY_BUILD_AGENT_URL = "build-agent-url";

    public static final String KEY_USERNAME = "username";

    public static String getScmTag( Map<String, Object> context, String defaultValue )
    {
        return getString( context, KEY_SCM_TAG, defaultValue );
    }

    public static String getScmTagBase( Map<String, Object> context, String defaultValue )
    {
        return getString( context, KEY_SCM_TAGBASE, defaultValue );
    }

    public static String getArguments( Map<String, Object> context, String defaultValue )
    {
        return getString( context, KEY_ARGUMENTS, defaultValue );
    }

    public static String getPrepareGoals( Map<String, Object> context, String defaultValue )
    {
        return getString( context, KEY_PREPARE_GOALS, defaultValue );
    }

    public static String getPerformGoals( Map<String, Object> context, String defaultValue )
    {
        return getString( context, KEY_PERFORM_GOALS, defaultValue );
    }

    public static String getScmCommentPrefix( Map<String, Object> context, String defaultValue )
    {
        return getString( context, KEY_SCM_COMMENT_PREFIX, defaultValue );
    }

    public static Boolean getAutoVersionSubmodules( Map<String, Object> context, boolean defaultValue )
    {
        return getBoolean( context, KEY_AUTO_VERSION_SUBMODULES, defaultValue );
    }

    public static Boolean getAddSchema( Map<String, Object> context, boolean defaultValue )
    {
        return getBoolean( context, KEY_ADD_SCHEMA, defaultValue );
    }

    public static Long getStartTime( Map<String, Object> context )
    {
        return new Long( getString( context, KEY_START_TIME ) );
    }

    public static Long getEndTime( Map<String, Object> context )
    {
        return new Long( getString( context, KEY_END_TIME ) );
    }

    public static int getReleaseResultCode( Map<String, Object> context )
    {
        return getInteger( context, KEY_RELEASE_RESULT_CODE );
    }

    public static String getReleaseOutput( Map<String, Object> context )
    {
        return getString( context, KEY_RELEASE_OUTPUT );
    }

    public static int getReleaseState( Map<String, Object> context )
    {
        return getInteger( context, KEY_RELEASE_STATE );
    }

    public static List getReleasePhases( Map<String, Object> context )
    {
        return getList( context, KEY_RELEASE_PHASES, new ArrayList() );
    }

    public static List getCompletedReleasePhases( Map<String, Object> context )
    {
        return getList( context, KEY_COMPLETED_RELEASE_PHASES, new ArrayList() );
    }

    public static String getReleaseInProgress( Map<String, Object> context )
    {
        return getString( context, KEY_RELEASE_IN_PROGRESS, "" );
    }

    public static String getReleaseError( Map<String, Object> context )
    {
        return getString( context, KEY_RELEASE_ERROR, null );
    }

    public static boolean getUseReleaseProfile( Map<String, Object> context, boolean defaultValue )
    {
        return getBoolean( context, KEY_USE_RELEASE_PROFILE, defaultValue );
    }

    public static String getGoals( Map<String, Object> context, String defaultValue )
    {
        return getString( context, KEY_GOALS, defaultValue );
    }

    public static String getReleaseId( Map<String, Object> context )
    {
        return getString( context, KEY_RELEASE_ID );
    }

    public static String getReleaseGoal( Map<String, Object> context )
    {
        return getString( context, KEY_RELEASE_GOAL );
    }

    public static String getBuildAgentUrl( Map<String, Object> context )
    {
        return getString( context, KEY_BUILD_AGENT_URL );
    }

    public static int getProjectId( Map<String, Object> context )
    {
        return getInteger( context, KEY_PROJECT_ID );
    }

    public static String getUsername( Map<String, Object> context )
    {
        return getString( context, KEY_USERNAME, "" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Integer getInteger( Map<String, Object> context, String key )
    {
        return (Integer) getObject( context, key );
    }

    public static Integer getInteger( Map<String, Object> context, String key, Object defaultValue )
    {
        return (Integer) getObject( context, key, defaultValue );
    }

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

    public static List getList( Map<String, Object> context, String key, Object defaultValue )
    {
        Object obj = getObject( context, key, defaultValue );

        if ( obj == null )
        {
            return null;
        }
        else
        {
            List<Object> list = new ArrayList<Object>();

            if ( obj instanceof Object[] )
            {
                Object[] objA = (Object[]) obj;

                list.addAll( Arrays.asList( objA ) );
            }
            else
            {
                list = (List<Object>) obj;
            }

            return list;
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