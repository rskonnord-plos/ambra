  <div id="browseNav">
    <div>
      <form class="browseForm" action="browse.action" method="get" name="browseForm">
        <fieldset>
          <legend>How would you like to browse?</legend>
          <ol>
            <li><label for="date"><input type="radio" onclick="document.browseForm.submit()" name="field" id="date" value="date" /> By Publication Date</label></li>
            <li><label for="subject"><input type="radio" name="field" id="subject" value="subject" checked="checked" /> By Subject</label></li>
          </ol>
        </fieldset>
      </form>
    </div>

    <ul class="subjects">
      <#assign infoText = "">
      <#list categoryInfos?keys as subjectName>
      <#if catName == subjectName>
        <li class="current">${subjectName} (${categoryInfos[subjectName]})</li>
        <#assign infoText = "in <strong>" + subjectName+ "</strong>">
      <#else>
        <@s.url id="browseURL" action="browse" namespace="/article" field="${field}" catName="${subjectName}" includeParams="none"/>
        <li><@s.a href="%{browseURL}">${subjectName} (${categoryInfos[subjectName]})</@s.a></li>
      </#if>

      </#list>
    </ul>
  </div> <!-- browse nav -->

