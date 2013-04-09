<#macro iFrameAd zone id height width>
  <#assign url = "" />
  <#if id?length gt 0>
  <iframe id='${id}' name='${id}'
          src='${url}/afr.php?zoneid=${zone}&amp;cb=<@randomNumber maxValue=10000/>'
          frameborder='0' scrolling='no' width='${width}' height='${height}'><a
          href='${url}/ck.php?n=${id}&amp;cb=<@randomNumber maxValue=10000/>'
          target='_top'><img src='${url}/avw.php?zoneid=${zone}&amp;cb=<@randomNumber maxValue=10000/>&amp;n=${id}'
                             border='0' alt=''/></a></iframe>
  </#if>
</#macro>
