package org.apache.maven.continuum.web.action;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.continuum.buildmanager.BuildManagerException;
import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.web.bean.ProjectGroupUserBean;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.struts2.interceptor.TokenInterceptor;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.RbacObjectNotFoundException;
import org.codehaus.plexus.redback.rbac.Role;
import org.codehaus.plexus.redback.rbac.UserAssignment;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.role.RoleManagerException;
import org.codehaus.plexus.redback.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProjectGroupAction:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="projectGroup"
 */
public class ProjectGroupAction
    extends ContinuumConfirmAction
{
    private static final Logger logger = LoggerFactory.getLogger( ProjectGroupAction.class );

    private static final Map<String, String> FILTER_CRITERIA = new HashMap<String, String>();

    static
    {
        FILTER_CRITERIA.put( "username", "Username contains" );
        FILTER_CRITERIA.put( "fullName", "Name contains" );
        FILTER_CRITERIA.put( "email", "Email contains" );
    }

    /**
     * @plexus.requirement role-hint="cached"
     */
    private RBACManager rbac;

    /**
     * @plexus.requirement role-hint="default"
     */
    private RoleManager roleManager;

    /**
     * @plexus.requirement role-hint="parallel"
     */
    private BuildsManager parallelBuildsManager;

    private int projectGroupId;

    private ProjectGroup projectGroup;

    private String name;

    private String description;

    private Map projects = new HashMap();

    private Map<Integer, String> projectGroups = new HashMap<Integer, String>();

    private boolean confirmed;

    private boolean projectInCOQueue = false;

    private Collection<Project> projectList;

    private List<ProjectGroupUserBean> projectGroupUsers;

    private String filterProperty;

    private String filterKey;

    //Default order is by username
    private String sorterProperty = "username";

    private boolean ascending = true;

    private Collection groupProjects;

    private int releaseProjectId;

    private Map<String, Integer> buildDefinitions;

    private int buildDefinitionId;

    private boolean fromSummaryPage = false;

    private String preferredExecutor = "maven2";

    private String url;

    private int repositoryId;

    private List<LocalRepository> repositories;

    private boolean disabledRepositories = true;

    private List<ProjectScmRoot> projectScmRoots;

    private Random randomizer;

    private String encodedRandomVal;

    private static List<String> encodedRandomValCache =  new LinkedList();

    private boolean explicitCSRFCheck = false;

    private static final int CACHE_MAX_SIZE = 30;

    public ProjectGroupAction()
    {
        randomizer = new SecureRandom();
    }

    public String summary()
        throws ContinuumException
    {
        try
        {
            checkViewProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( ContinuumException e )
        {
            addActionError( getText( "projectGroup.invalid.id", "Invalid Project Group Id: " + projectGroupId,
                                     Integer.toString( projectGroupId ) ) );
            return "to_summary_page";
        }

        projectGroup = getContinuum().getProjectGroupWithProjects( projectGroupId );

        List<BuildDefinition> projectGroupBuildDefs =
            getContinuum().getBuildDefinitionsForProjectGroup( projectGroupId );

        if ( projectGroupBuildDefs != null )
        {
            this.buildDefinitions = new LinkedHashMap<String, Integer>( projectGroupBuildDefs.size() );
            for ( BuildDefinition buildDefinition : projectGroupBuildDefs )
            {

                if ( !buildDefinition.isDefaultForProject() )
                {
                    String key = StringUtils.isEmpty( buildDefinition.getDescription() ) ? buildDefinition.getGoals()
                        : buildDefinition.getDescription();
                    buildDefinitions.put( key, buildDefinition.getId() );
                }
            }
        }
        else
        {
            this.buildDefinitions = Collections.EMPTY_MAP;
        }

        if ( projectGroup != null )
        {
            if ( projectGroup.getProjects() != null && projectGroup.getProjects().size() > 0 )
            {
                int nbMaven2Projects = 0;
                int nbMaven1Projects = 0;
                int nbAntProjects = 0;
                int nbShellProjects = 0;

                Project rootProject = ( getContinuum().getProjectsInBuildOrder(
                    getContinuum().getProjectsInGroupWithDependencies( projectGroupId ) ) ).get( 0 );
                if ( "maven2".equals( rootProject.getExecutorId() ) || "maven-1".equals( rootProject.getExecutorId() ) )
                {
                    url = rootProject.getUrl();
                }

                for ( Object o : projectGroup.getProjects() )
                {
                    Project p = (Project) o;
                    if ( "maven2".equals( p.getExecutorId() ) )
                    {
                        nbMaven2Projects += 1;
                    }
                    else if ( "maven-1".equals( p.getExecutorId() ) )
                    {
                        nbMaven1Projects += 1;
                    }
                    else if ( "ant".equals( p.getExecutorId() ) )
                    {
                        nbAntProjects += 1;
                    }
                    else if ( "shell".equals( p.getExecutorId() ) )
                    {
                        nbShellProjects += 1;
                    }
                }

                int nbActualPreferredProject = nbMaven2Projects;
                if ( nbMaven1Projects > nbActualPreferredProject )
                {
                    preferredExecutor = "maven-1";
                    nbActualPreferredProject = nbMaven1Projects;
                }
                if ( nbAntProjects > nbActualPreferredProject )
                {
                    preferredExecutor = "ant";
                    nbActualPreferredProject = nbAntProjects;
                }
                if ( nbShellProjects > nbActualPreferredProject )
                {
                    preferredExecutor = "shell";
                }
            }

            projectScmRoots = getContinuum().getProjectScmRootByProjectGroup( projectGroup.getId() );
        }

        // explicit csrf check for CONTINUUM-2622
        encodedRandomVal = generateEncodedRandomVal();

        synchronized( encodedRandomValCache )
        {
            // check size of cache first before adding anything in the cache
            if( encodedRandomValCache.size() == CACHE_MAX_SIZE )
            {
                ( ( LinkedList ) encodedRandomValCache ).removeFirst();
            }
            encodedRandomValCache.add( decodeRandomVal( encodedRandomVal ) );
        }

        return SUCCESS;
    }

    public String members()
        throws ContinuumException
    {
        try
        {
            checkViewProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        projectGroup = getContinuum().getProjectGroupWithProjects( projectGroupId );

        groupProjects = projectGroup.getProjects();

        populateProjectGroupUsers( projectGroup );

        return SUCCESS;
    }

    public Collection getGroupProjects()
        throws ContinuumException
    {
        return groupProjects;
    }

    public String buildDefinitions()
        throws ContinuumException
    {
        return summary();
    }

    public String notifiers()
        throws ContinuumException
    {
        return summary();
    }

    public String remove()
        throws ContinuumException
    {
        try
        {
            checkRemoveProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        if ( confirmed )
        {
            try
            {
                getContinuum().removeProjectGroup( projectGroupId );
            }
            catch ( ContinuumException e )
            {
                logger.error( "Error while removing project group with id " + projectGroupId, e );
                addActionError( getText( "projectGroup.delete.error", "Unable to remove project group",
                                         Integer.toString( projectGroupId ) ) );
            }
        }
        else
        {
            // explicit CSRF check for CONTINUUM-2622 - need to explicitly implement for remove project group because <s:token/> doesn't work
            //   in project group summary as there is a <s:action> whose result is being executed in the page causing a double submission
            if( explicitCSRFCheck  )
            {
                if( StringUtils.isEmpty( encodedRandomVal ) || !encodedRandomValCache.contains( decodeRandomVal( encodedRandomVal ) ) )
                {
                    logger.error( "Token not found in cache!" );
                    addActionError( getText( "projectGroup.remove.invalid.token", "Action not allowed to continue - invalid token found!" ) );
                    return TokenInterceptor.INVALID_TOKEN_CODE;
                }
                else
                {
                    logger.info( "Token found in cache.." );
                    // remove it from the cache if found and let the action continue
                    encodedRandomValCache.remove( decodeRandomVal( encodedRandomVal ) );  
                }
            }

            name = getProjectGroupName();
            return CONFIRM;
        }

        AuditLog event = new AuditLog( "Project Group id=" + projectGroupId, AuditLogConstants.REMOVE_PROJECT_GROUP );
        event.setCategory( AuditLogConstants.PROJECT );
        event.setCurrentUser( getPrincipal() );
        event.log();

        return SUCCESS;
    }

    private void initialize()
        throws ContinuumException
    {
        try
        {
            checkManageLocalRepositoriesAuthorization();
            disabledRepositories = false;
        }
        catch ( AuthorizationRequiredException authzE )
        {
            // do nothing
        }

        projectGroup = getContinuum().getProjectGroupWithProjects( projectGroupId );

        projectList = projectGroup.getProjects();

        if ( projectList != null )
        {
            for ( Project p : projectList )
            {
                try
                {
                    if ( parallelBuildsManager.isInAnyCheckoutQueue( p.getId() ) )
                    {
                        projectInCOQueue = true;
                    }
                }
                catch ( BuildManagerException e )
                {
                    throw new ContinuumException( e.getMessage(), e );
                }
                projects.put( p, p.getProjectGroup().getId() );
            }
        }

        for ( ProjectGroup pg : getContinuum().getAllProjectGroups() )
        {
            if ( isAuthorized( projectGroup.getName() ) )
            {
                projectGroups.put( pg.getId(), pg.getName() );
            }
        }
        repositories = getContinuum().getRepositoryService().getAllLocalRepositories();
    }

    public String edit()
        throws ContinuumException
    {
        try
        {
            checkModifyProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        initialize();

        name = projectGroup.getName();

        description = projectGroup.getDescription();

        projectList = projectGroup.getProjects();

        if ( projectGroup.getLocalRepository() != null )
        {
            repositoryId = projectGroup.getLocalRepository().getId();
        }
        else
        {
            repositoryId = -1;
        }

        Collection<Project> projList = getContinuum().getProjectsInGroupWithDependencies( projectGroup.getId() );
        if ( projList != null && projList.size() > 0 )
        {
            Project rootProject = ( getContinuum().getProjectsInBuildOrder( projList ) ).get( 0 );

            if ( rootProject != null )
            {
                setUrl( rootProject.getUrl() );
            }
        }
        return SUCCESS;
    }

    public String save()
        throws Exception
    {
        try
        {
            checkModifyProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        if ( name != null )
        {
            if ( name.equals( "" ) )
            {
                addActionError( getText( "projectGroup.error.name.required" ) );
            }
            else if ( name.trim().equals( "" ) )
            {
                addActionError( getText( "projectGroup.error.name.cannot.be.spaces" ) );
            }
            else
            {
                name = name.trim();
                for ( ProjectGroup projectGroup : getContinuum().getAllProjectGroups() )
                {
                    if ( name.equals( projectGroup.getName() ) && projectGroup.getId() != projectGroupId )
                    {
                        addActionError( getText( "projectGroup.error.name.already.exists" ) );
                    }
                }
            }
            if ( hasActionErrors() )
            {
                initialize();
                return INPUT;
            }
        }

        projectGroup = getContinuum().getProjectGroupWithProjects( projectGroupId );

        // need to administer roles since they are based off of this
        // todo convert everything like to work off of string keys
        if ( !name.equals( projectGroup.getName() ) )
        {
            // CONTINUUM-1502
            name = name.trim();
            try
            {
                roleManager.updateRole( "project-administrator", projectGroup.getName(), name );
                roleManager.updateRole( "project-developer", projectGroup.getName(), name );
                roleManager.updateRole( "project-user", projectGroup.getName(), name );

                projectGroup.setName( name );
            }
            catch ( RoleManagerException e )
            {
                throw new ContinuumException( "unable to rename the project group", e );
            }

        }

        projectGroup.setDescription( description );

        // [CONTINUUM-2228]. In select field can't select empty values.
        if ( repositoryId > 0 )
        {
            LocalRepository repository = getContinuum().getRepositoryService().getLocalRepository( repositoryId );
            projectGroup.setLocalRepository( repository );
        }

        getContinuum().updateProjectGroup( projectGroup );

        Collection<Project> projectList = getContinuum().getProjectsInGroupWithDependencies( projectGroupId );
        if ( projectList != null && projectList.size() > 0 )
        {
            Project rootProject = ( getContinuum().getProjectsInBuildOrder( projectList ) ).get( 0 );

            rootProject.setUrl( url );

            getContinuum().updateProject( rootProject );
        }

        Iterator keys = projects.keySet().iterator();
        while ( keys.hasNext() )
        {
            String key = (String) keys.next();

            String[] id = (String[]) projects.get( key );

            int projectId = Integer.parseInt( key );

            Project project = null;
            Iterator i = projectGroup.getProjects().iterator();
            while ( i.hasNext() )
            {
                project = (Project) i.next();
                if ( projectId == project.getId() )
                {
                    break;
                }
            }

            ProjectGroup newProjectGroup = getContinuum().getProjectGroupWithProjects( new Integer( id[0] ) );

            if ( newProjectGroup.getId() != projectGroup.getId() && isAuthorized( newProjectGroup.getName() ) )
            {
                logger.info( "Moving project " + project.getName() + " to project group " + newProjectGroup.getName() );
                project.setProjectGroup( newProjectGroup );

                // CONTINUUM-1512
                Collection<BuildResult> results = getContinuum().getBuildResultsForProject( project.getId() );
                for ( BuildResult br : results )
                {
                    getContinuum().removeBuildResult( br.getId() );
                }

                getContinuum().updateProject( project );
            }
        }

        AuditLog event = new AuditLog( "Project Group id=" + projectGroupId, AuditLogConstants.MODIFY_PROJECT_GROUP );
        event.setCategory( AuditLogConstants.PROJECT );
        event.setCurrentUser( getPrincipal() );
        event.log();

        return SUCCESS;
    }

    public String build()
        throws ContinuumException
    {
        try
        {
            checkBuildProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        if ( this.getBuildDefinitionId() == -1 )
        {
            getContinuum().buildProjectGroup( projectGroupId );
        }
        else
        {
            getContinuum().buildProjectGroupWithBuildDefinition( projectGroupId, buildDefinitionId );
        }

        AuditLog event = new AuditLog( "Project Group id=" + projectGroupId, AuditLogConstants.FORCE_BUILD );
        event.setCategory( AuditLogConstants.PROJECT );
        event.setCurrentUser( getPrincipal() );
        event.log();

        if ( this.isFromSummaryPage() )
        {
            return "to_summary_page";
        }
        else
        {
            return SUCCESS;
        }
    }

    public String release()
        throws ContinuumException
    {
        try
        {
            checkBuildProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        // get the parent of the group by finding the parent project
        // i.e., the project that doesn't have a parent, or it's parent is not in the group.

        Project parent = null;

        boolean allBuildsOk = true;

        boolean allMavenTwo = true;

        projectList = getContinuum().getProjectsInGroupWithDependencies( projectGroupId );

        if ( projectList != null )
        {
            for ( Project p : projectList )
            {
                if ( p.getState() != ContinuumProjectState.OK )
                {
                    allBuildsOk = false;
                }

                if ( ( p.getParent() == null ) || ( !isParentInProjectGroup( p.getParent(), projectList ) ) )
                {
                    if ( parent == null )
                    {
                        parent = p;
                    }
                    else
                    {
                        // currently, we have no provisions for releasing 2 or more parents
                        // at the same time, this will be implemented in the future
                        addActionError( getText( "projectGroup.release.error.severalParentProjects" ) );
                        return INPUT;
                    }
                }

                if ( !"maven2".equals( p.getExecutorId() ) )
                {
                    allMavenTwo = false;
                }
            }
        }

        if ( parent == null )
        {
            addActionError( getText( "projectGroup.release.error.emptyGroup" ) );
            return INPUT;
        }

        releaseProjectId = parent.getId();

        if ( allBuildsOk && allMavenTwo )
        {
            return SUCCESS;
        }
        else
        {
            addActionError( getText( "projectGroup.release.error.projectNotInSuccess" ) );
            return INPUT;
        }
    }

    private boolean isParentInProjectGroup( ProjectDependency parent, Collection<Project> projectsInGroup )
        throws ContinuumException
    {
        boolean result = false;

        for ( Project project : projectsInGroup )
        {
            if ( parent != null )
            {
                if ( ( project.getArtifactId().equals( parent.getArtifactId() ) ) &&
                    ( project.getGroupId().equals( parent.getGroupId() ) ) &&
                    ( project.getVersion().equals( parent.getVersion() ) ) )
                {
                    result = true;
                }
            }
        }

        return result;
    }

    private void populateProjectGroupUsers( ProjectGroup group )
    {
        List<User> users = new ArrayList<User>();

        try
        {
            List<Role> roles = rbac.getAllRoles();
            List<String> roleNames = new ArrayList<String>();
            for ( Role r : roles )
            {
                String projectGroupName = StringUtils.substringAfter( r.getName(), "-" ).trim();

                if ( projectGroupName.equals( group.getName() ) )
                {
                    roleNames.add( r.getName() );
                }
            }
            List<UserAssignment> userAssignments = rbac.getUserAssignmentsForRoles( roleNames );
            for ( UserAssignment ua : userAssignments )
            {
                User u = getSecuritySystem().getUserManager().findUser( ua.getPrincipal() );
                if ( u != null )
                {
                    users.add( u );
                }
            }
        }
        catch ( Exception e )
        {
            logger.error( "Can't get the users list", e );
        }

        if ( StringUtils.isNotBlank( filterKey ) )
        {
            users = findUsers( users, filterProperty, filterKey );
        }
        if ( StringUtils.isNotBlank( sorterProperty ) )
        {
            sortUsers( users, sorterProperty, ascending );
        }

        projectGroupUsers = new ArrayList<ProjectGroupUserBean>();

        if ( users == null )
        {
            return;
        }

        for ( User user : users )
        {
            ProjectGroupUserBean pgUser = new ProjectGroupUserBean();

            pgUser.setUser( user );

            pgUser.setProjectGroup( group );

            try
            {
                Collection<Role> effectiveRoles = rbac.getEffectivelyAssignedRoles( user.getUsername() );

                for ( Role role : effectiveRoles )
                {
                    if ( role.getName().indexOf( projectGroup.getName() ) > -1 )
                    {
                        pgUser.setRoles( effectiveRoles );
                        projectGroupUsers.add( pgUser );
                        break;
                    }
                }
            }
            catch ( RbacObjectNotFoundException e )
            {
                pgUser.setRoles( Collections.EMPTY_LIST );
            }
            catch ( RbacManagerException e )
            {
                pgUser.setRoles( Collections.EMPTY_LIST );
            }
        }
    }

    private List<User> findUsers( List<User> users, String searchProperty, String searchKey )
    {
        List<User> userList = new ArrayList<User>();
        for ( User user : users )
        {
            if ( "username".equals( searchProperty ) )
            {
                String username = user.getUsername();
                if ( username != null )
                {
                    if ( username.toLowerCase().indexOf( searchKey.toLowerCase() ) >= 0 )
                    {
                        userList.add( user );
                    }
                }
            }
            else if ( "fullName".equals( searchProperty ) )
            {
                String fullname = user.getFullName();
                if ( fullname != null )
                {
                    if ( fullname.toLowerCase().indexOf( searchKey.toLowerCase() ) >= 0 )
                    {
                        userList.add( user );
                    }
                }
            }
            else if ( "email".equals( searchProperty ) )
            {
                String email = user.getEmail();
                if ( email != null )
                {
                    if ( email.toLowerCase().indexOf( searchKey.toLowerCase() ) >= 0 )
                    {
                        userList.add( user );
                    }
                }
            }
        }

        return userList;
    }

    private void sortUsers( List<User> userList, final String sorterProperty, final boolean orderAscending )
    {
        Collections.sort( userList, new Comparator<User>()
        {
            public int compare( User o1, User o2 )
            {
                String value1, value2;
                if ( "fullName".equals( sorterProperty ) )
                {
                    value1 = o1.getFullName();
                    value2 = o2.getFullName();
                }
                else if ( "email".equals( sorterProperty ) )
                {
                    value1 = o1.getEmail();
                    value2 = o2.getEmail();
                }
                else
                {
                    value1 = o1.getUsername();
                    value2 = o2.getUsername();
                }
                if ( orderAscending )
                {
                    return ComparatorUtils.nullLowComparator( null ).compare( value1, value2 );
                }
                return ComparatorUtils.nullLowComparator( null ).compare( value2, value1 );
            }
        } );
    }

    protected String generateEncodedRandomVal()
    {
        String encodedRandomVale;

        byte[] random =  new byte[16];
        randomizer.nextBytes( random );
        byte[] all = new byte[17];

        for( int i = 0; i < random.length; i++ )
        {
            all[i] = random[i];
        }

        // include time to ensure uniqueness
        byte time = ( byte ) System.currentTimeMillis();
        all[16] = time;

        // encode as string
        encodedRandomVale = Base64.encodeBase64String( all );

        return encodedRandomVale;
    }

    protected String decodeRandomVal( String encodedRandomVal )
    {
        byte[] randomValInBytes = Base64.decodeBase64( encodedRandomVal );

        String decodedRandomVal = "";
        if( randomValInBytes != null )
        {
            decodedRandomVal = new String( randomValInBytes );
        }

        return decodedRandomVal;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

    public void setProjectGroup( ProjectGroup projectGroup )
    {
        this.projectGroup = projectGroup;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed( boolean confirmed )
    {
        this.confirmed = confirmed;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public Map getProjects()
    {
        return projects;
    }

    public void setProjects( Map projects )
    {
        this.projects = projects;
    }

    public Map<Integer, String> getProjectGroups()
    {
        return projectGroups;
    }

    public void setProjectGroups( Map<Integer, String> projectGroups )
    {
        this.projectGroups = projectGroups;
    }

    public boolean isProjectInCOQueue()
    {
        return projectInCOQueue;
    }

    public void setProjectInCOQueue( boolean projectInQueue )
    {
        this.projectInCOQueue = projectInQueue;
    }

    public Collection<Project> getProjectList()
    {
        return projectList;
    }

    public List<ProjectGroupUserBean> getProjectGroupUsers()
    {
        return projectGroupUsers;
    }

    public boolean isAscending()
    {
        return ascending;
    }

    public void setAscending( boolean ascending )
    {
        this.ascending = ascending;
    }

    public String getFilterKey()
    {
        return filterKey;
    }

    public void setFilterKey( String filterKey )
    {
        this.filterKey = filterKey;
    }

    public String getFilterProperty()
    {
        return filterProperty;
    }

    public void setFilterProperty( String filterProperty )
    {
        this.filterProperty = filterProperty;
    }

    public Map<String, String> getCriteria()
    {
        return FILTER_CRITERIA;
    }

    public void setReleaseProjectId( int releaseProjectId )
    {
        this.releaseProjectId = releaseProjectId;
    }

    public int getReleaseProjectId()
    {
        return this.releaseProjectId;
    }

    public ProjectGroup getProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        if ( projectGroup == null )
        {
            projectGroup = getContinuum().getProjectGroup( projectGroupId );
        }
        else
        {
            if ( projectGroup.getId() != projectGroupId )
            {
                projectGroup = getContinuum().getProjectGroup( projectGroupId );
            }
        }

        return projectGroup;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {

        return getProjectGroup( projectGroupId ).getName();
    }

    public Map<String, Integer> getBuildDefinitions()
    {
        return buildDefinitions;
    }

    public void setBuildDefinitions( Map<String, Integer> buildDefinitions )
    {
        this.buildDefinitions = buildDefinitions;
    }

    public int getBuildDefinitionId()
    {
        return buildDefinitionId;
    }

    public void setBuildDefinitionId( int buildDefinitionId )
    {
        this.buildDefinitionId = buildDefinitionId;
    }

    public boolean isFromSummaryPage()
    {
        return fromSummaryPage;
    }

    public void setFromSummaryPage( boolean fromSummaryPage )
    {
        this.fromSummaryPage = fromSummaryPage;
    }

    public String getPreferredExecutor()
    {
        return preferredExecutor;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public int getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId( int repositoryId )
    {
        this.repositoryId = repositoryId;
    }

    public List<LocalRepository> getRepositories()
    {
        return repositories;
    }

    public void setRepositories( List<LocalRepository> repositories )
    {
        this.repositories = repositories;
    }

    public boolean isDisabledRepositories()
    {
        return disabledRepositories;
    }

    public void setDisabledRepositories( boolean disabledRepositories )
    {
        this.disabledRepositories = disabledRepositories;
    }

    public List<ProjectScmRoot> getProjectScmRoots()
    {
        return projectScmRoots;
    }

    public void setProjectScmRoots( List<ProjectScmRoot> projectScmRoots )
    {
        this.projectScmRoots = projectScmRoots;
    }

    private boolean isAuthorized( String projectGroupName )
    {
        try
        {
            checkAddProjectToGroupAuthorization( projectGroupName );
            return true;
        }
        catch ( AuthorizationRequiredException authzE )
        {
            return false;
        }
    }

    public String getSorterProperty()
    {
        return sorterProperty;
    }

    public void setSorterProperty( String sorterProperty )
    {
        this.sorterProperty = sorterProperty;
    }

    public String getEncodedRandomVal()
    {
        return encodedRandomVal;
    }

    public void setEncodedRandomVal( String encodedRandomVal )
    {
        this.encodedRandomVal = encodedRandomVal;
    }

    public boolean isExplicitCSRFCheck()
    {
        return explicitCSRFCheck;
    }

    public void setExplicitCSRFCheck( boolean explicitCSRFCheck )
    {
        this.explicitCSRFCheck = explicitCSRFCheck;
    }
}
