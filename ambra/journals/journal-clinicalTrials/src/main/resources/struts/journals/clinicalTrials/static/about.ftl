<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2008 by Topaz, Inc.
  http://topazproject.org
  
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
<!-- begin : main content -->
<div id="content" class="static">
  <h1>About the PLoS Hub for Clinical Trials</h1>

  <p class="intro">Find information about PLoS Hub for Clinical Trials on the following pages:</p>

  <@s.url action="information" namespace="/static" includeParams="none" id="infoURL"/>
  <@s.url action="license" namespace="/static" includeParams="none" id="licenseURL"/>
  <@s.url action="faq" namespace="/static" includeParams="none" id="faqURL"/>
  <@s.url action="contact" namespace="/static" includeParams="none" id="contactURL"/>
  <ul>
    <li><@s.a href="${infoURL}" title="PLoS Hub | Information">Information</@s.a> - General information about the PLoS Hub for Clinical Trials</li>
    <li><@s.a href="${licenseURL}" title="PLoS Hub | License">Open-Access License</@s.a> - Read more about the open-access license terms.</li>
    <li><@s.a href="${faqURL}" title="PLoS Hub | FAQ">FAQ</@s.a> - Answers to frequently asked questions about the PLoS Hub for Clinical Trials.</li>
    <li><@s.a href="${contactURL}" title="PLoS Hub | Contact Us">Contact Us</@s.a> - How to reach the PLoS Hub for Clinical Trials and PLoS.</li>
  </ul>
</div>
<!-- end : main contents -->