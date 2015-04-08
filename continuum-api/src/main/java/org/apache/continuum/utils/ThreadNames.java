package org.apache.continuum.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple utility for creating thread names uniformly.
 */
public class ThreadNames
{
    private static Map<String, Integer> threadCountByName = new HashMap<String, Integer>();

    public static String formatNext( String format, Object... args )
    {
        int nextId;
        String baseName = String.format( format, args );
        synchronized ( threadCountByName )
        {
            if ( !threadCountByName.containsKey( baseName ) )
            {
                nextId = 0;
            }
            else
            {
                nextId = threadCountByName.get( baseName );
            }
            threadCountByName.put( baseName, nextId + 1 );
        }
        return String.format( "%s-%s", baseName, nextId );
    }
}
