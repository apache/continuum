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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class Section {
    private final String name;
    private final SortedSet testCases;
    private final SortedMap testSuites;
    private final long errorCount;
    private final long failureCount;
    private final long totalTime;
    private final boolean newResult;

    public Section(String name, SortedSet testCases) {
        if (name == null) throw new NullPointerException("name is null");
        if (testCases == null) throw new NullPointerException("testCases is null");
        this.name = name;
        this.testCases = testCases;

        // sort the test cases by class name keeping a running count
        long errorCount = 0;
        long failureCount = 0;
        long totalTime = 0;
        boolean newResult = false;
        Map testCasesByClass = new TreeMap();
        for (Iterator iterator = testCases.iterator(); iterator.hasNext();) {
            TestCase testCase = (TestCase) iterator.next();
            String className = testCase.getClassName();
            TreeSet suiteTestCases = (TreeSet) testCasesByClass.get(className);
            if (suiteTestCases == null) {
                suiteTestCases = new TreeSet();
                testCasesByClass.put(className, suiteTestCases);
            }

            if (testCase.isFailed()) {
                failureCount++;
            } else if (testCase.isError()) {
                errorCount++;
            }
            totalTime += testCase.getTime();
            newResult = newResult || testCase.isNewResult();
            suiteTestCases.add(testCase);
        }
        this.errorCount = errorCount;
        this.failureCount = failureCount;
        this.totalTime = totalTime;
        this.newResult = newResult;

        SortedMap testSuites = new TreeMap();
        for (Iterator iterator = testCasesByClass.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String className = (String) entry.getKey();
            Set suiteTestCases = (Set) entry.getValue();
            TestSuite testSuite = new TestSuite(className, suiteTestCases);
            testSuites.put(testSuite.getName(), testSuite);
        }
        this.testSuites = Collections.unmodifiableSortedMap(testSuites);
    }

    public String getName() {
        return name;
    }

    public SortedSet getTestCases() {
        return testCases;
    }

    public Collection getTestSuites() {
        return testSuites.values();
    }

    public Collection getItems() {
        return testSuites.values();
    }

    public long getTestCount() {
        return testCases.size();
    }

    public long getPassCount() {
        return testCases.size() - errorCount - failureCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public String getTotalTimeString() {
        return ReportUtil.formatTime(getTotalTime());
    }

    public boolean isPassed() {
        return testCases.size() > 0 && failureCount == 0 && errorCount == 0;
    }

    public int getPassPercentage() {
        if (testCases.isEmpty()) {
            return 0;
        }
        return (int) ((0.0 + getPassCount()) * 100 / testCases.size());
    }

    public int getPassBarSize() {
        return getPassPercentage() * 2;
    }

    public int getFailBarSize() {
        return 200 - getPassBarSize();
    }

    public boolean isNewResult() {
        return newResult;
    }
}
