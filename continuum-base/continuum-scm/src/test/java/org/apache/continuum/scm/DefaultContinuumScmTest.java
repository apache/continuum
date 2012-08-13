package org.apache.continuum.scm;

import junit.framework.TestCase;
import org.apache.continuum.scm.manager.ScmManager;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.repository.ScmRepository;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.util.Date;

public class DefaultContinuumScmTest
    extends TestCase
{
    private ScmManager scmManager;

    private DefaultContinuumScm continuumScm;

    private Mockery context;

    private ContinuumScmConfiguration config;

    @Override
    public void setUp()
    {
        context = new JUnit3Mockery();
        context.setImposteriser( ClassImposteriser.INSTANCE );

        scmManager = context.mock( ScmManager.class );

        continuumScm = new DefaultContinuumScm();
        continuumScm.setScmManager( scmManager );

        config = getScmConfiguration();
    }

    public void testChangeLogWithScmVersion()
        throws Exception
    {
        config.setTag( "1.0-SNAPSHOT" );

        context.checking( new Expectations()
        {
            {
                one( scmManager ).makeScmRepository( config.getUrl() );
                one( scmManager ).changeLog( with( any( ScmRepository.class ) ), with( any( ScmFileSet.class ) ), with(
                    any( ScmVersion.class ) ), with( any( ScmVersion.class ) ) );
            }
        } );

        continuumScm.changeLog( config );

        context.assertIsSatisfied();
    }

    public void testChangeLogWithNoScmVersion()
        throws Exception
    {
        config.setTag( "" );

        context.checking( new Expectations()
        {
            {
                one( scmManager ).makeScmRepository( config.getUrl() );
                one( scmManager ).changeLog( with( any( ScmRepository.class ) ), with( any( ScmFileSet.class ) ), with(
                    any( Date.class ) ), with( aNull( Date.class ) ), with( equal( 0 ) ), with( aNull(
                    ScmBranch.class ) ), with( aNull( String.class ) ) );
            }
        } );

        continuumScm.changeLog( config );
        context.assertIsSatisfied();
    }

    private ContinuumScmConfiguration getScmConfiguration()
    {
        ContinuumScmConfiguration config = new ContinuumScmConfiguration();
        config.setWorkingDirectory( new File( "1" ) );
        config.setUrl( "scm:svn:http://svn.apache.org/repos/asf/maven/plugins/trunk/maven-clean-plugin" );

        return config;
    }
}
