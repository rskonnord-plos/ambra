alter table article
  modify column doi varchar(150) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  modify column eLocationID varchar(150) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL;

alter table articleAsset
  modify column doi varchar(150) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL;

update annotation set type = 'Comment', title = concat("Publisher's Note:", title) where type = 'MinorCorrection';