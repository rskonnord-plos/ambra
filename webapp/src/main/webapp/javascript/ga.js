//GA TRACKING v2.0 by www.blastam.com - analytics@blastam.com
// cross-domain/addIgnoreds list should be in here - if it needs to be updated there will be no need to modify beacons

//if we are on domain under UA-338393-1

if (bamGAID == 'UA-338393-1'){
  var bamGAcrossDomains = ["plosone.org", "plosbiology.org","plosmedicine.org","plosgenetics.org","ploscompbiol.org","plospathogens.org","plosntds.org","ploscollections.org","plosreports.org","ploshubs.org","plos.org"]; // cross domain list
  var bamGAIgnoredRefs = ["plosone.org", "plosbiology.org","plosmedicine.org","plosgenetics.org","ploscompbiol.org","plospathogens.org","plosntds.org","ploscollections.org","plosreports.org","ploshubs.org","plos.org"];
} else {
  var bamGAcrossDomains = [];
}

if (typeof(bamGAID) != 'undefined') {
  var _bamGA = [];

// required Functions for basic tracking
  _bamGA.getDomain = function(url) {
    return url.replace(/^https?\:\/\//i,"").split('.').reverse()[1] + '.' + url.split('.').reverse()[0];
  }
  _bamGA.getParameterInURL = function(url,name) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regexS = "[\\?&]" + name + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(url);
    if(results == null)
      return undefined;
    else
      return decodeURIComponent(results[1].replace(/\+/g, " "));
  }

//settings
  _bamGA.accountMain = bamGAID;
  _bamGA.delay = 200;

  _bamGA.hostname = window.location.hostname;
  _bamGA.uri = window.location.pathname;
  _bamGA.thisDomain = _bamGA.getDomain(_bamGA.hostname);
  _bamGA.cookieDomain = '.' + _bamGA.thisDomain;
  _bamGA.thisDomainMatch = new RegExp(_bamGA.thisDomain,'i');

// The gaq
  var _gaq = [];

  _gaq.push(['_setAccount', _bamGA.accountMain],['_setDomainName',_bamGA.cookieDomain],['_setAllowLinker', true],['_setAllowAnchor', true]);

// Add ignored refs - cycle through list if defined 
  if (typeof(bamGAIgnoredRefs) != 'undefined') {
    for (i in bamGAIgnoredRefs) {
      _gaq.push(['_addIgnoredRef', bamGAIgnoredRefs[i]]);
    }
  } else {
    _gaq.push(['_addIgnoredRef', _bamGA.thisDomain]);
  }

// Force virtual pagevew or 404 error page if defined 
  if (typeof(bamGAVPV) != 'undefined') {
    _gaq.push(['_trackPageview', bamGAVPV]);
  }
  else if (typeof(bamGA404) != 'undefined' && bamGA404) {
    _gaq.push(['_trackPageview', '/404' + document.location.pathname + document.location.search]);
    _gaq.push(['_trackEvent','404s',_bamGA.uri,'referrer: ' + document.referrer,undefined,true]);
  } else
  if (typeof(bamGAnoTrack) == 'undefined') _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

// track cross domain, outbound, email, file download
  _bamGA.extendTracking = function() {
    try{
      var filetypes = /\.(zip|exe|dmg|pdf|doc*|xls*|ppt*|mp3|slxb|pps*|vsd|vxd|txt|rar|wma|mov|avi|wmv|flv|wav)$/i;
      var baseHref = '';

      if (jQuery('base').attr('href') != undefined) {
        baseHref = jQuery('base').attr('href');
      }

      jQuery('a').each(function(e) {
        var el = jQuery(this);
        var href = (typeof(el.attr('href')) != 'undefined' ) ? el.attr('href'):"";
        var curOnClick = '' + el.attr('onclick');

        var linkServer = (href.match(/^https?\:/i)) ? href.match(/^(([a-z]+:)?(\/\/)?[^\/]+).*$/)[1]:"";
        var linkDomain = (linkServer != "") ? _bamGA.getDomain(linkServer): "";
        var inDomains = (jQuery.inArray(linkDomain, bamGAcrossDomains) >= 0) ? true:false;
        var isThisDomain = (_bamGA.thisDomain == linkDomain) ? true:false;

        var elEv = [];
        elEv.delay = (el.attr('target') == undefined || el.attr('target').toLowerCase() != '_blank') ? true:false;

        if (!href.match(/^javascript:/i) && !curOnClick.match(/_bamGA.track.*Redirect/)) {

          var trace = true;
          if (linkDomain !="" && inDomains && !isThisDomain) {
            _gaq.push(function() {
              var t = _gat._getTrackerByName();
              el.attr('href', t._getLinkerUrl(el.attr('href')));
            });
          }

//outbound - non-interaction event
          if (href.match(/^https?\:/i) && !isThisDomain && !inDomains) {
            elEv.category = "external";
            elEv.action = "click";
            elEv.label = href.replace(/^https?\:\/\//i, '');
            elEv.value = undefined;
            elEv.non_i = true;
            elEv.loc = href;
          }
//redirect script tracking
          else if (typeof(bamGAreDirect)!='undefined' && href.match(bamGAreDirect)) {
            elEv.category = "external";
            elEv.action = "redirect";
            elEv.label = _bamGA.getParameterInURL(href,bamGAreDirectPar).replace(/^https?\:\/\//i, '');
            elEv.value = undefined;
            elEv.non_i = true;
            elEv.loc = href;
          }
//mailto
          else if (href.match(/^mailto\:/i)) {
            elEv.category = "email";
            elEv.action = "click";
            elEv.label = href.replace(/^mailto\:/i, '');
            elEv.value = undefined;
            elEv.non_i = false;
          }
//file downloads
          else if (href.match(filetypes)) {
            var extension = (/[.]/.exec(href)) ? /[^.]+$/.exec(href) : undefined;
            elEv.category = "download";
            elEv.action = "click-" + extension;
            elEv.label = href;
            elEv.value = undefined;
            elEv.non_i = false;
            elEv.loc = baseHref + href;
          }
//track link clicks on any page that has bamGATrackPageClicks set to true
          else if (typeof(bamGATrackPageClicks)!='undefined' && bamGATrackPageClicks == true) {
            elEv.category = "click-tracking";
            elEv.action = document.location.hostname + document.location.pathname;
            elEv.label = href;
            elEv.value = undefined;
            elEv.non_i = true;
            elEv.loc = baseHref + href;
          }
          else trace = false;
// perform _trackEvent
          el.click( function() {

            if (trace) {
              _gaq.push(['_trackEvent', elEv.category, elEv.action, elEv.label, elEv.value, elEv.non_i]);
              if (elEv.loc && elEv.delay) {
                setTimeout(function() { location.href = elEv.loc; }, _bamGA.delay);
                return false;
              }
            }
          });
        } // end if javascript or Redirect
      }); // end a.each
    }  // end try
    catch(e){
    }
  }; // end extend.tracking function

// Custom function
  _bamGA.customTracking= function() {
    try {
      // begin custom code
      jQuery('#lightBox').each(function(e) {
        var lb = jQuery(this);

        lb.click(function() {
          _gaq.push(["_trackEvent", "click-tracking", "View All Figures"]);
        });
      });
      // end custom code
    }
    catch(e){}
  };
} // end if !bamGAID

if (typeof jQuery != 'undefined') {
  jQuery(document).ready(function() {
    _bamGA.extendTracking();
    _bamGA.customTracking();
  });
}

//END GA TRACKING