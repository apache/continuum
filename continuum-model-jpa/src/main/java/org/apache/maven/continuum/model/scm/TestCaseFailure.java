package org.apache.maven.continuum.model.scm;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.maven.continuum.model.CommonUpdatableModelEntity;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@Entity
@Table( name = "TEST_CASE_FAILURE" )
public class TestCaseFailure extends CommonUpdatableModelEntity
{

    /**
     * Field name
     */
    @Basic
    @Column( name = "NAME" )
    private String name;

    /**
     * Field exception
     */
    @Basic
    @Column( name = "EXCEPTION" )
    private String exception;

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
     * @return the exception
     */
    public String getException()
    {
        return exception;
    }

    /**
     * @param exception
     *            the exception to set
     */
    public void setException( String exception )
    {
        this.exception = exception;
    }

}
