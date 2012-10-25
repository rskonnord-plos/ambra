
var $win = $(window);
var $pagebdy = $('#pagebdy');

$(document).ready(function() { 
	
	// detect touch screen
	$.support.touchEvents = (function(){
		return (('ontouchstart' in window) || window.DocumentTouch && document instanceof DocumentTouch);
	})();
		
	if ($.support.touchEvents) {
		$('html').addClass('touch');
	}

	$.fn.doOnce = function(func){ 
		this.length && func.apply(this); 
		return this; 
	}
	
	$('#nav-main').doOnce(function(){
		this.navmain();
	});
	
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
			sections: $article.find('a[toc]').closest('div')
		});
	});
	
	
	$('#figure-thmbs').doOnce(function(){
		this.carousel({
			access : true
		});
	});
	
	$('#article-block').find('div.btn-reveal').doOnce(function(){
		this.hoverEnhanced({
			trigger: 'span.btn'
		});
	});

	$('.article a[href^="#"]').on('click', function(e){
		e.preventDefault();
		var href = $(this).attr('href').split('#')[1];
		var b = $('a[name="' + href + '"]');
		$('html,body').animate({scrollTop:b.offset().top - 100}, 500, 'linear', function(){
			// see spec
			// window.location.hash = '#' + href;
		});
	});

	if (!$.support.touchEvents) {
		$article.doOnce(function(){
			this.scrollFrame();
		});
	}

});

var $nav_article = $('#nav-article');
if ($nav_article.length) {
	items_l = $nav_article.find('li').length
	$nav_article.addClass('items-' + items_l);
}


// search filter
var $hdr_search = $('#hdr-search-results');
if ($hdr_search.length) {
	var $srch_facets = $('#search-facets');
	var $facets = $srch_facets.find('.facet');
	var $menu_itms = $srch_facets.find('div[data-facet]');
	$menu_itms.each(function() {
		$this = $(this);
		ref = $this.data('facet');
		if ($('#' + ref).length == 0) {
			$this.addClass('inactive');
		}
	})
	$menu_itms.on('click', function() {
		$this = $(this);
		if ($this.hasClass('active') || $this.hasClass('inactive')) { return false; }
		$menu_itms.removeClass('active');
		$facets.hide();
		$facets.find('dl.more').hide();
		$facets.find('.view-more').show();
		$this.addClass('active');
		ref = $this.data('facet');
		$('#' + ref).show();
	});
	$chkbxs = $srch_facets.find(':checkbox');
	$chkbxs.each(function() {
		chkbx = $(this);
		if (chkbx.prop('checked')) {
			chkbx.closest('dd').addClass('checked');
		}
	})
	$chkbxs.on('change', function() {
		chkbx = $(this);
		if (chkbx.prop('checked')) {
			chkbx.closest('dd').addClass('checked');
		} else {
			chkbx.closest('dd').removeClass('checked');
		}
	});
	
	
	$srch_facets.find('.view-more').on('click', function() {
		$(this).hide()
		.closest('div.facet').find('dl.more').show();
	});
	$srch_facets.find('.view-less').on('click', function() {
		this_facet = $(this).closest('div.facet');
		this_facet.find('dl.more').hide();
		this_facet.find('.view-more').show();
	});
	
	$('#startDateAsStringId').datepicker({
		changeMonth: true,
		changeYear: true,
		maxDate: 0,
		dateFormat: "yy-mm-dd",
		onSelect: function( selectedDate ) {
			$('#endDateAsStringId').datepicker('option', 'minDate', selectedDate );
		}
	});
	$('#endDateAsStringId').datepicker({
		changeMonth: true,	
		changeYear: true,	
		maxDate: 0,
		dateFormat: "yy-mm-dd",
		onSelect: function( selectedDate ) {
			$('#startDateAsStringId').datepicker('option', 'maxDate', selectedDate );
		}
	});
	
	$toggle_filter = $('<div class="toggle btn">filter by &nbsp;+</div>').toggle(function() {
		$srch_facets.show();
		$toggle_filter.addClass('open');
	}, function() {
		$srch_facets.hide();
		$toggle_filter.removeClass('open');
	}
	).prependTo($hdr_search.find('div.options'));
	
	$('#sortPicklist').uniform();
}



(function($){
	$.fn.navmain = function() {	
		return this.each(function() {
			var $this = $(this);
			$submenu_parents = $this.find('div.submenu').closest('li');
			var vis = null;

			var showMenu = function() {
				if (vis !== null) {
					vis.removeClass('hover');
				}
				$(this).addClass('hover');
				vis = $(this)
			}
			var hideMenu = function() {
				$(this).removeClass('hover');
			}

			var config = {    
				over: showMenu, 
				timeout: 500, 
				out: hideMenu 

			};
			$submenu_parents.hoverIntent(config);

		});
	};
})(jQuery);
	
	
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
					// && (win_top < (ftr_top - (el_h + options.margin))) //the element is not overlapping the footer
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
				var link = $(this);
				event.preventDefault();
				$('html,body').animate({scrollTop:$('[name="'+this.hash.substring(1)+'"]').offset().top - options.margin}, 500, function(){
					// see spec
					// window.location.hash = link.attr('href');
				});
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
			var $btn = $('<div class="btn-g"><img src="images/logo.plos.95.png" alt="PLOS logo" class="btn-logo"/><a href="#close" class="btn-close">close</a></div>').on('click', function() {
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
				if ($win.width() < 960) {
					if (top_open) {
						$title.stop()
						.css({ 'top' : '-100px'});
						top_open = false;
					}
					if (bot_open) {
						$bnr.stop()
						.css({ 'bottom' : '-100px'});
						bot_open = false;
					}
					return false;
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
	$.fn.hoverEnhanced = function(options) {	
		defaults = {
			trigger : ''
		};
		var options = $.extend(defaults, options);
		return this.each(function() {
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
				$this.find(options.trigger).on('click', function() {
					$this.siblings().removeClass('reveal');
					$this.toggleClass('reveal');
				})
			}
		});
	};
})(jQuery);



(function($){
	$.fn.carousel = function(options) {	
		defaults = {
			speed : 500,
			access : false,
			autoplay : false,
			delay : 10000,
			defaultpaddingbottom : 10
			
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
				if ($items.length <= visible) { 
					$wrapper.css('paddingBottom', options.defaultpaddingbottom );
					$wrapper.scrollLeft(0);
					return false; 
				}

				// add empty items to last page if needed
				if ($items.length % visible) {
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
				
				if ($this.hasClass('carousel-videos')) {
					$slider.find('div.clone').each(function() {
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
})(jQuery)



// advanced search
var $adv_search = $('#unformattedSearchFormId');
if ($adv_search.length) {
	
	$('#startDateAsStringId').datepicker({
		changeMonth: true,
		changeYear: true,
		maxDate: 0,
		dateFormat: "yy-mm-dd",
		onSelect: function( selectedDate ) {
			$('#endDateAsStringId').datepicker('option', 'minDate', selectedDate );
		}
	});
	$('#endDateAsStringId').datepicker({
		changeMonth: true,	
		changeYear: true,	
		maxDate: 0,
		dateFormat: "yy-mm-dd",
		onSelect: function( selectedDate ) {
			$('#startDateAsStringId').datepicker('option', 'maxDate', selectedDate );
		}
	});
	
	var $query_field = $('#queryFieldId');
	var $term_el = $('#queryTermDivBlockId');
	var $term = $('#queryTermId');
	var $date_el = $('#startAndEndDateDivBlockId');
	var $date_start = $('#startDateAsStringId');
	var $date_end = $('#endDateAsStringId');
	var $query = $('#unformattedQueryId');
	var is_date;
	var isDate = function(field) {
		if (field.val() == 'publication_date' || field.val() == 'received_date' || field.val() == 'accepted_date') {
			is_date = true;
		} else	 {
			is_date = false;
		}
	}
	var changeField = function() {
		if (is_date && $date_el.is(':hidden')) {
			$term_el.hide();
			$date_el.show();
			$term.prop('disabled', true);
			$date_start.prop('disabled', false);
			$date_end.prop('disabled', false);
		} else if (!is_date && $date_el.is(':visible')) {
			$term_el.show();
			$date_el.hide();
			$term.prop('disabled', false);
			$date_start.prop('disabled', true);
			$date_end.prop('disabled', true);
		}
	}
	$query_field.change(function() {
		isDate($(this));
		changeField();
		 
	});
	isDate($query_field);
	changeField();
	
	
	var journals_all = $('#journalsOpt_all');
	var subject_all = $('#subjectOption_all');
	var article_all = $('#articleType_all');
	var disableFormEls = function(el) {
		inpts = el.closest('ol').find('.options input');
		inpts.prop('disabled', true);
	}
	var enableFormEls = function(el) {
		inpts = el.closest('ol').find('.options input');
		inpts.prop('disabled', false);

	}
	journals_all.change(function() {
		disableFormEls($(this));
	});
	$('#journalsOpt_slct').change(function() {
		enableFormEls($(this));
	});

	subject_all.change(function() {
		disableFormEls($(this));
	});
	$('#subjectOption_some').change(function() {
		enableFormEls($(this));
	});

	article_all.change(function() {
		disableFormEls($(this));
	});
	$('#articleType_one').change(function() {
		enableFormEls($(this));
	});
	if (journals_all.is(':checked')) {
		disableFormEls(journals_all);
	}
	if (subject_all.is(':checked')) {
		disableFormEls(subject_all);
	}
	if (article_all.is(':checked')) {
		disableFormEls(article_all);
	}
	
	
	var updateQuery = function() {
		var conj = $(this).val();
		var term_v = $.trim($term.val());
		var query_type = $query_field.val();
		var date_s_v = $date_start.val();
		var date_e_v = $date_end.val();
		var query_v = $query.val();
		var q_string;
		if (is_date) {
			if (date_s_v.length < 1) {
				alert('Please enter a Start Date in the left-hand date field');
				return false;
			}
			if (date_e_v.length < 1) {
				alert('Please enter an End Date in the right-hand date field');
				return false;
			}
			q_string = '[' + date_s_v + 'T00:00:00Z TO ' + date_e_v + 'T23:59:59Z] ';
		} else {
			if (term_v.length < 1) {
				alert('Please enter a Search Term in the text field next to the picklist');
				return false;
			}
			if (term_v.match(/\s/)) {
				q_string = '"' + term_v + '"';
			} else {
				q_string = term_v;
			}
			$term.val('');
		}
		if (query_v.length) {
			q = '(' + query_v + ') ' + conj + ' ' + query_type + ':' + q_string;
		} else if (conj == 'NOT') {	
			q = conj + ' ' + query_type + ':' + q_string;
		} else {		
			q = query_type + ':' + q_string;
		}
		$query.val(q);
	}	
	
	$('#queryConjunctionAndId').on('click', updateQuery);
	$('#queryConjunctionOrId').on('click', updateQuery);
	$('#queryConjunctionNotId').on('click', updateQuery);
	
		
	$('#clearFiltersButtonId2').on('click', function() {
		journals_all.prop('checked', true);
		subject_all.prop('checked', true);
		article_all.prop('checked', true);
		disableFormEls(journals_all);
		disableFormEls(subject_all);
		disableFormEls(article_all);
	});
	$('#clearUnformattedQueryButtonId').on('click', function() {
		$query.val('')
	});
}



	
// BEGIN Figure Viewer
var launchModal = function(doi, ref, state, el) {
	var path = '/article/fetchObject.action?uri='
	//var path = 'article/';
	var $modal = $('<div id="fig-viewer" class="modal" />');
	var $thmbs = $('<div id="fig-viewer-thmbs" />');
	var $slides = $('<div id="fig-viewer-slides" />');
	var $abstract = $('<div id="fig-viewer-abst" />');
	var $mask = $('<div id="modal-mask" />').on('click', function() {
		killModal();
	});
	var active_thmb = null;
	var page_url;
	var modal_h;
	var slides_h;
	var $figs;
	var figs_h;	
	var thmbs_h;
	var abst_h;
	var $abs_txt;
	var abs_txt_h;
	var buildFigs = function() {
		$.ajax({
			url: '/article/lightbox.action?uri='+ doi,
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
					+ ' <div class="icon">PPT</div> <a href="' + "/article/" + this.uri + "/powerpoint" + '" class="ppt">PowerPoint slide</a>'
					+ ' <div class="icon">PNG</div> <a href="' + "/article/" + this.uri + "/largerimage" + '" class="png">larger image (' + displaySize(this.sizeLarge) + ')</a>'
					+ ' <div class="icon">TIFF</div> <a href="' + "/article/" + this.uri + "/originalimage" + '" class="tiff">original image (' + displaySize(this.sizeTiff) + ')</a>'
					+ '</p><p>'
					+ '<span class="btn active">browse figures</span>'
					+ '<span class="btn" onclick="toggleModalState();">view abstract</span>'
					+ '<a class="btn" href="' + context_hash + '" onclick="killModal();">show in context</a>'
					+ '</p>';
					slide.append(img);
					txt.append(title);
					// only append the toggle + description if the description isn't blank
					if (!/^\s*$/.test(this.transformedDescription)) {
						txt.append($toggle);
						txt.append(desc);
					}
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
		$.jsonp({
			url: 'http://api.plos.org/search?q=doc_type:full%20and%20id:%22' + doi + '%22' + '&fl=abstract&facet=false&hl=false&wt=json&api_key=plos',
			dataType:'json',
			context: document.body,
			timeout: 10000,
			callbackParameter: "json.wrf",
			success: function(data){
				$.each(data.response.docs, function(){
					abstract_html ='<div id="fig-viewer-abst">'
					+ '<div class="txt"><p>' + this["abstract"] + '</p></div>'
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
			},
			error:function(xOptions, textStatus) {
				console.log('Error: ' + textStatus);
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
		modal_h = $modal.height();
		slides_h =$slides.height();
		$figs = $slides.find('div.figure');
		figs_h = $figs.eq(0).height();
		thmbs_h = $thmbs.height();
		abst_h = $abstract.height();
		$abs_txt = $abstract.find('div.txt');
		abs_txt_h = $abs_txt.height();
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
		$mask.css({'width':win_w,'height':doc_h});
		if (win_h >= modal_h) {
			$modal.css('top',  Math.round(win_h/2 - modal_h/2));
			$slides.css('height', slides_h);
			$figs.css('height', figs_h);
			$thmbs.css('height', thmbs_h);
			$abstract.css('height', abst_h);
			$abs_txt.css('height', abs_txt_h);
		} else {
			$modal.css('top', 0);
			$slides.css('height', win_h - (modal_h - slides_h));
			$figs.css('height', win_h - (modal_h - figs_h));
			$thmbs.css('height', win_h - (modal_h - thmbs_h));
			$abstract.css('height', win_h - (modal_h - abst_h));
			$abs_txt.css('height', win_h - (modal_h - abs_txt_h));
		}
		$modal.css('left', Math.round(win_w/2 - $modal.width()/2));
	}
}


var $figure_thmbs = $('#figure-thmbs');
if ($figure_thmbs.length) {
	$lnks = $figure_thmbs.find('.item a');
	$wrap = $figure_thmbs.find('div.wrapper');
	if ($lnks.length) {
		$lnks.on('click', function(e) {
			e.preventDefault();		
			doi= $(this).data('doi');
			ref= $(this).data('uri');
			launchModal(doi, ref, 'fig');
		});
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
	} else {
		$figure_thmbs.addClass('collapse');
	}
}


// inline figures
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


// figure search results
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

// figure link in article floating nav
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

function getParameterByName(name)
{
  name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
  var regexS = "[\\?&]" + name + "=([^&#]*)";
  var regex = new RegExp(regexS);
  var results = regex.exec(window.location.search);
  if(results == null)
    return "";
  else
    return decodeURIComponent(results[1].replace(/\+/g, " "));
}

var imageURI = getParameterByName("imageURI");
if(imageURI){
  var index = imageURI.lastIndexOf(".");
  if (index > 0) {
    var doi = imageURI.substr(0, index);
    launchModal(doi, imageURI, 'fig');
  }
}
delete imageURI;

//Stolen from:
//http://stackoverflow.com/questions/149055/how-can-i-format-numbers-as-money-in-javascript
Number.prototype.format = function(c, d, t){
  var n = this, c = isNaN(c = Math.abs(c)) ? 2 : c, d = d == undefined ? "," : d, t = t == undefined ? "." :
    t, s = n < 0 ? "-" : "", i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "", j = (j = i.length) > 3 ? j % 3 : 0;
  return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d +
    Math.abs(n - i).toFixed(c).slice(2) : "");
};