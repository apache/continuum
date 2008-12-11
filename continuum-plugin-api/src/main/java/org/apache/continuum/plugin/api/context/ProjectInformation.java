package org.apache.continuum.plugin.api.context;

import org.apache.continuum.model.ProjectGroup;
import org.apache.continuum.model.BuildDefinitionTemplate;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ProjectInformation
{
    // defined constants m2,m1,ant,shell etc...
    private ProjectType type;

    private ProjectGroup projectGroup;

    private BuildDefinitionTemplate buildDefTemplate;

    // for non maven projects
    private String name;

    private String version;

    public ProjectType getType()
    {
        return type;
    }

    public void setType( ProjectType type )
    {
        this.type = type;
    }

    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

    public void setProjectGroup( ProjectGroup projectGroup )
    {
        this.projectGroup = projectGroup;
    }

    public BuildDefinitionTemplate getBuildDefTemplate()
    {
        return buildDefTemplate;
    }

    public void setBuildDefTemplate( BuildDefinitionTemplate buildDefTemplate )
    {
        this.buildDefTemplate = buildDefTemplate;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }
}
