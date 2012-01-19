package org.topazproject.ambra.solr;

/**
 * Service for converting to names of fields that are meaningful to solr
 * <p/>
 * <p/>
 * User: Alex Kudlick Date: Mar 1, 2011
 * <p/>
 * org.topazproject.ambra.solr
 */
public interface SolrFieldConversion {

  /**
   * Get the name of the field in solr that counts views for articles over the number of days given.  If none matches,
   * the method should return the field that counts over the closest number of days
   *
   * @param numDays - The number of days over which to count views
   * @return - the field name in solr
   */
  public String getViewCountingFieldName(int numDays);

  /**
   *
   * @return - the solr field that counts views over all time
   */
  public String getAllTimeViewsField();
}
