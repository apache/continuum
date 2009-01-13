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

import java.util.List;

import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.store.ContinuumStoreException;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public interface BuildQueueDao
{
    BuildQueue addBuildQueue( BuildQueue buildQueue )
        throws ContinuumStoreException;

    BuildQueue getBuildQueue( int buildQueueId )
        throws ContinuumStoreException;

    BuildQueue getBuildQueueByName( String buildQueueName )
        throws ContinuumStoreException;

    void removeBuildQueue( BuildQueue buildQueue )
        throws ContinuumStoreException;

    BuildQueue storeBuildQueue( BuildQueue buildQueue )
        throws ContinuumStoreException;

    List<BuildQueue> getAllBuildQueues()
        throws ContinuumStoreException;
}
