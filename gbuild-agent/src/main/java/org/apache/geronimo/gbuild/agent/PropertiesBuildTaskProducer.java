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
import org.apache.maven.continuum.execution.shell.ShellBuildExecutor;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
public class PropertiesBuildTaskProducer extends AbstractLogEnabled implements Startable, DirectoryMonitor.Listener {

    /**
     * @plexus.configuration
     */
    private String coordinatorUrl;

    /**
     * @plexus.configuration
     */
    private String buildTaskQueue;

    /**
     * @plexus.configuration
     */
    private String includePrefix = "include.";

    /**
     * @plexus.configuration
     */
    private String buildPrefix = "build.";

    /**
     * @plexus.configuration
     */
    private String headerPrefix = "header.";

    /**
     * @plexus.configuration
     */
    private String watchDirectory;

    /**
     * @plexus.configuration
     */
    private int pollInterval;

    private DirectoryMonitor scanner;

    public synchronized void start() throws StartingException {
        File dir = new File(watchDirectory);
        scanner = new DirectoryMonitor(dir, this, pollInterval);
        Thread thread = new Thread(scanner);
        thread.setDaemon(false);
        thread.start();
    }

    public synchronized void stop() throws StoppingException {
        scanner.stop();
    }

    public boolean fileAdded(File file) {
        Properties properties = null;
        try {
            FileInputStream in = new FileInputStream(file);
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            getLogger().error("Unable to load properties file: "+file.getAbsolutePath(), e);
        }
        try {
            queueBuildTasks(properties);
        } catch (Exception e) {
            getLogger().error("Unable to process file: "+file.getAbsolutePath(), e);
        }
        return true;
    }

    public boolean fileRemoved(File file) {
        return true;
    }

    public void fileUpdated(File file) {
    }


    // TODO: Improve this so you can have ${my-property} parts to property values
    private void queueBuildTasks(Properties def) throws Exception {

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(coordinatorUrl);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue buildQueue = session.createQueue(buildTaskQueue);

        MessageProducer producer = session.createProducer(buildQueue);

        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        int id = getInteger(def, "project.id");

        String scmUrl = getString(def, "project.scmUrl");

        String name = getString(def, "project.name");

        String version = getString(def, "project.version");

        String buildFile = getString(def, "project.buildFile");

        getLogger().info("Project - " + id + " - " + name + " " + version);

        String executor = ShellBuildExecutor.ID;

        MapContinuumStore store = new MapContinuumStore();

        Project project = new Project();
        project.setId(id);
        project.setScmUrl(scmUrl);
        project.setName(name);
        project.setVersion(version);

        project.setExecutorId(executor);

        project.setState(ContinuumProjectState.OK);
        store.updateProject(project);

        int buildIds = 0;
        for (Iterator iterator = def.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            if (!key.startsWith(buildPrefix)) {
                continue;
            }

            getLogger().info("Build - " + buildIds + " - " + key + " " + value);

            BuildDefinition bd = new BuildDefinition();
            bd.setId(buildIds++);
            bd.setBuildFile(buildFile);
            bd.setArguments(value);

            project.addBuildDefinition(bd);
            store.storeBuildDefinition(bd);

            HashMap map = new HashMap();

            map.put(key, value);

            ContinuumBuildAgent.setStore(map, store);
            ContinuumBuildAgent.setProjectId(map, project.getId());
            ContinuumBuildAgent.setBuildDefinitionId(map, bd.getId());
            ContinuumBuildAgent.setTrigger(map, ContinuumProjectState.TRIGGER_FORCED);

            addProperties("project.", def, map);
            addProperties(headerPrefix, def, map);
            addProperties(includePrefix, def, map);

            producer.send(session.createObjectMessage(map));
        }

        session.close();
        connection.close();
    }

    private void addProperties(String prefix, Properties def, HashMap map) {
        for (Iterator iterator = def.entrySet().iterator(); iterator.hasNext();) {

            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            if (key.startsWith(prefix)) {
                map.put(key, value);
            }
        }
    }

    protected static String getString(Map context, String key) {
        return (String) getObject(context, key);
    }

    protected static String getString(Map context, String key, String defaultValue) {
        return (String) getObject(context, key, defaultValue);
    }

    public static boolean getBoolean(Map context, String key) {
        return ((Boolean) getObject(context, key)).booleanValue();
    }

    protected static int getInteger(Map context, String key) {
        return ((Integer) getObject(context, key, null)).intValue();
    }

    protected static Object getObject(Map context, String key) {
        if (!context.containsKey(key)) {
            throw new RuntimeException("Missing key '" + key + "'.");
        }

        Object value = context.get(key);

        if (value == null) {
            throw new RuntimeException("Missing value for key '" + key + "'.");
        }

        return value;
    }

    protected static Object getObject(Map context, String key, Object defaultValue) {
        Object value = context.get(key);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

}
