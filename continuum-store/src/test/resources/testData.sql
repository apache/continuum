# Test data to set up test Continuum database.

# Set up Project Groups
insert into PROJECTGROUP (ID, DESCRIPTION, GROUP_ID, GROUPKEY, NAME)
  values (1, 'Default Group' , 'default', 'Default' , 'Default Group');
insert into PROJECTGROUP (ID, DESCRIPTION, GROUP_ID, GROUPKEY, NAME)
  values (2, 'Group 1' , 'org.apache.maven.continuum', 'Continuum' , 'Continuum Group');
  
      
# Set up projects
insert into PROJECT (ID, DESCRIPTION, ARTIFACT_ID, GROUP_ID, GROUP_KEY, 
    PROJECTKEY, NAME, BUILD_NUMBER, LATEST_BUILD_ID, OLD_STATE, 
    PROJECT_GROUP_ID_OID, "STATE")
  values (1, 'Test Project 1', 'project-1',  'org.test.projects', 'project1',
    'project1', 'Project 1', 0, 0, 0, 1, 0);
insert into PROJECT (ID, DESCRIPTION, ARTIFACT_ID, GROUP_ID, GROUP_KEY, 
    PROJECTKEY, NAME, BUILD_NUMBER, LATEST_BUILD_ID, 
    OLD_STATE, PROJECT_GROUP_ID_OID, "STATE")
  values (2, 'Test Project 2', 'project-2',  'org.test.projects', 'project2',
    'project2', 'Project 2', 0, 0, 0, 1, 0);
insert into PROJECT (ID, DESCRIPTION, ARTIFACT_ID, GROUP_ID, GROUP_KEY, 
    PROJECTKEY, NAME, BUILD_NUMBER, LATEST_BUILD_ID, OLD_STATE, 
    PROJECT_GROUP_ID_OID, "STATE")
  values (3, 'Test Project 3', 'project-3', 'org.test.projects', 'project3',
    'project3', 'Project 3', 0, 0, 0, 2, 0);
