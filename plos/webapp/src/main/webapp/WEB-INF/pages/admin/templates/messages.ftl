<#if actionErrors?has_content> 
  <fieldset>
    <legend><b>Errors</b></legend>
    <p>
      <#list actionErrors as error>
        ${error} <br/>
      </#list>
    </p>
  </fieldset>
  <br/>
  <hr/>
</#if>

<#if actionMessages?has_content> 
  <fieldset>
    <legend><b>Messages</b></legend>
    <p>
      <#list actionMessages as message>
        ${message} <br/>
      </#list>
    </p>
  </fieldset>
  <br/>
  <hr/>
</#if>
