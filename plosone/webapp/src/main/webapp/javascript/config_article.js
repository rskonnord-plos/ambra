/*var djConfig = {
	isDebug: false,
	debugContainerId : "dojoDebug",
	debugAtAllCosts: false,
  bindEncoding: "UTF-8"
};*/

var annotationConfig = {
	articleContainer: "articleContainer",
  rhcCount: "dcCount",
	trigger: "addAnnotation",
  lastAncestor: "researchArticle",
  xpointerMarker: "xpt",
  annotationMarker: "note",
  annotationImgMarker: "noteImg",
	regionalDialogMarker : "rdm",
	excludeSelection: "noSelect",
  tipDownDiv: "dTip",
  tipUpDiv: "dTipu",
  isAuthor: false,  //TODO: *** Default to false when the hidden input is hooked up.
  isPublic: true,
  rangeInfoObj: new Object()
};

var formConfig = {
	commentMaxLen: 250
};

var commentConfig = {
  sectionTitle: "viewCmtTitle",
  sectionDetail: "viewCmtDetail",  
  sectionComment: "viewComment", 
  sectionLink: "viewLink", 
  retrieveMsg: "retrieveMsg",  
  tipDownDiv: "cTip",
  tipUpDiv: "cTipu"
};  

var multiCommentConfig = {
  sectionTitle: "viewCmtTitle",
  sectionDetail: "viewCmtDetail",  
  sectionComment: "viewComment",  
  retrieveMsg: "retrieveMsg",  
  tipDownDiv: "mTip",
  tipUpDiv: "mTipu"
};  

var ratingConfig =  {
	insight:  "rateInsight",
	reliability: "ratingReliability",
	style: "rateStyle",
	ratingContainer: "ratingRhc"
};