/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2012 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$.fn.comments = function () {

  var DURATION = 500; // Duration of JQuery animations, in milliseconds

  this.clearReply = function (replyId) {
    ["report", "respond"].forEach(function (replyType) {
      $("#" + replyType + "-" + replyId).hide("blind", {direction:"vertical"}, DURATION)
    });
  }

  this.report = function (replyId) {
    $("#respond-" + replyId).hide();
    $("#report-" + replyId).show("blind", DURATION);
  }

  this.respond = function (replyId) {
    $("#report-" + replyId).hide();
    $("#respond-" + replyId).show("blind", DURATION);
  }

  this.submitResponse = function (parentId, submitUrl) {
    var errorMsg = $('#responseSubmitMsg-' + parentId);
    errorMsg.hide(); // in case it was already shown from a previous attempt

    var responseToPost = this.getResponseData(parentId);
    var ajax = $.ajax(submitUrl, {
        dataType:"json",
        data:responseToPost,
        dataFilter:function (data, type) {
          return data.replace(/(^\s*\/\*\s*)|(\s*\*\/\s*$)/g, '');
        },
        success:function (data, textStatus, jqXHR) {
          var errors = Array();
          for (var errorKey in data.fieldErrors) {
            errors.push(data.fieldErrors[errorKey]);
          }
          if (errors.length > 0) {
            errorMsg.html(errors.join('<br/>'));
            errorMsg.show("blind", DURATION);
          } else {
            putNewResponse(parentId, responseToPost);
          }
        },
        error:function (jqXHR, textStatus, errorThrown) {
          alert(textStatus + '\n' + errorThrown);
        },
        complete:function (jqXHR, textStatus) {
        }
      }
    );
  }

  this.putNewResponse = function (parentId, newResponse) {
    var html = [
        '<div class="response">',
        '  <div class="info">',
        '    <h3>', newResponse.commentTitle, '</h3>',
        '    <h4>',
        '      <a href="{showUserURL}" class="user icon">{reply.creatorDisplayName}</a>',
        '      replied to',
        '      <a href="{authorURL}" class="user icon">{replyToAuthorName}</a>',
        '      on <strong>{reply.created?string("dd MMM yyyy </strong>at<strong> HH:mm zzz")}</strong>',
        '    </h4>',
        '      <div class="arrow"></div>',
        '  </div>',
        '  <div class="response_content">',
        newResponse.comment,
        '      <div class="competing_interests">',
        newResponse.isCompetingInterest
          ? '  <strong>Competing interests declared:</strong> ' + newResponse.ciStatement
          : '  <strong>No competing interests declared.</strong>',
        '      </div>',
        '  </div>',
        '</div>'
      ]
      ;
    $('#replies_to-' + parentId).append($(html.join(' ')));
  }

  this.getResponseData = function (parentId) {
    var data = {
      inReplyTo:parentId,
      commentTitle:$('#comment_title-' + parentId).val(),
      comment:$('#comment-' + parentId).val()
    };

    if ($('#no_competing-' + parentId).attr("checked")) {
      data.isCompetingInterest = false;
    } else {
      data.isCompetingInterest = true;
      data.ciStatement = $('#competing_interests-' + parentId).val();
    }

    return data;
  }

}
