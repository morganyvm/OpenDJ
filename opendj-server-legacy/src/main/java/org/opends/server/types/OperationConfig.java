/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at legal-notices/CDDLv1_0.txt
 * or http://forgerock.org/license/CDDLv1.0.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at legal-notices/CDDLv1_0.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2008 Sun Microsystems, Inc.
 *      Portions Copyright 2015 ForgeRock AS.
 */

package org.opends.server.types;

/**
 * Base for data structures that define configuration
 * for operations.
 */
@org.opends.server.types.PublicAPI(
     stability=org.opends.server.types.StabilityLevel.VOLATILE,
     mayInstantiate=true,
     mayExtend=false,
     mayInvoke=true)
public abstract class OperationConfig {

  /**
   * When true indicates that the operation should stop as soon as
   * possible.
   */
  private boolean cancelled;

  /**
   * Indicates that this operation has been cancelled and the
   * operation if executing should finish as soon as possible.
   */
  public void cancel()
  {
    this.cancelled = true;
  }

  /**
   * Indicates whether or not this operation has been
   * cancelled.
   *
   * @return boolean where true indicates that this
   *         operation has been cancelled and if currently
   *         executing will finish as soon as possible
   */
  public boolean isCancelled()
  {
    return this.cancelled;
  }
}
