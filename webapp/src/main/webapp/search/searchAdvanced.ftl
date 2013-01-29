<#import "search_variables.ftl" as search>
<#assign checkedstr = "checked=\"checked\"">
<#assign slctdstr = "selected=\"selected\"">
<#macro slctd tstr str><#if tstr == str>${slctdstr}</#if></#macro>
<#macro chkd tstr str><#if tstr == str>${checkedstr}</#if></#macro>
<!-- begin : unformatted search form -->

<div id="pagebdy-wrap" class="bg-dk">
  <div id="pagebdy">

    <div id="search-adv-block" class="cf">
      <div class="header hdr-search-adv">
        <h1>Advanced Search</h1>
      </div>

      <div class="col-1">
        <div class="block blk-style-a blk-find-article">

          <div class="header">
            <h3>Find an Article</h3>
          </div>

          <div class="body">
            <@s.url id="quickSearchURL" includeParams="none" namespace="/search" action="quickSearch" />
            <form id="quickFind" name="quickSearchForm" action="${quickSearchURL}"
                  method="get" class="advSearch" title="Find An Article Search Form">
              <@s.hidden name="pageSize" />
              <p>Use citation information to quickly find a specific article.</p>
              <fieldset>
                <legend>Enter the following:</legend>
                <div class="selectJour">
                  <label>Journal:</label>
                  <select name="filterJournals" id="filterJournalsPicklistId" title="Journals Picklist">
                  <#if journals??>
                    <#list journals as journal>
                      <option id="filterJournalsPicklist_${journal.name}"
                              value="${journal.name}"<#if journal.name == journalContext> selected="selected"</#if>
                              title="${freemarker_config.getDisplayName(journal.name)}">
                      ${freemarker_config.getDisplayName(journal.name)}</option>
                    </#list>
                  </#if>
                  </select>
                </div>
                <ol class="text">
                  <li><label for="volumeId">Volume:</label> <span class="example">e.g.: 3</span>
                    <input type="text" name="volume" id="volumeId" value="" title="Volume"/></li>
                  <li><label for="eLocationIdId">eNumber:</label> <span class="example">e.g.: e2243</span>
                    <input type="text" name="eLocationId" id="eLocationIdId" value="" title="eNumber"/></li>
                </ol>
              </fieldset>
              <fieldset>
                <legend class="conjunction"><span>Or</span></legend>
                <ul class="text">
                  <li><label for="idId">Article DOI:</label> <span class="example">e.g.: 10.1371/journal.pone.0002243</span>
                    <input type="text" name="id" id="idId" size="17" value="" title="Article DOI"/>
                  </li>
                </ul>
              </fieldset>
              <div class="btnwrap">
                <input type="submit" id="buttonGoId" class="primary" value="Go" title="Go"/>
              </div>
            </form>
          </div>

        </div><!--block blk-style-a blk-find-article-->

        <div class="help-block">
          <h4>Instructions for "Construct Your Search"</h4>
          <ol>
            <li>Choose a field to search from the picklist.</li>
            <li>Enter search term(s).</li>
            <li>Click the AND OR or NOT buttons to add terms to the search box.</li>
            <li>Repeat steps as necessary.</li>
            <li>Select journals and/or subject categories below, if desired.</li>
            <li>Click Search to run your query, or click Preview to see the result count of your query in the Search History
              section.
            </li>
          </ol>

          <h4>Special Characters</h4>
          <ul>
            <li>The following characters have special meanings to the query engine</li>
            <li><strong> : ! &amp; " ' ^ + - | ( ) [ ] { } \ </strong></li>
            <li>Therefore, all of these characters will be "escaped" by preceding each one with a backslash character</li>
            <li>The wildcard characters <strong>?</strong> and <strong>*</strong> are <i>not</i> escaped</li>
          </ul>

          <h4>Special Words</h4>
          <ul>
            <li>The upper-case words <strong>AND</strong>, <strong>OR</strong>, <strong>NOT</strong>,
              and <strong>TO</strong> have special meanings to the query engine, so these words
              will be changed to lower-case when they are used as searchable terms
            </li>
          </ul>
        </div>

      </div> <!-- col-1 -->

      <div class="col-2">
        <@s.url id="searchHelpURL" includeParams="none" namespace="/static" action="searchHelp" />
        <@s.url id="unformattedSearchURL" includeParams="none" namespace="/search" action="advancedSearch" />
        <form id="unformattedSearchFormId" name="unformattedSearchForm" action="${unformattedSearchURL}"
              method="get" enctype="multipart/form-data" class="advSearch" title="Advanced Search">
          <@s.hidden name="pageSize" />
          <@s.hidden name="sort" />
          <@s.url id="searchHelpURL" includeParams="none" namespace="/static" action="searchHelp" />

          <fieldset id="queryBuilder">
            <legend><span>Construct Your Search <a href="${searchHelpURL}">Help</a></span></legend>

            <div>
              <select name="queryField" id="queryFieldId" title="Search Field">
                <option value="" disabled="disabled">----- Popular -----</option>
                <option value="everything" selected="selected" title="All Fields">All Fields</option>
                <option value="title" title="Title">Title</option>
                <option value="author" title="Author">Author</option>
                <option value="body" title="Body">Body</option>
                <option value="abstract" title="Abstract">Abstract</option>
                <option value="subject" title="Subject">Subject</option>
                <option value="publication_date" title="Publication Date">Publication Date</option>
                <option value="" disabled="disabled">----- Other -----</option>
                <option value="accepted_date" title="Accepted Date">Accepted Date</option>
                <option value="id" title="Article DOI">Article DOI (Digital Object Identifier)</option>
                <option value="article_type" title="Article Type">Article Type</option>
                <option value="affiliate" title="Author Affiliations">Author Affiliations</option>
                <option value="competing_interest" title="Competing Interest Statement">Competing Interest Statement</option>
                <option value="conclusions" title="Conclusions">Conclusions</option>
                <option value="editor" title="Editor">Editor</option>
                <option value="elocation_id" title="eNumber">eNumber</option>
                <option value="figure_table_caption" title="Figure &amp; Table Caption">Figure &amp; Table Captions</option>
                <option value="financial_disclosure" title="Financial Disclosure Statement">Financial Disclosure Statement
                </option>
                <option value="introduction" title="Introduction">Introduction</option>
                <option value="issue" title="Issue Number">Issue Number</option>
                <option value="materials_and_methods" title="Materials and Methods">Materials and Methods</option>
                <option value="received_date" title="Received Date">Received Date</option>
                <option value="reference" title="References">References</option>
                <option value="results_and_discussion" title="Results and Discussion">Results and Discussion</option>
                <option value="supporting_information" title="Supporting Information">Supporting Information</option>
                <option value="trial_registration" title="Trial Registration">Trial Registration</option>
                <option value="volume" title="Volume Number">Volume Number</option>
              </select>

              <span id="queryTermDivBlockId" style="display:inline;">
                <input type="text" name="queryTerm" id="queryTermId" placeholder="Enter search terms" title="Search Term"
                       value="${query}"/>
              </span>
              <span id="startAndEndDateDivBlockId" style="display:none;">
                <input type="text" name="startDateAsString" maxlength="10" placeholder="YYYY-MM-DD" id="startDateAsStringId"
                       disabled="disabled"/>
                                to
                <input type="text" name="endDateAsString" maxlength="10" placeholder="YYYY-MM-DD" id="endDateAsStringId"
                       disabled="disabled"/>
              </span>
            </div>

            <div id="queryConjunction">Add to your search with:
              <input type="button" name="queryConjunctionAnd" id="queryConjunctionAndId" value="AND" title="AND"/>
              <input type="button" name="queryConjunctionOr" id="queryConjunctionOrId" value="OR" title="OR"/>
              <input type="button" name="queryConjunctionNot" id="queryConjunctionNotId" value="NOT" title="NOT"/>
            </div>

            <div>
              <textarea name="unformattedQuery" id="unformattedQueryId" title="Query">${unformattedQuery}</textarea>
            </div>

            <div class="btnwrap">
              <input type="submit" id="buttonSearchId" class="primary" value="Search" title="Search"/>
              <input type="button" name="clearUnformattedQueryButton" id="clearUnformattedQueryButtonId" value="Clear Query"
                     title="Clear Query"/>
            </div>
          </fieldset>

          <#if filterReset>
            <fieldset id="filterReset">
              <legend><span>There are no results for this search query.</span></legend>
              <#if ((filterSubjects?size > 0) || (filterJournals?size > 0) || (filterArticleType?length > 1))>
                <ol>
                  <li>You are filtering with the following parameters:</li>

                  <#if (filterSubjects?size > 0)>
                    <li>Subject categories:
                      <b><#list filterSubjects as subject>"${subject}"
                        <#if (subject_index) gt filterSubjects?size - 3><#if subject_has_next>
                        and </#if><#else><#if subject_has_next>, </#if></#if></#list></b>
                    </li>
                  </#if>
                  <#if (filterJournals?size > 0)>
                    <li>Journals:
                      <b><#list filterJournals as journal>"${freemarker_config.getDisplayName(journal)}"
                        <#if (journal_index) gt filterJournals?size - 3><#if journal_has_next>
                          and </#if><#else><#if journal_has_next>, </#if></#if></#list></b>
                    </li>
                  </#if>
                  <#if (filterArticleType?length > 1)>
                    <li>Article Type: <b>${filterArticleType}</b></li>
                  </#if>
                  <li>
                    <div class="btnwrap">
                      <a class="btn" href="${advancedSearchURL}?<@URLParameters parameters=searchParameters names="filterKeyword,filterArticleType,filterJournals,filterSubjects,startPage" values="" />&noSearchFlag=set"/>Clear Filters</a>
                    </div>
                  </li>
                </ol>
              </#if>
            </fieldset>
          </#if>

          <fieldset id="journals">
            <legend><span>Filter by Journal</span></legend>
            <ol>
              <li><label><input id="journalsOpt_all" type="radio" name="journalOpt" value="all" <#if (filterJournals?size == 0)> checked</#if>
                                title="Search All Journals"/> Search all journals</label></li>
              <li><label><input id="journalsOpt_slct" type="radio" name="journalOpt" value="some" <#if (filterJournals?size gt 0)> checked</#if>
                                title="Search Selected Journals"/> Only search in the following journals:</label></li>
              <li class="options">
                <fieldset id="fsJournalOpt">
                  <ul>
                  <#if journals??>
                    <#list journals as journal>
                      <li>
                        <input id="filterJournals_${journal.name}" name="filterJournals" value="${journal.name}"
                               type="checkbox" <#if (filterJournals?seq_contains(journal.name)) > checked</#if>
                               title="Select ${freemarker_config.getDisplayName(journal.name)}"
                               alt="Select Journal ${freemarker_config.getDisplayName(journal.name)} Check Box"/>&nbsp;
                        <label for="filterJournals_${journal.name}">${freemarker_config.getDisplayName(journal.name)}</label>
                      </li>
                    </#list>
                  <#else>
                    <br/>
                    <span id="filterReset" style="color:red;">ERROR: There are no matching journals in the system.</span>
                  </#if>
                  </ul>
                </fieldset>
              </li>
            </ol>
          </fieldset>

          <fieldset id="subjCats">
            <legend><span>Filter by Subject Area</span></legend>
            <ol>
              <li><label><input id="subjectOption_all" type="radio" <#if (filterSubjects?size == 0) > checked</#if> name="subjectCatOpt" value="all"
                                title="Search All Subject Categories"/> Search all subject areas</label></li>
              <li><label><input id="subjectOption_some" type="radio" name="subjectCatOpt" value="some"
                                title="Search Selected Subject Categories" <#if (filterSubjects?size gt 0)> checked</#if>/>
                Only look for articles with the following subject areas:</label></li>
              <li class="options">
                <fieldset id="fsSubjectOpt">

                <#if (filterSubjects?size gt 0)>
                  <p>Listed below are all subject categories from
                    <b title="Articles that already match your entered search terms">matching</b> articles.</p></#if>
                <#if (unformattedQuery?length gt 0)>
                  <p><i>(#) indicates the number of articles with
                    <#if (filterSubjects?size lte 0)><b title="Articles that already match your entered search terms"></#if>
                    matching<#if (filterSubjects?size lte 0)></b></#if> terms in each subject.</i></p>
                <#else>
                  <p><i>(#) indicates the number of articles in each subject.</i></p>
                </#if>
                <#if subjects?? && subjects?size gt 0>
                  <#assign colSize = (subjects?size / 2) + 0.5>
                  <ul>
                    <#list subjects?sort_by("name") as subject>
                      <#if (subject_index + 1) lte colSize>
                        <#assign subjectId = subject.name?replace(" ","_","r")>
                        <li>
                          <input id="filterSubjects_${subjectId}" name="filterSubjects" value="${subject.name}"
                                 type="checkbox" <#if (filterSubjects?seq_contains(subject.name)) > checked</#if>
                                 title="Select Subject Category ${subject.name}"
                                 alt="Select Subject Category ${subject.name} Check Box"/>&nbsp;
                          <label for="filterSubjects_${subjectId}">${subject.name} (${subject.count})</label></li>
                      </#if>
                    </#list>
                  </ul>
                  <ul>
                    <#list subjects?sort_by("name") as subject>
                      <#if (subject_index + 1) gt colSize>
                        <#assign subjectId = subject.name?replace(" ","_","r")>
                        <li>
                          <input id="filterSubjects_${subjectId}" name="filterSubjects" value="${subject.name}"
                                 type="checkbox" <#if (filterSubjects?seq_contains(subject.name)) > checked</#if>
                                 title="Select Subject Category ${subject.name}"
                                 alt="Select Subject Category ${subject.name} Check Box"/>&nbsp;
                          <label for="filterSubjects_${subjectId}">${subject.name} (${subject.count})</label></li>
                      </#if>
                    </#list>
                  </ul>
                <#else>
                  <br/>
                  <span id="filterReset" style="color:red;">There are no matching subjects in the current result set.</span>
                </#if>
                </fieldset>
              </li>
            </ol>
          </fieldset>

          <fieldset id="artType">
            <legend><span>Filter by Article Type</span></legend>
            <ol>
              <li><label><input id="articleType_all" type="radio" checked="checked" name="filterArticleTypeOpt" value="all"
                <#if (filterArticleType?length == 0)> checked</#if> title="Search All Article Types"/> Search all article types</label></li>
              <li><label><input id="articleType_one" type="radio" name="filterArticleTypeOpt" value="some"
                <#if (filterArticleType?length gt 0)> checked</#if> title="Search For Only Selected Article Type"/> Search for one of the following:</label></li>
              <li class="options">
                <fieldset id="fsarticleTypOpt">
                <#if articleTypes?? && articleTypes?size gt 0>
                  <#assign colSize = (articleTypes?size / 2) + 0.5>
                  <ul>
                    <#list articleTypes?sort_by("name") as articleType>
                      <#if (articleType_index + 1) lte colSize>
                        <#assign articleTypeId = articleType.name?replace(" ","_","r")>
                        <li>
                          <input id="filterArticleType_${articleTypeId}" name="filterArticleType" value="${articleType.name}"
                                 type="radio" <#if (filterArticleType == articleType.name) > checked</#if>
                                 title="Select Article Type ${articleType.name}"
                                 alt="Select Article Type ${articleType.name} Check Box"/>&nbsp;
                          <label for="filterArticleType_${articleTypeId}">${articleType.name}</label>
                        </li>
                      </#if>
                    </#list>
                  </ul>
                  <ul>
                    <#list articleTypes?sort_by("name") as articleType>
                      <#if (articleType_index + 1) gt colSize>
                        <#assign articleTypeId = articleType.name?replace(" ","_","r")>
                        <li>
                          <input id="filterArticleType_${articleTypeId}" name="filterArticleType" value="${articleType.name}"
                                 type="radio" <#if (filterArticleType == articleType.name) > checked="true"</#if>
                                 title="Select Article Type ${articleType.name}"
                                 alt="Select Article Type ${articleType.name} Check Box"/>&nbsp;
                          <label for="filterArticleType_${articleTypeId}">${articleType.name}</label>
                        </li>
                      </#if>
                    </#list>
                  </ul>
                <#else>
                  <br/>
                  <span id="filterReset" style="color:red;">ERROR: There are no matching article types in the system.</span>
                </#if>
                </fieldset>
              </li>
            </ol>
          </fieldset>

          <div class="btnwrap">
            <input type="submit" id="buttonSearchId2" class="primary" value="Search" title="Search"/>
            <input type="button" name="clearFiltersButton2" id="clearFiltersButtonId2" value="Clear Filters"
                   title="Clear Filters"/>
          </div>

        </form>

      </div> <!-- col 2 -->
    </div> <!-- search-adv-block -->
  </div> <!-- pagebdy -->
</div> <!-- pagebdy-wrap -->
