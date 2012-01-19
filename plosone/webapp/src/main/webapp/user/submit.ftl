<#if editedByAdmin>
  <@ww.hidden name="topazId"/>
  <@ww.submit value="Submit" tabindex="99"/>
<#else>
  <div class="btnwrap"><input type="button" id="formSubmit" name="formSubmit" value="Save" tabindex="99"/></div>
</#if>
