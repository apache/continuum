package org.apache.geronimo.gbuild;

import org.activemq.ActiveMQConnection;
import org.activemq.ActiveMQConnectionFactory;
import org.activemq.util.IndentPrinter;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.io.IOException;

/**
 * Hello world!
 */
public class App {

    public static void main(final String[] args) {
        Runnable broker = new Runnable() {
            public void run() {
                org.activemq.broker.impl.Main.main(args);
            }
        };
        thread(broker, true);

        thread(new HelloWorldProducer(), false);

        thread(new HelloWorldConsumer(), false);

        System.out.println("Hello World!");
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public static class HelloWorldProducer implements Runnable {

        public void run() {
            try {
                String url = "tcp://localhost:61616";
                String subject = "EXAMPLES";
                String clientID = "superConsumer";
                boolean topic = false;
                boolean durable = false;
                boolean transacted = false;
                int ackMode = Session.AUTO_ACKNOWLEDGE;
                int timeToLive = 0;

                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                if (durable && clientID != null) {
                    connection.setClientID(clientID);
                }
                connection.start();

                // Create a Session
                Session session = connection.createSession(transacted, ackMode);

                // Create the destination (Topic or Queue)
                Destination destination = null;
                if (topic) {
                    destination = session.createTopic(subject);
                } else {
                    destination = session.createQueue(subject);
                }

                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(destination);
                if (durable) {
                    producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                } else {
                    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                }
                if (timeToLive != 0) {
                    producer.setTimeToLive(timeToLive);
                }

                // Create a messages
                TextMessage message = session.createTextMessage("Hello world! From: " + this.hashCode());

                // Tell the producer to send the message
                producer.send(message);

                // If we are running with transaction support, commit
                if (transacted) {
                    session.commit();
                }

                // lets dump the stats for fun, optional
                dumpStats(connection);

                // Clean up
                session.close();
                connection.close();
            }
            catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }

        protected void dumpStats(Connection connection) {
            ActiveMQConnection c = (ActiveMQConnection) connection;
            c.getConnectionStats().dump(new IndentPrinter());
        }
    }

    public static class HelloWorldConsumer implements Runnable, MessageListener, ExceptionListener {

        String url = "tcp://localhost:61616";
        String subject = "EXAMPLES";
        String consumerName = "james";
        String clientID = "superConsumer";
        boolean topic = false;
        boolean durable = false;
        boolean transacted = false;
        int ackMode = Session.AUTO_ACKNOWLEDGE;
        int timeToLive = 0;

        protected int count = 0;
        protected int dumpCount = 10;
        protected boolean verbose = true;
        protected int maxiumMessages = 1;
        private boolean running;
        private Session session;
        private long sleepTime = 0;

        public void run() {
            try {
                running = true;

                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                if (durable && clientID != null) {
                    connection.setClientID(clientID);
                }
                connection.setExceptionListener(this);

                // Create a Session
                session = connection.createSession(transacted, ackMode);

                // Create the destination (Topic or Queue)
                Destination destination = null;
                if (topic) {
                    destination = session.createTopic(subject);
                } else {
                    destination = session.createQueue(subject);
                }

                MessageConsumer consumer = null;
                if (durable && topic) {
                    consumer = session.createDurableSubscriber((Topic) destination, consumerName);
                } else {
                    consumer = session.createConsumer(destination);
                }

                if (maxiumMessages <= 0) {
                    consumer.setMessageListener(this);
                }

                if (maxiumMessages > 0) {
                    consumeMessagesAndClose(connection, session, consumer);
                }
            }
            catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }

        public void onMessage(Message message) {
            try {
                if (message instanceof TextMessage) {
                    TextMessage txtMsg = (TextMessage) message;
                    if (verbose) {

                        String msg = txtMsg.getText();
                        if (msg.length() > 50) {
                            msg = msg.substring(0, 50) + "...";
                        }

                        System.out.println("Received: " + msg);
                    }
                } else {
                    if (verbose) {
                        System.out.println("Received: " + message);
                    }
                }
                if (transacted) {
                    session.commit();
                }
                /*
                if (++count % dumpCount == 0) {
                    dumpStats(connection);
                }
                */
            }
            catch (JMSException e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            } finally {
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        synchronized public void onException(JMSException ex) {
            System.out.println("JMS Exception occured.  Shutting down client.");
            running = false;
        }

        synchronized boolean isRunning() {
            return running;
        }

        protected void consumeMessagesAndClose(Connection connection, Session session, MessageConsumer consumer) throws JMSException, IOException {
            System.out.println("We are about to wait until we consume: " + maxiumMessages + " message(s) then we will shutdown");

            for (int i = 0; i < maxiumMessages && isRunning();) {
                Message message = consumer.receive(1000);
                if (message != null) {
                    i++;
                    onMessage(message);
                }
            }
            System.out.println("Closing connection");
            consumer.close();
            session.close();
            connection.close();
        }


    }
}
