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

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

/**
 * @version $Rev$ $Date$
 */
public class ClientManager extends AbstractLogEnabled implements ExceptionListener {

    /**
     * @plexus.configuration
     */
    private String brokerUrl;

    /**
     * @plexus.configuration
     */
    private int pingInterval;

    /**
     * @plexus.configuration
     */
    private int reconnectAttempts;

    /**
     * @plexus.configuration
     */
    private int reconnectDelay;

    private Client client;

    public ClientManager(String brokerUrl, int pingInterval, int reconnectAttempts, int reconnectDelay) {
        this.brokerUrl = brokerUrl;
        this.pingInterval = pingInterval;
        this.reconnectAttempts = reconnectAttempts;
        this.reconnectDelay = reconnectDelay;
    }

    public synchronized void start() throws StartingException {
        try {
            setClient(new Client(brokerUrl, this, getLogger(), reconnectDelay, reconnectAttempts, pingInterval));
        } catch (Throwable e) {
            getLogger().error("Could not create connection to: " + brokerUrl, e);
            throw new StartingException("Could not create connection to: " + brokerUrl);
        }
    }

    public synchronized void stop() throws StoppingException {
        try {
            getClient().close();
        } catch (JMSException e) {
            getLogger().error("Could not close connection to: " + brokerUrl, e);
            throw new StoppingException("Could not close connection to: " + brokerUrl);
        }
    }

    public void onException(JMSException ex) {
        getLogger().fatalError("JMS Exception occured.  Attempting reconnect.", ex);
        try {
            reconnect();
        } catch (JMSException e) {
            getLogger().error("Reconnect failed.", e);
        }
    }

    private synchronized void reconnect() throws JMSException {
        this.client = client.reconnect();
    }

    public synchronized Client getClient() {
        return client;
    }

    public synchronized void setClient(Client client) {
        this.client = client;
    }
}
