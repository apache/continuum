package org.apache.continuum.webdav.util;

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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WorkingCopyPathUtilTest
{

    @Test
    public void testGetProjectId()
    {
        String href = "/path/1/src/main/java";
        assertEquals( 1, WorkingCopyPathUtil.getProjectId( href ) );

        href = "path/2/src/test";
        assertEquals( 2, WorkingCopyPathUtil.getProjectId( href ) );
    }

    @Test
    public void testGetLogicalPath()
    {
        String href = "/workingcopy/1/src/main/java/org/apache/maven/someartifact.jar";
        assertEquals( "/src/main/java/org/apache/maven/someartifact.jar", WorkingCopyPathUtil.getLogicalResource(
            href ) );

        href = "workingcopy/1/src/main/java/org/apache/maven/someartifact.jar";
        assertEquals( "/src/main/java/org/apache/maven/someartifact.jar", WorkingCopyPathUtil.getLogicalResource(
            href ) );

        href = "workingcopy/1/src/main/java/";
        assertEquals( "/src/main/java/", WorkingCopyPathUtil.getLogicalResource( href ) );

        href = "workingcopy";
        assertEquals( "/", WorkingCopyPathUtil.getLogicalResource( href ) );
    }
}
