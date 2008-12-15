package org.apache.continuum.builder.utils;

import java.util.Map;

public class ContinuumBuildConstant
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

    public static final String KEY_SCM_COMMAND_OUTPUT = "scm-command-output";

    public static final String KEY_SCM_COMMAND_LINE = "scm-command-line";

    public static final String KEY_SCM_PROVIDER_MESSAGE = "scm-provider-message";

    public static final String KEY_SCM_EXCEPTION = "scm-exception";

    public static final String KEY_SCM_SUCCESS = "scm-success";

    public static final String KEY_PROJECT_GROUP_ID = "project-group-id";

    public static final String KEY_SCM_ROOT_ADDRESS = "scm-root-address";

    public static final String KEY_SCM_ERROR = "scm-error";

    public static final String KEY_PROJECT_NAME = "project-name";

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

    public static boolean isScmSuccess( Map context )
    {
        return getBoolean( context, KEY_SCM_SUCCESS );
    }

    public static int getProjectGroupId( Map context )
    {
        return getInteger( context, KEY_PROJECT_GROUP_ID );
    }

    public static String getScmRootAddress( Map context )
    {
        return getString( context, KEY_SCM_ROOT_ADDRESS );
    }

    public static String getScmError( Map context )
    {
        return getString( context, KEY_SCM_ERROR );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected static String getString( Map context, String key )
    {
        Object obj = getObject( context, key, null );

        if ( obj == null )
        {
            return null;
        }
        else
        {
            return (String) obj;
        }
    }

    protected static String getString( Map context, String key, String defaultValue )
    {
        return (String) getObject( context, key, defaultValue );
    }

    protected static boolean getBoolean( Map context, String key )
    {
        Object obj = getObject( context, key, null );
        
        if ( obj == null )
        {
            return false;
        }
        else
        {
            return ( (Boolean) obj ).booleanValue();
        }
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
