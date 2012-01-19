/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.interpreter

class AnswerTest extends GroovyTestCase {
  def sample1 = '''
<answer>
  <query>
    <variables>
      <article/>
      <k0/>
      <k1/>
    </variables>
    <solution>
      <article resource="info:doi/10.1371/journal.pone.0000005"/>
      <k0>
        <variables>
          <article/>
          <title/>
        </variables>
        <solution>
          <article resource="http://www.w3.org/1999/02/22-rdf-syntax-ns#bladebla"/>
          <title datatype="http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral">
            Concentration of the Most-Cited Papers in the Scientific Literature: Analysis of Journal Ecosystems
          </title>
        </solution>
      </k0>
      <k1 datatype="http://www.w3.org/2001/XMLSchema#double">
        2.0
      </k1>
    </solution>
    <solution>
      <article resource="info:doi/10.1371/journal.pone.0000000"/>
      <k0>
        <variables>
          <article/>
          <title/>
        </variables>
        <solution>
          <article resource="info:doi/10.1371/journal.pone.0000000"/>
          <title datatype="http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral">
            PLoS ONE Sandbox: A Place to Learn and Play
          </title>
        </solution>
      </k0>
      <k1 datatype="http://www.w3.org/2001/XMLSchema#double">
        93.0
      </k1>
    </solution>
  </query>
</answer>
'''
  def aliases = [ "xsd": "http://www.w3.org/2001/XMLShema#",
                  "rdf": "http://www.w3.org/1999/02/22-rdf-syntax-ns#" ]

  void testParsing() {
    def xml = new XmlSlurper().parseText(sample1)
    def ans = new Answer(xml.query[0])
    assert ans != null
    assert ans.getHeaders()[0] == "article"
    assert ans.getHeaders().size() == 3
    ans.flatten()
    assert ans.getHeaders()[2] == "title"
    ans.quote(Answer.createReduceClosure(aliases))
    assert ans[0][1].toString() == "rdf:bladebla"
    assert ans.getLengths()[2] == 99
    ans.quote([Answer.createTruncateClosure(30), Answer.rdfQuoteClosure])
    assert ans[0][2].toString() == "'Concentration of the Most-Ci...'"
//    println ans.getHeaders()
//    ans.data.each() { println it }
  }
}
