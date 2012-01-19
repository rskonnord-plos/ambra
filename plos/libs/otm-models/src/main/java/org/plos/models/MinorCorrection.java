package org.plos.models;

import org.topazproject.otm.annotations.Entity;

@Entity(type = MinorCorrection.RDF_TYPE)
public class MinorCorrection extends Correction  implements ArticleAnnotation {
  public static final String RDF_TYPE = Annotea.TOPAZ_TYPE_NS + "MinorCorrection";
  public String getType() {
    return RDF_TYPE;
  }
}
