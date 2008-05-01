package org.apache.continuum.model.scm;

import org.apache.continuum.model.CommonUpdatableEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@Entity
@Table(name = "TEST_RESULT")
public class TestResult
    extends CommonUpdatableEntity
{

    /**
     * Field testCount
     */
    @Basic
    @Column(name = "TEST_COUNT", nullable = false)
    private int testCount = 0;

    /**
     * Field failureCount
     */
    @Basic
    @Column(name = "FAILURE_COUNT", nullable = false)
    private int failureCount = 0;

    /**
     * Field totalTime
     */
    @Basic
    @Column(name = "TOTAL_TIME", nullable = false)
    private long totalTime = 0;

    /**
     * Field suiteResults
     */
    @OneToMany
    private List<SuiteResult> suiteResults;

    /**
     * @return the testCount
     */
    public int getTestCount()
    {
        return testCount;
    }

    /**
     * @param testCount the testCount to set
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
     * @param failureCount the failureCount to set
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
     * @param totalTime the totalTime to set
     */
    public void setTotalTime( long totalTime )
    {
        this.totalTime = totalTime;
    }

    /**
     * @return the suiteResults
     */
    public List<SuiteResult> getSuiteResults()
    {
        return suiteResults;
    }

    /**
     * @param suiteResults the suiteResults to set
     */
    public void setSuiteResults( List<SuiteResult> suiteResults )
    {
        this.suiteResults = suiteResults;
    }

}
