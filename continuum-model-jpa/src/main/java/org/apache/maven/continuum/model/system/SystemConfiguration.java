package org.apache.maven.continuum.model.system;

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
@Table( name = "SYSTEM_CONFIGURATION" )
public class SystemConfiguration extends CommonUpdatableModelEntity
{

    /**
     * Field guestAccountEnabled
     */
    @Basic
    @Column( name = "FLG_GUEST_ACCOUNT_ENABLED", nullable = false )
    private boolean guestAccountEnabled = true;

    /**
     * Field defaultScheduleDescription
     */
    @Basic
    @Column( name = "DEFAULT_SCHEDULE_DESC", nullable = false )
    private String defaultScheduleDescription = "Run hourly";

    /**
     * Field defaultScheduleCronExpression
     */
    @Basic
    @Column( name = "DEFAULT_SCHEDULE_CRON_EXP", nullable = false )
    private String defaultScheduleCronExpression = "0 0 * * * ?";

    /**
     * Field workingDirectory
     */
    @Basic
    @Column( name = "WORKING_DIRECTORY", nullable = false )
    private String workingDirectory = "working-directory";

    /**
     * Field buildOutputDirectory
     */
    @Basic
    @Column( name = "BUILD_OUTPUT_DIRECTORY", nullable = false )
    private String buildOutputDirectory = "build-output-directory";

    /**
     * Field deploymentRepositoryDirectory
     */
    @Basic
    @Column( name = "DEPLOYMENT_REPOSITORY_DIRECTORY" )
    private String deploymentRepositoryDirectory;

    /**
     * Field baseUrl
     */
    @Basic
    @Column( name = "BASE_URL" )
    private String baseUrl;

    /**
     * Field initialized
     */
    @Basic
    @Column( name = "FLG_INITIALIZED", nullable = false )
    private boolean initialized = false;

    /**
     * @return the guestAccountEnabled
     */
    public boolean isGuestAccountEnabled()
    {
        return guestAccountEnabled;
    }

    /**
     * @param guestAccountEnabled
     *            the guestAccountEnabled to set
     */
    public void setGuestAccountEnabled( boolean guestAccountEnabled )
    {
        this.guestAccountEnabled = guestAccountEnabled;
    }

    /**
     * @return the defaultScheduleDescription
     */
    public String getDefaultScheduleDescription()
    {
        return defaultScheduleDescription;
    }

    /**
     * @param defaultScheduleDescription
     *            the defaultScheduleDescription to set
     */
    public void setDefaultScheduleDescription( String defaultScheduleDescription )
    {
        this.defaultScheduleDescription = defaultScheduleDescription;
    }

    /**
     * @return the defaultScheduleCronExpression
     */
    public String getDefaultScheduleCronExpression()
    {
        return defaultScheduleCronExpression;
    }

    /**
     * @param defaultScheduleCronExpression
     *            the defaultScheduleCronExpression to set
     */
    public void setDefaultScheduleCronExpression( String defaultScheduleCronExpression )
    {
        this.defaultScheduleCronExpression = defaultScheduleCronExpression;
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
     * @return the buildOutputDirectory
     */
    public String getBuildOutputDirectory()
    {
        return buildOutputDirectory;
    }

    /**
     * @param buildOutputDirectory
     *            the buildOutputDirectory to set
     */
    public void setBuildOutputDirectory( String buildOutputDirectory )
    {
        this.buildOutputDirectory = buildOutputDirectory;
    }

    /**
     * @return the deploymentRepositoryDirectory
     */
    public String getDeploymentRepositoryDirectory()
    {
        return deploymentRepositoryDirectory;
    }

    /**
     * @param deploymentRepositoryDirectory
     *            the deploymentRepositoryDirectory to set
     */
    public void setDeploymentRepositoryDirectory( String deploymentRepositoryDirectory )
    {
        this.deploymentRepositoryDirectory = deploymentRepositoryDirectory;
    }

    /**
     * @return the baseUrl
     */
    public String getBaseUrl()
    {
        return baseUrl;
    }

    /**
     * @param baseUrl
     *            the baseUrl to set
     */
    public void setBaseUrl( String baseUrl )
    {
        this.baseUrl = baseUrl;
    }

    /**
     * @return the initialized
     */
    public boolean isInitialized()
    {
        return initialized;
    }

    /**
     * @param initialized
     *            the initialized to set
     */
    public void setInitialized( boolean initialized )
    {
        this.initialized = initialized;
    }

}
