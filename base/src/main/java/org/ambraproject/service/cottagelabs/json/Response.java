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
package org.ambraproject.service.cottagelabs.json;

import com.google.gson.annotations.Expose;
import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("com.googlecode.jsonschema2pojo")
public class Response {
  @Expose
  private Integer requested;
  @Expose
  private List<Error> errors = new ArrayList<Error>();
  @Expose
  private List<Result> results = new ArrayList<Result>();
  @Expose
  private List<Processing> processing = new ArrayList<Processing>();

  public Integer getRequested() {
    return requested;
  }

  public void setRequested(Integer requested) {
    this.requested = requested;
  }

  public List<Error> getErrors() {
    return errors;
  }

  public void setErrors(List<Error> errors) {
    this.errors = errors;
  }

  public List<Result> getResults() {
    return results;
  }

  public void setResults(List<Result> results) {
    this.results = results;
  }

  public List<Processing> getProcessing() {
    return processing;
  }

  public void setProcessing(List<Processing> processing) {
    this.processing = processing;
  }
}
