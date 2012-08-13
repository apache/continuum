package org.apache.continuum.buildagent.installation;

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

import org.apache.maven.continuum.execution.ExecutorConfigurator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import java.util.HashMap;
import java.util.Map;

/**
 * @plexus.component role="org.apache.continuum.buildagent.installation.BuildAgentInstallationService" role-hint="default"
 */
public class DefaultBuildAgentInstallationService
    implements BuildAgentInstallationService, Initializable
{
    private Map<String, ExecutorConfigurator> typesValues;

    public ExecutorConfigurator getExecutorConfigurator( String type )
    {
        return this.typesValues.get( type );
    }

    public void initialize()
        throws InitializationException
    {
        this.typesValues = new HashMap<String, ExecutorConfigurator>();
        this.typesValues.put( BuildAgentInstallationService.ANT_TYPE, new ExecutorConfigurator( "ant", "bin",
                                                                                                "ANT_HOME",
                                                                                                "-version" ) );

        this.typesValues.put( BuildAgentInstallationService.ENVVAR_TYPE, null );
        this.typesValues.put( BuildAgentInstallationService.JDK_TYPE, new ExecutorConfigurator( "java", "bin",
                                                                                                "JAVA_HOME",
                                                                                                "-version" ) );
        this.typesValues.put( BuildAgentInstallationService.MAVEN1_TYPE, new ExecutorConfigurator( "maven", "bin",
                                                                                                   "MAVEN_HOME",
                                                                                                   "-v" ) );
        this.typesValues.put( BuildAgentInstallationService.MAVEN2_TYPE, new ExecutorConfigurator( "mvn", "bin",
                                                                                                   "M2_HOME", "-v" ) );
    }

    public String getEnvVar( String type )
    {
        ExecutorConfigurator executorConfigurator = this.typesValues.get( type );
        return executorConfigurator == null ? null : executorConfigurator.getEnvVar();
    }
}
