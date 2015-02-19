package org.apache.maven.continuum.installation;

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
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.profile.AlreadyExistsProfileException;

import java.util.List;

/**
 * @author <a href="mailto:olamy@codehaus.org">olamy</a>
 * @since 13 juin 07
 */
public interface InstallationService
{
    String ROLE = InstallationService.class.getName();

    String JDK_TYPE = "jdk";

    String MAVEN2_TYPE = "maven2";

    String MAVEN1_TYPE = "maven1";

    String ANT_TYPE = "ant";

    String ENVVAR_TYPE = "envvar";

    public Installation add( Installation installation, boolean automaticProfile )
        throws InstallationException, AlreadyExistsProfileException, AlreadyExistsInstallationException;

    public Installation add( Installation installation )
        throws InstallationException, AlreadyExistsInstallationException;

    public void update( Installation installation )
        throws InstallationException, AlreadyExistsInstallationException;

    public void delete( Installation installation )
        throws InstallationException;

    public Installation getInstallation( int installationId )
        throws InstallationException;

    public Installation getInstallation( String installationName )
        throws InstallationException;

    public List<Installation> getAllInstallations()
        throws InstallationException;

    public String getEnvVar( String type );

    /**
     * @param type
     * @return ExecutorConfigurator or null if unknown type
     */
    public ExecutorConfigurator getExecutorConfigurator( String type );


    /**
     * @param installation
     * @return output of JAVA_HOME/bin/java -version (JAVA_HOME = installation.getVarValue()
     * @throws InstallationException
     */
    public List<String> getJavaVersionInfo( Installation installation )
        throws InstallationException;

    /**
     * @return output of JAVA_HOME/bin/java -version
     * @throws InstallationException
     */
    public List<String> getDefaultJavaVersionInfo()
        throws InstallationException;

    /**
     * @param path
     * @param executorConfigurator (ec)
     * @return the cli output of $path/ec.relativePath.ec.executable ec.versionArgument
     * @throws InstallationException
     */
    public List<String> getExecutorVersionInfo( String path, ExecutorConfigurator executorConfigurator,
                                                Profile profile )
        throws InstallationException;

}
