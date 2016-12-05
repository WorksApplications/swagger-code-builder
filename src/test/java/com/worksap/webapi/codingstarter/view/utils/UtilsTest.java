/*
 *   Copyright 2016 Works Applications Co.,Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.worksap.webapi.codingstarter.view.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * TODO: Write Javadoc
 */
public class UtilsTest {

    private Utils utils = new Utils();

    @Test
    public void parseVersion_Major() throws Exception {
        SemanticVersion version = utils.parseVersion("1");
        assertEquals(version.getMajor(), "1");
        assertNull(version.getMinor());
        assertNull(version.getPatch());
        assertNull(version.getSuffix());
    }

    @Test
    public void parseVersion_Minor() throws Exception {
        SemanticVersion version = utils.parseVersion("1.2");
        assertEquals(version.getMajor(), "1");
        assertEquals(version.getMinor(), "2");
        assertNull(version.getPatch());
        assertNull(version.getSuffix());
    }

    @Test
    public void parseVersion_Patch() throws Exception {
        SemanticVersion version = utils.parseVersion("1.2.3");
        assertEquals(version.getMajor(), "1");
        assertEquals(version.getMinor(), "2");
        assertEquals(version.getPatch(), "3");
        assertNull(version.getSuffix());
    }

    @Test
    public void parseVersion_Suffix() throws Exception {
        SemanticVersion version = utils.parseVersion("1.2.3-SNAPSHOT");
        assertEquals(version.getMajor(), "1");
        assertEquals(version.getMinor(), "2");
        assertEquals(version.getPatch(), "3");
        assertEquals(version.getSuffix(), "SNAPSHOT");
    }

    @Test
    public void parseVersion_Empty() throws Exception {
        SemanticVersion version = utils.parseVersion("");
        assertNull(version);
    }

    @Test
    public void parseVersion_Null() throws Exception {
        SemanticVersion version = utils.parseVersion(null);
        assertNull(version);
    }
}