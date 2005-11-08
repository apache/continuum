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

import junit.framework.TestCase;

import java.io.File;

/**
 * @version $Rev$ $Date$
 */
public class RelativePathTest extends TestCase {
    public void testRelativeSame() {
        File source = new File("/one/two/three/four");
        File target = new File("/one/two/three/four");
        String expectedPath = ".";
        relativeTests(expectedPath, source, target);
    }

    public void testRelativeParent() {
        File source = new File("/one/two/three/four/five/six/seven");
        File target = new File("/one/two/three/four");
        String expectedPath = "../../..";
        relativeTests(expectedPath, source, target);
    }

    public void testRelativeChild() {
        File source = new File("/one/two/three/four");
        File target = new File("/one/two/three/four/five/six/seven");
        String expectedPath = "five/six/seven";
        relativeTests(expectedPath, source, target);
    }

    public void testRelativeDirs() {
        File source = new File("/one/two/three/four/five/six/seven");
        File target = new File("/one/two/three/four/cinco/seis/siete");
        String expectedPath = "../../../cinco/seis/siete";
        relativeTests(expectedPath, source, target);
    }

    public void testNotRelative() {
        File source = new File("/one/two/three/four");
        File target = new File("/uno/dos/tres/cuatro");
        String expectedPath = "/uno/dos/tres/cuatro";
        relativeTests(expectedPath, source, target);
    }

    public void testRelativeSameWithSpaces() {
        File source = new File("/o n e/t w o/t h r e e/f o u r");
        File target = new File("/o n e/t w o/t h r e e/f o u r");
        String expectedPath = ".";
        relativeTests(expectedPath, source, target);
    }

    public void testRelativeParentWithSpaces() {
        File source = new File("/o n e/t w o/t h r e e/f o u r/f i v e/six/seven");
        File target = new File("/o n e/t w o/t h r e e/f o u r");
        String expectedPath = "../../..";
        relativeTests(expectedPath, source, target);
    }

    public void testRelativeChildWithSpaces() {
        File source = new File("/o n e/t w o/t h r e e/f o u r");
        File target = new File("/o n e/t w o/t h r e e/f o u r/f i v e/s i x/s e v e n");
        String expectedPath = "f i v e/s i x/s e v e n";
        relativeTests(expectedPath, source, target);
    }

    public void testRelativeDirsWithSpaces() {
        File source = new File("/o n e/t w o/t h r e e/f o u r/f i v e/s i x/s e v e n");
        File target = new File("/o n e/t w o/t h r e e/f o u r/c i n c o/s e i s/s i e t e");
        String expectedPath = "../../../c i n c o/s e i s/s i e t e";
        relativeTests(expectedPath, source, target);
    }

    public void testNotRelativeWithSpaces() {
        File source = new File("/o n e/t w o/t h r e e/f o u r");
        File target = new File("/u n o/d o s/t r e s/c u a t r o");
        String expectedPath = "/u n o/d o s/t r e s/c u a t r o";
        relativeTests(expectedPath, source, target);
    }

    private void relativeTests(String expectedPath, File source, File target) {
        relativeTest(expectedPath, source, target);
        relativeTest(expectedPath, source, target.getAbsoluteFile());
        relativeTest(expectedPath, source, ReportUtil.normalizeFile(target));
    }

    private void relativeTest(String expectedPath, File sourceDir, File targetFile) {
        String path = ReportUtil.relativePath(sourceDir, targetFile);
        assertEquals(expectedPath, path);
        File normalizedTarget = ReportUtil.normalizeFile(targetFile);
        File normalizedPath = new File(path);
        if (!normalizedPath.isAbsolute()) normalizedPath = new File(sourceDir, path);
        normalizedPath = ReportUtil.normalizeFile(normalizedPath);
        assertEquals(normalizedTarget, normalizedPath);
    }

}
