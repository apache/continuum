CREATE SCHEMA sa;

CREATE TABLE acl_object_identity (
     id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT acl_object_identity_PK PRIMARY KEY,
     object_identity VARCHAR(250) NOT NULL,
     parent_object INTEGER,
     acl_class VARCHAR(250) NOT NULL,
     CONSTRAINT unique_object_identity UNIQUE(object_identity),
     FOREIGN KEY (parent_object) REFERENCES acl_object_identity(id)
);

CREATE TABLE acl_permission (
     id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT acl_permission_PK PRIMARY KEY,
     acl_object_identity INTEGER NOT NULL,
     recipient VARCHAR(100) NOT NULL,
     mask INTEGER NOT NULL,
     CONSTRAINT unique_recipient UNIQUE(acl_object_identity, recipient),
     FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity(id)
);

--INSERT INTO acl_object_identity VALUES (1, 'org.apache.maven.continuum.model.project.Project:1', null, 'org.acegisecurity.acl.basic.SimpleAclEntry');
--INSERT INTO acl_object_identity VALUES (2, 'org.apache.maven.continuum.model.project.Project:2', 1, 'org.acegisecurity.acl.basic.SimpleAclEntry');
--INSERT INTO acl_object_identity VALUES (3, 'org.apache.maven.continuum.model.project.Project:3', 1, 'org.acegisecurity.acl.basic.SimpleAclEntry');
--INSERT INTO acl_object_identity VALUES (4, 'org.apache.maven.continuum.model.project.Project:4', 1, 'org.acegisecurity.acl.basic.SimpleAclEntry');
--INSERT INTO acl_object_identity VALUES (5, 'org.apache.maven.continuum.model.project.Project:5', 3, 'org.acegisecurity.acl.basic.SimpleAclEntry');
--INSERT INTO acl_object_identity VALUES (6, 'org.apache.maven.continuum.model.project.Project:6', 3, 'org.acegisecurity.acl.basic.SimpleAclEntry');

--INSERT INTO acl_permission (acl_object_identity, recipient, mask) VALUES (1, 'ROLE_ADMIN', 1);
--INSERT INTO acl_permission (acl_object_identity, recipient, mask) VALUES (2, 'ROLE_ADMIN', 0);
--INSERT INTO acl_permission (acl_object_identity, recipient, mask) VALUES (2, 'marissa', 2);
--INSERT INTO acl_permission (acl_object_identity, recipient, mask) VALUES (3, 'scott', 14);
--INSERT INTO acl_permission (acl_object_identity, recipient, mask) VALUES (6, 'scott', 1);
