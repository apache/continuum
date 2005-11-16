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
import org.apache.maven.continuum.buildcontroller.BuildController;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.configuration.ConfigurationLoadingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.ObjectMessage;
import java.net.InetAddress;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

/**
 * @version $Rev$ $Date$
 */
public class ContinuumBuildAgent extends AbstractContinuumBuildAgent {

    public static final String KEY_BUILD_RESULTS = "build-results";

    /**
     * @plexus.requirement
     */
    private BuildAgentExtentionManager extentionManager;

    /**
     * @plexus.requirement
     */
    private BuildController controller;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

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
    private String buildTaskQueue;

    /**
     * @plexus.configuration
     */
    private String buildResultsTopic;

    /**
     * @plexus.configuration
     */
    private String workingDirectory;

    /**
     * @plexus.configuration
     */
    private String buildOutputDirectory;

    public void run() {
        try {
            getLogger().info("Continuum Build Agent starting.");
            getLogger().info("coordinatorUrl "+coordinatorUrl);
            getLogger().info("buildTaskQueue "+buildTaskQueue);
            getLogger().info("buildResultsTopic "+buildResultsTopic);
            getLogger().info("workingDirectory "+workingDirectory);
            getLogger().info("buildOutputDirectory "+buildOutputDirectory);
            getLogger().info("adminAddress "+adminAddress);
            getLogger().info("contributor "+contributor);

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer buildConsumer = createQueueConsumer(session, buildTaskQueue);

            MessageProducer resultsProducer = createTopicProducer(session, buildResultsTopic);

            getLogger().info("Continuum Build Agent started and waiting for work.");

            while (run) {
                // Wait for a message
                Message message = buildConsumer.receive(1000);

                if (message == null){

                    continue;

                } else if (message instanceof ObjectMessage) {

                    try {
                        getLogger().info("Message Received "+ message.getJMSMessageID() +" on "+ connection.getClientID()+":"+buildTaskQueue);

                        ObjectMessage objectMessage = (ObjectMessage) message;

                        Map build = getMap(objectMessage, message);

                        execute(build);

                        HashMap results = getBuildResults(build);

                        ObjectMessage resultMessage = session.createObjectMessage(results);

                        getLogger().info("Finished processing "+ message.getJMSMessageID());

                        resultsProducer.send(resultMessage);

                        getLogger().info("Results sent to "+ buildResultsTopic );

                    } catch (Exception e) {
                        getLogger().error("Failed Processing message "+message.getJMSMessageID());
                    }

                } else {
                    getLogger().warn("Agent received incorrect message type: "+message.getClass().getName());
                }
            }

            buildConsumer.close();
            session.close();
        } catch (Exception e) {
            getLogger().error("Agent failed.", e);
        }
    }

    private HashMap getBuildResults(Map build) {
        return (HashMap) getObject(build, KEY_BUILD_RESULTS);
    }

    public void execute(Map context) throws Exception {

        ContinuumStore store = getContinuumStore(context);

        ThreadContextContinuumStore.setStore(store);

        init();

        int projectId = getProjectId(context);

        int buildDefinitionId = getBuildDefinitionId(context);

        int trigger = getTrigger(context);

        extentionManager.preProcess(context);

        build(projectId, buildDefinitionId, trigger);

        HashMap results = new HashMap();

        context.put(KEY_BUILD_RESULTS, results);

        results.put(KEY_STORE, store);

        results.put(KEY_PROJECT_ID, new Integer(projectId));

        results.put(KEY_BUILD_DEFINITION_ID, new Integer(buildDefinitionId));

        results.put(KEY_TRIGGER, new Integer(trigger));

        setSystemProperty(results, KEY_OS_VERSION);

        setSystemProperty(results, KEY_OS_NAME);

        setSystemProperty(results, KEY_JAVA_VERSION);

        setSystemProperty(results, KEY_JAVA_VENDOR);

        InetAddress localHost = InetAddress.getLocalHost();

        results.put(KEY_HOST_NAME, localHost.getHostName());

        results.put(KEY_HOST_ADDRESS, localHost.getHostAddress());

        results.put(KEY_CONTRIBUTOR, contributor);

        results.put(KEY_ADMIN_ADDRESS, adminAddress);

        extentionManager.postProcess(context, results);
    }

    public void init() throws ConfigurationLoadingException {
        configurationService.load();
        configurationService.setWorkingDirectory(new File(workingDirectory));
        configurationService.setBuildOutputDirectory(new File(buildOutputDirectory));
    }

    public void build(int projectId, int buildDefinitionId, int trigger) {
        controller.build(projectId, buildDefinitionId, trigger);
    }

    public static void setSystemProperty(Map results, String name) {
        results.put(name, System.getProperty(name));
    }

}
