package org.apache.geronimo.gbuild.agent;
/**
 * @version $Rev$ $Date$
 */

import junit.framework.TestCase;
import org.activemq.broker.impl.BrokerContainerImpl;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.BasicConfigurator;

import javax.jms.JMSException;
import javax.jms.ExceptionListener;
import javax.jms.Message;
import javax.jms.ObjectMessage;

public class MessagingClientTest extends TestCase {
    private TestLogger logger;

    public void testSend() throws Exception {

        logger = new TestLogger("hive");
        String hiveLocation = "tcp://localhost:44668";

        BrokerContainerImpl behive = new BrokerContainerImpl();
        behive.addConnector(hiveLocation);

        logger.debug("STARTING...");
        behive.start();
        logger.debug("STARED");

        BusyBee[] bees = new BusyBee[3];
        for (int i = 0; i < bees.length; i++) {
            bees[i] = new BusyBee("bee"+i, hiveLocation);
        }

        work(10000);

        logger.debug("STOPPING...");
        behive.stop();
        logger.debug("STOPPED");

        rest(1000);

        logger.debug("STARTING...");
        behive.start();
        logger.debug("STARED");

        work(10000);

        logger.debug("STOPPING...");
        behive.stop();
        logger.debug("STOPPED");

        rest(1000);

        logger.debug("STARTING...");
        behive.start();
        logger.debug("STARED");

        work(10000);

        for (int i = 0; i < bees.length; i++) {
            BusyBee bee = bees[i];
            assertTrue("alive", bee.isRunning());
            assertNotNull("fatality", bee.fatality);
            bee.die();
        }

        logger.debug("STOPPING...");
        behive.stop();
        logger.debug("STOPPED");
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
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        public void run() {
            run = true;

            try {
                String subject = "flowers";
                client.addQueue(subject);

                while (run) {
                    client.send(subject, name + " collecting pollen");
                    ObjectMessage message = (ObjectMessage) client.receive(subject, 30000);
                    logger.debug("Buzzzz ... "+message.getObject());
                }
            } catch (JMSException e) {
                logger.error("Hive Destroyed!!! ("+e.getMessage()+")");
                fatality = e;
                run = false;
            }
        }

        public synchronized boolean isRunning() {
            return run;
        }

        public synchronized void die(){
            run = false;
        }

        public void onException(JMSException jmsException) {
            logger.error("Can't Find Hive!!! ("+jmsException.getMessage()+")");
            fatality = jmsException;
            run = false;
        }
    }
}