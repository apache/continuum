package org.apache.maven.continuum.model.project;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.maven.continuum.model.CommonUpdatableModelEntity;

/**
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@Entity
@Table( name = "BUILD_SCHEDULE" )
public class Schedule extends CommonUpdatableModelEntity
{

    /**
     * Field active
     */
    @Basic
    @Column( name = "FLG_ACTIVE", nullable = false )
    private boolean active = false;

    /**
     * Field name
     */
    @Basic
    @Column( name = "NAME", nullable = false )
    private String name;

    /**
     * Field description
     */
    @Basic
    @Column( name = "DESCRIPTION" )
    private String description;

    /**
     * Field delay
     */
    @Basic
    @Column( name = "SCHEDULE_DELAY" )
    private int delay = 0;

    /**
     * Field maxJobExecutionTime
     */
    @Basic
    @Column( name = "MAX_JOB_EXECUTION_TIME" )
    private int maxJobExecutionTime = 3600;

    /**
     * Field cronExpression
     */
    @Basic
    @Column( name = "CRON_EXPRESSION" )
    private String cronExpression;

    /**
     * @return the active
     */
    public boolean isActive()
    {
        return active;
    }

    /**
     * @param active
     *            the active to set
     */
    public void setActive( boolean active )
    {
        this.active = active;
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
     * @return the delay
     */
    public int getDelay()
    {
        return delay;
    }

    /**
     * @param delay
     *            the delay to set
     */
    public void setDelay( int delay )
    {
        this.delay = delay;
    }

    /**
     * @return the maxJobExecutionTime
     */
    public int getMaxJobExecutionTime()
    {
        return maxJobExecutionTime;
    }

    /**
     * @param maxJobExecutionTime
     *            the maxJobExecutionTime to set
     */
    public void setMaxJobExecutionTime( int maxJobExecutionTime )
    {
        this.maxJobExecutionTime = maxJobExecutionTime;
    }

    /**
     * @return the cronExpression
     */
    public String getCronExpression()
    {
        return cronExpression;
    }

    /**
     * @param cronExpression
     *            the cronExpression to set
     */
    public void setCronExpression( String cronExpression )
    {
        this.cronExpression = cronExpression;
    }

}
