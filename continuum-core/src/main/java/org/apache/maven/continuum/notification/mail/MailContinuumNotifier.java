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

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.velocity.VelocityComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class MailContinuumNotifier
    extends AbstractContinuumNotifier
    implements Initializable
{
    private Logger log = LoggerFactory.getLogger( getClass() );

    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private VelocityComponent velocity;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    /**
     * @plexus.requirement
     */
    private JavaMailSender javaMailSender;

    /**
     * @plexus.requirement
     */
    private ReportTestSuiteGenerator reportTestSuiteGenerator;

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------
    /**
     * @plexus.configuration
     */
    private String fromMailbox;

    /**
     * @plexus.configuration
     */
    private String fromName;

    /**
     * @plexus.configuration
     */
    private String toOverride;

    /**
     * @plexus.configuration
     */
    private String timestampFormat;

    /**
     * @plexus.configuration
     */
    private boolean includeBuildResult = true;

    /**
     * @plexus.configuration
     */
    private boolean includeBuildSummary = true;

    /**
     * @plexus.configuration
     */
    private boolean includeTestSummary = true;

    /**
     * @plexus.configuration
     */
    private boolean includeOutput = false;

    /**
     * Customizable mail subject.  Use any combination of literal text, project or build attributes.
     * Examples:
     * "[continuum] BUILD ${state}: ${project.groupId} ${project.name}" results in "[continuum] BUILD SUCCESSFUL: foo.bar Hello World"
     * "[continuum] BUILD ${state}: ${project.name} ${project.scmTag}" results in "[continuum] BUILD SUCCESSFUL: Hello World Branch001"
     * "[continuum] BUILD ${state}: ${project.name} ${build.durationTime}" results in "[continuum] BUILD SUCCESSFUL: Hello World 2 sec"
     * "[continuum] BUILD ${state}: ${project.name}, Build Def - ${build.buildDefinition.description}" results in "[continuum] BUILD SUCCESSFUL: Hello World, Build Def - Nightly Test Build"
     *
     * @plexus.configuration
     */
    private String subjectFormat = "[continuum] BUILD ${state}: ${project.groupId} ${project.name}";

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
        String buildOutput = getBuildOutput( project, build );
        BuildDefinition buildDefinition = context.getBuildDefinition();

        // ----------------------------------------------------------------------
        // If there wasn't any building done, don't notify
        // ----------------------------------------------------------------------

        if ( build == null )
        {
            return;
        }

        // ----------------------------------------------------------------------
        // Generate and send email
        // ----------------------------------------------------------------------

        if ( messageId.equals( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE ) )
        {
            buildComplete( project, notifiers, build, buildOutput, messageId, context, buildDefinition );
        }
    }

    private void buildComplete( Project project, List<ProjectNotifier> notifiers, BuildResult build, String buildOutput,
                                String messageId, MessageContext context, BuildDefinition buildDefinition )
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
        buildComplete( project, notifiersList, build, previousBuild, buildOutput, messageId, context, buildDefinition );
    }

    private void buildComplete( Project project, List<ProjectNotifier> notifiers, BuildResult build,
                                BuildResult previousBuild, String buildOutput, String messageId,
                                MessageContext messageContext, BuildDefinition buildDefinition )
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

            context.put( "includeOutput", includeOutput );

            if ( includeBuildResult )
            {
                context.put( "buildOutput", buildOutput );
            }

            if ( includeBuildSummary )
            {
                context.put( "build", build );

                ReportTestResult reportTestResult =
                    reportTestSuiteGenerator.generateReportTestResult( build.getId(), project.getId() );

                context.put( "testResult", reportTestResult );

                context.put( "project", project );

                context.put( "changesSinceLastSuccess", continuum.getChangesSinceLastSuccess( project.getId(), build
                    .getId() ) );

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

                context.put( "javaVersion",
                             System.getProperty( "java.version" ) + "(" + System.getProperty( "java.vendor" ) + ")" );

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

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private List<String> getJavaHomeInformations( BuildDefinition buildDefinition )
        throws InstallationException
    {
        if ( buildDefinition == null )
        {
            return continuum.getInstallationService().getDefaultJdkInformations();
        }
        Profile profile = buildDefinition.getProfile();
        if ( profile == null )
        {
            return continuum.getInstallationService().getDefaultJdkInformations();
        }
        return continuum.getInstallationService().getJdkInformations( profile.getJdk() );
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
                executorConfigurator = continuum.getInstallationService()
                    .getExecutorConfigurator( InstallationService.MAVEN2_TYPE );
            }
            else if ( MavenOneBuildExecutor.ID.equals( project.getExecutorId() ) )
            {
                executorConfigurator = continuum.getInstallationService()
                    .getExecutorConfigurator( InstallationService.MAVEN1_TYPE );
            }
            else if ( AntBuildExecutor.ID.equals( project.getExecutorId() ) )
            {
                executorConfigurator = continuum.getInstallationService()
                    .getExecutorConfigurator( InstallationService.ANT_TYPE );
            }
            else
            {
                return Arrays.asList( "No builder defined" );
            }
        }

        return continuum.getInstallationService().getExecutorConfiguratorVersion( builder == null ? null : builder
            .getVarValue(), executorConfigurator, profile );
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

        boolean velocityResults = velocity.getEngine().evaluate( context, writer, "subjectPattern", subjectFormat );

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
            log
                .warn( project.getName() +
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
                for ( ProjectNotifier notifier : notifiers )
                {
                    Map<String, String> conf = notifier.getConfiguration();
                    if ( conf != null )
                    {
                        String addressField = conf.get( ADDRESS_FIELD );

                        if ( StringUtils.isNotEmpty( addressField ) )
                        {
                            String[] addresses = StringUtils.split( addressField, "," );
                            List<Address> recipients = new ArrayList<Address>();
                            for ( String address : addresses )
                            {
                                // TODO: set a proper name
                            	InternetAddress to = new InternetAddress( address.trim() );

                                log.info( "Recipient: To '" + to + "'." );

                                recipients.add( to );
                            }
                            message.setRecipients(Message.RecipientType.TO, recipients.toArray(new Address[recipients.size()]));
                        }
                        
                        String committerField = (String) notifier.getConfiguration().get( COMMITTER_FIELD );
                        if ( StringUtils.isNotEmpty( committerField ) && context.getBuildResult() != null )
                        {
                            if ( Boolean.parseBoolean( committerField ) )
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
                                                log.warn( "no email address is defined in developers list for '" +
                                                    scmId + "' scm id." );
                                            }
                                            else
                                            {
                                                // TODO: set a proper name
                                                InternetAddress to = new InternetAddress( email.trim() );
                                                log.info( "Recipient: To '" + to + "'." );

                                                message.addRecipient( Message.RecipientType.TO, to );
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

            javaMailSender.send( message );
            //mailSender.send( message );
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
        if ( address.contains( "," ) )
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
