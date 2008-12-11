package org.apache.continuum.plugin.api.context;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ProjectType
{
    public static final ProjectType ANT = new ProjectType( "ant" );

    public static final ProjectType MAVEN_1 = new ProjectType( "maven1" );

    public static final ProjectType MAVEN_2 = new ProjectType( "maven2" );

    public static final ProjectType SHELL = new ProjectType( "shell" );

    private String name;

    private ProjectType( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
