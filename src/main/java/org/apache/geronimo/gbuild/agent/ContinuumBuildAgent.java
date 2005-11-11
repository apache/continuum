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
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.buildcontroller.BuildController;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.MessageProducer;
import javax.jms.DeliveryMode;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
    private String url;

    /**
     * @plexus.configuration
     */
    private String buildQueueSubject;

    /**
     * @plexus.configuration
     */
    private String buildResultsSubject;

    private boolean run;

    public void run() {
        try {
            run = true;

            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

            // Create a Connection
            Connection connection = connectionFactory.createConnection();

            connection.start();

            connection.setExceptionListener(this);

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer buildConsumer = createQueueConsumer(session, buildQueueSubject);

            MessageProducer resultsProducer = createTopicProducer(session, buildResultsSubject);

            while (run) {
                // Wait for a message
                Message message = buildConsumer.receive();

                if (message instanceof MapMessage) {

                    MapMessage mapMessage = (MapMessage) message;

                    ContinuumStore store = getContinuumStore(mapMessage);

                    ContinuumStoreContext.setStore(store);

                    int projectId = getProjectId(mapMessage);

                    int buildDefinitionId = getBuildDefinitionId(mapMessage);

                    int trigger = getTrigger(mapMessage);

                    controller.build(projectId, buildDefinitionId, trigger);

                    MapMessage results = session.createMapMessage();

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

                    resultsProducer.send(results);

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
        }
    }

    private void setAdminAddress(MapMessage results) throws JMSException {
        results.setString(KEY_ADMIN_ADDRESS, adminAddress);
    }

    private void setContributor(MapMessage results) throws JMSException {
        results.setString(KEY_CONTRIBUTOR, contributor);
    }

    private void setHostInformation(MapMessage results) throws UnknownHostException, JMSException {
        InetAddress localHost = InetAddress.getLocalHost();
        results.setString(KEY_HOST_NAME, localHost.getHostName());
        results.setString(KEY_HOST_ADDRESS, localHost.getHostAddress());
    }

    private void setSystemProperty(MapMessage results, String name) throws JMSException {
        results.setString(name, System.getProperty(name));
    }

    private void setStore(MapMessage results, ContinuumStore store) throws JMSException {
        results.setObject(KEY_STORE, store);
    }

    private void setBuildDefinitionId(MapMessage results, int buildDefinitionId) throws JMSException {
        results.setInt(KEY_BUILD_DEFINITION_ID, buildDefinitionId);
    }

    private void setTrigger(MapMessage results, int trigger) throws JMSException {
        results.setInt(KEY_TRIGGER, trigger);
    }

    private void setProjectId(MapMessage results, int projectId) throws JMSException {
        results.setInt(KEY_PROJECT_ID, projectId);
    }

    private int getTrigger(MapMessage mapMessage) throws JMSException {
        return mapMessage.getIntProperty(KEY_TRIGGER);
    }

    private int getBuildDefinitionId(MapMessage mapMessage) throws JMSException {
        return mapMessage.getIntProperty(KEY_BUILD_DEFINITION_ID);
    }

    private int getProjectId(MapMessage mapMessage) throws JMSException {
        return mapMessage.getIntProperty(KEY_PROJECT_ID);
    }

    private ContinuumStore getContinuumStore(MapMessage mapMessage) throws JMSException {
        return (ContinuumStore) mapMessage.getObject(KEY_STORE);
    }

    private MessageConsumer createQueueConsumer(Session session, String subject) throws JMSException {
        Queue queue = session.createQueue(subject);

        MessageConsumer consumer = session.createConsumer(queue);

        return consumer;
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
