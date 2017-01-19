package contest.winter2017;

import java.security.Permission;
import java.util.LinkedList;

public class SecurityReporter extends SecurityManager {
	private LinkedList<PermissionEvent> permissionEvents;

	public SecurityReporter() {
		permissionEvents = new LinkedList<PermissionEvent>();
	}

	@Override
	public void checkPermission(Permission toCheck) {
		StackTraceElement[] traceElements = new Throwable().getStackTrace();
		PermissionEvent event = new PermissionEvent(toCheck, traceElements);
		permissionEvents.add(event);
	}
}

class PermissionEvent {
	private Permission permission;
	private StackTraceElement[] traceElements;

	public PermissionEvent(Permission permision, StackTraceElement[] traceElements) {
		this.permission = permision;
		this.traceElements = traceElements;
	}
}
// on possible security issues :
	// get stack trace
	// make sure it's not the 
		// securitymanager making the call
	// save the warning