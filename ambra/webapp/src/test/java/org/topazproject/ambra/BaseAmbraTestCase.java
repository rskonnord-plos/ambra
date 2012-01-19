/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.ambra;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.topazproject.ambra.annotation.action.CreateAnnotationAction;
import org.topazproject.ambra.annotation.action.CreateFlagAction;
import org.topazproject.ambra.annotation.action.CreateReplyAction;
import org.topazproject.ambra.annotation.action.DeleteAnnotationAction;
import org.topazproject.ambra.annotation.action.DeleteFlagAction;
import org.topazproject.ambra.annotation.action.DeleteReplyAction;
import org.topazproject.ambra.annotation.action.GetAnnotationAction;
import org.topazproject.ambra.annotation.action.GetFlagAction;
import org.topazproject.ambra.annotation.action.GetReplyAction;
import org.topazproject.ambra.annotation.action.ListAnnotationAction;
import org.topazproject.ambra.annotation.action.ListFlagAction;
import org.topazproject.ambra.annotation.action.ListReplyAction;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.annotation.service.ReplyService;
import org.topazproject.ambra.annotation.service.AnnotationConverter;
import org.topazproject.ambra.article.action.FetchArticleAction;
import org.topazproject.ambra.article.action.FetchObjectAction;
import org.topazproject.ambra.article.action.SlideshowAction;
import org.topazproject.ambra.article.service.ArticlePersistenceService;
import org.topazproject.ambra.article.service.FetchArticleService;
import org.topazproject.ambra.permission.service.PermissionsService;
import org.topazproject.ambra.user.action.AdminUserAlertsAction;
import org.topazproject.ambra.user.action.AdminUserProfileAction;
import org.topazproject.ambra.user.action.AssignAdminRoleAction;
import org.topazproject.ambra.user.action.DisplayUserAction;
import org.topazproject.ambra.user.action.MemberUserAlertsAction;
import org.topazproject.ambra.user.action.MemberUserProfileAction;
import org.topazproject.ambra.user.action.SearchUserAction;
import org.topazproject.ambra.user.service.UserService;
import org.topazproject.ambra.util.ProfanityCheckingService;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

  public abstract class BaseAmbraTestCase extends AbstractDependencyInjectionSpringContextTests {
  private FetchArticleAction fetchArticleAction;
  private CreateAnnotationAction createAnnotationAction;
  private DeleteAnnotationAction deleteAnnotationAction;
  private ListAnnotationAction listAnnotationAction;
  private ListReplyAction listReplyAction;
  private DeleteReplyAction deleteReplyAction;
  private CreateReplyAction createReplyAction;
  private GetAnnotationAction getAnnotationAction;
  private GetReplyAction getReplyAction;
  private MemberUserProfileAction memberUserProfileAction;
  private AdminUserProfileAction adminUserProfileAction;
  private MemberUserAlertsAction memberUserAlertsAction;
  private AdminUserAlertsAction adminUserAlertsAction;
  private AssignAdminRoleAction assignAdminRoleAction;
  private FetchObjectAction fetchObjectAction;
  private SlideshowAction slideshowAction;
  private DisplayUserAction displayUserAction;
  private CreateFlagAction createFlagAction;
  private GetFlagAction getFlagAction;

  private FetchArticleService fetchArticleService;
  private ProfanityCheckingService profanityCheckingService;
  private ArticlePersistenceService articlePersistenceService;
  private PermissionsService permissionsService;
  private AnnotationService annotationService;
  private ReplyService replyService;
  private AnnotationConverter converter;
  private UserService userService;
  private DeleteFlagAction deleteFlagAction;
  private ListFlagAction listFlagAction;
  private SearchUserAction searchUserAction;

  protected String[] getConfigLocations() {
    return new String[]{"nonWebApplicationContext.xml", "testApplicationContext.xml", "propertyConfigurer.xml", "countryList.xml", "profaneWords.xml"};
  }

  protected PermissionsService getPermissionsService() {
    return permissionsService;
  }

  public void setPermissionsService(final PermissionsService permissionsService) {
    this.permissionsService = permissionsService;
  }

  protected ArticlePersistenceService getArticlePersistenceService() throws MalformedURLException, ServiceException {
    return articlePersistenceService;
  }

  public void setArticlePersistenceService(final ArticlePersistenceService articlePersistenceService) {
    this.articlePersistenceService = articlePersistenceService;
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
  
  public ReplyService getReplyService() {
    return replyService;
  }

  public void setReplyService(ReplyService replyService) {
    this.replyService = replyService;
  }

  public AnnotationConverter getAnnotationConverter() {
    return converter;
  }

  public void setAnnotationConverter(AnnotationConverter converter) {
    this.converter = converter;
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
   * @return Returns the memberUserProfileAction.
   */
  public MemberUserProfileAction getMemberUserProfileAction() {
    return memberUserProfileAction;
  }

  /**
   * @param memberUserProfileAction The memberUserProfileAction to set.
   */
  public void setMemberUserProfileAction(MemberUserProfileAction memberUserProfileAction) {
    this.memberUserProfileAction = memberUserProfileAction;
  }

  /**
   * Getter for adminUserProfileAction.
   * @return Value of adminUserProfileAction.
   */
  public AdminUserProfileAction getAdminUserProfileAction() {
    return adminUserProfileAction;
  }

  /**
   * Setter for adminUserProfileAction.
   * @param adminUserProfileAction Value to set for adminUserProfileAction.
   */
  public void setAdminUserProfileAction(final AdminUserProfileAction adminUserProfileAction) {
    this.adminUserProfileAction = adminUserProfileAction;
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
  public SlideshowAction getSecondaryObjectAction() {
    return slideshowAction;
  }

  /**
   * @param slideshowAction secondaryObjectAction
   */
  public void setSecondaryObjectAction(final SlideshowAction slideshowAction) {
    this.slideshowAction = slideshowAction;
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
   * Setter for property 'memberUserAlertsAction'.
   * @param memberUserAlertsAction Value to set for property 'memberUserAlertsAction'.
   */
  public void setMemberUserAlertsAction(final MemberUserAlertsAction memberUserAlertsAction) {
    this.memberUserAlertsAction = memberUserAlertsAction;
  }

  /** @return Value for property 'memberUserAlertsAction'. */
  public MemberUserAlertsAction getMemberUserAlertsAction() {
    return memberUserAlertsAction;
  }

  /**
   * Getter for adminUserAlertsAction.
   * @return Value of adminUserAlertsAction.
   */
  public AdminUserAlertsAction getAdminUserAlertsAction() {
    return adminUserAlertsAction;
  }

  /**
   * Setter for adminUserAlertsAction.
   * @param adminUserAlertsAction Value to set for adminUserAlertsAction.
   */
  public void setAdminUserAlertsAction(final AdminUserAlertsAction adminUserAlertsAction) {
    this.adminUserAlertsAction = adminUserAlertsAction;
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

  /** return SearchUserAction */
  public SearchUserAction getSearchUserAction() {
    return searchUserAction;
  }

  /** set SearchUserAction */
  public void setSearchUserAction(final SearchUserAction searchUserAction) {
    this.searchUserAction = searchUserAction;
  }
}
