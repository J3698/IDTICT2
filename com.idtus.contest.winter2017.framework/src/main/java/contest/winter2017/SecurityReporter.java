package contest.winter2017;

import java.io.FilePermission;
import java.io.PrintStream;
import java.net.SocketPermission;
import java.security.Permission;
import java.security.UnresolvedPermission;
import java.util.LinkedList;

import javax.security.auth.PrivateCredentialPermission;
import javax.security.auth.kerberos.DelegationPermission;
import javax.security.auth.kerberos.ServicePermission;

/**
 * 
 * 
 * @author ICT-2
 */
public class SecurityReporter extends SecurityManager {
	private static final int WATCHDOG_EXIT_CODE = 302590835;

	private LinkedList<PermissionEvent> permissionEvents;
	private PrintStream stdOut;

	/** 
	 * Ctr for SecurityReporter
	 * 
	 * @param stdOutu - output stream to print log to
	 */
	public SecurityReporter(PrintStream stdOut) {
		this.stdOut = stdOut;
		permissionEvents = new LinkedList<PermissionEvent>();
	}

	/**
	 * Method to log permissions used. This method attempts
	 * to allow the SecurityReporter to circumvent itself,
	 * by disabling the security manager if the permission
	 * to check originated from the SecurityReporter.
	 * <p>
	 * Note, it is possible for a jar under test to perform
	 * unlogged operations using a different thread. This
	 * could be prevented by granting permissions by looking
	 * at the stacktrace, instead of disabling the security
	 * manager.
	 * 
	 * @param toCheck - permission to check and possible log
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
					throw new SecurityException(
							"Jar under test may not change security manager.");
				}
			}
		}

		// disable manager
		System.setSecurityManager(null);

		if (toCheck instanceof RuntimePermission && toCheck.getName().contains("exitVM")) {
			if (toCheck.getName().contains("" + WATCHDOG_EXIT_CODE)) {
				System.out.println("<<WATCHDOG_PROGRAM_END>>");
			} else {
				permissionEvents.add(new PermissionEvent(toCheck, thread));
			}
			outputSecurityLog();
		} else {
			permissionEvents.add(new PermissionEvent(toCheck, thread));
		}

		// enable manager
		System.setSecurityManager(this);
	}

	/**
	 * Method to output security events seen so far.
	 */
	public void outputSecurityLog() {
		PrintStream out = System.out;
		System.setOut(this.stdOut);
		System.out.println("<<WATCHDOG_OUTPUT_START>>");
		for (PermissionEvent event : permissionEvents) {
			Permission perm = event.getPermission();
			String name;
			if (perm instanceof UnresolvedPermission || perm instanceof FilePermission ||
					perm instanceof SocketPermission || perm instanceof PrivateCredentialPermission ||
					perm instanceof DelegationPermission || perm instanceof ServicePermission) {
				name = perm.getClass().getSimpleName();
			} else {
				name = perm.getName();
				int pos = name.indexOf('.');
				if (pos != -1) {
					name = name.substring(0, pos);
				}
			}
			System.out.println(name);
		}
		System.out.println("<<WATCHDOG_OUTPUT_END>>");
		System.setOut(out);
	}
}


class PermissionEvent {
	private Permission permission;
	private StackTraceElement[] traceElements;

	public PermissionEvent(Permission permision, StackTraceElement[] traceElements) {
		this.permission = permision;
		this.traceElements = traceElements;
	}

	public Permission getPermission() {
		return this.permission;
	}

	public StackTraceElement[] getTraceElements() {
		return this.traceElements;
	}
}



// on possible security issues :
	// get stack trace
	// make sure it's not the 
		// securitymanager making the call
	// save the warning


/*
checkAccept(String host, int port)
checkAccess(Thread t)
checkAccess(ThreadGroup g)
checkAwtEventQueueAccess()
checkConnect(String host, int port)
checkConnect(String host, int port, Object context)
checkCreateClassLoader()
checkDelete(String file)
checkExec(String cmd)
checkExit(int status)
checkLink(String lib)
checkListen(int port)
checkMemberAccess(Class<?> clazz, int which)
checkMulticast(InetAddress maddr)
checkPackageAccess(String pkg)
checkPackageDefinition(String pkg)
checkPermission(Permission perm)
checkPermission(Permission perm, Object context)
checkPrintJobAccess()
checkPropertiesAccess()
checkPropertyAccess(String key)
checkRead(FileDescriptor fd)
checkRead(String file)
checkRead(String file, Object context)
checkSecurityAccess(String target)
checkSetFactory()
checkSystemClipboardAccess()
checkTopLevelWindow(Object window)
checkWrite(FileDescriptor fd)
checkWrite(String file)
*/