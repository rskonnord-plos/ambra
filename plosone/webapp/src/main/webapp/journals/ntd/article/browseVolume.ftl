<div id="content" class="browse static">
  <!-- begin : banner -->
  <div id="bannerRight">
    <script language='JavaScript' type='text/javascript'src='http://ads.plos.org/adx.js'></script>
    <script language='JavaScript' type='text/javascript'>
      <!--
        if (!document.phpAds_used) document.phpAds_used = ',';
        phpAds_random = new String (Math.random());
        phpAds_random = phpAds_random.substring(2,11);

        document.write ("<" + "script language='JavaScript'   type='text/javascript' src='");
        document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
        document.write ("&amp;what=zone:177&amp;source=ONE&amp;withText=1&amp;block=1");
        document.write ("&amp;exclude=" + document.phpAds_used);
        if (document.referrer)
          document.write ("&amp;referer=" + escape(document.referrer));
        document.write ("'><" + "/script>");
      //-->
    </script>
    <noscript>
      <a href='http://ads.plos.org/adclick.php?n=a98abd23' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:177&amp;source=ONE&amp;n=a98abd23' border='0' alt='' /></a>
    </noscript>
  </div>
  <!-- end : banner -->

  <h1>Volumes / Issues</h1>

  <div id="browse-results">
    <#if volumeInfos?exists>
      <#list volumeInfos as volumeInfo>
        <fieldset>
          <legend>${volumeInfo.displayName}</legend>
          <#if volumeInfo.imageArticle?exists>
            <@s.url id="imageSmURL" action="fetchObject" namespace="/article"
              uri="${volumeInfo.imageArticle}.g001" representation="PNG_S" includeParams="none"/>
            <@s.url id="imageLgURL" action="slideshow" namespace="/article"
              uri="${volumeInfo.imageArticle}" imageURI="${volumeInfo.imageArticle}.g001"
              includeParams="none"/>
            <p>
              <img class="thumbnail" border="1" align="left" alt="thumbnail" src="${imageSmURL}""/>
              ${volumeInfo.description}<br/>
              <a href="${imageLgURL}">View larger image</a>
            </p>
          </#if>
          <#if volumeInfo.description?exists>
            <p>${volumeInfo.description}</p>
          </#if>

          <#if volumeInfo.issueInfos?exists>
            <#list volumeInfo.issueInfos as issueInfo>
              <#if issueInfo.imageArticle?exists>
                <@s.url id="imageSmURL" action="fetchObject" namespace="/article"
                  uri="${issueInfo.imageArticle}.g001" representation="PNG_S" includeParams="none"/>
                <p>
                  <img class="thumbnail" border="1" align="left" alt="thumbnail" src="${imageSmURL}""/>
                </p>
              </#if>
              <@s.url id="issueURL" action="browseIssue" namespace="/journals/ntd/article"
                issue="${issueInfo.id}" field="issue" includeParams="none"/>
              <a href="${issueURL}">${issueInfo.displayName}</a>
            </#list>
          </#if>
        <fieldset>
      </#list>
    </#if>
  </div> <!-- search results -->
</div> <!--content-->
