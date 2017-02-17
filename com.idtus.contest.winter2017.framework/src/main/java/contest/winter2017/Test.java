package contest.winter2017;

import java.util.List;
import java.util.Map;

/**
 * This class represents a basic test that is extracted from the executable jar.
 * These tests are run against the executable jar to determine a pass/fail
 * status.
 * 
 * @author IDT
 */
public class Test {
	/**
	 * List of parameter values that will be passed into the executable jar as a
	 * single test.
	 */
	@SuppressWarnings("rawtypes")
	private List parameters;

	/**
	 * Regex string that describes the expected standard out result for the
	 * test.
	 */
	private String stdOutExpectedResultRegex;

	/**
	 * Regex string that describes the expected standard err result for the
	 * test.
	 */
	private String stdErrExpectedResultRegex;

	/**
	 * Constructs a test object with the given input map.
	 * 
	 * @param inputMap
	 *            - input map for the test
	 */
	@SuppressWarnings("rawtypes")
	public Test(Map inputMap) {
		this.parameters = (List) inputMap.get("parameters");
		this.stdOutExpectedResultRegex = (String) inputMap.get("stdOutExpectedResultRegex");
		this.stdErrExpectedResultRegex = (String) inputMap.get("stdErrExpectedResultRegex");
	}

	/**
	 * Gets the parameter list of this test.
	 * 
	 * @return a list of parameters for this test
	 */
	@SuppressWarnings("rawtypes")
	public List getParameters() {
		return parameters;
	}

	/**
	 * Gets the regex for the expected standard out.
	 * 
	 * @return the regex string for expected standard out
	 */
	public String getStdOutExpectedResultRegex() {
		return stdOutExpectedResultRegex;
	}

	/**
	 * Gets the regex for the expected standard error.
	 * 
	 * @return the regex string for expected standard error
	 */
	public String getStdErrExpectedResultRegex() {
		return stdErrExpectedResultRegex;
	}

}
