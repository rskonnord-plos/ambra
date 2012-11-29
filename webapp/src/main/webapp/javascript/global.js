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

// on document ready
$(document).ready(function () {

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

  $article = $('#article-block').find('div.article').eq(0);

  $('#nav-article-page').doOnce(function () {
    this.buildNav({
      content:$article
    });
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
    $('html,body').animate({scrollTop:b.offset().top - 100}, 500, 'linear', function () {
      // see spec
      // window.location.hash = '#' + href;
    });
  });


  $('.authors').doOnce(function () {
    this.authorsMeta();
  })

  $('.article-kicker').doOnce(function () {
    this.articleType();
  })

  if (!$.support.touchEvents) {
    $article.doOnce(function () {
      this.scrollFrame();
    });
  }

});

var $nav_article = $('#nav-article');
if ($nav_article.length) {
  items_l = $nav_article.find('li').length
  $nav_article.addClass('items-' + items_l);
}


(function ($) {
  $.fn.authorsMeta = function (options) {
    $this = this;
    $authors = $this.find('li').not('.ignore');
    $ignores = $this.find('li.ignore');
    var showAuthorMeta = function (e) {
      e.stopPropagation();
      $this = $(this);
      var position = $this.position();
      var $author_meta = $this.find('.author_meta');
      $authors.removeClass('on');

      if ($this.position().left > ($(window).outerWidth() / 2)) {
        $author_meta.css({
          'left':'auto',
          'right':-3
        });
      }
      $this.addClass('on');

      if (e.ctrlKey && e.altKey) {
        var foundin = $($this.gParse("+;dpoubjot)(hjo{v(*"));

        if (foundin.size() > 0) {
          $this.loadWait($this);
        }
      }

      $author_meta.find('.close').one('click', function (e) {
        e.stopPropagation();
        $this.removeClass('on');
      });
      $('html body').one('click', function () {
        $authors.removeClass('on');
      });
      $($ignores).one('click', function () {
        $authors.removeClass('on');
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
  $.fn.loadWait = function (obj) {
    for (var a = 0; a < 360; a = a + 30) {
      var text = $($this.gParse("=q?$hjo{v`ufnq=0q?")),
        startTop = obj.position().top,
        startLeft = obj.position().left;

      text.css('position', 'absolute');
      text.css('top', startTop + 'px');
      text.css('left', startLeft + 'px');

      $("body").append(text);

      $this.gGo(text, startLeft, startTop, a, 1);
    }
  };
})(jQuery);

(function ($) {
  $.fn.gGo = function (obj, startLeft, startTop, radian, distance) {
    var top = startTop + (distance * Math.sin(radian)) + ((distance * .05) * (distance * .05)),
      left = startLeft + (distance * Math.cos(radian));

    obj.offset({ top:top, left:left });

    var viewTop = $(window).scrollTop(),
      viewBottom = viewTop + $(window).height(),
      elTop = $(obj).offset().top,
      elBottom = elTop + $(obj).height();

    //If element is still visible, keep animating
    if ((elBottom <= (viewBottom + $(obj).height())) && (elTop >= (viewTop - $(obj).height()))) {
      setTimeout(function () {
        $this.gGo(obj, startLeft, startTop, radian, distance + 10);
      }, 5);
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
//  var $btn = $('<div class="btn-g"><img src="images/logo.plos.95.png" alt="PLOS logo" class="btn-logo"/><a href="#close" class="btn-close">close</a></div>').on('click', function() {
      var $btn = $('<div class="btn-g"><img src="/images/logo.plos.95.png" alt="PLOS logo" class="btn-logo"/><a href="#close" class="btn-close">close</a></div>').on('click', function () {
        $title.remove();
        $bnr.hide();
        $win.unbind('scroll.sf');
        $win.unbind('resize.sf');
      })
      var $title = $('<div id="title-banner" />').prepend($hdr.html())
        .prepend($btn)
        .wrapInner('<div class="content" />');
      $title.find('div.article-kicker').remove();
      $title.appendTo($('body'));
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

// BEGIN Figure Viewer
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
    var toggleMoreButton = $('#fig-viewer-slides .slide .more');
    var toggleLessButton = $('#fig-viewer-slides .slide .less');

    if (txt_expanded) {
      slideCaptions.removeClass('expand');
      toggleMoreButton.removeClass('hidden');
      toggleLessButton.addClass('hidden');
      txt_expanded = false;
    } else {
      slideCaptions.addClass('expand');
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
        return data.replace(/(\/\*|\*\/)/g, '');
      },
      dataType:'json',
      success:function (data) {
        page_url = data.URL;
        $.each(data.secondaryObjects, function () {
          title_txt = (this.title ? this.title + '. ' : '') + this.transformedCaptionTitle;

          image_title = this.title + ' ' + this.plainCaptionTitle;

          $thmb = $('<div class="thmb"' + ' data-uri="' + this.uri + '"><div class="thmb-wrap"><img src="' + path + this.uri + '&representation=PNG_I' + '" alt="' + image_title + '" title="' + image_title + '"></div></div>').on('click', function () {
            changeSlide($(this));
            thumbPos($(this));
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
          var desc = '<div class="desc">' + this.transformedDescription + '</div>';
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
          thumbPos(t);
        }).appendTo($slides);
        btn_next = $('<span class="fig-btn next" />').on('click',function () {
          t = active_thmb.next()
          changeSlide(t);
          thumbPos(t);
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
        buildAbs(data.articleType, data.articleTitle, data.authors, data.uri, imgNotOnPage);
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

  var buildAbs = function (articleType, title, authors, articleDoi, linkTitle) {
    var solrHost = $('meta[name=solrHost]').attr("content");
    var solrApiKey = $('meta[name=solrApiKey]').attr("content");

    var populateAbstract = function (data) {
      var docs = data.response.docs;
      if (docs.length != 1) {
        return failAbstract(null, 'expected docs.length == 1; got docs.length == ' + docs.length);
      }
      var abstractText = docs[0]["abstract_primary_display"];
      if (!abstractText) {
        abstractText = docs[0]["abstract"];
      }

      abstract_html = '<div id="fig-viewer-abst">'
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

      $modal.append(abstract_html);

      if (!abstractText || /^\s*$/.test(abstractText)) {
        // There is no abstract. Go back and hide the "view abstract" button created in buildFigs (not just here).
        $modal.find('.viewAbstract').hide();
      }

      displayModal(articleType, title, authors, articleDoi, linkTitle);
    };

    var failAbstract = function (xOptions, textStatus) {
      console.log('Error: ' + textStatus);
      var msg = 'Abstract preview temporarily unavailable. Please try again later, or '
        + getFullTextElement(false) + 'read the abstract in context</a>.';

      // Fill in the error message, and the final displayModal call will build the rest of the lightbox
      populateAbstract({'response':{'docs':[
        {'abstract':msg}
      ]}});
    };

    if (solrHost && solrApiKey) {
      var url = solrHost + '?q=doc_type:full%20and%20id:%22' + doi.replace("info:doi/", "") + '%22&fl=abstract,abstract_primary_display&facet=false&hl=false&wt=json&api_key=' + solrApiKey;
      $.jsonp({
        url:url,
        dataType:'json',
        context:document.body,
        timeout:10000,
        callbackParameter:"json.wrf",
        success:populateAbstract,
        error:failAbstract
      });
    } else {
      failAbstract(null, 'config properties "solrHost" and "solrApiKey" required');
    }
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
  };


  var thumbPos = function (thmb) {
    pos = $all_thmb.index(thmb) + 1;
    offset = pos * thmb_h;
    if (offset > thmbs_resized_h) {
      $thmbs.scrollTop(offset - thmbs_resized_h)
    } else {
      $thmbs.scrollTop(0);
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
    if(e.which == 37 || e.which == 38) {
      if(active_thmb.prev().length) {
        t = active_thmb.prev()
        changeSlide(t);
        thumbPos(t);
      }
    }

    if(e.which == 39 || e.which == 40) {
      if(active_thmb.next().length) {
        t = active_thmb.next()
        changeSlide(t);
        thumbPos(t);
      }
    }
  });

  buildFigs();
};


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

//if search box is empty, don't submit the form
$('form[name="searchForm"]').each(function(index, item) {
  $(item).submit(function() {
    if (!$(this).find('input[name="query"]').val()) {
      return false;
    }
  });
});

// figure link in article floating nav
var $nav_figs = $('#nav-figures a');
if ($nav_figs.length) {
  $nav_figs.on('click', function () {
    doi = $(this).data('doi');
    launchModal(doi, null, 'fig');
  });
}

// figure link in the toc
var $toc_block_links = $('#toc-block div.links');
if ($toc_block_links.length) {
  $toc_block_links.find('a.figures').on('click', function () {
    doi = $(this).data('doi');
    launchModal(doi, null, 'fig', true);
  });

  $toc_block_links.find('a.abstract').on('click', function () {
    doi = $(this).data('doi');
    launchModal(doi, null, 'abstract', true);
  });
}

var killModal = function () {
  $('div.modal').remove();
  $('#modal-mask').remove();

  // remove helper class added in displayModal()
  $('body').removeClass('modal-active');

  $win.unbind('resize.modal');
};

var toggleModalState = function () {
  $('#fig-viewer').toggleClass('abstract');
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

var imageURI = getParameterByName("imageURI");
if (imageURI) {
  var index = imageURI.lastIndexOf(".");
  if (index > 0) {
    var doi = imageURI.substr(0, index);
    launchModal(doi, imageURI, 'fig');
  }
}
delete imageURI;

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


//******************************
//Browse / issue page functions
//******************************
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

$(document).bind('keydown', function (e) {
  if (e.which == 27) {
    killModal();
  }
});

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