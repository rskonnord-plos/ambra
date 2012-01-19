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

import java.util.HashSet;
import java.util.Set;
}

/**
 * This is the parser for the OQL (object-query-language).
 *
 * @author Ronald Tschalär
 */
class QueryParser extends Parser("OqlParser");

options {
    exportVocab = Query;
    buildAST    = true;
    k           = 2;    // for a.b.*
}

tokens {
    SELECT = "select";
    FROM   = "from";
    WHERE  = "where";
    PROJ   = "projection";
    AND    = "and";
    OR     = "or";
    ORDER  = "order";
    BY     = "by";
    LIMIT  = "limit";
    OFFSET = "offset";
    REF    = "ref";
    FUNC   = "func";
    PARAM  = "param";
    EXPR   = "expr";
    SUBQ   = "subquery";
    CAST   = "cast";
    ASC    = "asc";
    DESC   = "desc";
}

{
    private final Set<String> paramNames = new HashSet<String>();

    public Set<String> getParameterNames() {
      return paramNames;
    }
}

query
{ astFactory.setASTNodeClass(OqlAST.class); }
    :   select SEMI!
    ;

select !
    :   SELECT s:sclause FROM f:fclause (WHERE w:wclause)? (o:oclause)? (l:lclause)? (t:tclause)? {
          // reorder select, from, and where clauses
          #select =
            #(SELECT, #(FROM, f), #([WHERE,"where"], w), #([PROJ,"projection"], s), o, l, t);
        }
    ;


sclause // select clause
    :   pitem (COMMA^ pitem)*
    ;

pitem
    :   e:pexpr! (var)? {
          astFactory.addASTChild(currentAST, #e);       // switch var and expr arround
        }
    ;

pexpr   // projection expression
    :   (pfname LPAREN) => pfunc
    |   fldexpr
    |   subquery
    ;

fldexpr // field expression
    :   (var (DOT (field|predicate))* (DOT STAR)) => var (DOT! (field|predicate))* (DOT! STAR) { #fldexpr = #([REF,"ref"], fldexpr); }
    |   var (DOT! (field|predicate))* { #fldexpr = #([REF,"ref"], fldexpr); }
    ;

subquery        // subquery
    :   LPAREN! select RPAREN! { #subquery = #([SUBQ,"subquery"], subquery); }
    ;

pfunc   // projection function
    :   pfname LPAREN! (pfarg (COMMA! pfarg)*)? RPAREN! { #pfunc = #([FUNC,"function"], pfunc); }
    ;

pfname  // projection function name
    :   ID (COLON ID)?
    ;

pfarg   // projection function argument
    :   (pfname LPAREN) => pfunc
    |   fldexpr
    |   constant
    ;


fclause // from clause
    :   oclass var (COMMA^ oclass var)*
    ;

oclass  // object class
{ StringBuilder cls = new StringBuilder(); }
    :   ID          { cls.append(#ID.getText()); }
        (DOT! e:ID! { cls.append('.').append(#e.getText()); } )*
                    { #oclass.setText(cls.toString()); }
    ;


wclause // where clause
    :   oexpr
    ;

oexpr
    :   ( aexpr OR ) => aexpr (OR! aexpr)+ { #oexpr = #([OR,"or"], oexpr); }
    |   aexpr
    ;

aexpr
    :   ( expr AND ) => expr (AND! expr)+ { #aexpr = #([AND,"and"], aexpr); }
    |   expr
    ;

expr
    :   cexpr
    |   LPAREN! oexpr RPAREN!
    ;

cexpr
    :   (var ASGN)         => var ASGN^ (selector|constant)     // alias
    |   (selector (EQ|NE)) => selector (EQ^|NE^) (selector|constant)  // equality comparisons
    |                         constant (EQ^|NE^) selector       // equality comparisons, part 2
    |                         cfunc                             // boolean functions
    ;

selector
    :   (CAST LPAREN)   => cast (DOT! (field|predicate|dexpr))* { #selector = #([REF,"ref"], selector); }
    |   (cfname LPAREN) => cfunc
    |   var (DOT! (field|predicate|dexpr))* { #selector = #([REF,"ref"], selector); }
    ;

cast
    :   CAST^ LPAREN! selector COMMA! oclass RPAREN!
    ;

cfunc   // condition function
    :   cfname LPAREN! (cfarg (COMMA! cfarg)*)? RPAREN! { #cfunc = #([FUNC,"function"], cfunc); }
    ;

cfname  // condition function name
    :   ID (COLON ID)?
    ;

cfarg   // condition function argument
    :   selector
    |   constant
    ;

dexpr   // predicate expression
    :   LBRACE! var PTR! (oexpr)? RBRACE! { #dexpr = #([EXPR,"expr"], dexpr); }
    ;


oclause
    :   ORDER^ BY! oitem (COMMA! oitem)*
    ;

oitem
    :   var (ASC|DESC)?
    ;

lclause
    :   LIMIT^ NUM
    ;

tclause
    :   OFFSET^ NUM
    ;


var
    :   ID
    ;

field
    :   ID
    ;

predicate
    :   URIREF
    ;

constant
    :   QSTRING ((DHAT URIREF) | (AT ID))?
    |   URIREF
    |   parameter
    ;

parameter
    :   ! COLON! n:ID  { paramNames.add(#n.getText()); #parameter = #([PARAM, "parameter"], n); }
    ;


/**
 * This is the lexer for the OQL (object-query-language).
 *
 * @author Ronald Tschalär
 */
class QueryLexer extends Lexer;

options {
    k = 2;      // needed for newline junk
    charVocabulary = '\u0000'..'\uFFFE'; // allow unicode
}

LPAREN : '(' ;
RPAREN : ')' ;
LBRACE : '{' ;
RBRACE : '}' ;
COMMA  : ',' ;
DOT    : '.' ;
COLON  : ':' ;
SEMI   : ';' ;
STAR   : '*' ;
DHAT   : "^^" ;
AT     : '@' ;
EQ     : '=' ;
NE     : "!=" ;
ASGN   : ":=" ;
PTR    : "->" ;
NUM    : ('-')? ('0'..'9')+ ;
ID     : ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;
QSTRING: '\'' ('\\' . | ~'\'')* '\'' ;
URIREF : '<' (~'>')+ '>' ;
WS     : ( ' '
         | '\r' '\n' { newline(); }
         | '\r' { newline(); }
         | '\n' { newline(); }
         | '\t'
         )
         { $setType(Token.SKIP); }
       ;

