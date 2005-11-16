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

import javax.jms.Session;
import javax.jms.MessageConsumer;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class BuildResultsContinuumAgent extends AbstractContinuumBuildAgent {

    /**
     * @plexus.requirement
     */
    private BuildResultsExtentionManager extentionManager;

    /**
     * @plexus.configuration
     */
    private String buildResultsTopic;


    public void run() {
        try {
            getLogger().info("Continuum Build Agent starting.");
            getLogger().info("coordinatorUrl "+coordinatorUrl);
            getLogger().info("buildResultsTopic "+buildResultsTopic);

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer resultsConsumer = createConsumer(session, buildResultsTopic);

            getLogger().info("Continuum Build Agent started and waiting for work.");

            while (run) {
                // Wait for a message
                Message message = resultsConsumer.receive(1000);

                if (message == null){

                    continue;

                } else if (message instanceof ObjectMessage) {

                    try {
                        getLogger().info("Message Received "+ message.getJMSMessageID() +" on "+ connection.getClientID()+":"+buildResultsTopic);

                        ObjectMessage objectMessage = (ObjectMessage) message;

                        Map context = getMap(objectMessage, message);

                        execute(context);

                        getLogger().info("Finished processing "+ message.getJMSMessageID());

                    } catch (Exception e) {
                        getLogger().error("Failed Processing message "+message.getJMSMessageID());
                    }

                } else {
                    getLogger().warn("Agent received incorrect message type: "+message.getClass().getName());
                }
            }

            resultsConsumer.close();
            session.close();
        } catch (Exception e) {
            getLogger().error("Agent failed.", e);
        }
    }

    public void execute(Map map) throws Exception {
        extentionManager.execute(map);
    }


}
