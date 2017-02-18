package contest.winter2017;

import java.util.ArrayList;
import java.util.List;

/**
 * A dummy test generator. This class encapsulates the example code IDT wrote
 * for security tests. It returns the same test every time for a given jar. The
 * returned test will be valid, however this test generator should only be used
 * in the event that no other test generators are functional.
 * 
 * @author ICT-2
 */
public class DummyTestGenerator extends TestGenerator {
	/**
	 * Constructs a dummy test generator with the given parameter factory and
	 * list of outputs.
	 * 
	 * @param parameterFactory
	 *            - parameter factory for this test generator.
	 * 
	 * @param outputs
	 *            - list of outputs encountered by any tests run outputs is
	 *            updated automatically by the tester
	 */
	public DummyTestGenerator(ParameterFactory parameterFactory, List<Output> outputs) {
		super(parameterFactory, outputs);
	}

	/**
	 * Gets the next test to be run.
	 * <p>
	 * Returns the same test every time for a given jar.
	 * 
	 * When a test is run, the associated Output object is added to the end of
	 * this class's outputs list via updateOutputs.
	 * 
	 * @return an array of objects which represent parameters to be tested.
	 */
	public Object[] nextTest() {
		/////////// START EXAMPLE CODE /////////////
		// This example demonstrates how to use the ParameterFactory to figure
		// out the parameter types of parameters
		// for each of the jars under test - this can be a difficult task
		// because of the concepts of fixed and
		// dependent parameters (see the explanation at the top of the
		// ParameterFactory class). As we figure out
		// what each parameter type is, we are assigning it a simple (dumb)
		// value so that we can use those parameters
		// to execute the black-box jar. By the time we finish this example, we
		// will have an array of concrete
		// parameters that we can use to execute the black-box jar.

		// start with a blank parameter list since we are going to start with
		// the first parameter
		List<String> previousParameterStrings = new ArrayList<String>();
		List<Parameter> potentialParameters = getParameterFactory().getNext(previousParameterStrings);
		Parameter potentialParameter;
		while (!potentialParameters.isEmpty()) {
			String parameterString = "";
			potentialParameter = potentialParameters.get(0);

			// if(potentialParameter.isOptional()) //TODO? - your team might
			// want to look at this flag and handle it as well!

			// an enumeration parameter is one that has multiple options
			if (potentialParameter.isEnumeration()) {
				// dumb logic - given a list of options, always use the first
				// one
				parameterString = potentialParameter.getEnumerationValues().get(0) + " ";

				// if the parameter has internal format (eg.
				// "<number>:<number>PM EST")
				if (potentialParameter.isFormatted()) {

					// loop over the areas of the format that must be replaced
					// and choose values
					List<Object> formatVariableValues = new ArrayList<Object>();
					for (Class<?> type : potentialParameter.getFormatVariables(parameterString)) {
						if (type == Integer.class) {
							// dumb logic - always use 1 for an integer
							formatVariableValues.add(new Integer(1));
						} else if (type == String.class) {
							// dumb logic - always use 'one' for an integer
							formatVariableValues.add(new String("one"));
						}
					}

					// build the formatted parameter string with the chosen
					// values (eg. 1:1PM EST)
					parameterString = potentialParameter.getFormattedParameter(parameterString, formatVariableValues);
				}
				previousParameterStrings.add(parameterString);
				// if it is not an enumeration parameter, it is either an
				// Integer, Double, or String
			} else {
				if (potentialParameter.getType() == Integer.class) {
					// dumb logic - always use '1' for an Integer
					parameterString = Integer.toString(1) + " ";
					previousParameterStrings.add(parameterString);
				} else if (potentialParameter.getType() == Double.class) {
					// dumb logic - always use '1.0' for a Double
					parameterString = Double.toString(1.0) + " ";
					previousParameterStrings.add(parameterString);
				} else if (potentialParameter.getType() == String.class) {

					// if the parameter has internal format (eg.
					// "<number>:<number>PM EST")
					if (potentialParameter.isFormatted()) {

						// loop over the areas of the format that must be
						// replaced and choose values
						List<Object> formatVariableValues = new ArrayList<Object>();
						for (Class<?> type : potentialParameter.getFormatVariables()) {
							if (type == Integer.class) {
								// dumb logic - always use '1' for an Integer
								formatVariableValues.add(new Integer(1));
							} else if (type == String.class) {
								// dumb logic - always use 'one' for a string
								formatVariableValues.add(new String("one"));
							}
						}

						// build the formatted parameter string with the chosen
						// values (eg. 1:1PM EST)
						parameterString = potentialParameter.getFormattedParameter(formatVariableValues);
					} else {
						// dumb logic - always use 'one' for a String
						parameterString = "one ";
					}

					previousParameterStrings.add(parameterString);
				} else {
					parameterString = "unknown type";
				}
			}
			// because of the challenge associated with dependent parameters, we
			// must go one parameter at a time, building up the parameter list -
			// getNext is the method that we are using to get the next set of
			// options, given an accumulating parameter list.
			potentialParameters = getParameterFactory().getNext(previousParameterStrings);
		}
		return previousParameterStrings.toArray();
	}
}
