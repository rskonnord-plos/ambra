/*
 * $HeadURL:: http://ambraproject.org/svn/plos/templates/branches/figure-improvements/jo#$
 * $Id: init_twitter.js 10044 2012-05-17 23:39:03Z mbaehr $
 *
 * Copyright (c) 2006-2010 by Public Library of Science
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


$(document).ready(function() {
  var doi = $('meta[name=citation_doi]').attr("content");

  var alm = new $.fn.alm();
  alm.getCitesTwitterOnly(doi, setTweets , setTwitterError);
});

function setTwitterError(xOptions, textStatus) {
  $("#twitterError").text("Our system is having a bad day. We are working on it. Please check back later.");
  $("#twitterError").show("blind", 1000);
}

function setTweets(json, textStatus, xOptions) {
  window.tweetsResponse = json;

  $("#spinner").fadeOut(1000);

  $("#tweets").css("display", "none");
  showTweetsPage(0);
  $("#tweets").show("blind", 1000);
}

function showTweetsPage(currentPage) {

  var json = window.tweetsResponse;
  var numTweets = 0;

  var pageSize = 50;

  $("#tweets").empty();

  if (json.article.source != null && json.article.source.length > 0
      && json.article.source[0].events != null && json.article.source[0].events.length > 0) {

    var events = json.article.source[0].events;
    events = events.sort(sort_tweets_by_date);

    numTweets = events.length;

    // example: if currentPage is 2 and numTweets is 420
    // totalPages is 9 == ceil of 420/50
    // startIndex is 100, endIndex is 149.
    var totalPages = Math.ceil(numTweets/pageSize);
    var startIndex = currentPage * pageSize;

    // if last page has less than 50 tweets, use remaining tweets.
    var endIndex = Math.min(numTweets, startIndex + pageSize);

    var ol = $('<ol></ol>');

    for (var i = startIndex; i < endIndex; i++) {
      var tweet = events[i].event;
      var tweet_url = events[i].event_url;

      var created_dt = $.format.date(Date.parse(tweet.created_at), "MMM d, yyyy");

      ol.append("<li><div><img src=\"" + tweet.user_profile_image + "\"/><span class=\"text\"><a href=\"https://twitter.com/#!/" + tweet.user + "\">"
          + tweet.user + "</a> " + linkify(tweet.text) + "</span><br/><a target='_blank' href=\""+tweet_url+"\"><span class=\"text\">" + created_dt + "</span></a></div></li>");

    }

    ol.attr("start", currentPage * pageSize + 1);
    ol.css("counter-reset", "item " + (currentPage * pageSize));

    var pagination = paging(totalPages, currentPage);
    $("#tweets").append(pagination.clone(), ol, pagination);
  }

  var statusMsg = "";
  if (numTweets < 1) {
    statusMsg = "No tweets found";
  } else {
    var pluralization = "";
    if (numTweets > 1) { // This page should never be displayed if less than 1 citation.
      pluralization = "s";
    }

    statusMsg = numTweets + " tweet" + pluralization
        + " as recorded by Twitter.  Article published "
        + $.format.date(json.article.published, "MMM d, yyyy")
        + ". Tweets updated "
        + $.format.date(json.article.source[0].updated_at, "MMM d, yyyy");
  }
  var h3 = $('<h3></h3>').html(statusMsg);
  
  $("#tweets").prepend(h3);
}

function sort_tweets_by_date(a, b){
  return  (Date.parse(b.event.created_at)) - (Date.parse(a.event.created_at));
}

function linkify(tweetText) {
  //Add an extra space so we capture urls/tags/usernames that end on the final character
  tweetText = tweetText + " ";

  //Replace URLs with a real link
  var urlRegex = /((ht|f)tp(s?):\/\/[a-zA-Z0-9\-\.\/]+?)([\s])/g;
  var newValue = tweetText.replace(urlRegex, function(match, url, eol, offset, original) {
    //console.log(url);
    //console.log(eol);
    return "<a href=\"" + url + "\">" + url + "</a> ";
  });

  //Replace tags with a link to a search for the tag
  var tagRegex = /#([A-Za-z0-9/-]+?)([:\s])/g;
  newValue = newValue.replace(tagRegex, function(match, tag, spacer, offset, original) {
    return "<a href=\"https://twitter.com/#!/search/%23" + tag + "\">#" + tag + "</a>" + spacer;
  });

  //Replace Retweet username with a link to the user profile
  var usernameRegex = /@([A-Za-z0-9_]+)([:\s])/g;
  newValue = newValue.replace(usernameRegex, function(match, username, spacer, offset, original) {
    return "<a href=\"https://twitter.com/" + username + "\">@" + username + "</a>" + spacer;
  });

  return newValue;
}

function paging(totalPages, currentPage) {
  var pagination = $("<div></div>");

  // no pagination if only one page
  if (totalPages > 1) {
    // otherwise adding pagination

    //the logic is same as in templates/journals/PLoSDefault/webapp/search/searchResults.ftl

    /*
     It supports the following use cases and will output the following:
     current page is zero based.
     page number = current page + 1.

     if current page is the start or end
     if current page is 0:
     < 1 2 3  ... 10 >
     if current page is 9:
     < 1 ...8 9 10 >

     if current page is 4:
     (Current page is greater then 2 pages away from start or end)
     < 1 ... 4 5 6 ... 10 >

     if current page is less then 2 pages away from start or end:
     current page is 7:
     < 1 ...7 8 9 10 >
     current page is 2:
     < 1 2 3 4 ... 10 >
     */

    pagination.attr("class", "pagination");

    var ellipsis = '<span>...</span>';
    var prev = '<span class="prev">&lt;</span>';
    var next = '<span class="next">&gt;</span>';

    var showTweetsCurPage = 'showTweetsPage(' + (currentPage + 1) + '); return false;';

    if (totalPages < 4) {
      // if less than 4 pages, do not put "..."
      // put < with or without link depending on whether this is first page.
      if (currentPage > 0) {
        pagination.append(pagingAnchor(showTweetsCurPage, "&lt;", "prev"));
      }
      else {
        pagination.append(prev);
      }

      // put page number for all pages
      // do not put link for current page.
      for (var pageNumber=0; pageNumber<totalPages; ++pageNumber) {
        if (pageNumber == currentPage) {
          pagination.append("<strong>" + (currentPage + 1) + "</strong>");
        }
        else {
          pagination.append(pagingAnchor('showTweetsPage(' + pageNumber + '); return false;', pageNumber + 1));
        }
      }

      // put > at the end with or without link depending on whether it is the last page.
      if (currentPage < (totalPages - 1)) {
        pagination.append(pagingAnchor(showTweetsCurPage, "&gt;", "next"));
      }
      else {
        pagination.append(next);
      }
    }
    else {
      // if >=4 pages then need to put "..."
      // put < and first page number always.
      // The link is present if this is not the first page.
      if (currentPage > 0) {
        pagination.append(pagingAnchor(showTweetsCurPage, "&lt;", "prev"));
        pagination.append(pagingAnchor('showTweetsPage(' + 0 + '); return false;', 1));
      }
      else {
        pagination.append(prev + '<strong>1</strong>');
      }
      // put the first "..." if this is more than 2 pages away from start.
      if (currentPage > 2) {
        pagination.append(ellipsis);
      }
      // put the three page numbers -- one before, current and one after
      for (var pageNumber=Math.min(currentPage, 0); pageNumber<=Math.max(3, currentPage+2); ++pageNumber) {
        if ((pageNumber > 1 && pageNumber < totalPages && pageNumber > (currentPage - 1)
            || ((pageNumber == (totalPages - 2)) && (pageNumber > (currentPage - 2))))) {
          if ((currentPage + 1) == pageNumber) {
            pagination.append("<strong>" + pageNumber + "</strong>");
          }
          else {
            pagination.append(pagingAnchor('showTweetsPage(' + (pageNumber - 1) + '); return false;', pageNumber));
          }
        }
      }
      // if this is more than 2 pages away from last page, put last "..."
      if (currentPage < (totalPages - 3)) {
        pagination.append(ellipsis);
      }
      // put the last page number and >.
      // The link depends of whether this is the last page.
      if (currentPage < (totalPages - 1)) {
        pagination.append(pagingAnchor('showTweetsPage(' + (totalPages - 1) + '); return false;', totalPages));
        pagination.append(pagingAnchor(showTweetsCurPage, "&gt;", "next"));
      }
      else {
        pagination.append('<strong>' + totalPages + '</strong>' + next);
      }
    }
  }

  return pagination;
}

function pagingAnchor(onclickLogic, pagingText, className) {

  var anchor = $('<a></a>').attr({
    href: "#",
    onclick: onclickLogic
  }).html(pagingText);

  if (className) {
    anchor.attr("class", className);
  }

  return anchor;
}
