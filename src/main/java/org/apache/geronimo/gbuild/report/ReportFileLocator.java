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

/**
 * @version $Rev$ $Date$
 */
public class ReportFileLocator {
    private final File workDir;

    public ReportFileLocator(File workDir) {
        this.workDir = workDir;
    }

    public String getReportFile(String className, String testName) {
        if (workDir != null) {
            String path = className.replace('.', '/') + '_' + testName + ".jtr";
            if (new File(workDir, path).exists()) {
                return path;
            }
        }
        return null;
    }
}
