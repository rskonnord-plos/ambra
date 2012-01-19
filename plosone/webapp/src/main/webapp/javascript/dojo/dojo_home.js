/*
	Copyright (c) 2004-2006, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/community/licensing.shtml
*/

/*
	This is a compiled version of Dojo, built for deployment and not for
	development. To get an editable version, please visit:

		http://dojotoolkit.org

	for documentation and information on getting the source.
	
	bundled from 0.4.2	

*/

if(typeof dojo=="undefined"){
var dj_global=this;
var dj_currentContext=this;
function dj_undef(_1,_2){
return (typeof (_2||dj_currentContext)[_1]=="undefined");
}
if(dj_undef("djConfig",this)){
var djConfig={};
}
if(dj_undef("dojo",this)){
var dojo={};
}
dojo.global=function(){
return dj_currentContext;
};
dojo.locale=djConfig.locale;
dojo.version={major:0,minor:0,patch:0,flag:"dev",revision:Number("$Rev$".match(/[0-9]+/)[0]),toString:function(){
with(dojo.version){
return major+"."+minor+"."+patch+flag+" ("+revision+")";
}
}};
dojo.evalProp=function(_3,_4,_5){
if((!_4)||(!_3)){
return undefined;
}
if(!dj_undef(_3,_4)){
return _4[_3];
}
return (_5?(_4[_3]={}):undefined);
};
dojo.parseObjPath=function(_6,_7,_8){
var _9=(_7||dojo.global());
var _a=_6.split(".");
var _b=_a.pop();
for(var i=0,l=_a.length;i<l&&_9;i++){
_9=dojo.evalProp(_a[i],_9,_8);
}
return {obj:_9,prop:_b};
};
dojo.evalObjPath=function(_e,_f){
if(typeof _e!="string"){
return dojo.global();
}
if(_e.indexOf(".")==-1){
return dojo.evalProp(_e,dojo.global(),_f);
}
var ref=dojo.parseObjPath(_e,dojo.global(),_f);
if(ref){
return dojo.evalProp(ref.prop,ref.obj,_f);
}
return null;
};
dojo.errorToString=function(_11){
if(!dj_undef("message",_11)){
return _11.message;
}else{
if(!dj_undef("description",_11)){
return _11.description;
}else{
return _11;
}
}
};
dojo.raise=function(_12,_13){
if(_13){
_12=_12+": "+dojo.errorToString(_13);
}else{
_12=dojo.errorToString(_12);
}
try{
if(djConfig.isDebug){
dojo.hostenv.println("FATAL exception raised: "+_12);
}
}
catch(e){
}
throw _13||Error(_12);
};
dojo.debug=function(){
};
dojo.debugShallow=function(obj){
};
dojo.profile={start:function(){
},end:function(){
},stop:function(){
},dump:function(){
}};
function dj_eval(_15){
return dj_global.eval?dj_global.eval(_15):eval(_15);
}
dojo.unimplemented=function(_16,_17){
var _18="'"+_16+"' not implemented";
if(_17!=null){
_18+=" "+_17;
}
dojo.raise(_18);
};
dojo.deprecated=function(_19,_1a,_1b){
var _1c="DEPRECATED: "+_19;
if(_1a){
_1c+=" "+_1a;
}
if(_1b){
_1c+=" -- will be removed in version: "+_1b;
}
dojo.debug(_1c);
};
dojo.render=(function(){
function vscaffold(_1d,_1e){
var tmp={capable:false,support:{builtin:false,plugin:false},prefixes:_1d};
for(var i=0;i<_1e.length;i++){
tmp[_1e[i]]=false;
}
return tmp;
}
return {name:"",ver:dojo.version,os:{win:false,linux:false,osx:false},html:vscaffold(["html"],["ie","opera","khtml","safari","moz"]),svg:vscaffold(["svg"],["corel","adobe","batik"]),vml:vscaffold(["vml"],["ie"]),swf:vscaffold(["Swf","Flash","Mm"],["mm"]),swt:vscaffold(["Swt"],["ibm"])};
})();
dojo.hostenv=(function(){
var _21={isDebug:false,allowQueryConfig:false,baseScriptUri:"",baseRelativePath:"",libraryScriptUri:"",iePreventClobber:false,ieClobberMinimal:true,preventBackButtonFix:true,delayMozLoadingFix:false,searchIds:[],parseWidgets:true};
if(typeof djConfig=="undefined"){
djConfig=_21;
}else{
for(var _22 in _21){
if(typeof djConfig[_22]=="undefined"){
djConfig[_22]=_21[_22];
}
}
}
return {name_:"(unset)",version_:"(unset)",getName:function(){
return this.name_;
},getVersion:function(){
return this.version_;
},getText:function(uri){
dojo.unimplemented("getText","uri="+uri);
}};
})();
dojo.hostenv.getBaseScriptUri=function(){
if(djConfig.baseScriptUri.length){
return djConfig.baseScriptUri;
}
var uri=new String(djConfig.libraryScriptUri||djConfig.baseRelativePath);
if(!uri){
dojo.raise("Nothing returned by getLibraryScriptUri(): "+uri);
}
var _25=uri.lastIndexOf("/");
djConfig.baseScriptUri=djConfig.baseRelativePath;
return djConfig.baseScriptUri;
};
(function(){
var _26={pkgFileName:"__package__",loading_modules_:{},loaded_modules_:{},addedToLoadingCount:[],removedFromLoadingCount:[],inFlightCount:0,modulePrefixes_:{dojo:{name:"dojo",value:"src"}},setModulePrefix:function(_27,_28){
this.modulePrefixes_[_27]={name:_27,value:_28};
},moduleHasPrefix:function(_29){
var mp=this.modulePrefixes_;
return Boolean(mp[_29]&&mp[_29].value);
},getModulePrefix:function(_2b){
if(this.moduleHasPrefix(_2b)){
return this.modulePrefixes_[_2b].value;
}
return _2b;
},getTextStack:[],loadUriStack:[],loadedUris:[],post_load_:false,modulesLoadedListeners:[],unloadListeners:[],loadNotifying:false};
for(var _2c in _26){
dojo.hostenv[_2c]=_26[_2c];
}
})();
dojo.hostenv.loadPath=function(_2d,_2e,cb){
var uri;
if(_2d.charAt(0)=="/"||_2d.match(/^\w+:/)){
uri=_2d;
}else{
uri=this.getBaseScriptUri()+_2d;
}
if(djConfig.cacheBust&&dojo.render.html.capable){
uri+="?"+String(djConfig.cacheBust).replace(/\W+/g,"");
}
try{
return !_2e?this.loadUri(uri,cb):this.loadUriAndCheck(uri,_2e,cb);
}
catch(e){
dojo.debug(e);
return false;
}
};
dojo.hostenv.loadUri=function(uri,cb){
if(this.loadedUris[uri]){
return true;
}
var _33=this.getText(uri,null,true);
if(!_33){
return false;
}
this.loadedUris[uri]=true;
if(cb){
_33="("+_33+")";
}
var _34=dj_eval(_33);
if(cb){
cb(_34);
}
return true;
};
dojo.hostenv.loadUriAndCheck=function(uri,_36,cb){
var ok=true;
try{
ok=this.loadUri(uri,cb);
}
catch(e){
dojo.debug("failed loading ",uri," with error: ",e);
}
return Boolean(ok&&this.findModule(_36,false));
};
dojo.loaded=function(){
};
dojo.unloaded=function(){
};
dojo.hostenv.loaded=function(){
this.loadNotifying=true;
this.post_load_=true;
var mll=this.modulesLoadedListeners;
for(var x=0;x<mll.length;x++){
mll[x]();
}
this.modulesLoadedListeners=[];
this.loadNotifying=false;
dojo.loaded();
};
dojo.hostenv.unloaded=function(){
var mll=this.unloadListeners;
while(mll.length){
(mll.pop())();
}
dojo.unloaded();
};
dojo.addOnLoad=function(obj,_3d){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.modulesLoadedListeners.push(obj);
}else{
if(arguments.length>1){
dh.modulesLoadedListeners.push(function(){
obj[_3d]();
});
}
}
if(dh.post_load_&&dh.inFlightCount==0&&!dh.loadNotifying){
dh.callLoaded();
}
};
dojo.addOnUnload=function(obj,_40){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.unloadListeners.push(obj);
}else{
if(arguments.length>1){
dh.unloadListeners.push(function(){
obj[_40]();
});
}
}
};
dojo.hostenv.modulesLoaded=function(){
if(this.post_load_){
return;
}
if(this.loadUriStack.length==0&&this.getTextStack.length==0){
if(this.inFlightCount>0){
dojo.debug("files still in flight!");
return;
}
dojo.hostenv.callLoaded();
}
};
dojo.hostenv.callLoaded=function(){
if(typeof setTimeout=="object"||(djConfig["useXDomain"]&&dojo.render.html.opera)){
setTimeout("dojo.hostenv.loaded();",0);
}else{
dojo.hostenv.loaded();
}
};
dojo.hostenv.getModuleSymbols=function(_42){
var _43=_42.split(".");
for(var i=_43.length;i>0;i--){
var _45=_43.slice(0,i).join(".");
if((i==1)&&!this.moduleHasPrefix(_45)){
_43[0]="../"+_43[0];
}else{
var _46=this.getModulePrefix(_45);
if(_46!=_45){
_43.splice(0,i,_46);
break;
}
}
}
return _43;
};
dojo.hostenv._global_omit_module_check=false;
dojo.hostenv.loadModule=function(_47,_48,_49){
if(!_47){
return;
}
_49=this._global_omit_module_check||_49;
var _4a=this.findModule(_47,false);
if(_4a){
return _4a;
}
if(dj_undef(_47,this.loading_modules_)){
this.addedToLoadingCount.push(_47);
}
this.loading_modules_[_47]=1;
var _4b=_47.replace(/\./g,"/")+".js";
var _4c=_47.split(".");
var _4d=this.getModuleSymbols(_47);
var _4e=((_4d[0].charAt(0)!="/")&&!_4d[0].match(/^\w+:/));
var _4f=_4d[_4d.length-1];
var ok;
if(_4f=="*"){
_47=_4c.slice(0,-1).join(".");
while(_4d.length){
_4d.pop();
_4d.push(this.pkgFileName);
_4b=_4d.join("/")+".js";
if(_4e&&_4b.charAt(0)=="/"){
_4b=_4b.slice(1);
}
ok=this.loadPath(_4b,!_49?_47:null);
if(ok){
break;
}
_4d.pop();
}
}else{
_4b=_4d.join("/")+".js";
_47=_4c.join(".");
var _51=!_49?_47:null;
ok=this.loadPath(_4b,_51);
if(!ok&&!_48){
_4d.pop();
while(_4d.length){
_4b=_4d.join("/")+".js";
ok=this.loadPath(_4b,_51);
if(ok){
break;
}
_4d.pop();
_4b=_4d.join("/")+"/"+this.pkgFileName+".js";
if(_4e&&_4b.charAt(0)=="/"){
_4b=_4b.slice(1);
}
ok=this.loadPath(_4b,_51);
if(ok){
break;
}
}
}
if(!ok&&!_49){
dojo.raise("Could not load '"+_47+"'; last tried '"+_4b+"'");
}
}
if(!_49&&!this["isXDomain"]){
_4a=this.findModule(_47,false);
if(!_4a){
dojo.raise("symbol '"+_47+"' is not defined after loading '"+_4b+"'");
}
}
return _4a;
};
dojo.hostenv.startPackage=function(_52){
var _53=String(_52);
var _54=_53;
var _55=_52.split(/\./);
if(_55[_55.length-1]=="*"){
_55.pop();
_54=_55.join(".");
}
var _56=dojo.evalObjPath(_54,true);
this.loaded_modules_[_53]=_56;
this.loaded_modules_[_54]=_56;
return _56;
};
dojo.hostenv.findModule=function(_57,_58){
var lmn=String(_57);
if(this.loaded_modules_[lmn]){
return this.loaded_modules_[lmn];
}
if(_58){
dojo.raise("no loaded module named '"+_57+"'");
}
return null;
};
dojo.kwCompoundRequire=function(_5a){
var _5b=_5a["common"]||[];
var _5c=_5a[dojo.hostenv.name_]?_5b.concat(_5a[dojo.hostenv.name_]||[]):_5b.concat(_5a["default"]||[]);
for(var x=0;x<_5c.length;x++){
var _5e=_5c[x];
if(_5e.constructor==Array){
dojo.hostenv.loadModule.apply(dojo.hostenv,_5e);
}else{
dojo.hostenv.loadModule(_5e);
}
}
};
dojo.require=function(_5f){
dojo.hostenv.loadModule.apply(dojo.hostenv,arguments);
};
dojo.requireIf=function(_60,_61){
var _62=arguments[0];
if((_62===true)||(_62=="common")||(_62&&dojo.render[_62].capable)){
var _63=[];
for(var i=1;i<arguments.length;i++){
_63.push(arguments[i]);
}
dojo.require.apply(dojo,_63);
}
};
dojo.requireAfterIf=dojo.requireIf;
dojo.provide=function(_65){
return dojo.hostenv.startPackage.apply(dojo.hostenv,arguments);
};
dojo.registerModulePath=function(_66,_67){
return dojo.hostenv.setModulePrefix(_66,_67);
};
if(djConfig["modulePaths"]){
for(var param in djConfig["modulePaths"]){
dojo.registerModulePath(param,djConfig["modulePaths"][param]);
}
}
dojo.setModulePrefix=function(_68,_69){
dojo.deprecated("dojo.setModulePrefix(\""+_68+"\", \""+_69+"\")","replaced by dojo.registerModulePath","0.5");
return dojo.registerModulePath(_68,_69);
};
dojo.exists=function(obj,_6b){
var p=_6b.split(".");
for(var i=0;i<p.length;i++){
if(!obj[p[i]]){
return false;
}
obj=obj[p[i]];
}
return true;
};
dojo.hostenv.normalizeLocale=function(_6e){
var _6f=_6e?_6e.toLowerCase():dojo.locale;
if(_6f=="root"){
_6f="ROOT";
}
return _6f;
};
dojo.hostenv.searchLocalePath=function(_70,_71,_72){
_70=dojo.hostenv.normalizeLocale(_70);
var _73=_70.split("-");
var _74=[];
for(var i=_73.length;i>0;i--){
_74.push(_73.slice(0,i).join("-"));
}
_74.push(false);
if(_71){
_74.reverse();
}
for(var j=_74.length-1;j>=0;j--){
var loc=_74[j]||"ROOT";
var _78=_72(loc);
if(_78){
break;
}
}
};
dojo.hostenv.localesGenerated;
dojo.hostenv.registerNlsPrefix=function(){
dojo.registerModulePath("nls","nls");
};
dojo.hostenv.preloadLocalizations=function(){
if(dojo.hostenv.localesGenerated){
dojo.hostenv.registerNlsPrefix();
function preload(_79){
_79=dojo.hostenv.normalizeLocale(_79);
dojo.hostenv.searchLocalePath(_79,true,function(loc){
for(var i=0;i<dojo.hostenv.localesGenerated.length;i++){
if(dojo.hostenv.localesGenerated[i]==loc){
dojo["require"]("nls.dojo_"+loc);
return true;
}
}
return false;
});
}
preload();
var _7c=djConfig.extraLocale||[];
for(var i=0;i<_7c.length;i++){
preload(_7c[i]);
}
}
dojo.hostenv.preloadLocalizations=function(){
};
};
dojo.requireLocalization=function(_7e,_7f,_80,_81){
dojo.hostenv.preloadLocalizations();
var _82=dojo.hostenv.normalizeLocale(_80);
var _83=[_7e,"nls",_7f].join(".");
var _84="";
if(_81){
var _85=_81.split(",");
for(var i=0;i<_85.length;i++){
if(_82.indexOf(_85[i])==0){
if(_85[i].length>_84.length){
_84=_85[i];
}
}
}
if(!_84){
_84="ROOT";
}
}
var _87=_81?_84:_82;
var _88=dojo.hostenv.findModule(_83);
var _89=null;
if(_88){
if(djConfig.localizationComplete&&_88._built){
return;
}
var _8a=_87.replace("-","_");
var _8b=_83+"."+_8a;
_89=dojo.hostenv.findModule(_8b);
}
if(!_89){
_88=dojo.hostenv.startPackage(_83);
var _8c=dojo.hostenv.getModuleSymbols(_7e);
var _8d=_8c.concat("nls").join("/");
var _8e;
dojo.hostenv.searchLocalePath(_87,_81,function(loc){
var _90=loc.replace("-","_");
var _91=_83+"."+_90;
var _92=false;
if(!dojo.hostenv.findModule(_91)){
dojo.hostenv.startPackage(_91);
var _93=[_8d];
if(loc!="ROOT"){
_93.push(loc);
}
_93.push(_7f);
var _94=_93.join("/")+".js";
_92=dojo.hostenv.loadPath(_94,null,function(_95){
var _96=function(){
};
_96.prototype=_8e;
_88[_90]=new _96();
for(var j in _95){
_88[_90][j]=_95[j];
}
});
}else{
_92=true;
}
if(_92&&_88[_90]){
_8e=_88[_90];
}else{
_88[_90]=_8e;
}
if(_81){
return true;
}
});
}
if(_81&&_82!=_84){
_88[_82.replace("-","_")]=_88[_84.replace("-","_")];
}
};
(function(){
var _98=djConfig.extraLocale;
if(_98){
if(!_98 instanceof Array){
_98=[_98];
}
var req=dojo.requireLocalization;
dojo.requireLocalization=function(m,b,_9c,_9d){
req(m,b,_9c,_9d);
if(_9c){
return;
}
for(var i=0;i<_98.length;i++){
req(m,b,_98[i],_9d);
}
};
}
})();
}
if(typeof window!="undefined"){
(function(){
if(djConfig.allowQueryConfig){
var _9f=document.location.toString();
var _a0=_9f.split("?",2);
if(_a0.length>1){
var _a1=_a0[1];
var _a2=_a1.split("&");
for(var x in _a2){
var sp=_a2[x].split("=");
if((sp[0].length>9)&&(sp[0].substr(0,9)=="djConfig.")){
var opt=sp[0].substr(9);
try{
djConfig[opt]=eval(sp[1]);
}
catch(e){
djConfig[opt]=sp[1];
}
}
}
}
}
if(((djConfig["baseScriptUri"]=="")||(djConfig["baseRelativePath"]==""))&&(document&&document.getElementsByTagName)){
var _a6=document.getElementsByTagName("script");
var _a7=/(__package__|dojo|bootstrap1)\.js([\?\.]|$)/i;
for(var i=0;i<_a6.length;i++){
var src=_a6[i].getAttribute("src");
if(!src){
continue;
}
var m=src.match(_a7);
if(m){
var _ab=src.substring(0,m.index);
if(src.indexOf("bootstrap1")>-1){
_ab+="../";
}
if(!this["djConfig"]){
djConfig={};
}
if(djConfig["baseScriptUri"]==""){
djConfig["baseScriptUri"]=_ab;
}
if(djConfig["baseRelativePath"]==""){
djConfig["baseRelativePath"]=_ab;
}
break;
}
}
}
var dr=dojo.render;
var drh=dojo.render.html;
var drs=dojo.render.svg;
var dua=(drh.UA=navigator.userAgent);
var dav=(drh.AV=navigator.appVersion);
var t=true;
var f=false;
drh.capable=t;
drh.support.builtin=t;
dr.ver=parseFloat(drh.AV);
dr.os.mac=dav.indexOf("Macintosh")>=0;
dr.os.win=dav.indexOf("Windows")>=0;
dr.os.linux=dav.indexOf("X11")>=0;
drh.opera=dua.indexOf("Opera")>=0;
drh.khtml=(dav.indexOf("Konqueror")>=0)||(dav.indexOf("Safari")>=0);
drh.safari=dav.indexOf("Safari")>=0;
var _b3=dua.indexOf("Gecko");
drh.mozilla=drh.moz=(_b3>=0)&&(!drh.khtml);
if(drh.mozilla){
drh.geckoVersion=dua.substring(_b3+6,_b3+14);
}
drh.ie=(document.all)&&(!drh.opera);
drh.ie50=drh.ie&&dav.indexOf("MSIE 5.0")>=0;
drh.ie55=drh.ie&&dav.indexOf("MSIE 5.5")>=0;
drh.ie60=drh.ie&&dav.indexOf("MSIE 6.0")>=0;
drh.ie70=drh.ie&&dav.indexOf("MSIE 7.0")>=0;
var cm=document["compatMode"];
drh.quirks=(cm=="BackCompat")||(cm=="QuirksMode")||drh.ie55||drh.ie50;
dojo.locale=dojo.locale||(drh.ie?navigator.userLanguage:navigator.language).toLowerCase();
dr.vml.capable=drh.ie;
drs.capable=f;
drs.support.plugin=f;
drs.support.builtin=f;
var _b5=window["document"];
var tdi=_b5["implementation"];
if((tdi)&&(tdi["hasFeature"])&&(tdi.hasFeature("org.w3c.dom.svg","1.0"))){
drs.capable=t;
drs.support.builtin=t;
drs.support.plugin=f;
}
if(drh.safari){
var tmp=dua.split("AppleWebKit/")[1];
var ver=parseFloat(tmp.split(" ")[0]);
if(ver>=420){
drs.capable=t;
drs.support.builtin=t;
drs.support.plugin=f;
}
}else{
}
})();
dojo.hostenv.startPackage("dojo.hostenv");
dojo.render.name=dojo.hostenv.name_="browser";
dojo.hostenv.searchIds=[];
dojo.hostenv._XMLHTTP_PROGIDS=["Msxml2.XMLHTTP","Microsoft.XMLHTTP","Msxml2.XMLHTTP.4.0"];
dojo.hostenv.getXmlhttpObject=function(){
var _b9=null;
var _ba=null;
try{
_b9=new XMLHttpRequest();
}
catch(e){
}
if(!_b9){
for(var i=0;i<3;++i){
var _bc=dojo.hostenv._XMLHTTP_PROGIDS[i];
try{
_b9=new ActiveXObject(_bc);
}
catch(e){
_ba=e;
}
if(_b9){
dojo.hostenv._XMLHTTP_PROGIDS=[_bc];
break;
}
}
}
if(!_b9){
return dojo.raise("XMLHTTP not available",_ba);
}
return _b9;
};
dojo.hostenv._blockAsync=false;
dojo.hostenv.getText=function(uri,_be,_bf){
if(!_be){
this._blockAsync=true;
}
var _c0=this.getXmlhttpObject();
function isDocumentOk(_c1){
var _c2=_c1["status"];
return Boolean((!_c2)||((200<=_c2)&&(300>_c2))||(_c2==304));
}
if(_be){
var _c3=this,_c4=null,gbl=dojo.global();
var xhr=dojo.evalObjPath("dojo.io.XMLHTTPTransport");
_c0.onreadystatechange=function(){
if(_c4){
gbl.clearTimeout(_c4);
_c4=null;
}
if(_c3._blockAsync||(xhr&&xhr._blockAsync)){
_c4=gbl.setTimeout(function(){
_c0.onreadystatechange.apply(this);
},10);
}else{
if(4==_c0.readyState){
if(isDocumentOk(_c0)){
_be(_c0.responseText);
}
}
}
};
}
_c0.open("GET",uri,_be?true:false);
try{
_c0.send(null);
if(_be){
return null;
}
if(!isDocumentOk(_c0)){
var err=Error("Unable to load "+uri+" status:"+_c0.status);
err.status=_c0.status;
err.responseText=_c0.responseText;
throw err;
}
}
catch(e){
this._blockAsync=false;
if((_bf)&&(!_be)){
return null;
}else{
throw e;
}
}
this._blockAsync=false;
return _c0.responseText;
};
dojo.hostenv.defaultDebugContainerId="dojoDebug";
dojo.hostenv._println_buffer=[];
dojo.hostenv._println_safe=false;
dojo.hostenv.println=function(_c8){
if(!dojo.hostenv._println_safe){
dojo.hostenv._println_buffer.push(_c8);
}else{
try{
var _c9=document.getElementById(djConfig.debugContainerId?djConfig.debugContainerId:dojo.hostenv.defaultDebugContainerId);
if(!_c9){
_c9=dojo.body();
}
var div=document.createElement("div");
div.appendChild(document.createTextNode(_c8));
_c9.appendChild(div);
}
catch(e){
try{
document.write("<div>"+_c8+"</div>");
}
catch(e2){
window.status=_c8;
}
}
}
};
dojo.addOnLoad(function(){
dojo.hostenv._println_safe=true;
while(dojo.hostenv._println_buffer.length>0){
dojo.hostenv.println(dojo.hostenv._println_buffer.shift());
}
});
function dj_addNodeEvtHdlr(_cb,_cc,fp){
var _ce=_cb["on"+_cc]||function(){
};
_cb["on"+_cc]=function(){
fp.apply(_cb,arguments);
_ce.apply(_cb,arguments);
};
return true;
}
function dj_load_init(e){
var _d0=(e&&e.type)?e.type.toLowerCase():"load";
if(arguments.callee.initialized||(_d0!="domcontentloaded"&&_d0!="load")){
return;
}
arguments.callee.initialized=true;
if(typeof (_timer)!="undefined"){
clearInterval(_timer);
delete _timer;
}
var _d1=function(){
if(dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
};
if(dojo.hostenv.inFlightCount==0){
_d1();
dojo.hostenv.modulesLoaded();
}else{
dojo.hostenv.modulesLoadedListeners.unshift(_d1);
}
}
if(document.addEventListener){
if(dojo.render.html.opera||(dojo.render.html.moz&&(djConfig["enableMozDomContentLoaded"]===true))){
document.addEventListener("DOMContentLoaded",dj_load_init,null);
}
window.addEventListener("load",dj_load_init,null);
}
if(dojo.render.html.ie&&dojo.render.os.win){
document.attachEvent("onreadystatechange",function(e){
if(document.readyState=="complete"){
dj_load_init();
}
});
}
if(/(WebKit|khtml)/i.test(navigator.userAgent)){
var _timer=setInterval(function(){
if(/loaded|complete/.test(document.readyState)){
dj_load_init();
}
},10);
}
if(dojo.render.html.ie){
dj_addNodeEvtHdlr(window,"beforeunload",function(){
dojo.hostenv._unloading=true;
window.setTimeout(function(){
dojo.hostenv._unloading=false;
},0);
});
}
dj_addNodeEvtHdlr(window,"unload",function(){
dojo.hostenv.unloaded();
if((!dojo.render.html.ie)||(dojo.render.html.ie&&dojo.hostenv._unloading)){
dojo.hostenv.unloaded();
}
});
dojo.hostenv.makeWidgets=function(){
var _d3=[];
if(djConfig.searchIds&&djConfig.searchIds.length>0){
_d3=_d3.concat(djConfig.searchIds);
}
if(dojo.hostenv.searchIds&&dojo.hostenv.searchIds.length>0){
_d3=_d3.concat(dojo.hostenv.searchIds);
}
if((djConfig.parseWidgets)||(_d3.length>0)){
if(dojo.evalObjPath("dojo.widget.Parse")){
var _d4=new dojo.xml.Parse();
if(_d3.length>0){
for(var x=0;x<_d3.length;x++){
var _d6=document.getElementById(_d3[x]);
if(!_d6){
continue;
}
var _d7=_d4.parseElement(_d6,null,true);
dojo.widget.getParser().createComponents(_d7);
}
}else{
if(djConfig.parseWidgets){
var _d7=_d4.parseElement(dojo.body(),null,true);
dojo.widget.getParser().createComponents(_d7);
}
}
}
}
};
dojo.addOnLoad(function(){
if(!dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
});
try{
if(dojo.render.html.ie){
document.namespaces.add("v","urn:schemas-microsoft-com:vml");
document.createStyleSheet().addRule("v\\:*","behavior:url(#default#VML)");
}
}
catch(e){
}
dojo.hostenv.writeIncludes=function(){
};
if(!dj_undef("document",this)){
dj_currentDocument=this.document;
}
dojo.doc=function(){
return dj_currentDocument;
};
dojo.body=function(){
return dojo.doc().body||dojo.doc().getElementsByTagName("body")[0];
};
dojo.byId=function(id,doc){
if((id)&&((typeof id=="string")||(id instanceof String))){
if(!doc){
doc=dj_currentDocument;
}
var ele=doc.getElementById(id);
if(ele&&(ele.id!=id)&&doc.all){
ele=null;
eles=doc.all[id];
if(eles){
if(eles.length){
for(var i=0;i<eles.length;i++){
if(eles[i].id==id){
ele=eles[i];
break;
}
}
}else{
ele=eles;
}
}
}
return ele;
}
return id;
};
dojo.setContext=function(_dc,_dd){
dj_currentContext=_dc;
dj_currentDocument=_dd;
};
dojo._fireCallback=function(_de,_df,_e0){
if((_df)&&((typeof _de=="string")||(_de instanceof String))){
_de=_df[_de];
}
return (_df?_de.apply(_df,_e0||[]):_de());
};
dojo.withGlobal=function(_e1,_e2,_e3,_e4){
var _e5;
var _e6=dj_currentContext;
var _e7=dj_currentDocument;
try{
dojo.setContext(_e1,_e1.document);
_e5=dojo._fireCallback(_e2,_e3,_e4);
}
finally{
dojo.setContext(_e6,_e7);
}
return _e5;
};
dojo.withDoc=function(_e8,_e9,_ea,_eb){
var _ec;
var _ed=dj_currentDocument;
try{
dj_currentDocument=_e8;
_ec=dojo._fireCallback(_e9,_ea,_eb);
}
finally{
dj_currentDocument=_ed;
}
return _ec;
};
}
dojo.requireIf((djConfig["isDebug"]||djConfig["debugAtAllCosts"]),"dojo.debug");
dojo.requireIf(djConfig["debugAtAllCosts"]&&!window.widget&&!djConfig["useXDomain"],"dojo.browser_debug");
dojo.requireIf(djConfig["debugAtAllCosts"]&&!window.widget&&djConfig["useXDomain"],"dojo.browser_debug_xd");
dojo.provide("dojo.lang.common");
dojo.lang.inherits=function(_ee,_ef){
if(!dojo.lang.isFunction(_ef)){
dojo.raise("dojo.inherits: superclass argument ["+_ef+"] must be a function (subclass: ["+_ee+"']");
}
_ee.prototype=new _ef();
_ee.prototype.constructor=_ee;
_ee.superclass=_ef.prototype;
_ee["super"]=_ef.prototype;
};
dojo.lang._mixin=function(obj,_f1){
var _f2={};
for(var x in _f1){
if((typeof _f2[x]=="undefined")||(_f2[x]!=_f1[x])){
obj[x]=_f1[x];
}
}
if(dojo.render.html.ie&&(typeof (_f1["toString"])=="function")&&(_f1["toString"]!=obj["toString"])&&(_f1["toString"]!=_f2["toString"])){
obj.toString=_f1.toString;
}
return obj;
};
dojo.lang.mixin=function(obj,_f5){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(obj,arguments[i]);
}
return obj;
};
dojo.lang.extend=function(_f8,_f9){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(_f8.prototype,arguments[i]);
}
return _f8;
};
dojo.inherits=dojo.lang.inherits;
dojo.mixin=dojo.lang.mixin;
dojo.extend=dojo.lang.extend;
dojo.lang.find=function(_fc,_fd,_fe,_ff){
if(!dojo.lang.isArrayLike(_fc)&&dojo.lang.isArrayLike(_fd)){
dojo.deprecated("dojo.lang.find(value, array)","use dojo.lang.find(array, value) instead","0.5");
var temp=_fc;
_fc=_fd;
_fd=temp;
}
var _101=dojo.lang.isString(_fc);
if(_101){
_fc=_fc.split("");
}
if(_ff){
var step=-1;
var i=_fc.length-1;
var end=-1;
}else{
var step=1;
var i=0;
var end=_fc.length;
}
if(_fe){
while(i!=end){
if(_fc[i]===_fd){
return i;
}
i+=step;
}
}else{
while(i!=end){
if(_fc[i]==_fd){
return i;
}
i+=step;
}
}
return -1;
};
dojo.lang.indexOf=dojo.lang.find;
dojo.lang.findLast=function(_105,_106,_107){
return dojo.lang.find(_105,_106,_107,true);
};
dojo.lang.lastIndexOf=dojo.lang.findLast;
dojo.lang.inArray=function(_108,_109){
return dojo.lang.find(_108,_109)>-1;
};
dojo.lang.isObject=function(it){
if(typeof it=="undefined"){
return false;
}
return (typeof it=="object"||it===null||dojo.lang.isArray(it)||dojo.lang.isFunction(it));
};
dojo.lang.isArray=function(it){
return (it&&it instanceof Array||typeof it=="array");
};
dojo.lang.isArrayLike=function(it){
if((!it)||(dojo.lang.isUndefined(it))){
return false;
}
if(dojo.lang.isString(it)){
return false;
}
if(dojo.lang.isFunction(it)){
return false;
}
if(dojo.lang.isArray(it)){
return true;
}
if((it.tagName)&&(it.tagName.toLowerCase()=="form")){
return false;
}
if(dojo.lang.isNumber(it.length)&&isFinite(it.length)){
return true;
}
return false;
};
dojo.lang.isFunction=function(it){
return (it instanceof Function||typeof it=="function");
};
(function(){
if((dojo.render.html.capable)&&(dojo.render.html["safari"])){
dojo.lang.isFunction=function(it){
if((typeof (it)=="function")&&(it=="[object NodeList]")){
return false;
}
return (it instanceof Function||typeof it=="function");
};
}
})();
dojo.lang.isString=function(it){
return (typeof it=="string"||it instanceof String);
};
dojo.lang.isAlien=function(it){
if(!it){
return false;
}
return !dojo.lang.isFunction(it)&&/\{\s*\[native code\]\s*\}/.test(String(it));
};
dojo.lang.isBoolean=function(it){
return (it instanceof Boolean||typeof it=="boolean");
};
dojo.lang.isNumber=function(it){
return (it instanceof Number||typeof it=="number");
};
dojo.lang.isUndefined=function(it){
return ((typeof (it)=="undefined")&&(it==undefined));
};
dojo.provide("dojo.dom");
dojo.dom.ELEMENT_NODE=1;
dojo.dom.ATTRIBUTE_NODE=2;
dojo.dom.TEXT_NODE=3;
dojo.dom.CDATA_SECTION_NODE=4;
dojo.dom.ENTITY_REFERENCE_NODE=5;
dojo.dom.ENTITY_NODE=6;
dojo.dom.PROCESSING_INSTRUCTION_NODE=7;
dojo.dom.COMMENT_NODE=8;
dojo.dom.DOCUMENT_NODE=9;
dojo.dom.DOCUMENT_TYPE_NODE=10;
dojo.dom.DOCUMENT_FRAGMENT_NODE=11;
dojo.dom.NOTATION_NODE=12;
dojo.dom.dojoml="http://www.dojotoolkit.org/2004/dojoml";
dojo.dom.xmlns={svg:"http://www.w3.org/2000/svg",smil:"http://www.w3.org/2001/SMIL20/",mml:"http://www.w3.org/1998/Math/MathML",cml:"http://www.xml-cml.org",xlink:"http://www.w3.org/1999/xlink",xhtml:"http://www.w3.org/1999/xhtml",xul:"http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul",xbl:"http://www.mozilla.org/xbl",fo:"http://www.w3.org/1999/XSL/Format",xsl:"http://www.w3.org/1999/XSL/Transform",xslt:"http://www.w3.org/1999/XSL/Transform",xi:"http://www.w3.org/2001/XInclude",xforms:"http://www.w3.org/2002/01/xforms",saxon:"http://icl.com/saxon",xalan:"http://xml.apache.org/xslt",xsd:"http://www.w3.org/2001/XMLSchema",dt:"http://www.w3.org/2001/XMLSchema-datatypes",xsi:"http://www.w3.org/2001/XMLSchema-instance",rdf:"http://www.w3.org/1999/02/22-rdf-syntax-ns#",rdfs:"http://www.w3.org/2000/01/rdf-schema#",dc:"http://purl.org/dc/elements/1.1/",dcq:"http://purl.org/dc/qualifiers/1.0","soap-env":"http://schemas.xmlsoap.org/soap/envelope/",wsdl:"http://schemas.xmlsoap.org/wsdl/",AdobeExtensions:"http://ns.adobe.com/AdobeSVGViewerExtensions/3.0/"};
dojo.dom.isNode=function(wh){
if(typeof Element=="function"){
try{
return wh instanceof Element;
}
catch(e){
}
}else{
return wh&&!isNaN(wh.nodeType);
}
};
dojo.dom.getUniqueId=function(){
var _115=dojo.doc();
do{
var id="dj_unique_"+(++arguments.callee._idIncrement);
}while(_115.getElementById(id));
return id;
};
dojo.dom.getUniqueId._idIncrement=0;
dojo.dom.firstElement=dojo.dom.getFirstChildElement=function(_117,_118){
var node=_117.firstChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.nextSibling;
}
if(_118&&node&&node.tagName&&node.tagName.toLowerCase()!=_118.toLowerCase()){
node=dojo.dom.nextElement(node,_118);
}
return node;
};
dojo.dom.lastElement=dojo.dom.getLastChildElement=function(_11a,_11b){
var node=_11a.lastChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.previousSibling;
}
if(_11b&&node&&node.tagName&&node.tagName.toLowerCase()!=_11b.toLowerCase()){
node=dojo.dom.prevElement(node,_11b);
}
return node;
};
dojo.dom.nextElement=dojo.dom.getNextSiblingElement=function(node,_11e){
if(!node){
return null;
}
do{
node=node.nextSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_11e&&_11e.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.nextElement(node,_11e);
}
return node;
};
dojo.dom.prevElement=dojo.dom.getPreviousSiblingElement=function(node,_120){
if(!node){
return null;
}
if(_120){
_120=_120.toLowerCase();
}
do{
node=node.previousSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_120&&_120.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.prevElement(node,_120);
}
return node;
};
dojo.dom.moveChildren=function(_121,_122,trim){
var _124=0;
if(trim){
while(_121.hasChildNodes()&&_121.firstChild.nodeType==dojo.dom.TEXT_NODE){
_121.removeChild(_121.firstChild);
}
while(_121.hasChildNodes()&&_121.lastChild.nodeType==dojo.dom.TEXT_NODE){
_121.removeChild(_121.lastChild);
}
}
while(_121.hasChildNodes()){
_122.appendChild(_121.firstChild);
_124++;
}
return _124;
};
dojo.dom.copyChildren=function(_125,_126,trim){
var _128=_125.cloneNode(true);
return this.moveChildren(_128,_126,trim);
};
dojo.dom.replaceChildren=function(node,_12a){
var _12b=[];
if(dojo.render.html.ie){
for(var i=0;i<node.childNodes.length;i++){
_12b.push(node.childNodes[i]);
}
}
dojo.dom.removeChildren(node);
node.appendChild(_12a);
for(var i=0;i<_12b.length;i++){
dojo.dom.destroyNode(_12b[i]);
}
};
dojo.dom.removeChildren=function(node){
var _12e=node.childNodes.length;
while(node.hasChildNodes()){
dojo.dom.removeNode(node.firstChild);
}
return _12e;
};
dojo.dom.replaceNode=function(node,_130){
return node.parentNode.replaceChild(_130,node);
};
dojo.dom.destroyNode=function(node){
if(node.parentNode){
node=dojo.dom.removeNode(node);
}
if(node.nodeType!=3){
if(dojo.evalObjPath("dojo.event.browser.clean",false)){
dojo.event.browser.clean(node);
}
if(dojo.render.html.ie){
node.outerHTML="";
}
}
};
dojo.dom.removeNode=function(node){
if(node&&node.parentNode){
return node.parentNode.removeChild(node);
}
};
dojo.dom.getAncestors=function(node,_134,_135){
var _136=[];
var _137=(_134&&(_134 instanceof Function||typeof _134=="function"));
while(node){
if(!_137||_134(node)){
_136.push(node);
}
if(_135&&_136.length>0){
return _136[0];
}
node=node.parentNode;
}
if(_135){
return null;
}
return _136;
};
dojo.dom.getAncestorsByTag=function(node,tag,_13a){
tag=tag.toLowerCase();
return dojo.dom.getAncestors(node,function(el){
return ((el.tagName)&&(el.tagName.toLowerCase()==tag));
},_13a);
};
dojo.dom.getFirstAncestorByTag=function(node,tag){
return dojo.dom.getAncestorsByTag(node,tag,true);
};
dojo.dom.isDescendantOf=function(node,_13f,_140){
if(_140&&node){
node=node.parentNode;
}
while(node){
if(node==_13f){
return true;
}
node=node.parentNode;
}
return false;
};
dojo.dom.innerXML=function(node){
if(node.innerXML){
return node.innerXML;
}else{
if(node.xml){
return node.xml;
}else{
if(typeof XMLSerializer!="undefined"){
return (new XMLSerializer()).serializeToString(node);
}
}
}
};
dojo.dom.createDocument=function(){
var doc=null;
var _143=dojo.doc();
if(!dj_undef("ActiveXObject")){
var _144=["MSXML2","Microsoft","MSXML","MSXML3"];
for(var i=0;i<_144.length;i++){
try{
doc=new ActiveXObject(_144[i]+".XMLDOM");
}
catch(e){
}
if(doc){
break;
}
}
}else{
if((_143.implementation)&&(_143.implementation.createDocument)){
doc=_143.implementation.createDocument("","",null);
}
}
return doc;
};
dojo.dom.createDocumentFromText=function(str,_147){
if(!_147){
_147="text/xml";
}
if(!dj_undef("DOMParser")){
var _148=new DOMParser();
return _148.parseFromString(str,_147);
}else{
if(!dj_undef("ActiveXObject")){
var _149=dojo.dom.createDocument();
if(_149){
_149.async=false;
_149.loadXML(str);
return _149;
}else{
dojo.debug("toXml didn't work?");
}
}else{
var _14a=dojo.doc();
if(_14a.createElement){
var tmp=_14a.createElement("xml");
tmp.innerHTML=str;
if(_14a.implementation&&_14a.implementation.createDocument){
var _14c=_14a.implementation.createDocument("foo","",null);
for(var i=0;i<tmp.childNodes.length;i++){
_14c.importNode(tmp.childNodes.item(i),true);
}
return _14c;
}
return ((tmp.document)&&(tmp.document.firstChild?tmp.document.firstChild:tmp));
}
}
}
return null;
};
dojo.dom.prependChild=function(node,_14f){
if(_14f.firstChild){
_14f.insertBefore(node,_14f.firstChild);
}else{
_14f.appendChild(node);
}
return true;
};
dojo.dom.insertBefore=function(node,ref,_152){
if((_152!=true)&&(node===ref||node.nextSibling===ref)){
return false;
}
var _153=ref.parentNode;
_153.insertBefore(node,ref);
return true;
};
dojo.dom.insertAfter=function(node,ref,_156){
var pn=ref.parentNode;
if(ref==pn.lastChild){
if((_156!=true)&&(node===ref)){
return false;
}
pn.appendChild(node);
}else{
return this.insertBefore(node,ref.nextSibling,_156);
}
return true;
};
dojo.dom.insertAtPosition=function(node,ref,_15a){
if((!node)||(!ref)||(!_15a)){
return false;
}
switch(_15a.toLowerCase()){
case "before":
return dojo.dom.insertBefore(node,ref);
case "after":
return dojo.dom.insertAfter(node,ref);
case "first":
if(ref.firstChild){
return dojo.dom.insertBefore(node,ref.firstChild);
}else{
ref.appendChild(node);
return true;
}
break;
default:
ref.appendChild(node);
return true;
}
};
dojo.dom.insertAtIndex=function(node,_15c,_15d){
var _15e=_15c.childNodes;
if(!_15e.length||_15e.length==_15d){
_15c.appendChild(node);
return true;
}
if(_15d==0){
return dojo.dom.prependChild(node,_15c);
}
return dojo.dom.insertAfter(node,_15e[_15d-1]);
};
dojo.dom.textContent=function(node,text){
if(arguments.length>1){
var _161=dojo.doc();
dojo.dom.replaceChildren(node,_161.createTextNode(text));
return text;
}else{
if(node.textContent!=undefined){
return node.textContent;
}
var _162="";
if(node==null){
return _162;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
_162+=dojo.dom.textContent(node.childNodes[i]);
break;
case 3:
case 2:
case 4:
_162+=node.childNodes[i].nodeValue;
break;
default:
break;
}
}
return _162;
}
};
dojo.dom.hasParent=function(node){
return Boolean(node&&node.parentNode&&dojo.dom.isNode(node.parentNode));
};
dojo.dom.isTag=function(node){
if(node&&node.tagName){
for(var i=1;i<arguments.length;i++){
if(node.tagName==String(arguments[i])){
return String(arguments[i]);
}
}
}
return "";
};
dojo.dom.setAttributeNS=function(elem,_168,_169,_16a){
if(elem==null||((elem==undefined)&&(typeof elem=="undefined"))){
dojo.raise("No element given to dojo.dom.setAttributeNS");
}
if(!((elem.setAttributeNS==undefined)&&(typeof elem.setAttributeNS=="undefined"))){
elem.setAttributeNS(_168,_169,_16a);
}else{
var _16b=elem.ownerDocument;
var _16c=_16b.createNode(2,_169,_168);
_16c.nodeValue=_16a;
elem.setAttributeNode(_16c);
}
};
dojo.provide("dojo.html.common");
dojo.lang.mixin(dojo.html,dojo.dom);
dojo.html.body=function(){
dojo.deprecated("dojo.html.body() moved to dojo.body()","0.5");
return dojo.body();
};
dojo.html.getEventTarget=function(evt){
if(!evt){
evt=dojo.global().event||{};
}
var t=(evt.srcElement?evt.srcElement:(evt.target?evt.target:null));
while((t)&&(t.nodeType!=1)){
t=t.parentNode;
}
return t;
};
dojo.html.getViewport=function(){
var _16f=dojo.global();
var _170=dojo.doc();
var w=0;
var h=0;
if(dojo.render.html.mozilla){
w=_170.documentElement.clientWidth;
h=_16f.innerHeight;
}else{
if(!dojo.render.html.opera&&_16f.innerWidth){
w=_16f.innerWidth;
h=_16f.innerHeight;
}else{
if(!dojo.render.html.opera&&dojo.exists(_170,"documentElement.clientWidth")){
var w2=_170.documentElement.clientWidth;
if(!w||w2&&w2<w){
w=w2;
}
h=_170.documentElement.clientHeight;
}else{
if(dojo.body().clientWidth){
w=dojo.body().clientWidth;
h=dojo.body().clientHeight;
}
}
}
}
return {width:w,height:h};
};
dojo.html.getScroll=function(){
var _174=dojo.global();
var _175=dojo.doc();
var top=_174.pageYOffset||_175.documentElement.scrollTop||dojo.body().scrollTop||0;
var left=_174.pageXOffset||_175.documentElement.scrollLeft||dojo.body().scrollLeft||0;
return {top:top,left:left,offset:{x:left,y:top}};
};
dojo.html.getParentByType=function(node,type){
var _17a=dojo.doc();
var _17b=dojo.byId(node);
type=type.toLowerCase();
while((_17b)&&(_17b.nodeName.toLowerCase()!=type)){
if(_17b==(_17a["body"]||_17a["documentElement"])){
return null;
}
_17b=_17b.parentNode;
}
return _17b;
};
dojo.html.getAttribute=function(node,attr){
node=dojo.byId(node);
if((!node)||(!node.getAttribute)){
return null;
}
var ta=typeof attr=="string"?attr:new String(attr);
var v=node.getAttribute(ta.toUpperCase());
if((v)&&(typeof v=="string")&&(v!="")){
return v;
}
if(v&&v.value){
return v.value;
}
if((node.getAttributeNode)&&(node.getAttributeNode(ta))){
return (node.getAttributeNode(ta)).value;
}else{
if(node.getAttribute(ta)){
return node.getAttribute(ta);
}else{
if(node.getAttribute(ta.toLowerCase())){
return node.getAttribute(ta.toLowerCase());
}
}
}
return null;
};
dojo.html.hasAttribute=function(node,attr){
return dojo.html.getAttribute(dojo.byId(node),attr)?true:false;
};
dojo.html.getCursorPosition=function(e){
e=e||dojo.global().event;
var _183={x:0,y:0};
if(e.pageX||e.pageY){
_183.x=e.pageX;
_183.y=e.pageY;
}else{
var de=dojo.doc().documentElement;
var db=dojo.body();
_183.x=e.clientX+((de||db)["scrollLeft"])-((de||db)["clientLeft"]);
_183.y=e.clientY+((de||db)["scrollTop"])-((de||db)["clientTop"]);
}
return _183;
};
dojo.html.isTag=function(node){
node=dojo.byId(node);
if(node&&node.tagName){
for(var i=1;i<arguments.length;i++){
if(node.tagName.toLowerCase()==String(arguments[i]).toLowerCase()){
return String(arguments[i]).toLowerCase();
}
}
}
return "";
};
if(dojo.render.html.ie&&!dojo.render.html.ie70){
if(window.location.href.substr(0,6).toLowerCase()!="https:"){
(function(){
var _188=dojo.doc().createElement("script");
_188.src="javascript:'dojo.html.createExternalElement=function(doc, tag){ return doc.createElement(tag); }'";
dojo.doc().getElementsByTagName("head")[0].appendChild(_188);
})();
}
}else{
dojo.html.createExternalElement=function(doc,tag){
return doc.createElement(tag);
};
}
dojo.html._callDeprecated=function(_18b,_18c,args,_18e,_18f){
dojo.deprecated("dojo.html."+_18b,"replaced by dojo.html."+_18c+"("+(_18e?"node, {"+_18e+": "+_18e+"}":"")+")"+(_18f?"."+_18f:""),"0.5");
var _190=[];
if(_18e){
var _191={};
_191[_18e]=args[1];
_190.push(args[0]);
_190.push(_191);
}else{
_190=args;
}
var ret=dojo.html[_18c].apply(dojo.html,args);
if(_18f){
return ret[_18f];
}else{
return ret;
}
};
dojo.html.getViewportWidth=function(){
return dojo.html._callDeprecated("getViewportWidth","getViewport",arguments,null,"width");
};
dojo.html.getViewportHeight=function(){
return dojo.html._callDeprecated("getViewportHeight","getViewport",arguments,null,"height");
};
dojo.html.getViewportSize=function(){
return dojo.html._callDeprecated("getViewportSize","getViewport",arguments);
};
dojo.html.getScrollTop=function(){
return dojo.html._callDeprecated("getScrollTop","getScroll",arguments,null,"top");
};
dojo.html.getScrollLeft=function(){
return dojo.html._callDeprecated("getScrollLeft","getScroll",arguments,null,"left");
};
dojo.html.getScrollOffset=function(){
return dojo.html._callDeprecated("getScrollOffset","getScroll",arguments,null,"offset");
};
dojo.provide("dojo.uri.Uri");
dojo.uri=new function(){
this.dojoUri=function(uri){
return new dojo.uri.Uri(dojo.hostenv.getBaseScriptUri(),uri);
};
this.moduleUri=function(_194,uri){
var loc=dojo.hostenv.getModuleSymbols(_194).join("/");
if(!loc){
return null;
}
if(loc.lastIndexOf("/")!=loc.length-1){
loc+="/";
}
var _197=loc.indexOf(":");
var _198=loc.indexOf("/");
if(loc.charAt(0)!="/"&&(_197==-1||_197>_198)){
loc=dojo.hostenv.getBaseScriptUri()+loc;
}
return new dojo.uri.Uri(loc,uri);
};
this.Uri=function(){
var uri=arguments[0];
for(var i=1;i<arguments.length;i++){
if(!arguments[i]){
continue;
}
var _19b=new dojo.uri.Uri(arguments[i].toString());
var _19c=new dojo.uri.Uri(uri.toString());
if((_19b.path=="")&&(_19b.scheme==null)&&(_19b.authority==null)&&(_19b.query==null)){
if(_19b.fragment!=null){
_19c.fragment=_19b.fragment;
}
_19b=_19c;
}else{
if(_19b.scheme==null){
_19b.scheme=_19c.scheme;
if(_19b.authority==null){
_19b.authority=_19c.authority;
if(_19b.path.charAt(0)!="/"){
var path=_19c.path.substring(0,_19c.path.lastIndexOf("/")+1)+_19b.path;
var segs=path.split("/");
for(var j=0;j<segs.length;j++){
if(segs[j]=="."){
if(j==segs.length-1){
segs[j]="";
}else{
segs.splice(j,1);
j--;
}
}else{
if(j>0&&!(j==1&&segs[0]=="")&&segs[j]==".."&&segs[j-1]!=".."){
if(j==segs.length-1){
segs.splice(j,1);
segs[j-1]="";
}else{
segs.splice(j-1,2);
j-=2;
}
}
}
}
_19b.path=segs.join("/");
}
}
}
}
uri="";
if(_19b.scheme!=null){
uri+=_19b.scheme+":";
}
if(_19b.authority!=null){
uri+="//"+_19b.authority;
}
uri+=_19b.path;
if(_19b.query!=null){
uri+="?"+_19b.query;
}
if(_19b.fragment!=null){
uri+="#"+_19b.fragment;
}
}
this.uri=uri.toString();
var _1a0="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=this.uri.match(new RegExp(_1a0));
this.scheme=r[2]||(r[1]?"":null);
this.authority=r[4]||(r[3]?"":null);
this.path=r[5];
this.query=r[7]||(r[6]?"":null);
this.fragment=r[9]||(r[8]?"":null);
if(this.authority!=null){
_1a0="^((([^:]+:)?([^@]+))@)?([^:]*)(:([0-9]+))?$";
r=this.authority.match(new RegExp(_1a0));
this.user=r[3]||null;
this.password=r[4]||null;
this.host=r[5];
this.port=r[7]||null;
}
this.toString=function(){
return this.uri;
};
};
};
dojo.provide("dojo.html.style");
dojo.html.getClass=function(node){
node=dojo.byId(node);
if(!node){
return "";
}
var cs="";
if(node.className){
cs=node.className;
}else{
if(dojo.html.hasAttribute(node,"class")){
cs=dojo.html.getAttribute(node,"class");
}
}
return cs.replace(/^\s+|\s+$/g,"");
};
dojo.html.getClasses=function(node){
var c=dojo.html.getClass(node);
return (c=="")?[]:c.split(/\s+/g);
};
dojo.html.hasClass=function(node,_1a7){
return (new RegExp("(^|\\s+)"+_1a7+"(\\s+|$)")).test(dojo.html.getClass(node));
};
dojo.html.prependClass=function(node,_1a9){
_1a9+=" "+dojo.html.getClass(node);
return dojo.html.setClass(node,_1a9);
};
dojo.html.addClass=function(node,_1ab){
if(dojo.html.hasClass(node,_1ab)){
return false;
}
_1ab=(dojo.html.getClass(node)+" "+_1ab).replace(/^\s+|\s+$/g,"");
return dojo.html.setClass(node,_1ab);
};
dojo.html.setClass=function(node,_1ad){
node=dojo.byId(node);
var cs=new String(_1ad);
try{
if(typeof node.className=="string"){
node.className=cs;
}else{
if(node.setAttribute){
node.setAttribute("class",_1ad);
node.className=cs;
}else{
return false;
}
}
}
catch(e){
dojo.debug("dojo.html.setClass() failed",e);
}
return true;
};
dojo.html.removeClass=function(node,_1b0,_1b1){
try{
if(!_1b1){
var _1b2=dojo.html.getClass(node).replace(new RegExp("(^|\\s+)"+_1b0+"(\\s+|$)"),"$1$2");
}else{
var _1b2=dojo.html.getClass(node).replace(_1b0,"");
}
dojo.html.setClass(node,_1b2);
}
catch(e){
dojo.debug("dojo.html.removeClass() failed",e);
}
return true;
};
dojo.html.replaceClass=function(node,_1b4,_1b5){
dojo.html.removeClass(node,_1b5);
dojo.html.addClass(node,_1b4);
};
dojo.html.classMatchType={ContainsAll:0,ContainsAny:1,IsOnly:2};
dojo.html.getElementsByClass=function(_1b6,_1b7,_1b8,_1b9,_1ba){
_1ba=false;
var _1bb=dojo.doc();
_1b7=dojo.byId(_1b7)||_1bb;
var _1bc=_1b6.split(/\s+/g);
var _1bd=[];
if(_1b9!=1&&_1b9!=2){
_1b9=0;
}
var _1be=new RegExp("(\\s|^)(("+_1bc.join(")|(")+"))(\\s|$)");
var _1bf=_1bc.join(" ").length;
var _1c0=[];
if(!_1ba&&_1bb.evaluate){
var _1c1=".//"+(_1b8||"*")+"[contains(";
if(_1b9!=dojo.html.classMatchType.ContainsAny){
_1c1+="concat(' ',@class,' '), ' "+_1bc.join(" ') and contains(concat(' ',@class,' '), ' ")+" ')";
if(_1b9==2){
_1c1+=" and string-length(@class)="+_1bf+"]";
}else{
_1c1+="]";
}
}else{
_1c1+="concat(' ',@class,' '), ' "+_1bc.join(" ') or contains(concat(' ',@class,' '), ' ")+" ')]";
}
var _1c2=_1bb.evaluate(_1c1,_1b7,null,XPathResult.ANY_TYPE,null);
var _1c3=_1c2.iterateNext();
while(_1c3){
try{
_1c0.push(_1c3);
_1c3=_1c2.iterateNext();
}
catch(e){
break;
}
}
return _1c0;
}else{
if(!_1b8){
_1b8="*";
}
_1c0=_1b7.getElementsByTagName(_1b8);
var node,i=0;
outer:
while(node=_1c0[i++]){
var _1c6=dojo.html.getClasses(node);
if(_1c6.length==0){
continue outer;
}
var _1c7=0;
for(var j=0;j<_1c6.length;j++){
if(_1be.test(_1c6[j])){
if(_1b9==dojo.html.classMatchType.ContainsAny){
_1bd.push(node);
continue outer;
}else{
_1c7++;
}
}else{
if(_1b9==dojo.html.classMatchType.IsOnly){
continue outer;
}
}
}
if(_1c7==_1bc.length){
if((_1b9==dojo.html.classMatchType.IsOnly)&&(_1c7==_1c6.length)){
_1bd.push(node);
}else{
if(_1b9==dojo.html.classMatchType.ContainsAll){
_1bd.push(node);
}
}
}
}
return _1bd;
}
};
dojo.html.getElementsByClassName=dojo.html.getElementsByClass;
dojo.html.toCamelCase=function(_1c9){
var arr=_1c9.split("-"),cc=arr[0];
for(var i=1;i<arr.length;i++){
cc+=arr[i].charAt(0).toUpperCase()+arr[i].substring(1);
}
return cc;
};
dojo.html.toSelectorCase=function(_1cd){
return _1cd.replace(/([A-Z])/g,"-$1").toLowerCase();
};
if(dojo.render.html.ie){
dojo.html.getComputedStyle=function(node,_1cf,_1d0){
node=dojo.byId(node);
if(!node||!node.style){
return _1d0;
}
return node.currentStyle[dojo.html.toCamelCase(_1cf)];
};
dojo.html.getComputedStyles=function(node){
return node.currentStyle;
};
}else{
dojo.html.getComputedStyle=function(node,_1d3,_1d4){
node=dojo.byId(node);
if(!node||!node.style){
return _1d4;
}
var s=document.defaultView.getComputedStyle(node,null);
return (s&&s[dojo.html.toCamelCase(_1d3)])||"";
};
dojo.html.getComputedStyles=function(node){
return document.defaultView.getComputedStyle(node,null);
};
}
dojo.html.getStyleProperty=function(node,_1d8){
node=dojo.byId(node);
return (node&&node.style?node.style[dojo.html.toCamelCase(_1d8)]:undefined);
};
dojo.html.getStyle=function(node,_1da){
var _1db=dojo.html.getStyleProperty(node,_1da);
return (_1db?_1db:dojo.html.getComputedStyle(node,_1da));
};
dojo.html.setStyle=function(node,_1dd,_1de){
node=dojo.byId(node);
if(node&&node.style){
var _1df=dojo.html.toCamelCase(_1dd);
node.style[_1df]=_1de;
}
};
dojo.html.setStyleText=function(_1e0,text){
try{
_1e0.style.cssText=text;
}
catch(e){
_1e0.setAttribute("style",text);
}
};
dojo.html.copyStyle=function(_1e2,_1e3){
if(!_1e3.style.cssText){
_1e2.setAttribute("style",_1e3.getAttribute("style"));
}else{
_1e2.style.cssText=_1e3.style.cssText;
}
dojo.html.addClass(_1e2,dojo.html.getClass(_1e3));
};
dojo.html.getUnitValue=function(node,_1e5,_1e6){
var s=dojo.html.getComputedStyle(node,_1e5);
if((!s)||((s=="auto")&&(_1e6))){
return {value:0,units:"px"};
}
var _1e8=s.match(/(\-?[\d.]+)([a-z%]*)/i);
if(!_1e8){
return dojo.html.getUnitValue.bad;
}
return {value:Number(_1e8[1]),units:_1e8[2].toLowerCase()};
};
dojo.html.getUnitValue.bad={value:NaN,units:""};
if(dojo.render.html.ie){
dojo.html.toPixelValue=function(_1e9,_1ea){
if(!_1ea){
return 0;
}
if(_1ea.slice(-2)=="px"){
return parseFloat(_1ea);
}
var _1eb=0;
with(_1e9){
var _1ec=style.left;
var _1ed=runtimeStyle.left;
runtimeStyle.left=currentStyle.left;
try{
style.left=_1ea||0;
_1eb=style.pixelLeft;
style.left=_1ec;
runtimeStyle.left=_1ed;
}
catch(e){
}
}
return _1eb;
};
}else{
dojo.html.toPixelValue=function(_1ee,_1ef){
return (_1ef&&(_1ef.slice(-2)=="px")?parseFloat(_1ef):0);
};
}
dojo.html.getPixelValue=function(node,_1f1,_1f2){
return dojo.html.toPixelValue(node,dojo.html.getComputedStyle(node,_1f1));
};
dojo.html.setPositivePixelValue=function(node,_1f4,_1f5){
if(isNaN(_1f5)){
return false;
}
node.style[_1f4]=Math.max(0,_1f5)+"px";
return true;
};
dojo.html.styleSheet=null;
dojo.html.insertCssRule=function(_1f6,_1f7,_1f8){
if(!dojo.html.styleSheet){
if(document.createStyleSheet){
dojo.html.styleSheet=document.createStyleSheet();
}else{
if(document.styleSheets[0]){
dojo.html.styleSheet=document.styleSheets[0];
}else{
return null;
}
}
}
if(arguments.length<3){
if(dojo.html.styleSheet.cssRules){
_1f8=dojo.html.styleSheet.cssRules.length;
}else{
if(dojo.html.styleSheet.rules){
_1f8=dojo.html.styleSheet.rules.length;
}else{
return null;
}
}
}
if(dojo.html.styleSheet.insertRule){
var rule=_1f6+" { "+_1f7+" }";
return dojo.html.styleSheet.insertRule(rule,_1f8);
}else{
if(dojo.html.styleSheet.addRule){
return dojo.html.styleSheet.addRule(_1f6,_1f7,_1f8);
}else{
return null;
}
}
};
dojo.html.removeCssRule=function(_1fa){
if(!dojo.html.styleSheet){
dojo.debug("no stylesheet defined for removing rules");
return false;
}
if(dojo.render.html.ie){
if(!_1fa){
_1fa=dojo.html.styleSheet.rules.length;
dojo.html.styleSheet.removeRule(_1fa);
}
}else{
if(document.styleSheets[0]){
if(!_1fa){
_1fa=dojo.html.styleSheet.cssRules.length;
}
dojo.html.styleSheet.deleteRule(_1fa);
}
}
return true;
};
dojo.html._insertedCssFiles=[];
dojo.html.insertCssFile=function(URI,doc,_1fd,_1fe){
if(!URI){
return;
}
if(!doc){
doc=document;
}
var _1ff=dojo.hostenv.getText(URI,false,_1fe);
if(_1ff===null){
return;
}
_1ff=dojo.html.fixPathsInCssText(_1ff,URI);
if(_1fd){
var idx=-1,node,ent=dojo.html._insertedCssFiles;
for(var i=0;i<ent.length;i++){
if((ent[i].doc==doc)&&(ent[i].cssText==_1ff)){
idx=i;
node=ent[i].nodeRef;
break;
}
}
if(node){
var _204=doc.getElementsByTagName("style");
for(var i=0;i<_204.length;i++){
if(_204[i]==node){
return;
}
}
dojo.html._insertedCssFiles.shift(idx,1);
}
}
var _205=dojo.html.insertCssText(_1ff,doc);
dojo.html._insertedCssFiles.push({"doc":doc,"cssText":_1ff,"nodeRef":_205});
if(_205&&djConfig.isDebug){
_205.setAttribute("dbgHref",URI);
}
return _205;
};
dojo.html.insertCssText=function(_206,doc,URI){
if(!_206){
return;
}
if(!doc){
doc=document;
}
if(URI){
_206=dojo.html.fixPathsInCssText(_206,URI);
}
var _209=doc.createElement("style");
_209.setAttribute("type","text/css");
var head=doc.getElementsByTagName("head")[0];
if(!head){
dojo.debug("No head tag in document, aborting styles");
return;
}else{
head.appendChild(_209);
}
if(_209.styleSheet){
var _20b=function(){
try{
_209.styleSheet.cssText=_206;
}
catch(e){
dojo.debug(e);
}
};
if(_209.styleSheet.disabled){
setTimeout(_20b,10);
}else{
_20b();
}
}else{
var _20c=doc.createTextNode(_206);
_209.appendChild(_20c);
}
return _209;
};
dojo.html.fixPathsInCssText=function(_20d,URI){
if(!_20d||!URI){
return;
}
var _20f,str="",url="",_212="[\\t\\s\\w\\(\\)\\/\\.\\\\'\"-:#=&?~]+";
var _213=new RegExp("url\\(\\s*("+_212+")\\s*\\)");
var _214=/(file|https?|ftps?):\/\//;
regexTrim=new RegExp("^[\\s]*(['\"]?)("+_212+")\\1[\\s]*?$");
if(dojo.render.html.ie55||dojo.render.html.ie60){
var _215=new RegExp("AlphaImageLoader\\((.*)src=['\"]("+_212+")['\"]");
while(_20f=_215.exec(_20d)){
url=_20f[2].replace(regexTrim,"$2");
if(!_214.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_20d.substring(0,_20f.index)+"AlphaImageLoader("+_20f[1]+"src='"+url+"'";
_20d=_20d.substr(_20f.index+_20f[0].length);
}
_20d=str+_20d;
str="";
}
while(_20f=_213.exec(_20d)){
url=_20f[1].replace(regexTrim,"$2");
if(!_214.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_20d.substring(0,_20f.index)+"url("+url+")";
_20d=_20d.substr(_20f.index+_20f[0].length);
}
return str+_20d;
};
dojo.html.setActiveStyleSheet=function(_216){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("title")){
a.disabled=true;
if(a.getAttribute("title")==_216){
a.disabled=false;
}
}
}
};
dojo.html.getActiveStyleSheet=function(){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("title")&&!a.disabled){
return a.getAttribute("title");
}
}
return null;
};
dojo.html.getPreferredStyleSheet=function(){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("rel").indexOf("alt")==-1&&a.getAttribute("title")){
return a.getAttribute("title");
}
}
return null;
};
dojo.html.applyBrowserClass=function(node){
var drh=dojo.render.html;
var _222={dj_ie:drh.ie,dj_ie55:drh.ie55,dj_ie6:drh.ie60,dj_ie7:drh.ie70,dj_iequirks:drh.ie&&drh.quirks,dj_opera:drh.opera,dj_opera8:drh.opera&&(Math.floor(dojo.render.version)==8),dj_opera9:drh.opera&&(Math.floor(dojo.render.version)==9),dj_khtml:drh.khtml,dj_safari:drh.safari,dj_gecko:drh.mozilla};
for(var p in _222){
if(_222[p]){
dojo.html.addClass(node,p);
}
}
};
dojo.kwCompoundRequire({common:["dojo.html.common","dojo.html.style"]});
dojo.provide("dojo.html.*");
dojo.provide("dojo.string.common");
dojo.string.trim=function(str,wh){
if(!str.replace){
return str;
}
if(!str.length){
return str;
}
var re=(wh>0)?(/^\s+/):(wh<0)?(/\s+$/):(/^\s+|\s+$/g);
return str.replace(re,"");
};
dojo.string.trimStart=function(str){
return dojo.string.trim(str,1);
};
dojo.string.trimEnd=function(str){
return dojo.string.trim(str,-1);
};
dojo.string.repeat=function(str,_22a,_22b){
var out="";
for(var i=0;i<_22a;i++){
out+=str;
if(_22b&&i<_22a-1){
out+=_22b;
}
}
return out;
};
dojo.string.pad=function(str,len,c,dir){
var out=String(str);
if(!c){
c="0";
}
if(!dir){
dir=1;
}
while(out.length<len){
if(dir>0){
out=c+out;
}else{
out+=c;
}
}
return out;
};
dojo.string.padLeft=function(str,len,c){
return dojo.string.pad(str,len,c,1);
};
dojo.string.padRight=function(str,len,c){
return dojo.string.pad(str,len,c,-1);
};
dojo.provide("dojo.string");
dojo.provide("dojo.lang.extras");
dojo.lang.setTimeout=function(func,_23a){
var _23b=window,_23c=2;
if(!dojo.lang.isFunction(func)){
_23b=func;
func=_23a;
_23a=arguments[2];
_23c++;
}
if(dojo.lang.isString(func)){
func=_23b[func];
}
var args=[];
for(var i=_23c;i<arguments.length;i++){
args.push(arguments[i]);
}
return dojo.global().setTimeout(function(){
func.apply(_23b,args);
},_23a);
};
dojo.lang.clearTimeout=function(_23f){
dojo.global().clearTimeout(_23f);
};
dojo.lang.getNameInObj=function(ns,item){
if(!ns){
ns=dj_global;
}
for(var x in ns){
if(ns[x]===item){
return new String(x);
}
}
return null;
};
dojo.lang.shallowCopy=function(obj,deep){
var i,ret;
if(obj===null){
return null;
}
if(dojo.lang.isObject(obj)){
ret=new obj.constructor();
for(i in obj){
if(dojo.lang.isUndefined(ret[i])){
ret[i]=deep?dojo.lang.shallowCopy(obj[i],deep):obj[i];
}
}
}else{
if(dojo.lang.isArray(obj)){
ret=[];
for(i=0;i<obj.length;i++){
ret[i]=deep?dojo.lang.shallowCopy(obj[i],deep):obj[i];
}
}else{
ret=obj;
}
}
return ret;
};
dojo.lang.firstValued=function(){
for(var i=0;i<arguments.length;i++){
if(typeof arguments[i]!="undefined"){
return arguments[i];
}
}
return undefined;
};
dojo.lang.getObjPathValue=function(_248,_249,_24a){
with(dojo.parseObjPath(_248,_249,_24a)){
return dojo.evalProp(prop,obj,_24a);
}
};
dojo.lang.setObjPathValue=function(_24b,_24c,_24d,_24e){
dojo.deprecated("dojo.lang.setObjPathValue","use dojo.parseObjPath and the '=' operator","0.6");
if(arguments.length<4){
_24e=true;
}
with(dojo.parseObjPath(_24b,_24d,_24e)){
if(obj&&(_24e||(prop in obj))){
obj[prop]=_24c;
}
}
};
dojo.provide("dojo.io.common");
dojo.io.transports=[];
dojo.io.hdlrFuncNames=["load","error","timeout"];
dojo.io.Request=function(url,_250,_251,_252){
if((arguments.length==1)&&(arguments[0].constructor==Object)){
this.fromKwArgs(arguments[0]);
}else{
this.url=url;
if(_250){
this.mimetype=_250;
}
if(_251){
this.transport=_251;
}
if(arguments.length>=4){
this.changeUrl=_252;
}
}
};
dojo.lang.extend(dojo.io.Request,{url:"",mimetype:"text/plain",method:"GET",content:undefined,transport:undefined,changeUrl:undefined,formNode:undefined,sync:false,bindSuccess:false,useCache:false,preventCache:false,load:function(type,data,_255,_256){
},error:function(type,_258,_259,_25a){
},timeout:function(type,_25c,_25d,_25e){
},handle:function(type,data,_261,_262){
},timeoutSeconds:0,abort:function(){
},fromKwArgs:function(_263){
if(_263["url"]){
_263.url=_263.url.toString();
}
if(_263["formNode"]){
_263.formNode=dojo.byId(_263.formNode);
}
if(!_263["method"]&&_263["formNode"]&&_263["formNode"].method){
_263.method=_263["formNode"].method;
}
if(!_263["handle"]&&_263["handler"]){
_263.handle=_263.handler;
}
if(!_263["load"]&&_263["loaded"]){
_263.load=_263.loaded;
}
if(!_263["changeUrl"]&&_263["changeURL"]){
_263.changeUrl=_263.changeURL;
}
_263.encoding=dojo.lang.firstValued(_263["encoding"],djConfig["bindEncoding"],"");
_263.sendTransport=dojo.lang.firstValued(_263["sendTransport"],djConfig["ioSendTransport"],false);
var _264=dojo.lang.isFunction;
for(var x=0;x<dojo.io.hdlrFuncNames.length;x++){
var fn=dojo.io.hdlrFuncNames[x];
if(_263[fn]&&_264(_263[fn])){
continue;
}
if(_263["handle"]&&_264(_263["handle"])){
_263[fn]=_263.handle;
}
}
dojo.lang.mixin(this,_263);
}});
dojo.io.Error=function(msg,type,num){
this.message=msg;
this.type=type||"unknown";
this.number=num||0;
};
dojo.io.transports.addTransport=function(name){
this.push(name);
this[name]=dojo.io[name];
};
dojo.io.bind=function(_26b){
if(!(_26b instanceof dojo.io.Request)){
try{
_26b=new dojo.io.Request(_26b);
}
catch(e){
dojo.debug(e);
}
}
var _26c="";
if(_26b["transport"]){
_26c=_26b["transport"];
if(!this[_26c]){
dojo.io.sendBindError(_26b,"No dojo.io.bind() transport with name '"+_26b["transport"]+"'.");
return _26b;
}
if(!this[_26c].canHandle(_26b)){
dojo.io.sendBindError(_26b,"dojo.io.bind() transport with name '"+_26b["transport"]+"' cannot handle this type of request.");
return _26b;
}
}else{
for(var x=0;x<dojo.io.transports.length;x++){
var tmp=dojo.io.transports[x];
if((this[tmp])&&(this[tmp].canHandle(_26b))){
_26c=tmp;
break;
}
}
if(_26c==""){
dojo.io.sendBindError(_26b,"None of the loaded transports for dojo.io.bind()"+" can handle the request.");
return _26b;
}
}
this[_26c].bind(_26b);
_26b.bindSuccess=true;
return _26b;
};
dojo.io.sendBindError=function(_26f,_270){
if((typeof _26f.error=="function"||typeof _26f.handle=="function")&&(typeof setTimeout=="function"||typeof setTimeout=="object")){
var _271=new dojo.io.Error(_270);
setTimeout(function(){
_26f[(typeof _26f.error=="function")?"error":"handle"]("error",_271,null,_26f);
},50);
}else{
dojo.raise(_270);
}
};
dojo.io.queueBind=function(_272){
if(!(_272 instanceof dojo.io.Request)){
try{
_272=new dojo.io.Request(_272);
}
catch(e){
dojo.debug(e);
}
}
var _273=_272.load;
_272.load=function(){
dojo.io._queueBindInFlight=false;
var ret=_273.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
var _275=_272.error;
_272.error=function(){
dojo.io._queueBindInFlight=false;
var ret=_275.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
dojo.io._bindQueue.push(_272);
dojo.io._dispatchNextQueueBind();
return _272;
};
dojo.io._dispatchNextQueueBind=function(){
if(!dojo.io._queueBindInFlight){
dojo.io._queueBindInFlight=true;
if(dojo.io._bindQueue.length>0){
dojo.io.bind(dojo.io._bindQueue.shift());
}else{
dojo.io._queueBindInFlight=false;
}
}
};
dojo.io._bindQueue=[];
dojo.io._queueBindInFlight=false;
dojo.io.argsFromMap=function(map,_278,last){
var enc=/utf/i.test(_278||"")?encodeURIComponent:dojo.string.encodeAscii;
var _27b=[];
var _27c=new Object();
for(var name in map){
var _27e=function(elt){
var val=enc(name)+"="+enc(elt);
_27b[(last==name)?"push":"unshift"](val);
};
if(!_27c[name]){
var _281=map[name];
if(dojo.lang.isArray(_281)){
dojo.lang.forEach(_281,_27e);
}else{
_27e(_281);
}
}
}
return _27b.join("&");
};
dojo.io.setIFrameSrc=function(_282,src,_284){
try{
var r=dojo.render.html;
if(!_284){
if(r.safari){
_282.location=src;
}else{
frames[_282.name].location=src;
}
}else{
var idoc;
if(r.ie){
idoc=_282.contentWindow.document;
}else{
if(r.safari){
idoc=_282.document;
}else{
idoc=_282.contentWindow;
}
}
if(!idoc){
_282.location=src;
return;
}else{
idoc.location.replace(src);
}
}
}
catch(e){
dojo.debug(e);
dojo.debug("setIFrameSrc: "+e);
}
};
dojo.provide("dojo.lang.array");
dojo.lang.mixin(dojo.lang,{has:function(obj,name){
try{
return typeof obj[name]!="undefined";
}
catch(e){
return false;
}
},isEmpty:function(obj){
if(dojo.lang.isObject(obj)){
var tmp={};
var _28b=0;
for(var x in obj){
if(obj[x]&&(!tmp[x])){
_28b++;
break;
}
}
return _28b==0;
}else{
if(dojo.lang.isArrayLike(obj)||dojo.lang.isString(obj)){
return obj.length==0;
}
}
},map:function(arr,obj,_28f){
var _290=dojo.lang.isString(arr);
if(_290){
arr=arr.split("");
}
if(dojo.lang.isFunction(obj)&&(!_28f)){
_28f=obj;
obj=dj_global;
}else{
if(dojo.lang.isFunction(obj)&&_28f){
var _291=obj;
obj=_28f;
_28f=_291;
}
}
if(Array.map){
var _292=Array.map(arr,_28f,obj);
}else{
var _292=[];
for(var i=0;i<arr.length;++i){
_292.push(_28f.call(obj,arr[i]));
}
}
if(_290){
return _292.join("");
}else{
return _292;
}
},reduce:function(arr,_295,obj,_297){
var _298=_295;
if(arguments.length==2){
_297=_295;
_298=arr[0];
arr=arr.slice(1);
}else{
if(arguments.length==3){
if(dojo.lang.isFunction(obj)){
_297=obj;
obj=null;
}
}else{
if(dojo.lang.isFunction(obj)){
var tmp=_297;
_297=obj;
obj=tmp;
}
}
}
var ob=obj||dj_global;
dojo.lang.map(arr,function(val){
_298=_297.call(ob,_298,val);
});
return _298;
},forEach:function(_29c,_29d,_29e){
if(dojo.lang.isString(_29c)){
_29c=_29c.split("");
}
if(Array.forEach){
Array.forEach(_29c,_29d,_29e);
}else{
if(!_29e){
_29e=dj_global;
}
for(var i=0,l=_29c.length;i<l;i++){
_29d.call(_29e,_29c[i],i,_29c);
}
}
},_everyOrSome:function(_2a1,arr,_2a3,_2a4){
if(dojo.lang.isString(arr)){
arr=arr.split("");
}
if(Array.every){
return Array[_2a1?"every":"some"](arr,_2a3,_2a4);
}else{
if(!_2a4){
_2a4=dj_global;
}
for(var i=0,l=arr.length;i<l;i++){
var _2a7=_2a3.call(_2a4,arr[i],i,arr);
if(_2a1&&!_2a7){
return false;
}else{
if((!_2a1)&&(_2a7)){
return true;
}
}
}
return Boolean(_2a1);
}
},every:function(arr,_2a9,_2aa){
return this._everyOrSome(true,arr,_2a9,_2aa);
},some:function(arr,_2ac,_2ad){
return this._everyOrSome(false,arr,_2ac,_2ad);
},filter:function(arr,_2af,_2b0){
var _2b1=dojo.lang.isString(arr);
if(_2b1){
arr=arr.split("");
}
var _2b2;
if(Array.filter){
_2b2=Array.filter(arr,_2af,_2b0);
}else{
if(!_2b0){
if(arguments.length>=3){
dojo.raise("thisObject doesn't exist!");
}
_2b0=dj_global;
}
_2b2=[];
for(var i=0;i<arr.length;i++){
if(_2af.call(_2b0,arr[i],i,arr)){
_2b2.push(arr[i]);
}
}
}
if(_2b1){
return _2b2.join("");
}else{
return _2b2;
}
},unnest:function(){
var out=[];
for(var i=0;i<arguments.length;i++){
if(dojo.lang.isArrayLike(arguments[i])){
var add=dojo.lang.unnest.apply(this,arguments[i]);
out=out.concat(add);
}else{
out.push(arguments[i]);
}
}
return out;
},toArray:function(_2b7,_2b8){
var _2b9=[];
for(var i=_2b8||0;i<_2b7.length;i++){
_2b9.push(_2b7[i]);
}
return _2b9;
}});
dojo.provide("dojo.lang.func");
dojo.lang.hitch=function(_2bb,_2bc){
var fcn=(dojo.lang.isString(_2bc)?_2bb[_2bc]:_2bc)||function(){
};
return function(){
return fcn.apply(_2bb,arguments);
};
};
dojo.lang.anonCtr=0;
dojo.lang.anon={};
dojo.lang.nameAnonFunc=function(_2be,_2bf,_2c0){
var nso=(_2bf||dojo.lang.anon);
if((_2c0)||((dj_global["djConfig"])&&(djConfig["slowAnonFuncLookups"]==true))){
for(var x in nso){
try{
if(nso[x]===_2be){
return x;
}
}
catch(e){
}
}
}
var ret="__"+dojo.lang.anonCtr++;
while(typeof nso[ret]!="undefined"){
ret="__"+dojo.lang.anonCtr++;
}
nso[ret]=_2be;
return ret;
};
dojo.lang.forward=function(_2c4){
return function(){
return this[_2c4].apply(this,arguments);
};
};
dojo.lang.curry=function(_2c5,func){
var _2c7=[];
_2c5=_2c5||dj_global;
if(dojo.lang.isString(func)){
func=_2c5[func];
}
for(var x=2;x<arguments.length;x++){
_2c7.push(arguments[x]);
}
var _2c9=(func["__preJoinArity"]||func.length)-_2c7.length;
function gather(_2ca,_2cb,_2cc){
var _2cd=_2cc;
var _2ce=_2cb.slice(0);
for(var x=0;x<_2ca.length;x++){
_2ce.push(_2ca[x]);
}
_2cc=_2cc-_2ca.length;
if(_2cc<=0){
var res=func.apply(_2c5,_2ce);
_2cc=_2cd;
return res;
}else{
return function(){
return gather(arguments,_2ce,_2cc);
};
}
}
return gather([],_2c7,_2c9);
};
dojo.lang.curryArguments=function(_2d1,func,args,_2d4){
var _2d5=[];
var x=_2d4||0;
for(x=_2d4;x<args.length;x++){
_2d5.push(args[x]);
}
return dojo.lang.curry.apply(dojo.lang,[_2d1,func].concat(_2d5));
};
dojo.lang.tryThese=function(){
for(var x=0;x<arguments.length;x++){
try{
if(typeof arguments[x]=="function"){
var ret=(arguments[x]());
if(ret){
return ret;
}
}
}
catch(e){
dojo.debug(e);
}
}
};
dojo.lang.delayThese=function(farr,cb,_2db,_2dc){
if(!farr.length){
if(typeof _2dc=="function"){
_2dc();
}
return;
}
if((typeof _2db=="undefined")&&(typeof cb=="number")){
_2db=cb;
cb=function(){
};
}else{
if(!cb){
cb=function(){
};
if(!_2db){
_2db=0;
}
}
}
setTimeout(function(){
(farr.shift())();
cb();
dojo.lang.delayThese(farr,cb,_2db,_2dc);
},_2db);
};
dojo.provide("dojo.string.extras");
dojo.string.substituteParams=function(_2dd,hash){
var map=(typeof hash=="object")?hash:dojo.lang.toArray(arguments,1);
return _2dd.replace(/\%\{(\w+)\}/g,function(_2e0,key){
if(typeof (map[key])!="undefined"&&map[key]!=null){
return map[key];
}
dojo.raise("Substitution not found: "+key);
});
};
dojo.string.capitalize=function(str){
if(!dojo.lang.isString(str)){
return "";
}
if(arguments.length==0){
str=this;
}
var _2e3=str.split(" ");
for(var i=0;i<_2e3.length;i++){
_2e3[i]=_2e3[i].charAt(0).toUpperCase()+_2e3[i].substring(1);
}
return _2e3.join(" ");
};
dojo.string.isBlank=function(str){
if(!dojo.lang.isString(str)){
return true;
}
return (dojo.string.trim(str).length==0);
};
dojo.string.encodeAscii=function(str){
if(!dojo.lang.isString(str)){
return str;
}
var ret="";
var _2e8=escape(str);
var _2e9,re=/%u([0-9A-F]{4})/i;
while((_2e9=_2e8.match(re))){
var num=Number("0x"+_2e9[1]);
var _2ec=escape("&#"+num+";");
ret+=_2e8.substring(0,_2e9.index)+_2ec;
_2e8=_2e8.substring(_2e9.index+_2e9[0].length);
}
ret+=_2e8.replace(/\+/g,"%2B");
return ret;
};
dojo.string.escape=function(type,str){
var args=dojo.lang.toArray(arguments,1);
switch(type.toLowerCase()){
case "xml":
case "html":
case "xhtml":
return dojo.string.escapeXml.apply(this,args);
case "sql":
return dojo.string.escapeSql.apply(this,args);
case "regexp":
case "regex":
return dojo.string.escapeRegExp.apply(this,args);
case "javascript":
case "jscript":
case "js":
return dojo.string.escapeJavaScript.apply(this,args);
case "ascii":
return dojo.string.encodeAscii.apply(this,args);
default:
return str;
}
};
dojo.string.escapeXml=function(str,_2f1){
str=str.replace(/&/gm,"&amp;").replace(/</gm,"&lt;").replace(/>/gm,"&gt;").replace(/"/gm,"&quot;");
if(!_2f1){
str=str.replace(/'/gm,"&#39;");
}
return str;
};
dojo.string.escapeSql=function(str){
return str.replace(/'/gm,"''");
};
dojo.string.escapeRegExp=function(str){
return str.replace(/\\/gm,"\\\\").replace(/([\f\b\n\t\r[\^$|?*+(){}])/gm,"\\$1");
};
dojo.string.escapeJavaScript=function(str){
return str.replace(/(["'\f\b\n\t\r])/gm,"\\$1");
};
dojo.string.escapeString=function(str){
return ("\""+str.replace(/(["\\])/g,"\\$1")+"\"").replace(/[\f]/g,"\\f").replace(/[\b]/g,"\\b").replace(/[\n]/g,"\\n").replace(/[\t]/g,"\\t").replace(/[\r]/g,"\\r");
};
dojo.string.summary=function(str,len){
if(!len||str.length<=len){
return str;
}
return str.substring(0,len).replace(/\.+$/,"")+"...";
};
dojo.string.endsWith=function(str,end,_2fa){
if(_2fa){
str=str.toLowerCase();
end=end.toLowerCase();
}
if((str.length-end.length)<0){
return false;
}
return str.lastIndexOf(end)==str.length-end.length;
};
dojo.string.endsWithAny=function(str){
for(var i=1;i<arguments.length;i++){
if(dojo.string.endsWith(str,arguments[i])){
return true;
}
}
return false;
};
dojo.string.startsWith=function(str,_2fe,_2ff){
if(_2ff){
str=str.toLowerCase();
_2fe=_2fe.toLowerCase();
}
return str.indexOf(_2fe)==0;
};
dojo.string.startsWithAny=function(str){
for(var i=1;i<arguments.length;i++){
if(dojo.string.startsWith(str,arguments[i])){
return true;
}
}
return false;
};
dojo.string.has=function(str){
for(var i=1;i<arguments.length;i++){
if(str.indexOf(arguments[i])>-1){
return true;
}
}
return false;
};
dojo.string.normalizeNewlines=function(text,_305){
if(_305=="\n"){
text=text.replace(/\r\n/g,"\n");
text=text.replace(/\r/g,"\n");
}else{
if(_305=="\r"){
text=text.replace(/\r\n/g,"\r");
text=text.replace(/\n/g,"\r");
}else{
text=text.replace(/([^\r])\n/g,"$1\r\n").replace(/\r([^\n])/g,"\r\n$1");
}
}
return text;
};
dojo.string.splitEscaped=function(str,_307){
var _308=[];
for(var i=0,_30a=0;i<str.length;i++){
if(str.charAt(i)=="\\"){
i++;
continue;
}
if(str.charAt(i)==_307){
_308.push(str.substring(_30a,i));
_30a=i+1;
}
}
_308.push(str.substr(_30a));
return _308;
};
dojo.provide("dojo.undo.browser");
try{
if((!djConfig["preventBackButtonFix"])&&(!dojo.hostenv.post_load_)){
document.write("<iframe style='border: 0px; width: 1px; height: 1px; position: absolute; bottom: 0px; right: 0px; visibility: visible;' name='djhistory' id='djhistory' src='"+(djConfig["dojoIframeHistoryUrl"]||dojo.hostenv.getBaseScriptUri()+"iframe_history.html")+"'></iframe>");
}
}
catch(e){
}
if(dojo.render.html.opera){
dojo.debug("Opera is not supported with dojo.undo.browser, so back/forward detection will not work.");
}
dojo.undo.browser={initialHref:(!dj_undef("window"))?window.location.href:"",initialHash:(!dj_undef("window"))?window.location.hash:"",moveForward:false,historyStack:[],forwardStack:[],historyIframe:null,bookmarkAnchor:null,locationTimer:null,setInitialState:function(args){
this.initialState=this._createState(this.initialHref,args,this.initialHash);
},addToHistory:function(args){
this.forwardStack=[];
var hash=null;
var url=null;
if(!this.historyIframe){
if(djConfig["useXDomain"]&&!djConfig["dojoIframeHistoryUrl"]){
dojo.debug("dojo.undo.browser: When using cross-domain Dojo builds,"+" please save iframe_history.html to your domain and set djConfig.dojoIframeHistoryUrl"+" to the path on your domain to iframe_history.html");
}
this.historyIframe=window.frames["djhistory"];
}
if(!this.bookmarkAnchor){
this.bookmarkAnchor=document.createElement("a");
dojo.body().appendChild(this.bookmarkAnchor);
this.bookmarkAnchor.style.display="none";
}
if(args["changeUrl"]){
hash="#"+((args["changeUrl"]!==true)?args["changeUrl"]:(new Date()).getTime());
if(this.historyStack.length==0&&this.initialState.urlHash==hash){
this.initialState=this._createState(url,args,hash);
return;
}else{
if(this.historyStack.length>0&&this.historyStack[this.historyStack.length-1].urlHash==hash){
this.historyStack[this.historyStack.length-1]=this._createState(url,args,hash);
return;
}
}
this.changingUrl=true;
setTimeout("window.location.href = '"+hash+"'; dojo.undo.browser.changingUrl = false;",1);
this.bookmarkAnchor.href=hash;
if(dojo.render.html.ie){
url=this._loadIframeHistory();
var _30f=args["back"]||args["backButton"]||args["handle"];
var tcb=function(_311){
if(window.location.hash!=""){
setTimeout("window.location.href = '"+hash+"';",1);
}
_30f.apply(this,[_311]);
};
if(args["back"]){
args.back=tcb;
}else{
if(args["backButton"]){
args.backButton=tcb;
}else{
if(args["handle"]){
args.handle=tcb;
}
}
}
var _312=args["forward"]||args["forwardButton"]||args["handle"];
var tfw=function(_314){
if(window.location.hash!=""){
window.location.href=hash;
}
if(_312){
_312.apply(this,[_314]);
}
};
if(args["forward"]){
args.forward=tfw;
}else{
if(args["forwardButton"]){
args.forwardButton=tfw;
}else{
if(args["handle"]){
args.handle=tfw;
}
}
}
}else{
if(dojo.render.html.moz){
if(!this.locationTimer){
this.locationTimer=setInterval("dojo.undo.browser.checkLocation();",200);
}
}
}
}else{
url=this._loadIframeHistory();
}
this.historyStack.push(this._createState(url,args,hash));
},checkLocation:function(){
if(!this.changingUrl){
var hsl=this.historyStack.length;
if((window.location.hash==this.initialHash||window.location.href==this.initialHref)&&(hsl==1)){
this.handleBackButton();
return;
}
if(this.forwardStack.length>0){
if(this.forwardStack[this.forwardStack.length-1].urlHash==window.location.hash){
this.handleForwardButton();
return;
}
}
if((hsl>=2)&&(this.historyStack[hsl-2])){
if(this.historyStack[hsl-2].urlHash==window.location.hash){
this.handleBackButton();
return;
}
}
}
},iframeLoaded:function(evt,_317){
if(!dojo.render.html.opera){
var _318=this._getUrlQuery(_317.href);
if(_318==null){
if(this.historyStack.length==1){
this.handleBackButton();
}
return;
}
if(this.moveForward){
this.moveForward=false;
return;
}
if(this.historyStack.length>=2&&_318==this._getUrlQuery(this.historyStack[this.historyStack.length-2].url)){
this.handleBackButton();
}else{
if(this.forwardStack.length>0&&_318==this._getUrlQuery(this.forwardStack[this.forwardStack.length-1].url)){
this.handleForwardButton();
}
}
}
},handleBackButton:function(){
var _319=this.historyStack.pop();
if(!_319){
return;
}
var last=this.historyStack[this.historyStack.length-1];
if(!last&&this.historyStack.length==0){
last=this.initialState;
}
if(last){
if(last.kwArgs["back"]){
last.kwArgs["back"]();
}else{
if(last.kwArgs["backButton"]){
last.kwArgs["backButton"]();
}else{
if(last.kwArgs["handle"]){
last.kwArgs.handle("back");
}
}
}
}
this.forwardStack.push(_319);
},handleForwardButton:function(){
var last=this.forwardStack.pop();
if(!last){
return;
}
if(last.kwArgs["forward"]){
last.kwArgs.forward();
}else{
if(last.kwArgs["forwardButton"]){
last.kwArgs.forwardButton();
}else{
if(last.kwArgs["handle"]){
last.kwArgs.handle("forward");
}
}
}
this.historyStack.push(last);
},_createState:function(url,args,hash){
return {"url":url,"kwArgs":args,"urlHash":hash};
},_getUrlQuery:function(url){
var _320=url.split("?");
if(_320.length<2){
return null;
}else{
return _320[1];
}
},_loadIframeHistory:function(){
var url=(djConfig["dojoIframeHistoryUrl"]||dojo.hostenv.getBaseScriptUri()+"iframe_history.html")+"?"+(new Date()).getTime();
this.moveForward=true;
dojo.io.setIFrameSrc(this.historyIframe,url,false);
return url;
}};
dojo.provide("dojo.io.BrowserIO");
if(!dj_undef("window")){
dojo.io.checkChildrenForFile=function(node){
var _323=false;
var _324=node.getElementsByTagName("input");
dojo.lang.forEach(_324,function(_325){
if(_323){
return;
}
if(_325.getAttribute("type")=="file"){
_323=true;
}
});
return _323;
};
dojo.io.formHasFile=function(_326){
return dojo.io.checkChildrenForFile(_326);
};
dojo.io.updateNode=function(node,_328){
node=dojo.byId(node);
var args=_328;
if(dojo.lang.isString(_328)){
args={url:_328};
}
args.mimetype="text/html";
args.load=function(t,d,e){
while(node.firstChild){
dojo.dom.destroyNode(node.firstChild);
}
node.innerHTML=d;
};
dojo.io.bind(args);
};
dojo.io.formFilter=function(node){
var type=(node.type||"").toLowerCase();
return !node.disabled&&node.name&&!dojo.lang.inArray(["file","submit","image","reset","button"],type);
};
dojo.io.encodeForm=function(_32f,_330,_331){
if((!_32f)||(!_32f.tagName)||(!_32f.tagName.toLowerCase()=="form")){
dojo.raise("Attempted to encode a non-form element.");
}
if(!_331){
_331=dojo.io.formFilter;
}
var enc=/utf/i.test(_330||"")?encodeURIComponent:dojo.string.encodeAscii;
var _333=[];
for(var i=0;i<_32f.elements.length;i++){
var elm=_32f.elements[i];
if(!elm||elm.tagName.toLowerCase()=="fieldset"||!_331(elm)){
continue;
}
var name=enc(elm.name);
var type=elm.type.toLowerCase();
if(type=="select-multiple"){
for(var j=0;j<elm.options.length;j++){
if(elm.options[j].selected){
_333.push(name+"="+enc(elm.options[j].value));
}
}
}else{
if(dojo.lang.inArray(["radio","checkbox"],type)){
if(elm.checked){
_333.push(name+"="+enc(elm.value));
}
}else{
_333.push(name+"="+enc(elm.value));
}
}
}
var _339=_32f.getElementsByTagName("input");
for(var i=0;i<_339.length;i++){
var _33a=_339[i];
if(_33a.type.toLowerCase()=="image"&&_33a.form==_32f&&_331(_33a)){
var name=enc(_33a.name);
_333.push(name+"="+enc(_33a.value));
_333.push(name+".x=0");
_333.push(name+".y=0");
}
}
return _333.join("&")+"&";
};
dojo.io.FormBind=function(args){
this.bindArgs={};
if(args&&args.formNode){
this.init(args);
}else{
if(args){
this.init({formNode:args});
}
}
};
dojo.lang.extend(dojo.io.FormBind,{form:null,bindArgs:null,clickedButton:null,init:function(args){
var form=dojo.byId(args.formNode);
if(!form||!form.tagName||form.tagName.toLowerCase()!="form"){
throw new Error("FormBind: Couldn't apply, invalid form");
}else{
if(this.form==form){
return;
}else{
if(this.form){
throw new Error("FormBind: Already applied to a form");
}
}
}
dojo.lang.mixin(this.bindArgs,args);
this.form=form;
this.connect(form,"onsubmit","submit");
for(var i=0;i<form.elements.length;i++){
var node=form.elements[i];
if(node&&node.type&&dojo.lang.inArray(["submit","button"],node.type.toLowerCase())){
this.connect(node,"onclick","click");
}
}
var _340=form.getElementsByTagName("input");
for(var i=0;i<_340.length;i++){
var _341=_340[i];
if(_341.type.toLowerCase()=="image"&&_341.form==form){
this.connect(_341,"onclick","click");
}
}
},onSubmit:function(form){
return true;
},submit:function(e){
e.preventDefault();
if(this.onSubmit(this.form)){
dojo.io.bind(dojo.lang.mixin(this.bindArgs,{formFilter:dojo.lang.hitch(this,"formFilter")}));
}
},click:function(e){
var node=e.currentTarget;
if(node.disabled){
return;
}
this.clickedButton=node;
},formFilter:function(node){
var type=(node.type||"").toLowerCase();
var _348=false;
if(node.disabled||!node.name){
_348=false;
}else{
if(dojo.lang.inArray(["submit","button","image"],type)){
if(!this.clickedButton){
this.clickedButton=node;
}
_348=node==this.clickedButton;
}else{
_348=!dojo.lang.inArray(["file","submit","reset","button"],type);
}
}
return _348;
},connect:function(_349,_34a,_34b){
if(dojo.evalObjPath("dojo.event.connect")){
dojo.event.connect(_349,_34a,this,_34b);
}else{
var fcn=dojo.lang.hitch(this,_34b);
_349[_34a]=function(e){
if(!e){
e=window.event;
}
if(!e.currentTarget){
e.currentTarget=e.srcElement;
}
if(!e.preventDefault){
e.preventDefault=function(){
window.event.returnValue=false;
};
}
fcn(e);
};
}
}});
dojo.io.XMLHTTPTransport=new function(){
var _34e=this;
var _34f={};
this.useCache=false;
this.preventCache=false;
function getCacheKey(url,_351,_352){
return url+"|"+_351+"|"+_352.toLowerCase();
}
function addToCache(url,_354,_355,http){
_34f[getCacheKey(url,_354,_355)]=http;
}
function getFromCache(url,_358,_359){
return _34f[getCacheKey(url,_358,_359)];
}
this.clearCache=function(){
_34f={};
};
function doLoad(_35a,http,url,_35d,_35e){
if(((http.status>=200)&&(http.status<300))||(http.status==304)||(location.protocol=="file:"&&(http.status==0||http.status==undefined))||(location.protocol=="chrome:"&&(http.status==0||http.status==undefined))){
var ret;
if(_35a.method.toLowerCase()=="head"){
var _360=http.getAllResponseHeaders();
ret={};
ret.toString=function(){
return _360;
};
var _361=_360.split(/[\r\n]+/g);
for(var i=0;i<_361.length;i++){
var pair=_361[i].match(/^([^:]+)\s*:\s*(.+)$/i);
if(pair){
ret[pair[1]]=pair[2];
}
}
}else{
if(_35a.mimetype=="text/javascript"){
try{
ret=dj_eval(http.responseText);
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=null;
}
}else{
if(_35a.mimetype=="text/json"||_35a.mimetype=="application/json"){
try{
ret=dj_eval("("+http.responseText+")");
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=false;
}
}else{
if((_35a.mimetype=="application/xml")||(_35a.mimetype=="text/xml")){
ret=http.responseXML;
if(!ret||typeof ret=="string"||!http.getResponseHeader("Content-Type")){
ret=dojo.dom.createDocumentFromText(http.responseText);
}
}else{
ret=http.responseText;
}
}
}
}
if(_35e){
addToCache(url,_35d,_35a.method,http);
}
_35a[(typeof _35a.load=="function")?"load":"handle"]("load",ret,http,_35a);
}else{
var _364=new dojo.io.Error("XMLHttpTransport Error: "+http.status+" "+http.statusText);
_35a[(typeof _35a.error=="function")?"error":"handle"]("error",_364,http,_35a);
}
}
function setHeaders(http,_366){
if(_366["headers"]){
for(var _367 in _366["headers"]){
if(_367.toLowerCase()=="content-type"&&!_366["contentType"]){
_366["contentType"]=_366["headers"][_367];
}else{
http.setRequestHeader(_367,_366["headers"][_367]);
}
}
}
}
this.inFlight=[];
this.inFlightTimer=null;
this.startWatchingInFlight=function(){
if(!this.inFlightTimer){
this.inFlightTimer=setTimeout("dojo.io.XMLHTTPTransport.watchInFlight();",10);
}
};
this.watchInFlight=function(){
var now=null;
if(!dojo.hostenv._blockAsync&&!_34e._blockAsync){
for(var x=this.inFlight.length-1;x>=0;x--){
try{
var tif=this.inFlight[x];
if(!tif||tif.http._aborted||!tif.http.readyState){
this.inFlight.splice(x,1);
continue;
}
if(4==tif.http.readyState){
this.inFlight.splice(x,1);
doLoad(tif.req,tif.http,tif.url,tif.query,tif.useCache);
}else{
if(tif.startTime){
if(!now){
now=(new Date()).getTime();
}
if(tif.startTime+(tif.req.timeoutSeconds*1000)<now){
if(typeof tif.http.abort=="function"){
tif.http.abort();
}
this.inFlight.splice(x,1);
tif.req[(typeof tif.req.timeout=="function")?"timeout":"handle"]("timeout",null,tif.http,tif.req);
}
}
}
}
catch(e){
try{
var _36b=new dojo.io.Error("XMLHttpTransport.watchInFlight Error: "+e);
tif.req[(typeof tif.req.error=="function")?"error":"handle"]("error",_36b,tif.http,tif.req);
}
catch(e2){
dojo.debug("XMLHttpTransport error callback failed: "+e2);
}
}
}
}
clearTimeout(this.inFlightTimer);
if(this.inFlight.length==0){
this.inFlightTimer=null;
return;
}
this.inFlightTimer=setTimeout("dojo.io.XMLHTTPTransport.watchInFlight();",10);
};
var _36c=dojo.hostenv.getXmlhttpObject()?true:false;
this.canHandle=function(_36d){
return _36c&&dojo.lang.inArray(["text/plain","text/html","application/xml","text/xml","text/javascript","text/json","application/json"],(_36d["mimetype"].toLowerCase()||""))&&!(_36d["formNode"]&&dojo.io.formHasFile(_36d["formNode"]));
};
this.multipartBoundary="45309FFF-BD65-4d50-99C9-36986896A96F";
this.bind=function(_36e){
if(!_36e["url"]){
if(!_36e["formNode"]&&(_36e["backButton"]||_36e["back"]||_36e["changeUrl"]||_36e["watchForURL"])&&(!djConfig.preventBackButtonFix)){
dojo.deprecated("Using dojo.io.XMLHTTPTransport.bind() to add to browser history without doing an IO request","Use dojo.undo.browser.addToHistory() instead.","0.4");
dojo.undo.browser.addToHistory(_36e);
return true;
}
}
var url=_36e.url;
var _370="";
if(_36e["formNode"]){
var ta=_36e.formNode.getAttribute("action");
if((ta)&&(!_36e["url"])){
url=ta;
}
var tp=_36e.formNode.getAttribute("method");
if((tp)&&(!_36e["method"])){
_36e.method=tp;
}
_370+=dojo.io.encodeForm(_36e.formNode,_36e.encoding,_36e["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
if(_36e["file"]){
_36e.method="post";
}
if(!_36e["method"]){
_36e.method="get";
}
if(_36e.method.toLowerCase()=="get"){
_36e.multipart=false;
}else{
if(_36e["file"]){
_36e.multipart=true;
}else{
if(!_36e["multipart"]){
_36e.multipart=false;
}
}
}
if(_36e["backButton"]||_36e["back"]||_36e["changeUrl"]){
dojo.undo.browser.addToHistory(_36e);
}
var _373=_36e["content"]||{};
if(_36e.sendTransport){
_373["dojo.transport"]="xmlhttp";
}
do{
if(_36e.postContent){
_370=_36e.postContent;
break;
}
if(_373){
_370+=dojo.io.argsFromMap(_373,_36e.encoding);
}
if(_36e.method.toLowerCase()=="get"||!_36e.multipart){
break;
}
var t=[];
if(_370.length){
var q=_370.split("&");
for(var i=0;i<q.length;++i){
if(q[i].length){
var p=q[i].split("=");
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+p[0]+"\"","",p[1]);
}
}
}
if(_36e.file){
if(dojo.lang.isArray(_36e.file)){
for(var i=0;i<_36e.file.length;++i){
var o=_36e.file[i];
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}else{
var o=_36e.file;
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}
if(t.length){
t.push("--"+this.multipartBoundary+"--","");
_370=t.join("\r\n");
}
}while(false);
var _379=_36e["sync"]?false:true;
var _37a=_36e["preventCache"]||(this.preventCache==true&&_36e["preventCache"]!=false);
var _37b=_36e["useCache"]==true||(this.useCache==true&&_36e["useCache"]!=false);
if(!_37a&&_37b){
var _37c=getFromCache(url,_370,_36e.method);
if(_37c){
doLoad(_36e,_37c,url,_370,false);
return;
}
}
var http=dojo.hostenv.getXmlhttpObject(_36e);
var _37e=false;
if(_379){
var _37f=this.inFlight.push({"req":_36e,"http":http,"url":url,"query":_370,"useCache":_37b,"startTime":_36e.timeoutSeconds?(new Date()).getTime():0});
this.startWatchingInFlight();
}else{
_34e._blockAsync=true;
}
if(_36e.method.toLowerCase()=="post"){
if(!_36e.user){
http.open("POST",url,_379);
}else{
http.open("POST",url,_379,_36e.user,_36e.password);
}
setHeaders(http,_36e);
http.setRequestHeader("Content-Type",_36e.multipart?("multipart/form-data; boundary="+this.multipartBoundary):(_36e.contentType||"application/x-www-form-urlencoded"));
try{
http.send(_370);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_36e,{status:404},url,_370,_37b);
}
}else{
var _380=url;
if(_370!=""){
_380+=(_380.indexOf("?")>-1?"&":"?")+_370;
}
if(_37a){
_380+=(dojo.string.endsWithAny(_380,"?","&")?"":(_380.indexOf("?")>-1?"&":"?"))+"dojo.preventCache="+new Date().valueOf();
}
if(!_36e.user){
http.open(_36e.method.toUpperCase(),_380,_379);
}else{
http.open(_36e.method.toUpperCase(),_380,_379,_36e.user,_36e.password);
}
setHeaders(http,_36e);
try{
http.send(null);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_36e,{status:404},url,_370,_37b);
}
}
if(!_379){
doLoad(_36e,http,url,_370,_37b);
_34e._blockAsync=false;
}
_36e.abort=function(){
try{
http._aborted=true;
}
catch(e){
}
return http.abort();
};
return;
};
dojo.io.transports.addTransport("XMLHTTPTransport");
};
}
dojo.provide("dojo.io.cookie");
dojo.io.cookie.setCookie=function(name,_382,days,path,_385,_386){
var _387=-1;
if((typeof days=="number")&&(days>=0)){
var d=new Date();
d.setTime(d.getTime()+(days*24*60*60*1000));
_387=d.toGMTString();
}
_382=escape(_382);
document.cookie=name+"="+_382+";"+(_387!=-1?" expires="+_387+";":"")+(path?"path="+path:"")+(_385?"; domain="+_385:"")+(_386?"; secure":"");
};
dojo.io.cookie.set=dojo.io.cookie.setCookie;
dojo.io.cookie.getCookie=function(name){
var idx=document.cookie.lastIndexOf(name+"=");
if(idx==-1){
return null;
}
var _38b=document.cookie.substring(idx+name.length+1);
var end=_38b.indexOf(";");
if(end==-1){
end=_38b.length;
}
_38b=_38b.substring(0,end);
_38b=unescape(_38b);
return _38b;
};
dojo.io.cookie.get=dojo.io.cookie.getCookie;
dojo.io.cookie.deleteCookie=function(name){
dojo.io.cookie.setCookie(name,"-",0);
};
dojo.io.cookie.setObjectCookie=function(name,obj,days,path,_392,_393,_394){
if(arguments.length==5){
_394=_392;
_392=null;
_393=null;
}
var _395=[],_396,_397="";
if(!_394){
_396=dojo.io.cookie.getObjectCookie(name);
}
if(days>=0){
if(!_396){
_396={};
}
for(var prop in obj){
if(obj[prop]==null){
delete _396[prop];
}else{
if((typeof obj[prop]=="string")||(typeof obj[prop]=="number")){
_396[prop]=obj[prop];
}
}
}
prop=null;
for(var prop in _396){
_395.push(escape(prop)+"="+escape(_396[prop]));
}
_397=_395.join("&");
}
dojo.io.cookie.setCookie(name,_397,days,path,_392,_393);
};
dojo.io.cookie.getObjectCookie=function(name){
var _39a=null,_39b=dojo.io.cookie.getCookie(name);
if(_39b){
_39a={};
var _39c=_39b.split("&");
for(var i=0;i<_39c.length;i++){
var pair=_39c[i].split("=");
var _39f=pair[1];
if(isNaN(_39f)){
_39f=unescape(pair[1]);
}
_39a[unescape(pair[0])]=_39f;
}
}
return _39a;
};
dojo.io.cookie.isSupported=function(){
if(typeof navigator.cookieEnabled!="boolean"){
dojo.io.cookie.setCookie("__TestingYourBrowserForCookieSupport__","CookiesAllowed",90,null);
var _3a0=dojo.io.cookie.getCookie("__TestingYourBrowserForCookieSupport__");
navigator.cookieEnabled=(_3a0=="CookiesAllowed");
if(navigator.cookieEnabled){
this.deleteCookie("__TestingYourBrowserForCookieSupport__");
}
}
return navigator.cookieEnabled;
};
if(!dojo.io.cookies){
dojo.io.cookies=dojo.io.cookie;
}
dojo.kwCompoundRequire({common:["dojo.io.common"],rhino:["dojo.io.RhinoIO"],browser:["dojo.io.BrowserIO","dojo.io.cookie"],dashboard:["dojo.io.BrowserIO","dojo.io.cookie"]});
dojo.provide("dojo.io.*");
dojo.provide("dojo.event.common");
dojo.event=new function(){
this._canTimeout=dojo.lang.isFunction(dj_global["setTimeout"])||dojo.lang.isAlien(dj_global["setTimeout"]);
function interpolateArgs(args,_3a2){
var dl=dojo.lang;
var ao={srcObj:dj_global,srcFunc:null,adviceObj:dj_global,adviceFunc:null,aroundObj:null,aroundFunc:null,adviceType:(args.length>2)?args[0]:"after",precedence:"last",once:false,delay:null,rate:0,adviceMsg:false,maxCalls:-1};
switch(args.length){
case 0:
return;
case 1:
return;
case 2:
ao.srcFunc=args[0];
ao.adviceFunc=args[1];
break;
case 3:
if((dl.isObject(args[0]))&&(dl.isString(args[1]))&&(dl.isString(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
}else{
if((dl.isString(args[1]))&&(dl.isString(args[2]))){
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
}else{
if((dl.isObject(args[0]))&&(dl.isString(args[1]))&&(dl.isFunction(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
var _3a5=dl.nameAnonFunc(args[2],ao.adviceObj,_3a2);
ao.adviceFunc=_3a5;
}else{
if((dl.isFunction(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))){
ao.adviceType="after";
ao.srcObj=dj_global;
var _3a5=dl.nameAnonFunc(args[0],ao.srcObj,_3a2);
ao.srcFunc=_3a5;
ao.adviceObj=args[1];
ao.adviceFunc=args[2];
}
}
}
}
break;
case 4:
if((dl.isObject(args[0]))&&(dl.isObject(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isString(args[1]))&&(dl.isObject(args[2]))){
ao.adviceType=args[0];
ao.srcObj=dj_global;
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isFunction(args[1]))&&(dl.isObject(args[2]))){
ao.adviceType=args[0];
ao.srcObj=dj_global;
var _3a5=dl.nameAnonFunc(args[1],dj_global,_3a2);
ao.srcFunc=_3a5;
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))&&(dl.isFunction(args[3]))){
ao.srcObj=args[1];
ao.srcFunc=args[2];
var _3a5=dl.nameAnonFunc(args[3],dj_global,_3a2);
ao.adviceObj=dj_global;
ao.adviceFunc=_3a5;
}else{
if(dl.isObject(args[1])){
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=dj_global;
ao.adviceFunc=args[3];
}else{
if(dl.isObject(args[2])){
ao.srcObj=dj_global;
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
ao.srcObj=ao.adviceObj=ao.aroundObj=dj_global;
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
ao.aroundFunc=args[3];
}
}
}
}
}
}
break;
case 6:
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=args[3];
ao.adviceFunc=args[4];
ao.aroundFunc=args[5];
ao.aroundObj=dj_global;
break;
default:
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=args[3];
ao.adviceFunc=args[4];
ao.aroundObj=args[5];
ao.aroundFunc=args[6];
ao.once=args[7];
ao.delay=args[8];
ao.rate=args[9];
ao.adviceMsg=args[10];
ao.maxCalls=(!isNaN(parseInt(args[11])))?args[11]:-1;
break;
}
if(dl.isFunction(ao.aroundFunc)){
var _3a5=dl.nameAnonFunc(ao.aroundFunc,ao.aroundObj,_3a2);
ao.aroundFunc=_3a5;
}
if(dl.isFunction(ao.srcFunc)){
ao.srcFunc=dl.getNameInObj(ao.srcObj,ao.srcFunc);
}
if(dl.isFunction(ao.adviceFunc)){
ao.adviceFunc=dl.getNameInObj(ao.adviceObj,ao.adviceFunc);
}
if((ao.aroundObj)&&(dl.isFunction(ao.aroundFunc))){
ao.aroundFunc=dl.getNameInObj(ao.aroundObj,ao.aroundFunc);
}
if(!ao.srcObj){
dojo.raise("bad srcObj for srcFunc: "+ao.srcFunc);
}
if(!ao.adviceObj){
dojo.raise("bad adviceObj for adviceFunc: "+ao.adviceFunc);
}
if(!ao.adviceFunc){
dojo.debug("bad adviceFunc for srcFunc: "+ao.srcFunc);
dojo.debugShallow(ao);
}
return ao;
}
this.connect=function(){
if(arguments.length==1){
var ao=arguments[0];
}else{
var ao=interpolateArgs(arguments,true);
}
if(dojo.lang.isArray(ao.srcObj)&&ao.srcObj!=""){
var _3a7={};
for(var x in ao){
_3a7[x]=ao[x];
}
var mjps=[];
dojo.lang.forEach(ao.srcObj,function(src){
if((dojo.render.html.capable)&&(dojo.lang.isString(src))){
src=dojo.byId(src);
}
_3a7.srcObj=src;
mjps.push(dojo.event.connect.call(dojo.event,_3a7));
});
return mjps;
}
var mjp=dojo.event.MethodJoinPoint.getForMethod(ao.srcObj,ao.srcFunc);
if(ao.adviceFunc){
var mjp2=dojo.event.MethodJoinPoint.getForMethod(ao.adviceObj,ao.adviceFunc);
}
mjp.kwAddAdvice(ao);
return mjp;
};
this.log=function(a1,a2){
var _3af;
if((arguments.length==1)&&(typeof a1=="object")){
_3af=a1;
}else{
_3af={srcObj:a1,srcFunc:a2};
}
_3af.adviceFunc=function(){
var _3b0=[];
for(var x=0;x<arguments.length;x++){
_3b0.push(arguments[x]);
}
dojo.debug("("+_3af.srcObj+")."+_3af.srcFunc,":",_3b0.join(", "));
};
this.kwConnect(_3af);
};
this.connectBefore=function(){
var args=["before"];
for(var i=0;i<arguments.length;i++){
args.push(arguments[i]);
}
return this.connect.apply(this,args);
};
this.connectAround=function(){
var args=["around"];
for(var i=0;i<arguments.length;i++){
args.push(arguments[i]);
}
return this.connect.apply(this,args);
};
this.connectOnce=function(){
var ao=interpolateArgs(arguments,true);
ao.once=true;
return this.connect(ao);
};
this.connectRunOnce=function(){
var ao=interpolateArgs(arguments,true);
ao.maxCalls=1;
return this.connect(ao);
};
this._kwConnectImpl=function(_3b8,_3b9){
var fn=(_3b9)?"disconnect":"connect";
if(typeof _3b8["srcFunc"]=="function"){
_3b8.srcObj=_3b8["srcObj"]||dj_global;
var _3bb=dojo.lang.nameAnonFunc(_3b8.srcFunc,_3b8.srcObj,true);
_3b8.srcFunc=_3bb;
}
if(typeof _3b8["adviceFunc"]=="function"){
_3b8.adviceObj=_3b8["adviceObj"]||dj_global;
var _3bb=dojo.lang.nameAnonFunc(_3b8.adviceFunc,_3b8.adviceObj,true);
_3b8.adviceFunc=_3bb;
}
_3b8.srcObj=_3b8["srcObj"]||dj_global;
_3b8.adviceObj=_3b8["adviceObj"]||_3b8["targetObj"]||dj_global;
_3b8.adviceFunc=_3b8["adviceFunc"]||_3b8["targetFunc"];
return dojo.event[fn](_3b8);
};
this.kwConnect=function(_3bc){
return this._kwConnectImpl(_3bc,false);
};
this.disconnect=function(){
if(arguments.length==1){
var ao=arguments[0];
}else{
var ao=interpolateArgs(arguments,true);
}
if(!ao.adviceFunc){
return;
}
if(dojo.lang.isString(ao.srcFunc)&&(ao.srcFunc.toLowerCase()=="onkey")){
if(dojo.render.html.ie){
ao.srcFunc="onkeydown";
this.disconnect(ao);
}
ao.srcFunc="onkeypress";
}
if(!ao.srcObj[ao.srcFunc]){
return null;
}
var mjp=dojo.event.MethodJoinPoint.getForMethod(ao.srcObj,ao.srcFunc,true);
mjp.removeAdvice(ao.adviceObj,ao.adviceFunc,ao.adviceType,ao.once);
return mjp;
};
this.kwDisconnect=function(_3bf){
return this._kwConnectImpl(_3bf,true);
};
};
dojo.event.MethodInvocation=function(_3c0,obj,args){
this.jp_=_3c0;
this.object=obj;
this.args=[];
for(var x=0;x<args.length;x++){
this.args[x]=args[x];
}
this.around_index=-1;
};
dojo.event.MethodInvocation.prototype.proceed=function(){
this.around_index++;
if(this.around_index>=this.jp_.around.length){
return this.jp_.object[this.jp_.methodname].apply(this.jp_.object,this.args);
}else{
var ti=this.jp_.around[this.around_index];
var mobj=ti[0]||dj_global;
var meth=ti[1];
return mobj[meth].call(mobj,this);
}
};
dojo.event.MethodJoinPoint=function(obj,_3c8){
this.object=obj||dj_global;
this.methodname=_3c8;
this.methodfunc=this.object[_3c8];
this.squelch=false;
};
dojo.event.MethodJoinPoint.getForMethod=function(obj,_3ca){
if(!obj){
obj=dj_global;
}
var ofn=obj[_3ca];
if(!ofn){
ofn=obj[_3ca]=function(){
};
if(!obj[_3ca]){
dojo.raise("Cannot set do-nothing method on that object "+_3ca);
}
}else{
if((typeof ofn!="function")&&(!dojo.lang.isFunction(ofn))&&(!dojo.lang.isAlien(ofn))){
return null;
}
}
var _3cc=_3ca+"$joinpoint";
var _3cd=_3ca+"$joinpoint$method";
var _3ce=obj[_3cc];
if(!_3ce){
var _3cf=false;
if(dojo.event["browser"]){
if((obj["attachEvent"])||(obj["nodeType"])||(obj["addEventListener"])){
_3cf=true;
dojo.event.browser.addClobberNodeAttrs(obj,[_3cc,_3cd,_3ca]);
}
}
var _3d0=ofn.length;
obj[_3cd]=ofn;
_3ce=obj[_3cc]=new dojo.event.MethodJoinPoint(obj,_3cd);
if(!_3cf){
obj[_3ca]=function(){
return _3ce.run.apply(_3ce,arguments);
};
}else{
obj[_3ca]=function(){
var args=[];
if(!arguments.length){
var evt=null;
try{
if(obj.ownerDocument){
evt=obj.ownerDocument.parentWindow.event;
}else{
if(obj.documentElement){
evt=obj.documentElement.ownerDocument.parentWindow.event;
}else{
if(obj.event){
evt=obj.event;
}else{
evt=window.event;
}
}
}
}
catch(e){
evt=window.event;
}
if(evt){
args.push(dojo.event.browser.fixEvent(evt,this));
}
}else{
for(var x=0;x<arguments.length;x++){
if((x==0)&&(dojo.event.browser.isEvent(arguments[x]))){
args.push(dojo.event.browser.fixEvent(arguments[x],this));
}else{
args.push(arguments[x]);
}
}
}
return _3ce.run.apply(_3ce,args);
};
}
obj[_3ca].__preJoinArity=_3d0;
}
return _3ce;
};
dojo.lang.extend(dojo.event.MethodJoinPoint,{squelch:false,unintercept:function(){
this.object[this.methodname]=this.methodfunc;
this.before=[];
this.after=[];
this.around=[];
},disconnect:dojo.lang.forward("unintercept"),run:function(){
var obj=this.object||dj_global;
var args=arguments;
var _3d6=[];
for(var x=0;x<args.length;x++){
_3d6[x]=args[x];
}
var _3d8=function(marr){
if(!marr){
dojo.debug("Null argument to unrollAdvice()");
return;
}
var _3da=marr[0]||dj_global;
var _3db=marr[1];
if(!_3da[_3db]){
dojo.raise("function \""+_3db+"\" does not exist on \""+_3da+"\"");
}
var _3dc=marr[2]||dj_global;
var _3dd=marr[3];
var msg=marr[6];
var _3df=marr[7];
if(_3df>-1){
if(_3df==0){
return;
}
marr[7]--;
}
var _3e0;
var to={args:[],jp_:this,object:obj,proceed:function(){
return _3da[_3db].apply(_3da,to.args);
}};
to.args=_3d6;
var _3e2=parseInt(marr[4]);
var _3e3=((!isNaN(_3e2))&&(marr[4]!==null)&&(typeof marr[4]!="undefined"));
if(marr[5]){
var rate=parseInt(marr[5]);
var cur=new Date();
var _3e6=false;
if((marr["last"])&&((cur-marr.last)<=rate)){
if(dojo.event._canTimeout){
if(marr["delayTimer"]){
clearTimeout(marr.delayTimer);
}
var tod=parseInt(rate*2);
var mcpy=dojo.lang.shallowCopy(marr);
marr.delayTimer=setTimeout(function(){
mcpy[5]=0;
_3d8(mcpy);
},tod);
}
return;
}else{
marr.last=cur;
}
}
if(_3dd){
_3dc[_3dd].call(_3dc,to);
}else{
if((_3e3)&&((dojo.render.html)||(dojo.render.svg))){
dj_global["setTimeout"](function(){
if(msg){
_3da[_3db].call(_3da,to);
}else{
_3da[_3db].apply(_3da,args);
}
},_3e2);
}else{
if(msg){
_3da[_3db].call(_3da,to);
}else{
_3da[_3db].apply(_3da,args);
}
}
}
};
var _3e9=function(){
if(this.squelch){
try{
return _3d8.apply(this,arguments);
}
catch(e){
dojo.debug(e);
}
}else{
return _3d8.apply(this,arguments);
}
};
if((this["before"])&&(this.before.length>0)){
dojo.lang.forEach(this.before.concat(new Array()),_3e9);
}
var _3ea;
try{
if((this["around"])&&(this.around.length>0)){
var mi=new dojo.event.MethodInvocation(this,obj,args);
_3ea=mi.proceed();
}else{
if(this.methodfunc){
_3ea=this.object[this.methodname].apply(this.object,args);
}
}
}
catch(e){
if(!this.squelch){
dojo.debug(e,"when calling",this.methodname,"on",this.object,"with arguments",args);
dojo.raise(e);
}
}
if((this["after"])&&(this.after.length>0)){
dojo.lang.forEach(this.after.concat(new Array()),_3e9);
}
return (this.methodfunc)?_3ea:null;
},getArr:function(kind){
var type="after";
if((typeof kind=="string")&&(kind.indexOf("before")!=-1)){
type="before";
}else{
if(kind=="around"){
type="around";
}
}
if(!this[type]){
this[type]=[];
}
return this[type];
},kwAddAdvice:function(args){
this.addAdvice(args["adviceObj"],args["adviceFunc"],args["aroundObj"],args["aroundFunc"],args["adviceType"],args["precedence"],args["once"],args["delay"],args["rate"],args["adviceMsg"],args["maxCalls"]);
},addAdvice:function(_3ef,_3f0,_3f1,_3f2,_3f3,_3f4,once,_3f6,rate,_3f8,_3f9){
var arr=this.getArr(_3f3);
if(!arr){
dojo.raise("bad this: "+this);
}
var ao=[_3ef,_3f0,_3f1,_3f2,_3f6,rate,_3f8,_3f9];
if(once){
if(this.hasAdvice(_3ef,_3f0,_3f3,arr)>=0){
return;
}
}
if(_3f4=="first"){
arr.unshift(ao);
}else{
arr.push(ao);
}
},hasAdvice:function(_3fc,_3fd,_3fe,arr){
if(!arr){
arr=this.getArr(_3fe);
}
var ind=-1;
for(var x=0;x<arr.length;x++){
var aao=(typeof _3fd=="object")?(new String(_3fd)).toString():_3fd;
var a1o=(typeof arr[x][1]=="object")?(new String(arr[x][1])).toString():arr[x][1];
if((arr[x][0]==_3fc)&&(a1o==aao)){
ind=x;
}
}
return ind;
},removeAdvice:function(_404,_405,_406,once){
var arr=this.getArr(_406);
var ind=this.hasAdvice(_404,_405,_406,arr);
if(ind==-1){
return false;
}
while(ind!=-1){
arr.splice(ind,1);
if(once){
break;
}
ind=this.hasAdvice(_404,_405,_406,arr);
}
return true;
}});
dojo.provide("dojo.event.topic");
dojo.event.topic=new function(){
this.topics={};
this.getTopic=function(_40a){
if(!this.topics[_40a]){
this.topics[_40a]=new this.TopicImpl(_40a);
}
return this.topics[_40a];
};
this.registerPublisher=function(_40b,obj,_40d){
var _40b=this.getTopic(_40b);
_40b.registerPublisher(obj,_40d);
};
this.subscribe=function(_40e,obj,_410){
var _40e=this.getTopic(_40e);
_40e.subscribe(obj,_410);
};
this.unsubscribe=function(_411,obj,_413){
var _411=this.getTopic(_411);
_411.unsubscribe(obj,_413);
};
this.destroy=function(_414){
this.getTopic(_414).destroy();
delete this.topics[_414];
};
this.publishApply=function(_415,args){
var _415=this.getTopic(_415);
_415.sendMessage.apply(_415,args);
};
this.publish=function(_417,_418){
var _417=this.getTopic(_417);
var args=[];
for(var x=1;x<arguments.length;x++){
args.push(arguments[x]);
}
_417.sendMessage.apply(_417,args);
};
};
dojo.event.topic.TopicImpl=function(_41b){
this.topicName=_41b;
this.subscribe=function(_41c,_41d){
var tf=_41d||_41c;
var to=(!_41d)?dj_global:_41c;
return dojo.event.kwConnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this.unsubscribe=function(_420,_421){
var tf=(!_421)?_420:_421;
var to=(!_421)?null:_420;
return dojo.event.kwDisconnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this._getJoinPoint=function(){
return dojo.event.MethodJoinPoint.getForMethod(this,"sendMessage");
};
this.setSquelch=function(_424){
this._getJoinPoint().squelch=_424;
};
this.destroy=function(){
this._getJoinPoint().disconnect();
};
this.registerPublisher=function(_425,_426){
dojo.event.connect(_425,_426,this,"sendMessage");
};
this.sendMessage=function(_427){
};
};
dojo.provide("dojo.event.browser");
dojo._ie_clobber=new function(){
this.clobberNodes=[];
function nukeProp(node,prop){
try{
node[prop]=null;
}
catch(e){
}
try{
delete node[prop];
}
catch(e){
}
try{
node.removeAttribute(prop);
}
catch(e){
}
}
this.clobber=function(_42a){
var na;
var tna;
if(_42a){
tna=_42a.all||_42a.getElementsByTagName("*");
na=[_42a];
for(var x=0;x<tna.length;x++){
if(tna[x]["__doClobber__"]){
na.push(tna[x]);
}
}
}else{
try{
window.onload=null;
}
catch(e){
}
na=(this.clobberNodes.length)?this.clobberNodes:document.all;
}
tna=null;
var _42e={};
for(var i=na.length-1;i>=0;i=i-1){
var el=na[i];
try{
if(el&&el["__clobberAttrs__"]){
for(var j=0;j<el.__clobberAttrs__.length;j++){
nukeProp(el,el.__clobberAttrs__[j]);
}
nukeProp(el,"__clobberAttrs__");
nukeProp(el,"__doClobber__");
}
}
catch(e){
}
}
na=null;
};
};
if(dojo.render.html.ie){
dojo.addOnUnload(function(){
dojo._ie_clobber.clobber();
try{
if((dojo["widget"])&&(dojo.widget["manager"])){
dojo.widget.manager.destroyAll();
}
}
catch(e){
}
if(dojo.widget){
for(var name in dojo.widget._templateCache){
if(dojo.widget._templateCache[name].node){
dojo.dom.destroyNode(dojo.widget._templateCache[name].node);
dojo.widget._templateCache[name].node=null;
delete dojo.widget._templateCache[name].node;
}
}
}
try{
window.onload=null;
}
catch(e){
}
try{
window.onunload=null;
}
catch(e){
}
dojo._ie_clobber.clobberNodes=[];
});
}
dojo.event.browser=new function(){
var _433=0;
this.normalizedEventName=function(_434){
switch(_434){
case "CheckboxStateChange":
case "DOMAttrModified":
case "DOMMenuItemActive":
case "DOMMenuItemInactive":
case "DOMMouseScroll":
case "DOMNodeInserted":
case "DOMNodeRemoved":
case "RadioStateChange":
return _434;
break;
default:
var lcn=_434.toLowerCase();
return (lcn.indexOf("on")==0)?lcn.substr(2):lcn;
break;
}
};
this.clean=function(node){
if(dojo.render.html.ie){
dojo._ie_clobber.clobber(node);
}
};
this.addClobberNode=function(node){
if(!dojo.render.html.ie){
return;
}
if(!node["__doClobber__"]){
node.__doClobber__=true;
dojo._ie_clobber.clobberNodes.push(node);
node.__clobberAttrs__=[];
}
};
this.addClobberNodeAttrs=function(node,_439){
if(!dojo.render.html.ie){
return;
}
this.addClobberNode(node);
for(var x=0;x<_439.length;x++){
node.__clobberAttrs__.push(_439[x]);
}
};
this.removeListener=function(node,_43c,fp,_43e){
if(!_43e){
var _43e=false;
}
_43c=dojo.event.browser.normalizedEventName(_43c);
if(_43c=="key"){
if(dojo.render.html.ie){
this.removeListener(node,"onkeydown",fp,_43e);
}
_43c="keypress";
}
if(node.removeEventListener){
node.removeEventListener(_43c,fp,_43e);
}
};
this.addListener=function(node,_440,fp,_442,_443){
if(!node){
return;
}
if(!_442){
var _442=false;
}
_440=dojo.event.browser.normalizedEventName(_440);
if(_440=="key"){
if(dojo.render.html.ie){
this.addListener(node,"onkeydown",fp,_442,_443);
}
_440="keypress";
}
if(!_443){
var _444=function(evt){
if(!evt){
evt=window.event;
}
var ret=fp(dojo.event.browser.fixEvent(evt,this));
if(_442){
dojo.event.browser.stopEvent(evt);
}
return ret;
};
}else{
_444=fp;
}
if(node.addEventListener){
node.addEventListener(_440,_444,_442);
return _444;
}else{
_440="on"+_440;
if(typeof node[_440]=="function"){
var _447=node[_440];
node[_440]=function(e){
_447(e);
return _444(e);
};
}else{
node[_440]=_444;
}
if(dojo.render.html.ie){
this.addClobberNodeAttrs(node,[_440]);
}
return _444;
}
};
this.isEvent=function(obj){
return (typeof obj!="undefined")&&(obj)&&(typeof Event!="undefined")&&(obj.eventPhase);
};
this.currentEvent=null;
this.callListener=function(_44a,_44b){
if(typeof _44a!="function"){
dojo.raise("listener not a function: "+_44a);
}
dojo.event.browser.currentEvent.currentTarget=_44b;
return _44a.call(_44b,dojo.event.browser.currentEvent);
};
this._stopPropagation=function(){
dojo.event.browser.currentEvent.cancelBubble=true;
};
this._preventDefault=function(){
dojo.event.browser.currentEvent.returnValue=false;
};
this.keys={KEY_BACKSPACE:8,KEY_TAB:9,KEY_CLEAR:12,KEY_ENTER:13,KEY_SHIFT:16,KEY_CTRL:17,KEY_ALT:18,KEY_PAUSE:19,KEY_CAPS_LOCK:20,KEY_ESCAPE:27,KEY_SPACE:32,KEY_PAGE_UP:33,KEY_PAGE_DOWN:34,KEY_END:35,KEY_HOME:36,KEY_LEFT_ARROW:37,KEY_UP_ARROW:38,KEY_RIGHT_ARROW:39,KEY_DOWN_ARROW:40,KEY_INSERT:45,KEY_DELETE:46,KEY_HELP:47,KEY_LEFT_WINDOW:91,KEY_RIGHT_WINDOW:92,KEY_SELECT:93,KEY_NUMPAD_0:96,KEY_NUMPAD_1:97,KEY_NUMPAD_2:98,KEY_NUMPAD_3:99,KEY_NUMPAD_4:100,KEY_NUMPAD_5:101,KEY_NUMPAD_6:102,KEY_NUMPAD_7:103,KEY_NUMPAD_8:104,KEY_NUMPAD_9:105,KEY_NUMPAD_MULTIPLY:106,KEY_NUMPAD_PLUS:107,KEY_NUMPAD_ENTER:108,KEY_NUMPAD_MINUS:109,KEY_NUMPAD_PERIOD:110,KEY_NUMPAD_DIVIDE:111,KEY_F1:112,KEY_F2:113,KEY_F3:114,KEY_F4:115,KEY_F5:116,KEY_F6:117,KEY_F7:118,KEY_F8:119,KEY_F9:120,KEY_F10:121,KEY_F11:122,KEY_F12:123,KEY_F13:124,KEY_F14:125,KEY_F15:126,KEY_NUM_LOCK:144,KEY_SCROLL_LOCK:145};
this.revKeys=[];
for(var key in this.keys){
this.revKeys[this.keys[key]]=key;
}
this.fixEvent=function(evt,_44e){
if(!evt){
if(window["event"]){
evt=window.event;
}
}
if((evt["type"])&&(evt["type"].indexOf("key")==0)){
evt.keys=this.revKeys;
for(var key in this.keys){
evt[key]=this.keys[key];
}
if(evt["type"]=="keydown"&&dojo.render.html.ie){
switch(evt.keyCode){
case evt.KEY_SHIFT:
case evt.KEY_CTRL:
case evt.KEY_ALT:
case evt.KEY_CAPS_LOCK:
case evt.KEY_LEFT_WINDOW:
case evt.KEY_RIGHT_WINDOW:
case evt.KEY_SELECT:
case evt.KEY_NUM_LOCK:
case evt.KEY_SCROLL_LOCK:
case evt.KEY_NUMPAD_0:
case evt.KEY_NUMPAD_1:
case evt.KEY_NUMPAD_2:
case evt.KEY_NUMPAD_3:
case evt.KEY_NUMPAD_4:
case evt.KEY_NUMPAD_5:
case evt.KEY_NUMPAD_6:
case evt.KEY_NUMPAD_7:
case evt.KEY_NUMPAD_8:
case evt.KEY_NUMPAD_9:
case evt.KEY_NUMPAD_PERIOD:
break;
case evt.KEY_NUMPAD_MULTIPLY:
case evt.KEY_NUMPAD_PLUS:
case evt.KEY_NUMPAD_ENTER:
case evt.KEY_NUMPAD_MINUS:
case evt.KEY_NUMPAD_DIVIDE:
break;
case evt.KEY_PAUSE:
case evt.KEY_TAB:
case evt.KEY_BACKSPACE:
case evt.KEY_ENTER:
case evt.KEY_ESCAPE:
case evt.KEY_PAGE_UP:
case evt.KEY_PAGE_DOWN:
case evt.KEY_END:
case evt.KEY_HOME:
case evt.KEY_LEFT_ARROW:
case evt.KEY_UP_ARROW:
case evt.KEY_RIGHT_ARROW:
case evt.KEY_DOWN_ARROW:
case evt.KEY_INSERT:
case evt.KEY_DELETE:
case evt.KEY_F1:
case evt.KEY_F2:
case evt.KEY_F3:
case evt.KEY_F4:
case evt.KEY_F5:
case evt.KEY_F6:
case evt.KEY_F7:
case evt.KEY_F8:
case evt.KEY_F9:
case evt.KEY_F10:
case evt.KEY_F11:
case evt.KEY_F12:
case evt.KEY_F12:
case evt.KEY_F13:
case evt.KEY_F14:
case evt.KEY_F15:
case evt.KEY_CLEAR:
case evt.KEY_HELP:
evt.key=evt.keyCode;
break;
default:
if(evt.ctrlKey||evt.altKey){
var _450=evt.keyCode;
if(_450>=65&&_450<=90&&evt.shiftKey==false){
_450+=32;
}
if(_450>=1&&_450<=26&&evt.ctrlKey){
_450+=96;
}
evt.key=String.fromCharCode(_450);
}
}
}else{
if(evt["type"]=="keypress"){
if(dojo.render.html.opera){
if(evt.which==0){
evt.key=evt.keyCode;
}else{
if(evt.which>0){
switch(evt.which){
case evt.KEY_SHIFT:
case evt.KEY_CTRL:
case evt.KEY_ALT:
case evt.KEY_CAPS_LOCK:
case evt.KEY_NUM_LOCK:
case evt.KEY_SCROLL_LOCK:
break;
case evt.KEY_PAUSE:
case evt.KEY_TAB:
case evt.KEY_BACKSPACE:
case evt.KEY_ENTER:
case evt.KEY_ESCAPE:
evt.key=evt.which;
break;
default:
var _450=evt.which;
if((evt.ctrlKey||evt.altKey||evt.metaKey)&&(evt.which>=65&&evt.which<=90&&evt.shiftKey==false)){
_450+=32;
}
evt.key=String.fromCharCode(_450);
}
}
}
}else{
if(dojo.render.html.ie){
if(!evt.ctrlKey&&!evt.altKey&&evt.keyCode>=evt.KEY_SPACE){
evt.key=String.fromCharCode(evt.keyCode);
}
}else{
if(dojo.render.html.safari){
switch(evt.keyCode){
case 25:
evt.key=evt.KEY_TAB;
evt.shift=true;
break;
case 63232:
evt.key=evt.KEY_UP_ARROW;
break;
case 63233:
evt.key=evt.KEY_DOWN_ARROW;
break;
case 63234:
evt.key=evt.KEY_LEFT_ARROW;
break;
case 63235:
evt.key=evt.KEY_RIGHT_ARROW;
break;
case 63236:
evt.key=evt.KEY_F1;
break;
case 63237:
evt.key=evt.KEY_F2;
break;
case 63238:
evt.key=evt.KEY_F3;
break;
case 63239:
evt.key=evt.KEY_F4;
break;
case 63240:
evt.key=evt.KEY_F5;
break;
case 63241:
evt.key=evt.KEY_F6;
break;
case 63242:
evt.key=evt.KEY_F7;
break;
case 63243:
evt.key=evt.KEY_F8;
break;
case 63244:
evt.key=evt.KEY_F9;
break;
case 63245:
evt.key=evt.KEY_F10;
break;
case 63246:
evt.key=evt.KEY_F11;
break;
case 63247:
evt.key=evt.KEY_F12;
break;
case 63250:
evt.key=evt.KEY_PAUSE;
break;
case 63272:
evt.key=evt.KEY_DELETE;
break;
case 63273:
evt.key=evt.KEY_HOME;
break;
case 63275:
evt.key=evt.KEY_END;
break;
case 63276:
evt.key=evt.KEY_PAGE_UP;
break;
case 63277:
evt.key=evt.KEY_PAGE_DOWN;
break;
case 63302:
evt.key=evt.KEY_INSERT;
break;
case 63248:
case 63249:
case 63289:
break;
default:
evt.key=evt.charCode>=evt.KEY_SPACE?String.fromCharCode(evt.charCode):evt.keyCode;
}
}else{
evt.key=evt.charCode>0?String.fromCharCode(evt.charCode):evt.keyCode;
}
}
}
}
}
}
if(dojo.render.html.ie){
if(!evt.target){
evt.target=evt.srcElement;
}
if(!evt.currentTarget){
evt.currentTarget=(_44e?_44e:evt.srcElement);
}
if(!evt.layerX){
evt.layerX=evt.offsetX;
}
if(!evt.layerY){
evt.layerY=evt.offsetY;
}
var doc=(evt.srcElement&&evt.srcElement.ownerDocument)?evt.srcElement.ownerDocument:document;
var _452=((dojo.render.html.ie55)||(doc["compatMode"]=="BackCompat"))?doc.body:doc.documentElement;
if(!evt.pageX){
evt.pageX=evt.clientX+(_452.scrollLeft||0);
}
if(!evt.pageY){
evt.pageY=evt.clientY+(_452.scrollTop||0);
}
if(evt.type=="mouseover"){
evt.relatedTarget=evt.fromElement;
}
if(evt.type=="mouseout"){
evt.relatedTarget=evt.toElement;
}
this.currentEvent=evt;
evt.callListener=this.callListener;
evt.stopPropagation=this._stopPropagation;
evt.preventDefault=this._preventDefault;
}
return evt;
};
this.stopEvent=function(evt){
if(window.event){
evt.cancelBubble=true;
evt.returnValue=false;
}else{
evt.preventDefault();
evt.stopPropagation();
}
};
};
dojo.kwCompoundRequire({common:["dojo.event.common","dojo.event.topic"],browser:["dojo.event.browser"],dashboard:["dojo.event.browser"]});
dojo.provide("dojo.event.*");

