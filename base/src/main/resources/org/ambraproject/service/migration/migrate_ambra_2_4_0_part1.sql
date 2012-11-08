create table pingback (
    pingbackID bigint not null auto_increment,
    articleID bigint not null CHARACTER SET utf8 COLLATE utf8_bin null;,
    url varchar(255) not null CHARACTER SET utf8 COLLATE utf8_bin null;,
    title varchar(255),
    lastModified datetime not null,
    created datetime not null,
    primary key (pingbackID),
    unique (articleID, url)
);
