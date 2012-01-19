package org.plos.action;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.interceptor.ServletResponseAware;

public class PageNotFoundAction extends NoOpAction implements ServletResponseAware {
  private HttpServletResponse response;

  @Override
  public String execute() throws Exception {
    response.setStatus(404);
    return SUCCESS;
  }

  public void setServletResponse(HttpServletResponse resp) {
    response = resp;
  }
}
