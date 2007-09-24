/**
 * 
 */
package org.apache.maven.continuum.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@MappedSuperclass
public abstract class CommonUpdatableModelEntity extends CommonUpdatableEntity
{
    /**
     * 
     */
    @Basic
    @Column( name = "MODEL_ENCODING", nullable = false )
    private String modelEncoding = "UTF-8";

    /**
     * @return the modelEncoding
     */
    public String getModelEncoding()
    {
        return modelEncoding;
    }

    /**
     * @param modelEncoding
     *            the modelEncoding to set
     */
    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

}
