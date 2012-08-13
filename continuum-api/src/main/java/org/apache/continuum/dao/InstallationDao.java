package org.apache.continuum.dao;

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

import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.store.ContinuumStoreException;

import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public interface InstallationDao
{
    Installation addInstallation( Installation installation )
        throws ContinuumStoreException;

    List<Installation> getAllInstallations()
        throws ContinuumStoreException;

    void removeInstallation( Installation installation )
        throws ContinuumStoreException;

    void updateInstallation( Installation installation )
        throws ContinuumStoreException;

    Installation getInstallation( int installationId )
        throws ContinuumStoreException;

    Installation getInstallation( String installationName )
        throws ContinuumStoreException;
}
