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
  // NOTE: 'note-pending' class is used to identify js-based annotation 
  //  related document markup prior to persisting the annotation
  annotationMarker: "note note-pending",
  pendingAnnotationMarker: 'note-pending',
  annotationImgMarker: "noteImg",
	regionalDialogMarker : "rdm",
	excludeSelection: "noSelect",
  tipDownDiv: "dTip",
  tipUpDiv: "dTipu",
  isAuthor: false,  //TODO: *** Default to false when the hidden input is hooked up.
  isPublic: true,
  dfltAnnSelErrMsg: 'This area of text cannot be notated.',
  annSelErrMsg: null,
  rangeInfoObj: new Object(),
  annTypeMinorCorrection: 'MinorCorrection',
  annTypeFormalCorrection: 'FormalCorrection',
  styleMinorCorrection: 'minrcrctn', // generalized css class name for minor corrections
  styleFormalCorrection: 'frmlcrctn' // generalized css class name for formal corrections
};

var formalCorrectionConfig = {
  styleFormalCorrectionHeader: 'fch', // css class name for the formal correction header node
  fchId: 'fch', // the formal correction header node dom id
  fcListId: 'fclist', // the formal correction header sub-node referencing the ordered list
  annid: 'annid' // dom node attribute name to use to store annotation ids 
};

var formConfig = {
	commentMaxLen: 250
};

var commentConfig = {
  cmtContainer: "cmtContainer",
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