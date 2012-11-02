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

  /*
   * Must be supplied from the calling page (where it would be populated by FreeMarker).
   */
  this.addresses = null;

  function animatedShow(element) {
    element.show("blind", 500);
  }

  function animatedHide(element) {
    element.hide("blind", {direction:"vertical"}, 500);
  }

  /**
   * Return a reference to the JQuery page element for a reply.
   * @param replyId  the ID of the reply, or null if the page has only one reply
   * @return {*} the element
   */
  function getReplyElement(replyId) {
    return (replyId == null)
      ? $('#reply')
      : $('#reply-' + replyId);
  }

  /**
   * Clear the box beneath a reply (whichever one is showing, if any).
   * @param replyId  the ID of the reply whose box should be cleared
   */
  this.clearReply = function (replyId) {
    var reply = getReplyElement(replyId);
    animatedHide(reply.find('.report_box'));
    animatedHide(reply.find('.respond_box'));
  };

  /**
   * Show a drop-down box beneath a reply to prompt a user action.
   * @param replyId  the reply where the box should appear
   * @param typeToHide  the type of other box to hide before showing this one
   * @param typeToShow  the type of box to show
   * @param setupCallback  a function (that takes the new box as an argument) to call to finish setting it up
   */
  function showBox(replyId, typeToHide, typeToShow, setupCallback) {
    var reply = getReplyElement(replyId);
    reply.find('.' + typeToHide + '_box').hide();

    // Set up the new HTML object
    var container = reply.find('.' + typeToShow + '_box');
    var box = $('#' + typeToShow + '_skeleton').clone();
    box.find('.btn_cancel').attr('onclick', 'comments.clearReply(' + replyId + ')');
    setupCallback(box);

    // Display it
    container.html(box);
    container.show();
    animatedShow(box);
  }

  /**
   * Show the "report a concern" box beneath a reply, clearing the response box first if necessary.
   * @param replyId  the ID of the reply where the box should be shown
   */
  this.showReportBox = function (replyId) {
    showBox(replyId, 'respond', 'report', function (box) {
      // TODO
    });
  };

  /**
   * Show the "respond to this posting" box beneath a reply, clearing the report box first if necessary.
   * @param replyId  the ID of the reply where the box should be shown
   */
  this.showRespondBox = function (replyId) {
    var parentTitle = getReplyElement(replyId).find('h3').text();
    showBox(replyId, 'report', 'respond', function (box) {
      box.find('.btn_submit').attr('onclick', 'comments.submitResponse(' + replyId + ')');
      box.find('[name="comment_title"]').attr("value", 'RE: ' + parentTitle);
    });
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
  this.submitResponse = function (parentId) {
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
    var errorMsg = getReplyElement(parentId).find('.error');
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
            animatedShow(errorMsg);
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
    var parent = getReplyElement(parentId);

    var data = {
      commentTitle:parent.find('[name="comment_title"]').val(),
      comment:parent.find('[name="comment"]').val()
    };

    var ciRadio = parent.find('input:radio[name="competing"]:checked');
    data.isCompetingInterest = Boolean(ciRadio.val());
    if (data.isCompetingInterest) {
      data.ciStatement = parent.find('[name="competing_interests"]').val();
    }

    return data;
  }

  /**
   * Add a comment in its proper place in its thread.
   * @param parentId  the ID of the comment's parent (defines where to put the new comment)
   * @param reply  data for the new response (currently from AnnotationView; TODO finalize contract)
   */
  function putComment(parentId, parentDepth, reply, addresses) {
    alert("Unimplemented"); // TODO
  }

};