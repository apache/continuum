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

import org.codehaus.plexus.logging.Logger;
import org.activemq.ActiveMQConnectionFactory;

import javax.jms.JMSException;
import javax.jms.ExceptionListener;
import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.jms.ObjectMessage;
import javax.jms.Message;
import javax.jms.DeliveryMode;
import java.net.SocketException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class MessagingClient implements ExceptionListener {

    private final HashMap destinations;
    private final String transportUrl;
    private final ExceptionListener listener;
    private final Logger logger;
    private Session session;
    private Connection connection;
    private boolean failed;
    private int maxAttempts = 10;

    public MessagingClient(String transportUrl, Logger logger, ExceptionListener listener) throws JMSException {
        this.transportUrl = transportUrl;
        this.logger = logger;
        this.destinations = new HashMap();
        this.listener = listener;
        connection = connect();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public Logger getLogger() {
        return logger;
    }

    private void reset() throws JMSException {
        getLogger().info("Resetting connection, session and destinations");
        synchronized (destinations) {
            connection = connect();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Reset all instances of Queue and Topic
            for (Iterator iterator = destinations.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String subject = (String) entry.getKey();
                Destination destination = (Destination) entry.getValue();
                if (destination instanceof Queue){
                    Queue queue = session.createQueue(subject);
                    destinations.put(subject, queue);
                } else {
                    Topic queue = session.createTopic(subject);
                    destinations.put(subject, queue);
                }
            }
        }
    }

    public void close() throws JMSException {
        synchronized (destinations) {
            session.close();
            connection.close();
        }
    }

    public void addTopic(String subject) throws JMSException {
        synchronized (destinations) {
            Topic queue = session.createTopic(subject);
            destinations.put(subject, queue);
        }
    }

    public void addQueue(String subject) throws JMSException {
        synchronized (destinations) {
            Queue queue = session.createQueue(subject);
            destinations.put(subject, queue);
        }
    }

    public void send(String subject, Serializable message) throws JMSException {
        Destination destination = getDestination(subject);
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        ObjectMessage objectMessage = session.createObjectMessage(message);
        producer.send(objectMessage);
    }

    public Message receive(String subject, long timeout) throws JMSException {
        Destination destination = getDestination(subject);
        MessageConsumer consumer = session.createConsumer(destination);
        return consumer.receive(timeout);
    }


    private Destination getDestination(String subject) {
        synchronized (destinations) {
            return (Destination) destinations.get(subject);
        }
    }


    private Connection connect() throws JMSException {
        return connect(1);
    }

    private Connection connect(int tries) throws JMSException {
        getLogger().debug("Attempt "+tries+" to establish connection to "+transportUrl);
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(transportUrl);
            Connection connection = connectionFactory.createConnection();
            connection.start();
            connection.setExceptionListener(this);
            return connection;
        } catch (JMSException e) {
            if (tries >= maxAttempts){
                throw e;
            } else {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException dontCare) {}
                return connect(++tries);
            }
        }
    }


    public void onException(JMSException jmsException) {
        getLogger().error(jmsException.getMessage());
        Throwable cause = jmsException.getCause();
        if (!failed && cause instanceof SocketException) {
            try {
                reset();
            } catch (JMSException e) {
                getLogger().error("Unable to restablish a connection to "+transportUrl);
                failed = true;
                listener.onException(jmsException);
            }
        } else {
            listener.onException(jmsException);
        }
    }
}
