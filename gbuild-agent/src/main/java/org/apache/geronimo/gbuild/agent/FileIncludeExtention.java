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

import org.apache.maven.continuum.configuration.ConfigurationService;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class FileIncludeExtention extends AbstractLogEnabled implements BuildAgentExtention {

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.configuration
     */
    private String prefix;


    public void preProcess(Map build) {
    }

    public void postProcess(Map build, Map results) {
        Iterator keys = build.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (key.startsWith(prefix)){
                include(key, build, results);
            }
        }
    }

    // TODO: Maybe allow for the file contents to be compressed
    private void include(String fileNameKey, Map build, Map results) {
        getLogger().debug("Looking for " + fileNameKey);

        String fileName = (String) build.get(fileNameKey);

        if (fileName == null) {
            return;
        }

        getLogger().info("Found entry " + fileNameKey + " = " + fileName);

        File workingDirectory = configurationService.getWorkingDirectory();

        File file = new File(workingDirectory, fileName);

        if (!file.exists()) {

            getLogger().warn("File to include doesn't exist: " + file.getAbsolutePath());

            return;
        }

        try {

            String content = FileUtils.fileRead(file.getAbsolutePath());

            results.put(fileNameKey, content);

        }
        catch (IOException e) {
            getLogger().warn("Error reading file to include: " + file.getAbsolutePath(), e);
        }
    }


}
