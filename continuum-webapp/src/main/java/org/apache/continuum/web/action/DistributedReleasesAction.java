package org.apache.continuum.web.action;

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

import org.apache.continuum.release.distributed.DistributedReleaseUtil;
import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.model.DistributedReleaseSummary;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="distributedRelease"
 */
public class DistributedReleasesAction
    extends ContinuumActionSupport
    implements SecureAction
{
    private List<DistributedReleaseSummary> releasesSummary;

    public String list()
        throws Exception
    {
        DistributedReleaseManager releaseManager = getContinuum().getDistributedReleaseManager();

        List<Map<String, Object>> releases = releaseManager.getAllReleasesInProgress();

        releasesSummary = new ArrayList<DistributedReleaseSummary>();

        for ( Map<String, Object> release : releases )
        {
            DistributedReleaseSummary summary = new DistributedReleaseSummary();
            summary.setReleaseId( DistributedReleaseUtil.getReleaseId( release ) );
            summary.setReleaseGoal( DistributedReleaseUtil.getReleaseGoal( release ) );
            summary.setBuildAgentUrl( DistributedReleaseUtil.getBuildAgentUrl( release ) );
            summary.setProjectId( DistributedReleaseUtil.getProjectId( release ) );

            releasesSummary.add( summary );
        }

        return SUCCESS;
    }

    public List<DistributedReleaseSummary> getReleasesSummary()
    {
        return releasesSummary;
    }

    public void setReleasesSummary( List<DistributedReleaseSummary> releasesSummary )
    {
        this.releasesSummary = releasesSummary;
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_VIEW_RELEASE, Resource.GLOBAL );

        return bundle;
    }
}
