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


import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.service.ItqlInterpreterException;

/**
 * Resolver for the rdf:type of a DOI-URI.
 *
 * @author Pradeep Krishnan, Alex Worden
 */
public class DOITypeResolver {
  private static final Log log = LogFactory.getLog(DOITypeResolver.class);
  private static final String MODEL = "<local:///topazproject#filter:model=ri>";
  private static final String QUERY = "select $t from " + MODEL + " where <${doi}> <rdf:type> $t";
  private static final String GET_ANNOTATES_QUERY = "select $x from " + MODEL + " where <${doi}> <http://www.w3.org/2000/10/annotation-ns#annotates> $x";
  
  //
  private ItqlHelper itql;

  /**
   * Creates a new DOITypeResolver object.
   *
   * @param mulgaraUri the mulgara service uri
   *
   * @throws MalformedURLException if service's uri is not a valid URL
   * @throws ServiceException if an error occurred locating the web-service
   * @throws RemoteException if an error occurred talking to the web-service
   */
  public DOITypeResolver(URI mulgaraUri)
                  throws MalformedURLException, ServiceException, RemoteException {
    itql = new ItqlHelper(mulgaraUri);
  }

  /**
   * Queries the ITQL database and returns all known rdf:type values of a URI.
   *
   * @param doi the doi uri
   *
   * @return returns an array of rdf:types
   *
   * @throws ItqlInterpreterException if an exception was encountered while processing the queries
   * @throws AnswerException if an exception occurred parsing the query response
   * @throws RemoteException if an exception occurred talking to the service
   */
  public Set<String> getRdfTypes(URI doi)
                       throws ItqlInterpreterException, AnswerException, RemoteException {
    String query = ItqlHelper.bindValues(QUERY, "doi", doi.toString());

    String results;

    synchronized (this) {
      results = itql.doQuery(query, null);
    }

    StringAnswer answer = new StringAnswer(results);
    List<String[]> rows = (List<String[]>)((StringAnswer.StringQueryAnswer) (answer.getAnswers().get(0))).getRows();

    HashSet<String> types = new HashSet<String>();
    for (String[] row : rows) {
      types.add(row[0]);
    }
    
    return types;
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
  public String getAnnotatedRoot(String annotationDoi) throws ItqlInterpreterException, RemoteException, AnswerException {
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
    
    return annotationDoi;
  }
  
  /**
   * Close the database handle.
   *
   * @throws Throwable on an error
   */
  protected void finalize() throws Throwable {
    try {
      itql.close();
    } catch (Throwable t) {
    }

    itql = null;

    super.finalize();
  }
}
