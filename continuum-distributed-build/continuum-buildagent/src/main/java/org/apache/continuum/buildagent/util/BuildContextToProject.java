package org.apache.continuum.buildagent.util;

import org.apache.continuum.buildagent.model.BuildContext;
import org.apache.maven.continuum.model.project.Project;

//stateless buildcontext to project
public class BuildContextToProject
{
    
    private BuildContext buildContext;
    

    public static Project  getProject(BuildContext buildContext)
    {
        Project project = new Project();       

        project.setScmUrl( buildContext.getScmUrl() );

        project.setScmUsername( buildContext.getScmPassword());

        project.setScmPassword( buildContext.getScmPassword() );

        project.setExecutorId( buildContext.getExecutorId() );
        
        
        //rename ?
        project.setName( "distributed-build-[projectId="+buildContext.getProjectId()+"]" );
        
        return project;
    }
    
    public BuildContext getBuildContext()
    {
        return buildContext;
    }

    public void setBuildContext( BuildContext buildContext )
    {
        this.buildContext = buildContext;
    }
    
    
    
    
}
