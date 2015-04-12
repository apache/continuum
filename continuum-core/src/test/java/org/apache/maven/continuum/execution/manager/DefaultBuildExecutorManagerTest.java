package org.apache.maven.continuum.execution.manager;

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

import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.execution.AbstractBuildExecutor;
import org.apache.maven.continuum.execution.ant.AntBuildExecutor;
import org.apache.maven.continuum.execution.maven.m1.MavenOneBuildExecutor;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.execution.shell.ShellBuildExecutor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class DefaultBuildExecutorManagerTest
    extends AbstractContinuumTest
{
    private BuildExecutorManager builderManager;

    @Before
    public void setUp()
        throws Exception
    {
        builderManager = lookup( BuildExecutorManager.class );
    }

    @Test
    public void testMavenTwoBuildExecutorDependencyInjection()
        throws Exception
    {
        MavenTwoBuildExecutor executor = (MavenTwoBuildExecutor) builderManager.getBuildExecutor(
            MavenTwoBuildExecutor.ID );

        assertCommonFields( executor );
        assertNotNull( executor.getBuilderHelper() );
        assertNotNull( executor.getProjectHelper() );
        assertNotNull( executor.getConfigurationService() );
    }

    @Test
    public void testMavenOneBuildExecutorDependencyInjection()
        throws Exception
    {
        MavenOneBuildExecutor executor = (MavenOneBuildExecutor) builderManager.getBuildExecutor(
            MavenOneBuildExecutor.ID );

        assertCommonFields( executor );
        assertNotNull( executor.getMetadataHelper() );
    }

    @Test
    public void testAntBuildExecutorDependencyInjection()
        throws Exception
    {
        AntBuildExecutor executor = (AntBuildExecutor) builderManager.getBuildExecutor( AntBuildExecutor.ID );

        assertCommonFields( executor );
    }

    @Test
    public void testShellBuildExecutorDependencyInjection()
        throws Exception
    {
        ShellBuildExecutor executor = (ShellBuildExecutor) builderManager.getBuildExecutor( ShellBuildExecutor.ID );

        assertCommonFields( executor );
    }

    private void assertCommonFields( AbstractBuildExecutor executor )
    {
        assertNotNull( executor.getShellCommandHelper() );
        assertNotNull( executor.getExecutableResolver() );
        assertNotNull( executor.getWorkingDirectoryService() );
        assertNotNull( executor.getInstallationService() );
    }
}
