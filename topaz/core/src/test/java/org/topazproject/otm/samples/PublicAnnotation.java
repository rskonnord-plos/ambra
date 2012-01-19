package org.topazproject.otm.samples;

import java.net.URI;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

@Entity(type=Annotea.NS + "Public")
public class PublicAnnotation extends Annotation {
  @Predicate(uri = Rdf.topaz + "hasNote")
  public String note;

  public PublicAnnotation() {
  }

  public PublicAnnotation(URI id) {
    super(id);
  }
}
