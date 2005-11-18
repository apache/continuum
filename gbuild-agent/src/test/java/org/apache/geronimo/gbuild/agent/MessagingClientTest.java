package org.apache.geronimo.gbuild.agent;
/**
 * @version $Rev$ $Date$
 */

import junit.framework.TestCase;
import org.activemq.broker.impl.BrokerContainerImpl;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

public class MessagingClientTest extends TestCase {
    private TestLogger logger;

    public void __testSend() throws Exception {
        final String url = "tcp://localhost:44668";

        BusyBee[] bees = new BusyBee[1];
        for (int i = 0; i < bees.length; i++) {
            bees[i] = new BusyBee("bee" + i, url);
        }

        Runnable worker = new Runnable(){
            public void run() {
                String[] args = new String[]{url};
                org.activemq.broker.impl.Main.main(args);
            }
        };
        runAndCrash(worker);
        runAndCrash(worker);
        runAndCrash(worker);

    }

    public void testNothing(){
        
    }
    private void runAndCrash(Runnable worker) throws InterruptedException {
        Thread thread = new Thread(worker);
        thread.start();
        Thread.sleep(30000);
        thread.stop();
    }

    public void _testSend() throws Exception {

        logger = new TestLogger("hive");
        String hiveLocation = "tcp://localhost:44668";

        BrokerContainerImpl behive = new BrokerContainerImpl();
        behive.addConnector(hiveLocation);

        logger.debug("1. STARTING...");
        behive.start();
        logger.debug("1. STARTED");

        BusyBee[] bees = new BusyBee[2];
        for (int i = 0; i < bees.length; i++) {
            bees[i] = new BusyBee("bee" + i, hiveLocation);
        }

        work(30000);

        logger.debug("1. STOPPING...");
        behive.stop();
        logger.debug("1. STOPPED");

        rest(10000);
        behive = new BrokerContainerImpl();
        behive.addConnector(hiveLocation);

        logger.debug("2. STARTING...");
        behive.start();
        logger.debug("2. STARTED");

        work(30000);

        logger.debug("2. STOPPING...");
        behive.stop();
        logger.debug("2. STOPPED");

        rest(10000);

        behive = new BrokerContainerImpl();
        behive.addConnector(hiveLocation);
        logger.debug("3. STARTING...");
        behive.start();
        logger.debug("3. STARTED");

        work(30000);

        for (int i = 0; i < bees.length; i++) {
            BusyBee bee = bees[i];
            assertTrue("alive", bee.isRunning());
            assertNull("fatality", bee.getFatality());
            bee.stop();
        }

        work(10000);

        logger.debug("3. STOPPING...");
        behive.stop();
        logger.debug("3. STOPPED");
        logger.debug("DONE");
    }


    private void work(int i) throws InterruptedException {
        Thread.sleep(i);
    }

    private void rest(int i) throws InterruptedException {
        Thread.sleep(i);
    }

    public static class BusyBee implements Runnable, ExceptionListener {
        private final MessagingClient client;
        private boolean run;
        public Exception fatality;
        private TestLogger logger;
        private String name;

        public BusyBee(String name, String url) throws JMSException {
            this.logger = new TestLogger(name);
            this.client = new MessagingClient(url, logger, this);
            this.name = name;
            this.run = true;
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        public void run() {
            String subject = "flowers";
            int i = 1;

            try {
                client.addQueue(subject);
            } catch (JMSException exception) {
                die(exception);
            }

            while (isRunning()) {
                try {
                    client.send(subject, name + " collecting pollen" + (i++));
                    client.send(subject, name + " collecting pollen" + (i++));
                    client.send(subject, name + " collecting pollen" + (i++));
                    printMessage(client.receive(subject, 5000));
                    printMessage(client.receive(subject, 5000));
                    printMessage(client.receive(subject, 5000));
                } catch (JMSException e) {
                    logger.debug("Hickup - " + e.getMessage());
                }
            }
        }

        private void printMessage(Message messagee) throws JMSException {
            ObjectMessage message = (ObjectMessage) messagee;
            if (message == null) {
                logger.debug("timeout");
                return;
            }
            logger.debug("Buzzzz ... " + message.getObject());
        }

        public synchronized Exception getFatality() {
            return fatality;
        }

        public synchronized void setFatality(Exception fatality) {
            this.fatality = fatality;
        }

        public synchronized boolean isRunning() {
            return run;
        }

        public synchronized void die(Exception e) {
            run = false;
            fatality = e;
        }

        public synchronized void stop() {
            run = false;
        }

        public void onException(JMSException jmsException) {
            logger.error("Can't Find Hive!!! (" + jmsException.getMessage() + ")");
            die(jmsException);
        }
    }
}