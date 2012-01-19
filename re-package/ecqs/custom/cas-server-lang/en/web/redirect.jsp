<%@ page session="false" %>

<%
  String serviceId = (String) request.getAttribute("serviceId");
%>

<html>

  <head>

    <title>PLoS Authentication</title>

    <script>
  window.location.href="<%= serviceId %>";
 </script>
  
</head>
  

<body>
 
    <noscript>
<p>Click <a href="<%= serviceId %>">here</a> to access the service you requested.</p></noscript>

  </body>


</html>
