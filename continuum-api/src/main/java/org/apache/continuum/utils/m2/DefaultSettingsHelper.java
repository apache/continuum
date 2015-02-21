package org.apache.continuum.utils.m2;

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

import org.apache.maven.continuum.execution.SettingsConfigurationException;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service( "settingsHelper" )
public class DefaultSettingsHelper
    implements SettingsHelper
{
    @Resource
    private MavenSettingsBuilder mavenSettingsBuilder;

    public Settings getSettings()
        throws SettingsConfigurationException
    {
        try
        {
            return mavenSettingsBuilder.buildSettings( false );
        }
        catch ( IOException e )
        {
            throw new SettingsConfigurationException( "error reading settings file", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new SettingsConfigurationException( e.getMessage(), e.getCause(), e.getLineNumber(),
                                                      e.getColumnNumber() );
        }
    }
}
