/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
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

package org.topazproject.otm.query;

import java.net.URI;
import java.util.List;

import antlr.ASTFactory;
import antlr.RecognitionException;
import antlr.collections.AST;

import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.SessionFactory;

/**
 * A collection of utilities related to query parsing and handling.
 *
 * @author Ronald Tschalär
 */
public class ASTUtil implements ConstraintsTokenTypes {
  /** Not meant to be instantiated */
  private ASTUtil() {
  }

  /**
   * Create an ID object.
   *
   * @param obj the object from which to create an id; may be a String or an OqlAST. If this is a
   *            String then that is used as the text of a new OqlAST instance; otherwise the object
   *            is dup'd and the type changed to ID.
   * @param af  the ast-factory to use
   * @return the new AST object of type ID
   */
  public static OqlAST makeID(Object obj, ASTFactory af) {
    if (obj instanceof OqlAST) {
      OqlAST ast = (OqlAST) af.dup((OqlAST) obj);
      if (ast.getType() != ID)
        ast.setType(ID);
      return ast;
    }

    return (OqlAST) af.create(ID, (String) obj);
  }

  /**
   * Create an ID object that's a variable.
   *
   * @param obj the object from which to create an id; see {@link #makeID}
   * @param af  the ast-factory to use
   * @return the new AST object of type ID and marked as a variable
   */
  public static OqlAST makeVar(Object obj, ASTFactory af) {
    OqlAST ast = makeID(obj, af);
    ast.setIsVar(true);
    return ast;
  }

  /**
   * Create a new triple object. The graph used is that attached to the predicate, if the predicate
   * is an OqlAST. The arguments are dup'd if they are OqlAST's.
   *
   * @param s  the subject; make be a String or OqlAST
   * @param p  the predicate; make be a String or OqlAST
   * @param o  the object; make be a String or OqlAST
   * @param af the ast-factory to use
   * @return the new triple object
   */
  public static OqlAST makeTriple(Object s, Object p, Object o, ASTFactory af) {
    return makeTriple(s, p, o, (p instanceof OqlAST) ? ((OqlAST) p).getGraph() : null, af);
  }

  /**
   * Create a new triple object. The arguments are dup'd if they are OqlAST's.
   *
   * @param s  the subject; make be a String or OqlAST
   * @param p  the predicate; make be a String or OqlAST
   * @param o  the object; make be a String or OqlAST
   * @param g  the graph; make be a String or OqlAST or null
   * @param af the ast-factory to use
   * @return the new triple object
   */
  public static OqlAST makeTriple(Object s, Object p, Object o, Object g, ASTFactory af) {
    OqlAST sa = makeID(s, af);
    OqlAST pa = makeID(p, af);
    OqlAST oa = makeID(o, af);
    OqlAST ga = (g != null) ? makeID(g, af) : null;

    AST tr = af.create(TRIPLE, "triple");
    if (pa.isInverse())
      return (OqlAST) af.make(new AST[] { tr, oa, pa, sa, ga });
    else
      return (OqlAST) af.make(new AST[] { tr, sa, pa, oa, ga });
  }

  /**
   * Look up the uri of the given graph.
   *
   * @param graphId the id of the graph (as specified in the {@link GraphConfig})
   * @param sf      the current session-factory
   * @return the graph's uri
   * @throws RecognitionException if no graph with the given name has been configured
   */
  public static String getGraphUri(String graphId, SessionFactory sf) throws RecognitionException {
    GraphConfig gc = sf.getGraph(graphId);
    if (gc == null)
      throw new RecognitionException("Unable to find graph '" + graphId + "'");
    return gc.getUri().toString();
  }

  /**
   * Find a graph of the given type. If there are multiple graphs of the given type, then an
   * arbitrary one is selected.
   *
   * @param graphType the graph's type
   * @param sf        the current session-factory
   * @return the graph's uri
   * @throws RecognitionException if no graph with the given type has been configured
   */
  public static String getGraphUri(URI graphType, SessionFactory sf) throws RecognitionException {
    List<GraphConfig> gc = sf.getGraphs(graphType);
    if (gc == null)
      throw new RecognitionException("Unable to find a graph of type '" + graphType +
                                     "' - please make sure you've created and configured a graph" +
                                     " for this type");
    return gc.get(0).getUri().toString();
  }

  /**
   * Create an AST tree from the given parts.
   *
   * @param af       the ast-factory to use
   * @param rootType the AST type of the root AST
   * @param rootName the text of the root AST
   * @param children the list of children to add; note that these children are not cloned, but
   *                 instead are added as is. Null children are ignored
   * @return the created AST
   */
  public static OqlAST makeTree(ASTFactory af, int rootType, String rootName, AST... children) {
    AST[] nodes = new AST[children.length + 1];
    nodes[0] = af.create(rootType, rootName);
    System.arraycopy(children, 0, nodes, 1, children.length);
    return (OqlAST) af.make(nodes);
  }

  /**
   * Finds the definition of given alias.
   *
   * @param alias the name of the alias
   * @param top   the top of the AST tree (where clause)
   * @return the alias definition (the ASGN node), or null of not found
   */
  public static OqlAST findAliasDef(String alias, OqlAST top) {
    return findAliasDef(alias, top, top);
  }

  private static OqlAST findAliasDef(String alias, OqlAST cur, OqlAST top) {
    if (cur.getType() == ASGN) {
      if (alias.equals(cur.getFirstChild().getText()))
        return cur;
    } else {
      for (OqlAST n = (OqlAST) cur.getFirstChild(); n != null; n = (OqlAST) n.getNextSibling()) {
        OqlAST res = findAliasDef(alias, n, top);
        if (res != null)
          return res;
      }
    }

    return null;
  }

  /**
   * Get the last dereference in a sequence. I.e. in the expression 'a.b.c' return 'c'. This will
   * resolve aliases, so if you have 'a := b and b := c.d' and invoke this on 'a' then you'll get
   * back 'd'.
   *
   * @param deref the dereference sequence; this node is expected to be an ID node, and the
   *              dereferences are siblings
   * @param top   the top of the AST tree (where clause)
   * @return the node of the last dereference, or null if <var>deref</var> is never dereferenced
   */
  public static OqlAST getLastDeref(OqlAST deref, OqlAST top) {
    if (deref.getNextSibling() == null && deref.getType() == ID) {      // presumably an alias
      OqlAST def = findAliasDef(deref.getText(), top);
      if (def == null)
        return null;

      def = (OqlAST) def.getFirstChild().getNextSibling();
      if (def.getType() == REF)
        return getLastDeref((OqlAST) def.getFirstChild(), top);
      else
        return null;
    } else {
      while (deref.getNextSibling() != null)
        deref = (OqlAST) deref.getNextSibling();

      if (deref.getType() == REF)
        return getLastDeref((OqlAST) deref.getFirstChild(), top);
      else
        return deref;
    }
  }
}
