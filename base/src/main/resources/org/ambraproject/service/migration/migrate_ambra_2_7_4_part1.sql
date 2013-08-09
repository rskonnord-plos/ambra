create table articleCategory (
  articleCategoryID bigint not null auto_increment,
  displayName varchar(255) CHARACTER SET utf8 COLLATE utf8_bin default null,
  journalID bigint default null,
  journalSortOrder int(11) default null,
  created datetime not null,
  lastModified datetime not null,
  PRIMARY KEY (articleCategoryID),
  constraint unique key (journalID, displayName),
  constraint foreign key (journalID) references journal (journalID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table articleCategoryList (
  articleCategoryID bigint not null,
  sortOrder int(11) not null,
  doi varchar(255) CHARACTER SET utf8 COLLATE utf8_bin null,
  PRIMARY KEY (articleCategoryID, sortOrder),
  constraint foreign key (articleCategoryID) references articleCategory (articleCategoryID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
