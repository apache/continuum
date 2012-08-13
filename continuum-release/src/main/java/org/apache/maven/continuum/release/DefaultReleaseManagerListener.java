package org.apache.maven.continuum.release;

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

import org.apache.maven.shared.release.ReleaseManagerListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Edwin Punzalan
 * @version $Id$
 */
public class DefaultReleaseManagerListener
    implements ReleaseManagerListener, ContinuumReleaseManagerListener
{
    private String goalName;

    private List<String> completedPhases;

    private String inProgress;

    private List<String> phases;

    private String error;

    private int state;

    private String username;

    public void goalStart( String name, List phases )
    {
        state = LISTENING;
        goalName = name;
        this.phases = phases;
        completedPhases = Collections.synchronizedList( new ArrayList<String>() );
        inProgress = null;
    }

    public void phaseStart( String name )
    {
        inProgress = name;
    }

    public void phaseEnd()
    {
        completedPhases.add( inProgress );

        inProgress = null;
    }

    public void phaseSkip( String name )
    {
        completedPhases.add( name );
    }

    public void goalEnd()
    {
        state = FINISHED;
    }

    public void error( String message )
    {
        error = message;
        goalEnd();
    }

    public List<String> getCompletedPhases()
    {
        return completedPhases;
    }

    public String getInProgress()
    {
        return inProgress;
    }

    public List<String> getPhases()
    {
        return phases;
    }

    public String getGoalName()
    {
        return goalName;
    }

    public String getError()
    {
        return error;
    }

    public int getState()
    {
        return state;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }
}
