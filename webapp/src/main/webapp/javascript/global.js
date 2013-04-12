/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
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

  $('.article a[href^="#"]').on('click', function (e) {
    e.preventDefault();
    var href = $(this).attr('href').split('#')[1];
    var b = $('a[name="' + href + '"]');

    window.history.pushState({}, document.title, $(this).attr('href'));

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
  var $nav_article = $('#nav-article');
  if ($nav_article.length) {
    items_l = $nav_article.find('li').length
    $nav_article.addClass('items-' + items_l);
  }

  var $figure_thmbs = $('#figure-thmbs');
  if ($figure_thmbs.length) {
    $lnks = $figure_thmbs.find('.item a');
    $wrap = $figure_thmbs.find('div.wrapper');
    if ($lnks.length) {
      $lnks.on('click', function (e) {
        e.preventDefault();
        doi = $(this).data('doi');
        ref = $(this).data('uri');
        launchModal(doi, ref, 'fig');
      });
      $fig_tog = $('<span>Hide Figures</span>').toggle(function () {
          $wrap.hide();
          $figure_thmbs.find('div.buttons').hide();
          $figure_thmbs.find('div.controls').hide();
          $fig_tog.html('Show Figures')
            .toggleClass('hide');
        },function () {
          $wrap.show();
          $figure_thmbs.find('div.buttons').show();
          $figure_thmbs.find('div.controls').show();
          $fig_tog.html('Hide Figures')
            .toggleClass('hide');
        }
      ).insertAfter($figure_thmbs)
        .wrap('<div id="fig-toggle" class="cf" />');
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
      launchModal(doi, ref, 'fig');
    });
    $lnks.append('<div class="expand" />');
  }

  // figure search results
  var $fig_results = $('#fig-search-results');
  if ($fig_results.length) {
    $fig_results.find('a.figures').on('click', function () {
      doi = $(this).data('doi');
      launchModal(doi, null, 'fig', true);
    });
    $fig_results.find('a.abstract').on('click', function () {
      doi = $(this).data('doi');
      launchModal(doi, null, 'abstract', true);
    });
  }

  // figure link in article floating nav
  var $nav_figs = $('#nav-figures a');
  if ($nav_figs.length) {
    $nav_figs.on('click', function () {
      var doi = $(this).data('doi');
      launchModal(doi, null, 'fig');
    });
  }

  // figure link in the toc
  var $toc_block_links = $('#toc-block div.links');
  if ($toc_block_links.length) {
    $toc_block_links.find('a.figures').on('click', function () {
      var doi = $(this).data('doi');
      launchModal(doi, null, 'fig', true);
    });

    $toc_block_links.find('a.abstract').on('click', function () {
      var doi = $(this).data('doi');
      launchModal(doi, null, 'abstract', true);
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
    console.log("pjax click " + this.name);
    if(selected_tab == "metrics") {
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

//For Google Analytics Event Tracking
var category, action, label;
category = "tab menu actions";
action = "tab menu click";
$(document).ajaxComplete(function(){
    if(pjax_selected_tab != null){ label = pjax_selected_tab;};
    if(typeof(_gaq) !== 'undefined'){
      _gaq.push(['_trackEvent',category,action,label]);
    }
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

          window.history.pushState({}, document.title, event.target.href);

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

// Begin Figure Viewer

var launchModal = function (doi, ref, state, imgNotOnPage) {
  var path = '/article/fetchObject.action?uri='
  //var path = 'article/';
  var $modal = $('<div id="fig-viewer" class="modal" />');
  var $thmbs = $('<div id="fig-viewer-thmbs" />');
  var $slides = $('<div id="fig-viewer-slides" />');
  var $abstract = $('<div id="fig-viewer-abst" />');
  var $mask = $('<div id="modal-mask" />').on('click', function () {
    killModal();
  });
  var active_thmb = null;
  var page_url;
  var modal_h;
  var slides_h;
  var $figs;
  var figs_h;
  var thmbs_h;
  var thmb_h;
  var thmbs_resized_h;
  var $thmb_1;
  var abst_h;
  var $abs_txt;
  var abs_txt_h;
  var $all_thmb;
  var $all_sld;
  var txt_expanded;

  /**
   * When the user clicks "more" or "less", change the expanded state of every slide.
   */
  var toggleExpand = function () {
    var slideCaptions = $('#fig-viewer-slides .slide .content');
    var titleCaptions = $('#fig-viewer-slides .slide .title');
    var toggleMoreButton = $('#fig-viewer-slides .slide .more');
    var toggleLessButton = $('#fig-viewer-slides .slide .less');

    if (txt_expanded) {
      slideCaptions.removeClass('expand');
      titleCaptions.removeClass('titleexpand');
      toggleMoreButton.removeClass('hidden');
      toggleLessButton.addClass('hidden');
      txt_expanded = false;
    } else {
      slideCaptions.addClass('expand');
      titleCaptions.addClass('titleexpand');
      toggleMoreButton.addClass('hidden');
      toggleLessButton.removeClass('hidden');
      txt_expanded = true;
    }
  };

  var buildFigs = function () {
    $.ajax({
      url:'/article/lightbox.action?uri=' + doi,
      // url: 'article/' + doi,
      dataFilter:function (data, type) {
        return data.replace(/(^\/\*|\*\/$)/g, '');
      },
      dataType:'json',
      error: function (jqXHR, textStatus, errorThrown) {
        console.log(errorThrown);
      },
      success:function (data) {
        page_url = data.URL;
        $.each(data.secondaryObjects, function () {
          title_txt = (this.title ? this.title + '. ' : '') + this.transformedCaptionTitle;

          image_title = this.title + ' ' + this.plainCaptionTitle;

          $thmb = $('<div class="thmb"' + ' data-uri="' + this.uri + '"><div class="thmb-wrap"><img src="' + path + this.uri + '&representation=PNG_I' + '" alt="' + image_title + '" title="' + image_title + '"></div></div>').on('click', function () {
            changeSlide($(this));
          })
          $thmbs.append($thmb);
          var slide = $('<div class="slide" />');
          var txt = $('<div class="txt" />');
          var lnks = $('<div class="lnks" />');
          var content = $('<div class="content" />')
          var title = '<div class="title">' + title_txt + '</div>';
          txt_expanded = false;
          var toggleMore = $('<div class="toggle more">show more</div>').click(toggleExpand);
          var toggleLess = $('<div class="toggle less hidden">show less</div>').click(toggleExpand);
          var context_hash = showInContext(this.uri);
          if (imgNotOnPage) { // the image is on another page
            context_hash = '/article/' + page_url + context_hash;
          }
          var desc = '<div class="desc">' + this.transformedDescription + '<p>' + this.doi.replace('info:doi/','doi:') + '</p></div>';
          var img = '<div class="figure" data-img-src="' + path + this.uri + '&representation=' + this.repMedium + '" data-img-txt="' + image_title + '"></div>'
          var lnks_txt = '<ul class="download">'
            + '<li class="label">Download: </li>'
            + '<li><span class="icon"><a href="' + "/article/" + this.uri + "/powerpoint" + '">PPT</a></span> <a href="' + "/article/" + this.uri + "/powerpoint" + '" class="ppt">PowerPoint slide</a></li>'
            + '<li><span class="icon"><a href="' + "/article/" + this.uri + "/largerimage" + '">PNG</a></span> <a href="' + "/article/" + this.uri + "/largerimage" + '" class="png">larger image (' + displaySize(this.sizeLarge) + ')</a></li>'
            + '<li><span class="icon"><a href="' + "/article/" + this.uri + "/originalimage" + '">TIFF</a></span> <a href="' + "/article/" + this.uri + "/originalimage" + '" class="tiff">original image (' + displaySize(this.sizeTiff) + ')</a></li>'
            + '</ul>'
            + '<ul class="figure_navigation">'
            + '<li><span class="btn active">browse figures</span></li>'
            + '<li><span class="btn viewAbstract" onclick="toggleModalState();">view abstract</span></li>'
            + '<li><a class="btn" href="' + context_hash + '" onclick="killModal();">show in context</a></li>'
            + '</ul>'
          slide.append(img);
          content.append(title);

          if (!/^\s*$/.test(this.transformedDescription) ||
            $(title).text().length > 50) {
            content.append(toggleMore);
            content.append(desc);
          }
          txt.append(toggleLess);
          txt.append(content);
          lnks.append(lnks_txt);
          slide.append(txt);
          slide.append(lnks);
          $slides.append(slide);
        });

        btn_prev = $('<span class="fig-btn prev" />').on('click',function () {
          t = active_thmb.prev()
          changeSlide(t);
        }).appendTo($slides);
        btn_next = $('<span class="fig-btn next" />').on('click',function () {
          t = active_thmb.next()
          changeSlide(t);
        }).appendTo($slides);
        $modal.append($slides);
        $modal.append($thmbs);
        $thmb_1 = $thmbs.find('div.thmb').eq(0);
        $all_thmb = $thmbs.find('div.thmb');
        $all_sld = $slides.find('div.slide');
        if (ref) {
          $thmbs.find('div[data-uri="' + ref + '"]').trigger('click');
        } else {
          $thmb_1.trigger('click');
        }
        buildAbs(data, imgNotOnPage);
      }
    });
  };

  /**
   * @param isButton {boolean} {@code true} to style as a button; {@code false} for plain-text link
   * @return {String} an HTML snippet for the lightbox's "full text" button
   */
  var getFullTextElement = function (isButton) {
    var articleBlock = $('#article-block');
    var aClass = isButton ? 'class="btn"' : '';
    if (articleBlock.length == 0) {
      // Not on the article page, so the full text button should link there
      return '<a ' + aClass + ' href="' + '/article/' + page_url + '">';
    }
    // On the article page, so "full text" closes the lightbox and jumps to the abstract
    var href = '#abstract0';
    if (articleBlock.find(href).length == 0) {
      href = '#'; // article has no abstract; default to top of page
    }
    return '<a ' + aClass + ' href="' + href + '" onclick="killModal();">';
  };

  var buildAbs = function (data, linkTitle) {
    var abstractText = data.abstractText;
    var abstractHtml = '<div id="fig-viewer-abst">'
      + '<div class="txt"><p>' + abstractText + '</p></div>'
      + '<div class="lnks">'
      + '<ul class="download">'
      + '<li class="label">Download: </li>'
//   + '<li><span class="icon">PDF</span> <a href="' + "/article/" + this.uri + "/pdf" + '" class="pdf">Full Article PDF Version</a></li>'
      + '<li><span class="icon">PDF</span> <a href="' + "/article/fetchObjectAttachment.action?uri=" + doi + "&representation=PDF" + '" class="pdf">Full Article PDF Version</a></li>'
      + '</ul>'
      + '<ul class="figure_navigation">'
      + '<li><span class="btn" onclick="toggleModalState();">browse figures</span></li>'
      + '<li><span class="btn active viewAbstract">view abstract</span></li>'
      + '<li>' + getFullTextElement(true) + 'show in context</a></li>'
      + '</ul>'
      + '</div>'
      + '</div>';

    $modal.append(abstractHtml);

    if (!abstractText || /^\s*$/.test(abstractText)) {
      // There is no abstract. Go back and hide the "view abstract" button created in buildFigs (not just here).
      $modal.find('.viewAbstract').hide();
    }

    displayModal(data.articleType, data.articleTitle, data.authors, data.uri, linkTitle);
  };


  var displayModal = function (articleType, title, authors, articleDoi, linkTitle) {
    $hdr = $('<div class="header" />');
    if (linkTitle) {
      var articleLink = "http://dx.plos.org/" + articleDoi.replace("info:doi/", "");
      h1 = '<h1><a href="' + articleLink + '">' + title + '</a></h1>';
    } else {
      h1 = '<h1>' + title + '</h1>';
    }
    authorList = $('<ul class="authors"></ul>');
    $.each(authors, function (index, author) {
      $('<li>' + author + '</li>').appendTo(authorList);
    })

    $hdr.append('<span id="article-type-heading">' + articleType + '</span>');
    $hdr.append(h1);
    $hdr.append(authorList);

    $close = $('<span class="close" />').on('click', function () {
      killModal();
    });
    $hdr.append($close);
    $modal.prepend($hdr);
    if (state == 'abstract') {
      $modal.addClass('abstract');
    }

    $('body').append($modal)
      .append($mask);

    // add helper class to body element to prevent page scrolling when modal is open
    $('body').addClass('modal-active');

    modal_h = $modal.height();
    slides_h = $slides.height();
    $figs = $slides.find('div.figure');
    figs_h = $figs.eq(0).height();
    thmbs_h = $thmbs.height();
    abst_h = $abstract.height();
    $abs_txt = $abstract.find('div.txt');
    abs_txt_h = $abs_txt.height();
    bt = parseInt($thmb_1.css('borderTopWidth'));
    bb = parseInt($thmb_1.css('borderBottomWidth'));
    h = $thmb_1.innerHeight();
    thmb_h = bt + bb + h;
    thmbs_resized_h = thmbs_h;
    resizeModal();
    $win.bind('resize.modal', resizeModal);

  };

  var changeSlide = function (thmb) {
    $all_sld.hide();
    this_sld = $all_sld.eq($all_thmb.index(thmb));
    $fig = this_sld.find('div.figure');
    if ($fig.is('[data-img-src]')) {
      src = $fig.data('img-src');
      txt = $fig.data('img-txt');
      img = '<img src="' + src + '" title="' + txt + '" alt="' + txt + '">';
      $fig.append(img);
      $fig.removeAttr('data-img-src')
        .removeAttr('data-img-txt');
    }
    this_sld.show();
    if (active_thmb !== null) {
      active_thmb.removeClass('active');
    }
    active_thmb = thmb;
    active_thmb.addClass('active');
    active_thmb.next().length ? btn_next.show() : btn_next.hide();
    active_thmb.prev().length ? btn_prev.show() : btn_prev.hide();

    thumbPos(thmb);
  };


  /**
   * Bring a thumbnail image into view if it's scrolled out of view.
   * @param thmb the thumbnail image to bring into view
   */
  var thumbPos = function (thmb) {
    var index = $all_thmb.index(thmb);
    var thmb_top = index * thmb_h;
    var thmb_bottom = thmb_top + thmb_h;
    var currentScroll = $thmbs.scrollTop();

    if (thmb_top < currentScroll) {
      // thmb is above the top of the visible area, so snap it to the top
      $thmbs.scrollTop(thmb_top);
    } else if (currentScroll + thmbs_resized_h < thmb_bottom) {
      // thmb is below the bottom of the visible area, so snap it to the bottom
      $thmbs.scrollTop(thmb_bottom - thmbs_resized_h);
    }
  };


  var displaySize = function (num) {
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

  var showInContext = function (uri) {
    uri = uri.split('/');
    uri = uri.pop();
    uri = uri.split('.');
    uri = uri.slice(1);
    uri = uri.join('-');
    return '#' + uri;
  };


  var resizeModal = function () {
    var doc_h = $(document).height();
    var win_h = $win.height();
    var win_w = $win.width();
    $mask.css({'width':win_w, 'height':doc_h});
    if (win_h >= modal_h) {
      $modal.css('top', Math.round(win_h / 2 - modal_h / 2));
      $slides.css('height', slides_h);
      $figs.css('height', figs_h);
      $thmbs.css('height', thmbs_h);
      $abstract.css('height', abst_h);
      $abs_txt.css('height', abs_txt_h);
      thmbs_resized_h = thmbs_h;
    } else {
      $modal.css('top', 0);
      $slides.css('height', win_h - (modal_h - slides_h));
      $figs.css('height', win_h - (modal_h - figs_h));
      $thmbs.css('height', thmbs_resized_h);
      $abstract.css('height', win_h - (modal_h - abst_h));
      $abs_txt.css('height', win_h - (modal_h - abs_txt_h));
    }
    $modal.css('left', Math.round(win_w / 2 - $modal.width() / 2));
    thumbPos(active_thmb);
  };

  $(this).bind('keydown', function (e) {
    if (e.which == 37 || e.which == 38) {
      if (active_thmb.prev().length) {
        t = active_thmb.prev()
        changeSlide(t);
      }
      return false;
    }

    if (e.which == 39 || e.which == 40) {
      if (active_thmb.next().length) {
        t = active_thmb.next()
        changeSlide(t);
      }
      return false;
    }
  });

  buildFigs();
};

var killModal = function () {
  $('div.modal').remove();
  $('#modal-mask').remove();

  // remove helper class added in displayModal()
  $('body').removeClass('modal-active');

  $win.unbind('resize.modal');
  //will record the timeStamp for when the modal is closed
  close_time = event.timeStamp;
};

// End Figure Viewer

// Begin other global functions

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
    launchModal(doi, null, 'fig', true);
  });
}

var toggleModalState = function () {
  $('#fig-viewer').toggleClass('abstract');
};

var imageURI = getParameterByName("imageURI");
if (imageURI) {
  var index = imageURI.lastIndexOf(".");
  if (index > 0) {
    var doi = imageURI.substr(0, index);
    launchModal(doi, imageURI, 'fig');
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


$(document).bind('keydown', function (e) {
  if (e.which == 27) {
    killModal();
  }
});

// End global block

// call the initialization function

initMainContainer();


// Begin PJAX related code

var pjax_selected_tab = null; // last clicked pjax content

if ($(document).pjax) {
  $(document).pjax("#nav-article ul li a", "#pjax-container",
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
        if($.pjax.contentCache[window.location.href] === undefined ||
            !$.pjax.contentCache[window.location.href].loaded) {
          onLoadALM();
        }
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
  });
}

// End Pjax related code

$(function() {
  //Stolen from:
  //http://www.vancelucas.com/blog/fixing-ie7-z-index-issues-with-jquery/
  if($.browser.msie && jQuery.browser.version < 10) {
    var zIndexNumber = 1000;
    $('div.sidebar').find('div').each(function() {
      $(this).css('zIndex', zIndexNumber);
      zIndexNumber -= 10;
    });
  }
});

