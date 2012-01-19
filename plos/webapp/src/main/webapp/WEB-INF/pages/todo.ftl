<html>
  <head>
    <title>Welcome to Plosone</title>
  </head>
  <body>

    <h1>Welcome to the Plosone webapp</h1>

    <legend>Messages</legend>

    <fieldset>
      <p>
        <#list actionMessages as message>
          ${message} <br/>
        </#list>
      </p>
    </fieldset>


    <fieldset>
      <legend>A few things for you to do</legend>
      <p>
          <@s.url id="articleListURL"  namespace="/article" action="articleList" />
          <@s.a href="%{articleListURL}">View Articles</@s.a>
      </p>

			<p>
			  <@s.a href="/plos-registration/registerPart1.action">Register New User</@s.a>
			</p>
		
			<p>
					<#assign returnURL="http://"+freemarker_config.getPlosOneHost()+"/"+freemarker_config.getContext()>
					<a href="${freemarker_config.getCasLoginURL()}?service=${returnURL?url("UTF-8")}">Login</a>
			</p>

			<p>
					<a href="${freemarker_config.getCasLogoutURL()}">Logout</a>
			</p>


<!--      <p>
          <@s.url id="createAnnotationURL" namespace="/annotation/secure" action="createAnnotation" />
          <@s.a href="%{createAnnotationURL}">Create Annotation</@s.a>
      </p>-->

<!--      <p>
          <@s.url id="listAnnotationURL" namespace="/annotation" action="listAnnotation">
            <@s.param name="target" value="%{'http://here.is/viru'}"/>
          </@s.url>
          <@s.a href="%{listAnnotationURL}">List Annotations for http://here.is/viru </@s.a>
      </p>

      <p>
          <@s.url id="listAnnotationURL" namespace="/annotation" action="listAnnotation">
            <@s.param name="target" value="%{'http://localhost:9090/fedora/get/doi:10.1371%2Fjournal.pone.0000008/XML'}"/>
          </@s.url>
          <@s.a href="%{listAnnotationURL}">List Annotations for http://localhost:9090/fedora/get/doi:10.1371%2Fjournal.pone.0000008/XML</@s.a>
      </p>-->


<!--
      <p>
          <@s.url id="createUserURL" namespace="/user" action="newUser" />
          <@s.a href="%{createUserURL}">Create User</@s.a>
      </p>

      <p>
          <@s.url id="createAdminUserURL" namespace="/user/secure" action="assignAdminRole" />
          <@s.a href="%{createAdminUserURL}">Assign admin role</@s.a>
      </p>
-->
      <p>
          <@s.url id="adminTopURL" namespace="/admin" action="adminTop" />
          <@s.a href="%{adminTopURL}">Admin Functions</@s.a>
      </p>
      
      <p>
          <@s.url id="getUserAlertURL" namespace="/user/secure" action="retrieveUserAlerts" />
          <@s.a href="%{getUserAlertURL}">Retrieve user alerts</@s.a>
      </p>

      <p>
          <@s.url id="searchStartURL" namespace="/search" action="simpleSearch" />
          <@s.a href="%{searchStartURL}">Search</@s.a>
      </p>

      <p>
          <@s.url id="retrieveUserProfileURL" namespace="/user/secure" action="retrieveUserProfile" />
          <@s.a href="%{retrieveUserProfileURL}">Retrieve User Profile</@s.a>
      </p>

      <p>
          <@s.url id="homeURL" namespace="" action="home" />
          <@s.a href="%{homeURL}">Home</@s.a>
      </p>

    </fieldset>
  </body>
</html>
