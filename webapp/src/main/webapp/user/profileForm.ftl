<#--
  Copyright (c) 2007-2013 by Public Library of Science
  http://plos.org
  http://ambraproject.org
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<form name="userForm" id="userForm" action="/user/secure/profile/save" method="post"
title="User Information Form" class="ambra-form" enctype="multipart/form-data">
  <#if (fieldErrors.size() != 0 && tabID == "profile")>
    <p class="required">Please correct the errors below. </p>
  </#if>

  <#--store the email, alerts, and displayName on the page so that they get set on the action when we go back to save-->
  <@s.hidden name="email"/>
  <@s.hidden name="alertsJournals"/>
  <@s.hidden name="displayName"/>
  <@s.action name="selectList" namespace="" id="selectList"/>
  <fieldset>
    <legend>Public Information <span class="note">(<span class="required">*</span> are required.)</span></legend>
    <ol>
      <@s.select label="Title" name="title" value="title" list="%{#selectList.allTitles}" tabindex="1" col=true />
      <@s.textfield name="givenNames" label="First Name" required="true" tabindex="2" col=true />
      <@s.textfield name="surnames" label="Last Name" required="true" tabindex="3" col=true />

      <@s.textfield name="city" label="City" tabindex="4" row=true col=true />
      <@s.select label="Country" name="country" value="country" list="%{#selectList.get('countries')}" tabindex="5" col=true />

      <@s.textarea name="biographyText" label="Short Biography" cols="50" tabindex="6"/>

      <@s.textfield name="researchAreasText" label="Research Areas" tabindex="7" row=true col=true />
      <@s.textfield name="interestsText" label="Other Interests"  tabindex="8" col=true/>

      <@s.textfield name="homePage" label="Home page" tabindex="9" row=true col=true />
      <@s.textfield name="weblog" label="Blog URL" tabindex="10" col=true/>
    </ol>
  </fieldset>
  <fieldset>
    <legend>Additional Information</legend>
    <ol>
      <@s.select label="Organization Type" name="organizationType" value="organizationType"
      list="%{#selectList.allOrganizationTypes}" tabindex="11" col=true />
      <@s.textfield name="organizationName" label="Organization Name" tabindex="12" col=true />
      <@s.textarea name="postalAddress" label="Organization Address" cssClass="long-input"  rows="5" tabindex="13"  row=true col=true/>
      <@s.select label="Position Type" name="positionType" value="positionType" list="%{#selectList.allPositionTypes}" tabindex="14"  col=true />
    </ol>
  </fieldset>
  <fieldset>
    <label for="organizationVisibility">
      <@s.checkbox name="organizationVisibility" tabIndex="15"/>Display my Additional Information publicly.
    </label>
  </fieldset>

  <div class="btnwrap"><input type="submit" id="formSubmit" class="btn primary" name="formSubmit" value="Save" tabindex="16"/></div>

</form>

