package org.apache.continuum.plugin.build;

import org.apache.continuum.model.BuildResult;
import org.apache.continuum.model.ScmResult;
import org.apache.continuum.plugin.api.BuildProjectPlugin;
import org.apache.continuum.plugin.api.context.ProjectInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class BuildPlugin
    implements BuildProjectPlugin
{
    Logger log = LoggerFactory.getLogger( BuildPlugin.class );

    public BuildPlugin()
    {
        log.info( "Starting " + getName() );
    }

    public String getName()
    {
        return getClass().getName();
    }

    public void execute( ProjectInformation projectInformation, ScmResult scmResult, BuildResult buildResult )
    {
        log.info( "Executing " + getName() );
        buildResult.setResult( "OK" );
    }
}
