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
@Table( name = "SCM_RESULT" )
public class ScmResult extends CommonUpdatableModelEntity
{

    /**
     * Field success
     */
    @Basic
    @Column( name = "FLG_SUCCESS" )
    private boolean success = false;

    /**
     * Field commandLine
     */
    @Basic
    @Column( name = "COMMAND_LINE" )
    private String commandLine;

    /**
     * Field providerMessage
     */
    @Basic
    @Column( name = "PROVIDER_MESSAGE" )
    private String providerMessage;

    /**
     * Field commandOutput
     */
    @Basic
    @Column( name = "COMMAND_OUTPUT" )
    private String commandOutput;

    /**
     * Field exception XXX: Renamed from 'EXCEPTION' to 'EXCEPTION_MSG'
     */
    @Basic
    @Column( name = "EXCEPTION_MSG" )
    private String exception;

    /**
     * Field changes
     */
    @OneToMany
    private List<ChangeSet> changes;

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
     * @return the commandLine
     */
    public String getCommandLine()
    {
        return commandLine;
    }

    /**
     * @param commandLine
     *            the commandLine to set
     */
    public void setCommandLine( String commandLine )
    {
        this.commandLine = commandLine;
    }

    /**
     * @return the providerMessage
     */
    public String getProviderMessage()
    {
        return providerMessage;
    }

    /**
     * @param providerMessage
     *            the providerMessage to set
     */
    public void setProviderMessage( String providerMessage )
    {
        this.providerMessage = providerMessage;
    }

    /**
     * @return the commandOutput
     */
    public String getCommandOutput()
    {
        return commandOutput;
    }

    /**
     * @param commandOutput
     *            the commandOutput to set
     */
    public void setCommandOutput( String commandOutput )
    {
        this.commandOutput = commandOutput;
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

    /**
     * @return the changes
     */
    public List<ChangeSet> getChanges()
    {
        return changes;
    }

    /**
     * @param changes
     *            the changes to set
     */
    public void setChanges( List<ChangeSet> changes )
    {
        this.changes = changes;
    }

}
