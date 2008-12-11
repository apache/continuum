package org.apache.continuum.plugin.notification;

import org.apache.continuum.model.BuildResult;
import org.apache.continuum.model.ScmResult;
import org.apache.continuum.plugin.api.SendNotificationPlugin;
import org.apache.continuum.plugin.api.context.ProjectInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class MyNotificationPlugin
    implements SendNotificationPlugin
{
    private Logger log = LoggerFactory.getLogger( MyNotificationPlugin.class );

    public String getName()
    {
        return this.getClass().getName();
    }

    public void execute( ProjectInformation projectInformation, ScmResult scmResult, BuildResult buildResult )
    {
        log.info( "BuildResut = " + buildResult.getResult() );
    }
}
