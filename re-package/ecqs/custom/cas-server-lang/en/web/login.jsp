<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  <head>
    <title>PLoS : Login</title>

    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <style type="text/css" media="all"> @import "css/plos.css";</style>

    <link rel="shortcut icon" href="images/pone_favicon.ico" type="image/x-icon" />
    <link rel="home" title="home" href="http://www.plosone.org"></link>

  </html>
  <body>
    <div id="container">
      <div id="logo">PLoS</div>
      <div id="content">
        <fieldset>
          <legend>Login to Your PLoS Account</legend>
          <form method="post" name="login_form">
            <input type="hidden" name="lt" value="<%= request.getAttribute("edu.yale.its.tp.cas.lt") %>" />
            <ol>
                <% if (request.getAttribute("edu.yale.its.tp.cas.badUsernameOrPassword") != null) { %>
		<li><em>Please enter a valid email and password</em></li>
                <% } else if (request.getAttribute("edu.yale.its.tp.cas.service") == null) { %>
                <li><em>You may login now in order to access protected services later.</em></li>
                <% } else if (request.getAttribute("edu.yale.its.tp.cas.badLoginTicket") != null) { %>
                <li><em>Bad Login Ticket: Please check to make sure you are coming from a PLoS site.</em></li>
                <% } else { %>                        
                <!-- <em>You have requested access to a site that requires authentication.</em> -->               
                <% } %>

              <li>
                <label for="username">Email</label>
                <input type="text" name="username" tabindex="1"/>
              </li>
              <li>
                <label for="password">Password</label>
                <input type="password" name="password" tabindex="2"/>
              </li>
              <li class="btn">
                 <input type="submit" name="Go!" value="Go!" tabindex="3"/>
              </li>
            </ol>
          </form>
        </fieldset>

        <ul class="links">
          <li><a href="/plos-registration/forgotPassword.action" title="Click here if you forgot your password" tabindex="11">Forgotten Password?</a></li>
          <li><a href="/plos-registration/register.action" tabindex="12"><strong>Register for a New Account</strong></a></li>
        </ul> 
      </div>

      <div id="ftr">
        <ul>  
<!--          <li><a href="login" title="Login to Your PLoS Account" tabindex="20">Login</a></li>-->
          <li><a href="privacy.html" title="PLoS Statement" tabindex="21">Privacy Statement</a></li>
          <li><a href="terms.html" title="PLoS Terms of Use" tabindex="22">Terms of Use</a></li>
          <li><a href="http://www.plos.org" title="PLoS.org" tabindex="23">PLoS.org</a></li>
        </ul>   
      </div>
    </div>
  </body>
</html>
