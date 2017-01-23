package contest.winter2017;

import java.util.List;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Class to hold output (std out/err) associated with a given test run.
 * This class also holds permissions used within a test run.
 * 
 * @author IDT
 */
class Output {

	/**
	 * Pattern to find exceptions from the standard error associated with a given test run
	 */
	private static Pattern exceptionFinder = Pattern.compile(
			"(\\n|^).+Exception[^\\n]+(\\n\\t+\\Qat \\E.+)+(\n\\QCaused by\\E[^\\n]+(\\n\\t+\\Qat \\E.+)+)*");

	/**
	 * String of the standard out associated with a given test run
	 */
	private String stdOutString = null;
	
	/**
	 * String of the standard error associated with a given test run 
	 */
	private String stdErrString = null;

	/**
	 * HashMap representing how many times different permissions have been used
	 */
	private HashMap<String, Integer> permissionLogMap = null;

	/**
	 * Ctr for Output object
	 * @param stdOutString - std out string to hold
	 * @param stdErrString - std err string to hold
	 */
	public Output(String stdOutString, String stdErrString) {
		this.stdOutString = stdOutString;
		this.stdErrString = stdErrString;
		this.permissionLogMap = new HashMap<String, Integer>();
	}

	/**
	 * Ctr for Output object
	 */
	public Output() {
		this("", "");
	}

	 /**
	 * Method to log a permission request to this output
	 * 
	 * @param message - security log to add to this output
	 */
	public void logPermission(String message) {
		if (this.permissionLogMap.keySet().contains(message)) {
			int num = this.permissionLogMap.get(message);
			this.permissionLogMap.put(message, num + 1);
		} else {
			this.permissionLogMap.put(message, 1);
		}
	}

	/**
	 * Method to reset permission log map. This may
	 * become necessary if the security reporter
	 * decides to output an updated security log.
	 */
	public void resetPermissionLog() {
		this.permissionLogMap = new HashMap<String, Integer>();
	}

	/**
	 * Getter for permsission log string
	 * @return String representation of the permission log associated with a given test run
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
	 * Getter for std out string
	 * @return String representation of std out associated with a given test run
	 */
	public String getStdOutString() {
		return stdOutString;
	}
	
	/**
	 * Setter for std out string
	 * @param stdOutString - String representation of std out associated with a given test run
	 */
	public void setStdOutString(String stdOutString) {
		this.stdOutString = stdOutString;
	}

	/**
	 * Getter for exceptions parsed from the stdErrString of a given test run
	 * 
	 * @return List<String> representation of exceptions
	 */
	public Set<String> getExceptions() {
		String toSplit = this.stdErrString;

		Set<String> exceptions = new HashSet<String>();
		Matcher matcher = exceptionFinder.matcher(toSplit);
		while (matcher.find()) {
			String match = matcher.group(0);
			toSplit.replace(match, "<<SPLIT_ERROR_STRING>>");
			exceptions.add(match);
		}
		StringTokenizer tokenizer = new StringTokenizer(toSplit, "<<SPLIT_ERROR_STRING>>");
		while (tokenizer.hasMoreTokens()) {
			exceptions.add(tokenizer.nextToken());
		}

		return exceptions;
	}

	/**
	 * Getter for std err string
	 * @return String representation of std err associated with a given test run
	 */
	public String getStdErrString() {
		return stdErrString;
	}
	
	/**
	 * Setter for std err string
	 * @param stdErrString - String representation of std err associated with a given test run
	 */
	public void setStdErrString(String stdErrString) {
		this.stdErrString = stdErrString;
	}
}
