dojo.provide("topaz.widget.RegionalDialog");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.ContentPane");
dojo.require("dojo.event.*");
dojo.require("dojo.gfx.color");
dojo.require("dojo.html.layout");
dojo.require("dojo.html.display");
dojo.require("dojo.widget.Dialog");		// for RegionalDialog
dojo.require("dojo.html.iframe");

// summary
//	Mixin for widgets implementing a modal dialog
dojo.declare(
	"topaz.widget.RegionalDialogBase", 
	[dojo.widget.Dialog],
	{
	  isContainer: true,

	  // static variables
	  shared: {bg: null, bgIframe: null, onClickCatcher: null},

	  // String
	  //	provide a focusable element or element id if you need to
	  //	work around FF's tendency to send focus into outer space on hide
	  focusElement: "",

    // String
	  //	color of viewport when displaying a dialog
	  bgColor: "black",
		
		// Number
		//	opacity (0~1) of viewport color (see bgColor attribute)
		bgOpacity: 0.4,

		// Boolean
		//	if true, readjusts the dialog (and dialog background) when the user moves the scrollbar
		followScroll: true,
		
		// closeOnBackgroundClick: Boolean
		//	clicking anywhere on the background will close the dialog
		closeOnBackgroundClick: true,

		_changeTipDirection: function(isTipDown, xShift) {
			var dTip = this.tipDownNode;
			var dTipu = this.tipUpNode;
			
			dTip.className = dTip.className.replace(/\son/, "");
    	dTipu.className = dTipu.className.replace(/\son/, ""); 
			
			var targetTip = (isTipDown) ? dTip : dTipu;
			
			targetTip.className = targetTip.className.concat(" on");

      //if (BrowserDetect.browser == "Explorer" && BrowserDetect.version < 7) 
 		  // 	targetTip.style.marginLeft = (xShift) ? xShift + "px" : "auto";
 		  //else
 		   	targetTip.style.left = (xShift) ? xShift + "px" : "auto";
		},
		
		placeModalDialog: function() {
			var scroll_offset = dojo.html.getScroll().offset;
			var viewport_size = dojo.html.getViewport();
			var dialog_marker = this.markerNode;
			
			var markerOffset = topaz.domUtil.getCurrentOffset(dialog_marker);
			
			// find the size of the dialog
			var mb = dojo.html.getMarginBox(this.containerNode);
			
			var mbWidth = mb.width;
			var mbHeight = mb.height;
			var vpWidth = viewport_size.width;
			var vpHeight = viewport_size.height;
			var scrollX = scroll_offset.x;
			var scrollY = scroll_offset.y;
			
			// The height of the tip.
			var tipHeight = 22;
			
			// The width of the tip.
			var tipWidth = 39;
			
			// The minimum distance from the left edge of the dialog box to the left edge of the tip.
			var tipMarginLeft = 22;
			
			// The minimum distance from the right edge of the dialog box to the right edge of the tip.
			var tipMarginRight = 22;
			
			// The minimum distance from either side edge of the dialog box to the corresponding side edge of the viewport.
			var mbMarginX = 10;
			
			// The minimum distance from the top or bottom edge of the dialog box to the top or bottom, respectively, of the viewport.
			var mbMarginY = 10;

      // The height of the bug. This is used when the tip points up to figure out how far down to push everything.
      var bugHeight = 15;

      // The minimum x-offset of the dialog box top-left corner, relative to the page.
      var xMin = scrollX + mbMarginX;
      
      // The minimum y-offset of the dialog box top-left corner, relative to the page.
      var yMin = scrollY + mbMarginY;
      
      // The maximum x-offset of the dialog box top-left corner, relative to the page.
      var xMax = scrollX + vpWidth - mbMarginX - mbWidth;

      // The maximum y-offset of the dialog box top-left corner, relative to the page.
      var yMax = scrollY + vpHeight - mbMarginY - mbHeight;
      
      // The minimum x-offset of the tip left edge, relative to the page.
      var xTipMin = xMin + tipMarginLeft;

      // The maximum x-offset of the tip left edge, relative to the page.
      var xTipMax = xMax + mbWidth - tipMarginRight - tipWidth;

      // True if the tip is pointing down (the default)
      var tipDown = true;

      // Sanity check to make sure that the viewport is large enough to accomodate the dialog box, the tip, and the minimum margins
      if (xMin > xMax || yMin > yMax || xTipMin > xTipMax) {
        // big error. Do something about it!
      }
			
/*      dojo.byId(djConfig.debugContainerId).innerHTML = "markerOffset.top = " + markerOffset.top
      															  + "<br/>" + "markerOffset.left = "  + markerOffset.left
      															  + "<br/>" + "mbWidth = " + mbWidth
      															  + "<br/>" + "mbHeight = " + mbHeight
      															  + "<br/>" + "vpWidth = " + vpWidth
      															  + "<br/>" + "vpHeight = " + vpHeight
      															  + "<br/>" + "scrollX = " + scrollX
      															  + "<br/>" + "scrollY = " + scrollY;
*/      															 
			// Default values put the box generally above and to the right of the annotation "bug"
      var xTip = markerOffset.left - (tipWidth / 2);
      var yTip = markerOffset.top - tipHeight - (tipHeight/4);
      
      var x = xTip - tipMarginLeft;
      var y = yTip - mbHeight;

      // If the box is too far to the left, try sliding it over to the right. The tip will slide with it, and thus no longer be pointing directly to the bug.
      if (x < xMin) {
        x = xMin;
        if (xTip < xTipMin) {
          xTip = xTipMin;
        }
      }
      // If the box is too far to the right, slide it over to the left, but leave the tip in the same place if possible.
      else if (x > xMax) {
        x = xMax;
        if (xTip > xTipMax) {
          xTip = xTipMax;
        }
      }

      // If the box is too far up, flip it over and put it below the annotation.
      if (y < yMin) {
        tipDown = false; // flip the tip

        yTip = markerOffset.top + bugHeight - (tipHeight/4);
        y = yTip + tipHeight;
        
        if (y > yMax) {
          // this is bad, because it means that there isn't enough room above or below the annotation for the dialog box, the tip, and/or the minimum margins
        }
      }
      
      var xTipDiff = markerOffset.left - x;
      
      if(xTipDiff < tipMarginLeft) {
        xTipPos = tipMarginLeft - (tipWidth / 4);
        x = x - (tipMarginLeft - xTipDiff);
      }
      else {
        xTipPos = xTipDiff - (tipWidth / 4);
        //x = x - (tipMarginLeft - xTipDiff);
      }
            
      this._changeTipDirection(tipDown, xTipPos);

			with(this.domNode.style){
				left = x + "px";
				top = y + "px";
			}

/*      dojo.byId(djConfig.debugContainerId).innerHTML += "<br/>" + "left = " + this.domNode.style.left
      															                  + "<br/>" + "top = "  + this.domNode.style.top;
*/		}
		
	});

// summary
//	Pops up a modal dialog window, blocking access to the screen and also graying out the screen
//	Dialog is extended from ContentPane so it supports all the same parameters (href, etc.)
dojo.widget.defineWidget(
	"topaz.widget.RegionalDialog",
	[topaz.widget.RegionalDialogBase],
	{
		// summary
		//	Pops up a modal dialog window, blocking access to the screen and also graying out the screen
		//	Dialog is extended from ContentPane so it supports all the same parameters (href, etc.)

		templatePath: dojo.uri.dojoUri("../topaz/widget/templates/RegionalDialog.html"),

		// blockDuration: Integer
		//	number of seconds for which the user cannot dismiss the dialog
		blockDuration: 0,
		
		// lifetime: Integer
		//	if set, this controls the number of seconds the dialog will be displayed before automatically disappearing
		lifetime: 0,

		// closeNode: String
		//	Id of button or other dom node to click to close this dialog
		closeNode: "",

		postMixInProperties: function(){
			//topaz.widget.RegionalDialogBase.superclass.postMixInProperties.apply(this, arguments);
			topaz.widget.RegionalDialog.superclass.postMixInProperties.apply(this, arguments);
			if(this.closeNode){
				this.setCloseControl(this.closeNode);
			}

      this.strings = {
    		markerNode: this.markerNode,
    		
    		tipDownNode: this.tipDownNode,
    		
    		tipUpNode: this.tipUpNode
      }
		},

		postCreate: function(){
			topaz.widget.RegionalDialog.superclass.postCreate.apply(this, arguments);
			topaz.widget.RegionalDialogBase.prototype.postCreate.apply(this, arguments);
		},

		show: function() {
      dojo.widget.Dialog.superclass.show.apply(this, arguments);

			this.showModalDialog();
			topaz.widget.RegionalDialog.superclass.show.call(this);
		},

		onLoad: function(){
			// when href is specified we need to reposition
			// the dialog after the data is loaded
      dojo.widget.Dialog.superclass.onLoad.apply(this, arguments);
			this.placeModalDialog();
			topaz.widget.RegionalDialog.superclass.onLoad.call(this);
		},
		
		fillInTemplate: function(){
			// dojo.event.connect(this.domNode, "onclick", this, "killEvent");
		},

		hide: function(){
      dojo.widget.Dialog.superclass.hide.apply(this, arguments);
			this.hideModalDialog();
			topaz.widget.RegionalDialog.superclass.hide.call(this);

			if(this.timer){
				clearInterval(this.timer);
			}
		},
		
		setTimerNode: function(node){
			// summary
			//	specify into which node to write the remaining # of seconds
			// TODO: make this a parameter too
			this.timerNode = node;
		},

		setMarker: function(node) {
		  // summary
		  // when specified is clicked, pass along the marker object
		  this.markerNode = node;
		},
		
		setTipUp: function(node) {
		  // summary
		  // when specified is clicked, pass along the marker object
		  this.tipUpNode = node;
		},
		
		setTipDown: function(node) {
		  // summary
		  // when specified is clicked, pass along the marker object
		  this.tipDownNode = node;
		}
	}
);
