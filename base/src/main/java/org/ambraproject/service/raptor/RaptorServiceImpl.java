/*
 * Copyright (c) 2006-2013 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.ambraproject.service.raptor;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVEntryParser;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;
import org.ambraproject.views.AcademicEditorView;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class RaptorServiceImpl implements RaptorService {
  private static final Logger log = LoggerFactory.getLogger(RaptorServiceImpl.class);

  private String serviceUrl;
  private HttpClient httpClient;

  public List<AcademicEditorView> getAcademicEditor() throws IOException {
    String getRequest = serviceUrl + "/?q=services&report=editor_page&db=em,ap&Download=raw";

    GetMethod get = new GetMethod(getRequest);
    int response = httpClient.executeMethod(get);

    if(response == 200) {
      return parseResults(get.getResponseBodyAsStream());
    } else {
      log.error("Request: {} Returned status {}", getRequest, response);
      return null;
    }
  }

  //A parser class for the csv engine
  private class AcademicEditorParser implements CSVEntryParser<AcademicEditorView> {
    public AcademicEditorView parseEntry(String[] line) {

      if(line.length != 8) {
        throw new RuntimeException("Bad response received from raptor, wrong number of columns: " + line.length);
      } else {
        String[] subjects = line[5].split(";");

        //Make sure subjects are unique
        Hashtable<String, String> subjectHash = new Hashtable<String, String>();
        for(String subject : subjects) {
          subjectHash.put(subject, "");
        }

      return AcademicEditorView.builder()
        .setId(line[0])
        .setName(line[1])
        .setLastName(line[2])
        .setInstitute(line[3])
        .setCountry(line[4])
        .setSubjects(new ArrayList<String>(subjectHash.keySet()))
        .setType(line[6])
        .setJournalKey(line[7])
        .build();
      }
    }
  }

  private List<AcademicEditorView> parseResults(InputStream is) throws IOException {
    List<org.ambraproject.views.AcademicEditorView> results = new ArrayList<org.ambraproject.views.AcademicEditorView>();

    CSVStrategy strategy = new CSVStrategy('\t', '\"', '#', true, true);
    CSVReader<AcademicEditorView> csvParser = new CSVReaderBuilder(new InputStreamReader(is))
      .strategy(strategy)
      .entryParser(new AcademicEditorParser())
      .build();

    return csvParser.readAll();
  }

  @Required
  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  @Required
  public void setHttpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }
}
