package org.apache.continuum.buildagent.utils;

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

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContinuumBuildAgentUtil
{
    public static final String EOL = System.getProperty( "line.separator" );

    public static final String KEY_PROJECT_ID = "project-id";

    public static final String KEY_PROJECT_VERSION = "project-version";

    public static final String KEY_BUILD_NUMBER = "build-number";

    public static final String KEY_BUILD_DEFINITION_ID = "builddefinition-id";

    public static final String KEY_BUILD_DEFINITION_LABEL = "builddefinition-label";

    public static final String KEY_TRIGGER = "trigger";

    public static final String KEY_USERNAME = "username";

    public static final String KEY_EXECUTOR_ID = "executor-id";

    public static final String KEY_SCM_URL = "scm-url";

    public static final String KEY_SCM_USERNAME = "scm-username";

    public static final String KEY_SCM_PASSWORD = "scm-password";

    public static final String KEY_BUILD_FILE = "build-file";

    public static final String KEY_GOALS = "goals";

    public static final String KEY_ARGUMENTS = "arguments";

    public static final String KEY_BUILD_FRESH = "build-fresh";

    public static final String KEY_START_TIME = "start-time";

    public static final String KEY_END_TIME = "end-time";

    public static final String KEY_BUILD_ERROR = "build-error";

    public static final String KEY_BUILD_EXIT_CODE = "build-exit-code";

    public static final String KEY_BUILD_STATE = "build-state";

    public static final String KEY_SCM_STATE = "scm-state";

    public static final String KEY_SCM_COMMAND_OUTPUT = "scm-command-output";

    public static final String KEY_SCM_COMMAND_LINE = "scm-command-line";

    public static final String KEY_SCM_PROVIDER_MESSAGE = "scm-provider-message";

    public static final String KEY_SCM_EXCEPTION = "scm-exception";

    public static final String KEY_PROJECT_GROUP_ID = "project-group-id";

    public static final String KEY_PROJECT_GROUP_NAME = "project-group-name";

    public static final String KEY_SCM_ROOT_ADDRESS = "scm-root-address";

    public static final String KEY_SCM_ROOT_ID = "scm-root-id";

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

    public static final String KEY_SCM_CHANGES = "scm-changes";

    public static final String KEY_CHANGESET_AUTHOR = "changeset-author";

    public static final String KEY_CHANGESET_COMMENT = "changeset-comment";

    public static final String KEY_CHANGESET_DATE = "changeset-date";

    public static final String KEY_CHANGESET_FILES = "changeset-files";

    public static final String KEY_CHANGEFILE_NAME = "changefile-name";

    public static final String KEY_CHANGEFILE_REVISION = "changefile-revision";

    public static final String KEY_CHANGEFILE_STATUS = "changefile-status";

    public static final String KEY_OLD_SCM_RESULT = "old-scm-result";

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

    public static final String KEY_PROJECT_MODULES = "project-modules";

    public static final String KEY_MAVEN_PROJECT = "maven-project";

    public static final String KEY_LATEST_UPDATE_DATE = "latest-update-date";

    public static final String KEY_BUILD_AGENT_URL = "build-agent-url";

    public static final String KEY_SCM_TAGBASE = "scm-tagbase";

    public static final String KEY_PREPARE_GOALS = "preparation-goals";

    public static final String KEY_PERFORM_GOALS = "perform-goals";

    public static final String KEY_SCM_COMMENT_PREFIX = "scm-comment-prefix";

    public static final String KEY_AUTO_VERSION_SUBMODULES = "auto-version-submodules";

    public static final String KEY_ADD_SCHEMA = "add-schema";

    public static final String KEY_USE_RELEASE_PROFILE = "use-release-profile";

    public static final String KEY_RELEASE_VERSION = "release-version";

    public static final String KEY_DEVELOPMENT_VERSION = "development-version";

    public static final String KEY_USE_EDIT_MODE = "use-edit-mode";

    public static final String KEY_RELEASE_RESULT_CODE = "release-result-code";

    public static final String KEY_RELEASE_OUTPUT = "release-output";

    public static final String KEY_BUILD_CONTEXTS = "build-contexts";

    public static final String KEY_MAX_JOB_EXEC_TIME = "max-job-exec-time";

    public static final String KEY_RELEASE_STATE = "state";

    public static final String KEY_RELEASE_PHASES = "release-phases";

    public static final String KEY_RELEASE_IN_PROGRESS = "release-in-progress";

    public static final String KEY_COMPLETED_RELEASE_PHASES = "completed-release-phases";

    public static final String KEY_RELEASE_ERROR = "release-error";

    public static final String KEY_LOCAL_REPOSITORY_NAME = "repo-name";

    public static final String KEY_LOCAL_REPOSITORY_LAYOUT = "repo-layout";

    public static Integer getProjectId( Map<String, Object> context )
    {
        return getInteger( context, KEY_PROJECT_ID );
    }

    public static String getProjectName( Map<String, Object> context )
    {
        return getString( context, KEY_PROJECT_NAME );
    }

    public static Integer getProjectState( Map<String, Object> context )
    {
        return getInteger( context, KEY_PROJECT_STATE );
    }

    public static Integer getBuildDefinitionId( Map<String, Object> context )
    {
        return getInteger( context, KEY_BUILD_DEFINITION_ID );
    }

    public static String getBuildFile( Map<String, Object> context )
    {
        return getString( context, KEY_BUILD_FILE );
    }

    public static String getExecutorId( Map<String, Object> context )
    {
        return getString( context, KEY_EXECUTOR_ID );
    }

    public static String getGoals( Map<String, Object> context )
    {
        return getString( context, KEY_GOALS );
    }

    public static String getArguments( Map<String, Object> context )
    {
        return getString( context, KEY_ARGUMENTS );
    }

    public static String getScmUrl( Map<String, Object> context )
    {
        return getString( context, KEY_SCM_URL );
    }

    public static String getScmUsername( Map<String, Object> context )
    {
        return getString( context, KEY_SCM_USERNAME, "" );
    }

    public static String getScmPassword( Map<String, Object> context )
    {
        return getString( context, KEY_SCM_PASSWORD, "" );
    }

    public static boolean isBuildFresh( Map<String, Object> context )
    {
        return getBoolean( context, KEY_BUILD_FRESH );
    }

    public static int getProjectGroupId( Map<String, Object> context )
    {
        return getInteger( context, KEY_PROJECT_GROUP_ID );
    }

    public static String getScmRootAddress( Map<String, Object> context )
    {
        return getString( context, KEY_SCM_ROOT_ADDRESS );
    }

    public static int getScmRootState( Map<String, Object> context )
    {
        return getInteger( context, KEY_SCM_ROOT_STATE );
    }

    public static Project getProject( Map<String, Object> context )
    {
        return (Project) getObject( context, KEY_PROJECT );
    }

    public static BuildDefinition getBuildDefinition( Map<String, Object> context )
    {
        return (BuildDefinition) getObject( context, KEY_BUILD_DEFINITION );
    }

    public static ScmResult getCheckoutScmResult( Map<String, Object> context, Object defaultValue )
    {
        return (ScmResult) getObject( context, KEY_CHECKOUT_SCM_RESULT, defaultValue );
    }

    public static ScmResult getUpdateScmResult( Map<String, Object> context, Object defaultValue )
    {
        return (ScmResult) getObject( context, KEY_UPDATE_SCM_RESULT, defaultValue );
    }

    public static ScmResult getScmResult( Map<String, Object> context, Object defaultValue )
    {
        return (ScmResult) getObject( context, KEY_SCM_RESULT, defaultValue );
    }

    public static int getTrigger( Map<String, Object> context )
    {
        return getInteger( context, KEY_TRIGGER );
    }

    public static String getUsername( Map<String, Object> context )
    {
        return getString( context, KEY_USERNAME, "scheduled" );
    }

    public static BuildTrigger getBuildTrigger( Map<String, Object> context )
    {
        return new BuildTrigger( getTrigger( context ), getUsername( context ) );
    }

    public static BuildResult getBuildResult( Map<String, Object> context, Object defaultValue )
    {
        return (BuildResult) getObject( context, KEY_BUILD_RESULT, defaultValue );
    }

    public static Map<String, String> getEnvironments( Map<String, Object> context )
    {
        return (Map<String, String>) getObject( context, KEY_ENVIRONMENTS );
    }

    public static String getLocalRepository( Map<String, Object> context )
    {
        return getString( context, KEY_LOCAL_REPOSITORY, "" );
    }

    public static String getProjectVersion( Map<String, Object> context )
    {
        return getString( context, KEY_PROJECT_VERSION );
    }

    public static String getProjectGroupName( Map<String, Object> context )
    {
        return getString( context, KEY_PROJECT_GROUP_NAME );
    }

    public static int getBuildNumber( Map<String, Object> context )
    {
        return getInteger( context, KEY_BUILD_NUMBER );
    }

    public static List<Map<String, Object>> getOldScmChanges( Map<String, Object> context )
    {
        return getList( context, KEY_OLD_SCM_CHANGES );
    }

    public static String getChangeSetAuthor( Map<String, Object> context )
    {
        return getString( context, KEY_CHANGESET_AUTHOR );
    }

    public static String getChangeSetComment( Map<String, Object> context )
    {
        return getString( context, KEY_CHANGESET_COMMENT );
    }

    public static long getChangeSetDate( Map<String, Object> context )
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

    public static List getChangeSetFiles( Map<String, Object> context )
    {
        return getList( context, KEY_CHANGESET_FILES );
    }

    public static String getChangeFileName( Map<String, Object> context )
    {
        return getString( context, KEY_CHANGEFILE_NAME );
    }

    public static String getChangeFileRevision( Map<String, Object> context )
    {
        return getString( context, KEY_CHANGEFILE_REVISION );
    }

    public static String getChangeFileStatus( Map<String, Object> context )
    {
        return getString( context, KEY_CHANGEFILE_STATUS );
    }

    public static ScmResult getOldScmResult( Map<String, Object> context, ScmResult defaultValue )
    {
        return (ScmResult) getObject( context, KEY_OLD_SCM_RESULT, defaultValue );
    }

    public static List getScmChanges( Map<String, Object> context )
    {
        return getList( context, KEY_SCM_CHANGES );
    }

    public static Date getLatestUpdateDate( Map<String, Object> context )
    {
        return getDate( context, KEY_LATEST_UPDATE_DATE );
    }

    public static String getBuildAgentUrl( Map<String, Object> context )
    {
        return getString( context, KEY_BUILD_AGENT_URL );
    }

    public static String getGroupId( Map<String, Object> context )
    {
        return getString( context, KEY_GROUP_ID );
    }

    public static String getArtifactId( Map<String, Object> context )
    {
        return getString( context, KEY_ARTIFACT_ID );
    }

    public static Map getReleaseVersion( Map<String, Object> context )
    {
        return getMap( context, KEY_RELEASE_VERSION );
    }

    public static Map getDevelopmentVersion( Map<String, Object> context )
    {
        return getMap( context, KEY_DEVELOPMENT_VERSION );
    }

    public static String getScmTagBase( Map<String, Object> context )
    {
        return getString( context, KEY_SCM_TAGBASE, "" );
    }

    public static String getScmCommentPrefix( Map<String, Object> context )
    {
        return getString( context, KEY_SCM_COMMENT_PREFIX, "" );
    }

    public static String getScmTag( Map<String, Object> context )
    {
        return getString( context, KEY_SCM_TAG, "" );
    }

    public static String getPrepareGoals( Map<String, Object> context )
    {
        return getString( context, KEY_PREPARE_GOALS, "" );
    }

    public static String getPerformGoals( Map<String, Object> context )
    {
        return getString( context, KEY_PERFORM_GOALS, "" );
    }

    public static String getUseEditMode( Map<String, Object> context )
    {
        return getString( context, KEY_USE_EDIT_MODE, "" );
    }

    public static String getAddSchema( Map<String, Object> context )
    {
        return getString( context, KEY_ADD_SCHEMA, "" );
    }

    public static String getAutoVersionSubmodules( Map<String, Object> context )
    {
        return getString( context, KEY_AUTO_VERSION_SUBMODULES, "" );
    }

    public static List getBuildContexts( Map<String, Object> context )
    {
        return getList( context, KEY_BUILD_CONTEXTS );
    }

    public static int getMaxExecutionTime( Map<String, Object> context )
    {
        return getInteger( context, KEY_MAX_JOB_EXEC_TIME );
    }

    public static String getLocalRepositoryName( Map<String, Object> context )
    {
        return getString( context, KEY_LOCAL_REPOSITORY_NAME, "" );
    }

    public static String getLocalRepositoryLayout( Map<String, Object> context )
    {
        return getString( context, KEY_LOCAL_REPOSITORY_LAYOUT, "" );
    }

    public static int getScmRootId( Map<String, Object> context )
    {
        return getInteger( context, KEY_SCM_ROOT_ID );
    }

    public static String getBuildDefinitionLabel( Map<String, Object> context )
    {
        return getString( context, KEY_BUILD_DEFINITION_LABEL, "" );
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

    public static int getInteger( Map<String, Object> context, String key )
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

    public static List getList( Map<String, Object> context, String key )
    {
        Object obj = getObject( context, key, null );

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

    public static Date getDate( Map<String, Object> context, String key )
    {
        Object obj = getObject( context, key, null );

        if ( obj == null )
        {
            return null;
        }
        else
        {
            return (Date) obj;
        }
    }

    protected static Map getMap( Map<String, Object> context, String key )
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

    public static Map<String, Object> createScmResult( BuildContext buildContext )
    {
        Map<String, Object> result = new HashMap<String, Object>();
        ScmResult scmResult = buildContext.getScmResult();

        result.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, buildContext.getProjectId() );
        if ( StringUtils.isEmpty( scmResult.getCommandLine() ) )
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_COMMAND_LINE, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_COMMAND_LINE, scmResult.getCommandLine() );
        }
        if ( StringUtils.isEmpty( scmResult.getCommandOutput() ) )
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_COMMAND_OUTPUT, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_COMMAND_OUTPUT, scmResult.getCommandOutput() );
        }
        if ( StringUtils.isEmpty( scmResult.getProviderMessage() ) )
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_PROVIDER_MESSAGE, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_PROVIDER_MESSAGE, scmResult.getProviderMessage() );
        }
        if ( StringUtils.isEmpty( scmResult.getException() ) )
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_EXCEPTION, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_EXCEPTION, scmResult.getException() );
        }
        result.put( ContinuumBuildAgentUtil.KEY_SCM_SUCCESS, scmResult.isSuccess() );
        result.put( ContinuumBuildAgentUtil.KEY_SCM_CHANGES, getScmChanges( scmResult ) );

        return result;
    }

    private static List<Map<String, Object>> getScmChanges( ScmResult scmResult )
    {
        List<Map<String, Object>> scmChanges = new ArrayList<Map<String, Object>>();

        List<ChangeSet> changes = scmResult.getChanges();

        if ( changes != null )
        {
            for ( ChangeSet cs : changes )
            {
                Map<String, Object> changeSet = new HashMap<String, Object>();

                if ( StringUtils.isNotEmpty( cs.getAuthor() ) )
                {
                    changeSet.put( ContinuumBuildAgentUtil.KEY_CHANGESET_AUTHOR, cs.getAuthor() );
                }
                else
                {
                    changeSet.put( ContinuumBuildAgentUtil.KEY_CHANGESET_AUTHOR, "" );
                }
                if ( StringUtils.isNotEmpty( cs.getComment() ) )
                {
                    changeSet.put( ContinuumBuildAgentUtil.KEY_CHANGESET_COMMENT, cs.getComment() );
                }
                else
                {
                    changeSet.put( ContinuumBuildAgentUtil.KEY_CHANGESET_COMMENT, "" );
                }
                if ( cs.getDateAsDate() != null )
                {
                    changeSet.put( ContinuumBuildAgentUtil.KEY_CHANGESET_DATE, cs.getDateAsDate() );
                }
                changeSet.put( ContinuumBuildAgentUtil.KEY_CHANGESET_FILES, getChangeFiles( cs.getFiles() ) );

                scmChanges.add( changeSet );
            }
        }

        return scmChanges;
    }

    private static List<Map<String, String>> getChangeFiles( List<ChangeFile> changeFiles )
    {
        List<Map<String, String>> files = new ArrayList<Map<String, String>>();

        if ( changeFiles != null )
        {
            for ( ChangeFile file : changeFiles )
            {
                Map<String, String> changeFile = new HashMap<String, String>();
                if ( StringUtils.isNotEmpty( file.getName() ) )
                {
                    changeFile.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_NAME, file.getName() );
                }
                else
                {
                    changeFile.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_NAME, "" );
                }
                if ( StringUtils.isNotEmpty( file.getRevision() ) )
                {
                    changeFile.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_REVISION, file.getRevision() );
                }
                else
                {
                    changeFile.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_REVISION, "" );
                }
                if ( StringUtils.isNotEmpty( file.getStatus() ) )
                {
                    changeFile.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_STATUS, file.getStatus() );
                }
                else
                {
                    changeFile.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_STATUS, "" );
                }

                files.add( changeFile );
            }
        }

        return files;
    }

    public static List<File> getFiles( String userDirectory, File workingDirectory )
        throws ContinuumException
    {
        return getFiles( workingDirectory, null, userDirectory );
    }

    private static List<File> getFiles( File baseDirectory, String currentSubDirectory, String userDirectory )
    {
        List<File> dirs = new ArrayList<File>();

        File workingDirectory;

        if ( currentSubDirectory != null )
        {
            workingDirectory = new File( baseDirectory, currentSubDirectory );
        }
        else
        {
            workingDirectory = baseDirectory;
        }

        String[] files = workingDirectory.list();

        if ( files != null )
        {
            for ( String file : files )
            {
                File current = new File( workingDirectory, file );

                String currentFile;

                if ( currentSubDirectory == null )
                {
                    currentFile = file;
                }
                else
                {
                    currentFile = currentSubDirectory + "/" + file;
                }

                if ( userDirectory != null && current.isDirectory() && userDirectory.startsWith( currentFile ) )
                {
                    dirs.add( current );

                    dirs.addAll( getFiles( baseDirectory, currentFile, userDirectory ) );
                }
                else
                {
                    dirs.add( current );
                }
            }
        }

        return dirs;
    }
}