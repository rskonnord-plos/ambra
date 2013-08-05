create table news (
  sortOrder integer not null default 0,
  articleID bigint not null default 0,
  primary key (sortOrder),
  constraint foreign key (articleID) references article(articleID),
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
