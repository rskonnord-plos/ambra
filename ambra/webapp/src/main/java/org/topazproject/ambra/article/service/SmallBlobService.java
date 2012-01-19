/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
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
package org.topazproject.ambra.article.service;

import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.cache.AbstractObjectListener;

import org.topazproject.otm.Session;
import org.topazproject.otm.ClassMetadata;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to provide small blobs from cache or from blob store. This has been added to
 * improve the render performance where the article contains a lot of small images.
 *
 * @author Dragisa Krsmanovic
 */
public interface SmallBlobService {

  public byte[] getSmallBlob(final Representation representation) throws Exception;

}
