package org.apache.maven.continuum.model.scm;

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.maven.continuum.model.CommonUpdatableModelEntity;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@Entity
@Table( name = "CHANGE_SET" )
public class ChangeSet extends CommonUpdatableModelEntity
{

    /**
     * Field author
     */
    @Basic
    @Column( name = "AUTHOR" )
    private String author;

    /**
     * Field comment
     */
    @Basic
    @Column( name = "COMMENT_TEXT" )
    private String comment;

    /**
     * Field date
     */
    @Temporal( TemporalType.TIME )
    @Column( name = "DATE", nullable = false )
    private Date date;

    /**
     * Field files
     */
    @OneToMany
    private List<ChangeFile> files;

    /**
     * @return the author
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * @param author
     *            the author to set
     */
    public void setAuthor( String author )
    {
        this.author = author;
    }

    /**
     * @return the comment
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * @param comment
     *            the comment to set
     */
    public void setComment( String comment )
    {
        this.comment = comment;
    }

    /**
     * @return the date
     */
    public Date getDate()
    {
        return date;
    }

    /**
     * @param date
     *            the date to set
     */
    public void setDate( Date date )
    {
        this.date = date;
    }

    /**
     * @return the files
     */
    public List<ChangeFile> getFiles()
    {
        return files;
    }

    /**
     * @param files
     *            the files to set
     */
    public void setFiles( List<ChangeFile> files )
    {
        this.files = files;
    }

}
