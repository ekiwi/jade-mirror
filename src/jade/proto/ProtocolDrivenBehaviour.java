package jade.proto;

import jade.core.Behaviour;

/**************************************************************

  Name: ProtocolDrivenBehaviour

  Responsibility and Collaborations:

  + Simplifies the realization of an agent behaviour adhering to a
    known interaction protocol.
    (Protocol)

  + Relies on an Interaction object to maintain specific data for
    every interaction the agent participates in.
    (Interaction)

****************************************************************/
public class ProtocolDrivenBehaviour implements Behaviour {

  ProtocolDrivenBehaviour() {
  }

  public void execute() {
  }

  public boolean done() {
    return true; // FIXME: To be implemented
  }

}
