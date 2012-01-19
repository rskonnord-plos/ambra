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
  <div id="browseNav">

    <div>
      <form class="browseForm" action="browse.action" method="get" name="browseForm">
        <fieldset>
          <legend>How would you like to browse?</legend>
          <ol>
            <li><label for="date"><input type="radio" name="field" id="date" checked="checked" /> By Publication Date</label></li>
            <li><label for="subject"><input onclick="document.browseForm.submit();" type="radio" name="field" id="subject" /> By Subject</label></li>
          </ol>
        </fieldset>
      </form>
    </div>
    <#assign infoText = "">
    <ul>
      <#if year == -1 && month == -1 && day == -1>
      <#assign infoText = "in the <strong>past week</strong>">
      <li class="current">Browse past week</li>
      <#else>
      <@s.url id="browseDateURL" action="browse" namespace="/article" field="${field}" includeParams="none"/>
      <li><@s.a href="%{browseDateURL}">Browse past week</@s.a></li>
      </#if>
    </ul>
    <form id="selectPubDate" class="" title="Select Publication Date" enctype="multipart/form-data" method="post" action="" onsubmit="return true;" name="selectPubDate">
      <fieldset>
        <legend>Browse by publication date</legend>
        <div>
          <label for="yearSelect">Year</label>
          <select id="yearSelect" name="yearSelect">
            <option selected="selected" value="2008">2008</option>
            <option value="2007">2007</option>
            <option value="2006">2006</option>
            <option value="2005">2005</option>
          </select>
        </div>
        <div>
          <label for="quarterSelect">Quarter</label>
          <select id="yearSelect" name="quarterSelect">
            <option selected="selected" value="q3">Jul-Sep</option>
            <option value="q2">Apr-Jun</option>
            <option value="q1">Jan-Mar</option>
          </select>
        </div>
      </fieldset>
    </form>  

<#if articleDates?exists>
    <ol>
    <#list articleDates?keys?reverse as curYear>
      <#assign curYearStr = curYear?string("#") >
      <li>${curYearStr}</li>
      <#list articleDates(curYear)?keys?reverse as curMon>
      <li><#assign curMonStr = curMon?date("MM")?string("MMM") >
        <ol>
          <#if curYear == year && curMon == month && day == -1>
          <li class="current">
          <#assign infoText = "in <strong>" + curMonStr + " " + curYearStr + "</strong>">
          <#else>
          <li class="month">
          </#if>
          <@s.url id="monthURL" action="browse" namespace="/article" field="${field}" year="${curYear?c}" month="${curMon?c}" includeParams="none"/>
          <@s.a href="%{monthURL}">${curMonStr}</@s.a></li>
        <#list articleDates(curYear)(curMon) as curDay>
          <#assign curDayStr = curDay?string("00") >
          <#if curYear == year && curMon == month && curDay == day>
          <li class="current">
          <#assign infoText = "on <strong>" + curDayStr + " " + curMonStr + " " + curYearStr + "</strong>">
          <#else>
          <li>
          </#if>
          <@s.url id="dayURL" action="browse" namespace="/article" field="${field}" year="${curYear?c}" month="${curMon?c}" day="${curDay?c}" includeParams="none"/>
          <@s.a href="%{dayURL}">${curDayStr}</@s.a></li>
        </#list>
        </ol>
      </li>
      </#list>
    </#list>
    </ol>
</#if> <!-- articleDates != null -->

  </div> <!-- browse nav-->

