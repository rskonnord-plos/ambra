/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark a Blob field. It may only be applied to byte array fields.
 * Only one field in an {@link Entity Entity} may be marked as a Blob field and 
 * it must be a scalar field. The {@link Id @Id} represents the id of the Blob
 * and must be unique in the {@link org.topazproject.otm.BlobStore BlobStore} where
 * this Blob is persisted.
 * <p>
 * Blob fields cannot be used in a 
 * {@link org.topazproject.otm.criterion.Restrictions Restrictions} or in OQL where clause 
 * or in OQL projection lists including {@link Projection @Projection} in {@link View View}'s.
 * Use the {@link Entity Entity} containing this blob in those cases.
 * <p>
 * When an entity contains a Blob, the rest of the fields may be thought of as "meta-data"
 * for the Blob. However with this usage pattern it is not possible to load the meta-data
 * alone and load the blobs lazily. Therefore wherever possible it is recommended that
 * blobs be given a separate id and stored in a separate entity with just the @Id and @Blob 
 * annotated fields and the new entity defined as an association.
 *
 * @author Pradeep Krishnan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Blob {
}
