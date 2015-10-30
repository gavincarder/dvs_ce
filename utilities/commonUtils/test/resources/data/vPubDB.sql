;             
CREATE USER IF NOT EXISTS TEST SALT '429f57f434be6c00' HASH 'c5c2b527a30e2110c029d98b07a291bbbcb35f8aa475a7820320ac3a7937f329' ADMIN;         
CREATE SEQUENCE PUBLIC.ODME_SEQNUM START WITH 100;            
CREATE MEMORY TABLE PUBLIC.BEER(
    ID VARCHAR(64) NOT NULL,
    BREWER_ID VARCHAR(64),
    NAME VARCHAR(255),
    TYPE VARCHAR(255),
    ABV DOUBLE,
    SERVING VARCHAR(255),
    PRICE DOUBLE,
    DESCRIPTION VARCHAR(512)
);   
ALTER TABLE PUBLIC.BEER ADD CONSTRAINT PUBLIC.CONSTRAINT_1 PRIMARY KEY(ID);   
-- 21 +/- SELECT COUNT(*) FROM PUBLIC.BEER;   
INSERT INTO PUBLIC.BEER(ID, BREWER_ID, NAME, TYPE, ABV, SERVING, PRICE, DESCRIPTION) VALUES
('f598195e-b6ee-4790-a96a-f12a3ca5c3cf', '31442b00-47c4-11e3-8f96-0800200c9a66', '21st Ammendment Hop Crisis!', 'IPA', 9.7, '12 oz. Draft', 8.0, 'Hop Crisis. Crisis? What Crisis? A few years ago, when hop prices shot through the roof and the worldwide hop market went into a tailspin, at our pub in San Francisco we decided there was only one thing for us to do. We made the biggest, hoppiest IPA we could imagine and aged it on oak for good measure. This Imperial IPA breaks all the rules with more malt, more hops and more aroma.'),
('6f11fe00-d22a-46f0-887a-e4c13a90cfcd', '31442b01-47c4-11e3-8f96-0800200c9a66', 'Avery Karma', 'Ale', 5.2, '13 oz. Draft', 6.0, 'Karma barrel-aged with Brett from Drie Fonteinen.'),
('f8c4f7f5-77de-4c74-8c08-79b3f025b787', '31442b06-47c4-11e3-8f96-0800200c9a66', 'Delirium Tremens', 'Blond', 8.5, '13 oz. Draft', 9.0, 'The allusion to the pink elephant a consequence of a delirium tremens the day before, is not by chance.'),
('a71af791-090a-42fa-bc26-eb6708e3e720', '31442b07-47c4-11e3-8f96-0800200c9a66', 'Gulden Draak', 'Barley Wine', 10.5, '13 oz. Draft', 9.0, 'Gulden Draak is a beer in a class of its own. It is a beer that is so rich, so glowing, so full of its very own characteristic flavor, that it reminds some who try it of chocolate and others of coffee. The English call this type of beer a Barley Wine. Gulden Draak (Golden Dragon) smells of triumph. It is a definite party beer. It is no wonder that Gulden Draak was crowned best beer in the world by the American Tasting Institute in 1998! This beer won many other awards as well.'),
('06ac5f5c-d1f8-445a-a9b7-f3d94b45ac45', '31442b02-47c4-11e3-8f96-0800200c9a66', 'Bavik Pils', 'Pilsner', 5.0, '16 oz. Draft', 7.0, 'A light flavored beer, smooth and malty. Clean finish, with a light malt aftertaste.'),
('f3d0f8cb-ac35-418c-97db-ed6f81d17eb6', '31442b03-47c4-11e3-8f96-0800200c9a66', 'Breckenridge Trademark Pale Ale', 'IPA', 5.7, '16 oz. Draft', 7.0, 'We craft this American pale ale with hearty amounts of pale and Munich malts, then balance it with lofty amounts of hops throughout. It''s a finessed version of hop-head fun -- a black diamond beer without the bumps and dangerous curves.'),
('2555fca3-c48f-44f1-9e70-abefdcbd3d3d', '31442b04-47c4-11e3-8f96-0800200c9a66', 'Brooklyn Pilsner', 'Pilsner', 5.0, '16 oz. Draft', 6.5, 'Brooklyn Pilsner is a refreshing golden lager beer, brewed in the style favored by New York"s pre-Prohibition brewers. In the 1840"s, the pilsner style emerged from central Europe to become the world"s most popular style of beer.'),
('723dfa6d-3168-4f29-adc3-e0383a570249', '31442b05-47c4-11e3-8f96-0800200c9a66', 'Witterkerke', 'Witbier', 5.0, '16 oz. Draft', 7.0, 'Features the nutty quench of a wheat ale combined with the delightful aromatics and subtle flavor contributed by Maine wild blueberries.'),
('e4059176-8aa8-493b-9736-96594c75d9a8', '31442b08-47c4-11e3-8f96-0800200c9a66', 'Captain Lawrence Captain''s Kolsch', 'Kolsch', 5.0, '16 oz. Draft', 6.5, 'Low in bitterness, but packed full of malty rich flavors, this beer expresses a small amount of the fruitiness you find in most ales, as well as the clean crisp flavors associated with a lager. The aroma is influenced by the small amount of American grown hops we add post fermentation which contribute a touch citrus.'),
('07349198-f15d-4602-81d7-ed41afbdadd4', '31442b09-47c4-11e3-8f96-0800200c9a66', 'Coronado Mermaid''s Red Ale', 'Ale', 5.7, '16 oz. Draft', 7.0, STRINGDECODE('Mermaid\u2019s Red\u2122 bucks the shallow mid-range red ale stereotype. Loaded with Cascade hops, it delivers a fresh floral aroma and sharp bitter notes, all while delivering a solid kick of roasted malts that fades seamlessly into a rich, chocolaty finish with hints of clove and caramel.')),
('2e747d7d-e636-4def-848d-2de1764fb63f', '31442b09-47c4-11e3-8f96-0800200c9a66', 'Coronado Blue Bridge Coffee Stout', 'Stout', 6.2, '16 oz. Draft', 7.0, STRINGDECODE('Just as the iconic Coronado Bay Bridge connects our island home to the mainland, this java-tinged dry stout, brewed using dark roasted coffee beans from local San Diego artisanal roasters Caf? Moto, provides a bridge uniting craft beer drinkers with CBC\u2019s rich, flavorful SoCal brewing traditions.'));            
INSERT INTO PUBLIC.BEER(ID, BREWER_ID, NAME, TYPE, ABV, SERVING, PRICE, DESCRIPTION) VALUES
('96648a2a-6bff-4a46-a8fc-faa40b69d811', '31442b09-47c4-11e3-8f96-0800200c9a66', 'Coronado Orange Avenue Wit', 'Witbier', 5.7, '16 oz. Draft', 7.0, STRINGDECODE('This So-Cal take on a traditional witbier honors Coronado\u2019s main street, which is home to our brewpub and was once lined with orange trees. Bolstered by orange zest, coriander and orange blossom honey, its Belgium by way of Coronado. Expect a refreshing, light-bodied brew rife with citrus zing and a hint of earthy spice')),
('97e15479-2500-4ec7-9b2c-d057823baf14', '31442b10-47c4-11e3-8f96-0800200c9a66', 'Estrella Damn', 'Lager', 4.6, '16 oz. Draft', 7.0, 'This beer is the jewel in the crown of Damm"s century-long beer-brewing experience. Since 1876, when Alsatian August Kuentzmann Damm founded the company bearing his name, numerous generations of master brewers have perfected this lager until it has become a landmark in the world of beers.'),
('1ca08797-c35f-4203-bd6f-7591309d8013', '31442b11-47c4-11e3-8f96-0800200c9a66', 'Goose Island Honkers Ale', 'Ale', 5.0, '16 oz. Draft', 6.5, 'Inspired by visits to English country pubs, Honkers Ale combines a spicy hop aroma with a rich malt middle to create a perfectly balanced ale that is immensely drinkable. A smooth, drinkable English Bitter for those looking for more from their beer.'),
('3a9f7292-a2ab-493b-8833-813ece77a04d', '31442b12-47c4-11e3-8f96-0800200c9a66', 'Left Hand Milk Stout Nitro', 'Stout', 6.0, '16 oz. Draft', 6.5, STRINGDECODE('Dark and delicious, America\u2019s great milk stout will change your perception about what a stout can be.')),
('3788e89d-a396-47d1-b486-59cfed68db46', '31442b14-47c4-11e3-8f96-0800200c9a66', 'Nebraska Brewing Company Brunette Nut Brown', 'Ale', 4.8, '16 oz. Draft', 7.0, 'This medium-bodied English-style brown ale is all about the malt. It has a blend of six different malts and a low hop character. Moderate malty sweetness with slight nutty/caramelly flavor and aroma. Smooth, malty and easy to drink! Light beer drinkers often like this beer after they get past the fear of its color.'),
('c8712d3f-c411-4d45-99cb-eb63cf65b1d0', '31442b15-47c4-11e3-8f96-0800200c9a66', 'Paulaner Hefeweizen', 'Hefeweizen', 5.5, '16 oz. Draft', 7.0, 'High in effervescence and low in calories, with a crisp, refreshing fruity flavor.'),
('64158994-03d4-450d-98de-5268737b52b3', '31442b16-47c4-11e3-8f96-0800200c9a66', 'Peak Organic Summer Session', 'IPA', 5.0, '16 oz. Draft', 6.5, STRINGDECODE('A traditional summer wheat beer marries a West Coast pale ale. Locally grown wheat provides a complex mouthfeel and Amarillo\u00ae dry hopping gives a citrusy aroma.')),
('470c36af-a543-4b80-81c1-79a5437b1206', '31442b17-47c4-11e3-8f96-0800200c9a66', 'Sea Dog Blue Paw Blueberry', 'Hefeweizen', 4.6, '16 oz. Draft', 6.0, 'Features the nutty quench of a wheat ale combined with the delightful aromatics and subtle flavor contributed by Maine wild blueberries.'),
('b49f1022-2060-411d-806e-27daf465c8d6', '31442b18-47c4-11e3-8f96-0800200c9a66', 'Guinness', 'Stout', 5.0, '16 oz. Draft', 6.0, 'Inspired by visits to English country pubs, Honkers Ale combines a spicy hop aroma with a rich malt middle to create a perfectly balanced ale that is immensely drinkable. A smooth, drinkable English Bitter for those looking for more from their beer.'),
('1b0095d3-d09f-4182-b078-6525e17dd662', '31442b09-47c4-11e3-8f96-0800200c9a66', 'Long Ireland IPA', 'IPA', 7.3, '16 oz. Draft', 6.5, 'American IPA.');          
CREATE MEMORY TABLE PUBLIC.BREWERPROPERTY(
    NAME VARCHAR(256) NOT NULL,
    DATATYPE VARCHAR(256),
    DESCRIPTION VARCHAR(4096),
    DEFAULTVALUE VARCHAR(256),
    POSSIBLEVALUES VARCHAR(4096),
    PRODUCERMAPPINGPROPERTY VARCHAR(256),
    ENTITYNAME VARCHAR(256)
);        
ALTER TABLE PUBLIC.BREWERPROPERTY ADD CONSTRAINT PUBLIC.CONSTRAINT_9 PRIMARY KEY(NAME);       
-- 2 +/- SELECT COUNT(*) FROM PUBLIC.BREWERPROPERTY;          
INSERT INTO PUBLIC.BREWERPROPERTY(NAME, DATATYPE, DESCRIPTION, DEFAULTVALUE, POSSIBLEVALUES, PRODUCERMAPPINGPROPERTY, ENTITYNAME) VALUES
('BrewerCP1', 'Edm.String', 'My Brewer CP1', 'CABrewer1', NULL, 'CP1', NULL),
('BrewerCP2', 'Edm.String', 'My Brewer CP2', 'CABrewer2', NULL, 'CP2', NULL);        
CREATE MEMORY TABLE PUBLIC.CLASS(
    ID VARCHAR(2048) NOT NULL,
    NAME VARCHAR(2048),
    DESC VARCHAR(2048)
);        
ALTER TABLE PUBLIC.CLASS ADD CONSTRAINT PUBLIC.CONSTRAINT_3 PRIMARY KEY(ID);  
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.CLASS;   
CREATE MEMORY TABLE PUBLIC.STYLE(
    NAME VARCHAR(64) NOT NULL,
    DESCRIPTION VARCHAR(255)
);           
ALTER TABLE PUBLIC.STYLE ADD CONSTRAINT PUBLIC.CONSTRAINT_4 PRIMARY KEY(NAME);
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.STYLE;   
CREATE MEMORY TABLE PUBLIC.COMMENT(
    ID VARCHAR(64) NOT NULL,
    REFERENCED_RESOURCE_ID VARCHAR(64),
    COMMENTER_NAME VARCHAR(255),
    COMMENTER_EMAIL VARCHAR(255),
    COMMENT VARCHAR(255)
); 
ALTER TABLE PUBLIC.COMMENT ADD CONSTRAINT PUBLIC.CONSTRAINT_6 PRIMARY KEY(ID);
-- 23 +/- SELECT COUNT(*) FROM PUBLIC.COMMENT;
INSERT INTO PUBLIC.COMMENT(ID, REFERENCED_RESOURCE_ID, COMMENTER_NAME, COMMENTER_EMAIL, COMMENT) VALUES
('31442b20-47c4-11e3-8f96-0800200c9a66', 'f598195e-b6ee-4790-a96a-f12a3ca5c3cf', 'Bob', 'bob@bmail.com', 'Very tasty!'),
('31442b30-47c4-11e3-8f96-0800200c9a66', '6f11fe00-d22a-46f0-887a-e4c13a90cfcd', 'Charlie', 'charles@bmail.com', 'A nice, mellow brew.'),
('31442b40-47c4-11e3-8f96-0800200c9a66', '06ac5f5c-d1f8-445a-a9b7-f3d94b45ac45', 'Diane', 'di@bmail.com', 'Light but not meak.'),
('31442b50-47c4-11e3-8f96-0800200c9a66', 'f3d0f8cb-ac35-418c-97db-ed6f81d17eb6', 'Ed', 'edward@bmail.com', 'Has quite a kick.'),
('31442b60-47c4-11e3-8f96-0800200c9a66', '2555fca3-c48f-44f1-9e70-abefdcbd3d3d', 'Faye', 'faye@bmail.com', 'It''s not safe to drink the water but the beer is great.'),
('31442b70-47c4-11e3-8f96-0800200c9a66', '723dfa6d-3168-4f29-adc3-e0383a570249', 'Grace', 'grace@bmail.com', 'Interesting flavor.'),
('31442b80-47c4-11e3-8f96-0800200c9a66', 'f8c4f7f5-77de-4c74-8c08-79b3f025b787', 'Harry', 'harold@bmail.com', 'Powerful stuff.'),
('31442b90-47c4-11e3-8f96-0800200c9a66', 'a71af791-090a-42fa-bc26-eb6708e3e720', 'Ian', 'ian@bmail.com', 'Worth every penny.'),
('31442c10-47c4-11e3-8f96-0800200c9a66', 'e4059176-8aa8-493b-9736-96594c75d9a8', 'Jane', 'jane@bmail.com', 'Just what you expect from Captain Lawrence.'),
('31442c20-47c4-11e3-8f96-0800200c9a66', '07349198-f15d-4602-81d7-ed41afbdadd4', 'Kathy', 'kate@bmail.com', 'Great taste and color.'),
('31442c30-47c4-11e3-8f96-0800200c9a66', '2e747d7d-e636-4def-848d-2de1764fb63f', 'Larry', 'larry@bmail.com', 'Strong taste but not overpowering.'),
('31442c40-47c4-11e3-8f96-0800200c9a66', '96648a2a-6bff-4a46-a8fc-faa40b69d811', 'Mark', 'mark@bmail.com', 'Perfect on hot summer''s day.'),
('31442c50-47c4-11e3-8f96-0800200c9a66', '97e15479-2500-4ec7-9b2c-d057823baf14', 'Nancy', 'nancy@bmail.com', 'The name says it all.'),
('31442c60-47c4-11e3-8f96-0800200c9a66', '1ca08797-c35f-4203-bd6f-7591309d8013', 'Olivia', 'olivia@bmail.com', 'One of my top 10 favorites.'),
('31442c70-47c4-11e3-8f96-0800200c9a66', '3a9f7292-a2ab-493b-8833-813ece77a04d', 'Pete', 'peter@bmail.com', 'They weren''t kidding about the "Nitro".'),
('31442c80-47c4-11e3-8f96-0800200c9a66', '1b0095d3-d09f-4182-b078-6525e17dd662', 'Quinn', 'quinn@bmail.com', 'Best local brew around.'),
('31442c90-47c4-11e3-8f96-0800200c9a66', '3788e89d-a396-47d1-b486-59cfed68db46', 'Rebecca', 'becky@bmail.com', 'Apparently they also know about beer out there.'),
('31442d10-47c4-11e3-8f96-0800200c9a66', 'c8712d3f-c411-4d45-99cb-eb63cf65b1d0', 'Stan', 'stanley@bmail.com', 'Nothing special.'),
('31442d20-47c4-11e3-8f96-0800200c9a66', '64158994-03d4-450d-98de-5268737b52b3', 'Tina', 'tina@bmail.com', 'Excellent!'),
('31442d30-47c4-11e3-8f96-0800200c9a66', '470c36af-a543-4b80-81c1-79a5437b1206', 'Uri', 'uri@bmail.com', 'Man Law: No fruit in beer.'),
('31442d40-47c4-11e3-8f96-0800200c9a66', 'b49f1022-2060-411d-806e-27daf465c8d6', 'Vicki', 'vicki@bmail.com', 'It''s not just for breakfast.'),
('31442d41-47c4-11e3-8f96-0800200c9a66', 'b49f1022-2060-411d-806e-27daf465c8d6', 'Wally', 'walter @bmail.com', 'Always good.'),
('31442d42-47c4-11e3-8f96-0800200c9a66', 'b49f1022-2060-411d-806e-27daf465c8d6', 'Yves', 'yves@bmail.com', 'Reliable standby.');        
CREATE MEMORY TABLE PUBLIC.BREWER(
    ID VARCHAR(64) NOT NULL,
    NAME VARCHAR(255),
    DESCRIPTION VARCHAR(255),
    CREATIONTIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    INITTIMESTAMP TIMESTAMP DEFAULT '2014-11-10 10:32:02.56',
    CREATOR VARCHAR(255) DEFAULT 'casd',
    CREATORID INT DEFAULT 10,
    CP1 VARCHAR(256) DEFAULT 'CABrewer1',
    CP2 VARCHAR(256) DEFAULT 'CABrewer2'
);        
ALTER TABLE PUBLIC.BREWER ADD CONSTRAINT PUBLIC.CONSTRAINT_7 PRIMARY KEY(ID); 
-- 19 +/- SELECT COUNT(*) FROM PUBLIC.BREWER; 
INSERT INTO PUBLIC.BREWER(ID, NAME, DESCRIPTION, CREATIONTIMESTAMP, INITTIMESTAMP, CREATOR, CREATORID, CP1, CP2) VALUES
('31442b00-47c4-11e3-8f96-0800200c9a66', '21st Amendment Brewery', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b01-47c4-11e3-8f96-0800200c9a66', 'Avery Brewing Company', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b02-47c4-11e3-8f96-0800200c9a66', 'Bavik-De Brabandere', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b03-47c4-11e3-8f96-0800200c9a66', 'Breckenridge Brewing', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b04-47c4-11e3-8f96-0800200c9a66', 'Brooklyn Brewery', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b05-47c4-11e3-8f96-0800200c9a66', 'Brouwerij Bavik', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b06-47c4-11e3-8f96-0800200c9a66', 'Brouwerij Huyghe', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b07-47c4-11e3-8f96-0800200c9a66', 'Brouwerij Van Steenberge', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b08-47c4-11e3-8f96-0800200c9a66', 'Captain Lawrence Brewing Company', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b09-47c4-11e3-8f96-0800200c9a66', 'Coronado Brewing Company', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b10-47c4-11e3-8f96-0800200c9a66', 'Damn', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b11-47c4-11e3-8f96-0800200c9a66', 'Goose Island Beer Company', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b12-47c4-11e3-8f96-0800200c9a66', 'Left Hand Brewing', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b13-47c4-11e3-8f96-0800200c9a66', 'Long Ireland Beer Company', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b14-47c4-11e3-8f96-0800200c9a66', 'Nebraska Brewing Company', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b15-47c4-11e3-8f96-0800200c9a66', 'Paulaner Brauerei', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b16-47c4-11e3-8f96-0800200c9a66', 'Peak Organic Brewing Company', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b17-47c4-11e3-8f96-0800200c9a66', 'Sea Dog Brewing', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'),
('31442b18-47c4-11e3-8f96-0800200c9a66', 'St. James Gate', NULL, TIMESTAMP '2014-12-08 15:58:05.359', TIMESTAMP '2014-11-10 10:32:02.56', 'casd', 10, 'CABrewer1', 'CABrewer2'); 
CREATE MEMORY TABLE PUBLIC.STUDENTPROPERTY(
    NAME VARCHAR(256) NOT NULL,
    DATATYPE VARCHAR(256),
    DESCRIPTION VARCHAR(4096),
    DEFAULTVALUE VARCHAR(256),
    POSSIBLEVALUES VARCHAR(4096),
    PRODUCERMAPPINGPROPERTY VARCHAR(256),
    ENTITYNAME VARCHAR(256)
);       
ALTER TABLE PUBLIC.STUDENTPROPERTY ADD CONSTRAINT PUBLIC.CONSTRAINT_A PRIMARY KEY(NAME);      
-- 1 +/- SELECT COUNT(*) FROM PUBLIC.STUDENTPROPERTY;         
INSERT INTO PUBLIC.STUDENTPROPERTY(NAME, DATATYPE, DESCRIPTION, DEFAULTVALUE, POSSIBLEVALUES, PRODUCERMAPPINGPROPERTY, ENTITYNAME) VALUES
('StudentCP1', 'Edm.String', 'My Student CP1', 'CAStudent', NULL, 'CP1', NULL);    
CREATE MEMORY TABLE PUBLIC.SIGNUP(
    STUDENT_ID VARCHAR(2048) NOT NULL,
    CLASS_ID VARCHAR(2048) NOT NULL
);           
ALTER TABLE PUBLIC.SIGNUP ADD CONSTRAINT PUBLIC.CONSTRAINT_91 PRIMARY KEY(STUDENT_ID, CLASS_ID);              
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.SIGNUP;  
CREATE MEMORY TABLE PUBLIC.STUDENT(
    ID VARCHAR(2048) NOT NULL,
    FIRSTNAME VARCHAR(2048),
    LASTNAME VARCHAR(2048),
    CP1 VARCHAR(256) DEFAULT 'CAStudent'
);  
ALTER TABLE PUBLIC.STUDENT ADD CONSTRAINT PUBLIC.CONSTRAINT_B PRIMARY KEY(ID);
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.STUDENT; 
ALTER TABLE PUBLIC.SIGNUP ADD CONSTRAINT PUBLIC.CONSTRAINT_91C FOREIGN KEY(CLASS_ID) REFERENCES PUBLIC.CLASS(ID) NOCHECK;     
ALTER TABLE PUBLIC.SIGNUP ADD CONSTRAINT PUBLIC.CONSTRAINT_91C8 FOREIGN KEY(STUDENT_ID) REFERENCES PUBLIC.STUDENT(ID) NOCHECK;
ALTER TABLE PUBLIC.BEER ADD CONSTRAINT PUBLIC.CONSTRAINT_1F0 FOREIGN KEY(BREWER_ID) REFERENCES PUBLIC.BREWER(ID) NOCHECK;     
ALTER TABLE PUBLIC.COMMENT ADD CONSTRAINT PUBLIC.CONSTRAINT_63 FOREIGN KEY(REFERENCED_RESOURCE_ID) REFERENCES PUBLIC.BEER(ID) NOCHECK;        
ALTER TABLE PUBLIC.BEER ADD CONSTRAINT PUBLIC.CONSTRAINT_1F FOREIGN KEY(TYPE) REFERENCES PUBLIC.STYLE(NAME) NOCHECK;          
