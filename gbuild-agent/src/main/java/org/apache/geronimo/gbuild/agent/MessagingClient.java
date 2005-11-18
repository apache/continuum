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

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;
import org.activemq.ActiveMQConnectionFactory;
import org.codehaus.plexus.logging.Logger;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import java.io.Serializable;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class MessagingClient implements ExceptionListener {

    private final Map destinations;
    private final String transportUrl;
    private final ExceptionListener listener;
    private final Logger logger;
    private final Session session;
    private final Connection connection;
    private boolean failed;
    private int maxAttempts = 10;
    private JMSException exception;

    public MessagingClient(String transportUrl, Logger logger) throws JMSException {
        this(transportUrl, logger, new NullListener());
    }

    public MessagingClient(String transportUrl, Logger logger, ExceptionListener listener) throws JMSException {
        this.transportUrl = transportUrl;
        this.logger = logger;
        this.destinations = new ConcurrentHashMap();
        this.listener = listener;
        connection = connect();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public static class NullListener implements ExceptionListener {
        public void onException(JMSException jmsException) {
        }
    }

    public Logger getLogger() {
        return logger;
    }

//    private synchronized void reset() throws JMSException {
////        getLogger().info("Resetting connection, session and destinations");
////        connection = connect();
////        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
////            // Reset all instances of Queue and Topic
////            for (Iterator iterator = destinations.entrySet().iterator(); iterator.hasNext();) {
////                Map.Entry entry = (Map.Entry) iterator.next();
////                String subject = (String) entry.getKey();
////                Destination destination = (Destination) entry.getValue();
////                if (destination instanceof Queue){
////                    Queue queue = session.createQueue(subject);
////                    destinations.put(subject, queue);
////                } else {
////                    Topic queue = session.createTopic(subject);
////                    destinations.put(subject, queue);
////                }
////            }
//    }

    public synchronized void close() throws JMSException {
        session.close();
        connection.close();
    }

    public void addTopic(String subject) throws JMSException {
        destinations.put(subject, Topic.class);
//        synchronized (destinations) {
//            Topic queue = session.createTopic(subject);
//            destinations.put(subject, queue);
//        }
    }

    public void addQueue(String subject) throws JMSException {
        destinations.put(subject, Queue.class);
//        synchronized (destinations) {
//            Queue queue = session.createQueue(subject);
//            destinations.put(subject, queue);
//        }
    }

    public void send(String subject, Serializable content) throws JMSException {
        Class type = (Class) destinations.get(subject);
        if (type == null) {
            throw new IllegalStateException("Desination " + subject + " not configured.");
        }
//        Connection connection = connect();
//        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination;
        if (type == Topic.class) {
            destination = session.createTopic(subject);
        } else {
            destination = session.createQueue(subject);
        }
        MessageProducer producer = session.createProducer(destination);
        Message message = session.createObjectMessage(content);
        producer.send(message);
    }

    public Message receive(String subject, long timeout) throws JMSException {
        Class type = (Class) destinations.get(subject);
        if (type == null) {
            throw new IllegalStateException("Desination " + subject + " not configured.");
        }
//        Connection connection = connect();
//        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination;
        if (type == Topic.class) {
            destination = session.createTopic(subject);
        } else {
            destination = session.createQueue(subject);
        }
        MessageConsumer consumer = session.createConsumer(destination);
        return consumer.receive(timeout);
    }

    private Connection connect() throws JMSException {
        return connect(1);
    }

    private Connection connect(int tries) throws JMSException {
        getLogger().debug("Attempt " + tries + " to establish connection to " + transportUrl);
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(transportUrl);
            Connection connection = connectionFactory.createConnection();
//            connection.setExceptionListener(this);
            connection.start();
            return connection;
        } catch (JMSException e) {
            if (tries >= maxAttempts) {
                throw e;
            } else {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException dontCare) {
                }
                return connect(++tries);
            }
        }
    }


    public void onException(JMSException jmsException) {
        getLogger().error(jmsException.getMessage());
//        if (failed) {
//            listener.onException(jmsException);
//        } else {
//            try {
//                reset();
//            } catch (JMSException e) {
//                getLogger().error("Unable to restablish a connection to " + transportUrl);
//                failed = true;
//                exception = jmsException;
//                listener.onException(jmsException);
//            }
//        }
    }
}
