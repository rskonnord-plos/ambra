create table news (
  newsID bigint not null auto_increment,
  displayName varchar(255) CHARACTER SET utf8 COLLATE utf8_bin default null,
  journalID bigint not null,
  created datetime null,
  lastModified datetime null,
  PRIMARY KEY (newsID, journalID),
  constraint foreign key (journalID) references journal (journalID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table newsArticleList (
  newsID bigint not null,
  articleID bigint not null,
  sortOrder int(11) not null,
  PRIMARY KEY (newsID, articleID, sortOrder),
  constraint foreign key (newsID) references news (newsID),
  constraint foreign key (articleID) references article (articleID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
