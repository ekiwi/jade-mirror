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

package jade.imtp.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import jade.util.leap.List;

import jade.core.AID;
import jade.core.Location;
import jade.core.NotFoundException;
import jade.core.IMTPException;

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPException;

/**
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$
 */
interface AgentContainerRMI extends Remote {

    public void moveAgent(AID agentID, Location where) throws RemoteException, NotFoundException, IMTPException;
    public void waitAgent(AID agentID) throws RemoteException, NotFoundException, IMTPException;
    public byte[] fetchClassFile(String name) throws RemoteException, ClassNotFoundException, IMTPException;
    public void disableDebugger(AID debuggerName, AID notToBeDebugged) throws RemoteException, IMTPException;
    public void exit() throws RemoteException, IMTPException;
    public void uninstallMTP(String address) throws RemoteException, NotFoundException, MTPException, IMTPException;
    public void wakeAgent(AID agentID) throws RemoteException, NotFoundException, IMTPException;
    public void createAgent(AID agentID, byte[] serializedInstance, AgentContainerRMI classSite, boolean startIt) throws RemoteException, IMTPException;
    public void createAgent(AID agentID, String className, Object[] arguments, boolean startIt) throws RemoteException, IMTPException;
    public void killAgent(AID agentID) throws RemoteException, NotFoundException, IMTPException;
    public String installMTP(String address, String className) throws RemoteException, MTPException, IMTPException;
    public void disableSniffer(AID snifferName, AID notToBeSniffed) throws RemoteException, IMTPException;
    public void resumeAgent(AID agentID) throws RemoteException, NotFoundException, IMTPException;
    public void suspendAgent(AID agentID) throws RemoteException, NotFoundException, IMTPException;
    public void copyAgent(AID agentID, Location where, String newName) throws RemoteException, NotFoundException, IMTPException;
    public void enableSniffer(AID snifferName, AID toBeSniffed) throws RemoteException, IMTPException;
    public void enableDebugger(AID debuggerName, AID toBeDebugged) throws RemoteException, IMTPException;
    public void routeOut(ACLMessage msg, AID receiver, String address) throws RemoteException, MTPException, IMTPException;
    public void postTransferResult(AID agentID, boolean result, List messages) throws RemoteException, NotFoundException, IMTPException;
    public void dispatch(ACLMessage msg, AID receiverID) throws RemoteException, NotFoundException, IMTPException;
    public void updateRoutingTable(int op, String address, AgentContainerRMI ac) throws RemoteException, IMTPException;
    public void ping(boolean hang) throws RemoteException, IMTPException;
    
}

