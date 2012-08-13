package org.apache.maven.continuum.web.action;

import org.apache.continuum.web.action.AbstractActionTest;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.web.action.stub.ProjectGroupActionStub;
import org.apache.maven.continuum.web.bean.ProjectGroupUserBean;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.Role;
import org.codehaus.plexus.redback.rbac.UserAssignment;
import org.codehaus.plexus.redback.rbac.jdo.JdoRole;
import org.codehaus.plexus.redback.rbac.jdo.JdoUserAssignment;
import org.jmock.Mock;

import java.util.ArrayList;
import java.util.List;

public class ProjectGroupActionTest
    extends AbstractActionTest
{
    private ProjectGroupActionStub action;

    private Mock continuum;

    private Mock rbac;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        action = new ProjectGroupActionStub();
        continuum = mock( Continuum.class );
        rbac = mock( RBACManager.class );

        action.setContinuum( (Continuum) continuum.proxy() );
        action.setRbacManager( (RBACManager) rbac.proxy() );
    }

    public void testViewMembersWithProjectAdminRole()
        throws Exception
    {
        ProjectGroup group = new ProjectGroup();
        group.setName( "Project A" );

        List<Role> roles = new ArrayList<Role>();
        Role role1 = new JdoRole();
        role1.setName( "Project User - Project A" );
        roles.add( role1 );

        Role role2 = new JdoRole();
        role2.setName( "Continuum Manage Scheduling" );
        roles.add( role2 );

        Role role3 = new JdoRole();
        role3.setName( "Project Developer - Project A" );
        roles.add( role3 );

        Role role4 = new JdoRole();
        role4.setName( "Project Administrator - Project A" );
        roles.add( role4 );

        List<UserAssignment> userAssignments = new ArrayList<UserAssignment>();
        UserAssignment ua1 = new JdoUserAssignment();
        ua1.setPrincipal( "user1" );
        userAssignments.add( ua1 );

        List<Role> eRoles = roles;

        continuum.expects( once() ).method( "getProjectGroupWithProjects" ).will( returnValue( group ) );
        rbac.expects( once() ).method( "getAllRoles" ).will( returnValue( roles ) );
        rbac.expects( once() ).method( "getUserAssignmentsForRoles" ).will( returnValue( userAssignments ) );
        rbac.expects( once() ).method( "getEffectivelyAssignedRoles" ).will( returnValue( eRoles ) );

        action.members();

        continuum.verify();
        rbac.verify();

        List<ProjectGroupUserBean> users = action.getProjectGroupUsers();
        assertEquals( 1, users.size() );
        assertTrue( users.get( 0 ).isAdministrator() );
        assertTrue( users.get( 0 ).isDeveloper() );
        assertTrue( users.get( 0 ).isUser() );
    }

    public void testViewMembersWithProjectUserRole()
        throws Exception
    {
        ProjectGroup group = new ProjectGroup();
        group.setName( "Project A" );

        List<Role> roles = new ArrayList<Role>();
        Role role1 = new JdoRole();
        role1.setName( "Project User - Project A" );
        roles.add( role1 );

        Role role2 = new JdoRole();
        role2.setName( "Continuum Manage Scheduling" );
        roles.add( role2 );

        Role role3 = new JdoRole();
        role3.setName( "Project Developer - test-group" );
        roles.add( role3 );

        Role role4 = new JdoRole();
        role4.setName( "Project Administrator - test-group" );
        roles.add( role4 );

        Role role5 = new JdoRole();
        role5.setName( "Project Administrator - Project C" );
        roles.add( role5 );

        List<UserAssignment> userAssignments = new ArrayList<UserAssignment>();
        UserAssignment ua1 = new JdoUserAssignment();
        ua1.setPrincipal( "user1" );
        userAssignments.add( ua1 );

        List<Role> eRoles = new ArrayList<Role>();
        eRoles.add( role1 );
        eRoles.add( role2 );
        eRoles.add( role5 );

        continuum.expects( once() ).method( "getProjectGroupWithProjects" ).will( returnValue( group ) );
        rbac.expects( once() ).method( "getAllRoles" ).will( returnValue( roles ) );
        rbac.expects( once() ).method( "getUserAssignmentsForRoles" ).will( returnValue( userAssignments ) );
        rbac.expects( once() ).method( "getEffectivelyAssignedRoles" ).will( returnValue( eRoles ) );

        action.members();

        continuum.verify();
        rbac.verify();

        List<ProjectGroupUserBean> users = action.getProjectGroupUsers();
        assertEquals( 1, users.size() );
        assertFalse( users.get( 0 ).isAdministrator() );
        assertFalse( users.get( 0 ).isDeveloper() );
        assertTrue( users.get( 0 ).isUser() );
    }
}
