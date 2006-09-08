package org.apache.maven.continuum.release;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.maven.plugins.release.ReleaseManagerListener;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Edwin Punzalan
 */
public class DefaultReleaseManagerListener
    implements ReleaseManagerListener, ContinuumReleaseManagerListener
{
    private List completed;

    private String inProgress;

    private List phases;

    public void goalStart( String name, List phases )
    {
        this.phases = phases;
        completed = Collections.synchronizedList( new ArrayList() );
        inProgress = null;
    }

    public void phaseStart( String name )
    {
        inProgress = name;
    }

    public void phaseEnd()
    {
        completed.add( inProgress );
        inProgress = null;
    }

    public void phaseSkip( String name )
    {
        completed.add( name );
    }

    public void goalEnd()
    {
    }

    public void error( String message )
    {
    }

    public List getCompletedPhases()
    {
        return completed;
    }

    public String getInProgress()
    {
        return inProgress;
    }

    public List getPhases()
    {
        return phases;
    }

    public void setPhases( List phases )
    {
        this.phases = phases;
    }
}
