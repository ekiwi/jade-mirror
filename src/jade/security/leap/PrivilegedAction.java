package jade.security.leap;


public interface PrivilegedAction
//__JADE_ONLY__BEGIN
		extends java.security.PrivilegedAction
//__JADE_ONLY__END
{
	public Object run();
}