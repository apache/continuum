package org.apache.continuum.distributed.transport.master;

import java.util.Map;

import com.atlassian.xmlrpc.ServiceObject;

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

/**
 * MasterBuildAgentTransportService
 */
@ServiceObject( "MasterBuildAgentTransportService" )
public interface MasterBuildAgentTransportService
{
    public Boolean returnBuildResult( Map buildResult ) throws Exception;

    public Boolean startProjectBuild( Integer projectId ) throws Exception;

    public Boolean prepareBuildFinished( Map prepareBuildResult ) throws Exception; 

    public Boolean startPrepareBuild( Map prepareBuildResult ) throws Exception;

    public Map<String, String> getEnvironments( Integer buildDefinitionId, String installationType ) throws Exception;

    public Boolean updateProject( Map project ) throws Exception;

    public Boolean ping() throws Exception;
}
