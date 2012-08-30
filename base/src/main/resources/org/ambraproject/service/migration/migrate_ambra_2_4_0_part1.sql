create table pingback (
    pingbackID bigint not null auto_increment,
    lastModified datetime not null,
    created datetime not null,
    articleID bigint not null,
    url varchar(255) not null,
    title varchar(255),
    primary key (pingbackID),
    unique (articleID, url)
);
