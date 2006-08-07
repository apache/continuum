package org.apache.maven.continuum.security.acegi.aspectj;

/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.MockAuthenticationManager;
import org.acegisecurity.MockRunAsManager;
import org.acegisecurity.acl.AclProvider;
import org.acegisecurity.acl.AclProviderManager;
import org.acegisecurity.acl.basic.AclObjectIdentity;
import org.acegisecurity.acl.basic.BasicAclDao;
import org.acegisecurity.acl.basic.BasicAclEntry;
import org.acegisecurity.acl.basic.BasicAclProvider;
import org.acegisecurity.acl.basic.NamedEntityObjectIdentity;
import org.acegisecurity.acl.basic.SimpleAclEntry;
import org.acegisecurity.afterinvocation.AfterInvocationProvider;
import org.acegisecurity.afterinvocation.AfterInvocationProviderManager;
import org.acegisecurity.afterinvocation.BasicAclEntryAfterInvocationCollectionFilteringProvider;
import org.acegisecurity.afterinvocation.BasicAclEntryAfterInvocationProvider;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.intercept.method.MethodDefinitionMap;
import org.acegisecurity.intercept.method.MethodDefinitionSourceMapping;
import org.acegisecurity.intercept.method.aspectj.AspectJSecurityInterceptor;
import org.acegisecurity.providers.TestingAuthenticationToken;
import org.acegisecurity.vote.AccessDecisionVoter;
import org.acegisecurity.vote.AffirmativeBased;
import org.acegisecurity.vote.BasicAclEntryVoter;
import org.acegisecurity.vote.RoleVoter;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.acegi.intercept.method.aspectj.AspectJSecurityInterceptorHelper;

/**
 * Test for {@link ContinuumSecurityAspect}
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class ProjectSecurityTest
    extends TestCase
{

    private static final String USERNAME = "marissa";

    private ContinuumStub continuum;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        //        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        //        dataSource.setDriverClassName( "org.hsqldb.jdbcDriver" );
        //        dataSource.setUrl( "jdbc:hsqldb:mem:continuum" );
        //        dataSource.setUsername( "sa" );
        //        dataSource.setPassword( "" );
        //
        //        JdbcExtendedDaoImpl aclDao = new JdbcExtendedDaoImpl();
        //        aclDao.setDataSource( dataSource );

        BasicAclProvider basicAclProvider = new BasicAclProvider();
        basicAclProvider.setBasicAclDao( new MockDao() );

        AclProviderManager aclManager = new AclProviderManager();
        aclManager.setProviders( Arrays.asList( new AclProvider[] { basicAclProvider } ) );

        RoleVoter roleVoter = new RoleVoter();

        BasicAclEntryVoter aclProjectReadVoter = new BasicAclEntryVoter();
        aclProjectReadVoter.setProcessConfigAttribute( "ACL_PROJECT_READ" );
        aclProjectReadVoter.setProcessDomainObjectClass( Project.class );
        aclProjectReadVoter.setAclManager( aclManager );
        aclProjectReadVoter.setRequirePermission( new int[] { SimpleAclEntry.ADMINISTRATION, SimpleAclEntry.READ } );

        AffirmativeBased accessDecisionManager = new AffirmativeBased();
        accessDecisionManager.setAllowIfAllAbstainDecisions( false );

        accessDecisionManager.setDecisionVoters( Arrays.asList( new AccessDecisionVoter[] {
            roleVoter,
            aclProjectReadVoter } ) );

        BasicAclEntryAfterInvocationCollectionFilteringProvider afterAclCollectionRead = new BasicAclEntryAfterInvocationCollectionFilteringProvider();
        afterAclCollectionRead.setAclManager( aclManager );
        afterAclCollectionRead.setRequirePermission( new int[] { SimpleAclEntry.ADMINISTRATION, SimpleAclEntry.READ } );

        BasicAclEntryAfterInvocationProvider afterAclRead = new BasicAclEntryAfterInvocationProvider();
        afterAclRead.setAclManager( aclManager );
        afterAclRead.setRequirePermission( new int[] { SimpleAclEntry.ADMINISTRATION, SimpleAclEntry.READ } );

        AfterInvocationProviderManager afterInvocationProviderManager = new AfterInvocationProviderManager();
        afterInvocationProviderManager.setProviders( Arrays.asList( new AfterInvocationProvider[] {
            afterAclCollectionRead,
            afterAclRead } ) );

        MethodDefinitionSourceMapping mapping = new MethodDefinitionSourceMapping();
        mapping.setMethodName( Continuum.class.getCanonicalName() + ".getAllProjects" );
        mapping.setConfigAttributes( Arrays.asList( new String[] { "ROLE_USER", "AFTER_ACL_COLLECTION_READ" } ) );

        List mappings = new ArrayList();
        mappings.add( mapping );

        MethodDefinitionMap methodDefinitionSource = new MethodDefinitionMap();
        methodDefinitionSource.setMappings( mappings );

        AspectJSecurityInterceptor si = new AspectJSecurityInterceptor();
        si.setObjectDefinitionSource( methodDefinitionSource );
        si.setAccessDecisionManager( accessDecisionManager );
        si.setAuthenticationManager( new MockAuthenticationManager() );
        si.setRunAsManager( new MockRunAsManager() );
        si.setAfterInvocationManager( afterInvocationProviderManager );

        continuum = new ContinuumStub();
        AspectJSecurityInterceptorHelper helper = new AspectJSecurityInterceptorHelper();
        helper.setAspectName( "org.apache.maven.continuum.security.acegi.aspectj.ContinuumSecurityAspect" );
        helper.setSecurityInterceptor( si );
        helper.initialize();
    }

    public void testGetAllProjects()
        throws Exception
    {
        Project project1 = new Project();
        project1.setId( 1 );

        Project project2 = new Project();
        project2.setId( 2 );

        List mockProjects = new ArrayList();
        mockProjects.add( project1 );
        mockProjects.add( project2 );

        continuum.setMockProjects( mockProjects );

        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                                                                                    USERNAME,
                                                                                    "koala",
                                                                                    new GrantedAuthority[] { new GrantedAuthorityImpl(
                                                                                                                                       "ROLE_USER" ) } );
        SecurityContextHolder.getContext().setAuthentication( authentication );

        Collection allProjects = continuum.getAllProjects( 1, 1000 );

        assertEquals( "Number of projects returned does not match", 1, allProjects.size() );
        assertEquals( "The returned project is not the right one", 1, ( (Project) allProjects.iterator().next() )
            .getId() );
    }

    /**
     * {@link BasicAclDao} that will allow READ for {@link Project} with id 1.
     * 
     * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
     * @version $Id$
     */
    private class MockDao
        implements BasicAclDao
    {
        private SimpleAclEntry aclEntry1, aclEntryDefault;

        public MockDao()
        {
            aclEntryDefault = new SimpleAclEntry();
            aclEntryDefault.addPermission( SimpleAclEntry.NOTHING );
            aclEntryDefault.setRecipient( USERNAME );

            aclEntry1 = new SimpleAclEntry();
            aclEntry1.addPermission( SimpleAclEntry.READ );
            aclEntry1.setRecipient( USERNAME );
        }

        public BasicAclEntry[] getAcls( AclObjectIdentity aclObjectIdentity )
        {
            NamedEntityObjectIdentity objectIdentity = ( (NamedEntityObjectIdentity) aclObjectIdentity );

            if ( objectIdentity.getId().equals( "1" ) )
            {
                return new BasicAclEntry[] { aclEntry1 };
            }
            else
            {
                return new BasicAclEntry[] { aclEntryDefault };
            }
        }
    }
}
