dojo.provide("dojo.dojo_custom");

		dojo.debug("topaz.rangeInfoObj = ", topaz.annotation.rangeInfoObj);

dojo.widget.ModalDialogBase.prototype = {
	placeModalDialog: 
	function (){
		dojo.debug("inside prototype");
		var scroll_offset = dojo.html.getScroll().offset;
		var viewport_size = dojo.html.getViewport();
		
		// find the size of the dialog
		var mb = dojo.html.getMarginBox(this.containerNode);
		
		// find the size of the dialog
		var x = topaz.annotation.rangeInfoObj.pageOffsetX;
		var y = topaz.annotation.rangeInfoObj.pageOffsetY;
		//var x = scroll_offset.x + (viewport_size.width - mb.width)/2;
		//var y = scroll_offset.y + (viewport_size.height - mb.height)/2;

		dojo.debug("rangeInfoObj = ", topaz.annotation.rangeInfoObj);
		dojo.debug("x = " + x);
		dojo.debug("y = " + y);
		this.domNode.style.left = x + "px";
		this.domNode.style.top = y + "px";
	}
};


