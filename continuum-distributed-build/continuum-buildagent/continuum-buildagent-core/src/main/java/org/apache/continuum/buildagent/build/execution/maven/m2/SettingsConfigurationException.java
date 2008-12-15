package org.apache.continuum.buildagent.build.execution.maven.m2;

public class SettingsConfigurationException
    extends Exception
    {
    private int lineNumber;
    
    private int columnNumber;
    
    public SettingsConfigurationException( String message )
    {
        super( message );
    }
    
    public SettingsConfigurationException( String message, Throwable cause )
    {
        super( message, cause );
    }
    
    public SettingsConfigurationException( String message, Throwable cause, int lineNumber, int columnNumber )
    {
        super( message + ( lineNumber > 0 ? "\n  Line:   " + lineNumber : "" ) +
            ( columnNumber > 0 ? "\n  Column: " + columnNumber : "" ), cause );
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
    
    public int getColumnNumber()
    {
        return columnNumber;
    }
    
    public int getLineNumber()
    {
        return lineNumber;
    }
}