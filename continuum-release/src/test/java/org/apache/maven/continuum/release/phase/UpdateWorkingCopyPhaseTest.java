package org.apache.maven.continuum.release.phase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.continuum.release.config.ContinuumReleaseDescriptor;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.phase.ReleasePhase;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.util.FileUtils;

public class UpdateWorkingCopyPhaseTest
    extends PlexusInSpringTestCase
{
    private UpdateWorkingCopyPhase phase;
    
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        phase = (UpdateWorkingCopyPhase) lookup( ReleasePhase.ROLE, "update-working-copy" );
        
        // set up project scm
        File scmPathFile = new File( getBasedir(), "target/scm-src" ).getAbsoluteFile();
        File scmTargetPathFile = new File( getBasedir(), "/target/scm-test" ).getAbsoluteFile();
        FileUtils.copyDirectoryStructure( scmPathFile, scmTargetPathFile );
    }
    
    public void testWorkingDirDoesNotExist()
        throws Exception
    {
        assertNotNull( phase );
        
        ContinuumReleaseDescriptor releaseDescriptor = createReleaseDescriptor();
        
        File workingDirectory = new File( releaseDescriptor.getWorkingDirectory() );
        
        // assert no working directory yet
        assertFalse( workingDirectory.exists() );
        
        phase.execute( releaseDescriptor, new Settings(), null );
        
        assertTrue( workingDirectory.exists() );
    }
    
    public void testWorkingDirAlreadyExistsWithProjectCheckout()
        throws Exception
    {
        assertNotNull( phase );
        
        ContinuumReleaseDescriptor releaseDescriptor = createReleaseDescriptor();
        
        File workingDirectory = new File( releaseDescriptor.getWorkingDirectory() );
        
        // assert working directory already exists with project checkout
        assertTrue( workingDirectory.exists() );
        assertTrue( workingDirectory.listFiles().length > 0 );
        
        phase.execute( releaseDescriptor, new Settings(), null );
        
        assertTrue( workingDirectory.exists() );
    }
    
    public void testWorkingDirAlreadyExistsNoProjectCheckout()
        throws Exception
    {
        assertNotNull( phase );
        
        ContinuumReleaseDescriptor releaseDescriptor = createReleaseDescriptor();
        
        File workingDirectory = new File( releaseDescriptor.getWorkingDirectory() );
        FileUtils.deleteDirectory( workingDirectory );
        workingDirectory.mkdirs();
        
        // assert empty working directory
        assertTrue( workingDirectory.exists() );
        assertTrue( workingDirectory.listFiles().length == 0 );
        
        phase.execute( releaseDescriptor, new Settings(), null );
        
        assertTrue( workingDirectory.exists() );
    }
    
    private ContinuumReleaseDescriptor createReleaseDescriptor()
    {
        // project source and working directory paths
        String projectUrl = getBasedir() + "/target/scm-test/trunk";
        String workingDirPath = getBasedir() + "/target/test-classes/updateWorkingCopy_working-directory";
        
        // create release descriptor
        ContinuumReleaseDescriptor releaseDescriptor = new ContinuumReleaseDescriptor();
        releaseDescriptor.setScmSourceUrl( "scm:svn:file://" + projectUrl );
        releaseDescriptor.setWorkingDirectory( workingDirPath );
        
        return releaseDescriptor;
    }
}
