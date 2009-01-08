package org.apache.continuum.buildagent.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ScmResult;

public class ContinuumBuildAgentUtil
{
    public static final String EOL = System.getProperty( "line.separator" );

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

    public static final String KEY_PROJECT_GROUP_ID = "project-group-id";

    public static final String KEY_SCM_ROOT_ADDRESS = "scm-root-address";

    public static final String KEY_SCM_ROOT_STATE = "scm-root-state";

    public static final String KEY_CHECKOUT_SCM_RESULT = "checkout-scm-result";

    public static final String KEY_UPDATE_SCM_RESULT = "update-scm-result";

    public static final String KEY_WORKING_DIRECTORY_EXISTS = "working-directory-exists";

    public static final String KEY_PROJECT = "project";

    public static final String KEY_BUILD_DEFINITION = "build-definition";

    public static final String KEY_SCM_RESULT = "scm-result";

    public static final String KEY_WORKING_DIRECTORY = "working-directory";

    public static final String KEY_SCM_SUCCESS = "scm-success";

    public static final String KEY_SCM_ERROR = "scm-error";

    public static final String KEY_BUILD_RESULT = "build-result";

    public static final String KEY_PROJECT_NAME = "project-name";

    public static final String KEY_BUILD_OUTPUT = "build-output";

    public static final String KEY_PROJECT_STATE = "project-state";

    public static final String KEY_INSTALLATION_NAME = "installation-name";

    public static final String KEY_INSTALLATION_TYPE = "installation-type";

    public static final String KEY_INSTALLATION_VAR_NAME = "installation-var-name";

    public static final String KEY_INSTALLATION_VAR_VALUE = "installation-var-value";

    public static final String KEY_ENVIRONMENTS = "environments";

    public static final String KEY_LOCAL_REPOSITORY = "local-repository";

    public static Integer getProjectId( Map context )
    {
        return getInteger( context, KEY_PROJECT_ID );
    }

    public static String getProjectName( Map context )
    {
        return getString( context, KEY_PROJECT_NAME );
    }

    public static Integer getProjectState( Map context )
    {
        return getInteger( context, KEY_PROJECT_STATE );
    }

    public static Integer getBuildDefinitionId( Map context )
    {
        return getInteger( context, KEY_BUILD_DEFINITION_ID );
    }

    public static String getBuildFile( Map context )
    {
        return getString( context, KEY_BUILD_FILE );
    }

    public static String getExecutorId( Map context )
    {
        return getString( context, KEY_EXECUTOR_ID );
    }

    public static String getGoals( Map context )
    {
        return getString( context, KEY_GOALS );
    }

    public static String getArguments( Map context )
    {
        return getString( context, KEY_ARGUMENTS );
    }

    public static String getScmUrl( Map context )
    {
        return getString( context, KEY_SCM_URL );
    }

    public static String getScmUsername( Map context )
    {
        return getString( context, KEY_SCM_USERNAME );
    }

    public static String getScmPassword( Map context )
    {
        return getString( context, KEY_SCM_PASSWORD );
    }

    public static boolean isBuildFresh( Map context )
    {
        return getBoolean( context, KEY_BUILD_FRESH );
    }

    public static int getProjectGroupId( Map context )
    {
        return getInteger( context, KEY_PROJECT_GROUP_ID );
    }

    public static String getScmRootAddress( Map context )
    {
        return getString( context, KEY_SCM_ROOT_ADDRESS );
    }

    public static int getScmRootState( Map context )
    {
        return getInteger( context, KEY_SCM_ROOT_STATE );
    }

    public static Project getProject( Map context )
    {
        return (Project) getObject( context, KEY_PROJECT );
    }

    public static BuildDefinition getBuildDefinition( Map context )
    {
        return (BuildDefinition) getObject( context, KEY_BUILD_DEFINITION );
    }

    public static ScmResult getCheckoutScmResult( Map context, Object defaultValue )
    {
        return (ScmResult) getObject( context, KEY_CHECKOUT_SCM_RESULT, defaultValue );
    }

    public static ScmResult getUpdateScmResult( Map context, Object defaultValue )
    {
        return (ScmResult) getObject( context, KEY_UPDATE_SCM_RESULT, defaultValue );
    }

    public static ScmResult getScmResult( Map context, Object defaultValue )
    {
        return (ScmResult) getObject( context, KEY_SCM_RESULT );
    }

    public static int getTrigger( Map context )
    {
        return (Integer) getObject( context, KEY_TRIGGER );
    }

    public static BuildResult getBuildResult( Map context, Object defaultValue )
    {
        return (BuildResult) getObject( context, KEY_BUILD_RESULT );
    }

    public static Map<String, String> getEnvironments( Map context )
    {
        return (Map<String, String>) getObject( context, KEY_ENVIRONMENTS );
    }

    public static String getLocalRepository( Map context )
    {
        return (String) getObject( context, KEY_LOCAL_REPOSITORY );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static String getString( Map context, String key )
    {
        return (String) getObject( context, key );
    }

    public static String getString( Map context, String key, String defaultValue )
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

    public static int getInteger( Map context, String key )
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

    public static String throwableToString( Throwable error )
    {
        if ( error == null )
        {
            return "";
        }

        StringWriter writer = new StringWriter();

        PrintWriter printer = new PrintWriter( writer );

        error.printStackTrace( printer );

        printer.flush();

        return writer.getBuffer().toString();
    }

    public static String throwableMessagesToString( Throwable error )
    {
        if ( error == null )
        {
            return "";
        }

        StringBuffer buffer = new StringBuffer();

        buffer.append( error.getMessage() );

        error = error.getCause();

        while ( error != null )
        {
            buffer.append( EOL );

            buffer.append( error.getMessage() );

            error = error.getCause();
        }

        return buffer.toString();
    }
}