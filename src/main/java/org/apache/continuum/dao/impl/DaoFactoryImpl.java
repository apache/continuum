package org.apache.continuum.dao.impl;

import org.apache.continuum.dao.api.GenericDao;
import org.apache.continuum.model.project.Project;
import org.apache.continuum.model.project.ProjectGroup;
import org.apache.continuum.model.project.ProjectNotifier;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DaoFactoryImpl
{
    public GenericDao<ProjectGroup> createProjectGroupDao()
    {
        return new GenericDaoJpa<ProjectGroup>( ProjectGroup.class );
    }

    public GenericDao<Project> createProjectDao()
    {
        return new GenericDaoJpa<Project>( Project.class );
    }

    public GenericDao<ProjectNotifier> createNotifierDao()
    {
        return new GenericDaoJpa<ProjectNotifier>( ProjectNotifier.class );
    }
}
