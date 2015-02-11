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

import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.ExecutorConfigurator;
import org.apache.maven.continuum.execution.ant.AntBuildExecutor;
import org.apache.maven.continuum.execution.maven.m1.MavenOneBuildExecutor;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.installation.InstallationException;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.notification.AbstractContinuumNotifier;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.notification.MessageContext;
import org.apache.maven.continuum.notification.NotificationException;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.reports.surefire.ReportTestResult;
import org.apache.maven.continuum.reports.surefire.ReportTestSuiteGenerator;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.velocity.VelocityComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class MailContinuumNotifier
    extends AbstractContinuumNotifier
    implements Initializable
{
    private static final Logger log = LoggerFactory.getLogger( MailContinuumNotifier.class );

    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    @Requirement
    private VelocityComponent velocity;

    @Requirement
    private ConfigurationService configurationService;

    @Requirement
    private Continuum continuum;

    @Requirement
    private JavaMailSender javaMailSender;

    @Requirement
    private ReportTestSuiteGenerator reportTestSuiteGenerator;

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    @Configuration( "" )
    private String fromMailbox;

    @Configuration( "" )
    private String fromName;

    @Configuration( "" )
    private String toOverride;

    @Configuration( "" )
    private String timestampFormat;

    @Configuration( "" )
    private boolean includeBuildSummary = true;

    @Configuration( "" )
    private boolean includeTestSummary = true;

    @Configuration( "" )
    private boolean includeBuildOutput = false;

    /**
     * Customizable mail subject.  Use any combination of literal text, project or build attributes.
     * Examples:
     * "[continuum] BUILD ${state}: ${project.groupId} ${project.name}" results in "[continuum] BUILD SUCCESSFUL: foo.bar Hello World"
     * "[continuum] BUILD ${state}: ${project.name} ${project.scmTag}" results in "[continuum] BUILD SUCCESSFUL: Hello World Branch001"
     * "[continuum] BUILD ${state}: ${project.name} ${build.durationTime}" results in "[continuum] BUILD SUCCESSFUL: Hello World 2 sec"
     * "[continuum] BUILD ${state}: ${project.name}, Build Def - ${build.buildDefinition.description}" results in "[continuum] BUILD SUCCESSFUL: Hello World, Build Def - Nightly Test Build"
     */
    @Configuration( "" )
    private String buildSubjectFormat = "[continuum] BUILD ${state}: ${project.groupId} ${project.name}";

    /**
     * Customizable mail subject
     */
    @Configuration( "" )
    private String prepareBuildSubjectFormat =
        "[continuum] PREPARE BUILD ${state]: ${projectScmRoot.projectGroup.name}";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String buildHost;

    private FormatterTool formatterTool;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private static final String FALLBACK_FROM_MAILBOX = "continuum@localhost";

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
    {
        try
        {
            InetAddress address = InetAddress.getLocalHost();

            buildHost = StringUtils.clean( address.getHostName() );

            if ( buildHost == null )
            {
                buildHost = "localhost";
            }
        }
        catch ( UnknownHostException ex )
        {
            fromName = "Continuum";
        }

        // ----------------------------------------------------------------------
        // From mailbox
        // ----------------------------------------------------------------------

        if ( StringUtils.isEmpty( fromMailbox ) )
        {
            log.info( "The from mailbox is not configured, will use the nag email address from the project." );

            fromMailbox = null;
        }
        else
        {
            log.info( "Using '" + fromMailbox + "' as the from mailbox for all emails." );
        }

        if ( StringUtils.isEmpty( fromName ) )
        {
            fromName = "Continuum@" + buildHost;
        }

        log.info( "From name: " + fromName );

        log.info( "Build host name: " + buildHost );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        formatterTool = new FormatterTool( timestampFormat );
    }

    // ----------------------------------------------------------------------
    // Notifier Implementation
    // ----------------------------------------------------------------------

    public String getType()
    {
        return "mail";
    }

    public void sendMessage( String messageId, MessageContext context )
        throws NotificationException
    {
        Project project = context.getProject();
        List<ProjectNotifier> notifiers = context.getNotifiers();
        BuildResult build = context.getBuildResult();

        if ( build != null )
        {
            log.error( "br state=" + build.getState() );
        }

        if ( project != null )
        {
            log.error( "project state=" + project.getState() );
        }

        BuildDefinition buildDefinition = context.getBuildDefinition();
        ProjectScmRoot projectScmRoot = context.getProjectScmRoot();

        boolean isPrepareBuildComplete = messageId.equals(
            ContinuumNotificationDispatcher.MESSAGE_ID_PREPARE_BUILD_COMPLETE );

        if ( projectScmRoot == null && isPrepareBuildComplete )
        {
            return;
        }

        // ----------------------------------------------------------------------
        // If there wasn't any building done, don't notify
        // ----------------------------------------------------------------------

        if ( build == null && !isPrepareBuildComplete )
        {
            return;
        }

        // ----------------------------------------------------------------------
        // Generate and send email
        // ----------------------------------------------------------------------

        if ( messageId.equals( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE ) )
        {
            buildComplete( project, notifiers, build, messageId, context, buildDefinition );
        }
        else if ( isPrepareBuildComplete )
        {
            prepareBuildComplete( projectScmRoot, notifiers, messageId, context );
        }
    }

    private void buildComplete( Project project, List<ProjectNotifier> notifiers, BuildResult build, String messageId,
                                MessageContext context, BuildDefinition buildDefinition )
        throws NotificationException
    {
        BuildResult previousBuild = getPreviousBuild( project, buildDefinition, build );

        List<ProjectNotifier> notifiersList = new ArrayList<ProjectNotifier>();
        for ( ProjectNotifier notifier : notifiers )
        {
            // ----------------------------------------------------------------------
            // Check if the mail should be sent at all
            // ----------------------------------------------------------------------

            if ( shouldNotify( build, previousBuild, notifier ) )
            {
                notifiersList.add( notifier );
            }
        }
        buildComplete( project, notifiersList, build, previousBuild, messageId, context, buildDefinition );
    }

    private void buildComplete( Project project, List<ProjectNotifier> notifiers, BuildResult build,
                                BuildResult previousBuild, String messageId, MessageContext messageContext,
                                BuildDefinition buildDefinition )
        throws NotificationException
    {
        // ----------------------------------------------------------------------
        // Generate the mail contents
        // ----------------------------------------------------------------------

        String packageName = getClass().getPackage().getName().replace( '.', '/' );

        String templateName = packageName + "/templates/" + project.getExecutorId() + "/" + messageId + ".vm";

        StringWriter writer = new StringWriter();

        String content;

        try
        {
            VelocityContext context = new VelocityContext();

            context.put( "includeTestSummary", includeTestSummary );

            context.put( "includeOutput", includeBuildOutput );

            if ( includeBuildOutput )
            {
                context.put( "buildOutput", getBuildOutput( project, build ) );
            }

            if ( includeBuildSummary )
            {
                context.put( "build", build );

                ReportTestResult reportTestResult = reportTestSuiteGenerator.generateReportTestResult( build.getId(),
                                                                                                       project.getId() );

                context.put( "testResult", reportTestResult );

                context.put( "project", project );

                context.put( "changesSinceLastSuccess", continuum.getChangesSinceLastSuccess( project.getId(),
                                                                                              build.getId() ) );

                context.put( "previousBuild", previousBuild );

                // ----------------------------------------------------------------------
                // Tools
                // ----------------------------------------------------------------------

                context.put( "formatter", formatterTool );

                // TODO: Make the build host a part of the build

                context.put( "buildHost", buildHost );

                String osName = System.getProperty( "os.name" );

                String osPatchLevel = System.getProperty( "sun.os.patch.level" );

                if ( osPatchLevel != null )
                {
                    osName = osName + "(" + osPatchLevel + ")";
                }

                context.put( "osName", osName );

                context.put( "javaVersion", System.getProperty( "java.version" ) + "(" + System.getProperty(
                    "java.vendor" ) + ")" );

                // TODO only in case of a java project ?
                context.put( "javaHomeInformations", getJavaHomeInformations( buildDefinition ) );

                context.put( "builderVersions", getBuilderVersion( buildDefinition, project ) );
            }

            // ----------------------------------------------------------------------
            // Data objects
            // ----------------------------------------------------------------------

            context.put( "reportUrl", getReportUrl( project, build, configurationService ) );

            // TODO put other profile env var could be a security if they provide passwords ?

            // ----------------------------------------------------------------------
            // Generate
            // ----------------------------------------------------------------------

            velocity.getEngine().mergeTemplate( templateName, context, writer );

            content = writer.getBuffer().toString();
        }
        catch ( ResourceNotFoundException e )
        {
            log.info( "No such template: '" + templateName + "'." );

            return;
        }
        catch ( Exception e )
        {
            throw new NotificationException( "Error while generating mail contents.", e );
        }

        // ----------------------------------------------------------------------
        // Send the mail
        // ----------------------------------------------------------------------

        String subject;
        try
        {
            subject = generateSubject( project, build );
        }
        catch ( Exception e )
        {
            throw new NotificationException( "Error while generating mail subject.", e );
        }

        sendMessage( project, notifiers, subject, content, messageContext );
    }

    private void prepareBuildComplete( ProjectScmRoot projectScmRoot, List<ProjectNotifier> notifiers, String messageId,
                                       MessageContext messageContext )
        throws NotificationException
    {
        // ----------------------------------------------------------------------
        // Generate the mail contents
        // ----------------------------------------------------------------------

        String packageName = getClass().getPackage().getName().replace( '.', '/' );

        String templateName = packageName + "/templates/" + messageId + ".vm";

        StringWriter writer = new StringWriter();

        String content;

        try
        {
            VelocityContext context = new VelocityContext();

            // ----------------------------------------------------------------------
            // Data objects
            // ----------------------------------------------------------------------

            context.put( "reportUrl", getReportUrl( projectScmRoot.getProjectGroup(), projectScmRoot,
                                                    configurationService ) );

            context.put( "projectScmRoot", projectScmRoot );

            // TODO put other profile env var could be a security if they provide passwords ?

            // ----------------------------------------------------------------------
            // Generate
            // ----------------------------------------------------------------------

            velocity.getEngine().mergeTemplate( templateName, context, writer );

            content = writer.getBuffer().toString();
        }
        catch ( ResourceNotFoundException e )
        {
            log.info( "No such template: '" + templateName + "'." );

            return;
        }
        catch ( Exception e )
        {
            throw new NotificationException( "Error while generating mail contents.", e );
        }

        // ----------------------------------------------------------------------
        // Send the mail
        // ----------------------------------------------------------------------

        String subject;
        try
        {
            subject = generateSubject( projectScmRoot );
        }
        catch ( Exception e )
        {
            throw new NotificationException( "Error while generating mail subject.", e );
        }

        sendMessage( projectScmRoot, notifiers, subject, content, messageContext );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private List<String> getJavaHomeInformations( BuildDefinition buildDefinition )
        throws InstallationException
    {
        if ( buildDefinition == null )
        {
            return continuum.getInstallationService().getDefaultJavaVersionInfo();
        }
        Profile profile = buildDefinition.getProfile();
        if ( profile == null )
        {
            return continuum.getInstallationService().getDefaultJavaVersionInfo();
        }
        return continuum.getInstallationService().getJavaVersionInfo( profile.getJdk() );
    }

    private List<String> getBuilderVersion( BuildDefinition buildDefinition, Project project )
        throws InstallationException
    {
        ExecutorConfigurator executorConfigurator;
        Installation builder = null;
        Profile profile = null;
        if ( buildDefinition != null )
        {
            profile = buildDefinition.getProfile();
            if ( profile != null )
            {
                builder = profile.getBuilder();
            }
        }
        if ( builder != null )
        {
            executorConfigurator = continuum.getInstallationService().getExecutorConfigurator( builder.getType() );
        }
        else
        {
            // depends on ExecutorId
            if ( MavenTwoBuildExecutor.ID.equals( project.getExecutorId() ) )
            {
                executorConfigurator = continuum.getInstallationService().getExecutorConfigurator(
                    InstallationService.MAVEN2_TYPE );
            }
            else if ( MavenOneBuildExecutor.ID.equals( project.getExecutorId() ) )
            {
                executorConfigurator = continuum.getInstallationService().getExecutorConfigurator(
                    InstallationService.MAVEN1_TYPE );
            }
            else if ( AntBuildExecutor.ID.equals( project.getExecutorId() ) )
            {
                executorConfigurator = continuum.getInstallationService().getExecutorConfigurator(
                    InstallationService.ANT_TYPE );
            }
            else
            {
                return Arrays.asList( "No builder defined" );
            }
        }

        return continuum.getInstallationService().getExecutorVersionInfo(
            builder == null ? null : builder.getVarValue(), executorConfigurator, profile );
    }

    private String generateSubject( Project project, BuildResult build )
        throws Exception
    {
        String state = getState( project, build );

        VelocityContext context = new VelocityContext();
        context.put( "project", project );
        context.put( "build", build );
        context.put( "state", state );

        StringWriter writer = new StringWriter();

        boolean velocityRes = velocity.getEngine().evaluate( context, writer, "subjectPattern", buildSubjectFormat );

        return writer.toString();
    }

    private String generateSubject( ProjectScmRoot projectScmRoot )
        throws Exception
    {
        String state = getState( projectScmRoot );

        VelocityContext context = new VelocityContext();
        context.put( "projectScmRoot", projectScmRoot );
        context.put( "state", state );

        StringWriter writer = new StringWriter();

        boolean velocityResults = velocity.getEngine().evaluate( context, writer, "subjectPattern",
                                                                 prepareBuildSubjectFormat );

        return writer.toString();
    }

    private String getState( Project project, BuildResult build )
    {
        int state = project.getState();

        if ( build != null )
        {
            state = build.getState();
        }

        if ( state == ContinuumProjectState.OK )
        {
            return "SUCCESSFUL";
        }
        else if ( state == ContinuumProjectState.FAILED )
        {
            return "FAILURE";
        }
        else if ( state == ContinuumProjectState.ERROR )
        {
            return "ERROR";
        }
        else
        {
            log.warn( "Unknown build state " + state + " for project " + project.getId() );

            return "ERROR: Unknown build state " + state;
        }
    }

    private String getState( ProjectScmRoot projectScmRoot )
    {
        int state = projectScmRoot.getState();

        if ( state == ContinuumProjectState.UPDATED )
        {
            return "SUCCESSFUL";
        }
        else if ( state == ContinuumProjectState.ERROR )
        {
            return "ERROR";
        }
        else
        {
            log.warn(
                "Unknown prepare build state " + state + " for SCM Root URL " + projectScmRoot.getScmRootAddress() +
                    " in projectGroup " + projectScmRoot.getProjectGroup().getId() );

            return "ERROR: Unknown build state " + state;
        }
    }

    private void sendMessage( Project project, List<ProjectNotifier> notifiers, String subject, String content,
                              MessageContext context )
        throws NotificationException
    {
        if ( notifiers.size() == 0 )
        {
            // This is a useful message for the users when debugging why they don't
            // receive any mails

            log.info( "No mail notifier for '" + project.getName() + "'." );

            return;
        }

        String fromMailbox = getFromMailbox( notifiers );

        if ( fromMailbox == null )
        {
            log.warn( project.getName() +
                          ": Project is missing nag email and global from mailbox is missing, not sending mail." );

            return;
        }

        try
        {

            MimeMessage message = javaMailSender.createMimeMessage();

            message.addHeader( "X-Continuum-Build-Host", buildHost );

            message.addHeader( "X-Continuum-Project-Id", Integer.toString( project.getId() ) );

            message.addHeader( "X-Continuum-Project-Name", project.getName() );

            message.setSubject( subject );

            log.info( "Message Subject: '" + subject + "'." );

            message.setText( content );

            InternetAddress from = new InternetAddress( fromMailbox, fromName );

            message.setFrom( from );

            log.info( "Sending message: From '" + from + "'." );

            if ( StringUtils.isEmpty( toOverride ) )
            {
                Set<String> listRecipents = new HashSet<String>();
                for ( ProjectNotifier notifier : notifiers )
                {
                    Map<String, String> conf = notifier.getConfiguration();
                    if ( conf != null )
                    {
                        String addressField = conf.get( ADDRESS_FIELD );

                        if ( StringUtils.isNotEmpty( addressField ) )
                        {
                            String[] addresses = StringUtils.split( addressField, "," );
                            for ( String address : addresses )
                            {
                                if ( !listRecipents.contains( address.trim() ) )
                                {
                                    // [CONTINUUM-2281] Dont repeat addesss in recipents.
                                    // TODO: set a proper name
                                    InternetAddress to = new InternetAddress( address.trim() );

                                    log.info( "Recipient: To '" + to + "'." );
                                    message.addRecipient( Message.RecipientType.TO, to );
                                    listRecipents.add( address.trim() );
                                }
                            }

                        }

                        if ( context.getBuildResult() != null )
                        {
                            String committerField = (String) notifier.getConfiguration().get( COMMITTER_FIELD );
                            String developerField = (String) notifier.getConfiguration().get( DEVELOPER_FIELD );
                            // Developers constains committers.
                            if ( StringUtils.isNotEmpty( developerField ) && Boolean.parseBoolean( developerField ) )
                            {
                                List<ProjectDeveloper> developers = project.getDevelopers();
                                if ( developers == null || developers.isEmpty() )
                                {
                                    log.warn(
                                        "No developers have been configured...notifcation email will not be sent" );
                                    return;
                                }
                                Map<String, String> developerToEmailMap = mapDevelopersToRecipients( developers );
                                for ( String email : developerToEmailMap.values() )
                                {
                                    if ( !listRecipents.contains( email.trim() ) )
                                    {
                                        InternetAddress to = new InternetAddress( email.trim() );
                                        log.info( "Recipient: To '" + to + "'." );
                                        message.addRecipient( Message.RecipientType.TO, to );
                                        listRecipents.add( email.trim() );
                                    }
                                }
                            }
                            else if ( StringUtils.isNotEmpty( committerField ) && Boolean.parseBoolean(
                                committerField ) )
                            {
                                ScmResult scmResult = context.getBuildResult().getScmResult();
                                if ( scmResult != null && scmResult.getChanges() != null &&
                                    !scmResult.getChanges().isEmpty() )
                                {
                                    List<ProjectDeveloper> developers = project.getDevelopers();
                                    if ( developers == null || developers.isEmpty() )
                                    {
                                        log.warn( "No developers have been configured...notifcation email " +
                                                      "will not be sent" );
                                        return;
                                    }

                                    Map<String, String> developerToEmailMap = mapDevelopersToRecipients( developers );

                                    List<ChangeSet> changes = scmResult.getChanges();

                                    for ( ChangeSet changeSet : changes )
                                    {
                                        String scmId = changeSet.getAuthor();
                                        if ( StringUtils.isNotEmpty( scmId ) )
                                        {
                                            String email = developerToEmailMap.get( scmId );
                                            if ( StringUtils.isEmpty( email ) )
                                            {
                                                //TODO: Add a default domain so mail address won't be required
                                                log.warn(
                                                    "no email address is defined in developers list for '" + scmId +
                                                        "' scm id." );
                                            }
                                            else if ( !listRecipents.contains( email.trim() ) )
                                            {
                                                // [CONTINUUM-2281] Dont repeat addesss in recipents.)
                                                // TODO: set a proper name
                                                InternetAddress to = new InternetAddress( email.trim() );
                                                log.info( "Recipient: To '" + to + "'." );

                                                message.addRecipient( Message.RecipientType.TO, to );
                                                listRecipents.add( email.trim() );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                // TODO: use configuration file instead of to load it fron component configuration
                // TODO: set a proper name
                InternetAddress to = new InternetAddress( toOverride.trim() );
                log.info( "Recipient: To '" + to + "'." );

                message.addRecipient( Message.RecipientType.TO, to );
            }

            message.setSentDate( new Date() );

            if ( message.getAllRecipients() != null && ( message.getAllRecipients() ).length > 0 )
            {
                javaMailSender.send( message );
            }
        }
        catch ( AddressException ex )
        {
            throw new NotificationException( "Exception while sending message.", ex );
        }
        catch ( MessagingException ex )
        {
            throw new NotificationException( "Exception while sending message.", ex );
        }
        catch ( UnsupportedEncodingException ex )
        {
            throw new NotificationException( "Exception while sending message.", ex );
        }
    }

    private void sendMessage( ProjectScmRoot projectScmRoot, List<ProjectNotifier> notifiers, String subject,
                              String content, MessageContext context )
        throws NotificationException
    {
        ProjectGroup projectGroup = projectScmRoot.getProjectGroup();

        if ( notifiers.size() == 0 )
        {
            // This is a useful message for the users when debugging why they don't
            // receive any mails

            log.info( "No mail notifier for '" + projectGroup.getName() + "'." );

            return;
        }

        String fromMailbox = getFromMailbox( notifiers );

        if ( fromMailbox == null )
        {
            log.warn( projectGroup.getName() +
                          ": ProjectGroup is missing nag email and global from mailbox is missing, not sending mail." );

            return;
        }

        MimeMessage message = javaMailSender.createMimeMessage();

        try
        {
            message.setSubject( subject );

            log.info( "Message Subject: '" + subject + "'." );

            message.setText( content );

            InternetAddress from = new InternetAddress( fromMailbox, fromName );

            message.setFrom( from );

            log.info( "Sending message: From '" + from + "'." );

            if ( StringUtils.isEmpty( toOverride ) )
            {
                for ( ProjectNotifier notifier : notifiers )
                {
                    if ( !shouldNotify( projectScmRoot, notifier ) )
                    {
                        continue;
                    }

                    Map<String, String> conf = notifier.getConfiguration();
                    if ( conf != null )
                    {
                        String addressField = conf.get( ADDRESS_FIELD );

                        if ( StringUtils.isNotEmpty( addressField ) )
                        {
                            String[] addresses = StringUtils.split( addressField, "," );

                            for ( String address : addresses )
                            {
                                // TODO: set a proper name
                                InternetAddress to = new InternetAddress( address.trim() );

                                log.info( "Recipient: To '" + to + "'." );
                                message.addRecipient( Message.RecipientType.TO, to );
                            }
                        }
                    }
                }
            }
            else
            {
                // TODO: use configuration file instead of to load it fron component configuration
                // TODO: set a proper name
                InternetAddress to = new InternetAddress( toOverride.trim() );
                log.info( "Recipient: To '" + to + "'." );

                message.addRecipient( Message.RecipientType.TO, to );
            }

            message.setSentDate( new Date() );

            if ( message.getAllRecipients() != null && ( message.getAllRecipients() ).length > 0 )
            {
                javaMailSender.send( message );
            }
        }
        catch ( AddressException ex )
        {
            throw new NotificationException( "Exception while sending message.", ex );
        }
        catch ( MessagingException ex )
        {
            throw new NotificationException( "Exception while sending message.", ex );
        }
        catch ( UnsupportedEncodingException ex )
        {
            throw new NotificationException( "Exception while sending message.", ex );
        }
    }

    private Map<String, String> mapDevelopersToRecipients( List<ProjectDeveloper> developers )
    {
        Map<String, String> developersMap = new HashMap<String, String>();

        for ( ProjectDeveloper developer : developers )
        {
            if ( StringUtils.isNotEmpty( developer.getScmId() ) && StringUtils.isNotEmpty( developer.getEmail() ) )
            {
                developersMap.put( developer.getScmId(), developer.getEmail() );
            }
        }

        return developersMap;
    }

    private String getFromMailbox( List<ProjectNotifier> notifiers )
    {
        if ( fromMailbox != null )
        {
            return fromMailbox;
        }

        String address = null;

        for ( ProjectNotifier notifier : notifiers )
        {
            Map<String, String> configuration = notifier.getConfiguration();
            if ( configuration != null && StringUtils.isNotEmpty( configuration.get( ADDRESS_FIELD ) ) )
            {
                address = configuration.get( ADDRESS_FIELD );
                break;
            }
        }

        if ( StringUtils.isEmpty( address ) )
        {
            return FALLBACK_FROM_MAILBOX;
        }
        // olamy : CONTINUUM-860 if address contains commas we use only the first one
        if ( address != null && address.contains( "," ) )
        {
            String[] addresses = StringUtils.split( address, "," );
            return addresses[0];
        }
        return address;
    }

    public String getBuildHost()
    {
        return buildHost;
    }

    public void setBuildHost( String buildHost )
    {
        this.buildHost = buildHost;
    }

    public String getToOverride()
    {
        return toOverride;
    }

    public void setToOverride( String toOverride )
    {
        this.toOverride = toOverride;
    }
}
