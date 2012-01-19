topaz.htmlUtil = new Object();

topaz.htmlUtil = {
  getQuerystring: function() {
    var paramQuery = unescape(document.location.search.substring(1));
    var paramArray = paramQuery.split("&");
    
    var queryArray = new Array();
    
    for (var i=0;i<paramArray.length;i++) {
      var pair = paramArray[i].split("=");
      
      queryArray.push({param: pair[0], value: pair[1]});
    }     
    
    return queryArray;
  }  
}  