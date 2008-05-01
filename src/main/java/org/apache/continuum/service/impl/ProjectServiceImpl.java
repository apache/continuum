package org.apache.continuum.service.impl;

import org.apache.continuum.dao.api.GenericDao;
import org.apache.continuum.model.project.Project;
import org.apache.continuum.model.project.ProjectNotifier;
import org.apache.continuum.service.api.ProjectService;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ProjectServiceImpl
    implements ProjectService
{
    GenericDao<Project> projectDao;

    GenericDao<ProjectNotifier> notifierDao;

    public Project saveOrUpdate( Project project )
    {
        return projectDao.saveOrUpdate( project );
    }

    public Project getProject( long projecId )
    {
        return projectDao.findById( projecId );
    }

    public Project getProject( String groupId, String artifactId, String version )
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put( "groupId", groupId );
        params.put( "artifactId", artifactId );
        params.put( "version", version );
        return projectDao.findUniqByNamedQueryAndNamedParams( Project.class, "Project.find", params );
    }

    public List<ProjectNotifier> getNotifiers( Project p )
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put( "projectId", p.getId() );
        return notifierDao.findByNamedQueryAndNamedParams( ProjectNotifier.class, "Notifier.findAllFromProject",
                                                           params );
    }

    @Transactional
    public void addNotifier( Project p, ProjectNotifier notifier )
    {
        p.addNotifier( notifier );
        projectDao.saveOrUpdate( p );
    }

    @Transactional
    public void removeNotifier( Project p, ProjectNotifier notifier )
    {
        p.removeNotifier( notifier );
        notifierDao.delete( notifier );
        projectDao.saveOrUpdate( p );
    }

    public GenericDao<Project> getProjectDao()
    {
        return projectDao;
    }

    public void setProjectDao( GenericDao<Project> projectDao )
    {
        this.projectDao = projectDao;
    }

    public GenericDao<ProjectNotifier> getNotifierDao()
    {
        return notifierDao;
    }

    public void setNotifierDao( GenericDao<ProjectNotifier> notifierDao )
    {
        this.notifierDao = notifierDao;
    }
}
