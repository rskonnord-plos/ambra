package org.topazproject.otm.samples;

import org.topazproject.otm.annotations.Embeddable;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Model;
import org.topazproject.otm.annotations.Ns;
import org.topazproject.otm.annotations.Rdf;

@Ns(Rdf.topaz)
@Model("ri")
public class NoRdfType {
  @Id
  public String id;
  public String foo = "foo";
  public String bar = "bar";

  public NoRdfType() {
  }

  public NoRdfType(String id) {
    this.id = id;
  }
}
