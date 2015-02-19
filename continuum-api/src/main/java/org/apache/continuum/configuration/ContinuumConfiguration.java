package org.apache.continuum.configuration;

import java.io.File;

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
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 17 juin 2008
 */
public interface ContinuumConfiguration
{
    /**
     * @return an empty or a filled on but never null !
     * @throws ContinuumConfigurationException
     *
     */
    GeneralConfiguration getGeneralConfiguration()
        throws ContinuumConfigurationException;

    void setGeneralConfiguration( GeneralConfiguration generalConfiguration )
        throws ContinuumConfigurationException;

    void save()
        throws ContinuumConfigurationException;

    void save( File file )
        throws ContinuumConfigurationException;

    void reload()
        throws ContinuumConfigurationException;

    void reload( File file )
        throws ContinuumConfigurationException;
}
