#cache cottage lab data licensing info for cited articles
create table licenseInfo (
  licenseID bigint not null auto_increment,
  provenanceID bigint,
  maintainer varchar(30) character set utf8 collate utf8_bin,
  family varchar(30) character set utf8 collate utf8_bin,
  title varchar(100) character set utf8 collate utf8_bin,
  domain_data bit,
  url varchar(100) character set utf8 collate utf8_bin,
  version varchar(30) character set utf8 collate utf8_bin,
  domain_content bit,
  is_okd_compliant bit,
  is_osi_compliant bit,
  type varchar(30) character set utf8 collate utf8_bin,
  jurisdiction varchar(30) character set utf8 collate utf8_bin,
  open_access bit,
  isBY bit,
  isNC bit,
  isND bit,
  isSA bit,
  primary key (licenseID),
  index (provenanceID),
  index (family),
  index (isBY),
  index (isNC),
  index (isSA),
  constraint foreign key (provenanceID) references licenseProvenance(provenanceID) on delete cascade
) engine=innodb auto_increment=1 default charset=utf8;


create table provenanceInfo (
  provenanceID bigint,
  category varchar(30) character set utf8 collate utf8_bin,
  description  varchar(100) character set utf8 collate utf8_bin,
  agent varchar(50) character set utf8 collate utf8_bin,
  source  varchar(100) character set utf8 collate utf8_bin,
  date datetime,
  handler  varchar(30) character set utf8 collate utf8_bin,
  handlerVersion varchar(30) character set utf8 collate utf8_bin,
  primary key (provenanceID),
  index (category),
  index (handler)
) engine=innodb auto_increment=1 default charset=utf8;