package org.apache.maven.continuum.wagon;

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

import org.apache.maven.continuum.PlexusSpringTestCase;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.notification.MessageContext;
import org.apache.maven.continuum.notification.Notifier;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:nramirez@exist">Napoleon Esmundo C. Ramirez</a>
 */
public class WagonContinuumNotifierTest
    extends PlexusSpringTestCase
{
    private ServletServer server;

    private Notifier notifier;

    private MessageContext context;

    @Before
    public void setUp()
        throws Exception
    {
        server = lookup( ServletServer.class );
        notifier = (Notifier) lookup( Notifier.class.getName(), "wagon" );

        Project project = new Project();
        project.setId( 2 );

        BuildResult build = new BuildResult();
        build.setId( 1 );
        build.setProject( project );
        build.setStartTime( System.currentTimeMillis() );
        build.setEndTime( System.currentTimeMillis() + 1234567 );
        build.setState( ContinuumProjectState.OK );
        build.setTrigger( ContinuumProjectState.TRIGGER_FORCED );
        build.setExitCode( 0 );

        BuildDefinition buildDefinition = new BuildDefinition();
        buildDefinition.setBuildFile( "pom.xml" );

        context = new MessageContext();
        context.setProject( project );
        context.setBuildResult( build );
        context.setBuildDefinition( buildDefinition );

        String basedir = System.getProperty( "basedir" );
        if ( basedir == null )
        {
            throw new Exception( "basedir must be defined" );
        }
    }

    @Test
    public void testSendNotification()
        throws Exception
    {
        notifier.sendMessage( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE, context );
    }
}
