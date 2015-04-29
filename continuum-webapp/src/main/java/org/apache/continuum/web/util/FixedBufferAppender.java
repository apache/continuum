package org.apache.continuum.web.util;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FixedBufferAppender
    extends AppenderSkeleton
{

    private final Queue<String> lines = new ConcurrentLinkedQueue<String>();

    private int linesBuffered = 120;

    private int queued = 0;

    @Override
    protected void append( LoggingEvent event )
    {
        String formattedLine = layout.format( event );

        // Add formatted line (may contain throwable if layout handles)
        lines.add( formattedLine );
        queued++;

        // Add throwable information if layout doesn't handle
        ThrowableInformation throwInfo = event.getThrowableInformation();
        if ( throwInfo != null && layout.ignoresThrowable() )
        {
            for ( String traceLine : throwInfo.getThrowableStrRep() )
            {
                lines.add( String.format( "%s%n", traceLine ) );
                queued++;
            }
        }

        // Shrink the queue back to the desired buffer size (temporarily gets larger)
        while ( queued > linesBuffered )
        {
            lines.remove();
            queued--;
        }
    }

    @Override
    public boolean requiresLayout()
    {
        return true;
    }

    @Override
    public void close()
    {
        lines.clear();
        queued = 0;
    }

    public void setLinesBuffered( int linesBuffered )
    {
        this.linesBuffered = linesBuffered;
    }

    public Collection<String> getLines()
    {
        return Collections.unmodifiableCollection( lines );
    }
}
