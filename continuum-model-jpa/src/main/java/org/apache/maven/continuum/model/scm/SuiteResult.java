package org.apache.maven.continuum.model.scm;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.maven.continuum.model.CommonUpdatableModelEntity;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@Entity
@Table( name = "SUITE_RESULT" )
public class SuiteResult extends CommonUpdatableModelEntity
{

    /**
     * Field name
     */
    @Basic
    @Column( name = "NAME" )
    private String name;

    /**
     * Field testCount
     */
    @Basic
    @Column( name = "TEST_COUNT" )
    private int testCount = 0;

    /**
     * Field failureCount
     */
    @Basic
    @Column( name = "FAILURE_COUNT" )
    private int failureCount = 0;

    /**
     * Field totalTime
     */
    @Basic
    @Column( name = "TOTAL_TIME" )
    private long totalTime = 0;

    /**
     * Field failures
     */
    @OneToMany
    private List<TestCaseFailure> failures;

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
     * @return the testCount
     */
    public int getTestCount()
    {
        return testCount;
    }

    /**
     * @param testCount
     *            the testCount to set
     */
    public void setTestCount( int testCount )
    {
        this.testCount = testCount;
    }

    /**
     * @return the failureCount
     */
    public int getFailureCount()
    {
        return failureCount;
    }

    /**
     * @param failureCount
     *            the failureCount to set
     */
    public void setFailureCount( int failureCount )
    {
        this.failureCount = failureCount;
    }

    /**
     * @return the totalTime
     */
    public long getTotalTime()
    {
        return totalTime;
    }

    /**
     * @param totalTime
     *            the totalTime to set
     */
    public void setTotalTime( long totalTime )
    {
        this.totalTime = totalTime;
    }

    /**
     * @return the failures
     */
    public List<TestCaseFailure> getFailures()
    {
        return failures;
    }

    /**
     * @param failures
     *            the failures to set
     */
    public void setFailures( List<TestCaseFailure> failures )
    {
        this.failures = failures;
    }

}
