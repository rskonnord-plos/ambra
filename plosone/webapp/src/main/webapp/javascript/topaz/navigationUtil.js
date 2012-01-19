topaz.navigation = new Object();

topaz.navigation = {
 buildTOC: function(tocObj){
   var tocEl = document.getElementsByTagAndAttributeName(null, 'toc');
   
   var ul = document.createElement('ul');
   
   for (var i=0; i<tocEl.length; i++) {
     var li = document.createElement('li');
     var anchor = document.createElement('a');
     anchor.href = "#" + tocEl[i].getAttributeNode('toc').nodeValue;
     if (i == tocEl.length -1) {
       anchor.className = 'last';
     }
     var tocText = document.createTextNode(tocEl[i].getAttributeNode('title').nodeValue);
     anchor.appendChild(tocText);
     li.appendChild(anchor);
     
     ul.appendChild(li);
   }
   
   tocObj.appendChild(ul);
 } 
}  