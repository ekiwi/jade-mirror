/*
  $Log$
  Revision 1.5  1999/06/16 00:19:11  rimassa
  Added a root() method to retrieve the root behaviour (i.e. the one
  directly scheduled by the agent) for a given behaviour object.

  Revision 1.4  1999/06/15 14:32:25  rimassa
  Added support for timeouts in block() and restart() methods.

  Revision 1.3  1999/06/10 14:18:16  rimassa
  Corrected two wrong cross-references in Javadoc comments.

  Revision 1.2  1999/06/09 16:13:57  rimassa
  Added a block() method with timeout.

  Revision 1.1  1999/05/20 13:43:16  rimassa
  Moved all behaviour classes in their own subpackage.

  Revision 1.8  1999/04/06 00:09:35  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.7  1999/02/16 08:07:07  rimassa
  Removed a fixed FIXME.

  Revision 1.6  1998/10/30 18:18:48  rimassa
  Added an abstract reset() method to restore a Behaviour's initial
  state.

  Revision 1.5  1998/10/04 18:01:02  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core.behaviours;

import jade.core.Agent;

/**
   Abstract base class for <b><em>JADE</em></b> behaviours.  Extending
   this class directly should only be needed for particular behaviours
   with special synchronization needs; this is because event based
   notification used for blocking and restarting behaviours is
   directly accessible at this level.

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
public abstract class Behaviour {

  /**
     A constant for child-to-parent notifications.
   */
  protected final int NOTIFY_UP = -1;

  /**
     A constant for parent-to-child notifications.
   */
  protected final int NOTIFY_DOWN = 1;

  /**
     Event class for notifying blocked and restarted behaviours.
     This class is used to notify interested behaviours when a
     Behaviour changes its runnable state. It may be sent to
     behaviour's parent (<em>upward notification</em> or to behaviour's
     children (<em>downward notification</em>).
  */
  protected class RunnableChangedEvent {
    private boolean runnable;
    private int direction;

    /**
       Re-init event content. This method can be used to rewrite an
       existing event with new data (much cheaper than making a new
       object).
       @param b A <code>boolean</code> flag; when <code>false</code>
       it means that a behaviour passed from <em>Ready</em> to
       <em>Blocked</em> state. When <code>true</code> it means that a
       behaviour passed from <em>Blocked</em> to <em>Ready</em> (this
       flag is the truth value of the predicate <em><b>'The behaviour
       has now become runnable'</b></em>.
       @param d A notification direction: when direction is
       <code>NOTIFY_UP</code>, the event travels upwards the behaviour
       containment hierarchy; when it is <code>NOTIFY_DOWN</code>, the
       event travels downwards.
    */
    public void init(boolean b, int d) {
      runnable = b;
      direction = d;
    }


    /**
       Read event source.
       @return The <code>Behaviour</code> object which generated this event.
    */
    public Behaviour getSource() {
      return Behaviour.this;
    }

    /**
      Check whether the event is runnable.
      @return <code>true</code> when the behaviour generating this
      event has become <em>Ready</em>, <code>false</code> when it has
      become <em>Blocked</em>.  */
    public boolean isRunnable() {
      return runnable;
    }

    /**
       Check which direction this event is travelling.
       @return <code>true</code> when the event is a notification
       going from a child behaviour to its parent; <code>false</code>
       otherwise.
    */
    public boolean isUpwards() {
      return direction == NOTIFY_UP;
    }

  } // End of RunnableChangedEvent class


  private boolean runnableState = true;

  /**
     The agent this behaviour belongs to.
   */
  protected Agent myAgent;

  /**
     This event object will be re-used for every state change
     notification.
   */
  protected RunnableChangedEvent myEvent = new RunnableChangedEvent();

  /**
     Back pointer to the enclosing Behaviour (if present).
     @see jade.core.behaviours.ComplexBehaviour
  */
  protected ComplexBehaviour parent;

  final void setParent(ComplexBehaviour cb) {
    parent = cb;
  }

  /**
     Default constructor. It does not set the agent owning this
     behaviour object.
  */
  public Behaviour() {
  }

  /**
     Constructor with owner agent.
     @param a The agent owning this behaviour.
   */
  public Behaviour(Agent a) {
    myAgent = a;
  }

  /**
     Runs the behaviour. This abstract method must be implemented by
     <code>Behaviour</code>subclasses to perform ordinary behaviour
     duty. An agent schedules its behaviours calling their
     <code>action()</code> method; since all the behaviours belonging
     to the same agent are scheduled cooperatively, this method
     <b>must not</b> enter in an endless loop and should return as
     soon as possible to preserve agent responsiveness. To split a
     long and slow task into smaller section, recursive behaviour
     aggregation may be used.
     @see jade.core.behaviours.ComplexBehaviour
  */
  public abstract void action();

  /**
     Check if this behaviour is done. The agent scheduler calls this
     method to see whether a <code>Behaviour</code> still need to be
     run or it has completed its task. Concrete behaviours must
     implement this method to return their completion state. Finished
     behaviours are removed from the scheduling queue, while others
     are kept within to be run again when their turn comes again.
     @return <code>true</code> if the behaviour has completely executed.
  */
  public abstract boolean done();

  /**
     Restores behaviour initial state. This method must be implemented
     by concrete subclasses in such a way that calling
     <code>reset()</code> on a behaviour object is equivalent to
     destroying it and recreating it back. The main purpose for this
     method is to realize multistep cyclic behaviours without needing
     expensive constructions an deletion of objects at each loop
     iteration.
  */
  public abstract void reset();

  /**
     Handler for block/restart events. This method handles
     notification by copying its runnable state and then by simply
     forwarding the event when it is travelling upwards and by doing
     nothing when it is travelling downwards, since an ordinary
     behaviour has no children.
     @param rce The event to handle
  */
  protected void handle(RunnableChangedEvent rce) {
    setRunnable(rce.isRunnable());
    if( (parent != null) && (rce.isUpwards()) ) {
      parent.handle(rce);
    }
  }

  /**
     Returns the root for this <code>Behaviour</code> object. That is,
     the top-level behaviour this one is a part of. Agents apply
     scheduling only to top-level behaviour objects, so they just call
     <code>restart()</code> on root behaviours.
     @return The top-level behaviour this behaviour is a part of. If
     this one is a top level behaviour itself, then simply
     <code>this</code> is returned.
     @see jade.core.behaviours.Behaviour#restart()
   */
  public Behaviour root() {
    if(parent != null)
      return parent.root();
    else
      return this;
  }

  // Sets the runnable/not-runnable state
  void setRunnable(boolean runnable) {
    runnableState = runnable;
  }

  /**
     Returns whether this <code>Behaviour</code> object is blocked or
     not.
     @return <code>true</code> when this behaviour is not blocked,
     <code>false</code> when it is.
   */
  public boolean isRunnable() {
    return runnableState;
  }

  /**
     Blocks this behaviour. When this method is called, the behaviour
     state is set to <em>Blocked</em> and a suitable event is fired to
     notify its parent behaviour. Then the behaviour is put into a
     blocked behaviours queue by the agent scheduler. If this method
     is called from within <code>action()</code> method, behaviour
     suspension occurs as soon as <code>action()</code> returns.
     @see jade.core.behaviours.Behaviour#restart() 
  */
  public void block() {
    myEvent.init(false, NOTIFY_UP);
    handle(myEvent);
  }


  /**
     Blocks this behaviour for a specified amount of time. The
     behaviour will be restarted when among the three following
     events happens.
     <ul>
     <li> <em>A time of <code>millis</code> milliseconds has passed
     since the call to <code>block()</code>.</em>
     <li> <em>An ACL message is received by the agent this behaviour
     belongs to.</em>
     <li> <em>Method <code>restart()</code> is called explicitly on
     this behaviour object.</em>
     </ul>
     @param millis The amount of time to block, in
     milliseconds. <em><b>Notice:</b> a value of 0 for
     <code>millis</code> is equivalent to a call to
     <code>block()</code> without arguments.</em>
     @see jade.core.behaviours.Behaviour#block()
  */
  public void block(long millis) {
    myAgent.restartLater(this, millis);
    block();
  }

  /**
     Restarts a blocked behaviour. This method fires a suitable event
     to notify this behaviour's parent. When the agent scheduler
     inserts a blocked event back into the agent ready queue, it
     restarts it automatically. When this method is called, any timer
     associated with this behaviour object is cleared.
     @see jade.core.behaviours.Behaviour#block()
  */
  public void restart() {
    if(myAgent != null)
     myAgent.notifyRestarted(this);
    myEvent.init(true, NOTIFY_UP);
    handle(myEvent);
  }


}
