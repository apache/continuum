package org.apache.continuum.web.startup;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.maven.continuum.Continuum;
import org.codehaus.plexus.spring.PlexusToSpringUtils;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 15 mars 2008
 * @version $Id$
 */
public class ContinuumStartup
    implements ServletContextListener
{

    private Logger log = LoggerFactory.getLogger( getClass() );
    
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
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext( sce
            .getServletContext() );

        // to simulate Plexus load on start with Spring
        Continuum continuum = (Continuum) wac.getBean( PlexusToSpringUtils.buildSpringId( Continuum.class ) );

        BuildsManager buildsManager = (BuildsManager) wac.getBean( PlexusToSpringUtils.buildSpringId( BuildsManager.class, "parallel" ) );

        TaskQueueExecutor prepareRelease = (TaskQueueExecutor) wac.getBean( PlexusToSpringUtils
            .buildSpringId( TaskQueueExecutor.class, "prepare-release" ) );

        TaskQueueExecutor performRelease = (TaskQueueExecutor) wac.getBean( PlexusToSpringUtils
            .buildSpringId( TaskQueueExecutor.class, "perform-release" ) );

        TaskQueueExecutor rollbackRelease = (TaskQueueExecutor) wac.getBean( PlexusToSpringUtils
            .buildSpringId( TaskQueueExecutor.class, "rollback-release" ) );        
        
        TaskQueueExecutor purge = (TaskQueueExecutor) wac.getBean( PlexusToSpringUtils
            .buildSpringId( TaskQueueExecutor.class, "purge" ) );

        TaskQueueExecutor prepareBuildProject = (TaskQueueExecutor) wac.getBean( PlexusToSpringUtils
            .buildSpringId( TaskQueueExecutor.class, "prepare-build-project" ) );

        DistributedBuildManager distributedBuildManager = (DistributedBuildManager) wac.getBean( PlexusToSpringUtils
            .buildSpringId( DistributedBuildManager.class ) );
    }

}
