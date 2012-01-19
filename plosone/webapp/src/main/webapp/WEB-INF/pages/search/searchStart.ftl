<html>
  <head>
    <title>Search</title>
  </head>
  <body>
    <legend>Messages</legend>
  
    <fieldset>
      <p>
        <#list actionMessages as message>
          ${message} <br/>
        </#list>
      </p>
    </fieldset>

    <p>
      <fieldset>
        <legend>Simple Search</legend>
        <@s.form name="simpleSearchForm" action="simpleSearch" namespace="/search" method="post">
          <@s.textfield name="query" label="Query" required="true"/>
          <@s.submit value="simple search" />
        </@s.form>
      </fieldset>

      <fieldset>
        <legend>Advanced Search</legend>
        <@s.form name="advancedSearchForm" action="advancedSearch" namespace="/search" method="post">
          <@s.textfield name="title" label="Title" />
          <@s.textfield name="text" label="Text" />
          <@s.textfield name="description" label="Description" />
          <@s.textfield name="creator" label="Creator" />
          <@s.submit value="advanced search" />
        </@s.form>
      </fieldset>
    </p>
  </body>
</html>
