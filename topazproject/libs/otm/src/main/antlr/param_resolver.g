/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

header
{
/*
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.query;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.otm.mapping.Serializer;
import org.topazproject.otm.query.Results;

import antlr.RecognitionException;
import antlr.collections.AST;
}

/**
 * This is an AST transformer for OQL that replaces parameter references with their values.
 *
 * @author Ronald Tschalär 
 */
class ParameterResolver extends TreeParser("OqlTreeParser");

options {
    importVocab = Query;
    buildAST    = true;
}

{
    private void resolveParams(AST node, Map<String, Object> paramValues)
        throws RecognitionException {
      if (node.getType() == PARAM) {
        resolveParamNode(node, paramValues);
      } else {
        for (AST n = node.getFirstChild(); n != null; n = n.getNextSibling())
          resolveParams(n, paramValues);
      }
    }

    private void resolveParamNode(AST node, Map<String, Object> paramValues)
        throws RecognitionException {
      String name = node.getFirstChild().getText();
      if (!paramValues.containsKey(name))
        throw new RecognitionException("No value found for parameter '" + name + "'");

      ExprType type = ((OqlAST) node).getExprType();
      Object   val = paramValues.get(name);

      if (val instanceof URI) {
        if (type != null && type.getType() != ExprType.Type.URI)
          reportWarning("type mismatch in parameter '" + name + "': parsed type is '" +
                        type + "' but parameter value is a URI");
        makeUriref(node, (URI) val);
      } else if (val instanceof Results.Literal) {
        Results.Literal lit = (Results.Literal) val;
        if (type != null) {
          if (lit.getDatatype() == null && type.getType() != ExprType.Type.UNTYPED_LIT)
            reportWarning("type mismatch in parameter '" + name + "': parsed type is '" +
                          type + "' but parameter value is a plain literal");

          if (lit.getDatatype() != null) {
            if (type.getType() != ExprType.Type.TYPED_LIT)
              reportWarning("type mismatch in parameter '" + name + "': parsed type is '" +
                            type + "' but parameter value is a typed literal");
            else if (!expandAliases(lit.getDatatype()).equals(type.getDataType()))
              reportWarning("type mismatch in parameter '" + name + "': parsed type is '" +
                            type + "' but parameter value is a typed literal with datatype '" +
                            expandAliases(lit.getDatatype()) + "'");
          }
        }

        makeLiteral(node, lit.getValue(), lit.getDatatype(), lit.getLanguage());
      } else {
        if (type == null)
          throw new RecognitionException("The parsed type for parameter '" + name +
                                         "' is unknown and the value given (" + val +
                                         ") is neither a URI nor a literal");

        try {
          Serializer s = ((OqlAST) node).getSerializer();
          String txt = (s != null) ? s.serialize(val) : val.toString();

          if (type.getType() == ExprType.Type.URI)
            makeUriref(node, new URI(txt));
          else if (type.getType() == ExprType.Type.UNTYPED_LIT)
            makeLiteral(node, txt, null, null);
          else if (type.getType() == ExprType.Type.TYPED_LIT)
            makeLiteral(node, txt, new URI(type.getDataType()), null);
          else
            makeUriref(node, new URI(txt));
        } catch (Exception e) {
          throw (RecognitionException)
              new RecognitionException("Error serializing the value for parameter '" + name +
                                       "': " + val).initCause(e);
        }
      }
    }

    private void makeUriref(AST node, URI val) {
      node.setType(URIREF);
      node.setFirstChild(null);
      node.setText("<" + expandAliases(val) + ">");
    }

    private void makeLiteral(AST node, String val, URI dtype, String lang) {
      node.setType(QSTRING);
      node.setFirstChild(null);
      node.setText("'" + ItqlHelper.escapeLiteral(val) + "'");

      if (dtype != null) {
        AST t1 = astFactory.create(DHAT);
        AST t2 = astFactory.create(URIREF, "<" + expandAliases(dtype) + ">");
        t2.setNextSibling(node.getNextSibling());
        t1.setNextSibling(t2);
        node.setNextSibling(t1);
      } else if (lang != null) {
        AST t1 = astFactory.create(AT);
        AST t2 = astFactory.create(ID, lang);
        t2.setNextSibling(node.getNextSibling());
        t1.setNextSibling(t2);
        node.setNextSibling(t1);
      }
    }

    private String expandAliases(URI uri) {
      String u = uri.toString();
      // TODO: should use aliases in SessionFactory
      for (String alias : (Set<String>) ItqlHelper.getDefaultAliases().keySet())
        u = u.replaceFirst("^" + alias + ":", (String) ItqlHelper.getDefaultAliases().get(alias));
      return u;
    }
}

query[Map<String, Object> paramValues]
    : ! { #query = astFactory.dupList(_t); resolveParams(#query, paramValues); }
    ;

