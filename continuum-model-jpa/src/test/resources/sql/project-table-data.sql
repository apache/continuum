# Test SQL to populate data here

INSERT INTO PROJECT (ID, DATE_CREATED, DATE_UPDATED, MODEL_ENCODING, ARTIFACT_ID, GROUP_ID, DESCRIPTION, NAME, FLG_SCM_USE_CACHE)
  VALUES (100, 2007-11-01, 2007-11-10, 'UTF-8', 'continuum-jpa-model', 'org.apache.maven.continuum', 'Model for Continuum using JPA', 'continuum-jpa-model', false);

INSERT INTO PROJECT (ID, DATE_CREATED, DATE_UPDATED, MODEL_ENCODING, ARTIFACT_ID, GROUP_ID, DESCRIPTION, NAME, FLG_SCM_USE_CACHE)
  VALUES (101, 2007-11-02, 2007-11-11, 'UTF-8', 'continuum-jdo-model', 'org.apache.maven.continuum', 'Legacy Model for Continuum using JDO', 'continuum-jdo-model', true);