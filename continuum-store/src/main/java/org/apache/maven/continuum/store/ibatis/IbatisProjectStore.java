package org.apache.maven.continuum.store.ibatis;

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
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.store.ProjectStore;

import java.util.List;

/**
 * Concrete implementation of {@link ProjectStore} that uses Ibatis framework to
 * access the underlying datastore.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 * @plexus.component role="org.apache.maven.continuum.store.ProjectStore"
 *                   role-hint="ibatis"
 */
public class IbatisProjectStore implements ProjectStore
{

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#deleteProject(org.apache.maven.continuum.model.project.Project)
     */
    public void deleteProject( Project project ) throws ContinuumStoreException
    {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#getAllProjects()
     */
    public List getAllProjects() throws ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#lookupProject(org.apache.maven.continuum.key.GroupProjectKey)
     */
    public Project lookupProject( GroupProjectKey key )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#saveProject(org.apache.maven.continuum.model.project.Project)
     */
    public Project saveProject( Project project ) throws ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
