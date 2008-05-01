package org.apache.continuum.service.impl;

import org.apache.continuum.dao.api.GenericDao;
import org.apache.continuum.model.project.Project;
import org.apache.continuum.model.project.ProjectGroup;
import org.apache.continuum.service.api.ProjectGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
@Service
public class ProjectGroupServiceImpl
    implements ProjectGroupService
{
    GenericDao<ProjectGroup> projectGroupDao;

    GenericDao<Project> projectDao;

    public ProjectGroup saveOrUpdate( ProjectGroup projectGroup )
    {
        return projectGroupDao.saveOrUpdate( projectGroup );
    }

    public ProjectGroup getProjectGroup( long pgId )
    {
        return projectGroupDao.findById( pgId );
    }

    public ProjectGroup getProjectGroup( String groupId )
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put( "groupId", groupId );
        return projectGroupDao.findUniqByNamedQueryAndNamedParams( ProjectGroup.class, "ProjectGroup.findProjectGroup",
                                                                   params );
    }

    public ProjectGroup addProjectGroup( ProjectGroup pg )
    {
        return projectGroupDao.saveOrUpdate( pg );
    }

    @Transactional
    public void removeProjectGroup( ProjectGroup pg )
    {
        if ( pg == null )
        {
            return;
        }

        for ( Project p : pg.getProjects() )
        {
            projectDao.delete( p );
        }

        projectGroupDao.delete( pg );
    }

    public void addProject( ProjectGroup pg, Project p )
    {
        pg.addProject( p );
        projectGroupDao.saveOrUpdate( pg );
    }

    @Transactional
    public void removeProject( ProjectGroup pg, Project p )
    {
        pg.removeProject( p );
        projectDao.delete( p );
        projectGroupDao.saveOrUpdate( pg );
    }

    public List<Project> getProjects( ProjectGroup pg )
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put( "projectGroup", pg );
        return projectDao.findByNamedQueryAndNamedParams( Project.class, "ProjectGroup.findProjects", params );
    }

    public GenericDao<ProjectGroup> getProjectGroupDao()
    {
        return projectGroupDao;
    }

    public void setProjectGroupDao( GenericDao<ProjectGroup> projectGroupDao )
    {
        this.projectGroupDao = projectGroupDao;
    }

    public GenericDao<Project> getProjectDao()
    {
        return projectDao;
    }

    public void setProjectDao( GenericDao<Project> projectDao )
    {
        this.projectDao = projectDao;
    }
}
