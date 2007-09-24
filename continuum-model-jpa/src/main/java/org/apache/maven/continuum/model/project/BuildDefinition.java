package org.apache.maven.continuum.model.project;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.maven.continuum.model.CommonUpdatableModelEntity;
import org.apache.maven.continuum.model.system.Profile;

/**
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@Entity
@Table( name = "BUILD_DEFINITION" )
public class BuildDefinition extends CommonUpdatableModelEntity
{

    /**
     * Field defaultForProject
     */
    @Basic
    @Column( name = "FLG_DEFAULT_PROJECT", nullable = false )
    private boolean defaultForProject = false;

    /**
     * Field goals
     */
    @Basic
    @Column( name = "GOALS" )
    private String goals;

    /**
     * Field arguments
     */
    @Basic
    @Column( name = "ARGUMENTS" )
    private String arguments;

    /**
     * Field buildFile
     */
    @Basic
    @Column( name = "BUILD_FILE" )
    private String buildFile;

    /**
     * Field buildFresh
     */
    @Basic
    @Column( name = "FLG_BUILD_FRESH", nullable = false )
    private boolean buildFresh = false;

    /**
     * Field description
     */
    @Basic
    @Column( name = "DESCRIPTION" )
    private String description;

    /**
     * Field type
     */
    @Basic
    @Column( name = "TYPE" )
    private String type;

    /**
     * Field schedule
     */
    @OneToOne
    private Schedule schedule;

    /**
     * Field profile
     */
    @OneToOne
    private Profile profile;

    /**
     * Field alwaysBuild
     */
    @Basic
    @Column( name = "FLG_ALWAYS_BUILD" )
    private boolean alwaysBuild = false;

    /**
     * Field template
     */
    @Basic
    @Column( name = "FLG_TEMPLATE" )
    private boolean template = false;

}
