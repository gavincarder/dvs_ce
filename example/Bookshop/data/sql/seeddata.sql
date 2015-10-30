;             
CREATE USER IF NOT EXISTS TEST SALT '59973a18f78f3d09' HASH '597e75038a2d934671ac01ec0705c54776161a71c4521a5ec5ce0354410737b2' ADMIN;         
CREATE SEQUENCE PUBLIC.ODME_SEQNUM START WITH 141;            
CREATE MEMORY TABLE PUBLIC.ADDRESS(
    ___ID___ VARCHAR NOT NULL,
    STREETADDRESS VARCHAR NOT NULL,
    CITY VARCHAR NOT NULL,
    POSTALCODE VARCHAR,
    REGION VARCHAR NOT NULL,
    COUNTRY VARCHAR
);          
ALTER TABLE PUBLIC.ADDRESS ADD CONSTRAINT PUBLIC.CONSTRAINT_E PRIMARY KEY(___ID___);          
-- 24 +/- SELECT COUNT(*) FROM PUBLIC.ADDRESS;
INSERT INTO PUBLIC.ADDRESS(___ID___, STREETADDRESS, CITY, POSTALCODE, REGION, COUNTRY) VALUES
('Personnel(EMP00127)/ContactInfo/MailingAddress', '999 Circle Street', 'Trail', 'V1R 1P5', 'British Columbia', 'Canada'),
('Personnel(EMP00127)/WorkLocation', '1020 Grand Blvd.', 'Potlatch', '83855', 'ID', 'USA'),
('Personnel(EMP00128)/ContactInfo/MailingAddress', '44 Square Square', 'Trail', 'V1R 1P5', 'British Columbia', 'Canada'),
('Personnel(EMP00128)/WorkLocation', '1020 Grand Blvd.', 'Potlatch', '83855', 'ID', 'USA'),
('Personnel(EMP00129)/ContactInfo/MailingAddress', '20 Plain Street', 'Viola', '83855', 'ID', NULL),
('Personnel(EMP00129)/WorkLocation', '1020 Grand Blvd.', 'Potlatch', '83855', 'ID', 'USA'),
('Personnel(EMP00130)/ContactInfo/MailingAddress', '120 High Street', 'Moscow', '83843', 'ID', NULL),
('Personnel(EMP00130)/WorkLocation', '1020 Grand Blvd.', 'Potlatch', '83855', 'ID', 'USA'),
('Personnel(EMP00131)/ContactInfo/MailingAddress', '14 Warren Street', 'Viola', '83855', 'ID', NULL),
('Personnel(EMP00131)/WorkLocation', '1020 Grand Blvd.', 'Potlatch', '83855', 'ID', 'USA'),
('Personnel(EMP00132)/ContactInfo/MailingAddress', '141 Locksmith Way', 'Moscow', '83843', 'ID', NULL),
('Personnel(EMP00132)/WorkLocation', '1020 Grand Blvd.', 'Potlatch', '83855', 'ID', 'USA'),
('Personnel(EMP00133)/ContactInfo/MailingAddress', '678 Tommy Lane', 'Potlatch', '83855', 'ID', NULL),
('Personnel(EMP00133)/WorkLocation', '1020 Grand Blvd.', 'Potlatch', '83855', 'ID', 'USA'),
('Personnel(EMP00134)/ContactInfo/MailingAddress', '202 Elm Street', 'Pullman', '99164', 'WA', NULL),
('Personnel(EMP00134)/WorkLocation', '100 Main Street', 'Pullman', '99164', 'WA', 'USA'),
('Personnel(EMP00135)/ContactInfo/MailingAddress', '202 Tyburn Street', 'Pullman', '99164', 'WA', NULL),
('Personnel(EMP00135)/WorkLocation', '100 Main Street', 'Pullman', '99164', 'WA', 'USA'),
('Personnel(EMP00136)/ContactInfo/MailingAddress', '1 Gashford Place', 'Pullman', '99164', 'WA', NULL),
('Personnel(EMP00136)/WorkLocation', '100 Main Street', 'Pullman', '99164', 'WA', 'USA'),
('Customer(CUS00137)/ContactInfo/MailingAddress', '1201 S 2ND ST', 'Millwaukee', '53201', 'WI', 'USA'),
('Customer(CUS00138)/ContactInfo/MailingAddress', '1 COMPUTER ASSOCIATES PLZ', 'Islandia', '11749-7000', 'NY', 'USA'),
('Order(ORD00139)/ShipTo', '1201 S 2ND ST', 'Millwaukee', '53201', 'WI', 'USA'),
('Order(ORD00140)/ShipTo', '1 COMPUTER ASSOCIATES PLZ', 'Islandia', '11749-7000', 'NY', 'USA');           
CREATE MEMORY TABLE PUBLIC.DIMENSIONS(
    ___ID___ VARCHAR NOT NULL,
    HEIGHT DOUBLE NOT NULL,
    WIDTH DOUBLE NOT NULL,
    THICKNESS DOUBLE NOT NULL
);            
ALTER TABLE PUBLIC.DIMENSIONS ADD CONSTRAINT PUBLIC.CONSTRAINT_3 PRIMARY KEY(___ID___);       
-- 12 +/- SELECT COUNT(*) FROM PUBLIC.DIMENSIONS;             
INSERT INTO PUBLIC.DIMENSIONS(___ID___, HEIGHT, WIDTH, THICKNESS) VALUES
('Book(FIC-101-001)/Size', 9.0, 5.5, 0.1),
('Book(FIC-101-002)/Size', 9.0, 5.5, 1.0),
('Book(FIC-101-003)/Size', 9.0, 5.5, 1.2),
('Book(FIC-101-004)/Size', 9.0, 5.5, 1.0),
('Book(FIC-101-005)/Size', 9.0, 5.5, 1.0),
('Book(SOC-102-001)/Size', 11.0, 7.5, 1.0),
('Book(FIC-104-001)/Size', 15.0, 9.5, 2.0),
('Book(FIC-104-002)/Size', 5.0, 3.5, 0.1),
('Book(FIC-105-001)/Size', 7.0, 4.0, 1.5),
('Book(FIC-105-002)/Size', 7.0, 4.0, 2.0),
('Book(FIC-105-003)/Size', 7.0, 4.0, 1.5),
('Book(REF-100-001)/Size', 8.0, 5.5, 70.0);   
CREATE MEMORY TABLE PUBLIC.CONTACTINFO(
    ___ID___ VARCHAR NOT NULL,
    PRIMARYPHONE VARCHAR,
    ALTERNATEPHONES VARCHAR DEFAULT '[]' NOT NULL,
    PRIMARYEMAIL VARCHAR,
    ALTERNATEEMAILS VARCHAR DEFAULT '[]' NOT NULL,
    MAILINGADDRESS VARCHAR
);         
ALTER TABLE PUBLIC.CONTACTINFO ADD CONSTRAINT PUBLIC.CONSTRAINT_8 PRIMARY KEY(___ID___);      
-- 12 +/- SELECT COUNT(*) FROM PUBLIC.CONTACTINFO;            
INSERT INTO PUBLIC.CONTACTINFO(___ID___, PRIMARYPHONE, ALTERNATEPHONES, PRIMARYEMAIL, ALTERNATEEMAILS, MAILINGADDRESS) VALUES
('Personnel(EMP00127)/ContactInfo', '(123)-456-7890', '["(345)-123-7777"]', 'boss@nonesuchbooks.com', '["jcapogrosso@nonesuch.com","bighead@bmail.com"]', '{"StreetAddress":"999 Circle Street","City":"Trail","PostalCode":"V1R 1P5","Region":"British Columbia","Country":"Canada"}'),
('Personnel(EMP00128)/ContactInfo', '(123)-456-9999', '[]', 'cratchet@nonesuchbooks.com', '["r.cratchet@bmail.com"]', '{"StreetAddress":"44 Square Square","City":"Trail","PostalCode":"V1R 1P5","Region":"British Columbia","Country":"Canada"}'),
('Personnel(EMP00129)/ContactInfo', '(345)-123-7777', '[""]', 'rowan.eden@nonesuchbooks.com', '["rowan.eden@nonesuch.com"]', '{"StreetAddress":"20 Plain Street","City":"Viola","PostalCode":"83855","Region":"ID"}'),
('Personnel(EMP00130)/ContactInfo', NULL, '["(345)-123-6666"]', NULL, '["artful@nonesuch.com"]', '{"StreetAddress":"120 High Street","City":"Moscow","PostalCode":"83843","Region":"ID"}'),
('Personnel(EMP00131)/ContactInfo', '(345)-123-4512', '["(345)-321-7667"]', 'haredale@nonesuchbooks.com', '["deredrum@nonesuch.com"]', '{"StreetAddress":"14 Warren Street","City":"Viola","PostalCode":"83855","Region":"ID"}'),
('Personnel(EMP00132)/ContactInfo', '(345)-123-4512', '["(345)-655-8667"]', 'varden@nonesuchbooks.com', '["miss.varden@bmail.com"]', '{"StreetAddress":"141 Locksmith Way","City":"Moscow","PostalCode":"83843","Region":"ID"}'),
('Personnel(EMP00133)/ContactInfo', '(345)-123-4512', '["(345)-564-8007"]', 'willet@nonesuchbooks.com', '["joseph.willet@nonesuch.com","jaw@bmail.com"]', '{"StreetAddress":"678 Tommy Lane","City":"Potlatch","PostalCode":"83855","Region":"ID"}'),
('Personnel(EMP00134)/ContactInfo', '(800)-777-7777', '["(509)-677-5678","(509)-675-5555"]', 'rudge@nonesuchbooks.com', '["barnaby.rudge@bmail.com"]', '{"StreetAddress":"202 Elm Street","City":"Pullman","PostalCode":"99164","Region":"WA"}'),
('Personnel(EMP00135)/ContactInfo', '(800)-777-7777', '["(509)-434-0078","(509)-434-4232"]', 'grueby@nonesuchbooks.com', '["john.grueby@bmail.com"]', '{"StreetAddress":"202 Tyburn Street","City":"Pullman","PostalCode":"99164","Region":"WA"}'),
('Personnel(EMP00136)/ContactInfo', '(800)-777-7777', '["(509)-666-6969"]', 'gordon@nonesuchbooks.com', '["george.gordon.iv@bmail.com"]', '{"StreetAddress":"1 Gashford Place","City":"Pullman","PostalCode":"99164","Region":"WA"}'),
('Customer(CUS00137)/ContactInfo', '(917)-342-2010', '["(917)-342-2000"]', NULL, '["William.Lam@mycompany.com"]', '{"StreetAddress":"1201 S 2ND ST","City":"Millwaukee","PostalCode":"53201","Region":"WI","Country":"USA"}'),
('Customer(CUS00138)/ContactInfo', '(321)-342-1010', '["(321)-342-1986"]', NULL, '["Emily.Lee@mytest.com"]', '{"StreetAddress":"1 COMPUTER ASSOCIATES PLZ","City":"Islandia","PostalCode":"11749-7000","Region":"NY","Country":"USA"}');      
CREATE MEMORY TABLE PUBLIC.CUSTOMER(
    ___ID___ VARCHAR NOT NULL,
    ___CLASS___ VARCHAR,
    ___ETAG___ VARCHAR,
    ID VARCHAR NOT NULL,
    CUSTOMERNAME VARCHAR NOT NULL,
    DESCRIPTION VARCHAR(2048),
    CONTACTINFO VARCHAR
);            
ALTER TABLE PUBLIC.CUSTOMER ADD CONSTRAINT PUBLIC.CONSTRAINT_5 PRIMARY KEY(ID);               
-- 2 +/- SELECT COUNT(*) FROM PUBLIC.CUSTOMER;
INSERT INTO PUBLIC.CUSTOMER(___ID___, ___CLASS___, ___ETAG___, ID, CUSTOMERNAME, DESCRIPTION, CONTACTINFO) VALUES
('Customer(CUS00137)', NULL, NULL, 'CUS00137', 'William, Lam', '', '{"PrimaryPhone":"(917)-342-2010","AlternatePhones":["(917)-342-2000"],"AlternateEmails":["William.Lam@mycompany.com"],"MailingAddress":{"StreetAddress":"1201 S 2ND ST","City":"Millwaukee","PostalCode":"53201","Region":"WI","Country":"USA"}}'),
('Customer(CUS00138)', NULL, NULL, 'CUS00138', 'Emily, Lee', '', '{"PrimaryPhone":"(321)-342-1010","AlternatePhones":["(321)-342-1986"],"AlternateEmails":["Emily.Lee@mytest.com"],"MailingAddress":{"StreetAddress":"1 COMPUTER ASSOCIATES PLZ","City":"Islandia","PostalCode":"11749-7000","Region":"NY","Country":"USA"}}');   
CREATE MEMORY TABLE PUBLIC.BOOK(
    ___ID___ VARCHAR NOT NULL,
    ___CLASS___ VARCHAR,
    ___ETAG___ VARCHAR,
    ID VARCHAR(16) NOT NULL,
    AUTHORID VARCHAR NOT NULL,
    TITLE VARCHAR NOT NULL,
    SYNOPSIS VARCHAR,
    LISTPRICE DOUBLE,
    WEIGHT DOUBLE,
    SIZE VARCHAR
);        
ALTER TABLE PUBLIC.BOOK ADD CONSTRAINT PUBLIC.CONSTRAINT_1 PRIMARY KEY(ID);   
-- 12 +/- SELECT COUNT(*) FROM PUBLIC.BOOK;   
INSERT INTO PUBLIC.BOOK(___ID___, ___CLASS___, ___ETAG___, ID, AUTHORID, TITLE, SYNOPSIS, LISTPRICE, WEIGHT, SIZE) VALUES
('Book(FIC-101-001)', NULL, NULL, 'FIC-101-001', '101', 'A Christmas Carol', 'Scrooge meets ghosts.  Is reborn.', 9.99, 0.2, '{"Height":9,"Width":5.5,"Thickness":0.1}'),
('Book(FIC-101-002)', NULL, NULL, 'FIC-101-002', '101', 'Bleak House', 'In the end the lawyers always win.', 24.95, 1.2, '{"Height":9,"Width":5.5,"Thickness":1}'),
('Book(FIC-101-003)', NULL, NULL, 'FIC-101-003', '101', 'Oliver Twist', 'Orphaned boy meets interesting people.', 24.95, 1.4, '{"Height":9,"Width":5.5,"Thickness":1.2}'),
('Book(FIC-101-004)', NULL, NULL, 'FIC-101-004', '101', 'A Tale of Two Cities', 'It was the best of times, it was the worst of times', 24.95, 1.0, '{"Height":9,"Width":5.5,"Thickness":1}'),
('Book(FIC-101-005)', NULL, NULL, 'FIC-101-005', '101', 'Pickwick Papers', 'Still funny after all these years', 24.95, 1.0, '{"Height":9,"Width":5.5,"Thickness":1}'),
('Book(SOC-102-001)', NULL, NULL, 'SOC-102-001', '102', 'London Labor and the London Poor', 'Seminal work describing the lives and livelihoods of London''s underclass', 52.99, 2.0, '{"Height":11,"Width":7.5,"Thickness":1}'),
('Book(FIC-104-001)', NULL, NULL, 'FIC-104-001', '104', 'Descriptio civitatum et regionum ad septentrionalem plagam Danubii', 'Purported early medieval work describing the peoples of Central Europe.', NULL, 3.0, '{"Height":15,"Width":9.5,"Thickness":2}'),
('Book(FIC-104-002)', NULL, NULL, 'FIC-104-002', '104', 'Sir Orfeo', 'Medieval retelling of the Orpheus Legend', NULL, 0.2, '{"Height":5,"Width":3.5,"Thickness":0.1}'),
('Book(FIC-105-001)', NULL, NULL, 'FIC-105-001', '105', 'Tales of the South Pacific', 'Collection of related stories set in the Pacific during World War II.', 29.99, 0.5, '{"Height":7,"Width":4,"Thickness":1.5}'),
('Book(FIC-105-002)', NULL, NULL, 'FIC-105-002', '105', 'Texas', 'A quasi-historical novel which at over a thousand pages is almost bigger than its subject.', 29.99, 1.0, '{"Height":7,"Width":4,"Thickness":2}'),
('Book(FIC-105-003)', NULL, NULL, 'FIC-105-003', '105', 'Chesapeake', 'Historical fiction centered on the Chesapeake Bay region from pre-colonial times through the late twentieth century', 29.99, 0.5, '{"Height":7,"Width":4,"Thickness":1.5}'),
('Book(REF-100-001)', NULL, NULL, 'REF-100-001', '100', 'Cabinet Cyclopedia', 'Massive 133 Volume early 19th century Encyclopedia, authored by various prominent writers, and edited by the inimical Dionysius Lardner.', NULL, 50.2, '{"Height":8,"Width":5.5,"Thickness":70}'); 
CREATE MEMORY TABLE PUBLIC.WORKTEAMPROPERTY(
    ___ID___ VARCHAR NOT NULL,
    ___CLASS___ VARCHAR,
    ___ETAG___ VARCHAR,
    NAME VARCHAR(256) NOT NULL,
    DATATYPE VARCHAR(256),
    DESCRIPTION VARCHAR(4096),
    DEFAULTVALUE VARCHAR(256),
    POSSIBLEVALUES VARCHAR(4096),
    PRODUCERMAPPINGPROPERTY VARCHAR(256),
    ENTITYNAME VARCHAR(256)
);   
ALTER TABLE PUBLIC.WORKTEAMPROPERTY ADD CONSTRAINT PUBLIC.CONSTRAINT_D PRIMARY KEY(NAME);     
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.WORKTEAMPROPERTY;        
CREATE MEMORY TABLE PUBLIC.PERSONNEL(
    ___ID___ VARCHAR NOT NULL,
    ___CLASS___ VARCHAR,
    ___ETAG___ VARCHAR,
    ID VARCHAR NOT NULL,
    NAME VARCHAR,
    TITLE VARCHAR,
    CONTACTINFO VARCHAR,
    SUPERVISORID VARCHAR,
    SKILLSET VARCHAR DEFAULT '[]' NOT NULL,
    WORKLOCATION VARCHAR,
    CLOCKIN TIME,
    CLOCKOUT TIME
);              
ALTER TABLE PUBLIC.PERSONNEL ADD CONSTRAINT PUBLIC.CONSTRAINT_D8 PRIMARY KEY(ID);             
-- 10 +/- SELECT COUNT(*) FROM PUBLIC.PERSONNEL;              
INSERT INTO PUBLIC.PERSONNEL(___ID___, ___CLASS___, ___ETAG___, ID, NAME, TITLE, CONTACTINFO, SUPERVISORID, SKILLSET, WORKLOCATION, CLOCKIN, CLOCKOUT) VALUES
('Personnel(EMP00127)', NULL, NULL, 'EMP00127', 'Julius Capogrosso', 'Owner', '{"PrimaryPhone":"(123)-456-7890","AlternatePhones":["(345)-123-7777"],"PrimaryEmail":"boss@nonesuchbooks.com","AlternateEmails":["jcapogrosso@nonesuch.com","bighead@bmail.com"],"MailingAddress":{"StreetAddress":"999 Circle Street","City":"Trail","PostalCode":"V1R 1P5","Region":"British Columbia","Country":"Canada"}}', NULL, '["MBA","Medieval History"]', '{"StreetAddress":"1020 Grand Blvd.","City":"Potlatch","PostalCode":"83855","Region":"ID","Country":"USA"}', NULL, NULL),
('Personnel(EMP00128)', NULL, NULL, 'EMP00128', 'Robert Cratchet', 'CFO', '{"PrimaryPhone":"(123)-456-9999","AlternatePhones":[],"PrimaryEmail":"cratchet@nonesuchbooks.com","AlternateEmails":["r.cratchet@bmail.com"],"MailingAddress":{"StreetAddress":"44 Square Square","City":"Trail","PostalCode":"V1R 1P5","Region":"British Columbia","Country":"Canada"}}', 'EMP00127', '["CPA","MBA"]', '{"StreetAddress":"1020 Grand Blvd.","City":"Potlatch","PostalCode":"83855","Region":"ID","Country":"USA"}', TIME '09:00:00', TIME '19:00:00'),
('Personnel(EMP00129)', NULL, NULL, 'EMP00129', 'Rowena Eden', 'Manager', '{"PrimaryPhone":"(345)-123-7777","AlternatePhones":[""],"PrimaryEmail":"rowan.eden@nonesuchbooks.com","AlternateEmails":["rowan.eden@nonesuch.com"],"MailingAddress":{"StreetAddress":"20 Plain Street","City":"Viola","PostalCode":"83855","Region":"ID"}}', NULL, '["Victorian Literature"]', '{"StreetAddress":"1020 Grand Blvd.","City":"Potlatch","PostalCode":"83855","Region":"ID","Country":"USA"}', TIME '09:00:00', TIME '19:00:00'),
('Personnel(EMP00130)', NULL, NULL, 'EMP00130', 'Arthur Dodger', 'Stockboy', '{"PrimaryPhone":null,"AlternatePhones":["(345)-123-6666"],"PrimaryEmail":null,"AlternateEmails":["artful@nonesuch.com"],"MailingAddress":{"StreetAddress":"120 High Street","City":"Moscow","PostalCode":"83843","Region":"ID"}}', 'EMP00129', '[]', '{"StreetAddress":"1020 Grand Blvd.","City":"Potlatch","PostalCode":"83855","Region":"ID","Country":"USA"}', TIME '16:00:00', TIME '19:00:00'),
('Personnel(EMP00131)', NULL, NULL, 'EMP00131', 'Reuben Haredale', 'Associate', '{"PrimaryPhone":"(345)-123-4512","AlternatePhones":["(345)-321-7667"],"PrimaryEmail":"haredale@nonesuchbooks.com","AlternateEmails":["deredrum@nonesuch.com"],"MailingAddress":{"StreetAddress":"14 Warren Street","City":"Viola","PostalCode":"83855","Region":"ID"}}', 'EMP00129', '["Victorian Literature","Horticulture"]', '{"StreetAddress":"1020 Grand Blvd.","City":"Potlatch","PostalCode":"83855","Region":"ID","Country":"USA"}', TIME '09:00:00', TIME '16:00:00'),
('Personnel(EMP00132)', NULL, NULL, 'EMP00132', 'Dolly Varden', 'Associate', '{"PrimaryPhone":"(345)-123-4512","AlternatePhones":["(345)-655-8667"],"PrimaryEmail":"varden@nonesuchbooks.com","AlternateEmails":["miss.varden@bmail.com"],"MailingAddress":{"StreetAddress":"141 Locksmith Way","City":"Moscow","PostalCode":"83843","Region":"ID"}}', 'EMP00129', '["Horticulture"]', '{"StreetAddress":"1020 Grand Blvd.","City":"Potlatch","PostalCode":"83855","Region":"ID","Country":"USA"}', TIME '12:00:00', TIME '19:00:00'),
('Personnel(EMP00133)', NULL, NULL, 'EMP00133', 'Joe Willet', 'Junior Associate', '{"PrimaryPhone":"(345)-123-4512","AlternatePhones":["(345)-564-8007"],"PrimaryEmail":"willet@nonesuchbooks.com","AlternateEmails":["joseph.willet@nonesuch.com","jaw@bmail.com"],"MailingAddress":{"StreetAddress":"678 Tommy Lane","City":"Potlatch","PostalCode":"83855","Region":"ID"}}', 'EMP00132', '["BS Sociology"]', '{"StreetAddress":"1020 Grand Blvd.","City":"Potlatch","PostalCode":"83855","Region":"ID","Country":"USA"}', TIME '12:00:00', TIME '19:00:00'),
('Personnel(EMP00134)', NULL, NULL, 'EMP00134', 'Barnaby Rudge', 'Manager', '{"PrimaryPhone":"(800)-777-7777","AlternatePhones":["(509)-677-5678","(509)-675-5555"],"PrimaryEmail":"rudge@nonesuchbooks.com","AlternateEmails":["barnaby.rudge@bmail.com"],"MailingAddress":{"StreetAddress":"202 Elm Street","City":"Pullman","PostalCode":"99164","Region":"WA"}}', 'EMP00127', '["MS Sociology","Medieval History"]', '{"StreetAddress":"100 Main Street","City":"Pullman","PostalCode":"99164","Region":"WA","Country":"USA"}', TIME '09:00:00', TIME '19:00:00');            
INSERT INTO PUBLIC.PERSONNEL(___ID___, ___CLASS___, ___ETAG___, ID, NAME, TITLE, CONTACTINFO, SUPERVISORID, SKILLSET, WORKLOCATION, CLOCKIN, CLOCKOUT) VALUES
('Personnel(EMP00135)', NULL, NULL, 'EMP00135', 'John Grueby', 'Associate', '{"PrimaryPhone":"(800)-777-7777","AlternatePhones":["(509)-434-0078","(509)-434-4232"],"PrimaryEmail":"grueby@nonesuchbooks.com","AlternateEmails":["john.grueby@bmail.com"],"MailingAddress":{"StreetAddress":"202 Tyburn Street","City":"Pullman","PostalCode":"99164","Region":"WA"}}', 'EMP00134', '["Military History"]', '{"StreetAddress":"100 Main Street","City":"Pullman","PostalCode":"99164","Region":"WA","Country":"USA"}', TIME '09:00:00', TIME '19:00:00'),
('Personnel(EMP00136)', NULL, NULL, 'EMP00136', 'George Gordon', 'Associate', '{"PrimaryPhone":"(800)-777-7777","AlternatePhones":["(509)-666-6969"],"PrimaryEmail":"gordon@nonesuchbooks.com","AlternateEmails":["george.gordon.iv@bmail.com"],"MailingAddress":{"StreetAddress":"1 Gashford Place","City":"Pullman","PostalCode":"99164","Region":"WA"}}', 'EMP00134', '["Theology"]', '{"StreetAddress":"100 Main Street","City":"Pullman","PostalCode":"99164","Region":"WA","Country":"USA"}', TIME '09:00:00', TIME '19:00:00');              
CREATE MEMORY TABLE PUBLIC.MEMBERSHIP(
    EMPLOYEE VARCHAR NOT NULL,
    TEAM VARCHAR NOT NULL
);         
ALTER TABLE PUBLIC.MEMBERSHIP ADD CONSTRAINT PUBLIC.CONSTRAINT_C PRIMARY KEY(EMPLOYEE, TEAM); 
-- 13 +/- SELECT COUNT(*) FROM PUBLIC.MEMBERSHIP;             
INSERT INTO PUBLIC.MEMBERSHIP(EMPLOYEE, TEAM) VALUES
('EMP00127', 'Historians'),
('EMP00134', 'Historians'),
('EMP00135', 'Historians'),
('EMP00127', 'Management'),
('EMP00128', 'Management'),
('EMP00129', 'Management'),
('EMP00134', 'Management'),
('EMP00130', 'Associates'),
('EMP00131', 'Associates'),
('EMP00132', 'Associates'),
('EMP00133', 'Associates'),
('EMP00135', 'Associates'),
('EMP00136', 'Associates'); 
CREATE MEMORY TABLE PUBLIC.CUSTORDER(
    ___ID___ VARCHAR NOT NULL,
    ___CLASS___ VARCHAR,
    ___ETAG___ VARCHAR,
    ID VARCHAR NOT NULL,
    CUSTOMERID VARCHAR NOT NULL,
    STATUS VARCHAR DEFAULT 'open',
    SHIPVIA VARCHAR DEFAULT 'Parcel Post',
    SHIPTO VARCHAR,
    TAGS VARCHAR DEFAULT '[]' NOT NULL,
    CREATED TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    LASTUPDATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);              
ALTER TABLE PUBLIC.CUSTORDER ADD CONSTRAINT PUBLIC.CONSTRAINT_6 PRIMARY KEY(ID);              
-- 2 +/- SELECT COUNT(*) FROM PUBLIC.CUSTORDER;               
INSERT INTO PUBLIC.CUSTORDER(___ID___, ___CLASS___, ___ETAG___, ID, CUSTOMERID, STATUS, SHIPVIA, SHIPTO, TAGS, CREATED, LASTUPDATE) VALUES
('Order(ORD00139)', NULL, NULL, 'ORD00139', 'CUS00137', 'open', 'Parcel Post', '{"StreetAddress":"1201 S 2ND ST","City":"Millwaukee","PostalCode":"53201","Region":"WI","Country":"USA"}', '[]', TIMESTAMP '2015-08-04 16:12:32.633', TIMESTAMP '2015-08-04 16:12:32.633'),
('Order(ORD00140)', NULL, NULL, 'ORD00140', 'CUS00138', 'open', 'Parcel Post', '{"StreetAddress":"1 COMPUTER ASSOCIATES PLZ","City":"Islandia","PostalCode":"11749-7000","Region":"NY","Country":"USA"}', '[]', TIMESTAMP '2015-08-04 16:12:36.214', TIMESTAMP '2015-08-04 16:12:36.214');           
CREATE MEMORY TABLE PUBLIC.COMMENT(
    ___ID___ VARCHAR NOT NULL,
    ___CLASS___ VARCHAR,
    ___ETAG___ VARCHAR,
    ID INT NOT NULL,
    BOOKID VARCHAR(16) NOT NULL,
    CRITICNAME VARCHAR NOT NULL,
    CRITICEMAIL VARCHAR NOT NULL,
    COMMENT VARCHAR,
    POSTED TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);               
ALTER TABLE PUBLIC.COMMENT ADD CONSTRAINT PUBLIC.CONSTRAINT_63 PRIMARY KEY(ID);               
-- 21 +/- SELECT COUNT(*) FROM PUBLIC.COMMENT;
INSERT INTO PUBLIC.COMMENT(___ID___, ___CLASS___, ___ETAG___, ID, BOOKID, CRITICNAME, CRITICEMAIL, COMMENT, POSTED) VALUES
('Comment(106)', NULL, NULL, 106, 'REF-100-001', 'Bill Nye', 'wnye@nonesuch.org', 'Only lightly touched this massive compendium, but the strange slant on scientific topics gives this some interest.   Larden''s opinions that locomotive speed above 30 MPH would be fatal to the occupants particularly amusing.', TIMESTAMP '2015-08-04 16:11:33.696'),
('Comment(107)', NULL, NULL, 107, 'REF-100-001', 'Thomas Bowdler', 'prof.bowdler@bluenose.org', 'A most informative encyclopedia carefully prepared to safely edify the aspiring.   Scrupulously clean.', TIMESTAMP '2015-08-04 16:11:34.548'),
('Comment(108)', NULL, NULL, 108, 'FIC-101-001', 'G. Scott', 'gcscott@nonesuch.org', 'The movie was better.', TIMESTAMP '2015-08-04 16:11:35.841'),
('Comment(109)', NULL, NULL, 109, 'FIC-101-001', 'Sarah Deangelo', 'smd@erehwon.org', 'No finer family tradition that reading this classic ghost story to the children during the holidays.', TIMESTAMP '2015-08-04 16:11:36.8'),
('Comment(110)', NULL, NULL, 110, 'FIC-101-003', 'Sarah Deangelo', 'smd@erehwon.org', 'Darker than the play or the movie, but still a wonderful read for capable young readers.', TIMESTAMP '2015-08-04 16:11:37.641'),
('Comment(111)', NULL, NULL, 111, 'FIC-101-003', 'Mike Devine', 'mike@oldsanjuan.pr.org', 'Bill Sykes still scares me.', TIMESTAMP '2015-08-04 16:11:38.496'),
('Comment(112)', NULL, NULL, 112, 'FIC-101-003', 'Mike Devine', 'mike@oldsanjuan.pr.org', 'Oh, and Fagin and the Artful Dodger are the real stars.  Twist is merely a MacGuffin', TIMESTAMP '2015-08-04 16:11:39.991'),
('Comment(113)', NULL, NULL, 113, 'FIC-101-003', 'Ben Gunn', 'bgunn@marooned.org', 'Not enough cheese to be of interest.', TIMESTAMP '2015-08-04 16:11:40.943'),
('Comment(114)', NULL, NULL, 114, 'FIC-101-003', 'R. L. Stevenson', 'rls@nonesuch.org', 'Not to disparage my thought child, but Ben has an unhealthy focus on cheese, and his opinions on 19th century fiction should be discounted.', TIMESTAMP '2015-08-04 16:11:41.877'),
('Comment(115)', NULL, NULL, 115, 'FIC-101-003', 'Jack', 'jtd@nonesuch.org', 'Dickens presents a richly realized vision of London, and a vast cast of memorable characters.   Sentimental and contrived by modern standards, but still worth a read.', TIMESTAMP '2015-08-04 16:11:42.827'),
('Comment(116)', NULL, NULL, 116, 'FIC-101-004', 'Sydney Carton', 'Carton@nonesuch.org', 'T''is a far far better thing than I have ever read.', TIMESTAMP '2015-08-04 16:11:43.634'),
('Comment(117)', NULL, NULL, 117, 'FIC-101-005', 'Augustus Snodgrass', 'snod@grass.org', 'Too humorous for one of a serious poetic disposition.', TIMESTAMP '2015-08-04 16:11:44.653'),
('Comment(118)', NULL, NULL, 118, 'FIC-101-005', 'Sam Weller', 'samuel.weller@noresuch.org', 'En ''onest good read.', TIMESTAMP '2015-08-04 16:11:45.479'),
('Comment(119)', NULL, NULL, 119, 'SOC-102-001', 'Jacob Riis', 'jacob.riis@noresuch.org', 'A harrowing and copiously documented survey of the grievous state of the lower classes in Victorian London.', TIMESTAMP '2015-08-04 16:11:46.634'),
('Comment(120)', NULL, NULL, 120, 'SOC-102-001', 'Sam Weller', 'samuel.weller@noresuch.org', 'A must read if you ever wanted to know what a mud lark or a mushfaker did.', TIMESTAMP '2015-08-04 16:11:47.636'),
('Comment(121)', NULL, NULL, 121, 'FIC-104-001', 'Dionysius Lardner', 'professor.lardner@univdublin.er.edu', 'The definitive survey of mid-European peoples authored during the dark ages.', TIMESTAMP '2015-08-04 16:11:48.549'),
('Comment(122)', NULL, NULL, 122, 'FIC-104-002', 'Bloodgood Cutter', 'farmerpoet@nonesuch.com', 'A more happy ending that the Orpheus myth, but found the middle English rough going and the rhyme and meter inferior to my own work.', TIMESTAMP '2015-08-04 16:11:50.074'),
('Comment(123)', NULL, NULL, 123, 'FIC-105-001', 'Richard Rogers', 'richard.rogers@nonesuch.com', 'Inspiring!', TIMESTAMP '2015-08-04 16:11:50.845'),
('Comment(124)', NULL, NULL, 124, 'FIC-105-002', 'Sam Travis', 'samuel.travis@nonesuch.com', 'Could have been twice as long and not have covered all the glory that was the Republic of Texas.', TIMESTAMP '2015-08-04 16:11:52.393');             
INSERT INTO PUBLIC.COMMENT(___ID___, ___CLASS___, ___ETAG___, ID, BOOKID, CRITICNAME, CRITICEMAIL, COMMENT, POSTED) VALUES
('Comment(125)', NULL, NULL, 125, 'FIC-105-002', 'Ima Hogg', 'firstlady@nonesuch.tx', 'Weak on artistic merit, but a subject close to my heart.', TIMESTAMP '2015-08-04 16:11:53.211'),
('Comment(126)', NULL, NULL, 126, 'FIC-105-003', 'Horatio Hornblower', 'hornblower@hms.sutherland.org', 'So much print wasted on a mere bay.', TIMESTAMP '2015-08-04 16:11:54.135');     
CREATE MEMORY TABLE PUBLIC.AUTHOR(
    ___ID___ VARCHAR NOT NULL,
    ___CLASS___ VARCHAR,
    ___ETAG___ VARCHAR,
    ID VARCHAR NOT NULL,
    NAME VARCHAR(64) NOT NULL,
    BIO VARCHAR,
    DOB DATE,
    DOD DATE
);            
ALTER TABLE PUBLIC.AUTHOR ADD CONSTRAINT PUBLIC.CONSTRAINT_7 PRIMARY KEY(ID); 
-- 6 +/- SELECT COUNT(*) FROM PUBLIC.AUTHOR;  
INSERT INTO PUBLIC.AUTHOR(___ID___, ___CLASS___, ___ETAG___, ID, NAME, BIO, DOB, DOD) VALUES
('Author(100)', NULL, NULL, '100', 'Dionysius Lardner', '19th Century Irish scientific writer who edited the 133-volume Cabinet Cyclopaedia.', DATE '1793-04-03', DATE '1859-04-29'),
('Author(101)', NULL, NULL, '101', 'Charles Dickens', 'Preminent British novelist of the Victorian era.', DATE '1812-02-07', DATE '1870-06-09'),
('Author(102)', NULL, NULL, '102', 'Henry Mayhew', '19th century urban commentator and social critic.', NULL, NULL),
('Author(103)', NULL, NULL, '103', 'Julius George Alfred Payder IV', NULL, DATE '1956-07-04', NULL),
('Author(104)', NULL, NULL, '104', 'Anonymous', 'Formerly the most prolific author of all.', NULL, NULL),
('Author(105)', NULL, NULL, '105', 'James A. Michener', 'Prolific American author of numerous quasi-historic sagas.', DATE '1903-02-03', DATE '1997-10-16');    
CREATE MEMORY TABLE PUBLIC.WORKTEAM(
    ___ID___ VARCHAR NOT NULL,
    ___CLASS___ VARCHAR,
    ___ETAG___ VARCHAR,
    TEAMNAME VARCHAR NOT NULL,
    DESCRIPTION VARCHAR
);          
ALTER TABLE PUBLIC.WORKTEAM ADD CONSTRAINT PUBLIC.CONSTRAINT_30 PRIMARY KEY(TEAMNAME);        
-- 3 +/- SELECT COUNT(*) FROM PUBLIC.WORKTEAM;
INSERT INTO PUBLIC.WORKTEAM(___ID___, ___CLASS___, ___ETAG___, TEAMNAME, DESCRIPTION) VALUES
('WorkTeam(Historians)', NULL, NULL, 'Historians', 'Staff who are acolytes of Clio.'),
('WorkTeam(Management)', NULL, NULL, 'Management', 'Management Level staff.'),
('WorkTeam(Associates)', NULL, NULL, 'Associates', 'Non-Management staff.');            
CREATE MEMORY TABLE PUBLIC.ORDERPROPERTY(
    ___ID___ VARCHAR NOT NULL,
    ___CLASS___ VARCHAR,
    ___ETAG___ VARCHAR,
    NAME VARCHAR(256) NOT NULL,
    DATATYPE VARCHAR(256),
    DESCRIPTION VARCHAR(4096),
    DEFAULTVALUE VARCHAR(256),
    POSSIBLEVALUES VARCHAR(4096),
    PRODUCERMAPPINGPROPERTY VARCHAR(256),
    ENTITYNAME VARCHAR(256)
);      
ALTER TABLE PUBLIC.ORDERPROPERTY ADD CONSTRAINT PUBLIC.CONSTRAINT_E7 PRIMARY KEY(NAME);       
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.ORDERPROPERTY;           
CREATE MEMORY TABLE PUBLIC.INVENTORY(
    ___ID___ VARCHAR NOT NULL,
    ___CLASS___ VARCHAR,
    ___ETAG___ VARCHAR,
    ID VARCHAR(16) NOT NULL,
    PRICE REAL,
    INSTOCK INT DEFAULT 0,
    ONORDER INT DEFAULT 0
);            
ALTER TABLE PUBLIC.INVENTORY ADD CONSTRAINT PUBLIC.CONSTRAINT_2 PRIMARY KEY(ID);              
-- 12 +/- SELECT COUNT(*) FROM PUBLIC.INVENTORY;              
INSERT INTO PUBLIC.INVENTORY(___ID___, ___CLASS___, ___ETAG___, ID, PRICE, INSTOCK, ONORDER) VALUES
('Inventory(REF-100-001)', NULL, NULL, 'REF-100-001', NULL, 0, 0),
('Inventory(FIC-101-002)', NULL, NULL, 'FIC-101-002', 19.95, 4, 0),
('Inventory(FIC-101-003)', NULL, NULL, 'FIC-101-003', 14.95, 0, 10),
('Inventory(FIC-101-004)', NULL, NULL, 'FIC-101-004', 19.5, 1, 6),
('Inventory(FIC-101-005)', NULL, NULL, 'FIC-101-005', 19.95, 6, 0),
('Inventory(SOC-102-001)', NULL, NULL, 'SOC-102-001', 49.95, 2, 0),
('Inventory(FIC-104-001)', NULL, NULL, 'FIC-104-001', 39.95, 1, 0),
('Inventory(FIC-104-002)', NULL, NULL, 'FIC-104-002', 5.99, 3, 0),
('Inventory(FIC-105-001)', NULL, NULL, 'FIC-105-001', 24.95, 3, 0),
('Inventory(FIC-105-002)', NULL, NULL, 'FIC-105-002', 15.0, 50, 0),
('Inventory(FIC-105-003)', NULL, NULL, 'FIC-105-003', 24.95, 1, 5),
('Inventory(FIC-101-001)', NULL, NULL, 'FIC-101-001', 9.99, 6, 0);  
CREATE MEMORY TABLE PUBLIC.LINEITEM(
    ___ID___ VARCHAR NOT NULL,
    ___CLASS___ VARCHAR,
    ___ETAG___ VARCHAR,
    ORDERNUMBER VARCHAR NOT NULL,
    LINENUMBER INT NOT NULL,
    ITEMID VARCHAR NOT NULL,
    PRICESOLD DOUBLE,
    QUANTITY INT NOT NULL
);  
ALTER TABLE PUBLIC.LINEITEM ADD CONSTRAINT PUBLIC.CONSTRAINT_75 PRIMARY KEY(ORDERNUMBER, LINENUMBER);         
-- 3 +/- SELECT COUNT(*) FROM PUBLIC.LINEITEM;
INSERT INTO PUBLIC.LINEITEM(___ID___, ___CLASS___, ___ETAG___, ORDERNUMBER, LINENUMBER, ITEMID, PRICESOLD, QUANTITY) VALUES
('LineItem(ORD00139,200)', NULL, NULL, 'ORD00139', 200, 'FIC-101-001', 16.5, 2),
('LineItem(ORD00139,201)', NULL, NULL, 'ORD00139', 201, 'FIC-101-003', 18.5, 1),
('LineItem(ORD00140,200)', NULL, NULL, 'ORD00140', 200, 'FIC-101-001', 16.5, 3);             
ALTER TABLE PUBLIC.LINEITEM ADD CONSTRAINT PUBLIC.CONSTRAINT_75890 FOREIGN KEY(ORDERNUMBER) REFERENCES PUBLIC.CUSTORDER(ID) NOCHECK;          
ALTER TABLE PUBLIC.MEMBERSHIP ADD CONSTRAINT PUBLIC.CONSTRAINT_CD FOREIGN KEY(EMPLOYEE) REFERENCES PUBLIC.PERSONNEL(ID) NOCHECK;              
ALTER TABLE PUBLIC.PERSONNEL ADD CONSTRAINT PUBLIC.CONSTRAINT_D8F FOREIGN KEY(SUPERVISORID) REFERENCES PUBLIC.PERSONNEL(ID) NOCHECK;          
ALTER TABLE PUBLIC.INVENTORY ADD CONSTRAINT PUBLIC.CONSTRAINT_2D FOREIGN KEY(ID) REFERENCES PUBLIC.BOOK(ID) NOCHECK;          
ALTER TABLE PUBLIC.LINEITEM ADD CONSTRAINT PUBLIC.CONSTRAINT_7589 FOREIGN KEY(ITEMID) REFERENCES PUBLIC.BOOK(ID) NOCHECK;     
ALTER TABLE PUBLIC.PERSONNEL ADD CONSTRAINT PUBLIC.CONSTRAINT_D8F2 FOREIGN KEY(SUPERVISORID) REFERENCES PUBLIC.PERSONNEL(ID) NOCHECK;         
ALTER TABLE PUBLIC.LINEITEM ADD CONSTRAINT PUBLIC.CONSTRAINT_758 FOREIGN KEY(ITEMID) REFERENCES PUBLIC.INVENTORY(ID) NOCHECK; 
ALTER TABLE PUBLIC.MEMBERSHIP ADD CONSTRAINT PUBLIC.CONSTRAINT_CD0 FOREIGN KEY(TEAM) REFERENCES PUBLIC.WORKTEAM(TEAMNAME) NOCHECK;            
ALTER TABLE PUBLIC.CUSTORDER ADD CONSTRAINT PUBLIC.CONSTRAINT_62 FOREIGN KEY(CUSTOMERID) REFERENCES PUBLIC.CUSTOMER(ID) NOCHECK;              
ALTER TABLE PUBLIC.COMMENT ADD CONSTRAINT PUBLIC.CONSTRAINT_637 FOREIGN KEY(BOOKID) REFERENCES PUBLIC.BOOK(ID) NOCHECK;       
ALTER TABLE PUBLIC.BOOK ADD CONSTRAINT PUBLIC.CONSTRAINT_1F FOREIGN KEY(AUTHORID) REFERENCES PUBLIC.AUTHOR(ID) NOCHECK;       
