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
          <@ww.url id="articleListURL"  namespace="/article" action="articleList" />
          <@ww.a href="%{articleListURL}">View Articles</@ww.a>
      </p>

			<p>
			  <@ww.a href="/plos-registration/registerPart1.action">Register New User</@ww.a>
			</p>
		
			<p>
					<#assign returnURL="http://"+freemarker_config.getPlosOneHost()+"/"+freemarker_config.getContext()>
					<a href="${freemarker_config.getCasLoginURL()}?service=${returnURL?url("UTF-8")}">Login</a>
			</p>

			<p>
					<a href="${freemarker_config.getCasLogoutURL()}">Logout</a>
			</p>


<!--      <p>
          <@ww.url id="createAnnotationURL" namespace="/annotation/secure" action="createAnnotation" />
          <@ww.a href="%{createAnnotationURL}">Create Annotation</@ww.a>
      </p>-->

<!--      <p>
          <@ww.url id="listAnnotationURL" namespace="/annotation" action="listAnnotation">
            <@ww.param name="target" value="%{'http://here.is/viru'}"/>
          </@ww.url>
          <@ww.a href="%{listAnnotationURL}">List Annotations for http://here.is/viru </@ww.a>
      </p>

      <p>
          <@ww.url id="listAnnotationURL" namespace="/annotation" action="listAnnotation">
            <@ww.param name="target" value="%{'http://localhost:9090/fedora/get/doi:10.1371%2Fjournal.pone.0000008/XML'}"/>
          </@ww.url>
          <@ww.a href="%{listAnnotationURL}">List Annotations for http://localhost:9090/fedora/get/doi:10.1371%2Fjournal.pone.0000008/XML</@ww.a>
      </p>-->


<!--
      <p>
          <@ww.url id="createUserURL" namespace="/user" action="newUser" />
          <@ww.a href="%{createUserURL}">Create User</@ww.a>
      </p>

      <p>
          <@ww.url id="createAdminUserURL" namespace="/user/secure" action="assignAdminRole" />
          <@ww.a href="%{createAdminUserURL}">Assign admin role</@ww.a>
      </p>
-->
      <p>
          <@ww.url id="adminTopURL" namespace="/admin" action="adminTop" />
          <@ww.a href="%{adminTopURL}">Admin Functions</@ww.a>
      </p>
      
      <p>
          <@ww.url id="getUserAlertURL" namespace="/user/secure" action="retrieveUserAlerts" />
          <@ww.a href="%{getUserAlertURL}">Retrieve user alerts</@ww.a>
      </p>

      <p>
          <@ww.url id="searchStartURL" namespace="/search" action="simpleSearch" />
          <@ww.a href="%{searchStartURL}">Search</@ww.a>
      </p>

      <p>
          <@ww.url id="retrieveUserProfileURL" namespace="/user/secure" action="retrieveUserProfile" />
          <@ww.a href="%{retrieveUserProfileURL}">Retrieve User Profile</@ww.a>
      </p>

      <p>
          <@ww.url id="homeURL" namespace="" action="home" />
          <@ww.a href="%{homeURL}">Home</@ww.a>
      </p>

    </fieldset>
  </body>
</html>
