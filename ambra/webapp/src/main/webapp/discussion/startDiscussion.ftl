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
<div id="discussionContainer">
  <!-- begin : main content -->
  <div id="content">
    <h1>Start a Discussion</h1>        
    <div class="source">
      <span>On the Article</span>
      <@s.url id="articlePageURL" action="fetchArticle" namespace="/article" articleURI="${articleInfo.id}" includeParams="none"/>
      <@s.a href="%{articlePageURL}" title="Back to original article" cssClass="article icon">${articleInfo.dublinCore.title}</@s.a>
    </div>

    <div class="original response">
      <!-- begin : posting response -->
      <div class="posting pane" id="DiscussionPanel">
        <h5>Post Your Discussion Comment</h5>
        <div class="close btn" id="btnCancelResponse"><@s.a href="%{articlePageURL}" title="Cancel and go back to original article">Cancel</@s.a></div>
          <form name="discussionResponse" method="post" action="">
          <input type="hidden" name="target" value="${articleInfo.id}" />        
          <input type="hidden" name="commentTitle" value="" />
          <input type="hidden" name="comment" value="" />
          <input type="hidden" name="isPublic" value="true" />

          <div id="responseSubmitMsg" class="error"/>                
          <fieldset>
            <legend>Compose Your Response</legend>

            <label for="responseTitle"><span class="none">Enter your comment title</span><!-- error message text <em>A title is required for all public annotations</em>--></label>
            <input type="text" name="responseTitle" id="responseTitle" value="Enter your comment title..." class="title" alt="Enter your comment title..." />

            <label for="responseArea"><span class="none">Enter your comment</span><!-- error message style <em>Please enter your response</em>--></label>
            <textarea id="responseArea" title="Enter your comment..." class="response" name="responseArea" >Enter your comment...</textarea>

            <div class="btnwrap"><input name="post" value="Post" type="button" id="btnPostResponse" title="Click to Post Your Response"/></div>
          </fieldset>
        </form>
      </div>
      <!-- end : posting response -->
    </div>
  </div>
  <!-- end : main contents -->
</div>

<#include "/widget/loadingCycle.ftl">
