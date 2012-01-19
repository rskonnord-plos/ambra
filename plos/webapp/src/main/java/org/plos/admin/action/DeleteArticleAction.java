package org.plos.admin.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.article.util.ArticleDeleteException;
import org.plos.article.util.ArticleUtil;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.otm.Session;

public class DeleteArticleAction extends BaseAdminActionSupport {

  private static final Log log = LogFactory.getLog(DeleteArticleAction.class);
  private Session session;
  private String article;
  
  public String execute() throws Exception {
    boolean error = false;
    
    try {
      ArticleUtil.delete(article, session);
    } catch (ArticleDeleteException ade) {
      addActionError("Failed to successfully delete article: "+article+". <br>"+ade.toString());
      log.error("Failed to successfully delete article: "+article, ade);
      error = true;
    }
    
    if (!error) {
      addActionMessage("Successfully deleted article: "+article);
    }
    
    return base();
  }
  
  public void setArticle(String a) {
    article = a;
  }
  
  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session The OTM session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }
}
