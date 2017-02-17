package contest.winter2017;

import java.io.FilePermission;
import java.io.PrintStream;
import java.security.Permission;
import java.util.LinkedList;
import java.util.PropertyPermission;

/**
 * This class watches for security permission requests. This class is not meant
 * to curtail actions requested by executable jars under test, but to keep track
 * of them, and to report them to the tester.
 * 
 * @author ICT-2
 */
public class SecurityReporter extends SecurityManager {
	/**
	 * Int exit code to signify whether the jar under test is trying to
	 * terminate the program.
	 */
	private static final int WATCHDOG_EXIT_CODE = 302590835;

	/**
	 * List of permissions attempted by the jar under test.
	 */
	private LinkedList<PermissionEvent> permissionEvents;

	/**
	 * Reference to stdOut, in case the jar under test uses System.setOut.
	 */
	private PrintStream stdOut;

	/**
	 * Constructs a new security reporter with the specified standard out.
	 * 
	 * @param stdOut
	 *            - output stream to print log to
	 */
	public SecurityReporter(PrintStream stdOut) {
		this.stdOut = stdOut;
		permissionEvents = new LinkedList<PermissionEvent>();
	}

	/**
	 * Logs a permission requested.
	 * <p>
	 * This method attempts to allow the security reporter to circumvent itself,
	 * disabling the security manager if the permission to check originated from
	 * the security reporter.
	 * <p>
	 * Note, it is possible for a jar under test to perform unlogged operations
	 * using a different thread while the SecurityReporter has disabled itself.
	 * This could be prevented by granting permissions by looking at the stack
	 * trace, instead of disabling the security manager.
	 * 
	 * @param toCheck
	 *            - permission to check and possibly log
	 */
	@Override
	public void checkPermission(Permission toCheck) {
		StackTraceElement[] thread = Thread.currentThread().getStackTrace();
		// allow this class to set the security manager
		if (toCheck.getName().equals("setSecurityManager")) {
			for (StackTraceElement element : thread) {
				if (("" + element).startsWith("contest.winter2017.SecurityReporter")) {
					return;
				} else if (!("" + element).startsWith("java.lang")) {
					throw new SecurityException("Jar under test may not change security manager.");
				}
			}
		}

		// disable manager to do real work
		System.setSecurityManager(null);

		if (toCheck instanceof RuntimePermission && toCheck.getName().contains("exitVM")) {
			// log exit permissions if they dont use the special int
			if (toCheck.getName().contains("" + WATCHDOG_EXIT_CODE)) {
				outputSecurityLog();
			} else {
				permissionEvents.add(new PermissionEvent(toCheck));
				outputSecurityLog();
			}
		} else {
			permissionEvents.add(new PermissionEvent(toCheck));
		}

		// enable manager
		System.setSecurityManager(this);
	}

	/**
	 * Outputs security events seen so far.
	 */
	public void outputSecurityLog() {
		PrintStream out = System.out;
		System.setOut(this.stdOut);
		System.out.println("<<WATCHDOG_OUTPUT_START>>");
		for (PermissionEvent event : permissionEvents) {
			Permission perm = event.getPermission();
			String name;
			if (perm instanceof FilePermission || perm instanceof PropertyPermission) {
				name = perm.getClass().getSimpleName();
			} else {
				name = perm.getName().split("\\Q.\\E")[0];
			}
			System.out.println(name);
		}
		System.out.println("<<WATCHDOG_OUTPUT_END>>");
		System.setOut(out);
	}
}

/**
 * Class to encapsulate a permission request. In the future, this could also
 * hold the stack trace at the permission. This could be parsed to find where
 * dubious permissions are executed from.
 * 
 * @author ICT-2
 */

class PermissionEvent {
	/**
	 * Permission logged to this permission event.
	 */
	private Permission permission;

	/**
	 * Constructs a PermissionEvent with the given permission and StackTrace.
	 * 
	 * @param permision
	 *            - permission to log
	 * @param traceElements
	 *            - stack trace to log
	 */
	public PermissionEvent(Permission permission) {
		this.permission = permission;
	}

	/**
	 * Gets the permission logged to this permission event.
	 * 
	 * @return permission logged to this permission event
	 */
	public Permission getPermission() {
		return this.permission;
	}
}