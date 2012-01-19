package org.topazproject.ambra.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.BaseTest;
import org.topazproject.ambra.article.service.ArticlePersistenceService;
import org.topazproject.ambra.article.service.NoSuchObjectIdException;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.ObjectInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Alex Kudlick 9/27/11
 */
public class EntityUtilTest extends BaseTest {

  @Autowired
  protected EntityUtil entityUtil;
  @Autowired
  protected ArticlePersistenceService articlePersistenceService; //just using this to retrieve articles from the db

  @DataProvider(name = "basicArticle")
  public Object[][] getBasicArticle() {
    Article savedArticle = new Article();
    URI id = URI.create("id:test-article-basic");
    savedArticle.setId(id);
    savedArticle.setArchiveName("test-archive-name");
    savedArticle.setState(0);
    savedArticle.setContextElement("fig");
    savedArticle.seteIssn("1234");
    dummyDataStore.store(savedArticle);

    Article transientArticle = new Article();
    transientArticle.setId(URI.create("id:will-get-overwritten"));
    transientArticle.setArchiveName("new archive name");
    transientArticle.setState(savedArticle.getState());
    transientArticle.setContextElement(savedArticle.getContextElement());
    transientArticle.seteIssn("new eIssn");

    return new Object[][]{
        {id, savedArticle, transientArticle}
    };
  }

  @Test(dataProvider = "basicArticle")
  public void testCopyBasicArticleProperties(URI originalId, Article savedInstance, Article transientInstance) throws Exception {
    savedInstance = articlePersistenceService.getArticle(savedInstance.getId(), DEFAULT_ADMIN_AUTHID);
    entityUtil.copyPropertiesFromTransientInstance(transientInstance, savedInstance);
    //check that the properites got copied
    //but not the id
    assertEquals(savedInstance.getId(), originalId, "article had id overwritten");
    assertEquals(savedInstance.getArchiveName(), transientInstance.getArchiveName(), "archive name didn't get copied over");
    assertEquals(savedInstance.getState(), transientInstance.getState(), "state didn't get copied over");
    assertEquals(savedInstance.getContextElement(), transientInstance.getContextElement(), "context element didn't get copied over");
    assertEquals(savedInstance.geteIssn(), transientInstance.geteIssn(), "eIssn didn't get copied over");

    //should be able to update article without a problem
    dummyDataStore.update(savedInstance);
    //check the properties on the article from the db
    savedInstance = articlePersistenceService.getArticle(savedInstance.getId(), DEFAULT_ADMIN_AUTHID);
    assertEquals(savedInstance.getId(), originalId, "article had id overwritten");
    assertEquals(savedInstance.getArchiveName(), transientInstance.getArchiveName(), "archive name didn't get copied over");
    assertEquals(savedInstance.getState(), transientInstance.getState(), "state didn't get copied over");
    assertEquals(savedInstance.getContextElement(), transientInstance.getContextElement(), "context element didn't get copied over");
    assertEquals(savedInstance.geteIssn(), transientInstance.geteIssn(), "eIssn didn't get copied over");

  }

  @DataProvider(name = "articleWithParts")
  public Object[][] getArticleWithParts() {
    //first article, don't add parts, just change properties on existing ones
    Article samePartsSaved = new Article();
    samePartsSaved.setId(URI.create("id:article-update-part"));
    List<ObjectInfo> onePartList = new ArrayList<ObjectInfo>(1);
    ObjectInfo part1 = new ObjectInfo();
    part1.setId(URI.create("id:test-part-1"));
    part1.setContextElement("fig");
    part1.seteIssn("1234");
    dummyDataStore.store(part1);
    onePartList.add(part1);
    samePartsSaved.setParts(onePartList);
    dummyDataStore.store(samePartsSaved);
    part1.setIsPartOf(samePartsSaved);
    dummyDataStore.update(part1);
    //copy of that article, but with some of the part's properties changed
    Article samePartsTransient = new Article();
    samePartsTransient.setId(samePartsSaved.getId());
    List<ObjectInfo> onePartTransientList = new ArrayList<ObjectInfo>(1);

    ObjectInfo part1Transient = new ObjectInfo();
    part1Transient.setId(part1.getId());
    part1Transient.setContextElement("new context element");
    part1Transient.seteIssn("new eIssn");

    onePartTransientList.add(part1Transient);
    samePartsTransient.setParts(onePartTransientList);

    Article noParts = new Article();
    noParts.setParts(new ArrayList<ObjectInfo>());

    //next, try adding parts to the list
    Article addPartsSaved = new Article();
    addPartsSaved.setId(URI.create("id:article-to-add-parts"));
    List<ObjectInfo> list = new ArrayList<ObjectInfo>(1);
    ObjectInfo part2 = new ObjectInfo();
    part2.setId(URI.create("id:test-part-2"));
    part2.seteIssn("12943");
    part2.setContextElement("fig");
    list.add(part2);
    dummyDataStore.store(part2);
    addPartsSaved.setParts(list);
    dummyDataStore.store(addPartsSaved);
    part2.setIsPartOf(addPartsSaved);
    dummyDataStore.update(part2);


    Article addPartsTransient = new Article();
    addPartsTransient.setId(addPartsSaved.getId());
    List<ObjectInfo> addedList = new ArrayList<ObjectInfo>();
    addedList.addAll(list);
    //add a new part to the list
    ObjectInfo part3 = new ObjectInfo();
    part3.setId(URI.create("id:test-part-3"));
    part3.seteIssn("12456");
    part3.setContextElement("blah");
    addedList.add(part3);

    addPartsTransient.setParts(addedList);

    Article reorderPartsSaved = new Article();
    reorderPartsSaved.setId(URI.create("id:article-to-reorder-parts"));
    List<ObjectInfo> partsToReorder = new ArrayList<ObjectInfo>(2);
    ObjectInfo part4 = new ObjectInfo();
    part4.setId(URI.create("id:test-part-4"));
    partsToReorder.add(part4);
    ObjectInfo part5 = new ObjectInfo();
    part5.setId(URI.create("id:test-part-5"));
    partsToReorder.add(part5);
    dummyDataStore.store(partsToReorder);
    reorderPartsSaved.setParts(partsToReorder);
    dummyDataStore.store(reorderPartsSaved);
    part4.setIsPartOf(reorderPartsSaved);
    dummyDataStore.update(part4);
    part5.setIsPartOf(reorderPartsSaved);
    dummyDataStore.update(part5);

    Article reorderPartsTransient = new Article();
    reorderPartsTransient.setId(URI.create("id:article-to-reorder-parts"));
    List<ObjectInfo> shuffledParts = new ArrayList<ObjectInfo>(2);
    shuffledParts.add(part5);
    shuffledParts.add(part4);
    reorderPartsTransient.setParts(shuffledParts);

    return new Object[][]{
        {samePartsSaved, samePartsTransient},   //just changes properties on a part
        {samePartsSaved, noParts},   //remove parts
        {addPartsSaved, addPartsTransient},   //add parts to the list
        {reorderPartsSaved, reorderPartsTransient}   //reorder the list
    };
  }


  @Test(dataProvider = "articleWithParts")
  public void testCopyArticleWithParts(Article savedArticle, Article transientArticle) throws Exception {
    savedArticle = articlePersistenceService.getArticle(savedArticle.getId(), DEFAULT_ADMIN_AUTHID);
    entityUtil.copyPropertiesFromTransientInstance(transientArticle, savedArticle);
    //check that the parts got copied over
    assertEquals(savedArticle.getParts().size(), transientArticle.getParts().size(), "didn't get correct number of parts copied over");
    for (int i = 0; i < savedArticle.getParts().size(); i++) {
      ObjectInfo actualPart = savedArticle.getParts().get(i);
      ObjectInfo expectedPart = transientArticle.getParts().get(i);
      assertEquals(actualPart.getId(), expectedPart.getId(), "incorrect order of parts");
      assertEquals(actualPart.getContextElement(), expectedPart.getContextElement(), "context element for part " + i + "didn't get copied over");
      assertEquals(actualPart.geteIssn(), expectedPart.geteIssn(), "eIssn for part " + i + "didn't get copied over");
    }

    //should be able to update now
    for (ObjectInfo part : savedArticle.getParts()) {
      try {
        //save/update as appropriate
        articlePersistenceService.getObjectInfo(part.getId().toString(), DEFAULT_ADMIN_AUTHID);
        dummyDataStore.update(part.getDublinCore());
        dummyDataStore.update(part);
      } catch (NoSuchObjectIdException e) {
        dummyDataStore.store(part.getDublinCore());
        dummyDataStore.store(part);
      }
    }
    dummyDataStore.update(savedArticle);
    //check the properties in the db
    savedArticle = articlePersistenceService.getArticle(savedArticle.getId(), DEFAULT_ADMIN_AUTHID);
    assertEquals(savedArticle.getParts().size(), transientArticle.getParts().size(), "didn't get correct number of parts copied over");
    for (int i = 0; i < savedArticle.getParts().size(); i++) {
      ObjectInfo actualPart = savedArticle.getParts().get(i);
      ObjectInfo expectedPart = transientArticle.getParts().get(i);
      assertEquals(actualPart.getId(), expectedPart.getId(), "incorrect order of parts");
      assertEquals(actualPart.getContextElement(), expectedPart.getContextElement(), "context element for part " + i + "didn't get copied over");
      assertEquals(actualPart.geteIssn(), expectedPart.geteIssn(), "eIssn for part " + i + "didn't get copied over");
    }

  }
}
