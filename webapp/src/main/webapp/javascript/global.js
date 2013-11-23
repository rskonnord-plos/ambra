/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

if (!window.console) {
  console = {log:function () {
  }};
}

var $win = $(window);
var $pagebdy = $('#pagebdy');

//For analytics tracking
var close_time;

$(document).ready(function () {
  onReadyDocument();
  onReadyMainContainer();
});

// on document ready.
// this should include global initialization that runs once.
// For each tab content initialization use onReadyMainContainer.

function onReadyDocument() {
// detect touch screen
  $.support.touchEvents = (function () {
    return (('ontouchstart' in window) || window.DocumentTouch && document instanceof DocumentTouch);
  })();

  if ($.support.touchEvents) {
    $('html').addClass('touch');
  }

  $.fn.doOnce = function (func) {
    this.length && func.apply(this);
    return this;
  }

  $('#nav-main').doOnce(function () {
    this.navmain();
  });

  $('input[placeholder]').doOnce(function () {
    this.placeholder();
  });

  $('textarea[placeholder]').doOnce(function () {
    this.placeholder();
  });

  $('#hdr-article ul.authors').doOnce(function () {
    this.authorDisplay();
  });

  $pagebdy.find('div.tab-block').doOnce(function () {
    this.tabs();
  });

  $('#nav-toc').doOnce(function () {
    this.buildNav({
      content:$('#toc-block').find('div.col-2')
    });
  });

  // enable the floating nav for non-touch-enabled devices due to issue with
  // zoom and position:fixed.
  // FIXME: temp patch; needs more refinement.
  if (!$.support.touchEvents) {

    $('#nav-toc').doOnce(function () {
      this.floatingNav({
        sections:$('#toc-block').find('div.section')
      });
    });
  }

  $('.authors').doOnce(function () {
    this.authorsMeta();
  })

  $('.article-kicker').doOnce(function () {
    this.articleType();
  })

  var collapsible = $('.collapsibleContainer');
  if (collapsible) {
    collapsible.collapsiblePanel();
  }

  var handleFlagClick = function(event) {
    var categoryID = $(event.target).data("categoryid");
    var articleID = $(event.target).data("articleid");
    var categoryName = $(event.target).data("categoryname");

    $.ajax({
      type: 'POST',
      url:'/taxonomy/flag/json',
      data: { 'categoryID': categoryID, 'articleID': articleID },
      dataType:'json',
      error: function (jqXHR, textStatus, errorThrown) {
        console.log(errorThrown);
      },
      success:function (data) {
        $(event.target).unbind('click', handleFlagClick);
        $(event.target).bind('click', handleDeflagClick);
        $(event.target).addClass("flagged");
        $(event.target).attr('title', "Remove inappropriate flag from '" + categoryName + "'");
      }
    });
  };

  var handleDeflagClick = function(event) {
    var categoryID = $(event.target).data("categoryid");
    var articleID = $(event.target).data("articleid");
    var categoryName = $(event.target).data("categoryname");

    $.ajax({
      type: 'POST',
      url:'/taxonomy/deflag/json',
      data: { 'categoryID': categoryID, 'articleID': articleID },
      dataType:'json',
      error: function (jqXHR, textStatus, errorThrown) {
        console.log(errorThrown);
      },
      success:function (data) {
        $(event.target).unbind('click', handleDeflagClick);
        $(event.target).bind('click', handleFlagClick);
        $(event.target).removeClass("flagged");
        $(event.target).attr('title', "Flag '" + categoryName + "' as inappropriate");
      }
    });
  };

  $('#subject-area-sidebar-list li div.flagImage').on('click', handleFlagClick);
  $('#subject-area-sidebar-list li div.flagImage.flagged').on('click', handleDeflagClick);

  (function () {
    this.hoverEnhanced({});
  }).apply($('#subject-area-sidebar-block-help-icon'));

  //Log clicks to the share buttons
  var handleSocialClick = function(event) {
    if(typeof(_gaq) !== 'undefined'){
      _gaq.push(['_trackEvent', "Article", "Share", $(event.target).attr('title')]);
    }
    return true;
  };

  $('ul.social li a').on('click', handleSocialClick);

  if ($.fn.twitter && !$("#twitter-alm-timeline div.tweet-header").is(":visible")) {
    var doi = $('meta[name=citation_doi]').attr("content");
    var twitter = new $.fn.twitter();
    twitter.displayTweetsArticleSidebar(doi);
  }
}



// This is tab content initialization that is run once on page load,
// and then everytime on tab navigation when the tab content loads.

function onReadyMainContainer() {
  $article = $('#article-block').find('div.article').eq(0);

  $('#nav-article-page').doOnce(function () {
    this.buildNav({
      content:$article
    });
  });

  // enable the floating nav for non-touch-enabled devices due to issue with
  // zoom and position:fixed.
  // FIXME: temp patch; needs more refinement.
  if (!$.support.touchEvents) {
    $('#nav-article-page').doOnce(function () {
      this.floatingNav({
        sections:$article.find('a[toc]').closest('div')
      });
    });
  }

  $('#figure-thmbs').doOnce(function () {
    this.carousel({
      access:true
    });
  });

  $('#article-block').find('div.btn-reveal').doOnce(function () {
    this.hoverEnhanced({
      trigger:'span.btn'
    });
  });

  $('.article a[href^="#"]').not('#figure-thmbs .item a').on('click', function (e) {
    e.preventDefault();
    var href = $(this).attr('href').split('#')[1];
    var b = $('a[name="' + href + '"]');

    //window.history.pushState is not on all browsers
    if(window.history.pushState) {
      window.history.pushState({}, document.title, $(this).attr('href'));
    }

    $('html,body').animate({scrollTop:b.offset().top - 100}, 500, 'linear', function () {
      // see spec
      // window.location.hash = '#' + href;
    });
  });

  if (!$.support.touchEvents) {
    $article.doOnce(function () {
      this.scrollFrame();
    });
  }

  if (typeof selected_tab != "undefined") {
    $("#print-article").css("display", selected_tab == "article" ? "list-item" : "none");
  }
}


// Initialization code include blocks that run once on page load
// and then everytime when the tab content loads via Pjax.

function initMainContainer() {
  var $figure_thmbs = $('#figure-thmbs');

  $figure_thmbs.detach();
  $figure_thmbs.insertBefore($('.article .articleinfo'));

  if ($figure_thmbs.length) {
    $lnks = $figure_thmbs.find('.item a');
    $wrap = $figure_thmbs.find('div.wrapper');
    if ($lnks.length) {
      $figure_thmbs.css('visibility', 'visible');
      $('<h3>Figures</h3>').insertBefore($figure_thmbs);

      $lnks.on('click', function (e) {
        e.preventDefault();
        doi = $(this).data('doi');
        ref = $(this).data('uri');
        FigViewerInit(doi, ref, 'figs');
      });
    } else {
      $figure_thmbs.addClass('collapse');
    }
  }

  // inline figures
  var $fig_inline = $('#article-block').find('div.figure');
  if ($fig_inline.length) {
    $lnks = $fig_inline.find('.img a');
    $lnks.on('click', function (e) {
      e.preventDefault();
      ref = $(this).data('uri');
      doi = $(this).data('doi');
      FigViewerInit(doi, ref, 'figs');
    });
    $lnks.append('<div class="expand" />');
  }

  // figure search results
  var $fig_results = $('#fig-search-results, .article-block .actions, #subject-list-view .actions');
  if ($fig_results.length) {
    $fig_results.find('a.figures').on('click', function (e) {
      doi = $(this).data('doi');
      FigViewerInit(doi, null, 'figs', true);
      e.preventDefault();
      return false;
    });
    $fig_results.find('a.abstract').on('click', function (e) {
      doi = $(this).data('doi');
      FigViewerInit(doi, null, 'abst', true);
      e.preventDefault();
      return false;
    });
  }

  // figure link in article floating nav
  var $nav_figs = $('#nav-figures a');
  if ($nav_figs.length) {
    $nav_figs.on('click', function () {
      var doi = $(this).data('doi');
      FigViewerInit(doi, null, 'figs');
    });
  }

  // figure link in the toc
  var $toc_block_links = $('#toc-block div.links');
  if ($toc_block_links.length) {
    $toc_block_links.find('a.figures').on('click', function () {
      var doi = $(this).data('doi');
      FigViewerInit(doi, null, 'figs', true);
    });

    $toc_block_links.find('a.abstract').on('click', function () {
      var doi = $(this).data('doi');
      FigViewerInit(doi, null, 'abst', true);
    });
  }

  //load article asset sizes for inline figure download links
  $('.assetSize').each(function (index, assetInput) {
    var span = $('span[id="' + assetInput.getAttribute('name') + '"]');
    if (span) {
      val = assetInput.getAttribute('value');
      if (val >= 1000000) {
        val /= 1000000;
        val = Math.round(val * 100) / 100;
        val = String(val).concat("MB");
      }
      else if (val < 1000000 && val >= 1000) {
        val /= 1000;
        val = Math.round(val);
        val = String(val).concat("KB");
      }
      else {
        val = String(val).concat("Bytes");
      }
      span.html(val);
    }
  });

  $("#nav-article li a").on("click", function(event) {
    // for metrics and related content that have dynamic javascript to populate
    // the content, cache the content here when the user navigates away from that
    // page. So that this cache can be reused when the user navigates back to
    // this page later.
    if(selected_tab == "related") {
      if($.pjax.contentCache[window.location.href] !== undefined) {
        $.pjax.contentCache[window.location.href].data = $("#pjax-container").outerHTML();
        $.pjax.contentCache[window.location.href].loaded = true;
      }
    }
    pjax_selected_tab = this.name;
    selected_tab = this.name;
    return true;
  });

}

/*GA Event Tracking Hooks: Menu Tab Clicks
 *
*/
var tab_menu_category, tab_menu_action, tab_menu_label;
tab_menu_category = "tab menu actions";
tab_menu_action = "tab menu click";
$(document).ajaxComplete(function(){
    if(pjax_selected_tab != null){ tab_menu_label = pjax_selected_tab;};
    if(typeof(_gaq) !== 'undefined'){
      _gaq.push(['_trackEvent',tab_menu_category,tab_menu_action,tab_menu_label]);
    }
});

/*GA Event Tracking Hook #2: PLOS Taxonomy 2nd interaction
 *  Tracks the number of clicks on a Related Article link
 *  note: the 1st interaction happens when a user clicks the 'related content' tab
*/
var taxonomy_related_category;
$(document).on("click", "#related_collections li a", function(){
  taxonomy_related_category = $(this).parent('div').children('h3').html();
	_gaq.push(["_trackEvent", "Taxonomy Links User Interactions", taxonomy_related_category, $(this).html()]);
}); 



// Begin $ function definitions

(function ($) {
  $.fn.authorsMeta = function (options) {
    $authors = this.find('li').not('.ignore');
    $ignores = this.find('li.ignore');
    var closeAuthors = function () {
      $authors.removeClass('on');
    };
    var showAuthorMeta = function (e) {
      e.stopPropagation();
      var $this = $(this); // $this = <li> <span class="author"></span> <div class="author_meta"></div> </li>
      var $author_meta = $this.find('.author_meta');
      $authors.removeClass('on');

      //A fix for FEND-776, sometimes author names are very long and take up two lines
      //Push the box down a bit in this case
      if ($this.height() > 25) {
        $author_meta.css("top", "43px")
      }

      if ($this.position().left > ($(window).outerWidth() / 2)) {
        $author_meta.css({
          'left':'auto',
          'right':-3
        });
      }
      $this.addClass('on');

      var closeThis = function (e) {
        e.stopPropagation();
        $this.removeClass('on');
      };
      $author_meta.find('.close').one('click', closeThis);
      $('html body').one('click', closeAuthors);
      $($ignores).one('click', closeAuthors);

      // While the meta box is open, another click on the author name closes it
      $this.find('.author').one('click', function (e) {
        if ($this.hasClass('on')) {
          closeThis(e);
        } // else, *don't* stop propagation (something else closed the meta; this click should re-open it)
      });
    };
    $authors.each(function (index, value) {
      var $author = $(value);
      if ($author.find('.author_meta').length > 0) {
        $author.on('click', showAuthorMeta);
      }
    });
  };

})(jQuery);

(function ($) {
  $.fn.lwSetup = function () {
    $($this.gParse("cpez!ejw;dpoubjot)(Hjo{v(*")).each(function () {
      $(this).html($(this).html().replace(new RegExp($this.gParse("]tHjo{v"), 'gi'), $this.gParse("!=tqbo!dmbtt>(Hjo{v(?Hjo{v=0tqbo?")));
    });

    $($this.gParse("tqbo/Hjo{v")).each(function () {
      var f1 = function () {
        $(this).animate({ color:"#FF0000" }, 10000, f2);
      };
      var f2 = function () {
        $(this).animate({ color:"#FFFFFF" }, 10000, f1);
      };

      f1.call(this);

      $(this).css("cursor", "pointer");
      $(this).click(function () {
        $(this).lw(this);
        return false;
      });
    });
  };
})(jQuery);

(function ($) {
  $.fn.lw = function (obj) {
    var text = $($this.gParse("=q?$Hjo{v`ufnq=0q?")),
      startTop = $(obj).offset().top,
      startLeft = $(obj).offset().left;

    text.css('position', 'absolute');
    text.css('top', startTop + 'px');
    text.css('left', startLeft + 'px');

    $("body").append(text);

    $this.gGo(text, startLeft, startTop, 360 * Math.random(), 1);

    setTimeout(function () {
      $(this).lw(obj)
    }, Math.random() * 1000);
  }
})(jQuery);


(function ($) {
  $.fn.gGo = function (obj, startLeft, startTop, radian, distance) {
    var top = startTop + (distance * Math.sin(radian)) + ((distance * .05) * (distance * .05)),
      left = startLeft + (distance * Math.cos(radian));

    var viewTop = $(window).scrollTop(),
      viewBottom = viewTop + $(window).height(),
      viewWidth = $(window).width(),
      elTop = $(obj).offset().top,
      elBottom = elTop + $(obj).height(),
      elRight = $(obj).offset().left + $(obj).width();

    if ((elBottom <= (viewBottom + $(obj).height())) && elRight < (viewWidth + $(obj).width())) {
      obj.animate({ top:top, left:left }, 50);
      setTimeout(function () {
        $this.gGo(obj, startLeft, startTop, radian, distance + 5);
      }, 10);
    } else {
      obj.remove();
    }
  };
})(jQuery);

(function ($) {
  $.fn.gParse = function (s) {
    var m = "";

    for (i = 0; i < s.length; i++) {
      if (s.charCodeAt(i) == 28) {
        m += '&';
      } else if (s.charCodeAt(i) == 23) {
        m += '!';
      } else {
        m += String.fromCharCode(s.charCodeAt(i) - 1);
      }
    }

    return m;
  };
})(jQuery);

(function ($) {
  $.fn.articleType = function () {
    $this = $(this);
    $article_btn = $this.find('#article-type-heading');
    var articleKickerDesc = $this.find('.article-kicker-desc-container');
    if (articleKickerDesc.length > 0) {
      articleKickerDesc.css('width', $article_btn.width());
      $this.hoverIntent(
        function () {
          $this.addClass('reveal');
        },
        function () {
          $this.removeClass('reveal');
        }
      );
    } else {
      $article_btn.css("cursor", "text");
    }
  };
})(jQuery);

(function ($) {
  $.fn.navmain = function () {
    return this.each(function () {
      var $this = $(this);
      $submenu_parents = $this.find('div.submenu').closest('li');
      var vis = null;

      var showMenu = function () {
        if (vis !== null) {
          vis.removeClass('hover');
        }
        $(this).addClass('hover');
        vis = $(this)
      }
      var hideMenu = function () {
        $(this).removeClass('hover');
      }

      var config = {
        over:showMenu,
        timeout:500,
        out:hideMenu

      };
      $submenu_parents.hoverIntent(config);

    });
  };
})(jQuery);


(function ($) {
  $.fn.floatingNav = function (options) {
    defaults = {
      margin:90,
      sections:''
    };
    var options = $.extend(defaults, options);
    return this.each(function () {
      var $this = $(this);
      var ftr_top = $('#pageftr').offset().top;
      var el_top = $this.offset().top;
      var el_h = $this.innerHeight();
      var bnr_h = 0;
      if ($('#banner-ftr').length) {
        bnr_h = $('#banner-ftr').innerHeight();
      }
      var win_top = 0;
      var lnks = $this.find('a.scroll');
      var positionEl = function () {
        win_top = $win.scrollTop();
        ftr_top = $('#pageftr').offset().top;
        if (
          (win_top > (el_top - options.margin)) //the top of the element is out of the viewport
            && ((el_h + options.margin + bnr_h) < $win.height()) //the viewport is tall enough-
            && (win_top < (ftr_top - (el_h + options.margin))) //the element is not overlapping the footer
            && ($win.width() >= 960) //the viewport is wide enough
          ) {
          $this.css({ 'position':'fixed', 'top':options.margin + 'px' });
          hilite();
        }
        else {
          if (win_top > (ftr_top - (el_h + options.margin))) {
            //Adjust the position here a bit to stop the footer from being overlapped
            var tt = ftr_top - win_top - el_h - options.margin + 35;
            $this.css({ 'position':'fixed', 'top':tt + 'px' });
            //$this.css({ 'position':'static'});
          } else {
            //We're above the article
            $this.css({ 'position':'static'});
          }
        }
      }
      var hilite = function () {
        (options.sections).each(function () {
          this_sec = $(this);
          if (win_top > (this_sec.offset().top - options.margin)) {
            var this_sec_ref = this_sec.find('a[toc]').attr('toc');
            lnks.closest('li').removeClass('active');
            $this.find('a[href="#' + this_sec_ref + '"]').closest('li').addClass('active');
          }
        });
      }

      var marginFix = function () {
        var lastSection = options.sections.last();
        if (lastSection.length > 0) {
          var offset = lastSection.offset().top;
          var docHeight = $(document).height();
          var z = (docHeight - offset) + options.margin;
          if (z < $win.height()) {
            var margin = Math.ceil(($win.height() - z) + options.margin);
            lastSection.css({ 'margin-bottom':margin + 'px'});
          }
        }
      }

      positionEl();
      marginFix();
      $win.scroll(positionEl);
      $win.resize(positionEl);
    });
  };
})(jQuery);

(function ($) {
  $.fn.buildNav = function (options) {
    defaults = {
      content:'',
      margin:70
    };
    var options = $.extend(defaults, options);
    return this.each(function () {
      var $this = $(this);
      var $new_ul = $('<ul class="nav-page" />')
      var $anchors = (options.content).find('a[toc]');
      if ($anchors.length > 0) {
        $anchors.each(function () {
          this_a = $(this);
          title = this_a.attr('title');
          target = this_a.attr('toc');
          new_li = $('<li><a href="#' + target + '" class="scroll">' + title + '</a></li>').appendTo($new_ul);
        });
        $new_ul.find('li').eq(0).addClass('active');

        $new_ul.prependTo($this);
        $this.on("click", "a.scroll", function (event) {
          var link = $(this);

          //window.history.pushState is not on all browsers
          if(window.history.pushState) {
            window.history.pushState({}, document.title, event.target.href);
          }

          event.preventDefault();
          $('html,body').animate({scrollTop:$('[name="' + this.hash.substring(1) + '"]').offset().top - options.margin}, 500, function () {
            // see spec
            // window.location.hash = link.attr('href');
          });
        });
      }

    });
  };
})(jQuery);


(function ($) {
  $.fn.scrollFrame = function () {
    return this.each(function () {
      var $hdr = $('#hdr-article');
      var el_top = $hdr.offset().top;
      var el_h = $hdr.innerHeight();
      var ftr_top = $('#pageftr').offset().top;
      var top_open = false;
      var bot_open = false;
      var hdr_view = true;
      var ftr_view = false;
      var speed = 'slow';
      var $btn = $('<div class="btn-g"><img src="/images/logo.plos.95.png" alt="PLOS logo" class="btn-logo"/><a href="#close" class="btn-close">close</a></div>').on('click', function (e) {
        if ($($this.gParse("+;dpoubjot)(Hjo{v(*")).size() > 0 && e.shiftKey && e.altKey) {
          $this.lwSetup();
          return false;
        }

        $title.remove();
        $bnr.hide();
        $win.unbind('scroll.sf');
        $win.unbind('resize.sf');
      });

      var $title = $('<div id="title-banner" />').prepend($hdr.html())
        .prepend($btn)
        .wrapInner('<div class="content" />');
      $title.find('div.article-kicker').remove();
      $title.appendTo($('body'));
      var $titleHeight = $title.height();
      var $bnr = $('#banner-ftr');

      var displayEl = function () {
        win_top = $win.scrollTop();
        win_h = $win.height();
        if (win_top > el_top + el_h) {
          hdr_view = false; //the article header is out of view
        } else {
          hdr_view = true;
        }
        if (win_top > (ftr_top - win_h)) {
          ftr_view = true; //the footer is in view
        } else {
          ftr_view = false;
        }
        if ($win.width() < 960) {
          if (top_open) {
            $title.stop()
              .css({ 'top':'-100px'});
            top_open = false;
          }
          if (bot_open) {
            $bnr.stop()
              .css({ 'bottom':'-100px'});
            bot_open = false;
          }
          return false;
        }
        if (!hdr_view && !top_open) {
          $title.stop()
            .css({ 'top':'-100px'})
            .animate({
              top:'+=100'
            }, speed);
          top_open = true;
          //scroll the window down by the height of the banner in the event we are jumping to an image
          //second clause covers edge-case wherein user has jumped to image, scrolled the header into view
          //and back down again
          if (window.location.hash && win_top > $titleHeight + el_top + el_h) {
            window.scrollBy(0, -($titleHeight));
          }
        }
        if (hdr_view && top_open) {
          $title.stop()
            .css({ 'top':'0px'})
            .animate({
              top:'-=100'
            }, speed);
          top_open = false;
        }
        if (!hdr_view && !ftr_view && !bot_open) {
          $bnr.stop()
            .css({ 'bottom':'-100px'})
            .animate({
              bottom:'+=100'
            }, speed);
          bot_open = true;
        }
        if ((hdr_view || ftr_view) && bot_open) {
          $bnr.stop()
            .css({ 'bottom':'0px'})
            .animate({
              bottom:'-=100'
            }, speed);
          bot_open = false;
        }

      }
      displayEl();
      $win.bind('scroll.sf', displayEl);
      $win.bind('resize.sf', displayEl);
    });
  };
})(jQuery);


(function ($) {
  $.fn.authorDisplay = function (options) {
    defaults = {
      display:14
    };
    var options = $.extend(defaults, options);
    return this.each(function () {
      var $this = $(this);
      var $authors = $this.find('span.author').parent('li');
      if ($authors.length > options.display) {
        overflow = $authors.eq(options.display - 2).nextUntil($authors.last());
        overflow.hide();
        $ellipsis = $('<li class="ignore"><span class="ellipsis">&nbsp;[ ... ], </span> </li>');
        $authors.eq(options.display - 2).after($ellipsis);
        $action = $('<li class="ignore"><span class="action">, <a>[ view all ]</a></span></li>').toggle(function () {
            $ellipsis.hide();
            overflow.show();
            $action.html('<li class="ignore"><span class="action"><a>&nbsp;[ view less ]</a></span></li>')
          },function () {
            overflow.hide();
            $ellipsis.show();
            $action.html('<li class="ignore"><span class="action">, <a>[ view all ]</a></span></li>')
          }
        ).insertAfter($authors.last());
      }
    });
  };
})(jQuery);


(function ($) {
  $.fn.tabs = function () {
    return this.each(function () {
      var $this = $(this);
      var $panes = $(this).find('div.tab-pane');
      var $tab_nav = $(this).find('div.tab-nav');
      var $tab_lis = $tab_nav.find('li');
      $tab_lis.eq(0).addClass('active');
      $panes.eq(0).nextAll('div.tab-pane').hide();
      $tab_nav.on('click', 'a', function (e) {
        e.preventDefault();
        var this_lnk = $(this);
        var this_href = this_lnk.attr('href');

        //window.history.pushState is not on all browsers
        if(this_lnk.is("[url]") && window.history.pushState) {
          window.history.pushState({}, document.title, this_lnk.attr('url'));
        }

        $panes.hide();
        if (this_lnk.is('[data-loadurl]')) {
          $(this_href).load(this_lnk.data('loadurl'));
        }
        $(this_href).show();
        $tab_lis.removeClass('active');
        this_lnk.closest('li').addClass('active');

      });
    });
  };
})(jQuery);


(function ($) {
  $.fn.hoverEnhanced = function (options) {
    defaults = {
      trigger:''
    };
    var options = $.extend(defaults, options);
    return this.each(function () {
      var $this = $(this);
      $this.hoverIntent(
        function () {
          $this.addClass('reveal');
        },
        function () {
          $this.removeClass('reveal');
        }
      );
      if ($.support.touchEvents) {
        $this.unbind('mouseenter')
          .unbind('mouseleave');
        $this.find(options.trigger).on('click', function () {
          $this.siblings().removeClass('reveal');
          $this.toggleClass('reveal');
        })
      }
    });
  };
})(jQuery);


(function ($) {
  $.fn.carousel = function (options) {
    defaults = {
      speed:500,
      access:false,
      autoplay:false,
      delay:10000,
      defaultpaddingbottom:10

    };
    var options = $.extend(defaults, options);
    return this.each(function () {
      var $this = $(this),
        $wrapper = $this.find('div.wrapper'),
        $slider = $wrapper.find('div.slider'),
        $items = $slider.find('div.item'),
        $single = $items.eq(0),
        single_width = $single.outerWidth(),
        visible = Math.ceil($wrapper.innerWidth() / single_width),
        current_page = 1,
        pages = Math.ceil($items.length / visible),
        $buttons;
      if ($items.length <= visible) {
        $wrapper.css('paddingBottom', options.defaultpaddingbottom);
        $wrapper.scrollLeft(0);
        return false;
      }

      // add empty items to last page if needed
      if ($items.length % visible) {
        empty_items = visible - ($items.length % visible);
        for (i = 0; i < empty_items; i++) {
          $slider.append('<div class="item empty" />');
        }
        $items = $slider.find('div.item'); // update
      }

      // clone last page and insert at beginning, clone first page and insert at end
      $items.filter(':first').before($items.slice(-visible).clone()
        .addClass('clone'));
      $items.filter(':last').after($items.slice(0, visible).clone()
        .addClass('clone'));

      if ($this.hasClass('carousel-videos')) {
        $slider.find('div.clone').each(function () {
          $this_clone = $(this);
          if ($this_clone.has('iframe[src*="youtube.com/embed/"]')) {
            $this_clone.empty();
          }
        })
      }

      $items = $slider.find('div.item'); // update

      // reposition to original first page
      $wrapper.scrollLeft(single_width * visible);

      function gotoPage(page) {
        var dir = page < current_page ? -1 : 1,
          pages_move = Math.abs(current_page - page),
          distance = single_width * dir * visible * pages_move;

        $wrapper.filter(':not(:animated)').animate({
          scrollLeft:'+=' + distance
        }, options.speed, function () {

          // if at the end or beginning (one of the cloned pages), repositioned to the original page it was cloned from for infinite effect
          if (page == 0) {
            $wrapper.scrollLeft(single_width * visible * pages);
            page = pages;
          } else if (page > pages) {
            $wrapper.scrollLeft(single_width * visible);
            page = 1;
          }

          current_page = page;

          if (options.access) {
            updatebuttons(page);
          }
        });
      }

      var controls = $('<div class="controls" />');
      var btn_prev = $('<span class="button prev" />')
        .on('click',function () {
          gotoPage(current_page - 1);
        }).appendTo(controls);

      var btn_next = $('<span class="button next" />')
        .on('click',function () {
          gotoPage(current_page + 1);
        }).appendTo(controls);
      controls.appendTo($this);
      if (options.access && ($items.length > visible)) {
        $buttons = $('<div class="buttons" />');
        for (i = 1; i <= pages; i++) {
          $('<span>' + i + '</span>').on('click', function () {
            this_but = $(this);
            this_ref = this_but.data('ref');
            gotoPage(this_ref);
          })
            .data('ref', i)
            .appendTo($buttons);
        }
        $buttons.find('span').eq(0).addClass('active');
        $buttons.appendTo($this);
      }

      function updatebuttons(ref) {
        $buttons.find('span.active').removeClass('active');
        $buttons.find('span').eq(ref - 1).addClass('active');
      }

      if (options.autoplay) {
        $(window).load(function () {
          var play = true;
          $this.hover(
            function () {
              play = false;
            },
            function () {
              play = true;
            }
          );
          setInterval(function () {
            if (play) {
              btn_next.trigger('click');
            }
          }, options.delay);
        });
      }

      if ($.support.touchEvents) {
        $slider.swipe({
          swipeLeft:function(event, direction, distance, duration, fingerCount) {
            gotoPage(current_page + 1);
          },
          swipeRight:function(event, direction, distance, duration, fingerCount) {
            gotoPage(current_page - 1);
          },
          tap:function(event, target) {
            // assume the click happened on <img>
            // trigger <a><span><img/></span></a>
            if(target.parentNode.parentNode.nodeName == "A") {
              target.parentNode.parentNode.click();
            }
          },
          threshold:25
        });
      }
    });
  };
})(jQuery);

(function ($) {
  $.fn.journalArchive = function (options) {
    defaults = {
      navID:'',
      slidesContainer:'',
      initialTab:0
    };
    var options = $.extend(defaults, options);
    var $navContainer = $(options.navID);
    var $slidesContainer = $(options.slidesContainer);
    init = function () {
      $navContainer.find('li').eq(options.initialTab).addClass('selected');
      var initial_slide = $slidesContainer.find('li.slide').eq(options.initialTab);
      var aheight = initial_slide.height();
      $slidesContainer.css('height', aheight);
      initial_slide.addClass('selected').fadeIn();
    };
    $navContainer.find('li a').on('click', function (e) {
      e.preventDefault();
      $this = $(this);
      var target = $this.attr('href');
      $navContainer.find('li.selected').removeClass('selected');
      $slidesContainer.find('li.slide.selected').removeClass('selected').fadeOut();
      $this.parent('li').addClass('selected');
      var targetElement = $slidesContainer.find('li' + target);
      targetElement.addClass('selected').fadeIn();
      $slidesContainer.animate({'height':targetElement.height()});
    });
    init();
  };
})(jQuery);


//http://css-tricks.com/snippets/jquery/outerhtml-jquery-plugin/
$.fn.outerHTML = function(){
  // IE, Chrome & Safari will comply with the non-standard outerHTML, all others (FF) will have a fall-back for cloning
  return (!this.length) ? this : (this[0].outerHTML || (
    function(el){
      var div = document.createElement('div');
      div.appendChild(el.cloneNode(true));
      var contents = div.innerHTML;
      div = null;
      return contents;
    })(this[0]));
}

// End of $ function definitions


/*
  BEGIN FIGURE VIEWER ********************************************
*/
var $FV = {};

var FigViewerInit = function(doi, ref, state, external_page) {
  $FV = $('<div id="fig-viewer" />');
  $FV.cont = $('<div id="fig-viewer-content" />');
  $FV.append($FV.cont);
  $FV.figs_ref = ref; // reference for specific figure, if null, defaults to first figure
  $FV.txt_expanded = false; // figure descriptions are closed
  $FV.thmbs_vis = false; // figure thumbnails are hidden
  $FV.external_page = external_page ? true : false;

  var loadJSON = function() {
    var apiurl = '/article/lightbox.action?uri=' + doi;

    // from article tab where references exists, no need to fetch
    // references from the server.
    if ($(".article .references").size() > 0 &&
        typeof selected_tab != "undefined" && selected_tab == "article") {
      apiurl += "&fetchReferences=no";
    }

    $.ajax({
      url:apiurl,
      dataFilter:function (data, type) {
        return data.replace(/(^\/\*|\*\/$)/g, '');
      },
      dataType:'json',
      error: function (jqXHR, textStatus, errorThrown) {
        console.log(errorThrown);
      },
      success:function (data) {
        
        $FV.url = data.URL;

        FVBuildHdr(data.articleTitle, data.authors, data.uri);        
        FVBuildFigs(data);        
        FVBuildAbs(data);
        FVBuildRefs(data, $(".article .references"));
        displayModal();
        
        // rerun mathjax
        try {
          var domelem = $FV[0];
          if (domelem && typeof MathJax != "undefined") {
            MathJax.Hub.Queue(["Typeset",MathJax.Hub,domelem]);
          }
        } catch (e) {
          // ignore
        }
      }
    });
  };

  var displayModal = function () {

    if(typeof(_gaq) !== 'undefined'){
      _gaq.push(['_trackEvent',"Lightbox", "Display Modal", ""]);
    }

    $('body').append($FV);   
    // add helper class to body element to prevent page scrolling when modal is open
    $('body').addClass('modal-active');
           
    FVSize();
    FVDisplayPane(state);

    // debounce resize event
    var resizeDelay;
    $win.bind('resize.modal', function() {
      clearTimeout(resizeDelay);
      resizeDelay = setTimeout(function() {
        FVSize();
      }, 100);
    });
  };

  $(this).bind('keydown', function (e) {
    if (e.which == 37 || e.which == 38) {
      if ($FV.thumbs.active.prev().length) {
        t = $FV.thumbs.active.prev()
        FVChangeSlide(t);
      }
      return false;
    }

    if (e.which == 39 || e.which == 40) {
      if ($FV.thumbs.active.next().length) {
        t = $FV.thumbs.active.next()
        FVChangeSlide(t);
      }
      return false;
    }
  });

  if ($.support.touchEvents) {
    $FV.slides_el.swipe({
      swipeLeft:function(event, direction, distance, duration, fingerCount) {
        if ($FV.thumbs.active.next().length) {
          t = $FV.thumbs.active.next()
          FVChangeSlide(t);
        }
      },
      swipeRight:function(event, direction, distance, duration, fingerCount) {
        if ($FV.thumbs.active.prev().length) {
          t = $FV.thumbs.active.prev()
          FVChangeSlide(t);
        }
      },
      tap:function(event, target) {
        target.click();
      },
      threshold:25
    });
  }

  loadJSON();
  
};

var FVSize = function () {
  var win_h = $win.height()
  var frame_h = parseInt($FV.cont.css('marginTop')) + parseInt($FV.cont.css('marginTop'));
  var hdr_h = $FV.hdr.innerHeight()
  var fig_h = win_h - frame_h - $FV.slides.eq(0).find('div.data').innerHeight() - hdr_h;
  $FV.cont.css('height', win_h - frame_h);
  $FV.figs.css('height', fig_h);
  $FV.thumbs_cont.css('height', fig_h - parseInt($FV.thumbs_el.css('paddingTop')));
  $FV.abst_pane.css('height', win_h - frame_h - hdr_h);
  $FV.refs_pane.css('height', win_h - frame_h - hdr_h);
  if ($FV.thmbs_vis) {
    FVThumbPos($FV.thumbs.active);
  }
};

// build header elements
var FVBuildHdr = function(title, authors, articleDoi) {
  $FV.hdr = $('<div class="header" />');
  if ($FV.external_page) {
    var articleLink = "http://dx.plos.org/" + articleDoi.replace("info:doi/", "");
    var h1 = '<h1><a href="' + articleLink + '">' + title + '</a></h1>';
  } else {
    var h1 = '<h1>' + title + '</h1>';
  }
  var authorList = $('<ul class="authors"></ul>');
  $.each(authors, function (index, author) {
    $('<li>' + author + '</li>').appendTo(authorList);
  });
  
  var nav = '<ul class="nav">'
  + '<li class="abst">Abstract</li>'
  + '<li class="figs">Figures</li>'
  + '<li class="refs">References</li>'
  + '</ul>'
  
  $FV.hdr.append(h1);
  $FV.hdr.append(authorList); 
  $FV.hdr.append(nav);
  
  $FV.hdr.find('.nav li').on('click', function() {
    FVDisplayPane(this.className);
  });

  $close = $('<span class="close" title="close" />').on('click', function() {
    FVClose();
  });
  $FV.hdr.append($close);
  
  $FV.cont.prepend($FV.hdr);
}

// build figures pane
var FVBuildFigs = function(data) {
  $FV.figs_pane = $('<div id="fig-viewer-figs" class="pane" />');
  $FV.thumbs_el = $('<div id="fig-viewer-thmbs" />');
  $FV.thumbs_cont = $('<div id="fig-viewer-thmbs-content" />');
  $FV.controls_el = $('<div id="fig-viewer-controls" />');
  $FV.slides_el = $('<div id="fig-viewer-slides" />');
  $FV.staging_el = $('<div class="staging" />'); // hidden container for loading large images
  $FV.figs_set = [];  // all figures array
  var path = '/article/fetchObject.action?uri='
  
  var showInContext = function (uri) {
    uri = uri.split('/');
    uri = uri.pop();
    uri = uri.split('.');
    uri = uri.slice(1);
    uri = uri.join('-');
    return '#' + uri;
  };
    
  $.each(data.secondaryObjects, function () {
    var title_txt = (this.title ? '<b>' +this.title + ':</b> ' : '') + this.transformedCaptionTitle;

    var image_title = this.title + ' ' + this.plainCaptionTitle;

    var $thmb = $('<div class="thmb"' + ' data-uri="' + this.uri + '"><div class="thmb-wrap"><img src="' + path + this.uri + '&representation=PNG_I' + '" alt="' + image_title + '" title="' + image_title + '"></div></div>').on('click', function () {
      FVChangeSlide($(this));
    })
    $FV.thumbs_cont.append($thmb);
    var slide = $('<div class="slide" />');
    var data = $('<div class="data" />');
    var txt = $('<div class="txt" />');
    var content = $('<div class="content" />')
    var title = '<div class="title">' + title_txt + '</div>';
    var toggleMore = $('<div class="toggle more">view all</div>').click(FVToggleExpand);
    var toggleLess = $('<div class="toggle less" title="view less" />').click(FVToggleExpand);
    var context_hash = showInContext(this.uri);
    if ($FV.external_page) { // the image is on another page
      context_hash = '/article/' + $FV.url + context_hash;
    }
    var desc = '<div class="desc">' + this.transformedDescription + '<p>' + this.doi.replace('info:doi/','doi:') + '</p></div>';
    
    // we're not building the images here, just divs with the src of medium & large verisons in data attributes
    var $fig = $('<div class="figure" data-img-src="' + path + this.uri + '&representation=' + this.repMedium + '" data-img-lg-src="' + path + this.uri + '&representation=' + this.repLarge + '" data-img-txt="' + image_title + '"></div>');
    
    $fig.data('state', 0) // track image loading state of figure
    .data('off-top', 0) 
    .data('off-left', 0);
    $FV.figs_set.push($fig);
    
    var staging = '<div class="staging" />'; // hidden container for loading large image
    
    var dl = '<div class="download">'
      + '<h3>Download:</h3>'
      + '<div class="item"><a href="' + "/article/" + this.uri + "/powerpoint" + '" title="PowerPoint slide"><span class="btn">PPT</span></a></div>'
      + '<div class="item"><a href="' + "/article/" + this.uri + "/largerimage" + '" title="large image"><span class="btn">PNG</span><span class="size">' + convertToBytes(this.sizeLarge) + '</span></a></div>'
      + '<div class="item"><a href="' + "/article/" + this.uri + "/originalimage" + '" title="original image"><span class="btn">TIFF</span><span class="size">' + convertToBytes(this.sizeTiff) + '</span></a></div>'
      + '</div>'

    var context_lnk = '<a class="btn lnk_context" href="' + context_hash + '" onclick="FVClose();">Show in Context</a>';
    
    slide.append($fig);
    slide.append(staging);
    content.append(title);

    if (!/^\s*$/.test(this.transformedDescription)) {
      content.append(toggleMore);
      content.append(desc);
    }
    txt.append(toggleLess);
    txt.append(content);
    data.append(txt);
    data.append(context_lnk);
    data.append(dl);
    slide.append(data);
    $FV.slides_el.append(slide);
  });
  
  // thumbnail close button  
  $('<span class="btn-thmb-close" title="close" />').on('click',function() {
    $FV.figs_pane.toggleClass('thmbs-vis');
    $FV.thmbs_vis = $FV.thmbs_vis ? false : true;
  }).appendTo($FV.thumbs_el);
  $FV.thumbs_el.append($FV.thumbs_cont);
  
  $FV.slides = $FV.slides_el.find('div.slide'); // all slides
  $FV.figs = $FV.slides_el.find('div.figure'); // all figures
  $FV.thumbs = $FV.thumbs_el.find('div.thmb'); // all thumbnails
  $FV.thumbs.active = null; // used to track active thumb & figure
  
  // figures controls
  $('<span class="fig-btn thmb-btn">All Figures</span>').on('click',function() {
    $FV.figs_pane.toggleClass('thmbs-vis');
    $FV.thmbs_vis = $FV.thmbs_vis ? false : true;
    FVThumbPos($FV.thumbs.active);
  }).appendTo($FV.controls_el);
  $FV.nxt = $('<span class="fig-btn next"><i class="icn"></i> Next</span>').on('click',function() {
    FVChangeSlide($FV.thumbs.active.next());
  }).appendTo($FV.controls_el);
  $FV.prv = $('<span class="fig-btn prev"><i class="icn"></i> Previous</span>').on('click',function() {
    FVChangeSlide($FV.thumbs.active.prev());
  }).appendTo($FV.controls_el);
  
  $FV.loading = $('<div class="loading-bar"><!--[if lte IE 8]>LOADING<![endif]--></div>').appendTo($FV.controls_el);
  $FV.zoom = $('<div id="fv-zoom" />');
  $FV.zoom.min = $('<div id="fv-zoom-min" />').appendTo($FV.zoom);
  $FV.zoom.sldr = $('<div id="fv-zoom-sldr" />').appendTo($FV.zoom);
  $FV.zoom.max = $('<div id="fv-zoom-max" />').appendTo($FV.zoom);
  $FV.controls_el.append($FV.zoom);
  
  $FV.figs_pane.append($FV.slides_el);
  $FV.figs_pane.append($FV.thumbs_el);
  $FV.figs_pane.append($FV.controls_el);
  $FV.figs_pane.append($FV.staging_el);
  
  $FV.cont.append($FV.figs_pane);
  
}


// build abstract pane
var FVBuildAbs = function(data) {
  var abstractText = data.abstractText;
  $FV.abst_pane = $('<div id="fig-viewer-abst" class="pane cf" />');
  var $abst_content = $('<div class="abstract" />');
  $abst_content.append(abstractText);
  if (!$abst_content.find('p').length) {
    $abst_content.wrapInner('<p>');
  }  
  var lnk_pdf = '<div class="fv-lnk-pdf"><a href="/article/fetchObject.action?uri=' + data.uri + '&representation=PDF" target="_blank" class="btn">Download: Full Article PDF Version</a></div>' 
  $abst_content.append(lnk_pdf);
  
  var abst_info = '<div class="info">'
  + '</div>';
  
  $FV.abst_pane.append($abst_content);
  $FV.abst_pane.append(abst_info);
  $FV.cont.append($FV.abst_pane);

  if (!abstractText || /^\s*$/.test(abstractText)) {
    // There is no abstract. Go back and hide the "view abstract" button created in FVBuildHdr.
    $FV.hdr.find('li.abstract').hide();
  }

};

// build references pane
var FVBuildRefs = function(data, existing_references) {
  $FV.refs_pane = $('<div id="fig-viewer-refs" class="pane cf" />');
  var $refs_content = $('<ol class="references" />');
  if (data.references) {
    // TODO: write code to display data.references
    $refs_content.append("Display " + data.references.length + " references here");
  }
  else if (existing_references.size() > 0) {
    // copy existing article's references' HTML
    $refs_content.html(existing_references.html());
  }
  $FV.refs_pane.append($refs_content);
  $FV.cont.append($FV.refs_pane);
};


// toggle between panes
var FVDisplayPane = function(pane) {
  $FV.removeClass('abst figs refs').addClass(pane); 
  if (pane == 'figs') {
    if ($FV.thumbs.active == null) { // no thumb is active so this is the 1st figure displayed
      // call FVChangeSlide() via thumbnail click, to display correct figure
      if ($FV.figs_ref) { // specific figure is requested
        $FV.thumbs_cont.find('div[data-uri="' + $FV.figs_ref + '"]').trigger('click');
      } else { // default to first figure
        $FV.thumbs.eq(0).trigger('click');
      }
    } else {
      // A figure was displayed, then a different pane was selected and then user returned to the figure pane
      // If a medium or large image finished loading while the figure pane was not visible -
      // figure building would stop (it requires figure pane to be visible to access image dimensions)
      // run FVDisplayFig() again to update figure status
      FVDisplayFig($FV.thumbs.index($FV.thumbs.active));
    }
  } 
};


/**
 * When the user clicks "more" or "less", change the expanded state of every slide.
 */
var FVToggleExpand = function() {
if ($FV.txt_expanded) {
    $FV.slides_el.removeClass('txt-expand');
    $FV.txt_expanded = false;
  } else {
    $FV.slides_el.addClass('txt-expand');
    $FV.txt_expanded = true;
  }
};

// change figure slides functionality
var FVChangeSlide = function($thmb) {

  if(typeof(_gaq) !== 'undefined'){
    _gaq.push(['_trackEvent',"Lightbox", "Slide Changed", ""]);
  }

  if ($FV.thumbs.active !== null) { // not the initial slide
    $FV.thumbs.active.removeClass('active');
    var old_fig = $FV.figs_set[$FV.thumbs.index($FV.thumbs.active)];
    var old_img = old_fig.find('img');
    if (old_img.hasClass('ui-draggable')) { // the slide we are leaving had a drag-enabled figure, reset it
      FVDragStop(old_fig, old_fig.find('img'));
    }
  }
  $FV.thumbs.active = $thmb;
  $FV.thumbs.active.addClass('active');
  
  $FV.slides.hide();
  var i = $FV.thumbs.index($thmb);
  var this_sld = $FV.slides.eq(i);
  this_sld.show();  
  FVDisplayFig(i);
  
  $FV.thumbs.active.next().length ? $FV.nxt.removeClass('invisible') : $FV.nxt.addClass('invisible');
  $FV.thumbs.active.prev().length ? $FV.prv.removeClass('invisible') : $FV.prv.addClass('invisible');
  if ($FV.thmbs_vis) { // no point updating this if you con't see it
    FVThumbPos($thmb);
  }
  
};


/**
 * Bring a thumbnail image into view if it's scrolled out of view.
 * @param thmb the thumbnail image to bring into view
 */
var FVThumbPos = function($thmb) {
  var index = $FV.thumbs.index($thmb);
  var thmb_h = $thmb.outerHeight(true);
  var thmb_top = index * thmb_h;
  var thmb_bot = thmb_top + thmb_h;
  var current_scroll = $FV.thumbs_cont.scrollTop();
  var thumbs_h = $FV.thumbs_cont.innerHeight();
  if (thmb_top < current_scroll) {
    // thmb is above the top of the visible area, so snap it to the top
    $FV.thumbs_cont.scrollTop(thmb_top);
  } else if (current_scroll + thumbs_h < thmb_bot) {
    // thmb is below the bottom of the visible area, so snap it to the bottom
    $FV.thumbs_cont.scrollTop(thmb_bot - thumbs_h);
  }
};


// this function checks the status of figure image building/resizing, and directs to appropriate next step
var FVDisplayFig = function(i) {
  $FV.loading.show();
  $FV.zoom.hide();
  var this_fig = $FV.figs_set[i];
  var $img = this_fig.find('img')
  var state = this_fig.data('state');
  // state of figure
  // 0 - no image loaded
  // 1 - medium image loading
  // 2 - medium image loaded, not visible, not yet resized
  // 3 - medium image visible & resized, large image in process of loading
  // 4 - medium image visible, large image loaded in hidden staging div
  // 5 - large image visible
  switch(state) {
    case 0:
      FVLoadMedImg(i)
      break;
    case 1:
     // waiting on medium image to load
      break;
    case 2:
      FVSizeImgToFit(this_fig, false);
      $img.removeClass('invisible');
      this_fig.data('state', 3);
      FVLoadLargeImg(i);
      break;
    case 3:
      // waiting on large image to load
      break;
    case 4:
      FVSwitchImg($FV.figs_set[i]);
      $FV.loading.hide();
      this_fig.data('state', 5);
      break;
    case 5:
      $FV.loading.hide();
      FVSizeImgToFit(this_fig, true);
      if (this_fig.hasClass('zoom')) {       
        FVFigFunctions(this_fig);
      }
      break;
  }
};


// build medium image, when loaded - size to fit, call load large image function
var FVLoadMedImg = function(i) {
  var src = $FV.figs_set[i].data('img-src');
  var txt = $FV.figs_set[i].data('img-txt');
  var $img = $('<img src="' + src + '" title="' + txt + '" alt="' + txt + '" class="med invisible">');
  $FV.figs_set[i].append($img); // add medium image (set to hidden in css)
  $FV.figs_set[i].data('state', 1);
  $FV.figs_set[i].imagesLoaded(function() {
    $FV.figs_set[i].data('state', 2);
    if (i == $FV.thumbs.index($FV.thumbs.active) && $FV.hasClass('figs')) { // true if this slide is still visible
      FVSizeImgToFit($FV.figs_set[i], false);
      $FV.figs_set[i].find('img').removeClass('invisible');
      $FV.figs_set[i].data('state', 3);
      FVLoadLargeImg(i);
    }
  });
};


// load large images in div.staging
var FVLoadLargeImg = function(i) {
  var src = $FV.figs_set[i].data('img-lg-src');
  var txt = $FV.figs_set[i].data('img-txt');
  var $img = $('<img src="' + src + '" title="' + txt + '" alt="' + txt + '" class="lg invisible">');
  $FV.figs_set[i].next('div.staging').append($img); // load large img into 'staging' div
  $FV.figs_set[i].next('div.staging').imagesLoaded(function() {
    $FV.figs_set[i].data('state', 4);
    if (i == $FV.thumbs.index($FV.thumbs.active) && $FV.hasClass('figs')) { // true if this slide is still visible
      FVSwitchImg($FV.figs_set[i]);
      $FV.loading.hide();
      $FV.figs_set[i].data('state', 5);
    }
  });
};

// size images to fit in parent element
var FVSizeImgToFit = function(el, down_only) {
  var img = el.find('img');
  var el_h = el.height();
  var el_w = el.width();
  var i_h = img.height();
  var i_w = img.width();
  
  // sizes image to fit, scalling up or down, and centering
  // setting size with height
  var sizeAndCenter = function() {
    // compare aspect ratios of parent and image and set dimensions accordingly
    if (el_w / el_h > i_w / i_h) {
      img.css({'height': el_h});
      // horizontally center after resizing, (zoom uses margin values so can't use auto)
      img.css({'marginLeft' : Math.round((el_w -  img.width()) / 2), 'marginTop' : 0});
    } else {
      // calculate height to make width match parent
      img.css({'height' : Math.round(el_w * (i_h / i_w))});
      // vertically center after resizing
      img.css({'marginTop' : Math.round((el_h - img.height()) / 2), 'marginLeft' : 0}); 
    }
  }
  
  if (down_only) { // this is a large image and we don't want to scale up.
    if (el_h > el.data('img_l_h') && el_w > el.data('img_l_w')) { // native size smaller than viewport
      img.css({'marginTop' : Math.round((el_h - i_h) / 2), 'marginLeft' : Math.round((el_w - i_w) / 2)}); // center
      el.removeClass('zoom'); // too small to zoom
    } else {
      sizeAndCenter();
      el.addClass('zoom');
    }
  } else { // this is a medium image, we will scale up or down
     sizeAndCenter();
  }
}

// switch medium image with large image
var FVSwitchImg = function($fig) {
  var $img_m = $fig.find('img');
  var img_m_h = $img_m.height();
  var $img_l = $fig.next('div.staging').find('img');
  // move large image into figure div (image hidden by css)
  $fig.append($img_l); 
  var img_l_h = $img_l.height();
  var img_l_w = $img_l.width();  
  // store native dimensions
  $fig.data('img_l_w', img_l_w).data('img_l_h', img_l_h);
  if (img_l_h < img_m_h) { // large image smaller than resized medium image
    $img_l.css({'marginTop' : Math.round(($fig.height() - img_l_h) / 2), 'marginLeft' : Math.round(($fig.width() - img_l_w) / 2)}); // center
  } else {
    $img_l.css({'height' : img_m_h, 'marginTop' : $img_m.css('marginTop'), 'marginLeft' : $img_m.css('marginLeft')}); // match dimensions and position of medium image
    $fig.addClass('zoom'); // zoomable & draggable
  }
  $fig.html($img_l.removeClass('invisible')); // replace
  var drag_bx = $('<div class="drag-bx" />'); // insert drag containment element
  $fig.wrapInner(drag_bx);
  if ($fig.hasClass('zoom')) {
    FVFigFunctions($fig);
  }
};


// zoom & drag figure
// this is called following either
// on the visible slide, a large image that is bigger than the slide has finished loading
// OR navigating to a slide whose large image is bigger than the slide and has already loaded.
var FVFigFunctions = function($fig) {
  var $img = $fig.find('img'); 
  var real_h = $fig.data('img_l_h'); // native height of image
  var real_w = $fig.data('img_l_w'); // native width of image
  var img_mt = parseInt($img.css('marginTop')); // top margin of sized to fit image
  var img_ml = parseInt($img.css('marginLeft')); // left margin of sized to fit image
  var resize_h = $img.height(); // height of sized to fit image
  var drag = false; // dragging not enabled
  var $drgbx = $fig.find('div.drag-bx');
  
  $FV.zoom.show();  
  $FV.zoom.sldr.slider({
    min: resize_h,
    max: real_h,
    value: resize_h,
    slide: function(e, ui) {
      imgResize(ui.value);
    },
    stop: function(e, ui) {
      if (!drag) {
        // enable dragging
        FVDragInit($fig, $img);
      }
      if (ui.value == resize_h) { // slider is at minimum value
        // kill drag
        FVDragStop($fig, $img);
        drag = false;
      } else {
        FVSizeDragBox($fig, $img);
        drag = true;
      }
    }
  });
  
  // max(+) buttton 
  $FV.zoom.max.on('click', function() {
    $FV.zoom.sldr.slider('value', real_h );
    imgResize(real_h);
    FVDragInit($fig, $img);
    FVSizeDragBox($fig, $img)
  });
  
  // min(-) buttton 
  $FV.zoom.min.on('click', function() {
    if (!drag) { // dragging is not enabled, so image must be zoomed in, nothing to do here
      return false;
    }
    $FV.zoom.sldr.slider('value', resize_h );
    $img.css({
      'height': resize_h,
      'marginTop': img_mt,
      'marginLeft': img_ml
    });
    FVDragStop($fig, $img);
    drag = false;
  });
  
  var imgResize = function(x) {
    $img.css({
      'height': x,
      'marginTop': img_mt - Math.round((x - resize_h) / 2),
      'marginLeft': img_ml - Math.round(((x - resize_h) / 2) * (real_w / real_h))
    });
  }
};

var FVDragInit = function($fig, $img) {
  $img.draggable({
    containment: 'parent',
    stop: function(e, ui) {
      // FVSizeDragBox() sets top/left values to match margins of figure
      // following dragging these are no longer in sync, top/left have changed
      // storing the difference between values, (if no dragging value is 0)
      // when a figure is resized the margins are updated and then the drag box is resized
      // to calculate dimensions/position of resized drag bax, the difference in values prior to figure resize is required
      $fig.data('off-top', Math.abs(parseInt($img.css('marginTop'))) - parseInt($img.css('top')))
      .data('off-left', Math.abs(parseInt($img.css('marginLeft'))) - parseInt($img.css('left')))
    }
  });
};

var FVDragStop = function($fig, $img) {
  $img.draggable('destroy');  
  // reset
  $img.css({'top' : 0, 'left' : 0,}); 
  $fig.find('div.drag-bx').removeAttr('style');
  $fig.data('off-top', 0) 
  .data('off-left', 0);
};

// Size & position div to contain figure dragging
// adds top/left declarations to image so that image remains in same position following containing div sizing/positioning
// runs following figure resize
var FVSizeDragBox = function($fig, $img) {
  var $drgbx = $fig.find('div.drag-bx');
  var fig_h = $fig.height();
  var fig_w = $fig.width();
  var img_h = $img.height();
  var img_w = $img.width()
  var img_mt = parseInt($img.css('marginTop'));
  var img_ml = parseInt($img.css('marginLeft'));
  var img_tp = isNaN(parseInt($img.css('top'))) ? 0 : parseInt($img.css('top'));
  var img_lt = isNaN(parseInt($img.css('left'))) ? 0 : parseInt($img.css('left'));
  if (fig_h > img_h) {
    $drgbx.css({
      'top' : img_mt * -1,
      'height': fig_h + img_mt
    });
    $img.css({
      'top' : img_mt - $fig.data('off-top')
    });
  } else {
    $drgbx.css({
      'top' :  (img_h - fig_h + img_mt) * -1,
      'height': ((img_h * 2) - fig_h) + img_mt
    });
    $img.css({
      'top' : img_h - fig_h + img_mt - $fig.data('off-top')
    });
  }
  if (fig_w > img_w) {
    $drgbx.css({
      'left' : img_ml * -1,
      'width': fig_w + img_ml
    });
    $img.css({
      'left' : img_ml - $fig.data('off-left')
    });
  } else {
    $drgbx.css({
      'left' :  (img_w - fig_w + img_ml) * -1,
      'width': ((img_w * 2) - fig_w) + img_ml
    });
    $img.css({
      'left' : img_w - fig_w + img_ml - $fig.data('off-left')
    });
  }

};


// close 
var FVClose = function() {
  $FV.remove();

  // remove helper class added in displayModal()
  $('body').removeClass('modal-active');

  $win.unbind('resize.modal');
  //will record the timeStamp for when the modal is closed
  if(typeof event !== 'undefined') {
    close_time = event.timeStamp;
  }
};

/*
  END FIGURE VIEWER ********************************************
*/

// Begin other global functions

// contert numbers to data storage units
var convertToBytes = function(num) {
  if (num < 0) {
    return "unknown";
  } else {
    if (num < 1000) {
      return "" + num + "B";
    } else {
      if (num < 1000000) {
        return "" + Math.round(num / 1000) + "KB";
      } else {
        return "" + Math.round(num / 10000) / 100 + "MB";
      }
    }
  }
};

function getParameterByName(name) {
  name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
  var regexS = "[\\?&]" + name + "=([^&#]*)";
  var regex = new RegExp(regexS);
  var results = regex.exec(window.location.search);
  if (results == null)
    return "";
  else
    return decodeURIComponent(results[1].replace(/\+/g, " "));
}

//Stolen from:
//http://stackoverflow.com/questions/149055/how-can-i-format-numbers-as-money-in-javascript
Number.prototype.format = function (c, d, t) {
  var n = this, c = isNaN(c = Math.abs(c)) ? 2 : c, d = d == undefined ? "," : d, t = t == undefined ? "." :
    t, s = n < 0 ? "-" : "", i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "", j = (j = i.length) > 3 ? j % 3 : 0;
  return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d +
    Math.abs(n - i).toFixed(c).slice(2) : "");
};

// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function (from, to) {
  var rest = this.slice((to || from) + 1 || this.length);
  this.length = from < 0 ? this.length + from : from;
  return this.push.apply(this, rest);
};


// Begin collapsible

// Collapsible div used on the 500 page (error.ftl) to hold the exception stacktrace.
// Based on code from http://www.darreningram.net/pages/examples/jQuery/CollapsiblePanelPlugin.aspx
// (No copyright statements there, but including for attribution.)

(function($) {
  $.fn.extend({
    collapsiblePanel: function() {
      return $(this).each(ConfigureCollapsiblePanel);
    }
  });
})(jQuery);

function ConfigureCollapsiblePanel() {
  $(this).addClass("ui-widget");

  // Check if there are any child elements, if not then wrap the inner text within a new div.
  if ($(this).children().length == 0) {
    $(this).wrapInner("<div></div>");
  }

  // Wrap the contents of the container within a new div.
  $(this).children().wrapAll("<div class='collapsibleContainerContent ui-widget-content'></div>");

  // Create a new div as the first item within the container.  Put the title of the panel in here.
  $("<div class='collapsibleContainerTitle ui-widget-header'><div>" + $(this).attr("title") + "</div></div>").prependTo($(this));

  // Assign a call to CollapsibleContainerTitleOnClick for the click event of the new title div.
  $(".collapsibleContainerTitle", this).click(CollapsibleContainerTitleOnClick);

  // Keep the widget closed initially.
  $(".collapsibleContainerContent", $(this).parent()).toggle();
}

function CollapsibleContainerTitleOnClick() {

  // The item clicked is the title div... get this parent (the overall container) and toggle the content within it.
  $(".collapsibleContainerContent", $(this).parent()).slideToggle();
}

// End collapsible

// Begin global blocks

//if search box is empty, don't submit the form
//This is a little weird, but there are multiple forms on multiple pages
//The home/global and advanced search pages
$('form[name="searchForm"], form[name="searchStripForm"]').each(function (index, item) {
  $(item).submit(function () {
    //Form fields are sometimes name differently pending on where the search came from
    //namely simple or advanced
    if (!$(this).find('input[name="query"], input[name="unformattedQuery"]').val()) {
      return false;
    }
    else {
      $('#db input[name="startPage"]').val('0');
    }
  });
});

var $toc_block_cover = $('#toc-block .cover img');
if ($toc_block_cover.length) {
  var doi = $toc_block_cover.data('doi');
  $toc_block_cover.click(function () {
    FigViewerInit(doi, null, 'figs', true);
  });
}

var imageURI = getParameterByName("imageURI");
if (imageURI) {
  var index = imageURI.lastIndexOf(".");
  if (index > 0) {
    var doi = imageURI.substr(0, index);
    FigViewerInit(doi, imageURI, 'figs');
  }
}
delete imageURI;


//Browse / issue page functions
// on window load
$(window).load(function () {
  $('.journal_issues').doOnce(function () {
    this.journalArchive({
      navID:'#journal_years',
      slidesContainer:'#journal_slides',
      initialTab:0
    });
  });
});


//Why is this bound universally?  That seems strange.
//-Joe
$(document).bind('keydown', function (e) {
  if (e.which == 27) {
    FVClose();
  }
});

// End global block

// call the initialization function

initMainContainer();


// Begin PJAX related code

var pjax_selected_tab = null; // last clicked pjax content

if ($(document).pjax) {
  $(document).pjax("#nav-article ul li a, .nav-col .nav-col-comments a, .sidebar .sidebar-comments p a", "#pjax-container",
      {container: "#pjax-container", fragment: "#pjax-container", timeout: 5000, scrollTo: "do-not"});

  $("#pjax-container").on("pjax:complete", function(event) {
    // invoke document ready and window initialization code
    onReadyMainContainer();
    initMainContainer();

    // when metrics tab is selected, load highcharts.js only
    // if it was not already loaded. If the tab content was loaded from
    // pjax (and not from cache) then also initialize ALM.

    if (pjax_selected_tab == "metrics") {
      if (typeof Highcharts == "undefined") {
        $.getScript("/javascript/highcharts.js", function(data, textStatus, jqxhr) {
          onLoadALM();
        });
      }
      else {
        onLoadALM();
      }
    }

    else if (pjax_selected_tab == "article"){
      // figshare_widget_load variable is defined if figshare was loaded before.
      // but plos_widget.js must be loaded again to show the figshare when
      // switching to article tab
      // if switching from another tab to article tab
      // then add figshare css and js files.
      // e.g. metrics --> article
      // do not add css if article tab was already opened before.
      // e.g: article --> metrics --> article
      // if landing page is article then p_widget.js is included from article.ftl
      if (typeof figshare_widget_load == "undefined") {
        function add_widget_css() {
          var headtg = document.getElementsByTagName('head')[0];
          if (!headtg) {
            return;
          }
          var linktg = document.createElement('link');
          linktg.type = 'text/css';
          linktg.rel = 'stylesheet';
          linktg.href = 'http://wl.figshare.com/static/css/p_widget.css?v=8';
          headtg.appendChild(linktg);
        }
        add_widget_css();
      }
      $.getScript("http://wl.figshare.com/static/plos_widget.js?v=10");
      $.getScript("http://wl.figshare.com/static/jmvc/main_app/resources/jwplayer/jwplayer.js");
      figshare_widget_load = true;
    }

    // For related pages, if no item exists under more_by_authors and
    // the page is not yet cached, reload the javascript to populate the
    // related content.
    else if (pjax_selected_tab == "related"){
      if($('div[id="more_by_authors"] > ul > li').length == 0) {
        if($.pjax.contentCache[window.location.href] === undefined ||
            !$.pjax.contentCache[window.location.href].loaded) {
          $.getScript("/javascript/related_content.js");
        }
      }
    }

  });
}

// End Pjax related code

$(function() {
  //Stolen from:
  //http://www.vancelucas.com/blog/fixing-ie7-z-index-issues-with-jquery/
  if($.browser.msie && jQuery.browser.version < 10) {
    var zIndexNumber = 500;
    $('div.sidebar').find('div').each(function() {
      $(this).css('zIndex', zIndexNumber);
      zIndexNumber -= 10;
    });
  }
});

/*
 * jQuery UI Autocomplete HTML Extension
 *
 * Copyright 2010, Scott González (http://scottgonzalez.com)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 *
 * http://github.com/scottgonzalez/jquery-ui-extensions
 */

// HTML extension to autocomplete borrowed from
// https://github.com/scottgonzalez/jquery-ui-extensions/blob/master/autocomplete/jquery.ui.autocomplete.html.js

(function( $ ) {

  var proto = $.ui.autocomplete.prototype,
    initSource = proto._initSource;

  function filter( array, term ) {
    var matcher = new RegExp( $.ui.autocomplete.escapeRegex(term), "i" );
    return $.grep( array, function(value) {
      return matcher.test( $( "<div>" ).html( value.label || value.value || value ).text() );
    });
  }

  $.extend( proto, {
    _initSource: function() {
      if ($.isArray(this.options.source) ) {
        this.source = function( request, response ) {
          response( filter( this.options.source, request.term ) );
        };
      } else {
        initSource.call( this );
      }
    },

    _renderItem: function( ul, item) {
      return $( "<li></li>" )
        .data( "item.autocomplete", item )
        .append( $( "<a style=\"line-height: "
          + (item.value ? 0.9 : 2)
          + "; font-size: 12px;\"></a>" )
          [item.type == "html" ? "html" : "text"]( item.label ) )
        .appendTo( ul );
    }
  });

})( jQuery );

// table popup and download as CSV
function tableOpen(tableId, type) {
  try {
    var table = $('div.table-wrap[name="' + tableId + '"]')
    if (type == "HTML") {
      var w = window.open();
      w.document.open();
      w.document.writeln('<html><head><link rel="stylesheet" type="text/css" href="/css/global.css"></head>');
      w.document.writeln('<body style="background-color: #ffffff;">');
      w.document.writeln('<div class="table-wrap">' + table.html() + '</div>');
      w.document.writeln('</body></html>')
      w.document.close();
    }
    else if (type == "CSV") {
      //http://stackoverflow.com/questions/7161113/how-do-i-export-html-table-data-as-csv-file
      function row2CSV(tmpRow) {
        var tmp = tmpRow.join('') // to remove any blank rows
        if (tmpRow.length > 0 && tmp != '') {
          var mystr = tmpRow.join(',');
          csvData[csvData.length] = mystr;
        }
      }
      function formatData(input) {
        // replace " with “
        var regexp = new RegExp(/["]/g);
        var output = input.replace(regexp, "“");
        //HTML
        var regexp = new RegExp(/\<[^\<]+\>/g);
        var output = output.replace(regexp, "");
        if (output == "") return '';
        return '"' + output + '"';
      }
      var csvData = [];
      var headerArr = [];
      var tmpRow = [];
      $(table).find('thead td').each(function() {
        tmpRow[tmpRow.length] = formatData($(this).html());
      });
      row2CSV(tmpRow);
      $(table).find('tbody tr').each(function() {
        var tmpRow = [];
        $(this).find('td').each(function() {
          tmpRow[tmpRow.length] = formatData($(this).html());
        });
        row2CSV(tmpRow);
      });
      var mydata = csvData.join('\n');
      var dataurl = 'data:text/csv;base64,' + $.base64.encode($.base64.utf8_encode(mydata));
      if ($.browser && ($.browser.chrome)) {
        // you can specify a file name in <a ...> tag on chrome.
        // http://stackoverflow.com/questions/283956/is-there-any-way-to-specify-a-suggested-filename-when-using-data-uri
        function downloadWithName(uri, name) {
          function eventFire(el, etype){
            if (el.fireEvent) {
              (el.fireEvent('on' + etype));
            } else {
              var evObj = document.createEvent('Events');
              evObj.initEvent(etype, true, false);
              el.dispatchEvent(evObj);
            }
          }
          var link = document.createElement("a");
          link.download = name;
          link.href = uri;
          eventFire(link, "click");
        }
        downloadWithName(dataurl, tableId + ".csv");
      }
      else {
        window.location = dataurl;
      }
    }
  }
  catch (e) {
    console.log(e);
  }
  return false;
}