package org.apache.maven.continuum.model.system;

import java.io.Serializable;

import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.Schedule;

/**
 * This is not an entity.
 * 
 * @version $Revision$ $Date$
 */
public class ContinuumDatabase implements Serializable
{

    /**
     * Field projectGroups
     */
    private java.util.List projectGroups;

    /**
     * Field systemConfiguration
     */
    private SystemConfiguration systemConfiguration;

    /**
     * Field installations
     */
    private java.util.List installations;

    /**
     * Field schedules
     */
    private java.util.List schedules;

    /**
     * Field profiles
     */
    private java.util.List profiles;

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Method addInstallation
     * 
     * @param installation
     */
    public void addInstallation( Installation installation )
    {
        if ( !( installation instanceof Installation ) )
        {
            throw new ClassCastException(
                                          "ContinuumDatabase.addInstallations(installation) parameter must be instanceof "
                                                          + Installation.class.getName() );
        }
        getInstallations().add( installation );
    } // -- void addInstallation(Installation)

    /**
     * Method addProfile
     * 
     * @param profile
     */
    public void addProfile( Profile profile )
    {
        if ( !( profile instanceof Profile ) )
        {
            throw new ClassCastException( "ContinuumDatabase.addProfiles(profile) parameter must be instanceof "
                            + Profile.class.getName() );
        }
        getProfiles().add( profile );
    } // -- void addProfile(Profile)

    /**
     * Method addProjectGroup
     * 
     * @param projectGroup
     */
    public void addProjectGroup( ProjectGroup projectGroup )
    {
        if ( !( projectGroup instanceof ProjectGroup ) )
        {
            throw new ClassCastException(
                                          "ContinuumDatabase.addProjectGroups(projectGroup) parameter must be instanceof "
                                                          + ProjectGroup.class.getName() );
        }
        getProjectGroups().add( projectGroup );
    } // -- void addProjectGroup(ProjectGroup)

    /**
     * Method addSchedule
     * 
     * @param schedule
     */
    public void addSchedule( Schedule schedule )
    {
        if ( !( schedule instanceof Schedule ) )
        {
            throw new ClassCastException( "ContinuumDatabase.addSchedules(schedule) parameter must be instanceof "
                            + Schedule.class.getName() );
        }
        getSchedules().add( schedule );
    } // -- void addSchedule(Schedule)

    /**
     * Method getInstallations
     */
    public java.util.List getInstallations()
    {
        if ( this.installations == null )
        {
            this.installations = new java.util.ArrayList();
        }

        return this.installations;
    } // -- java.util.List getInstallations()

    /**
     * Method getProfiles
     */
    public java.util.List getProfiles()
    {
        if ( this.profiles == null )
        {
            this.profiles = new java.util.ArrayList();
        }

        return this.profiles;
    } // -- java.util.List getProfiles()

    /**
     * Method getProjectGroups
     */
    public java.util.List getProjectGroups()
    {
        if ( this.projectGroups == null )
        {
            this.projectGroups = new java.util.ArrayList();
        }

        return this.projectGroups;
    } // -- java.util.List getProjectGroups()

    /**
     * Method getSchedules
     */
    public java.util.List getSchedules()
    {
        if ( this.schedules == null )
        {
            this.schedules = new java.util.ArrayList();
        }

        return this.schedules;
    } // -- java.util.List getSchedules()

    /**
     * Get null
     */
    public SystemConfiguration getSystemConfiguration()
    {
        return this.systemConfiguration;
    } // -- SystemConfiguration getSystemConfiguration()

    /**
     * Method removeInstallation
     * 
     * @param installation
     */
    public void removeInstallation( Installation installation )
    {
        if ( !( installation instanceof Installation ) )
        {
            throw new ClassCastException(
                                          "ContinuumDatabase.removeInstallations(installation) parameter must be instanceof "
                                                          + Installation.class.getName() );
        }
        getInstallations().remove( installation );
    } // -- void removeInstallation(Installation)

    /**
     * Method removeProfile
     * 
     * @param profile
     */
    public void removeProfile( Profile profile )
    {
        if ( !( profile instanceof Profile ) )
        {
            throw new ClassCastException( "ContinuumDatabase.removeProfiles(profile) parameter must be instanceof "
                            + Profile.class.getName() );
        }
        getProfiles().remove( profile );
    } // -- void removeProfile(Profile)

    /**
     * Method removeProjectGroup
     * 
     * @param projectGroup
     */
    public void removeProjectGroup( ProjectGroup projectGroup )
    {
        if ( !( projectGroup instanceof ProjectGroup ) )
        {
            throw new ClassCastException(
                                          "ContinuumDatabase.removeProjectGroups(projectGroup) parameter must be instanceof "
                                                          + ProjectGroup.class.getName() );
        }
        getProjectGroups().remove( projectGroup );
    } // -- void removeProjectGroup(ProjectGroup)

    /**
     * Method removeSchedule
     * 
     * @param schedule
     */
    public void removeSchedule( Schedule schedule )
    {
        if ( !( schedule instanceof Schedule ) )
        {
            throw new ClassCastException( "ContinuumDatabase.removeSchedules(schedule) parameter must be instanceof "
                            + Schedule.class.getName() );
        }
        getSchedules().remove( schedule );
    } // -- void removeSchedule(Schedule)

    /**
     * Set null
     * 
     * @param installations
     */
    public void setInstallations( java.util.List installations )
    {
        this.installations = installations;
    } // -- void setInstallations(java.util.List)

    /**
     * Set null
     * 
     * @param profiles
     */
    public void setProfiles( java.util.List profiles )
    {
        this.profiles = profiles;
    } // -- void setProfiles(java.util.List)

    /**
     * Set null
     * 
     * @param projectGroups
     */
    public void setProjectGroups( java.util.List projectGroups )
    {
        this.projectGroups = projectGroups;
    } // -- void setProjectGroups(java.util.List)

    /**
     * Set null
     * 
     * @param schedules
     */
    public void setSchedules( java.util.List schedules )
    {
        this.schedules = schedules;
    } // -- void setSchedules(java.util.List)

    /**
     * Set null
     * 
     * @param systemConfiguration
     */
    public void setSystemConfiguration( SystemConfiguration systemConfiguration )
    {
        this.systemConfiguration = systemConfiguration;
    } // -- void setSystemConfiguration(SystemConfiguration)

    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }
}
