package FIPA;

/**
* FIPA/DateTimeHolder.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from fipa.idl
* marted� 14 agosto 2001 12.58.13 GMT+02:00
*/


// to the local timezone.
public final class DateTimeHolder implements org.omg.CORBA.portable.Streamable
{
  public FIPA.DateTime value = null;

  public DateTimeHolder ()
  {
  }

  public DateTimeHolder (FIPA.DateTime initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = FIPA.DateTimeHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    FIPA.DateTimeHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return FIPA.DateTimeHelper.type ();
  }

}
