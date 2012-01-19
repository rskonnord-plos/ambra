dojo.require("dojo.string.extras");

  dojo.provide("topaz.manifest");
  var topazMap = {"regionalDialog":"topaz.widget.RegionalDialog"};
  
  dojo.registerNamespaceResolver( "topaz", function(name) {
      return topazMap[name];}
);