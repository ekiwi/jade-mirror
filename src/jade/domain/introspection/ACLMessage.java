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

package jade.domain.introspection;

import jade.domain.FIPAAgentManagement.Envelope;

/**

  This class represents an ACL message.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
public class ACLMessage {

  private Envelope env;
  private String payload;

  public void setEnvelope(Envelope e) {
    env = e;
  }

  public Envelope getEnvelope() {
    return env;
  }

  public void setAclRepresentation(String r) {
    if(env != null)
      env.setAclRepresentation(r);
  }

  public String getAclRepresentation() {
    if(env != null)
      return env.getAclRepresentation();
    else
      return null;
  }

  public void setPayload(String p) {
    payload = p;
  }

  public String getPayload() {
    return payload;
  }

}
