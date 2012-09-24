
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
	
	$('#nav-article-page').doOnce(function(){
		this.floatingNav({
			sections: $article.find('div.section')
		});
	});
	
	$('#hdr-article').doOnce(function(){
		this.scrollFrame();
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
			var bnr_h = $('#banner-ftr').innerHeight();
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
			var $this = $(this);
			var el_top = $this.offset().top;
			var el_h = $this.innerHeight();
			var ftr_top = $('#pageftr').offset().top;
			var top_open = false;
			var bot_open = false;
			var hdr_view = true;
			var ftr_view = false;
			var speed = 'slow';
			var $btn = $('<div class="btn">close</div>').on('click', function() {
				$new_el.remove();
				$bnr.hide();
				$win.unbind('scroll.sf');
				$win.unbind('resize.sf');			
			})
			var $title = $('<div id="title-banner" />').prepend($this.html())
			.prepend($btn)
			.wrapInner('<div class="content" />')
			.appendTo($('body'));
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
				$ellipsis = $('<span class="ellipsis"> [ ... ], </span>');
				$authors.eq(options.display -2).after($ellipsis);
				$action = $('<span class="action">, <a>[ view all ]</a></span>').toggle(function() {
					$ellipsis.hide();
					overflow.show();
					$action.html('<span class="action">, <a>[ view less ]</a></span>')
				}, function() {
					overflow.hide();
					$ellipsis.show();
					$action.html('<span class="action">, <a>[ view all ]</a></span>')
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


