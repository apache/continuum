package org.apache.maven.continuum.notification.mail;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.continuum.notification.mail.MockJavaMailSender;
import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.notification.MessageContext;
import org.apache.maven.continuum.notification.Notifier;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MailContinuumNotifierTest
    extends AbstractContinuumTest
{
    
    protected Logger logger = LoggerFactory.getLogger( getClass() );
    
    public void testSuccessfulBuild()
        throws Exception
    {
        MailContinuumNotifier notifier = (MailContinuumNotifier) lookup( Notifier.class.getName(), "mail" );
        String toOverride = "recipient@host.com";
        notifier.setToOverride( toOverride );

        ProjectGroup group = createStubProjectGroup( "foo.bar", "" );

        Project project = addProject( "Test Project", group );

        BuildResult build = makeBuild( ContinuumProjectState.OK );

        MimeMessage mailMessage = sendNotificationAndGetMessage( project, build, "lots out build output", toOverride );

        assertEquals( "[continuum] BUILD SUCCESSFUL: foo.bar Test Project", mailMessage.getSubject() );

        dumpContent( mailMessage, "recipient@host.com" );
    }

    public void testFailedBuild()
        throws Exception
    {
        ProjectGroup group = createStubProjectGroup( "foo.bar", "" );

        Project project = addProject( "Test Project", group );

        BuildResult build = makeBuild( ContinuumProjectState.FAILED );

        MimeMessage mailMessage = sendNotificationAndGetMessage( project, build, "output", null );

        assertEquals( "[continuum] BUILD FAILURE: foo.bar Test Project", mailMessage.getSubject() );

        dumpContent( mailMessage );
    }

    public void testErrorenousBuild()
        throws Exception
    {
        ProjectGroup group = createStubProjectGroup( "foo.bar", "" );

        Project project = addProject( "Test Project", group );

        BuildResult build = makeBuild( ContinuumProjectState.ERROR );

        build.setError( "Big long error message" );

        MimeMessage mailMessage = sendNotificationAndGetMessage( project, build, "lots of stack traces", null );

        assertEquals( "[continuum] BUILD ERROR: foo.bar Test Project", mailMessage.getSubject() );

        dumpContent( mailMessage );
    }

    private void dumpContent( MimeMessage mailMessage )
        throws Exception
    {
        dumpContent( mailMessage, null );
    }

    private void dumpContent( MimeMessage mailMessage, String toOverride )
        throws Exception
    {
        Address[] tos  = mailMessage.getRecipients( RecipientType.TO );
        if ( toOverride != null )
        {
            assertEquals( toOverride, ( (InternetAddress) tos[ 0 ] ).getAddress() );
        }
        else
        {
            assertEquals( "foo@bar", ( (InternetAddress) tos[ 0 ] ).getAddress() );
        }
        assertTrue( "The template isn't loaded correctly.",
                    ((String)mailMessage.getContent()).indexOf( "#shellBuildResult()" ) < 0 );
        assertTrue( "The template isn't loaded correctly.",
                    ((String)mailMessage.getContent()).indexOf( "Build statistics" ) > 0 );

        if ( true )
        {
            logger.info( ((String)mailMessage.getContent()) );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private MimeMessage sendNotificationAndGetMessage( Project project, BuildResult build, String buildOutput, String toOverride )
        throws Exception
    {
        MessageContext context = new MessageContext();

        context.setProject( project );

        context.setBuildResult( build );

        ProjectNotifier projectNotifier = new ProjectNotifier();
        projectNotifier.setType( "mail" );
        Map<String, String> config = new HashMap<String, String>();
        config.put( MailContinuumNotifier.ADDRESS_FIELD, "foo@bar" );
        projectNotifier.setConfiguration( config );
        List<ProjectNotifier> projectNotifiers = new ArrayList<ProjectNotifier>();
        projectNotifiers.add( projectNotifier );
        context.setNotifier( projectNotifiers );

        //context.put( ContinuumNotificationDispatcher.CONTEXT_BUILD_OUTPUT, buildOutput );

        //context.put( "buildHost", "foo.bar.com" );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        Notifier notifier = (Notifier) lookup( Notifier.class.getName(), "mail" );

        ( (MailContinuumNotifier) notifier ).setBuildHost( "foo.bar.com" );

        notifier.sendMessage( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE, context );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        MockJavaMailSender mailSender = (MockJavaMailSender) lookup( JavaMailSender.class, "continuum" );

        assertEquals( 1, mailSender.getReceivedEmails().size() );

        List<MimeMessage> mails = mailSender.getReceivedEmails();

        MimeMessage mailMessage = (MimeMessage) mails.get( 0 );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertEquals( "continuum@localhost", ((InternetAddress) mailMessage.getFrom()[0]).getAddress() );

        assertEquals( "Continuum", ((InternetAddress) mailMessage.getFrom()[0]).getPersonal() );

        Address[] tos  = mailMessage.getRecipients( RecipientType.TO );

        assertEquals( 1, tos.length );

        assertEquals(toOverride == null ? "foo@bar" : toOverride, ( (InternetAddress) tos[0] ).getAddress() );

        return mailMessage;
    }

    private BuildResult makeBuild( int state )
    {
        BuildResult build = new BuildResult();

        build.setId( 17 );

        build.setStartTime( System.currentTimeMillis() );

        build.setEndTime( System.currentTimeMillis() + 1234567 );

        build.setState( state );

        build.setTrigger( ContinuumProjectState.TRIGGER_FORCED );

        build.setExitCode( 10 );

        return build;
    }
}
