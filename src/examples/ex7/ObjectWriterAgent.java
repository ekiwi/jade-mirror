/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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

package examples.ex7;

import jade.lang.acl.ACLMessage;
import jade.core.Agent;
import jade.domain.AgentManagementOntology;

import java.util.*;
import java.io.*;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/

public class ObjectWriterAgent extends Agent {


protected void setup() {

  System.out.println(getLocalName()+" agent sends an ACLMessage whose content is a set of Java objects");

  /** Search with the DF for the name of the ObjectReaderAgent **/
  String reader = new String();
  AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();    
  dfd.setType("ObjectReaderAgent"); 
  try {
    while (true) {
      System.out.println(getLocalName()+ " waiting for an ObjectReaderAgent registering with the DF");
      AgentManagementOntology.DFSearchResult result;
      Vector vc = new Vector(1);
      AgentManagementOntology.Constraint c = new AgentManagementOntology.Constraint();
      c.setName(AgentManagementOntology.Constraint.DFDEPTH);
      c.setFn(AgentManagementOntology.Constraint.MAX); // MIN
      c.setArg(3);
      vc.addElement(c);
      result = searchDF("DF",dfd,vc);
      Enumeration e = result.elements();
      if (e.hasMoreElements()) {
	dfd = (AgentManagementOntology.DFAgentDescriptor)e.nextElement();
	reader = dfd.getName();
	break;
      } 
      Thread.sleep(10000);
    }
  } catch (Exception fe) {
    System.err.println(getLocalName()+" search with DF is not succeeded because of " + fe.getMessage());
    doDelete();
    }

    try {
      ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

      msg.addDest(reader);

      ByteArrayOutputStream c = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(c);

      Person p = new Person("JADE", "CSELT", new Date(), 2);
      oos.writeObject(p);
      System.out.println(getLocalName()+" written "+p.toString());
      oos.writeObject("Today");
      System.out.println(getLocalName()+" written Today");
      oos.writeObject(new Integer(12345));
      System.out.println(getLocalName()+" written 12345");
      Date d = new Date();
      oos.writeObject(d);
      System.out.println(getLocalName()+" written "+d.toString());
      oos.flush();
      
      msg.setContentBase64(c.toByteArray());
      send(msg);
      System.out.println(getLocalName()+" sent a message to "+reader);
    } catch (IOException e ) {
      e.printStackTrace();
    }
    doDelete();
  }
}


