package org.apache.continuum;

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

import org.apache.continuum.utils.m2.LocalRepositoryHelper;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.execution.SettingsConfigurationException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

public abstract class AbstractAddProjectTest
    extends AbstractContinuumTest
{
    private static void mkdirs( File directory )
        throws IOException
    {
        if ( !directory.exists() && !directory.mkdirs() )
        {
            throw new IOException( "Unable to create repository " + directory );
        }
    }

    protected void createLocalRepository()
        throws IOException, SettingsConfigurationException
    {
        LocalRepositoryHelper helper = (LocalRepositoryHelper) lookup( LocalRepositoryHelper.class );
        ArtifactRepository repo = helper.getLocalRepository();

        File localRepo = new File( repo.getBasedir() );
        mkdirs( localRepo );

        File artifact = new File( localRepo,
                                  "org/apache/maven/continuum/continuum-parent/1.0.3/continuum-parent-1.0.3.pom" );
        mkdirs( artifact.getParentFile() );

        FileUtils.copyFile( getTestFile( "src/test/resources/projects/continuum/pom.xml" ), artifact );
    }
}
