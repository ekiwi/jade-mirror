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

package jade.core.messaging;

//#MIDP_EXCLUDE_FILE

import java.io.IOException;
import java.util.Date;

import jade.core.ServiceFinder;

import jade.core.HorizontalCommand;
import jade.core.VerticalCommand;
import jade.core.GenericCommand;
import jade.core.Service;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.core.Sink;
import jade.core.Filter;
import jade.core.Node;

import jade.core.AgentContainer;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.NameClashException;
import jade.core.UnreachableException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.util.leap.Iterator;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.List;
import jade.util.leap.LinkedList;



/**

   The JADE service to manage the persistent storage of undelivered
   ACL messages installed on the platform.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public class PersistentDeliveryService extends BaseService {

    private static final String[] OWNED_COMMANDS = new String[] {
	PersistentDeliverySlice.ACTIVATE_MESSAGE_STORE,
	PersistentDeliverySlice.DEACTIVATE_MESSAGE_STORE,
	PersistentDeliverySlice.REGISTER_MESSAGE_TEMPLATE,
	PersistentDeliverySlice.DEREGISTER_MESSAGE_TEMPLATE,
	PersistentDeliverySlice.STORE_UNDELIVERED_MESSAGE
    };



    public void init(AgentContainer ac, Profile p) throws ProfileException {
	super.init(ac, p);
	myContainer = ac;
	myServiceFinder = myContainer.getServiceFinder();

	try {
	    MessageManager.Channel ch = (MessageManager.Channel)myServiceFinder.findService(MessagingSlice.NAME);
	    myManager = PersistentDeliveryManager.instance(p, ch);
	    myManager.start();
	}
	catch(IMTPException imtpe) {
	    imtpe.printStackTrace();
	}
	catch(ServiceException se) {
	    se.printStackTrace();
	}
    }

    public String getName() {
	return PersistentDeliverySlice.NAME;
    }

    public Class getHorizontalInterface() {
	try {
	    return Class.forName(PersistentDeliverySlice.NAME + "Slice");
	}
	catch(ClassNotFoundException cnfe) {
	    return null;
	}
    }

    public Service.Slice getLocalSlice() {
	return localSlice;
    }

    public Filter getCommandFilter(boolean direction) {
	if(direction == Filter.INCOMING) {
	    return inFilter;
	}
	else {
	    return null;
	}
    }

    public Sink getCommandSink(boolean side) {
	if(side == Sink.COMMAND_SOURCE) {
	    return senderSink;
	}
	else {
	    return receiverSink;
	}
    }

    public String[] getOwnedCommands() {
	return OWNED_COMMANDS;
    }

    // This inner class handles the messaging commands on the command
    // issuer side, turning them into horizontal commands and
    // forwarding them to remote slices when necessary.
    private class CommandSourceSink implements Sink {

	// Implementation of the Sink interface

	public void consume(VerticalCommand cmd) {
		
	    try {
		String name = cmd.getName();
		if(name.equals(PersistentDeliverySlice.ACTIVATE_MESSAGE_STORE)) {
		    handleActivateMessageStore(cmd);
		}
		if(name.equals(PersistentDeliverySlice.DEACTIVATE_MESSAGE_STORE)) {
		    handleDeactivateMessageStore(cmd);
		}
		else if(name.equals(PersistentDeliverySlice.REGISTER_MESSAGE_TEMPLATE)) {
		    handleRegisterMessageTemplate(cmd);
		}
		else if(name.equals(PersistentDeliverySlice.DEREGISTER_MESSAGE_TEMPLATE)) {
		    handleDeregisterMessageTemplate(cmd);
		}
		else if(name.equals(PersistentDeliverySlice.STORE_UNDELIVERED_MESSAGE)) {
		    cmd.setReturnValue(new Boolean(handleStoreUndeliveredMessage(cmd)));
		}
	    }
	    catch(IMTPException imtpe) {
		cmd.setReturnValue(imtpe);
	    }
	    catch(NotFoundException nfe) {
		cmd.setReturnValue(nfe);
	    }
	    catch(NameClashException nce) {
		cmd.setReturnValue(nce);
	    }
	    catch(ServiceException se) {
		cmd.setReturnValue(new IMTPException("A Service Exception occurred", se));		
	    }
	}


	// Vertical command handler methods

	private void handleActivateMessageStore(VerticalCommand cmd) throws IMTPException, ServiceException, NameClashException {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];

	    PersistentDeliverySlice targetSlice = (PersistentDeliverySlice)getSlice(sliceName);
	    try {
		targetSlice.activateMsgStore(storeName);
	    }
	    catch(IMTPException imtpe) {
		targetSlice = (PersistentDeliverySlice)getFreshSlice(sliceName);
		targetSlice.activateMsgStore(storeName);
	    }

	}

	private void handleDeactivateMessageStore(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];

	    PersistentDeliverySlice targetSlice = (PersistentDeliverySlice)getSlice(sliceName);
	    try {
		targetSlice.deactivateMsgStore(storeName);
	    }
	    catch(IMTPException imtpe) {
		targetSlice = (PersistentDeliverySlice)getFreshSlice(sliceName);
		targetSlice.deactivateMsgStore(storeName);
	    }

	}

	private void handleRegisterMessageTemplate(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException, NameClashException {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];
	    MessageTemplate mt = (MessageTemplate)params[2];

	    PersistentDeliverySlice targetSlice = (PersistentDeliverySlice)getSlice(sliceName);
	    try {
		targetSlice.registerTemplate(storeName, mt);
	    }
	    catch(IMTPException imtpe) {
		targetSlice = (PersistentDeliverySlice)getFreshSlice(sliceName);
		targetSlice.registerTemplate(storeName, mt);
	    }

	}

	private void handleDeregisterMessageTemplate(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];
	    MessageTemplate mt = (MessageTemplate)params[2];

	    PersistentDeliverySlice targetSlice = (PersistentDeliverySlice)getSlice(sliceName);
	    try {
		targetSlice.deregisterTemplate(storeName, mt);
	    }
	    catch(IMTPException imtpe) {
		targetSlice = (PersistentDeliverySlice)getFreshSlice(sliceName);
		targetSlice.deregisterTemplate(storeName, mt);
	    }
	}

	private boolean handleStoreUndeliveredMessage(VerticalCommand cmd) throws IMTPException, ServiceException {

	    Object[] params = cmd.getParams();
	    ACLMessage msg = (ACLMessage)params[0];
	    AID receiver = (AID)params[1];

	    Service.Slice[] slices = getAllSlices();
	    for(int i = 0; i < slices.length; i++) {
		try {
		    PersistentDeliverySlice slice = (PersistentDeliverySlice)slices[i];
		    boolean accepted = slice.storeMessage(null, msg, receiver);

		    if(accepted) {
			return true;
		    }
		}
		catch(Exception e) {
		    // Ignore it and try other slices...
		}
	    }

	    return false;
	}


    } // End of CommandSourceSink class


    private class CommandTargetSink implements Sink {

	// Implementation of the Sink interface

	public void consume(VerticalCommand cmd) {
		
	    //	    try {
		String name = cmd.getName();
		if(name.equals(PersistentDeliverySlice.ACTIVATE_MESSAGE_STORE)) {
		    handleActivateMessageStore(cmd);
		}
		if(name.equals(PersistentDeliverySlice.DEACTIVATE_MESSAGE_STORE)) {
		    handleDeactivateMessageStore(cmd);
		}
		else if(name.equals(PersistentDeliverySlice.REGISTER_MESSAGE_TEMPLATE)) {
		    handleRegisterMessageTemplate(cmd);
		}
		else if(name.equals(PersistentDeliverySlice.DEREGISTER_MESSAGE_TEMPLATE)) {
		    handleDeregisterMessageTemplate(cmd);
		}
		/************
	    }
	    catch(IMTPException imtpe) {
		cmd.setReturnValue(imtpe);
	    }
	    catch(NotFoundException nfe) {
		cmd.setReturnValue(nfe);
	    }
	    catch(NameClashException nce) {
		cmd.setReturnValue(nce);
	    }
	    catch(ServiceException se) {
		cmd.setReturnValue(new IMTPException("A Service Exception occurred", se));		
	    }
		***************/
	}


	// Vertical command handler methods

	private void handleActivateMessageStore(VerticalCommand cmd) {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];

	    System.out.println("--- ACTIVATE_MESSAGE_STORE: Not Implemented ---");

	}

	private void handleDeactivateMessageStore(VerticalCommand cmd) {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];

	    System.out.println("--- DEACTIVATE_MESSAGE_STORE: Not Implemented ---");

	}

	private void handleRegisterMessageTemplate(VerticalCommand cmd) {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];
	    MessageTemplate mt = (MessageTemplate)params[2];

	    System.out.println("--- REGISTER_MESSAGE_TEMPLATE: Not Implemented ---");

	}

	private void handleDeregisterMessageTemplate(VerticalCommand cmd) {
	    Object[] params = cmd.getParams();
	    String sliceName = (String)params[0];
	    String storeName = (String)params[1];
	    MessageTemplate mt = (MessageTemplate)params[2];

	    System.out.println("--- DEREGISTER_MESSAGE_TEMPLATE: Not Implemented ---");

	}


    } // End of CommandTargetSink class


    private class CommandIncomingFilter implements Filter {

	public void accept(VerticalCommand cmd) {

	    try {
		String name = cmd.getName();

		if(name.equals(jade.core.management.AgentManagementSlice.INFORM_CREATED)) {
		    handleInformCreated(cmd);
		}
	    }
	    catch(IMTPException imtpe) {
		cmd.setReturnValue(imtpe);
	    }
	    catch(ServiceException se) {
		cmd.setReturnValue(se);
	    }
	}

	private void handleInformCreated(VerticalCommand cmd) throws IMTPException, ServiceException {

	    Object[] params = cmd.getParams();

	    AID agentID = (AID)params[0];

	    Service.Slice[] slices = getAllSlices();
	    for(int i = 0; i < slices.length; i++) {
		try {
		    PersistentDeliverySlice slice = (PersistentDeliverySlice)slices[i];
		    slice.flushMessages(agentID);
		}
		catch(Exception e) {
		    // Ignore it and try other slices...
		}
	    }
	}

	public void setBlocking(boolean newState) {
	    // Do nothing. Blocking and Skipping not supported
	}

    	public boolean isBlocking() {
	    return false; // Blocking and Skipping not implemented
	}

	public void setSkipping(boolean newState) {
	    // Do nothing. Blocking and Skipping not supported
	}

	public boolean isSkipping() {
	    return false; // Blocking and Skipping not implemented
	}

    } // End of CommandIncomingFilter class


    /**
       Inner class for this service: this class receives commands from
       service <code>Sink</code> and serves them, coordinating with
       remote parts of this service through the <code>Slice</code>
       interface (that extends the <code>Service.Slice</code>
       interface).
    */
    private class ServiceComponent implements Service.Slice {


	// Implementation of the Service.Slice interface

	public Service getService() {
	    return PersistentDeliveryService.this;
	}

	public Node getNode() throws ServiceException {
	    try {
		return PersistentDeliveryService.this.getLocalNode();
	    }
	    catch(IMTPException imtpe) {
		throw new ServiceException("Problem in contacting the IMTP Manager", imtpe);
	    }
	}

	public VerticalCommand serve(HorizontalCommand cmd) {
	    VerticalCommand result = null;
	    try {
		String cmdName = cmd.getName();
		Object[] params = cmd.getParams();

		if(cmdName.equals(PersistentDeliverySlice.H_ACTIVATEMSGSTORE)) {
		    GenericCommand gCmd = new GenericCommand(PersistentDeliverySlice.ACTIVATE_MESSAGE_STORE, PersistentDeliverySlice.NAME, null);
		    String name = (String)params[0];
		    gCmd.addParam(name);

		    result = gCmd;
		}
		else if(cmdName.equals(PersistentDeliverySlice.H_DEACTIVATEMSGSTORE)) {
		    GenericCommand gCmd = new GenericCommand(PersistentDeliverySlice.DEACTIVATE_MESSAGE_STORE, PersistentDeliverySlice.NAME, null);
		    String name = (String)params[0];
		    gCmd.addParam(name);

		    result = gCmd;
		}
		else if(cmdName.equals(PersistentDeliverySlice.H_REGISTERTEMPLATE)) {
		    GenericCommand gCmd = new GenericCommand(PersistentDeliverySlice.REGISTER_MESSAGE_TEMPLATE, PersistentDeliverySlice.NAME, null);
		    String storeName = (String)params[0];
		    MessageTemplate mt = (MessageTemplate)params[1];
		    gCmd.addParam(storeName);
		    gCmd.addParam(mt);

		    result = gCmd;
		    
		}
		else if(cmdName.equals(PersistentDeliverySlice.H_DEREGISTERTEMPLATE)) {
		    GenericCommand gCmd = new GenericCommand(PersistentDeliverySlice.DEREGISTER_MESSAGE_TEMPLATE, PersistentDeliverySlice.NAME, null);
		    String storeName = (String)params[0];
		    MessageTemplate mt = (MessageTemplate)params[1];
		    gCmd.addParam(storeName);
		    gCmd.addParam(mt);

		    result = gCmd;
		}
		else if(cmdName.equals(PersistentDeliverySlice.H_STOREMESSAGE)) {
		    String storeName = (String)params[0];
		    ACLMessage msg = (ACLMessage)params[1];
		    AID receiver = (AID)params[2];

		    cmd.setReturnValue(new Boolean(storeMessage(storeName, msg, receiver)));
		}
		else if(cmdName.equals(PersistentDeliverySlice.H_FLUSHMESSAGES)) {
		    AID receiver = (AID)params[0];

		    flushMessages(receiver);
		}
	    }
	    catch(Throwable t) {
		cmd.setReturnValue(t);
	    }
	    finally {
		return result;
	    }
	}

	private boolean storeMessage(String storeName, ACLMessage msg, AID receiver) throws IMTPException, ServiceException {

	    long delay = messageFilter.delayBeforeExpiration(msg);
	    if(delay != PersistentDeliveryFilter.NOW) {
		try {
		    myManager.storeMessage(storeName, msg, receiver, delay);
		    return true;
		}
		catch(IOException ioe) {
		    throw new ServiceException("I/O Error in message storage", ioe);
		}
	    }
	    else {
		return false;
	    }

	}

	private void flushMessages(AID receiver) {
	    myManager.flushMessages(receiver);
	}

    } // End of ServiceComponent class


    private class DefaultMessageFilter implements PersistentDeliveryFilter {

	// Never store messages
	public long delayBeforeExpiration(ACLMessage msg) {
	    return NOW;
	}

	/****

	// Test configuration: require the :ontology slot to hold the
	// name of the local container, and use :reply-by message slot
	// to select the expiration time.
	public long delayBeforeExpiration(ACLMessage msg) {

	    if(msg.getOntology().equals(myContainer.getID().getName())) {
		Date d = msg.getReplyByDate();
		if(d != null) {
		    long delay = d.getTime() - System.currentTimeMillis();
		    return (delay > 0) ? delay : 0;
		}
		else {
		    return NEVER;
		}
	    }
	    else {
		return NOW;
	    }

	}
	***/

    } // End of DefaultMessageFilter class


    /**
       Activates the ACL codecs and MTPs as specified in the given
       <code>Profile</code> instance.
       @param myProfile The <code>Profile</code> instance containing
       the list of ACL codecs and MTPs to activate on this node.
    **/
    public void boot(Profile myProfile) throws ServiceException {
	try {
	    // Load the supplied class to filter messages, or use the default
	    String className = myProfile.getParameter(Profile.PERSISTENT_DELIVERY_FILTER, null);
	    if(className != null) {
		Class c = Class.forName(className);
		messageFilter = (PersistentDeliveryFilter)c.newInstance();
	    }
	    else {
		messageFilter = new DefaultMessageFilter();
	    }
	}
	catch(Exception e) {
	    throw new ServiceException("Exception in message filter initialization", e);
	}

    }


    // The concrete agent container, providing access to LADT, etc.
    private AgentContainer myContainer;

    // The service finder component
    private ServiceFinder myServiceFinder;

    // The component managing ACL message storage and delayed delivery
    private PersistentDeliveryManager myManager;

    // The local slice for this service
    private final ServiceComponent localSlice = new ServiceComponent();

    // The command sink, source side
    private final CommandSourceSink senderSink = new CommandSourceSink();

    // The command sink, target side
    private final CommandTargetSink receiverSink = new CommandTargetSink();

    // The command filter, incoming direction
    private final CommandIncomingFilter inFilter = new CommandIncomingFilter();

    // Service-specific data

    // The filter to be matched by undelivered ACL messages
    private PersistentDeliveryFilter messageFilter;

}