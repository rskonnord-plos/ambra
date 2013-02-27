alter table savedSearchParams
  modify column hash varchar(25) character set utf8 collate utf8_bin not null,
  add index (hash),
  add unique key(hash);
