package org.topazproject.ambra.solr;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.BaseAmbraTestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Alex Kudlick Date: Mar 1, 2011
 * <p/>
 * org.topazproject.ambra.solr
 */
public class SolrFieldConversionTest extends BaseAmbraTestCase{

  private static final Map<Integer, String> viewCountingFields = new HashMap<Integer, String>();
  private static final String testAllTimeField = "all_time_field";
  private SolrFieldConversion solrFieldConverter;

  static {
    viewCountingFields.put(14, "two_week_field");
    viewCountingFields.put(30, "one_month_field");
  }

  @BeforeClass
  public void setUpConverter(){
    solrFieldConverter = new SolrFieldConversionImpl();
    ((SolrFieldConversionImpl)solrFieldConverter).setAllTimeViewsField(testAllTimeField);
    ((SolrFieldConversionImpl)solrFieldConverter).setViewCountingFields(viewCountingFields);
  }

  @DataProvider(name = "viewCountingFields")
  public Object[][] viewCountingFields() {
    return new Object[][] {
        {Integer.valueOf(14),"two_week_field"},
        {Integer.valueOf(10),"two_week_field"},
        {Integer.valueOf(17),"two_week_field"},
        {Integer.valueOf(30),"one_month_field"},
        {Integer.valueOf(28),"one_month_field"},
        {Integer.valueOf(40),"one_month_field"},
    };
  }

  @Test(dataProvider = "viewCountingFields")
  public void testViewCountingConversion(Integer numDays,String expectedField){
    assertEquals(expectedField,solrFieldConverter.getViewCountingFieldName(numDays));
  }

}
