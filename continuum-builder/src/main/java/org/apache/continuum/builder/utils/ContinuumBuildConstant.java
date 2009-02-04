package org.apache.continuum.builder.utils;

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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.maven.continuum.model.project.BuildResult;

public class ContinuumBuildConstant
{
    public static final String KEY_PROJECT_ID = "project-id";

    public static final String KEY_PROJECT_VERSION = "project-version";

    public static final String KEY_PROJECT_BUILD_NUMBER = "build-number";

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

    public static final String KEY_ALWAYS_BUILD = "always-build";

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

    public static final String KEY_PROJECT_GROUP_NAME = "project-group-name";
    
    public static final String KEY_SCM_ROOT_ADDRESS = "scm-root-address";

    public static final String KEY_SCM_ERROR = "scm-error";

    public static final String KEY_PROJECT_NAME = "project-name";

    public static final String KEY_BUILD_OUTPUT = "build-output";

    public static final String KEY_BUILD_RESULT = "build-result";

    public static final String KEY_PROJECT_STATE = "project-state";

    public static final String KEY_INSTALLATION_NAME = "installation-name";

    public static final String KEY_INSTALLATION_TYPE = "installation-type";

    public static final String KEY_INSTALLATION_VAR_NAME = "installation-var-name";

    public static final String KEY_INSTALLATION_VAR_VALUE = "installation-var-value";

    public static final String KEY_LOCAL_REPOSITORY = "local-repository";

    public static final String KEY_SCM_CHANGES = "scm-changes";

    public static final String KEY_CHANGESET_ID = "changeset-id";

    public static final String KEY_CHANGESET_AUTHOR = "changeset-author";

    public static final String KEY_CHANGESET_COMMENT = "changeset-comment";

    public static final String KEY_CHANGESET_DATE = "changeset-date";

    public static final String KEY_CHANGESET_FILES = "changeset-files";

    public static final String KEY_CHANGEFILE_NAME = "changefile-name";

    public static final String KEY_CHANGEFILE_REVISION = "changefile-revision";

    public static final String KEY_CHANGEFILE_STATUS = "changefile-status";

    public static final String KEY_OLD_SCM_CHANGES = "old-scm-changes";

    public static final String KEY_PROJECT_DESCRIPTION = "project-description";

    public static final String KEY_GROUP_ID = "group-id";

    public static final String KEY_ARTIFACT_ID = "artifact-id";

    public static final String KEY_PROJECT_DEVELOPERS = "project-developers";

    public static final String KEY_PROJECT_DEPENDENCIES = "project-dependencies";

    public static final String KEY_PROJECT_NOTIFIERS = "project-notifiers";

    public static final String KEY_PROJECT_URL = "project-url";

    public static final String KEY_SCM_TAG = "scm-tag";

    public static final String KEY_PROJECT_PARENT = "project-parent";

    public static final String KEY_NOTIFIER_TYPE = "notifier-type";

    public static final String KEY_NOTIFIER_CONFIGURATION = "notifier-configuration";

    public static final String KEY_NOTIFIER_FROM = "notifier-from";

    public static final String KEY_NOTIFIER_RECIPIENT_TYPE = "notifier-recipient-type";

    public static final String KEY_NOTIFIER_ENABLED = "notifier-enabled";

    public static final String KEY_NOTIFIER_SEND_ON_SUCCESS = "notifier-send-on-success";

    public static final String KEY_NOTIFIER_SEND_ON_FAILURE = "notifier-send-on-failure";

    public static final String KEY_NOTIFIER_SEND_ON_ERROR = "notifier-send-on-error";

    public static final String KEY_NOTIFIER_SEND_ON_SCMFAILURE = "notifier-send-on-scmfailure";

    public static final String KEY_NOTIFIER_SEND_ON_WARNING = "notifier-send-on-warning";

    public static final String KEY_PROJECT_DEVELOPER_NAME = "developer-name";

    public static final String KEY_PROJECT_DEVELOPER_EMAIL = "developer-email";

    public static final String KEY_PROJECT_DEVELOPER_SCMID = "developer-scmid";

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
        return new Long( getString( context, KEY_BUILD_START ) );
    }

    public static long getBuildEnd( Map context )
    {
        return new Long( getString( context, KEY_BUILD_END ) );
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

    public static String getBuildOutput( Map context )
    {
        return getString( context, KEY_BUILD_OUTPUT );
    }

    public static BuildResult getBuildResult( Map context, Object defaultValue )
    {
        return (BuildResult) getObject( context, KEY_BUILD_RESULT, defaultValue );
    }

    public static String getInstallationName( Map context )
    {
        return getString( context, KEY_INSTALLATION_NAME );
    }

    public static String getInstallationType( Map context )
    {
        return getString( context, KEY_INSTALLATION_TYPE );
    }

    public static String getInstallationVarValue( Map context )
    {
        return getString( context, KEY_INSTALLATION_VAR_VALUE );
    }

    public static String getInstallationVarName( Map context )
    {
        return getString( context, KEY_INSTALLATION_VAR_NAME );
    }

    public static List getScmChanges( Map context )
    {
        return getList( context, KEY_SCM_CHANGES );
    }

    public static String getChangeSetAuthor( Map context )
    {
        return getString( context, KEY_CHANGESET_AUTHOR );
    }

    public static String getChangeSetComment( Map context )
    {
        return getString( context, KEY_CHANGESET_COMMENT );
    }

    public static long getChangeSetDate( Map context )
    {
        Date date = getDate( context, KEY_CHANGESET_DATE );

        if ( date == null )
        {
            return 0;
        }
        else
        {
            return date.getTime();
        }
    }

    public static List getChangeSetFiles( Map context )
    {
        return getList( context, KEY_CHANGESET_FILES );
    }

    public static String getChangeFileName( Map context )
    {
        return getString( context, KEY_CHANGEFILE_NAME );
    }

    public static String getChangeFileRevision( Map context )
    {
        return getString( context, KEY_CHANGEFILE_REVISION );
    }

    public static String getChangeFileStatus( Map context )
    {
        return getString( context, KEY_CHANGEFILE_STATUS );
    }

    public static String getGroupId( Map context )
    {
        return getString( context, KEY_GROUP_ID);
    }

    public static String getArtifactId( Map context )
    {
        return getString( context, KEY_ARTIFACT_ID );
    }

    public static String getVersion( Map context )
    {
        return getString( context, KEY_PROJECT_VERSION );
        
    }

    public static String getProjectName( Map context )
    {
        return getString( context, KEY_PROJECT_NAME );
    }

    public static String getProjectDescription( Map context )
    {
        return getString( context, KEY_PROJECT_DESCRIPTION );
    }

    public static String getProjectUrl( Map context )
    {
        return getString( context, KEY_PROJECT_URL );
    }

    public static String getScmUrl( Map context )
    {
        return getString( context, KEY_SCM_URL );
    }

    public static String getScmTag( Map context )
    {
        return getString( context, KEY_SCM_TAG );
    }

    public static Map getProjectParent( Map context )
    {
        return getMap( context, KEY_PROJECT_PARENT );
    }

    public static List getProjectDevelopers( Map context )
    {
        return getList( context, KEY_PROJECT_DEVELOPERS );
    }

    public static String getDeveloperName( Map context )
    {
        return getString( context, KEY_PROJECT_DEVELOPER_NAME );
    }

    public static String getDeveloperEmail( Map context )
    {
        return getString( context, KEY_PROJECT_DEVELOPER_EMAIL );
    }

    public static String getDeveloperScmId( Map context )
    {
        return getString( context, KEY_PROJECT_DEVELOPER_SCMID );
    }

    public static List getProjectDependencies( Map context )
    {
        return getList( context, KEY_PROJECT_DEPENDENCIES );
    }

    public static List getProjectNotifiers( Map context )
    {
        return getList( context, KEY_PROJECT_NOTIFIERS );
    }

    public static Map getNotifierConfiguration( Map context )
    {
        return getMap( context, KEY_NOTIFIER_CONFIGURATION );
    }

    public static int getNotifierFrom( Map context )
    {
        return getInteger( context, KEY_NOTIFIER_FROM );
    }

    public static int getNotifierRecipientType( Map context )
    {
        return getInteger( context, KEY_NOTIFIER_RECIPIENT_TYPE );
    }

    public static String getNotifierType( Map context )
    {
        return getString( context, KEY_NOTIFIER_TYPE );
    }

    public static boolean isNotifierEnabled( Map context )
    {
        return getBoolean( context, KEY_NOTIFIER_ENABLED );
    }

    public static boolean isNotifierSendOnError( Map context )
    {
        return getBoolean( context, KEY_NOTIFIER_SEND_ON_ERROR );
    }

    public static boolean isNotifierSendOnFailure( Map context )
    {
        return getBoolean( context, KEY_NOTIFIER_SEND_ON_FAILURE );
    }

    public static boolean isNotifierSendOnScmFailure( Map context )
    {
        return getBoolean( context, KEY_NOTIFIER_SEND_ON_SCMFAILURE );
    }

    public static boolean isNotifierSendOnSuccess( Map context )
    {
        return getBoolean( context, KEY_NOTIFIER_SEND_ON_SUCCESS );
    }

    public static boolean isNotifierSendOnWarning( Map context )
    {
        return getBoolean( context, KEY_NOTIFIER_SEND_ON_WARNING );
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

    protected static Date getDate( Map context, String key )
    {
        Object obj = getObject( context, key, null );

        if ( obj == null )
        {
            return null;
        }
        else
        {
            return (Date)obj;
        }
    }

    protected static List getList( Map context, String key )
    {
        Object obj = getObject( context, key, null );

        if ( obj == null )
        {
            return null;
        }
        else
        {
            List list = new ArrayList();
            Object[] objA = (Object[]) obj;

            for ( Object o : objA )
            {
                if ( o instanceof Map )
                {
                    list.add( (Map) o );
                }
                else
                {
                    list.add( o );
                }
            }

            return list;
        }
    }

    protected static Map getMap( Map context, String key )
    {
        Object obj = getObject( context, key, null );

        if ( obj == null )
        {
            return null;
        }
        else
        {
            return (Map) obj;
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
