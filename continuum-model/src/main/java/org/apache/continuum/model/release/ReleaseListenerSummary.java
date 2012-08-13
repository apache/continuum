package org.apache.continuum.model.release;

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

import java.util.List;

public class ReleaseListenerSummary
{
    private String goalName;

    private String error;

    private String username;

    private String inProgress;

    private int state;

    private List<String> phases;

    public String getGoalName()
    {
        return goalName;
    }

    public void setGoalName( String goalName )
    {
        this.goalName = goalName;
    }

    public String getError()
    {
        return error;
    }

    public void setError( String error )
    {
        this.error = error;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getInProgress()
    {
        return inProgress;
    }

    public void setInProgress( String inProgress )
    {
        this.inProgress = inProgress;
    }

    public int getState()
    {
        return state;
    }

    public void setState( int state )
    {
        this.state = state;
    }

    public List<String> getPhases()
    {
        return phases;
    }

    public void setPhases( List<String> phases )
    {
        this.phases = phases;
    }

    public List<String> getCompletedPhases()
    {
        return completedPhases;
    }

    public void setCompletedPhases( List<String> completedPhases )
    {
        this.completedPhases = completedPhases;
    }

    private List<String> completedPhases;
}
