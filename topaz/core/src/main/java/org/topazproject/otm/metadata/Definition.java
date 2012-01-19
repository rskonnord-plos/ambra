/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.otm.metadata;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;

/**
 * The base class for all meta definitions. Definitions all have a name that may optionally
 * contain a namespace portion followed by a ':' and a namespace specific portion, the local name.
 * Class names will usually have no namespace defined and are expected to be globally unique.
 * Property names however will usually have  a namespace to disambiguate.
 *
 * <p>Definitions in addition can reference another definition. The reference may indicate this is
 * an 'alias' or 're-use' of another definition or may indicate that this definition is 'based-on'
 * another definition. In either case, forward declarations of the referenced definition is
 * allowed and therefore the referenced definition may not exist at the time of this
 * definition.</p>
 *
 * @author Pradeep Krishnan
 */
public class Definition {
  private static enum State {UNRESOLVED, RESOLVING, RESOLVED;};

  private final String name;
  private final String specific;
  private final String reference;
  private final String supersedes;
  private State        state = State.UNRESOLVED;

  /**
   * Creates a new Definition object.
   *
   * @param name   The unique name of this definition.
   */
  public Definition(String name) {
    this(name, null, null);
  }

  /**
   * Creates a new Definition object.
   *
   * @param name   The unique name of this definition.
   * @param reference The definition to refer to resolve undefined attribiutes or null.
   * @param supersedes The definition that this supersedes or null.
   */
  public Definition(String name, String reference, String supersedes) {
    this.name        = name;
    this.reference   = reference;
    this.supersedes  = supersedes;

    specific         = name.substring(name.lastIndexOf(':') + 1);
  }

  /**
   * Gets the unique name of this definition.
   *
   * @return name as String.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the local name for this definition.
   *
   * @return the local name
   */
  public String getLocalName() {
    return specific;
  }

  /**
   * Gets the reference definition.
   *
   * @return the reference or null
   */
  public String getReference() {
    return reference;
  }

  /**
   * Gets the superseded definition.
   *
   * @return the definition this supersedes or null
   */
  public String getSupersedes() {
    return supersedes;
  }

  /**
   * Resolve the reference and update this definition. Sub-classes are not expected
   * to override this. Instead they should provide a {@link #resolve} method to
   * do any p
   *
   * @param sf the SessionFactory
   *
   * @throws OtmException on an error
   */
  public final void resolveReference(SessionFactory sf) throws OtmException {
    if (state == State.RESOLVED)
      return;

    if (state == State.RESOLVING)
      throw new OtmException("Circular reference detected while resolving '" + reference
                             + "' from '" + name + "'");

    try {
      state = State.RESOLVING;

      Definition ref;

      if (reference == null)
        ref = null;
      else {
        ref = sf.getDefinition(reference);

        if (ref == null)
          throw new OtmException("No definition in SessionFactory for reference '" + reference
                                 + "' defined in '" + name + "'");

        ref.resolveReference(sf);
      }

      resolve(sf, ref);
      state = State.RESOLVED;
    } finally {
      if (state == State.RESOLVING)
        state = State.UNRESOLVED;
    }
  }

  /**
   * Resolve this definition based on values defined in the reference. Note that this
   * will be called even when this definition does not refer to another. This is so that
   * sub-classes may use this to do any validation checks. Once this method is called
   * this definition is considered immutable.
   *
   * @param sf the SessionFactory
   * @param ref the reference definition or null if there is no reference for this definition.
   *
   * @throws OtmException on an error
   */
  protected void resolve(SessionFactory sf, Definition ref)
                  throws OtmException {
  }
}
