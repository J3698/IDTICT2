package contest.winter2017;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jacoco.core.analysis.CoverageBuilder;

/**
 * Class to hold output associated with a given test run. Output includes the
 * standard err, standard out, coverage builder, and permissions requested
 * during a test.
 * 
 * @author IDT
 */
public class Output {
	/**
	 * Pattern to find exceptions from the standard error associated with a
	 * given test run.
	 */
	private static Pattern exceptionFinder = Pattern
			.compile("(\\n|^).+Exception[^\\n]+(\\n\\t+\\Qat \\E.+)+(\n\\QCaused by\\E[^\\n]+(\\n\\t+\\Qat \\E.+)+)*");

	/**
	 * String command used for this test
	 */
	private String command;

	/**
	 * String of the standard out associated with a given test run.
	 */
	private String stdOutString = null;

	/**
	 * String of the standard error associated with a given test run.
	 */
	private String stdErrString = null;

	/**
	 * Coverage builder associated with a given test run.
	 */
	private CoverageBuilder coverageBuilder;

	/**
	 * HashMap representing how many times different permissions have been used
	 * during a given test run.
	 */
	private HashMap<String, Integer> permissionLogMap = null;

	/**
	 * Constructs a new output object with the specified output and error
	 * strings.
	 * 
	 * @param stdOutString
	 *            - standard out string to store
	 * @param stdErrString
	 *            - standard err string to store
	 */
	public Output(String command, String stdOutString, String stdErrString) {
		this.command = command;
		this.stdOutString = stdOutString;
		this.stdErrString = stdErrString;
		this.permissionLogMap = new HashMap<String, Integer>();
	}

	/**
	 * Constructs a new output object with no initial command, output or error
	 * strings, or coverage builder.
	 */
	public Output() {
		this("", "", "");
	}

	/**
	 * Logs the specified permission request to this output.
	 * 
	 * @param permission
	 *            - permission to log to this output
	 */
	public void logPermission(String permission) {
		if (this.permissionLogMap.keySet().contains(permission)) {
			int num = this.permissionLogMap.get(permission);
			this.permissionLogMap.put(permission, num + 1);
		} else {
			this.permissionLogMap.put(permission, 1);
		}
	}

	/**
	 * Resets the permission log HashMap.
	 * <p>
	 * This method becomes necessary if the security reporter decides to output
	 * an updated security log.
	 */
	public void resetPermissionLog() {
		this.permissionLogMap = new HashMap<String, Integer>();
	}

	/**
	 * Gets the permission log string.
	 * <p>
	 * The permission log string is created by appending the number of times a
	 * permission is used to its name. The name-number pairs are then appended
	 * together on one line.
	 * 
	 * @return String representation of the permission log associated with a
	 *         given test run
	 */
	public String getPermissionLogString() {
		StringBuffer out = new StringBuffer();
		for (Entry<String, Integer> entry : this.permissionLogMap.entrySet()) {
			String rep = entry.getKey() + "(" + entry.getValue() + "x) ";
			out.append(rep);
		}
		return out.toString();
	}

	/**
	 * Returns a map of permissions used to their occurrences.
	 * 
	 * @return a map of permissions used to their occurrences
	 */
	public HashMap<String, Integer> getPermissionMap() {
		return this.permissionLogMap;
	}

	/**
	 * Gets the standard out string.
	 * 
	 * @return String representation of standard out associated with a given
	 *         test run
	 */
	public String getStdOutString() {
		return stdOutString;
	}

	/**
	 * Sets the standard out string.
	 * 
	 * @param stdOutString
	 *            - String representation of standard out associated with a
	 *            given test run
	 */
	public void setStdOutString(String stdOutString) {
		this.stdOutString = stdOutString;
	}

	/**
	 * Gets the exceptions/errors parsed from the stdErrString of a given test
	 * run.
	 * <p>
	 * The standard err string is parsed using a regular exception. The regular
	 * expression parses Exceptions (including chained exceptions) from each
	 * other, and from other standard error output. Exceptions and other error
	 * output are added to a set to remove duplicates.
	 * 
	 * @return a string list of representation of exceptions and error messages
	 */
	public Set<String> getExceptions() {
		// find exceptions
		String toSplit = this.stdErrString;
		Set<String> exceptions = new HashSet<String>();
		Matcher matcher = exceptionFinder.matcher(toSplit);
		while (matcher.find()) {
			String match = matcher.group(0);
			toSplit = toSplit.replace(match, "SPLITERRORSTRING");
			exceptions.add(match);
		}

		// parse other standard error output
		toSplit = toSplit.replace("\n", " ");
		for (String err : toSplit.split("SPLITERRORSTRING")) {
			if (!err.trim().equals("")) {
				exceptions.add(err);
			}
		}

		return exceptions;
	}

	/**
	 * Gets the standard err string.
	 * 
	 * @return String representation of standard err associated with a given
	 *         test run
	 */
	public String getStdErrString() {
		return stdErrString;
	}

	/**
	 * Sets the standard err string.
	 * 
	 * @param stdErrString
	 *            - String representation of standard err associated with a
	 *            given test run
	 */
	public void setStdErrString(String stdErrString) {
		this.stdErrString = stdErrString;
	}

	/**
	 * Deletes the coverage builder associated with this output.
	 * 
	 * @return true if there was a coverage builder to remove, otherwise false
	 */
	public boolean clearBuilder() {
		boolean toReturn = (this.coverageBuilder != null);
		this.coverageBuilder = null;
		return toReturn;
	}

	/**
	 * Returns this outputs coverage builder.
	 * 
	 * @return this output's coverage builder
	 */
	public CoverageBuilder getCoverageBuilder() {
		return this.coverageBuilder;
	}

	/**
	 * Sets the coverage builder.
	 * 
	 * @param builder
	 *            - coverage builder to set
	 */
	public void setCoverageBuilder(CoverageBuilder builder) {
		this.coverageBuilder = builder;
	}

	/**
	 * Returns the command that ran this output's test.
	 * 
	 * @return the command that ran this output's test
	 */
	public String getCommand() {
		return this.command;
	}

	/**
	 * Sets the command.
	 * 
	 * @param command
	 *            - command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}
}
