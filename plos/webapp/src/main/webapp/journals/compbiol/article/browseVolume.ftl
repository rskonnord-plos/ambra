<div id="content" class="archive">
    <h1>Journal Archive</h1>
    <p>This archive contains links to the full-text of all issues of 
    <em>${freemarker_config.getDisplayName(journalContext)}.</em></p>
  <!-- begin : search results -->
  <div id="browse-results">
    <h2>Current Issue</h2>
    <div id="issueImage">
      <div id="thumbnail">
      
        <@s.url id="currentIssueURL" action="browseIssue" namespace="/journals/ntd/article"
                issue="${currentIssue.id}" includeParams="none"/>
<#if currentIssue.imageArticle?exists>
        <@s.url id="currentIssueImgURL" action="fetchObject" namespace="/article" 
                uri="${currentIssue.imageArticle}.g001" representation="PNG_S" includeParams="none"/>
        <a href="${currentIssueURL}"><img alt="Issue Image" src="${currentIssueImgURL}" width="120" height="120" /></a>
</#if>
        <a href="${currentIssueURL}">${currentIssue.displayName}</a>
        <p>${currentVolume.displayName}</p>
      </div>
      <h3>About This Image</h3>
      ${currentIssue.description}
      <div class="clearer">&nbsp;</div>
    </div>

<!-- Example Dynamic Drive Tabs -->

<#if volumeInfos?exists>
    <h2>Past Issues</h2>
    <div class="plosTabsContainer">
    <ul id="volumeTabs" class="plostabs">
      <#assign volNum = 1/>
      <#list volumeInfos as volumeInfo>
        <li><a href="#" rel="Vol_${volNum}" >${volumeInfo.displayName}</a></li>
        <#assign volNum = volNum +1/>
      </#list>
    </ul>

    <!-- <div style="border:1px solid gray; width:100%; margin-bottom: 1em; padding: 10px"> -->
    <#assign volNum = 1/>
    <#list volumeInfos as volumeInfo>
      <div class="plosTabPane" id="Vol_${volNum}">
        <#assign issueNum = 1/>
        <#list volumeInfo.issueInfos as issueInfo>
          <@s.url id="issueURL" action="browseIssue" namespace="/journals/ntd/article"
                  issue="${issueInfo.id}" includeParams="none"/>
          <div class="thumbnail">
<#if issueInfo.imageArticle?exists>
            <@s.url id="issueImgURL" action="fetchObject" namespace="/article" 
                    uri="${issueInfo.imageArticle}.g001" representation="PNG_S" includeParams="none"/>
            <a href="${issueURL}"><img alt="Issue Image" src="${issueImgURL}" width="120" height="120"/></a>
</#if>
            <a href="${issueURL}">${issueInfo.displayName}</a>
            <!-- <p>Vol.${volNum}, No.${issueNum}</p> -->
          </div>
          <#assign issueNum = issueNum +1 />
        </#list> <!-- end of listing issues within volume -->
        <div class="clearer">&nbsp;</div>
      </div>
      <#assign volNum = volNum +1/>
    </#list>
    </div> 
    </div> <!-- end: plosTabsContainer -->

<div class="clearer">&nbsp;</div>

<script type="text/javascript">
var volTabs=new ddtabcontent("volumeTabs")
volTabs.setpersist(true)
volTabs.setselectedClassTarget("linkparent") //"link" or "linkparent"
volTabs.init()
<#if volNum gt 1>
volTabs.expandit(0) // select the first tab (if there is one or more defined)
</#if>
</script>

</#if><!-- end : volumeInfos?exists -->
  </div> <!-- end : search results -->
</div> <!--content-->
