/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.rpc.ServiceException;

import org.apache.axis.types.NonNegativeInteger;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.PasswordProtectedService;
import org.topazproject.authentication.UnProtectedService;
import org.topazproject.common.impl.SimpleTopazContext;
import org.topazproject.common.impl.TopazContext;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.mulgara.itql.AnswerSet;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.ws.pap.Profiles;
import org.topazproject.ws.pap.UserProfile;

/** 
 * This provides the implementation of the profiles service.
 * 
 * <p>A profile is stored as a foaf:Person. Permissions can be managed via the {@link
 * org.topazproject.ws.permissions.Permissions Permissions} service, where the resource is the
 * internal user-id.
 *
 * @author Ronald Tschalär
 */
public class ProfilesImpl implements Profiles {
  private static final Log    log            = LogFactory.getLog(ProfilesImpl.class);

  private static final String FOAF_URI       = "http://xmlns.com/foaf/0.1/";
  private static final String BIO_URI        = "http://purl.org/vocab/bio/0.1/";
  private static final String ADDR_URI       = "http://wymiwyg.org/ontologies/foaf/postaddress#";

  private static final Configuration CONF    = ConfigurationStore.getInstance().getConfiguration();

  private static final String MODEL          = "<" + CONF.getString("topaz.models.profiles") + ">";
  private static final String MODEL_TYPE     =
      "<" + CONF.getString("topaz.models.profiles[@type]", "tucana:Model") + ">";
  private static final String USER_MODEL     = "<" + CONF.getString("topaz.models.users") + ">";
  private static final String STR_MODEL      = "<" + CONF.getString("topaz.models.str") + ">";
  private static final String STR_MODEL_TYPE =
      "<" + CONF.getString("topaz.models.str[@type]", null) + ">";
  private static final String IDS_NS         = "topaz.ids";
  private static final String PROF_PATH_PFX  = "profile";

  private static final Map    aliases;

  private static final String ITQL_GET_PROFID =
      ("select $profId from ${MODEL} where " +
       "$profId <rdf:type> <foaf:Person> and " +
       "$profId <foaf:holdsAccount> <${userId}>;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_PROF =
      ("select $p $o from ${MODEL} where " +
       "$profId <rdf:type> <foaf:Person> and " +
       "$profId <foaf:holdsAccount> <${userId}> and " +
       "$profId $p $o;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_CLEAR_PROF =
      ("delete select <${profId}> $p $o from ${MODEL} where <${profId}> $p $o from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_CREATE_PROF =
      ("insert <${profId}> <rdf:type> <foaf:Person> " +
              "<${profId}> <foaf:holdsAccount> <${userId}> " +
              "into ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_TEST_USERID =
      ("select $userId from ${USER_MODEL} where " +
       "  $userId <rdf:type> <foaf:OnlineAccount> and $userId <tucana:is> <${userId}>;").
      replaceAll("\\Q${USER_MODEL}", USER_MODEL);

  private static final String ITQL_FIND_USER_BY_PROF_PRE =
      ("select $userId $tmpl from ${MODEL} where " +
       "$userId <rdf:type> <foaf:OnlineAccount> in ${USER_MODEL} and " +
       "    $profId <rdf:type> <foaf:Person> and " +
       "    $profId <foaf:holdsAccount> $userId and (").
      replaceAll("\\Q${MODEL}", MODEL).replaceAll("\\Q${USER_MODEL}", USER_MODEL);

  private static final String ITQL_FIND_USER_BY_PROF_POST = ") limit 100;";

  private final TopazContext ctx;
  private final ProfilesPEP pep;
  private final String      baseURI;

  private String[] newProfIds = new String[0];
  private int      newProfIdIdx;

  static {
    aliases = ItqlHelper.getDefaultAliases();
    aliases.put("foaf", FOAF_URI);
    aliases.put("bio",  BIO_URI);
    aliases.put("addr", ADDR_URI);
  }

  /**
   * Initialize the ITQL model. 
   *
   * @param itql itql handle to use
   */
  public static void initializeModel(ItqlHelper itql) throws RemoteException {
    itql.doUpdate("create " + MODEL + " " + MODEL_TYPE + ";", aliases);
    itql.doUpdate("create " + STR_MODEL + " " + STR_MODEL_TYPE + ";", aliases);
  }

  /**
   * Create a new profiles instance.
   *
   * @param pep the policy-enforcer to use for access-control
   * @param ctx the topaz context
   *
   */
  public ProfilesImpl(ProfilesPEP pep, TopazContext ctx) {
    this.ctx   = ctx;
    this.pep   = pep;
    this.baseURI = ctx.getObjectBaseUri().toString();
  }
  /** 
   * Create a new profiles manager instance. 
   *
   * @param mulgaraSvc the mulgara web-service
   * @param fedoraSvc  the fedora management web-service
   * @param pep        the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the mulgara or fedora services
   * @throws ConfigurationException if any required config is missing
   * @throws IOException if an error occurred talking to the mulgara or fedora services
   */
  public ProfilesImpl(ProtectedService mulgaraSvc, ProtectedService fedoraSvc, ProfilesPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this.pep = pep;

    ItqlHelper itql = new ItqlHelper(mulgaraSvc);

    FedoraAPIM apim = APIMStubFactory.create(fedoraSvc);

    Configuration conf = ConfigurationStore.getInstance().getConfiguration();
    conf = conf.subset("topaz");

    if (!conf.containsKey("objects.base-uri"))
      throw new ConfigurationException("missing key 'topaz.objects.base-uri'");
    baseURI = conf.getString("objects.base-uri");

    try {
      new URI(baseURI);
    } catch (URISyntaxException use) {
      throw new ConfigurationException("key 'topaz.objects.base-uri' does not contain a valid URI",
                                       use);
    }
    ctx = new SimpleTopazContext(itql, apim, null);
  }

  /** 
   * Create a new profiles manager instance. 
   *
   * @param mulgaraUri  the uri of the mulgara server
   * @param fedoraUri   the uri of fedora
   * @param username    the username to talk to fedora
   * @param password    the password to talk to fedora
   * @param pep         the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the mulgara or fedora services
   * @throws ConfigurationException if any required config is missing
   * @throws IOException if an error occurred talking to the mulgara or fedora services
   */
  public ProfilesImpl(URI mulgaraUri, URI fedoraUri, String username, String password,
                      ProfilesPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this(new UnProtectedService(mulgaraUri.toString()),
         new PasswordProtectedService(fedoraUri.toString(), username, password),
         pep);
  }

  public UserProfile getProfile(String userId) throws NoSuchUserIdException, RemoteException {
    URI owner = ItqlHelper.validateUri(userId, "userId");

    if (log.isDebugEnabled())
      log.debug("Getting profile for '" + userId + "'");

    // get raw profile
    UserProfile prof = getRawProfile(userId);
    if (prof == null)
      return null;

    // filter profile based on access-controls.
    if (prof.getDisplayName() != null && !checkAccess(owner, pep.GET_DISP_NAME))
      prof.setDisplayName(null);
    if (prof.getRealName() != null && !checkAccess(owner, pep.GET_REAL_NAME))
      prof.setRealName(null);
    if (prof.getGivenNames() != null && !checkAccess(owner, pep.GET_GIVEN_NAMES))
      prof.setGivenNames(null);
    if (prof.getSurnames() != null && !checkAccess(owner, pep.GET_SURNAMES))
      prof.setSurnames(null);
    if (prof.getTitle() != null && !checkAccess(owner, pep.GET_TITLE))
      prof.setTitle(null);
    if (prof.getGender() != null && !checkAccess(owner, pep.GET_GENDER))
      prof.setGender(null);
    if (prof.getPositionType() != null && !checkAccess(owner, pep.GET_POSITION_TYPE))
      prof.setPositionType(null);
    if (prof.getOrganizationName() != null && !checkAccess(owner, pep.GET_ORGANIZATION_NAME))
      prof.setOrganizationName(null);
    if (prof.getOrganizationType() != null && !checkAccess(owner, pep.GET_ORGANIZATION_TYPE))
      prof.setOrganizationType(null);
    if (prof.getPostalAddress() != null && !checkAccess(owner, pep.GET_POSTAL_ADDRESS))
      prof.setPostalAddress(null);
    if (prof.getCity() != null && !checkAccess(owner, pep.GET_CITY))
      prof.setCity(null);
    if (prof.getCountry() != null && !checkAccess(owner, pep.GET_COUNTRY))
      prof.setCountry(null);
    if (prof.getEmail() != null && !checkAccess(owner, pep.GET_EMAIL))
      prof.setEmail(null);
    if (prof.getHomePage() != null && !checkAccess(owner, pep.GET_HOME_PAGE))
      prof.setHomePage(null);
    if (prof.getWeblog() != null && !checkAccess(owner, pep.GET_WEBLOG))
      prof.setWeblog(null);
    if (prof.getBiography() != null && !checkAccess(owner, pep.GET_BIOGRAPHY))
      prof.setBiography(null);
    if (prof.getInterests() != null && !checkAccess(owner, pep.GET_INTERESTS))
      prof.setInterests(null);
    if (prof.getPublications() != null && !checkAccess(owner, pep.GET_PUBLICATIONS))
      prof.setPublications(null);
    if (prof.getBiographyText() != null && !checkAccess(owner, pep.GET_BIOGRAPHY_TEXT))
      prof.setBiographyText(null);
    if (prof.getInterestsText() != null && !checkAccess(owner, pep.GET_INTERESTS_TEXT))
      prof.setInterestsText(null);
    if (prof.getResearchAreasText() != null && !checkAccess(owner, pep.GET_RESEARCH_AREAS_TEXT))
      prof.setResearchAreasText(null);

    return prof;
  }

  private boolean checkAccess(URI owner, String perm) {
    try {
      pep.checkAccess(perm, owner);
      return true;
    } catch (SecurityException se) {
      if (log.isDebugEnabled())
        log.debug("access '" + perm + "' to '" + owner + "'s profile denied", se);
      return false;
    }
  }

  public void setProfile(String userId, UserProfile profile)
      throws NoSuchUserIdException, RemoteException {
    pep.checkAccess(pep.SET_PROFILE, ItqlHelper.validateUri(userId, "userId"));

    if (log.isDebugEnabled())
      log.debug("Setting profile for '" + userId + "'");

    ItqlHelper itql = ctx.getItqlHelper();
    String txn = "set-profile " + userId;
    try {
      itql.beginTxn(txn);

      String profId = getProfileId(userId);
      if (profId == null)
        profId = getNewProfId();

      setRawProfile(userId, profId, profile);

      itql.commitTxn(txn);
      txn = null;
    } finally {
      try {
        if (txn != null)
          itql.rollbackTxn(txn);
      } catch (Throwable t) {
        log.debug("Error rolling failed transaction", t);
      }
    }
  }

  public String[] findUsersByProfile(UserProfile[] templates, boolean[] ignoreCase)
      throws RemoteException {
    pep.checkAccess(pep.FIND_USERS_BY_PROF, URI.create("dummy:dummy"));

    if (templates == null || templates.length == 0)
      return new String[0];

    if (log.isDebugEnabled())
      log.debug("find users by profile");

    StringBuffer qry = new StringBuffer(ITQL_FIND_USER_BY_PROF_PRE.length() +
                                        templates.length * 100 +
                                        ITQL_FIND_USER_BY_PROF_POST.length());
    qry.append(ITQL_FIND_USER_BY_PROF_PRE);

    boolean haveTempl = false;
    int     matchAll  = -1;
    int[]   cntr      = new int[] { 0 };
    for (int idx = 0; idx < templates.length; idx++) {
      if (templates[idx] == null)
        continue;
      haveTempl = true;

      qry.append("(");
      if (!templateToConstraints(templates[idx], idx, qry, ignoreCase[idx], cntr)) {
        matchAll = idx;
        break;
      }
      qry.append(") or ");
    }

    if (!haveTempl)
      return new String[0];

    if (matchAll >= 0) {
      // deal with special case of empty template to avoid kowari bug and optimize this case
      qry.setLength(ITQL_FIND_USER_BY_PROF_PRE.length());
      qry.append("$tmpl <tucana:is> '").append(matchAll).append("'");
    } else
      // remove trailing ' or '
      qry.setLength(qry.length() - 4);

    qry.append(ITQL_FIND_USER_BY_PROF_POST);

    try {
      AnswerSet ans = new AnswerSet(ctx.getItqlHelper().doQuery(qry.toString(), aliases));
      ans.next();
      AnswerSet.QueryAnswerSet rows = ans.getQueryResults();

      Set ids = new HashSet();

      while (rows.next()) {
        String userId = rows.getString("userId");
        String tmpl   = rows.getString("tmpl");
        if (matchAll >= 0 || checkQueryAccess(userId, templates[Integer.parseInt(tmpl)]))
          ids.add(userId);
      }

      return (String[]) ids.toArray(new String[ids.size()]);
    } catch (AnswerException ae) {
      throw new RemoteException("Error looking up users by profile", ae);
    }
  }

  protected boolean templateToConstraints(UserProfile templ, int idx, StringBuffer sb,
                                          boolean ignCase, int[] cntr) {
    int startLen = sb.length();

    addValConstraint(templ.getDisplayName(), "topaz:displayName", sb, ignCase, cntr);
    addValConstraint(templ.getRealName(), "foaf:name", sb, ignCase, cntr);

    addValConstraint(templ.getGivenNames(), "foaf:givenname", sb, ignCase, cntr);
    addValConstraint(templ.getSurnames(), "foaf:surname", sb, ignCase, cntr);
    addValConstraint(templ.getTitle(), "foaf:title", sb, ignCase, cntr);
    addValConstraint(templ.getGender(), "foaf:gender", sb, ignCase, cntr);
    addValConstraint(templ.getBiography(), "bio:olb", sb, ignCase, cntr);
    addValConstraint(templ.getPositionType(), "topaz:positionType", sb, ignCase, cntr);
    addValConstraint(templ.getOrganizationName(), "topaz:organizationName", sb, ignCase, cntr);
    addValConstraint(templ.getOrganizationType(), "topaz:organizationType", sb, ignCase, cntr);
    addValConstraint(templ.getPostalAddress(), "topaz:postalAddress", sb, ignCase, cntr);
    addValConstraint(templ.getCity(), "addr:town", sb, ignCase, cntr);
    addValConstraint(templ.getCountry(), "addr:country", sb, ignCase, cntr);

    String email = templ.getEmail();
    if (email != null)
      addRefConstraint("mailto:" + email, "foaf:mbox", sb, ignCase, cntr);

    addRefConstraint(templ.getHomePage(), "foaf:homepage", sb, ignCase, cntr);
    addRefConstraint(templ.getWeblog(), "foaf:weblog", sb, ignCase, cntr);
    addRefConstraint(templ.getPublications(), "foaf:publications", sb, ignCase, cntr);

    String[] interests = templ.getInterests();
    for (int idx2 = 0; interests != null && idx2 < interests.length; idx2++)
      addRefConstraint(interests[idx2], "foaf:interest", sb, ignCase, cntr);

    addValConstraint(templ.getBiographyText(), "topaz:bio", sb, ignCase, cntr);
    addValConstraint(templ.getInterestsText(), "topaz:interests", sb, ignCase, cntr);
    addValConstraint(templ.getResearchAreasText(), "topaz:researchAreas", sb, ignCase, cntr);

    int endLen = sb.length();

    sb.append("$tmpl <tucana:is> '").append(idx).append("'");

    return (endLen > startLen);
  }

  protected void addValConstraint(String field, String pred, StringBuffer sb, boolean ignCase,
                                  int[] cntr) {
    if (field != null) {
      if (ignCase) {
        sb.append("$profId <").append(pred).append("> $val").append(cntr[0]).
           append(" and $val").append(cntr[0]).append(" <topaz:equalsIgnoreCase> '").
           append(ItqlHelper.escapeLiteral(field)).append("' in ").append(STR_MODEL).
           append(" and ");
        cntr[0]++;
      } else
        sb.append("$profId <").append(pred).append("> '").
           append(ItqlHelper.escapeLiteral(field)).append("' and ");
    }
  }

  protected void addRefConstraint(String field, String pred, StringBuffer sb, boolean ignCase,
                                  int[] cntr) {
    if (field != null) {
      if (ignCase) {
        sb.append("$profId <").append(pred).append("> $val").append(cntr[0]).
           append(" and $val").append(cntr[0]).append(" <topaz:equalsIgnoreCase> <").
           append(ItqlHelper.validateUri(field, pred)).append("> in ").append(STR_MODEL).
           append(" and ");
        cntr[0]++;
      } else
        sb.append("$profId <").append(pred).append("> <").
           append(ItqlHelper.validateUri(field, pred)).append("> and ");
    }
  }

  protected boolean checkQueryAccess(String userId, UserProfile templ) {
    URI owner = URI.create(userId);

    if (templ.getDisplayName() != null && !checkAccess(owner, pep.GET_DISP_NAME))
      return false;
    if (templ.getRealName() != null && !checkAccess(owner, pep.GET_REAL_NAME))
      return false;
    if (templ.getGivenNames() != null && !checkAccess(owner, pep.GET_GIVEN_NAMES))
      return false;
    if (templ.getSurnames() != null && !checkAccess(owner, pep.GET_SURNAMES))
      return false;
    if (templ.getTitle() != null && !checkAccess(owner, pep.GET_TITLE))
      return false;
    if (templ.getGender() != null && !checkAccess(owner, pep.GET_GENDER))
      return false;
    if (templ.getPositionType() != null && !checkAccess(owner, pep.GET_POSITION_TYPE))
      return false;
    if (templ.getOrganizationName() != null && !checkAccess(owner, pep.GET_ORGANIZATION_NAME))
      return false;
    if (templ.getOrganizationType() != null && !checkAccess(owner, pep.GET_ORGANIZATION_TYPE))
      return false;
    if (templ.getPostalAddress() != null && !checkAccess(owner, pep.GET_POSTAL_ADDRESS))
      return false;
    if (templ.getCity() != null && !checkAccess(owner, pep.GET_CITY))
      return false;
    if (templ.getCountry() != null && !checkAccess(owner, pep.GET_COUNTRY))
      return false;
    if (templ.getEmail() != null && !checkAccess(owner, pep.GET_EMAIL))
      return false;
    if (templ.getHomePage() != null && !checkAccess(owner, pep.GET_HOME_PAGE))
      return false;
    if (templ.getWeblog() != null && !checkAccess(owner, pep.GET_WEBLOG))
      return false;
    if (templ.getBiography() != null && !checkAccess(owner, pep.GET_BIOGRAPHY))
      return false;
    if (templ.getInterests() != null && !checkAccess(owner, pep.GET_INTERESTS))
      return false;
    if (templ.getPublications() != null && !checkAccess(owner, pep.GET_PUBLICATIONS))
      return false;
    if (templ.getBiographyText() != null && !checkAccess(owner, pep.GET_BIOGRAPHY_TEXT))
      return false;
    if (templ.getInterestsText() != null && !checkAccess(owner, pep.GET_INTERESTS_TEXT))
      return false;
    if (templ.getResearchAreasText() != null && !checkAccess(owner, pep.GET_RESEARCH_AREAS_TEXT))
      return false;

    return true;
  }

  /**
   * Get the id (url) of the profile node for the given internal user-id.
   *
   * @param userId the user's internal id
   * @return the profile id, or null if this user doesn't have one (doesn't exist)
   * @throws NoSuchUserIdException if the user does not exist
   * @throws RemoteException if an error occurred talking to the db
   */
  protected String getProfileId(String userId) throws NoSuchUserIdException, RemoteException {
    try {
      /* Implementation note:
       * Instead of doing two queries we could also use a subquery:
       *
       *   select $userId subquery(
       *     select $profId from <profiles-model> where $profId <foaf:holdsAccount> $userId )
       *   from <user-model> where $userId <rdf:type> <foaf:OnlineAccount> and
       *                           $userId <tucana:is> <${userId}>;
       *
       * But the answer is harder to parse, and I'm not sure it gains anything over the two-query
       * solution in this case.
       */
      ItqlHelper itql = ctx.getItqlHelper();
      String qry = ItqlHelper.bindValues(ITQL_TEST_USERID, "userId", userId) +
                   ItqlHelper.bindValues(ITQL_GET_PROFID, "userId", userId);
      StringAnswer ans = new StringAnswer(itql.doQuery(qry, aliases));

      List user = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      if (user.size() == 0)
        throw new NoSuchUserIdException(userId);

      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(1)).getRows();
      return rows.size() == 0 ? null : ((String[]) rows.get(0))[0];
    } catch (AnswerException ae) {
      throw new RemoteException("Error getting profile-url for user '" + userId + "'", ae);
    }
  }

  /**
   * Set the profile. This does no access-checks.
   *
   * @param userId  the user's internal id
   * @param profId  the url of the profile node
   * @param profile the new profile
   * @throws RemoteException if an error occurred updating the profile
   */
  protected void setRawProfile(String userId, String profId, UserProfile profile)
      throws RemoteException {
    StringBuffer cmd = new StringBuffer(500);

    cmd.append(ItqlHelper.bindValues(ITQL_CLEAR_PROF, "profId", profId));

    if (profile != null) {
      cmd.append(ItqlHelper.bindValues(ITQL_CREATE_PROF, "profId", profId, "userId", userId));

      cmd.append("insert ");

      addLiteralVal(cmd, profId, "topaz:displayName", profile.getDisplayName());
      addLiteralVal(cmd, profId, "foaf:name", profile.getRealName());
      addLiteralVal(cmd, profId, "foaf:givenname", profile.getGivenNames());
      addLiteralVal(cmd, profId, "foaf:surname", profile.getSurnames());
      addLiteralVal(cmd, profId, "foaf:title", profile.getTitle());
      addLiteralVal(cmd, profId, "foaf:gender", profile.getGender());
      addLiteralVal(cmd, profId, "bio:olb", profile.getBiography());
      addLiteralVal(cmd, profId, "topaz:positionType", profile.getPositionType());
      addLiteralVal(cmd, profId, "topaz:organizationName", profile.getOrganizationName());
      addLiteralVal(cmd, profId, "topaz:organizationType", profile.getOrganizationType());
      addLiteralVal(cmd, profId, "topaz:postalAddress", profile.getPostalAddress());
      addLiteralVal(cmd, profId, "addr:town", profile.getCity());
      addLiteralVal(cmd, profId, "addr:country", profile.getCountry());

      String email = profile.getEmail();
      if (email != null)
        addReference(cmd, profId, "foaf:mbox", "mailto:" + email);

      addReference(cmd, profId, "foaf:homepage", profile.getHomePage());
      addReference(cmd, profId, "foaf:weblog", profile.getWeblog());
      addReference(cmd, profId, "foaf:publications", profile.getPublications());

      String[] interests = profile.getInterests();
      for (int idx = 0; interests != null && idx < interests.length; idx++)
        addReference(cmd, profId, "foaf:interest", interests[idx]);

      addLiteralVal(cmd, profId, "topaz:bio", profile.getBiographyText());
      addLiteralVal(cmd, profId, "topaz:interests", profile.getInterestsText());
      addLiteralVal(cmd, profId, "topaz:researchAreas", profile.getResearchAreasText());

      cmd.append(" into ").append(MODEL).append(";");
    }

    ctx.getItqlHelper().doUpdate(cmd.toString(), aliases);
  }

  private static final void addLiteralVal(StringBuffer buf, String subj, String pred, String lit) {
    if (lit == null)
      return;

    buf.append("<").append(subj).append("> <").append(pred).append("> '").
        append(ItqlHelper.escapeLiteral(lit)).append("' ");
  }

  private static final void addReference(StringBuffer buf, String subj, String pred, String uri) {
    if (uri == null)
      return;
    ItqlHelper.validateUri(uri, pred);

    buf.append("<").append(subj).append("> <").append(pred).append("> <").append(uri).append("> ");
  }

  /**
   * Get the profile. This does no access-checks.
   *
   * @param userId  the user's id
   * @return the user's profile, or null
   * @throws NoSuchUserIdException if the user does not exist
   * @throws RemoteException if an error occurred retrieving the profile
   */
  protected UserProfile getRawProfile(String userId) throws NoSuchUserIdException, RemoteException {
    StringAnswer ans;
    try {
      String qry = ItqlHelper.bindValues(ITQL_TEST_USERID, "userId", userId) +
                   ItqlHelper.bindValues(ITQL_GET_PROF, "userId", userId);
      ans = new StringAnswer(ctx.getItqlHelper().doQuery(qry, aliases));
    } catch (AnswerException ae) {
      throw new RemoteException("Error getting profile-info for user '" + userId + "'", ae);
    }

    List user = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
    if (user.size() == 0)
      throw new NoSuchUserIdException(userId);

    List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(1)).getRows();
    if (rows.size() == 0)
      return null;

    UserProfile prof = new UserProfile();
    List interests   = new ArrayList();

    for (int idx = 0; idx < rows.size(); idx++) {
      String[] row = (String[]) rows.get(idx);

      if (row[0].equals(ItqlHelper.TOPAZ_URI + "displayName"))
        prof.setDisplayName(row[1]);
      else if (row[0].equals(FOAF_URI + "name"))
        prof.setRealName(row[1]);
      else if (row[0].equals(FOAF_URI + "givenname"))
        prof.setGivenNames(row[1]);
      else if (row[0].equals(FOAF_URI + "surname"))
        prof.setSurnames(row[1]);
      else if (row[0].equals(FOAF_URI + "title"))
        prof.setTitle(row[1]);
      else if (row[0].equals(FOAF_URI + "gender"))
        prof.setGender(row[1]);
      else if (row[0].equals(ItqlHelper.TOPAZ_URI + "positionType"))
        prof.setPositionType(row[1]);
      else if (row[0].equals(ItqlHelper.TOPAZ_URI + "organizationName"))
        prof.setOrganizationName(row[1]);
      else if (row[0].equals(ItqlHelper.TOPAZ_URI + "organizationType"))
        prof.setOrganizationType(row[1]);
      else if (row[0].equals(ItqlHelper.TOPAZ_URI + "postalAddress"))
        prof.setPostalAddress(row[1]);
      else if (row[0].equals(ADDR_URI + "town"))
        prof.setCity(row[1]);
      else if (row[0].equals(ADDR_URI + "country"))
        prof.setCountry(row[1]);
      else if (row[0].equals(FOAF_URI + "mbox"))
        prof.setEmail(row[1].substring(7));
      else if (row[0].equals(FOAF_URI + "homepage"))
        prof.setHomePage(row[1]);
      else if (row[0].equals(FOAF_URI + "weblog"))
        prof.setWeblog(row[1]);
      else if (row[0].equals(BIO_URI + "olb"))
        prof.setBiography(row[1]);
      else if (row[0].equals(FOAF_URI + "publications"))
        prof.setPublications(row[1]);
      else if (row[0].equals(FOAF_URI + "interest"))
        interests.add(row[1]);
      else if (row[0].equals(ItqlHelper.TOPAZ_URI + "bio"))
        prof.setBiographyText(row[1]);
      else if (row[0].equals(ItqlHelper.TOPAZ_URI + "interests"))
        prof.setInterestsText(row[1]);
      else if (row[0].equals(ItqlHelper.TOPAZ_URI + "researchAreas"))
        prof.setResearchAreasText(row[1]);
    }

    if (interests.size() > 0)
      prof.setInterests((String[]) interests.toArray(new String[interests.size()]));

    return prof;
  }

  /** 
   * Get an id (url) for a new profile node. 
   * 
   * @return the url
   * @throws RemoteException if an error occurred getting the new id
   */
  protected synchronized String getNewProfId() throws RemoteException {
    if (newProfIdIdx >= newProfIds.length) {
      newProfIds = ctx.getFedoraAPIM().getNextPID(new NonNegativeInteger("20"), IDS_NS);
      newProfIdIdx = 0;
    }

    return baseURI + PROF_PATH_PFX + '/' +
           newProfIds[newProfIdIdx++].substring(IDS_NS.length() + 1);
  }
}
