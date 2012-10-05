var $win = $(window);
var $pagebdy = $('#pagebdy');

$(document).ready(function() {

  $.fn.doOnce = function(func){
    this.length && func.apply(this);
    return this;
  }

  $('input[placeholder]').doOnce(function(){
    this.placeholder();
  });

  $('textarea[placeholder]').doOnce(function(){
    this.placeholder();
  });

  $('#hdr-article p.authors').doOnce(function(){
    this.authorDisplay();
  });

  $pagebdy.find('div.tab-block').doOnce(function(){
    this.tabs();
  });

  $article = $('#article-block').find('div.article').eq(0);

  $('#nav-article-page').doOnce(function(){
    this.buildNav({
      content: $article
    });
  });

  $('#nav-toc').doOnce(function(){
    this.buildNav({
      content: $('#toc-block').find('div.col-2')
    });
  });

  $('#nav-toc').doOnce(function(){
    this.floatingNav({
      sections: $('#toc-block').find('div.section')
    });
  });

  $('#nav-article-page').doOnce(function(){
    this.floatingNav({
      sections: $article.find('div.section')
    });
  });

  $article.doOnce(function(){
    this.scrollFrame();
  });

  $('#figure-thmbs').doOnce(function(){
    this.carousel({
      access : true
    });
  });

});

var $nav_article = $('#nav-article');
if ($nav_article.length) {
  items_l = $nav_article.find('li').length
  $nav_article.addClass('items-' + items_l);
}



var $fig_search = $('#fig-search-block');
if ($fig_search.length) {
  $refine = $('<span id="search-display">Refine</span>').toggle(function() {
    $fig_search.addClass('refine');
    $refine.addClass('open').text('Hide');
  }, function() {
    $fig_search.removeClass('refine');
    $refine.removeClass('open').text('Refine');
  });
  $fig_search.find('div.header').append($refine);
}



(function($){
  $.fn.floatingNav = function(options) {
    defaults = {
      margin : 90,
      sections: ''
    };
    var options = $.extend(defaults, options);
    return this.each(function() {
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
      var positionEl = function() {
        win_top = $win.scrollTop();
        if (
            (win_top > (el_top - options.margin)) //the top of the element is out of the viewport
                && ((el_h + options.margin + bnr_h) < $win.height()) //the viewport is tall enough-
                && (win_top < (ftr_top - (el_h + options.margin))) //the element is not overlapping the footer
                && ($win.width() >= 960) //the viewport is wide enough
            ) {
          $this.css({ 'position' : 'fixed', 'top' : options.margin + 'px' });
          hilite()
        }
        else {
          $this.css({ 'position' : 'static'});
        }
      }
      var hilite = function() {
        (options.sections).each(function() {
          this_sec = $(this);
          if (win_top > (this_sec.offset().top - options.margin)) {
            var this_sec_ref = this_sec.find('a[toc]').attr('toc');
            lnks.closest('li').removeClass('active');
            $this.find('a[href="#' + this_sec_ref + '"]').closest('li').addClass('active');
          }
        });
      }

      var marginFix = function() {
        x = (options.sections).last().offset().top
        y = $(document).height();
        z = (y - x) + options.margin;
        if (z < $win.height()) {
          margin = Math.ceil(($win.height() - z) + options.margin);
          (options.sections).last().css({ 'margin-bottom' : margin + 'px'});
        }
      }

      positionEl();
      marginFix();
      $win.scroll(positionEl);
      $win.resize(positionEl);
    });
  };
})(jQuery);

(function($){
  $.fn.buildNav = function(options) {
    defaults = {
      content : '',
      margin : 70
    };
    var options = $.extend(defaults, options);
    return this.each(function() {
      var $this = $(this);
      var $new_ul = $('<ul class="nav-page" />')
      var $anchors = (options.content).find('a[toc]');
      $anchors.each(function() {
        this_a = $(this);
        title = this_a.attr('title');
        target = this_a.attr('toc');
        new_li = $('<li><a href="#' + target + '" class="scroll">' + title +'</a></li>').appendTo($new_ul);
      });
      $new_ul.find('li').eq(0).addClass('active');
      $new_ul.prependTo($this);
      $this.on("click", "a.scroll", function(event){
        $('html,body').animate({scrollTop:$('[name="'+this.hash.substring(1)+'"]').offset().top - options.margin}, 500);
      });

    });
  };
})(jQuery);



(function($){
  $.fn.scrollFrame = function() {
    return this.each(function() {
      var $hdr = $('#hdr-article');
      var el_top = $hdr.offset().top;
      var el_h = $hdr.innerHeight();
      var ftr_top = $('#pageftr').offset().top;
      var top_open = false;
      var bot_open = false;
      var hdr_view = true;
      var ftr_view = false;
      var speed = 'slow';
      var $btn = $('<div class="btn-g"><img src="/images/logo.plos.95.png" alt="PLOS logo" class="btn-logo"/><a href="#close" class="btn-close">close</a></div>').on('click', function() {
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

      var displayEl = function() {
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

        if (!hdr_view && !top_open) {
          $title.stop()
              .css({ 'top' : '-100px'})
              .animate({
                top: '+=100'
              }, speed);
          top_open = true;
        }
        if (hdr_view && top_open) {
          $title.stop()
              .css({ 'top' : '0px'})
              .animate({
                top: '-=100'
              }, speed);
          top_open = false;
        }
        if (!hdr_view && !ftr_view && !bot_open) {
          $bnr.stop()
              .css({ 'bottom' : '-100px'})
              .animate({
                bottom: '+=100'
              }, speed);
          bot_open = true;
        }
        if ((hdr_view || ftr_view) && bot_open) {
          $bnr.stop()
              .css({ 'bottom' : '0px'})
              .animate({
                bottom: '-=100'
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



(function($){
  $.fn.authorDisplay = function(options) {
    defaults = {
      display : 14
    };
    var options = $.extend(defaults, options);
    return this.each(function() {
      var $this = $(this);
      var $authors = $this.find('span.author');
      if ($authors.length > options.display) {
        overflow = $authors.eq(options.display -2).nextUntil($authors.last());
        overflow.hide();
        $ellipsis = $('<span class="ellipsis"> [ ... ] </span>');
        $authors.eq(options.display -2).after($ellipsis);
        $action = $('<span class="action">, <a>[ view all ]</a></span>').toggle(function() {
              $ellipsis.hide();
              overflow.show();
              $action.html('<span class="action"> <a>[ view less ]</a></span>')
            }, function() {
              overflow.hide();
              $ellipsis.show();
              $action.html('<span class="action"> <a>[ view all ]</a></span>')
            }
        ).insertAfter($authors.last());
      }
    });
  };
})(jQuery);



(function($){
  $.fn.tabs = function() {
    return this.each(function() {
      var $this = $(this);
      var $panes = $(this).find('div.tab-pane');
      var $tab_nav = $(this).find('div.tab-nav');
      var $tab_lis = $tab_nav.find('li');
      $tab_lis.eq(0).addClass('active');
      $panes.eq(0).nextAll('div.tab-pane').hide();
      $tab_nav.on('click', 'a', function(e) {
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


(function($){
  $.fn.carousel = function(options) {
    defaults = {
      speed : 500,
      access : false,
      autoplay : false,
      infinite : true,
      delay : 10000

    };
    var options = $.extend(defaults, options);
    return this.each(function() {
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
      if (options.infinite) {
        // add empty items to last page if needed
        if (($items.length % visible) != 0) {
          empty_items = visible - ($items.length % visible);
          for (i=0; i<empty_items; i++) {
            $slider.append('<div class="item empty" />');
          }
          $items = $slider.find('div.item'); // update
        }

        // clone last page and insert at beginning, clone first page and insert at end
        $items.filter(':first').before($items.slice(-visible).clone()
            .addClass('clone'));
        $items.filter(':last').after($items.slice(0, visible).clone()
            .addClass('clone'));

        $items = $slider.find('div.item'); // update

        // reposition to original first page
        $wrapper.scrollLeft(single_width * visible);
      } else {
        $wrapper.scrollLeft(0);
      }

      function gotoPage(page) {
        var dir = page < current_page ? -1 : 1,
            pages_move = Math.abs(current_page - page),
            distance = single_width * dir * visible * pages_move;

        $wrapper.filter(':not(:animated)').animate({
          scrollLeft : '+=' + distance
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
          .on('click', function() {
            gotoPage(current_page - 1);
          }).appendTo(controls);

      var btn_next = $('<span class="button next" />')
          .on('click', function() {
            gotoPage(current_page + 1);
          }).appendTo(controls);
      controls.appendTo($this);

      if (options.access && ($items.length > visible)) {
        $buttons = $('<div class="buttons" />');
        for (i=1; i<=pages; i++) {
          $('<span>' + i + '</span>').on('click', function() {
            this_but = $(this);
            this_ref = this_but.data('ref');
            gotoPage(this_ref);
          })
              .data('ref',i)
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
        $(window).load(function() {
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


var launchModal = function(doi, ref, state, el) {
  var path = '/article/fetchObject.action?uri=';
  var $modal = $('<div id="fig-viewer" class="modal" />');
  var $thmbs = $('<div id="fig-viewer-thmbs" />');
  var $slides = $('<div id="fig-viewer-slides" />');
  var $mask = $('<div id="modal-mask" />').on('click', function() {
    killModal();
  });
  var active_thmb = null;
  var page_url;
  var buildFigs = function() {
    $.ajax({
    url: '/article/lightbox.action?uri=' + doi,
      dataFilter: function (data, type) {
        return data.replace(/(\/\*|\*\/)/g, '');
      },
      dataType:'json',
      success: function(data){
        page_url = data.URL;
        $.each(data.secondaryObjects, function(){
          title_txt = (this.title ? this.title + '. ' : '')  + this.transformedCaptionTitle;
          $thmb = $('<div class="thmb"'  + ' data-uri="' + this.uri +'"><img src="' + path + this.uri + '&representation=PNG_I' + '" alt="' + title_txt  + '" title="' + title_txt + '"></div>').on('click', function() {
            changeSlide($(this));
          })
          $thmbs.append($thmb);
          slide = $('<div class="slide" />');
          txt = $('<div class="txt" />');
          lnks = $('<div class="lnks" />');
          title = '<span class="title">' + title_txt + '</span>';
          $toggle = $('<span class="toggle">more</span>').toggle(function() {
            $(this).closest('div.txt').addClass('expand');
            $(this).html('<span class="toggle">less</span>');
          }, function() {
            $(this).closest('div.txt').removeClass('expand');
            $(this).html('<span class="toggle">more</span>');
          });
          context_hash = showInContext(this.uri);
          if (el) { // the image is on another page
            context_hash = '/article/' + page_url + context_hash;
          }
          desc = '<div class="desc">' + this.transformedDescription + '</div>';
          img = '<div class="figure" data-img-src="' + path + this.uri + '&representation=' + this.repMedium + '" data-img-txt="' + title_txt + '"></div>'
          lnks_txt = '<p class="dl">Download'
              + ' <a href="' + "/article/" + this.uri + "/powerpoint" + '" class="ppt">PowerPoint slide</a>'
              + ' <a href="' + "/article/" + this.uri + "/largerimage" + '" class="png">larger image (' + displaySize(this.sizeLarge) + ')</a>'
              + ' <a href="' + "/article/" + this.uri + "/originalimage" + '" class="tiff">original image (' + displaySize(this.sizeTiff) + ')</a>'
              + '</p><p>'
              + '<span class="btn active">browse figures</span>'
              + '<span class="btn" onclick="toggleModalState();">view abstract</span>'
              + '<a class="btn" href="' + context_hash + '" onclick="killModal();">show in context</a>'
              + '</p>';
          slide.append(img);
          txt.append(title);
          txt.append($toggle);
          txt.append(desc);
          lnks.append(lnks_txt);
          slide.append(txt);
          slide.append(lnks);
          $slides.append(slide);
        });

        btn_prev = $('<span class="fig-btn prev" />').on('click', function() {
          changeSlide(active_thmb.prev());
        }).appendTo($slides);
        btn_next = $('<span class="fig-btn next" />').on('click', function() {
          changeSlide(active_thmb.next());
        }).appendTo($slides);
        $modal.append($slides);
        $modal.append($thmbs);

        if (ref) {
          $thmbs.find('div[data-uri="' + ref + '"]').trigger('click');
        } else {
          $thmbs.find('div.thmb').eq(0).trigger('click');
        }

        buildAbs();


      }
    });
  }();

  var buildAbs = function() {
    $.ajax({
      //url: 'http://api.plos.org/search?q=doc_type:full%20and%20id:%22' + doi + '&fl=abstract&facet=false&hl=false&wt=json&api_key=plos',
      url: 'article/SOLR/' + doi,
      dataType:'json',
      success: function(data){
        $.each(data.response.docs, function(){
          abstract_html ='<div id="fig-viewer-abst">'
              + '<div class="txt">' + this.abstract + '</div>'
              + '<div class="lnks">'
              + '<p class="dl">Download'
              + ' <a href="' + "/article/" + this.uri + "/pdf" + '" class="pdf">Full Artilce PDF Version</a>'
              + '</p><p>'
              + '<span class="btn" onclick="toggleModalState();">browse figures</span>'
              + '<span class="btn active">view abstract</span>'
              + '<a class="btn" href="' + '/article/' + page_url + '">full text</a>'
              + '</p>'
              + '</div>'
              + '</div>';
          $modal.append(abstract_html);
        });
        displayModal();
      }
    });
  }

  var displayModal = function() {
    $hdr = $('<div class="header" />');
    $hdr_article = $('#hdr-article');

    if ($hdr_article.length) {
      $hdr.append($hdr_article.html());
    } else if (el) {
      kicker = el.find('.article-kicker').clone();
      h1 = '<h1>' + el.find('.article a').text() + '</h1>';
      authors = el.find('.authors').clone();
      $hdr.append(kicker);
      $hdr.append(h1);
      $hdr.append(authors);
    }
    $close = $('<span class="close" />').on('click', function() {
      killModal();
    });
    $hdr.append($close);
    $modal.prepend($hdr);
    if (state == 'abstract') {
      $modal.addClass('abstract');
    }

    $('body').append($modal)
        .append($mask);
    resizeModal();
    $win.bind('resize.modal', resizeModal);

  }

  var changeSlide = function(thmb) {
    $all_thmb = $thmbs.find('div.thmb');
    $all_sld = $slides.find('div.slide');
    $all_sld.hide();
    this_sld = $all_sld.eq($all_thmb.index(thmb));
    $fig = this_sld.find('div.figure');
    if ($fig.is('[data-img-src]')) {
      src = $fig.data('img-src');
      txt = $fig.data('img-txt');
      img ='<img src="' + src + '" title="' + txt + '" alt="' + txt + '">';
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

  }

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
  }

  var showInContext = function(uri) {
    uri = uri.split('/');
    uri = uri.pop();
    uri = uri.split('.');
    uri = uri.slice(1);
    uri = uri.join('-');
    return '#' + uri;
  }




  var resizeModal = function() {
    var doc_h = $(document).height();
    var win_h = $win.height();
    var win_w = $win.width();
    var modal_h = $modal.height();
    $mask.css({'width':win_w,'height':doc_h});
    if (win_h >= modal_h) {
      $modal.css('top',  win_h/2 - modal_h/2);
    } else {
      $modal.css('top', 0);
    }
    $modal.css('left', win_w/2 - $modal.width()/2);
  }
}


var $figure_thmbs = $('#figure-thmbs');
if ($figure_thmbs.length) {
  $figure_thmbs.find('.item a').on('click', function(e) {
    e.preventDefault();
    doi= $(this).data('doi');
    ref= $(this).data('uri');
    launchModal(doi, ref, 'fig');
  });
  $wrap = $figure_thmbs.find('div.wrapper');
  $fig_tog = $('<span>Hide Figures</span>').toggle(function() {
        $wrap.hide();
        $figure_thmbs.find('div.buttons').hide();
        $figure_thmbs.find('div.controls').hide();
        $fig_tog.html('Show Figures')
            .toggleClass('hide');
      }, function() {
        $wrap.show();
        $figure_thmbs.find('div.buttons').show();
        $figure_thmbs.find('div.controls').show();
        $fig_tog.html('Hide Figures')
            .toggleClass('hide');
      }
  ).insertAfter($figure_thmbs)
      .wrap('<div id="fig-toggle" class="cf" />');
}

var $fig_inline = $('#article-block').find('div.figure');
if ($fig_inline.length) {
  $lnks = $fig_inline.find('.img a');
  $lnks.on('click', function(e) {
    e.preventDefault();
    ref= $(this).data('uri');
    doi= $(this).data('doi');
    launchModal(doi, ref, 'fig');
  });
  $lnks.append('<div class="expand" />');
}

var $fig_results = $('#fig-search-results');
if ($fig_results.length) {
  $fig_results.find('a.figures').on('click', function() {
    doi= $(this).data('doi');
    el = $(this).closest('ul').closest('li');
    launchModal(doi, null, 'fig', el);
  });
  $fig_results.find('a.abstract').on('click', function() {
    doi= $(this).data('doi');
    el = $(this).closest('ul').closest('li');
    launchModal(doi, null, 'abstract', el);
  });
}


var $nav_figs = $('#nav-figures a');
if ($nav_figs.length) {
  $nav_figs.on('click', function() {
    doi= $(this).data('doi');
    launchModal(doi, null, 'fig');
  });
}


var killModal = function(){
  $('div.modal').remove();
  $('#modal-mask').remove();
  $win.unbind('resize.modal');
}

var toggleModalState = function() {
  $('#fig-viewer').toggleClass('abstract');
}
