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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class SummaryReportLoader {
    private final Set reportFiles;
    private final boolean newResult;
    private final ReportFileLocator reportFileLocator;

    public SummaryReportLoader(Set reportFiles, boolean newResult, ReportFileLocator reportFileLocator) {
        this.reportFiles = reportFiles;
        this.newResult = newResult;
        this.reportFileLocator = reportFileLocator;
    }

    public SummaryReportLoader(File reportsDir, boolean newResult, ReportFileLocator reportFileLocator) {
        reportFiles = new HashSet();
        File[] files = reportsDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.canRead() && file.getName().endsWith(".properties")) {
                reportFiles.add(file);
            }
        }
        this.newResult = newResult;
        this.reportFileLocator = reportFileLocator;
    }

    public SortedSet loadTestCases() throws Exception {
        SortedSet testcases = new TreeSet();
        for (Iterator iterator = reportFiles.iterator(); iterator.hasNext();) {
            File reportFile = (File) iterator.next();
            Set cases = SummaryReportUtil.loadTestCases(reportFile, newResult, reportFileLocator);
            testcases.addAll(cases);
        }
        return testcases;
    }
}
