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
package org.topazproject.otm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark a Blob field. Only one field in an {@link Entity Entity}
 * may be marked as a Blob field and it must be a scalar field. The {@link Id @Id}
 * represents the id of the Blob and must be unique in the
 * {@link org.topazproject.otm.BlobStore BlobStore} where this Blob is persisted.
 * <p>
 * Blob fields cannot be used in a
 * {@link org.topazproject.otm.criterion.Restrictions Restrictions} or in OQL <code>where</code> clause
 * or in OQL projection lists including {@link Projection @Projection} in {@link View View}'s.
 * Use the {@link Entity Entity} containing this blob in those cases.
 *
 * @author Pradeep Krishnan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Blob {
}
