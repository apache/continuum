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

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @version
 */
public class ContinuumScmUtilsTest
    extends TestCase
{
    public void testGitProviderWithSSHProtocolUsernameInUrl()
        throws Exception
    {
        ContinuumScmConfiguration scmConfiguration = new ContinuumScmConfiguration();

        scmConfiguration = ContinuumScmUtils.setSCMCredentialsforSSH( scmConfiguration,
                           "scm:git:ssh://sshUser@gitrepo.com/myproject.git", "dummyuser", "dummypassword" );

        assertEquals( "sshUser", scmConfiguration.getUsername() );
        assertTrue( StringUtils.isBlank( scmConfiguration.getPassword() ) );
    }

    public void testGitProviderWithSSHProtocolUsernameAndPasswordInUrl()
        throws Exception
    {
        ContinuumScmConfiguration scmConfiguration = new ContinuumScmConfiguration();

        scmConfiguration = ContinuumScmUtils.setSCMCredentialsforSSH( scmConfiguration,
                           "scm:git:ssh://sshUser:sshPassword@gitrepo.com/myproject.git", "dummyuser", "dummypassword" );
        
        assertEquals( "sshUser", scmConfiguration.getUsername() );
        assertEquals( "sshPassword", scmConfiguration.getPassword() );
    }

    public void testGitProviderWithSSHProtocolNoCredentialsInUrl()
        throws Exception
    {
        ContinuumScmConfiguration scmConfiguration = new ContinuumScmConfiguration();

        scmConfiguration = ContinuumScmUtils.setSCMCredentialsforSSH( scmConfiguration,
                           "scm:git:ssh://gitrepo.com/myproject.git", "dummyuser", "dummypassword" );

        assertEquals( "dummyuser", scmConfiguration.getUsername() );
        assertEquals( "dummypassword", scmConfiguration.getPassword() );
    }

    public void testNotGitProvider()
        throws Exception
    {
        ContinuumScmConfiguration scmConfiguration = new ContinuumScmConfiguration();

        scmConfiguration = ContinuumScmUtils.setSCMCredentialsforSSH( scmConfiguration,
                           "scm:svn:ssh://svnrepo.com/repos/myproject/trunk", "dummyuser", "dummypassword" );

        assertEquals( "dummyuser", scmConfiguration.getUsername() );
        assertEquals( "dummypassword", scmConfiguration.getPassword() );
    }

    public void testNotSSHProtocol()
        throws Exception
    {
        ContinuumScmConfiguration scmConfiguration = new ContinuumScmConfiguration();

        scmConfiguration = ContinuumScmUtils.setSCMCredentialsforSSH( scmConfiguration,
                           "scm:git:https://gitrepo.com/myproject.git", "dummyuser", "dummypassword" );

        assertEquals( "dummyuser", scmConfiguration.getUsername() );
        assertEquals( "dummypassword", scmConfiguration.getPassword() );
    }
}
