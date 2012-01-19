/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.interpreter;

/** Base class for itql/rdf types below */
class Value {
  /** The raw value from mulgara */
  String value
  /** The quoted value if quote() was called */
  String quotedValue

  String toString() { return quotedValue ? quotedValue : value }
  int size() { return toString().size() }

  /** 
   * Use the suplied closure(s) to quote our value
   *
   * @param f a closer or list of closures that are passed ourself and must return
   *          the quoted value as a string.
   */
  void quote(f) {
    if (f instanceof Closure) {
      quotedValue = f(this)
    } else {
      quotedValue = null
      f.each() { quotedValue = it(this) }
    }
  }
}

class Literal  extends Value   { Literal(value)  { this.value = value } }
class RdfDate  extends Value   { RdfDate(value)  { this.value = value } } // TODO: extend Literal
class RdfInt   extends Value   { RdfInt(value)   { this.value = value } } // TODO: extend Literal
class Resource extends Value   { Resource(value) { this.value = value } }
class Blank    extends Value   { Blank(value)    { this.value = value } }
class Empty    extends Value   { Empty()         { this.value = ""    } }

/**
 * Represents one row of an itql answer. Some columns may also contain rows and
 * so on if there were subqueries. The flatten() and quote() methods modify 
 * instance data.
 */
class Row {
  def vars
  def hdrs = [ ]
  def vals = [ ]
  Row(sol, vars) {
    // TODO: Handle now vars (like count())
    this.vars = vars
    vars.each() { hdrs.add(it) }
    vars.each() { var ->
      def val = sol."$var"."@resource".toString()
      if (val) {
        val = new Resource(val)
      } else {
        val = sol."$var"."@blank-node".toString()
        if (val) {
          val = new Blank(val)
        } else if (sol."$var".children().size() == 0) {
          val = sol."$var".toString().trim()
          if (val) {
            // TODO: Capture more data-types
            switch (sol."$var"."@datatype") {
              case "http://www.w3.org/2001/XMLSchema#int":  val = new RdfInt(val); break
              case "http://www.w3.org/2001/XMLSchema#date": val = new RdfDate(val); break
              default: val = new Literal(val)
            }
          } else {
            val = new Empty() // If a value does not exist in a sub-query
          }
        } else if (!val) {
          // TODO: Handle a subquery that returns multiple subrows per row
          def subvars = sol."$var".variables.children().list()*.name()
          val = new Row(sol."$var".solution, subvars)
        }
      }
      vals.add(val)
    }
  }

  /** Flatten any subqueries into a simple row */
  void flatten() {
    // Assume all rows have the same type
    int col = 0
    def newvals = [ ]
    def newhdrs = [ ]
    vals.each() { val ->
      if (val instanceof Row) {
        val.flatten()
        newvals += val.vals
        newhdrs += val.hdrs
      } else {
        newvals.add(val)
        newhdrs.add(hdrs[col])
      }
      col++
    }
    vals = newvals
    hdrs = newhdrs
  }

  // Duck typing: Make Row function as if it was an array of columns
  void quote(f)      { vals.each() { it.quote(f) } }
  def getLengths()   { return vals*.size() }
  String toString()  { return vals.toString() }
  def getAt(int pos) { return vals[pos] }
  def iterator()     { return vals.iterator() }
  def size()         { return vals.size() }
}

/**
 * Represent an answer from itql
 */
class Answer {
  def vars
  def data = [ ]

  /**
   * Construct an Answer
   *
   * @param ans should be results from XmlSlurper. i.e.
   *            new XmlSlurper().parseText(itql.doQuery(...)).query[0]
   */
  Answer(ans) {
    vars = ans.variables.children().list()*.name()
    ans.solution.each() { data.add(new Row(it, vars)) }
  }

  /** flatten any subquery results into main query */
  void flatten() {
    data.each() { row ->
      row.flatten()
    }
  }

  def getHeaders() {
    if (data)
      return data[0].hdrs
  }

  /** 
   * quote data with suplied closure(s)
   *
   * @param f a closer or list of closures that are passed ourself and must return
   *          the quoted value as a string.
   */
  def quote(f) {
    data.each() { it.quote(f) }
  }

  /** Return the maximum lengths of all columns */
  def getLengths() {
    def lengths = getHeaders()*.size()
    data.each() { row ->
      def col = 0
      row.getLengths().each() { length ->
        lengths[col] = [ lengths[col++], length ].max()
      }
    }
    return lengths
  }

  // Duck typing helpers ... make us look like our data
  def getAt(int pos) { return data[pos] }
  def iterator()     { return data.iterator() }

  // Closure helpers for quote()

  static def csvQuoteClosure = { v ->
    println "Quoting: ${v.getClass().getName()}: $v"
    switch (v) {
      case Literal: return '"' + v.toString().replace('"', '""') + '"'
      case Resource: return "<$v>"
      default: return v.toString()
    }
  }

  static def rdfQuoteClosure = { v ->
    switch (v) {
      case RdfDate: return v.toString(); break
      case RdfInt:  return v.toString(); break
      case Literal: return "'" + v.toString().replace("'", "\\'") + "'"; break
      case Resource: return "<$v>"; break
      default: return v.toString()
    }
  }

  static def createReduceClosure(aliases) {
    return { v ->
      if (!(v instanceof Resource)) return v.toString()
      for (alias in aliases) {
        if (v.value == alias.value) return v.toString()
        def val = v.toString().replace(alias.value, alias.key + ":")
        if (val != v.toString()) return val
      }
      return v.toString()
    }
  }

  static def createTruncateClosure(int length) {
    return { v ->
      if (!(v instanceof Literal)) return v.toString()
      if (v.toString().size() > length) {
        return v.toString()[0..(length-3)] + "..."
      } else {
        return v.toString()
      }
    }
  }
}
