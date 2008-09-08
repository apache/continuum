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

import org.apache.continuum.model.release.ContinuumReleaseResult;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 */
public interface ContinuumReleaseResultDao
{
    ContinuumReleaseResult addContinuumReleaseResult( ContinuumReleaseResult releaseResult )
        throws ContinuumStoreException;

    void removeContinuumReleaseResult( ContinuumReleaseResult releaseResult )
        throws ContinuumStoreException;

    List<ContinuumReleaseResult> getContinuumReleaseResultsByProjectGroup( int projectGroupId );

    List<ContinuumReleaseResult> getAllContinuumReleaseResults();

    ContinuumReleaseResult getContinuumReleaseResult( int releaseResultId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;
}
