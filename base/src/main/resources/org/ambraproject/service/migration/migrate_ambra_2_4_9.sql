ALTER TABLE article ADD COLUMN strkImgURI VARCHAR(50) CHARACTER SET utf8 COLLATE utf8_bin null after url;
ALTER TABLE annotation ADD COLUMN highlightedText TEXT CHARACTER SET utf8 COLLATE utf8_bin null after competingInterestBody;

alter table pingback change column url url varchar(255) CHARACTER SET utf8 COLLATE utf8_bin not null,
  change column title title varchar(255) CHARACTER SET utf8 COLLATE utf8_bin null,
  change column created created datetime not null after title,
  change column lastModified lastModified datetime null after created;

DELIMITER $$
DROP PROCEDURE IF EXISTS MaxFigTab$$
CREATE PROCEDURE MaxFigTab(
       IN ID INT, fld VARCHAR(50),
       OUT figDoi VARCHAR(50))
    BEGIN
       DECLARE CONTINUE HANDLER FOR NOT FOUND
           SET figDOi = NULL;

       SELECT doi
       INTO figDoi
       FROM articleAsset
       WHERE articleID = ID
       AND
       contextElement = fld
       order by sortorder desc limit 1;
    END$$

 DROP PROCEDURE IF EXISTS UpdateStrkImage$$
 CREATE PROCEDURE UpdateStrkImage()
 BEGIN
    DECLARE  no_more_articles INT DEFAULT 0;
    DECLARE  fDoi VARCHAR(50) DEFAULT NULL;
    DECLARE  article_id int;
    DECLARE  cur_article CURSOR FOR
               SELECT  articleID FROM article WHERE strkImgURI IS NULL;
    DECLARE  CONTINUE HANDLER FOR NOT FOUND
               SET  no_more_articles = 1;

    OPEN  cur_article;
    FETCH  cur_article INTO article_id;
    REPEAT
       CALL MaxFigTab(article_id,'fig', @fDoi);
       IF @fDoi IS NULL THEN
          SET no_more_articles = 0;
       END IF;
       IF @fDoi IS NOT NULL THEN
          UPDATE article Set strkImgURI = @fDOI WHERE articleID = article_id;
       END IF;
       FETCH  cur_article INTO article_id;
       UNTIL  no_more_articles = 1
    END REPEAT;
    CLOSE  cur_article;

    OPEN  cur_article;
    FETCH  cur_article INTO article_id;
    REPEAT
       CALL MaxFigTab(article_id, 'table-wrap', @fDoi);
       IF @fDoi IS NULL THEN
          SET no_more_articles = 0;
       END IF;
       IF @fDoi IS NOT NULL THEN
          UPDATE article Set strkImgURI = @fDOI WHERE articleID = article_id;
       END IF;
       FETCH  cur_article INTO article_id;
       UNTIL  no_more_articles = 1
    END REPEAT;
    CLOSE  cur_article;
 END$$
DELIMITER ;

CALL UpdateStrkImage();
DROP PROCEDURE IF EXISTS MaxFigTab;
DROP PROCEDURE IF EXISTS UpdateStrkImage;
