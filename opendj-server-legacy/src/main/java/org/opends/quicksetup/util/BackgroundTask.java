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
 *      Copyright 2006-2010 Sun Microsystems, Inc.
 *      Portions Copyright 2015 ForgeRock AS.
 */
package org.opends.quicksetup.util;



/**
 * This class provides a mechanism for running a task in the background using a
 * separate thread and providing the caller with notification when it has
 * completed.
 * @param <T> type of object returned by this process
 */
public abstract class BackgroundTask<T>
{
  /**
   * Creates a new thread and begins running the task in the background.  When
   * the task has completed, the {@code backgroundTaskCompleted} method will be
   * invoked.
   */
  public final void startBackgroundTask()
  {
    BackgroundTaskThread<T> taskThread = new BackgroundTaskThread<>(this);
    taskThread.start();
  }



  /**
   * Performs all processing associated with the task.
   *
   * @return  An {@code Object} with information about the processing performed
   *          for this task, or {@code null} if no return value is needed.
   *
   * @throws Throwable throwable that will be passed through the method
   *          backgroundTaskCompleted.
   */
  public abstract T processBackgroundTask() throws Throwable;



  /**
   * This method will be invoked to indicate that the background task has
   * completed.  If processing completed successfully, then the
   * {@code Throwable} argument will be {@code null} and the {@code returnValue}
   * argument will contain the value returned by the
   * {@code processBackgroundTask} method.  If an exception or error was thrown,
   * then the {@code throwable} argument will not be {@code null}.
   *
   * @param  returnValue  The value returned by the
   *                      {@code processBackgroundTask} method when processing
   *                      completed, or {@code null} if no value was returned or
   *                      an exception was encountered during processing.
   * @param  throwable    A {@code Throwable} instance (e.g., an exception) that
   *                      was raised during processing, or {@code null} if all
   *                      processing completed successfully.
   */
  public abstract void backgroundTaskCompleted(T returnValue,
                                               Throwable throwable);
}
