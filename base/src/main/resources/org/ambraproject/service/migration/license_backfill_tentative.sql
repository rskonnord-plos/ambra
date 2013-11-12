#performance optimization duplicates some information from citedArticleLicense to minimize sql joins via hibernate
alter table citedArticle
    add column citedArticleLicenseID bigint(20),
    add constraint foreign key (citedArticleLicenseID) references citedArticleLicense (citedArticleLicenseID);

#contains common licensing information
create table license (
  licenseID bigint(20),
  title varchar(100) character set utf8 collate utf8_bin,
  version varchar(30) character set utf8 collate utf8_bin,
  family varchar(30) character set utf8 collate utf8_bin,
  description varchar(1500) character set utf8 collate utf8_bin,
  is_okd_compliant bit,
  is_osi_compliant bit,
  url text character set utf8 collate utf8_bin, #ask jLin
  domain_software bit,
  domain_data bit,
  domain_content bit,
  isBY bit,
  isNC bit,
  isND bit,
  isSA bit,
  type varchar(30) character set utf8 collate utf8_bin,
  jurisdiction varchar(30) character set utf8 collate utf8_bin,
  created datetime,
  lastModified datetime,
  primary key (licenseID),
  index (family),
  index (type),
  unique index (title, version)
) engine=innodb auto_increment=1 default charset=utf8;

#per citation licensing information
create table citedArticleLicense (
  citedArticleLicenseID bigint(20),
  licenseID bigint(20),
  status varchar(30) char set utf8 collate utf8_bin,
  maintainer varchar(30) character set utf8 collate utf8_bin,
  open_access bit,
  provenanceCategory varchar(30) char set utf8 collate utf8_bin,
  provenanceDescription text char set utf8 collate utf8_bin,
  provenanceAgent varchar(100) char set utf8 collate utf8_bin,
  provenanceSource varchar(100) char set utf8 collate utf8_bin,
  provenanceDate datetime,
  provenanceHandler varchar(30) char set utf8 collate utf8_bin,
  HandlerVersion varchar(30) char set utf8 collate utf8_bin,
  created datetime,
  lastModified datetime,
  primary key (citedArticleLicenseID),
  constraint foreign key (licenseID) references license (licenseID),
  index (status)
) engine=innodb auto_increment=1 default charset=utf8;