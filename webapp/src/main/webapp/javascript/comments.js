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

  /**
   * Table mapping reply IDs to their page depths. Necessary for placing newly posted replies on the page.
   * @type {Object}
   */
  var depths = {};

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
      ? $('.reply')
      : $('#reply-' + replyId);
  }

  /**
   * Return a reference to the JQuery div that holds the replies to a parent comment.
   * @param parentId  the non-null ID of the parent comment
   * @return {*} the reply list div
   */
  function getReplyListFor(parentId) {
    return $('#replies_to-' + parentId);
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
    showBox(replyId, 'respond', 'report',
      function (box) {
        box.find('.btn_submit').attr('onclick', 'comments.submitReport(' + replyId + ')');
        box.find('.close_confirm').attr('onclick', 'comments.clearReply(' + replyId + ')');
      });
  };

  /**
   * Show the "respond to this posting" box beneath a reply, clearing the report box first if necessary.
   * @param replyId  the ID of the reply where the box should be shown
   */
  this.showRespondBox = function (replyId, depth) {
    depths[replyId] = depth;
    var parentTitle = getReplyElement(replyId).find('.response_title').text();
    showBox(replyId, 'report', 'respond',
      function (box) {
        box.find('.btn_submit').attr('onclick', 'comments.submitResponse(' + replyId + ')');
        box.find('[name="comment_title"]').attr("value", 'RE: ' + parentTitle);
      });
  };

  /**
   * Send an Ajax request to the server, using parameters appropriate to this page.
   * @param url  the URL to send the Ajax request to
   * @param data  an object to send as the request data
   * @param success  callback for success (per $.ajax)
   */
  function sendAjaxRequest(url, data, success) {
    $.ajax(url, {
      dataType:"json",
      data:data,
      dataFilter:function (data, type) {
        // Remove block comment from around JSON, if present
        return data.replace(/(^\s*\/\*\s*)|(\s*\*\/\s*$)/g, '');
      },
      success:success,
      error:function (jqXHR, textStatus, errorThrown) {
        alert(textStatus + '\n' + errorThrown);
      },
      complete:function (jqXHR, textStatus) {
      }
    });
  }

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
    submit($('.error'), this.addresses.submitDiscussionURL, null, commentData, submittedCallback);
  };

  /**
   * Submit the response data from a reply's response box and show the result. Talks to the server over Ajax.
   * @param parentId  the ID of the existing reply, to which the user is responding
   */
  this.submitResponse = function (parentId) {
    var commentData = getCommentData(parentId);
    commentData.inReplyTo = parentId;

    var addresses = this.addresses; // make available in the local scope
    var submittedCallback = function (data) {
      // Make a second Ajax request to get the new comment (we need its back-end representation)
      sendAjaxRequest(addresses.getAnnotationURL, {annotationId:data.replyId},
        function (data, textStatus, jqXHR) {
          // Got the new comment; now add the content to the page
          putComment(parentId, data.annotationId, data.annotation, addresses);
        });
    };
    var errorMsgElement = getReplyElement(parentId).find('.subresponse .error');

    submit(errorMsgElement, this.addresses.submitReplyURL, parentId, commentData, submittedCallback);
  };

  /**
   * Submit a report (flag) from a reply's "report a concern" button and show the result.
   * @param replyId  the ID of the reply being flagged
   */
  this.submitReport = function (replyId) {
    var reply = getReplyElement(replyId);
    var data = {
      target:replyId,
      reasonCode:reply.find('input:radio[name="reason"]:checked').val(),
      comment:reply.find('[name="additional_info"]').val()
    };
    var reportDialog = reply.find('.review');
    var errorMsgElement = reportDialog.find('.error');
    var submittedCallback = function (data) {
      reportDialog.find('.flagForm').hide();
      animatedShow(reportDialog.find('.flagConfirm'));
    };
    submit(errorMsgElement, this.addresses.submitFlagURL, replyId, data, submittedCallback);
  };

  /**
   * Submit user input in general to the server.
   *
   * @param errorMsgElement  the JQuery element in which to display any error messages
   * @param submitUrl  the URL to send the Ajax request to
   * @param parentId  the ID of the parent reply, or null if the page doesn't show other replies
   * @param data  the comment's content, as an object that can be sent to the server
   * @param submittedCallback  a function to call after the comment has been submitted without errors
   */
  function submit(errorMsgElement, submitUrl, parentId, data, submittedCallback) {
    errorMsgElement.hide(); // in case it was already shown from a previous attempt

    sendAjaxRequest(submitUrl, data,
      function (data, textStatus, jqXHR) {
        // No errors between client and server successfully, but there may be user validation errors.
        var errors = new Array();
        for (var errorKey in data.fieldErrors) {
          errors.push(data.fieldErrors[errorKey]);
        }
        if (errors.length > 0) {
          errorMsgElement.html(errors.join('<br/>'));
          animatedShow(errorMsgElement);
        } else {
          // No validation errors, meaning the comment was submitted successfully and persisted.
          submittedCallback(data);
        }
      });
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
   * @param childId  the ID of the new comment
   * @param childReply  data for the new comment
   */
  function putComment(parentId, childId, childReply, addresses) {
    var childDepth = depths[parentId] + 1;
    depths[childId] = childDepth;
    var comment = $('#reply-skeleton').clone();

    comment.attr('id', 'reply-' + childId);
    comment.attr('style', 'margin-left: ' + (30 * childDepth) + 'px');
    comment.find('.response_title').text(childReply.title);
    comment.find('.response_body').html(childReply.body);

    comment.find('.flag.btn').attr('onclick', 'comments.showReportBox(' + childId + ')');
    comment.find('.respond.btn').attr('onclick', 'comments.showRespondBox(' + childId + ', ' + childDepth + ')');

    // We need to set some of the raw HTML here because the skeletal reply only covers one mode
    comment.find('.competing_interests').html(
      (childReply.competingInterestStatement != null && childReply.competingInterestStatement.length > 0)
        ? ('<strong>Competing interests declared:</strong> ' + childReply.competingInterestStatement)
        : ('<strong>No competing interests declared.</strong>')
    );

    getReplyListFor(parentId).append(comment);
    comment.show();
  }

};