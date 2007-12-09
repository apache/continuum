package org.apache.maven.continuum.model.project;

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.maven.continuum.model.CommonUpdatableModelEntity;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.model.scm.TestResult;

/**
 * 
 * This class is a single continuum build.
 * 
 * @version $Id$
 */
@Entity
@Table( name = "BUILD_RESULT" )
public class BuildResult extends CommonUpdatableModelEntity
{

    /**
     * Field project
     */
    @ManyToOne
    @JoinColumn( name = "ID_PROJECT" )
    private Project project;

    /**
     * Field buildDefinition
     */
    @OneToOne
    @JoinColumn( name = "ID_BUILD_DEFINITION" )
    private BuildDefinition buildDefinition = null;

    /**
     * Field buildNumber
     */
    @Basic
    @Column( name = "BUILD_NUMBER" )
    private int buildNumber = 0;

    /**
     * Field state.
     * <p>
     * TODO: This is a candidate for enum.
     */
    @Basic
    @Column( name = "RESULT_STATE" )
    private int state = 0;

    /**
     * Field trigger
     * <p>
     * TODO: enum?
     */
    @Basic
    @Column( name = "RESULT_TRIGGER" )
    private int trigger = 0;

    /**
     * Field startTime
     */
    @Temporal( TemporalType.TIME )
    @Column( name = "START_TIME", nullable = false )
    private Date startTime;

    /**
     * Field endTime
     */
    @Temporal( TemporalType.TIME )
    @Column( name = "END_TIME", nullable = false )
    private Date endTime;

    /**
     * Field error
     */
    @Basic
    @Column( name = "ERROR" )
    private String error;

    /**
     * Field success
     */
    @Basic
    @Column( name = "FLG_SUCCESS", nullable = false )
    private boolean success = false;

    /**
     * Field exitCode
     */
    @Basic
    @Column( name = "EXIT_CODE", nullable = false )
    private int exitCode = 0;

    /**
     * Field scmResult
     */
    @OneToOne( fetch = FetchType.EAGER )
    private ScmResult scmResult;

    /**
     * Field testResult
     */
    @OneToOne( fetch = FetchType.EAGER )
    private TestResult testResult;

    /**
     * Field modifiedDependencies
     */
    @OneToMany( cascade = CascadeType.ALL )
    @JoinTable( name = "PROJECT_DEPENDENCY", joinColumns = @JoinColumn( name = "BUILD_ID" ), inverseJoinColumns = @JoinColumn( name = "ID_PROJECT_DEPENDENCY" ) )
    private List<ProjectDependency> modifiedDependencies;

    /**
     * @return the project
     */
    public Project getProject()
    {
        return project;
    }

    /**
     * @param project
     *            the project to set
     */
    public void setProject( Project project )
    {
        this.project = project;
    }

    /**
     * @return the buildDefinition
     */
    public BuildDefinition getBuildDefinition()
    {
        return buildDefinition;
    }

    /**
     * @param buildDefinition
     *            the buildDefinition to set
     */
    public void setBuildDefinition( BuildDefinition buildDefinition )
    {
        this.buildDefinition = buildDefinition;
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
     * @return the trigger
     */
    public int getTrigger()
    {
        return trigger;
    }

    /**
     * @param trigger
     *            the trigger to set
     */
    public void setTrigger( int trigger )
    {
        this.trigger = trigger;
    }

    /**
     * @return the startTime
     */
    public Date getStartTime()
    {
        return startTime;
    }

    /**
     * @param startTime
     *            the startTime to set
     */
    public void setStartTime( Date startTime )
    {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public Date getEndTime()
    {
        return endTime;
    }

    /**
     * @param endTime
     *            the endTime to set
     */
    public void setEndTime( Date endTime )
    {
        this.endTime = endTime;
    }

    /**
     * @return the error
     */
    public String getError()
    {
        return error;
    }

    /**
     * @param error
     *            the error to set
     */
    public void setError( String error )
    {
        this.error = error;
    }

    /**
     * @return the success
     */
    public boolean isSuccess()
    {
        return success;
    }

    /**
     * @param success
     *            the success to set
     */
    public void setSuccess( boolean success )
    {
        this.success = success;
    }

    /**
     * @return the exitCode
     */
    public int getExitCode()
    {
        return exitCode;
    }

    /**
     * @param exitCode
     *            the exitCode to set
     */
    public void setExitCode( int exitCode )
    {
        this.exitCode = exitCode;
    }

    /**
     * @return the scmResult
     */
    public ScmResult getScmResult()
    {
        return scmResult;
    }

    /**
     * @param scmResult
     *            the scmResult to set
     */
    public void setScmResult( ScmResult scmResult )
    {
        this.scmResult = scmResult;
    }

    /**
     * @return the testResult
     */
    public TestResult getTestResult()
    {
        return testResult;
    }

    /**
     * @param testResult
     *            the testResult to set
     */
    public void setTestResult( TestResult testResult )
    {
        this.testResult = testResult;
    }

    /**
     * @return the modifiedDependencies
     */
    public List<ProjectDependency> getModifiedDependencies()
    {
        return modifiedDependencies;
    }

    /**
     * @param modifiedDependencies
     *            the modifiedDependencies to set
     */
    public void setModifiedDependencies( List<ProjectDependency> modifiedDependencies )
    {
        this.modifiedDependencies = modifiedDependencies;
    }

}
