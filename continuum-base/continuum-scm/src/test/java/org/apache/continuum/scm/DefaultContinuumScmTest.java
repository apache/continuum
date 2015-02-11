package org.apache.continuum.scm;

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

import org.apache.continuum.scm.manager.ScmManager;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import static org.mockito.Mockito.*;

public class DefaultContinuumScmTest
{
    private ScmManager scmManager;

    private ScmRepository scmRepository;

    private ScmProviderRepository scmProviderRepository;

    private DefaultContinuumScm continuumScm;

    private ContinuumScmConfiguration config;

    @Before
    public void setUp()
        throws ScmRepositoryException, NoSuchScmProviderException
    {
        config = new ContinuumScmConfiguration();
        config.setWorkingDirectory( new File( "1" ) );
        config.setUrl( "scm:svn:http://svn.apache.org/repos/asf/maven/plugins/trunk/maven-clean-plugin" );

        scmManager = mock( ScmManager.class );
        scmRepository = mock( ScmRepository.class );
        scmProviderRepository = mock( ScmProviderRepository.class );
        when( scmManager.makeScmRepository( config.getUrl() ) ).thenReturn( scmRepository );
        when( scmRepository.getProviderRepository() ).thenReturn( scmProviderRepository );

        continuumScm = new DefaultContinuumScm();
        continuumScm.setScmManager( scmManager );
    }

    @Test
    public void testChangeLogWithScmVersion()
        throws Exception
    {
        config.setTag( "1.0-SNAPSHOT" );

        continuumScm.changeLog( config );

        verify( scmManager ).changeLog( any( ScmRepository.class ), any( ScmFileSet.class ), any( ScmVersion.class ),
                                        any( ScmVersion.class ) );
    }

    @Test
    public void testChangeLogWithNoScmVersion()
        throws Exception
    {
        config.setTag( "" );

        continuumScm.changeLog( config );

        verify( scmManager ).changeLog( any( ScmRepository.class ), any( ScmFileSet.class ), any( Date.class ),
                                        isNull( Date.class ), eq( 0 ), isNull( ScmBranch.class ),
                                        isNull( String.class ) );
    }
}