package org.apache.continuum.purge.repository.scanner;

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

import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.purge.controller.PurgeController;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutorException;

import java.util.List;

/**
 * Codes were taken from Archiva and made some changes.
 *
 * @author Maria Catherine Tan
 * @since 25 jul 07
 */
public interface RepositoryScanner
{
    /**
     * <p>
     * Typical Ignorable Content patterns.
     * </p>
     */
    public static final String[] IGNORABLE_CONTENT =
        {"bin/**", "reports/**", ".index", ".reports/**", ".maven/**", "**/.svn/**", "**/*snapshot-version",
            "*/website/**", "*/licences/**", "**/.htaccess", "**/*.html", "**/*.txt", "**/README*", "**/CHANGELOG*",
            "**/KEYS*" + "**/*.xml*"};

    public void scan( LocalRepository repository, PurgeController purgeController )
        throws ContinuumPurgeExecutorException;

    public void scan( LocalRepository repository, PurgeController purgeController, List<String> ignoredContentPatterns )
        throws ContinuumPurgeExecutorException;
}