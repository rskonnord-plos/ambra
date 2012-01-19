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
<html>
    <head>
        <title>Verification email sent</title>
    </head>
    <body>
        <br/>
        <h3>Hello <@s.property value="loginName1"/> : Please check your email account for further instructions to setup your account</h3>

<!--        <p>
          Your password: <@s.property value="password1"/>
        </p>
-->
        <fieldset>
            <legend>Email body</legend>
            <p>

                Please click the following link to verify your email address:

                <@s.url includeParams="none" id="emailVerificationURL" action="emailVerification">
                  <@s.param name="loginName" value="user.loginName"/>
                  <@s.param name="emailVerificationToken" value="user.emailVerificationToken"/>
                </@s.url>
                <@s.a href="%{emailVerificationURL}"  >${emailVerificationURL}</@s.a>
            </p>
        </fieldset>

    </body>
</html>
