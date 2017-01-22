package contest.winter2017;

import java.util.LinkedList;

/**
 * Class to hold output (std out/err) associated with a given test run 
 * 
 * @author IDT
 */
class Output {

	/**
	 * String of the standard out associated with a given test run
	 */
	private String stdOutString = null;
	
	/**
	 * String of a standard error associated with a given test run 
	 */
	private String stdErrString = null;

	private StringBuffer securityLogString;

	/**
	 * Ctr for Output object
	 * @param stdOutString - std out string to hold
	 * @param stdErrString - std err string to hold
	 */
	public Output(String stdOutString, String stdErrString) {
		this.stdOutString = stdOutString;
		this.stdErrString = stdErrString;
		securityLogString = new StringBuffer();
	}

	/**
	 * Ctr for Output object
	 */
	public Output() {
		this("", "");
	}

	 /**
	 * Method to add a security message to this output
	 * 
	 * @param message - message to add to this output
	 */
	public void addSecurityMessage(String message) {
		securityLogString.append(message + " ");
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
	 * @param String representation of std out associated with a given test run
	 */
	public void setStdOutString(String stdOutString) {
		this.stdOutString = stdOutString;
	}

	/**
	 * Setter for std err string
	 * @return String representation of std err associated with a given test run
	 */
	public String setStdErrString(String stdErrString) {
		return stdErrString;
	}

	/**
	 * Getter for std err string
	 * @return String representation of std err associated with a given test run
	 */
	public String getStdErrString() {
		return stdErrString;
	}

	/**
	 * Method to reset security log string. This
	 * may become necessary if the security
	 * reporter decides to output an updated
	 * security log.
	 */
	public void resetSecurityLogString() {
		this.securityLogString = new StringBuffer();
	}

	/**
	 * Getter for security log strin
	 * @return String representation of the security log associated with a given test run
	 */
	public String getSecurityLogString() {
		return this.securityLogString.toString();
	}
	
}
