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

import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.codehaus.plexus.action.AbstractAction;

import java.util.Map;

/**
 * This is used for testing the fix for CONTINUUM-2391. See BuildPRrojectTaskExecutorTest.java and
 * BuildProjectTaskExecutorTest.xml for details.
 */
public class ContinuumActionStub
    extends AbstractAction
{
    public void execute( Map context )
        throws Exception
    {
        if ( !ContinuumBuildAgentUtil.getLocalRepository( context ).equals( "/home/user/.m2/repository" ) )
        {
            throw new Exception( "Local repository set in the build context should not have been a full path!" );
        }
    }
}
