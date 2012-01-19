package org.topazproject.ambra.util;

/**
 * Utility class for operations involving basic beans that require knowledge of which classes are mapped as entities in
 * the ORM
 *
 * @author Alex Kudlick 9/27/11
 */
public interface EntityUtil {
  /**
   * Copy properties from a transient object to an already persisted object to be updated.  This is necessary if you
   * want to update an object in the database with all the properties from a transient instance.
   * <p/>
   * This works by calling all getters and setters on the objects to be copied. NOTES: <ul> <li>If the property is
   * mapped as a persistent object, then the properties are recursively copied over.</li> <li>Collections of
   * non-persistent objects are just replaced</li><li>Collections of persistent entities are updated by copying over
   * elements with matching ids.  Elements from the old collection that don't have a match in the new one will be
   * removed, and elements in the new collection that don't have a match in the old will be added.  Lists will be
   * reordered to have the same order as the new collection</li> <li>Arrays are copied over element by element</li>
   * <li>The id property is kept the same</li> <li>All other properties are simply replaced</li> </ul>
   * <p/>
   * See <a href="http://stackoverflow.com/questions/4779239/update-persistent-object-with-transient-object-using-hibernate">this
   * post on StackOverflow</a> for more information.
   *
   * @param from - the transient object with properties to be copied
   * @param to   - the persistent object to which to copy the properties
   * @throws Exception - Reflection exceptions
   */
  public void copyPropertiesFromTransientInstance(Object from, Object to) throws Exception;

}
