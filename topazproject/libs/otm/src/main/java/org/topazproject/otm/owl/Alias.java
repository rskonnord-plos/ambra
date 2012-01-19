/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.owl;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.annotations.Id;

/**
 * Represents the way we store aliases in #metadata
 *
 * @author Eric Brown
 */
@Entity(model="metadata")
public class Alias {
  @Id
  private URI prefix;
  @Predicate(uri=Rdf.topaz + "hasAlias")
  private List<String> aliases = new ArrayList<String>();

  /**
   * Get the prefix of the alias
   *
   * @return the prefix of the alias
   */
  public URI getPrefix() {
    return prefix;
  }

  /**
   * Set the prefix of the alias
   *
   * @param prefix that defines this alias
   */
  public void setPrefix(URI prefix) {
    this.prefix = prefix;
  }

  /**
   * List of possible aliases
   *
   * @return list of possible aliases
   */
  public List<String> getAliases() {
    return aliases;
  }

  /**
   * Set list of aliases
   *
   * @param aliases a list of aliases
   */
  public void setAliases(List<String> aliases) {
    this.aliases = aliases;
  }

  /**
   * Add an alias for this prefix
   *
   * @param alias is the alias to add
   */
  public void addAlias(String alias) {
    aliases.add(alias);
  }
}
