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

package jade.lang.acl;

import java.io.IOException;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.Date;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.util.leap.Properties;
import jade.util.leap.Iterator;

/**
 * This class implements the LEAP codec for ACLMessages.
 * @author Jerome Picault - Motorola Labs
 * @author Giovanni Caire - TILAB
 * @version $Date$ $Revision$
 */
public class LEAPACLCodec implements ACLCodec{

  public static String NAME = "leap.acl.rep";

  /**
   * Encodes an <code>ACLMessage</code> object into a byte sequence,
   * according to the specific message representation.
   * @param msg The ACL message to encode.
   * @return a byte array, containing the encoded message.
   */
  public byte[] encode(ACLMessage msg){
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    try{
      serializeACL(msg, dos);
      return baos.toByteArray();
    } catch (IOException ioe){
      ioe.printStackTrace();
    } catch (CodecException ce){
      ce.printStackTrace();
    }
    return new byte[0];
  }

  /**
   * Recovers an <code>ACLMessage</code> object back from raw data,
   * using the specific message representation to interpret the byte
   * sequence.
   * @param data The byte sequence containing the encoded message.
   * @return A new <code>ACLMessage</code> object, built from the raw
   * data.
   * @exception CodecException If some kind of syntax error occurs.
   */
  public ACLMessage decode(byte[] data) throws CodecException{
    DataInputStream din = new DataInputStream(new ByteArrayInputStream(data));
    try{
      return deserializeACL(din);
    } catch (IOException ioe){
      throw new CodecException(getName()+" ACLMessage decoding exception",ioe);
    }
  }

  /**
   * Query the name of the message representation handled by this
   * <code>Codec</code> object. The FIPA standard representations have
   * a name starting with <code><b>"fipa.acl.rep."</b></code>.
   * @return The name of the handled ACL message representation.
   */
  public String getName(){
    return NAME;
  }


  /**
   */
  public final static void serializeACL(ACLMessage msg, DataOutputStream dos) throws IOException, CodecException {
    dos.writeByte(msg.getPerformative());

    byte presence1 = 0;
    byte presence2 = 0;
    AID sender = msg.getSender();
    String language = msg.getLanguage();
    String ontology = msg.getOntology();
    String encoding = msg.getEncoding();
    String protocol = msg.getProtocol();
    String conversationId = msg.getConversationId();
    String inReplyTo = msg.getInReplyTo();
    String replyWith = msg.getReplyWith();
    Date replyBy = msg.getReplyByDate();
    Envelope envelope = null;
		//#CUSTOM_EXCLUDE_BEGIN
		//#CUSTOMJ2SE_EXCLUDE_BEGIN
		envelope = msg.getEnvelope();
    Properties props = msg.getAllUserDefinedParameters();
    if (props.size() > 63) {
    	throw new CodecException("Cannot serialize more than 63 params",null);
    }
		//#CUSTOMJ2SE_EXCLUDE_END
		//#CUSTOM_EXCLUDE_END

    if (sender != null) { presence1 |= 0x80; }
    if (language != null) { presence1 |= 0x40; }
    if (ontology != null) { presence1 |= 0x20; }
    if (encoding != null) { presence1 |= 0x10; }
    if (protocol != null) { presence1 |= 0x08; }
    if (conversationId != null) { presence1 |= 0x04; }
    if (inReplyTo != null) { presence1 |= 0x02; }
    if (replyWith != null) { presence1 |= 0x01; }
    if (replyBy != null) { presence2 |= 0x80; }
    if (envelope != null) { presence2 |= 0x40; }
		//#CUSTOM_EXCLUDE_BEGIN
		//#CUSTOMJ2SE_EXCLUDE_BEGIN
    presence2 |= (props.size() & 0x3F);
		//#CUSTOMJ2SE_EXCLUDE_END
		//#CUSTOM_EXCLUDE_END
    dos.writeByte(presence1);
    dos.writeByte(presence2);

    if (sender != null) { serializeAID(sender, dos); }
    if (language != null) { dos.writeUTF(language); }
    if (ontology != null) { dos.writeUTF(ontology); }
    if (encoding != null) { dos.writeUTF(encoding); }
    if (protocol != null) { dos.writeUTF(protocol); }
    if (conversationId != null) { dos.writeUTF(conversationId); }
    if (inReplyTo != null) { dos.writeUTF(inReplyTo); }
    if (replyWith != null) { dos.writeUTF(replyWith); }
    if (replyBy != null) { dos.writeLong(replyBy.getTime()); }
		//#CUSTOM_EXCLUDE_BEGIN
		//#CUSTOMJ2SE_EXCLUDE_BEGIN
    if (envelope != null) { serializeEnvelope(envelope, dos); }

    // User defined parameters
    serializeProperties(props, dos);
		//#CUSTOMJ2SE_EXCLUDE_END
		//#CUSTOM_EXCLUDE_END    
    // Receivers
    Iterator it = msg.getAllReceiver();
    while (it.hasNext()) {
      dos.writeBoolean(true);
      serializeAID((AID) it.next(), dos);
    } 
    dos.writeBoolean(false);

    // Reply-to
    it = msg.getAllReplyTo();
    while (it.hasNext()) {
      dos.writeBoolean(true);
      serializeAID((AID) it.next(), dos);
    } 
    dos.writeBoolean(false);
    
    // Content
    if (msg.hasByteSequenceContent()) {
    	// Content present in bynary form
      dos.writeByte(2);
      byte[] content = msg.getByteSequenceContent();
      dos.writeInt(content.length);
      dos.write(content, 0, content.length);
    } 
    else {
      String content = msg.getContent();
      if (content != null) { 
    		// Content present in String form
      	dos.writeByte(1);
      	dos.writeUTF(content);
      }
      else {
    		// Content NOT present
      	dos.writeByte(0);
      }
    } 
  }

  /**
   */
  public final static ACLMessage deserializeACL(DataInputStream dis) throws IOException, CodecException {
  	ACLMessage msg = new ACLMessage((int) dis.readByte());

    byte presence1 = dis.readByte();
    byte presence2 = dis.readByte();
    
    if ((presence1 & 0x80) != 0) { msg.setSender(deserializeAID(dis)); }
    if ((presence1 & 0x40) != 0) { msg.setLanguage(dis.readUTF()); }
    if ((presence1 & 0x20) != 0) { msg.setOntology(dis.readUTF()); }
    if ((presence1 & 0x10) != 0) { msg.setEncoding(dis.readUTF()); }
    if ((presence1 & 0x08) != 0) { msg.setProtocol(dis.readUTF()); }
    if ((presence1 & 0x04) != 0) { msg.setConversationId(dis.readUTF()); }
    if ((presence1 & 0x02) != 0) { msg.setInReplyTo(dis.readUTF()); }
    if ((presence1 & 0x01) != 0) { msg.setReplyWith(dis.readUTF()); }
    if ((presence2 & 0x80) != 0) { msg.setReplyByDate(new Date(dis.readLong())); }
		//#CUSTOM_EXCLUDE_BEGIN
		//#CUSTOMJ2SE_EXCLUDE_BEGIN
    if ((presence2 & 0x40) != 0) { msg.setEnvelope(deserializeEnvelope(dis)); }
    // User defined properties
    int propsSize = presence2 & 0x3F;
    for (int i = 0; i < propsSize; ++i) {
    	String key = dis.readUTF();
    	String val = dis.readUTF();
    	msg.addUserDefinedParameter(key, val);
    }
		//#CUSTOMJ2SE_EXCLUDE_END
		//#CUSTOM_EXCLUDE_END
    
    // Receivers
    while (dis.readBoolean()) {
    	msg.addReceiver(deserializeAID(dis));
    } 

    // Reply-to
    while (dis.readBoolean()) {
    	msg.addReplyTo(deserializeAID(dis));
    }

    // Content
    byte type = dis.readByte();
    if (type == 2) {
    	// Content present in bynary form
      byte[] content = new byte[dis.readInt()];
      dis.read(content, 0, content.length);
      msg.setByteSequenceContent(content);
    }
    else if (type == 1) {
    	// Content present in String form
    	msg.setContent(dis.readUTF());
    }
        
    return msg;
  }


  private final static void serializeAID(AID id, DataOutputStream dos) throws IOException, CodecException {
    byte presence = 0;
    String name = id.getName();
    Iterator addresses = id.getAllAddresses();
		//#CUSTOM_EXCLUDE_BEGIN
		//#CUSTOMJ2SE_EXCLUDE_BEGIN
    Iterator resolvers = id.getAllResolvers();
    Properties props = id.getAllUserDefinedSlot();
    if (props.size() > 31) {
    	throw new CodecException("Cannot serialize more than 31 slots",null);
    }
		//#CUSTOMJ2SE_EXCLUDE_END
		//#CUSTOM_EXCLUDE_END
    if (name != null) { presence |= 0x80; }
    if (addresses.hasNext()) { presence |= 0x40; }
		//#CUSTOM_EXCLUDE_BEGIN
		//#CUSTOMJ2SE_EXCLUDE_BEGIN
    if (resolvers.hasNext()) { presence |= 0x20; }
    presence |= (props.size() & 0x1F);
		//#CUSTOMJ2SE_EXCLUDE_END
		//#CUSTOM_EXCLUDE_END
    dos.writeByte(presence);
    
    if (name != null) { dos.writeUTF(name); }
    // Addresses
    while (addresses.hasNext()) {
    	dos.writeUTF((String) addresses.next());
    	dos.writeBoolean(addresses.hasNext());
    }
		//#CUSTOM_EXCLUDE_BEGIN
		//#CUSTOMJ2SE_EXCLUDE_BEGIN
    // Resolvers
    while (resolvers.hasNext()) {
    	serializeAID((AID) resolvers.next(), dos);
    	dos.writeBoolean(resolvers.hasNext());
    }
    // User defined slots
    serializeProperties(props, dos);
		//#CUSTOMJ2SE_EXCLUDE_END
		//#CUSTOM_EXCLUDE_END
  }
  
  private final static AID deserializeAID(DataInputStream dis) throws IOException, CodecException {
    byte presence = dis.readByte();
    AID id = ((presence & 0x80) != 0 ? new AID(dis.readUTF(), AID.ISGUID) : new AID());
    
    // Addresses
    if ((presence & 0x40) != 0) {
    	do {
    		id.addAddresses(dis.readUTF());
    	} while (dis.readBoolean());
    }
    //#CUSTOM_EXCLUDE_BEGIN
		//#CUSTOMJ2SE_EXCLUDE_BEGIN
    // Resolvers
    if ((presence & 0x20) != 0) {
    	do {
    		id.addResolvers(deserializeAID(dis));
    	} while (dis.readBoolean());
    }
    
    // User defined slots
    int propsSize = presence & 0x1F;
    for (int i = 0; i < propsSize; ++i) {
    	String key = dis.readUTF();
    	String val = dis.readUTF();
    	id.addUserDefinedSlot(key, val);
    }
		//#CUSTOMJ2SE_EXCLUDE_END
    //#CUSTOM_EXCLUDE_END
  	return id;
  }
  
  //#CUSTOM_EXCLUDE_BEGIN
  //#CUSTOMJ2SE_EXCLUDE_BEGIN
  private final static void serializeEnvelope(Envelope env, DataOutputStream dos) throws IOException, CodecException {
    //  	System.out.println("LEAPACLCodec.serializeEnvelope() not yet implemented");
  	// FIXME: To be implemented
  }
  
  private final static Envelope deserializeEnvelope(DataInputStream dis) throws IOException, CodecException {
    //  	System.out.println("LEAPACLCodec.deserializeEnvelope() not yet implemented");
  	// FIXME: To be implemented
  	return null;
  }
  
  private static final void serializeProperties(Properties props, DataOutputStream dos) throws IOException, CodecException {
    Enumeration e = props.keys();
    while (e.hasMoreElements()) {
    	String key = (String) e.nextElement();
    	dos.writeUTF(key);
    	dos.writeUTF(props.getProperty(key));
    }
  }  
  //#CUSTOMJ2SE_EXCLUDE_END
  //#CUSTOM_EXCLUDE_END


}