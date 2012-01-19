/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.mapping;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.util.HashMap;
import java.util.Map;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.metadata.EmbeddedDefinition;

/**
 * A convenient base class for all mappers.
 *
 * @author Pradeep krishnan
 */
public class EmbeddedMapperImpl extends AbstractMapper implements EmbeddedMapper {
  private final ClassMetadata      embedded;
  private final EmbeddedDefinition def;

  /**
   * Creates a new EmbeddedMapperImpl object.
   *
   * @param def     the property definition
   * @param binders the binders
   * @param embedded the embedded class metadata
   */
  public EmbeddedMapperImpl(EmbeddedDefinition def, Map<EntityMode, Binder> binders,
                            ClassMetadata embedded) {
    super(binders);
    this.embedded   = embedded;
    this.def        = def;
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata getEmbeddedClass() {
    return embedded;
  }

  /*
   * inherited javadoc
   */
  public Mapper promote(Mapper m) throws OtmException {
    Map<EntityMode, Binder> fb = new HashMap<EntityMode, Binder>();

    for (EntityMode mode : getBinders().keySet()) {
      EmbeddedBinder binder = (EmbeddedBinder) getBinder(mode);
      fb.put(mode, binder.promote(m.getBinder(mode)));
    }

    try {
      return buildProxy(m, getName() + "." + m.getName(), fb);
    } catch (Exception e) {
      throw new OtmException("Failed to create a proxy", e);
    }
  }

  private static Mapper buildProxy(final Mapper m, final String name,
                                   final Map<EntityMode, Binder> fb)
                            throws Exception {
    // ensure these methods exist
    m.getClass().getMethod("getName");
    m.getClass().getMethod("getBinders");
    m.getClass().getMethod("getBinder", EntityMode.class);
    m.getClass().getMethod("getBinder", Session.class);

    InvocationHandler handler =
      new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args)
                      throws Throwable {
          String mn = method.getName();

          if ("getName".equals(mn))
            return name;

          if ("getBinder".equals(mn)) {
            if (args[0] instanceof EntityMode)
              return fb.get((EntityMode) args[0]);

            if (args[0] instanceof Session)
              return fb.get(((Session) args[0]).getEntityMode());
          }

          if ("getBinders".equals(mn))
            return fb;

          return method.invoke(m, args);
        }
      };

    return (Mapper) Proxy.newProxyInstance(m.getClass().getClassLoader(),
                                           m.getClass().getInterfaces(), handler);
  }

  /*
   * inherited javadoc
   */
  public EmbeddedDefinition getDefinition() {
    return def;
  }
}
