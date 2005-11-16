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

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.activemq.ActiveMQConnectionFactory;

import javax.jms.ExceptionListener;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Queue;
import javax.jms.MessageProducer;
import javax.jms.Topic;
import javax.jms.DeliveryMode;
import javax.jms.ObjectMessage;
import javax.jms.Message;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractContinuumBuildAgent extends AbstractContinuumAgentAction implements BuildAgent, ExceptionListener, Startable {
    /**
     * @plexus.configuration
     */
    protected String coordinatorUrl;

    protected boolean run;

    protected Connection connection;

    public synchronized void start() throws StartingException {
        try {
            connection = createConnection(coordinatorUrl);
        } catch (Throwable e) {
            getLogger().error("Could not create connection to: "+coordinatorUrl, e);
            throw new StartingException("Could not create connection to: "+coordinatorUrl);
        }

        run = true;
        Thread agentThread = new Thread(this);
        agentThread.setDaemon(false);
        agentThread.start();
    }

    public synchronized void stop() throws StoppingException {
        run = false;
        try {
            connection.close();
        } catch (JMSException e) {
            getLogger().error("Could not close connection to: "+coordinatorUrl, e);
            throw new StoppingException("Could not close connection to: "+coordinatorUrl);
        }
    }

    public synchronized void onException(JMSException ex) {
        getLogger().fatalError("JMS Exception occured.  Shutting down client.", ex);
        run = false;
    }

    public synchronized boolean isRunning() {
        return run;
    }

    protected Connection createConnection(String coordinatorUrl) throws JMSException {
        // Create a ConnectionFactory
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(coordinatorUrl);

        // Create a Connection
        Connection connection = connectionFactory.createConnection();

        connection.start();

        connection.setExceptionListener(this);

        return connection;
    }

    protected MessageConsumer createQueueConsumer(Session session, String subject) throws JMSException {
        Queue queue = session.createQueue(subject);

        return session.createConsumer(queue);
    }

    protected MessageProducer createTopicProducer(Session session, String subject) throws JMSException {
        Topic topic = session.createTopic(subject);

        MessageProducer producer = session.createProducer(topic);

        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        return producer;
    }

    protected MessageConsumer createConsumer(Session session, String subject) throws JMSException {
        Topic topic = session.createTopic(subject);
        return session.createConsumer(topic);
    }

    protected Map getMap(ObjectMessage objectMessage, Message message) throws JMSException, BuildAgentException {
        try {
            return (Map) objectMessage.getObject();
        } catch (Exception e) {
            throw new BuildAgentException("Message.getObject failed on "+ message.getJMSMessageID(), e);
        }
    }
}
