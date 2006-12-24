package org.apache.maven.continuum.key;

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;

/*
 * Copyright 2006 The Apache Software Foundation.
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

/**
 * Wraps up the data necessary for distinguishing a group or an
 * individual project.
 *
 * @author: Jesse McConnell <jmcconnell@apache.org>
 * @version: $ID:$
 */
public class GroupProjectKey
{
    /**
     * String based unique key for a {@link ProjectGroup}. 
     */
    private String groupKey;

    /**
     * String based unique key for a {@link Project} within a 
     * {@link ProjectGroup}. 
     */
    private String projectKey;

    /**
     * Returns the unique {@link ProjectGroup} key.
     * @return unique {@link ProjectGroup} key.
     */
    public String getGroupKey()
    {
        return groupKey;
    }

    /**
     * Sets the unique {@link ProjectGroup} key.
     * @param groupKey key to set for the {@link ProjectGroup}.
     */
    public void setGroupKey( String groupKey )
    {
        this.groupKey = groupKey;
    }

    /**
     * Returns {@link Project} key.
     * @return Project key.
     */
    public String getProjectKey()
    {
        return projectKey;
    }

    /**
     * Sets the {@link Project} key.
     * @param projectKey key to set for the {@link Project}.
     */
    public void setProjectKey( String projectKey )
    {
        this.projectKey = projectKey;
    }

    /**
     * Determines if there was a group key set for a {@link ProjectGroup} or 
     * not.
     *  
     * @return <code>true</code> if the {@link ProjectGroup} was set, else 
     *          <code>false</code>.
     */
    public boolean hasGroupKey()
    {
        return ( null != groupKey && !groupKey.trim().equals( "" ) );
    }

    /**
     * Determines if there was a project key set for a {@link Project} or not.
     *  
     * @return <code>true</code> if the {@link Project} was set, else 
     *          <code>false</code>.
     */
    public boolean hasProjectKey()
    {
        return ( null != projectKey && !projectKey.trim().equals( "" ) );
    }
}
