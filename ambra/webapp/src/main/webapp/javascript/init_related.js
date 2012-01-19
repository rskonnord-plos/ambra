/*
 * $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
 * http://topazproject.org
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
dojo.require("dojo.fx");

dojo.addOnLoad(function()
  {
    if(almHost != null && almHost != '' && almHost != 'alm.example.org') {
      var almService = new ambra.alm(almHost);
      var doi = escape(dojo.byId('doi').value);

      doi = doi.replace(new RegExp('/', 'g'),'%2F');

      almService.getRelatedBlogs(doi, setRelatedBlogs);
      almService.getCites(doi, setCites);
      almService.getSocialBookMarks(doi, setBookmarks);
      almService.getIDs(doi, setIDs);
    } else {
      alert('The related article metrics server is not defined.\nMake sure the ambra/services/alm/host node is defined your ambra.xml file.')
    }
  }
);

function setBookmarks(response, args)
{
  dojo.fadeOut({ node:'relatedBookmarksSpinner', duration: 1000 }).play();

  if (response == null)
  {
    setError('relatedBookmarks');
    return;
  }

  var numRendered = 0;
  var doi = escape(dojo.byId('doi').value);

  if (response.article.sources.length > 0) {
    var html = "<ul>";

    for(var a = 0; a < response.article.sources.length; a++)
    {
      var url = response.article.sources[a].public_url;

      //Only list links that HAVE DEFINED URLS
      if (url) {
        html = html + "<li><a href=\"" + url + "\">" + response.article.sources[a].source + " (" + response.article.sources[a].count + ")</a></li>";
        numRendered++;
      }
    }

    html = html + "</ul>"
  }

  if (numRendered == 0){
    html = "<ul><li>No related bookmarks found</li></ul>";
  }

  dojo.byId('relatedBookmarks').innerHTML = html;
  dojo.fx.wipeIn({ node:'relatedBookmarks', duration: 500 }).play();
}

function setIDs(response, args)
{
  dojo.fadeOut({ node:'relatedArticlesSpinner', duration: 1000 }).play();

  if (response == null)
  {
    setError('pubMedRelatedErr');
    return;
  }

  var pubMedID = 0;

  if (response.article.pub_med) {
    pubMedID = response.article.pub_med;
    dojo.byId('pubMedRelatedURL').href="http://www.ncbi.nlm.nih.gov/sites/entrez?Db=pubmed&DbFrom=pubmed&Cmd=Link&LinkName=pubmed_pubmed&LinkReadableName=Related%20Articles&IdsFromResult=" + pubMedID + "&ordinalpos=1&itool=EntrezSystem2.PEntrez.Pubmed.Pubmed_ResultsPanel.Pubmed_RVCitation";
    dojo.fx.wipeIn({ node:'pubMedRelatedLI', duration: 500 }).play();
  }
}

function setCites(response, args)
{
  dojo.byId('relatedCites').style.display = 'none';
  dojo.fadeOut({ node:'relatedCitesSpinner', duration: 1000 }).play();

  if (response == null)
  {
    setError('relatedCites');
    return;
  }

  var numCitesRendered = 0;
  var doi = escape(dojo.byId('doi').value);

  if (response.article.sources.length > 0) {
    var html = "<ul>";

    for(var a = 0; a < response.article.sources.length; a++)
    {
      var url = response.article.sources[a].public_url;

      //Only list links that HAVE DEFINED URLS
      if (url) {
        html = html + "<li><a href=\"" + url + "\">" + response.article.sources[a].source + " (" + response.article.sources[a].count + ")</a></li>";
        numCitesRendered++;
      }
    }

    html = html + "</ul>"
  }

  if (numCitesRendered == 0){
    html = "<ul><li>No related citations found</li></ul>";
  }

  dojo.byId('relatedCites').innerHTML = html;
  dojo.fx.wipeIn({ node:'relatedCites', duration: 500 }).play();
}

function setRelatedBlogs(response, args)
{
  var html = "";

  dojo.byId('relatedBlogPosts').style.display = 'none';
  dojo.fadeOut({ node:'relatedBlogSpinner', duration: 1000 }).play();

  if (response == null)
  {
    setError('relatedBlogPosts');
    return;
  }

  var numBlogsRendered = 0;
  var doi = escape(dojo.byId('doi').value);

  if (response.article.sources.length > 0) {
    html = "<ul>";

    for(var a = 0; a < response.article.sources.length; a++)
    {
      var url = response.article.sources[a].public_url;

      //Only list links that HAVE DEFINED URLS
      if (url) {
        html = html + "<li><a href=\"" + url + "\">" + response.article.sources[a].source + " (" + response.article.sources[a].count + ")</a></li>";
        numBlogsRendered++;
      }
    }

    html = html + "</ul>"
  } else

  if (numBlogsRendered == 0) {
    html = "<ul><li>No related blog entries found</li></ul>";
  }

  dojo.byId('relatedBlogPosts').innerHTML = html;
  dojo.fx.wipeIn({ node:'relatedBlogPosts', duration: 1000 }).play();
}

function setError(textID)
{
  dojo.byId(textID).innerHTML = '<span class="inlineError"><img src="../../images/icon_error.gif"/>&nbsp;Unable to retrieve this data at this time.</span>';
  dojo.fx.wipeIn({ node:textID, duration: 1000 }).play();
}
