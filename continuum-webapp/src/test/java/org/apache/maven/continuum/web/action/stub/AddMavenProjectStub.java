package org.apache.maven.continuum.web.action.stub;

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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.web.action.AddMavenProjectAction;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;

/**
 * A stubbed implementation of {@link org.apache.maven.continuum.web.action.AddMavenProjectAction} useful for testing
 * the abstract class's functionality.
 */
public class AddMavenProjectStub
    extends AddMavenProjectAction
{
    @Override
    protected void checkAddProjectGroupAuthorization()
        throws AuthorizationRequiredException
    {
        // skip authorization check
    }
    
    @Override
    protected ContinuumProjectBuildingResult doExecute( String pomUrl, int selectedProjectGroup, boolean checkProtocol,
                                                        boolean scmUseCache )
        throws ContinuumException
    {
        return new ContinuumProjectBuildingResult();
    }
}
