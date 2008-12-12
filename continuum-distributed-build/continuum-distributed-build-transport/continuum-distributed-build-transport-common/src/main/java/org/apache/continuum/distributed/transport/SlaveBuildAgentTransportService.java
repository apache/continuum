package org.apache.continuum.distributed.transport;

import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.model.Installation;

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
 * SlaveBuildAgentTransportService
 */
@ServiceObject( "SlaveBuildAgentTransportService" )
public interface SlaveBuildAgentTransportService
{
    public Boolean buildProjects( List<Map> projectsBuildContext ) throws Exception;
    
    public Map getBuildResult( int projectId ) throws Exception;
    
    public Integer getProjectCurrentlyBuilding() throws Exception;
    
    public List<Installation> getAvailableInstallations() throws Exception;
    
    public Boolean isBusy() throws Exception;
    
    public Boolean ping() throws Exception;

    public Boolean cancelBuild() throws Exception;
}
