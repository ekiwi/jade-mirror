package FIPA;

/**
* FIPA/EnvelopeHolder.java
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from fipa.idl
* venerd� 7 dicembre 2001 16.57.12 CET
*/

public final class EnvelopeHolder implements org.omg.CORBA.portable.Streamable
{
  public FIPA.Envelope value = null;

  public EnvelopeHolder ()
  {
  }

  public EnvelopeHolder (FIPA.Envelope initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = FIPA.EnvelopeHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    FIPA.EnvelopeHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return FIPA.EnvelopeHelper.type ();
  }

}
