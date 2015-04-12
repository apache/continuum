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

import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.notification.mail.MockJavaMailSender;
import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.notification.MessageContext;
import org.apache.maven.continuum.notification.Notifier;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class MailContinuumNotifierTest
    extends AbstractContinuumTest
{
    protected static final Logger logger = LoggerFactory.getLogger( MailContinuumNotifierTest.class );

    @Test
    public void testSuccessfulBuild()
        throws Exception
    {
        MailContinuumNotifier notifier = (MailContinuumNotifier) lookup( Notifier.class, "mail" );
        String toOverride = "recipient@host.com";
        notifier.setToOverride( toOverride );

        ProjectGroup group = createStubProjectGroup( "foo.bar", "" );

        BuildResultDao brDao = lookup( BuildResultDao.class );
        Project project = addProject( "Test Project", group );
        BuildResult br = makeBuild( ContinuumProjectState.FAILED );
        brDao.addBuildResult( project, br );

        br = makeBuild( ContinuumProjectState.OK );
        brDao.addBuildResult( project, br );

        br = makeBuild( ContinuumProjectState.FAILED );
        brDao.addBuildResult( project, br );

        BuildResult build = makeBuild( ContinuumProjectState.OK );
        assertEquals( ContinuumProjectState.OK, build.getState() );

        project.setState( build.getState() );
        getProjectDao().updateProject( project );

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setType( "maven2" );
        buildDef.setBuildFile( "pom.xml" );
        buildDef.setGoals( "clean install" );
        buildDef.setArguments( "" );
        BuildDefinitionDao buildDefDao = (BuildDefinitionDao) lookup( BuildDefinitionDao.class );
        buildDef = buildDefDao.addBuildDefinition( buildDef );
        build.setBuildDefinition( buildDef );
        assertEquals( ContinuumProjectState.OK, build.getState() );

        brDao.addBuildResult( project, build );
        build = brDao.getLatestBuildResultForProjectWithDetails( project.getId() );
        assertEquals( ContinuumProjectState.OK, build.getState() );

        MimeMessage mailMessage = sendNotificationAndGetMessage( project, build, buildDef, "lots out build output",
                                                                 toOverride );

        assertEquals( "[continuum] BUILD SUCCESSFUL: foo.bar Test Project", mailMessage.getSubject() );

        String mailContent = dumpContent( mailMessage, "recipient@host.com" );

        //CONTINUUM-1946
        assertTrue( mailContent.indexOf( "Goals: clean install" ) > 0 );
    }

    @Test
    public void testFailedBuild()
        throws Exception
    {
        ProjectGroup group = createStubProjectGroup( "foo.bar", "" );

        Project project = addProject( "Test Project", group );

        BuildResult build = makeBuild( ContinuumProjectState.FAILED );

        MimeMessage mailMessage = sendNotificationAndGetMessage( project, build, null, "output", null );

        assertEquals( "[continuum] BUILD FAILURE: foo.bar Test Project", mailMessage.getSubject() );

        dumpContent( mailMessage );
    }

    @Test
    public void testErroneousBuild()
        throws Exception
    {
        ProjectGroup group = createStubProjectGroup( "foo.bar", "" );

        Project project = addProject( "Test Project", group );

        BuildResult build = makeBuild( ContinuumProjectState.ERROR );

        build.setError( "Big long error message" );

        MimeMessage mailMessage = sendNotificationAndGetMessage( project, build, null, "lots of stack traces", null );

        assertEquals( "[continuum] BUILD ERROR: foo.bar Test Project", mailMessage.getSubject() );

        dumpContent( mailMessage );
    }

    private String dumpContent( MimeMessage mailMessage )
        throws Exception
    {
        return dumpContent( mailMessage, null );
    }

    private String dumpContent( MimeMessage mailMessage, String toOverride )
        throws Exception
    {
        Address[] tos = mailMessage.getRecipients( RecipientType.TO );
        if ( toOverride != null )
        {
            assertEquals( toOverride, ( (InternetAddress) tos[0] ).getAddress() );
        }
        else
        {
            assertEquals( "foo@bar", ( (InternetAddress) tos[0] ).getAddress() );
        }
        assertTrue( "The template isn't loaded correctly.", ( (String) mailMessage.getContent() ).indexOf(
            "#shellBuildResult()" ) < 0 );
        assertTrue( "The template isn't loaded correctly.", ( (String) mailMessage.getContent() ).indexOf(
            "Build statistics" ) > 0 );

        String mailContent = (String) mailMessage.getContent();

        logger.info( mailContent );

        return mailContent;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private MimeMessage sendNotificationAndGetMessage( Project project, BuildResult build, BuildDefinition buildDef,
                                                       String buildOutput, String toOverride )
        throws Exception
    {
        MessageContext context = new MessageContext();

        context.setProject( project );

        context.setBuildResult( build );

        context.setBuildDefinition( buildDef );

        ProjectNotifier projectNotifier = new ProjectNotifier();
        projectNotifier.setType( "mail" );
        Map<String, String> config = new HashMap<String, String>();
        config.put( MailContinuumNotifier.ADDRESS_FIELD, "foo@bar" );
        projectNotifier.setConfiguration( config );
        List<ProjectNotifier> projectNotifiers = new ArrayList<ProjectNotifier>();
        projectNotifiers.add( projectNotifier );
        context.setNotifier( projectNotifiers );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        Notifier notifier = lookup( Notifier.class, "mail" );

        ( (MailContinuumNotifier) notifier ).setBuildHost( "foo.bar.com" );

        notifier.sendMessage( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE, context );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        MockJavaMailSender mailSender = (MockJavaMailSender) lookup( JavaMailSender.class, "continuum" );

        assertEquals( 1, mailSender.getReceivedEmails().size() );

        List<MimeMessage> mails = mailSender.getReceivedEmails();

        MimeMessage mailMessage = mails.get( 0 );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertEquals( "continuum@localhost", ( (InternetAddress) mailMessage.getFrom()[0] ).getAddress() );

        assertEquals( "Continuum", ( (InternetAddress) mailMessage.getFrom()[0] ).getPersonal() );

        Address[] tos = mailMessage.getRecipients( RecipientType.TO );

        assertEquals( 1, tos.length );

        assertEquals( toOverride == null ? "foo@bar" : toOverride, ( (InternetAddress) tos[0] ).getAddress() );

        return mailMessage;
    }

    private BuildResult makeBuild( int state )
    {
        BuildResult build = new BuildResult();

        build.setStartTime( System.currentTimeMillis() );

        build.setEndTime( System.currentTimeMillis() + 1234567 );

        build.setState( state );

        build.setTrigger( ContinuumProjectState.TRIGGER_FORCED );

        build.setExitCode( 10 );

        return build;
    }
}
