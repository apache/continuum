/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.gbuild.agent;

import org.activemq.ActiveMQConnectionFactory;
import org.apache.maven.continuum.buildcontroller.BuildController;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.configuration.ConfigurationLoadingException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.ObjectMessage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @version $Rev$ $Date$
 */
public class ContinuumBuildAgent extends AbstractLogEnabled implements BuildAgent, ExceptionListener, Startable {

    // ----------------------------------------------------------------------
    // Keys for the values that can be in the context
    // ----------------------------------------------------------------------

    public static final String KEY_STORE = "store";

    public static final String KEY_PROJECT_ID = "projectId";

    public static final String KEY_BUILD_DEFINITION_ID = "buildDefinitionId";

    public static final String KEY_TRIGGER = "trigger";

    public static final String KEY_HOST_NAME = "hostName";

    public static final String KEY_HOST_ADDRESS = "hostAddress";

    public static final String KEY_CONTRIBUTOR = "contributor";

    public static final String KEY_ADMIN_ADDRESS = "adminAddress";

    /**
     * @plexus.requirement
     */
    private BuildAgentExtentionManager extentionManager;

    /**
     * @plexus.requirement
     */
    private BuildController controller;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.configuration
     */
    private String contributor;

    /**
     * @plexus.configuration
     */
    private String adminAddress;

    /**
     * @plexus.configuration
     */
    private String coordinatorUrl;

    /**
     * @plexus.configuration
     */
    private String buildTaskQueue;

    /**
     * @plexus.configuration
     */
    private String buildResultsTopic;

    /**
     * @plexus.configuration
     */
    private String workingDirectory;

    /**
     * @plexus.configuration
     */
    private String buildOutputDirectory;

    private boolean run;

    public void run() {
        try {
            getLogger().info("Continuum Build Agent starting.");
            getLogger().info("coordinatorUrl "+coordinatorUrl);
            getLogger().info("buildTaskQueue "+buildTaskQueue);
            getLogger().info("buildResultsTopic "+buildResultsTopic);
            getLogger().info("workingDirectory "+workingDirectory);
            getLogger().info("buildOutputDirectory "+buildOutputDirectory);
            getLogger().info("adminAddress "+adminAddress);
            getLogger().info("contributor "+contributor);

            run = true;

            Connection connection = null;
            try {
                connection = createConnection(coordinatorUrl);
            } catch (Throwable e) {
                getLogger().error("Could not create connection to: "+coordinatorUrl, e);
                return;
            }

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer buildConsumer = createQueueConsumer(session, buildTaskQueue);

            MessageProducer resultsProducer = createTopicProducer(session, buildResultsTopic);

            getLogger().info("Continuum Build Agent started and waiting for work.");

            while (run) {
                // Wait for a message
                Message message = buildConsumer.receive(1000);

                if (message == null){

                    continue;

                } else if (message instanceof ObjectMessage) {

                    try {
                        getLogger().info("Message Received "+ message.getJMSMessageID() +" on "+ connection.getClientID()+":"+buildTaskQueue);

                        ObjectMessage objectMessage = (ObjectMessage) message;

                        Map buildTask = getMap(objectMessage, message);

                        ContinuumStore store = getContinuumStore(buildTask);

                        ThreadContextContinuumStore.setStore(store);

                        init();

                        int projectId = getProjectId(buildTask);

                        int buildDefinitionId = getBuildDefinitionId(buildTask);

                        int trigger = getTrigger(buildTask);

                        extentionManager.preProcess(buildTask);

                        build(projectId, buildDefinitionId, trigger);

                        HashMap results = new HashMap();

                        setStore(results, store);

                        setProjectId(results, projectId);

                        setBuildDefinitionId(results, buildDefinitionId);

                        setTrigger(results, trigger);

                        setSystemProperty(results, "os.version");

                        setSystemProperty(results, "os.name");

                        setSystemProperty(results, "java.version");

                        setSystemProperty(results, "java.vendor");

                        setHostInformation(results);

                        setContributor(results);

                        setAdminAddress(results);

                        extentionManager.postProcess(buildTask, results);

                        getLogger().info("Finished processing "+ message.getJMSMessageID());

                        ObjectMessage resultMessage = session.createObjectMessage(results);

                        resultsProducer.send(resultMessage);

                        getLogger().info("Results sent to "+ buildResultsTopic );

                    } catch (Exception e) {
                        getLogger().error("Failed Processing message "+message.getJMSMessageID());
                    }

                } else {
                    getLogger().warn("Agent received incorrect message type: "+message.getClass().getName());
                }
            }

            buildConsumer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            getLogger().error("Agent failed.", e);
        }
    }

    private Map getMap(ObjectMessage objectMessage, Message message) throws JMSException, BuildAgentException {
        try {
            return (Map) objectMessage.getObject();
        } catch (Exception e) {
            throw new BuildAgentException("Message.getObject failed on "+ message.getJMSMessageID(), e);
        }
    }

    public void init() throws ConfigurationLoadingException {
        configurationService.load();
        configurationService.setWorkingDirectory(new File(workingDirectory));
        configurationService.setBuildOutputDirectory(new File(buildOutputDirectory));
    }

    public void build(int projectId, int buildDefinitionId, int trigger) {
        controller.build(projectId, buildDefinitionId, trigger);
    }

    private Connection createConnection(String coordinatorUrl) throws JMSException {
        // Create a ConnectionFactory
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(coordinatorUrl);

        // Create a Connection
        Connection connection = connectionFactory.createConnection();

        connection.start();

        connection.setExceptionListener(this);

        return connection;
    }

    public void setAdminAddress(Map results) throws JMSException {
        results.put(KEY_ADMIN_ADDRESS, adminAddress);
    }

    public void setContributor(Map results) throws JMSException {
        results.put(KEY_CONTRIBUTOR, contributor);
    }

    public static void setHostInformation(Map results) throws UnknownHostException, JMSException {
        InetAddress localHost = InetAddress.getLocalHost();
        results.put(KEY_HOST_NAME, localHost.getHostName());
        results.put(KEY_HOST_ADDRESS, localHost.getHostAddress());
    }

    public static void setSystemProperty(Map results, String name) throws JMSException {
        results.put(name, System.getProperty(name));
    }

    public static void setStore(Map results, ContinuumStore store) throws JMSException {
        results.put(KEY_STORE, store);
    }

    public static void setBuildDefinitionId(Map results, int buildDefinitionId) throws JMSException {
        results.put(KEY_BUILD_DEFINITION_ID, new Integer(buildDefinitionId));
    }

    public static void setTrigger(Map results, int trigger) throws JMSException {
        results.put(KEY_TRIGGER, new Integer(trigger));
    }

    public static void setProjectId(Map results, int projectId) throws JMSException {
        results.put(KEY_PROJECT_ID, new Integer(projectId));
    }

    public static int getTrigger(Map mapMessage) throws JMSException {
        return ((Integer)mapMessage.get(KEY_TRIGGER)).intValue();
    }

    public static int getBuildDefinitionId(Map mapMessage) throws JMSException {
        return ((Integer)mapMessage.get(KEY_BUILD_DEFINITION_ID)).intValue();
    }

    public static int getProjectId(Map mapMessage) throws JMSException {
        return ((Integer)mapMessage.get(KEY_PROJECT_ID)).intValue();
    }

    public static ContinuumStore getContinuumStore(Map mapMessage) throws JMSException {
        return (ContinuumStore) mapMessage.get(KEY_STORE);
    }

    private MessageConsumer createQueueConsumer(Session session, String subject) throws JMSException {
        Queue queue = session.createQueue(subject);

        return session.createConsumer(queue);
    }

    private MessageProducer createTopicProducer(Session session, String subject) throws JMSException {
        Topic topic = session.createTopic(subject);

        MessageProducer producer = session.createProducer(topic);

        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        return producer;
    }

    public synchronized boolean isRunning() {
        return run;
    }

    public synchronized void start() throws StartingException {
        run = true;
        Thread agentThread = new Thread(this);
        agentThread.setDaemon(false);
        agentThread.start();
    }

    public synchronized void stop() throws StoppingException {
        run = false;
    }

    public synchronized void onException(JMSException ex) {
        System.out.println("JMS Exception occured.  Shutting down client.");
    }

}
