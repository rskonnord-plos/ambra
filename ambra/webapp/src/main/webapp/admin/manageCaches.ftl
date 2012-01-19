<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2010 by Public Library of Science
  
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
<#include "includes/globals.ftl">
<html>
  <head>
    <title>Ambra: Administration: Manage Caches</title>
    <#include "includes/header.ftl">
  </head>
  <body>
    <h1 style="text-align: center">Ambra: Administration: Manage Caches</h1>
    <#include "includes/navigation.ftl">

    <@messages />

    <#if cacheKeys?has_content>
      <fieldset>
        <legend><strong>${cacheName}</strong> Keys</legend>
        <p>
          <#list cacheKeys as cacheKey>
            <@s.url id="remove" namespace="/admin" action="manageCaches"
              cacheAction="remove" cacheName="${cacheName}" cacheKey="${cacheKey}"/>
            <@s.url id="get" namespace="/admin" action="manageCaches"
              cacheAction="get" cacheName="${cacheName}" cacheKey="${cacheKey}"/>
            ${cacheKey} <@s.a href="%{remove}">remove()</@s.a> <@s.a href="%{get}">get()</@s.a><br/>
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

    <h2>Clear Cache Object with Assistance</h2>
    <@s.form name="clearCacheWithAssistanceForm" action="clearCacheWithAssistance" namespace="/admin" method="post">
        <p>Please enter a URI for an article, annotation, or issue and we will try our best to clear all related keys in the cache.</p>
        <@s.textfield name="cacheURI" label="Object URI" required="true"/><br />
        <@s.radio name="cacheURIType" label="Clear Article" list="{'Article'}"/><br />
        <@s.radio name="cacheURIType" label="Clear Annotation" list="{'Annotation'}"/><br />
        <@s.radio name="cacheURIType" label="Clear Issue" list="{'Issue'}"/><br />
        <@s.submit value="Clear Cache" />
    </@s.form>

    <h2>Statistics:</h2>
    <a href="http://ehcache.sourceforge.net/javadoc/net/sf/ehcache/Statistics.html#getStatisticsAccuracyDescription()">Javadoc</a>,
    <a href="http://ehcache.sourceforge.net/javadoc/net/sf/ehcache/Statistics.html#STATISTICS_ACCURACY_BEST_EFFORT">STATISTICS_ACCURACY_BEST_EFFORT</a>
    <blockquote>
      <p>
        Accurately measuring statistics can be expensive.
      </p>
    </blockquote>
    <br/>

    <h2>getKeys:</h2>
    <a href="http://ehcache.sourceforge.net/javadoc/net/sf/ehcache/Cache.html#getKeysNoDuplicateCheck()">Javadoc</a>
    <blockquote>
      <p>
        The time taken is O(log n). On a single cpu 1.8Ghz P4, approximately 6ms is required for 1000 entries and 36 for 50000.
      </p>
    </blockquote>
  </body>
</html>
