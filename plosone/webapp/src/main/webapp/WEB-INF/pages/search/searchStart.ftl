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
        <@ww.form name="simpleSearchForm" action="simpleSearch" namespace="/search" method="post">
          <@ww.textfield name="query" label="Query" required="true"/>
          <@ww.submit value="simple search" />
        </@ww.form>
      </fieldset>

      <fieldset>
        <legend>Advanced Search</legend>
        <@ww.form name="advancedSearchForm" action="advancedSearch" namespace="/search" method="post">
          <@ww.textfield name="title" label="Title" />
          <@ww.textfield name="text" label="Text" />
          <@ww.textfield name="description" label="Description" />
          <@ww.textfield name="creator" label="Creator" />
          <@ww.submit value="advanced search" />
        </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
