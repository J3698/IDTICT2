package contest.winter2017;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
	private static final String REPLACE_STRING = "<<REPLACE_ME_STRING>>";
	private static final String REPLACE_INT = "<<REPLACE_ME_INT>>";
	private static final String REPLACE_DOUBLE = "<<REPLACE_ME_DOUBLE>>";

	/**
	 * String representation of this parameter
	 */
	private String toString = null;

	/**
	 * Input Map associated with this parameter
	 */
	@SuppressWarnings("rawtypes")
	private Map inputMap;
	
	
	/**
	 * Pattern to replace if the parameter is formatted
	 */
	private Pattern replaceMePattern = null;


	/**
	 * Ctr for Parameter
	 * @param inputMap - map containing parameter meta data
	 */
	@SuppressWarnings("rawtypes")
	public Parameter(Map inputMap) {
		this.inputMap = inputMap;
	}

	/**
	 * Getter for type of parameter (integer, long, double, float, String, etc)
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Class getType() {
		return (Class) this.inputMap.get("type");
	}

	/**
	 * Method to find out if this parameter is an enumeration or not, 
	 * meaning that there are multiple options associated with this parameter
	 * @return boolean true if this parameter is an enumeration, false if it is not
	 */
	public boolean isEnumeration() {
		if (inputMap.get("enumerated values") != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Getter for enumeration values (if this parameter is an enumeration)
	 * @return List<String> containing multiple options associated with this parameter
	 */
	@SuppressWarnings("unchecked")
	public List<String> getEnumerationValues() {
		return (List<String>) inputMap.get("enumerated values");
	}

	/**
	 * Getter for the min value associated with this parameter (if one exists)
	 * @return Object representing the minimum value associated with this parameter
	 */
	public Object getMin() {
		return inputMap.get("min");
	}

	/**
	 * Getter for the max value associated with this parameter (if one exists)
	 * @return Object representing the maximum value associated with this parameter
	 */
	public Object getMax() {
		return inputMap.get("max");
	}

	/**
	 * Getter for the optionality of the parameter
	 * @return boolean true indicates the parameter is optional
	 */
	public boolean isOptional() {
		if(inputMap.get("optional") != null) {
			return (Boolean)inputMap.get("optional");
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * Getter for the flag indicating whether or not the parameter has a specific format
	 * @return boolean true indicates the parameter has a specific format
	 */
	public boolean isFormatted() {
		if(isEnumeration()) {
			for(String enumValue : getEnumerationValues()) {
				if(enumValue.matches(".*<<REPLACE_ME_(STRING|INT|DOUBLE)>>.*")) {
					return true;
				}
			}
		}
		else {
			if(inputMap.get("format") != null) {
				return true;
			}
		}

		return false;
	}
	
	
	/**
	 * Getter for the format string the parameter has if it has a specific one
	 * @return String with the parameters format <<REPLACE_ME_...>> are included
	 */
	public String getFormat() {
		return (String)inputMap.get("format");
	}

	/**
	 * Getter for the types of the variables (eg. <<REPLACE_ME_STRING>>) in the format string
	 * @return List<Class> that contains the types of each of the <<REPLACE_ME_...>> in the format string
	 */
	@SuppressWarnings("rawtypes")
	public List<Class> getFormatVariables() {
		return getFormatVariables((String)inputMap.get("format"));
	}
	
	
	/**
	 * Getter for the types of the variables (eg. <<REPLACE_ME_STRING>>) in the format string... useful for formatted enumerated values
	 * @param format - string containing the format for this parameter
	 * @return List<Class> that contains the types of each of the <<REPLACE_ME_...>> in the format string
	 */
	@SuppressWarnings("rawtypes")
	public List<Class> getFormatVariables(String format) {
		
		if(format == null) {
			return null;
		}
		
		List<Class> typeList = new ArrayList<Class>();
		
		this.replaceMePattern = Pattern.compile("<<REPLACE_ME_(STRING|INT|DOUBLE)>>");
		Matcher replaceMeMatcher = replaceMePattern.matcher(format);
		while(replaceMeMatcher.find()) {
			switch(replaceMeMatcher.group()) {
				case REPLACE_STRING: {
					typeList.add(String.class);
					break;
				}
				case REPLACE_INT: {
					typeList.add(Integer.class);
					break;
				}
				default: {
					//NOP
					break;
				}
			}
		}

		return typeList;
	}


	/**
	 * Utility method to build a valid formatted parameter by replacing all of the <<REPLACE_ME_...>> in the format parameter string
	 * @param List<Object> - containing the values that will replace the format for <<REPLACE_ME_...>> placeholders of this formatted parameter
	 * @return String containing the parameter with <<REPLACE_ME_...>> placeholders replaced with the passed in values
	 */
	public String getFormattedParameter(List<Object> formatVariableValues) {
		return getFormattedParameter((String)inputMap.get("format"), formatVariableValues);
	}

	/**
	 * Utility method to build a valid formatted parameter by replacing all of the <<REPLACE_ME_...>> in the format parameter string
	 * @param String - containing the orginial format strin with the <<REPLACE_ME_...>> in it
	 * @param List<Object> - containing the values that will replace the format for <<REPLACE_ME_...>> placeholders of this formatted parameter
	 * @return String containing the parameter with <<REPLACE_ME_...>> placeholders replaced with the passed in values
	 */
	public String getFormattedParameter(String format, List<Object> formatVariableValues) {
		Matcher replaceMeMatcher = replaceMePattern.matcher(format);
		StringBuffer sb = new StringBuffer();
		for(Object variable : formatVariableValues) {
			if(replaceMeMatcher.find()) {
				replaceMeMatcher.appendReplacement(sb, variable.toString());
			}
		}
		replaceMeMatcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * A method to get the sub parameters of this parameter.
	 * An enumerated parameter has multiple values, and more
	 * than one of them could be used. Therefore, it's easiest
	 * to split a enumeration parameter into single-value
	 * sub-parameters to get different parameter combinations.
	 * 
	 * @return ParameterList of sub parameters
	 */
	public ParameterList getSubParameters() {
		ParameterList parameterList = new ParameterList();
		if (isEnumeration()) {
			Map<String, Object> parameterMap;
			List<String> toAdd;
			for (String str : getEnumerationValues()) {
				parameterMap = new HashMap<String, Object>();
				toAdd = new LinkedList<String>();
				toAdd.add(str);
				if (str.contains(REPLACE_STRING) || str.contains(REPLACE_INT) ||
						str.contains(REPLACE_DOUBLE)) {

					parameterMap.put("format", str);
					if (str.contains(REPLACE_STRING)) {
						parameterMap.put("type", String.class);
					} else if (str.contains(REPLACE_INT)) {
						parameterMap.put("type", Integer.class);
					} else {
						parameterMap.put("type", Double.class);
					}
				}
				parameterMap.put("enumerated values", toAdd);
				parameterList.addParameter(new Parameter(parameterMap));
			}
		} else {
			parameterList.addParameter(this);
		}
		return parameterList;
	}

	/**
	 * Method to represent this Parameter as a String. If this Parameter is an enumeration,
	 * this method returns the toString of the corresponding list of values. Otherwise, this
	 * method returns the format string of this Parameter.
	 * 
	 * @return String representing the Parameter
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

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof Parameter)) {
			return false;
		} else {
			return ("" + this).equals("" + obj);
		}
	}
}

class EnumParameter extends Parameter {
	// List
}