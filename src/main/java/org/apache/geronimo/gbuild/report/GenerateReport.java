package org.apache.geronimo.gbuild.report;

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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * @version $Revision$ $Date$
 */
public class GenerateReport {
    private final File sectionsFile;
    private final File workDir;
    private final File reportsDir;
    private final File outputDir;
    private final boolean forceHtml = false;

    public GenerateReport(File sectionsFile, File workDir, File reportsDir, File outputDir) {
        this.sectionsFile = sectionsFile;
        this.workDir = workDir;
        this.reportsDir = reportsDir;
        this.outputDir = outputDir;
    }

    public static void main(String[] args) throws Exception {
        long processStart = System.currentTimeMillis();

        if (args.length != 4) {
            System.out.println("Usage:");
            System.out.println("    java org.apache.geronimo.gbuild.report.ProcessResults sectionsFile workDir reportsDir outputDir");
            System.out.println(Arrays.asList(args));
            return;
        }

        // sections file
        File sectionsFile = new File(args[0]);
        if (!sectionsFile.canRead()) {
            System.out.println("sectionsFile is not a readable: " + sectionsFile.getAbsolutePath());
            return;
        }

        // work directory
        File workDir = new File(args[1]);
        if (workDir.exists() && !workDir.isDirectory()) {
            System.out.println("workDir is not a directory: " + workDir.getAbsolutePath());
            return;
        }

        // reports dir
        File reportsDir = new File(args[2]);
        if (!reportsDir.isDirectory()) {
            System.out.println("reportsDir is not a directory: " + reportsDir.getAbsolutePath());
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

        new GenerateReport(sectionsFile, workDir, reportsDir, outputDir).execute();
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


        // load the existing test cases from the reports
        begin = System.currentTimeMillis();
        SummaryReportLoader summaryReportLoader = new SummaryReportLoader(reportsDir, false, reportFileLocator);
        SortedSet reportsTestCases = summaryReportLoader.loadTestCases();
        System.out.println("Loaded " + reportsTestCases.size() + " tests from " + reportsDir.getName() + " in " + (System.currentTimeMillis() - begin) / 1000 + " sec");

        // merge the test cases
        SortedSet testCases = ReportUtil.mergeTestCases(workTestCases, reportsTestCases);

        // write work summary
        SummaryReportUtil.saveTestCases(workTestCases, new File(outputDir, "work.properties"));

        // create the section manager
        SortedMap sectionNamesByPackage = new TreeMap(ReportUtil.loadProperties(sectionsFile));
        SectionManager sectionManager = new SectionManager(sectionNamesByPackage, testCases);

        // unpack resources
        ReportUtil.unpackEmbeddedZip("META-INF/org/apache/geronimo/gbuild/report/resources.zip", new File(outputDir, "resources"));

        // generate html site
        begin = System.currentTimeMillis();
        int renderCount = renderHtml(sectionManager, outputDir);
        System.out.println("Rendered " + renderCount + " html reports in " + (System.currentTimeMillis() - begin) / 1000 + " sec");

        // generate summary reports
        begin = System.currentTimeMillis();
        renderCount = renderSummary(sectionManager, new File(outputDir, "reports"));
        System.out.println("Rendered " + renderCount + " summary reports in " + (System.currentTimeMillis() - begin) / 1000 + " sec");
    }

    private int renderHtml(SectionManager sectionManager, File outputDir) throws Exception {
        VelocityEngine velocity = new VelocityEngine();
        velocity.setProperty(Velocity.RESOURCE_LOADER, "class");
        velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocity.setProperty(Velocity.VM_LIBRARY, "org/apache/geronimo/gbuild/report/macros.vm");
        velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.NullLogSystem");

        velocity.init();
        Template summaryTemplate = velocity.getTemplate("org/apache/geronimo/gbuild/report/summary.vm");
        Template testSuiteTemplate = velocity.getTemplate("org/apache/geronimo/gbuild/report/testsuite.vm");

        // render the summary page
        int renderCount = 0;
        File summaryFile = new File(outputDir, "index.html");
        if (!summaryFile.exists() || sectionManager.isNewResult() || forceHtml) {
            renderVelocityTemplate("summary",
                    sectionManager,
                    summaryFile,
                    summaryTemplate);
            renderCount++;
        }

        File summaryDir = new File(outputDir, "summary");
        summaryDir.mkdirs();

        // render a page for each section
        for (Iterator iterator = sectionManager.getSections().iterator(); iterator.hasNext();) {
            Section section = (Section) iterator.next();
            File sectionFile = new File(summaryDir, section.getName() + ".html");
            if (!sectionFile.exists() || section.isNewResult() || forceHtml) {
                renderVelocityTemplate("summary",
                        section,
                        sectionFile,
                        summaryTemplate);
                renderCount++;
            }

            File sectionDir = new File(summaryDir, section.getName());
            sectionDir.mkdirs();

            // render a page for each testsuite
            for (Iterator iterator1 = section.getTestSuites().iterator(); iterator1.hasNext();) {
                TestSuite testSuite = (TestSuite) iterator1.next();

                File testSuiteFile = new File(sectionDir, testSuite.getName() + ".html");
                if (!testSuiteFile.exists() || testSuite.isNewResult() || forceHtml) {
                    renderVelocityTemplate("testSuite",
                            testSuite,
                            testSuiteFile,
                            testSuiteTemplate);
                    renderCount++;
                }
            }
        }
        return renderCount;
    }

    private static int renderSummary(SectionManager sectionManager, File outputDir) throws Exception {
        int renderCount = 0;
        if (!outputDir.isDirectory()) {
            if (!outputDir.mkdirs()) {
                throw new IOException("Could not create outputDir: " + outputDir.getAbsolutePath());
            }
        }

        // render a summary for each section
        for (Iterator iterator = sectionManager.getSections().iterator(); iterator.hasNext();) {
            Section section = (Section) iterator.next();

            // if the section is modified write a summary report
            if (section.isNewResult()) {
                SummaryReportUtil.saveTestCases(section.getTestCases(), new File(outputDir, section.getName() + ".properties"));
                System.out.println("Updated summary report for section " + section.getName());
                renderCount++;
            }
        }
        return renderCount;
    }

    private void renderVelocityTemplate(String name, Object value, File outputFile, Template template) throws Exception {
        ReportUtil.createDirectory(outputFile.getParentFile());

        VelocityContext context = new VelocityContext();
        context.put(name, value);

        String reportDir = ReportUtil.relativePath(outputFile.getParentFile(), workDir);
        context.put("reportDir", reportDir);

        String rootDir = ReportUtil.relativePath(outputFile.getParentFile(), outputDir);
        context.put("rootDir", rootDir);

        PrintStream out = null;
        FileReader templateReader = null;
        try {
            out = new PrintStream(new FileOutputStream(outputFile));
            PrintWriter writer = new PrintWriter(out);
            template.merge(context, writer);
            writer.flush();
        } finally {
            ReportUtil.close(out);
            ReportUtil.close(templateReader);
        }
    }
}
