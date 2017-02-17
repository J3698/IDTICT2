package contest.winter2017;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that represents a single parameter for an executable jar.
 * 
 * @author IDT
 */
public class Parameter {
	public static final String REPLACE_STRING = "<<REPLACE_ME_STRING>>";
	public static final String REPLACE_INT = "<<REPLACE_ME_INT>>";
	public static final String REPLACE_LONG = "<<REPLACE_ME_LONG>>";
	public static final String REPLACE_DOUBLE = "<<REPLACE_ME_DOUBLE>>";

	public static final String[] REPLACABLES = new String[] { REPLACE_STRING, REPLACE_INT, REPLACE_LONG,
			REPLACE_DOUBLE };

	/**
	 * Pattern to replace if this parameter is formatted.
	 */
	private static Pattern replaceMePattern = Pattern.compile("<<REPLACE_ME_(STRING|INT|DOUBLE|LONG)>>");

	/**
	 * String representation of this parameter.
	 */
	private String toString = null;

	/**
	 * Input map associated with this parameter.
	 */
	@SuppressWarnings("rawtypes")
	private Map inputMap;

	/**
	 * String regex key associated with this parameter.
	 */
	private String regexKey = null;

	/**
	 * Constructs a new parameter using the specified input map and key.
	 * 
	 * @param inputMap
	 *            - map containing parameter meta data
	 * @param key
	 *            - key for this parameter
	 */
	@SuppressWarnings("rawtypes")
	public Parameter(Map inputMap, String regexKey) {
		this.inputMap = inputMap;
		this.regexKey = regexKey;
	}

	/**
	 * Constructs a new parameter using the specified input map.
	 * 
	 * @param inputMap
	 *            - map containing parameter meta data
	 */
	@SuppressWarnings("rawtypes")
	public Parameter(Map inputMap) {
		this.inputMap = inputMap;
	}

	/**
	 * Returns the type of the parameter (integer, long, double, float, String,
	 * etc).
	 * 
	 * @return the class type of the parameter
	 */
	@SuppressWarnings("rawtypes")
	public Class getType() {
		return (Class) this.inputMap.get("type");
	}

	/**
	 * Returns whether this parameter is an enumeration or not, i.e. whether
	 * multiple options are associated with this parameter.
	 * 
	 * @return true if this parameter is an enumeration, false if it is not
	 */
	public boolean isEnumeration() {
		if (inputMap.get("enumerated values") != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the enumeration values if this parameter is an enumeration.
	 * 
	 * @return a string list containing the enumeration values associated with
	 *         this parameter
	 */
	@SuppressWarnings("unchecked")
	public List<String> getEnumerationValues() {
		return (List<String>) inputMap.get("enumerated values");
	}

	/**
	 * Returns the minimum value associated with this parameter (if one exists).
	 * 
	 * @return an object representing the minimum value associated with this
	 *         parameter
	 */
	public Object getMin() {
		return inputMap.get("min");
	}

	/**
	 * Returns the maximum value associated with this parameter (if one exists).
	 * 
	 * @return an object representing the maximum value associated with this
	 *         parameter
	 */
	public Object getMax() {
		return inputMap.get("max");
	}

	/**
	 * Returns whether this parameter is optional.
	 * 
	 * @return true if the parameter is optional, otherwise false
	 */
	public boolean isOptional() {
		if (inputMap.get("optional") != null) {
			return (Boolean) inputMap.get("optional");
		} else {
			return false;
		}
	}

	/**
	 * Returns the flag indicating whether or not the parameter has a specific
	 * format.
	 * 
	 * @return true if the parameter has a specific format, otherwise false
	 */
	public boolean isFormatted() {
		if (isEnumeration()) {
			for (String enumValue : getEnumerationValues()) {
				if (enumValue.matches(".*<<REPLACE_ME_(STRING|INT|DOUBLE|LONG)>>.*")) {
					return true;
				}
			}
		} else {
			if (inputMap.get("format") != null) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns this parameter's format string if one exists.
	 * 
	 * @return String with the parameters format <<REPLACE_ME_...>> are included
	 */
	public String getFormat() {
		return (String) inputMap.get("format");
	}

	/**
	 * Returns the types of the variables (eg. <<REPLACE_ME_STRING>>) in the
	 * format string
	 * 
	 * @return List<Class> that contains the types of each of the
	 *         <<REPLACE_ME_...>> in the format string
	 */
	@SuppressWarnings("rawtypes")
	public List<Class> getFormatVariables() {
		return getFormatVariables((String) inputMap.get("format"));
	}

	/**
	 * Returns the types of the variables (e.g. <<REPLACE_ME_STRING>>) in the
	 * format string.
	 * <p>
	 * This method is useful for formatting formatted enumerated values.
	 * 
	 * @param format
	 *            - string containing the format for this parameter
	 * @return string list that contains the types of each <<REPLACE_ME_...>> in
	 *         the format string
	 */
	@SuppressWarnings("rawtypes")
	public static List<Class> getFormatVariables(String format) {

		if (format == null) {
			return null;
		}

		List<Class> typeList = new ArrayList<Class>();

		Matcher replaceMeMatcher = Parameter.replaceMePattern.matcher(format);
		while (replaceMeMatcher.find()) {
			switch (replaceMeMatcher.group()) {
			case REPLACE_STRING: {
				typeList.add(String.class);
				break;
			}
			case REPLACE_INT: {
				typeList.add(Integer.class);
				break;
			}
			case REPLACE_DOUBLE: {
				typeList.add(Double.class);
				break;
			}
			case REPLACE_LONG: {
				typeList.add(Long.class);
				break;
			}
			default: {
				// NOP
				break;
			}
			}
		}

		return typeList;
	}

	/**
	 * Builds a valid formatted parameter by replacing each <<REPLACE_ME_...>>
	 * in the format parameter string
	 * 
	 * @param List<Object>
	 *            - containing the values that will replace the format for
	 *            <<REPLACE_ME_...>> placeholders of this formatted parameter
	 * @return a string containing the parameter with <<REPLACE_ME_...>>
	 *         placeholders replaced with the passed in values
	 */
	public String getFormattedParameter(List<Object> formatVariableValues) {
		return getFormattedParameter((String) inputMap.get("format"), formatVariableValues);
	}

	/**
	 * Builds a valid formatted parameter by replacing all of the
	 * <<REPLACE_ME_...>> in the format parameter string
	 * 
	 * @param String
	 *            - containing the original format string with the
	 *            <<REPLACE_ME_...>> in it
	 * @param List<Object>
	 *            - containing the values that will replace the format for
	 *            <<REPLACE_ME_...>> placeholders of this formatted parameter
	 * @return a string containing the parameter with <<REPLACE_ME_...>>
	 *         placeholders replaced with the passed in values
	 */
	public static String getFormattedParameter(String format, List<Object> formatVariableValues) {
		Matcher replaceMeMatcher = replaceMePattern.matcher(format);
		StringBuffer sb = new StringBuffer();
		for (Object variable : formatVariableValues) {
			if (replaceMeMatcher.find()) {
				replaceMeMatcher.appendReplacement(sb, variable.toString());
			}
		}
		replaceMeMatcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Returns a string representation of this parameter.
	 * <p>
	 * If this parameter has a key, the key is prefixed to the to string. The to
	 * string is created using enumerated values if they exist, otherwise the
	 * format string if it exists, otherwise the parameter type.
	 * 
	 * @return a string representing this parameter
	 */
	@Override
	public String toString() {
		if (this.toString != null) {
			return this.toString;
		} else {
			if (this.regexKey != null) {
				this.toString = this.regexKey + "=";
			}
			if (isEnumeration()) {
				this.toString += "" + this.inputMap.get("enumerated values");
			} else if (getFormat() != null) {
				this.toString += getFormat();
			} else {
				this.toString += "" + this.getType();
			}
			return this.toString;
		}
	}

	/**
	 * Returns a hash code value for this parameter.
	 * <p>
	 * The hash code is generated using the hash code of this parameter's
	 * toString.
	 * 
	 * @return an integer hash-code for this parameter
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * Returns whether this parameter is equal to another parameter.
	 * <p>
	 * Equality testing is done by comparing the string representation of the
	 * parameters.
	 * 
	 * @return true if this parameter and the other are equal, otherwise false
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Parameter)) {
			return false;
		} else {
			return ("" + this).equals("" + other);
		}
	}
}