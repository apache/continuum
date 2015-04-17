package org.apache.maven.continuum.web.util;

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

import org.apache.maven.continuum.project.ContinuumProjectState;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
public class StateGenerator
{
    public static final String NEW = "NEW";

    public static final String SUCCESS = "Success";

    public static final String FAILED = "Failed";

    public static final String ERROR = "Error";

    public static final String CANCELLED = "Canceled";

    public static final String BUILDING = "Building";

    public static final String UPDATING = "Updating";

    public static final String UPDATED = "Updated";

    public static final String CHECKING_OUT = "Checking Out";

    public static final String CHECKED_OUT = "Checked Out";

    public static final int UNKNOWN_STATE = Integer.MIN_VALUE;

    public static final String UNKNOWN = "Unknown";

    public static final Map<Integer, String[]> stateIconArgs = new HashMap<Integer, String[]>();

    static
    {
        stateIconArgs.put( ContinuumProjectState.OK, new String[] { "/images/icon_success_sml.gif", SUCCESS } );
        stateIconArgs.put( ContinuumProjectState.UPDATED, new String[] { "/images/icon_success_sml.gif", UPDATED } );
        stateIconArgs.put( ContinuumProjectState.FAILED, new String[] { "/images/icon_warning_sml.gif", FAILED } );
        stateIconArgs.put( ContinuumProjectState.ERROR, new String[] { "/images/icon_error_sml.gif", ERROR } );
        stateIconArgs.put( ContinuumProjectState.BUILDING, new String[] { "/images/building.gif", BUILDING } );
        stateIconArgs.put( ContinuumProjectState.UPDATING, new String[] { "/images/checkingout.gif", UPDATING } );
        stateIconArgs.put( ContinuumProjectState.CHECKING_OUT,
                           new String[] { "/images/checkingout.gif", CHECKING_OUT } );
        stateIconArgs.put( ContinuumProjectState.CHECKEDOUT,
                           new String[] { "/images/icon_success_sml.gif", CHECKED_OUT } );
        stateIconArgs.put( ContinuumProjectState.CANCELLED,
                           new String[] { "/images/icon_unknown_sml.gif", CANCELLED } );
    }

    public static String generate( int state, String contextPath )
    {
        String iconFmt = "<img src=\"" + contextPath + "%s\" alt=\"%2$s\" title=\"%2$s\" border=\"0\" />";

        if ( state == ContinuumProjectState.NEW )
        {
            return NEW;
        }

        if ( stateIconArgs.containsKey( state ) )
        {
            return String.format( iconFmt, stateIconArgs.get( state ) );
        }

        return String.format( iconFmt, "/images/icon_unknown_sml.gif", UNKNOWN );
    }
}
