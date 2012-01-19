package org.topazproject.otm.samples;

import org.topazproject.otm.annotations.Embeddable;
import org.topazproject.otm.annotations.Ns;
import org.topazproject.otm.annotations.Rdf;

@Embeddable
@Ns(Rdf.topaz)
public class SampleEmbeddable {

  public String foo;
  public String bar;

}
