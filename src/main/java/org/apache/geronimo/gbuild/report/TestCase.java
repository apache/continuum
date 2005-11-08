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




/**
 * @version $Rev$ $Date$
 */
public class TestCase implements Comparable {
    private final String name;
    private final String className;
    private final String reportFile;
    private final long time;
    private final boolean failed;
    private final boolean error;
    private final String msg;
    private final boolean newResult;

    public TestCase(String name, String className, String reportFile, long time, boolean failed, boolean error, String msg, boolean newResult) {
        this.name = name;
        this.className = className;
        this.reportFile = reportFile;
        this.time = time;
        this.failed = failed;
        this.error = error;
        this.msg = msg;
        this.newResult = newResult;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public String getUniqueId() {
        return className + '#' + name;
    }

    public String getReportFile() {
        return reportFile;
    }

    public long getTime() {
        return time;
    }

    public String getTimeString() {
        return ReportUtil.formatTime(getTime());
    }

    public boolean isPassed() {
        return !failed && !error;
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean isError() {
        return error;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isNewResult() {
        return newResult;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TestCase) {
            TestCase testCase = (TestCase) obj;
            return className.equals(testCase.className) && name.equals(testCase.name);
        }
        return false;
    }

    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + className.hashCode();
        hash = 31 * hash + name.hashCode();
        return hash;
    }

    public int compareTo(Object obj) {
        TestCase testCase = (TestCase) obj;
        int i = className.compareTo(testCase.className);
        if (i != 0) {
            return i;
        }
        return name.compareTo(testCase.name);
    }
}
