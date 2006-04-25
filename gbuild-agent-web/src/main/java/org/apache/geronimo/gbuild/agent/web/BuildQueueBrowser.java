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
package org.apache.geronimo.gbuild.agent.web;

import org.apache.geronimo.gbuild.agent.Client;
import org.apache.geronimo.gbuild.agent.ClientManager;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * @version $Rev$ $Date$
 */
public class BuildQueueBrowser extends AbstractLogEnabled implements WebComponent{

    /**
     * @plexus.requirement
     */
    private ClientManager clientManager;

    /**
     * @plexus.configuration
     */
    private String buildTaskQueue;

    /**
     * @plexus.configuration
     */
    private String keyList;

    /**
     * @plexus.configuration
     */
    private int maxWidth;

    public void service(HttpServletRequest request, HttpServletResponse response) throws Exception {
        getLogger().info("Browsing: "+buildTaskQueue);

        Client client = clientManager.getClient();
        Session session = client.getSession();
        Queue queue = session.createQueue(buildTaskQueue);
        QueueBrowser browser = session.createBrowser(queue);
        getLogger().info("Browser: "+browser);

        Enumeration enumeration = browser.getEnumeration();
        getLogger().info("Enum: "+enumeration);
    
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body><h1>Browsing "+buildTaskQueue+"</h1>");
        out.println("<table>");


        String[] keys = keyList.split(",");
        out.println("<tr>");
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            out.println("<td><b>");
            out.print(key);
            out.println("</b></td>");
        }
        out.println("</tr>");

        getLogger().info("Has elements: "+enumeration.hasMoreElements());
        while (enumeration.hasMoreElements()) {
            Message message = (Message) enumeration.nextElement();
            getLogger().info("Message: "+message);
            if (message instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) message;
                Object object = objectMessage.getObject();
                if (object instanceof HashMap) {
                    HashMap map = (HashMap) object;

                    out.println("<tr>");
                    for (int i = 0; i < keys.length; i++) {
                        String key = keys[i];
                        Object obj = map.get(key);

                        String data = obj.toString();
                        data = data.substring(0, Math.min(data.length(), maxWidth));

                        out.println("<td>");
                        // If the data itself contained xml or html, that would be bad.
                        out.print(data);
                        out.println("</td>");
                    }
                    out.println("</tr>");
                }
            }
        }
        browser.close();
        out.println("</table>");
        out.println("</body></html>");
        out.close();
    }
}
