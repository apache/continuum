package org.apache.continuum.utils.release;

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

import java.util.List;
import java.util.Map;

public interface ReleaseHelper
{

    /**
     * Extracts parameters specified for the maven-release-plugin from the given project's metadata.
     *
     * @param workingDirectory working directory of project containing pom file
     * @param pomFilename      the name of the pom file in working directory
     * @return a map consisting of the release plugin parameters from project metadata
     * @throws Exception
     */
    Map<String, Object> extractPluginParameters( String workingDirectory, String pomFilename )
        throws Exception;

    /**
     * Constructs a list of release preparation parameters for the given project and its modules. The parameter map for
     * each project consists of:
     * <ul>
     * <li>key - groupId:artifactId</li>
     * <li>name - name or artifactId if none</li>
     * <li>dev - the version the project will assume after preparation</li>
     * <li>release - the version the project will ultimately released as when performing</li>
     * </ul>
     *
     * @param workingDirectory      working directory of project
     * @param pomFilename           the filename of the pom inside the working directory
     * @param autoVersionSubmodules true sets all modules to the root project's version, false uses module versions
     * @param projects              the resulting list of parameter maps for the project and its modules
     * @throws Exception
     */
    public void buildVersionParams( String workingDirectory, String pomFilename, boolean autoVersionSubmodules,
                                    List<Map<String, String>> projects )
        throws Exception;
}
