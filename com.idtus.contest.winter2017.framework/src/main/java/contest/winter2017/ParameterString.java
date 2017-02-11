package contest.winter2017;

import java.util.Set;

class ParameterString {
	private String string;
	private Set<Parameter> usedParameters;
	private int uniqueLines;
	private Set<ParameterString> children;
	private ParameterString parent;
	private Output output;

	public ParameterString(String string) {
		this.string = string;
	}

	/**
	 * @return the usedParameters
	 */
	public Set<Parameter> getUsedParameters() {
		return usedParameters;
	}

	/**
	 * @return the string
	 */
	public String getString() {
		return string;
	}

	/**
	 * @param String
	 *            - the string to set
	 */
	public void setString(String string) {
		this.string = string;
	}

	/**
	 * @return the unique lines
	 */
	public int getUniqueLines() {
		return uniqueLines;
	}

	/**
	 * @param uniqueLines
	 *            - number of unique lines to set
	 */
	public void setUniqueLines(int uniqueLines) {
		this.uniqueLines = uniqueLines;
	}

	/**
	 * @return the children
	 */
	public Set<ParameterString> getChildren() {
		return children;
	}

	/**
	 * @return the parent
	 */
	public ParameterString getParent() {
		return parent;
	}

	/**
	 * @return the output
	 */
	public Output getOutput() {
		return this.output;
	}

	/**
	 * @param output
	 *            - the output to set
	 */
	public void setOutput(Output output) {
		this.output = output;
	}
}