package org.apache.maven.continuum.web.action;

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

import org.apache.continuum.web.action.AbstractActionTest;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.project.Project;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Test for {@link ReleasePrepareAction}
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 */
public class ReleasePrepareActionTest
    extends AbstractActionTest
{
    private ReleasePrepareAction action;

    private Continuum continuum;

    @Before
    public void setUp()
        throws Exception
    {
        action = new ReleasePrepareAction();
        continuum = mock( Continuum.class );
        //securitySessionMock = mock( SecuritySession.class );
        //Map map = new HashMap();
        //map.put( SecuritySystemConstants.SECURITY_SESSION_KEY, securitySessionMock );
        //action.setSession( map );
        action.setContinuum( continuum );
    }

    /**
     * Test that the tag base url for Subversion is correctly constructed
     *
     * @throws Exception
     */
    @Test
    public void testScmTagBaseSvn()
        throws Exception
    {
        //commented out because of problems in authorization checks

        String svnUrl = "https://svn.apache.org/repos/asf/maven/continuum";
        String scmUrl = "scm:svn:" + svnUrl + "/trunk/";
        //ProjectGroup projectGroup = new ProjectGroup();
        //continuum.expects( once() ).method( "getProjectGroupByProjectId" ).will( returnValue( projectGroup ) );
        Project project = new Project();
        project.setScmUrl( scmUrl );
        project.setWorkingDirectory( "." );
        when( continuum.getProject( anyInt() ) ).thenReturn( project );
        action.input(); // expected result?
        assertEquals( svnUrl + "/tags", action.getScmTagBase() );
    }

    /**
     * Test that tag base url for non Subverson SCMs is empty
     *
     * @throws Exception
     */
    @Test
    public void testScmTagBaseNonSvn()
        throws Exception
    {
        //commented out because of problems in authorization checks

        Project project = new Project();
        project.setScmUrl( "scm:cvs:xxx" );
        project.setWorkingDirectory( "." );
        when( continuum.getProject( anyInt() ) ).thenReturn( project );
        action.input(); // expected result?
        assertEquals( "", action.getScmTagBase() );
    }
}
