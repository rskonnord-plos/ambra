
package org.topazproject.otm.samples;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.UriPrefix;

@UriPrefix(Rdf.topaz)
@Entity(model="ri")
public class ClassWithEnum {
  @Id
  public String id;
  public enum Foo {bar1, bar2}
  public Foo foo;

  public ClassWithEnum() {
  }

  public ClassWithEnum(String id) {
    this.id = id;
  }
}
