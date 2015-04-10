package org.apache.maven.continuum.execution.shared;

import org.apache.continuum.utils.file.FileSystemManager;
import org.codehaus.plexus.util.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class JUnitReportArchiver
{
    private static Logger log = LoggerFactory.getLogger( JUnitReportArchiver.class );

    private String[] includeFiles = {};

    private String[] excludeFiles = {};

    private FileSystemManager fsManager;

    public void setIncludeFiles( String[] includeFiles )
    {
        this.includeFiles = includeFiles;
    }

    public void setExcludeFiles( String[] excludeFiles )
    {
        this.excludeFiles = excludeFiles;
    }

    public void setFileSystemManager( FileSystemManager fsManager )
    {
        this.fsManager = fsManager;
    }

    public String[] findReports( File workingDir )
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( workingDir );
        scanner.setIncludes( includeFiles );
        scanner.setExcludes( excludeFiles );
        scanner.scan();
        return scanner.getIncludedFiles();
    }

    public void archiveReports( File workingDir, File backupDir )
        throws IOException
    {
        String[] testResultFiles = findReports( workingDir );
        if ( testResultFiles.length > 0 )
        {
            log.info( "Backing up {} test reports", testResultFiles.length );
        }
        for ( String testResultFile : testResultFiles )
        {
            File xmlFile = new File( workingDir, testResultFile );
            if ( backupDir != null )
            {
                fsManager.copyFileToDir( xmlFile, backupDir );
            }
        }
    }
}
