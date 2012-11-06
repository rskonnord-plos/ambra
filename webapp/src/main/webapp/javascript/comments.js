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
   * Show an element with a user-friendly animation.
   * @param element  the element to show
   */
  function animatedShow(element) {
    element.show("blind", 500);
  }

  /**
   * Hide an element with a user-friendly animation.
   * @param toHide  the JQuery element to hide
   * @param callback  a callback to execute (if not null/false) after finishing the animation
   */
  function animatedHide(toHide, callback) {
    toHide.hide("blind", {direction:"vertical"}, 500, callback);
  }

  /**
   * Return a reference to the JQuery page element for a reply.
   * @param replyId  the ID of the reply
   * @return {*} the element
   */
  function getReplyElement(replyId) {
    return  $('#reply-' + replyId);
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
   * Produce a clone of a JQuery element with a new "id" attribute.
   * @param selector  a selector that will find the element to clone
   * @param id  the ID to assign to the new clone, or a false/null value to remove the clone's ID
   * @return {*}  the clone
   */
  function cloneWithId(selector, id) {
    var clone = $(selector).clone();
    return (id ? clone.attr('id', id) : clone.removeAttr('id'));
  }

  /**
   * Show a drop-down box beneath a reply to prompt a user action.
   *
   * @param replyId  the reply where the box should appear
   * @param typeToHide  the type of other box to hide before showing this one
   * @param typeToShow  the type of box to show
   * @param closeSelectors  JQuery selectors for everything that should close the box when clicked
   * @param setupCallback  a function (that takes the new box as an argument) to call to finish setting it up
   */
  this.showBox = function (replyId, typeToHide, typeToShow, closeSelectors, setupCallback) {
    var reply = getReplyElement(replyId);
    reply.find('.' + typeToHide + '_box').hide(); // Keep its input in case the user switches back

    // Set up the new HTML object
    var container = reply.find('.' + typeToShow + '_box');
    if (container.data('populated')) {
      // The HTML, and possibly some user input, is already in the container. Just display it.
      animatedShow(container);
      return;
    }
    container.data('populated', true); // so that the HTML won't get overwritten if the button is clicked again

    var box = cloneWithId('#' + typeToShow + '_skeleton', null);
    setupCallback(box);

    // How to close the box and clear any input
    var closeFunction = function () {
      container.data('populated', false); // so that the HTML gets rebuilt the next time it's opened
      animatedHide(container, function () {
        box.remove(); // Avoid holding the input in memory until the box is re-opened (it would be overwritten anyway)
      });
    };
    for (var i = 0; i < closeSelectors.length; i++) {
      box.find(closeSelectors[i]).click(closeFunction);
    }

    // Display it
    container.html(box);
    box.show();
    animatedShow(container);
  };

  /**
   * Show the "report a concern" box beneath a reply, clearing the response box first if necessary.
   * @param replyId  the ID of the reply where the box should be shown
   */
  this.showReportBox = function (replyId) {
    var outer = this;
    this.showBox(replyId, 'respond', 'report', ['.btn_cancel', '.close_confirm'],
      function (box) {
        box.find('.btn_submit').click(function () {
          outer.submitReport(replyId);
        });
      });
  };

  /**
   * Show the "respond to this posting" box beneath a reply, clearing the report box first if necessary.
   * @param replyId  the ID of the reply where the box should be shown
   */
  this.showRespondBox = function (replyId, depth) {
    var replyElement = getReplyElement(replyId);
    replyElement.data('depth', depth);
    var outer = this;
    var parentTitle = replyElement.find('.response_title').text();
    this.showBox(replyId, 'report', 'respond', ['.btn_cancel'],
      function (box) {
        box.find('.btn_submit').click(function () {
          outer.submitResponse(replyId);
        });
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
    var commentData = getCommentData($('.reply'));
    commentData.target = articleDoi;

    var listThreadURL = this.addresses.listThreadURL; // make available in the local scope
    var submittedCallback = function (data) {
      window.location = listThreadURL + '?root=' + data.annotationId;
    };
    submit($('.error'), this.addresses.submitDiscussionURL, commentData, submittedCallback);
  };

  /**
   * Submit the response data from a reply's response box and show the result. Talks to the server over Ajax.
   * @param parentId  the ID of the existing reply, to which the user is responding
   */
  this.submitResponse = function (parentId) {
    var replyElement = getReplyElement(parentId);
    var commentData = getCommentData(replyElement);
    commentData.inReplyTo = parentId;

    var outer = this;
    var submittedCallback = function (data) {
      // Make a second Ajax request to get the new comment (we need its back-end representation)
      sendAjaxRequest(outer.addresses.getAnnotationURL, {annotationId:data.replyId},
        function (data, textStatus, jqXHR) {
          // Got the new comment; now add the content to the page
          outer.putComment(parentId, data.annotationId, data.annotation);
        });
    };
    var errorMsgElement = replyElement.find('.subresponse .error');

    submit(errorMsgElement, this.addresses.submitReplyURL, commentData, submittedCallback);
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
    submit(errorMsgElement, this.addresses.submitFlagURL, data, submittedCallback);
  };

  /**
   * Submit user input in general to the server.
   *
   * @param errorMsgElement  the JQuery element in which to display any error messages
   * @param submitUrl  the URL to send the Ajax request to
   * @param data  the comment's content, as an object that can be sent to the server
   * @param submittedCallback  a function to call after the comment has been submitted without errors
   */
  function submit(errorMsgElement, submitUrl, data, submittedCallback) {
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
   * @param replyElement  the existing reply, to which the user is responding, as a JQuery element
   * @return {Object}  the response data, formatted to be sent over Ajax
   */
  function getCommentData(replyElement) {
    var data = {
      commentTitle:replyElement.find('[name="comment_title"]').val(),
      comment:replyElement.find('[name="comment"]').val()
    };

    var ciRadio = replyElement.find('input:radio[name="competing"]:checked');
    data.isCompetingInterest = Boolean(ciRadio.val());
    if (data.isCompetingInterest) {
      data.ciStatement = replyElement.find('[name="competing_interests"]').val();
    }

    return data;
  }

  function padZeroes(value, width) {
    value = value.toString();
    while (value.length < width) {
      value = '0' + value;
    }
    return value;
  }

  /**
   * Add a comment in its proper place in its thread.
   * @param parentId  the ID of the comment's parent (defines where to put the new comment)
   * @param childId  the ID of the new comment
   * @param childReply  data for the new comment
   */
  this.putComment = function (parentId, childId, childReply) {
    var comment = cloneWithId('#reply-skeleton', 'reply-' + childId);

    var parentReply = getReplyElement(parentId);
    parentReply.find('.subresponse').remove();
    var childDepth = parentReply.data('depth') + 1;
    comment.data('depth', childDepth);
    comment.attr('style', 'margin-left: ' + (30 * childDepth) + 'px');

    comment.find('.response_title').text(childReply.title);
    comment.find('.response_body').html(childReply.body);

    var authorLink = comment.find('a.replyCreator');
    authorLink.text(childReply.creatorDisplayName);
    var authorHref = authorLink.attr('href');
    authorLink.attr('href', authorHref.replace(/\/\d*$/, '/' + childReply.creatorID));

    var parentAuthor = parentReply.find('a.replyCreator');
    var repliedTo = comment.find('a.repliedTo');
    repliedTo.text(parentAuthor.text());
    repliedTo.attr('href', parentAuthor.attr('href'));

    var timestamp = new Date(childReply.createdFormatted); // UTC
    var timestampFormat
      = '<strong>' + $.datepicker.formatDate('dd M yy', timestamp) + '</strong> at <strong>'
      + padZeroes(timestamp.getUTCHours(), 2) + ':' + padZeroes(timestamp.getUTCMinutes(), 2)
      + ' GMT</strong>';
    comment.find('.replyTimestamp').html($(timestampFormat));

    var outer = this;
    comment.find('.flag.btn').click(function () {
      outer.showReportBox(childId);
    });
    comment.find('.respond.btn').click(function () {
      outer.showRespondBox(childId);
    });

    // We need to set some of the raw HTML here because the skeletal reply only covers one mode
    comment.find('.competing_interests').not('textarea').html(
      (childReply.competingInterestStatement != null && childReply.competingInterestStatement.length > 0)
        ? ('<strong>Competing interests declared:</strong> ' + childReply.competingInterestStatement)
        : ('<strong>No competing interests declared.</strong>')
    );

    var replyList = getReplyListFor(parentId);
    replyList.append(comment);
    replyList.append(cloneWithId('#replies_to-skeleton', 'replies_to-' + childId));
    comment.show();
  };

};