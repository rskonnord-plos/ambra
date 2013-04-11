(function() {
// Localize jQuery variable
  var jQuery;
  /******** Load jQuery if not present *********/
  if (window.jQuery === undefined || window.jQuery.fn.jquery !== '1.7.2') {
    var script_tag = document.createElement('script');
    script_tag.setAttribute("type","text/javascript");
    script_tag.setAttribute("src",
        "http://ajax.googleapis.com/ajax/libs/jquery/1.7/jquery.min.js");
    if (script_tag.readyState) {
      script_tag.onreadystatechange = function () { // For old versions of IE
        if (this.readyState == 'complete' || this.readyState == 'loaded') {
          scriptLoadHandler();
        }
      };
    } else { // Other browsers
      script_tag.onload = scriptLoadHandler;
    }
    // Try to find the head, otherwise default to the documentElement
    (document.getElementsByTagName("head")[0] || document.documentElement).appendChild(script_tag);
  } else {
    // The jQuery version on the window is the one we want to use
    jQuery = window.jQuery;
    main();
  }
  /******** Called once jQuery has loaded ******/
  function scriptLoadHandler() {
    // Restore $ and window.jQuery to their previous values and store the
    // new jQuery in our local jQuery variable
    jQuery = window.jQuery.noConflict(true);
    // Call our main function
    main();
  }
  /******** Our main function ********/
  function main() {
    (function(a){a.tools=a.tools||{version:"v1.2.7"},a.tools.overlay={addEffect:function(a,b,d){c[a]=[b,d]},conf:{close:null,closeOnClick:!0,closeOnEsc:!0,closeSpeed:"fast",effect:"default",fixed:!a.browser.msie||a.browser.version>6,left:"center",load:!1,mask:null,oneInstance:!0,speed:"normal",target:null,top:"10%"}};var b=[],c={};a.tools.overlay.addEffect("default",function(b,c){var d=this.getConf(),e=a(window);d.fixed||(b.top+=e.scrollTop(),b.left+=e.scrollLeft()),b.position=d.fixed?"fixed":"absolute",this.getOverlay().css(b).fadeIn(d.speed,c)},function(a){this.getOverlay().fadeOut(this.getConf().closeSpeed,a)});function d(d,e){var f=this,g=d.add(f),h=a(window),i,j,k,l=a.tools.expose&&(e.mask||e.expose),m=Math.random().toString().slice(10);l&&(typeof l=="string"&&(l={color:l}),l.closeOnClick=l.closeOnEsc=!1);var n=e.target||d.attr("rel");j=n?a(n):null||d;if(!j.length)throw"Could not find Overlay: "+n;d&&d.index(j)==-1&&d.click(function(a){f.load(a);return a.preventDefault()}),a.extend(f,{load:function(d){if(f.isOpened())return f;var i=c[e.effect];if(!i)throw"Overlay: cannot find effect : \""+e.effect+"\"";e.oneInstance&&a.each(b,function(){this.close(d)}),d=d||a.Event(),d.type="onBeforeLoad",g.trigger(d);if(d.isDefaultPrevented())return f;k=!0,l&&a(j).expose(l);var n=e.top,o=e.left,p=j.outerWidth({margin:!0}),q=j.outerHeight({margin:!0});typeof n=="string"&&(n=n=="center"?Math.max((h.height()-q)/2,0):parseInt(n,10)/100*h.height()),o=="center"&&(o=Math.max((h.width()-p)/2,0)),i[0].call(f,{top:n,left:o},function(){k&&(d.type="onLoad",g.trigger(d))}),l&&e.closeOnClick&&a.mask.getMask().one("click",f.close),e.closeOnClick&&a(document).on("click."+m,function(b){a(b.target).parents(j).length||f.close(b)}),e.closeOnEsc&&a(document).on("keydown."+m,function(a){a.keyCode==27&&f.close(a)});return f},close:function(b){if(!f.isOpened())return f;b=b||a.Event(),b.type="onBeforeClose",g.trigger(b);if(!b.isDefaultPrevented()){k=!1,c[e.effect][1].call(f,function(){b.type="onClose",g.trigger(b)}),a(document).off("click."+m+" keydown."+m),l&&a.mask.close();return f}},getOverlay:function(){return j},getTrigger:function(){return d},getClosers:function(){return i},isOpened:function(){return k},getConf:function(){return e}}),a.each("onBeforeLoad,onStart,onLoad,onBeforeClose,onClose".split(","),function(b,c){a.isFunction(e[c])&&a(f).on(c,e[c]),f[c]=function(b){b&&a(f).on(c,b);return f}}),i=j.find(e.close||".close"),!i.length&&!e.close&&(i=a("<a class=\"close\"></a>"),j.prepend(i)),i.click(function(a){f.close(a)}),e.load&&f.load()}a.fn.overlay=function(c){var e=this.data("overlay");if(e)return e;a.isFunction(c)&&(c={onBeforeLoad:c}),c=a.extend(!0,{},a.tools.overlay.conf,c),this.each(function(){e=new d(a(this),c),b.push(e),a(this).data("overlay",e)});return c.api?e:this}})(jQuery);
    (function(a){a.tools=a.tools||{version:"v1.2.7"};var b;b=a.tools.expose={conf:{maskId:"exposeMask",loadSpeed:"slow",closeSpeed:"fast",closeOnClick:!0,closeOnEsc:!0,zIndex:9998,opacity:.8,startOpacity:0,color:"#fff",onLoad:null,onClose:null}};function c(){if(a.browser.msie){var b=a(document).height(),c=a(window).height();return[window.innerWidth||document.documentElement.clientWidth||document.body.clientWidth,b-c<20?c:b]}return[a(document).width(),a(document).height()]}function d(b){if(b)return b.call(a.mask)}var e,f,g,h,i;a.mask={load:function(j,k){if(g)return this;typeof j=="string"&&(j={color:j}),j=j||h,h=j=a.extend(a.extend({},b.conf),j),e=a("#"+j.maskId),e.length||(e=a("<div/>").attr("id",j.maskId),a("body").append(e));var l=c();e.css({position:"absolute",top:0,left:0,width:l[0],height:l[1],display:"none",opacity:j.startOpacity,zIndex:j.zIndex}),j.color&&e.css("backgroundColor",j.color);if(d(j.onBeforeLoad)===!1)return this;j.closeOnEsc&&a(document).on("keydown.mask",function(b){b.keyCode==27&&a.mask.close(b)}),j.closeOnClick&&e.on("click.mask",function(b){a.mask.close(b)}),a(window).on("resize.mask",function(){a.mask.fit()}),k&&k.length&&(i=k.eq(0).css("zIndex"),a.each(k,function(){var b=a(this);/relative|absolute|fixed/i.test(b.css("position"))||b.css("position","relative")}),f=k.css({zIndex:Math.max(j.zIndex+1,i=="auto"?0:i)})),e.css({display:"block"}).fadeTo(j.loadSpeed,j.opacity,function(){a.mask.fit(),d(j.onLoad),g="full"}),g=!0;return this},close:function(){if(g){if(d(h.onBeforeClose)===!1)return this;e.fadeOut(h.closeSpeed,function(){d(h.onClose),f&&f.css({zIndex:i}),g=!1}),a(document).off("keydown.mask"),e.off("click.mask"),a(window).off("resize.mask")}return this},fit:function(){if(g){var a=c();e.css({width:a[0],height:a[1]})}},getMask:function(){return e},isLoaded:function(a){return a?g=="full":g},getConf:function(){return h},getExposed:function(){return f}},a.fn.mask=function(b){a.mask.load(b);return this},a.fn.expose=function(b){a.mask.load(b,this);return this}})(jQuery);
    (function(b,a,c){b.fn.jScrollPane=function(f){function d(E,P){var aA,R=this,Z,al,w,an,U,aa,z,r,aB,aG,aw,j,J,i,k,ab,V,ar,Y,u,B,at,ag,ao,H,m,av,az,y,ax,aJ,g,M,ak=true,Q=true,aI=false,l=false,aq=E.clone(false,false).empty(),ad=b.fn.mwheelIntent?"mwheelIntent.jsp":"mousewheel.jsp";aJ=E.css("paddingTop")+" "+E.css("paddingRight")+" "+E.css("paddingBottom")+" "+E.css("paddingLeft");g=(parseInt(E.css("paddingLeft"),10)||0)+(parseInt(E.css("paddingRight"),10)||0);function au(aS){var aN,aP,aO,aL,aK,aR,aQ=false,aM=false;aA=aS;if(Z===c){aK=E.scrollTop();aR=E.scrollLeft();E.css({overflow:"hidden",padding:0});al=E.innerWidth()+g;w=E.innerHeight();E.width(al);Z=b('<div class="jspPane" />').css("padding",aJ).append(E.children());an=b('<div class="jspContainer" />').css({width:al+"px",height:w+"px"}).append(Z).appendTo(E)}else{E.css("width","");aQ=aA.stickToBottom&&L();aM=aA.stickToRight&&C();aL=E.innerWidth()+g!=al||E.outerHeight()!=w;if(aL){al=E.innerWidth()+g;w=E.innerHeight();an.css({width:al+"px",height:w+"px"})}if(!aL&&M==U&&Z.outerHeight()==aa){E.width(al);return}M=U;Z.css("width","");E.width(al);an.find(">.jspVerticalBar,>.jspHorizontalBar").remove().end()}Z.css("overflow","auto");if(aS.contentWidth){U=aS.contentWidth}else{U=Z[0].scrollWidth}aa=Z[0].scrollHeight;Z.css("overflow","");z=U/al;r=aa/w;aB=r>1;aG=z>1;if(!(aG||aB)){E.removeClass("jspScrollable");Z.css({top:0,width:an.width()-g});o();F();S();x();aj()}else{E.addClass("jspScrollable");aN=aA.maintainPosition&&(J||ab);if(aN){aP=aE();aO=aC()}aH();A();G();if(aN){O(aM?(U-al):aP,false);N(aQ?(aa-w):aO,false)}K();ah();ap();if(aA.enableKeyboardNavigation){T()}if(aA.clickOnTrack){q()}D();if(aA.hijackInternalLinks){n()}}if(aA.autoReinitialise&&!ax){ax=setInterval(function(){au(aA)},aA.autoReinitialiseDelay)}else{if(!aA.autoReinitialise&&ax){clearInterval(ax)}}aK&&E.scrollTop(0)&&N(aK,false);aR&&E.scrollLeft(0)&&O(aR,false);E.trigger("jsp-initialised",[aG||aB])}function aH(){if(aB){an.append(b('<div class="jspVerticalBar" />').append(b('<div class="jspCap jspCapTop" />'),b('<div class="jspTrack" />').append(b('<div class="jspDrag" />').append(b('<div class="jspDragTop" />'),b('<div class="jspDragBottom" />'))),b('<div class="jspCap jspCapBottom" />')));V=an.find(">.jspVerticalBar");ar=V.find(">.jspTrack");aw=ar.find(">.jspDrag");if(aA.showArrows){at=b('<a class="jspArrow jspArrowUp" />').bind("mousedown.jsp",aF(0,-1)).bind("click.jsp",aD);ag=b('<a class="jspArrow jspArrowDown" />').bind("mousedown.jsp",aF(0,1)).bind("click.jsp",aD);if(aA.arrowScrollOnHover){at.bind("mouseover.jsp",aF(0,-1,at));ag.bind("mouseover.jsp",aF(0,1,ag))}am(ar,aA.verticalArrowPositions,at,ag)}u=w;an.find(">.jspVerticalBar>.jspCap:visible,>.jspVerticalBar>.jspArrow").each(function(){u-=b(this).outerHeight()});aw.hover(function(){aw.addClass("jspHover")},function(){aw.removeClass("jspHover")}).bind("mousedown.jsp",function(aK){b("html").bind("dragstart.jsp selectstart.jsp",aD);aw.addClass("jspActive");var s=aK.pageY-aw.position().top;b("html").bind("mousemove.jsp",function(aL){W(aL.pageY-s,false)}).bind("mouseup.jsp mouseleave.jsp",ay);return false});p()}}function p(){ar.height(u+"px");J=0;Y=aA.verticalGutter+ar.outerWidth();Z.width(al-Y-g);try{if(V.position().left===0){Z.css("margin-left",Y+"px")}}catch(s){}}function A(){if(aG){an.append(b('<div class="jspHorizontalBar" />').append(b('<div class="jspCap jspCapLeft" />'),b('<div class="jspTrack" />').append(b('<div class="jspDrag" />').append(b('<div class="jspDragLeft" />'),b('<div class="jspDragRight" />'))),b('<div class="jspCap jspCapRight" />')));ao=an.find(">.jspHorizontalBar");H=ao.find(">.jspTrack");i=H.find(">.jspDrag");if(aA.showArrows){az=b('<a class="jspArrow jspArrowLeft" />').bind("mousedown.jsp",aF(-1,0)).bind("click.jsp",aD);y=b('<a class="jspArrow jspArrowRight" />').bind("mousedown.jsp",aF(1,0)).bind("click.jsp",aD);
      if(aA.arrowScrollOnHover){az.bind("mouseover.jsp",aF(-1,0,az));y.bind("mouseover.jsp",aF(1,0,y))}am(H,aA.horizontalArrowPositions,az,y)}i.hover(function(){i.addClass("jspHover")},function(){i.removeClass("jspHover")}).bind("mousedown.jsp",function(aK){b("html").bind("dragstart.jsp selectstart.jsp",aD);i.addClass("jspActive");var s=aK.pageX-i.position().left;b("html").bind("mousemove.jsp",function(aL){X(aL.pageX-s,false)}).bind("mouseup.jsp mouseleave.jsp",ay);return false});m=an.innerWidth();ai()}}function ai(){an.find(">.jspHorizontalBar>.jspCap:visible,>.jspHorizontalBar>.jspArrow").each(function(){m-=b(this).outerWidth()});H.width(m+"px");ab=0}function G(){if(aG&&aB){var aK=H.outerHeight(),s=ar.outerWidth();u-=aK;b(ao).find(">.jspCap:visible,>.jspArrow").each(function(){m+=b(this).outerWidth()});m-=s;w-=s;al-=aK;H.parent().append(b('<div class="jspCorner" />').css("width",aK+"px"));p();ai()}if(aG){Z.width((an.outerWidth()-g)+"px")}aa=Z.outerHeight();r=aa/w;if(aG){av=Math.ceil(1/z*m);if(av>aA.horizontalDragMaxWidth){av=aA.horizontalDragMaxWidth}else{if(av<aA.horizontalDragMinWidth){av=aA.horizontalDragMinWidth}}i.width(av+"px");k=m-av;af(ab)}if(aB){B=Math.ceil(1/r*u);if(B>aA.verticalDragMaxHeight){B=aA.verticalDragMaxHeight}else{if(B<aA.verticalDragMinHeight){B=aA.verticalDragMinHeight}}aw.height(B+"px");j=u-B;ae(J)}}function am(aL,aN,aK,s){var aP="before",aM="after",aO;if(aN=="os"){aN=/Mac/.test(navigator.platform)?"after":"split"}if(aN==aP){aM=aN}else{if(aN==aM){aP=aN;aO=aK;aK=s;s=aO}}aL[aP](aK)[aM](s)}function aF(aK,s,aL){return function(){I(aK,s,this,aL);this.blur();return false}}function I(aN,aM,aQ,aP){aQ=b(aQ).addClass("jspActive");var aO,aL,aK=true,s=function(){if(aN!==0){R.scrollByX(aN*aA.arrowButtonSpeed)}if(aM!==0){R.scrollByY(aM*aA.arrowButtonSpeed)}aL=setTimeout(s,aK?aA.initialDelay:aA.arrowRepeatFreq);aK=false};s();aO=aP?"mouseout.jsp":"mouseup.jsp";aP=aP||b("html");aP.bind(aO,function(){aQ.removeClass("jspActive");aL&&clearTimeout(aL);aL=null;aP.unbind(aO)})}function q(){x();if(aB){ar.bind("mousedown.jsp",function(aP){if(aP.originalTarget===c||aP.originalTarget==aP.currentTarget){var aN=b(this),aQ=aN.offset(),aO=aP.pageY-aQ.top-J,aL,aK=true,s=function(){var aT=aN.offset(),aU=aP.pageY-aT.top-B/2,aR=w*aA.scrollPagePercent,aS=j*aR/(aa-w);if(aO<0){if(J-aS>aU){R.scrollByY(-aR)}else{W(aU)}}else{if(aO>0){if(J+aS<aU){R.scrollByY(aR)}else{W(aU)}}else{aM();return}}aL=setTimeout(s,aK?aA.initialDelay:aA.trackClickRepeatFreq);aK=false},aM=function(){aL&&clearTimeout(aL);aL=null;b(document).unbind("mouseup.jsp",aM)};s();b(document).bind("mouseup.jsp",aM);return false}})}if(aG){H.bind("mousedown.jsp",function(aP){if(aP.originalTarget===c||aP.originalTarget==aP.currentTarget){var aN=b(this),aQ=aN.offset(),aO=aP.pageX-aQ.left-ab,aL,aK=true,s=function(){var aT=aN.offset(),aU=aP.pageX-aT.left-av/2,aR=al*aA.scrollPagePercent,aS=k*aR/(U-al);if(aO<0){if(ab-aS>aU){R.scrollByX(-aR)}else{X(aU)}}else{if(aO>0){if(ab+aS<aU){R.scrollByX(aR)}else{X(aU)}}else{aM();return}}aL=setTimeout(s,aK?aA.initialDelay:aA.trackClickRepeatFreq);aK=false},aM=function(){aL&&clearTimeout(aL);aL=null;b(document).unbind("mouseup.jsp",aM)};s();b(document).bind("mouseup.jsp",aM);return false}})}}function x(){if(H){H.unbind("mousedown.jsp")}if(ar){ar.unbind("mousedown.jsp")}}function ay(){b("html").unbind("dragstart.jsp selectstart.jsp mousemove.jsp mouseup.jsp mouseleave.jsp");if(aw){aw.removeClass("jspActive")}if(i){i.removeClass("jspActive")}}function W(s,aK){if(!aB){return}if(s<0){s=0}else{if(s>j){s=j}}if(aK===c){aK=aA.animateScroll}if(aK){R.animate(aw,"top",s,ae)}else{aw.css("top",s);ae(s)}}function ae(aK){if(aK===c){aK=aw.position().top}an.scrollTop(0);J=aK;var aN=J===0,aL=J==j,aM=aK/j,s=-aM*(aa-w);if(ak!=aN||aI!=aL){ak=aN;aI=aL;E.trigger("jsp-arrow-change",[ak,aI,Q,l])}v(aN,aL);Z.css("top",s);E.trigger("jsp-scroll-y",[-s,aN,aL]).trigger("scroll")}function X(aK,s){if(!aG){return}if(aK<0){aK=0}else{if(aK>k){aK=k}}if(s===c){s=aA.animateScroll}if(s){R.animate(i,"left",aK,af)
    }else{i.css("left",aK);af(aK)}}function af(aK){if(aK===c){aK=i.position().left}an.scrollTop(0);ab=aK;var aN=ab===0,aM=ab==k,aL=aK/k,s=-aL*(U-al);if(Q!=aN||l!=aM){Q=aN;l=aM;E.trigger("jsp-arrow-change",[ak,aI,Q,l])}t(aN,aM);Z.css("left",s);E.trigger("jsp-scroll-x",[-s,aN,aM]).trigger("scroll")}function v(aK,s){if(aA.showArrows){at[aK?"addClass":"removeClass"]("jspDisabled");ag[s?"addClass":"removeClass"]("jspDisabled")}}function t(aK,s){if(aA.showArrows){az[aK?"addClass":"removeClass"]("jspDisabled");y[s?"addClass":"removeClass"]("jspDisabled")}}function N(s,aK){var aL=s/(aa-w);W(aL*j,aK)}function O(aK,s){var aL=aK/(U-al);X(aL*k,s)}function ac(aX,aS,aL){var aP,aM,aN,s=0,aW=0,aK,aR,aQ,aU,aT,aV;try{aP=b(aX)}catch(aO){return}aM=aP.outerHeight();aN=aP.outerWidth();an.scrollTop(0);an.scrollLeft(0);while(!aP.is(".jspPane")){s+=aP.position().top;aW+=aP.position().left;aP=aP.offsetParent();if(/^body|html$/i.test(aP[0].nodeName)){return}}aK=aC();aQ=aK+w;if(s<aK||aS){aT=s-aA.verticalGutter}else{if(s+aM>aQ){aT=s-w+aM+aA.verticalGutter}}if(aT){N(aT,aL)}aR=aE();aU=aR+al;if(aW<aR||aS){aV=aW-aA.horizontalGutter}else{if(aW+aN>aU){aV=aW-al+aN+aA.horizontalGutter}}if(aV){O(aV,aL)}}function aE(){return -Z.position().left}function aC(){return -Z.position().top}function L(){var s=aa-w;return(s>20)&&(s-aC()<10)}function C(){var s=U-al;return(s>20)&&(s-aE()<10)}function ah(){an.unbind(ad).bind(ad,function(aN,aO,aM,aK){var aL=ab,s=J;R.scrollBy(aM*aA.mouseWheelSpeed,-aK*aA.mouseWheelSpeed,false);return aL==ab&&s==J})}function o(){an.unbind(ad)}function aD(){return false}function K(){Z.find(":input,a").unbind("focus.jsp").bind("focus.jsp",function(s){ac(s.target,false)})}function F(){Z.find(":input,a").unbind("focus.jsp")}function T(){var s,aK,aM=[];aG&&aM.push(ao[0]);aB&&aM.push(V[0]);Z.focus(function(){E.focus()});E.attr("tabindex",0).unbind("keydown.jsp keypress.jsp").bind("keydown.jsp",function(aP){if(aP.target!==this&&!(aM.length&&b(aP.target).closest(aM).length)){return}var aO=ab,aN=J;switch(aP.keyCode){case 40:case 38:case 34:case 32:case 33:case 39:case 37:s=aP.keyCode;aL();break;case 35:N(aa-w);s=null;break;case 36:N(0);s=null;break}aK=aP.keyCode==s&&aO!=ab||aN!=J;return !aK}).bind("keypress.jsp",function(aN){if(aN.keyCode==s){aL()}return !aK});if(aA.hideFocus){E.css("outline","none");if("hideFocus" in an[0]){E.attr("hideFocus",true)}}else{E.css("outline","");if("hideFocus" in an[0]){E.attr("hideFocus",false)}}function aL(){var aO=ab,aN=J;switch(s){case 40:R.scrollByY(aA.keyboardSpeed,false);break;case 38:R.scrollByY(-aA.keyboardSpeed,false);break;case 34:case 32:R.scrollByY(w*aA.scrollPagePercent,false);break;case 33:R.scrollByY(-w*aA.scrollPagePercent,false);break;case 39:R.scrollByX(aA.keyboardSpeed,false);break;case 37:R.scrollByX(-aA.keyboardSpeed,false);break}aK=aO!=ab||aN!=J;return aK}}function S(){E.attr("tabindex","-1").removeAttr("tabindex").unbind("keydown.jsp keypress.jsp")}function D(){if(location.hash&&location.hash.length>1){var aL,aK;try{aL=b(location.hash)}catch(s){return}if(aL.length&&Z.find(location.hash)){if(an.scrollTop()===0){aK=setInterval(function(){if(an.scrollTop()>0){ac(location.hash,true);b(document).scrollTop(an.position().top);clearInterval(aK)}},50)}else{ac(location.hash,true);b(document).scrollTop(an.position().top)}}}}function aj(){b("a.jspHijack").unbind("click.jsp-hijack").removeClass("jspHijack")}function n(){aj();b("a[href^=#]").addClass("jspHijack").bind("click.jsp-hijack",function(){var s=this.href.split("#"),aK;if(s.length>1){aK=s[1];if(aK.length>0&&Z.find("#"+aK).length>0){ac("#"+aK,true);return false}}})}function ap(){var aL,aK,aN,aM,aO,s=false;an.unbind("touchstart.jsp touchmove.jsp touchend.jsp click.jsp-touchclick").bind("touchstart.jsp",function(aP){var aQ=aP.originalEvent.touches[0];aL=aE();aK=aC();aN=aQ.pageX;aM=aQ.pageY;aO=false;s=true}).bind("touchmove.jsp",function(aS){if(!s){return}var aR=aS.originalEvent.touches[0],aQ=ab,aP=J;R.scrollTo(aL+aN-aR.pageX,aK+aM-aR.pageY);aO=aO||Math.abs(aN-aR.pageX)>5||Math.abs(aM-aR.pageY)>5;
      return aQ==ab&&aP==J}).bind("touchend.jsp",function(aP){s=false}).bind("click.jsp-touchclick",function(aP){if(aO){aO=false;return false}})}function h(){var s=aC(),aK=aE();E.removeClass("jspScrollable").unbind(".jsp");E.replaceWith(aq.append(Z.children()));aq.scrollTop(s);aq.scrollLeft(aK)}b.extend(R,{reinitialise:function(aK){aK=b.extend({},aA,aK);au(aK)},scrollToElement:function(aL,aK,s){ac(aL,aK,s)},scrollTo:function(aL,s,aK){O(aL,aK);N(s,aK)},scrollToX:function(aK,s){O(aK,s)},scrollToY:function(s,aK){N(s,aK)},scrollToPercentX:function(aK,s){O(aK*(U-al),s)},scrollToPercentY:function(aK,s){N(aK*(aa-w),s)},scrollBy:function(aK,s,aL){R.scrollByX(aK,aL);R.scrollByY(s,aL)},scrollByX:function(s,aL){var aK=aE()+s,aM=aK/(U-al);X(aM*k,aL)},scrollByY:function(s,aL){var aK=aC()+s,aM=aK/(aa-w);W(aM*j,aL)},positionDragX:function(s,aK){X(s,aK)},positionDragY:function(aK,s){W(aK,s)},animate:function(aK,aN,s,aM){var aL={};aL[aN]=s;aK.animate(aL,{duration:aA.animateDuration,ease:aA.animateEase,queue:false,step:aM})},getContentPositionX:function(){return aE()},getContentPositionY:function(){return aC()},getContentWidth:function(){return U},getContentHeight:function(){return aa},getPercentScrolledX:function(){return aE()/(U-al)},getPercentScrolledY:function(){return aC()/(aa-w)},getIsScrollableH:function(){return aG},getIsScrollableV:function(){return aB},getContentPane:function(){return Z},scrollToBottom:function(s){W(j,s)},hijackInternalLinks:function(){n()},destroy:function(){h()}});au(P)}f=b.extend({},b.fn.jScrollPane.defaults,f);b.each(["mouseWheelSpeed","arrowButtonSpeed","trackClickSpeed","keyboardSpeed"],function(){f[this]=f[this]||f.speed});var e;this.each(function(){var g=b(this),h=g.data("jsp");if(h){h.reinitialise(f)}else{h=new d(g,f);g.data("jsp",h)}e=e?e.add(g):g});return e};b.fn.jScrollPane.defaults={showArrows:false,maintainPosition:true,stickToBottom:false,stickToRight:false,clickOnTrack:true,autoReinitialise:false,autoReinitialiseDelay:500,verticalDragMinHeight:0,verticalDragMaxHeight:99999,horizontalDragMinWidth:0,horizontalDragMaxWidth:99999,contentWidth:c,animateScroll:false,animateDuration:300,animateEase:"linear",hijackInternalLinks:false,verticalGutter:4,horizontalGutter:4,mouseWheelSpeed:0,arrowButtonSpeed:0,arrowRepeatFreq:50,arrowScrollOnHover:false,trackClickSpeed:0,trackClickRepeatFreq:70,verticalArrowPositions:"split",horizontalArrowPositions:"split",enableKeyboardNavigation:true,hideFocus:false,keyboardSpeed:0,initialDelay:300,speed:30,scrollPagePercent:0.8}})(jQuery,this);
    /*
     */
    (function($) {
      var types = ['DOMMouseScroll', 'mousewheel'];
      $.event.special.mousewheel = {
        setup: function() {
          if ( this.addEventListener ) {
            for ( var i=types.length; i; ) {
              this.addEventListener( types[--i], handler, false );
            }
          } else {
            this.onmousewheel = handler;
          }
        },
        teardown: function() {
          if ( this.removeEventListener ) {
            for ( var i=types.length; i; ) {
              this.removeEventListener( types[--i], handler, false );
            }
          } else {
            this.onmousewheel = null;
          }
        }
      };
      $.fn.extend({
        mousewheel: function(fn) {
          return fn ? this.bind("mousewheel", fn) : this.trigger("mousewheel");
        },
        unmousewheel: function(fn) {
          return this.unbind("mousewheel", fn);
        }
      });
      function handler(event) {
        var orgEvent = event || window.event, args = [].slice.call( arguments, 1 ), delta = 0, returnValue = true, deltaX = 0, deltaY = 0;
        event = $.event.fix(orgEvent);
        event.type = "mousewheel";
        // Old school scrollwheel delta
        if ( event.wheelDelta ) { delta = event.wheelDelta/120; }
        if ( event.detail     ) { delta = -event.detail/3; }
        // New school multidimensional scroll (touchpads) deltas
        deltaY = delta;
        // Gecko
        if ( orgEvent.axis !== undefined && orgEvent.axis === orgEvent.HORIZONTAL_AXIS ) {
          deltaY = 0;
          deltaX = -1*delta;
        }
        // Webkit
        if ( orgEvent.wheelDeltaY !== undefined ) { deltaY = orgEvent.wheelDeltaY/120; }
        if ( orgEvent.wheelDeltaX !== undefined ) { deltaX = -1*orgEvent.wheelDeltaX/120; }
        // Add event and delta to the front of the arguments
        args.unshift(event, delta, deltaX, deltaY);
        return $.event.handle.apply(this, args);
      }
    })(jQuery);
    jQuery.ajax = (function(_ajax){
      var protocol = location.protocol,
          hostname = location.hostname,
          exRegex = RegExp(protocol + '//' + hostname),
          YQL = 'http' + (/^https/.test(protocol)?'s':'') + '://query.yahooapis.com/v1/public/yql?callback=?',
          query = 'select * from json where url="{URL}"';
      function isExternal(url) {
        return !exRegex.test(url) && /:\/\//.test(url);
      }
      function get_id_from_url(url) {
        url = url.split('/')
        url = url[url.length-1]
        var id = url.split('.')[0]
        return id
      }
      function request_error(url) {
        url = url.split('/')
        url = url[url.length-1]
        var id = url.split('.')[0]
        var el = jQuery('#fig_article_'+id)
        el.unmask()
        el.css({
          'background-color': '#F5F5F5',
          'min-height': '300px',
          'background-image': 'url(http://dev.figshare.com/static/img/widget_error_bg.jpg)'
        })
        el.html("<div class='fig_error'><h6>WHOOPS! SOMETHING WHENT WRONG</h6><a href='javascript: void(0)' class='fig_refresh_now'>Refresh now</a></div>")
        jQuery('.fig_error', el).css('margin-top', parseInt(el.height()/2-70))
      }
      return function(o) {
        var url = o.url;
        o.dataType = 'text'
        if ( /get/i.test(o.type) && !/json/i.test(o.dataType) && isExternal(url) ) {
          o.url = o.url + "?callback=?";
          o.dataType = 'json';
          var new_odata = o.data
          o.data = {
            q:  get_id_from_url(url),
            start: (jQuery.browser.msie) ? 1 : 0,
            format: 'json'
          };
          // Since it's a JSONP request
          // complete === success
          if (!o.success && o.complete) {
            o.success = o.complete;
            delete o.complete;
          }
          o.success = (function(_success){
            return function(data) {
              if (_success) {
                // Fake XHR callback.
                try {
                  _success.call(this, {
                    responseText: data
                    // Get rid of them
                  }, 'success');
                } catch(e) {
                  request_error(url)
                }
              }
            };
          })(o.success);
        }
        return _ajax.apply(this, arguments);
      };
    })(jQuery.ajax);
// loadmask
    (function(a){a.fn.mask=function(b,c){a(this).each(function(){if(c!==undefined&&c>0){var d=a(this);d.data("_mask_timeout",setTimeout(function(){a.maskElement(d,b)},c))}else{a.maskElement(a(this),b)}})};a.fn.unmask=function(){a(this).each(function(){a.unmaskElement(a(this))})};a.fn.isMasked=function(){return this.hasClass("masked")};a.maskElement=function(b,c){if(b.data("_mask_timeout")!==undefined){clearTimeout(b.data("_mask_timeout"));b.removeData("_mask_timeout")}if(b.isMasked()){a.unmaskElement(b)}if(b.css("position")=="static"){b.addClass("masked-relative")}b.addClass("masked");var d=a('<div class="loadmask"></div>');if(navigator.userAgent.toLowerCase().indexOf("msie")>-1){d.height(b.height()+parseInt(b.css("padding-top"))+parseInt(b.css("padding-bottom")));d.width(b.width()+parseInt(b.css("padding-left"))+parseInt(b.css("padding-right")))}if(navigator.userAgent.toLowerCase().indexOf("msie 6")>-1){b.find("select").addClass("masked-hidden")}b.append(d);if(c!==undefined){var e=a('<div class="loadmask-msg" style="display:none;"></div>');e.append("<div>"+c+"</div>");b.append(e);e.css("top",Math.round(b.height()/2-(e.height()-parseInt(e.css("padding-top"))-parseInt(e.css("padding-bottom")))/2-16)+"px");e.css("left",Math.round(b.width()/2-(e.width()-parseInt(e.css("padding-left"))-parseInt(e.css("padding-right")))/2-26)+"px");e.show()}};a.unmaskElement=function(a){if(a.data("_mask_timeout")!==undefined){clearTimeout(a.data("_mask_timeout"));a.removeData("_mask_timeout")}a.find(".loadmask-msg,.loadmask").remove();a.removeClass("masked");a.removeClass("masked-relative");a.find("select").removeClass("masked-hidden")}})(jQuery)
    jQuery(document).ready(function($) {
      var domain = 'http://wl.figshare.com'
      var overlay_article_id = 0;
      var overlay_file_id = 0;
      var stupid_offset = 0;
      var articles_files = {};
      var articles_shares = {};
      var articles_types = {};
      var articles_files_list = {};
      var articles_current_file = {};
      var articles_files_count = {};
      var overlay = false;
      var win_height_orig = $(window).height() - 320;
      var fileset_prev_elem = 0;
      $('body').append("<a href='javascript: void(0)' id='go_overlay' style='display: none;'></a>")
      $('body').append("<a href='javascript: void(0)' id='go_share' style='display: none;'></a>")
      $('body').append('\
<div id="figshare_enlarge" class="enlarge-view" style="display:none">\
    <div class="fw-overlay-curtain"></div>\
    <a class="fw-close-enlarge" title="close overlay" href="javascript:void(0)">\
      <span>or press Esc</span>\
    </a>\
    <div class="fw-list-wrap black">\
    <ul class="fw-enlarge-list">\
        <li class="active">\
            <a class="fw-el-trigger" href="javascript:void(0)">\
                <div class="fw-el-thumb">\
                    <img src="http://dev.figshare.com/media/96550/142_232630.png" alt="description">\
                </div>\
                <div class="fw-el-details">\
                    <span class="fw-eld-filename">Phospholipuds_p...csv</span>\
                    <span class="fw-eld-filesize">112kb</span>\
                    <div class="fw-eld-icon"></div>\
                </div>\
            </a>\
        </li>\
        <li>\
            <a class="fw-el-trigger" href="javascript:void(0)">\
                <div class="fw-el-thumb">\
                    <img src="http://dev.figshare.com/media/96550/142_232630.png" alt="description">\
                </div>\
                <div class="fw-el-details">\
                    <span class="fw-eld-filename">Phospholipuds_p...csv</span>\
                    <span class="fw-eld-filesize">112kb</span>\
                    <div class="fw-eld-icon"></div>\
                </div>\
            </a>\
        </li>\
    </ul>\
    </div>\
      <div class="fw-enlarge-preview">\
        <div class="fw-preview-wrap">\
        <a href="javascript:void(0)" title="previous file" class="fw-ep-left">\
          <span></span>\
        </a>\
        <a href="javascript:void(0)" title="next file"class="fw-ep-right">\
          <span></span>\
        </a>\
          <div class="fw-ep-embed-wrap">\
            <div class="fw-ep-embed">\
            </div>\
          </div>\
          <div class="fw-ep-details">\
          </div>\
          <div class="fw-ep-caption-wrap">\
          <div class="fw-ep-caption">\
              <a href="javascript:void(0)" class="fp-cap-minimize"><span></span>Hide description</a>\
              <div class="fw-ep-cap-shadow"></div>\
              <div class="fw-epc-content-wrap">\
                <div class="fw-epc-content">\
                      <div class="fw-more-description">\
                        <a class="fw-epc-more" href="javascript:void(0)"><span></span>Show more</a>\
                      </div>\
                    <div class="fw-epc-desc"></div>\
                </div>\
              </div>\
          </div>\
          </div>\
      </div>\
    </div>\
</div>\
');
      /*
       */
      var  show_curret_info = function (file_id) {
        fileset_prev_elem = overlay_file_id
        $('#pv_'+overlay_file_id+' a').css('border', '1px solid black')
      }
      jQuery.fn.selectText=function(){var a=document;var b=this[0];if(a.body.createTextRange){var c=document.body.createTextRange();c.moveToElementText(b);c.select()}else if(window.getSelection){var d=window.getSelection();var c=document.createRange();c.selectNodeContents(b);d.removeAllRanges();d.addRange(c)}}
      var display_article = function (article_id, data) {
        var fig_el =  $('#fig_article_'+article_id)
        var wig_height = fig_el.css('height');
        wig_height = wig_height.substring(0, wig_height.length-2)
        $('#fig_article_'+article_id).unmask();
        articles_files_list[parseInt(data['article_id'])] = data['files_id_list']
        articles_current_file[parseInt(data['article_id'])] = data['files_id_list'][0]
        articles_files[parseInt(data['article_id'])] = data['files'];
        articles_files_count[parseInt(data['article_id'])] = data['article_files_count'];
        $('#fig_article_'+data['article_id']).html(data['widget_html']);
        var t_height = $('.fig_title', fig_el).height()
        var b_height = $('.fig_bottom', fig_el).height()
        if(wig_height<100) {
          wig_height = 0;
        }
        var cont_height = 330
        if(wig_height==0) {
        } else {
          cont_height = wig_height-t_height-b_height;
          var fig_desc = $('.fig_description', fig_el).height()
          cont_height -= fig_desc + 5
        }
        var first_p = $('.first_preview_bar', fig_el)
        if(first_p.length>0) {
          $('.fig_fileset', fig_el).height(cont_height+'px')
          cont_height -= first_p.height()+3
          if(wig_height==0) {
            $('.first_preview_box .viewer_container', fig_el).css('min-height', cont_height+'px')
          } else {
//$('.first_preview_box .viewer_container', fig_el).css('height', cont_height+3+'px')
          }
        } else {
          if(wig_height==0) {
            cont_height -= 50
            $('.viewer_container', fig_el).css('min-height', cont_height+'px')
          } else {
            //$('.viewer_container', fig_el).css('min-height', cont_height+'px')
            //$('.viewer_container', fig_el).height(cont_height+'px')
          }
        }
        articles_types[parseInt(data['article_id'])] = data['article_type'];
        if(data['article_type']==4) {
          setTimeout(function () {
            $('#fig_article_'+data['article_id']+' div.fig_fileset').jScrollPane({'showArrows': true, 'verticalDragMinHeight': 50});
          }, 300);
        } else {
          $('.viewer_container', $('#fig_article_'+article_id)).css('overflow', 'hidden')
        }
        var c = $('.fw-fileset .fw-file', fig_el)
        var new_h = (c.length*41)
        var content_height = $('.fw-content', fig_el).height()
        var file_wrap = $('.fw-fileset-wrap', fig_el)
        if(content_height< new_h) {
          file_wrap.css('height', parseInt(content_height*0.8)+'px')
        } else {
          file_wrap.css('height', new_h+'px')
        }
      }
      $('.figshare_widget').each(function (e, el) {
        //var article_id = $(el).attr('id').substring(12)
        try {
          var doi = $(this).attr('doi').replace('/', '_')
          $(this).addClass('figshare_file_widget')
        } catch(e) {
          //console.log($(this))
        }
        //$(this).mask("Loading...");
        var is_ie = ($.browser.msie) ? 1 : 0;
        $.getJSON(domain+'/widget/plos/'+doi+'.json', function(json) { //get information about the user usejquery from twitter api
          if(typeof json['responseText']!='undefined') {
            var data = json['responseText']
          } else {
            var data = jQuery.parseJSON(json)
          }
          if(typeof data =='string') {
            var data = jQuery.parseJSON(data)
          }
          $('#figshare_enlarge').addClass(data['skin'])
          var article_id = data['article_id']
          $(el).attr('id', 'fig_article_'+article_id)
          display_article(article_id, data)
        });
      });
      var change_enlarge_preview = function(article_id, file_id) {
        var fv_embed = $('#figshare_enlarge .fw-preview-wrap')
        fv_embed.mask('Loading...')
        $.getJSON(domain+'/articles/get_inline_display_html/'+article_id+'/'+file_id+'/1/', function(json) { //get information about the user usejquery from twitter api
          description_default()
          if(typeof json['responseText']!='undefined') {
            var data = json['responseText']
          } else {
            var data = jQuery.parseJSON(json)
          }
          if(typeof data =='string') {
            var data = jQuery.parseJSON(data)
          }
          var par = $('#figshare_enlarge')
          fv_embed.unmask()
          $('.fw-ep-embed', par).html(data['html'])
          var href = data['download']
          var el_def = '<span class="fw-epd-filename">'+data['name']+'</span>\
                          <a href="'+href+'" class="fw-epd-download">download <span class="fw-epd-filesize">('+data["size"]+')</span></a>'
          $('.fw-ep-details', par).html(el_def)
          //fw-epc-content
          $('.fw-epc-desc', par).html(data['description'])
          if(data['description']=='') {
            $('.fw-ep-caption-wrap', par).hide()
          } else {
            $('.fw-ep-caption-wrap', par).show()
            if($('.fw-epc-content',par).height()<$('.fw-epc-desc', par).height()) {
              $('.fw-more-description', par).show()
            } else {
              $('.fw-more-description', par).hide()
            }
          }
          $('#f_prev_'+article_id+'_'+en_f_id).parent().removeClass('active')
          $('#f_prev_'+article_id+'_'+file_id).parent().addClass('active')
          en_art_id = article_id
          en_f_id = file_id
          var f_l = articles_files_list[article_id]
          if(f_l[0]==file_id) {
            $('#figshare_enlarge .fw-ep-left').addClass('disabled')
            en_d[0]=1
          } else if(en_d[0]==1) {
            $('#figshare_enlarge .fw-ep-left').removeClass('disabled')
          }
          if(f_l[f_l.length-1]==file_id) {
            $('#figshare_enlarge .fw-ep-right').addClass('disabled')
            en_d[1]=1
          } else if(en_d[1]==1) {
            $('#figshare_enlarge .fw-ep-right').removeClass('disabled')
          }
        });
      }
      var w_h = $(window).height()
      var w_w = $(window).width()
      var fe_el = $('#figshare_enlarge')
      var fembed_el = $('.fw-ep-embed', fe_el).css({'height': (w_h-126-110)+"px"})
      // 38 88
      var l_w = parseInt(w_w * 0.20)
      if (l_w>280) {
        l_w = 280
      } else if (l_w<140) {
        l_w = 140
      }
      $('#figshare_enlarge .fw-list-wrap').css({'width': l_w+'px'})
      var n_w = (w_w-l_w-140)
      if(n_w>1140) {
        n_w = 1140
      }
      $('.fw-enlarge-preview', fe_el).css({
        'height': w_h-100+'px', 'margin-top': '-'+((w_h-100)/2)+'px',
        'width': n_w+'px', 'margin-left': "-"+(n_w-l_w)/2+'px'
      })
      $('.fw-preview-wrap', fe_el).css({'width': '100%'})
      //alert(w_w+"-"+l_w+'-'+n_w+"-"+(n_w-270)/2)
      //console.log('wh:'+w_h+"ww:"+w_w)
      var change_inline_preview = function(article_id, file_id) {
        var fv_embed = $('#fig_article_'+article_id+' .fw-embed')
        fv_embed.mask('Loading...')
        $.getJSON(domain+'/articles/get_inline_display_html/'+article_id+'/'+file_id+'/', function(json) { //get information about the user usejquery from twitter api
          if(typeof json['responseText']!='undefined') {
            var data = json['responseText']
          } else {
            var data = jQuery.parseJSON(json)
          }
          if(typeof data =='string') {
            var data = jQuery.parseJSON(data)
          }
          fv_embed.html(data['html'])
          $('#fig_article_'+article_id+' .fw-download-this span').html("("+ data['size'] +")")
          if(data['description']=='' || data['description']=='None') {
            $('#fig_article_'+article_id+' .fw-caption').hide()
          } else {
            $('#fig_article_'+article_id+' .fw-caption').show()
          }
          $('#fig_article_'+article_id+' .fw-cap-content').html(data['description'])
          $('#fig_article_'+article_id+' .fw-title-bar').html(data['name'])
          $('#fig_article_'+article_id+' .fw-download-this a').attr('href', data['download'])
        });
      }
      var en_art_id = 0;
      var en_f_id = 0;
      var en_d = [0, 0]
      // Left right arrows click
      $(document).keydown(function(v){
        if(v.keyCode== 37 || v.keyCode==38) {
          $('#figshare_enlarge .fw-ep-left').trigger('click')
        } else if(v.keyCode==39 || v.keyCode==40) {
          $('#figshare_enlarge .fw-ep-right').trigger('click')
        }
      });
      $(document).off('click', '#figshare_enlarge a.fw-ep-right').on('click', '#figshare_enlarge a.fw-ep-right', undefined, function() {
//        $('#figshare_enlarge a.fw-ep-right').live('click', function () {
        if($(this).hasClass('disabled')) return false;
        var f_l = articles_files_list[en_art_id]
        var n = $.inArray(en_f_id, f_l)
        change_enlarge_preview(en_art_id, f_l[n+1])
      })
      $(document).off('click', '#figshare_enlarge a.fw-ep-left').on('click', '#figshare_enlarge a.fw-ep-left', undefined, function() {
//        $('#figshare_enlarge a.fw-ep-left').live('click', function () {
        if($(this).hasClass('disabled')) return false;
        var f_l = articles_files_list[en_art_id]
        var n = $.inArray(en_f_id, f_l)
        change_enlarge_preview(en_art_id, f_l[n-1])
      })
      $(document).off('click', '#figshare_enlarge a.fw-el-trigger').on('click', '#figshare_enlarge a.fw-el-trigger', undefined, function() {
//        $('#figshare_enlarge a.fw-el-trigger').live('click', function () {
        var id = $(this).attr('id').split('_')
        change_enlarge_preview(parseInt(id[2]), parseInt(id[3]))
        return false;
      });
      var disable_n_p = function (par, pos, article_id) {
        if(pos==1) {
          $('.fw-prev', par).addClass('disabled')
        } else {
          $('.fw-prev', par).removeClass('disabled')
        }
        if(articles_files_list[article_id].length==pos) {
          $('.fw-next', par).addClass('disabled')
        } else {
          $('.fw-next', par).removeClass('disabled')
        }
      }
      $('.figshare_widget').off('click', '.fw-prev').on('click', '.fw-prev', undefined, function (e) {
//        $('.fw-prev').live('click', function (e) {
        var par = $(this).parent().parent().parent().parent()
        var c_el = $('.fwc-current', par)
        var a_id = parseInt($('.curent_article_id', par).val())
        if(parseInt(c_el.html())==1) {
          return false;
        }
        var c_pos = parseInt(c_el.html())-1
        c_el.html(c_pos)
        disable_n_p(par, c_pos, a_id)
        change_inline_preview(a_id, articles_files_list[a_id][c_pos-1])
        $('.curent_file_id', par).val(articles_files_list[a_id][c_pos-1])
      });
      $('.figshare_widget').off('click', '.fw-next').on('click', '.fw-next', undefined, function (e) {
//        $('.fw-next').live('click', function (e) {
        var par = $(this).parent().parent().parent().parent()
        var a_id = parseInt($('.curent_article_id', par).val())
        var c_el = $('.fwc-current', par)
        if(parseInt(c_el.html())>=articles_files_list[a_id].length) {
          return false;
        }
        var c_pos = parseInt(c_el.html())+1
        c_el.html(c_pos)
        disable_n_p(par, c_pos, a_id)
        change_inline_preview(a_id, articles_files_list[a_id][c_pos-1])
        $('.curent_file_id', par).val(articles_files_list[a_id][c_pos-1])
      });
      $('.figshare_widget').off('click', 'a.show_all_files').on('click', 'a.show_all_files', undefined, function (e) {
//        $('a.show_all_files').live('click', function () {
        var par = $(this).parent().parent().parent().parent().parent()
        $('.first_preview', par).hide()
        $('.fig_fileset', par).show()
        var h = $('.fig_fileset', par).height()
        var h_p = $('.jspPane', par).height()
        if(h_p<h) {
          $('.viewer_container', par).css('height', h_p+'px')
        }
        var api = $('.fig_fileset', par).data('jsp');
        try {
          api.reinitialise()
        } catch(e) {
        }
        return false;
      });
      $(document).off('click', 'a.prev_lnk').on('click', 'a.prev_lnk', undefined, function (e) {
//        $('a.prev_lnk').live('click', function (e) {
        var str = $(this).attr('id').split('_');
        var a_id = parseInt(str[2])
        var f_id = parseInt(str[1]);
        $('#figshare_enlarge .fw-enlarge-list').html(articles_files[a_id])
        if(articles_files_list[a_id].length==1) {
          $('#figshare_enlarge .fw-list-wrap').hide()
          $('#figshare_enlarge .fw-ep-left').hide()
          $('#figshare_enlarge .fw-ep-right').hide()
          $('#figshare_enlarge .fw-enlarge-preview').css({'width': "90%", 'margin-left': '-45%'})
        } else {
          $('#figshare_enlarge .fw-list-wrap').show()
          $('#figshare_enlarge .fw-ep-left').show()
          $('#figshare_enlarge .fw-ep-right').show()
          $('#figshare_enlarge .fw-enlarge-preview').css({
            'height': 'auto', 'margin-top': '-'+((w_h-100)/2)+'px',
            'width': n_w+'px', 'margin-left': "-"+(n_w-l_w)/2+'px'
          })
          /*
           */
        }
        if(l_w< 210) {
          $('#figshare_enlarge .fw-el-thumb').hide()
        }
        change_enlarge_preview(a_id, f_id)
        //alert('loeded')
        if ($.browser.msie  && parseInt($.browser.version, 10) === 7){
          $('html').css('overflow', 'hidden')
        }else{
          $('body').css('overflow', 'hidden')
          $('#figshare_enlarge').css('width', $(document).width())
        }
        $('#figshare_enlarge').show();
        $('.fw-list-wrap').jScrollPane({'showArrows': true, 'verticalDragMinHeight': 50});
      })
      $(document).off('click', 'a.fig_refresh_now').on('click', 'a.fig_refresh_now', undefined, function (e) {
//        $('a.fig_refresh_now').live('click', function (e) {
        var article_id = $(this).parent().parent().attr('id').split('_')[2]
        $('#fig_article_'+article_id+' .fig_error').mask("Loading...");
        var is_ie = ($.browser.msie) ? 1 : 0;
        $.getJSON(domain+'/widget/plos/'+article_id+'.json', function(json) { //get information about the user usejquery from twitter api
          if(typeof json['responseText']!='undefined') {
            var data = json['responseText']
          } else {
            var data = jQuery.parseJSON(json)
          }
          if(typeof data =='string') {
            var data = jQuery.parseJSON(data)
          }
          display_article(article_id, data)
        });
      })
      $(document).off('click', 'a.thumb_prev').on('click', 'a.thumb_prev', undefined, function (e) {
//        $('a.thumb_prev').live('click', function (e) {
        var str = $(this).parent().attr('id').substring(3);
        $('#pv_'+overlay_file_id+' a').css('border', '1px solid #ccc')
        overlay_file_id = parseInt(str);
        $('#display_preview').mask('Loading...')
        var is_ie = ($.browser.msie) ? 1 : 0;
        $.getJSON(domain+'/articles/plos_get_display_html/'+overlay_file_id+'/', function(json) {
          if(typeof json['responseText']!='undefined') {
            var data = json['responseText']
          } else {
            var data = jQuery.parseJSON(json)
          }
          if(typeof data =='string') {
            var data = jQuery.parseJSON(data)
          }
          $('#display_preview').html(data['html'])
          $('#d_lnk').html(data['link'])
          $('#d_lnk').css('visibility', 'visible')
          $('#display_preview .viewer_container_pop').css('height', win_height_orig+'px')
          $('.viewer_container_pop').show()
          $('#display_preview').css('height', win_height_orig+'px')
          if($('#display_preview .viewer_container_pop').hasClass('txt_viewer')) {
            $('#display_preview .viewer_container_pop').css('height', win_height_orig+'px')
          } else {
            $('#display_preview .viewer_container_pop').css('height', win_height_orig+'px')
          }
          show_curret_info(overlay_file_id)
        });
      });
      //CODRIN!!!!!!!!!
      var figshare_inside_menu=false;
      $('.figshare_widget').off('click', 'a.fw-download-trigger').on('click', 'a.fw-download-trigger', undefined, function (e) {
//        $('a.fw-download-trigger').live('click', function (e) {
        if(!$(this).parent().hasClass('active'))
          $(this).parent().addClass('active')
        else
          $(this).parent().removeClass('active')
        return false;
      });
      $('.fw-cta-download').mouseover(function(){
        figshare_inside_menu=true;
      });
      $('.fw-cta-download').mouseout(function(){
        figshare_inside_menu=false;
      });
      $(document).click(function(){
        if(!figshare_inside_menu){
          $('.fw-cta-download').removeClass('active')
        }
      });
      $('.figshare_widget').off('click', '.fw-list').on('click', '.fw-list', undefined, function (e) {
//        $('.fw-list').live('click',function(){
        var par_id = $(this).attr('id').split('_')[1]
        var par = $('#fig_article_'+par_id)
        $(this).toggleClass('active')
        if($('.fw-fileset-overlay', par).css('display')!="block"){
          $('.fw-fileset-overlay', par).fadeIn(440)
          $('.fw-fileset').jScrollPane({'showArrows': true, 'verticalDragMinHeight': 50});
        }else{
          $('.fw-fileset-overlay', par).fadeOut(440)
        }
      });
      $(document).off('click', '.fw-curtain').on('click', '.fw-curtain', undefined, function (e) {
//        $('.fw-curtain').live('click',function(){
        $('.fw-list').toggleClass('active')
        $('.fw-fileset-overlay').fadeOut(440)
      });
      $(document).off('click', '.fw-cta-enlarge').on('click', '.fw-cta-enlarge', undefined, function (e) {
//        $('.fw-cta-enlarge').live('click',function(){
        var article_id = parseInt($('.curent_article_id', $(this).parent()).val())
        var c_file = parseInt($('.curent_file_id', $(this).parent()).val())
        //alert(articles_files_list[article_id])
        $('#figshare_enlarge .fw-enlarge-list').html(articles_files[article_id])
        if(articles_files_list[article_id].length==1) {
          $('#figshare_enlarge .fw-list-wrap').hide()
          $('#figshare_enlarge .fw-ep-left').hide()
          $('#figshare_enlarge .fw-ep-right').hide()
          $('#figshare_enlarge .fw-enlarge-preview').css({'width': "90%", 'margin-left': '-45%'})
        } else {
          $('#figshare_enlarge .fw-list-wrap').show()
          $('#figshare_enlarge .fw-ep-left').show()
          $('#figshare_enlarge .fw-ep-right').show()
          $('#figshare_enlarge .fw-enlarge-preview').css({
            'height': 'auto', 'margin-top': '-'+((w_h-100)/2)+'px',
            'width': n_w+'px', 'margin-left': "-"+(n_w-l_w)/2+'px'
          })
          /*
           */
        }
        if(l_w< 210) {
          $('#figshare_enlarge .fw-el-thumb').hide()
        }
        change_enlarge_preview(article_id, c_file)
        //alert('loeded')
        if ($.browser.msie  && parseInt($.browser.version, 10) === 7){
          $('html').css('overflow', 'hidden')
        }else{
          $('body').css('overflow', 'hidden')
          $('#figshare_enlarge').css('width', $(document).width())
        }
        $('#figshare_enlarge').show();
        $('#figshare_enlarge .fw-list-wrap').jScrollPane({'showArrows': true, 'verticalDragMinHeight': 50});
      });
      $(document).off('click', '.fw-close-enlarge').on('click', '.fw-close-enlarge', undefined, function (e) {
//        $('.fw-close-enlarge').live('click',function(){
        if ($.browser.msie  && parseInt($.browser.version, 10) === 7){
          $('html').css('overflow', 'auto')
        }else{
          $('body').css('overflow', 'auto')
          $('#figshare_enlarge').css('width',$(document).width())
        }
        $('#figshare_enlarge').hide();
      })
      //can be deleted just emulating stuff
      var description_default = function () {
        var par = $('#figshare_enlarge')
        $('.fp-cap-minimize', par).hide()
        $('.fw-ep-caption-wrap', par).css({'height': "auto"})
        $('.fw-ep-caption', par).css({ 'bottom': 'auto', 'position': 'relative', 'min-height': 'auto'})
        $('.fw-epc-content', par).css({'max-height': "59px", 'overflow-y': 'hidden'})
      }
      $(document).off('click', '.fp-cap-minimize').on('click', '.fp-cap-minimize', undefined, function (e) {
//        $('.fp-cap-minimize').live('click',function(){
        $('#figshare_enlarge .fw-more-description').show()
        description_default()
        $('.fw-epc-content').css('overflow-y','hidden')
        return false;
      });
      $(document).off('click', '.fw-epc-more').on('click', '.fw-epc-more', undefined, function (e) {
//        $('.fw-epc-more').live('click',function(){
        var par = $('#figshare_enlarge')
        $('.fw-ep-caption-wrap', par).css({'height': $('#figshare_enlarge .fw-ep-caption-wrap').height()+"px"})
        $('.fw-ep-caption', par).css({ 'bottom': '0', 'left': '0', 'position': 'absolute', 'min-height': '115px'})
        $('.fw-epc-content', par).css({'max-height': $(".fw-enlarge-preview").height()*80/100, "overflow-y":"auto"})
        $('.fw-more-description', par).hide()
        $('.fp-cap-minimize', par).show()
        /*
         $(this).parent().parent().css({
         'position':'absolute',
         'bottom':0
         })
         */
      })
      $(document).off('click', '.fw-overlay-curtain').on('click', '.fw-overlay-curtain', undefined, function (e) {
//        $('.fw-overlay-curtain').live('click',function(){
        $('.fw-close-enlarge').trigger('click')
      })
      $('.figshare_widget').off('click', 'a.aj_download_all').on('click', 'a.aj_download_all', undefined, function (e) {
//        $('.figshare_widget a.aj_download_all').live('click', function () {
        var e = $(this)
        var init_html = e.html()
        var a_id = parseInt(e.attr('id').split('_')[1])
        e.html("Creating archive...")
        $.getJSON('http://figshare.com/download/article_url/'+a_id, function(json) { //get information about the user usejquery from twitter api
          if(typeof json['responseText']!='undefined') {
            var data = json['responseText']
          } else {
            var data = jQuery.parseJSON(json)
          }
          $('<iframe>', {src: "http://figshare.com"+data['download_url']}).css('display', 'none').appendTo('body');
          e.html(init_html)
        });
        return false
      })
      $(document).keydown(function(event){
        //console.log('dsa')
        if ( event.which == 27 ) {
          event.preventDefault();
          $('.fw-close-enlarge').trigger('click')
        }
      })
    });
  }
})(); // We call our anonymous function immediately
