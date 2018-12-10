package org.apache.maven.continuum.model.project;

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
@Table( name = "PROJECT_DEVELOPER" )
public class ProjectDeveloper extends CommonUpdatableModelEntity
{

    /**
     * Field scmId
     */
    @Basic
    @Column( name = "SCM_USERNAME" )
    private String scmId;

    /**
     * Field name
     */
    @Basic
    @Column( name = "NAME" )
    private String name;

    /**
     * Field email
     */
    @Basic
    @Column( name = "EMAIL" )
    private String email;

    /**
     * Field continuumId
     */
    @Basic
    @Column( name = "CONTINUUM_ID", nullable = false )
    private int continuumId = 0;

}
