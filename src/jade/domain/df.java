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

package jade.domain;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.net.InetAddress;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.Register;
import jade.domain.FIPAAgentManagement.Deregister;
import jade.domain.FIPAAgentManagement.Modify;
import jade.domain.FIPAAgentManagement.Search;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology;
import jade.domain.FIPAAgentManagement.MissingParameter;
import jade.domain.FIPAAgentManagement.AlreadyRegistered;
import jade.domain.FIPAAgentManagement.NotRegistered;
import jade.domain.JADEAgentManagement.JADEAgentManagementOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.sl.SL0Codec;
import jade.onto.basic.Action;
import jade.onto.basic.ResultPredicate;
import jade.proto.FipaRequestResponderBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.tools.dfgui.DFGUI;
import jade.proto.FipaRequestInitiatorBehaviour;


/**
  Standard <em>Directory Facilitator</em> agent. This class implements
  <em><b>FIPA</b></em> <em>DF</em> agent. <b>JADE</b> applications
  cannot use this class directly, but interact with it through
  <em>ACL</em> message passing. More <em>DF</em> agents can be created
  by application programmers to divide a platform into many
  <em><b>Agent Domains</b></em>.

  Each DF has a GUI but, by default, it is not visible. The GUI of the
  agent platform includes a menu item that allows to show the GUI of the
  default DF. 

  In order to show the GUI, you should simply send the following message
  to each DF agent: <code>(request :content (action DFName (SHOWGUI))
  :ontology jade-extensions :protocol fipa-request)</code>
 
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
public class df extends GuiAgent implements DFGUIAdapter {

  private abstract class DFBehaviour
    extends FipaRequestResponderBehaviour.ActionHandler 
    implements FipaRequestResponderBehaviour.Factory {

    protected DFBehaviour(ACLMessage req) {
      super(df.this,req);
    }
  

    // Each concrete subclass will implement this deferred method to
    // do action-specific work
    protected abstract void processAction(Action a) throws FIPAException;

    public void action() {

      try {
      	
      	ACLMessage msg = getRequest();

	// Extract the Action object from the message content
	List l = extractContent(msg);
	Action a = (Action)l.get(0);

      	// Do real action, deferred to subclasses
      	processAction(a);
      
      }
      catch(FIPAException fe) {	
      	sendReply(ACLMessage.REFUSE,fe.getMessage());
      }

    }

      public boolean done() {
      	return true;
      }

      public void reset() {}

  } // End of DFBehaviour class


  // These four concrete classes serve both as a Factory and as
  // Action: when seen as Factory they can spawn a new
  // Behaviour to process a given request, and when seen as
  // Action they process their request and terminate.

  private class RegBehaviour extends DFBehaviour {

    public RegBehaviour(ACLMessage msg) {
      super(msg);
    }

    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new RegBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      Register r = (Register)a.getAction();
      DFAgentDescription dfd = (DFAgentDescription)r.get_0();
      DFRegister(dfd);
      //sendReply(ACLMessage.AGREE,"( true )");
      sendReply(ACLMessage.INFORM,"( (done (action (Agent-Identifier :name "+getAID().getName()+") register))");
    }

  } // End of RegBehaviour class

  private class DeregBehaviour extends DFBehaviour {
    public DeregBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new DeregBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      Deregister d = (Deregister)a.getAction();
      DFAgentDescription dfd = (DFAgentDescription)d.get_0();
      DFDeregister(dfd);
      //sendReply(ACLMessage.AGREE,"( true )");
      sendReply(ACLMessage.INFORM,"( (done (action (Agent-Identifier :name "+getAID().getName()+") deregister))");
    }

  } // End of DeregBehaviour class

  private class ModBehaviour extends DFBehaviour {
    public ModBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new ModBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      Modify m = (Modify)a.getAction();
      DFAgentDescription dfd = (DFAgentDescription)m.get_0();
      DFModify(dfd);
      //sendReply(ACLMessage.AGREE,"( true )");
      sendReply(ACLMessage.INFORM,"( (done (action (Agent-Identifier :name "+getAID().getName()+") modify))");
    }

  } // End of ModBehaviour class

  private class RecursiveSearchBehaviour extends RequestFIPAServiceBehaviour 
  {
  	RecursiveSearchHandler rsh;
  	RecursiveSearchBehaviour(RecursiveSearchHandler rsh,AID children,DFAgentDescription dfd,SearchConstraints constraints) throws FIPAException
  	{
  		super(df.this,children,FIPAAgentManagementOntology.SEARCH,dfd,constraints);
  		this.rsh = rsh;
  	}
  	
  	protected void handleInform(ACLMessage reply)
  	{
  		super.handleInform(reply);
  		try{
  			rsh.addResults(this,getSearchResult());
  		}catch (FIPAException e){
  		}catch(NotYetReady nyr){}
  	}
  	
  	protected void handleRefuse(ACLMessage reply)
  	{
  		super.handleRefuse(reply);
  	}
  	
  	protected void handleFailure(ACLMessage reply)
  	{
  		super.handleFailure(reply);
  	}
  }//End class RecursiveSearchBehaviour
  
   private class SrchBehaviour extends DFBehaviour {
    public SrchBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new SrchBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      sendReply(ACLMessage.AGREE,"( true )");
    	Search s = (Search)a.getAction();
      DFAgentDescription dfd = (DFAgentDescription)s.get_0();
      SearchConstraints constraints = s.get_1();
      List l = DFSearch(dfd, constraints, getReply());
      Long maxResults = constraints.getMaxResults();
      
      if( maxResults != null)
      		if (l.size() >= maxResults.intValue()) 
      		{
      			l = l.subList(0, maxResults.intValue());
      		
      			ACLMessage msg = getRequest().createReply();
      			msg.setPerformative(ACLMessage.INFORM);
            ResultPredicate r = new ResultPredicate();
            r.set_0(a);
            for (int i=0; i<l.size(); i++)
      	      r.add_1(l.get(i));
            l.clear();
            l.add(r);
            fillContent(msg,l); 
            send(msg);
      			return;
      		}
      	
      Long maxDepth = constraints.getMaxDepth();
      
      if(maxDepth != null)		
      if (maxDepth.intValue() > 0 )
      { //recursive search on children
        
      	RecursiveSearchHandler rsh = new RecursiveSearchHandler(l, constraints, dfd, getRequest(),a);
      	SearchConstraints newConstr = new SearchConstraints();
      	
      	newConstr.setMaxDepth(new Long ((new Integer(constraints.getMaxDepth().intValue()-1)).longValue()));
      	
      	if(maxResults != null)
      	newConstr.setMaxResults(new Long((new Integer(constraints.getMaxResults().intValue() - l.size())).longValue()));
      	
      
      	Iterator childIt = children.iterator();
      	while(childIt.hasNext())
      	{
      	
      	    try{
      	    	RecursiveSearchBehaviour b = new RecursiveSearchBehaviour(rsh,(AID)childIt.next(), dfd, newConstr);
      	    	addBehaviour(b);
      	      rsh.addChildren(b);
      	    }catch(FIPAException e){}
      	}
      	if (children.size() != 0) 
      	return;
      }		
      ACLMessage msg = getRequest().createReply();
      msg.setPerformative(ACLMessage.INFORM);
      ResultPredicate r = new ResultPredicate();
      r.set_0(a);
      for (int i=0; i<l.size(); i++)
      	r.add_1(l.get(i));
      l.clear();
      l.add(r);
      fillContent(msg,l); 
      send(msg);
    }

  } // End of SrchBehaviour class


  private class RecursiveSearchHandler {
  	List children;
  	long deadline;
  	List results;
  	SearchConstraints constraints;
  	DFAgentDescription dfd;
  	ACLMessage request;
  	Action action;
    int DEFAULTTIMEOUT = 60000; // 1 minute	
  	long MAXRESULTS = 100; //Maximum number of results if not set 
  	
    //constructor
    RecursiveSearchHandler(List l, SearchConstraints c, DFAgentDescription dfd, ACLMessage msg, Action a) { 
	    this.results = l;
	    this.constraints = new SearchConstraints();
	    constraints.setMaxDepth(c.getMaxDepth()); //MaxDepth is not null by definition of this point of the code
	    if(c.getMaxResults() != null)
	      constraints.setMaxResults(c.getMaxResults());
	    else
	      constraints.setMaxResults(new Long(MAXRESULTS));
	    this.dfd = dfd;
	    this.request = msg;
	    this.children = new ArrayList();
	    
    	if (this.request.getReplyByDate() == null)
  		   this.deadline = System.currentTimeMillis() + DEFAULTTIMEOUT;
  	  else 
  	     this.deadline = this.request.getReplyByDate().getTime();
  	  this.action = a;   
  	}
  	
    void addChildren(Behaviour b) {
  		this.children.add(b);
  	}
  	void removeChildren(Behaviour b) {
  		this.children.remove(b);
  	}
  	
   	void addResults(Behaviour b, List localResults) throws FIPAException, jade.domain.RequestFIPAServiceBehaviour.NotYetReady {
  		this.children.remove(b);
  	// add local results to the full list of results
  		for (Iterator i=localResults.iterator(); i.hasNext(); )
  			results.add(i.next());
  		
  		if ( (results.size() >= constraints.getMaxResults().intValue()) || 
  			   (System.currentTimeMillis() >= deadline) || 
  			   (children.size() == 0)  )
   
  			{
  		   ACLMessage inform = request.createReply();
  		   inform.setPerformative(ACLMessage.INFORM);
  		   ResultPredicate r = new ResultPredicate();
         r.set_0(action);
         for (int i=0; i<results.size(); i++)
      	  r.add_1(results.get(i));
      	 ArrayList tuple = new ArrayList(1);
      	 tuple.add(r);
         fillContent(inform,tuple); 
         send(inform);
         // kill the behaviours in children
  			} 
  	}
  }
  private class ShowGUIBehaviour extends FipaRequestResponderBehaviour.ActionHandler 
                                 implements FipaRequestResponderBehaviour.Factory 
  {
  	protected ShowGUIBehaviour(ACLMessage msg) 
	{
      	super(df.this,msg);
  	}

  	public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) 
  	{
      	return new ShowGUIBehaviour(msg);
  	}

  	public void action () 
  	{ 
	    //sendReply(ACLMessage.AGREE,"( true )");
	    if (((df)myAgent).showGui())
	      sendReply(ACLMessage.INFORM,"( )");
	    else
	      sendReply(ACLMessage.FAILURE,"(Gui_is_being_shown_already)");
  	}
      
      public boolean done() 
      {
	  return true;
      }

      public void reset() 
      {
      }
  } // End of ShowGUIBehaviour class

  // This behaviour will be used to respond to request from the applet to know the parent with which this df is federated.
  /*private class GetParentsBehaviour extends FipaRequestResponderBehaviour.ActionHandler implements FipaRequestResponderBehaviour.Factory
  {
      protected GetParentsBehaviour(ACLMessage msg)
      {
	super(df.this,msg);
      }
  	
      public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg)
      {
	return new GetParentsBehaviour(msg);
      }

      public void action ()
      {
	sendReply(ACLMessage.AGREE,"FIXME");
  		
	try {
	  // FIXME: Construct the reply, using the FIPAAgentManagementOntology singleton to build the sequence of
	  // DFAgentDescription objects for the parents.
	  throw new FIPAException("STUB CODE !!!");
	}
	catch(FIPAException e) {
	  sendReply(ACLMessage.FAILURE,"Impossible to provide the needed information");
	}
      }

      public boolean done()
      {
	return true;
      }

      public void reset()
      {

      }
  	
  }*/ // End of GetParentsBehaviour class
  
  //This Behaviour returns the description of this df used to federate with another df 
  //It is used to reply to a request from the applet 
  /*private class GetDescriptionOfThisDFBehaviour extends FipaRequestResponderBehaviour.ActionHandler implements FipaRequestResponderBehaviour.Factory
  {
    protected GetDescriptionOfThisDFBehaviour(ACLMessage msg)
    {
      super(df.this,msg);
    }
  	
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg)
    {
      return new GetDescriptionOfThisDFBehaviour(msg);
    }
  	
    public void action()
      {
      	sendReply(ACLMessage.AGREE,"FIXME");

	      // FIXME: Create an 'inform' ACL message containing the DFAgentDescription object for this DF.

      }

    public boolean done()
    {
      return true;
    }

    public void reset()
    {

    }

  }*/ //  End of GetDescriptionOfThisDFBehaviour class
  
  // This behaviour allows the federation of this df with another df required by the APPLET
  /*private class FederateWithBehaviour extends FipaRequestResponderBehaviour.ActionHandler implements FipaRequestResponderBehaviour.Factory {

    protected FederateWithBehaviour(ACLMessage msg) {
      super(df.this,msg);
    }

    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new FederateWithBehaviour(msg);
    }

    public void action() {
      // FIXME: Federate with the given DF
    }

    public boolean done() {
      return true;
    }

    public void reset(){}


  } */// End of FederateWithBehaviour
  

  
  //This behaviour allow the applet to required the df to deregister itself from a parent of the federation
  /*private class DeregisterFromBehaviour extends FipaRequestResponderBehaviour.ActionHandler implements FipaRequestResponderBehaviour.Factory
  {

  	protected DeregisterFromBehaviour(ACLMessage msg)
  	{
  		super(df.this,msg);
  	}
  	
  	public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg)
  	{
  		return new DeregisterFromBehaviour(msg);
  	}
  	
  	public void action()
  	{
	  // FIXME:Deregister from the given DF;
  	}
  	
  	public boolean done()
  	{
  		return true;
  	}
  	public void reset(){}
  	
  }*///End DeregisterFromBehaviour
  
  /**
  All the actions requested via the DFGUI to another df extends this behaviour
  **/
  private class GUIRequestDFServiceBehaviour extends RequestFIPAServiceBehaviour
  {
    String actionName;
    DFGUI gui;
    AID receiverDF;
    DFAgentDescription dfd;
    
  	GUIRequestDFServiceBehaviour(AID receiverDF, String actionName, DFAgentDescription dfd, SearchConstraints constraints, DFGUI gui) throws FIPAException
  	{
  		
  		super(df.this,receiverDF,actionName,dfd,constraints);
  		
  		this.actionName = actionName;
  		this.gui = gui;
  		this.receiverDF = receiverDF;
  		this.dfd = dfd;
  	}
  	
  	protected void handleInform(ACLMessage msg)
  	{
  		super.handleInform(msg);
  		if(actionName.equalsIgnoreCase(FIPAAgentManagementOntology.SEARCH))
  		{
  			try{
  				gui.showStatusMsg("Search request Processed. Ready for new request");
  				gui.refreshLastSearchResults(getSearchResult(),msg.getSender());
  			}catch (Exception e){
  			e.printStackTrace();// should never happen
  			}
  		}
  		else
  		if(actionName.equalsIgnoreCase(FIPAAgentManagementOntology.REGISTER))
  		{
  			try{
  				gui.showStatusMsg("Request Processed. Ready for new request");
  				
  				if(dfd.getName().equals(df.this.getAID()))
  				{ //if what I register is  myself then I have federated with a parent
  					addParent(receiverDF,dfd);
  				}
  			}catch (Exception e){
  			e.printStackTrace();// should never happen
  			}
  		}
  		else
  		if(actionName.equalsIgnoreCase(FIPAAgentManagementOntology.DEREGISTER))
  		{
  			try
  			{
  				gui.showStatusMsg("Deregister request Processed. Ready for new request");
			    //this behaviour is never used to deregister an agent of this DF
			    // but only to deregister a parent or an agent that was registered with
			    // one of my parents or my children
  			    if(dfd.getName().equals(df.this.getAID()))
			      { 
			        //I deregister myself from a parent
			        removeParent(receiverDF);
			      }
  			    else
			      {
			        gui.removeSearchResult(dfd.getName());
			      }
  			}catch (Exception e){
  			e.printStackTrace();// should never happen
  			}
  		}
  		else 
  		if(actionName.equalsIgnoreCase(FIPAAgentManagementOntology.MODIFY))
  		{
  			try{
  				gui.showStatusMsg("Modify request processed. Ready for new request");
  			}catch(Exception e){
  			e.printStackTrace();
  			}
  		}

  	}
  	
  	protected void handleRefuse(ACLMessage msg)
  	{
  		super.handleRefuse(msg);
  		try{
  			gui.showStatusMsg("Request Refused: " + msg.getContent());
  		}catch(Exception e)
  		{}
  	}
  	
  	protected void handleFailure(ACLMessage msg)
  	{
  		super.handleFailure(msg);
  		try{
  		gui.showStatusMsg("Request Failed: " + msg.getContent());
  		}catch(Exception e){}
  	}
  	
  	protected void handleNotUnderstood(ACLMessage msg)
  	{
  		super.handleNotUnderstood(msg);
  		try{
  			gui.showStatusMsg("Request not understood: " + msg.getContent());
  		}catch(Exception e){}
  	}


  }
  
  private static int NUMBER_OF_AGENT_FOUND = 1000;

  /**
  @serial
  */
  private FipaRequestResponderBehaviour dispatcher;

  /**
  @serial
  */
  private FipaRequestResponderBehaviour jadeExtensionDispatcher;

  /**
  @serial
  */
  private List children = new ArrayList();
  /**
  @serial
  */

  private List parents = new ArrayList();
  
  private HashMap dscDFParentMap = new HashMap(); //corrispondence parent --> dfd description (of this df) used to federate.
  /**

  @serial
  */
  private DFGUI gui;

  // Current description of the df
  /**
  @serial
  */
  private DFAgentDescription thisDF = null;
  
 	
  
  /**
    This constructor creates a new <em>DF</em> agent. This can be used
    to create additional <em>DF</em> agents, beyond the default one
    created by <em><b>JADE</b></em> on platform startup.
  */
  public df() {

    MessageTemplate mt = 
      MessageTemplate.and(MessageTemplate.MatchLanguage(SL0Codec.NAME),
			  MessageTemplate.MatchOntology(FIPAAgentManagementOntology.NAME));

    dispatcher = new FipaRequestResponderBehaviour(this, mt);

    // Associate each DF action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message

    dispatcher.registerFactory(FIPAAgentManagementOntology.REGISTER, new RegBehaviour(null));
    dispatcher.registerFactory(FIPAAgentManagementOntology.DEREGISTER, new DeregBehaviour(null));
    dispatcher.registerFactory(FIPAAgentManagementOntology.MODIFY, new ModBehaviour(null));
    dispatcher.registerFactory(FIPAAgentManagementOntology.SEARCH, new SrchBehaviour(null));

    // Behaviour to deal with the GUI
    
    MessageTemplate mt1 = MessageTemplate.and(
                             MessageTemplate.MatchOntology(JADEAgentManagementOntology.NAME),
    	                       MessageTemplate.MatchLanguage(SL0Codec.NAME));
    jadeExtensionDispatcher = new FipaRequestResponderBehaviour(this, mt1);
    jadeExtensionDispatcher.registerFactory(JADEAgentManagementOntology.SHOWGUI, new ShowGUIBehaviour(null));
    // The following three actions are used only by the DFApplet	
    //jadeExtensionDispatcher.registerFactory("GETPARENTS", new GetParentsBehaviour(null)); 
    //jadeExtensionDispatcher.registerFactory("FEDERATE_WITH", new FederateWithBehaviour(null));
    //jadeExtensionDispatcher.registerFactory("DEREGISTER_FROM", new DeregisterFromBehaviour(null));
    //jadeExtensionDispatcher.registerFactory("GETDEFAULTDESCRIPTION", new GetDescriptionOfThisDFBehaviour(null)); 
  }

  /**
    This method starts all behaviours needed by <em>DF</em> agent to
    perform its role within <em><b>JADE</b></em> agent platform.
  */
  protected void setup() {
    // register the codec of the language
    registerLanguage(SL0Codec.NAME,new SL0Codec());	
		
    // register the ontology used by application
    registerOntology(FIPAAgentManagementOntology.NAME, FIPAAgentManagementOntology.instance());
    registerOntology(JADEAgentManagementOntology.NAME, JADEAgentManagementOntology.instance());

    // Add a message dispatcher behaviour
    addBehaviour(dispatcher);
    addBehaviour(jadeExtensionDispatcher);
    setDescriptionOfThisDF(getDefaultDescription());
   
  }  // End of method setup()

	/**
	  This method make visible the GUI of the DF.
	  @return true if the GUI was not visible already, false otherwise.
	*/
  public boolean showGui() {
   if (gui == null) 
  		{
		    gui = new DFGUI(df.this, false);
		    DFAgentDescription matchEverything = new DFAgentDescription();
		    List agents = agentDescriptions.search(matchEverything);
		    List AIDList = new ArrayList();
		    Iterator it = agents.iterator();
		    while(it.hasNext())
		    	AIDList.add(((DFAgentDescription)it.next()).getName());
		    
		    gui.refresh(AIDList.iterator(), parents.iterator(), children.iterator());
		    gui.setVisible(true);
		    return true;
  		}
 
   return false;
  }
   
 


  /**
    Cleanup <em>DF</em> on exit. This method performs all necessary
    cleanup operations during agent shutdown.
  */
  protected void takeDown() {

    if(gui != null)
	gui.disposeAsync();
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    Iterator it = parents.iterator();
    while(it.hasNext()) {
      AID parentName = (AID)it.next();
      try {
        DFServiceCommunicator.deregister(this, parentName, dfd);
      }
      catch(FIPAException fe) {
        fe.printStackTrace();
      }
    }
  }

  private boolean isADF(DFAgentDescription dfd) {
  	try {
  		return ((ServiceDescription)dfd.getAllServices().next()).getType().equalsIgnoreCase("fipa-df");
  	} catch (Exception e) {
  		return false;
  	}
  }
  
  /**
  * checks that all the mandatory slots for a register/modify/deregister action
  * are present.
  * @param actionName is the name of the action (one of 
  * <code>FIPAAgentManagementOntology.REGISTER</code>,
  * <code>FIPAAgentManagementOntology.MODIFY</code>,
  * <code>FIPAAgentManagementOntology.DEREGISTER</code>)
  * @param dfd is the DFAgentDescription to be checked for
  * @throws MissingParameter if one of the mandatory slots is missing
  **/
  private void checkMandatorySlots(String actionName, DFAgentDescription dfd) throws MissingParameter {
  	try {
  	  if (dfd.getName().getName().length() == 0)
  		  throw new MissingParameter(FIPAAgentManagementOntology.DFAGENTDESCRIPTION, "name");
  	} catch (Exception e) {
  		throw new MissingParameter(FIPAAgentManagementOntology.DFAGENTDESCRIPTION, "name");
  	}
  	if (!actionName.equalsIgnoreCase(FIPAAgentManagementOntology.DEREGISTER))
  	 for (Iterator i=dfd.getAllServices(); i.hasNext();) {
  		ServiceDescription sd =(ServiceDescription)i.next();
  		try {
  		  if (sd.getName().length() == 0)
  		   throw new MissingParameter(FIPAAgentManagementOntology.SERVICEDESCRIPTION, "name");
  	  } catch (Exception e) {
  		   throw new MissingParameter(FIPAAgentManagementOntology.SERVICEDESCRIPTION, "name");
  	  }
  	  try {
  		  if (sd.getType().length() == 0)
  		   throw new MissingParameter(FIPAAgentManagementOntology.SERVICEDESCRIPTION, "type");
  	  } catch (Exception e) {
  		   throw new MissingParameter(FIPAAgentManagementOntology.SERVICEDESCRIPTION, "type");
  	  }
  	 } //end of for
  }
  
  
  private KB agentDescriptions = new KBAbstractImpl() {
      protected boolean match(Object template, Object fact) {

	try {
	  DFAgentDescription templateDesc = (DFAgentDescription)template;
	  DFAgentDescription factDesc = (DFAgentDescription)fact;

	  // Match name
	  AID id1 = templateDesc.getName();
	  if(id1 != null) {
	    AID id2 = factDesc.getName();
	    if((id2 == null) || (!matchAID(id1, id2)))
	      return false;
	  }

	  // Match protocol set
	  Iterator itTemplate = templateDesc.getAllProtocols();
	  while(itTemplate.hasNext()) {
	    String templateProto = (String)itTemplate.next();
	    boolean found = false;
	    Iterator itFact = factDesc.getAllProtocols();
	    while(!found && itFact.hasNext()) {
	      String factProto = (String)itFact.next();
	      found = templateProto.equalsIgnoreCase(factProto);
	    }
	    if(!found)
	      return false;
	  }

	  // Match ontologies set
	  itTemplate = templateDesc.getAllOntologies();
	  while(itTemplate.hasNext()) {
	    String templateOnto = (String)itTemplate.next();
	    boolean found = false;
	    Iterator itFact = factDesc.getAllOntologies();
	    while(!found && itFact.hasNext()) {
	      String factOnto = (String)itFact.next();
	      found = templateOnto.equalsIgnoreCase(factOnto);
	    }
	    if(!found)
	      return false;
	  }

	  // Match languages set
	  itTemplate = templateDesc.getAllLanguages();
	  while(itTemplate.hasNext()) {
	    String templateLang = (String)itTemplate.next();
	    boolean found = false;
	    Iterator itFact = factDesc.getAllLanguages();
	    while(!found && itFact.hasNext()) {
	      String factLang = (String)itFact.next();
	      found = templateLang.equalsIgnoreCase(factLang);
	    }
	    if(!found)
	      return false;
	  }

	  // Match services set
	  itTemplate = templateDesc.getAllServices();
	  while(itTemplate.hasNext()) {
	    ServiceDescription templateSvc = (ServiceDescription)itTemplate.next();
	    boolean found = false;
	    Iterator itFact = factDesc.getAllServices();
	    while(!found && itFact.hasNext()) {
	      ServiceDescription factSvc = (ServiceDescription)itFact.next();
	      found = matchServiceDesc(templateSvc, factSvc);
	    }
	    if(!found)
	      return false;
	  }

	  return true;
	}
	catch(ClassCastException cce) {
	  return false;
	}
      }
    };


private void DFRegister(DFAgentDescription dfd) throws FIPAException {
    
    checkMandatorySlots(FIPAAgentManagementOntology.REGISTER, dfd);
    
    Object old = agentDescriptions.register(dfd.getName(), dfd);
    if(old != null)
      throw new AlreadyRegistered();

    if (isADF(dfd)) {
    	children.add(dfd.getName());
    	try {
    		gui.addChildren(dfd.getName());
    	} catch (Exception ex) {}
    }
    try{ //refresh the GUI if shown, exception thrown if the GUI was not shown
    		 gui.addAgentDesc(dfd.getName());
    		 gui.showStatusMsg("Registration of agent: " + dfd.getName().getName() + " done.");
    }catch(Exception ex){}
    
  }


  private void DFDeregister(DFAgentDescription dfd) throws FIPAException {
    checkMandatorySlots(FIPAAgentManagementOntology.DEREGISTER, dfd);

    Object old = agentDescriptions.deregister(dfd.getName());
    if(old == null)
      throw new NotRegistered();

    if (children.remove(dfd.getName()))
    	try {
    		gui.removeChildren(dfd.getName());
    	} catch (Exception e) {}
    try{ //refresh the GUI if shown, exception thrown if the GUI was not shown
      // this refresh must be here, otherwise the GUI is not synchronized with 
      // registration/deregistration made without using the GUI
      gui.removeAgentDesc(dfd.getName(),df.this.getAID());
      gui.showStatusMsg("Deregistration of agent: " + dfd.getName().getName() +" done.");
    }catch(Exception e1){}	
  }
    
  private void DFModify(DFAgentDescription dfd) throws FIPAException {
    checkMandatorySlots(FIPAAgentManagementOntology.MODIFY, dfd);

    Object old = agentDescriptions.deregister(dfd.getName());
    if(old == null)
      throw new NotRegistered();
    agentDescriptions.register(dfd.getName(), dfd);    
    try{
    	gui.removeAgentDesc(dfd.getName(), df.this.getAID());
    	gui.addAgentDesc(dfd.getName());
    	gui.showStatusMsg("Modify of agent: "+dfd.getName().getName() + " done.");
    }catch(Exception e){}

  }

  private List DFSearch(DFAgentDescription dfd, SearchConstraints constraints, ACLMessage reply) throws FIPAException {
    // Search has no mandatory slots
  	
    return agentDescriptions.search(dfd);
    
  }
	
	// AGENT DATA MODIFICATIONS FOLLOWING GUI EVENTS
	protected void onGuiEvent(GuiEvent ev)
	{
		try
		{
		
			switch(ev.getType()) 
			{
			case DFGUIAdapter.EXIT:
				gui.disposeAsync();
				gui = null;
				doDelete();
				break;
			case DFGUIAdapter.CLOSEGUI:
				gui.disposeAsync();
				gui = null;
				break;
			case DFGUIAdapter.REGISTER:
		
				if (ev.getParameter(0).equals(getName()) || ev.getParameter(0).equals(getLocalName())) 
				{
					// Register an agent with this DF
						DFRegister((DFAgentDescription)ev.getParameter(1));
					
				}
				else 
				{
				  // Register an agent with another DF. 
				  try
				    {
				      gui.showStatusMsg("Process your request & waiting for result...");
				      addBehaviour(new GUIRequestDFServiceBehaviour((AID)ev.getParameter(0),FIPAAgentManagementOntology.REGISTER,(DFAgentDescription)ev.getParameter(1),null,gui));
				    }catch (FIPAException fe) {
				      fe.printStackTrace(); //it should never happen
				    } catch(Exception ex){} //Might happen if the gui has been closed
				}
				break;
			case DFGUIAdapter.DEREGISTER:

				if(ev.getParameter(0).equals(getName()) || ev.getParameter(0).equals(getLocalName())) 
				{
					// Deregister an agent with this DF
					DFDeregister((DFAgentDescription)ev.getParameter(1));
					
				}
				else 
				{
					// Deregister an agent with another DF. 
				try
		 		{
		  	   gui.showStatusMsg("Process your request & waiting for result...");
		  		 addBehaviour(new GUIRequestDFServiceBehaviour((AID)ev.getParameter(0),FIPAAgentManagementOntology.DEREGISTER,(DFAgentDescription)ev.getParameter(1),null,gui));
		 		}catch (FIPAException fe) {
		 			fe.printStackTrace(); //it should never happen
		 			} catch(Exception ex){} //Might happen if the gui has been closed
				}
				break;
			case DFGUIAdapter.MODIFY:
				
				if(ev.getParameter(0).equals(getName()) || ev.getParameter(0).equals(getLocalName())) 
				{
					// Modify the description of an agent with this DF
					DFModify((DFAgentDescription)ev.getParameter(1));
					
				}
				else 
				{
					// Modify the description of an agent with another DF
					try{
						gui.showStatusMsg("Process your request & waiting for result..");
						addBehaviour(new GUIRequestDFServiceBehaviour((AID)ev.getParameter(0), FIPAAgentManagementOntology.MODIFY, (DFAgentDescription)ev.getParameter(1),null,gui));
					}catch(FIPAException fe1){
						fe1.printStackTrace();
					}//it should never happen
		 			catch(Exception ex){} //Might happen if the gui has been closed
				}
				break;
		  case DFGUIAdapter.SEARCH:
		  	 
		  	try{
		  		gui.showStatusMsg("Process your request & waiting for result...");
	  		  addBehaviour(new GUIRequestDFServiceBehaviour((AID)ev.getParameter(0),FIPAAgentManagementOntology.SEARCH,(DFAgentDescription)ev.getParameter(1),(SearchConstraints)ev.getParameter(2),gui));
	  	  }catch(FIPAException fe){
	  	   fe.printStackTrace();
	  	  }catch(Exception ex1){} //Might happen if the gui has been closed.
		  	 
		  	break;
		 	case DFGUIAdapter.FEDERATE:
		 		try
		 		{
		  	   gui.showStatusMsg("Process your request & waiting for result...");
		  	   
		  	   if(ev.getParameter(0).equals(getAID()) || ev.getParameter(1).equals(getLocalName()))
		 	  		gui.showStatusMsg("Self Federation not allowed");
		  		else
		  		addBehaviour(new GUIRequestDFServiceBehaviour((AID)ev.getParameter(0),FIPAAgentManagementOntology.REGISTER,(DFAgentDescription)ev.getParameter(1),null,gui));
		 		}catch (FIPAException fe) {
		 			fe.printStackTrace(); //it should never happen
		 			} catch(Exception ex){} //Might happen if the gui has been closed
		  	  
		  	
		 		break;
		 	
		 
			} // END of switch
		} // END of try
		catch(FIPAException fe) 
		{
			fe.printStackTrace();
		
		}
	}

	
	/**
	This method returns the descriptor of an agent registered with the df.
	*/
	public DFAgentDescription getDFAgentDsc(AID name) throws FIPAException
	{
	  DFAgentDescription template = new DFAgentDescription();
	  template.setName(name);
	  List l = agentDescriptions.search(template);
	  if(l.isEmpty())
	    return null;
	  else
	    return (DFAgentDescription)l.get(0);
	}

	/**
  * This method creates the DFAgent descriptor for this df used to federate with other df.
	*/
	private DFAgentDescription getDefaultDescription()
	{
	  	DFAgentDescription out = new DFAgentDescription();
	
			out.setName(getAID());
			out.addOntologies(FIPAAgentManagementOntology.NAME);
			out.addLanguages(SL0Codec.NAME);
			out.addProtocols("fipa-request");
			ServiceDescription sd = new ServiceDescription();
			sd.setName("df-service");
			sd.setType("fipa-df");
			sd.addOntologies(FIPAAgentManagementOntology.NAME);
			sd.addLanguages(SL0Codec.NAME);
			sd.addProtocols("fipa-request");
      try{
		  	sd.setOwnership(InetAddress.getLocalHost().getHostName());
		  }catch (java.net.UnknownHostException uhe){
		  	sd.setOwnership("unknown");}
		  
		  out.addServices(sd);
		  
		  return out;
	}

	
	/*
	* This method set the description of the df according to the DFAgentAgentDescription passed.
	* The programmers can call this method to provide a different initialization of the description of the df they are implemented.
	* The method is called inside the setup of the agent and set the df description using a default description.
	*/
	public void setDescriptionOfThisDF(DFAgentDescription dfd)
	{
		thisDF = dfd;
	}
	/**
	* This method returns the current description of this DF
	*/
	public DFAgentDescription getDescriptionOfThisDF()
	{
	    return thisDF;
	}
	
	/*
	* This method returns the description of this df used to federate with the given parent
	*/
	public DFAgentDescription getDescriptionOfThisDF(AID parent)
	{
		return (DFAgentDescription)dscDFParentMap.get(parent);
	}
	
	/*
	* This method can be used to add a parent (a DF with which the this DF is federated). 
	* @param dfName the parent df (the df with which this df has been registered)
	* @param dfd the description used by this df to register with the parent.
	*/
	public void addParent(AID dfName, DFAgentDescription dfd)
	{
	  parents.add(dfName);
	  gui.addParent(dfName);
    dscDFParentMap.put(dfName,dfd); //update the table of corrispondence between parents and description of this df used to federate.

	}
	
	/**
	this method can be used to remove a parent (a DF with which this DF is federated).
	*/
	public void removeParent(AID dfName)
	{
		parents.remove(dfName); 
		gui.removeParent(dfName);
		dscDFParentMap.remove(dfName);

	}

	
}