package org.apache.maven.continuum.core.action;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.util.Map;

import org.apache.maven.continuum.core.ContinuumCore;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.ContinuumScm;
import org.apache.maven.continuum.scm.UpdateScmResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import org.codehaus.plexus.action.Action;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractContinuumAction
    extends AbstractLogEnabled
    implements Action
{
    // ----------------------------------------------------------------------
    // Keys for the values that can be in the context
    // ----------------------------------------------------------------------

    public final static String KEY_PROJECT_ID = "projectId";

    public final static String KEY_BUILD_ID = "buildId";

    public static final String KEY_WORKING_DIRECTORY = "workingDirectory";

    public static final String KEY_CHECKOUT_SCM_RESULT = "checkOutResult";

    protected static final String KEY_UPDATE_SCM_RESULT = "updateResult";

    private static final String KEY_FORCED = "forced";

    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private ContinuumCore core;

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private ContinuumScm scm;

    /**
     * @plexus.requirement
     */
    private ContinuumNotificationDispatcher notifier;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected ContinuumCore getCore()
    {
        return core;
    }

    protected ContinuumStore getStore()
    {
        return store;
    }

    protected ContinuumScm getScm()
    {
        return scm;
    }

    protected ContinuumNotificationDispatcher getNotifier()
    {
        return notifier;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected String getProjectId( Map context )
        throws ContinuumStoreException
    {
        return getString( context, KEY_PROJECT_ID );
    }

    protected String getBuildId( Map context )
        throws ContinuumStoreException
    {
        return getString( context, KEY_BUILD_ID );
    }

    protected boolean isForced( Map context )
        throws ContinuumStoreException
    {
        return ((Boolean) getObject( context, KEY_FORCED )).booleanValue();
    }

    protected ContinuumProject getProject( Map context )
        throws ContinuumStoreException
    {
        return getStore().getProject( getProjectId( context ) );
    }

    protected ContinuumBuild getBuild( Map context )
        throws ContinuumStoreException
    {
        return getStore().getBuild( getBuildId( context ) );
    }

    protected File getWorkingDirectory( Map context )
    {
        return new File( getString( context, KEY_WORKING_DIRECTORY ) );
    }

    protected CheckOutScmResult getCheckOutResult( Map context )
    {
        return (CheckOutScmResult) getObject( context, KEY_CHECKOUT_SCM_RESULT );
    }

    protected UpdateScmResult getUpdateScmResult( Map context )
    {
        return (UpdateScmResult) getObject( context, KEY_UPDATE_SCM_RESULT );
    }

    protected UpdateScmResult getUpdateScmResult(  Map context, UpdateScmResult defaultValue )
    {
        return (UpdateScmResult) getObject( context, KEY_UPDATE_SCM_RESULT, defaultValue );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected String getString( Map context, String key )
    {
        return (String) context.get( key );
    }

    private Object getObject( Map context, String key )
    {
        Object value = context.get( key );

        if ( value == null )
        {
            throw new RuntimeException( "Missing value for key '" + key + "'." );
        }

        return value;
    }

    private Object getObject( Map context, String key, Object defaultValue )
    {
        Object value = context.get( key );

        if ( value == null )
        {
            return defaultValue;
        }

        return value;
    }
}
