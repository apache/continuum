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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

/**
 * @version $Rev$ $Date$
 */
public class ReportUtil {
    private ReportUtil() {
    }

    public static Properties loadProperties(File file) throws IOException {
        Properties properties = new Properties();
        if (!file.canRead()) {
            return properties;
        }

        InputStream in = null;
        try {
            in = new FileInputStream(file);
            properties.load(in);
        } finally {
            close(in);
        }
        return properties;
    }

    /**
     * Write the specified map to the output file in java.util.Properties format.
     *
     * @param map        the data to write
     * @param outputFile the file to which the data is written
     * @throws IOException if a problem occurs wile writing the data
     */
    public static void saveProperties(final Map map, File outputFile) throws IOException {
        createDirectory(outputFile.getParentFile());

        OutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);
            Properties properties = new Properties() {
                public Object get(Object key) {
                    return map.get(key);
                }

                public synchronized Enumeration keys() {
                    return Collections.enumeration(map.keySet());
                }
            };
            properties.store(out, null);
            out.flush();
        } finally {
            close(out);
        }
    }

    public static void close(Reader thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void close(InputStream thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void close(OutputStream thing) {
        if (thing != null && thing != System.out) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static String formatTime(long milis) {
        long hours = milis / (60 * 60 * 1000);
        milis = milis % (60 * 60 * 1000);

        long min = milis / (60 * 1000);
        milis = milis % (60 * 1000);

        long sec = milis / (1000);
        milis = milis % (1000);

        return (hours < 10 ? "0" : "") + hours + ":" +
                (min < 10 ? "0" : "") + min + ":" +
                (sec < 10 ? "0" : "") + sec;
    }

    public static SortedSet mergeTestCases(SortedSet one, SortedSet two) {
        Map testCases = new HashMap();
        for (Iterator iterator = one.iterator(); iterator.hasNext();) {
            TestCase testCase = (TestCase) iterator.next();
            testCases.put(testCase.getUniqueId(), testCase);
        }
        for (Iterator iterator = two.iterator(); iterator.hasNext();) {
            TestCase testCase = (TestCase) iterator.next();
            TestCase exising = (TestCase) testCases.get(testCase.getUniqueId());
            if (exising == null) {
                testCases.put(testCase, testCase);
            } else {
                boolean different = exising.isPassed() != testCase.isPassed() ||
                        exising.isFailed() != testCase.isFailed() ||
                        exising.isError() != testCase.isError();
                if (different && !exising.isNewResult() && testCase.isNewResult()) {
                    // results are different so make sure the test case we keep is the new result
                    testCases.put(testCase.getUniqueId(), testCase);
                } else if (!different && exising.isNewResult() && !testCase.isNewResult()) {
                    // results are not different so make sure the test case we keep is the old result
                    // this prevents unnecessary updaing
                    testCases.put(testCase.getUniqueId(), testCase);
                }
            }
        }
        return new TreeSet(testCases.values());
    }

    public static String relativePath(File sourceDir, File targetFile) {
        File normalizedTarget = normalizeFile(targetFile);
        File normalizedSource = normalizeFile(sourceDir);
        if (normalizedSource.equals(normalizedTarget)) {
            return ".";
        }

        List targetFileList = new ArrayList();
        for (File f = normalizedTarget; f != null; f = f.getParentFile()) {
            targetFileList.add(f);
        }
        Collections.reverse(targetFileList);

        StringBuffer path = new StringBuffer();
        for (File f = normalizedSource; f != null && f.getParentFile() != null; f = f.getParentFile()) {
            if (!targetFileList.contains(f)) {
                if (path.length() > 0) path.append("/");
                path.append("..");
            } else {
                int i = targetFileList.indexOf(f) + 1;
                for (; i < targetFileList.size(); i++) {
                    File file = (File) targetFileList.get(i);
                    if (path.length() > 0) path.append("/");
                    path.append(file.getName());
                }
                return path.toString();
            }
        }

        return targetFile.getAbsolutePath();
    }

    public static File normalizeFile(File targetFile) {
        //return new File(targetFile.getAbsoluteFile().toURI().normalize()).getAbsoluteFile();
        return targetFile;
    }

    public static void createDirectory(File dir) throws IOException {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Could not create directory: \"" + dir.getAbsolutePath() + "\"");
            }
        }
        if (!dir.isDirectory()) {
            throw new IOException("Directory is not a directory: \"" + dir.getAbsolutePath() + "\"");
        }
    }

    public static void unpackEmbeddedZip(String jarName, File outputDir) throws IOException {
        InputStream in = null;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) classLoader = ReportUtil.class.getClassLoader();
            in = classLoader.getResourceAsStream(jarName);
            ZipInputStream zipInputStream = new ZipInputStream(in);
            unpackZip(zipInputStream, outputDir);
        } finally {
            close(in);
        }
    }

    public static void unpackZip(ZipInputStream zipInputStream, File outputDir) throws IOException{
        for (ZipEntry zipEntry = zipInputStream.getNextEntry(); zipEntry != null; zipEntry = zipInputStream.getNextEntry()) {
            File file = new File(outputDir, zipEntry.getName());
            if (zipEntry.isDirectory()) {
                createDirectory(file);
            } else {
                createDirectory(file.getParentFile());
                OutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    writeAll(zipInputStream, out);
                } finally {
                    close(out);
                }
            }
        }
    }

    private static void writeAll(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int count;
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        out.flush();
    }
}
