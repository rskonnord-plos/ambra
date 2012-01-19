package org.topazproject.otm.samples;

import java.net.URI;

import org.topazproject.otm.annotations.Rdf;

@Rdf(Annotia.NS + "Public")
public class PublicAnnotation extends Annotation {

  public PublicAnnotation() {
  }

  public PublicAnnotation(URI id) {
    super(id);
  }
}
