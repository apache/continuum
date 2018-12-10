package org.apache.maven.continuum.model.project;

/**
 * null
 * 
 * @version $Revision$ $Date$
 */
public class ContinuumProjectState implements java.io.Serializable
{

    /**
     * Field name
     */
    private String name;

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Get null
     */
    public String getName()
    {
        return this.name;
    } // -- String getName()

    /**
     * Set null
     * 
     * @param name
     */
    public void setName( String name )
    {
        this.name = name;
    } // -- void setName(String)

    public final static int NEW = 1;

    public final static int OK = 2;

    public final static int FAILED = 3;

    public final static int ERROR = 4;

    public final static int BUILDING = 6;

    public final static int CHECKING_OUT = 7;

    public final static int UPDATING = 8;

    public final static int WARNING = 9;

    public final static int CHECKEDOUT = 10;

    // TODO: maybe move these to another class
    public static final int TRIGGER_FORCED = 1;

    // TODO: remove
    public static final int TRIGGER_SCHEDULED = 0;

    public static final int TRIGGER_UNKNOWN = TRIGGER_SCHEDULED;

    public String getI18nKey()
    {
        return "org.apache.maven.continuum.project.state." + name;
    }

    public boolean equals( Object object )
    {
        if ( !( object instanceof ContinuumProjectState ) )
        {
            return false;
        }

        ContinuumProjectState other = (ContinuumProjectState) object;

        return name.equals( other.name );
    }

    public int hashCode()
    {
        return name.hashCode();
    }

    public String toString()
    {
        return name;
    }

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
