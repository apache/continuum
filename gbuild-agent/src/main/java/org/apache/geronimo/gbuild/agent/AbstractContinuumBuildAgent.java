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

    private boolean run;

    private Connection connection;
    private Client client;


    public synchronized void start() throws StartingException {
        try {
            setClient(new Client(coordinatorUrl));
            connection = getClient().getConnection();
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
            getClient().close();
        } catch (JMSException e) {
            getLogger().error("Could not close connection to: "+coordinatorUrl, e);
            throw new StoppingException("Could not close connection to: "+coordinatorUrl);
        }
    }

    public void onException(JMSException ex) {
        getLogger().fatalError("JMS Exception occured.  Shutting down client.", ex);
        setRun(false);
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

    protected static MessageProducer createTopicProducer(Session session, String subject) throws JMSException {
        Topic topic = session.createTopic(subject);

        MessageProducer producer = session.createProducer(topic);

        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        return producer;
    }

    protected static MessageConsumer createConsumer(Session session, String subject) throws JMSException {
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

    public synchronized void setRun(boolean run) {
        this.run = run;
    }

    public synchronized Connection getConnection() throws JMSException {
        return getClient().getConnection();
    }

    public synchronized Session getSession() throws JMSException {
        return getClient().getSession();
    }

    public synchronized Client getClient() {
        return client;
    }

    public synchronized void setClient(Client client) {
        this.client = client;
    }

    public static class Client {
        private final String brokerUrl;
        private final Connection connection;
        private final Session session;

        private Client(String brokerUrl, Connection connection, Session session) {
            this.brokerUrl = brokerUrl;
            this.connection = connection;
            this.session = session;
        }

        public Client(String brokerUrl) throws JMSException {
            this.brokerUrl = brokerUrl;
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            connection = connectionFactory.createConnection();
//            connection.setExceptionListener(this);
            connection.start();
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        }

        public String getBrokerUrl() {
            return brokerUrl;
        }

        public Connection getConnection() {
            return connection;
        }

        public Session getSession() {
            return session;
        }

        public MessageConsumer createQueueConsumer(String subject) throws JMSException {
            Queue queue = session.createQueue(subject);
            return session.createConsumer(queue);
        }

        public MessageConsumer createTopicConsumer(String subject) throws JMSException {
            Topic topic = session.createTopic(subject);
            return session.createConsumer(topic);
        }

        public MessageProducer createTopicProducer(String subject) throws JMSException {
            Topic topic = session.createTopic(subject);
            MessageProducer producer = session.createProducer(topic);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            return producer;
        }

        public synchronized Client reconnect() throws JMSException {
            Connection connection = connect();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            return new Client(brokerUrl, connection, session);
        }

        public synchronized void close() throws JMSException {
            session.close();
            connection.close();
        }

        private Connection connect() throws JMSException {
            return connect(5);
        }

        private Connection connect(int tries) throws JMSException {
            try {
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
                Connection connection = connectionFactory.createConnection();
//            connection.setExceptionListener(this);
                connection.start();
                return connection;
            } catch (JMSException e) {
                if (tries <= 0) {
                    throw e;
                } else {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException dontCare) {
                    }
                    return connect(--tries);
                }
            }
        }
    }
}
