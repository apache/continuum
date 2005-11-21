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

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

import java.util.Map;
import java.util.Iterator;
import java.util.Date;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @version $Rev$ $Date$
 */
public class WriteIncludeFileExtention extends AbstractContinuumAgentAction implements BuildResultsExtention, Startable {

    /**
     * @plexus.configuration
     */
    private String includePattern;

    /**
     * @plexus.configuration
     */
    private String fileExtention;

    /**
     * @plexus.configuration
     */
    private String resultsDirectory;

    /**
     * @plexus.configuration
     */
    private String useHeader;

    /**
     * @plexus.configuration
     */
    private String dateFormat;


    private File directory;
    private SimpleDateFormat dateFormatter;

    public void start() throws StartingException {
        directory = new File(resultsDirectory);
        directory.mkdirs();
        assert directory.exists(): "File specified does not exist. " + directory.getAbsolutePath();
        assert directory.isDirectory(): "File specified is not a directory. " + directory.getAbsolutePath();
        assert directory.canWrite(): "Directory specified is not writable. " + directory.getAbsolutePath();

        getLogger().info("Include files will be written to "+directory.getAbsolutePath());
        dateFormatter = new SimpleDateFormat(dateFormat);
    }

    public void stop() throws StoppingException {
    }

    public void execute(Map context) throws Exception {
        getLogger().debug("Pattern "+includePattern);
        try {
            String header = (String) context.get(useHeader);

            for (Iterator iterator = context.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String key = (String) entry.getKey();
                Object value = entry.getValue();

                if (key.matches(includePattern)){
                    getLogger().info("Found include pattern "+key);
                    String fileName = header;
                    fileName += key.replaceFirst(includePattern, "");
                    fileName += "-"+ dateFormatter.format(new Date());
                    fileName += fileExtention;

                    write(fileName, (String)value);
                } else {
                    getLogger().debug("No Match "+key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void write(String fileName, String content) {
        File outputFile = new File(directory, fileName);
        try {
            getLogger().info("Writing "+content.length()+" characters to "+outputFile.getAbsolutePath());
            FileOutputStream file = new FileOutputStream(outputFile);
            file.write(content.getBytes());
            file.flush();
            file.close();
        } catch (IOException e) {
            getLogger().error("Could not write to file "+outputFile.getAbsolutePath(), e);
        }
    }
}
