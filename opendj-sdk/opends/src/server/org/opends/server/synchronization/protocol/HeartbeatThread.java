/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Portions Copyright 2007 Sun Microsystems, Inc.
 */

package org.opends.server.synchronization.protocol;

import org.opends.server.api.DirectoryThread;
import static org.opends.server.loggers.debug.DebugLogger.debugEnabled;
import static org.opends.server.loggers.debug.DebugLogger.debugVerbose;
import static org.opends.server.loggers.debug.DebugLogger.debugInfo;

import java.io.IOException;

/**
 * This thread publishes a heartbeat message on a given protocol session at
 * regular intervals when there are no other synchronization messages being
 * published.
 */
public class HeartbeatThread extends DirectoryThread
{

  /**
   * For test purposes only to simulate loss of heartbeats.
   */
  static private boolean heartbeatsDisabled = false;

  /**
   * The session on which heartbeats are to be sent.
   */
  private ProtocolSession session;


  /**
   * The time in milliseconds between heartbeats.
   */
  private long heartbeatInterval;


  /**
   * Set this to stop the thread.
   */
  private boolean shutdown = false;


  /**
   * Create a heartbeat thread.
   * @param threadName The name of the heartbeat thread.
   * @param session The session on which heartbeats are to be sent.
   * @param heartbeatInterval The desired interval between heartbeats in
   * milliseconds.
   */
  public HeartbeatThread(String threadName, ProtocolSession session,
                  long heartbeatInterval)
  {
    super(threadName);
    this.session = session;
    this.heartbeatInterval = heartbeatInterval;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run()
  {
    try
    {
      if (debugEnabled())
      {
        debugInfo("Heartbeat thread is starting, interval is %d",
                  heartbeatInterval);
      }
      HeartbeatMessage heartbeatMessage = new HeartbeatMessage();

      while (!shutdown)
      {
        long now = System.currentTimeMillis();
        if (debugEnabled())
        {
          debugVerbose("Heartbeat thread awoke at %d, last message was sent " +
              "at %d", now, session.getLastPublishTime());
        }

        if (now > session.getLastPublishTime() + heartbeatInterval)
        {
          if (!heartbeatsDisabled)
          {
            if (debugEnabled())
            {
              debugVerbose("Heartbeat sent at %d", now);
            }
            session.publish(heartbeatMessage);
          }
        }

        try
        {
          long sleepTime = session.getLastPublishTime() +
              heartbeatInterval - now;
          if (sleepTime <= 0)
          {
            sleepTime = heartbeatInterval;
          }

          if (debugEnabled())
          {
            debugVerbose("Heartbeat thread sleeping for %d", sleepTime);
          }
          Thread.sleep(sleepTime);
        }
        catch (InterruptedException e)
        {
          // Keep looping.
        }
      }
    }
    catch (IOException e)
    {
      if (debugEnabled())
      {
        debugInfo("Heartbeat thread could not send a heartbeat.");
      }
      // This will be caught in another thread.
    }
    finally
    {
      if (debugEnabled())
      {
        debugInfo("Heartbeat thread is exiting.");
      }
    }
  }


  /**
   * Call this method to stop the thread.
   */
  public void shutdown()
  {
    shutdown = true;
  }


  /**
   * For testing purposes only to simulate loss of heartbeats.
   * @param heartbeatsDisabled Set true to prevent heartbeats from being sent.
   */
  public static void setHeartbeatsDisabled(boolean heartbeatsDisabled)
  {
    HeartbeatThread.heartbeatsDisabled = heartbeatsDisabled;
  }
}
