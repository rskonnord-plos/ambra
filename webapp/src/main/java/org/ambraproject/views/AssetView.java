/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.views;
/**
 * Information regarding article assets contained in the file system such as asset size.
 * *
 */
public class AssetView {

  private final String doi;
  private final long size;
  private final String extension;

  public AssetView(String doi, long size, String extension) {
    this.doi = doi;
    this.size = size;
    this.extension = extension;
  }

  public String getExtension() {
    return extension;
  }

  public String getDoi() {
    return doi;
  }

  public long getSize() {
    return size;
  }

  @Override
  public String toString() {
    return "AssetView{" +
        "doi='" + doi + '\'' +
        ", size=" + size +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AssetView assetView = (AssetView) o;

    if (size != assetView.size) return false;
    return !(doi != null ? !doi.equals(assetView.doi) : assetView.doi != null);

  }

  @Override
  public int hashCode() {
    long result = doi != null ? doi.hashCode() : 0;
    result = 31 * result + size;
    return (int) result;
  }
}