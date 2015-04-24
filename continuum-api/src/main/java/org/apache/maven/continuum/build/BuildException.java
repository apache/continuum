package org.apache.maven.continuum.build;

import org.apache.maven.continuum.ContinuumException;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Represents an exceptional build condition.
 */
public class BuildException
    extends ContinuumException
{
    private String key;

    public BuildException( String message, String key )
    {
        super( message );
        this.key = key;
    }

    @Override
    public String getLocalizedMessage()
    {
        try
        {
            return ResourceBundle.getBundle( "localization/Continuum" ).getString( key );
        }
        catch ( MissingResourceException mre )
        {
            return getMessage();
        }
    }
}
