package org.apache.continuum.web.startup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.plexus.spring.PlexusToSpringUtils;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class BuildAgentStartup
    implements ServletContextListener
{
    private static final Logger log = LoggerFactory.getLogger( BuildAgentStartup.class );

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed( ServletContextEvent sce )
    {
        // nothing to do here

    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized( ServletContextEvent sce )
    {
        log.info( "Initializing Build Agent Task Queue Executor" );

        WebApplicationContext wac =
            WebApplicationContextUtils.getRequiredWebApplicationContext( sce.getServletContext() );

        TaskQueueExecutor buildAgent = (TaskQueueExecutor) wac.getBean(
            PlexusToSpringUtils.buildSpringId( TaskQueueExecutor.class, "build-agent" ) );

        TaskQueueExecutor prepareBuildAgent = (TaskQueueExecutor) wac.getBean(
            PlexusToSpringUtils.buildSpringId( TaskQueueExecutor.class, "prepare-build-agent" ) );

        TaskQueueExecutor prepareRelease = (TaskQueueExecutor) wac.getBean(
            PlexusToSpringUtils.buildSpringId( TaskQueueExecutor.class, "prepare-release" ) );

        TaskQueueExecutor performRelease = (TaskQueueExecutor) wac.getBean(
            PlexusToSpringUtils.buildSpringId( TaskQueueExecutor.class, "perform-release" ) );

        TaskQueueExecutor rollbackRelease = (TaskQueueExecutor) wac.getBean(
            PlexusToSpringUtils.buildSpringId( TaskQueueExecutor.class, "rollback-release" ) );
    }
}
