/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 *
 * Modified from code part of Fedora. It's license reads:
 * License and Copyright: The contents of this file will be subject to the
 * same open source license as the Fedora Repository System at www.fedora.info
 * It is expected to be released with Fedora version 2.2.
 * Copyright 2006 by The Technical University of Denmark.
 * All rights reserved.
 */
package org.topazproject.fedoragsearch.topazlucene;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.TreeSet;

import javax.xml.transform.stream.StreamSource;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexModifier;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

import dk.defxws.fedoragsearch.server.GenericOperationsImpl;
import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

/**
 * Performs the Lucene specific parts of the operations.
 *
 * Topaz is adding the following additional features.
 * <ul>
 *   <li>Cache DTDs
 *   <li>Axis filtering
 * </ul>
 *
 * @author  Eric Brown and <a href='mailto:gsp@dtv.dk'>Gert</a>
 * @version $Id$
 */
public class OperationsImpl extends GenericOperationsImpl {
  private static final Log log               = LogFactory.getLog(OperationsImpl.class);

  private static final String INDEX_PATH     = TopazConfig.INDEX_PATH;
  private static final String FEDORAOBJ_PATH = TopazConfig.FEDORAOBJ_PATH;

  private static final String GFIND_XSLT     = TopazConfig.GFIND_XSLT;
  private static final String BROWSE_XSLT    = TopazConfig.BROWSE_XSLT;
  private static final String INDEXINFO_XSLT = TopazConfig.INDEXINFO_XSLT;
  private static final String UPDATE_XSLT    = TopazConfig.UPDATE_XSLT;
  private static final String FOXML2LUCENE_XSLT = TopazConfig.FOXML2LUCENE_XSLT;
  private static final String FEDORA_BASE_URL   = TopazConfig.FEDORA_BASE_URL;

  private static final String INDEXINFO_XML  = TopazConfig.INDEXINFO_XML;
  private static final Analyzer analyzer     = TopazConfig.getAnalyzer();

  /**
   * Find a set of documents.
   *
   * @param query is the lucene formatted query string
   * @param hitPageStart is the hit number to start on (not the page # to start on)
   * @param hitPageSize is the number of hits to display on each page
   * @param snippetsMax is the maximum number of snippets to return per field
   * @param fieldMaxLength is the maximum number of characters to return in one field.
   *        If the field value exceeds this length, it will be truncated with elipses.
   * @param indexName is the name of the index to search
   * @param resultPageXslt is the transform to use on the results. (Ignored right now.)
   */
  public String gfindObjects(String query,
                             long   hitPageStart,
                             int    hitPageSize,
                             int    snippetsMax,
                             int    fieldMaxLength,
                             String indexName,
                             String resultPageXslt) throws RemoteException {
    log.debug("gfind: " + query);
    // Call base-class... not sure exactly what this does (perhaps setup some instance variables)
    super.gfindObjects(query, hitPageStart, hitPageSize, snippetsMax, fieldMaxLength,
                       indexName, resultPageXslt);
    // Do the query
    ResultSet resultSet =
        (new Connection()).createStatement().executeQuery(query, hitPageStart, hitPageSize,
                                                          snippetsMax, fieldMaxLength);

    if (log.isDebugEnabled())
      log.debug("resultSet.getResultXml():\n" + resultSet.getResultXml());

    // Do we really need to do this??? We just want to copy the xml...
    /* This generates some error due to bad gfindObjectstoResultPage...
    StringBuffer sb = TopazTransformer.transform(GFIND_XSLT, resultSet.getResultXml(), params);
    return sb.toString();
    */
    return resultSet.getResultXml().toString();
  }

  public String browseIndex(String startTerm,
                            int termPageSize,
                            String fieldName,
                            String indexName,
                            String resultPageXslt) throws java.rmi.RemoteException {
    super.browseIndex(startTerm, termPageSize, fieldName, indexName, resultPageXslt);
    StringBuffer resultXml = new StringBuffer("<fields>");
    int termNo = 0;
    IndexReader ir = null;

    try {
      ir = IndexReader.open(INDEX_PATH);
      Iterator fieldNames =
        (new TreeSet(ir.getFieldNames(IndexReader.FieldOption.INDEXED))).iterator();

      while (fieldNames.hasNext()) {
        resultXml.append("<field>"+fieldNames.next()+"</field>");
      }
      resultXml.append("</fields>");
      resultXml.append("<terms>");
      int pageSize = 0;
      Term beginTerm = new Term(fieldName, "");
      TermEnum terms;
      try {
        terms = ir.terms(beginTerm);
      } catch (IOException e) {
        throw new GenericSearchException("IndexReader terms error:\n" + e.toString());
      }
      try {
        while (terms.term()!=null && terms.term().field().equals(fieldName)
               && !"".equals(terms.term().text().trim())) {
          termNo++;
          if (startTerm.compareTo(terms.term().text()) <= 0 && pageSize < termPageSize) {
            pageSize++;
            resultXml.append("<term no=\""+termNo+"\""
                             +" fieldtermhittotal=\""+terms.docFreq()
                             +"\">"+terms.term().text()+"</term>");
          }
          terms.next();
        }
      } catch (IOException e) {
        throw new GenericSearchException("IndexReader terms.next error:\n" + e.toString());
      }
      try {
        terms.close();
      } catch (IOException e) {
        throw new GenericSearchException("IndexReader terms close error:\n" + e.toString());
      }
    } catch (IOException e) {
      throw new GenericSearchException("IndexReader new error:\n" + e.toString());
    } finally {
      if (ir!=null)
        try {
          ir.close();
        } catch (IOException e) {
        }
    }
    resultXml.append("</terms>");
    resultXml.insert(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                     + "<lucenebrowseindex "
                     + "   xmlns:dc=\"http://purl.org/dc/elements/1.1/"
                     + "\" startTerm=\"" + startTerm
                     + "\" termPageSize=\"" + termPageSize
                     + "\" fieldName=\"" + fieldName
                     + "\" indexName=\"" + indexName
                     + "\" termTotal=\"" + termNo + "\">");
    resultXml.append("</lucenebrowseindex>");

    if (log.isDebugEnabled())
      log.debug("resultXml="+resultXml);

    params[10] = "RESULTPAGEXSLT";
    params[11] = resultPageXslt;

    // Transform "browse" API output -- probably just copyxml -- why are we doing this???
    StringBuffer sb = TopazTransformer.transform(BROWSE_XSLT, resultXml, params);

    return sb.toString();
  }

  public String getIndexInfo(String indexName,
                             String resultPageXslt) throws RemoteException {
    super.getIndexInfo(indexName, resultPageXslt);
    InputStream infoStream =  null;
    String indexInfoPath = INDEXINFO_XSLT;
    try {
      infoStream = OperationsImpl.class.getResourceAsStream(indexInfoPath);
      if (infoStream == null) {
        throw new GenericSearchException("Error " + indexInfoPath + " not found in classpath");
      }
    } catch (IOException e) {
      throw new GenericSearchException("Error " + indexInfoPath + " not found in classpath", e);
    }

    // Transform "index info" API output -- probably just copyxml
    StringBuffer sb = TopazTransformer.transform(INDEXINFO_XSLT, infoStream, new String[] {});

    return sb.toString();
  }


  /**
   * Structure used to return results from sub-methods.
   */
  static class UpdateResults {
    StringBuffer resultXml = new StringBuffer();
    int insertTotal = 0;
    int deleteTotal = 0;
    int updateTotal = 0;
  }

  /**
   * updateIndex API is used to update the lucene index.
   *
   * Note: createEmpty is NOT supported. We do this if necessary on initialization in TopazConfig.
   *
   * @param action This is the sub-function: fromFoxmlFiles, fromPid or deletePid.
   *        The higher level API supports createEmpty. We do not.
   * @param value This is the value to use for the specific sub-function. It is usually
   *        a PID or a file or directory name.
   * @param repositoryName This is the name of the repository to update.
   * @param indexName This is the name of the index to update.
   * @param indexDocXslt The name of the xslt stylesheet to transform foxml with.
   * @param resultPageXslt The name of the xslt stylesheet to transform the results with.
   *        These results are practially meaningless. They are just informational. The
   *        stylesheet must be something that TopazTransformer can load (usually something
   *        in the java classpath.) It must support the following stylesheet parameters:
   *        OPERATION, ACTION, VALUE, REPOSITORYNAME, INDEXNAME, RESULTPAGEXSLT
   */
  public synchronized String updateIndex(String action,
                                         String value,
                                         String repositoryName,
                                         String indexName,
                                         String indexDocXslt,
                                         String resultPageXslt) throws RemoteException {
    if (log.isDebugEnabled())
      log.debug(action + ": '" + value + "'");

    UpdateResults results = new UpdateResults();
    results.resultXml.append("<luceneUpdateIndex indexName=\"").append(indexName).append("\">\n");

    IndexModifier modifier = null;
    int docCount = 0;
    try {
      // TODO: We should really only create once, but then would need web-listener to destroy
      modifier = new IndexModifier(INDEX_PATH, TopazConfig.getAnalyzer(), false);

      if ("fromFoxmlFiles".equals(action))
        fromFoxmlFiles(value, repositoryName, indexDocXslt, results, modifier);
      else if ("fromPid".equals(action))
        fromPid(value, repositoryName, indexDocXslt, results, modifier);
      else if ("deletePid".equals(action))
        deletePid(value, results, modifier);

      docCount = modifier.docCount();
      modifier.optimize();
    } catch (IOException ioe) {
      throw new GenericSearchException("Error talking to lucene", ioe);
    } finally {
      try {
        if (modifier != null)
          modifier.close();
      } catch (IOException ioe) {
        // The thing is aready close? Shouldn't happen
        log.warn("Error closing modifier", ioe);
      }
    }

    // Get rid of our old IndexSearcher
    Statement.allocateNewSearcher();

    results.resultXml.append("<counts")
      .append(" insertTotal=\"").append(results.insertTotal).append("\"")
      .append(" updateTotal=\"").append(results.updateTotal).append("\"")
      .append(" deleteTotal=\"").append(results.deleteTotal).append("\"")
      .append(" docCount=\"").append(docCount).append("\"")
      .append("/>\n")
      .append("</luceneUpdateIndex>\n");

    // Setup parameters for transform below
    params = new String[] {
      "OPERATION",      "updateIndex",
      "ACTION",         action,
      "VALUE",          value,
      "REPOSITORYNAME", repositoryName,
      "INDEXNAME",      indexName,
      "RESULTPAGEXSLT", resultPageXslt,
    };

    // Transform updateIndex results
    StringBuffer sb = TopazTransformer.transform(UPDATE_XSLT, results.resultXml, params);

    if (log.isDebugEnabled())
      log.debug("Index updated:\n" + sb);

    return sb.toString();
  }

  /**
   * Delete the indicated PID from the lucene index.
   */
  private void deletePid(String pid, UpdateResults results, IndexModifier modifier)
      throws GenericSearchException {
    try {
      results.deleteTotal += modifier.deleteDocuments(new Term("PID", pid));
    } catch (IOException e) {
      throw new GenericSearchException("Unable to delete pid '" + pid + "'", e);
    }
  }

  /**
   * Index the document(s) in the given file or directory.
   */
  private void fromFoxmlFiles(String filePath, String repositoryName, String indexDocXslt,
                              UpdateResults results, IndexModifier modifier)
      throws RemoteException {
    File objectDir = null;
    if (filePath == null || filePath.equals("")) {
      objectDir = new File(FEDORAOBJ_PATH);
      if (objectDir == null)
        throw new RemoteException("Fedora object path does not exist '" + FEDORAOBJ_PATH + "'");
    } else
      objectDir = new File(filePath);

    indexDocs(objectDir, repositoryName, indexDocXslt, results, modifier);
  }

  /**
   * Index the document of the indicated PID.
   */
  private void fromPid(String pid, String repositoryName, String indexDocXslt,
                       UpdateResults results, IndexModifier modifier) throws RemoteException {
    // Call super to get foxml -- stashed into super.foxmlRecord attribute (byte[])
    getFoxmlFromPid(pid, repositoryName);

    InputStream foxmlStream = new ByteArrayInputStream(foxmlRecord);
    indexDoc(pid, repositoryName, foxmlStream, indexDocXslt, results, modifier);
  }


  /**
   * Index the document(s) indicated by the specified file or directory.
   *
   * @param file Is a file or directory to index. If a directory, all foxml files found under
   *             that directory are indexed/re-indexed.
   *
   */
  private void indexDocs(File file, String repositoryName, String indexDocXslt,
                         UpdateResults results, IndexModifier modifier) throws RemoteException {
    if (file.isDirectory())
    {
      String[] files = file.list();
      for (int i = 0; i < files.length; i++)
        indexDocs(new File(file, files[i]), repositoryName, indexDocXslt, results, modifier);
    }
    else
    {
      try {
        InputStream foxmlStream = new FileInputStream(file);
        indexDoc(file.getName(), repositoryName, foxmlStream, indexDocXslt, results, modifier);
      } catch (FileNotFoundException e) {
        throw new GenericSearchException("Error reading file '" + file.getAbsolutePath() + "'", e);
      }
    }

    results.resultXml.append("<docCount>").append(modifier.docCount()).append("</docCount>\n");
  }

  /**
   * Index (or re-index) our document in lucene.
   *
   * @param identifier A string representing what is being ingested for logging purposes.
   * @param repositoryName The string to put into the meta-data.
   * @param foxmlStream The stream of foxml data from fedora.
   * @param indexDocXslt The name of the xslt stylesheet to transform foxml with.
   * @param results The results object to store our results into.
   * @param modifier The lucene IndexModifier to use to index the doucment.
   * @throws RemoteException If there is a problem.
   */
  private void indexDoc(String        identifier,
                        String        repositoryName,
                        InputStream   foxmlStream,
                        String        indexDocXslt,
                        UpdateResults results,
                        IndexModifier modifier) throws RemoteException {
    // Transform our stuff to get it into lucene
    StringBuffer sb = TopazTransformer.transform(
      FOXML2LUCENE_XSLT, foxmlStream, new String[] {
          "fedoraBaseURL", FEDORA_BASE_URL,
          "articleDS",     "XML" });

    if (log.isDebugEnabled())
      log.debug("indexDoc=\n"+sb.toString());

    /* Parse the XML our TopazTransformer returned above and create a Lucene Document.
     * This happends mostly in the constructor. All we care about here after that is the
     * Lucene Document and the PID.
     */
    IndexDocumentHandler hdlr = new IndexDocumentHandler(this, repositoryName, identifier, sb);
    Document doc = hdlr.getIndexDocument();
    String pid = hdlr.getPid();

    try {
      // Try to delete the document
      int deleted = 0;
      if (!(pid == null || pid.equals("")))
        deleted = modifier.deleteDocuments(new Term("PID", pid));
      results.deleteTotal += deleted; // Add count to instance variable

      // If our document has any fields (i.e. it isn't an image with no meta data), continue...
      if (doc.fields().hasMoreElements()) {
        // Add field(s) to our document's fields
        doc.add(new Field("repositoryName", repositoryName, Field.Store.YES,
                          Field.Index.UN_TOKENIZED, Field.TermVector.NO));

        // Add our document to lucene
        modifier.addDocument(doc);
        results.resultXml.append("<insert>").append(pid).append("</insert>\n")
                         .append("<docCount>").append(modifier.docCount()).append("</docCount>\n");

        // Update some instance variables used by calling methods
        if (deleted > 0) {
          results.updateTotal++;
          results.deleteTotal -= deleted;
        }
        else
          results.insertTotal++;
      }
    } catch (IOException e) {
      throw new GenericSearchException("Update error on '" + identifier + "'", e);
    }
  }
}
