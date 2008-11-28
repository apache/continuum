package org.apache.continuum.distributed.manager;

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

import java.util.Map;

import org.apache.continuum.distributed.manager.DistributedBuildManager;

public abstract class AbstractDistributedBuildManager
    implements DistributedBuildManager
{
    public static final String KEY_PROJECT_ID = "project-id";

    public static final String KEY_BUILD_DEFINITION_ID = "builddefinition-id";

    public static final String KEY_TRIGGER = "trigger";

    public static final String KEY_EXECUTOR_ID = "executor-id";

    public static final String KEY_SCM_URL = "scm-url";

    public static final String KEY_SCM_USERNAME = "scm-username";

    public static final String KEY_SCM_PASSWORD = "scm-password";

    public static final String KEY_BUILD_FILE = "build-file";

    public static final String KEY_GOALS = "goals";

    public static final String KEY_ARGUMENTS = "arguments";

    public static final String KEY_BUILD_FRESH = "build-fresh";

    public static final String KEY_BUILD_START = "build-start";

    public static final String KEY_BUILD_END = "build-end";

    public static final String KEY_BUILD_ERROR = "build-error";

    public static final String KEY_BUILD_EXIT_CODE = "build-exit-code";

    public static final String KEY_BUILD_STATE = "build-state";

    public static final String KEY_SCM_STATE = "scm-state";

    public static final String KEY_SCM_COMMAND_OUTPUT = "scm-command-output";

    public static final String KEY_SCM_COMMAND_LINE = "scm-command-line";

    public static final String KEY_SCM_PROVIDER_MESSAGE = "scm-provider-message";

    public static final String KEY_SCM_EXCEPTION = "scm-exception";

    public static final String KEY_PREPARE_BUILD_FINISHED = "prepare-build-finished";

    public static final String KEY_AGENT_URL = "agent-url";

    public static int getProjectId( Map context )
    {
        return getInteger( context, KEY_PROJECT_ID );
    }

    public static int getBuildDefinitionId( Map context )
    {
        return getInteger( context, KEY_BUILD_DEFINITION_ID );
    }

    public static String getBuildError( Map context )
    {
        return getString( context, KEY_BUILD_ERROR );
    }

    public static int getTrigger( Map context )
    {
        return getInteger( context, KEY_TRIGGER );
    }

    public static long getBuildStart( Map context )
    {
        return getLong( context, KEY_BUILD_START );
    }

    public static long getBuildEnd( Map context )
    {
        return getLong( context, KEY_BUILD_END );
    }

    public static int getBuildExitCode( Map context )
    {
        return getInteger( context, KEY_BUILD_EXIT_CODE );
    }

    public static int getBuildState( Map context )
    {
        return getInteger( context, KEY_BUILD_STATE );
    }

    public static String getScmCommandLine( Map context )
    {
        return getString( context, KEY_SCM_COMMAND_LINE );
    }

    public static String getScmCommandOutput( Map context )
    {
        return getString( context, KEY_SCM_COMMAND_OUTPUT );
    }

    public static String getScmException( Map context )
    {
        return getString( context, KEY_SCM_EXCEPTION );
    }

    public static String getScmProviderMessage( Map context )
    {
        return getString( context, KEY_SCM_PROVIDER_MESSAGE );
    }

    public static boolean isPrepareBuildFinished( Map context )
    {
        return getBoolean( context, KEY_PREPARE_BUILD_FINISHED );
    }

    public static String getAgentUrl( Map context )
    {
        return getString( context, KEY_AGENT_URL );
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

    protected static boolean getBoolean( Map context, String key )
    {
        return ( (Boolean) getObject( context, key ) ).booleanValue();
    }
    
    protected static boolean getBoolean( Map context, String key, boolean defaultValue )
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

    protected static long getLong( Map context, String key )
    {
        Object obj = getObject( context, key, null );
        
        if ( obj == null )
        {
            return 0;
        }
        else
        {
            return ( (Long) obj ).longValue();
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
