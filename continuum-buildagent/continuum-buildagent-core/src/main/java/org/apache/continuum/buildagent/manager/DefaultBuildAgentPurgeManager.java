package org.apache.continuum.buildagent.manager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @plexus.component role="org.apache.continuum.buildagent.manager.BuildAgentPurgeManager" role-hint="default"
 */
public class DefaultBuildAgentPurgeManager
    implements BuildAgentPurgeManager
{
    private static final Logger logger = LoggerFactory.getLogger( DefaultBuildAgentPurgeManager.class );

    /**
     * @plexus.requirement
     */
    private BuildAgentConfigurationService buildAgentConfigurationService;
    
    @Override
    public void executeDirectoryPurge( String directoryType, int daysOlder, int retentionCount, boolean deleteAll )
        throws Exception
    {
        StringBuilder log = new StringBuilder().append( "Executing directory purge with the following settings[directoryType=" ).
                                                append( directoryType ).append( ",daysOlder=" ).
                                                append( daysOlder ).append( ", retentionCount=" ).
                                                append( retentionCount ).append( ", deleteAll=" ).
                                                append( deleteAll ).append( "]" );
        logger.info( log.toString() );
        
        File directory = null;
        
        if ( "working".equals( directoryType ) || "releases".equals( directoryType ) )
        {
            directory = buildAgentConfigurationService.getWorkingDirectory();
        }
        else
        {
            logger.warn( "Cannot execute purge: DirectoryType: " + directoryType + " is not valid." );
            return;
        }
        if ( deleteAll )
        {
            purgeAll( directory, directoryType );
        }
        else
        {
            purgeFiles( directory, directoryType, daysOlder, retentionCount );
        }
        
        logger.info( "Directory purge execution done" );
    }
    
    private void purgeAll( File directory, String directoryType ) throws Exception
    {
        AndFileFilter filter = new AndFileFilter();
        filter.addFileFilter( DirectoryFileFilter.DIRECTORY );
        filter.addFileFilter( createFileFilterForDirectoryType( directoryType ) );
        
        File[] files = directory.listFiles( ( FileFilter ) filter );
        if  ( files == null )
        {
            return;
        }
        for ( File file : files )
        {
            try
            {
                FileUtils.deleteDirectory( file );
            }
            catch ( IOException e )
            {
                logger.warn( "Unable to purge " + directoryType + " directory: " + file.getName() );
            }
        }
    }
    
    private void purgeFiles( File directory, String directoryType, int daysOlder, int retentionCount )
    {
        AndFileFilter filter = new AndFileFilter();
        filter.addFileFilter( DirectoryFileFilter.DIRECTORY );
        filter.addFileFilter( createFileFilterForDirectoryType( directoryType ) );
        
        
        File[] files = directory.listFiles( ( FileFilter ) filter );
        
        if ( files == null )
        {
            return;
        }
        
        //calculate to include files not in the dayold category
        int countToPurge = files.length - retentionCount;
        
        if ( daysOlder > 0 )
        {
            long cutoff = System.currentTimeMillis() - ( 24 * 60 * 26 * 1000 * daysOlder );
            filter.addFileFilter( new AgeFileFilter( cutoff ) );
        }
        
        files = directory.listFiles( ( FileFilter ) filter );
        
        if ( files == null )
        {
            return;
        }
        
        Arrays.sort( files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR );
        
        for ( File file : files )
        {
            if ( countToPurge - 1 < 0 )
            {
                break;
            }
            try
            {
                FileUtils.deleteDirectory( file );
                countToPurge--;
            }
            catch ( IOException e )
            {
                logger.warn( "Unable to purge " + directoryType + " directory: " + file.getName() );
            }
        }
        
    }
    
    private IOFileFilter createFileFilterForDirectoryType( String directoryType )
    {
        WildcardFileFilter releasesFilter = new WildcardFileFilter( "releases-*" );
        
        if ( "working".equals( directoryType ) )
        {
            return new NotFileFilter( releasesFilter );
        } 
        else if ( "releases".equals( directoryType ) )
        {
            return releasesFilter;
        }
        else
        {
            return null;
        }
    }
    
    public void setBuildAgentConfigurationService( BuildAgentConfigurationService buildAgentConfigurationService )
    {
        this.buildAgentConfigurationService = buildAgentConfigurationService;
    }
}
