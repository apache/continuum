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

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Iterator;

/**
 * @version $Rev$ $Date$
 */
public class WebComponentInvoker extends HttpServlet implements javax.servlet.Servlet {


    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contextPath = request.getContextPath() + "/";
        String requestURI = request.getRequestURI().replaceFirst(contextPath, "");
        System.out.println("requestURI = " + requestURI);
        if (requestURI.length() > 0) {
            WebComponent webComponent = null;
            try {
                webComponent = (WebComponent) getPlexusContainer().lookup(WebComponent.ROLE, requestURI);
            } catch (Exception e) {
                response.setContentType("text/plain");
                response.getWriter().println("No such WebComponent: " + requestURI + ".  " + e.getMessage());
                return;
            }
            try {
                webComponent.service(request, response);
            } catch (Exception e) {
                response.setContentType("text/plain");
                PrintWriter writer = response.getWriter();
                writer.println("Error invoking WebComponent: " + requestURI + "." + e.getMessage());
                e.printStackTrace(writer);
                return;
            }
        } else {
            try {
                PlexusContainer plexusContainer = getPlexusContainer();
                Map map = plexusContainer.lookupMap(WebComponent.ROLE);
                for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    Object key = entry.getKey();
                    Object value = entry.getValue();

                    response.setContentType("text/plain");
                    PrintWriter out = response.getWriter();
                    out.println(key +" \t " + value);
                }
            } catch (ComponentLookupException e) {
                e.printStackTrace();
                response.setContentType("text/plain");
                PrintWriter writer = response.getWriter();
                writer.println("Error invoking WebComponent: " + requestURI + "." + e.getMessage());
                e.printStackTrace(writer);
            }
        }
    }


    public PlexusContainer getPlexusContainer() {
        ServletContext servletContext = getServletContext();
        return (PlexusContainer) servletContext.getAttribute(PlexusConstants.PLEXUS_KEY);
    }


}
