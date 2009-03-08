package org.apache.maven.continuum.release.tasks;

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

import org.apache.continuum.model.repository.LocalRepository;
import org.apache.maven.shared.release.ReleaseManagerListener;
import org.apache.maven.shared.release.config.ReleaseDescriptor;

import java.io.File;

/**
 * @author Edwin Punzalan
 * @version $Id$
 */
public class PerformReleaseProjectTask
    extends AbstractReleaseProjectTask
{
    private File buildDirectory;

    private String goals;

    private boolean useReleaseProfile = true;
    
    private LocalRepository localRepository;

    public PerformReleaseProjectTask( String releaseId, ReleaseDescriptor descriptor, File buildDirectory, String goals,
                                      boolean useReleaseProfile, ReleaseManagerListener listener )
    {
        this( releaseId, descriptor, buildDirectory, goals, useReleaseProfile, listener, null );
    }

    public PerformReleaseProjectTask( String releaseId, ReleaseDescriptor descriptor, File buildDirectory, String goals,
                                      boolean useReleaseProfile, ReleaseManagerListener listener, LocalRepository repository )
    {
        super( releaseId, descriptor, listener );
        setBuildDirectory( buildDirectory );
        setGoals( goals );
        setUseReleaseProfile( useReleaseProfile );
        setLocalRepository( repository );
    }
    
    public String getGoals()
    {
        return goals;
    }

    public void setGoals( String goals )
    {
        this.goals = goals;
    }

    public boolean isUseReleaseProfile()
    {
        return useReleaseProfile;
    }

    public void setUseReleaseProfile( boolean useReleaseProfile )
    {
        this.useReleaseProfile = useReleaseProfile;
    }

    public File getBuildDirectory()
    {
        return buildDirectory;
    }

    public void setBuildDirectory( File buildDirectory )
    {
        this.buildDirectory = buildDirectory;
    }
    
    public LocalRepository getLocalRepository()
    {
        return localRepository;
    }
    
    public void setLocalRepository( LocalRepository localRepository )
    {
        this.localRepository = localRepository;
    }
}
