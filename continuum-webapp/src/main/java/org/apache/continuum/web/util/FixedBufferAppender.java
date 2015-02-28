package org.apache.continuum.web.util;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

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
        lines.add( formattedLine );
        queued++;
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
