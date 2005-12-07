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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/**
 * @version $Rev$ $Date$
 */
public class SummaryReportUtil {
    private SummaryReportUtil() {
    }

    public static Set loadTestCases(File reportFile, boolean newResult, ReportFileLocator reportFileLocator) throws IOException {
        Set testcases = new HashSet();
        Properties properties = ReportUtil.loadProperties(reportFile);
        for (Iterator caseIterator = properties.entrySet().iterator(); caseIterator.hasNext();) {
            Map.Entry entry = (Map.Entry) caseIterator.next();
            String storeName = (String) entry.getKey();
            String storeValue = (String) entry.getValue();

            TestCase testCase = createTestcase(storeName, storeValue, newResult, reportFileLocator);
            if (testCase != null) {
                testcases.add(testCase);
            }
        }
        return testcases;
    }

    public static void saveTestCases(Set testCases, File outputFile) throws IOException {
        Map summary = new TreeMap();
        for (Iterator iterator = testCases.iterator(); iterator.hasNext();) {
            TestCase testCase = (TestCase) iterator.next();
            summary.put(getStoreName(testCase), getStoreValue(testCase));
        }
        ReportUtil.saveProperties(summary, outputFile);
    }

    private static TestCase createTestcase(String storeName, String storeValue, boolean newResult, ReportFileLocator reportFileLocator) {
        try {
            int index = storeName.lastIndexOf('#');
            String name = storeName.substring(index + 1);
            String classname = storeName.substring(0, index);
            String reportFile = reportFileLocator.getReportFile(classname, name);

            char flag = storeValue.charAt(0);
            index = storeValue.indexOf(')');
            long time = Long.parseLong(storeValue.substring(3, index));
            boolean failed;
            boolean error;
            String msg;
            if (flag == 'P') {
                failed = false;
                error = false;
                msg = "";
            } else {
                if (flag == 'F') {
                    failed = true;
                    error = false;
                } else {
                    failed = false;
                    error = true;
                }
                if (storeValue.length() >= index + 2) {
                    msg = storeValue.substring(index + 2);
                } else {
                    msg = "";
                }
            }

            return new TestCase(name, classname, reportFile, time, failed, error, msg, newResult);
        } catch (NumberFormatException e) {
            System.out.println("FAILED TO PARSE - "+storeName +" = "+storeValue);
            return null;
        }
    }

    private static String getStoreName(TestCase testCase) {
        return testCase.getClassName() + "#" + testCase.getName();
    }

    private static String getStoreValue(TestCase testCase) {
        if (testCase.isPassed()) {
            return "P (" + testCase.getTime() + ")";
        } else if (testCase.isFailed()) {
            return "F (" + testCase.getTime() + ") " + testCase.getMsg();
        } else {
            return "F (" + testCase.getTime() + ") " + testCase.getMsg();
        }
    }
}
