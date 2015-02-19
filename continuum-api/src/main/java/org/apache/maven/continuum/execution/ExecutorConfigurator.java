package org.apache.maven.continuum.execution;

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
 * @author <a href="mailto:olamy@codehaus.org">olamy</a>
 * @since 19 juin 07
 */
public class ExecutorConfigurator
{
    private String executable;

    private String relativePath;

    private String envVar;

    private String versionArgument;


    public ExecutorConfigurator()
    {
        // nothing
    }

    public ExecutorConfigurator( String executable, String relativePath, String envVar, String versionArgument )
    {
        this.executable = executable;
        this.relativePath = relativePath;
        this.envVar = envVar;
        this.versionArgument = versionArgument;
    }

    /**
     * @return mvn for maven2 ExecutorConfigurator
     */
    public String getExecutable()
    {
        return executable;
    }

    public void setExecutable( String executable )
    {
        this.executable = executable;
    }

    /**
     * @return bin for maven2 ExecutorConfigurator
     */
    public String getRelativePath()
    {
        return relativePath;
    }

    public void setRelativePath( String relativePath )
    {
        this.relativePath = relativePath;
    }

    /**
     * @return M2_HOME for maven2 ExecutorConfigurator
     */
    public String getEnvVar()
    {
        return envVar;
    }

    public void setEnvVar( String envVar )
    {
        this.envVar = envVar;
    }

    /**
     * @return the versionArgument
     */
    public String getVersionArgument()
    {
        return versionArgument;
    }

    /**
     * @param versionArgument the versionArgument to set
     */
    public void setVersionArgument( String versionArgument )
    {
        this.versionArgument = versionArgument;
    }

}
