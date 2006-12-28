package org.apache.maven.continuum.store;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.maven.continuum.key.GroupProjectKey;
import org.apache.maven.continuum.model.project.ProjectGroup;

/**
 * Concrete implementation for {@link ProjectGroupStore}.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 */
public class JdoProjectGroupStore extends AbstractJdoStore implements ProjectGroupStore
{

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectGroupStore#deleteProjectGroup(org.apache.maven.continuum.model.project.ProjectGroup)
     */
    public void deleteProjectGroup( ProjectGroup group ) throws ContinuumStoreException
    {
        // TODO: Any checks before Group should be deleted?
        removeObject( group );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectGroupStore#lookupProjectGroup(org.apache.maven.continuum.key.GroupProjectKey)
     */
    public ProjectGroup lookupProjectGroup( GroupProjectKey key )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (ProjectGroup) getObjectFromQuery( ProjectGroup.class, "key", key.getProjectKey(), null );

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectGroupStore#saveProjectGroup(org.apache.maven.continuum.model.project.ProjectGroup)
     */
    public ProjectGroup saveProjectGroup( ProjectGroup group ) throws ContinuumStoreException
    {
        updateObject( group );
        return group;
    }

}
