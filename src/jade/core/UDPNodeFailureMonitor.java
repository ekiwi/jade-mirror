/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.core;

//#MIDP_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.util.Logger;
import jade.util.leap.Collection;


/**
 * The <code>UDPNodeFailureMonitor</code> class detects node failures and
 * notifies its registered listener using the UDP protocol.
 * 
 * @author Roland Mungenast - Profactor
 * @see jade.core.NodeFailureMonitor
 */
public class UDPNodeFailureMonitor extends NodeFailureMonitor {

  /**
   * The target is connected. That means that it is sending
   * ping messages regulary.
   */
  public static final int CONNECTED = 0;
  
  /**
   * The target is unreachable. The target doesn't
   * send ping messages any more.
   */
  public static final int UNREACHABLE = 1;
  
  /**
   * The final state. The target isn't
   * monitored anymore.
   */
  public static final int FINAL = 2;
  

  private long deadline = -1;
  private long lastPing = -1;

  private int state = -1;
  private UDPMonitorServer server;
  private Logger logger = Logger.getMyLogger(this.getClass().getName());
  
  
  /**
   * Constructor
   * @param n target node to monitor
   * @param nel listener to inform about new events
   */
  public void init(Profile p, Node n, NodeEventListener nel) {
    super.init(p, n, nel);
    setState(CONNECTED);
    server = UDPMonitorServer.getInstance(p);
  }

  public void start() {
      server.register(this);
  }

  public void stop() {
    server.deregister(this);
  }
  
  /**
   * Returns all child nodes of the targeted node
   * @return a <code>Collection</code> of <code>Node</code> instances
   */
  public Collection getChildNodes() {
    return childNodes;
  }
  
  /**
   * Returns the time when the last ping message has been received 
   * from the targeted node
   * 
   * @return the difference, measured in milliseconds, 
   * between the current time and midnight, January 1, 1970 UTC.
   */
  public long getLastPing() {
    return lastPing;
  }
  
  /**
   * Returns the deadline until which the monitor will 
   * wait for a new ping message.
   * 
   * @return the difference, measured in milliseconds, 
   * between the current time and midnight, January 1, 1970 UTC.
   */
  public long getDeadline() {
    return deadline;
  }
  
  /**
   * Returns the current state.
   */
  public int getState() {
    return state;
  }
  
  /**
   * Sets the current state.
   */
  void setState(int newState) {
    
    if (logger.isLoggable(Logger.FINEST)) {
      logger.log(Logger.FINEST, "Transition to state " + newState + 
          " for node '" + target.getName() + "'");
    }
    
    // --> CONNECTED
    if (state == -1 && newState == CONNECTED) {
      listener.nodeAdded(target);
    
    // CONNECTED --> UNREACHABLE
    } else if (newState == UNREACHABLE) {
      listener.nodeUnreachable(target);
    
    // UNREACHABLE --> CONNECTED
    } else if (state == UNREACHABLE && newState == CONNECTED) {
      listener.nodeReachable(target);
    
    // REMOVED
    } else if (newState == FINAL) {
      listener.nodeRemoved(target);
      server.deregister(this);
    }
    
    state = newState;
  }
  
  /**
   * Sets the time when the last ping message has been received 
   * from the targeted node
   * 
   * @param time the difference, measured in milliseconds, 
   * between the current time and midnight, January 1, 1970 UTC.
   */
  void setLastPing(long time) {
    lastPing = time;
  }
  
  /**
   * Sets the time until which the monitor will 
   * wait for a new ping message.
   * 
   * @param time the difference, measured in milliseconds, 
   * between the current time and midnight, January 1, 1970 UTC.
   */
  void setDeadline(long time) {
    deadline = time;
  }
  
  int failedPings = 0;
  
  void setFailedPings(int value) {
   failedPings = value; 
  }
  
  int getFailedPings() {
   return failedPings; 
  }
  

}
