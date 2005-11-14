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

/**
 * @version $Rev$ $Date$
 */
public class ContinuumBuildAgent implements BuildAgent, ExceptionListener {

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
            init();
            run = true;

            Connection connection = null;
            try {
                connection = createConnection(coordinatorUrl);
            } catch (Throwable e) {
                System.out.println("Could not create connection to: "+coordinatorUrl);
                e.printStackTrace();
                return;
            }

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer buildConsumer = createQueueConsumer(session, buildTaskQueue);

            MessageProducer resultsProducer = createTopicProducer(session, buildResultsTopic);

            while (run) {
                // Wait for a message
                Message message = buildConsumer.receive();

                if (message instanceof ObjectMessage) {

                    ObjectMessage objectMessage = (ObjectMessage) message;

                    Map mapMessage = (Map) objectMessage.getObject();

                    ContinuumStore store = getContinuumStore(mapMessage);

                    ThreadContextContinuumStore.setStore(store);

                    int projectId = getProjectId(mapMessage);

                    int buildDefinitionId = getBuildDefinitionId(mapMessage);

                    int trigger = getTrigger(mapMessage);

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

                    ObjectMessage resultMessage = session.createObjectMessage(results);

                    resultsProducer.send(resultMessage);

                } else {
                    System.out.println("Incorect message type: " + message.getClass().getName());
                }
            }

            buildConsumer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
            System.out.println("ContinuumBuildAgent.run");
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

    private void setAdminAddress(Map results) throws JMSException {
        results.put(KEY_ADMIN_ADDRESS, adminAddress);
    }

    private void setContributor(Map results) throws JMSException {
        results.put(KEY_CONTRIBUTOR, contributor);
    }

    private void setHostInformation(Map results) throws UnknownHostException, JMSException {
        InetAddress localHost = InetAddress.getLocalHost();
        results.put(KEY_HOST_NAME, localHost.getHostName());
        results.put(KEY_HOST_ADDRESS, localHost.getHostAddress());
    }

    private void setSystemProperty(Map results, String name) throws JMSException {
        results.put(name, System.getProperty(name));
    }

    private void setStore(Map results, ContinuumStore store) throws JMSException {
        results.put(KEY_STORE, store);
    }

    private void setBuildDefinitionId(Map results, int buildDefinitionId) throws JMSException {
        results.put(KEY_BUILD_DEFINITION_ID, new Integer(buildDefinitionId));
    }

    private void setTrigger(Map results, int trigger) throws JMSException {
        results.put(KEY_TRIGGER, new Integer(trigger));
    }

    private void setProjectId(Map results, int projectId) throws JMSException {
        results.put(KEY_PROJECT_ID, new Integer(projectId));
    }

    private int getTrigger(Map mapMessage) throws JMSException {
        return ((Integer)mapMessage.get(KEY_TRIGGER)).intValue();
    }

    private int getBuildDefinitionId(Map mapMessage) throws JMSException {
        return ((Integer)mapMessage.get(KEY_BUILD_DEFINITION_ID)).intValue();
    }

    private int getProjectId(Map mapMessage) throws JMSException {
        return ((Integer)mapMessage.get(KEY_PROJECT_ID)).intValue();
    }

    private ContinuumStore getContinuumStore(Map mapMessage) throws JMSException {
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

    public synchronized void stop() {
        run = false;
    }

    public synchronized void onException(JMSException ex) {
        System.out.println("JMS Exception occured.  Shutting down client.");
    }

}
