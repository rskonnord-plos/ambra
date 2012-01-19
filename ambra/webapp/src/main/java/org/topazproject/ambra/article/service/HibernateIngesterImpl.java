/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
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

import org.hibernate.HibernateException;
import org.plos.filestore.FSIDMapper;
import org.plos.filestore.FileStoreException;
import org.plos.filestore.FileStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.admin.service.DocumentManagementService;
import org.topazproject.ambra.admin.service.SyndicationService;
import org.topazproject.ambra.article.ArchiveProcessException;
import org.topazproject.ambra.models.*;
import org.topazproject.ambra.service.HibernateServiceImpl;
import org.topazproject.ambra.util.HibernateEntityUtil;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Alex Kudlick
 */
public class HibernateIngesterImpl extends HibernateServiceImpl implements Ingester {
  private static final Logger log = LoggerFactory.getLogger(HibernateIngesterImpl.class);

  private FileStoreService fileStoreService;
  private SyndicationService syndicationService;
  private IngestArchiveProcessor ingestArchiveProcessor;
  private DocumentManagementService documentManagementService;
  private HibernateEntityUtil entityUtil;

  /**
   * Set the IngestArchiveProcessor to use to create an Article object from the XML
   *
   * @param ingestArchiveProcessor - the xml processor to use
   */
  @Required
  public void setIngestArchiveProcessor(IngestArchiveProcessor ingestArchiveProcessor) {
    this.ingestArchiveProcessor = ingestArchiveProcessor;
  }

  /**
   * Set the documentManagementService, used to remove files from the filesystem on reingest
   *
   * @param documentManagementService the document management service to use
   */
  @Required
  public void setDocumentManagementService(DocumentManagementService documentManagementService) {
    this.documentManagementService = documentManagementService;
  }

  /**
   * Set the {@link FileStoreService} to use to store files
   *
   * @param fileStoreService - the filestore to use
   */
  @Required
  public void setFileStoreService(FileStoreService fileStoreService) {
    this.fileStoreService = fileStoreService;
  }

  /**
   * Set the Syndication Service to use in creating syndications for ingested articles
   *
   * @param syndicationService - the {@link SyndicationService} to set
   */
  @Required
  public void setSyndicationService(SyndicationService syndicationService) {
    this.syndicationService = syndicationService;
  }

  /**
   * Set the Utility used to copy properties from the transient article instances retrieved from the xml.
   *
   * @param entityUtil - the utility to use
   */
  @Required
  public void setEntityUtil(HibernateEntityUtil entityUtil) {
    this.entityUtil = entityUtil;
  }


  /**
   * TODO: Rollback from the filestore if there's a problem storing the files
   *
   * @param archive - the archive to ingest
   * @param force   if true then don't check whether this article already exists but just save this new article.
   * @return the new article
   * @throws DuplicateArticleIdException if an article exists with the same URI as the new article and <var>force</var>
   *                                     is false
   * @throws IngestException             if there's any other problem ingesting the article
   */
  @Transactional(rollbackFor = Throwable.class)
  public Article ingest(ZipFile archive, boolean force)
      throws DuplicateArticleIdException, IngestException {
    Article article = null;
    try {
      final Document articleXml = ingestArchiveProcessor.extractArticleXml(archive);
      article = ingestArchiveProcessor.processArticle(archive, articleXml);

      //Check if we already have an article
      Article existingArticle = (Article) hibernateTemplate.get(Article.class, article.getId());
      // if the article is in Disabled state, we allow ingest without force
      if (!force && existingArticle != null && existingArticle.getState() != Article.STATE_DISABLED) {
        throw new DuplicateArticleIdException(article.getId().toString());
      }

      //Note that we can't use saveOrUpdate() here because what we actually have is a transient instance with the
      //properties we want to save. So if there's already an article for this id, we need to load it up, copy
      // the properties, and update.  Else we can just save the new article
      if (existingArticle != null) {
        log.debug("ReIngesting (force ingest) article: {}", existingArticle.getId());
        updateArticle(article, existingArticle);
      } else {
        log.debug("Saving article information for article: {}",article.getId());
        saveArticle(article);
      }

      // For every RelatedArticle object, create a reciprocal link from old Article to this Article.
      addReciprocalRelatedArticleAssociations(article);
      //create syndications
      syndicationService.createSyndications(article.getId().toString());
      //generate the crossref info doc

      //Store files to the file store
      storeFiles(archive, article.getId());

      return article;
    } catch (ArchiveProcessException e) {
      throw new IngestException("Error processing zip archive to extract article information; archive" + archive.getName(), e);
    } catch (URISyntaxException e) {
      throw new IngestException("Error creating syndications for article " + article.getId(), e);
    } catch (IOException e) {
      throw new IngestException("Error reading entries from zip archive: " + archive.getName(), e);
    } catch (FileStoreException e) {
      throw new IngestException("Error storing blobs to the file store; article: "
          + article.getId() + ", archive: " + archive.getName(), e);
    } catch (DataAccessException e) {
      throw new IngestException("Error storing information for article " + article.getId() + " to the SQL database", e);
    } catch (HibernateException e) {
      throw new IngestException("Error storing information for article " + article.getId() + " to the SQL database", e);
    }
  }

  private void saveArticle(Article article) {
    article.getDublinCore().getBibliographicCitation().getAuthors().clear();
    article.getDublinCore().getBibliographicCitation().getEditors().clear();
    //Just to make sure we aren't saving referenced user profiles
    for (Citation reference : article.getDublinCore().getReferences()) {
      reference.getAuthors().clear();
      reference.getEditors().clear();
    }

    //ensure we don't get constraint violations when saving bi-directional associations
    //(e.g. parts reference back to article, which hasn't been saved yet)
    for (ObjectInfo part : article.getParts()) {
      for (Representation representation : part.getRepresentations()) {
        representation.setObject(null);
      }
      part.setIsPartOf(null);
      hibernateTemplate.save(part.getDublinCore());
      hibernateTemplate.save(part);
    }
    for (Representation representation : article.getRepresentations()) {
      representation.setObject(null);
    }

    //save and cascade
    hibernateTemplate.save(article.getDublinCore());
    hibernateTemplate.save(article);

    //Go back and set isPartOf on the parts
    for (ObjectInfo part : article.getParts()) {
      for (Representation representation : part.getRepresentations()) {
        representation.setObject(part);
        hibernateTemplate.update(representation);
      }
      part.setIsPartOf(article);
      hibernateTemplate.update(part);
    }
    for (Representation representation : article.getRepresentations()) {
      representation.setObject(article);
      hibernateTemplate.update(representation);
    }
  }

  private void updateArticle(Article newArticle, Article oldArticle) throws IngestException {
    newArticle.getDublinCore().getBibliographicCitation().getAuthors().clear();
    newArticle.getDublinCore().getBibliographicCitation().getEditors().clear();

    //Recursively copy over all the new properties
    try {
      entityUtil.copyPropertiesFromTransientInstance(newArticle, oldArticle);
    } catch (Exception e) {
      throw new IngestException("Error copying properties for article " + oldArticle.getId(), e);
    }

    //ensure we don't get constraint violations when saving bi-directional associations
    //(e.g. parts reference back to article, which hasn't been saved yet)
    for (ObjectInfo part : oldArticle.getParts()) {
      for (Representation representation : part.getRepresentations()) {
        representation.setObject(null);
        if (representation.getId() != null && hibernateTemplate.get(Representation.class, representation.getId()) != null) {
          hibernateTemplate.update(representation);
        } else {
          hibernateTemplate.save(representation);
        }
      }
      part.setIsPartOf(null);
      if (part.getId() != null && hibernateTemplate.get(ObjectInfo.class, part.getId()) != null) {
        hibernateTemplate.update(part.getDublinCore());
        hibernateTemplate.update(part);
      } else {
        hibernateTemplate.save(part.getDublinCore());
        hibernateTemplate.save(part);
      }
    }
    for (Representation representation : oldArticle.getRepresentations()) {
      representation.setObject(null);
    }

    //update and cascade
    hibernateTemplate.update(oldArticle.getDublinCore());
    hibernateTemplate.update(oldArticle);

    //Go back and set isPartOf on the parts
    for (ObjectInfo part : oldArticle.getParts()) {
      for (Representation representation : part.getRepresentations()) {
        representation.setObject(part);
        hibernateTemplate.update(representation);
      }
      part.setIsPartOf(oldArticle);
      hibernateTemplate.update(part);
    }
    for (Representation representation : oldArticle.getRepresentations()) {
      representation.setObject(oldArticle);
      hibernateTemplate.save(representation);
    }
  }

  /**
   * Process files from the archive and store them to the {@link FileStoreService}
   *
   * @param archive   - the archive being ingested
   * @param articleId - the id for the article
   * @throws java.io.IOException - if there's a problem reading from the zip file
   * @throws org.plos.filestore.FileStoreException
   *                             - if there's a problem writing files to the file store
   */
  private void storeFiles(final ZipFile archive, URI articleId)
      throws IOException, FileStoreException {
    log.info("Removing existing files (if any) for {}", articleId);

    try {
      documentManagementService.removeFromFileSystem(articleId.toString());
    } catch (Exception e) {
      throw new FileStoreException("Error removing existing files from the file store", e);
    }

    log.info("Storing files from archive {} to the file store", archive.getName());
    Enumeration<? extends ZipEntry> entries = archive.entries();

    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      if (!entry.getName().equalsIgnoreCase("manifest.dtd")) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
          inputStream = archive.getInputStream(entry);
          outputStream = fileStoreService.getFileOutStream(
              FSIDMapper.zipToFSID(articleId.toString(), entry.getName()), entry.getSize());
          fileStoreService.copy(inputStream, outputStream);
        } finally {
          if (inputStream != null) {
            try {
              inputStream.close();
            } catch (IOException e) {
              log.warn("Error closing input stream while writing files", e);
            }
          }
          if (outputStream != null) {
            try {
              outputStream.close();
            } catch (IOException e) {
              log.warn("Error closing output stream while writing files", e);
            }
          }
        }
      }
    }
    log.info("Finished storing files from archive {}", archive.getName());
  }

  /**
   * When ingesting article B, if article B lists article A as a "related article", then create a RelatedArticle object
   * indicating that article B is related to article A. Associate this new RelatedArticle object to article A and
   * Session.saveOrUpdate() article A so that the new relationship will be saved.
   * <p/>
   * If article A does not yet exist, then do not create a new RelatedArticle object.
   *
   * @param newArticle The Article which is being ingested (Article B in the method description)
   */
  private void addReciprocalRelatedArticleAssociations(Article newArticle) {
    if (newArticle.getRelatedArticles() == null || newArticle.getRelatedArticles().size() < 1) {
      log.debug("The article " + newArticle.getId().toString() + " does not have any Related Articles.");
      return;
    }

    for (RelatedArticle newRelatedArticle : newArticle.getRelatedArticles()) {
      URI oldArticleUri = newRelatedArticle.getArticle();

      // If no old article, then a new RelatedArticle object makes no sense.
      Article oldArticle = (Article) hibernateTemplate.get(Article.class, oldArticleUri);
      if (oldArticle == null) {
        continue;
      }

      boolean isCreateNewRelatedArticle = true;
      if (oldArticle.getRelatedArticles() != null && oldArticle.getRelatedArticles().size() > 0) {
        for (RelatedArticle oldRelatedArticle : oldArticle.getRelatedArticles()) {
          if (oldRelatedArticle.getArticle().equals(newArticle.getId())) {
            isCreateNewRelatedArticle = false;
            break;
          }
        }
      }
      if (isCreateNewRelatedArticle) {
        RelatedArticle reciprocalLink = new RelatedArticle(); // Id set when object is written to DB
        reciprocalLink.setArticle(newArticle.getId());
        reciprocalLink.setRelationType(newRelatedArticle.getRelationType());

        if (oldArticle.getRelatedArticles() == null) {
          oldArticle.setRelatedArticles(new HashSet<RelatedArticle>());
        }
        oldArticle.getRelatedArticles().add(reciprocalLink);
        hibernateTemplate.saveOrUpdate(oldArticle); // Add the new RelatedArticle object to "oldArticle".

        log.debug("Just created the RelatedArticle: " + reciprocalLink.toString());
      }
    }
  }
}
