package org.apache.continuum.distributed.transport.master;

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

import com.atlassian.xmlrpc.ServiceObject;

import java.util.Map;

/**
 * MasterBuildAgentTransportService
 */
@ServiceObject( "MasterBuildAgentTransportService" )
public interface MasterBuildAgentTransportService
{
    public Boolean returnBuildResult( Map<String, Object> buildResult, String buildAgentUrl )
        throws Exception;

    public Boolean startProjectBuild( Integer projectId, Integer buildDefinitionId, String buildAgentUrl )
        throws Exception;

    public Boolean prepareBuildFinished( Map<String, Object> prepareBuildResult, String buildAgentUrl )
        throws Exception;

    public Boolean startPrepareBuild( Map<String, Object> prepareBuildResult, String buildAgentUrl )
        throws Exception;

    public Map<String, String> getEnvironments( Integer buildDefinitionId, String installationType )
        throws Exception;

    public Boolean updateProject( Map<String, Object> project )
        throws Exception;

    public Boolean ping()
        throws Exception;

    public Boolean shouldBuild( Map<String, Object> context, String buildAgentUrl )
        throws Exception;
}
