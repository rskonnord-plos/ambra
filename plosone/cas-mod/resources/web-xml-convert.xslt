<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="utf-8"
    doctype-system="http://java.sun.com/j2ee/dtds/web-app_2.3.dtd"
    doctype-public="-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"/>

<!-- 
	This is not used in the automated build process, but can be used to get the final web.xml 
	by applying this xslt to the one available in the esup quick start's tomcat-patch.
	You might have to take out the doctype from the initial web.xml for the xslt transformation 
	to work.	
	
	java -cp "D:\java\xalan-j_2_7_0\xalan.jar" org.apache.xalan.xslt.Process -XSL web-xml-convert.xslt -IN web.xml -OUT new-web.xml
 -->
 
  <xsl:template match="context-param[last()]">
    <xsl:call-template name="copy"/>
    <xsl:call-template name="add-context-params"/>
    <xsl:call-template name="add-filter"/>
  </xsl:template>

  <xsl:template name="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="add-context-params" xml:space="preserve">
    
    <xsl:comment>Context params for initializing the data source and user service</xsl:comment>
    <context-param>
      <param-name>jdbcDriver</param-name>
      <param-value>org.postgresql.Driver</param-value>
    </context-param>
    <context-param>
      <param-name>jdbcUrl</param-name>
      <param-value>jdbc:postgresql://localhost/postgres</param-value>
    </context-param>
    <context-param>
      <param-name>usernameToGuidSql</param-name>
      <param-value>select id from plos_user where loginname=?</param-value>
    </context-param>
    <context-param>
      <param-name>guidToUsernameSql</param-name>
      <param-value>select loginname from plos_user where id=?</param-value>
    </context-param>
    <context-param>
      <param-name>connectionValidationQuery</param-name>
      <param-value>select 1</param-value>
    </context-param>
    <context-param>
      <param-name>initialSize</param-name>
      <param-value>2</param-value>
    </context-param>
    <context-param>
      <param-name>maxActive</param-name>
      <param-value>10</param-value>
    </context-param>
    <context-param>
      <param-name>adminUser</param-name>
      <param-value>postgres</param-value>
    </context-param>
    <context-param>
      <param-name>adminPassword</param-name>
      <param-value>postgres</param-value>
    </context-param>
  </xsl:template>

  <xsl:template name="add-filter" xml:space="preserve">
    <xsl:comment>Filter class and mapping for username replacement with guid filter</xsl:comment>
    <filter>
      <filter-name>UsernameReplacementWithGuidFilter</filter-name>
      <filter-class>org.plos.auth.web.UsernameReplacementWithGuidFilter</filter-class>
    </filter>
    <filter-mapping>
      <filter-name>UsernameReplacementWithGuidFilter</filter-name>
      <url-pattern>/login</url-pattern>
    </filter-mapping>
  </xsl:template>

  <xsl:template match="listener[last()]">
    <xsl:call-template name="copy"/>
    <xsl:call-template name="add-listener"/>
  </xsl:template>

  <xsl:template name="add-listener" xml:space="preserve">
      
		<!-- initialize the DatabaseContext and UserService -->
    <listener>
      <listener-class>org.plos.auth.web.AuthServletContextListener</listener-class>
    </listener>
  </xsl:template>

  <xsl:template match="servlet[last()]">
    <xsl:call-template name="copy"/>
    <xsl:call-template name="add-servlet"/>
  </xsl:template>

  <xsl:template name="add-servlet" xml:space="preserve">
		
		<!-- get email address for a given guid -->
    <servlet>
      <servlet-name>Email</servlet-name>
      <servlet-class>org.plos.auth.web.GetEmailAddress</servlet-class>
    </servlet>
  </xsl:template>

  <xsl:template match="servlet-mapping[last()]">
    <xsl:call-template name="copy"/>
    <xsl:call-template name="add-servlet-mapping"/>
  </xsl:template>

  <xsl:template name="add-servlet-mapping" xml:space="preserve">
    <servlet-mapping>
      <servlet-name>Email</servlet-name>
      <url-pattern>/email</url-pattern>
    </servlet-mapping>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:call-template name="copy"/>
  </xsl:template>

</xsl:stylesheet>
