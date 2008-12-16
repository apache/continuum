package org.apache.continuum.buildagent.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ContinuumUtils
{
    public static final String EOL = System.getProperty( "line.separator" );

    public static String throwableToString( Throwable error )
    {
        if ( error == null )
        {
            return "";
        }

        StringWriter writer = new StringWriter();

        PrintWriter printer = new PrintWriter( writer );

        error.printStackTrace( printer );

        printer.flush();

        return writer.getBuffer().toString();
    }

    public static String throwableMessagesToString( Throwable error )
    {
        if ( error == null )
        {
            return "";
        }

        StringBuffer buffer = new StringBuffer();

        buffer.append( error.getMessage() );

        error = error.getCause();

        while ( error != null )
        {
            buffer.append( EOL );

            buffer.append( error.getMessage() );

            error = error.getCause();
        }

        return buffer.toString();
    }
}
