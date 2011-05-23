package org.apache.continuum.scm;

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
