create table savedSearchParams (
  savedSearchParamsID bigint(20) not null auto_increment,
  searchParams text character set utf8 collate utf8_bin not null,
  hash varchar(25) character set utf8 collate utf8_bin null,
  created datetime not null,
  lastmodified datetime not null,
  primary key (savedSearchParamsID)
) engine=innodb auto_increment=1 default charset=utf8;

alter table savedSearch
  add column savedSearchParamsID bigint(20) null after userProfileID,
  add constraint foreign key (savedSearchParamsID) references savedSearchParams(savedSearchParamsID);

insert into savedSearchParams(searchParams, created, lastmodified)
  select distinct searchParams, now(), now() from savedSearch;

update savedSearch, savedSearchParams
  set savedSearch.savedSearchParamsID = savedSearchParams.savedSearchParamsID
  where savedSearch.searchParams = savedSearchParams.searchParams;

alter table savedSearch
  modify column savedSearchParamsID bigint(20) not null,
  drop column searchParams;
