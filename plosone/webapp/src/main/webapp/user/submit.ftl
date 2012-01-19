<#if editedByAdmin>
  <@ww.hidden name="topazId"/>
  <@ww.submit value="Submit" tabindex="200"/>
<#else>
  <div class="btnwrap"><input type="button" id="formSubmit" name="formSubmit" value="Save" tabindex="200"/></div>
</#if>
