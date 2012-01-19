package org.topazproject.otm.samples;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.UriPrefix;

@UriPrefix(Rdf.topaz)
@Entity(model="ri")
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
