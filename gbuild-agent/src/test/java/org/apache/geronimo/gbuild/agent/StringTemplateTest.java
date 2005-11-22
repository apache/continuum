package org.apache.geronimo.gbuild.agent;
/**
 * @version $Rev$ $Date$
 */

import junit.framework.*;
import org.apache.geronimo.gbuild.agent.StringTemplate;

import java.util.HashMap;

public class StringTemplateTest extends TestCase {
    StringTemplate stringTemplate;

    public void testApply() throws Exception {
        StringTemplate dataMask = new StringTemplate("{dir}/{name}.{ext}");

        HashMap map = new HashMap();
        map.put("dir", "foo");
        map.put("name", "bar");
        map.put("ext", "txt");

        assertEquals("foo/bar.txt",dataMask.apply(map));
    }
}