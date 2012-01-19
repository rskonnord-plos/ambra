package org.topazproject.otm.samples;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Embeddable;
import org.topazproject.otm.annotations.UriPrefix;

@Embeddable
@UriPrefix(Rdf.topaz)
public class SampleEmbeddable {

  public String foo;
  public String bar;

}
