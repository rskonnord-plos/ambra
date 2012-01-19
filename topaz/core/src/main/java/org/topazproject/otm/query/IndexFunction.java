/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.query;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import antlr.ASTFactory;
import antlr.RecognitionException;

import org.topazproject.mulgara.itql.AbstractAnswer;
import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.CollectionType;

/** 
 * @author Ronald Tschalär
 */
class IndexFunction implements ProjectionFunction, ConstraintsTokenTypes, TransformListener {
  public  static final String   FUNC_NAME = "index";
  private static final ExprType RET_TYPE  = ExprType.literalType(Rdf.xsd + "int", null);

  private final String               projVar;
  private       OqlAST[]             derefNodes;
  private       OqlAST[]             projList;
  private       CollectionType           colType;
  private       int                  projCol = -1;
  private       Map<String, Integer> indexes;

  /**
   * Create the index-function instance. We check that the argument makes sense: a single argument,
   * which is a variable reference and which contains no further dereferences (i.e. 'index(a)').
   */
  IndexFunction(List<OqlAST> args, List<ExprType> types) throws RecognitionException {
    if (args.size() != 1)
      throw new RecognitionException("index() must have exactly 1 argument, but found " +
                                     args.size());
    if (types.get(0) == null)
      throw new RecognitionException("The type of the argument to the index() function may not " +
                                     "be 'unknown'");

    if (args.get(0).getType() != REF)
      throw new RecognitionException("the argument to index() must be a variable reference");
    OqlAST var = (OqlAST) args.get(0).getFirstChild();
    if (var.getType() != ID || !var.isVar() || var.getNextSibling() != null)
      throw new RecognitionException("the argument to index() must reference an alias");

    projVar = var.getText();
  }

  public String getName() {
    return FUNC_NAME;
  }

  public ExprType getReturnType() {
    return RET_TYPE;
  }

  /**
   * Try to resolve the alias, find the last deref, and register ourselves on the predicate of that
   * last deref. E.g. in 'select index(a) from ... where a:= b.c.d' we look for the '.d' and hook
   * into the processing thereof so we can replace the 'index(a)' with the predicate variable.
   */
  public void postPredicatesHook(OqlAST pre, OqlAST post) throws RecognitionException {
    // #(SELECT #(FROM fclause) #(WHERE wclause) #(PROJ sclause) (oclause)? (lclause)? (tclause)?)
    assert post.getType() == SELECT;

    OqlAST f = (OqlAST) post.getFirstChild();
    OqlAST w = (OqlAST) f.getNextSibling();

    assert f.getType() == FROM;
    assert w.getType() == WHERE;

    OqlAST def = findAliasDef(projVar, (OqlAST) w.getFirstChild());
    if (def != null)
      def.addListener(this);
    else
      throw new RecognitionException("'" + projVar + "' is not an alias or the alias is not a " +
                                     "dereference");
  }

  private OqlAST findAliasDef(String var, OqlAST top) throws RecognitionException {
    return findAliasDef(var, top, top);
  }

  private OqlAST findAliasDef(String var, OqlAST cur, OqlAST top) throws RecognitionException {
    if (cur.getType() == ASGN) {
      if (var.equals(cur.getFirstChild().getText())) {
        OqlAST fact = (OqlAST) cur.getFirstChild().getNextSibling();
        switch (fact.getType()) {
          case REF:
            return getLastDeref((OqlAST) cur.getFirstChild(), top);

          case QSTRING:
          case URIREF:
            throw new RecognitionException("can't perform index() on a constant: " +
                                           fact.toStringTree());

          case FUNC:
            throw new RecognitionException("can't perform index() on another function: " +
                                           fact.toStringTree());

          default:
            throw new RecognitionException("unexpected type in alias asignment for '" + var +
                                           "': " + cur.toStringTree());
        }
      }
    } else {
      for (OqlAST n = (OqlAST) cur.getFirstChild(); n != null; n = (OqlAST) n.getNextSibling()) {
        OqlAST res = findAliasDef(var, n, top);
        if (res != null)
          return res;
      }
    }
    return null;
  }

  private OqlAST getLastDeref(OqlAST deref, OqlAST top) throws RecognitionException {
    if (deref.getNextSibling() == null && deref.getType() == ID) { // presumably another alias
      return findAliasDef(deref.getText(), top);
    } else {
      while (deref.getNextSibling() != null)
        deref = (OqlAST) deref.getNextSibling();

      switch (deref.getType()) {
        case URIREF:
          return deref;

        case REF:
          return getLastDeref((OqlAST) deref.getFirstChild(), top);

        default:
          throw new RecognitionException("can't perform index() on " +
                                         (deref.getType() == EXPR ? "predicate-expression" :
                                          deref.getType() == STAR ? "predicate-wildcard" :
                                          deref.getType() == ID ? "id-field" :
                                          "unknown node type " + deref.getType()) +
                                         ": " + deref.toStringTree());
      }
    }
  }

  /**
   * Remember the predicate node(s) being generated for the collection.
   */
  public void deref(OqlAST reg, OqlAST[] nodes) {
    derefNodes = nodes;
  }

  /**
   * Generate where-clause constraints for index(a). We also go ahead at this point and set up the
   * projection variables we want to be used, though they won't be asked for till
   * getItqlProjections() below.
   */
  public OqlAST toItql(List<OqlAST> args, List<OqlAST> vars, OqlAST resVar, ASTFactory af)
      throws RecognitionException {
    OqlAST arg = args.get(0);
    OqlAST var = vars.get(0);

    // process depending on collection-type
    colType = var.getExprType().getCollectionType();
    switch (colType) {
      case PREDICATE:
        return ASTUtil.makeTree(af, AND, "and", af.dupTree(arg),
                                ASTUtil.makeTriple(var, "<mulgara:equals>", resVar, af));

      case RDFBAG:
      case RDFSEQ:
      case RDFALT:
        OqlAST pred = derefNodes[3];
        return ASTUtil.makeTree(af, AND, "and", af.dupTree(arg),
                                ASTUtil.makeTriple(pred, "<mulgara:equals>", resVar, af));

      case RDFLIST:
        OqlAST s = (OqlAST) af.dupTree(derefNodes[2]);
        OqlAST n = (OqlAST) af.dupTree(derefNodes[3]);
        projList = new OqlAST[] { s, n };
        return ASTUtil.makeTree(af, AND, "and", af.dupTree(arg),
                                ASTUtil.makeTriple(s, "<mulgara:equals>", resVar, af));

      default:
        throw new RecognitionException("Unknown mapper-type '" + colType + "'");
    }
  }

  /**
   * Generate projection variable(s) for index(a). For RdfList this is a pair representing the subj
   * and obj in 's &lt;rdf:next&gt; o'; for all others this is null, meaning use normal projection
   * variable (which was tied to the variable representing the predicate via additional where-clause
   * constraints in toItql() above).
   */
  public OqlAST[] getItqlProjections() {
    return projList;
  }

  public List<String> initVars(List<String> vars, int col) {
    if (projCol < 0)
      projCol = col;

    if (col == projCol + 1)
      vars.remove(col);

    return vars;
  }

  public List<Object> initTypes(List<Object> types, int col) {
    if (projCol < 0)
      projCol = col;

    if (col == projCol + 1)
      types.remove(col);
    else
      types.set(col, String.class);

    return types;
  }

  /**
   */
  public Answer initItqlResult(Answer qa, int col) throws OtmException {
    if (projCol < 0)
      projCol = col;

    if (col == projCol)
      return qa;

    try {
      qa.beforeFirst();
      indexes = buildRdfListOrder(qa, projCol);

      qa.beforeFirst();
      return new RdfListAnswer(qa, projCol);
    } catch (AnswerException ae) {
      throw new OtmException("Error parsing answer", ae);
    }
  }

  private Map<String, Integer> buildRdfListOrder(Answer qa, int col) throws AnswerException {
    Map<String, String>  fwd     = new HashMap<String, String>();
    Map<String, String>  rev     = new HashMap<String, String>();
    Map<String, Integer> indexes = new HashMap<String, Integer>();

    while (qa.next()) {
      String node = qa.getString(col);
      String next = qa.getString(col + 1);
      fwd.put(node, next);
      rev.put(next, node);
    }

    String node = rev.keySet().iterator().next();
    while (rev.containsKey(node))
      node = rev.get(node);

    for (int idx = 0; node != null; node = fwd.get(node), idx++)
      indexes.put(node, idx);

    return indexes;
  }

  /**
   * For multiple-predicate collections we just return the current row number; for RdfSeq/Bag/Alt 
   * we extract the index n directly from the predicate rdf:_&lt;n&gt;; for RdfList ...
   */
  public Object getItqlResult(Answer qa, int row, int col, Results.Type type, boolean eager)
      throws OtmException, AnswerException {
    int idx;

    switch (colType) {
      case PREDICATE:
        idx = row;
        break;

      case RDFBAG:
      case RDFSEQ:
      case RDFALT:
        // rdf:_<n> -> http://www.w3.org/1999/02/22-rdf-syntax-ns#_<n>
        idx = Integer.parseInt(qa.getString(col).substring(44)) - 1;
        break;

      case RDFLIST:
        idx = indexes.get(qa.getString(col));
        break;

      default:
        throw new OtmException("Unknown mapper-type '" + colType + "'");
    }

    return new Results.Literal(Integer.toString(idx), null, URI.create(RET_TYPE.getDataType()));
  }

  private static class RdfListAnswer extends AbstractAnswer {
    private final Answer delegate;
    private final int    pos;

    RdfListAnswer(Answer delegate, int pos) {
      this.delegate = delegate;
      this.pos      = pos;

      String[] vars = new String[delegate.getVariables().length - 1];
      System.arraycopy(delegate.getVariables(), 0, vars, 0, pos + 1);
      if (pos < vars.length - 1)
        System.arraycopy(delegate.getVariables(), pos + 2, vars, pos + 1, vars.length - pos - 1);

      variables = vars;
      message   = delegate.getMessage();
    }

    private int mapIndex(int idx) {
      return (idx > pos) ? idx + 1 : idx;
    }

    public void beforeFirst() throws AnswerException {
      delegate.beforeFirst();
    }

    public boolean next() throws AnswerException {
      return delegate.next();
    }

    public void close() {
      delegate.close();
    }

    public boolean isLiteral(int idx) throws AnswerException {
      return delegate.isLiteral(mapIndex(idx));
    }

    public String getLiteralDataType(int idx) throws AnswerException {
      return delegate.getLiteralDataType(mapIndex(idx));
    }

    public String getLiteralLangTag(int idx) throws AnswerException {
      return delegate.getLiteralLangTag(mapIndex(idx));
    }

    public String getString(int idx) throws AnswerException {
      return delegate.getString(mapIndex(idx));
    }

    public boolean isURI(int idx) throws AnswerException {
      return delegate.isURI(mapIndex(idx));
    }

    public URI getURI(int idx) throws AnswerException {
      return delegate.getURI(mapIndex(idx));
    }

    public boolean isBlankNode(int idx) throws AnswerException {
      return delegate.isBlankNode(mapIndex(idx));
    }

    public String getBlankNode(int idx) throws AnswerException {
      return delegate.getBlankNode(mapIndex(idx));
    }

    public boolean isSubQueryResults(int idx) throws AnswerException {
      return delegate.isSubQueryResults(mapIndex(idx));
    }

    public Answer getSubQueryResults(int idx) throws AnswerException {
      return delegate.getSubQueryResults(mapIndex(idx));
    }
  }
}
