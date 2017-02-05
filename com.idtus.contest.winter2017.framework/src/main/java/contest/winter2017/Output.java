package contest.winter2017;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jacoco.core.analysis.CoverageBuilder;

/**
 * Class to hold output associated with a given test run. Output includes
 * standard err, standard out, and permissions requested during a test.
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
	 * String of the standard out associated with a given test run.
	 */
	private String stdOutString = null;

	/**
	 * String of the standard error associated with a given test run.
	 */
	private String stdErrString = null;

	/**
	 * Coeverage builder associated with a given test run.
	 */
	private CoverageBuilder coverageBuilder;

	/**
	 * HashMap representing how many times different permissions have been used
	 * during a given test run.
	 */
	private HashMap<String, Integer> permissionLogMap = null;

	/**
	 * Constructs a new Output object with the specified output and error
	 * strings.
	 * 
	 * @param stdOutString
	 *            - std out string to store
	 * @param stdErrString
	 *            - std err string to store
	 */
	public Output(String stdOutString, String stdErrString) {
		this.stdOutString = stdOutString;
		this.stdErrString = stdErrString;
		this.permissionLogMap = new HashMap<String, Integer>();
	}

	/**
	 * Constructs a new Output object with no initial output or error strings.
	 * Ctr for the Output object.
	 */
	public Output() {
		this("", "");
	}

	/**
	 * Logs the specified permission request to this output.
	 * 
	 * @param message
	 *            - permission to log to this output
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
	 * Gets the std out string.
	 * 
	 * @return String representation of std out associated with a given test run
	 */
	public String getStdOutString() {
		return stdOutString;
	}

	/**
	 * Sets the std out string.
	 * 
	 * @param stdOutString
	 *            - String representation of std out associated with a given
	 *            test run
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
	 * @return List<String> representation of exceptions and error messages
	 */
	public Set<String> getExceptions() {
		String toSplit = this.stdErrString;
		System.out.println(this.stdErrString);
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
	 * Gets the std err string.
	 * 
	 * @return String representation of std err associated with a given test run
	 */
	public String getStdErrString() {
		return stdErrString;
	}

	/**
	 * Sets the std err string.
	 * 
	 * @param stdErrString
	 *            - String representation of std err associated with a given
	 *            test run
	 */
	public void setStdErrString(String stdErrString) {
		this.stdErrString = stdErrString;
	}

	/**
	 * Returns this outputs coverage builder.
	 * 
	 * @return this outputs coverage builder
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
}
