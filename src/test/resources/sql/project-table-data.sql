# Test SQL to populate data here

INSERT INTO PROJECT (ID, DATE_CREATED, DATE_UPDATED, ARTIFACT_ID, GROUP_ID, DESCRIPTION, NAME, FLG_SCM_USE_CACHE)
  VALUES (100, 2007-11-01, 2007-11-10, 'continuum-jpa-model', 'org.apache.continuum', 'Model for Continuum using JPA', 'continuum-jpa-model', false);

INSERT INTO PROJECT (ID, DATE_CREATED, DATE_UPDATED, ARTIFACT_ID, GROUP_ID, DESCRIPTION, NAME, FLG_SCM_USE_CACHE)
  VALUES (101, 2007-11-02, 2007-11-11, 'continuum-jdo-model', 'org.apache.continuum', 'Legacy Model for Continuum using JDO', 'continuum-jdo-model', true);