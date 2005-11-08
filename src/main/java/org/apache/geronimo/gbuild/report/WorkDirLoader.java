/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.gbuild.report;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class WorkDirLoader {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
    private static final String LOG_START = "#section";

    private final File workDir;
    private final boolean newResult;
    private final ReportFileLocator reportFileLocator;

    public WorkDirLoader(File workDir, boolean newResult, ReportFileLocator reportFileLocator) {
        this.workDir = workDir;
        this.newResult = newResult;
        this.reportFileLocator = reportFileLocator;
    }

    public SortedSet loadTestCases() throws Exception {
        SortedSet testcases = new TreeSet();
        LinkedList resultFiles = new LinkedList();
        findResultFiles(workDir, resultFiles);
        for (Iterator iterator = resultFiles.iterator(); iterator.hasNext();) {
            File resultsFile = (File) iterator.next();
            TestCase testCase = createTestcase(resultsFile, workDir);
            testcases.add(testCase);
        }
        return testcases;
    }

    public TestCase createTestcase(File resultsFile, File workDir) throws IOException {
        Properties properties = loadResultsProperties(resultsFile);
        String name = properties.getProperty("id");

        String classname;
        String path = workDir.toURI().relativize(resultsFile.toURI()).getPath();
        if (!path.endsWith("_" + name + ".jtr")) {
            System.err.println("Path should end with " + name + ".jtr - " + path);
            classname = properties.getProperty("classname");
        } else {
            String classPart = path.substring(0, path.length() - name.length() - 5);
            classname = classPart.replace('/', '.');
        }

        String reportFile = reportFileLocator.getReportFile(classname, name);

        boolean failed;
        boolean error;
        String msg;
        String execStatus = properties.getProperty("execStatus");
        if (execStatus.startsWith("Passed.")) {
            failed = false;
            error = false;
            msg = "";
        } else if (execStatus.startsWith("Failed.")) {
            failed = true;
            error = false;
            msg = execStatus.substring(7).trim();
        } else if (execStatus.startsWith("Error.")) {
            failed = true;
            error = false;
            msg = execStatus.substring(6).trim();
        } else {
            failed = false;
            error = true;
            msg = execStatus;
        }

        long t = 0;
        try {
            long start = DATE_FORMAT.parse(properties.getProperty(("start"))).getTime();
            long end = DATE_FORMAT.parse(properties.getProperty(("end"))).getTime();
            t = end - start;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        long time = t;
        return new TestCase(name, classname, reportFile, time, failed, error, msg, newResult);
    }

    private static void findResultFiles(File dir, Collection resultsFiles) {
        File[] files = dir.listFiles();
        if (null == files) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                findResultFiles(files[i], resultsFiles);
            } else if (files[i].getName().endsWith(".jtr")) {
                resultsFiles.add(files[i]);
            }
        }
    }

    private static Properties loadResultsProperties(File resultsFile) throws IOException {
        FileReader in = null;
        Properties properties = new Properties();
        try {
            // we're going to read in line at a time
            in = new FileReader(resultsFile);
            LineNumberReader lineReader = new LineNumberReader(in);

            // and write to a string buffer
            StringWriter stringWriter = new StringWriter();
            PrintWriter out = new PrintWriter(stringWriter);

            // read until the log start
            String line;
            while ((line = lineReader.readLine()) != null) {
                if (line.startsWith(LOG_START)) {
                    break;
                }
                out.println(line);
            }

            // now load the properties
            properties.load(new ByteArrayInputStream(stringWriter.toString().getBytes()));
        } finally {
            ReportUtil.close(in);
        }
        return properties;
    }
}
