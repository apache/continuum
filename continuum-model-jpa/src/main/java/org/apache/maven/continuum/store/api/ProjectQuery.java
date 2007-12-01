/**
 * 
 */
package org.apache.maven.continuum.store.api;

import org.apache.maven.continuum.model.project.Project;

/**
 * Wraps up retrieval criteria for {@link Project}s.
 * 
 * @author <a href='mailto:rinku@apache.org'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public class ProjectQuery implements Query<Project>
{

    /**
     * Project id criteria.
     */
    private Long id;

    /**
     * Project groupId criteria.
     */
    private String groupId;

    /**
     * Project artifactId criteria.
     */
    private String artifactId;

    /**
     * Project Artifact version criteria.
     */
    private String version;

    /**
     * Project name criteria.
     */
    private String name;

    /**
     * Determines if a Project id criteria was specified in the query.
     * 
     * @return
     */
    public boolean hasId()
    {
        return ( null != this.id && this.id >= 0 );
    }

    /**
     * @return the id
     */
    public Long getId()
    {
        return this.id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId( Long id )
    {
        this.id = id;
    }

    /**
     * Determines if a ProjectGroup Id criteria was specified.
     * 
     * @return
     */
    public boolean hasGroupId()
    {
        return ( null != this.groupId );
    }

    /**
     * @return the groupId
     */
    public String getGroupId()
    {
        return this.groupId;
    }

    /**
     * @param groupId
     *            the groupId to set
     */
    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    /**
     * Determines if an artifact Id criteria was specified.
     * 
     * @return
     */
    public boolean hasArtifactId()
    {
        return ( null != this.artifactId );
    }

    /**
     * @return the artifactId
     */
    public String getArtifactId()
    {
        return this.artifactId;
    }

    /**
     * @param artifactId
     *            the artifactId to set
     */
    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    /**
     * Determines if a Version criteria was specified.
     * 
     * @return
     */
    public boolean hasVersion()
    {
        return ( null != this.version );
    }

    /**
     * @return the version
     */
    public String getVersion()
    {
        return this.version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion( String version )
    {
        this.version = version;
    }

    /**
     * Determines if a project name criteria was specified.
     * 
     * @return
     */
    public boolean hasName()
    {
        return ( null != this.name );
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName( String name )
    {
        this.name = name;
    }

}
