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

@Generated("com.googlecode.jsonschema2pojo")
public class Processing {
  @Expose
  private Identifier identifier;

  public Identifier getIdentifier() {
    return identifier;
  }

  public void setIdentifer(Identifier identifier) {
    this.identifier = identifier;
  }
}
