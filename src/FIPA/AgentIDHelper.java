package FIPA;


/**
* FIPA/AgentIDHelper.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from fipa.idl
* marted� 14 agosto 2001 12.58.13 GMT+02:00
*/

abstract public class AgentIDHelper
{
  private static String  _id = "IDL:FIPA/AgentID:1.0";

  public static void insert (org.omg.CORBA.Any a, FIPA.AgentID that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static FIPA.AgentID extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  private static boolean __active = false;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      synchronized (org.omg.CORBA.TypeCode.class)
      {
        if (__typeCode == null)
        {
          if (__active)
          {
            return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );
          }
          __active = true;
          org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [4];
          org.omg.CORBA.TypeCode _tcOf_members0 = null;
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[0] = new org.omg.CORBA.StructMember (
            "name",
            _tcOf_members0,
            null);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_alias_tc (FIPA.URLHelper.id (), "URL", _tcOf_members0);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_sequence_tc (0, _tcOf_members0);
          _members0[1] = new org.omg.CORBA.StructMember (
            "addresses",
            _tcOf_members0,
            null);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_recursive_tc ("");
          _members0[2] = new org.omg.CORBA.StructMember (
            "resolvers",
            _tcOf_members0,
            null);
          _tcOf_members0 = FIPA.PropertyHelper.type ();
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_sequence_tc (0, _tcOf_members0);
          _members0[3] = new org.omg.CORBA.StructMember (
            "userDefinedProperties",
            _tcOf_members0,
            null);
          __typeCode = org.omg.CORBA.ORB.init ().create_struct_tc (FIPA.AgentIDHelper.id (), "AgentID", _members0);
          __active = false;
        }
      }
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static FIPA.AgentID read (org.omg.CORBA.portable.InputStream istream)
  {
    FIPA.AgentID value = new FIPA.AgentID ();
    value.name = istream.read_string ();
    int _len0 = istream.read_long ();
    value.addresses = new String[_len0];
    for (int _o1 = 0;_o1 < value.addresses.length; ++_o1)
      value.addresses[_o1] = FIPA.URLHelper.read (istream);
    int _len1 = istream.read_long ();
    value.resolvers = new FIPA.AgentID[_len1];
    for (int _o2 = 0;_o2 < value.resolvers.length; ++_o2)
      value.resolvers[_o2] = FIPA.AgentIDHelper.read (istream);
    int _len2 = istream.read_long ();
    value.userDefinedProperties = new FIPA.Property[_len2];
    for (int _o3 = 0;_o3 < value.userDefinedProperties.length; ++_o3)
      value.userDefinedProperties[_o3] = FIPA.PropertyHelper.read (istream);
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, FIPA.AgentID value)
  {
    ostream.write_string (value.name);
    ostream.write_long (value.addresses.length);
    for (int _i0 = 0;_i0 < value.addresses.length; ++_i0)
      FIPA.URLHelper.write (ostream, value.addresses[_i0]);
    ostream.write_long (value.resolvers.length);
    for (int _i1 = 0;_i1 < value.resolvers.length; ++_i1)
      FIPA.AgentIDHelper.write (ostream, value.resolvers[_i1]);
    ostream.write_long (value.userDefinedProperties.length);
    for (int _i2 = 0;_i2 < value.userDefinedProperties.length; ++_i2)
      FIPA.PropertyHelper.write (ostream, value.userDefinedProperties[_i2]);
  }

}
