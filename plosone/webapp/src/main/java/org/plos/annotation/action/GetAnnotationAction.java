/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.action;

/**
 * Get annotation for a given id.
 *
 * We can't use BaseGetAnnotationAction directly as spring runs into this problem
 * org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with
 * name 'org.plos.annotation.action.AnnotationActionsTest': Unsatisfied dependency expressed through
 * bean property 'getAnnotationAction': There are 2 beans of type
 * [class org.plos.annotation.action.GetAnnotationAction] for autowire by type. There should have
 * been exactly 1 to be able to autowire property 'getAnnotationAction' of bean 'org.plos.annotation.action.AnnotationActionsTest'.
 * Consider using autowire by name instead.
 */
public class GetAnnotationAction extends BaseGetAnnotationAction {
}
