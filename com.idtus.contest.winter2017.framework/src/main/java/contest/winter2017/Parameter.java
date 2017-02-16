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
	 * Input Map associated with this parameter.
	 */
	@SuppressWarnings("rawtypes")
	private Map inputMap;

	/**
	 * Constructs a new Paraneter using the specified input map.
	 * 
	 * @param inputMap
	 *            - map containing parameter meta data
	 */
	@SuppressWarnings("rawtypes")
	public Parameter(Map inputMap) {
		this.inputMap = inputMap;
	}

	/**
	 * Gets the type of the parameter (integer, long, double, float, String,
	 * etc).
	 * 
	 * @return Class type of the parameter
	 */
	@SuppressWarnings("rawtypes")
	public Class getType() {
		return (Class) this.inputMap.get("type");
	}

	/**
	 * Returns whether this parameter is an enumeration or not, i.e. whether
	 * multiple options are associated with this parameter.
	 * 
	 * @return boolean true if this parameter is an enumeration, false if it is
	 *         not
	 */
	public boolean isEnumeration() {
		if (inputMap.get("enumerated values") != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets enumeration values if this parameter is an enumeration.
	 * 
	 * @return List<String> containing multiple options associated with this
	 *         parameter
	 */
	@SuppressWarnings("unchecked")
	public List<String> getEnumerationValues() {
		return (List<String>) inputMap.get("enumerated values");
	}

	/**
	 * Gets the min value associated with this parameter (if one exists).
	 * 
	 * @return Object representing the minimum value associated with this
	 *         parameter
	 */
	public Object getMin() {
		return inputMap.get("min");
	}

	/**
	 * Gets the max value associated with this parameter (if one exists).
	 * 
	 * @return Object representing the maximum value associated with this
	 *         parameter
	 */
	public Object getMax() {
		return inputMap.get("max");
	}

	/**
	 * Gets the optionality of this parameter.
	 * 
	 * @return boolean true indicates the parameter is optional
	 */
	public boolean isOptional() {
		if (inputMap.get("optional") != null) {
			return (Boolean) inputMap.get("optional");
		} else {
			return false;
		}
	}

	/**
	 * Gets the flag indicating whether or not the parameter has a specific
	 * format
	 * 
	 * @return boolean true indicates the parameter has a specific format
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
	 * Gets this parameter's format string if one exists
	 * 
	 * @return String with the parameters format <<REPLACE_ME_...>> are included
	 */
	public String getFormat() {
		return (String) inputMap.get("format");
	}

	/**
	 * Gets the types of the variables (eg. <<REPLACE_ME_STRING>>) in the format
	 * string
	 * 
	 * @return List<Class> that contains the types of each of the
	 *         <<REPLACE_ME_...>> in the format string
	 */
	@SuppressWarnings("rawtypes")
	public List<Class> getFormatVariables() {
		return getFormatVariables((String) inputMap.get("format"));
	}

	/**
	 * Gets the types of the variables (eg. <<REPLACE_ME_STRING>>) in the format
	 * string... useful for formatted enumerated values
	 * 
	 * @param format
	 *            - string containing the format for this parameter
	 * @return List<Class> that contains the types of each of the
	 *         <<REPLACE_ME_...>> in the format string
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
	 * Builds a valid formatted parameter by replacing all of the
	 * <<REPLACE_ME_...>> in the format parameter string
	 * 
	 * @param List<Object>
	 *            - containing the values that will replace the format for
	 *            <<REPLACE_ME_...>> placeholders of this formatted parameter
	 * @return String containing the parameter with <<REPLACE_ME_...>>
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
	 *            - containing the orginial format strin with the
	 *            <<REPLACE_ME_...>> in it
	 * @param List<Object>
	 *            - containing the values that will replace the format for
	 *            <<REPLACE_ME_...>> placeholders of this formatted parameter
	 * @return String containing the parameter with <<REPLACE_ME_...>>
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
	 * Gets the sub parameters of this parameter.
	 * <p>
	 * An enumerated parameter has multiple values, and more than one of them
	 * could be used. Therefore, it's useful to split a enumeration parameter
	 * into single-value sub-parameters to get different parameter combinations.
	 * 
	 * @return List of sub parameters
	 */
	/*
	 * public List<Parameter> getSubParameters() { List<Parameter> parameterList
	 * = new ArrayList<Parameter>(getEnumerationValues().size());
	 * 
	 * if (isEnumeration()) { Map<String, Object> parameterMap; List<String>
	 * toAdd; for (String str : getEnumerationValues()) { parameterMap = new
	 * HashMap<String, Object>(); toAdd = new LinkedList<String>();
	 * toAdd.add(str); if (str.contains(REPLACE_STRING) ||
	 * str.contains(REPLACE_INT) || str.contains(REPLACE_DOUBLE) ||
	 * str.contains(REPLACE_LONG)) {
	 * 
	 * parameterMap.put("format", str); if (str.contains(REPLACE_STRING)) {
	 * parameterMap.put("type", String.class); } else if
	 * (str.contains(REPLACE_INT)) { parameterMap.put("type", Integer.class); }
	 * else if (str.contains(REPLACE_LONG)) { parameterMap.put("type",
	 * Long.class); } else { parameterMap.put("type", Double.class); } }
	 * parameterMap.put("enumerated values", toAdd); parameterList.add(new
	 * Parameter(parameterMap)); } } else { parameterList.add(this); } return
	 * parameterList; }
	 */

	/**
	 * Returns a string representation of this parameter.
	 * <p>
	 * If this Parameter is an enumeration, this method returns the toString of
	 * the corresponding list of values. Otherwise, this method returns the
	 * format string of this Parameter.
	 * 
	 * @return String representing this parameter
	 */
	@Override
	public String toString() {
		if (this.toString != null) {
			return this.toString;
		} else {
			if (isEnumeration()) {
				this.toString = "" + this.inputMap.get("enumerated values");
			} else {
				this.toString = getFormat();
			}
			return this.toString;
		}
	}

	/**
	 * Returns a hash code value for this parameter.
	 * <p>
	 * The hash code is generated using the hashcode of this parameter's
	 * toString.
	 * 
	 * @return an integer hashcode for this parameter
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * Returns whether this parameter is equal to another parameter.
	 * <p>
	 * Equality testing is done by comparing the string representation of the
	 * parameters being compared.
	 * 
	 * @return boolean whether this parameter and the other parameter are equal
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