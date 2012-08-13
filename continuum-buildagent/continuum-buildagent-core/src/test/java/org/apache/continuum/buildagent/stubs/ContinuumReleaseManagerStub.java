package org.apache.continuum.buildagent.stubs;

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
import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.apache.maven.continuum.release.ContinuumReleaseManagerListener;
import org.apache.maven.continuum.release.DefaultContinuumReleaseManager;

import java.io.File;

public class ContinuumReleaseManagerStub
    extends DefaultContinuumReleaseManager
{
    public void perform( String releaseId, File buildDirectory, String goals, String arguments,
                         boolean useReleaseProfile, ContinuumReleaseManagerListener listener,
                         LocalRepository repository )
        throws ContinuumReleaseException
    {
        if ( !repository.getName().equalsIgnoreCase( "default" ) )
        {
            throw new ContinuumReleaseException( "Incorrect local repository name!" );
        }

        if ( !repository.getLocation().equals( "/home/user/.m2/repository" ) )
        {
            throw new ContinuumReleaseException( "Incorrect local repository location!" );
        }

        if ( !repository.getLayout().equals( "default" ) )
        {
            throw new ContinuumReleaseException( "Incorrect local repository layout!" );
        }
    }
}
