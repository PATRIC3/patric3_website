
// usage: log('inside coolFunc', this, arguments);
// paulirish.com/2009/log-a-lightweight-wrapper-for-consolelog/
window.log = function(){
  log.history = log.history || [];   // store logs to an array for reference
  log.history.push(arguments);
  if(this.console) console.log( Array.prototype.slice.call(arguments) );
};

// place any jQuery/helper plugins in here, instead of separate, slower script files.

/**
 * Resizes floated image containers to the size of the image
 */
$.fn.imageWidth = function(threshold) {
	/**
	 * Function takes a jquery object and a css property (called dimension) and determines how many pixels the item is
	 * @param {jQuery Object} $item The jQuery object we're looking at
	 * @param {String} dimension The CSS property name to look for
	 * @returns The width or height of the CSS property with 'px' removed
	 * @type Number
	 */
	var	determineDimension = function($item, dimension) {
		$item = $($item);
		if ($item.css(dimension)) {
			return parseInt($item.css(dimension).replace('px', ''), 10);
		} else {
			return 0;
		};
		return false;
	};
	
	var	resizeImage = function($image, $parent, $container) {
		// Determine the width of the image along with borders and padding
		var imageWidth = $image.width();
		var paddingLeft = determineDimension($image, 'padding-left');
		var paddingRight = determineDimension($image, 'padding-right');
		var borderLeft = determineDimension($image, 'border-left-width');
		var borderRight = determineDimension($image, 'border-right-width');

		// Calculate total edge (padding and border) width and the total width (edge and image)
		var edgeWidth = paddingLeft + paddingRight + borderLeft + borderRight;
		var totalWidth = imageWidth + edgeWidth;

		// Determine parent width
		var parentWidth = $parent.width();

		// If the image is greater then the threshold times the parent width resize the image and the container width
		// Otherwise set the image left's div to the size of the image plus the edge
		if ((threshold * parentWidth) <= totalWidth) {
			var revisedWidth = parentWidth * threshold;
			var revisedImageWidth = revisedWidth - edgeWidth;

			$image.width(revisedImageWidth);
			$container.width(parseInt(revisedWidth, 10));
		} else {
			$container.width(totalWidth);
		};
	};

	return this.each(function() {
		// Threshold is the maximum width an image plus its border and padding can be 
		// in relation to its parent container
		var threshold = (threshold) ? threshold : 2/3;

		// Find image within div
		var $image = $('img', $(this));
		var $parent = $(this).parent();
		var $container = $(this);

		$image.each(function(index) {
			resizeImage($image, $parent, $container);
			$(this).load(function() {			
				resizeImage($image, $parent, $container);
			});
		});
	});
};

/**
 * Set child elements in selected container to height of the tallest child
 */
$.fn.sameHeight = function() {
	return this.each(function() {
		tallest_height = 0;
		$(this).children().each(function(index) {
			tallest_height = ($(this).height() > tallest_height) ? $(this).height() : tallest_height;
		});
	
		if ($.browser != undefined && $.browser.msie == true && $.browser.version <= 6) {
			$(this).children().css('height', tallest_height);
		} else {
			$(this).children().css('min-height', tallest_height);
		};
	});		
};


/**
 * prep markup for styling
 */
var markup = function() {
	// section title headers - wrap to create blue bg
	$('.section-title').wrapInner('<span class="wrap">');

	// striping for long lists
	$('.long-list li:nth-child(odd)').addClass('alt');
	
	// striping for tables with .stripe class
	$('table.stripe tr:nth-child(odd)').addClass('alt');
	
	// last item in a list
	$('ul li:last-child').addClass('last');
	
	// first item in a list
	$('ul li:first-child').addClass('first');

	// last td in row
	$('tbody tr td:last-child').addClass('last');
	
	// first td in row
	$('tbody tr td:first-child').addClass('first');
	
	// last column in a set of columns
	$('.column:last-child').addClass('last');
	
	// match height of tabs to tallest tab
	function tabsHeight() {
		tallest_height = 0;
		$('.tabs .tab-headers li a span').each(function() {
			tallest_height = ($(this).outerHeight(true) > tallest_height) ? $(this).outerHeight(true) : tallest_height;
		});
		
		$('.tabs .tab-headers li a span').each(function() {
			$(this).css('height', tallest_height);
		});
		
	};
	
	tabsHeight();
};

/**
 * Initializes drop-down submenus in main navigation
 */
var subMenu = function() {
	$('.main-nav li').mouseenter(function(event) {
		event.preventDefault();
		$(this).children('.submenu').show();
		$(this).addClass('hover');
	});
	
	$('.main-nav li').mouseleave(function(event) {
		event.preventDefault();
		$(this).children('.submenu').hide();
		$(this).removeClass('hover');
	});
};

/**
 * Adds active classes for group mouseover on homepage "browse data" elements
 */
var browseData = function() {
	$('.browse-data li').bind('mouseover', function() {
		$('.browse-data li').removeClass("active");
		$(this).addClass("active");
	});
	
	$('.browse-data li').bind('mouseout', function() {
		$('.browse-data li').removeClass("active");
	});
};


/**
 * set heights for image overlays
 * if element has the class .img-height, use the image's height
 * otherwise, use the other child element
 */
$.fn.imgOverlay = function() {
	return this.each(function() {
		if ($(this).hasClass("img-height")) {
			myHeight = $(this).children('img').height();
			// set other element to vertically center - needs better math!
			myPos = (myHeight / 2) / 2;
			$(this).children(':not(img)').css('marginTop',myPos);
		} else {
			myHeight = $(this).children(':not(img)').outerHeight(true);
		}
		myHeight += 'px';
		$(this).css('height', myHeight);
	});
};

/* workflow slideshow - using jcarousel */
function initwfCarousel() {
	$('.workflow ul.carousel-container').jcarousel({
		itemFallbackDimension: 905,
		scroll: 1,
		visible: 1,
		buttonNextHTML: null,
		buttonPrevHTML: null,
		initCallback: workflowInitCallback,
		setupCallback: wfInit
	});
};

/**
 * workflow setup
 * calls the set heights function, if images are loaded. prevents incorrect height calculation.
 */
var wfInit = function() {

	// window.setTimeout(wfSetHeights, 1000, true);

	// if ie, force use of non-cached images to avoid .load function issues
	if ($.support.htmlSerialize == true) {
		$('.workflow img').each(function(){  this.src = this.src + '?random=' + (new Date()).getTime()}).load(function() {
			wfSetHeights();
		});
	} else {
		$('.workflow img').load(function(){ 
			wfSetHeights();
			alert('heyyyyy');
		});
	};
};


/**
 * workflow set heights
 * autoresizes heights of elements in steps to the tallest element in the step
 */
var wfSetHeights = function() {
	$('.step').each(function(i) {
		tallest_height = 0;
		tallest_slide = 0;

		// find the tallest carousel slide, instead of the tallest carousel,
		// in case this function defies us and runs before the carousels are completely initialized (I'm looking at you, firefox.)
		$(this).find('.carousel .slide').each(function() {
			tallest_slide = ($(this).height() > tallest_slide) ? $(this).height() : tallest_slide;
		});

		// then compare tallest_height against tallest_slide and the heights of the other elements
		$(this).find('.base, aside.support, .overview, .see-container').each(function() {
			tallest_height = ($(this).height() > tallest_height) ? $(this).height() : tallest_height;
		});
		
		// use the taller of tallest_slide and tallest_height
		if (tallest_height > tallest_slide) {
			$(this).find('.carousel, .base, aside.support, .overview, .see-container').css('height', tallest_height);
			$(this).css('height', tallest_height);
		} else {
			$(this).find('.carousel, .base, aside.support, .overview, .see-container').css('height', tallest_slide);
			$(this).css('height', tallest_slide);
		}
	});
};

/**
 * workflow controls
 * open and control carousel, open right-side overview panel, etc
 */
var wfControl = function() {
	// open carousel
	$('.see').click(function(event) {
		event.preventDefault();
		$(this).parents('.step').children('.carousel').addClass('active', 500);
	});
	
	// close carousel
	$('.carousel .button.close-slate').click(function(event) {
		event.preventDefault();
		$(this).parents('.carousel').removeClass('active', 500);
	});
	
	// close the overview panel
	$('.overview .button.close-slate').click(function(event) {
		event.preventDefault();
		$(this).parents('.wrapper-support').removeClass('active', 500);
		$(this).parents('.step').find('ul.overview-links li').removeClass('active');
	});
	
	// open the overview panel
	$('ul.overview-links li').click(function(event) {
		event.preventDefault();
		
		$(this).siblings().removeClass('active');
		$(this).addClass('active');
		
		support = $(this).parents('.step').find('.wrapper-support');
		myOverview = $(this).data('overview');
		overviews = $(this).parents('.step').find('.overview-text');

		// if panel is already open
		if (support.hasClass('active')) {
			// find the active overview text and compare it to the clicked overview link
			// prevents flutter if that overview is already open
			activeOverview = $(this).parents('.step').find('.overview-text.active').attr('data-overview');
			// if that overview is not open
			if (activeOverview != myOverview) {
				// find the matching overview, hide others and turn it on
				overviews.each(function() {
					if ($(this).attr('data-overview') == myOverview) {
						overviews.removeClass('active');
						overviews.fadeOut('slow');
						$(this).addClass('active');
						$(this).fadeIn('slow');
					}
				});
			}
		// panel not already open
		// hide any visible overviews and show the selected one
		} else {
			overviews.hide();
			support.addClass('active', 500);
			overviews.each(function() {
				if ($(this).data('overview') == myOverview) {
					$(this).fadeIn('slow');
					$(this).addClass('active');
				}
			});
		}
	});
};

/**
 * workflow carousel init callback
 * external nav for jcarousel
 * disable carousel prev/next buttons if no prev/next slide exists
 */
var workflowInitCallback = function(carousel) {
	$('.carousel').each(function() {
		$('li.slide:first-child').find('.prev').addClass('disabled');
		$('li.slide:last-child').find('.next').addClass('disabled');
	});
	
	$('nav.carousel-nav li a').bind('click', function() {
		event.preventDefault();
	});
	
	$('nav.carousel-nav li.prev').not('.disabled').bind('click', function() {
		myCarousel = $(this).parents('ul.carousel-container').data('jcarousel');
		myCarousel.prev();
		return false;
	});
	
	$('nav.carousel-nav li.next').not('.disabled').bind('click', function() {
		myCarousel = $(this).parents('ul.carousel-container').data('jcarousel');
		myCarousel.next();
		return false;
	});	
};

/* home slideshow - using nicefade */
function initNicefade() {
	$(function(){
		if($('.nicefade_container').length) {
			$('.nicefade_container').each(function(){
				if(this.children.length == 3) {
					$('ul.arrow, nav.feature-nav').addClass('thirds');
				}
			});
			nicefade = $('#nicefade_wrapper .nicefade_container').nicefade({
				animationDelay: 30000,
				afterSlideChange: nicefadeAfter
			});
	
			nicefadeSeek();
			nicefadeAfter();
		}
	});
	
	// last item in nav list
	$('.home .feature .feature-nav ul.nicefade_index-list li:last-child').addClass('last');
};

/* nicefade after
 * controls .active class for nicefade external nav
 */
function nicefadeAfter() {
	var idx = nicefade.current_slide().index() + 1;
	$('nav.feature-nav li').removeClass('active');
	$('ul.arrow li').removeClass('active');
	$('nav.feature-nav li').each(function() {
		if ($(this).data('slide') == idx) {
			$(this).addClass('active');
		}
	});
	$('ul.arrow li').each(function() {
		if ($(this).data('slide') == idx) {
			$(this).addClass('active');
		}
	});
};

/* nicefade seek
 * external nav for nidefade
 */
function nicefadeSeek() {
	$('nav.feature-nav li').bind('click', function() {
		nicefade.seek($(this).data('slide'));
		return false;
	});
};





/*!
 * nicefade
 * https://github.com/ridgehkr/nicefade
 */
 (function($){

 	var $container, $current_element, $target_element, $next_element, $previous_element, $indexList, stop_animation, $current_slide, functions, settings;

 	$.fn.nicefade = function( options ) {

 		// advance the slideshow one step forward
 		this.next = function() {

 			stop_animation = true;
 			$target_element = $next_element;
 			functions.fadeTo($target_element, functions.loopCycler, false);

 		}


 		// move the slideshow one step backward
 		this.previous = function() {

 			stop_animation = true;
 			$target_element = $previous_element;
 			functions.fadeTo($target_element, functions.loopCycler, false);

 		}


 		// show the slide at index @target_index
 		this.seek = function( target_index ) {

 			stop_animation = true;
 			target_index = parseInt(target_index);
 			$target_element = $container.children(':nth-child(' + target_index + ')');
 			functions.fadeTo($target_element, functions.loopCycler, false);			

 		}


 		// stop the slideshow's automated animation
 		this.stop = function( ) {

 			stop_animation = true;

 		}


 		// resume the slideshow's automated animation
 		/*
 		this.start = function( ) {

 			// TODO

 		}
 		*/


 		// resume the slideshow's automated animation
 		this.is_active = function( ) {

 			return ! stop_animation;

 		}

 		// get the currently active slide
 		this.current_slide = function() {

 			return $current_element;

 		}

 		this.slideshow_length = function(){

 			return $container.children().length;

 		}

 		this.target_slide = function() {

 			return $target_element;

 		}


 		return this.each(function() {

 			$container = $(this);

 			// Create some defaults, extending them with any options that were provided
 			settings = $.extend( {
 				'animationSpeed'	: 500,
 				'animationDelay'	: 5000,
 				'indexList'			: $container.siblings('.nicefade_index-list'),
 				'initialIndex'		: 1,
 				'currentClass'		: 'current',
 				'afterSlideChange'	: null,
 				'beforeSlideChange'	: null
 			}, options);


 			// helper functions container
 			functions = {

 				init: function() {

 					// set up variables to indicate initial state
 					$current_element = $('> *:nth-child(' + settings.initialIndex + ')', $container);
 					$target_element = $();
 					functions.updateSlideStatus();
 					$indexList = settings.indexList;
 					stop_animation = false;


 					// hide all elements that aren't the first one (NOTE: do this is your CSS to prevent a FOUC)
 					$container.children().not($current_element).hide();


 					// indicate initial index in index list
 					$indexList.children(':nth-child(' + settings.initialIndex + ')').addClass(settings.currentClass);


 					// click handler for index items. Switches view to requested slide
 					$indexList.find('a').click(function(e){
 						e.preventDefault();
 						stop_animation = true; // stop the slideshow from continuing

 						var requested_index = $(e.target).parent().index(),
 							$requested_slide = $container.children(':nth-child(' + (requested_index + 1) + ')'); // +1 to compensate for 0-index default

 						$target_element = $requested_slide;

 						functions.fadeTo($requested_slide, $.noop(), true);
 					});

 					// kick off the animation after the initial delay
 					setTimeout( function(){ functions.loopCycler(); }, settings.animationDelay);
 				},


 				// fade in to a new element and fade out the old one
 				fadeTo: function( element_in, callback, updateIndexImmediately ) {

 					if ( $.isFunction(settings.beforeSlideChange) )
 						settings.beforeSlideChange();

 					// perform animations
 					// NOTE: using fadeTo() instead of fadeIn() and fadeOut() because in not all cases do elements return to full opacity
 					// if clicking fast across indecis
 					$current_element.stop().fadeTo( settings.animationSpeed, 0, function() {
 						$(this).removeClass(settings.currentClass).hide();
 					});
 					$current_element = element_in.stop().fadeTo( settings.animationSpeed, 1, function() {

 						if ( callback )
 							callback();

 						functions.updateSlideStatus();

 						if ( ! updateIndexImmediately )
 							functions.updateIndex();

 						$.when( $(this).addClass(settings.currentClass) ).done(function(){

 							if ( $.isFunction(settings.afterSlideChange) )
 								settings.afterSlideChange();

 						});

 					});

 					// if the index list should be updated before the animation is complete
 					if ( updateIndexImmediately )
 						functions.updateIndex();

 				},

 				// recursive wrapper for setTimeout
 				loopCycler: function() {				
 					if ( ! stop_animation ) {
 						functions.fadeTo( $next_element, function() {				
 					        setTimeout( function() { functions.loopCycler(); }, settings.animationDelay );
 					    }, false);
 					}
 				},

 				// set the current and next slides
 				updateSlideStatus: function() {
 					$previous_element = $current_element.prev();
 					$next_element = $current_element.next();

 					if ( ! $next_element.length )
 						$next_element = $container.children(':first');

 					if ( ! $previous_element.length )
 						$previous_element = $container.children(':last');

 					$target_element = $();

 				},

 				// make slide index list indicate the current slide
 				updateIndex: function() {
 					var current_index = $current_element.index() + 1; // +1 to compensate for 0-index default				
 					$indexList.children(':nth-child(' + current_index + ')').addClass(settings.currentClass).siblings().removeClass(settings.currentClass);
 				}

 			}; // functions


 			// kick off the animations
 			functions.init();

 		});

 	}; // $.fn.nicefade


 })( jQuery );

/*!
 * jCarousel - Riding carousels with jQuery
 *   http://sorgalla.com/jcarousel/
 *
 * Copyright (c) 2006 Jan Sorgalla (http://sorgalla.com)
 * Dual licensed under the MIT (http://www.opensource.org/licenses/mit-license.php)
 * and GPL (http://www.opensource.org/licenses/gpl-license.php) licenses.
 *
 * Built on top of the jQuery library
 *   http://jquery.com
 *
 * Inspired by the "Carousel Component" by Bill Scott
 *   http://billwscott.com/carousel/
 */
 
 /*
  * Version 0.2 
  *
  * https://github.com/jsor/jcarousel/blob/0.2/lib/jquery.jcarousel.js
  */
 
 (function($) {
     // Default configuration properties.
     var defaults = {
         vertical: false,
         rtl: false,
         start: 1,
         offset: 1,
         size: null,
         scroll: 3,
         visible: null,
         animation: 'normal',
         easing: 'swing',
         auto: 0,
         wrap: null,
         initCallback: null,
         setupCallback: null,
         reloadCallback: null,
         itemLoadCallback: null,
         itemFirstInCallback: null,
         itemFirstOutCallback: null,
         itemLastInCallback: null,
         itemLastOutCallback: null,
         itemVisibleInCallback: null,
         itemVisibleOutCallback: null,
         animationStepCallback: null,
         buttonNextHTML: '<div></div>',
         buttonPrevHTML: '<div></div>',
         buttonNextEvent: 'click',
         buttonPrevEvent: 'click',
         buttonNextCallback: null,
         buttonPrevCallback: null,
         itemFallbackDimension: null
     }, windowLoaded = false;

     $(window).bind('load.jcarousel', function() { windowLoaded = true; });

     /**
      * The jCarousel object.
      *
      * @constructor
      * @class jcarousel
      * @param e {HTMLElement} The element to create the carousel for.
      * @param o {Object} A set of key/value pairs to set as configuration properties.
      * @cat Plugins/jCarousel
      */
     $.jcarousel = function(e, o) {
         this.options    = $.extend({}, defaults, o || {});

         this.locked          = false;
         this.autoStopped     = false;

         this.container       = null;
         this.clip            = null;
         this.list            = null;
         this.buttonNext      = null;
         this.buttonPrev      = null;
         this.buttonNextState = null;
         this.buttonPrevState = null;

         // Only set if not explicitly passed as option
         if (!o || o.rtl === undefined) {
             this.options.rtl = ($(e).attr('dir') || $('html').attr('dir') || '').toLowerCase() == 'rtl';
         }

         this.wh = !this.options.vertical ? 'width' : 'height';
         this.lt = !this.options.vertical ? (this.options.rtl ? 'right' : 'left') : 'top';

         // Extract skin class
         var skin = '', split = e.className.split(' ');

         for (var i = 0; i < split.length; i++) {
             if (split[i].indexOf('jcarousel-skin') != -1) {
                 $(e).removeClass(split[i]);
                 skin = split[i];
                 break;
             }
         }

         if (e.nodeName.toUpperCase() == 'UL' || e.nodeName.toUpperCase() == 'OL') {
             this.list      = $(e);
             this.clip      = this.list.parents('.jcarousel-clip');
             this.container = this.list.parents('.jcarousel-container');
         } else {
             this.container = $(e);
             this.list      = this.container.find('ul,ol').eq(0);
             this.clip      = this.container.find('.jcarousel-clip');
         }

         if (this.clip.size() === 0) {
             this.clip = this.list.wrap('<div></div>').parent();
         }

         if (this.container.size() === 0) {
             this.container = this.clip.wrap('<div></div>').parent();
         }

         if (skin !== '' && this.container.parent()[0].className.indexOf('jcarousel-skin') == -1) {
             this.container.wrap('<div class=" '+ skin + '"></div>');
         }

         this.buttonPrev = $('.jcarousel-prev', this.container);

         if (this.buttonPrev.size() === 0 && this.options.buttonPrevHTML !== null) {
             this.buttonPrev = $(this.options.buttonPrevHTML).appendTo(this.container);
         }

         this.buttonPrev.addClass(this.className('jcarousel-prev'));

         this.buttonNext = $('.jcarousel-next', this.container);

         if (this.buttonNext.size() === 0 && this.options.buttonNextHTML !== null) {
             this.buttonNext = $(this.options.buttonNextHTML).appendTo(this.container);
         }

         this.buttonNext.addClass(this.className('jcarousel-next'));

         this.clip.addClass(this.className('jcarousel-clip')).css({
             position: 'relative'
         });

         this.list.addClass(this.className('jcarousel-list')).css({
             overflow: 'hidden',
             position: 'relative',
             top: 0,
             margin: 0,
             padding: 0
         }).css((this.options.rtl ? 'right' : 'left'), 0);

         this.container.addClass(this.className('jcarousel-container')).css({
             position: 'relative'
         });

         if (!this.options.vertical && this.options.rtl) {
             this.container.addClass('jcarousel-direction-rtl').attr('dir', 'rtl');
         }

         var di = this.options.visible !== null ? Math.ceil(this.clipping() / this.options.visible) : null;
         var li = this.list.children('li');

         var self = this;

         if (li.size() > 0) {
             var wh = 0, j = this.options.offset;
             li.each(function() {
                 self.format(this, j++);
                 wh += self.dimension(this, di);
             });

             this.list.css(this.wh, (wh + 100) + 'px');

             // Only set if not explicitly passed as option
             if (!o || o.size === undefined) {
                 this.options.size = li.size();
             }
         }

         // For whatever reason, .show() does not work in Safari...
         this.container.css('display', 'block');
         this.buttonNext.css('display', 'block');
         this.buttonPrev.css('display', 'block');

         this.funcNext   = function() { self.next(); return false; };
         this.funcPrev   = function() { self.prev(); return false; };
         this.funcResize = function() { 
             if (self.resizeTimer) {
                 clearTimeout(self.resizeTimer);
             }

             self.resizeTimer = setTimeout(function() {
                 self.reload();
             }, 100);
         };

         if (this.options.initCallback !== null) {
             this.options.initCallback(this, 'init');
         }

         if (!windowLoaded && $.browser.safari) {
             this.buttons(false, false);
             $(window).bind('load.jcarousel', function() { self.setup(); });
         } else {
             this.setup();
         }
     };

     // Create shortcut for internal use
     var $jc = $.jcarousel;

     $jc.fn = $jc.prototype = {
         jcarousel: '0.2.8'
     };

     $jc.fn.extend = $jc.extend = $.extend;

     $jc.fn.extend({
         /**
          * Setups the carousel.
          *
          * @method setup
          * @return undefined
          */
         setup: function() {
             this.first       = null;
             this.last        = null;
             this.prevFirst   = null;
             this.prevLast    = null;
             this.animating   = false;
             this.timer       = null;
             this.resizeTimer = null;
             this.tail        = null;
             this.inTail      = false;

             if (this.locked) {
                 return;
             }

             this.list.css(this.lt, this.pos(this.options.offset) + 'px');
             var p = this.pos(this.options.start, true);
             this.prevFirst = this.prevLast = null;
             this.animate(p, false);

             $(window).unbind('resize.jcarousel', this.funcResize).bind('resize.jcarousel', this.funcResize);

             if (this.options.setupCallback !== null) {
                 this.options.setupCallback(this);
             }
         },

         /**
          * Clears the list and resets the carousel.
          *
          * @method reset
          * @return undefined
          */
         reset: function() {
             this.list.empty();

             this.list.css(this.lt, '0px');
             this.list.css(this.wh, '10px');

             if (this.options.initCallback !== null) {
                 this.options.initCallback(this, 'reset');
             }

             this.setup();
         },

         /**
          * Reloads the carousel and adjusts positions.
          *
          * @method reload
          * @return undefined
          */
         reload: function() {
             if (this.tail !== null && this.inTail) {
                 this.list.css(this.lt, $jc.intval(this.list.css(this.lt)) + this.tail);
             }

             this.tail   = null;
             this.inTail = false;

             if (this.options.reloadCallback !== null) {
                 this.options.reloadCallback(this);
             }

             if (this.options.visible !== null) {
                 var self = this;
                 var di = Math.ceil(this.clipping() / this.options.visible), wh = 0, lt = 0;
                 this.list.children('li').each(function(i) {
                     wh += self.dimension(this, di);
                     if (i + 1 < self.first) {
                         lt = wh;
                     }
                 });

                 this.list.css(this.wh, wh + 'px');
                 this.list.css(this.lt, -lt + 'px');
             }

             this.scroll(this.first, false);
         },

         /**
          * Locks the carousel.
          *
          * @method lock
          * @return undefined
          */
         lock: function() {
             this.locked = true;
             this.buttons();
         },

         /**
          * Unlocks the carousel.
          *
          * @method unlock
          * @return undefined
          */
         unlock: function() {
             this.locked = false;
             this.buttons();
         },

         /**
          * Sets the size of the carousel.
          *
          * @method size
          * @return undefined
          * @param s {Number} The size of the carousel.
          */
         size: function(s) {
             if (s !== undefined) {
                 this.options.size = s;
                 if (!this.locked) {
                     this.buttons();
                 }
             }

             return this.options.size;
         },

         /**
          * Checks whether a list element exists for the given index (or index range).
          *
          * @method get
          * @return bool
          * @param i {Number} The index of the (first) element.
          * @param i2 {Number} The index of the last element.
          */
         has: function(i, i2) {
             if (i2 === undefined || !i2) {
                 i2 = i;
             }

             if (this.options.size !== null && i2 > this.options.size) {
                 i2 = this.options.size;
             }

             for (var j = i; j <= i2; j++) {
                 var e = this.get(j);
                 if (!e.length || e.hasClass('jcarousel-item-placeholder')) {
                     return false;
                 }
             }

             return true;
         },

         /**
          * Returns a jQuery object with list element for the given index.
          *
          * @method get
          * @return jQuery
          * @param i {Number} The index of the element.
          */
         get: function(i) {
             return $('>.jcarousel-item-' + i, this.list);
         },

         /**
          * Adds an element for the given index to the list.
          * If the element already exists, it updates the inner html.
          * Returns the created element as jQuery object.
          *
          * @method add
          * @return jQuery
          * @param i {Number} The index of the element.
          * @param s {String} The innerHTML of the element.
          */
         add: function(i, s) {
             var e = this.get(i), old = 0, n = $(s);

             if (e.length === 0) {
                 var c, j = $jc.intval(i);
                 e = this.create(i);
                 while (true) {
                     c = this.get(--j);
                     if (j <= 0 || c.length) {
                         if (j <= 0) {
                             this.list.prepend(e);
                         } else {
                             c.after(e);
                         }
                         break;
                     }
                 }
             } else {
                 old = this.dimension(e);
             }

             if (n.get(0).nodeName.toUpperCase() == 'LI') {
                 e.replaceWith(n);
                 e = n;
             } else {
                 e.empty().append(s);
             }

             this.format(e.removeClass(this.className('jcarousel-item-placeholder')), i);

             var di = this.options.visible !== null ? Math.ceil(this.clipping() / this.options.visible) : null;
             var wh = this.dimension(e, di) - old;

             if (i > 0 && i < this.first) {
                 this.list.css(this.lt, $jc.intval(this.list.css(this.lt)) - wh + 'px');
             }

             this.list.css(this.wh, $jc.intval(this.list.css(this.wh)) + wh + 'px');

             return e;
         },

         /**
          * Removes an element for the given index from the list.
          *
          * @method remove
          * @return undefined
          * @param i {Number} The index of the element.
          */
         remove: function(i) {
             var e = this.get(i);

             // Check if item exists and is not currently visible
             if (!e.length || (i >= this.first && i <= this.last)) {
                 return;
             }

             var d = this.dimension(e);

             if (i < this.first) {
                 this.list.css(this.lt, $jc.intval(this.list.css(this.lt)) + d + 'px');
             }

             e.remove();

             this.list.css(this.wh, $jc.intval(this.list.css(this.wh)) - d + 'px');
         },

         /**
          * Moves the carousel forwards.
          *
          * @method next
          * @return undefined
          */
         next: function() {
             if (this.tail !== null && !this.inTail) {
                 this.scrollTail(false);
             } else {
                 this.scroll(((this.options.wrap == 'both' || this.options.wrap == 'last') && this.options.size !== null && this.last == this.options.size) ? 1 : this.first + this.options.scroll);
             }
         },

         /**
          * Moves the carousel backwards.
          *
          * @method prev
          * @return undefined
          */
         prev: function() {
             if (this.tail !== null && this.inTail) {
                 this.scrollTail(true);
             } else {
                 this.scroll(((this.options.wrap == 'both' || this.options.wrap == 'first') && this.options.size !== null && this.first == 1) ? this.options.size : this.first - this.options.scroll);
             }
         },

         /**
          * Scrolls the tail of the carousel.
          *
          * @method scrollTail
          * @return undefined
          * @param b {Boolean} Whether scroll the tail back or forward.
          */
         scrollTail: function(b) {
             if (this.locked || this.animating || !this.tail) {
                 return;
             }

             this.pauseAuto();

             var pos  = $jc.intval(this.list.css(this.lt));

             pos = !b ? pos - this.tail : pos + this.tail;
             this.inTail = !b;

             // Save for callbacks
             this.prevFirst = this.first;
             this.prevLast  = this.last;

             this.animate(pos);
         },

         /**
          * Scrolls the carousel to a certain position.
          *
          * @method scroll
          * @return undefined
          * @param i {Number} The index of the element to scoll to.
          * @param a {Boolean} Flag indicating whether to perform animation.
          */
         scroll: function(i, a) {
             if (this.locked || this.animating) {
                 return;
             }

             this.pauseAuto();
             this.animate(this.pos(i), a);
         },

         /**
          * Prepares the carousel and return the position for a certian index.
          *
          * @method pos
          * @return {Number}
          * @param i {Number} The index of the element to scoll to.
          * @param fv {Boolean} Whether to force last item to be visible.
          */
         pos: function(i, fv) {
             var pos  = $jc.intval(this.list.css(this.lt));

             if (this.locked || this.animating) {
                 return pos;
             }

             if (this.options.wrap != 'circular') {
                 i = i < 1 ? 1 : (this.options.size && i > this.options.size ? this.options.size : i);
             }

             var back = this.first > i;

             // Create placeholders, new list width/height
             // and new list position
             var f = this.options.wrap != 'circular' && this.first <= 1 ? 1 : this.first;
             var c = back ? this.get(f) : this.get(this.last);
             var j = back ? f : f - 1;
             var e = null, l = 0, p = false, d = 0, g;

             while (back ? --j >= i : ++j < i) {
                 e = this.get(j);
                 p = !e.length;
                 if (e.length === 0) {
                     e = this.create(j).addClass(this.className('jcarousel-item-placeholder'));
                     c[back ? 'before' : 'after' ](e);

                     if (this.first !== null && this.options.wrap == 'circular' && this.options.size !== null && (j <= 0 || j > this.options.size)) {
                         g = this.get(this.index(j));
                         if (g.length) {
                             e = this.add(j, g.clone(true));
                         }
                     }
                 }

                 c = e;
                 d = this.dimension(e);

                 if (p) {
                     l += d;
                 }

                 if (this.first !== null && (this.options.wrap == 'circular' || (j >= 1 && (this.options.size === null || j <= this.options.size)))) {
                     pos = back ? pos + d : pos - d;
                 }
             }

             // Calculate visible items
             var clipping = this.clipping(), cache = [], visible = 0, v = 0;
             c = this.get(i - 1);
             j = i;

             while (++visible) {
                 e = this.get(j);
                 p = !e.length;
                 if (e.length === 0) {
                     e = this.create(j).addClass(this.className('jcarousel-item-placeholder'));
                     // This should only happen on a next scroll
                     if (c.length === 0) {
                         this.list.prepend(e);
                     } else {
                         c[back ? 'before' : 'after' ](e);
                     }

                     if (this.first !== null && this.options.wrap == 'circular' && this.options.size !== null && (j <= 0 || j > this.options.size)) {
                         g = this.get(this.index(j));
                         if (g.length) {
                             e = this.add(j, g.clone(true));
                         }
                     }
                 }

                 c = e;
                 d = this.dimension(e);
                 if (d === 0) {
                     throw new Error('jCarousel: No width/height set for items. This will cause an infinite loop. Aborting...');
                 }

                 if (this.options.wrap != 'circular' && this.options.size !== null && j > this.options.size) {
                     cache.push(e);
                 } else if (p) {
                     l += d;
                 }

                 v += d;

                 if (v >= clipping) {
                     break;
                 }

                 j++;
             }

              // Remove out-of-range placeholders
             for (var x = 0; x < cache.length; x++) {
                 cache[x].remove();
             }

             // Resize list
             if (l > 0) {
                 this.list.css(this.wh, this.dimension(this.list) + l + 'px');

                 if (back) {
                     pos -= l;
                     this.list.css(this.lt, $jc.intval(this.list.css(this.lt)) - l + 'px');
                 }
             }

             // Calculate first and last item
             var last = i + visible - 1;
             if (this.options.wrap != 'circular' && this.options.size && last > this.options.size) {
                 last = this.options.size;
             }

             if (j > last) {
                 visible = 0;
                 j = last;
                 v = 0;
                 while (++visible) {
                     e = this.get(j--);
                     if (!e.length) {
                         break;
                     }
                     v += this.dimension(e);
                     if (v >= clipping) {
                         break;
                     }
                 }
             }

             var first = last - visible + 1;
             if (this.options.wrap != 'circular' && first < 1) {
                 first = 1;
             }

             if (this.inTail && back) {
                 pos += this.tail;
                 this.inTail = false;
             }

             this.tail = null;
             if (this.options.wrap != 'circular' && last == this.options.size && (last - visible + 1) >= 1) {
                 var m = $jc.intval(this.get(last).css(!this.options.vertical ? 'marginRight' : 'marginBottom'));
                 if ((v - m) > clipping) {
                     this.tail = v - clipping - m;
                 }
             }

             if (fv && i === this.options.size && this.tail) {
                 pos -= this.tail;
                 this.inTail = true;
             }

             // Adjust position
             while (i-- > first) {
                 pos += this.dimension(this.get(i));
             }

             // Save visible item range
             this.prevFirst = this.first;
             this.prevLast  = this.last;
             this.first     = first;
             this.last      = last;

             return pos;
         },

         /**
          * Animates the carousel to a certain position.
          *
          * @method animate
          * @return undefined
          * @param p {Number} Position to scroll to.
          * @param a {Boolean} Flag indicating whether to perform animation.
          */
         animate: function(p, a) {
             if (this.locked || this.animating) {
                 return;
             }

             this.animating = true;

             var self = this;
             var scrolled = function() {
                 self.animating = false;

                 if (p === 0) {
                     self.list.css(self.lt,  0);
                 }

                 if (!self.autoStopped && (self.options.wrap == 'circular' || self.options.wrap == 'both' || self.options.wrap == 'last' || self.options.size === null || self.last < self.options.size || (self.last == self.options.size && self.tail !== null && !self.inTail))) {
                     self.startAuto();
                 }

                 self.buttons();
                 self.notify('onAfterAnimation');

                 // This function removes items which are appended automatically for circulation.
                 // This prevents the list from growing infinitely.
                 if (self.options.wrap == 'circular' && self.options.size !== null) {
                     for (var i = self.prevFirst; i <= self.prevLast; i++) {
                         if (i !== null && !(i >= self.first && i <= self.last) && (i < 1 || i > self.options.size)) {
                             self.remove(i);
                         }
                     }
                 }
             };

             this.notify('onBeforeAnimation');

             // Animate
             if (!this.options.animation || a === false) {
                 this.list.css(this.lt, p + 'px');
                 scrolled();
             } else {
                 var o = !this.options.vertical ? (this.options.rtl ? {'right': p} : {'left': p}) : {'top': p};
                 // Define animation settings.
                 var settings = {
                     duration: this.options.animation,
                     easing:   this.options.easing,
                     complete: scrolled
                 };
                 // If we have a step callback, specify it as well.
                 if ($.isFunction(this.options.animationStepCallback)) {
                     settings.step = this.options.animationStepCallback;
                 }
                 // Start the animation.
                 this.list.animate(o, settings);
             }
         },

         /**
          * Starts autoscrolling.
          *
          * @method auto
          * @return undefined
          * @param s {Number} Seconds to periodically autoscroll the content.
          */
         startAuto: function(s) {
             if (s !== undefined) {
                 this.options.auto = s;
             }

             if (this.options.auto === 0) {
                 return this.stopAuto();
             }

             if (this.timer !== null) {
                 return;
             }

             this.autoStopped = false;

             var self = this;
             this.timer = window.setTimeout(function() { self.next(); }, this.options.auto * 1000);
         },

         /**
          * Stops autoscrolling.
          *
          * @method stopAuto
          * @return undefined
          */
         stopAuto: function() {
             this.pauseAuto();
             this.autoStopped = true;
         },

         /**
          * Pauses autoscrolling.
          *
          * @method pauseAuto
          * @return undefined
          */
         pauseAuto: function() {
             if (this.timer === null) {
                 return;
             }

             window.clearTimeout(this.timer);
             this.timer = null;
         },

         /**
          * Sets the states of the prev/next buttons.
          *
          * @method buttons
          * @return undefined
          */
         buttons: function(n, p) {
             if (n == null) {
                 n = !this.locked && this.options.size !== 0 && ((this.options.wrap && this.options.wrap != 'first') || this.options.size === null || this.last < this.options.size);
                 if (!this.locked && (!this.options.wrap || this.options.wrap == 'first') && this.options.size !== null && this.last >= this.options.size) {
                     n = this.tail !== null && !this.inTail;
                 }
             }

             if (p == null) {
                 p = !this.locked && this.options.size !== 0 && ((this.options.wrap && this.options.wrap != 'last') || this.first > 1);
                 if (!this.locked && (!this.options.wrap || this.options.wrap == 'last') && this.options.size !== null && this.first == 1) {
                     p = this.tail !== null && this.inTail;
                 }
             }

             var self = this;

             if (this.buttonNext.size() > 0) {
                 this.buttonNext.unbind(this.options.buttonNextEvent + '.jcarousel', this.funcNext);

                 if (n) {
                     this.buttonNext.bind(this.options.buttonNextEvent + '.jcarousel', this.funcNext);
                 }

                 this.buttonNext[n ? 'removeClass' : 'addClass'](this.className('jcarousel-next-disabled')).attr('disabled', n ? false : true);

                 if (this.options.buttonNextCallback !== null && this.buttonNext.data('jcarouselstate') != n) {
                     this.buttonNext.each(function() { self.options.buttonNextCallback(self, this, n); }).data('jcarouselstate', n);
                 }
             } else {
                 if (this.options.buttonNextCallback !== null && this.buttonNextState != n) {
                     this.options.buttonNextCallback(self, null, n);
                 }
             }

             if (this.buttonPrev.size() > 0) {
                 this.buttonPrev.unbind(this.options.buttonPrevEvent + '.jcarousel', this.funcPrev);

                 if (p) {
                     this.buttonPrev.bind(this.options.buttonPrevEvent + '.jcarousel', this.funcPrev);
                 }

                 this.buttonPrev[p ? 'removeClass' : 'addClass'](this.className('jcarousel-prev-disabled')).attr('disabled', p ? false : true);

                 if (this.options.buttonPrevCallback !== null && this.buttonPrev.data('jcarouselstate') != p) {
                     this.buttonPrev.each(function() { self.options.buttonPrevCallback(self, this, p); }).data('jcarouselstate', p);
                 }
             } else {
                 if (this.options.buttonPrevCallback !== null && this.buttonPrevState != p) {
                     this.options.buttonPrevCallback(self, null, p);
                 }
             }

             this.buttonNextState = n;
             this.buttonPrevState = p;
         },

         /**
          * Notify callback of a specified event.
          *
          * @method notify
          * @return undefined
          * @param evt {String} The event name
          */
         notify: function(evt) {
             var state = this.prevFirst === null ? 'init' : (this.prevFirst < this.first ? 'next' : 'prev');

             // Load items
             this.callback('itemLoadCallback', evt, state);

             if (this.prevFirst !== this.first) {
                 this.callback('itemFirstInCallback', evt, state, this.first);
                 this.callback('itemFirstOutCallback', evt, state, this.prevFirst);
             }

             if (this.prevLast !== this.last) {
                 this.callback('itemLastInCallback', evt, state, this.last);
                 this.callback('itemLastOutCallback', evt, state, this.prevLast);
             }

             this.callback('itemVisibleInCallback', evt, state, this.first, this.last, this.prevFirst, this.prevLast);
             this.callback('itemVisibleOutCallback', evt, state, this.prevFirst, this.prevLast, this.first, this.last);
         },

         callback: function(cb, evt, state, i1, i2, i3, i4) {
             if (this.options[cb] == null || (typeof this.options[cb] != 'object' && evt != 'onAfterAnimation')) {
                 return;
             }

             var callback = typeof this.options[cb] == 'object' ? this.options[cb][evt] : this.options[cb];

             if (!$.isFunction(callback)) {
                 return;
             }

             var self = this;

             if (i1 === undefined) {
                 callback(self, state, evt);
             } else if (i2 === undefined) {
                 this.get(i1).each(function() { callback(self, this, i1, state, evt); });
             } else {
                 var call = function(i) {
                     self.get(i).each(function() { callback(self, this, i, state, evt); });
                 };
                 for (var i = i1; i <= i2; i++) {
                     if (i !== null && !(i >= i3 && i <= i4)) {
                         call(i);
                     }
                 }
             }
         },

         create: function(i) {
             return this.format('<li></li>', i);
         },

         format: function(e, i) {
             e = $(e);
             var split = e.get(0).className.split(' ');
             for (var j = 0; j < split.length; j++) {
                 if (split[j].indexOf('jcarousel-') != -1) {
                     e.removeClass(split[j]);
                 }
             }
             e.addClass(this.className('jcarousel-item')).addClass(this.className('jcarousel-item-' + i)).css({
                 'float': (this.options.rtl ? 'right' : 'left'),
                 'list-style': 'none'
             }).attr('jcarouselindex', i);
             return e;
         },

         className: function(c) {
             return c + ' ' + c + (!this.options.vertical ? '-horizontal' : '-vertical');
         },

         dimension: function(e, d) {
             var el = $(e);

             if (d == null) {
                 return !this.options.vertical ?
                     ((el.innerWidth() +
                         $jc.intval(el.css('margin-left')) +
                         $jc.intval(el.css('margin-right')) +
                         $jc.intval(el.css('border-left-width')) +
                         $jc.intval(el.css('border-right-width'))) || $jc.intval(this.options.itemFallbackDimension)) :
                     ((el.innerHeight() +
                         $jc.intval(el.css('margin-top')) +
                         $jc.intval(el.css('margin-bottom')) +
                         $jc.intval(el.css('border-top-width')) +
                         $jc.intval(el.css('border-bottom-width'))) || $jc.intval(this.options.itemFallbackDimension));
             } else {
                 var w = !this.options.vertical ?
                     d - $jc.intval(el.css('marginLeft')) - $jc.intval(el.css('marginRight')) :
                     d - $jc.intval(el.css('marginTop')) - $jc.intval(el.css('marginBottom'));

                 $(el).css(this.wh, w + 'px');

                 return this.dimension(el);
             }
         },

         clipping: function() {
             return !this.options.vertical ?
                 this.clip[0].offsetWidth - $jc.intval(this.clip.css('borderLeftWidth')) - $jc.intval(this.clip.css('borderRightWidth')) :
                 this.clip[0].offsetHeight - $jc.intval(this.clip.css('borderTopWidth')) - $jc.intval(this.clip.css('borderBottomWidth'));
         },

         index: function(i, s) {
             if (s == null) {
                 s = this.options.size;
             }

             return Math.round((((i-1) / s) - Math.floor((i-1) / s)) * s) + 1;
         }
     });

     $jc.extend({
         /**
          * Gets/Sets the global default configuration properties.
          *
          * @method defaults
          * @return {Object}
          * @param d {Object} A set of key/value pairs to set as configuration properties.
          */
         defaults: function(d) {
             return $.extend(defaults, d || {});
         },

         intval: function(v) {
             v = parseInt(v, 10);
             return isNaN(v) ? 0 : v;
         },

         windowLoaded: function() {
             windowLoaded = true;
         }
     });

     /**
      * Creates a carousel for all matched elements.
      *
      * @example $("#mycarousel").jcarousel();
      * @before <ul id="mycarousel" class="jcarousel-skin-name"><li>First item</li><li>Second item</li></ul>
      * @result
      *
      * <div class="jcarousel-skin-name">
      *   <div class="jcarousel-container">
      *     <div class="jcarousel-clip">
      *       <ul class="jcarousel-list">
      *         <li class="jcarousel-item-1">First item</li>
      *         <li class="jcarousel-item-2">Second item</li>
      *       </ul>
      *     </div>
      *     <div disabled="disabled" class="jcarousel-prev jcarousel-prev-disabled"></div>
      *     <div class="jcarousel-next"></div>
      *   </div>
      * </div>
      *
      * @method jcarousel
      * @return jQuery
      * @param o {Hash|String} A set of key/value pairs to set as configuration properties or a method name to call on a formerly created instance.
      */
     $.fn.jcarousel = function(o) {
         if (typeof o == 'string') {
             var instance = $(this).data('jcarousel'), args = Array.prototype.slice.call(arguments, 1);
             return instance[o].apply(instance, args);
         } else {
             return this.each(function() {
                 var instance = $(this).data('jcarousel');
                 if (instance) {
                     if (o) {
                         $.extend(instance.options, o);
                     }
                     instance.reload();
                 } else {
                     $(this).data('jcarousel', new $jc(this, o));
                 }
             });
         }
     };

 })(jQuery);
