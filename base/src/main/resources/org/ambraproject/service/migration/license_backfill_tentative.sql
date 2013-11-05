#performance optimization duplicates some information from citedArticleLicense to minimize sql joins via hibernate
alter table citedArticle
    add column license varchar(30) character set utf8 collate utf8_bin,
    add column canonicalPublicationID varchar(50) character set utf8 collate utf8_bin unique,
    add column citedArticleLicenseID bigint(20),
    add constraint foreign key (citedArticleLicenseID) references citedArticleLicense (citedArticleLicenseID);

#per citation licensing information
create table citedArticleLicense (
  citedArticleLicenseID bigint(20),
  licenseID bigint(20),
  canonicalPublicationID varchar(50) character set utf8 collate utf8_bin unique,
  publicationIDType varchar (15) character set utf8 collate utf8_bin,
  status varchar(30) char set utf8 collate utf8_bin,
  maintainer varchar(30) character set utf8 collate utf8_bin,
  title varchar(100) character set utf8 collate utf8_bin,
  version varchar(30) character set utf8 collate utf8_bin,
  domain_data bit,
  domain_content bit,
  open_access bit,
  isBY bit,
  isNC bit,
  isND bit,
  isSA bit,
  provenanceCategory varchar(30) char set utf8 collate utf8_bin,
  provenanceDescription varchar(1500) char set utf8 collate utf8_bin,
  provenanceAgent varchar(100) char set utf8 collate utf8_bin,
  provenanceSource varchar(100) char set utf8 collate utf8_bin,
  provenanceDate datetime,
  provenanceHandler varchar(30) char set utf8 collate utf8_bin,
  HandlerVersion varchar(30) char set utf8 collate utf8_bin,
  primary key (citedArticleLicenseID),
  index (title),
  index (version),
  index (status),
  index (maintainer),
  index(version),
  constraint foreign key (licenseID) references License (licenseID)
) engine=innodb auto_increment=1 default charset=utf8;

#contains common licensing information
create table License (
  licenseID bigint(20),
  title varchar(100) character set utf8 collate utf8_bin,
  version varchar(30) character set utf8 collate utf8_bin,
  family varchar(30) character set utf8 collate utf8_bin,
  description varchar(1500) character set utf8 collate utf8_bin,
  is_okd_compliant bit,
  is_osi_compliant bit,
  url varchar(100) character set utf8 collate utf8_bin,
  domain_software bit,
  type varchar(30) character set utf8 collate utf8_bin,
  jurisdiction varchar(30) character set utf8 collate utf8_bin,
  primary key (licenseID),
  index (family),
  unique index (title, version)
) engine=innodb auto_increment=1 default charset=utf8;