package org.plos.models;

import org.topazproject.otm.annotations.Entity;

@Entity(type = FormalCorrection.RDF_TYPE)
public class FormalCorrection extends Correction implements ArticleAnnotation {
  public static final String RDF_TYPE = Annotea.TOPAZ_TYPE_NS + "FormalCorrection";
  public String getType() {
    return RDF_TYPE;
  }
}
