package org.apache.geronimo.gbuild.agent;
/**
 * @version $Rev$ $Date$
 */

import junit.framework.TestCase;
import org.activemq.ActiveMQConnectionFactory;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.execution.shell.ShellBuildExecutor;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Message;
import javax.jms.Queue;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MainTest extends TestCase {

    static {
        org.apache.log4j.BasicConfigurator.configure();
    }

    private CVS cvs;
    private File shellScript;

    public void testMain() throws Exception {
        String[] args = new String[]{};

//        thread(new ContinuumBroker(args), false);
//
//        Thread.sleep(5000);

//        thread(new ContinuumBuildProducers(), false);
//        Thread.sleep(5000);

//        Main.main(args);

        //Thread.sleep(60000);

    }

    protected void setUp() throws Exception {

        File root = new File("/Users/dblevins/work/gbuild/trunk/target/it").getCanonicalFile();
        File cvsroot = new File(root, "cvs-root");
        File module = new File(root, "shell");

        deleteAndCreateDirectory( module );

        shellScript = createScript(module);

        cvs = new CVS(cvsroot);
        cvs.init();
        cvs._import(module,"shell");

    }

    private File createScript(File module) throws IOException, CommandLineException {
        File script;

        String EOL = System.getProperty( "line.separator" );

        boolean isWindows = System.getProperty("os.name").startsWith("Windows");

        boolean isCygwin = "true".equals(System.getProperty("cygwin"));

        if ( isWindows && !isCygwin ) {

            script = new File( module, "script.bat" );

            String content = "@ECHO OFF" + EOL
                    + "IF \"%*\" == \"\" GOTO end" + EOL
                    + "FOR %%a IN (%*) DO ECHO %%a" + EOL
                    + ":end" + EOL;

            FileUtils.fileWrite( script.getAbsolutePath(), content );

        } else {

            script = new File( module, "script.sh" );

            String content = "#!/bin/bash" + EOL + "for arg in \"$@\"; do echo $arg ; done"+EOL;

            FileUtils.fileWrite( script.getAbsolutePath(), content );

            Chmod.exec(module, "+x", script);
        }

        return script;
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public static class ContinuumBroker implements Runnable {
        private final String[] args;

        public ContinuumBroker(String[] args) {
            this.args = args;
        }

        public void run() {
            org.activemq.broker.impl.Main.main(args);
        }

        public static void main(String[] args) {
            org.apache.log4j.BasicConfigurator.configure();
            new ContinuumBroker(args).run();
        }
    }

    public static class Producer {
        public static void main(String[] args) throws Exception {
            MainTest test = new MainTest();
            test.setUp();
            test.new ContinuumBuildProducers().run();
        }
    }

    public class ContinuumBuildProducers implements Runnable {

        public void run() {
            try {
                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("BUILD.QUEUE");

                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);



                Project project = new Project();
                project.setId(10);
                project.setScmUrl("scm|cvs|local|" + cvs.getCvsroot().getAbsolutePath() + "|shell");
                project.setName("Shell Project");
                project.setVersion("3.0");
                project.setExecutorId(ShellBuildExecutor.ID);

                BuildDefinition bd = new BuildDefinition();
                bd.setId(20);
                bd.setBuildFile(shellScript.getAbsolutePath());
                bd.setArguments("");

                project.addBuildDefinition(bd);

                MapContinuumStore store = new MapContinuumStore();

                store.updateProject(project);

                HashMap map = new HashMap();
                map.put(ContinuumBuildAgent.KEY_STORE, store);
                map.put(ContinuumBuildAgent.KEY_PROJECT_ID, new Integer(project.getId()));
                map.put(ContinuumBuildAgent.KEY_BUILD_DEFINITION_ID, new Integer(bd.getId()));
                map.put(ContinuumBuildAgent.KEY_TRIGGER, new Integer(0));

                // Create a messages
                Message message = session.createObjectMessage(map);

                producer.send(message);

                // Clean up
                session.close();
                connection.close();
            }
            catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }

    }

    public static class BuildTaskProducer {
        public static void main(String[] args) throws Exception {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:41616");
            Connection connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue buildQueue = session.createQueue("BUILD.TASKS");
            MessageProducer producer = session.createProducer(buildQueue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            MapContinuumStore store = new MapContinuumStore();

            Project project = new Project();
            project.setId(14);
            project.setScmUrl("scm:svn:https://svn.apache.org/repos/asf/geronimo/trunk");
//            project.setScmUrl("scm:cvs:pserver:anonymous@cvs.openejb.codehaus.org:/home/projects/openejb/scm:openejb1");
            project.setName("OpenEJB");
            project.setVersion("1.0-SNAPSHOT");
            project.setExecutorId(ShellBuildExecutor.ID);
            project.setState(ContinuumProjectState.OK);
            store.updateProject(project);

            String[] goals = new String[]{"clean", "default"};

            for (int i = 0; i < goals.length; i++) {
                String goal = goals[i];
                BuildDefinition bd = new BuildDefinition();
                bd.setId(i);
                bd.setBuildFile("/usr/local/maven/bin/maven");
                bd.setArguments(goal);
                project.addBuildDefinition(bd);
                store.storeBuildDefinition(bd);

                HashMap map = new HashMap();

                map.put(AbstractContinuumAgentAction.KEY_STORE, store);
                map.put(AbstractContinuumAgentAction.KEY_PROJECT_ID, new Integer(project.getId()));
                map.put(AbstractContinuumAgentAction.KEY_BUILD_DEFINITION_ID, new Integer(bd.getId()));
                map.put(AbstractContinuumAgentAction.KEY_TRIGGER, new Integer(ContinuumProjectState.TRIGGER_FORCED));

                producer.send(session.createObjectMessage(map));
            }

            connection.close();
            session.close();
        }
    }


    public static void deleteAndCreateDirectory(File directory)
            throws IOException {
        if (directory.isDirectory()) {
            FileUtils.deleteDirectory(directory);
        }

        assertTrue("Could not make directory " + directory, directory.mkdirs());
    }
}