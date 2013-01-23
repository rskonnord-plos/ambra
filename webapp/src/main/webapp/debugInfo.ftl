<html><head><title>Ambra Debug Information: ${host}</title></head>
<body>
<pre>
Host: ${host} (${hostIp})
${buildInfo}
Command Line: ${cmdLine}
Tomcat Version: ${tomcatVersion}
Active Sessions: ${sessionCount}
JVM free memory: ${jvmFreeMemory?string.number} MB; total memory: ${jvmTotalMemory?string.number} MB, max memory: ${jvmMaxMemory?string.number} MB
DB URL: ${dbUrl}, user ${dbUser}
SOLR URL: <a href="${solrUrl}">${solrUrl}</a>
Filestore: ${filestore}
</pre>

<p>
<@s.url id="threadDumpUrl" action="threadDump" namespace="/debug" />
<a href="${threadDumpUrl}">Thread Dump</a>

<@s.url id="processDumpUrl" action="processDump" namespace="/debug" />
  <a href="${processDumpUrl}">Process Dump</a>
</p>

<pre>
Config Dump:

${configuration}
</pre>
<br />
<br />
<hr />
<em>Generated ${timestamp?datetime?string.long}</em>
</body>
</html>
