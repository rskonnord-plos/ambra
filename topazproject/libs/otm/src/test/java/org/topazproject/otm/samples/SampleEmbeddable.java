package org.topazproject.otm.samples;

import org.topazproject.otm.annotations.Embeddable;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.annotations.Rdf;

@Embeddable
@UriPrefix(Rdf.topaz)
public class SampleEmbeddable {

  public String foo;
  public String bar;

}
