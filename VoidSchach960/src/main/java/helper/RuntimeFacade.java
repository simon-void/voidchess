/*
 * Created on 27.09.2006
 */

package helper;

public class RuntimeFacade
{
	final private static Runtime runtime = Runtime.getRuntime();
	
	public static void collectGarbage()
	{
		for( int i=0;i<4;i++ ) {
			runtime.runFinalization();
			runtime.gc();
			Thread.yield();
		}
	}
	
	public static void printMemoryUsage( String mark )
	{
		long total = runtime.totalMemory();
		long free  = runtime.freeMemory();
		long used  = total-free;
		
		StringBuilder sb = new StringBuilder( 32 );
		sb.append( mark );
		sb.append( ":" );
		sb.append( (used/1000) );
		sb.append( "kbyte " );
		sb.append( (total/1000) );
		sb.append( "kbyte" );
		System.out.println( sb.toString() );
	}
	
	public static void assertJavaVersion()
	{
		assertJavaVersion( System.getProperty( "java.version" ) );
	}

	static void assertJavaVersion( String javaVersion )
	{
		if( javaVersion.compareTo( "1.7" )<0 ) {
			throw new RuntimeException( "Dieses Spiel verlangt mindestens eine Javaversion von 1.7. Sie verwenden Version:"+javaVersion+". Um eine aktuelle Javaversion downzuloaden wenden Sie sich an www.sun.com .");
		}
	}

}
