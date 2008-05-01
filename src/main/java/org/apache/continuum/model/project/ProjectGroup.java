package org.apache.continuum.model.project;

import org.apache.continuum.model.CommonUpdatableEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@Entity
@Table(name = "PROJECT_GROUP")
@NamedQueries(
{
    @NamedQuery ( name = "ProjectGroup.findProjectGroup", query = "SELECT pg FROM ProjectGroup as pg WHERE pg.groupId = :groupId"),
    @NamedQuery ( name = "ProjectGroup.findProjects", query = "SELECT pg.projects FROM ProjectGroup as pg WHERE pg = :projectGroup")
})
public class ProjectGroup
    extends CommonUpdatableEntity
{

    /**
     * Field groupId
     */
    @Basic
    @Column(name = "GROUP_ID")
    private String groupId;

    /**
     * Field name
     */
    @Basic
    @Column(name = "NAME")
    private String name;

    /**
     * Field description
     */
    @Basic
    @Column(name = "DESCRIPTION")
    private String description;

    /**
     * Field projects
     */
    @OneToMany(mappedBy = "projectGroup")
    private List<Project> projects;

    /**
     * Field notifiers
     */
    @OneToMany
    private List<ProjectNotifier> notifiers;

    /**
     * Field buildDefinitions
     */
    @OneToMany
    private List<BuildDefinition> buildDefinitions;

    public void addProject( Project p )
    {
        projects.add( p );
    }

    public void addNotifier( ProjectNotifier n )
    {
        notifiers.add( n );
    }

    public void removeProject( Project p )
    {
        projects.remove( p );
    }

    public void removeNotifier( ProjectNotifier notifier )
    {
        notifiers.remove( notifier );
    }

    /**
     * @return the groupId
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * @param groupId the groupId to set
     */
    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription( String description )
    {
        this.description = description;
    }

    /**
     * @return the projects
     */
    public List<Project> getProjects()
    {
        if ( null == this.projects )
        {
            this.projects = new ArrayList<Project>();
        }
        return projects;
    }

    /**
     * @param projects the projects to set
     */
    public void setProjects( List<Project> projects )
    {
        this.projects = projects;
    }

    /**
     * @return the notifiers
     */
    public List<ProjectNotifier> getNotifiers()
    {
        return notifiers;
    }

    /**
     * @param notifiers the notifiers to set
     */
    public void setNotifiers( List<ProjectNotifier> notifiers )
    {
        this.notifiers = notifiers;
    }

    /**
     * @return the buildDefinitions
     */
    public List<BuildDefinition> getBuildDefinitions()
    {
        return buildDefinitions;
    }

    /**
     * @param buildDefinitions the buildDefinitions to set
     */
    public void setBuildDefinitions( List<BuildDefinition> buildDefinitions )
    {
        this.buildDefinitions = buildDefinitions;
    }

}
