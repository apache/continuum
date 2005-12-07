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
package org.apache.geronimo.gbuild.report;

import java.io.File;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class WorkDirReport {
    private final File workDir;
    private final File outputDir;

    public WorkDirReport(File workDir, File outputDir) {
        this.workDir = workDir;
        this.outputDir = outputDir;
    }

    public static void main(String[] args) throws Exception {
        long processStart = System.currentTimeMillis();

        if (args.length != 2) {
            System.out.println("Usage:");
            System.out.println("    java org.apache.geronimo.gbuild.report.WorkDirReport sectionsFile workDir reportsDir outputDir");
            System.out.println(Arrays.asList(args));
            return;
        }

        // work directory
        File workDir = new File(args[1]);
        if (workDir.exists() && !workDir.isDirectory()) {
            System.out.println("workDir is not a directory: " + workDir.getAbsolutePath());
            return;
        }

        // output directory
        File outputDir = new File(args[3]);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                System.out.println("Could not create outputDir: " + outputDir.getAbsolutePath());
                return;
            }
        }
        if (!outputDir.isDirectory()) {
            System.out.println("outputDir is not a directory: " + outputDir.getAbsolutePath());
            return;
        }

        new WorkDirReport(workDir, outputDir).execute();
        System.out.println("Elapsed time: " + (System.currentTimeMillis() - processStart) / 1000 + " sec");
    }


    public void execute() throws Exception {
        // load all of the result from your working directory
        ReportFileLocator reportFileLocator = new ReportFileLocator(workDir);
        long begin;
        SortedSet workTestCases = new TreeSet();
        if (workDir != null) {
            begin = System.currentTimeMillis();
            WorkDirLoader workDirLoader = new WorkDirLoader(workDir, true, reportFileLocator);
            workTestCases = workDirLoader.loadTestCases();
            System.out.println("Loaded " + workTestCases.size() + " tests from " + workDir.getName() + " in " + (System.currentTimeMillis() - begin) / 1000 + " sec");
        }

        // write work summary
        SummaryReportUtil.saveTestCases(workTestCases, new File(outputDir, "work.properties"));
    }

}
