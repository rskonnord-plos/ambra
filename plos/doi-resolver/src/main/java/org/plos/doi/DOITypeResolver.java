/* $HeadURL:: $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.doi;


import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.RdfUtil;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.stores.ItqlStore;

/**
 * Resolver for the rdf:type of a DOI-URI.
 *
 * @author Pradeep Krishnan
 */
public class DOITypeResolver {
  private static final String MODEL = "<rmi://localhost/topazproject#filter:model=ri>";
  private static final Log log = LogFactory.getLog(DOITypeResolver.class);
  private static final String QUERY = "select $t from " + MODEL + " where <${doi}> <rdf:type> $t";
  private static final String GET_ANNOTATES_QUERY = "select $x from " + MODEL + " where <${doi}> <http://www.w3.org/2000/10/annotation-ns#annotates> $x";

  //
  private final SessionFactory sf;

  /**
   * Creates a new DOITypeResolver object.
   *
   * @param mulgaraUri the mulgara service uri
   *
   * @throws OtmException if an error occurred talking to the web-service
   */
  public DOITypeResolver(URI mulgaraUri) throws OtmException {
    sf = new SessionFactoryImpl();
    sf.setTripleStore(new ItqlStore(mulgaraUri));
  }
  /**
   * Queries the ITQL database and returns all known rdf:type values of a URI.
   *
   * @param doi the doi uri
   *
   * @return returns an array of rdf:types
   *
   * @throws OtmException if an exception occurred talking to the service
   */
  public Set<String> getRdfTypes(URI doi) throws OtmException {
    String query = RdfUtil.bindValues(QUERY, "doi", doi.toString());

    List<String> types = new ArrayList<String>();
    Session sess = sf.openSession();
    try {
      Transaction tx = sess.beginTransaction();
      try {
        Results results = sess.doNativeQuery(query);
        results.beforeFirst();
        while (results.next())
          types.add(results.getString(0));
      } finally {
        tx.commit();
      }
    } finally {
      sess.close();
    }

    return new HashSet<String>(types);
  }

  /**
   * Recursively attempts to find the annotated root (article?) of the given annotation doi. Retruns the doi
   * found that does not annotate another doi. 
   * 
   * @param annotationDoi
   * @return
   * @throws ItqlInterpreterException
   * @throws RemoteException
   * @throws AnswerException
   */
  public String getAnnotatedRoot(String annotationDoi) {
    /* TODO - need to reimplement this without ItqlHelper... as that doesn't appear to be 
     * supported any longer in head. 
     * 
    String query = ItqlHelper.bindValues(GET_ANNOTATES_QUERY, "doi", annotationDoi.toString());
    
    String results;
    synchronized (this) {
      results = itql.doQuery(query, null);
    }
    StringAnswer answer = new StringAnswer(results);
    List<String[]> rows = (List<String[]>)((StringAnswer.StringQueryAnswer) (answer.getAnswers().get(0))).getRows();
    
    if ((rows.size() > 0) && (rows.get(0).length > 0)) {
      String annotatedDoi = rows.get(0)[0];
      if (annotatedDoi.equals(annotationDoi)) {
        log.error("Circular dependency found for annotation doi: '"+annotationDoi+"'");
        return annotatedDoi;
      }
      return getAnnotatedRoot(annotatedDoi);
    }
    */
    return annotationDoi;
  }
}
