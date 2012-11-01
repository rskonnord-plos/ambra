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

  /**
   * Duration of JQuery animations to show and hide page elements, in milliseconds.
   * @type {Number}
   */
  var DURATION = 500;

  /* 
   * Must be supplied from the calling page (where it would be populated by FreeMarker).
   */
  this.addresses = null;

  /**
   * Return a reference to a JQuery page element.
   * @param elementType  the prefix of the element ID
   * @param replyId  the ID of the reply to which the element to get belongs, or null if the page has only one reply
   * @return {*} the element
   */
  function getReplyElement(elementType, replyId) {
    return (replyId == null)
      ? $('#' + elementType)
      : $('#' + elementType + '-' + replyId);
  }

  /**
   * Clear the box beneath a reply (whichever one is showing, if any).
   * @param replyId  the ID of the reply whose box should be cleared
   */
  this.clearReply = function (replyId) {
    ["report", "respond"].forEach(function (replyType) {
      getReplyElement(replyType, replyId).hide("blind", {direction:"vertical"}, DURATION)
    });
  };

  /**
   * Hide a box beneath a reply, then show one.
   * @param from  the type of box to hide
   * @param to  the type of box to show
   * @param replyId  the ID of the reply to which the boxes belong
   */
  function switchReplyBox(from, to, replyId) {
    getReplyElement(from, replyId).hide();
    getReplyElement(to, replyId).show("blind", DURATION);
  }

  /**
   * Show the "report a concern" box beneath a reply, clearing the response box first if necessary.
   * @param replyId  the ID of the reply where the box should be shown
   */
  this.showReportBox = function (replyId) {
    switchReplyBox("respond", "report", replyId);
  };

  /**
   * Show the "respond to this posting" box beneath a reply, clearing the report box first if necessary.
   * @param replyId  the ID of the reply where the box should be shown
   */
  this.showRepondBox = function (replyId) {
    switchReplyBox("report", "respond", replyId);
  };

  /**
   * Submit a top-level response to an article and show the result. Talks to the server over Ajax.
   * @param articleDoi the DOI of the article to which the user is responding
   */
  this.submitDiscussion = function (articleDoi) {
    var commentData = getCommentData(null);
    commentData.target = articleDoi;

    var listThreadURL = this.addresses.listThreadURL; // make available in the local scope
    var submittedCallback = function (data) {
      window.location = listThreadURL + '?root=' + data.annotationId;
    };
    sendComment(commentData, null, this.addresses.submitDiscussionURL, submittedCallback);
  };

  /**
   * Submit the response data from a reply's response box and show the result. Talks to the server over Ajax.
   * @param parentId  the ID of the existing reply, to which the user is responding
   * @param parentDepth  the tree depth of the parent reply (how many steps away from the root reply)
   */
  this.submitResponse = function (parentId, parentDepth) {
    var commentData = getCommentData(parentId);
    commentData.inReplyTo = parentId;

    var addresses = this.addresses; // make available in the local scope
    var submittedCallback = function (data) {
      // Make a second Ajax request to get the new comment (we need its back-end representation)
      $.ajax(addresses.getAnnotationURL, {
        dataType:"json",
        data:{annotationId:data.replyId},
        dataFilter:function (data, type) {
          return data.replace(/(^\s*\/\*\s*)|(\s*\*\/\s*$)/g, '');
        },
        success:function (data, textStatus, jqXHR) {
          // Got the new comment; now add the content to the page
          putComment(parentId, parentDepth, data.annotation, addresses);
        },
        error:function (jqXHR, textStatus, errorThrown) {
          alert(textStatus + '\n' + errorThrown);
        },
        complete:function (jqXHR, textStatus) {
        }
      });
    };

    sendComment(commentData, parentId, this.addresses.submitReplyURL, submittedCallback);
  };

  /**
   * Send a comment to the server. The comment may be a top-level article comment, or a response to another response.
   *
   * @param commentData  the comment's content, as an object that can be sent to the server
   * @param parentId  the ID of the parent reply, or null if the page doesn't show other replies
   * @param submitUrl  the URL to send the Ajax request to
   * @param submittedCallback  a function to call after the comment has been submitted without errors
   */
  function sendComment(commentData, parentId, submitUrl, submittedCallback) {
    var errorMsg = getReplyElement("responseSubmitMsg", parentId);
    errorMsg.hide(); // in case it was already shown from a previous attempt

    $.ajax(submitUrl, {
        dataType:"json",
        data:commentData,
        dataFilter:function (data, type) {
          return data.replace(/(^\s*\/\*\s*)|(\s*\*\/\s*$)/g, '');
        },
        success:function (data, textStatus, jqXHR) {
          var errors = new Array();
          for (var errorKey in data.fieldErrors) {
            errors.push(data.fieldErrors[errorKey]);
          }
          if (errors.length > 0) {
            errorMsg.html(errors.join('<br/>'));
            errorMsg.show("blind", DURATION);
          } else {
            submittedCallback(data);
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

  /**
   * Pull the input for a submitted comment from the page.
   * @param parentId  the ID of the existing reply, to which the user is responding
   * @return {Object}  the response data, formatted to be sent over Ajax
   */
  function getCommentData(parentId) {
    var data = {
      commentTitle:getReplyElement("comment_title", parentId).val(),
      comment:getReplyElement("comment", parentId).val()
    };

    if (getReplyElement("no_competing", parentId).attr("checked")) {
      data.isCompetingInterest = false;
    } else {
      data.isCompetingInterest = true;
      data.ciStatement = getReplyElement("competing_interests", parentId).val();
    }

    return data;
  }

  /**
   * Add a comment in its proper place in its thread.
   * @param parentId  the ID of the comment's parent (defines where to put the new comment)
   * @param reply  data for the new response (currently from AnnotationView; TODO finalize contract)
   */
  function putComment(parentId, parentDepth, reply, addresses) {
    var replyId = 0; // reply.ID; // TODO Why is this not provided from Ajax call?
    var commentHtml = [
      '<div class="response" style="margin-left: ', 30 * (parentDepth + 1), 'px">\n',
      '  <div class="info">\n',
      '    <h3>', reply.title, '</h3>\n',
      '    <h4>\n',
      '      <a href="', addresses.showUserURL, '" class="user icon">${reply.creatorDisplayName}</a>\n',
      '      replied to\n',
      '      <a href="', addresses.authorURL, '" class="user icon">${replyToAuthorName}</a>\n',
      '      on <strong>${reply.created?string("dd MMM yyyy \'</strong>at<strong>\' HH:mm zzz")}</strong>\n',
      '    </h4>\n',
      '      <div class="arrow"></div>\n',
      '  </div>\n',
      '  <div class="response_content">\n',

      // TODO: Why does the Ajax call populate "truncated" fields but not body and competingInterestStatement?
      reply.truncatedBody,
      '      <div class="competing_interests">\n',
      reply.truncatedCompetingInterestStatement != null && reply.truncatedCompetingInterestStatement.length > 0
        ? '<strong>Competing interests declared:</strong> ' + reply.truncatedCompetingInterestStatement
        : '<strong>No competing interests declared.</strong>\n',

      '      </div>\n',
      '  </div>\n',

      '  <div class="toolbar">\n',
      '      <a href="', addresses.loginURL, '"\n',
      '         onclick="comments.showReportBox(\'', replyId, '\'); return false;"\n',
      '         class="flag tooltip btn" title="Report a Concern">\n',
      '        report a concern\n',
      '      </a>\n',
      '      <a href="', addresses.loginURL, '"\n',
      '         onclick="comments.showRepondBox(\'', replyId, '\'); return false;"\n',
      '         class="respond tooltip btn" title="Click to respond">\n',
      '        respond to this posting\n',
      '      </a>\n',
      '  </div>\n',

      '  <div class="reply review cf" id="report-', replyId, '" style="display: none">\n',
      '    <div id="flagForm">\n',
      '      <h4>Why should this posting be reviewed?</h4>\n',
      '      <div class="reply_content">\n',
      '        <p>See also Guidelines for Notes, Comments, and Corrections:</p>\n',
      '      </div>\n',
      '      <form class="cf">\n',
      '        <fieldset class="">\n',
      '          <div class="cf">\n',
      '            <input type="radio" name="reason" value="spam" id="spam"/>\n',
      '            <label for="spam">Spam</label>\n',
      '          </div>\n',
      '          <div class="cf">\n',
      '            <input type="radio" name="reason" value="offensive" id="offensive"/>\n',
      '            <label for="offensive">Offensive</label>\n',
      '          </div>\n',
      '          <div class="cf">\n',
      '            <input type="radio" name="reason" value="inappropriate" id="inappropriate"/>\n',
      '            <label for="inappropriate">Inappropriate</label>\n',
      '          </div>\n',
      '          <div class="cf">\n',
      '            <input type="radio" name="reason" value="other" id="other"/>\n',
      '            <label for="other">Other</label>\n',
      '          </div>\n',
      '          <textarea placeholder="Add any additional information here..." name="additional_info"></textarea>\n',
      '          <span class="btn btn_cancel" onclick="comments.clearReply(', replyId, ')">cancel</span>\n',
      '          <span class="btn">submit</span>\n', // TODO
      '        </fieldset>\n',
      '      </form>\n',
      '    </div>\n',
      '    <div id="flagConfirm" style="display: none;">\n',
      '      <h4>Thank You!</h4>\n',
      '      <p>Thank you for taking the time to flag this posting; we review flagged postings on a regular basis.</p>\n',
      '      <span class="close_confirm">close</span>\n',
      '    </div>\n',
      '  </div>\n',
      '  <div class="reply cf" id="respond-', replyId, '" style="display: none">\n',
      '    <h4>Post Your Discussion Comment</h4>\n',
      '    <div class="reply_content">\n',
      '      <p>Please follow our\n',
      '        <a href="', addresses.commentGuidelinesURL, '">guidelines for notes and comments</a>\n',
      '        and review our\n',
      '        <a href="', addresses.competingInterestURL, '">competing interests policy</a>.\n',
      '        Comments that do not conform to our guidelines will be promptly removed and the users account disabled. The\n',
      '        following must be avoided:\n',
      '      </p>\n',
      '      <ol>\n',
      '        <li>Remarks that could be interpreted as allegations of misconduct</li>\n',
      '        <li>Unsupported assertions or statements</li>\n',
      '        <li>Inflammatory or insulting language</li>\n',
      '      </ol>\n',
      '    </div>\n',
      '    <div id="responseSubmitMsg-', replyId, '" class="error" style="display:none;"></div>\n',
      '    <form class="cf">\n',
      '      <fieldset>\n',
      '        <input type="checkbox" name="is_correction"/><label>This is a correction.</label>\n',
      '        <input type="text" name="comment_title" placeholder="Enter your comment title..."\n',
      '               id="comment_title-', replyId, '" value="RE: ', reply.title, '">\n',
      '        <textarea name="comment" placeholder="Enter your comment..." id="comment-', replyId, '"></textarea>\n',
      '        <div class="help">\n',
      '          <p>Comments can include the following markup tags:</p>\n',
      '          <p><strong>Emphasis:</strong> <em>"italic"</em>""<strong><em>bold italic</em></strong>""</p>\n',
      '          <p><strong>Other:</strong> ^^<sup>superscript</sup>^^ ~~<sub>subscript</sub>~~</p>\n',
      '        </div>\n',
      '      </fieldset>\n',
      '      <fieldset>\n',
      '        <div class="cf"><input type="radio" name="competing" id="no_competing-', replyId, '" value="0" checked/>\n',
      '          <label for="no_competing-', replyId, '">\n',
      '            No, I don\'t have any competing interests to declare</label></div>\n',
      '        <div class="cf"><input type="radio" name="competing" value="1" id="yes_competing-', replyId, '"/>\n',
      '          <label for="yes_competing-', replyId, '">Yes, I have competing interests to declare (enter below):</label></div>\n',
      '        <textarea name="competing_interests" class="competing_interests" id="competing_interests-', replyId, '"\n',
      '                  placeholder="Enter your competing interests..."></textarea>\n',
      '        <span class="btn flt-r"\n',
      '              onclick="comments.submitResponse(', replyId, ')">\n',
      '          post</span>\n',
      '        <span class="btn flt-r btn_cancel" onclick="comments.clearReply(', replyId, ')">cancel</span>\n',
      '      </fieldset>\n',
      '    </form>\n',
      '  </div>\n',
      '</div>\n',
      '<div id="replies_to-', replyId, '">\n',
      '</div>'
    ];
    getReplyElement("replies_to", parentId).append($(commentHtml.join('')));
  }

};