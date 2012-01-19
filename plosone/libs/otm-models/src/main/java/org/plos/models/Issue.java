/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import java.net.URI;

import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.criterion.DetachedCriteria;

/**
 * A marker class to mark an Aggregation as an issue. 
 *
 * @author Pradeep Krishnan
 */
@Entity(type = PLoS.plos + "Issue", model = "ri")
public class Issue extends Aggregation {

}
