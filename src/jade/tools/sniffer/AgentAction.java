/*****************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in
 * compliance with the FIPA specifications. Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, version 2.1 of
 * the License.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *****************************************************************/

package jade.tools.sniffer;

import jade.gui.AgentTree;

/**
 * Javadoc documentation for the file
 * 
 * @author Francisco Regi, Andrea Soracchi - Universita` di Parma
 * @version $Date$ $Revision$
 */

/**
 * This class is useful to represent the actions that must perform with the agents.
 * 
 * @see jade.tools.sniffer.FixedAction
 * @see jade.tools.sniffer.SnifferAction
 */

abstract class AgentAction extends SnifferAction {

  public AgentAction(String IconPath, String ActionName, ActionProcessor actPro) {
    super(IconPath, ActionName, actPro);
  }

  public abstract void doAction(AgentTree.AgentNode node);

} // End of AgentAction
