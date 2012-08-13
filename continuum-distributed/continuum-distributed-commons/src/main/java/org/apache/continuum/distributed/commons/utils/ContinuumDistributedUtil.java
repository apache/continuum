package org.apache.continuum.distributed.commons.utils;

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

/**
 * ContinuumDistributedUtil
 */
public class ContinuumDistributedUtil
{
    public static final String KEY_PROJECT_ID = "project-id";

    public static final String KEY_PROJECT_GROUP_ID = "project-group-id";

    public static final String KEY_PROJECT_NAME = "project-name";

    public static final String KEY_ARTIFACT_ID = "artifact-id";

    public static int getProjectId( Map<String, Object> context )
    {
        return getInteger( context, KEY_PROJECT_ID );
    }

    public static int getProjectGroupId( Map<String, Object> context )
    {
        return getInteger( context, KEY_PROJECT_GROUP_ID );
    }

    public static String getArtifactId( Map<String, Object> context )
    {
        return getString( context, KEY_ARTIFACT_ID );
    }

    public static String getProjectName( Map<String, Object> context )
    {
        return getString( context, KEY_PROJECT_NAME );
    }

    public static String getProjectNameAndId( Map<String, Object> context )
    {
        StringBuilder result = new StringBuilder();

        if ( getProjectName( context ) != null )
        {
            result.append( getProjectName( context ) ).append( " " );
        }
        else if ( getArtifactId( context ) != null )
        {
            result.append( getArtifactId( context ) ).append( " " );
        }

        if ( context.containsKey( KEY_PROJECT_ID ) )
        {
            result.append( "(projectId=" ).append( getProjectId( context ) ).append( ")" );
        }
        else
        {
            result.append( "(projectGroupId=" ).append( getProjectGroupId( context ) ).append( ")" );
        }

        return result.toString();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private static String getString( Map<String, Object> context, String key )
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

    private static int getInteger( Map<String, Object> context, String key )
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

    private static Object getObject( Map<String, Object> context, String key, Object defaultValue )
    {
        Object value = context.get( key );

        if ( value == null )
        {
            return defaultValue;
        }

        return value;
    }
}

