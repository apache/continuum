package org.apache.continuum.model.project;

import org.apache.continuum.model.CommonUpdatableEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@Entity
@Table(name = "PROJECT_DEPENDENCY")
public class ProjectDependency
    extends CommonUpdatableEntity
{

    /**
     * Field groupId
     */
    @Basic
    @Column(name = "GROUP_ID")
    private String groupId;

    /**
     * Field artifactId
     */
    @Basic
    @Column(name = "ARTIFACT_ID")
    private String artifactId;

    /**
     * Field version
     */
    @Basic
    @Column(name = "VERSION")
    private String version;

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
     * @return the artifactId
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * @param artifactId the artifactId to set
     */
    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    /**
     * @return the version
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion( String version )
    {
        this.version = version;
    }

}
