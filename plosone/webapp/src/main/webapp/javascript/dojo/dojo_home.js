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

	bundled from release-0.4.0
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
if(typeof setTimeout=="object"){
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
return _6e?_6e.toLowerCase():dojo.locale;
};
dojo.hostenv.searchLocalePath=function(_6f,_70,_71){
_6f=dojo.hostenv.normalizeLocale(_6f);
var _72=_6f.split("-");
var _73=[];
for(var i=_72.length;i>0;i--){
_73.push(_72.slice(0,i).join("-"));
}
_73.push(false);
if(_70){
_73.reverse();
}
for(var j=_73.length-1;j>=0;j--){
var loc=_73[j]||"ROOT";
var _77=_71(loc);
if(_77){
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
function preload(_78){
_78=dojo.hostenv.normalizeLocale(_78);
dojo.hostenv.searchLocalePath(_78,true,function(loc){
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
var _7b=djConfig.extraLocale||[];
for(var i=0;i<_7b.length;i++){
preload(_7b[i]);
}
}
dojo.hostenv.preloadLocalizations=function(){
};
};
dojo.requireLocalization=function(_7d,_7e,_7f){
dojo.hostenv.preloadLocalizations();
var _80=[_7d,"nls",_7e].join(".");
var _81=dojo.hostenv.findModule(_80);
if(_81){
if(djConfig.localizationComplete&&_81._built){
return;
}
var _82=dojo.hostenv.normalizeLocale(_7f).replace("-","_");
var _83=_80+"."+_82;
if(dojo.hostenv.findModule(_83)){
return;
}
}
_81=dojo.hostenv.startPackage(_80);
var _84=dojo.hostenv.getModuleSymbols(_7d);
var _85=_84.concat("nls").join("/");
var _86;
dojo.hostenv.searchLocalePath(_7f,false,function(loc){
var _88=loc.replace("-","_");
var _89=_80+"."+_88;
var _8a=false;
if(!dojo.hostenv.findModule(_89)){
dojo.hostenv.startPackage(_89);
var _8b=[_85];
if(loc!="ROOT"){
_8b.push(loc);
}
_8b.push(_7e);
var _8c=_8b.join("/")+".js";
_8a=dojo.hostenv.loadPath(_8c,null,function(_8d){
var _8e=function(){
};
_8e.prototype=_86;
_81[_88]=new _8e();
for(var j in _8d){
_81[_88][j]=_8d[j];
}
});
}else{
_8a=true;
}
if(_8a&&_81[_88]){
_86=_81[_88];
}else{
_81[_88]=_86;
}
});
};
(function(){
var _90=djConfig.extraLocale;
if(_90){
if(!_90 instanceof Array){
_90=[_90];
}
var req=dojo.requireLocalization;
dojo.requireLocalization=function(m,b,_94){
req(m,b,_94);
if(_94){
return;
}
for(var i=0;i<_90.length;i++){
req(m,b,_90[i]);
}
};
}
})();
}
if(typeof window!="undefined"){
(function(){
if(djConfig.allowQueryConfig){
var _96=document.location.toString();
var _97=_96.split("?",2);
if(_97.length>1){
var _98=_97[1];
var _99=_98.split("&");
for(var x in _99){
var sp=_99[x].split("=");
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
var _9d=document.getElementsByTagName("script");
var _9e=/(__package__|dojo|bootstrap1)\.js([\?\.]|$)/i;
for(var i=0;i<_9d.length;i++){
var src=_9d[i].getAttribute("src");
if(!src){
continue;
}
var m=src.match(_9e);
if(m){
var _a2=src.substring(0,m.index);
if(src.indexOf("bootstrap1")>-1){
_a2+="../";
}
if(!this["djConfig"]){
djConfig={};
}
if(djConfig["baseScriptUri"]==""){
djConfig["baseScriptUri"]=_a2;
}
if(djConfig["baseRelativePath"]==""){
djConfig["baseRelativePath"]=_a2;
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
var _aa=dua.indexOf("Gecko");
drh.mozilla=drh.moz=(_aa>=0)&&(!drh.khtml);
if(drh.mozilla){
drh.geckoVersion=dua.substring(_aa+6,_aa+14);
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
var _ac=window["document"];
var tdi=_ac["implementation"];
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
}
})();
dojo.hostenv.startPackage("dojo.hostenv");
dojo.render.name=dojo.hostenv.name_="browser";
dojo.hostenv.searchIds=[];
dojo.hostenv._XMLHTTP_PROGIDS=["Msxml2.XMLHTTP","Microsoft.XMLHTTP","Msxml2.XMLHTTP.4.0"];
dojo.hostenv.getXmlhttpObject=function(){
var _b0=null;
var _b1=null;
try{
_b0=new XMLHttpRequest();
}
catch(e){
}
if(!_b0){
for(var i=0;i<3;++i){
var _b3=dojo.hostenv._XMLHTTP_PROGIDS[i];
try{
_b0=new ActiveXObject(_b3);
}
catch(e){
_b1=e;
}
if(_b0){
dojo.hostenv._XMLHTTP_PROGIDS=[_b3];
break;
}
}
}
if(!_b0){
return dojo.raise("XMLHTTP not available",_b1);
}
return _b0;
};
dojo.hostenv._blockAsync=false;
dojo.hostenv.getText=function(uri,_b5,_b6){
if(!_b5){
this._blockAsync=true;
}
var _b7=this.getXmlhttpObject();
function isDocumentOk(_b8){
var _b9=_b8["status"];
return Boolean((!_b9)||((200<=_b9)&&(300>_b9))||(_b9==304));
}
if(_b5){
var _ba=this,_bb=null,gbl=dojo.global();
var xhr=dojo.evalObjPath("dojo.io.XMLHTTPTransport");
_b7.onreadystatechange=function(){
if(_bb){
gbl.clearTimeout(_bb);
_bb=null;
}
if(_ba._blockAsync||(xhr&&xhr._blockAsync)){
_bb=gbl.setTimeout(function(){
_b7.onreadystatechange.apply(this);
},10);
}else{
if(4==_b7.readyState){
if(isDocumentOk(_b7)){
_b5(_b7.responseText);
}
}
}
};
}
_b7.open("GET",uri,_b5?true:false);
try{
_b7.send(null);
if(_b5){
return null;
}
if(!isDocumentOk(_b7)){
var err=Error("Unable to load "+uri+" status:"+_b7.status);
err.status=_b7.status;
err.responseText=_b7.responseText;
throw err;
}
}
catch(e){
this._blockAsync=false;
if((_b6)&&(!_b5)){
return null;
}else{
throw e;
}
}
this._blockAsync=false;
return _b7.responseText;
};
dojo.hostenv.defaultDebugContainerId="dojoDebug";
dojo.hostenv._println_buffer=[];
dojo.hostenv._println_safe=false;
dojo.hostenv.println=function(_bf){
if(!dojo.hostenv._println_safe){
dojo.hostenv._println_buffer.push(_bf);
}else{
try{
var _c0=document.getElementById(djConfig.debugContainerId?djConfig.debugContainerId:dojo.hostenv.defaultDebugContainerId);
if(!_c0){
_c0=dojo.body();
}
var div=document.createElement("div");
div.appendChild(document.createTextNode(_bf));
_c0.appendChild(div);
}
catch(e){
try{
document.write("<div>"+_bf+"</div>");
}
catch(e2){
window.status=_bf;
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
function dj_addNodeEvtHdlr(_c2,_c3,fp,_c5){
var _c6=_c2["on"+_c3]||function(){
};
_c2["on"+_c3]=function(){
fp.apply(_c2,arguments);
_c6.apply(_c2,arguments);
};
return true;
}
function dj_load_init(e){
var _c8=(e&&e.type)?e.type.toLowerCase():"load";
if(arguments.callee.initialized||(_c8!="domcontentloaded"&&_c8!="load")){
return;
}
arguments.callee.initialized=true;
if(typeof (_timer)!="undefined"){
clearInterval(_timer);
delete _timer;
}
var _c9=function(){
if(dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
};
if(dojo.hostenv.inFlightCount==0){
_c9();
dojo.hostenv.modulesLoaded();
}else{
dojo.addOnLoad(_c9);
}
}
if(document.addEventListener){
if(dojo.render.html.opera||(dojo.render.html.moz&&!djConfig.delayMozLoadingFix)){
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
var _cb=[];
if(djConfig.searchIds&&djConfig.searchIds.length>0){
_cb=_cb.concat(djConfig.searchIds);
}
if(dojo.hostenv.searchIds&&dojo.hostenv.searchIds.length>0){
_cb=_cb.concat(dojo.hostenv.searchIds);
}
if((djConfig.parseWidgets)||(_cb.length>0)){
if(dojo.evalObjPath("dojo.widget.Parse")){
var _cc=new dojo.xml.Parse();
if(_cb.length>0){
for(var x=0;x<_cb.length;x++){
var _ce=document.getElementById(_cb[x]);
if(!_ce){
continue;
}
var _cf=_cc.parseElement(_ce,null,true);
dojo.widget.getParser().createComponents(_cf);
}
}else{
if(djConfig.parseWidgets){
var _cf=_cc.parseElement(dojo.body(),null,true);
dojo.widget.getParser().createComponents(_cf);
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
dojo.setContext=function(_d4,_d5){
dj_currentContext=_d4;
dj_currentDocument=_d5;
};
dojo._fireCallback=function(_d6,_d7,_d8){
if((_d7)&&((typeof _d6=="string")||(_d6 instanceof String))){
_d6=_d7[_d6];
}
return (_d7?_d6.apply(_d7,_d8||[]):_d6());
};
dojo.withGlobal=function(_d9,_da,_db,_dc){
var _dd;
var _de=dj_currentContext;
var _df=dj_currentDocument;
try{
dojo.setContext(_d9,_d9.document);
_dd=dojo._fireCallback(_da,_db,_dc);
}
finally{
dojo.setContext(_de,_df);
}
return _dd;
};
dojo.withDoc=function(_e0,_e1,_e2,_e3){
var _e4;
var _e5=dj_currentDocument;
try{
dj_currentDocument=_e0;
_e4=dojo._fireCallback(_e1,_e2,_e3);
}
finally{
dj_currentDocument=_e5;
}
return _e4;
};
}
(function(){
if(typeof dj_usingBootstrap!="undefined"){
return;
}
var _e6=false;
var _e7=false;
var _e8=false;
if((typeof this["load"]=="function")&&((typeof this["Packages"]=="function")||(typeof this["Packages"]=="object"))){
_e6=true;
}else{
if(typeof this["load"]=="function"){
_e7=true;
}else{
if(window.widget){
_e8=true;
}
}
}
var _e9=[];
if((this["djConfig"])&&((djConfig["isDebug"])||(djConfig["debugAtAllCosts"]))){
_e9.push("debug.js");
}
if((this["djConfig"])&&(djConfig["debugAtAllCosts"])&&(!_e6)&&(!_e8)){
_e9.push("browser_debug.js");
}
var _ea=djConfig["baseScriptUri"];
if((this["djConfig"])&&(djConfig["baseLoaderUri"])){
_ea=djConfig["baseLoaderUri"];
}
for(var x=0;x<_e9.length;x++){
var _ec=_ea+"src/"+_e9[x];
if(_e6||_e7){
load(_ec);
}else{
try{
document.write("<scr"+"ipt type='text/javascript' src='"+_ec+"'></scr"+"ipt>");
}
catch(e){
var _ed=document.createElement("script");
_ed.src=_ec;
document.getElementsByTagName("head")[0].appendChild(_ed);
}
}
}
})();
dojo.provide("dojo.lang.common");
dojo.lang.inherits=function(_ee,_ef){
if(typeof _ef!="function"){
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
if(!it){
return false;
}
if((typeof (it)=="function")&&(it=="[object NodeList]")){
return false;
}
return (it instanceof Function||typeof it=="function");
};
dojo.lang.isString=function(it){
return (typeof it=="string"||it instanceof String);
};
dojo.lang.isAlien=function(it){
if(!it){
return false;
}
return !dojo.lang.isFunction()&&/\{\s*\[native code\]\s*\}/.test(String(it));
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
catch(E){
}
}else{
return wh&&!isNaN(wh.nodeType);
}
};
dojo.dom.getUniqueId=function(){
var _114=dojo.doc();
do{
var id="dj_unique_"+(++arguments.callee._idIncrement);
}while(_114.getElementById(id));
return id;
};
dojo.dom.getUniqueId._idIncrement=0;
dojo.dom.firstElement=dojo.dom.getFirstChildElement=function(_116,_117){
var node=_116.firstChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.nextSibling;
}
if(_117&&node&&node.tagName&&node.tagName.toLowerCase()!=_117.toLowerCase()){
node=dojo.dom.nextElement(node,_117);
}
return node;
};
dojo.dom.lastElement=dojo.dom.getLastChildElement=function(_119,_11a){
var node=_119.lastChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.previousSibling;
}
if(_11a&&node&&node.tagName&&node.tagName.toLowerCase()!=_11a.toLowerCase()){
node=dojo.dom.prevElement(node,_11a);
}
return node;
};
dojo.dom.nextElement=dojo.dom.getNextSiblingElement=function(node,_11d){
if(!node){
return null;
}
do{
node=node.nextSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_11d&&_11d.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.nextElement(node,_11d);
}
return node;
};
dojo.dom.prevElement=dojo.dom.getPreviousSiblingElement=function(node,_11f){
if(!node){
return null;
}
if(_11f){
_11f=_11f.toLowerCase();
}
do{
node=node.previousSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_11f&&_11f.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.prevElement(node,_11f);
}
return node;
};
dojo.dom.moveChildren=function(_120,_121,trim){
var _123=0;
if(trim){
while(_120.hasChildNodes()&&_120.firstChild.nodeType==dojo.dom.TEXT_NODE){
_120.removeChild(_120.firstChild);
}
while(_120.hasChildNodes()&&_120.lastChild.nodeType==dojo.dom.TEXT_NODE){
_120.removeChild(_120.lastChild);
}
}
while(_120.hasChildNodes()){
_121.appendChild(_120.firstChild);
_123++;
}
return _123;
};
dojo.dom.copyChildren=function(_124,_125,trim){
var _127=_124.cloneNode(true);
return this.moveChildren(_127,_125,trim);
};
dojo.dom.removeChildren=function(node){
var _129=node.childNodes.length;
while(node.hasChildNodes()){
node.removeChild(node.firstChild);
}
return _129;
};
dojo.dom.replaceChildren=function(node,_12b){
dojo.dom.removeChildren(node);
node.appendChild(_12b);
};
dojo.dom.removeNode=function(node){
if(node&&node.parentNode){
return node.parentNode.removeChild(node);
}
};
dojo.dom.getAncestors=function(node,_12e,_12f){
var _130=[];
var _131=(_12e&&(_12e instanceof Function||typeof _12e=="function"));
while(node){
if(!_131||_12e(node)){
_130.push(node);
}
if(_12f&&_130.length>0){
return _130[0];
}
node=node.parentNode;
}
if(_12f){
return null;
}
return _130;
};
dojo.dom.getAncestorsByTag=function(node,tag,_134){
tag=tag.toLowerCase();
return dojo.dom.getAncestors(node,function(el){
return ((el.tagName)&&(el.tagName.toLowerCase()==tag));
},_134);
};
dojo.dom.getFirstAncestorByTag=function(node,tag){
return dojo.dom.getAncestorsByTag(node,tag,true);
};
dojo.dom.isDescendantOf=function(node,_139,_13a){
if(_13a&&node){
node=node.parentNode;
}
while(node){
if(node==_139){
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
var _13d=dojo.doc();
if(!dj_undef("ActiveXObject")){
var _13e=["MSXML2","Microsoft","MSXML","MSXML3"];
for(var i=0;i<_13e.length;i++){
try{
doc=new ActiveXObject(_13e[i]+".XMLDOM");
}
catch(e){
}
if(doc){
break;
}
}
}else{
if((_13d.implementation)&&(_13d.implementation.createDocument)){
doc=_13d.implementation.createDocument("","",null);
}
}
return doc;
};
dojo.dom.createDocumentFromText=function(str,_141){
if(!_141){
_141="text/xml";
}
if(!dj_undef("DOMParser")){
var _142=new DOMParser();
return _142.parseFromString(str,_141);
}else{
if(!dj_undef("ActiveXObject")){
var _143=dojo.dom.createDocument();
if(_143){
_143.async=false;
_143.loadXML(str);
return _143;
}else{
dojo.debug("toXml didn't work?");
}
}else{
var _144=dojo.doc();
if(_144.createElement){
var tmp=_144.createElement("xml");
tmp.innerHTML=str;
if(_144.implementation&&_144.implementation.createDocument){
var _146=_144.implementation.createDocument("foo","",null);
for(var i=0;i<tmp.childNodes.length;i++){
_146.importNode(tmp.childNodes.item(i),true);
}
return _146;
}
return ((tmp.document)&&(tmp.document.firstChild?tmp.document.firstChild:tmp));
}
}
}
return null;
};
dojo.dom.prependChild=function(node,_149){
if(_149.firstChild){
_149.insertBefore(node,_149.firstChild);
}else{
_149.appendChild(node);
}
return true;
};
dojo.dom.insertBefore=function(node,ref,_14c){
if(_14c!=true&&(node===ref||node.nextSibling===ref)){
return false;
}
var _14d=ref.parentNode;
_14d.insertBefore(node,ref);
return true;
};
dojo.dom.insertAfter=function(node,ref,_150){
var pn=ref.parentNode;
if(ref==pn.lastChild){
if((_150!=true)&&(node===ref)){
return false;
}
pn.appendChild(node);
}else{
return this.insertBefore(node,ref.nextSibling,_150);
}
return true;
};
dojo.dom.insertAtPosition=function(node,ref,_154){
if((!node)||(!ref)||(!_154)){
return false;
}
switch(_154.toLowerCase()){
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
dojo.dom.insertAtIndex=function(node,_156,_157){
var _158=_156.childNodes;
if(!_158.length){
_156.appendChild(node);
return true;
}
var _159=null;
for(var i=0;i<_158.length;i++){
var _15b=_158.item(i)["getAttribute"]?parseInt(_158.item(i).getAttribute("dojoinsertionindex")):-1;
if(_15b<_157){
_159=_158.item(i);
}
}
if(_159){
return dojo.dom.insertAfter(node,_159);
}else{
return dojo.dom.insertBefore(node,_158.item(0));
}
};
dojo.dom.textContent=function(node,text){
if(arguments.length>1){
var _15e=dojo.doc();
dojo.dom.replaceChildren(node,_15e.createTextNode(text));
return text;
}else{
if(node.textContent!=undefined){
return node.textContent;
}
var _15f="";
if(node==null){
return _15f;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
_15f+=dojo.dom.textContent(node.childNodes[i]);
break;
case 3:
case 2:
case 4:
_15f+=node.childNodes[i].nodeValue;
break;
default:
break;
}
}
return _15f;
}
};
dojo.dom.hasParent=function(node){
return node&&node.parentNode&&dojo.dom.isNode(node.parentNode);
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
dojo.dom.setAttributeNS=function(elem,_165,_166,_167){
if(elem==null||((elem==undefined)&&(typeof elem=="undefined"))){
dojo.raise("No element given to dojo.dom.setAttributeNS");
}
if(!((elem.setAttributeNS==undefined)&&(typeof elem.setAttributeNS=="undefined"))){
elem.setAttributeNS(_165,_166,_167);
}else{
var _168=elem.ownerDocument;
var _169=_168.createNode(2,_166,_165);
_169.nodeValue=_167;
elem.setAttributeNode(_169);
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
var _16c=dojo.global();
var _16d=dojo.doc();
var w=0;
var h=0;
if(dojo.render.html.mozilla){
w=_16d.documentElement.clientWidth;
h=_16c.innerHeight;
}else{
if(!dojo.render.html.opera&&_16c.innerWidth){
w=_16c.innerWidth;
h=_16c.innerHeight;
}else{
if(!dojo.render.html.opera&&dojo.exists(_16d,"documentElement.clientWidth")){
var w2=_16d.documentElement.clientWidth;
if(!w||w2&&w2<w){
w=w2;
}
h=_16d.documentElement.clientHeight;
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
var _171=dojo.global();
var _172=dojo.doc();
var top=_171.pageYOffset||_172.documentElement.scrollTop||dojo.body().scrollTop||0;
var left=_171.pageXOffset||_172.documentElement.scrollLeft||dojo.body().scrollLeft||0;
return {top:top,left:left,offset:{x:left,y:top}};
};
dojo.html.getParentByType=function(node,type){
var _177=dojo.doc();
var _178=dojo.byId(node);
type=type.toLowerCase();
while((_178)&&(_178.nodeName.toLowerCase()!=type)){
if(_178==(_177["body"]||_177["documentElement"])){
return null;
}
_178=_178.parentNode;
}
return _178;
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
var _180={x:0,y:0};
if(e.pageX||e.pageY){
_180.x=e.pageX;
_180.y=e.pageY;
}else{
var de=dojo.doc().documentElement;
var db=dojo.body();
_180.x=e.clientX+((de||db)["scrollLeft"])-((de||db)["clientLeft"]);
_180.y=e.clientY+((de||db)["scrollTop"])-((de||db)["clientTop"]);
}
return _180;
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
var _185=dojo.doc().createElement("script");
_185.src="javascript:'dojo.html.createExternalElement=function(doc, tag){ return doc.createElement(tag); }'";
dojo.doc().getElementsByTagName("head")[0].appendChild(_185);
})();
}
}else{
dojo.html.createExternalElement=function(doc,tag){
return doc.createElement(tag);
};
}
dojo.html._callDeprecated=function(_188,_189,args,_18b,_18c){
dojo.deprecated("dojo.html."+_188,"replaced by dojo.html."+_189+"("+(_18b?"node, {"+_18b+": "+_18b+"}":"")+")"+(_18c?"."+_18c:""),"0.5");
var _18d=[];
if(_18b){
var _18e={};
_18e[_18b]=args[1];
_18d.push(args[0]);
_18d.push(_18e);
}else{
_18d=args;
}
var ret=dojo.html[_189].apply(dojo.html,args);
if(_18c){
return ret[_18c];
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
this.moduleUri=function(_191,uri){
var loc=dojo.hostenv.getModulePrefix(_191);
if(!loc){
return null;
}
if(loc.lastIndexOf("/")!=loc.length-1){
loc+="/";
}
return new dojo.uri.Uri(dojo.hostenv.getBaseScriptUri()+loc,uri);
};
this.Uri=function(){
var uri=arguments[0];
for(var i=1;i<arguments.length;i++){
if(!arguments[i]){
continue;
}
var _196=new dojo.uri.Uri(arguments[i].toString());
var _197=new dojo.uri.Uri(uri.toString());
if((_196.path=="")&&(_196.scheme==null)&&(_196.authority==null)&&(_196.query==null)){
if(_196.fragment!=null){
_197.fragment=_196.fragment;
}
_196=_197;
}else{
if(_196.scheme==null){
_196.scheme=_197.scheme;
if(_196.authority==null){
_196.authority=_197.authority;
if(_196.path.charAt(0)!="/"){
var path=_197.path.substring(0,_197.path.lastIndexOf("/")+1)+_196.path;
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
_196.path=segs.join("/");
}
}
}
}
uri="";
if(_196.scheme!=null){
uri+=_196.scheme+":";
}
if(_196.authority!=null){
uri+="//"+_196.authority;
}
uri+=_196.path;
if(_196.query!=null){
uri+="?"+_196.query;
}
if(_196.fragment!=null){
uri+="#"+_196.fragment;
}
}
this.uri=uri.toString();
var _19b="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=this.uri.match(new RegExp(_19b));
this.scheme=r[2]||(r[1]?"":null);
this.authority=r[4]||(r[3]?"":null);
this.path=r[5];
this.query=r[7]||(r[6]?"":null);
this.fragment=r[9]||(r[8]?"":null);
if(this.authority!=null){
_19b="^((([^:]+:)?([^@]+))@)?([^:]*)(:([0-9]+))?$";
r=this.authority.match(new RegExp(_19b));
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
dojo.html.hasClass=function(node,_1a2){
return (new RegExp("(^|\\s+)"+_1a2+"(\\s+|$)")).test(dojo.html.getClass(node));
};
dojo.html.prependClass=function(node,_1a4){
_1a4+=" "+dojo.html.getClass(node);
return dojo.html.setClass(node,_1a4);
};
dojo.html.addClass=function(node,_1a6){
if(dojo.html.hasClass(node,_1a6)){
return false;
}
_1a6=(dojo.html.getClass(node)+" "+_1a6).replace(/^\s+|\s+$/g,"");
return dojo.html.setClass(node,_1a6);
};
dojo.html.setClass=function(node,_1a8){
node=dojo.byId(node);
var cs=new String(_1a8);
try{
if(typeof node.className=="string"){
node.className=cs;
}else{
if(node.setAttribute){
node.setAttribute("class",_1a8);
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
dojo.html.removeClass=function(node,_1ab,_1ac){
try{
if(!_1ac){
var _1ad=dojo.html.getClass(node).replace(new RegExp("(^|\\s+)"+_1ab+"(\\s+|$)"),"$1$2");
}else{
var _1ad=dojo.html.getClass(node).replace(_1ab,"");
}
dojo.html.setClass(node,_1ad);
}
catch(e){
dojo.debug("dojo.html.removeClass() failed",e);
}
return true;
};
dojo.html.replaceClass=function(node,_1af,_1b0){
dojo.html.removeClass(node,_1b0);
dojo.html.addClass(node,_1af);
};
dojo.html.classMatchType={ContainsAll:0,ContainsAny:1,IsOnly:2};
dojo.html.getElementsByClass=function(_1b1,_1b2,_1b3,_1b4,_1b5){
_1b5=false;
var _1b6=dojo.doc();
_1b2=dojo.byId(_1b2)||_1b6;
var _1b7=_1b1.split(/\s+/g);
var _1b8=[];
if(_1b4!=1&&_1b4!=2){
_1b4=0;
}
var _1b9=new RegExp("(\\s|^)(("+_1b7.join(")|(")+"))(\\s|$)");
var _1ba=_1b7.join(" ").length;
var _1bb=[];
if(!_1b5&&_1b6.evaluate){
var _1bc=".//"+(_1b3||"*")+"[contains(";
if(_1b4!=dojo.html.classMatchType.ContainsAny){
_1bc+="concat(' ',@class,' '), ' "+_1b7.join(" ') and contains(concat(' ',@class,' '), ' ")+" ')";
if(_1b4==2){
_1bc+=" and string-length(@class)="+_1ba+"]";
}else{
_1bc+="]";
}
}else{
_1bc+="concat(' ',@class,' '), ' "+_1b7.join(" ') or contains(concat(' ',@class,' '), ' ")+" ')]";
}
var _1bd=_1b6.evaluate(_1bc,_1b2,null,XPathResult.ANY_TYPE,null);
var _1be=_1bd.iterateNext();
while(_1be){
try{
_1bb.push(_1be);
_1be=_1bd.iterateNext();
}
catch(e){
break;
}
}
return _1bb;
}else{
if(!_1b3){
_1b3="*";
}
_1bb=_1b2.getElementsByTagName(_1b3);
var node,i=0;
outer:
while(node=_1bb[i++]){
var _1c1=dojo.html.getClasses(node);
if(_1c1.length==0){
continue outer;
}
var _1c2=0;
for(var j=0;j<_1c1.length;j++){
if(_1b9.test(_1c1[j])){
if(_1b4==dojo.html.classMatchType.ContainsAny){
_1b8.push(node);
continue outer;
}else{
_1c2++;
}
}else{
if(_1b4==dojo.html.classMatchType.IsOnly){
continue outer;
}
}
}
if(_1c2==_1b7.length){
if((_1b4==dojo.html.classMatchType.IsOnly)&&(_1c2==_1c1.length)){
_1b8.push(node);
}else{
if(_1b4==dojo.html.classMatchType.ContainsAll){
_1b8.push(node);
}
}
}
}
return _1b8;
}
};
dojo.html.getElementsByClassName=dojo.html.getElementsByClass;
dojo.html.toCamelCase=function(_1c4){
var arr=_1c4.split("-"),cc=arr[0];
for(var i=1;i<arr.length;i++){
cc+=arr[i].charAt(0).toUpperCase()+arr[i].substring(1);
}
return cc;
};
dojo.html.toSelectorCase=function(_1c8){
return _1c8.replace(/([A-Z])/g,"-$1").toLowerCase();
};
dojo.html.getComputedStyle=function(node,_1ca,_1cb){
node=dojo.byId(node);
var _1ca=dojo.html.toSelectorCase(_1ca);
var _1cc=dojo.html.toCamelCase(_1ca);
if(!node||!node.style){
return _1cb;
}else{
if(document.defaultView&&dojo.html.isDescendantOf(node,node.ownerDocument)){
try{
var cs=document.defaultView.getComputedStyle(node,"");
if(cs){
return cs.getPropertyValue(_1ca);
}
}
catch(e){
if(node.style.getPropertyValue){
return node.style.getPropertyValue(_1ca);
}else{
return _1cb;
}
}
}else{
if(node.currentStyle){
return node.currentStyle[_1cc];
}
}
}
if(node.style.getPropertyValue){
return node.style.getPropertyValue(_1ca);
}else{
return _1cb;
}
};
dojo.html.getStyleProperty=function(node,_1cf){
node=dojo.byId(node);
return (node&&node.style?node.style[dojo.html.toCamelCase(_1cf)]:undefined);
};
dojo.html.getStyle=function(node,_1d1){
var _1d2=dojo.html.getStyleProperty(node,_1d1);
return (_1d2?_1d2:dojo.html.getComputedStyle(node,_1d1));
};
dojo.html.setStyle=function(node,_1d4,_1d5){
node=dojo.byId(node);
if(node&&node.style){
var _1d6=dojo.html.toCamelCase(_1d4);
node.style[_1d6]=_1d5;
}
};
dojo.html.setStyleText=function(_1d7,text){
try{
_1d7.style.cssText=text;
}
catch(e){
_1d7.setAttribute("style",text);
}
};
dojo.html.copyStyle=function(_1d9,_1da){
if(!_1da.style.cssText){
_1d9.setAttribute("style",_1da.getAttribute("style"));
}else{
_1d9.style.cssText=_1da.style.cssText;
}
dojo.html.addClass(_1d9,dojo.html.getClass(_1da));
};
dojo.html.getUnitValue=function(node,_1dc,_1dd){
var s=dojo.html.getComputedStyle(node,_1dc);
if((!s)||((s=="auto")&&(_1dd))){
return {value:0,units:"px"};
}
var _1df=s.match(/(\-?[\d.]+)([a-z%]*)/i);
if(!_1df){
return dojo.html.getUnitValue.bad;
}
return {value:Number(_1df[1]),units:_1df[2].toLowerCase()};
};
dojo.html.getUnitValue.bad={value:NaN,units:""};
dojo.html.getPixelValue=function(node,_1e1,_1e2){
var _1e3=dojo.html.getUnitValue(node,_1e1,_1e2);
if(isNaN(_1e3.value)){
return 0;
}
if((_1e3.value)&&(_1e3.units!="px")){
return NaN;
}
return _1e3.value;
};
dojo.html.setPositivePixelValue=function(node,_1e5,_1e6){
if(isNaN(_1e6)){
return false;
}
node.style[_1e5]=Math.max(0,_1e6)+"px";
return true;
};
dojo.html.styleSheet=null;
dojo.html.insertCssRule=function(_1e7,_1e8,_1e9){
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
_1e9=dojo.html.styleSheet.cssRules.length;
}else{
if(dojo.html.styleSheet.rules){
_1e9=dojo.html.styleSheet.rules.length;
}else{
return null;
}
}
}
if(dojo.html.styleSheet.insertRule){
var rule=_1e7+" { "+_1e8+" }";
return dojo.html.styleSheet.insertRule(rule,_1e9);
}else{
if(dojo.html.styleSheet.addRule){
return dojo.html.styleSheet.addRule(_1e7,_1e8,_1e9);
}else{
return null;
}
}
};
dojo.html.removeCssRule=function(_1eb){
if(!dojo.html.styleSheet){
dojo.debug("no stylesheet defined for removing rules");
return false;
}
if(dojo.render.html.ie){
if(!_1eb){
_1eb=dojo.html.styleSheet.rules.length;
dojo.html.styleSheet.removeRule(_1eb);
}
}else{
if(document.styleSheets[0]){
if(!_1eb){
_1eb=dojo.html.styleSheet.cssRules.length;
}
dojo.html.styleSheet.deleteRule(_1eb);
}
}
return true;
};
dojo.html._insertedCssFiles=[];
dojo.html.insertCssFile=function(URI,doc,_1ee,_1ef){
if(!URI){
return;
}
if(!doc){
doc=document;
}
var _1f0=dojo.hostenv.getText(URI,false,_1ef);
if(_1f0===null){
return;
}
_1f0=dojo.html.fixPathsInCssText(_1f0,URI);
if(_1ee){
var idx=-1,node,ent=dojo.html._insertedCssFiles;
for(var i=0;i<ent.length;i++){
if((ent[i].doc==doc)&&(ent[i].cssText==_1f0)){
idx=i;
node=ent[i].nodeRef;
break;
}
}
if(node){
var _1f5=doc.getElementsByTagName("style");
for(var i=0;i<_1f5.length;i++){
if(_1f5[i]==node){
return;
}
}
dojo.html._insertedCssFiles.shift(idx,1);
}
}
var _1f6=dojo.html.insertCssText(_1f0);
dojo.html._insertedCssFiles.push({"doc":doc,"cssText":_1f0,"nodeRef":_1f6});
if(_1f6&&djConfig.isDebug){
_1f6.setAttribute("dbgHref",URI);
}
return _1f6;
};
dojo.html.insertCssText=function(_1f7,doc,URI){
if(!_1f7){
return;
}
if(!doc){
doc=document;
}
if(URI){
_1f7=dojo.html.fixPathsInCssText(_1f7,URI);
}
var _1fa=doc.createElement("style");
_1fa.setAttribute("type","text/css");
var head=doc.getElementsByTagName("head")[0];
if(!head){
dojo.debug("No head tag in document, aborting styles");
return;
}else{
head.appendChild(_1fa);
}
if(_1fa.styleSheet){
_1fa.styleSheet.cssText=_1f7;
}else{
var _1fc=doc.createTextNode(_1f7);
_1fa.appendChild(_1fc);
}
return _1fa;
};
dojo.html.fixPathsInCssText=function(_1fd,URI){
function iefixPathsInCssText(){
var _1ff=/AlphaImageLoader\(src\=['"]([\t\s\w()\/.\\'"-:#=&?~]*)['"]/;
while(_200=_1ff.exec(_1fd)){
url=_200[1].replace(_202,"$2");
if(!_203.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_1fd.substring(0,_200.index)+"AlphaImageLoader(src='"+url+"'";
_1fd=_1fd.substr(_200.index+_200[0].length);
}
return str+_1fd;
}
if(!_1fd||!URI){
return;
}
var _200,str="",url="";
var _205=/url\(\s*([\t\s\w()\/.\\'"-:#=&?]+)\s*\)/;
var _203=/(file|https?|ftps?):\/\//;
var _202=/^[\s]*(['"]?)([\w()\/.\\'"-:#=&?]*)\1[\s]*?$/;
if(dojo.render.html.ie55||dojo.render.html.ie60){
_1fd=iefixPathsInCssText();
}
while(_200=_205.exec(_1fd)){
url=_200[1].replace(_202,"$2");
if(!_203.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_1fd.substring(0,_200.index)+"url("+url+")";
_1fd=_1fd.substr(_200.index+_200[0].length);
}
return str+_1fd;
};
dojo.html.setActiveStyleSheet=function(_206){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("title")){
a.disabled=true;
if(a.getAttribute("title")==_206){
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
var _212={dj_ie:drh.ie,dj_ie55:drh.ie55,dj_ie6:drh.ie60,dj_ie7:drh.ie70,dj_iequirks:drh.ie&&drh.quirks,dj_opera:drh.opera,dj_opera8:drh.opera&&(Math.floor(dojo.render.version)==8),dj_opera9:drh.opera&&(Math.floor(dojo.render.version)==9),dj_khtml:drh.khtml,dj_safari:drh.safari,dj_gecko:drh.mozilla};
for(var p in _212){
if(_212[p]){
dojo.html.addClass(node,p);
}
}
};
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
dojo.string.repeat=function(str,_21a,_21b){
var out="";
for(var i=0;i<_21a;i++){
out+=str;
if(_21b&&i<_21a-1){
out+=_21b;
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
dojo.lang.setTimeout=function(func,_22a){
var _22b=window,_22c=2;
if(!dojo.lang.isFunction(func)){
_22b=func;
func=_22a;
_22a=arguments[2];
_22c++;
}
if(dojo.lang.isString(func)){
func=_22b[func];
}
var args=[];
for(var i=_22c;i<arguments.length;i++){
args.push(arguments[i]);
}
return dojo.global().setTimeout(function(){
func.apply(_22b,args);
},_22a);
};
dojo.lang.clearTimeout=function(_22f){
dojo.global().clearTimeout(_22f);
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
dojo.lang.getObjPathValue=function(_238,_239,_23a){
with(dojo.parseObjPath(_238,_239,_23a)){
return dojo.evalProp(prop,obj,_23a);
}
};
dojo.lang.setObjPathValue=function(_23b,_23c,_23d,_23e){
if(arguments.length<4){
_23e=true;
}
with(dojo.parseObjPath(_23b,_23d,_23e)){
if(obj&&(_23e||(prop in obj))){
obj[prop]=_23c;
}
}
};
dojo.provide("dojo.io.common");
dojo.io.transports=[];
dojo.io.hdlrFuncNames=["load","error","timeout"];
dojo.io.Request=function(url,_240,_241,_242){
if((arguments.length==1)&&(arguments[0].constructor==Object)){
this.fromKwArgs(arguments[0]);
}else{
this.url=url;
if(_240){
this.mimetype=_240;
}
if(_241){
this.transport=_241;
}
if(arguments.length>=4){
this.changeUrl=_242;
}
}
};
dojo.lang.extend(dojo.io.Request,{url:"",mimetype:"text/plain",method:"GET",content:undefined,transport:undefined,changeUrl:undefined,formNode:undefined,sync:false,bindSuccess:false,useCache:false,preventCache:false,load:function(type,data,_245,_246){
},error:function(type,_248,_249,_24a){
},timeout:function(type,_24c,_24d,_24e){
},handle:function(type,data,_251,_252){
},timeoutSeconds:0,abort:function(){
},fromKwArgs:function(_253){
if(_253["url"]){
_253.url=_253.url.toString();
}
if(_253["formNode"]){
_253.formNode=dojo.byId(_253.formNode);
}
if(!_253["method"]&&_253["formNode"]&&_253["formNode"].method){
_253.method=_253["formNode"].method;
}
if(!_253["handle"]&&_253["handler"]){
_253.handle=_253.handler;
}
if(!_253["load"]&&_253["loaded"]){
_253.load=_253.loaded;
}
if(!_253["changeUrl"]&&_253["changeURL"]){
_253.changeUrl=_253.changeURL;
}
_253.encoding=dojo.lang.firstValued(_253["encoding"],djConfig["bindEncoding"],"");
_253.sendTransport=dojo.lang.firstValued(_253["sendTransport"],djConfig["ioSendTransport"],false);
var _254=dojo.lang.isFunction;
for(var x=0;x<dojo.io.hdlrFuncNames.length;x++){
var fn=dojo.io.hdlrFuncNames[x];
if(_253[fn]&&_254(_253[fn])){
continue;
}
if(_253["handle"]&&_254(_253["handle"])){
_253[fn]=_253.handle;
}
}
dojo.lang.mixin(this,_253);
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
dojo.io.bind=function(_25b){
if(!(_25b instanceof dojo.io.Request)){
try{
_25b=new dojo.io.Request(_25b);
}
catch(e){
dojo.debug(e);
}
}
var _25c="";
if(_25b["transport"]){
_25c=_25b["transport"];
if(!this[_25c]){
dojo.io.sendBindError(_25b,"No dojo.io.bind() transport with name '"+_25b["transport"]+"'.");
return _25b;
}
if(!this[_25c].canHandle(_25b)){
dojo.io.sendBindError(_25b,"dojo.io.bind() transport with name '"+_25b["transport"]+"' cannot handle this type of request.");
return _25b;
}
}else{
for(var x=0;x<dojo.io.transports.length;x++){
var tmp=dojo.io.transports[x];
if((this[tmp])&&(this[tmp].canHandle(_25b))){
_25c=tmp;
break;
}
}
if(_25c==""){
dojo.io.sendBindError(_25b,"None of the loaded transports for dojo.io.bind()"+" can handle the request.");
return _25b;
}
}
this[_25c].bind(_25b);
_25b.bindSuccess=true;
return _25b;
};
dojo.io.sendBindError=function(_25f,_260){
if((typeof _25f.error=="function"||typeof _25f.handle=="function")&&(typeof setTimeout=="function"||typeof setTimeout=="object")){
var _261=new dojo.io.Error(_260);
setTimeout(function(){
_25f[(typeof _25f.error=="function")?"error":"handle"]("error",_261,null,_25f);
},50);
}else{
dojo.raise(_260);
}
};
dojo.io.queueBind=function(_262){
if(!(_262 instanceof dojo.io.Request)){
try{
_262=new dojo.io.Request(_262);
}
catch(e){
dojo.debug(e);
}
}
var _263=_262.load;
_262.load=function(){
dojo.io._queueBindInFlight=false;
var ret=_263.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
var _265=_262.error;
_262.error=function(){
dojo.io._queueBindInFlight=false;
var ret=_265.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
dojo.io._bindQueue.push(_262);
dojo.io._dispatchNextQueueBind();
return _262;
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
dojo.io.argsFromMap=function(map,_268,last){
var enc=/utf/i.test(_268||"")?encodeURIComponent:dojo.string.encodeAscii;
var _26b=[];
var _26c=new Object();
for(var name in map){
var _26e=function(elt){
var val=enc(name)+"="+enc(elt);
_26b[(last==name)?"push":"unshift"](val);
};
if(!_26c[name]){
var _271=map[name];
if(dojo.lang.isArray(_271)){
dojo.lang.forEach(_271,_26e);
}else{
_26e(_271);
}
}
}
return _26b.join("&");
};
dojo.io.setIFrameSrc=function(_272,src,_274){
try{
var r=dojo.render.html;
if(!_274){
if(r.safari){
_272.location=src;
}else{
frames[_272.name].location=src;
}
}else{
var idoc;
if(r.ie){
idoc=_272.contentWindow.document;
}else{
if(r.safari){
idoc=_272.document;
}else{
idoc=_272.contentWindow;
}
}
if(!idoc){
_272.location=src;
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
dojo.lang.has=function(obj,name){
try{
return typeof obj[name]!="undefined";
}
catch(e){
return false;
}
};
dojo.lang.isEmpty=function(obj){
if(dojo.lang.isObject(obj)){
var tmp={};
var _27b=0;
for(var x in obj){
if(obj[x]&&(!tmp[x])){
_27b++;
break;
}
}
return _27b==0;
}else{
if(dojo.lang.isArrayLike(obj)||dojo.lang.isString(obj)){
return obj.length==0;
}
}
};
dojo.lang.map=function(arr,obj,_27f){
var _280=dojo.lang.isString(arr);
if(_280){
arr=arr.split("");
}
if(dojo.lang.isFunction(obj)&&(!_27f)){
_27f=obj;
obj=dj_global;
}else{
if(dojo.lang.isFunction(obj)&&_27f){
var _281=obj;
obj=_27f;
_27f=_281;
}
}
if(Array.map){
var _282=Array.map(arr,_27f,obj);
}else{
var _282=[];
for(var i=0;i<arr.length;++i){
_282.push(_27f.call(obj,arr[i]));
}
}
if(_280){
return _282.join("");
}else{
return _282;
}
};
dojo.lang.reduce=function(arr,_285,obj,_287){
var _288=_285;
var ob=obj?obj:dj_global;
dojo.lang.map(arr,function(val){
_288=_287.call(ob,_288,val);
});
return _288;
};
dojo.lang.forEach=function(_28b,_28c,_28d){
if(dojo.lang.isString(_28b)){
_28b=_28b.split("");
}
if(Array.forEach){
Array.forEach(_28b,_28c,_28d);
}else{
if(!_28d){
_28d=dj_global;
}
for(var i=0,l=_28b.length;i<l;i++){
_28c.call(_28d,_28b[i],i,_28b);
}
}
};
dojo.lang._everyOrSome=function(_290,arr,_292,_293){
if(dojo.lang.isString(arr)){
arr=arr.split("");
}
if(Array.every){
return Array[_290?"every":"some"](arr,_292,_293);
}else{
if(!_293){
_293=dj_global;
}
for(var i=0,l=arr.length;i<l;i++){
var _296=_292.call(_293,arr[i],i,arr);
if(_290&&!_296){
return false;
}else{
if((!_290)&&(_296)){
return true;
}
}
}
return Boolean(_290);
}
};
dojo.lang.every=function(arr,_298,_299){
return this._everyOrSome(true,arr,_298,_299);
};
dojo.lang.some=function(arr,_29b,_29c){
return this._everyOrSome(false,arr,_29b,_29c);
};
dojo.lang.filter=function(arr,_29e,_29f){
var _2a0=dojo.lang.isString(arr);
if(_2a0){
arr=arr.split("");
}
var _2a1;
if(Array.filter){
_2a1=Array.filter(arr,_29e,_29f);
}else{
if(!_29f){
if(arguments.length>=3){
dojo.raise("thisObject doesn't exist!");
}
_29f=dj_global;
}
_2a1=[];
for(var i=0;i<arr.length;i++){
if(_29e.call(_29f,arr[i],i,arr)){
_2a1.push(arr[i]);
}
}
}
if(_2a0){
return _2a1.join("");
}else{
return _2a1;
}
};
dojo.lang.unnest=function(){
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
};
dojo.lang.toArray=function(_2a6,_2a7){
var _2a8=[];
for(var i=_2a7||0;i<_2a6.length;i++){
_2a8.push(_2a6[i]);
}
return _2a8;
};
dojo.provide("dojo.lang.func");
dojo.lang.hitch=function(_2aa,_2ab){
var fcn=(dojo.lang.isString(_2ab)?_2aa[_2ab]:_2ab)||function(){
};
return function(){
return fcn.apply(_2aa,arguments);
};
};
dojo.lang.anonCtr=0;
dojo.lang.anon={};
dojo.lang.nameAnonFunc=function(_2ad,_2ae,_2af){
var nso=(_2ae||dojo.lang.anon);
if((_2af)||((dj_global["djConfig"])&&(djConfig["slowAnonFuncLookups"]==true))){
for(var x in nso){
try{
if(nso[x]===_2ad){
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
nso[ret]=_2ad;
return ret;
};
dojo.lang.forward=function(_2b3){
return function(){
return this[_2b3].apply(this,arguments);
};
};
dojo.lang.curry=function(ns,func){
var _2b6=[];
ns=ns||dj_global;
if(dojo.lang.isString(func)){
func=ns[func];
}
for(var x=2;x<arguments.length;x++){
_2b6.push(arguments[x]);
}
var _2b8=(func["__preJoinArity"]||func.length)-_2b6.length;
function gather(_2b9,_2ba,_2bb){
var _2bc=_2bb;
var _2bd=_2ba.slice(0);
for(var x=0;x<_2b9.length;x++){
_2bd.push(_2b9[x]);
}
_2bb=_2bb-_2b9.length;
if(_2bb<=0){
var res=func.apply(ns,_2bd);
_2bb=_2bc;
return res;
}else{
return function(){
return gather(arguments,_2bd,_2bb);
};
}
}
return gather([],_2b6,_2b8);
};
dojo.lang.curryArguments=function(ns,func,args,_2c3){
var _2c4=[];
var x=_2c3||0;
for(x=_2c3;x<args.length;x++){
_2c4.push(args[x]);
}
return dojo.lang.curry.apply(dojo.lang,[ns,func].concat(_2c4));
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
dojo.lang.delayThese=function(farr,cb,_2ca,_2cb){
if(!farr.length){
if(typeof _2cb=="function"){
_2cb();
}
return;
}
if((typeof _2ca=="undefined")&&(typeof cb=="number")){
_2ca=cb;
cb=function(){
};
}else{
if(!cb){
cb=function(){
};
if(!_2ca){
_2ca=0;
}
}
}
setTimeout(function(){
(farr.shift())();
cb();
dojo.lang.delayThese(farr,cb,_2ca,_2cb);
},_2ca);
};
dojo.provide("dojo.string.extras");
dojo.string.substituteParams=function(_2cc,hash){
var map=(typeof hash=="object")?hash:dojo.lang.toArray(arguments,1);
return _2cc.replace(/\%\{(\w+)\}/g,function(_2cf,key){
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
var _2d2=str.split(" ");
for(var i=0;i<_2d2.length;i++){
_2d2[i]=_2d2[i].charAt(0).toUpperCase()+_2d2[i].substring(1);
}
return _2d2.join(" ");
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
var _2d7=escape(str);
var _2d8,re=/%u([0-9A-F]{4})/i;
while((_2d8=_2d7.match(re))){
var num=Number("0x"+_2d8[1]);
var _2db=escape("&#"+num+";");
ret+=_2d7.substring(0,_2d8.index)+_2db;
_2d7=_2d7.substring(_2d8.index+_2d8[0].length);
}
ret+=_2d7.replace(/\+/g,"%2B");
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
dojo.string.escapeXml=function(str,_2e0){
str=str.replace(/&/gm,"&amp;").replace(/</gm,"&lt;").replace(/>/gm,"&gt;").replace(/"/gm,"&quot;");
if(!_2e0){
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
dojo.string.endsWith=function(str,end,_2e9){
if(_2e9){
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
dojo.string.startsWith=function(str,_2ed,_2ee){
if(_2ee){
str=str.toLowerCase();
_2ed=_2ed.toLowerCase();
}
return str.indexOf(_2ed)==0;
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
dojo.string.normalizeNewlines=function(text,_2f4){
if(_2f4=="\n"){
text=text.replace(/\r\n/g,"\n");
text=text.replace(/\r/g,"\n");
}else{
if(_2f4=="\r"){
text=text.replace(/\r\n/g,"\r");
text=text.replace(/\n/g,"\r");
}else{
text=text.replace(/([^\r])\n/g,"$1\r\n").replace(/\r([^\n])/g,"\r\n$1");
}
}
return text;
};
dojo.string.splitEscaped=function(str,_2f6){
var _2f7=[];
for(var i=0,_2f9=0;i<str.length;i++){
if(str.charAt(i)=="\\"){
i++;
continue;
}
if(str.charAt(i)==_2f6){
_2f7.push(str.substring(_2f9,i));
_2f9=i+1;
}
}
_2f7.push(str.substr(_2f9));
return _2f7;
};
dojo.provide("dojo.undo.browser");
try{
if((!djConfig["preventBackButtonFix"])&&(!dojo.hostenv.post_load_)){
document.write("<iframe style='border: 0px; width: 1px; height: 1px; position: absolute; bottom: 0px; right: 0px; visibility: visible;' name='djhistory' id='djhistory' src='"+(dojo.hostenv.getBaseScriptUri()+"iframe_history.html")+"'></iframe>");
}
}
catch(e){
}
if(dojo.render.html.opera){
dojo.debug("Opera is not supported with dojo.undo.browser, so back/forward detection will not work.");
}
dojo.undo.browser={initialHref:window.location.href,initialHash:window.location.hash,moveForward:false,historyStack:[],forwardStack:[],historyIframe:null,bookmarkAnchor:null,locationTimer:null,setInitialState:function(args){
this.initialState=this._createState(this.initialHref,args,this.initialHash);
},addToHistory:function(args){
this.forwardStack=[];
var hash=null;
var url=null;
if(!this.historyIframe){
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
var _2fe=args["back"]||args["backButton"]||args["handle"];
var tcb=function(_300){
if(window.location.hash!=""){
setTimeout("window.location.href = '"+hash+"';",1);
}
_2fe.apply(this,[_300]);
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
var _301=args["forward"]||args["forwardButton"]||args["handle"];
var tfw=function(_303){
if(window.location.hash!=""){
window.location.href=hash;
}
if(_301){
_301.apply(this,[_303]);
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
},iframeLoaded:function(evt,_306){
if(!dojo.render.html.opera){
var _307=this._getUrlQuery(_306.href);
if(_307==null){
if(this.historyStack.length==1){
this.handleBackButton();
}
return;
}
if(this.moveForward){
this.moveForward=false;
return;
}
if(this.historyStack.length>=2&&_307==this._getUrlQuery(this.historyStack[this.historyStack.length-2].url)){
this.handleBackButton();
}else{
if(this.forwardStack.length>0&&_307==this._getUrlQuery(this.forwardStack[this.forwardStack.length-1].url)){
this.handleForwardButton();
}
}
}
},handleBackButton:function(){
var _308=this.historyStack.pop();
if(!_308){
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
this.forwardStack.push(_308);
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
var _30f=url.split("?");
if(_30f.length<2){
return null;
}else{
return _30f[1];
}
},_loadIframeHistory:function(){
var url=dojo.hostenv.getBaseScriptUri()+"iframe_history.html?"+(new Date()).getTime();
this.moveForward=true;
dojo.io.setIFrameSrc(this.historyIframe,url,false);
return url;
}};
dojo.provide("dojo.io.BrowserIO");
dojo.io.checkChildrenForFile=function(node){
var _312=false;
var _313=node.getElementsByTagName("input");
dojo.lang.forEach(_313,function(_314){
if(_312){
return;
}
if(_314.getAttribute("type")=="file"){
_312=true;
}
});
return _312;
};
dojo.io.formHasFile=function(_315){
return dojo.io.checkChildrenForFile(_315);
};
dojo.io.updateNode=function(node,_317){
node=dojo.byId(node);
var args=_317;
if(dojo.lang.isString(_317)){
args={url:_317};
}
args.mimetype="text/html";
args.load=function(t,d,e){
while(node.firstChild){
if(dojo["event"]){
try{
dojo.event.browser.clean(node.firstChild);
}
catch(e){
}
}
node.removeChild(node.firstChild);
}
node.innerHTML=d;
};
dojo.io.bind(args);
};
dojo.io.formFilter=function(node){
var type=(node.type||"").toLowerCase();
return !node.disabled&&node.name&&!dojo.lang.inArray(["file","submit","image","reset","button"],type);
};
dojo.io.encodeForm=function(_31e,_31f,_320){
if((!_31e)||(!_31e.tagName)||(!_31e.tagName.toLowerCase()=="form")){
dojo.raise("Attempted to encode a non-form element.");
}
if(!_320){
_320=dojo.io.formFilter;
}
var enc=/utf/i.test(_31f||"")?encodeURIComponent:dojo.string.encodeAscii;
var _322=[];
for(var i=0;i<_31e.elements.length;i++){
var elm=_31e.elements[i];
if(!elm||elm.tagName.toLowerCase()=="fieldset"||!_320(elm)){
continue;
}
var name=enc(elm.name);
var type=elm.type.toLowerCase();
if(type=="select-multiple"){
for(var j=0;j<elm.options.length;j++){
if(elm.options[j].selected){
_322.push(name+"="+enc(elm.options[j].value));
}
}
}else{
if(dojo.lang.inArray(["radio","checkbox"],type)){
if(elm.checked){
_322.push(name+"="+enc(elm.value));
}
}else{
_322.push(name+"="+enc(elm.value));
}
}
}
var _328=_31e.getElementsByTagName("input");
for(var i=0;i<_328.length;i++){
var _329=_328[i];
if(_329.type.toLowerCase()=="image"&&_329.form==_31e&&_320(_329)){
var name=enc(_329.name);
_322.push(name+"="+enc(_329.value));
_322.push(name+".x=0");
_322.push(name+".y=0");
}
}
return _322.join("&")+"&";
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
var _32f=form.getElementsByTagName("input");
for(var i=0;i<_32f.length;i++){
var _330=_32f[i];
if(_330.type.toLowerCase()=="image"&&_330.form==form){
this.connect(_330,"onclick","click");
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
var _337=false;
if(node.disabled||!node.name){
_337=false;
}else{
if(dojo.lang.inArray(["submit","button","image"],type)){
if(!this.clickedButton){
this.clickedButton=node;
}
_337=node==this.clickedButton;
}else{
_337=!dojo.lang.inArray(["file","submit","reset","button"],type);
}
}
return _337;
},connect:function(_338,_339,_33a){
if(dojo.evalObjPath("dojo.event.connect")){
dojo.event.connect(_338,_339,this,_33a);
}else{
var fcn=dojo.lang.hitch(this,_33a);
_338[_339]=function(e){
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
var _33d=this;
var _33e={};
this.useCache=false;
this.preventCache=false;
function getCacheKey(url,_340,_341){
return url+"|"+_340+"|"+_341.toLowerCase();
}
function addToCache(url,_343,_344,http){
_33e[getCacheKey(url,_343,_344)]=http;
}
function getFromCache(url,_347,_348){
return _33e[getCacheKey(url,_347,_348)];
}
this.clearCache=function(){
_33e={};
};
function doLoad(_349,http,url,_34c,_34d){
if(((http.status>=200)&&(http.status<300))||(http.status==304)||(location.protocol=="file:"&&(http.status==0||http.status==undefined))||(location.protocol=="chrome:"&&(http.status==0||http.status==undefined))){
var ret;
if(_349.method.toLowerCase()=="head"){
var _34f=http.getAllResponseHeaders();
ret={};
ret.toString=function(){
return _34f;
};
var _350=_34f.split(/[\r\n]+/g);
for(var i=0;i<_350.length;i++){
var pair=_350[i].match(/^([^:]+)\s*:\s*(.+)$/i);
if(pair){
ret[pair[1]]=pair[2];
}
}
}else{
if(_349.mimetype=="text/javascript"){
try{
ret=dj_eval(http.responseText);
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=null;
}
}else{
if(_349.mimetype=="text/json"||_349.mimetype=="application/json"){
try{
ret=dj_eval("("+http.responseText+")");
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=false;
}
}else{
if((_349.mimetype=="application/xml")||(_349.mimetype=="text/xml")){
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
if(_34d){
addToCache(url,_34c,_349.method,http);
}
_349[(typeof _349.load=="function")?"load":"handle"]("load",ret,http,_349);
}else{
var _353=new dojo.io.Error("XMLHttpTransport Error: "+http.status+" "+http.statusText);
_349[(typeof _349.error=="function")?"error":"handle"]("error",_353,http,_349);
}
}
function setHeaders(http,_355){
if(_355["headers"]){
for(var _356 in _355["headers"]){
if(_356.toLowerCase()=="content-type"&&!_355["contentType"]){
_355["contentType"]=_355["headers"][_356];
}else{
http.setRequestHeader(_356,_355["headers"][_356]);
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
if(!dojo.hostenv._blockAsync&&!_33d._blockAsync){
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
var _35a=new dojo.io.Error("XMLHttpTransport.watchInFlight Error: "+e);
tif.req[(typeof tif.req.error=="function")?"error":"handle"]("error",_35a,tif.http,tif.req);
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
var _35b=dojo.hostenv.getXmlhttpObject()?true:false;
this.canHandle=function(_35c){
return _35b&&dojo.lang.inArray(["text/plain","text/html","application/xml","text/xml","text/javascript","text/json","application/json"],(_35c["mimetype"].toLowerCase()||""))&&!(_35c["formNode"]&&dojo.io.formHasFile(_35c["formNode"]));
};
this.multipartBoundary="45309FFF-BD65-4d50-99C9-36986896A96F";
this.bind=function(_35d){
if(!_35d["url"]){
if(!_35d["formNode"]&&(_35d["backButton"]||_35d["back"]||_35d["changeUrl"]||_35d["watchForURL"])&&(!djConfig.preventBackButtonFix)){
dojo.deprecated("Using dojo.io.XMLHTTPTransport.bind() to add to browser history without doing an IO request","Use dojo.undo.browser.addToHistory() instead.","0.4");
dojo.undo.browser.addToHistory(_35d);
return true;
}
}
var url=_35d.url;
var _35f="";
if(_35d["formNode"]){
var ta=_35d.formNode.getAttribute("action");
if((ta)&&(!_35d["url"])){
url=ta;
}
var tp=_35d.formNode.getAttribute("method");
if((tp)&&(!_35d["method"])){
_35d.method=tp;
}
_35f+=dojo.io.encodeForm(_35d.formNode,_35d.encoding,_35d["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
if(_35d["file"]){
_35d.method="post";
}
if(!_35d["method"]){
_35d.method="get";
}
if(_35d.method.toLowerCase()=="get"){
_35d.multipart=false;
}else{
if(_35d["file"]){
_35d.multipart=true;
}else{
if(!_35d["multipart"]){
_35d.multipart=false;
}
}
}
if(_35d["backButton"]||_35d["back"]||_35d["changeUrl"]){
dojo.undo.browser.addToHistory(_35d);
}
var _362=_35d["content"]||{};
if(_35d.sendTransport){
_362["dojo.transport"]="xmlhttp";
}
do{
if(_35d.postContent){
_35f=_35d.postContent;
break;
}
if(_362){
_35f+=dojo.io.argsFromMap(_362,_35d.encoding);
}
if(_35d.method.toLowerCase()=="get"||!_35d.multipart){
break;
}
var t=[];
if(_35f.length){
var q=_35f.split("&");
for(var i=0;i<q.length;++i){
if(q[i].length){
var p=q[i].split("=");
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+p[0]+"\"","",p[1]);
}
}
}
if(_35d.file){
if(dojo.lang.isArray(_35d.file)){
for(var i=0;i<_35d.file.length;++i){
var o=_35d.file[i];
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}else{
var o=_35d.file;
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}
if(t.length){
t.push("--"+this.multipartBoundary+"--","");
_35f=t.join("\r\n");
}
}while(false);
var _368=_35d["sync"]?false:true;
var _369=_35d["preventCache"]||(this.preventCache==true&&_35d["preventCache"]!=false);
var _36a=_35d["useCache"]==true||(this.useCache==true&&_35d["useCache"]!=false);
if(!_369&&_36a){
var _36b=getFromCache(url,_35f,_35d.method);
if(_36b){
doLoad(_35d,_36b,url,_35f,false);
return;
}
}
var http=dojo.hostenv.getXmlhttpObject(_35d);
var _36d=false;
if(_368){
var _36e=this.inFlight.push({"req":_35d,"http":http,"url":url,"query":_35f,"useCache":_36a,"startTime":_35d.timeoutSeconds?(new Date()).getTime():0});
this.startWatchingInFlight();
}else{
_33d._blockAsync=true;
}
if(_35d.method.toLowerCase()=="post"){
if(!_35d.user){
http.open("POST",url,_368);
}else{
http.open("POST",url,_368,_35d.user,_35d.password);
}
setHeaders(http,_35d);
http.setRequestHeader("Content-Type",_35d.multipart?("multipart/form-data; boundary="+this.multipartBoundary):(_35d.contentType||"application/x-www-form-urlencoded"));
try{
http.send(_35f);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_35d,{status:404},url,_35f,_36a);
}
}else{
var _36f=url;
if(_35f!=""){
_36f+=(_36f.indexOf("?")>-1?"&":"?")+_35f;
}
if(_369){
_36f+=(dojo.string.endsWithAny(_36f,"?","&")?"":(_36f.indexOf("?")>-1?"&":"?"))+"dojo.preventCache="+new Date().valueOf();
}
if(!_35d.user){
http.open(_35d.method.toUpperCase(),_36f,_368);
}else{
http.open(_35d.method.toUpperCase(),_36f,_368,_35d.user,_35d.password);
}
setHeaders(http,_35d);
try{
http.send(null);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_35d,{status:404},url,_35f,_36a);
}
}
if(!_368){
doLoad(_35d,http,url,_35f,_36a);
_33d._blockAsync=false;
}
_35d.abort=function(){
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
dojo.provide("dojo.io.cookie");
dojo.io.cookie.setCookie=function(name,_371,days,path,_374,_375){
var _376=-1;
if(typeof days=="number"&&days>=0){
var d=new Date();
d.setTime(d.getTime()+(days*24*60*60*1000));
_376=d.toGMTString();
}
_371=escape(_371);
document.cookie=name+"="+_371+";"+(_376!=-1?" expires="+_376+";":"")+(path?"path="+path:"")+(_374?"; domain="+_374:"")+(_375?"; secure":"");
};
dojo.io.cookie.set=dojo.io.cookie.setCookie;
dojo.io.cookie.getCookie=function(name){
var idx=document.cookie.lastIndexOf(name+"=");
if(idx==-1){
return null;
}
var _37a=document.cookie.substring(idx+name.length+1);
var end=_37a.indexOf(";");
if(end==-1){
end=_37a.length;
}
_37a=_37a.substring(0,end);
_37a=unescape(_37a);
return _37a;
};
dojo.io.cookie.get=dojo.io.cookie.getCookie;
dojo.io.cookie.deleteCookie=function(name){
dojo.io.cookie.setCookie(name,"-",0);
};
dojo.io.cookie.setObjectCookie=function(name,obj,days,path,_381,_382,_383){
if(arguments.length==5){
_383=_381;
_381=null;
_382=null;
}
var _384=[],_385,_386="";
if(!_383){
_385=dojo.io.cookie.getObjectCookie(name);
}
if(days>=0){
if(!_385){
_385={};
}
for(var prop in obj){
if(prop==null){
delete _385[prop];
}else{
if(typeof obj[prop]=="string"||typeof obj[prop]=="number"){
_385[prop]=obj[prop];
}
}
}
prop=null;
for(var prop in _385){
_384.push(escape(prop)+"="+escape(_385[prop]));
}
_386=_384.join("&");
}
dojo.io.cookie.setCookie(name,_386,days,path,_381,_382);
};
dojo.io.cookie.getObjectCookie=function(name){
var _389=null,_38a=dojo.io.cookie.getCookie(name);
if(_38a){
_389={};
var _38b=_38a.split("&");
for(var i=0;i<_38b.length;i++){
var pair=_38b[i].split("=");
var _38e=pair[1];
if(isNaN(_38e)){
_38e=unescape(pair[1]);
}
_389[unescape(pair[0])]=_38e;
}
}
return _389;
};
dojo.io.cookie.isSupported=function(){
if(typeof navigator.cookieEnabled!="boolean"){
dojo.io.cookie.setCookie("__TestingYourBrowserForCookieSupport__","CookiesAllowed",90,null);
var _38f=dojo.io.cookie.getCookie("__TestingYourBrowserForCookieSupport__");
navigator.cookieEnabled=(_38f=="CookiesAllowed");
if(navigator.cookieEnabled){
this.deleteCookie("__TestingYourBrowserForCookieSupport__");
}
}
return navigator.cookieEnabled;
};
if(!dojo.io.cookies){
dojo.io.cookies=dojo.io.cookie;
}
dojo.provide("dojo.io.*");
dojo.provide("dojo.event.common");
dojo.event=new function(){
this._canTimeout=dojo.lang.isFunction(dj_global["setTimeout"])||dojo.lang.isAlien(dj_global["setTimeout"]);
function interpolateArgs(args,_391){
var dl=dojo.lang;
var ao={srcObj:dj_global,srcFunc:null,adviceObj:dj_global,adviceFunc:null,aroundObj:null,aroundFunc:null,adviceType:(args.length>2)?args[0]:"after",precedence:"last",once:false,delay:null,rate:0,adviceMsg:false};
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
var _394=dl.nameAnonFunc(args[2],ao.adviceObj,_391);
ao.adviceFunc=_394;
}else{
if((dl.isFunction(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))){
ao.adviceType="after";
ao.srcObj=dj_global;
var _394=dl.nameAnonFunc(args[0],ao.srcObj,_391);
ao.srcFunc=_394;
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
var _394=dl.nameAnonFunc(args[1],dj_global,_391);
ao.srcFunc=_394;
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))&&(dl.isFunction(args[3]))){
ao.srcObj=args[1];
ao.srcFunc=args[2];
var _394=dl.nameAnonFunc(args[3],dj_global,_391);
ao.adviceObj=dj_global;
ao.adviceFunc=_394;
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
break;
}
if(dl.isFunction(ao.aroundFunc)){
var _394=dl.nameAnonFunc(ao.aroundFunc,ao.aroundObj,_391);
ao.aroundFunc=_394;
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
if(dojo.lang.isString(ao.srcFunc)&&(ao.srcFunc.toLowerCase()=="onkey")){
if(dojo.render.html.ie){
ao.srcFunc="onkeydown";
this.connect(ao);
}
ao.srcFunc="onkeypress";
}
if(dojo.lang.isArray(ao.srcObj)&&ao.srcObj!=""){
var _396={};
for(var x in ao){
_396[x]=ao[x];
}
var mjps=[];
dojo.lang.forEach(ao.srcObj,function(src){
if((dojo.render.html.capable)&&(dojo.lang.isString(src))){
src=dojo.byId(src);
}
_396.srcObj=src;
mjps.push(dojo.event.connect.call(dojo.event,_396));
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
var _39e;
if((arguments.length==1)&&(typeof a1=="object")){
_39e=a1;
}else{
_39e={srcObj:a1,srcFunc:a2};
}
_39e.adviceFunc=function(){
var _39f=[];
for(var x=0;x<arguments.length;x++){
_39f.push(arguments[x]);
}
dojo.debug("("+_39e.srcObj+")."+_39e.srcFunc,":",_39f.join(", "));
};
this.kwConnect(_39e);
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
this._kwConnectImpl=function(_3a6,_3a7){
var fn=(_3a7)?"disconnect":"connect";
if(typeof _3a6["srcFunc"]=="function"){
_3a6.srcObj=_3a6["srcObj"]||dj_global;
var _3a9=dojo.lang.nameAnonFunc(_3a6.srcFunc,_3a6.srcObj,true);
_3a6.srcFunc=_3a9;
}
if(typeof _3a6["adviceFunc"]=="function"){
_3a6.adviceObj=_3a6["adviceObj"]||dj_global;
var _3a9=dojo.lang.nameAnonFunc(_3a6.adviceFunc,_3a6.adviceObj,true);
_3a6.adviceFunc=_3a9;
}
_3a6.srcObj=_3a6["srcObj"]||dj_global;
_3a6.adviceObj=_3a6["adviceObj"]||_3a6["targetObj"]||dj_global;
_3a6.adviceFunc=_3a6["adviceFunc"]||_3a6["targetFunc"];
return dojo.event[fn](_3a6);
};
this.kwConnect=function(_3aa){
return this._kwConnectImpl(_3aa,false);
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
var mjp=dojo.event.MethodJoinPoint.getForMethod(ao.srcObj,ao.srcFunc);
return mjp.removeAdvice(ao.adviceObj,ao.adviceFunc,ao.adviceType,ao.once);
};
this.kwDisconnect=function(_3ad){
return this._kwConnectImpl(_3ad,true);
};
};
dojo.event.MethodInvocation=function(_3ae,obj,args){
this.jp_=_3ae;
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
dojo.event.MethodJoinPoint=function(obj,_3b6){
this.object=obj||dj_global;
this.methodname=_3b6;
this.methodfunc=this.object[_3b6];
this.squelch=false;
};
dojo.event.MethodJoinPoint.getForMethod=function(obj,_3b8){
if(!obj){
obj=dj_global;
}
if(!obj[_3b8]){
obj[_3b8]=function(){
};
if(!obj[_3b8]){
dojo.raise("Cannot set do-nothing method on that object "+_3b8);
}
}else{
if((!dojo.lang.isFunction(obj[_3b8]))&&(!dojo.lang.isAlien(obj[_3b8]))){
return null;
}
}
var _3b9=_3b8+"$joinpoint";
var _3ba=_3b8+"$joinpoint$method";
var _3bb=obj[_3b9];
if(!_3bb){
var _3bc=false;
if(dojo.event["browser"]){
if((obj["attachEvent"])||(obj["nodeType"])||(obj["addEventListener"])){
_3bc=true;
dojo.event.browser.addClobberNodeAttrs(obj,[_3b9,_3ba,_3b8]);
}
}
var _3bd=obj[_3b8].length;
obj[_3ba]=obj[_3b8];
_3bb=obj[_3b9]=new dojo.event.MethodJoinPoint(obj,_3ba);
obj[_3b8]=function(){
var args=[];
if((_3bc)&&(!arguments.length)){
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
if((x==0)&&(_3bc)&&(dojo.event.browser.isEvent(arguments[x]))){
args.push(dojo.event.browser.fixEvent(arguments[x],this));
}else{
args.push(arguments[x]);
}
}
}
return _3bb.run.apply(_3bb,args);
};
obj[_3b8].__preJoinArity=_3bd;
}
return _3bb;
};
dojo.lang.extend(dojo.event.MethodJoinPoint,{unintercept:function(){
this.object[this.methodname]=this.methodfunc;
this.before=[];
this.after=[];
this.around=[];
},disconnect:dojo.lang.forward("unintercept"),run:function(){
var obj=this.object||dj_global;
var args=arguments;
var _3c3=[];
for(var x=0;x<args.length;x++){
_3c3[x]=args[x];
}
var _3c5=function(marr){
if(!marr){
dojo.debug("Null argument to unrollAdvice()");
return;
}
var _3c7=marr[0]||dj_global;
var _3c8=marr[1];
if(!_3c7[_3c8]){
dojo.raise("function \""+_3c8+"\" does not exist on \""+_3c7+"\"");
}
var _3c9=marr[2]||dj_global;
var _3ca=marr[3];
var msg=marr[6];
var _3cc;
var to={args:[],jp_:this,object:obj,proceed:function(){
return _3c7[_3c8].apply(_3c7,to.args);
}};
to.args=_3c3;
var _3ce=parseInt(marr[4]);
var _3cf=((!isNaN(_3ce))&&(marr[4]!==null)&&(typeof marr[4]!="undefined"));
if(marr[5]){
var rate=parseInt(marr[5]);
var cur=new Date();
var _3d2=false;
if((marr["last"])&&((cur-marr.last)<=rate)){
if(dojo.event._canTimeout){
if(marr["delayTimer"]){
clearTimeout(marr.delayTimer);
}
var tod=parseInt(rate*2);
var mcpy=dojo.lang.shallowCopy(marr);
marr.delayTimer=setTimeout(function(){
mcpy[5]=0;
_3c5(mcpy);
},tod);
}
return;
}else{
marr.last=cur;
}
}
if(_3ca){
_3c9[_3ca].call(_3c9,to);
}else{
if((_3cf)&&((dojo.render.html)||(dojo.render.svg))){
dj_global["setTimeout"](function(){
if(msg){
_3c7[_3c8].call(_3c7,to);
}else{
_3c7[_3c8].apply(_3c7,args);
}
},_3ce);
}else{
if(msg){
_3c7[_3c8].call(_3c7,to);
}else{
_3c7[_3c8].apply(_3c7,args);
}
}
}
};
var _3d5=function(){
if(this.squelch){
try{
return _3c5.apply(this,arguments);
}
catch(e){
dojo.debug(e);
}
}else{
return _3c5.apply(this,arguments);
}
};
if((this["before"])&&(this.before.length>0)){
dojo.lang.forEach(this.before.concat(new Array()),_3d5);
}
var _3d6;
try{
if((this["around"])&&(this.around.length>0)){
var mi=new dojo.event.MethodInvocation(this,obj,args);
_3d6=mi.proceed();
}else{
if(this.methodfunc){
_3d6=this.object[this.methodname].apply(this.object,args);
}
}
}
catch(e){
if(!this.squelch){
dojo.raise(e);
}
}
if((this["after"])&&(this.after.length>0)){
dojo.lang.forEach(this.after.concat(new Array()),_3d5);
}
return (this.methodfunc)?_3d6:null;
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
this.addAdvice(args["adviceObj"],args["adviceFunc"],args["aroundObj"],args["aroundFunc"],args["adviceType"],args["precedence"],args["once"],args["delay"],args["rate"],args["adviceMsg"]);
},addAdvice:function(_3db,_3dc,_3dd,_3de,_3df,_3e0,once,_3e2,rate,_3e4){
var arr=this.getArr(_3df);
if(!arr){
dojo.raise("bad this: "+this);
}
var ao=[_3db,_3dc,_3dd,_3de,_3e2,rate,_3e4];
if(once){
if(this.hasAdvice(_3db,_3dc,_3df,arr)>=0){
return;
}
}
if(_3e0=="first"){
arr.unshift(ao);
}else{
arr.push(ao);
}
},hasAdvice:function(_3e7,_3e8,_3e9,arr){
if(!arr){
arr=this.getArr(_3e9);
}
var ind=-1;
for(var x=0;x<arr.length;x++){
var aao=(typeof _3e8=="object")?(new String(_3e8)).toString():_3e8;
var a1o=(typeof arr[x][1]=="object")?(new String(arr[x][1])).toString():arr[x][1];
if((arr[x][0]==_3e7)&&(a1o==aao)){
ind=x;
}
}
return ind;
},removeAdvice:function(_3ef,_3f0,_3f1,once){
var arr=this.getArr(_3f1);
var ind=this.hasAdvice(_3ef,_3f0,_3f1,arr);
if(ind==-1){
return false;
}
while(ind!=-1){
arr.splice(ind,1);
if(once){
break;
}
ind=this.hasAdvice(_3ef,_3f0,_3f1,arr);
}
return true;
}});
dojo.provide("dojo.event.topic");
dojo.event.topic=new function(){
this.topics={};
this.getTopic=function(_3f5){
if(!this.topics[_3f5]){
this.topics[_3f5]=new this.TopicImpl(_3f5);
}
return this.topics[_3f5];
};
this.registerPublisher=function(_3f6,obj,_3f8){
var _3f6=this.getTopic(_3f6);
_3f6.registerPublisher(obj,_3f8);
};
this.subscribe=function(_3f9,obj,_3fb){
var _3f9=this.getTopic(_3f9);
_3f9.subscribe(obj,_3fb);
};
this.unsubscribe=function(_3fc,obj,_3fe){
var _3fc=this.getTopic(_3fc);
_3fc.unsubscribe(obj,_3fe);
};
this.destroy=function(_3ff){
this.getTopic(_3ff).destroy();
delete this.topics[_3ff];
};
this.publishApply=function(_400,args){
var _400=this.getTopic(_400);
_400.sendMessage.apply(_400,args);
};
this.publish=function(_402,_403){
var _402=this.getTopic(_402);
var args=[];
for(var x=1;x<arguments.length;x++){
args.push(arguments[x]);
}
_402.sendMessage.apply(_402,args);
};
};
dojo.event.topic.TopicImpl=function(_406){
this.topicName=_406;
this.subscribe=function(_407,_408){
var tf=_408||_407;
var to=(!_408)?dj_global:_407;
return dojo.event.kwConnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this.unsubscribe=function(_40b,_40c){
var tf=(!_40c)?_40b:_40c;
var to=(!_40c)?null:_40b;
return dojo.event.kwDisconnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this._getJoinPoint=function(){
return dojo.event.MethodJoinPoint.getForMethod(this,"sendMessage");
};
this.setSquelch=function(_40f){
this._getJoinPoint().squelch=_40f;
};
this.destroy=function(){
this._getJoinPoint().disconnect();
};
this.registerPublisher=function(_410,_411){
dojo.event.connect(_410,_411,this,"sendMessage");
};
this.sendMessage=function(_412){
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
this.clobber=function(_415){
var na;
var tna;
if(_415){
tna=_415.all||_415.getElementsByTagName("*");
na=[_415];
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
var _419={};
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
var _41d=0;
this.normalizedEventName=function(_41e){
switch(_41e){
case "CheckboxStateChange":
case "DOMAttrModified":
case "DOMMenuItemActive":
case "DOMMenuItemInactive":
case "DOMMouseScroll":
case "DOMNodeInserted":
case "DOMNodeRemoved":
case "RadioStateChange":
return _41e;
break;
default:
return _41e.toLowerCase();
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
this.addClobberNodeAttrs=function(node,_422){
if(!dojo.render.html.ie){
return;
}
this.addClobberNode(node);
for(var x=0;x<_422.length;x++){
node.__clobberAttrs__.push(_422[x]);
}
};
this.removeListener=function(node,_425,fp,_427){
if(!_427){
var _427=false;
}
_425=dojo.event.browser.normalizedEventName(_425);
if((_425=="onkey")||(_425=="key")){
if(dojo.render.html.ie){
this.removeListener(node,"onkeydown",fp,_427);
}
_425="onkeypress";
}
if(_425.substr(0,2)=="on"){
_425=_425.substr(2);
}
if(node.removeEventListener){
node.removeEventListener(_425,fp,_427);
}
};
this.addListener=function(node,_429,fp,_42b,_42c){
if(!node){
return;
}
if(!_42b){
var _42b=false;
}
_429=dojo.event.browser.normalizedEventName(_429);
if((_429=="onkey")||(_429=="key")){
if(dojo.render.html.ie){
this.addListener(node,"onkeydown",fp,_42b,_42c);
}
_429="onkeypress";
}
if(_429.substr(0,2)!="on"){
_429="on"+_429;
}
if(!_42c){
var _42d=function(evt){
if(!evt){
evt=window.event;
}
var ret=fp(dojo.event.browser.fixEvent(evt,this));
if(_42b){
dojo.event.browser.stopEvent(evt);
}
return ret;
};
}else{
_42d=fp;
}
if(node.addEventListener){
node.addEventListener(_429.substr(2),_42d,_42b);
return _42d;
}else{
if(typeof node[_429]=="function"){
var _430=node[_429];
node[_429]=function(e){
_430(e);
return _42d(e);
};
}else{
node[_429]=_42d;
}
if(dojo.render.html.ie){
this.addClobberNodeAttrs(node,[_429]);
}
return _42d;
}
};
this.isEvent=function(obj){
return (typeof obj!="undefined")&&(typeof Event!="undefined")&&(obj.eventPhase);
};
this.currentEvent=null;
this.callListener=function(_433,_434){
if(typeof _433!="function"){
dojo.raise("listener not a function: "+_433);
}
dojo.event.browser.currentEvent.currentTarget=_434;
return _433.call(_434,dojo.event.browser.currentEvent);
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
this.fixEvent=function(evt,_437){
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
var _439=evt.keyCode;
if(_439>=65&&_439<=90&&evt.shiftKey==false){
_439+=32;
}
if(_439>=1&&_439<=26&&evt.ctrlKey){
_439+=96;
}
evt.key=String.fromCharCode(_439);
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
var _439=evt.which;
if((evt.ctrlKey||evt.altKey||evt.metaKey)&&(evt.which>=65&&evt.which<=90&&evt.shiftKey==false)){
_439+=32;
}
evt.key=String.fromCharCode(_439);
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
default:
evt.key=evt.charCode>0?String.fromCharCode(evt.charCode):evt.keyCode;
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
evt.currentTarget=(_437?_437:evt.srcElement);
}
if(!evt.layerX){
evt.layerX=evt.offsetX;
}
if(!evt.layerY){
evt.layerY=evt.offsetY;
}
var doc=(evt.srcElement&&evt.srcElement.ownerDocument)?evt.srcElement.ownerDocument:document;
var _43b=((dojo.render.html.ie55)||(doc["compatMode"]=="BackCompat"))?doc.body:doc.documentElement;
if(!evt.pageX){
evt.pageX=evt.clientX+(_43b.scrollLeft||0);
}
if(!evt.pageY){
evt.pageY=evt.clientY+(_43b.scrollTop||0);
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
evt.returnValue=false;
evt.cancelBubble=true;
}else{
evt.preventDefault();
evt.stopPropagation();
}
};
};
dojo.provide("dojo.event.*");

