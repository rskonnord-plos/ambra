<#--
  $HeadURL::                                                                            $
  $Id$

  Copyright (c) 2007-2009 by Topaz, Inc.
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
<#assign checkedstr = "checked=\"checked\"">
<#assign slctdstr = "selected=\"selected\"">
<#macro slctd tstr str><#if tstr == str>${slctdstr}</#if></#macro>
<#macro chkd tstr str><#if tstr == str>${checkedstr}</#if></#macro>
<#macro chkdlist tstr strlist><#list strlist as str><#if tstr == str>${checkedstr}</#if></#list></#macro>
<!-- begin : advanced search form -->
<div id="content" class="search">
  <!-- begin : right-hand column -->
  <div id="rhc">
    <!-- commenting out the Quick Article locator box until spec is better defined
    <div class="rhcBox">
      <h6>Quick Article Locator</h6>
      <form id="quickFind" name="quickFind" onsubmit="return true;" action="" method="post" enctype="multipart/form-data" class="" title="Quick Article Locator">
        <ol>
          <li>
            <fieldset>
              <legend><span>Go to a specific article fast if you know the following information.</span></legend>
              <label for="volNum">Volume#:</label>
              <input type="text" name="volNum" size="2" value="" id="volNum"/>,
              <label for="issueNum">Issue#:</label>
              <input type="text" name="issueNum" size="2" value="" id="issueNum"/>, <span class="noWrap"><em>and</em> <label for="eNum">E-Number:</label>
              <input type="text" name="eNum" size="15" value="" id="eNum"/></span>
            </fieldset>
          </li>
          <li>
            <label for="doi"><strong>or</strong>
            DOI: </label><input type="text" name="doi" size="25" value="" id="doi"/>
          </li>
          <li class="btnWrap"><input type="submit" id="button-find" value="Go"/></li>
        </ol>
      </form>
    </div>-->
  </div>
  <!-- end : right-hand column -->
  <!-- begin : primary content area -->
  <div class="content">
    <h1>Advanced Search</h1>
    <#assign currentJournalName = freemarker_config.getDisplayName(journalContext) />
    <p>Search the full text of all issues of <em>${currentJournalName}</em></p>
    <#if (fieldErrors?? && numFieldErrors > 0)>
      <div class="error">
        <#list fieldErrors?keys as key>
          <#list fieldErrors[key] as errorMessage>
            ${errorMessage}
          </#list>
        </#list>
      </div>
    </#if>
    <@s.url id="advSearchURL" includeParams="none" namespace="/search2" action="advancedSearch" />
    <form id="advSearchForm" name="advSearchForm" onsubmit="return true;" action="${advSearchURL}"
          method="post" enctype="multipart/form-data" class="advSearch" title="Advanced Search">
      <fieldset id="author">


        <legend><span>Search by Author</span></legend>
        <ol id="as_ol_an">
          <li>
            <@s.url id="searchHelpURL" includeParams="none" namespace="/static" action="searchHelp" />
            <label id="lblAuthorName" for="authorName">Author Name (<a href="${searchHelpURL}">help</a>): </label>
            <span id="as_anp"><input id="authorName" type="text" name="creator" size="35" value="${creatorStr?html}"/>
            <span class="controls"><span id="as_spn_ra" style="display:none;"><a id="as_a_ra" href="#" onclick="ambra.advsearch.onClickRmvAuthNameHandler(event); return false;">Remove</a><span id="as_a_spcr">&nbsp;|&nbsp;</span></span><a id="as_a_aa" href="#" onclick="ambra.advsearch.onClickAddAuthNameHandler(event); return false;">Add another author...</a></span></span>
          </li>
          <li id="as_an_opts" class="options" style="display:none;">
            <fieldset>
              <legend>Search for: </legend>
              <ol>
                <#if ((authorNameOp!"") == "any") || ((authorNameOp!"") == "")>
                  <#assign anyChecked = checkedstr>
                  <#assign allChecked = "">
                <#elseif (authorNameOp!"") == "all">
                  <#assign anyChecked = "">
                  <#assign allChecked = checkedstr>
                </#if>
                <li><label><input type="radio" name="authorNameOp" value="any" ${anyChecked} /> <em>Any</em> of these authors</label></li>
                <li><label><input type="radio" name="authorNameOp" value="all" ${allChecked} /> <em>All</em> of these</label></li>
              </ol>
            </fieldset>
          </li>
        </ol>
      </fieldset>
      <fieldset id="text">
        <legend><span>Search Article Text</span></legend>
        <ol>
          <li>
            <label for="textSearch-atLeastOne">for <em>at least one</em> of the words: </label>
            <input type="text" name="textSearchAtLeastOne" size="50" value="${textSearchAtLeastOne?html!""}" id="textSearch-atLeastOne"/>
          </li>
          <li>
            <label for="textSearch-all">for <em>all</em> the words: </label>
            <input type="text" name="textSearchAll" size="50" value="${textSearchAll?html!""}" id="textSearch-all"/>
          </li>
          <li>
            <label for="textSearch-exactPhrase">for the <em>exact phrase</em>: </label>
            <input type="text" name="textSearchExactPhrase" size="50" value="${textSearchExactPhrase?html!""}" id="textSearch-exactPhrase"/>
          </li>
          <li>
            <label for="textSearch-without"><em>without</em> the words: </label>
            <input type="text" name="textSearchWithout" size="50" value="${textSearchWithout?html!""}" id="textSearch-without"/>
          </li>
          <li>
            <label for="textSearch-where"><em>where</em> my words occur: </label>
            <select name="textSearchOption" id="textSearch-where">
              <option value="anywhere" <@slctd tstr="" str=(textSearchOption!"")/>>Anywhere in the article</option>
              <option value="abstract" <@slctd tstr="abstract" str=(textSearchOption!"")/>>In the abstract</option>
              <option value="refs" <@slctd tstr="refs" str=(textSearchOption!"")/>>In references</option>
              <option value="title" <@slctd tstr="title" str=(textSearchOption!"")/>>In the title</option>
            </select>
          </li>
        </ol>
      </fieldset>
      <fieldset id="journals">
        <legend><span>Journals</span></legend>
        <ol>
          <li><label><input id="journalsOpt_all" type="radio" name="journalOpt" value="all" <@chkd tstr="all" str=(journalOpt!"")/> /> Search all journals</label></li>
          <li><label><input id="journalsOpt_slct" type="radio" name="journalOpt" value="some" <@chkd tstr="some" str=(journalOpt!"some")/> /> Only search in the following journals:</label></li>
          <li class="options">
            <fieldset id="fsJournalOpt">
              <ul>
<#list journals as journal>
                <li><input id="limitToJournal_${journal.key}" name="limitToJournal" value="${journal.key}" type="checkbox" <@chkdlist tstr=journal.key strlist=(limitToJournal![])/>/>&nbsp;<label for="limitToJournal_${journal.key}">${freemarker_config.getDisplayName(journal.key)}</label></li>
</#list>
              </ul>
            </fieldset>
          </li>
        </ol>
      </fieldset>
      <fieldset id="subjCats">
        <legend><span>Subject Categories</span></legend>
        <ol>
          <li><label><input id="subjectCatOpt_all" type="radio" checked="checked" name="subjectCatOpt" value="all" <@chkd tstr="all" str=(subjectCatOpt!"")/> /> Search all subject categories</label></li>
          <li><label><input id="subjectCatOpt_slct" type="radio" name="subjectCatOpt" value="some" <@chkd tstr="some" str=(subjectCatOpt!"")/> /> Only search in the following subject categories:</label></li>
          <li class="options">
            <fieldset id="fsSubjectCatOpt">
              <p>(#) indicates the number of articles published in each subject category.</p>
<#if categoryInfos?size gt 0>
<#assign colSize = (categoryInfos?size / 2) + 0.5>
              <ul>
<#list categoryInfos?keys as category>
<#if (category_index + 1) lte colSize>
<#assign categoryId = category?replace("\\s|\'","","r")>
                <li><input id="limitToCategory_${categoryId}" name="limitToCategory" value="${category}" type="checkbox" <@chkdlist tstr=category strlist=(limitToCategory![])/>/>&nbsp;<label for="limitToCategory_${categoryId}">${category} (${categoryInfos[category]?size})</label></li>
</#if>
</#list>
              </ul>
              <ul>
<#list categoryInfos?keys as category>
<#if (category_index + 1) gt colSize>
<#assign categoryId = category?replace("\\s|\'","","r")>
                <li><input id="limitToCategory_${categoryId}" name="limitToCategory" value="${category}" type="checkbox" <@chkdlist tstr=category strlist=(limitToCategory![])/>/>&nbsp;<label for="limitToCategory_${categoryId}">${category} (${categoryInfos[category]?size})</label></li>
</#if>
</#list>
              </ul>
<#else>
There are no subjects in the system.
</#if>
            </fieldset>
          </li>
        </ol>
      </fieldset>
      <div class="btnwrap">
        <#-- TODO: Figure out how to 'order by' via FGS
        <label for="results-sort">Sort results by: </label>
          <select id="results-sort">
            <option value="relevance">Relevance</option>
            <option value="chron-newFirst">Newest first</option>
            <option value="chron-oldFirst">Oldest first</option>
          </select>
        -->
        <input type="button" id="button-search" value="Search"/>
      </div>
    </form>
  </div><!-- end : primary content area -->
</div><!-- end : advanced search form -->