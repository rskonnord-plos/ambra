<div dojoType="topaz:regionalDialog" id="CommentDialog" bgColor="white" bgOpacity="0" toggle="fade" toggleDuration="250" closeOnBackgroundClick="false" style="padding:0;margin:0;">
	<!-- begin : annotation preview view (wireframe page 12) [SIMPLE] -->
	<div class="dialog preview">
		<div class="tipu" id="cTipu"></div>
		<div class="btn close" id="btn_close" title="Click to close">
			<a title="Click to close">Close</a>
		</div>
		<div class="comment">
			<h6 id="viewCmtTitle"></h6>
			<div class="detail" id="viewCmtDetail"></div>
			<div class="contentwrap" id="viewComment"></div>
			<div class="detail" id="viewLink">
				<!--<a href="#" class="commentary icon" title="Click to view full thread and respond">View all responses</a>
				<a href="#" class="respond tooltip" title="Click to respond to this posting">Respond to this</a>-->
			</div>
		</div>
		<div class="tip" id="cTip"></div>
	</div>
	<!-- end : annotation preview view -->
</div>

<!-- begin : annotation preview view (wireframe page 12) [MULTIPLE] -->

<div dojoType="topaz:regionalDialog" id="CommentDialogMultiple" bgColor="white" bgOpacity="0" toggle="fade" toggleDuration="250" style="padding:0;margin:0;">
	<div class="dialog multiple preview">
		<div class="tipu" id="mTipu"></div>
		<div class="btn close" id="btn_close_multi" title="Click to close">
			<a title="Click to close">Close</a>
		</div>
		<ol id="multilist">
		</ol>
		<br/>
		<div id="multidetail">
		</div>
		<!-- display the following div only if there are more than four annotations in the same point -->
		<div class="tip" id="mTip"></div>
	</div>
</div>
<!-- end : annotation preview view -->


