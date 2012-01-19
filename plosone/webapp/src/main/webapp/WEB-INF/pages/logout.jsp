<%@ taglib uri="http://www.yale.edu/its/tp/cas/version2" prefix="cas" %>
 <%-- first destroy the web application's session --%>
 <% session.invalidate(); %>
 <%-- then logout of CAS --%>
 <cas:logout var="netID" scope="session" logoutUrl="https://localhost:7443/cas/logout" /> 