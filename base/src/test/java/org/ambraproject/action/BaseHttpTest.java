/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.action;

import org.ambraproject.util.Pair;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.commons.io.IOUtils;
import org.springframework.test.context.ContextConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base Test class for beans that make http requests.  Sets up an embedded http server for the beans to send requests
 * to, and provides a <a href="http://camel.apache.org/mock.html">mock endpoint</a> to set expectations about the http
 * requests the server will receive.  The easiest methods to use to use for this are {@link
 * org.apache.camel.component.mock.MockEndpoint#whenAnyExchangeReceived(org.apache.camel.Processor)} and {@link
 * org.apache.camel.component.mock.MockEndpoint#whenExchangeReceived(int, org.apache.camel.Processor)}, since you can
 * add any logic inside of the {@link org.apache.camel.Processor#process(org.apache.camel.Exchange)} method, including
 * assertions about the content received and behaviour for the response.
 * <p/>
 * To check the received content inside of the processor, use {@link org.apache.camel.Exchange#getIn()}, and to set the
 * response, use {@link org.apache.camel.Exchange#getOut()}. See {@link org.ambraproject.service.search.SolrHttpServiceTest} for
 * an example.
 * <p/>
 * If any of the {@link MockEndpoint} expectation methods are used, the test method will need to be annotated with
 * {@link org.springframework.test.annotation.DirtiesContext}
 *
 * @author Alex Kudlick 9/16/11
 */
@ContextConfiguration(locations = "http-test-context.xml")
public abstract class BaseHttpTest extends BaseTest {
  /**
   * The url of the http endpoint
   */
  protected static final String endpointUrl = "http://localhost:1234/select";
  /**
   * Endpoint that sits behind the embedded http server created.  Can be used to set the http response, and make
   * assertions about the received http message.
   */
  @EndpointInject(uri = "mock:end")
  protected MockEndpoint httpEndpoint;

  /**
   * String of valid solr xml response for testing
   */
  protected static final String testSolrXml;

  protected static final List<Pair<String, String>> articlesFromSolrXml;

  static {
    InputStream inputStream = null;
    StringWriter writer = new StringWriter();
    try {
      inputStream = BaseHttpTest.class.getClassLoader().getResourceAsStream("test-solr-response.xml");
      IOUtils.copy(inputStream, writer);
      testSolrXml = writer.toString();
    } catch (IOException e) {
      throw new Error("Error loading test solr xml (should be included in test resources)");
    } finally {
      if (inputStream != null) {
        IOUtils.closeQuietly(inputStream);
      }
      IOUtils.closeQuietly(writer);
    }

    List<Pair<String, String>> tempList = new ArrayList<Pair<String, String>>(11);
    tempList.add(new Pair<String, String>("10.1371/journal.pcbi.0010001", "Ab Initio Prediction of Transcription Factor Targets Using Structural Knowledge"));
    tempList.add(new Pair<String, String>("10.1371/journal.pcbi.0010002", "What Makes Ribosome-Mediated Transcriptional Attenuation Sensitive to Amino Acid Limitation?"));
    tempList.add(new Pair<String, String>("10.1371/journal.pcbi.0010003", "Predicting Functional Gene Links from Phylogenetic-Statistical Analyses of Whole Genomes"));
    tempList.add(new Pair<String, String>("10.1371/journal.pcbi.0010004", "<i>PLoS Computational Biology:</i> A New Community Journal"));
    tempList.add(new Pair<String, String>("10.1371/journal.pcbi.0010005", "An Open Forum for Computational Biology"));
    tempList.add(new Pair<String, String>("10.1371/journal.pcbi.0010006", "“Antedisciplinary” Science"));
    tempList.add(new Pair<String, String>("10.1371/journal.pcbi.0010007", "Susceptibility to Superhelically Driven DNA Duplex Destabilization: A Highly Conserved Property of Yeast Replication Origins"));
    tempList.add(new Pair<String, String>("10.1371/journal.pcbi.0010008", "Combinatorial Pattern Discovery Approach for the Folding Trajectory Analysis of a <i>β</i>-Hairpin"));
    tempList.add(new Pair<String, String>("10.1371/journal.pcbi.0010009", "Improving the Precision of the Structure–Function Relationship by Considering Phylogenetic Context"));
    tempList.add(new Pair<String, String>("10.1371/journal.pcbi.0010010", "Extraction of Transcript Diversity from Scientific Literature"));
    articlesFromSolrXml = Collections.unmodifiableList(tempList);
  }

}
