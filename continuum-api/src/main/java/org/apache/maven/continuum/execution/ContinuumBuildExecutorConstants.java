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
 * @author <a href="mailto:olamy at apache.org">olamy</a>
 * @version $Id$
 * @since 11 sept. 07
 */
public class ContinuumBuildExecutorConstants
{

    public static final String MAVEN_TWO_BUILD_EXECUTOR = "maven2";

    public static final String MAVEN_ONE_BUILD_EXECUTOR = "maven-1";

    public static final String ANT_BUILD_EXECUTOR = "ant";

    public static final String SHELL_BUILD_EXECUTOR = "shell";
    
    /**
     * Determines whether the executor type is an m1 or m2 build.
     * 
     * @param type
     * @return true if the excutor type will result in a maven 1 or 2+ build.
     */
    public static boolean isMaven( String type )
    {
        return MAVEN_ONE_BUILD_EXECUTOR.equals( type ) || MAVEN_TWO_BUILD_EXECUTOR.equals( type );
    }

}
