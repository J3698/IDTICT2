package contest.winter2017;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Parameters used to execute jars are tricky things (think command line flags),
 * so we developed a ParameterFactory class to help you get the parameter types
 * needed to execute the given jar. Why are executable jar command line
 * parameters/arguments tricky? Because they can be fixed (static) or dependent
 * (dynamic).
 * 
 * Fixed parameters are fairly simple. When a jar simply takes a fixed
 * number/type of parameters as inputs (e.g. java -jar isXDivisibleByY.jar 100
 * 10), the order of those two inputs matters, and the types are fixed.
 * 
 * Dependent parameters occur when subsequent parameter types/values depend upon
 * previous types/values. Often times, there are multiple options at each of the
 * levels. For example, take the following command: java -jar
 * randomGenerator.jar --randomrange start=100 stop=1000 step=0.5 The first
 * argument (--randomrange) was one possibility from several options (--shuffle,
 * --randomint, or --sample). The second, third, and fourth arguments are a
 * result of selecting --randomrange (dependent upon the first parameter), and
 * they could be in any order.
 * 
 * ParameterFactory is our attempt to reduce the complexity related to dependent
 * parameters. Parameter definitions are built in an iterative manner: on each
 * iteration that it will return all of the potential options for that parameter
 * index (each time you call, you pass in the sum of previous selections to
 * build up the parameter definition dynamically). The method that we wrote to
 * help is called getNext(List<String> previousParameterValues);
 * 
 * YOU ARE WELCOME TO CHANGE THIS CLASS, INCLUDING THE APPROACH. KEEP IN MIND
 * THAT YOU CAN'T CHANGE THE EXISTING FORMAT IN THE BLACK-BOX JARS THOUGH.
 * 
 * @author IDT
 */
public class ParameterFactory {

	/**
	 * Input map associated with a test's inputs.
	 */
	@SuppressWarnings("rawtypes")
	private Map inputMap;

	/**
	 * Map to map a parameter to the parameters can come after it.
	 */
	private Map<String, Object> dependentParametersMap;

	/**
	 * Whether the jar to test takes a fixed parameter list.
	 */
	private boolean bounded;

	/**
	 * ctr for Parameter Factory class
	 * 
	 * @param inputMap
	 *            - input map that describes all of the parameter data
	 *            associated with an executable jar
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ParameterFactory(Map inputMap) {
		this.inputMap = inputMap;
		if (this.inputMap.get("fixed parameter list") != null) {
			this.bounded = true;
		} else {
			this.bounded = false;
		}
		this.dependentParametersMap = (Map) this.inputMap.get("dependent parameters");

		boolean debug = false;
		if (debug) {
			System.out.println("______DEBUG______");
			if (dependentParametersMap == null) {
				System.out.println("FIXED");
				printList((List) this.inputMap.get("fixed parameter list"));
			} else {
				System.out.println("DEPENDENT");
				printMap(dependentParametersMap, "$: ");
			}
			System.out.println("______DEBUG______");
		}
	}

	/**
	 * Prints a list for debeug purposes.
	 * <p>
	 * Prints maps in the list using printMap.
	 * 
	 * @param list
	 *            - list to print
	 */
	@SuppressWarnings("rawtypes")
	private void printList(List<?> list) {
		for (Object obj : list) {
			if (obj instanceof Map) {
				printMap((Map) obj, "$: ");
			} else {
				System.out.print(obj);
			}
		}
		System.out.println();
	}

	/**
	 * Prints a map for debeug purposes.
	 * 
	 * @param map
	 *            - map to print
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void printMap(Map map, String lvl) {

		for (Entry<Object, Object> e : (Set<Entry<Object, Object>>) map.entrySet()) {
			System.out.println(lvl + e.getKey());
			lvl = "   " + lvl;
			if (e.getValue() instanceof Map) {
				printMap((Map<Object, Object>) e.getValue(), lvl);
			} else if (e.getValue() instanceof Iterable) {
				System.out.println(e.getValue());
			} else {
				System.out.println(lvl + e.getValue());
			}
			lvl = lvl.substring(3);
		}

	}

	/**
	 * Method to test if the parameters associated with this jar are fixed (aka
	 * bounded)
	 * 
	 * @return true if the parameters are fixed (bounded) and false if they are
	 *         not
	 */
	public boolean isBounded() {
		return this.bounded;
	}

	/**
	 * Method to deal with the complexity of dependent parameters. Also handles
	 * fixed parameters. For more information about dependent and fixed
	 * parameters, see explanation at the top of this class. We are essentially
	 * determining the potential parameters for a given index, and that index is
	 * determined by the values in previous ParameterValues (hence, we call this
	 * iteratively and build the definition). This code is certainly fair game
	 * for change.
	 * 
	 * @param previousParameterValues
	 *            - since this method is used iteratively to build up the
	 *            parameter definitions, this is the accumulated parameters that
	 *            have been passed in until now
	 * @return List of Parameter objects containing all metadata known about the
	 *         each Parameter
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Parameter> getNext(List<String> previousParameterValues) {

		// ultimately we are returning all possible parameters for a given index
		// (since we could be dealing with dependent parameters
		// and enumeration parameters)
		List<Parameter> possibleParamsList = new ArrayList<Parameter>();

		StringBuffer sb = new StringBuffer();
		for (String paramString : previousParameterValues) {
			sb.append(" " + paramString);
		}
		String currentParamsString = sb.toString();

		// process all dependent parameters
		if (this.dependentParametersMap != null) {

			for (Map.Entry<String, Object> mapEntry : this.dependentParametersMap.entrySet()) {

				if (currentParamsString.matches(mapEntry.getKey())
						|| (mapEntry.getKey().isEmpty() && currentParamsString.isEmpty())) {
					Object obj = mapEntry.getValue();
					if (obj instanceof Map) {
						possibleParamsList.add(new Parameter((Map) mapEntry.getValue()));
					} else {
						for (Map paramMap : (List<Map>) obj) {
							possibleParamsList.add(new Parameter(paramMap));
						}
					}
				}
			}

			// if there are no dependent parameters, process the fixed
			// parameters
		} else {
			List fixedParamList = (List) this.inputMap.get("fixed parameter list");

			if (previousParameterValues.size() < fixedParamList.size()) {
				Map paramMap = (Map) fixedParamList.get(previousParameterValues.size());
				possibleParamsList.add(new Parameter(paramMap));
			}
		}

		// return the list of possible parameters for this index
		return possibleParamsList;
	}

	public Map<?, ?> getParameterMap() {
		return inputMap;
	}
}
