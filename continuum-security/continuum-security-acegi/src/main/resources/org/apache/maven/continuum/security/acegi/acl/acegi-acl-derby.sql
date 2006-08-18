DROP TABLE acl_object_identity;

CREATE TABLE acl_object_identity (
     id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT acl_object_identity_PK PRIMARY KEY,
     object_identity VARCHAR(250) NOT NULL,
     parent_object INTEGER,
     acl_class VARCHAR(250) NOT NULL,
     CONSTRAINT unique_object_identity UNIQUE(object_identity),
     FOREIGN KEY (parent_object) REFERENCES acl_object_identity(id)
);

DROP TABLE acl_object_identity;

CREATE TABLE acl_permission (
     id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT acl_permission_PK PRIMARY KEY,
     acl_object_identity INTEGER NOT NULL,
     recipient VARCHAR(100) NOT NULL,
     mask INTEGER NOT NULL,
     CONSTRAINT unique_recipient UNIQUE(acl_object_identity, recipient),
     FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity(id)
);

INSERT INTO acl_object_identity VALUES (1, 'corp.DomainObject:1', null, 'org.acegisecurity.acl.basic.SimpleAclEntry');
INSERT INTO acl_object_identity VALUES (2, 'corp.DomainObject:2', 1, 'org.acegisecurity.acl.basic.SimpleAclEntry');
INSERT INTO acl_object_identity VALUES (3, 'corp.DomainObject:3', 1, 'org.acegisecurity.acl.basic.SimpleAclEntry');
INSERT INTO acl_object_identity VALUES (4, 'corp.DomainObject:4', 1, 'org.acegisecurity.acl.basic.SimpleAclEntry');
INSERT INTO acl_object_identity VALUES (5, 'corp.DomainObject:5', 3, 'org.acegisecurity.acl.basic.SimpleAclEntry');
INSERT INTO acl_object_identity VALUES (6, 'corp.DomainObject:6', 3, 'org.acegisecurity.acl.basic.SimpleAclEntry');

INSERT INTO acl_permission VALUES (null, 1, 'ROLE_SUPERVISOR', 1);
INSERT INTO acl_permission VALUES (null, 2, 'ROLE_SUPERVISOR', 0);
INSERT INTO acl_permission VALUES (null, 2, 'marissa', 2);
INSERT INTO acl_permission VALUES (null, 3, 'scott', 14);
INSERT INTO acl_permission VALUES (null, 6, 'scott', 1);
