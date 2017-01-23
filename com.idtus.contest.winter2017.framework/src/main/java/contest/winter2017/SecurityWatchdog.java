package contest.winter2017;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.jar.Attributes;

import javax.swing.JOptionPane;

import org.w3c.dom.DOMError;

/**
 * This class installs a security manager to monitor a test
 * instance, and then runs that test instance.
 * 
 * @author ICT-2
 *
 */
public class SecurityWatchdog {
	private static final int WATCHDOG_EXIT_CODE = 302590835;

	private static boolean watchdogStarted = false;
	private static boolean toolChain = true;

	/**
	 * Method used to run a jar under test with specified options.
	 *
	 * @param args - args with information for running jar to test
	 * @throws Exception - any uncaught exceptions thrown
	 */
	public static void main(String[] args) throws Exception {
			// ensure this code is being called properly
		try {
			if (watchdogStarted) {
				throw new SecurityException("Cannot access SecurityWatchdog.");
			} else if (args.length < 2) {
				throw new Exception("SecurityWatchdog should be run from Tester.java");
			} else {
				watchdogStarted = true;
			}

			// testing arguments
			File jarFileToTest = new File(args[0]);
			toolChain = args[1].equalsIgnoreCase("true");
			// get the args to pass to the next jar
			String[] argsToPass = new String[args.length - 2];
			for (int i = 2; i < args.length; i++) {
				argsToPass[i - 2] = args[i];
			}

			// laod the jar
			URL fileURL = null;
			URL jarURL = null;
		    JarURLConnection jarURLconn = null;
		    URLClassLoader cl = null;
		    try {
				fileURL = jarFileToTest.toURI().toURL();
				String jarUrlTemp = "jar:"+jarFileToTest.toURI().toString()+"!/";
				jarURL = new URL(jarUrlTemp);
				cl = URLClassLoader.newInstance(new URL[]{fileURL});
				jarURLconn = null;
				jarURLconn = (JarURLConnection)jarURL.openConnection();
		    } catch (IOException ioe) {
		    	watchdogError("LOAD JAR");
		    }

			// figure out where the entry-point (main class) is in the jar under test
			Attributes attr = null;
			try {
				attr = jarURLconn.getMainAttributes();
			} catch (IOException ioe) {
				watchdogError("LOAD MANIFEST");
			}

			String mainClassName = attr.getValue(Attributes.Name.MAIN_CLASS);
			if (mainClassName == null) {
				watchdogError("GET MAIN CLASS");
			}
	
			// load the Main class from the jar under test
			Class<?> mainClass = null;
			try {
				mainClass = cl.loadClass(mainClassName);
			} catch (ClassNotFoundException cnfe) {
				watchdogError("LOAD MAIN CLASS");
			}
	
			// use reflection to invoke the main method
			Method mainMethod = null;
			try {
				Class<?>[] mainArgs = new Class[] {String[].class};
				mainMethod = mainClass.getDeclaredMethod("main", mainArgs);
			} catch (NoSuchMethodException nsme) {
				watchdogError("ERROR: Could not load main method of jar to test.");
			}

			// set security manager if not using toolchain
			if (!toolChain) {
				try {
					System.setSecurityManager(new SecurityReporter(System.out));
				} catch (SecurityException se) {
					watchdogError("ERROR: Could not set security manager.");
				}
			}

			// invoke main method
			try {
				mainMethod.invoke(null, (Object) argsToPass);
			} catch (InvocationTargetException e) {
				// notify of exception, not just system err
				e.getCause().printStackTrace();
			} catch (IllegalAccessException | IllegalArgumentException e) {
				watchdogError("INVOKE MAIN METHOD", e);
			}
	
			// notify tester of program end
			System.exit(WATCHDOG_EXIT_CODE);
		} catch (Exception e) {
			watchdogError("RUN WATCHDOG", e);
		}
	}

	/**
	 * Method used to notify the tester
	 * of errors.
	 * 
	 * @param error - error to pass to tester
	 * @param e - exception to pass to tester
	 */
	private static void watchdogError(String error, Exception e) {
		System.err.println("<<WATCHDOG_OUTPUT_START>>");
		System.err.println(error);
		if (e != null) {
			e.printStackTrace();
		}
		System.err.println("<<WATCHDOG_OUTPUT_END>>");
		System.exit(0);
	}

	/**
	 * Method used to notify the tester
	 * of errors.
	 * 
	 * @param error - error to pass to tester
	 */
	private static void watchdogError(String error) {
		watchdogError(error, null);
	}
}
