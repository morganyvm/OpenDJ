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
 *      Portions Copyright 2014-2015 ForgeRock AS
 */
package org.opends.server.admin.condition;



import java.util.Arrays;
import java.util.List;

import org.opends.server.admin.AbstractManagedObjectDefinition;
import org.opends.server.admin.client.AuthorizationException;
import org.opends.server.admin.client.CommunicationException;
import org.opends.server.admin.client.ManagedObject;
import org.opends.server.admin.client.ManagementContext;
import org.opends.server.admin.server.ServerManagedObject;
import org.forgerock.opendj.config.server.ConfigException;
import org.forgerock.util.Reject;



/**
 * A condition which evaluates to <code>true</code> if and only if
 * all of its sub-conditions are <code>true</code>.
 */
public final class ANDCondition implements Condition {

  /** The list of sub-conditions. */
  private final List<Condition> conditions;



  /**
   * Creates a new logical AND condition with the provided
   * sub-conditions.
   *
   * @param conditions
   *          The sub-conditions which will be combined using a
   *          logical AND.
   */
  public ANDCondition(Condition... conditions) {
    Reject.ifNull(conditions);
    this.conditions = Arrays.asList(conditions);
  }



  /** {@inheritDoc} */
  public boolean evaluate(ManagementContext context,
      ManagedObject<?> managedObject) throws AuthorizationException,
      CommunicationException {
    for (Condition condition : conditions) {
      if (!condition.evaluate(context, managedObject)) {
        return false;
      }
    }
    return true;
  }



  /** {@inheritDoc} */
  public boolean evaluate(ServerManagedObject<?> managedObject)
      throws ConfigException {
    for (Condition condition : conditions) {
      if (!condition.evaluate(managedObject)) {
        return false;
      }
    }
    return true;
  }



  /** {@inheritDoc} */
  public void initialize(AbstractManagedObjectDefinition<?, ?> d)
      throws Exception {
    for (Condition condition : conditions) {
      condition.initialize(d);
    }
  }

}
