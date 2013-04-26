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
  See the Licen
  se for the specific language governing permissions and
  limitations under the License.
-->
<@s.url action="privacy" namespace="/static" includeParams="none" id="privacyURL"/>

<h1 class="displayName">${displayName}</h1>
<ul class="info-list">
  <li>Display Name: <b>${displayName}</b>&nbsp;<span class="note">(Display names are permanent)</span></li>
  <li>
    Email: <b>${email}</b> <a href="${freemarker_config.changeEmailURL}"
                              title="Click here to change your e-mail address">Change your e-mail address</a>
  </li>
  <li class="close-top note">(Your e-mail address will always be kept private. See the <a
      href="${privacyURL}">${freemarker_config.orgName} Privacy Statement</a> for more information.)
  </li>
  <li>
    <a href="${freemarker_config.changePasswordURL}" title="Click here to change your password">Change your
      password</a>
  </li>
</ul>

<div id="user-forms" class="tab-block" active="${tabID}">
  <div id="user-tabs" class="nav tab-nav">
    <ul>
      <li><a href="#profile" url="/user/secure/profile">Profile</a></li>
      <li><a href="#journalAlerts" url="/user/secure/profile/alerts/journal">Journal Alerts</a></li>
      <li><a href="#savedSearchAlerts" url="/user/secure/profile/alerts/search">Search Alerts</a></li>
    </ul>
  </div>
  <div class="tab-content">
    <div id="profile" class="tab-pane">
      <#include "profileForm.ftl">
    </div>
    <div id="journalAlerts" class="tab-pane">
      <#include "alertsForm.ftl">
    </div>
    <div id="savedSearchAlerts" class="tab-pane">
      <#include "searchAlertsForm.ftl">
    </div>
  </div>
</div>

<div id="save-confirm">
  Your preferences have been saved
</div>