ALTER TABLE article ADD COLUMN strkImgURI VARCHAR(50) CHARACTER SET utf8 COLLATE utf8_bin null after url;
ALTER TABLE annotation ADD COLUMN highlightedText TEXT CHARACTER SET utf8 COLLATE utf8_bin null after competingInterestBody;
