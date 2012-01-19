<html>
  <head>
    <title>PLoS ONE: Administration: Manage Caches</title>
  </head>
  <body>
    <h1 style="text-align: center">PLoS ONE: Administration: Manage Caches</h1>

    <@s.url id="adminTop" namespace="/admin" action="adminTop"/>
    <p style="text-align: right">
      <@s.a href="${adminTop}">Admin Top</@s.a>
    </p>
    <hr/>

    <#include "templates/messages.ftl">

    <#if cacheKeys?has_content>
      <fieldset>
        <legend><b>${cacheName}</b> Keys</legend>
        <p>
          <#list cacheKeys as cacheKey>
            ${cacheKey} <br/>
          </#list>
        </p>
      </fieldset>
      <br/>
      <hr/>
    </#if>

    <table border="1" cellpadding="2" cellspacing="0">
      <#list cacheStats.keySet().toArray() as cacheName>
        <tr>
          <th>${cacheName}</th>
          <#if cacheName != "">
            <@s.url id="clearStatistics" namespace="/admin" action="manageCaches"
              cacheAction="clearStatistics" cacheName="${cacheName}" />
            <@s.url id="removeAll" namespace="/admin" action="manageCaches"
              cacheAction="removeAll" cacheName="${cacheName}" />
            <@s.url id="getKeys" namespace="/admin" action="manageCaches"
              cacheAction="getKeys" cacheName="${cacheName}" />
            <td><@s.a href="%{clearStatistics}">clearStatistics</@s.a></td>
            <td><@s.a href="%{removeAll}">removeAll</@s.a></td>
            <td><@s.a href="%{getKeys}">getKeys</@s.a></td>
          <#else>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
          </#if>
          <#assign colums=cacheStats.get(cacheName)>
          <#list colums as column>
            <td>${column}</td>
          </#list>
        </tr>
      </#list>
    </table>

    <hr/>

    <strong>Statistics:</strong> <a href="http://ehcache.sourceforge.net/javadoc/net/sf/ehcache/Statistics.html#getStatisticsAccuracyDescription()">Javadoc</a>,
                                 <a href="http://ehcache.sourceforge.net/javadoc/net/sf/ehcache/Statistics.html#STATISTICS_ACCURACY_BEST_EFFORT">STATISTICS_ACCURACY_BEST_EFFORT</a>
    <blockquote>
      <p>
        Accurately measuring statistics can be expensive.
      </p>
    </blockquote>
    <br/>

    <strong>getKeys:</strong> <a href="http://ehcache.sourceforge.net/javadoc/net/sf/ehcache/Cache.html#getKeysNoDuplicateCheck()">Javadoc</a>
    <blockquote>
      <p>
        The time taken is O(log n). On a single cpu 1.8Ghz P4, approximately 6ms is required for 1000 entries and 36 for 50000.
      </p>
    </blockquote>
  </body>
</html>
