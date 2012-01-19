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
        <title>Reset password</title>
    </head>
    <body>
        <br/>
        <h3>Hello <@s.property value="loginName"/></h3>

        <fieldset>
            <legend>Please enter a new password</legend>
            <p>
              <@s.form method="post" name="forgotPasswordChangePasswordForm" action="forgotPasswordChangePasswordSubmit">
                <@s.hidden name="loginName" />
                <@s.hidden name="resetPasswordToken" />
                <@s.password name="password1" label="Enter your new password" />
                <@s.password name="password2" label="Enter your new password again" />
                <@s.submit value="change my password" />
              </@s.form>

            </p>
        </fieldset>

    </body>
</html>


