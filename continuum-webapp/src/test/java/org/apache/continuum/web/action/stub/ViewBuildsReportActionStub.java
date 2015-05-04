package org.apache.continuum.web.action.stub;

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

import org.apache.continuum.web.action.ViewBuildsReportAction;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.codehaus.plexus.redback.system.SecuritySession;

import java.util.Collection;
import java.util.Collections;

public class ViewBuildsReportActionStub
    extends ViewBuildsReportAction
{

    Collection<ProjectGroup> authorizedGroups = Collections.EMPTY_LIST;

    public void setSecuritySession( SecuritySession securitySession )
    {
        super.setSecuritySession( securitySession );
    }

    @Override
    protected Collection<ProjectGroup> getAuthorizedGroups()
    {
        return authorizedGroups;
    }

    public void setAuthorizedGroups( Collection<ProjectGroup> authorizedGroups )
    {
        this.authorizedGroups = authorizedGroups;
    }

    protected void checkViewProjectGroupAuthorization( String resource )
    {
        // skip authorization check
    }

    protected void checkViewReportsAuthorization()
    {
        // skip authorization check
    }
}
