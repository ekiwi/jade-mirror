/*
  $Log$
  Revision 1.2  1999/09/03 10:42:49  rimassa
  Some minor graphical adjustments.

  Revision 1.1  1999/09/02 15:06:10  rimassa
  Abstract interface for GUI/DF interaction.

  Revision 1.12  1999/07/02 14:34:16  bellifemine
  Versione 1.0 di JADE

  Revision 1.8  1999/06/22 13:16:17  rimassa
  Added a method to perform asynchronous disposal of the DF GUI.

  Revision 1.7  1999/04/06 16:11:46  rimassa
  Reimplemented DF GUI using Swing.

  Revision 1.4  1999/03/14 17:45:36  rimassa
  Decoupled event handler thread from DF agent thread using an event
  queue, avoiding deadlock on agent registration through GUI.

  Revision 1.3  1999/02/14 23:19:22  rimassa
  Changed getName() calls to getLocalName() where appropriate.

  Revision 1.2  1999/02/04 13:25:02  rimassa
  Removed some debugging code.

  Revision 1.1  1999/02/03 15:36:55  rimassa
  A class working as a GUI for DF agents.

*/

package jade.gui;

import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.io.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

//import jade.core.Agent;
//import jade.domain.*; 
//import jade.lang.acl.*;
import jade.domain.AgentManagementOntology; 
import jade.gui.GUI2DFCommunicatorInterface;

public class DFGUI extends JFrame {

  GUI2DFCommunicatorInterface agent;

    // Constants used by subFrames
  final static int REGISTER=0;  
  final static int VIEW=1;
  final static int MODIFY=2;
  final static int DEREGISTER=3;

  private Container cont;

  public Component add(Component c) {
    if(cont == null)
      cont = getContentPane();
    return cont.add(c);
  }//add(Component)

//   public void setLayout(LayoutManager l) {
//     if(cont == null)
//       cont = getContentPane();
//     cont.setLayout(l);
//   }//add(Component)

  public DFGUI() {
    // This code is automatically generated by Visual Cafe when you add
    // components to the visual environment. It instantiates and initializes
    // the components. To modify the code, only use code syntax that matches
    // what Visual Cafe can generate, or Visual Cafe may be unable to back
    // parse your Java file into its visual environment.
	
    //{{INIT_CONTROLS
    getContentPane().setLayout(null);
    setVisible(false);
    setSize(405,305);
    textFieldDFname = new JTextField();
    textFieldDFname.setVisible(false);
    textFieldDFname.setBounds(12,60,348,24);
    add(textFieldDFname);
    labelDFname = new JLabel("Insert the agent name of the DF you want to register with");
    labelDFname.setVisible(false);
    labelDFname.setBounds(12,12,348,40);
    add(labelDFname);
    buttonDFname = new JButton();
    buttonDFname.setText("OK");
    buttonDFname.setVisible(false);
    buttonDFname.setBounds(144,96,60,40);
    buttonDFname.setBackground(new Color(12632256));
    add(buttonDFname);
    textFieldErrMsg = new JTextField();
    textFieldErrMsg.setEditable(false);
    textFieldErrMsg.setVisible(false);
    textFieldErrMsg.setBounds(12,204,348,60);
    textFieldErrMsg.setFont(new Font("Dialog", Font.ITALIC, 10));
    textFieldErrMsg.setBackground(new Color(-71));
    add(textFieldErrMsg);
    setTitle("DF");
    //}}
    
    //{{INIT_MENUS
    mainMenuBar = new JMenuBar();
    menu1 = new JMenu("Commands");
    miView = new JMenuItem("View Registered Agent Descriptions");
    menu1.add(miView);
    miRegister = new JMenuItem("Register New Agent Description");
    menu1.add(miRegister);
    miModify = new JMenuItem("Modify a registered Agent Description");
    menu1.add(miModify);
    miDeregister = new JMenuItem("Deregister an Agent Description");
    menu1.add(miDeregister);
    miRegisterDF = new JMenuItem("Register this DF with another DF");
    menu1.add(miRegisterDF);
    menu1.addSeparator();
    miExit = new JMenuItem("Kill the DF Agent");
    menu1.add(miExit);
    mainMenuBar.add(menu1);
    menu3 = new JMenu("Help");
    try {
      mainMenuBar.setHelpMenu(menu3);
    } catch(Throwable t) {
    }//catch
    setJMenuBar(mainMenuBar);
    //$$ mainMenuBar.move(4,277);
    //}}
		
    //{{REGISTER_LISTENERS
    SymWindow aSymWindow = new SymWindow();
    this.addWindowListener(aSymWindow);
    SymAction lSymAction = new SymAction();
    miView.addActionListener(lSymAction);
    miRegister.addActionListener(lSymAction);
    miModify.addActionListener(lSymAction);
    miDeregister.addActionListener(lSymAction);
    miRegisterDF.addActionListener(lSymAction);
    miExit.addActionListener(lSymAction);

    SymMouse aSymMouse = new SymMouse();
    buttonDFname.addMouseListener(aSymMouse);
    //}}
  }//Constructor()
	
  public DFGUI(String title) {
    this();
    setTitle(title);
  }//Constructor(String)

  public DFGUI(GUI2DFCommunicatorInterface a) {
    this("DF " + a.getLocalName());
    agent = a;
  }//Constructor(df)
	
    /**
     * Shows or hides the component depending on the boolean flag b.
     * @param b  if true, show the component; otherwise, hide the component.
     * @see java.awt.Component#isVisible
     */
  public void setVisible(boolean b) {
    if(b) {
      setLocation(50, 50);
    }//if
    super.setVisible(b);
  }//setVisible(boolean)


  // Perform asynchronous disposal to avoid nasty InterruptedException
  // printout.
  public void disposeAsync() {

    class disposeIt implements Runnable {
      private Window toDispose;

      public disposeIt(Window w) {
	toDispose = w;
      }

      public void run() {
	toDispose.dispose();
      }

    }

    // Make AWT Event Dispatcher thread dispose RMA window for us.
    EventQueue.invokeLater(new disposeIt(this));

  }

  static public void main(String args[]) {
    (new DFGUI()).setVisible(true);
  }//main(String[])

  public void addNotify() {
    // Record the size of the window prior to calling parents addNotify.
    Dimension d = getSize();

    super.addNotify();

    if(fComponentsAdjusted)
      return;

    // Adjust components according to the insets
    setSize(getInsets().left + getInsets().right + d.width, getInsets().top + getInsets().bottom + d.height);
    Component components[] = getComponents();
    for (int i = 0; i < components.length; i++) {
      Point p = components[i].getLocation();
      p.translate(getInsets().left, getInsets().top);
      components[i].setLocation(p);
    }//for
    fComponentsAdjusted = true;
  }//addNotify()

  // Used for addNotify check.
  boolean fComponentsAdjusted = false;


  //{{DECLARE_MENUS
  JMenuBar mainMenuBar;
  JMenu menu1, menu3;
  JMenuItem miView, miRegister, miModify, miDeregister, miRegisterDF, miExit;
  //}}



  class SymWindow extends java.awt.event.WindowAdapter {
    public void windowClosing(java.awt.event.WindowEvent event) {
      Object object = event.getSource();
      if(object == DFGUI.this)
	DFGUI_WindowClosing(event);
    }//windowClosing(WindowEvent)
  }

  void DFGUI_WindowClosing(java.awt.event.WindowEvent event) {
    dispose();
  }//DFGUI_WindowClosing(WindowEvent)
	
  class SymAction implements java.awt.event.ActionListener {
    public void actionPerformed(java.awt.event.ActionEvent event) {
      textFieldErrMsg.setVisible(false);  
      Object object = event.getSource();
      if(object == miView)
	miView_Action(event);
      else if(object == miRegister)
	miRegister_Action(event);
      else if(object == miModify)
	miModify_Action(event);
      else if(object == miDeregister)
	miDeregister_Action(event);
      else if(object == miRegisterDF)
	miRegisterDF_Action(event);
      else if(object == miExit)
	miExit_Action(event);
    }//if
  }//actionPerformed(ActionEvent)


  void miExit_Action(java.awt.event.ActionEvent event) {
    // FIXME: add a window to ask are you sure the default DF is mandatory!?
    agent.doDelete();
    dispose();
  }
	
  void miView_Action(java.awt.event.ActionEvent event) {
    if(agent.getDFAgentDescriptors().hasMoreElements())
      (new agentDescriptionFrame("VIEW REGISTERED AGENT DESCRIPTIONS", agent.getDFAgentDescriptors(), VIEW ,agent)).setVisible(true); 
    else showErrorMsg("No data have yet been registered with the DF. Nothing to view");
  }

 public void showErrorMsg(String s) {
    textFieldErrMsg.setText(s);
    textFieldErrMsg.setVisible(true);
 }

 void miModify_Action(java.awt.event.ActionEvent event) {
   showErrorMsg("NOT YET IMPLEMENTED");
   /*
    if(agent.getDFAgentDescriptors().hasMoreElements())
      (new agentDescriptionFrame("MODIFY REGISTERED AGENT DESCRIPTIONS", agent.getDFAgentDescriptors(), MODIFY ,agent)).setVisible(true); 
    else System.err.println("No data have yet been registered with the DF. Nothing to modify");
    */
  }

 void miDeregister_Action(java.awt.event.ActionEvent event) {
    if(agent.getDFAgentDescriptors().hasMoreElements())
      (new agentDescriptionFrame("DEREGISTER REGISTERED AGENT DESCRIPTIONS", agent.getDFAgentDescriptors(), DEREGISTER ,agent)).setVisible(true); 
    else showErrorMsg("No data have yet been registered with the DF. Nothing to deregister");
  }

 void miRegister_Action(java.awt.event.ActionEvent event) {
    (new agentDescriptionFrame("REGISTER NEW AGENT DESCRIPTION", null, REGISTER,agent)).setVisible(true); 
  }

 void miRegisterDF_Action(java.awt.event.ActionEvent event) {
    labelDFname.setVisible(true);
    textFieldDFname.setVisible(true);
    buttonDFname.setVisible(true);
    textFieldDFname.requestFocus();
 }//miRegisterDF_Action(ActionEvent)


  //{{DECLARE_CONTROLS
  JTextField textFieldDFname, textFieldErrMsg;
  JLabel labelDFname;
  JButton buttonDFname;
  //}}

  class SymMouse extends java.awt.event.MouseAdapter {
    public void mouseClicked(java.awt.event.MouseEvent event) {
      textFieldErrMsg.setVisible(false);
      Object object = event.getSource();
      if(object == buttonDFname)
        buttonDFname_MouseClicked(event);
    }//mouseClicked(MouseEvent)
  }//class SymMouse

  void buttonDFname_MouseClicked(java.awt.event.MouseEvent event) {
    // to do: code goes here.
    // Registration of this DF with another DF
    AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();
    dfd.setName(agent.getName());
    dfd.addAddress(agent.getAddress());
    dfd.setType("DF");
    dfd.addInteractionProtocol("fipa-request");
    dfd.setOntology("fipa-agent-management");
    try {
      dfd.setOwnership(InetAddress.getLocalHost().getHostName());
    } catch(UnknownHostException uhe) {
    }//catch
    dfd.setDFState("active");

    AgentManagementOntology.ServiceDescriptor sd  = new AgentManagementOntology.ServiceDescriptor(); 
    sd.setName("Federated-DF"); 
    sd.setType("fipa-df");
    sd.setOntology("fipa-agent-management");
    sd.setFixedProps("(list (implemented-by CSELT) (version 0.94))");
    dfd.addAgentService(sd);

    String parentName = textFieldDFname.getText(); 
    agent.postRegisterEvent(parentName, dfd);

    //{{CONNECTION
    // Hide the TextField
    textFieldDFname.setVisible(false);
    // Hide the Label
    labelDFname.setVisible(false);
    // Hide the Button
    buttonDFname.setVisible(false);
    //}}
  }//buttonDFname_MouseClicked(MouseEvent)
}



class agentDescriptionFrame extends JFrame {

  private Container cont;
  public Component add(Component c) {
    if(cont == null)
      cont = getContentPane();
    return cont.add(c);
  }//Component add(Component)

  public agentDescriptionFrame() {
    // This code is automatically generated by Visual Cafe when you add
    // components to the visual environment. It instantiates and initializes
    // the components. To modify the code, only use code syntax that matches
    // what Visual Cafe can generate, or Visual Cafe may be unable to back
    // parse your Java file into its visual environment.

    //{{INIT_CONTROLS
    cont = getContentPane();
    cont.setLayout(new GridLayout(0, 4));
    setVisible(false);
    add(new JLabel(""));
    label1 = new JLabel("DF AGENT ", JLabel.RIGHT);
    add(label1);
    add(new JLabel("DESCRIPTION", JLabel.LEFT));
    add(new JLabel(""));
    label2 = new JLabel("agent-name:", JLabel.RIGHT);
    add(label2);
    textField1 = new JTextField();
    add(textField1);
    label3 = new JLabel("agent-type:", JLabel.RIGHT);
    add(label3);
    textField2 = new JTextField();
    add(textField2); // agent-type
    label4 = new JLabel("agent-address:", JLabel.RIGHT);
    add(label4);
    textField3 = new JTextField();
    add(textField3); // agent-address
    label5 = new JLabel("ownership:", JLabel.RIGHT);
    add(label5);
    textField4 = new JTextField();
    add(textField4);
    label6 = new JLabel("df-state:", JLabel.RIGHT);
    add(label6);
    textField5 = new JTextField();
    add(textField5); // df-state
    label7 = new JLabel("language:", JLabel.RIGHT);
    add(label7);
    textField6 = new JTextField();
    add(textField6); // language
    label8 = new JLabel("ontology:", JLabel.RIGHT);
    add(label8);
    textField7 = new JTextField();
    add(textField7); // ontology
    label9 = new JLabel("interaction-protocols:", JLabel.RIGHT);
    add(label9);
    textField14 = new JTextField();
    add(textField14); // interaction-protocols
    add(new JLabel("AGENT ", JLabel.RIGHT));
    label10 = new JLabel("SERVICES:", JLabel.LEFT);
    add(label10);
    add(new JLabel("Service no. ", JLabel.RIGHT));
    label11 = new JLabel("0 of 0", JLabel.LEFT);
    add(label11);
//     add(new JLabel(""));
    label12 = new JLabel("service-name:", JLabel.RIGHT);
    add(label12);
    textField8 = new JTextField();
    add(textField8); // service-name
    label13 = new JLabel("service-type:", JLabel.RIGHT);
    add(label13);
    textField9 = new JTextField();
    add(textField9); // service-type
    label14 = new JLabel("service-ontology:", JLabel.RIGHT);
    add(label14);
    textField10 = new JTextField();
    add(textField10); // service-ontology
    label15 = new JLabel("fixed-properties:", JLabel.RIGHT);
    add(label15);
    textField11 = new JTextField();
    add(textField11); // fixed-properties
    label16 = new JLabel("negotiable-properties:", JLabel.RIGHT);
    add(label16);
    textField12 = new JTextField();
    add(textField12);
    label17 = new JLabel("communication-properties:", JLabel.RIGHT);
    add(label17);
    textField13 = new JTextField();
    add(textField13); // communication-properties

    add(new JLabel(""));
    button1 = new JButton("NextService");
    add(button1);
    button2 = new JButton("PreviousService");
    add(button2);
    button3 = new JButton("NextAgent");
    add(button3);
    add(new JLabel(""));
    button6 = new JButton("AddNewService");
    add(button6);
    button7 = new JButton("DelThisService");
    add(button7);
    button8 = new JButton("PrevAgent");
    add(button8);

    add(new JLabel(""));
    button5 = new JButton("EXIT");
    add(button5);
    button4 = new JButton("OK");
    add(button4);

    setTitle("Agent Description");
    //}}

    //{{INIT_MENUS
    //}}

    //{{REGISTER_LISTENERS
    SymWindow aSymWindow = new SymWindow();
    this.addWindowListener(aSymWindow);
    SymMouse aSymMouse = new SymMouse();
    button1.addMouseListener(aSymMouse);
    button2.addMouseListener(aSymMouse);
    button3.addMouseListener(aSymMouse);
    button4.addMouseListener(aSymMouse);
    button5.addMouseListener(aSymMouse);
    button6.addMouseListener(aSymMouse);
    button7.addMouseListener(aSymMouse);
    button8.addMouseListener(aSymMouse);
    //}}

    pack();
  }
    
  public agentDescriptionFrame(String title) {
    this();
    setTitle(title);
  }//agentDescriptionFrame(String)

    /**
    * This constructor is used by DFGUI
    * @param title is the title of the Window
    * @param data is the structure with the data of the user
    * @param usage is one of the constants in DFGUI and indicates the usage of
    * @param thisAgent is the agent that executes this frame and it is used
    * to actually send messages
    * this DFGUI (REGISTER, MODIFY, VIEW, DEREGISTER)
    */
  public agentDescriptionFrame(String title, Enumeration data, int useFor, GUI2DFCommunicatorInterface thisAgent) {
    this(title);
    myAgent = thisAgent;
    usage = useFor;
    allData = data;
    switch (usage) {
    case DFGUI.REGISTER: {
      currentDFAgentDescriptor = new AgentManagementOntology.DFAgentDescriptor();
      button3.setVisible(false); // NextAgent
      button8.setVisible(false); // button PreviousAgent

      setServiceVisible(false);
      setAllEditable(true);
      break;
    }//DFGUI.REGISTER
    case DFGUI.MODIFY: { 
      setAllEditable(true);
      initializeDFAgentDescriptors(data);	            	   
      updateTextFields();
      if(! allDFAgentDescriptors.hasMoreElements())
        button3.setVisible(false); // button NextAgent
      if(! allServiceDescriptors.hasMoreElements())
        button1.setVisible(false); // button NextService
      button2.setVisible(false); // button PreviousService
      button8.setVisible(false); // button PreviousAgent
      break;
    }//DFGUI.MODIFY
    case DFGUI.DEREGISTER: {
      button6.setVisible(false); // AddNewService
      button7.setVisible(false); // DelThisService
      setAllEditable(false);
      initializeDFAgentDescriptors(data);	            	      
      updateTextFields();
      if(! allDFAgentDescriptors.hasMoreElements())
        button3.setVisible(false); // button NextAgent
      if(! allServiceDescriptors.hasMoreElements())
        button1.setVisible(false); // button NextService
      button2.setVisible(false); // button PreviousService
      button8.setVisible(false); // button PreviousAgent
      break;
    }//DFGUI.DEREGISTER
    case DFGUI.VIEW: {
      button4.setVisible(false);
      button6.setVisible(false); // AddNewService
      button7.setVisible(false); // DelThisService
      setAllEditable(false);
      initializeDFAgentDescriptors(data);	            	      
      updateTextFields();
      if(! allDFAgentDescriptors.hasMoreElements())
        button3.setVisible(false); // button NextAgent
      if(! allServiceDescriptors.hasMoreElements())
        button1.setVisible(false); // button NextService
      button2.setVisible(false); // button PreviousService
      button8.setVisible(false); // button PreviousAgent
      break;
    }//DFGUI.VIEW
    }//switch
    pack();
  }//Constructor(String, Enumeration, int, df)

/**
* This method initializes all the class variables related to the display of
* the DFAgentDescriptors
*/
  void initializeDFAgentDescriptors(Enumeration data) {
    allDFAgentDescriptors = data; 
    if(allDFAgentDescriptors.hasMoreElements()) {
      currentDFAgentDescriptor = (AgentManagementOntology.DFAgentDescriptor)allDFAgentDescriptors.nextElement();
      currentDFAgentDescriptorNumber++;
    }//if
    else currentDFAgentDescriptor = null;


    allServiceDescriptors = currentDFAgentDescriptor.getAgentServices();

    if(allServiceDescriptors.hasMoreElements())
      currentServiceDescriptor       = (AgentManagementOntology.ServiceDescriptor)allServiceDescriptors.nextElement();
    else currentServiceDescriptor = null;
    Enumeration e = currentDFAgentDescriptor.getAgentServices();  
    Object o;  
    numberOfServiceDescriptors     = 0; 
    while (e.hasMoreElements()) {
      numberOfServiceDescriptors ++;
      o=e.nextElement();
    }//while

    currentServiceDescriptorNumber = 0;
  }//initializeDFAgentDescriptors(Enumeration)

  void setServiceVisible(boolean tf) {
    textField8.setVisible(tf);
    textField9.setVisible(tf);
    textField10.setVisible(tf);
    textField11.setVisible(tf);
    textField12.setVisible(tf);
    textField13.setVisible(tf);
    label12.setVisible(tf);
    label13.setVisible(tf);
    label14.setVisible(tf);
    label15.setVisible(tf);
    label16.setVisible(tf);
    label17.setVisible(tf);
    button1.setVisible(tf);
    button2.setVisible(tf);
    label11.setVisible(tf);
  }//setServiceVisible(boolean)

  void setAllEditable(boolean tf) {
    // all textfields and textareas must become noteditable
    textField1.setEditable(tf);
    textField2.setEditable(tf);
    textField3.setEditable(tf);
    textField4.setEditable(tf);
    textField5.setEditable(tf);
    textField6.setEditable(tf);
    textField7.setEditable(tf);
    textField8.setEditable(tf);
    textField9.setEditable(tf);
    textField10.setEditable(tf);
    textField11.setEditable(tf);
    textField12.setEditable(tf);
    textField13.setEditable(tf);
    textField14.setEditable(tf);
  }//setAllEditable(boolean)
	
    /**
     * Shows or hides the component depending on the boolean flag b.
     * @param b  if true, show the component; otherwise, hide the component.
     * @see java.awt.Component#isVisible
     */
  public void setVisible(boolean b) {
    setLocation(50, 50);
    super.setVisible(b);
  }//setVisible(boolean)
	


  void updateTextFields() {
    label1.setText("DF AGENT DESCRIPTION no. "+currentDFAgentDescriptorNumber); //+" of " + numberOfDFAgentDescriptors);
    textField1.setText(currentDFAgentDescriptor.getName());
    textField2.setText(currentDFAgentDescriptor.getType());
    textField7.setText(currentDFAgentDescriptor.getOntology());
    textField4.setText(currentDFAgentDescriptor.getOwnership());
    textField5.setText(currentDFAgentDescriptor.getDFState());
    String str = "";
    Enumeration e = currentDFAgentDescriptor.getAddresses();
    while (e.hasMoreElements())
      str = str + (String)e.nextElement()+ " ";
    textField3.setText(str);
    str = "";
    e = currentDFAgentDescriptor.getInteractionProtocols();
    while (e.hasMoreElements())
      str = str + (String)e.nextElement() + " ";
    textField14.setText(str); 
    label11.setText(/*"Service no. " + */(currentServiceDescriptorNumber+1) + " of " + numberOfServiceDescriptors); 
    if(currentServiceDescriptor != null) {
      setServiceVisible(true);
      textField8.setText(currentServiceDescriptor.getName());
      textField9.setText(currentServiceDescriptor.getType());
      textField10.setText(currentServiceDescriptor.getOntology());
      textField11.setText(currentServiceDescriptor.getFixedProps());
      textField12.setText(currentServiceDescriptor.getNegotiableProps());
      textField13.setText(currentServiceDescriptor.getCommunicationProps());
    } else {
      setServiceVisible(false);
    }
  }

  Enumeration allData;
  Enumeration allDFAgentDescriptors;
  AgentManagementOntology.DFAgentDescriptor currentDFAgentDescriptor;
  int         currentDFAgentDescriptorNumber;
  int         numberOfDFAgentDescriptors;
  Enumeration allServiceDescriptors;
  AgentManagementOntology.ServiceDescriptor currentServiceDescriptor;
  int         numberOfServiceDescriptors;
  int         currentServiceDescriptorNumber; 

  int         usage = 0; // this class variable indicates for what usage the
	                   // frame has been created
 
  GUI2DFCommunicatorInterface       myAgent; // pointer to the agent to send messages
       
	//{{DECLARE_CONTROLS
	JLabel label1;
	JLabel label2;
	JLabel label3;
	JLabel label4;
	JLabel label5;
	JLabel label6;
	JLabel label7;
	JLabel label8;
	JLabel label9;
	JLabel label10;
	JLabel label11;
	JLabel label12;
	JLabel label13;
	JLabel label14;
	JLabel label15;
	JLabel label16;
	JLabel label17;

	JTextField textField1;
	JTextField textField2;
	JTextField textField3;
	JTextField textField4;
	JTextField textField5;
	JTextField textField6;
	JTextField textField7;
	JTextField textField8;
	JTextField textField9;
	JTextField textField10;
	JTextField textField11;
	JTextField textField12;
	JTextField textField13;
	JTextField textField14;



	JButton button1;
	JButton button2;
	JButton button3;
	JButton button4;
	JButton button5;
	JButton button6;
	JButton button7;
	JButton button8;

	//}}
	
	//{{DECLARE_MENUS
	//}}
	
	class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosing(java.awt.event.WindowEvent event)
		{
			Object object = event.getSource();
			if(object == agentDescriptionFrame.this)
				DFGUI_WindowClosing(event);
		}
	}
	
	void DFGUI_WindowClosing(java.awt.event.WindowEvent event)
	{
	  miExit_Action(null);
	}
	
	
	
	
	void miExit_Action(java.awt.event.ActionEvent event) {
          setVisible(false);	// hide the Frame
          dispose();			// free the system resources
	}//miExit_Action(ActionEvent)
	
	



	class SymMouse extends java.awt.event.MouseAdapter {
          public void mouseClicked(java.awt.event.MouseEvent event) {
            Object object = event.getSource();
            if(object == button1)
              button1_MouseClicked(event);
            else if(object == button2)
              button2_MouseClicked(event);
            else if(object == button3)
              button3_MouseClicked(event);
            else if(object == button4)
              button4_MouseClicked(event);
            else if(object == button5)
              button5_MouseClicked(event);
            else if(object == button6)
              button6_MouseClicked(event);
            else if(object == button7)
              button7_MouseClicked(event);
            else if(object == button8)
              button8_MouseClicked(event);
          }//if
	}//class SymMouse

  void button1_MouseClicked(java.awt.event.MouseEvent event) { // NextService
    if(usage == DFGUI.REGISTER) { // FIXME
      System.err.println("Next Service during Registration. Not yet implemented.");
      return;
    }//if
    if(allServiceDescriptors.hasMoreElements())
      currentServiceDescriptor       = (AgentManagementOntology.ServiceDescriptor)allServiceDescriptors.nextElement();    
    currentServiceDescriptorNumber++;
    updateTextFields();
    if(! allServiceDescriptors.hasMoreElements())
      button1.setVisible(false); // button NextService
    button2.setVisible(true); // button PreviousService
  }//button1_MouseClicked(MouseEvent)

  void button2_MouseClicked(java.awt.event.MouseEvent event) {
    if(usage == DFGUI.REGISTER) { // FIXME
      System.err.println("Previous Service during Registration. Not yet implemented.");
      return;
    }//if
    if(currentServiceDescriptorNumber < 1) {
      button2.setVisible(false);
    } else {
      allServiceDescriptors = currentDFAgentDescriptor.getAgentServices();
      for (int i=0; i<currentServiceDescriptorNumber; i++)
        currentServiceDescriptor = (AgentManagementOntology.ServiceDescriptor)allServiceDescriptors.nextElement();
      currentServiceDescriptorNumber--; 
      updateTextFields();
      if(! allServiceDescriptors.hasMoreElements())
        button1.setVisible(false); // button NextService
      if(currentServiceDescriptorNumber > 0)
        button2.setVisible(true); // button PreviousService
      else button2.setVisible(false);
    }//else
  }//button2_MouseClicked(MouseEvent)

  void button3_MouseClicked(java.awt.event.MouseEvent event) { // NextAgent
    initializeDFAgentDescriptors(allDFAgentDescriptors);
    updateTextFields();
    if(! allDFAgentDescriptors.hasMoreElements())
      button3.setVisible(false); // button NextAgent
    if(! allServiceDescriptors.hasMoreElements())
      button1.setVisible(false); // button NextService
    button2.setVisible(false); // button PreviousService
  }//button3_MouseClicked(MouseEvent)

  void button4_MouseClicked(java.awt.event.MouseEvent event) { // OK
    // can only be called to register/deregister/modify
    if(usage == DFGUI.REGISTER) {
      if(textField1.getText().length() > 0) currentDFAgentDescriptor.setName(textField1.getText());
      if(textField2.getText().length() > 0) currentDFAgentDescriptor.setType(textField2.getText());
      if(textField7.getText().length() > 0) currentDFAgentDescriptor.setOntology(textField7.getText());
      if(textField4.getText().length() > 0) currentDFAgentDescriptor.setOwnership(textField4.getText());
      if(textField5.getText().length() > 0) currentDFAgentDescriptor.setDFState(textField5.getText());
      if(textField3.getText().length() > 0) currentDFAgentDescriptor.addAddress(textField3.getText());
      if(textField13.getText().length() > 0) currentDFAgentDescriptor.addInteractionProtocol(textField13.getText());
      addAgentService();
      myAgent.postRegisterEvent(myAgent.getName(),currentDFAgentDescriptor);

    } else if(usage == DFGUI.DEREGISTER) {
      // currentDFAgentDescriptor already contains the description
      myAgent.postDeregisterEvent(myAgent.getName(),currentDFAgentDescriptor);
    } else if(usage == DFGUI.MODIFY) {
      //ACLMessage msg = new ACLMessage("request");
      //msg.setSource(myAgent.getName());
      //msg.setDest(myAgent.getName());
      //msg.setOntology("fipa-agent-management");
      //msg.setLanguage("SL0");
      //msg.setProtocol("fipa-request");
      //msg.setReplyWith(myAgent.getLocalName()+"-dfgui"+(new Date()).getTime());
      AgentManagementOntology.DFAction a = new AgentManagementOntology.DFAction();
      AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();
      if(textField1.getText().length() > 0) dfd.setName(textField1.getText());
      if(textField2.getText().length() > 0) dfd.setType(textField2.getText());
      if(textField7.getText().length() > 0) dfd.setOntology(textField7.getText());
      if(textField4.getText().length() > 0) dfd.setOwnership(textField4.getText());
      if(textField5.getText().length() > 0) dfd.setDFState(textField5.getText());
      if(textField3.getText().length() > 0) dfd.addAddress(textField3.getText());
      if(textField13.getText().length() > 0) dfd.addInteractionProtocol(textField13.getText());
      // FIXME what to do here?
      addAgentService();
      a.setName(AgentManagementOntology.DFAction.MODIFY);
      a.setActor(myAgent.getName());
      a.setArg(dfd);
      StringWriter text = new StringWriter();
      a.toText(text);
      //msg.setContent(text.toString()); 
      //msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
      //myAgent.send(msg);    
    }//

    dispose(); // close the window
  }//button4_MouseClicked(MouseEvent)

  void button5_MouseClicked(java.awt.event.MouseEvent event) { // Exit
    dispose();
  }//button5_MouseClicked(MouseEvent)

  /**
   * This method creates a new ServiceDescriptor, fills all its fields
   * with all those text fields that have been filled in the GUI, 
   * resets the textfields in the GUI, add the service description to the
   * current agent description, and updates the value of the variable 
   * numberOfServiceDescriptors
   */
  void addAgentService() {
    AgentManagementOntology.ServiceDescriptor sd  = new AgentManagementOntology.ServiceDescriptor(); 
    boolean sd_isnotempty=false;
    if(textField8.getText().length() > 0) {
      sd.setName(textField8.getText());
      textField8.setText("");
      sd_isnotempty = true; }
    if(textField9.getText().length() > 0) {
      sd.setType(textField9.getText());
      textField9.setText("");
      sd_isnotempty = true; }
    if(textField10.getText().length() > 0) {
      sd.setOntology(textField10.getText());
      textField10.setText("");
      sd_isnotempty = true; }
    if(textField11.getText().length() > 0) {
      sd.setFixedProps(textField11.getText());
      textField11.setText("");
      sd_isnotempty = true; }
    if(textField12.getText().length() > 0) {
      sd.setNegotiableProps(textField12.getText());
      textField12.setText("");
      sd_isnotempty = true; }
    if(textField13.getText().length() > 0) {
      sd.setCommunicationProps(textField13.getText());
      textField13.setText("");
      sd_isnotempty = true; }

    if(sd_isnotempty) {
      currentDFAgentDescriptor.addAgentService(sd);
      numberOfServiceDescriptors++;
    }//if
  }//addAgentService()

  void button6_MouseClicked(java.awt.event.MouseEvent event) { // Add New Service
    addAgentService();
    currentServiceDescriptorNumber++;
    label11.setText(/*"Service no. " + */currentServiceDescriptorNumber + " of " + numberOfServiceDescriptors);     
    setServiceVisible(true);
  }//button6_MouseClicked(MouseEvent)

  void button7_MouseClicked(java.awt.event.MouseEvent event) { // Delete this Service
    //FIXME. To DO
    System.err.println("Delete this service. Not yet Implemented.");
  }//button7_MouseClicked(MouseEvent)

  void button8_MouseClicked(java.awt.event.MouseEvent event) { // Previous Agent
    //FIXME. To DO
    if(currentDFAgentDescriptorNumber < 1) {
      button8.setVisible(false);
    } else {
      currentDFAgentDescriptorNumber = -1;
      initializeDFAgentDescriptors(allData);
      for (int i=0; i<currentDFAgentDescriptorNumber; i++)
        initializeDFAgentDescriptors(allDFAgentDescriptors);
      currentDFAgentDescriptorNumber--; 
      updateTextFields();
      if(! allServiceDescriptors.hasMoreElements())
        button1.setVisible(false); // button NextService
      if(currentServiceDescriptorNumber > 0)
        button2.setVisible(true); // button PreviousService
      else button2.setVisible(false);
    }//else
  }//button8_MouseClicked(MouseEvent)
}//class AgentDescriptorFrame
