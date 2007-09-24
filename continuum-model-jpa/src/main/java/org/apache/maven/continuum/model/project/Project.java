package org.apache.maven.continuum.model.project;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.maven.continuum.model.CommonUpdatableModelEntity;
import org.apache.maven.continuum.model.scm.ScmResult;

/**
 * A Project registered in the system.
 * 
 * @version $Id$
 */
@Entity
@Table( name = "PROJECT" )
public class Project extends CommonUpdatableModelEntity
{

    /**
     * Field groupId
     */
    @Basic
    @Column( name = "GROUP_ID" )
    private String groupId;

    /**
     * Field artifactId
     */
    @Basic
    @Column( name = "ARTIFACT_ID" )
    private String artifactId;

    /**
     * Artifact version
     */
    @Basic
    @Column( name = "VERSION" )
    private String version;

    /**
     * Field executorId
     */
    @Basic
    @Column( name = "EXECUTOR_ID" )
    private String executorId;

    /**
     * Field name
     */
    @Basic
    @Column( name = "NAME" )
    private String name;

    /**
     * Field description
     */
    @Basic
    @Column( name = "DESCRIPTION" )
    private String description;

    /**
     * Field url
     */
    @Basic
    @Column( name = "URL" )
    private String url;

    /**
     * Field scmUrl
     */
    @Basic
    @Column( name = "SCM_URL" )
    private String scmUrl;

    /**
     * Field scmTag
     */
    @Basic
    @Column( name = "SCM_TAG" )
    private String scmTag;

    /**
     * Field scmUsername
     */
    @Basic
    @Column( name = "SCM_USERNAME" )
    private String scmUsername;

    /**
     * Field scmPassword
     */
    @Basic
    @Column( name = "SCM_PASSWORD" )
    private String scmPassword;

    /**
     * Field scmUseCache
     */
    @Basic
    @Column( name = "FLG_SCM_USE_CACHE", nullable = false )
    private boolean scmUseCache = false;

    /**
     * Field state.
     * <p>
     * TODO: Review! This is a candidate for an enum type.
     */
    @Basic
    @Column( name = "STATE" )
    private int state = 1;

    /**
     * Field oldState
     * <p>
     * TODO: Review! This is a candidate for an enum type.
     */
    @Basic
    @Column( name = "OLD_STATE" )
    private int oldState = 0;

    /**
     * Field latestBuildId
     */
    @Basic
    @Column( name = "LATEST_BUILD_ID" )
    private int latestBuildId = 0;

    /**
     * Field buildNumber
     */
    @Basic
    @Column( name = "BUILD_NUMBER" )
    private int buildNumber = 0;

    /**
     * Field workingDirectory
     */
    @Basic
    @Column( name = "WORKING_DIRECTORY" )
    private String workingDirectory;

    /**
     * Collection of {@link BuildResult}s for this {@link Project} instance.
     */
    @OneToMany( mappedBy = "project" )
    private List<BuildResult> buildResults;

    /**
     * Field checkoutResult
     */
    @OneToOne
    @JoinColumn( name = "ID_CHECKOUT_RESULT" )
    private ScmResult checkoutResult;

    /**
     * Field developers
     * <p>
     * TODO:
     */
    @OneToMany
    private List<ProjectDeveloper> developers;

    /**
     * Field parent
     */
    @OneToOne
    @JoinColumn( name = "ID_PARENT" )
    private ProjectDependency parent;

    /**
     * Field dependencies
     */
    @OneToMany
    private List<ProjectDependency> dependencies;

    /**
     * Field projectGroup
     */
    @ManyToOne
    @JoinColumn( name = "ID_PROJECT_GROUP" )
    private ProjectGroup projectGroup;

    /**
     * Field notifiers
     */
    @OneToMany
    private List<ProjectNotifier> notifiers;

    /**
     * @return the groupId
     */
    public String getGroupId()
    {
        return groupId;
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
     * @return the artifactId
     */
    public String getArtifactId()
    {
        return artifactId;
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
     * @return the version
     */
    public String getVersion()
    {
        return version;
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
     * @return the executorId
     */
    public String getExecutorId()
    {
        return executorId;
    }

    /**
     * @param executorId
     *            the executorId to set
     */
    public void setExecutorId( String executorId )
    {
        this.executorId = executorId;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            the name to set
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
     * @param description
     *            the description to set
     */
    public void setDescription( String description )
    {
        this.description = description;
    }

    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param url
     *            the url to set
     */
    public void setUrl( String url )
    {
        this.url = url;
    }

    /**
     * @return the scmUrl
     */
    public String getScmUrl()
    {
        return scmUrl;
    }

    /**
     * @param scmUrl
     *            the scmUrl to set
     */
    public void setScmUrl( String scmUrl )
    {
        this.scmUrl = scmUrl;
    }

    /**
     * @return the scmTag
     */
    public String getScmTag()
    {
        return scmTag;
    }

    /**
     * @param scmTag
     *            the scmTag to set
     */
    public void setScmTag( String scmTag )
    {
        this.scmTag = scmTag;
    }

    /**
     * @return the scmUsername
     */
    public String getScmUsername()
    {
        return scmUsername;
    }

    /**
     * @param scmUsername
     *            the scmUsername to set
     */
    public void setScmUsername( String scmUsername )
    {
        this.scmUsername = scmUsername;
    }

    /**
     * @return the scmPassword
     */
    public String getScmPassword()
    {
        return scmPassword;
    }

    /**
     * @param scmPassword
     *            the scmPassword to set
     */
    public void setScmPassword( String scmPassword )
    {
        this.scmPassword = scmPassword;
    }

    /**
     * @return the scmUseCache
     */
    public boolean isScmUseCache()
    {
        return scmUseCache;
    }

    /**
     * @param scmUseCache
     *            the scmUseCache to set
     */
    public void setScmUseCache( boolean scmUseCache )
    {
        this.scmUseCache = scmUseCache;
    }

    /**
     * @return the state
     */
    public int getState()
    {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState( int state )
    {
        this.state = state;
    }

    /**
     * @return the oldState
     */
    public int getOldState()
    {
        return oldState;
    }

    /**
     * @param oldState
     *            the oldState to set
     */
    public void setOldState( int oldState )
    {
        this.oldState = oldState;
    }

    /**
     * @return the latestBuildId
     */
    public int getLatestBuildId()
    {
        return latestBuildId;
    }

    /**
     * @param latestBuildId
     *            the latestBuildId to set
     */
    public void setLatestBuildId( int latestBuildId )
    {
        this.latestBuildId = latestBuildId;
    }

    /**
     * @return the buildNumber
     */
    public int getBuildNumber()
    {
        return buildNumber;
    }

    /**
     * @param buildNumber
     *            the buildNumber to set
     */
    public void setBuildNumber( int buildNumber )
    {
        this.buildNumber = buildNumber;
    }

    /**
     * @return the workingDirectory
     */
    public String getWorkingDirectory()
    {
        return workingDirectory;
    }

    /**
     * @param workingDirectory
     *            the workingDirectory to set
     */
    public void setWorkingDirectory( String workingDirectory )
    {
        this.workingDirectory = workingDirectory;
    }

    /**
     * @return the buildResults
     */
    public List<BuildResult> getBuildResults()
    {
        return buildResults;
    }

    /**
     * @param buildResults
     *            the buildResults to set
     */
    public void setBuildResults( List<BuildResult> buildResults )
    {
        this.buildResults = buildResults;
    }

    /**
     * @return the checkoutResult
     */
    public ScmResult getCheckoutResult()
    {
        return checkoutResult;
    }

    /**
     * @param checkoutResult
     *            the checkoutResult to set
     */
    public void setCheckoutResult( ScmResult checkoutResult )
    {
        this.checkoutResult = checkoutResult;
    }

    /**
     * @return the developers
     */
    public List<ProjectDeveloper> getDevelopers()
    {
        return developers;
    }

    /**
     * @param developers
     *            the developers to set
     */
    public void setDevelopers( List<ProjectDeveloper> developers )
    {
        this.developers = developers;
    }

    /**
     * @return the parent
     */
    public ProjectDependency getParent()
    {
        return parent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent( ProjectDependency parent )
    {
        this.parent = parent;
    }

    /**
     * @return the dependencies
     */
    public List<ProjectDependency> getDependencies()
    {
        return dependencies;
    }

    /**
     * @param dependencies
     *            the dependencies to set
     */
    public void setDependencies( List<ProjectDependency> dependencies )
    {
        this.dependencies = dependencies;
    }

    /**
     * @return the projectGroup
     */
    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

    /**
     * @param projectGroup
     *            the projectGroup to set
     */
    public void setProjectGroup( ProjectGroup projectGroup )
    {
        this.projectGroup = projectGroup;
    }

    /**
     * @return the notifiers
     */
    public List<ProjectNotifier> getNotifiers()
    {
        return notifiers;
    }

    /**
     * @param notifiers
     *            the notifiers to set
     */
    public void setNotifiers( List<ProjectNotifier> notifiers )
    {
        this.notifiers = notifiers;
    }

}
