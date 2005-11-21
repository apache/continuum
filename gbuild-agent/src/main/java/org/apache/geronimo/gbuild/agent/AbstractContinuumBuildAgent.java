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
import org.codehaus.plexus.logging.Logger;
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
            setClient(new Client(coordinatorUrl, this, getLogger()));
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
        getLogger().fatalError("JMS Exception occured.  Attempting reconnect.", ex);
        try {
            reconnect();
        } catch (JMSException e) {
            getLogger().error("Reconnect failed.", e);
        }
//        setRun(false);
    }

    public synchronized void reconnect() throws JMSException {
        this.client = client.reconnect();
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

    public static class Client implements ExceptionListener {
        private final String brokerUrl;
        private final Connection connection;
        private final Session session;
        private final ExceptionListener listener;
        private final Logger logger;
        private boolean connected = true;

        private Client(Client old, Connection connection, Session session) {
            this.brokerUrl = old.brokerUrl;
            this.connection = connection;
            this.session = session;
            this.listener = old.listener;
            this.logger = old.logger;
        }

        public Client(String brokerUrl, ExceptionListener listener, Logger logger) throws JMSException {
            this.brokerUrl = brokerUrl;
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            connection = connectionFactory.createConnection();
            connection.setExceptionListener(this);
            connection.start();
            this.listener = listener;
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            this.logger = logger;
        }

        public synchronized boolean isConnected() {
            return connected;
        }

        private Logger getLogger() {
            return logger;
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
            failed();
            Connection connection = connect();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            return new Client(this, connection, session);
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
                connection.setExceptionListener(this);
                connection.start();
                getLogger().info("Client reconnect successful.");
                return connection;
            } catch (JMSException e) {
                if (tries <= 0) {
                    getLogger().info("Client reconnect failed.  Giving up.", e);
                    throw e;
                } else {
                    try {
                        int delay = 5000;
                        getLogger().info("Client reconnect failed.  Trying again in "+delay+" milliseconds. ("+ e.getMessage()+")");
                        Thread.sleep(delay);
                    } catch (InterruptedException dontCare) {
                    }
                    return connect(--tries);
                }
            }
        }

        /**
         * Marks this client as failed and returns its previous state
         * @return false if the client was not previously in a failed state
         */
        private synchronized boolean failed() {
            boolean failed = !connected;
            connected = false;
            return failed;
        }

        public void onException(JMSException jmsException) {
            getLogger().info("JMSException "+this.hashCode());
            this.listener.onException(jmsException);
        }
    }
}
