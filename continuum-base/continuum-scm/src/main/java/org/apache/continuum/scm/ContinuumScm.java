package org.apache.continuum.scm;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.io.IOException;

/**
 * Component that manages SCM interactions and checkouts within Continuum.
 *
 * @version $Id$
 */
public interface ContinuumScm
{
    /**
     * Check out a working copy for a project.
     *
     * @param configuration the configuration for the working copy and SCM
     * @return the result of the check out
     * @throws IOException                if there is a problem writing to the working copy location
     * @throws NoSuchScmProviderException if there is a problem with the configuration
     * @throws ScmRepositoryException     if there is a problem with the configuration
     * @throws ScmException               if there is a problem checking out
     */
    CheckOutScmResult checkout( ContinuumScmConfiguration configuration )
        throws IOException, ScmRepositoryException, NoSuchScmProviderException, ScmException;

    /**
     * Update a working copy for a project.
     *
     * @param config the configuration for the working copy and SCM
     * @return the result of the update
     * @throws NoSuchScmProviderException if there is a problem with the configuration
     * @throws ScmRepositoryException     if there is a problem with the configuration
     * @throws ScmException               if there is a problem updating
     */
    UpdateScmResult update( ContinuumScmConfiguration config )
        throws ScmRepositoryException, NoSuchScmProviderException, ScmException;

    /**
     * Get change log for a project
     *
     * @param config the configuration for the working copy and SCM
     * @return the result of the change log
     * @throws ScmRepositoryException     if there is a problem with the configuration
     * @throws NoSuchScmProviderException if there is a problem with the configuration
     * @throws ScmException               if there is a problem getting the change log
     */
    ChangeLogScmResult changeLog( ContinuumScmConfiguration config )
        throws ScmRepositoryException, NoSuchScmProviderException, ScmException;
}
