/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * Copyright (C) 2001 Motorola.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.imtp.leap.JICP;

//#MIDP_EXCLUDE_FILE

import jade.mtp.TransportAddress;
import jade.imtp.leap.*;
import jade.util.Logger;
import jade.util.leap.Properties;

import java.io.*;
import java.net.*;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 * @author Ronnie Taib - Motorola
 * @author Nicolas Lhuillier - Motorola
 * @author Steffen Rusitschka - Siemens
 */
public class JICPServer extends Thread implements PDPContextManager.Listener {
	private static final String LEAP_PROPERTIES_FILE = "leap.properties";
	private static final String PDP_CONTEXT_MANAGER_CLASS = "pdp-context-manager";
	
	private static final int LISTENING = 0;
	private static final int TERMINATING = 1;
 
	private int      state = LISTENING;
	
  private ServerSocket server;
  private ICP.Listener cmdListener;
  
  private int            mediatorCnt = 1;
  private Hashtable      mediators = new Hashtable();
  private Properties     leapProps = new Properties();
  private PDPContextManager  myPDPContextManager;
  
  private int handlersCnt = 0;
  private int maxHandlers;

  private ConnectionFactory connFactory;
  
  private InetAddress localHost;

	private Logger myLogger;
	
  /**
   * Constructor declaration
   */
  public JICPServer(int port, boolean changePortIfBusy, ICP.Listener l, ConnectionFactory f, int max) throws ICPException {
    cmdListener = l;
		connFactory = f;
		maxHandlers = max;
  	myLogger = new Logger("JICPServer", 2);
		
		// Read the LEAP configuration properties
		try {
			leapProps.load(LEAP_PROPERTIES_FILE);
		}
		catch (Exception e) {
			// Ignore: no back end properties specified
		}
		
		// Initialize the PDPContextManager if specified
		String pdpContextManagerClass = leapProps.getProperty(PDP_CONTEXT_MANAGER_CLASS);
		if (pdpContextManagerClass != null) {
			try {
				myPDPContextManager = (PDPContextManager) Class.forName(pdpContextManagerClass).newInstance();
				myPDPContextManager.init(leapProps); 
				myPDPContextManager.registerListener(this);
			}
			catch (Throwable t) {
				t.printStackTrace();
				myPDPContextManager = null;
			}
		}
		
    try {
    	localHost = InetAddress.getLocalHost(); 
      server = new ServerSocket(port);
    } 
    catch (IOException ioe) {
    	if (changePortIfBusy) {
    		// The specified port is busy. Let the system find a free one
    		try {
      		server = new ServerSocket(0);
    		}
    		catch (IOException ioe2) {
      		throw new ICPException("Problems initializing server socket. No free port found");
				}
    	}
    	else {
	      throw new ICPException("I/O error opening server socket on port "+port);
    	}
    } 

    setDaemon(true);
    setName("JICPServer-"+getLocalPort());
  }

  public int getLocalPort() {
  	return server.getLocalPort();
  }
  
  /**
     Shut down this JICP server
   */
  public synchronized void shutdown() {
	  myLogger.log("Shutting down JICPServer...", 3);
    state = TERMINATING;

    try {
      // Force the listening thread (this) to exit from the accept()
      // Calling this.interrupt(); should be the right way, but it seems
      // not to work...so do that by closing the server socket.
      server.close();
      
      // Wait for the listening thread to complete
      this.join();
    } 
    catch (IOException ioe) {
      ioe.printStackTrace();
    } 
    catch (InterruptedException ie) {
      ie.printStackTrace();
    } 
  } 
	
  /**
   * JICPServer thread entry point. Accept incoming connections 
   * and for each of them start a ConnectionHandler that handles it. 
   */
  public void run() {
    while (state != TERMINATING) {
      try {
      	// Accept connection
        Socket s = server.accept();
        InetAddress addr = s.getInetAddress();
        int port = s.getPort();
        myLogger.log("Incoming connection from "+addr+":"+port, 4);
        Connection c = connFactory.createConnection(s);
        new ConnectionHandler(c, addr, port).start();    // start a handler and go back to listening
      } 
      catch (InterruptedIOException e) {
        // These can be generated by socket timeout (just ignore
        // the exception) or by a call to the shutdown()
        // method (the state has been set to TERMINATING and the
        // server will exit).
      } 
      catch (Exception e) {
      	if (state == LISTENING) {
          myLogger.log("Problems accepting a new connection", 1);
          e.printStackTrace();

          // Stop listening
          state = TERMINATING;
      	}
      } 
    } // END of while(listen) 

		myLogger.log("JICPServer terminated", 2);
		
    // release socket
    try {
      server.close();
    } 
    catch (IOException io) {
      myLogger.log("I/O error closing the server socket", 1);
      io.printStackTrace();
    } 

    server = null;

    // Close all mediators
    Enumeration e = mediators.elements();
    while (e.hasMoreElements()) {
      JICPMediator m = (JICPMediator) e.nextElement();
      m.kill();
    } 
    mediators.clear();
  } 

  /**
   * Called by a Mediator to notify that it is no longer active
   */
  public void deregisterMediator(String id) {
    mediators.remove(id);
  } 
  
  /**
     Called by the JICPPeer ticker at each tick
   */
  public void tick(long currentTime) {
  	synchronized (mediators) {
	    Enumeration e = mediators.elements();
	    while (e.hasMoreElements()) {
	      JICPMediator m = (JICPMediator) e.nextElement();
	      m.tick(currentTime);
	    }
  	}
  }
  
  /**
     Called by the PDPContextManager (if any)
   */
  public void handlePDPContextClosed(String id) {
  	// FIXME: to be implemented
  }
  	
  /**
     Inner class ConnectionHandler.
     Handle a connection accepted by this JICPServer
   */
  class ConnectionHandler extends Thread {
    private Connection c;
    private InetAddress addr;
    private int port;

    /**
     * Constructor declaration
     * @param s
     */
    public ConnectionHandler(Connection c, InetAddress addr, int port) {
      this.c = c;
      this.addr = addr;
      this.port = port;
    }

    /**
     * Thread entry point
     */
    public void run() {
    	handlersCnt++;
    	
	    myLogger.log("CommandHandler started", 4);
      boolean closeConnection = true;
      int status = 0;
      byte type = (byte) 0;
      try {
	      boolean loop = false;
      	do {
	        // Read the incoming JICPPacket
	        JICPPacket pkt = c.readPacket();
	        JICPPacket reply = null;
	        status = 1;
	        	
	        type = pkt.getType();
	        switch (type) {
	        case JICPProtocol.COMMAND_TYPE:
	        case JICPProtocol.RESPONSE_TYPE:
	          // Get the right recipient and let it process the command.
	          String recipientID = pkt.getRecipientID();
	          if (recipientID != null) {
	            // The recipient is one of the mediators
	            JICPMediator m = (JICPMediator) mediators.get(recipientID);
	            if (m != null) {
		          	myLogger.log("Passing incoming packet to mediator "+recipientID, 4);
	              reply = m.handleJICPPacket(pkt, addr, port);
	            } 
	            else {
	            	// If the packet is a response we don't need to reply
	          		if (type == JICPProtocol.COMMAND_TYPE) { 
	              	reply = new JICPPacket("Unknown recipient "+recipientID, null);
	          		}
	            } 
	          } 
	          else {
	          	// The recipient is my ICP.Listener (the local CommandDispatcher)
	          	loop = true;
	          	if (type == JICPProtocol.COMMAND_TYPE) { 
	          		myLogger.log("Passing incoming COMMAND to local listener", 4);
		            byte[] rsp = cmdListener.handleCommand(pkt.getData());
		            byte dataInfo = JICPProtocol.DEFAULT_INFO;
		            if (handlersCnt >= maxHandlers) {
		            	// Too many connections open --> close the connection as soon as the command has been served
		            	dataInfo |= JICPProtocol.TERMINATED_INFO;
		            	loop = false;
		            }
		            reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, dataInfo, rsp);
	          	}
	            if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
	            	loop = false;
	            }
	          } 
	          break;
	
	        case JICPProtocol.GET_ADDRESS_TYPE:
	          // Respond sending back the caller address
	          myLogger.log("Received a GET_ADDRESS request from "+addr+":"+port, 2);
	          reply = new JICPPacket(JICPProtocol.GET_ADDRESS_TYPE, JICPProtocol.DEFAULT_INFO, addr.getHostAddress().getBytes());
	          break;

			  	//#PJAVA_EXCLUDE_BEGIN
	        case JICPProtocol.CREATE_MEDIATOR_TYPE:

	          // Starts a new Mediator and sends back its ID
	          String s = new String(pkt.getData());
	          Properties p = parseProperties(s);
	          
	          // If there is a PDPContextManager add the PDP context properties
	          if (myPDPContextManager != null) {
	          	Properties pdpContextInfo = myPDPContextManager.getPDPContextInfo(addr);
	          	if (pdpContextInfo != null) {
		          	mergeProperties(p, pdpContextInfo);
	          	}
	          	else {
	          		reply = new JICPPacket("Not authorized", null);
	          		break;
	          	}
	          }

					  // Get mediator ID from the passed properties (if present)
					  String id = p.getProperty(JICPProtocol.MEDIATOR_ID_KEY);
					  if(id != null) {
					  	// An existing front-end whose back-end was lost 
					  	p.setProperty(jade.core.BackEndContainer.RESYNCH, "true");
					  }
					  else {
					  	// Use the MSISDN (if present) 
					  	id = p.getProperty(PDPContextManager.MSISDN);
					  	if (id == null) {
					      // Construct a default id using the string representation of the server's TCP endpoint
					      id = "BE-"+localHost.getHostName() + ':' + server.getLocalPort() + '-' + String.valueOf(mediatorCnt++);
					  	}
					  }

	          myLogger.log("Received a CREATE_MEDIATOR request from "+ addr + ":" + port + ". ID is [" + id + "]", 2);

	          // Start the mediator
	          JICPMediator m = startMediator(id, p);
		  			m.handleIncomingConnection(c, pkt, addr, port);
	          mediators.put(id, m);
	          
	          // Create an ad-hoc reply including the assigned mediator-id and the IP address
	          String replyMsg = id+'#'+addr.getHostAddress();
	          reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, replyMsg.getBytes());
	        	closeConnection = false;
	        	break;
	
	        case JICPProtocol.CONNECT_MEDIATOR_TYPE:
	          // A mediated container is (re)connecting to its mediator
	          recipientID = pkt.getRecipientID();
	          
	          // FIXME: If there is a PDPContextManager  check that the recipientID is the MSISDN
	          
	          myLogger.log("Received a CONNECT_MEDIATOR request from "+addr+":"+port+". Mediator ID is "+recipientID, 2);
	          m = (JICPMediator) mediators.get(recipientID);
	          if (m != null) {
	          	// Don't close the connection, but pass it to the proper 
	          	// mediator. 
	          	m.handleIncomingConnection(c, pkt, addr, port);
	          	closeConnection = false;
		          reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, addr.getHostAddress().getBytes());
	          }
	          else {
	          	reply = new JICPPacket("Mediator "+recipientID+" not found", null);
	          }
	          break;
			  		//#PJAVA_EXCLUDE_END
	          
	        default:
	          // Send back an error response
	          myLogger.log("Uncorrect JICP data type: "+pkt.getType(), 1);
	          reply = new JICPPacket("Uncorrect JICP data type: "+pkt.getType(), null);
	        }
	        status = 2;
	
	        // Send the actual response data
	        if (reply != null) {
		        //reply.writeTo(out);
	        	c.writePacket(reply);
	        }
	        status = 3;
      	} while (loop); 
      } 
      catch (Exception e) {
      	switch (status) {
      	case 0:
	        myLogger.log("Communication error reading incoming packet from "+addr+":"+port, 1);
        	e.printStackTrace();
	        break;
	      case 1:
	      	myLogger.log("Error handling incoming packet", 1);
        	e.printStackTrace();
	      	// If the incoming packet was a command, try 
        	// to send back a generic error response
	        if (type == JICPProtocol.COMMAND_TYPE && c != null) {
	          try {
	            c.writePacket(new JICPPacket("Unexpected error", e));
	          } 
	          catch (IOException ioe) {   
	          	// Just print a warning
	          	myLogger.log("Can't send back error indication "+ioe, 1);
	          } 
	        }
	      	break;
	      case 2:
	      	myLogger.log("Communication error writing return packet to "+addr+":"+port, 1);
        	e.printStackTrace();
	      	break;
	      case 3:
	      	// This is a re-used connection waiting for the next incoming packet
	      	if (e instanceof EOFException) {
	      		myLogger.log("Client "+addr+":"+port+" has closed the connection.", 3);
	      	}
	      	else {
	      		myLogger.log("Unexpected client "+addr+":"+port+" termination. "+e.toString(), 3);
	      	}
      	}
      } 
      finally {
        try {
          if (closeConnection) {
            // Close connection
		        if (!addr.equals(localHost)) {
	          	myLogger.log("Closing connection with "+addr+":"+port, 4);
		        }
          	c.close();
          } 
        } 
        catch (IOException io) {
          myLogger.log("I/O error while closing the connection", 1);
          io.printStackTrace();
        } 
      	handlersCnt--;
      } 
    } 
  } // END of inner class ConnectionHandler

  private Properties parseProperties(String s) throws ICPException {
  	StringTokenizer st = new StringTokenizer(s, "=#");
  	Properties p = new Properties();
  	while (st.hasMoreTokens()) {
  		String key = st.nextToken();
  		if (!st.hasMoreTokens()) {
  			throw new ICPException("Wrong initialization properties format.");
  		}
  		p.setProperty(key, st.nextToken());
  	}
  	return p;
  }
  
  private void mergeProperties(Properties p1, Properties p2) {
		Enumeration e = p2.propertyNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			p1.setProperty(key, p2.getProperty(key));
		}
  }
  
  private JICPMediator startMediator(String id, Properties p) throws Exception {
		String className = p.getProperty(JICPProtocol.MEDIATOR_CLASS_KEY);
		if (className != null) {
  		JICPMediator m = (JICPMediator) Class.forName(className).newInstance();
  		mergeProperties(p, leapProps);
  		m.init(this, id, p);
  		return m;
		}
		else {
			throw new ICPException("No JICPMediator class specified.");
		}
  }
}

