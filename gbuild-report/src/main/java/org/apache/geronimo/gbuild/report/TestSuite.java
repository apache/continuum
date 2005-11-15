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
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @version $Rev$ $Date$
 */
public class TestSuite {
    private final String name;
    private final SortedMap testCases;
    private final long errorCount;
    private final long failureCount;
    private final long totalTime;
    private final boolean newResult;

    public TestSuite(String name, Set testCases) {
        this.name = name;

        long errorCount = 0;
        long failureCount = 0;
        long totalTime = 0;
        boolean newResult = false;
        SortedMap testCasesByName = new TreeMap();
        for (Iterator iterator = testCases.iterator(); iterator.hasNext();) {
            TestCase testCase = (TestCase) iterator.next();
            if (testCase.isFailed()) {
                failureCount++;
            } else if (testCase.isError()) {
                errorCount++;
            }
            totalTime += testCase.getTime();
            newResult = newResult || testCase.isNewResult();
            testCasesByName.put(testCase.getName(), testCase);
        }
        this.errorCount = errorCount;
        this.failureCount = failureCount;
        this.totalTime = totalTime;
        this.newResult = newResult;

        this.testCases = Collections.unmodifiableSortedMap(testCasesByName);
    }

    public String getName() {
        return name;
    }

    public TestCase getTestCase(String name) {
        return (TestCase) testCases.get(name);
    }

    public Collection getTestCases() {
        return testCases.values();
    }

    public Collection getItems() {
        return testCases.values();
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
        return getTestCount() > 0 && failureCount == 0 && errorCount == 0;
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
