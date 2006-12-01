package org.apache.maven.continuum;

import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * A test implementation of the settings builder that doesn't return ~/.m2/settings.xml to avoid interfering with the
 * normal execution environment.
 */
public class TestSettingsBuilder
    implements MavenSettingsBuilder
{
    public Settings buildSettings()
        throws IOException, XmlPullParserException
    {
        return new Settings();
    }

    public Settings buildSettings( File file )
        throws IOException, XmlPullParserException
    {
        return buildSettings();
    }
}
