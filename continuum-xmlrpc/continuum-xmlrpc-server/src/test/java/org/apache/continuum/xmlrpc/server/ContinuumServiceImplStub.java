package org.apache.continuum.xmlrpc.server;

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

import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.profile.ProfileException;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.server.ContinuumServiceImpl;

public class ContinuumServiceImplStub
    extends ContinuumServiceImpl
{
    protected void checkBuildProjectInGroupAuthorization( String resource )
        throws ContinuumException
    {
        // do nothing
    }

    protected void checkViewProjectGroupAuthorization( String resource )
        throws ContinuumException
    {
        // do nothing
    }

    protected void buildProjectWithBuildDefinition( int projectId, int buildDefinitionId, BuildTrigger buildTrigger )
    {
        // do nothing
    }

    public org.apache.maven.continuum.model.project.BuildDefinition getBuildDefinition( BuildDefinition buildDef,
                                                                                        org.apache.maven.continuum.model.project.BuildDefinition buildDefinition )
        throws ProfileException, ContinuumException
    {
        return populateBuildDefinition( buildDef, buildDefinition );
    }
}
