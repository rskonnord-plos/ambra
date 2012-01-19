/* $HeadURL::                                                                            $
* $Id$
*/
package org.plos;

import org.plos.annotation.action.BodyFetchAction;
import org.plos.annotation.action.CreateAnnotationAction;
import org.plos.annotation.action.CreateFlagAction;
import org.plos.annotation.action.CreateReplyAction;
import org.plos.annotation.action.DeleteAnnotationAction;
import org.plos.annotation.action.DeleteFlagAction;
import org.plos.annotation.action.DeleteReplyAction;
import org.plos.annotation.action.GetAnnotationAction;
import org.plos.annotation.action.GetFlagAction;
import org.plos.annotation.action.GetReplyAction;
import org.plos.annotation.action.ListAnnotationAction;
import org.plos.annotation.action.ListFlagAction;
import org.plos.annotation.action.ListReplyAction;
import org.plos.annotation.action.UnflagAnnotationAction;
import org.plos.annotation.service.AnnotationService;
import org.plos.article.action.FetchArticleAction;
import org.plos.article.action.FetchObjectAction;
import org.plos.article.action.SecondaryObjectAction;
import org.plos.article.service.ArticleWebService;
import org.plos.article.service.FetchArticleService;
import org.plos.permission.service.PermissionWebService;
import org.plos.search.action.SearchAction;
import org.plos.user.action.AssignAdminRoleAction;
import org.plos.user.action.DisplayUserAction;
import org.plos.user.action.UserAlertsAction;
import org.plos.user.action.UserProfileAction;
import org.plos.user.service.PreferencesWebService;
import org.plos.user.service.ProfileWebService;
import org.plos.user.service.UserService;
import org.plos.user.service.UserWebService;
import org.plos.util.ProfanityCheckingService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class BasePlosoneTestCase extends AbstractDependencyInjectionSpringContextTests {
  private FetchArticleAction fetchArticleAction;
  private CreateAnnotationAction createAnnotationAction;
  private DeleteAnnotationAction deleteAnnotationAction;
  private ListAnnotationAction listAnnotationAction;
  private ListReplyAction listReplyAction;
  private DeleteReplyAction deleteReplyAction;
  private CreateReplyAction createReplyAction;
  private BodyFetchAction bodyFetchAction;
  private GetAnnotationAction getAnnotationAction;
  private GetReplyAction getReplyAction;
  private UserProfileAction userProfileAction;
  private UserAlertsAction userAlertsAction;
  private AssignAdminRoleAction assignAdminRoleAction;
  private FetchObjectAction fetchObjectAction;
  private SecondaryObjectAction secondaryObjectAction;
  private DisplayUserAction displayUserAction;
  private CreateFlagAction createFlagAction;
  private GetFlagAction getFlagAction;

  private FetchArticleService fetchArticleService;
  private ProfanityCheckingService profanityCheckingService;
  private ArticleWebService articleWebService;
  private PermissionWebService permissionWebService;
  private AnnotationService annotationService;
  private PreferencesWebService preferencesWebService;
  private ProfileWebService profileWebService;
  private UserService userService;
  private UserWebService userWebService;
  private DeleteFlagAction deleteFlagAction;
  private UnflagAnnotationAction unflagAnnotationAction;
  private ListFlagAction listFlagAction;
  private SearchAction searchAction;

  protected String[] getConfigLocations() {
    return new String[]{"testApplicationContext.xml"};
  }

  protected PermissionWebService getPermissionWebService() {
    return permissionWebService;
  }

  public void setPermissionWebService(final PermissionWebService permissionWebService) {
    this.permissionWebService = permissionWebService;
  }

  protected ArticleWebService getArticleWebService() throws MalformedURLException, ServiceException {
    return articleWebService;
  }

  public void setArticleWebService(final ArticleWebService articleWebService) {
    this.articleWebService = articleWebService;
  }

  public FetchArticleService getFetchArticleService() throws MalformedURLException, ServiceException {
    return fetchArticleService;
  }

  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  protected FetchArticleAction getFetchArticleAction() throws MalformedURLException, ServiceException {
    return fetchArticleAction;
  }

  public void setFetchArticleAction(final FetchArticleAction fetchArticleAction) {
    this.fetchArticleAction = fetchArticleAction;
  }

  public AnnotationService getAnnotationService() throws MalformedURLException, ServiceException {
    return annotationService;
  }

  public void setAnnotationService(final AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  public CreateAnnotationAction getCreateAnnotationAction() {
    return createAnnotationAction;
  }

  public void setCreateAnnotationAction(final CreateAnnotationAction createAnnotationAnnotationAction) {
    this.createAnnotationAction = createAnnotationAnnotationAction;
  }

  public DeleteAnnotationAction getDeleteAnnotationAction() {
    return deleteAnnotationAction;
  }

  public void setDeleteAnnotationAction(final DeleteAnnotationAction deleteAnnotationAnnotationAction) {
    this.deleteAnnotationAction = deleteAnnotationAnnotationAction;
  }

  public ListAnnotationAction getListAnnotationAction() {
    return listAnnotationAction;
  }

  public void setListAnnotationAction(final ListAnnotationAction listAnnotationAnnotationAction) {
    this.listAnnotationAction = listAnnotationAnnotationAction;
  }

  public ListReplyAction getListReplyAction() {
    return listReplyAction;
  }

  public void setListReplyAction(final ListReplyAction listReplyAction) {
    this.listReplyAction = listReplyAction;
  }

  public DeleteReplyAction getDeleteReplyAction() {
    return deleteReplyAction;
  }

  public void setDeleteReplyAction(final DeleteReplyAction deleteReplyAction) {
    this.deleteReplyAction = deleteReplyAction;
  }

  public CreateReplyAction getCreateReplyAction() {
    return createReplyAction;
  }

  public void setCreateReplyAction(final CreateReplyAction createReplyAction) {
    this.createReplyAction = createReplyAction;
  }

  public void setAnnotationBodyFetcherAction(final BodyFetchAction bodyFetchAction) {
    this.bodyFetchAction = bodyFetchAction;
  }

  public BodyFetchAction getAnnotationBodyFetcherAction() {
    return bodyFetchAction;
  }

  public GetAnnotationAction getGetAnnotationAction() {
    return getAnnotationAction;
  }

  public void setGetAnnotationAction(final GetAnnotationAction getAnnotationAction) {
    this.getAnnotationAction = getAnnotationAction;
  }

  public GetFlagAction getGetFlagAction() {
    return getFlagAction;
  }

  public void setGetFlagAction(final GetFlagAction getFlagAction) {
    this.getFlagAction = getFlagAction;
  }

  public GetReplyAction getGetReplyAction() {
    return getReplyAction;
  }

  public void setGetReplyAction(final GetReplyAction getReplyAction) {
    this.getReplyAction = getReplyAction;
  }

  /**
   * @return Returns the bodyFetchAction.
   */
  public BodyFetchAction getBodyFetchAction() {
    return bodyFetchAction;
  }

  /**
   * @param bodyFetchAction The bodyFetchAction to set.
   */
  public void setBodyFetchAction(BodyFetchAction bodyFetchAction) {
    this.bodyFetchAction = bodyFetchAction;
  }

  /**
   * @return Returns the userProfileAction.
   */
  public UserProfileAction getUserProfileAction() {
    return userProfileAction;
  }

  /**
   * @param userProfileAction The userProfileAction to set.
   */
  public void setUserProfileAction(UserProfileAction userProfileAction) {
    this.userProfileAction = userProfileAction;
  }

  /**
   * @return Returns the displayUserAction.
   */
  public DisplayUserAction getDisplayUserAction() {
    return displayUserAction;
  }

  /**
   * @param displayUserAction The displayUserAction to set.
   */
  public void setDisplayUserAction(DisplayUserAction displayUserAction) {
    this.displayUserAction = displayUserAction;
  }

  /**
   * @return returns the fetchObjectAction
   */
  public FetchObjectAction getFetchObjectAction() {
    return fetchObjectAction;
  }

  /**
   * @param fetchObjectAction set the fetchObjectAction
   */
  public void setFetchObjectAction(final FetchObjectAction fetchObjectAction) {
    this.fetchObjectAction = fetchObjectAction;
  }

  /**
   * @return the SecondaryObjectAction
   */
  public SecondaryObjectAction getSecondaryObjectAction() {
    return secondaryObjectAction;
  }

  /**
   * @param secondaryObjectAction secondaryObjectAction
   */
  public void setSecondaryObjectAction(final SecondaryObjectAction secondaryObjectAction) {
    this.secondaryObjectAction = secondaryObjectAction;
  }

  /**
   * @return the CreateFlagAction
   */
  public CreateFlagAction getCreateFlagAction() {
    return createFlagAction;
  }

  /**
   * @param createFlagAction createFlagAction
   */
  public void setFlagAnnotationAction(final CreateFlagAction createFlagAction) {
    this.createFlagAction = createFlagAction;
  }

  /**
   * @return Returns the preferencesWebService.
   */
  public PreferencesWebService getPreferencesWebService() {
    return preferencesWebService;
  }

  /**
   * @param preferencesWebService The preferencesWebService to set.
   */
  public void setPreferencesWebService(PreferencesWebService preferencesWebService) {
    this.preferencesWebService = preferencesWebService;
  }

  /**
   * @return Returns the profileWebService.
   */
  public ProfileWebService getProfileWebService() {
    return profileWebService;
  }

  /**
   * @param profileWebService The profileWebService to set.
   */
  public void setProfileWebService(ProfileWebService profileWebService) {
    this.profileWebService = profileWebService;
  }

  /**
   * @return Returns the userService.
   */
  public UserService getUserService() {
    return userService;
  }

  /**
   * @param userService The userService to set.
   */
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  /**
   * @return Returns the userWebService.
   */
  public UserWebService getUserWebService() {
    return userWebService;
  }

  /**
   * @param userWebService The userWebService to set.
   */
  public void setUserWebService(UserWebService userWebService) {
    this.userWebService = userWebService;
  }

  protected URL getAsUrl(final String resourceToIngest) throws MalformedURLException {
    URL article = getClass().getResource(resourceToIngest);
    if (null == article) {
      article = new File(resourceToIngest).toURL();
    }
    return article;
  }

  /** @return the DeleteFlagAction */
  public DeleteFlagAction getDeleteFlagAction() {
    return deleteFlagAction;
  }

  /**
   * Set the DeleteFlagAction
   * @param deleteFlagAction deleteFlagAction
   */
  public void setDeleteFlagAction(final DeleteFlagAction deleteFlagAction) {
    this.deleteFlagAction = deleteFlagAction;
  }

  /**
   * @return the unflagAnnotationAction
   */
  public UnflagAnnotationAction getUnflagAnnotationAction() {
    return unflagAnnotationAction;
  }

  /** Set the unflagAnnotationAction */
  public void setUnflagAnnotationAction(final UnflagAnnotationAction unflagAnnotationAction) {
    this.unflagAnnotationAction = unflagAnnotationAction;
  }

  /** Get the listFlagAction */
  public ListFlagAction getListFlagAction() {
    return listFlagAction;
  }

  /** Set the listFlagAction */
  public void setListFlagAction(final ListFlagAction listFlagAction) {
    this.listFlagAction = listFlagAction;
  }

  /** Set the assignAdminRoleAction */
  public void setAssignAdminRoleAction(final AssignAdminRoleAction assignAdminRoleAction) {
    this.assignAdminRoleAction = assignAdminRoleAction;
  }

  /** Get the assignAdminRoleAction */
  protected AssignAdminRoleAction getAssignAdminRoleAction() {
    return assignAdminRoleAction;
  }

  /**
   * Setter for property 'userAlertsAction'.
   * @param userAlertsAction Value to set for property 'userAlertsAction'.
   */
  public void setUserAlertsAction(final UserAlertsAction userAlertsAction) {
    this.userAlertsAction = userAlertsAction;
  }

  /** @return Value for property 'userAlertsAction'. */
  public UserAlertsAction getUserAlertsAction() {
    return userAlertsAction;
  }

  /**
   * @return searchAction
   */
  public SearchAction getSearchAction() {
    return searchAction;
  }

  /**
   * Set searchAction
   * @param searchAction searchAction
   */
  public void setSearchAction(final SearchAction searchAction) {
    this.searchAction = searchAction;
  }

  /**
   * Getter for profanityCheckingService.
   * @return Value of profanityCheckingService.
   */
  public ProfanityCheckingService getProfanityCheckingService() {
    return profanityCheckingService;
  }

  /**
   * Setter for profanityCheckingService.
   * @param profanityCheckingService Value to set for profanityCheckingService.
   */
  public void setProfanityCheckingService(final ProfanityCheckingService profanityCheckingService) {
    this.profanityCheckingService = profanityCheckingService;
  }
}
