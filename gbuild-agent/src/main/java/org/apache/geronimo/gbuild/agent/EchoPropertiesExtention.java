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

import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class EchoPropertiesExtention extends AbstractLogEnabled implements BuildAgentExtention {

    public static final String KEY_ECHO_PROPERTIES = "echo";

    public void preProcess(Map build) {
    }

    public void postProcess(Map build, Map results) {

        String list = (String) build.get(KEY_ECHO_PROPERTIES);

        if (list == null){
            return;
        }

        String[] fields = list.split(",");

        for (int i = 0; i < fields.length; i++) {

            String field = fields[i];

            Object value = build.get(field);

            String text = toText(value);

            getLogger().debug("adding "+field + " = " + text);

            Object old = results.put(field, value);

            if (old != null){

                getLogger().warn("replaced "+field + " = " + toText(old));

            }

        }
    }

    private String toText(Object value) {
        String text = value.toString();

        int LIMIT = 50;
        if (text != null && text.length() > LIMIT) {
            text  = text.substring(0, LIMIT - 3) + "...";
        }
        return text;
    }
}
