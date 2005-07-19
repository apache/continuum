package org.apache.maven.continuum.configuration;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
 */
public interface ConfigurationService
{
    String ROLE = ConfigurationService.class.getName();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static final String CONFIGURATION_URL = "url";

    public static final String CONFIGURATION_BUILD_OUTPUT_DIRECTORY = "build-output-directory";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    String getUrl();

    void setUrl( String url );

    File getBuildOutputDirectory();

    void setBuildOutputDirectory( File buildOutputDirectory );

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    void load()
        throws ConfigurationLoadingException;

    void store()
        throws ConfigurationStoringException;
}
