package org.apache.continuum.model.project;

import org.apache.continuum.model.CommonUpdatableEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Map;

/**
 * Configures one method for notifying users/developers when a build breaks.
 * <p/>
 * TODO: Review this to use discriminators for different Notifier extensions.
 *
 * @version $Id$
 */
@Entity
@Table(name = "PROJECT_NOTIFIER")
@NamedQueries
({
    @NamedQuery( name = "Notifier.findAllFromProject", query = "SELECT p.notifiers from Project as p WHERE p.id = :projectId"),
    @NamedQuery( name = "Notifier.findAllFromProjectGroup", query = "SELECT pg.notifiers from ProjectGroup as pg WHERE pg.id = :projectGroupId")
})
public class ProjectNotifier
    extends CommonUpdatableEntity
{
    /**
     * Field type
     * <p/>
     * TODO: This is a candidate for enum type.
     */
    @Column(name = "GROUP_ID")
    private String type = "mail";

    /**
     * Field from
     */
    @Column(name = "NOTIFIER_ORIGIN", nullable = false)
    private int from = 0;

    /**
     * Field enabled
     */
    @Column(name = "FLG_ENABLED", nullable = false)
    private boolean enabled = true;

    /**
     * Field recipientType
     */
    @Column(name = "RECIPIENT_TYPE", nullable = false)
    private int recipientType = 0;

    /**
     * Field sendOnSuccess
     */
    @Column(name = "FLG_SEND_ON_SUCCESS", nullable = false)
    private boolean sendOnSuccess = true;

    /**
     * Field sendOnFailure
     */
    @Column(name = "FLG_SEND_ON_FAILURE", nullable = false)
    private boolean sendOnFailure = true;

    /**
     * Field sendOnError
     */
    @Column(name = "FLG_SEND_ON_ERROR", nullable = false)
    private boolean sendOnError = true;

    /**
     * Field sendOnWarning
     */
    @Column(name = "FLG_SEND_ON_WARNING", nullable = false)
    private boolean sendOnWarning = true;

    /**
     * Field configuration.
     * <p/>
     * TODO: Map!
     */
    @Transient
    private Map configuration;

    /**
     * TODO: Map! Enum?
     */
    public static final int FROM_PROJECT = 1;

    /**
     * TODO: Map! Enum?
     */
    public static final int FROM_USER = 2;

    /**
     * Method toString
     */
    public java.lang.String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "id = '" );
        buf.append( getId() + "'" );
        return buf.toString();
    } // -- java.lang.String toString()

    public boolean isFromProject()
    {
        return from == FROM_PROJECT;
    }

    public boolean isFromUser()
    {
        return from == FROM_USER;
    }

}
