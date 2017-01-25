package contest.winter2017;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.omg.CORBA.PUBLIC_MEMBER;

/**	
 *	 Parameters used to execute jars are tricky things (think command line flags), so we developed a ParameterFactory 
 * 	 class to help you get the parameter types needed to execute the given jar. Why are executable jar command line 
 *	 parameters/arguments tricky? Because they can be fixed (static) or dependent (dynamic).
 *	
 *	 Fixed parameters are fairly simple. When a jar simply takes a fixed number/type of parameters as inputs 
 *	 (e.g. java -jar isXDivisibleByY.jar 100 10), the order of those two inputs matters, and the types are fixed.
 *	 
 * 	 Dependent parameters occur when subsequent parameter types/values depend upon previous types/values. Often times, there
 *	 are multiple options at each of the levels. For example, take the following command:
 *	 java -jar randomGenerator.jar --randomrange start=100 stop=1000 step=0.5
 *	 The first argument (--randomrange) was one possibility from several options (--shuffle, --randomint, or --sample). 
 *	 The second, third, and fourth arguments are a result of selecting --randomrange (dependent upon the first parameter), 
 *	 and they could be in any order.
 *	
 *	 ParameterFactory is our attempt to reduce the complexity related to dependent parameters. Parameter definitions are built 
 *   in an iterative manner: on each iteration that it will return all of the potential options for that parameter index (each 
 *   time you call, you pass in the sum of previous selections to build up the parameter definition dynamically). The method 
 *   that we wrote to help is called getNext(List<String> previousParameterValues);
 *   
 *   YOU ARE WELCOME TO CHANGE THIS CLASS, INCLUDING THE APPROACH. KEEP IN MIND THAT YOU CAN'T CHANGE THE EXISTING FORMAT IN THE 
 *   BLACK-BOX JARS THOUGH. 
 *  
 *   @author IDT
 */
public class ParameterFactory {

	/**
	 * input map associated with a tests inputs
	 */
	@SuppressWarnings("rawtypes")
	private Map inputMap;

	/**
	 * map to map a parameter to the parameters can come after it
	 */
	private Map<String, Object> dependentParametersMap;

	/**
	 * list possible inputs to the jar
	 */
	private List<ParameterList> possibleParamLists;

	/**
	 * if this jar takes a fixed parameter list
	 */
	private boolean bounded;
	
	
	/**
	 * ctr for Parameter Factory class
	 * @param inputMap - input map that describes all of the parameter data associated with an executable jar
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
		
		/* Testing Code */
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

	private void printList(List list) {
		for (Object obj : list) {
			if (obj instanceof Map) {
				printMap((Map)obj, "$: ");
			} else {
				System.out.print(obj);				
			}
		}
		System.out.println();
	}

	private void printMap(Map m, String lvl) {

		for (Entry<Object, Object> e: (Set<Entry<Object, Object>>) m.entrySet()) {
			System.out.println(lvl + e.getKey());
			lvl = "   " + lvl; 
			if (e.getValue() instanceof Map) {
				printMap((Map)e.getValue(), lvl);
			} else if (e.getValue() instanceof Iterable) {
				System.out.println(e.getValue());
			} else {
				System.out.println(lvl + e.getValue());
			}
			lvl = lvl.substring(3);
		}

	}


	/**
	 * Method to test if the parameters associated with this jar are fixed (aka bounded)
	 * @return true if the parameters are fixed (bounded) and false if they are not
	 */
	public boolean isBounded() {
		return this.bounded;
	}

	/**
	 * Method to deal with the complexity of dependent parameters. Also handles fixed parameters.
	 * For more information about dependent and fixed parameters, see explanation at the top of this
	 * class. We are essentially determining the potential parameters for a given index, and that index  
	 * is determined by Parameters in previousParameterValues (hence, we call this iteratively and build
	 * the definition).
	 * 
	 * @param previousParameterValues - since this method is used iteratively to build up the parameter
	 *        definitions, this is the accumulated parameters that have been passed in until now
	 * @return List of Parameter objects containing all metadata known about the each Parameter
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Parameter> getNext(ParameterList previousParameterValues) {
		
		// we are returning all possible parameters for a given index
		List<Parameter> possibleParamsList = new ArrayList<Parameter>();

		// keep track of used parameters
		Set<Parameter> oldParams = new HashSet<Parameter>();
		for (Parameter parameter : previousParameterValues) {
			oldParams.add(parameter);
		}

		// process all dependent parameters
		if (this.dependentParametersMap != null) {

			for (Map.Entry<String, Object> mapEntry : this.dependentParametersMap.entrySet()) {
				boolean validParam = false;

				// if this parameter is the first paramter of the parameter list
				if (mapEntry.getKey().isEmpty() && previousParameterValues.size() == 0) {
					validParam = true;
				} else {
					// if this parameter's key is in the parameter list
					for (Parameter p : previousParameterValues) {
						if (("" + p).matches(mapEntry.getKey())) {
							validParam = true;
							break;
						}
					}
				}

				// check if the one or more parameters already used
				if (validParam) {
					Object obj = mapEntry.getValue();
					if (obj instanceof Map) {
						Parameter p = new Parameter((Map) mapEntry.getValue());
						if (!oldParams.contains(p)) {
							possibleParamsList.add(new Parameter((Map) mapEntry.getValue()));
						}
					} else {
						for (Map paramMap : (List<Map>) obj) {
							Parameter p = new Parameter(paramMap);
							if (!oldParams.contains(p)) {
								possibleParamsList.add(new Parameter(paramMap));
							}
						}
					}
				}
			}

		// if there are no dependent parameters, process the fixed parameters
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

	/**
	 * Method to return a list of possible parameter lists.
	 * Currently ignores optional parameters.
	 * 
	 * @return list of possile parameter lists
	 */
	public List<ParameterList> possibleParamLists() {
		if (this.possibleParamLists != null) {
			return this.possibleParamLists;
		} else {
			this.possibleParamLists = new LinkedList<ParameterList>();
			LinkedList<ParameterList> toProcess = new LinkedList<ParameterList>();
			toProcess.add(new ParameterList());
			while (!toProcess.isEmpty()) {
				ParameterList curr = toProcess.removeFirst();
				List<Parameter> possibleParameters = getNext(curr);
				// add non-optional parameters
				boolean moreParams = false;
				for (Parameter parameter : possibleParameters) {
					if (!parameter.isOptional()) {
						moreParams = true;
						if (parameter.isEnumeration()) {
							for (Parameter subParameter: parameter.getSubParameters()) {
								curr.addParameter(subParameter);
							}
						} else {
							curr.addParameter(parameter);
						}
					}
				}
				// if there might be more parameters reprocess the list later
				if (moreParams) {
					toProcess.addLast(curr);
				} else {
					this.possibleParamLists.add(curr);
				}
			}

			return this.possibleParamLists;
		}
	}
}







