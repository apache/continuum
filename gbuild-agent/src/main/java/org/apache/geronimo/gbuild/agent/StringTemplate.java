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
package org.apache.geronimo.gbuild.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version $Rev$ $Date$
 */
public class StringTemplate {

    private static final Pattern expr = Pattern.compile("\\{([^}]*)\\}");

    private final String[] tokens;
    private final String mask;
    private final Pattern[] patterns;

    public StringTemplate(String mask) {
        this.mask = mask;
        this.tokens = getTokens(mask);
        this.patterns = getPatterns(tokens);
    }

    /**
     * Pull tokens one at a time and replace
     * them on the string (loop)
     * <p/>
     * Notice that when referenced data is pulled from the
     * context, we don't apply a related StringTemplate.  This could
     * cause a circular reference and is complicated to code.
     * We are just skipping this feature for the moment.
     */
    public String apply(Map context) {
        String data = mask;
        try {
            synchronized (context) {
                for (int i = 0; i < tokens.length; i++) {
                    Matcher matcher = patterns[i].matcher(data);
                    data = matcher.replaceAll((String) context.get(tokens[i]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private String[] getTokens(String str) {
        List tokens = new ArrayList();
        Matcher matcher = expr.matcher(str);
        while (matcher.find()) {
            tokens.add(matcher.group(1));
        }
        return (String[]) tokens.toArray(new String[]{});
    }

    private Pattern[] getPatterns(String[] tokens) {
        Pattern[] patterns = new Pattern[tokens.length];
        for (int i = 0; i < patterns.length; i++) {
            patterns[i] = Pattern.compile("\\{" + tokens[i] + "\\}");
        }
        return patterns;
    }
}
