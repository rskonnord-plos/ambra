package org.topazproject.otm.samples;

import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

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
