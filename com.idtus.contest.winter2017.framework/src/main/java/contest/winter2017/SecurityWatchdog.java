package contest.winter2017;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;

/**
 * This class installs a security manager to monitor a jar under test, and then
 * runs that test instance.
 * <p>
 * Currently, this approach expects that the standard err and out will not be
 * changed by the jar under test. A fix for this could be to replace the
 * standard error and out, and send err and out from a separate thread, which
 * monitors and sends output form the "fake" standard err and out.
 * 
 * @author ICT-2
 *
 */
public class SecurityWatchdog {
	/**
	 * Random int exit code to signify whether the jar under test is trying to
	 * terminate the program.
	 */
	private static final int WATCHDOG_EXIT_CODE = 302590835;

	/**
	 * Whether the watch dog has already been started.
	 */
	private static boolean watchdogStarted = false;

	/**
	 * Whether tool chain output mode is enabled.
	 */
	private static boolean toolChain = true;

	/**
	 * Runs an executable jar under test with the specified options.
	 * <p>
	 *
	 * @param args
	 *            - arguments with information for the executable jar under test
	 * @throws Exception
	 *             - any uncaught exceptions thrown
	 */
	public static void main(String[] args) throws Throwable {
		try {
			// ensure this code is being called properly
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
			// get the arguments to pass to the next jar
			String[] argsToPass = new String[args.length - 2];
			for (int i = 2; i < args.length; i++) {
				argsToPass[i - 2] = args[i];
			}

			// load the jar
			URL fileURL = null;
			URL jarURL = null;
			JarURLConnection jarURLconn = null;
			URLClassLoader cl = null;
			try {
				fileURL = jarFileToTest.toURI().toURL();
				String jarUrlTemp = "jar:" + jarFileToTest.toURI().toString() + "!/";
				jarURL = new URL(jarUrlTemp);
				cl = URLClassLoader.newInstance(new URL[] { fileURL });
				jarURLconn = null;
				jarURLconn = (JarURLConnection) jarURL.openConnection();
			} catch (IOException ioe) {
				watchdogError("LOAD JAR");
			}

			// figure out the entry-point (main class) is in the jar under test
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

			// load the main class from the jar under test
			Class<?> mainClass = null;
			try {
				mainClass = cl.loadClass(mainClassName);
			} catch (ClassNotFoundException cnfe) {
				watchdogError("LOAD MAIN CLASS");
			}

			// use reflection to invoke the main method
			Method mainMethod = null;
			try {
				Class<?>[] mainArgs = new Class[] { String[].class };
				mainMethod = mainClass.getDeclaredMethod("main", mainArgs);
			} catch (NoSuchMethodException nsme) {
				watchdogError("ERROR: Could not load main method of jar to test.");
			}

			try {
				System.setSecurityManager(new SecurityReporter(System.out));
			} catch (SecurityException se) {
				watchdogError("ERROR: Could not set security manager.");
			}

			// invoke main method
			try {
				mainMethod.invoke(null, (Object) argsToPass);
			} catch (InvocationTargetException e) {
				// invocation target exceptions don't supply from which thread
				// they occurred, the main thread is assumed here
				Thread curr = Thread.currentThread();
				curr.getUncaughtExceptionHandler().uncaughtException(curr, e.getCause());
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
	 * Notifies the tester of errors.
	 * 
	 * @param error
	 *            - error to pass to tester
	 * @param e
	 *            - exception to pass to tester
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
	 * Notifies the tester of errors.
	 * 
	 * @param error
	 *            - error to pass to tester
	 */
	private static void watchdogError(String error) {
		watchdogError(error, null);
	}
}