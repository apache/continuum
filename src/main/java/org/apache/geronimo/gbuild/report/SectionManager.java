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
public class SectionManager {
    private final SortedSet testCases;
    private final Map sections;
    private final long errorCount;
    private final long failureCount;
    private final long totalTime;
    private final boolean newResult;

    public SectionManager(Map namedSections, SortedSet testCases) {
        this.testCases = testCases;

        // initialize the section set
        Map sectionsByPackage = new TreeMap();
        Map sectionSets = new TreeMap();
        for (Iterator iterator = namedSections.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String sectionName = (String) entry.getKey();
            String sectionPackage = (String) entry.getValue();
            sectionSets.put(sectionPackage, new TreeSet());
            sectionsByPackage.put(sectionPackage, sectionName);
        }

        // sort the test cases by section keeping a running count
        long errorCount = 0;
        long failureCount = 0;
        long totalTime = 0;
        boolean newResult = false;
        for (Iterator iterator = testCases.iterator(); iterator.hasNext();) {
            TestCase testCase = (TestCase) iterator.next();

            Set sectionTestCases = getSectionTestCases(testCase.getClassName(), sectionSets);
            sectionTestCases.add(testCase);

            if (testCase.isFailed()) {
                failureCount++;
            } else if (testCase.isError()) {
                errorCount++;
            }
            totalTime += testCase.getTime();
            newResult = newResult || testCase.isNewResult();
        }
        this.errorCount = errorCount;
        this.failureCount = failureCount;
        this.totalTime = totalTime;
        this.newResult = newResult;

        // build the section objects
        SortedMap sections = new TreeMap();
        for (Iterator iterator = sectionSets.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String sectionPackage = (String) entry.getKey();
            SortedSet sectionTestCases = (SortedSet) entry.getValue();
            String sectionName = (String) sectionsByPackage.get(sectionPackage);
            if (sectionName == null) sectionName = "other";
            Section section = new Section(sectionName, sectionTestCases);
            sections.put(section.getName(), section);
        }
        this.sections = Collections.unmodifiableSortedMap(sections);
    }


    private Set getSectionTestCases(String testCaseClass, Map sections) {
        for (Iterator sectionIterator = sections.entrySet().iterator(); sectionIterator.hasNext();) {
            Map.Entry sectionEntry = (Map.Entry) sectionIterator.next();
            String sectionPackage = (String) sectionEntry.getKey();
            sectionPackage += ".";
            Set sectionSet = (Set) sectionEntry.getValue();
            if (testCaseClass.startsWith(sectionPackage)) {
                return sectionSet;
            }
        }
        Set otherSet = (Set) sections.get("other");
        if (otherSet == null) {
            otherSet = new TreeSet();
            sections.put("other", otherSet);
        }
        return otherSet;
    }

    public String getName() {
        return "summary";
    }

    public Collection getSections() {
        return sections.values();
    }

    public Collection getItems() {
        return sections.values();
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

    public long getTestCount() {
        return testCases.size();
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
