<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2008 by Topaz, Inc.
  http://topazproject.org
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<div dojoType="dijit.Dialog" id="Rating">
  <div class="dialog annotate">
    <div class="tipu" id="dTipu"></div>
    <div class="btn close" title="Click to close and cancel"><a id="btn_cancel_rating" title="Click to close and cancel">Cancel</a></div>
    <div class="comment">
      <h5><span class="commentPublic">Rate This Article</span></h5>
      <p>See also <a href="${freemarker_config.getContext()}/static/ratingGuidelines.action" class="instructions">guidelines for rating</a>.</p>
      <div class="posting pane"><form name="ratingForm" id="ratingForm" method="post" action="">
        <input type="hidden" name="articleURI" value="${articleURI}" />  
        <input type="hidden" name="commentTitle" id="commentTitle" value="" />
        <input type="hidden" name="comment" id="commentArea" value="" />
        <fieldset>
          <legend>Compose Your Annotation</legend>
          <span id="submitRatingMsg" class="error"></span>
<#if isResearchArticle == true>
          <label for="insight">Insight</label>
          <ul class="star-rating rating edit" title="Rate insight" id="rateInsight">
            <li class="current-rating pct0"></li>
            <li><a href="javascript:void(0);" title="Bland" class="one-star" onclick="ambra.rating.setRatingCategory(this, 'insight', 1);">1</a></li>
            <li><a href="javascript:void(0);" title="" class="two-stars" onclick="ambra.rating.setRatingCategory(this, 'insight', 2);">2</a></li>
            <li><a href="javascript:void(0);" title="" class="three-stars" onclick="ambra.rating.setRatingCategory(this, 'insight', 3);">3</a></li>
            <li><a href="javascript:void(0);" title="" class="four-stars" onclick="ambra.rating.setRatingCategory(this, 'insight', 4);">4</a></li>
            <li><a href="javascript:void(0);" title="Profound" class="five-stars" onclick="ambra.rating.setRatingCategory(this, 'insight', 5);">5</a></li>
          </ul>    
          <input type="hidden" name="insight" title="insight" value="" />
          <label for="reliability">Reliability</label>
          <ul class="star-rating rating edit" title="Rate reliability" id="rateReliability">
            <li class="current-rating pct0"></li>
            <li><a href="javascript:void(0);" title="Tenuous" class="one-star" onclick="ambra.rating.setRatingCategory(this, 'reliability', 1);">1</a></li>
            <li><a href="javascript:void(0);" title="" class="two-stars" onclick="ambra.rating.setRatingCategory(this, 'reliability', 2);">2</a></li>
            <li><a href="javascript:void(0);" title="" class="three-stars" onclick="ambra.rating.setRatingCategory(this, 'reliability', 3);">3</a></li>
            <li><a href="javascript:void(0);" title="" class="four-stars" onclick="ambra.rating.setRatingCategory(this, 'reliability', 4);">4</a></li>
            <li><a href="javascript:void(0);" title="Unassailable" class="five-stars" onclick="ambra.rating.setRatingCategory(this, 'reliability', 5);">5</a></li>
          </ul>    
          <input type="hidden" name="reliability" title="reliability" value="" />
          <label for="style">Style</label>
          <ul class="star-rating rating edit" title="Rate style" id="rateStyle">
            <li class="current-rating pct0"></li>
            <li><a href="javascript:void(0);" title="Crude" class="one-star" onclick="ambra.rating.setRatingCategory(this, 'style', 1);">1</a></li>
            <li><a href="javascript:void(0);" title="" class="two-stars" onclick="ambra.rating.setRatingCategory(this, 'style', 2);">2</a></li>
            <li><a href="javascript:void(0);" title="" class="three-stars" onclick="ambra.rating.setRatingCategory(this, 'style', 3);">3</a></li>
            <li><a href="javascript:void(0);" title="" class="four-stars" onclick="ambra.rating.setRatingCategory(this, 'style', 4);">4</a></li>
            <li><a href="javascript:void(0);" title="Elegant" class="five-stars" onclick="ambra.rating.setRatingCategory(this, 'style', 5);">5</a></li>
          </ul>    
          <input type="hidden" name="style" title="style" value="" />
<#else>
          <label for="singleRating">Rating</label>
          <ul class="star-rating rating edit" title="Rate single" id="rateSingleRating">
            <li class="current-rating pct0"></li>
            <li><a href="javascript:void(0);" title="Bland" class="one-star" onclick="ambra.rating.setRatingCategory(this, 'singleRating', 1);">1</a></li>
            <li><a href="javascript:void(0);" title="" class="two-stars" onclick="ambra.rating.setRatingCategory(this, 'singleRating', 2);">2</a></li>
            <li><a href="javascript:void(0);" title="" class="three-stars" onclick="ambra.rating.setRatingCategory(this, 'singleRating', 3);">3</a></li>
            <li><a href="javascript:void(0);" title="" class="four-stars" onclick="ambra.rating.setRatingCategory(this, 'singleRating', 4);">4</a></li>
            <li><a href="javascript:void(0);" title="Profound" class="five-stars" onclick="ambra.rating.setRatingCategory(this, 'singleRating', 5);">5</a></li>
          </ul>    
          <input type="hidden" name="singleRating" title="singleRating" value="" />
</#if>
          <label for="cTitle" class="commentPublic"><span class="none">Enter your comment title</span><!-- error message text <em>A title is required for all public annotations</em>--></label>
          <input type="text" name="cTitle" id="cTitle" value="Enter your comment title..." class="title commentPublic" alt="Enter your comment title..." />
          <label for="cArea"><span class="none">Enter your comment</span><!-- error message text <em>Please enter your annotation</em>--></label>
          <textarea name="cArea" id="cArea" value="Enter your comment..." alt="Enter your comment...">Enter your comment...</textarea>
          <div class="btnwrap commentPublic"><input type="button" value="Post" class="commentPublic" title="Click to post your annotation publicly" id="btn_post_rating"/></div>
        </fieldset>
      </form></div>
    </div>
  </div>
</div>
